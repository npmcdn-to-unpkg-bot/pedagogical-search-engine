package ws.autocomplete.results

case class Disambiguation(uriA: String,
                          labelA: String,
                          bs: List[PageElement]) extends Result {}
