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
import ws.indices.response.QualityType
import ws.indices.spraythings.FilterParameterType

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
    new EnumNameSerializer(PrettifierType),
    new EnumNameSerializer(FilterParameterType),
    new EnumNameSerializer(QualityType)
  ) + ShortTypeHints(List(classOf[Spotlight]))
}
