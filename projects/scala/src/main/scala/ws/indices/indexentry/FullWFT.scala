package ws.indices.indexentry

import rsc.attributes.Source.Source
import rsc.indexers.Index
import ws.indices.indexentry.EngineType.Engine
import ws.indices.snippet.Snippet

case class FullWFT(entryId: String,
                   sumScore: Double,
                   resourceId: String,
                   title: String,
                   source: Source,
                   url: String,
                   snippet: Snippet,
                   topIndices: List[Index])
  extends IndexEntry with FullEntry {
  override def engine: Engine = EngineType.WikichimpFT
}
