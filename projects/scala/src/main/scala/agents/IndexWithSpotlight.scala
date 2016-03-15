package agents

import java.io.File

import org.json4s.native.JsonMethods._
import rsc.{Formatters, Resource}
import rsc.indexers.Indexer
import rsc.writers.Json
import utils.{Files, Logger, Settings}

object IndexWithSpotlight extends Formatters {
  def main(args: Array[String]): Unit = {

    val settings = new Settings()
    val threshold = 0.5 // Threshold on the Final-score of Spotlight

    // For each resource-file
    Files.explore(new File(settings.Resources.folder)).map(file => {
      // Parse it
      val json = parse(file.file)
      val r = json.extract[Resource]

      // Was it already indexed?
      val index = r.oIndexer match {
        case None => true
        case Some(indexer) => indexer match {
          case Indexer.StandardSpotlight => false
          case _ => false
        }
      }


      val name = file.file.getAbsolutePath
      index match {
        case false => {
          Logger.info(s"Skipping: $name")
        }
        case true => {
          new rsc.indexers.Spotlight(threshold).index(r) match {
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
