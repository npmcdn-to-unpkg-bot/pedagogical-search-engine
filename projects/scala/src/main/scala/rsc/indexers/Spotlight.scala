package rsc.indexers

import rsc.Resource
import rsc.Types.{Indices, Nodes, Spots}
import rsc.attributes.Candidate.Spotlight

class Spotlight(threshold: Double) {
  def index(r: Resource): Option[Resource] = {

    // Collect each "spots group"
    // .. title

    val titleSpots = r.title.oSpots match {
      case None => Nil
      case Some(l) => l
    }

    // .. keywords, categories, domains, subdomains
    val keySpots = r.oKeywords match {
      case None => Nil
      case Some(keywords) => keywords.flatMap(keyword => keyword.oSpots match {
        case None => Nil
        case Some(l) => l
      })
    }
    val catSpots = r.oCategories match {
      case None => Nil
      case Some(categories) => categories.flatMap(category => category.oSpots match {
        case None => Nil
        case Some(l) => l
      })
    }
    val domSpots = r.oDomains match {
      case None => Nil
      case Some(domains) => domains.flatMap(domain => domain.oSpots match {
        case None => Nil
        case Some(l) => l
      })
    }
    val subdomSpots = r.oSubdomains match {
      case None => Nil
      case Some(subdomains) => subdomains.flatMap(subdomain => subdomain.oSpots match {
        case None => Nil
        case Some(l) => l
      })
    }

    // descriptions
    val descsSpots = r.oDescriptions match {
      case None => Nil
      case Some(ds) => ds.flatMap(d => d.oSpots match {
        case None => Nil
        case Some(l) => l
      })
    }

    // Produce Title indices
    val oTitleIndices = produceIndices(List(
      titleSpots,
      keySpots,
      catSpots,
      domSpots,
      subdomSpots,
      descsSpots
    ).flatten) match {
      case Nil => None
      case indices => Some(indices)
    }

    // Produce Tocs indices
    val newOTocs = r.oTocs.map(tocs =>
      tocs.map(toc => {
        // Index the nodes
        val newNodes = indexNodes(toc.nodes)

        // Create the new toc
        toc.copy(nodes = newNodes)
     })
    )

    // Create the new Resource
    Some((oTitleIndices match {
      case None => r
      case _ => r.copy(
        oIndexer = Some(Indexer.StandardSpotlight),
        title = r.title.copy(oIndices = oTitleIndices)
      )
    }).copy(oTocs = newOTocs))
  }

  def produceIndices(spots: Spots): Indices = spots.flatMap(spot => {
    spot.candidates.flatMap(candidate => candidate match {
      case Spotlight(label, uri, scores, _) => {
        // Take only the candidates above the threshold
        if(scores.finalScore >= threshold) {
          List(Index(candidate.uri, scores.finalScore))
        } else {
          Nil
        }
      }
      case _ => Nil
    })
  })

  def indexNodes(nodes: Nodes): Nodes = nodes match {
    case Nil => Nil
    case _ => nodes.map(node => {
      // Index children
      val children = indexNodes(node.children)

      // Analyse each spot of the current node
      val indices = node.oSpots match {
        case None => Nil
        case Some(spots) => spots.flatMap(spot => {
          // take only good candidates
          spot.candidates.flatMap(candidate => candidate match {
            case Spotlight(label, uri, scores, _) => {
              val score = scores.finalScore
              if(score >= threshold) {
                // Create an index
                List(Index(uri, score))
              } else {
                Nil
              }
            }
          })
        })
      }

      // Create the new node
      indices match {
        case Nil => node
        case _ => node.copy(oIndices = Some(indices), children = children)
      }
    })
  }
}
