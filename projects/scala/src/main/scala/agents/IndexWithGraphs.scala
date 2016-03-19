package agents

import java.io.File

import org.json4s.native.JsonMethods._
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

      // Was it already indexed?
      val index = r.oIndexer match {
        case None => true
        case Some(indexer) => indexer match {
          case Indexer.Graph => false
          case _ => true
        }
      }


      val name = file.file.getAbsolutePath
      index match {
        case false => {
          Logger.info(s"Skipping: $name")
        }
        case true => {
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

          // todo: remove
          System.exit(1)
        }
      }
    })
  }
}
