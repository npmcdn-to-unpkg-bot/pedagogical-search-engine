package rsc.mit.layouts.table

import org.jsoup.nodes.{Element, Document}
import rsc.{TOC, ResourceElement, Node, LayoutExtractor}
import utils.Conversions._
import utils.Logger

object Topical extends LayoutExtractor {
  override def getOrFail(doc: Document): ResourceElement = {
      val table = l(doc.select(".maintabletemplate > table")) match {
        case x::Nil => x
      }

      // Table headers
      val headers = l(table.select("thead > tr > th")) match {
        case xs if xs.length > 0 =>
          // Only leaf elements
          xs.filter(x => x.children().size() == 0) match {
            case ys if ys.length > 0 => ys.map(e => normalize(text(e)))
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
          .filter(cells => (cells.size == headers.size || cells.size == 1)) match {
        case xs if xs.length > 0 => xs
      }

      // Construct the toc
      val toc = constructToc(rows, columnIndex)

      new ResourceElement(None, Some(List(toc)), None)
  }

  def constructToc(rows: List[List[Element]], index: Int): TOC = {
    def subNodes(rows: List[List[Element]],
                 acc: List[Node]): (List[Node], List[List[Element]]) = rows match {
      // No more rows
      case Nil => (acc, Nil)
      // At least one more row
      case _ => rows.head match {
        // .. it's a header
        case _::Nil => (acc, rows)
        // .. or not
        case _ => {
          val label = text(rows.head(index))
          subNodes(rows.tail, acc:::List(new Node(label, Nil)))
        }
      }
    }
    def constructRec(rows: List[List[Element]], chapter: Option[String], acc: List[Node]):
    List[Node] = subNodes(rows, Nil) match {
      // No remaining rows
      case (nodes, Nil) => chapter match {
        case None => acc:::nodes
        case Some(c) => acc:::List(new Node(c, nodes))
      }
      // Some remaining rows
      case (nodes, remainingRows) => {
        val label = text(remainingRows.head(0))
        chapter match {
          case None => constructRec(remainingRows.tail, Some(label), acc:::nodes)
          case Some(c) => constructRec(remainingRows.tail, Some(label), acc:::List(new Node(c, nodes)))
        }
      }
    }
    new TOC(constructRec(rows, None, Nil))
  }
}
