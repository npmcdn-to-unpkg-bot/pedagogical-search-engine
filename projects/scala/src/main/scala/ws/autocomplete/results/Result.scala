package ws.autocomplete.results

trait Result {
  def prettyPrint(): String = toString
}
