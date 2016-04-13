package ws.autocomplete

import java.util.concurrent.Executors

import slick.jdbc.JdbcBackend._
import ws.autocomplete.fetcher.Jdbc
import ws.autocomplete.results.Result
import ws.autocomplete.strategy.Basic
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class MysqlService {
  // Create the service itself
  val db = Database.forConfig("wikichimp.autocomplete.slick")
  val fetcher = new Jdbc(db)

  // The strategy tries to have the number of results in this window
  val maximum = 10
  val minimum = 5

  def search(text: String): Future[List[Result]] = {
    if(text.trim().size == 0) {
      // todo: discard the query
    }

    // Launch the search
    val sc = new SearchContext(text, minimum, maximum)
    val processed = Basic.process(fetcher, sc)

    // todo: remove this post-processing
    val postProcessed = processed.map {
      results => {
        //println(s"Completed: nbRes=${results.size}, ranking:")
        //results.map(r => println(r.prettyPrint()))
        results
      }
    }.recover({
      // If anything goes wrong
      case e => {
        println(s"failed: $text, reason: ${e.getMessage}")
        e.printStackTrace()
        Nil
      }
    })
    postProcessed
  }
}
