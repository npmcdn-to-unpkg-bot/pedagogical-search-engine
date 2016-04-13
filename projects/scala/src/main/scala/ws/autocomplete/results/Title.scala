package ws.autocomplete.results

case class Title(label: String, uri: String, in: Int) extends Result {
  override def prettyPrint(): String = s"t: $label ($uri: $in)"
}
