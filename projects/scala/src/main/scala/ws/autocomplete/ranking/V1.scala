package ws.autocomplete.ranking

import ws.autocomplete.results.{Disambiguation, Redirect, Result, Title}

object V1 {
  def rank(results: List[Result], text: String): List[Result] = {
    results.groupBy(r => {
      val label = r match {
        case Disambiguation(_, label, _) => label
        case Redirect(label, _, _, _) => label
        case Title(label, _, _) => label
      }
      projectSize(label, text) // smallest lengths first
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
      case (acc, result) => (result match {
        case Disambiguation(uriA, _, _) => contains(acc, uriA)
        case Redirect(_, _, uriB, _) => contains(acc, uriB)
        case Title(_, uri, _) => contains(acc, uri)
      }) match {
        case true => acc
        case false => acc ::: List(result)
      }
    }
  }

  def projectSize(s: String, text: String): Int = math.min(s.size, text.size + 3)
  def contains(acc: List[Result], uri: String): Boolean = acc match {
    case Nil => false
    case head::tail => (head match {
      case Disambiguation(uri2, _, _) => uri2
      case Redirect(_, _, uri2, _) => uri2
      case Title(_, uri2, _) => uri2
    }).equals(uri) || contains(tail, uri)
  }
}
