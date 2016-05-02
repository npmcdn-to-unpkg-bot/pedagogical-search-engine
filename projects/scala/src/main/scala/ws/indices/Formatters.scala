package ws.indices

import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, ShortTypeHints}
import rsc.annotators.Annotator
import rsc.attributes.Candidate.Spotlight
import rsc.attributes.Level
import rsc.importers.Importer
import rsc.indexers.Indexer
import rsc.snippets
import ws.indices.response.QualityType

trait Formatters {
  implicit val formats = DefaultFormats ++ Seq(
    new EnumNameSerializer(QualityType),
    new EnumNameSerializer(snippets.Source)
  )
}
