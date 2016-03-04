package coursera.layouts

import org.json4s.JsonAST.JValue
import org.jsoup.nodes.Document
import rtoc.{LayoutExtractor, Node}

object Simple extends LayoutExtractor[(List[Node], Option[JValue])] {
  def unapply(doc: Document) = {
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

      // Create the extracted pairs
      val pairs = (nodes, None)

      Some(pairs)
    } catch {
      case e => None
    }
  }
}
