package rsc.safaribooks.layouts.metadata

import org.jsoup.nodes.Document
import rsc.extraction.LayoutExtractor
import rsc.safaribooks.Types.Metadata
import utils.Conversions.{l, textOf}

object Simple extends LayoutExtractor[Metadata] {
  override def getOrFail(doc: Document): Metadata = {
    // Meta-box
    val metaEls = l(doc.select("ul.metadatalist > li")) match {
      case lis if !lis.isEmpty => lis
    }

    // Title
    val title = l(doc.select("ul.metadatalist > li h3.book_title")) match {
      case e::Nil => textOf(e)
    }

    // Publisher
    val publisher = metaEls.filter(metaEl => textOf(metaEl).startsWith("Publisher: ")) match {
      case publiEl::Nil => textOf(publiEl).replace("Publisher: ", "")
    }

    // Create the metadata
    Metadata(title, publisher)
  }
}
