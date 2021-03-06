package ws.indices.indexentry
import rsc.attributes.Source.Source
import ws.indices.indexentry.EngineType.Engine

case class PartialWikichimp(entryId: String,
                            sumScore: Double,
                            resourceId: String,
                            source: Source)
  extends IndexEntry {
  override def engine: Engine = EngineType.Wikichimp
}
