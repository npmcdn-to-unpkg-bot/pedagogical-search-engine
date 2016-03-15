package rsc

import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import rsc.annotators.Annotator
import rsc.attributes.{Level, Source}

trait Formatters {
  implicit val formats = DefaultFormats ++ Seq(
    new EnumNameSerializer(Source),
    new EnumNameSerializer(Level),
    new EnumNameSerializer(Annotator)
  )
}
