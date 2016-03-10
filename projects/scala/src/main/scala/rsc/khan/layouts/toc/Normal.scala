package rsc.khan.layouts.toc

import org.jsoup.nodes.Document
import rsc.{TOC, Node, LayoutExtractor, ResourceElement}
import utils.Conversions._

object Normal extends LayoutExtractor {
  override def getOrFail(doc: Document): ResourceElement = {
    // Get nodes
    val blockSel = "div.tutorial-overview-block div.tutorial-container"
    val nodes = l(doc.select(blockSel)) match {
      case chapterEls if !chapterEls.isEmpty => chapterEls.map(chapterEl => {
          // Chapter
          val chapSel = "div.tutorial-overview a.tutorial-title"
          val chapter = l(chapterEl.select(chapSel)) match {
            case x::Nil => text(x)
          }

          // Sections
          val secSel = "ul.progress-container > li.progress-item span.progress-title"
          val sections = l(chapterEl.select(secSel)) match {
            case sectionEls if !sectionEls.isEmpty => sectionEls.map(text(_))
          }

          // Create Node
          new Node(chapter, sections.map(new Node(_, Nil)))
        })
    }

    // Create resource element
    new ResourceElement(None, Some(List(new TOC(nodes))), None)
  }
}
