package rsc

import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, ShortTypeHints}
import rsc.annotators.Annotator
import rsc.attributes.Candidate.Spotlight
import rsc.attributes.{Level, Source}
import rsc.indexers.Indexer

trait Formatters {
  implicit val formats = DefaultFormats ++ Seq(
    new EnumNameSerializer(Source),
    new EnumNameSerializer(Level),
    new EnumNameSerializer(Annotator),
    new EnumNameSerializer(Indexer)
  ) + ShortTypeHints(List(classOf[Spotlight]))
}
