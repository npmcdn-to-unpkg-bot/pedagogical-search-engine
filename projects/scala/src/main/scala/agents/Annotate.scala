package agents

import rsc.Formatters
import spotlight.{Printer, WebService}
import utils.Settings

object Annotate extends Formatters {
  def main(args: Array[String]): Unit = {
    // Load the configuration file
    val settings = new Settings()

    // Create the web-service
    val webService = new WebService(settings.Spotlight.host, settings.Spotlight.port)
    val texts = List(
      "Priority queues are used in computer science algorithms"
    )
    val oSpots = webService.textsToSpots(texts)

    oSpots match {
      case None => println("No results")
      case Some(ls) => ls.zip(texts).map(p => {
        val text = p._2
        val spots = p._1
        spots.map(spot => println(Printer.printSpot(spot, text)))
      })
    }

    webService.shutdown
  }
}
