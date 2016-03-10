package rsc.safaribooks.layouts.toc

import org.jsoup.nodes.{Element, Document}
import rsc.{Node, TOC, ResourceElement, LayoutExtractor}
import utils.Conversions.{l, text}

object Simple extends LayoutExtractor {
  override def getOrFail(doc: Document): ResourceElement = {
    // TOC entries
    val entryEls = l(doc.select("div.toc_book > div")) match {
      case es if !es.isEmpty => es
    }

    // nodes
    val tuples = entryEls.map(entryEl => {
      val classes = l(entryEl.classNames())
      val level = classes.filter(c => c.startsWith("level")) match {
        case Nil => 0
        case s::Nil => s.replace("level", "").toInt
      }
      (entryEl, level)
    })

    def getSubNodes(l: List[(Element, Int)], level: Int, acc: List[Node]): (List[Node], List[(Element, Int)]) = {
      if(l.isEmpty) {
        (acc, Nil)
      } else if(l.head._2 < level) {
        (acc, l)
      } else {
        val currentLevel = l.head._2
        val currentEl = l.head._1

        val r = getSubNodes(l.tail, level + 1, Nil)
        val children = r._1
        val newL = r._2
        val newNode = new Node(text(currentEl), children)

        getSubNodes(newL, level, acc:::List(newNode))
      }
    }

    val nodes = getSubNodes(tuples, 0, Nil)._1

    // Create the resource-element
    new ResourceElement(None, Some(List(new TOC(nodes))), None)
  }
}
