package ws.autocomplete

import slick.jdbc.JdbcBackend.Database
import ws.autocomplete.fetcher.Jdbc
import ws.autocomplete.strategy.Basic

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object mysqlService extends App {
  val db = Database.forConfig("wikichimp.autocomplete.slick")

  try {
    val text = "java language"

    if(text.trim().size == 0) {
      // todo: discard the query
    }

    // The strategy tries to have the number of results in this window
    val maximum = 10
    val minimum = 5

    // Launch the search
    val sc = new SearchContext(text, minimum, maximum)
    val fetcher = new Jdbc(db)
    val processed = Basic.process(fetcher, sc)

    // todo: remove this post-processing
    val postProcessed = processed.map {
      results => {
        println(s"Completed: nbRes=${results.size}, ranking:")
        results.map(r => println(r.prettyPrint()))
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
