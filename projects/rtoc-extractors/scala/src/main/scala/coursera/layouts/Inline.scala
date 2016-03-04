package coursera.layouts

import org.json4s.JsonAST.JValue
import org.jsoup.nodes.Document
import rtoc.{LayoutExtractor, Node}

object Inline extends LayoutExtractor[(List[Node], Option[JValue])] {
  def unapply(doc: Document) = {
    try {
      // Extract syllabus
      val es = doc.select("div.rc-CdpDetails > div.c-cd-section > h2")
      val p = l(es)
      val syllabus = l(es) match {
        case Nil => ???
        case headers => headers.map(e => (e, e.text().toLowerCase.trim)).
          filter(p => p._2 match {
            case "course syllabus" => true
            case _ => false
          }) match {
          case p::Nil => p._1.parent()
        }
      }

      val nodes = l(syllabus.select("ol > li")) match {
        case ls @ xs::x => ls.map(e => l(e.children()) match {
            // Only handle syllabus without depth
          case Nil => new Node(e.text(), Nil)
        })
      }

      Some((nodes, None))
    } catch {
      case e => None
    }
  }
}
