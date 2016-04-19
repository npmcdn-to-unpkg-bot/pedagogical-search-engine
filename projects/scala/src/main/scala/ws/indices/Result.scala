package ws.indices

case class Result(resourceId: String,
                  entryId: String,
                  score: Double,
                  title: String,
                  typeCol: String,
                  href: String,
                  snippet: String) {
  def toPublicResponse() =
    PublicResponse(title, typeCol, href, snippet, score)

}
