package evaluation.manual.json

import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer

trait Formatters {
  implicit val formats = DefaultFormats ++ Seq(
    new EnumNameSerializer(Type)
  )
}

