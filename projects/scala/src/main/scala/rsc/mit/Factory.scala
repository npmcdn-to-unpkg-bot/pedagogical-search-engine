package rsc.mit

import org.jsoup.nodes.Document
import rsc.Resource
import rsc.Resource._
import rsc.attributes.{Level, Source}
import rsc.mit.Types.{Course, Page}
import rsc.mit.layouts.table.Topical
import rsc.mit.layouts.title.Basic

class Factory extends rsc.extraction.Factory[Course] {

  override def getOrFail(course: Course): Resource = {
    // Toc
    val toc = course.pages.filter(_.isCalendar) match {
      case calendar::Nil => openPage(calendar) match {
        case Topical(x) => x
      }
    }

    // Metadata
    val source = Source.MIT
    val title = course.pages.filter(_.isHome) match {
      case home::Nil => openPage(home) match {
        case Basic(x) => x
      }
    }

    val level = Level.University
    val href = course.href

    Resource(source, title,
      oLevel = Some(level),
      oHref = Some(href),
      oTocs = Some(toc::Nil))
  }

  def openPage(page: Page): Document = openWeird(settings.Resources.Mit.pages, page.localPath)
}
