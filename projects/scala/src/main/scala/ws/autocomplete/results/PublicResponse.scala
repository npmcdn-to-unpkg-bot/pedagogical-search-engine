package ws.autocomplete.results

case class PublicResponse(label: String,
                          uri: String,
                          disambiguating: List[PublicResponse]) {}
