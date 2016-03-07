package coursera.layouts

import org.jsoup.nodes.Document
import rsc.{TOC, ResourceElement, LayoutExtractor, Node}
import utils.Conversions.l

object Simple extends LayoutExtractor {
  def unapply(doc: Document): Option[ResourceElement] = {
    try {
      // For each Weekly-module
      val nodes = l(doc.select("div.rc-Syllabus > div.week-entry")) match {
        case weekEntries @ (xs::x) => weekEntries.map(weekEntry => {
          // module title
          val title = l(weekEntry.select("p.module-name")) match {
            case module::Nil => module.text()
            case modules => modules.head.text()
          }

          // module topics
          val topics = l(weekEntry.select("ol.week-topics > li")) match {
            case t @ x::xs => t.map(_.text())
          }

          // Create node
          new Node(title, topics.map(new Node(_, Nil)))
        })
      }

      Some(new ResourceElement(None, Some(List(new TOC(nodes))), None))
    } catch {
      case e => None
    }
  }
}
