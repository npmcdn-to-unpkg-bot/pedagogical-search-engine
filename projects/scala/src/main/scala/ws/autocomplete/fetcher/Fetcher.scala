package ws.autocomplete.fetcher

import ws.autocomplete.SearchContext
import ws.autocomplete.results.Result

import scala.concurrent.Future

trait Fetcher {
  def getResults(sc: SearchContext): Future[List[Result]]
}
