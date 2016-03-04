package coursera.layouts

import Utils.Logger
import org.json4s.JsonAST.JValue
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import rtoc.Node

import scala.collection.JavaConverters._

object Simple {
  def unapply(doc: Document): Option[(List[Node], Option[JValue])] = {
    try {
      // Course label
      val label = l(doc.select("h1.course-name")) match {
        case e::Nil => e.text()
      }

      // For each Weekly-module
      val nodes = l(doc.select("div.rc-Syllabus > div.week-entry")).map(weekEntry => {
        // module title
        val title = l(weekEntry.select("p.module-name")) match {
          case module::Nil => module.text()
        }

        // module topics
        val topics = l(weekEntry.select("ol.week-topics > li")) match {
          case t @ x::xs => t.map(_.text())
        }

        // Create node
        new Node(title, topics.map(new Node(_, Nil)))
      })

      // Create metadata
      val metadata: Option[JValue] = None

      // Create the extracted pairs
      val pairs = (nodes, metadata)

      Some(pairs)
    } catch {
      case e => None
    }
  }

  def l(es: Elements): List[Element] = es.iterator().asScala.toList
}
