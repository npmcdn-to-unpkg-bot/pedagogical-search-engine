package agents

import java.io.File

import org.json4s.native.JsonMethods._
import rsc.annotators.{Annotator, Standard}
import rsc.writers.Json
import rsc.{Formatters, Resource}
import spotlight.LazyWebService
import utils.{Files, Logger, Settings}

import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object Annotate extends Formatters {
  def main(args: Array[String]): Unit = {

    val settings = new Settings()
    val ws = new LazyWebService(settings.Spotlight.host, settings.Spotlight.port)

    // For each resource-file
    val futures: List[Future[Resource]] =
      Files.explore(new File(settings.Resources.folder)).flatMap(file => {

        val friendlyName = file.file.getAbsolutePath
        Logger.info(".. " + friendlyName)

      // Parse it
      val json = parse(file.file)
      val r = json.extract[Resource]

      // Has the resource been annotated already?
      val annotate = r.oAnnotator match {
        case Some(annotator) => annotator match {
          case Annotator.Standard => false
          case _ => true
        }
        case None => true
      }

      annotate match {
        case false => {
          Logger.info("Skipping: " + friendlyName)
          Nil
        }
        case true => {
          // Annotate it
          val future = Standard.annotate(r, ws)

          future onComplete {
            case Failure(t) => {
              Logger.error("Failed: " + friendlyName)
            }
            case Success(newR) => {
              // Write it
              Json.write(newR, Some(file.file.getAbsolutePath))
              Logger.info("OK: " + friendlyName)
            }
          }

          List(future)
        }
      }
    })


    val fs = Future.sequence(futures)

    fs onComplete {
      case Failure(t) => {
        Logger.error("Failed globally")

        // Create the web-service
        ws.shutdown
      }
      case Success(xl) => {
        Logger.info("Succeeded globally")

        // Create the web-service
        ws.shutdown
      }
    }

    Await.result(fs, 10 days)
  }
}
