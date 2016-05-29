package rsc.indexers

import rsc.Resource
import rsc.Types.{Nodes, Spots}
import rsc.attributes.Candidate.{Spotlight => CSpotlight}

/**
  * @param cutoff No more than this number of indices per entry
  */
class Spotlight(cutoff: Int = 100) {
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

    // Tocs
    val tocsRootNodes = r.oTocs.map(_.flatMap(toc => toc.nodes)).getOrElse(Nil)
    val tocsIndices = nodesDistinctIndices(tocsRootNodes)

    // Produce Title indices
    val oTitleIndices = spotsToUniqueIndices(List(
      titleSpots,
      keySpots,
      catSpots,
      domSpots,
      subdomSpots,
      descsSpots
    ).flatten) match {
      case Nil => None
      case indices =>
        Some(skim(distinctIndices(indices ::: tocsIndices)))
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
      case Some(indices) => r.copy(
        oIndexer = Some(Indexer.StandardSpotlight),
        title = r.title.copy(oIndices = Some(
          new Indices(indices)
        ))
      )
    }).copy(oTocs = newOTocs))
  }

  def spotsToUniqueIndices(spots: Spots): List[Index] = spots.flatMap(spot => {
    val indices = spot.candidates.flatMap(candidate => candidate match {
      case CSpotlight(label, uri, scores, _) =>
          List(Index(candidate.uri, scores.finalScore))

      case _ => Nil
    })

    distinctIndices(indices)
  })

  def distinctIndices(indices: List[Index])
  : List[Index] = {
    indices.groupBy(_.uri).map{
      case (uri, group) =>
        val sorted = group.sortBy(-_.score)
        sorted.head
    }.toList
  }

  def indicesFromOSpots(oSpots: Option[Spots])
  : List[Index] = {
    oSpots match {
      case None => Nil
      case Some(spots) => spots.flatMap(spot => {
        spot.candidates.flatMap(candidate => candidate match {
          case CSpotlight(label, uri, scores, _) =>
            List(Index(uri, scores.finalScore))
        })
      })
    }
  }

  def nodesDistinctIndices(nodes: Nodes)
  : List[Index] = {
    val indices = nodes.flatMap(node => {
      val current = indicesFromOSpots(node.oSpots)
      val children = nodesDistinctIndices(node.children)

      current ::: children
    })

    distinctIndices(indices)
  }

  def indexNodes(nodes: Nodes): Nodes = nodes match {
    case Nil => Nil
    case _ => nodes.map(node => {
      // Children indices
      val children = indexNodes(node.children)

      // Current indices
      val current = skim(nodesDistinctIndices(List(node)))

      node.copy(oIndices = Some(new Indices(current)), children = children)
    })
  }

  def skim(indices: List[Index])
  : List[Index] = indices.sortBy(-_.score).take(cutoff)
}
