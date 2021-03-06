package rsc.mit

import java.io.File

import org.jsoup.nodes.Document
import rsc.Resource
import rsc.Resource._
import rsc.attributes.{Level, Source, Title}
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

    Resource(
      source = source,
      title = Title(title),
      oLevel = Some(level),
      oHref = Some(href),
      oTocs = Some(toc::Nil))
  }

  def openPage(page: Page): Document = {
    val file = new File(page.localPath)
    val relativePath = file.getParentFile.getName + "/" + file.getName
    open(settings.Resources.Mit.pages + "/" + relativePath)
  }
}
