package ws.indices.indexentry
import ws.indices.indexentry.EngineType.Engine

case class PartialWikichimp(entryId: String,
                            sumScore: Double,
                            resourceId: String)
  extends IndexEntry {
  override def engine: Engine = EngineType.Wikichimp
}
