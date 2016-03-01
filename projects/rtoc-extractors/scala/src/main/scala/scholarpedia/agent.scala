package scholarpedia

import java.io.File

import Utils.Logger
import org.json4s._
import org.json4s.native.JsonMethods._
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import rtoc.Node
import scala.collection.JavaConverters._


object agent {
  def main(args: Array[String]): Unit = {
    val outputFolder = new File(args(0))

    // Check output-folder arg
    outputFolder.isDirectory match {
      case false => {
        val path = outputFolder.getPath
        Logger.error(s"Output path should be a valid folder: $path")
        System.exit(1)
      }
      case true => {
        val pre = outputFolder.getAbsolutePath
        val pages = new File(s"$pre/pages")
        val screenshots = new File(s"$pre/screenshots")
        val data = new File(s"$pre/data.json")

        // Check output-folder content
        (pages.isDirectory, screenshots.isDirectory, data.isFile) match {
          case (true, true, true) => extract(data, pages, screenshots)
          case _ => {
            if(!pages.isDirectory) {
              val path = pages.getPath
              Logger.error(s"No 'pages' folder found: $path")
            }
            if(!screenshots.isDirectory) {
              val path = pages.getPath
              Logger.error(s"No 'screenshots' folder found: $path")
            }
            if(!data.isFile) {
              val path = pages.getPath
              Logger.error(s"No 'data.json' file found: $path")
            }
            System.exit(1)
          }
        }
      }
    }


    // Define Articles
    case class Article(label: String, href: String, status: Option[String], page: Option[String])
    case class Downloaded(label: String, href: String, page: String)

    def extract(dataFile: File, pages: File, screenshots: File) = {
      // Parse data
      implicit val formats = DefaultFormats
      val data = parse(dataFile)

      // Extract each article
      val articles = data.extract[List[Article]]

      // Log articles without status
      articles.filter(a => a.status.isEmpty) match {
        case Nil => {}
        case xs => {
          val nb = xs.size
          Logger.warning(s"$nb do not have a status:")
          xs.map(a => Logger.warning(a.toString))
        }
      }

      // Filter downloaded articles
      val downloaded = articles.flatMap(a => a.page match {
        case None => Nil
        case Some(page) => List(Downloaded(a.label, a.href, page))
      })

      // Extract articles
      downloaded.take(1).map(process(_))

      def process(article: Downloaded) = {
        println(article)
        val utf8 = "UTF-8"
        val baseURL = "http://www.scholarpedia.org/"

        val pageName = article.page.split("/").toList.reverse.head
        val path = pages.getAbsolutePath
        val articleFile = new File(s"$path/$pageName")
        val doc = Jsoup.parse(articleFile, utf8, baseURL)

        val title = doc.select("#firstHeading").text()
        val authors = doc.select("#sp_authors .bold").iterator().asScala.map(_.text())

        def getNodes(ul: Element): List[Node] = {
          val ulChildren = ul.children().iterator().asScala.toList
          val lis = ulChildren.filter(_.tag().getName == "li")

          lis.map(li => {
            val liChildren = li.children().iterator().asScala.toList
            val label: String = liChildren.filter(_.tag().getName == "a") match {
              case Nil => "???"
              case x::Nil => x.select("span.toctext").first().text()
              case x::xs => "???"
            }

            val children = liChildren.filter(_.tag().getName == "ul") match {
              case Nil => Nil
              case x::Nil => getNodes(x)
              case x::xs => Nil
            }

            new Node(label, children)
          })
        }

        val entries = getNodes(doc.select("#toc ul").first()).map(println(_))

      }
    }
  }
}
