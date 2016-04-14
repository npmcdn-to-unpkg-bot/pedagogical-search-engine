package ws.autocomplete.formatters

import ws.autocomplete.results.{Disambiguation, PageElement, PublicResponse, Result}

object Basic extends Formatter {
  def format(results: List[Result]): List[PublicResponse] = {
    results.map {
      case result => result match {
        case Disambiguation(uriA, labelA, bs) => bs match {
          case one::Nil => {
            // If there is only one thing to disambiguate, flatten things
            val label = one.label
            val uri = one.uri
            PublicResponse(getNiceLabel(label, uri), uri, Nil)
          }
          case many => {
            val label = result.displayLabel()
            val uri = result.pageUri()
            val dis = bs.map {
              case PageElement(uri, label, _) => {
                PublicResponse(getNiceLabel(label, uri), uri, Nil)
              }
            }
            PublicResponse(getNiceLabel(label, uri), uri, dis)
          }
        }
        case _ => {
          val label = result.displayLabel()
          val uri = result.pageUri()
          PublicResponse(getNiceLabel(label, uri), uri, Nil)
        }
      }
    }
  }

  def getNiceLabel(label: String, uri: String)
  : String = {
    val l2 = label.replaceAll("[^a-zA-Z0-9]", "")
    val u2 = uri.replaceAll("%[A-Z0-9]{2}", "").replaceAll("[^a-zA-Z0-9]", "")
    // Replace the possible "disambiguation" suffix (case insensitive)
    val filtered = label.replaceAll("(?i)\\(disambiguation\\)", "").trim()
    l2.equals(u2) match {
      case true => filtered
      case false => s"$filtered ($uri)"
    }
  }
}
