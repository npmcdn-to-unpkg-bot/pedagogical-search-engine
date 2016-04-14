package ws.autocomplete
import slick.jdbc.JdbcBackend._
import ws.autocomplete.fetcher.Jdbc
import ws.autocomplete.results.PublicResponse
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

  def search(t: String): Future[List[PublicResponse]] = {
    val text = t.trim()

    // Check the minimal conditions for the query to be valid
    if(text.size < 1 || text.size > 256) {
      Future.successful(Nil)
    } else {
      // Launch the search
      val sc = new SearchContext(text, minimum, maximum)
      val processed = Basic.process(fetcher, sc)

      // Return the results
      processed.map(formatters.Basic.format(_))
    }
  }
}
