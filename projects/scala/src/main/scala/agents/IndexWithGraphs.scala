package agents

import java.io.File

import org.json4s.native.JsonMethods._
import rsc.annotators.Annotator
import rsc.indexers.{Graph, Indexer}
import rsc.writers.Json
import rsc.{Formatters, Resource}
import utils.{Files, Logger, Settings}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

object IndexWithGraphs extends Formatters {
  def main(args: Array[String]): Unit = {
    // todo: delete
    println("start")

    val settings = new Settings()

    // For each resource-file
    val futures = Files.explore(
      new File(settings.Resources.folder)).flatMap(file => {
      val name = file.file.getAbsolutePath
      val indexer = new Graph()

      try {
        // Parse it
        val json = parse(file.file)
        val r = json.extract[Resource]

        // Was it already annotated?
        val annotated = r.oAnnotator match {
          case None => false
          case Some(annotator) => annotator match {
            case Annotator.Standard => true
            case _ => false
          }
        }

        // Was it already indexed?
        val indexed = r.oIndexer match {
          case None => false
          case Some(i) => i match {
            case Indexer.Graph => false // todo: true
            case _ => false
          }
        }

        // Index it
        (annotated, indexed) match {
          case (false, _) => {
            Logger.info(s"Skipping - Resource not annotated: $name")
            Nil
          }
          case (_ ,true) => {
            Logger.info(s"Skipping - Resource already indexed: $name")
            Nil
          }
          case  _ => {
            Logger.info(s"Processing ${file.file.getAbsolutePath}")

            val future = indexer.index(r) andThen {
              case Failure(t) => {
                Logger.error(s"Error: $name")
                t.printStackTrace()
              }
              case Success(oNewR) => oNewR match {
                case Some(newR) => {
                  Json.write(newR, Some(file.file.getAbsolutePath))
                  Logger.info(s"OK: $name")
                }
                case None => {
                  Logger.error(s"Cannot index: $name")
                }
              }
            }
            List(future)
          }
        }
      } catch {
        case e => {
          Logger.info(s"Cannot parse: $name")
          Nil
        }
      }
    })

    val merged = Future.sequence(futures)
    merged onComplete {
      case Failure(_) => {
        Logger.info(s"Global Failure")
      }
      case Success(_) => {
        Logger.info(s"Global Success")
      }
    }

    Await.result(merged, 10 days)
  }
}
