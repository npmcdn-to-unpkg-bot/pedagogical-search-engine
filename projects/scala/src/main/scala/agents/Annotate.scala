package agents

import spotlight.WebService
import utils.Settings

object Annotate {
  def main(args: Array[String]): Unit = {
    // Load the configuration file
    val settings = new Settings()

    // Create the web-service
    val webService = new WebService(settings.Spotlight.host, settings.Spotlight.port)
    val spots = webService.textsToSpots(List(
      "Google mail is a public website.",
      "Obama lives in the White House in Washington"
    ))

    spots.map(s => s.map(println(_)))

    webService.shutdown
  }
}