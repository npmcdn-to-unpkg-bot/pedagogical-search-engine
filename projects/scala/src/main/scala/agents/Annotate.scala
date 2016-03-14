package agents

import java.io.File

import org.json4s.native.JsonMethods._
import rsc.annotators.Standard
import rsc.writers.Json
import rsc.{Formatters, Resource}
import spotlight.WebService
import utils.{Files, Settings}

object Annotate extends Formatters {
  def main(args: Array[String]): Unit = {

    val settings = new Settings()
    val webService = new WebService(settings.Spotlight.host, settings.Spotlight.port)

    // For each resource-file
    Files.explore(new File(settings.Resources.folder)).map(file => {
      // Parse it
      val json = parse(file.file)
      val r = json.extract[Resource]

      // todo: test that the resource has not yet been annotated

      // Annotate it
      val newR = Standard.annotate(r, webService)

      // Write it
      Json.write(newR, Some(file.file.getAbsolutePath))
    })

    // Create the web-service
    webService.shutdown
  }
}
