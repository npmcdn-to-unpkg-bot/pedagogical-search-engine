package ws.autocomplete.results

case class Title(label: String,
                 uri: String,
                 in: Int,
                 available: Boolean)
  extends Result {
  override def prettyPrint(): String = s"t: $label ($uri: $in)"
  override def searchLabel(): String = label
  override def pageUri(): String = uri
  override def displayLabel(): String = label
  override def isAvailable(): Boolean = available
}
