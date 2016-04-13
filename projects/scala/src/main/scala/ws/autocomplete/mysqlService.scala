package ws.autocomplete

import slick.jdbc.JdbcBackend.Database
import ws.autocomplete.fetcher.Jdbc
import ws.autocomplete.ranking.SizeFirst
import ws.autocomplete.results._
import ws.autocomplete.strategy.{Rebounce, Strategy}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object mysqlService extends App {

  val db = Database.forConfig("wikichimp.autocomplete.slick")

  try {
    val text = "order british"

    if(text.trim().size == 0) {
      // todo: discard the query
    }

    // The strategy tries to have the number of results in this window
    val maximum = 10
    val minimum = 5

    // Launch the search
    val sc = new SearchContext(text, minimum, maximum)
    val fetcher = new Jdbc(db)
    val processed = Rebounce.process(fetcher, sc)

    // todo: remove this post-processing
    val postProcessed = processed.map {
      results => {
        println(s"Completed: nbRes=${results.size}, ranking:")
        results.map(println(_))
      }
    }.recover({
      // If anything goes wrong
      case e => {
        println(s"failed: $text, reason: ${e.getMessage}")
        e.printStackTrace()
      }
    })
    Await.result(postProcessed, Duration.Inf)

  } finally db.close
}
