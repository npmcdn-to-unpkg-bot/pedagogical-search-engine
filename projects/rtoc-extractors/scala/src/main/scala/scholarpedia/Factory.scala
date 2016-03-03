package scholarpedia

import java.io.File

import Utils.Logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import rtoc.{Node, Resource}
import scholarpedia.Types.Downloaded
import scala.collection.JavaConverters._
import scala.util.hashing.MurmurHash3

class Factory(pages: File, outputFolder: File) extends rtoc.Factory[Downloaded](outputFolder) {
  val utf8 = "UTF-8"
  val baseURL = "http://www.scholarpedia.org/"

  override def produceResources(article: Downloaded): List[Resource] = {
    Logger.info("Processing " + article.toString)

    // Parse the article
    val pageName = article.page.split("/").toList.reverse.head
    val inPath = pages.getAbsolutePath
    val articleFile = new File(s"$inPath/$pageName")
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

      // Create the resource
      val entries = getNodes(rootUl)
      val outPath = outputFolder.getAbsolutePath
      val resource = new Resource(entries, newFile(article.href, s"$outPath/scholarpedia/"))
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

  def newFile(href: String, folder: String): File = {
    // (Re-)Create folder
    new File(folder).mkdir()

    // Create file name
    val url = s"scholarpedia$href"
    val hash = MurmurHash3.stringHash(url)
    val name = hash.toString

    // Create file
    val file = new File(s"$folder/$name.json")
    file.createNewFile()

    file
  }
}
