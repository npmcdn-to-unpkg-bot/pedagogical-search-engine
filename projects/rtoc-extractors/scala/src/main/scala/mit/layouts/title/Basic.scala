package mit.layouts.title

import org.json4s.JsonAST.{JString, JField, JObject}
import org.jsoup.nodes.Document
import rsc.{ResourceElement, LayoutExtractor}
import utils.Conversions._

object Basic extends LayoutExtractor{
  override def getOrFail(doc: Document): ResourceElement = {
    val title = l(doc.select("#course_title .title")) match {
      case e::Nil => e.text()
    }
    val metadata = JObject(JField("title", JString(title)))
    new ResourceElement(Some(metadata), None, None)
  }
}
