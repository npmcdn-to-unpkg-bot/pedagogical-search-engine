package ws.autocomplete.formatters

import ws.autocomplete.results.{PublicResponse, Result}

trait Formatter {
  def format(results: List[Result]): List[PublicResponse]
}
