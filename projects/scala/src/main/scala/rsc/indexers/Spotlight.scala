package rsc.indexers

import rsc.Resource
import rsc.Types.{Indices, Spots, Nodes}
import rsc.attributes.Candidate.Spotlight
import utils.Logger

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

    // tocs
    val tocsSpots = r.oTocs match {
      case None => Nil
      case Some(tocs) => tocs.flatMap(toc => nodesSpots(toc.nodes))
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
    // todo

    // Create the new Resource
    oTitleIndices match {
      case None => None
      case _ => {
        Some(r.copy(
          oIndexer = Some(Indexer.StandardSpotlight),
          title = r.title.copy(oIndices = oTitleIndices)
        ))
      }
    }
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

  def nodesSpots(nodes: Nodes): Spots = nodes match {
    case Nil => Nil
    case _ => nodes.flatMap(node => {
      val spots = node.oSpots match {
        case None => Nil
        case Some(l) => l
      }
      val childrenSpots = nodesSpots(node.children)
      spots:::childrenSpots
    })
  }
}
