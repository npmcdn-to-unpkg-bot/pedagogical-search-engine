package khan

import java.io.File

import khan.Types.Course
import khan.layouts.metadata.Normal
import org.json4s.JsonAST.JString
import org.json4s.JsonDSL._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import rsc.{ResourceElement, Resource}
import utils.Logger
import utils.Conversions._

class Factory(outputFolder: File) extends rsc.Factory[Course] {
  val utf8 = "UTF-8"
  val baseURL = "http://www.coursera.com/"

  override def getOrFail(course: Course): Resource = {
    // Open course
    val doc = open(course.path)

    // Metadata
    val extractedEl = doc match {
      case Normal(re) => re
    }
    val additionalEl = ResourceElement(Some(
      ("source" -> "khanacademy") ~ ("level" -> "fundamental") ~
      ("categories" -> course.parents)
    ), None, None)

    val metaEl = extractedEl.mergeWith(additionalEl)

    // Create name
    val title = extractedEl.oMetadata match {
      case Some(m) => m \\ "title" match {
        case JString(x) => x
      }
    }
    val name = (course.parents:::List(title)).map(normalize(_)).mkString("-")

    // Create resource
    val outputPath = outputFolder.getAbsolutePath
    Logger.info(s"$name: OK")
    new Resource(
      metaEl.oMetadata,
      None,
      None,
      s"$outputPath/khan",
      name)
  }

  def open(path: String): Document = Jsoup.parse(new File(path), utf8, baseURL)
}
