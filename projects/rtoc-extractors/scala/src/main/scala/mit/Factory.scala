package mit

import java.io.File

import mit.Types.{Page, Course}
import mit.layouts.table.Topical
import org.json4s.JsonDSL._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import rsc.{Resource}
import utils.Conversions._

class Factory(coursesFolder: File, outputFolder: File) extends rsc.Factory[Course](outputFolder) {
  val utf8 = "UTF-8"
  val baseURL = "http://ocw.mit.edu/"

  override def getOrFail(course: Course): Resource = {
    course.pages.filter(_.isCalendar) match {
      case calendar::Nil => {
        // parse
        val doc = openPage(calendar)

        doc match {
          case Topical(rElement) => {
            // metadata
            val metadata = ("level" -> "university") ~ ("href" -> course.href)

            // Create the resource
            new Resource(
              Some(metadata),
              rElement.oTocs,
              None,
              outputFolder.getAbsolutePath + "/mit",
              name(course.href))
          }
        }
      }
    }
  }

  def name(href: String): String = hash(s"mit$href")

  def openPage(page: Page): Document = {
    val path = coursesFolder.getAbsolutePath + page.localPath.replaceFirst("output/courses", "")
    val file = new File(path)
    Jsoup.parse(file, utf8, baseURL)
  }
}
