package rsc.coursera.layouts.toc

import org.jsoup.nodes.Document
import rsc.extraction.LayoutExtractor
import rsc.toc.{Node, Toc}
import utils.Conversions.{l, textOf}

object Simple extends LayoutExtractor[Toc] {
  override def getOrFail(doc: Document): Toc = {
    // For each Weekly-module
    val nodes = l(doc.select("div.rc-Syllabus > div.week-entry")) match {
      case weekEntries @ (xs::x) => weekEntries.flatMap(weekEntry => {
        // module title
        val title = l(weekEntry.select("p.module-name")) match {
          case module::Nil => textOf(module)
          case modules => textOf(modules.head)
        }

        title match {
          case t if t.length > 0 => {
            // module topics
            val topics = l(weekEntry.select("ol.week-topics > li")) match {
              case t @ x::xs => t.flatMap(textOf(_) match {
                case t if t.length > 0 => List(t)
                case _ => Nil
              })
            }

            topics match {
              case ts if !ts.isEmpty => List(new Node(title, topics.map(new Node(_, Nil))))
              case _ => List(new Node(title, Nil))
            }
          }
          case _ => Nil
        }
      })
    }

    nodes match {
      case ns if !ns.isEmpty => new Toc(nodes)
    }
  }
}
