package rsc.snippets

import rsc.Resource
import rsc.attributes.Spot
import rsc.indexers.Indices
import rsc.toc.Node

class Simple {
  def snippetize(r: Resource): Resource = {
    // Process the title
    val r1 = snippetizeTitle(r)

    // Process the tocs
    val r2 = r.oTocs match {
      case None => r1
      case Some(tocs) => {
        // Snippetize the tocs
        val newTocs = tocs.map {
          case toc => {
            // Snippetize the nodes
            val newNodes = toc.nodes.map {
              case node => snippetizeNode(node)
            }
            toc.copy(nodes = newNodes)
          }
        }
        r1.copy(oTocs = Some(newTocs))
      }
    }

    // Return the snippetized resource
    r2
  }

  def snippetizeTitle(r: Resource): Resource = r.title.oIndices match {
    case None => r
    case Some(indices) => {
      // Extract the uris of the indices
      val uris = extractUris(indices)

      // Collect the top-line
      val topLine = Line(Source.title, r.title.label, collectThere(r.title.oSpots, uris))

      // Collect the other-lines
      // From tocs
      val tocOL = r.oTocs match {
        case None => Nil
        case Some(tocs) => tocs.flatMap {
          case toc => toc.nodes.flatMap {
            case node => collectThereAndBelow(node, uris)
          }
        }
      }

      // From description
      val descriptionOL = r.oDescriptions match {
        case None => Nil
        case Some(descriptions) => descriptions.map {
          case description => {
            Line(
              Source.description,
              description.text,
              collectThere(description.oSpots, uris))
          }
        }
      }

      // From keywords
      val keywordsOL = r.oKeywords match {
        case None => Nil
        case Some(keywords) => keywords.map {
          case keyword => {
            Line(
              Source.keywords,
              keyword.label,
              collectThere(keyword.oSpots, uris)
            )
          }
        }
      }

      // From categories
      val categoriesOL = r.oCategories match {
        case None => Nil
        case Some(categories) => categories.map {
          case category => {
            Line(
              Source.categories,
              category.label,
              collectThere(category.oSpots, uris)
            )
          }
        }
      }

      // From domains
      val domainsOL = r.oDomains match {
        case None => Nil
        case Some(domains) => domains.map {
          case domain => {
            Line(
              Source.domains,
              domain.label,
              collectThere(domain.oSpots, uris)
            )
          }
        }
      }

      // From subdomains
      val subdomainsOL = r.oSubdomains match {
        case None => Nil
        case Some(subdomains) => subdomains.map {
          case subdomain => {
            Line(
              Source.subdomains,
              subdomain.label,
              collectThere(subdomain.oSpots, uris)
            )
          }
        }
      }

      // Create the new resource
      val otherLines = tocOL:::descriptionOL:::keywordsOL:::categoriesOL:::domainsOL:::subdomainsOL
      val snippet = Snippet(topLine, otherLines)
      val newIndices = indices.copy(oSnippet = Some(snippet))
      val newTitle = r.title.copy(oIndices = Some(newIndices))
      r.copy(title = newTitle)
    }
  }

  def extractUris(indices: Indices): Set[String] =
    indices.values.map(_.uri).toSet

  def snippetizeNode(node: Node): Node = {
    node.oIndices match {
      case None => node
      case Some(indices) => {
        // Extract the uris of the indices
        val uris: Set[String] = extractUris(indices)

        // Collect the top-line
        val topLine = Line(Source.toc, node.label, collectThere(node.oSpots, uris))

        // Collect the other-lines
        val otherLines: List[Line] = node.children.flatMap {
          case child => collectThereAndBelow(child, uris)
        }

        // Create the new node
        val snippet = Snippet(topLine, otherLines)
        val newIndices = indices.copy(oSnippet = Some(snippet))

        node.copy(oIndices = Some(newIndices))
      }
    }
  }

  def collectThere(oSpots: Option[List[Spot]], uris: Set[String])
  : List[Index] = oSpots match {
    case None => Nil
    case Some(spots) => spots.flatMap(collectIndices(_, uris))
  }

  def collectThereAndBelow(node: Node, uris: Set[String])
  : List[Line] = {
    // Collect the indices from the candidates of this entry
    val xs = collectThere(node.oSpots, uris) match {
      case Nil => Nil
      case indices => List(Line(Source.toc, node.label, indices))
    }

    // Collect below
    xs:::node.children.flatMap {
      case child => collectThereAndBelow(child, uris)
    }
  }

  def collectIndices(spot: Spot, uris: Set[String]): List[Index] = {
    // Filter the candidates the match the uris
    val candidates = spot.candidates.filter {
      case candidate => uris.contains(candidate.uri)
    }

    // Extract the indices
    candidates match {
      case Nil => Nil
      case cs => cs.map {
        case candidate => Index(candidate.uri, spot.start, spot.end)
      }
    }
  }
}
