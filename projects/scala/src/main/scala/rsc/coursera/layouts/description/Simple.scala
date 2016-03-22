package rsc.coursera.layouts.description

import org.jsoup.nodes.Document
import rsc.attributes.Description
import rsc.extraction.LayoutExtractor
import utils.Conversions.{l, textOf}

object Simple extends LayoutExtractor[Description] {
  override def getOrFail(doc: Document): Description = {
    l(doc.select("div.about-container > div.rc-AboutBox > h2")) match {
      case h2::Nil => textOf(h2).toLowerCase match {
        case "about this course" => {
          l(doc.select("div.about-container > div.rc-AboutBox > p")) match {
            case paragraph::Nil => textOf(paragraph) match {
              case t if t.size > 0 => new Description(t)
            }
          }
        }
      }
    }
  }
}

