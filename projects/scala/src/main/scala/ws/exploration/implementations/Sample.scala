package ws.exploration.implementations

import slick.jdbc.JdbcBackend._
import ws.exploration.statistics.{Printer, Statistics}
import ws.exploration.{Queries, UserRun}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object Sample extends App {

  val db = Database.forConfig("wikichimp.userStudy.exploration.slick")

  // Load all the data
  val studyData = for {
    clicks <- db.run(Queries.allClicks())
    messages <- db.run(Queries.allMessages())
    searches <- db.run(Queries.allSearches())
  } yield (clicks.toList, messages.toList, searches.toList)

  val ultimate = studyData.map {
    case (clicks, messages, searches) =>
      val runs = UserRun.generateFrom(clicks, messages, searches)

      // Filter the runs you are interested in
      // ..

      // Create the statistics & the printer
      val stat = new Statistics(runs, clicks, messages, searches)
      val printer = new Printer(stat)

      // Print some statistics
      println(s"Statistics on ${runs.size} runs")
      println(printer.clickCount())
  }

  Await.result(ultimate, Duration.Inf)
}
