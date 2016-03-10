package rsc.khan

import java.io.File

import Types.Course
import org.json4s.JsonAST.JString
import org.json4s.JsonDSL._
import rsc.{Resource, ResourceElement}
import utils.Conversions._
import utils.Logger

class Factory(outputFolder: File) extends rsc.Factory[Course] {
  override def getOrFail(course: Course): Resource = {
    // Open course
    val doc = open(course.path)

    // TOC
    val tocEl = doc match {
      case layouts.toc.Normal(re) => re
    }

    // Metadata
    val extractedEl = doc match {
      case layouts.metadata.Normal(re) => re
    }
    val additionalEl = ResourceElement(Some(
      ("source" -> "khanacademy") ~ ("level" -> "fundamental") ~
      ("categories" -> course.parents)
    ), None, None)

    // Create name
    val title = extractedEl.oMetadata match {
      case Some(m) => m \\ "title" match {
        case JString(x) => x
      }
    }
    val name = (course.parents:::List(title)).map(normalize(_)).mkString("-")

    // Create resource
    Logger.info(s"$name: OK")
    val mergedEl = tocEl.mergeWith(extractedEl, additionalEl)
    val outputPath = outputFolder.getAbsolutePath + "/khan"
    mergedEl.resource(outputPath, name)
  }
}
