package mit.layouts.table

import org.json4s.JValue
import org.jsoup.nodes.Document
import rtoc.{Node, LayoutExtractor}
import utils.Conversions._

object Topical extends LayoutExtractor[(List[Node], Option[JValue])] {
  override def unapply(doc: Document): Option[(List[Node], Option[JValue])] = {
    try {
      val table = l(doc.select(".maintabletemplate > table")) match {
        case x::Nil => x
      }

      // Table headers
      val headers = l(table.select("thead > tr > th")) match {
        case xs if xs.length > 0 =>
          // Only leaf elements
          xs.filter(x => x.children().size() == 0) match {
            case ys if ys.length > 0 => ys.map(e => normalize(e.text()))
        }
      }

      // Header with a "topics" label
      val topicLabels = List("topics")
      val columnIndex = headers.zipWithIndex.filter(p => topicLabels.contains(p._1)) match {
        case p::Nil => p._2
      }

      // Regular rows
      val rows = l(table.select("tbody > tr"))
        .map(row => l(row.select("td")))
          .filter(cells => cells.size == headers.size) match {
        case xs if xs.length > 0 => xs
      }

      // Nodes
      val nodes = rows.map(row => {
        val topic = row(columnIndex).text()
        new Node(topic, Nil)
      })

      Some((nodes, None))
    } catch {
      case e => None
    }
  }
}
