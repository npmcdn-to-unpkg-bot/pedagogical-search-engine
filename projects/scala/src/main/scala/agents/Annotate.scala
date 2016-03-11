package agents

import spotlight.WebService
import utils.Settings

object Annotate {
  def main(args: Array[String]): Unit = {
    // Load the configuration file
    val settings = new Settings()

    // Create the web-service
    val webService = new WebService(settings.Spotlight.host, settings.Spotlight.port)
    val oSpots = webService.textsToSpots(List(
      "6 Heapsort\n  6.1 Heaps\n  6.2 Maintaining the heap property \n  6.3 Building a heap \n  6.4 The heapsort algorithm\n  6.5 Priority queues"
    ))

    oSpots match {
      case None => println("No results")
      case Some(spots) => spots.map(s => s.map(println(_)))
    }

    webService.shutdown
  }
}
