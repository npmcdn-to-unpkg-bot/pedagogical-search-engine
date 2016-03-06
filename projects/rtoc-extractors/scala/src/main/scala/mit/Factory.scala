package mit

import java.io.File

import mit.Types.{Page, Course}
import mit.layouts.table.Topical
import org.json4s.JsonDSL._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import rtoc.Types.{Nodes, Resources}
import rtoc.{Node, Resource, Syllabus}
import utils.Logger

import scala.util.hashing.MurmurHash3

class Factory(coursesFolder: File, outputFolder: File) extends rtoc.Factory[Course](outputFolder) {
  val utf8 = "UTF-8"
  val baseURL = "http://ocw.mit.edu/"

  override def produceResources(course: Course): Resources = {
    try {
      course.pages.filter(_.isCalendar) match {
        case calendar::Nil => {
          // parse
          val doc = openPage(calendar)

          doc match {
            case Topical(nodes, metadata) => {
              val syllabuses = List(Syllabus(nodes))

              // metadata
              val metadata = ("level" -> "university") ~ ("href" -> course.href)

              val resource = new Resource(syllabuses,
                metadata,
                outputFolder.getAbsolutePath + "/mit",
                name(course.href))
              List(resource)
            }
          }
        }
      }
    } catch {
      case e => Nil
    }
  }

  def name(href: String): String =
    MurmurHash3.stringHash(s"mit$href").toString

  def openPage(page: Page): Document = {
    val path = coursesFolder.getAbsolutePath + page.localPath.replaceFirst("output/courses", "")
    val file = new File(path)
    Jsoup.parse(file, utf8, baseURL)
  }
}
