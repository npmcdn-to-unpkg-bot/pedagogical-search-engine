package rsc.coursera

import java.io.File

import rsc.coursera.layouts.{Simple, Inline}
import utils.{Logger}
import utils.Conversions._
import Types.Course
import coursera.layouts.Simple
import org.json4s.JsonDSL._
import org.jsoup.Jsoup
import rsc.Types.{Nodes}
import rsc.{ResourceElement, Node, Resource}

class Factory(pages: File, outputFolder: File) extends rsc.Factory[Course] {
  val utf8 = "UTF-8"
  val baseURL = "http://www.coursera.com/"

  override def getOrFail(course: Course): Resource = {
    // Parse
    val doc = getPage(course)

    // Match the page layout
    val href = course.href
    doc match {
      case Simple(p) => {
        Logger.info(s"Simple-layout: $href")
        process(course, p)
      }
      case Inline(p) => {
        Logger.info(s"Inline-layout: $href")
        process(course, p)
      }
    }
  }

  def process(course: Course, element: ResourceElement): Resource = {
    // Create the metadata
    val partners = course.partner match {
      case Some(partner) => Some(partner::Nil)
      case None => None
    }
    val metadata1 =
      ("source" -> "coursera") ~ ("level" -> "university") ~
        ("title" -> course.label) ~ ("href" -> course.href) ~
        ("partners" -> partners) ~ ("miniature" -> course.localImg) ~
        ("screenshot" -> course.screenshot) ~ ("domain" -> course.domain) ~
        ("subdomain" -> course.subdomain)

    // Merge metadata
    val metadata = element.oMetadata match {
      case None => metadata1
      case Some(metadata2) => metadata2.merge(metadata1)
    }

    // Extend toc with domain, subdomain
    def addDomain(nodes: Nodes): Nodes = course.domain match {
      case Some(domain) => new Node(domain, nodes)::Nil
      case None => nodes
    }

    def addSubdomain(nodes: Nodes): Nodes = course.subdomain match {
      case Some(subdomain) => new Node(subdomain, nodes)::Nil
      case None => nodes
    }

    def completeNodes(nodes: Nodes): Nodes = addDomain(addSubdomain(nodes))

    // Create the resource
    val outPath = outputFolder.getAbsolutePath

    new Resource(
      Some(metadata),
      element.oTocs,
      None,
      s"$outPath/coursera",
      name(course)
    )
  }

  def name(course: Course): String = normalize(course.label) + "-" + hash(course.localPath)

  def getPage(course: Course) = {
    val name = course.localPath.split("/").toList.reverse.head
    val path = pages.getAbsolutePath
    val file = new File(s"$path/$name")
    Jsoup.parse(file, utf8, baseURL)
  }
}
