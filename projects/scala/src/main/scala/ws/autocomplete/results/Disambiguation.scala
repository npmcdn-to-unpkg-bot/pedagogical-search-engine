package ws.autocomplete.results

case class Disambiguation(uriA: String,
                          labelA: String,
                          bs: List[PageElement]) extends Result {
  override def prettyPrint(): String = {
    val t = new StringBuilder
    t ++= s"d: $labelA($uriA)"
    bs.map {
      case PageElement(uri, label, in) =>
        t ++= s"\n   $label ($uri: $in)"
    }
    t.toString()
  }
  override def searchLabel(): String = labelA
  override def pageUri(): String = uriA
  override def displayLabel(): String = labelA
}
