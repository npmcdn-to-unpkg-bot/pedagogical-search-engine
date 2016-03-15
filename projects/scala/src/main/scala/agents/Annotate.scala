package agents

import java.io.File

import org.json4s.native.JsonMethods._
import rsc.annotators.{Annotator, Standard}
import rsc.writers.Json
import rsc.{Formatters, Resource}
import spotlight.WebService
import utils.{Logger, Files, Settings}

object Annotate extends Formatters {
  def main(args: Array[String]): Unit = {

    val settings = new Settings()
    val webService = new WebService(settings.Spotlight.host, settings.Spotlight.port)

    // For each resource-file
    Files.explore(new File(settings.Resources.folder)).map(file => {
      // Parse it
      val json = parse(file.file)
      val r = json.extract[Resource]

      // Has the resource been annotated already?
      val annotate = r.oAnnotator match {
        case Some(annotator) => annotator match {
          case Annotator.Standard => false
        }
        case None => true
      }

      val friendlyName = file.file.getAbsolutePath
      annotate match {
        case false => {
          Logger.info("Skipping: " + friendlyName)
        }
        case true => {
          // Annotate it
          Standard.annotate(r, webService) match {
            case None => {
              Logger.error("Failed: " + friendlyName)
            }
            case Some(newR) => {
              // Write it
              Json.write(newR, Some(file.file.getAbsolutePath))
              Logger.info("OK: " + friendlyName)
            }
          }
        }
      }
    })

    // Create the web-service
    webService.shutdown
  }
}
