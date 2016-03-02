package scholarpedia

import java.io.File

import Utils.Logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import rtoc.{Node, Resource}
import scholarpedia.Types.Downloaded
import scala.collection.JavaConverters._

class Factory(pages: File) extends rtoc.Factory[Downloaded] {
  val utf8 = "UTF-8"
  val baseURL = "http://www.scholarpedia.org/"

  override def produceResources(article: Downloaded): List[Resource] = {
    // todo: remove
    Logger.debug(article.toString)

    // Parse the article
    val pageName = article.page.split("/").toList.reverse.head
    val path = pages.getAbsolutePath
    val articleFile = new File(s"$path/$pageName")
    val doc = Jsoup.parse(articleFile, utf8, baseURL)

    // Extract the informations
    try {
      val title = doc.select("#firstHeading").text()
      val authors = doc.select("#sp_authors .bold").iterator().asScala.map(_.text())

      def getNodes(ul: Element): List[Node] = {
        val ulChildren = ul.children().iterator().asScala.toList
        val lis = ulChildren.filter(_.tag().getName == "li")

        lis.map(li => {
          val liChildren = li.children().iterator().asScala.toList
          val label: String = liChildren.filter(_.tag().getName == "a") match {
            case x::Nil => x.select("span.toctext").first().text()
          }

          val children = liChildren.filter(_.tag().getName == "ul") match {
            case Nil => Nil
            case x::Nil => getNodes(x)
          }

          new Node(label, children)
        })
      }

      val rootUl = doc.select("#toc ul").iterator().asScala.toList match {
        case x::xs => x
      }

      val entries = getNodes(rootUl)

      // Create the resource
      val resource = new Resource(entries)
      resource::Nil
    } catch {
      // No resource was created
      case e => {
        e.printStackTrace()
        val name = article.label
        Logger.error(s"Cannot create resource: '$name'")
        Nil
      }
    }
  }
}
