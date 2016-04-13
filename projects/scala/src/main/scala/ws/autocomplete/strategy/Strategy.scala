package ws.autocomplete.strategy

import ws.autocomplete.SearchContext
import ws.autocomplete.fetcher.Fetcher
import ws.autocomplete.results.Result

import scala.concurrent.Future

trait Strategy {
  def process(getter: Fetcher, sc: SearchContext)
  : Future[List[Result]]
}
