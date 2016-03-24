package rsc.annotators

import rsc.Resource
import rsc.Types._
import spotlight.WebService

object Standard {
  def annotate(r: Resource, ws: WebService): Option[Resource] = {
    try {
      Some(newResource(r, ws))
    } catch {
      case e => None
    }
  }

  private def newResource(r: Resource, ws: WebService): Resource = {

    // Function to annotate the nodes recursively
    def annotateNodes(nodes: Nodes): Nodes = nodes match {
      case Nil => Nil
      case _ => {
        val labels = nodes.map(_.label)
        ws.annotateSeparately(labels) match {
          case Some(l) => l.zip(nodes).map {
            case (spots, node) => {
              val newChildren = annotateNodes(node.children)
              node.copy(oSpots = Some(spots), children = newChildren)
            }
          }
        }
      }
    }

    // Construct new elements
    val newTitle = ws.annotateSingle(r.title.label) match {
      case Some(spots) => r.title.copy(oSpots = Some(spots))
    }
    val newOKeywords = r.oKeywords.map(keywords => {
      val labels = keywords.map(_.label)
      ws.annotateSeparately(labels) match {
        case Some(l) => l.zip(keywords).map {
          case(spots, keyword) => {
            keyword.copy(oSpots = Some(spots))
          }
        }
      }
    })
    val newOCategories = r.oCategories.map(categories => {
      val labels = categories.map(_.label)
      ws.annotateSeparately(labels) match {
        case Some(l) => l.zip(categories).map {
          case (spots, category) => {
            category.copy(oSpots = Some(spots))
          }
        }
      }
    })
    val newODomains = r.oDomains.map(domains => {
      val labels = domains.map(_.label)
      ws.annotateSeparately(labels) match {
        case Some(l) => l.zip(domains).map {
          case (spots, domain) => {
            domain.copy(oSpots = Some(spots))
          }
        }
      }
    })
    val newOSubdomains = r.oSubdomains.map(subdomains => {
      val labels = subdomains.map(_.label)
      ws.annotateSeparately(labels) match {
        case Some(l) => l.zip(subdomains).map {
          case (spots, subdomain) => {
            subdomain.copy(oSpots = Some(spots))
          }
        }
      }
    })

    val newOTocs = r.oTocs.map(tocs => tocs.map(toc => {
      val newNodes = annotateNodes(toc.nodes)
      toc.copy(nodes = newNodes)
    }))

    val newODescriptions = r.oDescriptions.map(descriptions =>
      ws.annotateSeparately(descriptions.map(_.text)) match {
        case Some(l) => l.zip(descriptions).map {
          case (spots, description) => {
            description.copy(oSpots = Some(spots))
          }
        }
      }
    )

    // Create the new resource
    r.copy(
      oAnnotator = Some(Annotator.Standard),
      title = newTitle,
      oKeywords = newOKeywords,
      oCategories = newOCategories,
      oDomains = newODomains,
      oSubdomains = newOSubdomains,
      oTocs = newOTocs,
      oDescriptions = newODescriptions
    )
  }
}
