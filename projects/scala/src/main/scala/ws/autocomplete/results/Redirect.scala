package ws.autocomplete.results

case class Redirect(labelA: String, labelB: String, uriB: String, inB: Int) extends Result {
  override def prettyPrint(): String = s"r: $labelA ($uriB: $inB)"
}
