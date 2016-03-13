package rsc.scholarpedia

import org.jsoup.nodes.Element
import rsc.Resource
import rsc.Types.Nodes
import rsc.attributes.{Level, Source}
import rsc.scholarpedia.Types.Article
import rsc.toc.{Node, Toc}
import utils.Conversions._

class Factory extends rsc.extraction.Factory[Article] {

  override def getOrFail(article: Article): Resource = {
    // Parse the article
    val doc = openWeird(settings.Resources.Scholarpedia.pages, article.page)

    // Metadata
    val source = Source.Scholarpedia
    val title = l(doc.select("#firstHeading")) match {
      case e::Nil => e.text()
    }

    val level = Level.Expert
    val authors = l(doc.select("#sp_authors .bold")).map(_.text())
    val href = article.href

    // Toc
    val rootUl = l(doc.select("#toc ul")) match {
      case uls if uls.length > 0 => uls.head
    }
    val toc = Toc(getNodes(rootUl))

    Resource(source, title,
      oLevel = Some(level),
      oAuthors = Some(authors),
      oHref = Some(href))
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
