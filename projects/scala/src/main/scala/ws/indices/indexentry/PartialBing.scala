package ws.indices.indexentry

import rsc.attributes.Source.Source
import ws.indices.indexentry.EngineType.Engine

case class PartialBing(entryId: String,
                       rank: Int,
                       source: Source)
  extends IndexEntry {
  override def engine: Engine = EngineType.Bing
}
