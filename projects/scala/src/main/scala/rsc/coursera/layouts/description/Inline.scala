package rsc.coursera.layouts.description

import org.jsoup.nodes.Document
import rsc.attributes.Description
import rsc.extraction.LayoutExtractor
import utils.Conversions.{textOf, l}

object Inline extends LayoutExtractor[Description] {
  override def getOrFail(doc: Document): Description = {
    l(doc.select("div.c-cd-section")) match {
      case sections if !sections.isEmpty => sections.filter(
        section => (
          l(section.children()).
            filter(e => e.tagName().equals("h2")).
            filter(e => textOf(e).toLowerCase.equals("about the course")).
            size == 1
          )
      ) match {
        case section :: Nil => {
          val t = l(section.children()).
            filterNot(e => e.tagName().equals("h2")).
            map(e => textOf(e)).
            mkString(" ")

          t match {
            case text if text.length > 0 => new Description(text)
          }
        }
      }
    }
  }
}

