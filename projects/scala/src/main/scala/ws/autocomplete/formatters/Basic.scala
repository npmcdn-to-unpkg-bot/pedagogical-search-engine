package ws.autocomplete.formatters

import ws.autocomplete.results.{Disambiguation, PageElement, PublicResponse, Result}

object Basic extends Formatter {
  def format(results: List[Result]): List[PublicResponse] = {
    val responses = results.map {
      case result => result match {
        case Disambiguation(uriA, labelA, bs, available) => bs match {
          case one::Nil =>
            // If there is only one thing to disambiguate, flatten things
            val label = one.label
            val uri = one.uri
            PublicResponse(label, getNiceLabel(label, uri), uri, available, Nil)

          case many =>
            val label = result.displayLabel()
            val uri = result.pageUri()
            val dis = bs.map {
              case PageElement(u, l, _, a) =>
                PublicResponse(label, getNiceLabel(l, u), u, a, Nil)
            }
            PublicResponse(label, getNiceLabel(label, uri), uri, available, dis)
        }
        case _ =>
          val label = result.displayLabel()
          val uri = result.pageUri()
          val available = result.isAvailable()
          PublicResponse(label, getNiceLabel(label, uri), uri, available, Nil)
      }
    }

    // Flattening could have introduced duplicates
    val filtered = responses.foldLeft(List[PublicResponse]()) {
      case (acc, response) =>
        val uris = acc.map(_.uri)
        uris.contains(response.uri) match {
          case true => acc
          case false => acc ::: List(response)
        }
    }

    filtered
  }

  def getNiceLabel(label: String, uri: String)
  : String = {
    val l2 = label.replaceAll("[^a-zA-Z0-9]", "")
    val u2 = uri.replaceAll("%[A-Z0-9]{2}", "").replaceAll("[^a-zA-Z0-9]", "")
    // Replace the possible "disambiguation" suffix (case insensitive)
    val filtered = label.replaceAll("(?i)\\(disambiguation\\)", "").trim()
    l2.equals(u2) match {
      case true => filtered
      case false =>
        val uriDecoded = java.net.URLDecoder.decode(uri, "UTF-8")
        s"$filtered ($uriDecoded)"
    }
  }
}
