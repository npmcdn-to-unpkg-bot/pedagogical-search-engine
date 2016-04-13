package ws.autocomplete.results

case class Disambiguation(uriA: String,
                          labelA: String,
                          bs: List[PageElement]) extends Result {
  override def prettyPrint(): String = {
    val t = new StringBuilder
    t ++= s"d: $labelA($uriA)"
    bs.map {
      case PageElement(uri, label, in) =>
        t ++= s"   $label ($uri: $in)"
    }
    t.toString()
  }
}
