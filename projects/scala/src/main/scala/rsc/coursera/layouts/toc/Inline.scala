package rsc.coursera.layouts.toc

import org.jsoup.nodes.Document
import rsc.extraction.LayoutExtractor
import rsc.toc.{Node, Toc}
import utils.Conversions.{l, textOf}

object Inline extends LayoutExtractor[Toc] {
  override def getOrFail(doc: Document): Toc = {
    // Extract toc
    val tocEl = l(doc.select("div.rc-CdpDetails > div.c-cd-section > h2")) match {
      case headers if !headers.isEmpty =>
        headers.map(e => (e, textOf(e).toLowerCase)).
          filter(p => p._2 == "course syllabus") match {
        case p::Nil => p._1.parent()
      }
    }

    val nodes = l(tocEl.select("ol > li")) match {
      case ls @ xs::x => ls.flatMap(e => l(e.children()) match {
          // Only handle toc without depth
        case Nil => textOf(e) match {
          case t if t.length > 0 => List(new Node(t, Nil))
          case _ => Nil
        }
      })
    }

    nodes match {
      case ns if !ns.isEmpty => new Toc(nodes)
    }
  }
}
