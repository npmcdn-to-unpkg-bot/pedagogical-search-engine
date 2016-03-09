package safaribooks.layouts.metadata

import org.jsoup.nodes.Document
import rsc.{ResourceElement, LayoutExtractor}
import utils.Conversions.{l, text}
import org.json4s.JsonDSL._

object Simple extends LayoutExtractor {
  override def getOrFail(doc: Document): ResourceElement = {
    // Meta-box
    val metaEls = l(doc.select("ul.metadatalist > li")) match {
      case lis if !lis.isEmpty => lis
    }

    // Title
    val title = l(doc.select("ul.metadatalist > li h3.book_title")) match {
      case e::Nil => text(e)
    }

    // Publisher
    val publisher = metaEls.filter(metaEl => text(metaEl).startsWith("Publisher: ")) match {
      case publiEl::Nil => text(publiEl).replace("Publisher: ", "")
    }

    // Create the metadata
    val metadata = ("title" -> title) ~ ("publisher" -> publisher)

    // Create the resource-element
    new ResourceElement(Some(metadata), None, None)
  }
}
