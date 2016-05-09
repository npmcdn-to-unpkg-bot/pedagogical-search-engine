package ws.autocomplete.ranking

import ws.autocomplete.SearchContext
import ws.autocomplete.results.{Disambiguation, Redirect, Result, Title}

object SizeFirst extends  Ranking {
  def rank(results: List[Result], sc: SearchContext): List[Result] = {
    results.groupBy(r => {
      val label = r match {
        case Disambiguation(_, l, _, _) => l
        case Redirect(l, _, _, _, _) => l
        case Title(l, _, _, _) => l
      }
      projectSize(label, sc.text) // smallest lengths first
    }).toList.sortBy {
      case (length, _) => length
    }.flatMap {
      case (l, rs) => rs.sortBy {
        case Disambiguation(_, _, _, _) => Integer.MIN_VALUE // Disambiguations first
        case Redirect(_, _, _, in, _) => -in // max |In| then
        case Title(_, _, in, _) => -in
      }
    }.foldLeft(List[Result]()) {
      // Filter out duplicate "uri"s
      case (acc, result) =>
        result.isContainedIn(acc) match {
          case true => acc
          case false => acc ::: List(result)
        }
    }
  }

  def projectSize(s: String, text: String): Int = math.min(s.length, text.length + 3)
}
