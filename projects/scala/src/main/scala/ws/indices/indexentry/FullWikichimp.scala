package ws.indices.indexentry

import rsc.attributes.Source.Source
import ws.indices.indexentry.EngineType.Engine
import ws.indices.snippet.Snippet

case class FullWikichimp(entryId: String,
                         sumScore: Double,
                         resourceId: String,
                         title: String,
                         source: Source,
                         url: String,
                         snippet: Snippet,
                         topIndices: List[rsc.indexers.Index])
  extends FullEntry {
  override def engine: Engine = EngineType.Wikichimp
}
