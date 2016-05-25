package ws.exploration

import slick.jdbc.JdbcBackend._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object ExplorationAgent extends App {

  val db = Database.forConfig("wikichimp.userStudy.exploration.slick")

  // Load all the data
  val studyData = for {
    clicks <- db.run(Queries.allClicks())
    messages <- db.run(Queries.allMessages())
    searches <- db.run(Queries.allSearches())
  } yield (clicks, messages, searches)

  val ultimate = studyData.map {
    case (clicks, messages, searches) =>
      println(s"clicks: ${clicks.size}, messages: ${messages.size}, searches: ${searches.size}")
      //
      val runs = UserRun.generateFrom(clicks.toList, messages.toList, searches.toList)

      val filtered = FilterOut.requested(runs)

      filtered.map(println)

      println(s"${runs.size} -> ${filtered.size}")
  }

  Await.result(ultimate, Duration.Inf)
}
