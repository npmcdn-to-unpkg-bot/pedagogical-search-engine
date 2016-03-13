package agents

import java.io.File

import org.json4s.DefaultFormats
import rsc.{Formatters, Resource}
import spotlight.WebService
import utils.Settings
import org.json4s.native.JsonMethods._

object Annotate extends Formatters {
  def main(args: Array[String]): Unit = {
    // Load the configuration file
    val settings = new Settings()

    val file = new File("/media/redwd/data/master-thesis/resources/coursera/history of rock part one-0318335341.json")
    val json = parse(file)
    val resources = json.extract[Resource]

//    // Create the web-service
//    val webService = new WebService(settings.Spotlight.host, settings.Spotlight.port)
//    val oSpots = webService.textsToSpots(List(
//      "6 Heapsort\n  6.1 Heaps\n  6.2 Maintaining the heap property \n  6.3 Building a heap \n  6.4 The heapsort algorithm\n  6.5 Priority queues"
//    ))
//
//    oSpots match {
//      case None => println("No results")
//      case Some(spots) => spots.map(s => s.map(println(_)))
//    }
//
//    webService.shutdown
  }
}
