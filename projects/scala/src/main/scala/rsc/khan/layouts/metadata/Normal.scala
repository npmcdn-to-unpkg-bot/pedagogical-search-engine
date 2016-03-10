package rsc.khan.layouts.metadata

import org.jsoup.nodes.Document
import rsc.{ResourceElement, LayoutExtractor}
import utils.Conversions._
import org.json4s.JsonDSL._

object Normal extends LayoutExtractor {
  override def getOrFail(doc: Document): ResourceElement = {
    // title
    val title = l(doc.select("div.topic-info-inner h1.topic-title")) match {
      case e::Nil => text(e)
    }

    // href
    var url = l(doc.select("html head meta"))
      .filter(e => e.hasAttr("property"))
      .filter(e => e.attr("property") == "og:url") match {
      case e::Nil => e.attr("content")
    }
    var href = url.replace("https://www.khanacademy.org", "")

    // Create resource element
    val metadata = ("title" -> title) ~ ("href" -> href)
    ResourceElement(Some(metadata), None, None)
  }
}
