package rsc.mit.layouts.title

import org.jsoup.nodes.Document
import rsc.extraction.LayoutExtractor
import utils.Conversions._

object Basic extends LayoutExtractor[String] {
  override def getOrFail(doc: Document): String = {
    l(doc.select("#course_title .title")) match {
      case e::Nil => e.text()
    }
  }
}
