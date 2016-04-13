package ws.autocomplete.ranking

import ws.autocomplete.SearchContext
import ws.autocomplete.results.Result

trait Ranking {
  def rank(results: List[Result], sc: SearchContext): List[Result]
}
