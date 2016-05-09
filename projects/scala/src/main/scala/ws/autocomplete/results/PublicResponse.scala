package ws.autocomplete.results

case class PublicResponse(label: String,
                          uri: String,
                          available: Boolean,
                          disambiguating: List[PublicResponse]) {}
