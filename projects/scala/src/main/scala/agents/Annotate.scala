package agents

import java.io.File

import org.json4s.native.JsonMethods._
import rsc.Types._
import rsc.writers.Json
import rsc.{Resource, Formatters}
import spotlight.WebService
import utils.{Files, Settings}

object Annotate extends Formatters {
  def main(args: Array[String]): Unit = {

    val settings = new Settings()
    val webService = new WebService(settings.Spotlight.host, settings.Spotlight.port)

    // Annotate each resource-file
    Files.explore(new File(settings.Resources.folder)).map(file => {
      val json = parse(file.file)
      val r = json.extract[Resource]

      // Construct new elements
      val newTitle = webService.textToSpots(r.title.label) match {
        case Some(spots) => r.title.copy(oSpots = Some(spots))
      }
      val newOKeywords = r.oKeywords.map(keywords => {
        val labels = keywords.map(_.label)
        webService.textsToSpots(labels) match {
          case Some(l) => l.zip(keywords).map(p => {
            val keyword = p._2
            val spots = p._1
            keyword.copy(oSpots = Some(spots))
          })
        }
      })
      val newOCategories = r.oCategories.map(categories => {
        val labels = categories.map(_.label)
        webService.textsToSpots(labels) match {
          case Some(l) => l.zip(categories).map(p => {
            val category = p._2
            val spots = p._1
            category.copy(oSpots = Some(spots))
          })
        }
      })
      val newODomains = r.oDomains.map(domains => {
        val labels = domains.map(_.label)
        webService.textsToSpots(labels) match {
          case Some(l) => l.zip(domains).map(p => {
            val domain = p._2
            val spots = p._1
            domain.copy(oSpots = Some(spots))
          })
        }
      })
      val newOSubdomains = r.oSubdomains.map(subdomains => {
        val labels = subdomains.map(_.label)
        webService.textsToSpots(labels) match {
          case Some(l) => l.zip(subdomains).map(p => {
            val subdomain = p._2
            val spots = p._1
            subdomain.copy(oSpots = Some(spots))
          })
        }
      })

      def annotateNodes(nodes: Nodes): Nodes = nodes match {
        case Nil => Nil
        case _ => {
          val labels = nodes.map(_.label)
          webService.textsToSpots(labels) match {
            case Some(l) => l.zip(nodes).map(p => {
              val node = p._2
              val spots = p._1
              val newChildren = annotateNodes(node.children)
              node.copy(oSpots = Some(spots), children = newChildren)
            })
          }
        }
      }

      val newOTocs = r.oTocs.map(tocs => tocs.map(toc => {
        val newNodes = annotateNodes(toc.nodes)
        toc.copy(nodes = newNodes)
      }))

      val newODescriptions = r.oDescriptions.map(descriptions =>
        webService.textsToSpots(descriptions.map(_.text)) match {
          case Some(l) => l.zip(descriptions).map(p => {
            val description = p._2
            val spots = p._1
            description.copy(oSpots = Some(spots))
          })
        }
      )

      // Create the new resource
      val newResource = r.copy(
        title = newTitle,
        oKeywords = newOKeywords,
        oCategories = newOCategories,
        oDomains = newODomains,
        oSubdomains = newOSubdomains,
        oTocs = newOTocs,
        oDescriptions = newODescriptions
      )

      // Write it
      Json.write(newResource, Some(file.file.getAbsolutePath))
    })

    // Create the web-service
    webService.shutdown
  }
}
