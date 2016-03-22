package rsc.khan.layouts.metadata

import org.jsoup.nodes.Document
import rsc.extraction.LayoutExtractor
import rsc.khan.Types.Metadata
import utils.Conversions._

object Normal extends LayoutExtractor[Metadata] {
  override def getOrFail(doc: Document): Metadata = {
    // title
    val title = l(doc.select("div.topic-info-inner h1.topic-title")) match {
      case e::Nil => textOf(e)
    }

    // href
    var url = l(doc.select("html head meta"))
      .filter(e => e.hasAttr("property"))
      .filter(e => e.attr("property") == "og:url") match {
      case e::Nil => e.attr("content")
    }
    var href = url.replace("https://www.khanacademy.org", "")

    Metadata(title, href)
  }
}
