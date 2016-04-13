package ws.autocomplete.strategy

import ws.autocomplete.fetcher.Fetcher
import ws.autocomplete.SearchContext
import ws.autocomplete.query.Queries
import ws.autocomplete.ranking.SizeFirst
import ws.autocomplete.results.Result

import scala.concurrent.Future

object Rebounce extends Strategy {
  def process(fetcher: Fetcher, sc: SearchContext)
  : Future[List[Result]] = fetcher.getResults(sc).flatMap(results => {
    // Rank the results
    val ranked = SizeFirst.rank(results, sc)
    val truncated = ranked.take(sc.maxRes)

    // "Rebounce" if the results too few
    val completed = (ranked.size >= sc.minRes) match {
      case true => Future.successful(truncated)
      case false => {
        // Not enough results
        val gap = sc.minRes - ranked.size

        // Can we hack to get more results?
        sc.text.split(' ').map(_.trim).filter(_.size > 0).toList match {
          case Nil => Future.successful(truncated) // Nothing was typed?
          case one::Nil => Future.successful(truncated) // Nothing clever to do with only one word
          case firstWord::more => {
            // todo
            ???
          }
        }
      }
    }
    completed
  })
}
