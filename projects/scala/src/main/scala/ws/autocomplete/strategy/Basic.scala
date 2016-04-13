package ws.autocomplete.strategy

import ws.autocomplete.SearchContext
import ws.autocomplete.fetcher.Fetcher
import ws.autocomplete.ranking.SizeFirst
import ws.autocomplete.results.Result

import scala.concurrent.Future

object Basic extends Strategy {
  def process(fetcher: Fetcher, sc: SearchContext)
  : Future[List[Result]] = {
    // Get the execution context
    implicit val ec = fetcher._ec

    // Perform the search
    fetcher.getResults(sc).flatMap(results => {
      // Rank the results
      val ranked = SizeFirst.rank(results, sc)
      Future.successful(ranked.take(sc.maxRes))
    })
  }
}
