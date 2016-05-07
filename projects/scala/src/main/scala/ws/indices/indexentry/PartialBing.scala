package ws.indices.indexentry

import ws.indices.indexentry.EngineType.Engine

case class PartialBing(entryId: String,
                       rank: Int)
  extends IndexEntry {
  override def engine: Engine = EngineType.Bing
}
