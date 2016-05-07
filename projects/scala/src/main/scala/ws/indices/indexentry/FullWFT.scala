package ws.indices.indexentry

import ws.indices.enums.WebsiteSourceType.WebsiteSource
import ws.indices.snippet.Snippet

case class FullWFT(entryId: String,
                   sumScore: Double,
                   resourceId: String,
                   title: String,
                   source: WebsiteSource,
                   url: String,
                   snippet: Snippet)
  extends IndexEntry with FullEntry {

}
