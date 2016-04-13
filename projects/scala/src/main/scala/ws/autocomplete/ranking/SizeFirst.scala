package ws.autocomplete.ranking

import ws.autocomplete.SearchContext
import ws.autocomplete.results.{Disambiguation, Redirect, Result, Title}

object SizeFirst extends  Ranking {
  def rank(results: List[Result], sc: SearchContext): List[Result] = {
    results.groupBy(r => {
      val label = r match {
        case Disambiguation(_, label, _) => label
        case Redirect(label, _, _, _) => label
        case Title(label, _, _) => label
      }
      projectSize(label, sc.text) // smallest lengths first
    }).toList.sortBy {
      case (length, _) => length
    }.flatMap {
      case (l, rs) => rs.sortBy {
        case Disambiguation(_, _, _) => Integer.MIN_VALUE // Disambiguations first
        case Redirect(_, _, _, in) => -in // max |In| then
        case Title(_, _, in) => -in
      }
    }.foldLeft(List[Result]()) {
      // Filter out duplicate "uri"s
      case (acc, result) => result.isContainedIn(acc) match {
        case true => acc
        case false => acc ::: List(result)
      }
    }
  }

  def projectSize(s: String, text: String): Int = math.min(s.size, text.size + 3)
}
