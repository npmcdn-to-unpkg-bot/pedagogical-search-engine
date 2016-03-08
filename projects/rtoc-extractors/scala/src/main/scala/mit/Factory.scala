package mit

import java.io.File

import mit.Types.{Page, Course}
import mit.layouts.table.Topical
import mit.layouts.title.Basic
import org.json4s.JsonDSL._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import rsc.{ResourceElement, Resource}
import utils.Conversions._
import utils.{Conversions, Logger}

class Factory(coursesFolder: File, outputFolder: File) extends rsc.Factory[Course] {
  val utf8 = "UTF-8"
  val baseURL = "http://ocw.mit.edu/"

  override def getOrFail(course: Course): Resource = {
    // TOC
    val tocEl = course.pages.filter(_.isCalendar) match {
      case calendar::Nil => openPage(calendar) match {
        case Topical(x) => x
      }
    }

    // Title
    val titleEl = course.pages.filter(_.isHome) match {
      case home::Nil => openPage(home) match {
        case Basic(x) => x
      }
    }

    // Additional metadata
    val metadata = ("level" -> "university") ~ ("href" -> course.href) ~
      ("source" -> "mit-ocw")
    val metaEl = ResourceElement(Some(metadata), None, None)

    // Merge elements
    val mergedEl = tocEl.mergeWith(titleEl, metaEl)

    // Create resource
    mergedEl.resource(outputFolder.getAbsolutePath + "/mit", name(course))
  }

  def name(course: Course): String = normalize(course.courseNumber) + "-" + hash(course.uniqueName)

  def openPage(page: Page): Document = {
    val path = coursesFolder.getAbsolutePath + page.localPath.replaceFirst("output/courses", "")
    val file = new File(path)
    Jsoup.parse(file, utf8, baseURL)
  }
}
