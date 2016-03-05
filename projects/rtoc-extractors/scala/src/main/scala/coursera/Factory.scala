package coursera

import java.io.File

import Utils.Logger
import coursera.Types.Course
import coursera.layouts.{Inline, Simple}
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import org.jsoup.Jsoup
import rtoc.{Node, Resource}

import scala.util.hashing.MurmurHash3

class Factory(pages: File, outputFolder: File) extends rtoc.Factory[Course](outputFolder) {
  val utf8 = "UTF-8"
  val baseURL = "http://www.coursera.com/"

  override def produceResources(course: Course): List[Resource] = {
    // Parse
    val doc = parse(course)

    // Extract the informations
    try {
      // Match the page layout
      val href = course.href
      doc match {
        case Simple(p) => {
          Logger.info(s"Simple-layout: $href")
          produceResource(course, p)
        }
        case Inline(p) => {
          Logger.info(s"Inline-layout: $href")
          produceResource(course, p)
        }
        case _ => Nil
      }
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

  def produceResource(course: Course, pair: (List[Node], Option[JValue])): List[Resource] = {
    // Create the metadata
    val partners = course.partner match {
      case Some(partner) => partner::Nil
      case None => Nil
    }
    val metadata1 =
      ("source" -> "coursera") ~ ("level" -> "university") ~
        ("title" -> course.label) ~ ("href" -> course.href) ~
        ("partners" -> partners) ~ ("miniature" -> course.localImg) ~
        ("screenshot" -> course.screenshot) ~ ("domain" -> course.domain) ~
        ("subdomain" -> course.subdomain)

    // Merge metadata
    val metadata = pair._2 match {
      case None => metadata1
      case Some(metadata2) => metadata2.merge(metadata1)
    }

    // Extend toc with domain, subdomain
    def addDomain(nodes: List[Node]): List[Node] = course.domain match {
      case Some(domain) => new Node(domain, nodes)::Nil
      case None => nodes
    }

    def addSubdomain(nodes: List[Node]): List[Node] = course.subdomain match {
      case Some(subdomain) => new Node(subdomain, nodes)::Nil
      case None => nodes
    }

    def completeNodes(nodes: List[Node]): List[Node] = addDomain(addSubdomain(nodes))

    val toc = completeNodes(pair._1)

    // Create the resource
    val outPath = outputFolder.getAbsolutePath
    val resource = new Resource(
      toc,
      metadata,
      s"$outPath/coursera",
      name(course.href)
    )
    resource::Nil
  }

  def name(href: String): String =
    MurmurHash3.stringHash(s"coursera$href").toString

  def parse(course: Course) = {
    val name = course.localPath.split("/").toList.reverse.head
    val path = pages.getAbsolutePath
    val file = new File(s"$path/$name")
    Jsoup.parse(file, utf8, baseURL)
  }
}