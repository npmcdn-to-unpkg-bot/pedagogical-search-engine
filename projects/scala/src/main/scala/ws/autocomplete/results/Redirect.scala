package ws.autocomplete.results

case class Redirect(labelA: String, labelB: String, uriB: String, inB: Int) extends Result {}
