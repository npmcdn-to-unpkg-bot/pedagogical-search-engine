package ws.autocomplete.fetcher

import ws.autocomplete.SearchContext
import ws.autocomplete.results.Result

import scala.concurrent.{ExecutionContext, Future}

trait Fetcher {
  val _ec: ExecutionContext
  def getResults(sc: SearchContext): Future[List[Result]]
}
