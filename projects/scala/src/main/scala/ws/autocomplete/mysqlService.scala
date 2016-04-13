package ws.autocomplete

import slick.jdbc.JdbcBackend.Database
import ws.autocomplete.ranking.V1
import ws.autocomplete.results._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object mysqlService extends App {

  val db = Database.forConfig("wikichimp.autocomplete.slick")

  try {
    val text = "order british"

    // The strategy tries to have the number of results in this window
    val maximum = 10
    val minimum = 5

    // Launch the search
    val future = db.run(Queries.getAction(text, maximum).map(results => {
      val ranked = V1.rank(results.toList, text)
      val completed = (ranked.size >= minimum) match {
        case true => ranked.take(maximum)
        case false => {
          ???
        }
      }

      println(s"Completed: nbRes=${completed.size}, ranking:")
      ranked.map(println(_))
    })).recover({
      // If anything goes wrong
      case e => {
        println(s"failed: $text, reason: ${e.getMessage}")
        e.printStackTrace()
      }
    })

    Await.result(future, Duration.Inf)
  } finally db.close
}
