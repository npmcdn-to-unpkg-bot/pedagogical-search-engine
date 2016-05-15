package rsc

import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, ShortTypeHints}
import rsc.annotators.Annotator
import rsc.attributes.Candidate.Spotlight
import rsc.attributes.{Level, PointerNameType}
import rsc.importers.Importer
import rsc.indexers.Indexer
import rsc.prettifier.PrettifierType
import ws.indices.indexentry.EngineType

trait Formatters {
  implicit val formats = DefaultFormats ++ Seq(
    new EnumNameSerializer(attributes.Source),
    new EnumNameSerializer(Level),
    new EnumNameSerializer(Annotator),
    new EnumNameSerializer(Indexer),
    new EnumNameSerializer(Importer),
    new EnumNameSerializer(snippets.Source),
    new EnumNameSerializer(snippets.Snippetizer),
    new EnumNameSerializer(EngineType),
    new EnumNameSerializer(PointerNameType),
    new EnumNameSerializer(PrettifierType)
  ) + ShortTypeHints(List(classOf[Spotlight]))
}
