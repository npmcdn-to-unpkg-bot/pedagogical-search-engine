package coursera

import java.io.File

import Utils.Logger
import coursera.Types.Course
import org.json4s.JsonDSL._
import org.jsoup.Jsoup
import rtoc.Resource

import scala.util.hashing.MurmurHash3

class Factory(pages: File, outputFolder: File) extends rtoc.Factory[Course](outputFolder) {
  val utf8 = "UTF-8"
  val baseURL = "http://www.coursera.com/"

  override def produceResources(course: Course): List[Resource] = {
    Logger.info("Processing " + course.toString)

    // Parse
    val doc = parse(course)

    // Extract the informations
    try {
      // Metadata
      val metadata =
        ("title" -> course.label)

      // Create the resource
      val outPath = outputFolder.getAbsolutePath
      val resource = new Resource(
        Nil,
        metadata,
        s"$outPath/coursera",
        name(course.href)
      )
      // todo: Produce a more complete resource
      // resource::Nil

      Nil
    } catch {
      // No resource was created
      case e => {
        e.printStackTrace()
        val name = course.label
        Logger.error(s"Cannot create resource: '$name'")
        Nil
      }
    }
  }

  def name(href: String): String =
    MurmurHash3.stringHash(s"scholarpedia$href").toString

  def parse(course: Course) = {
    val name = course.localPath.split("/").toList.reverse.head
    val path = pages.getAbsolutePath
    val file = new File(s"$path/$name")
    Jsoup.parse(file, utf8, baseURL)
  }
}
