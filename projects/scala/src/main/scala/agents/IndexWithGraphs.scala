package agents

import java.io.File

import org.json4s.native.JsonMethods._
import rsc.annotators.Annotator
import rsc.indexers.{Graph, Indexer}
import rsc.writers.Json
import rsc.{Formatters, Resource}
import utils.{Files, Logger, Settings}

object IndexWithGraphs extends Formatters {
  def main(args: Array[String]): Unit = {

    val settings = new Settings()

    // For each resource-file
    Files.explore(new File(settings.Resources.folder)).map(file => {
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
        case Some(indexer) => indexer match {
          case Indexer.Graph => true
          case _ => false
        }
      }


      val name = file.file.getAbsolutePath
      (annotated, indexed) match {
        case (false, _) => {
          Logger.info(s"Skipping - Resource not annotated: $name")
        }
        case (_ ,true) => {
          Logger.info(s"Skipping - Resource already indexed: $name")
        }
        case  _ => {
          Logger.info(s"Processing ${file.file.getAbsolutePath}")

          new Graph().index(r) match {
            case None => {
              Logger.error(s"Cannot index: $name")
            }
            case Some(newR) => {
              Json.write(newR, Some(file.file.getAbsolutePath))
              Logger.info(s"OK: $name")
            }
          }
        }
      }
    })
  }
}
