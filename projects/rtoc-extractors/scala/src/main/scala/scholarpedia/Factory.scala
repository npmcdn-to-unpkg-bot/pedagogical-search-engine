package scholarpedia

import java.io.File

import utils.Conversions._
import org.json4s.JsonDSL._
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import rsc.Types.{Metadata, Nodes}
import rsc.{TOC, Node, Resource}
import scholarpedia.Types.Article

import scala.collection.JavaConverters._

class Factory(pages: File, outputFolder: File) extends rsc.Factory[Article](outputFolder) {
  val utf8 = "UTF-8"
  val baseURL = "http://www.scholarpedia.org/"

  override def getOrFail(article: Article): Resource = {
      // Parse the article
      val doc = getPage(article)

      // Metadata
      val title = l(doc.select("#firstHeading")) match {
        case e::Nil => e.text()
      }
      val authors = l(doc.select("#sp_authors .bold")).map(_.text())
      val metadata: Metadata =
        ("title" -> title) ~ ("authors" -> authors) ~
          ("source" -> "scholarpedia") ~ ("level" -> "expert") ~
          ("href" -> article.href)

      // TOC
      val rootUl = l(doc.select("#toc ul")) match {
        case uls if uls.length > 0 => uls.head
      }
      val toc = TOC(getNodes(rootUl))

      // Create the resource
      val outPath = outputFolder.getAbsolutePath
      new Resource(
        Some(metadata),
        Some(List(toc)),
        None,
        s"$outPath/scholarpedia",
        name(article.href)
      )
  }

  def name(href: String): String = hash(s"scholarpedia$href")

  def getPage(article: Article) = {
    val name = article.page.split("/").toList.reverse.head
    val path = pages.getAbsolutePath
    val file = new File(s"$path/$name")
    Jsoup.parse(file, utf8, baseURL)
  }

  def getNodes(ul: Element): Nodes = {
    val ulChildren = l(ul.children())
    ulChildren.filter(_.tag().getName == "li") match {
        // Get <li> entries
      case lis if lis.size > 0 => lis.map(li => {
        // Get its label
        val label: String = l(li.children()).filter(_.tag().getName == "a") match {
          case link::Nil => l(link.select("span.toctext")) match {
            case label::Nil => label.text()
          }
        }

        // Get its sub-entries
        val children = l(li.children()).filter(_.tag().getName == "ul") match {
          case Nil => Nil
          case x::Nil => getNodes(x)
        }

        // Create toc-node
        new Node(label, children)
      })
    }
  }
}
