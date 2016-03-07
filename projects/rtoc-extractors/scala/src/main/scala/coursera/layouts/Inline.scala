package coursera.layouts

import org.jsoup.nodes.Document
import rsc.{TOC, ResourceElement, LayoutExtractor, Node}
import utils.Conversions._

object Inline extends LayoutExtractor {
  override def getOrFail(doc: Document): ResourceElement = {
    // Extract toc
    val tocEl = l(doc.select("div.rc-CdpDetails > div.c-cd-section > h2")) match {
      case headers if !headers.isEmpty =>
        headers.map(e => (e, normalize(e.text()))). filter(p => p._2 =="course syllabus") match {
        case p::Nil => p._1.parent()
      }
    }

    val nodes = l(tocEl.select("ol > li")) match {
      case ls @ xs::x => ls.map(e => l(e.children()) match {
          // Only handle toc without depth
        case Nil => new Node(e.text(), Nil)
      })
    }

    new ResourceElement(None, Some(List(new TOC(nodes))), None)
  }
}
