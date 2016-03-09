package safaribooks.layouts.toc

import org.jsoup.nodes.Document
import rsc.{Node, TOC, ResourceElement, LayoutExtractor}
import utils.Conversions.{l, text}

object Simple extends LayoutExtractor {
  override def getOrFail(doc: Document): ResourceElement = {
    // TOC entries
    val entries = l(doc.select("div.toc_book > div")) match {
      case es if !es.isEmpty => es.map(text(_))
    }

    // nodes
    val nodes = entries.map(new Node(_, Nil))

    // Create the resource-element
    new ResourceElement(None, Some(List(new TOC(nodes))), None)
  }
}
