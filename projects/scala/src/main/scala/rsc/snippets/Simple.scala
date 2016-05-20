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
      case Some(tocs) =>
        // Snippetize the tocs
        val newTocs = tocs.map {
          case toc =>
            // Snippetize the nodes
            val newNodes = toc.nodes.map {
              case node =>
                snippetizeNodeRec(node, 0, None)
            }
            toc.copy(nodes = newNodes)
          }
        r1.copy(oTocs = Some(newTocs))
    }

    // Return the snippetized resource
    r2.copy(oSnippetizer = Some(Snippetizer.Simple))
  }

  def snippetizeTitle(r: Resource): Resource = r.title.oIndices match {
    case None => r
    case Some(indices) =>
      // Extract the uris of the indices
      val uris = extractUris(indices)

      // Collect the top-line
      val topLine = Line(Source.title, r.title.label, collectThere(r.title.oSpots, uris, 0), 1)

      // Collect the other-lines
      // From tocs
      val tocOL = r.oTocs match {
        case None => Nil
        case Some(tocs) => tocs.flatMap {
          case toc => toc.nodes.flatMap {
            case node => collectThereAndBelow(node, uris, 0)
          }
        }
      }

      // From description
      val descriptionOL = r.oDescriptions match {
        case None => Nil
        case Some(descriptions) => descriptions.map {
          case description =>
            Line(
              Source.description,
              description.text,
              collectThere(description.oSpots, uris, 0),
              1
            )
        }
      }

      // From keywords
      val keywordsOL = r.oKeywords match {
        case None => Nil
        case Some(keywords) => keywords.map {
          case keyword =>
            Line(
              Source.keywords,
              keyword.label,
              collectThere(keyword.oSpots, uris, 0),
              1
            )
        }
      }

      // From categories
      val categoriesOL = r.oCategories match {
        case None => Nil
        case Some(categories) => categories.map {
          case category =>
            Line(
              Source.categories,
              category.label,
              collectThere(category.oSpots, uris, 0),
              1
            )
        }
      }

      // From domains
      val domainsOL = r.oDomains match {
        case None => Nil
        case Some(domains) => domains.map {
          case domain =>
            Line(
              Source.domains,
              domain.label,
              collectThere(domain.oSpots, uris, 0),
              1
            )
        }
      }

      // From subdomains
      val subdomainsOL = r.oSubdomains match {
        case None => Nil
        case Some(subdomains) => subdomains.map {
          case subdomain =>
            Line(
              Source.subdomains,
              subdomain.label,
              collectThere(subdomain.oSpots, uris, 0),
              1
            )
        }
      }

      // Create the new resource
      val otherLines = tocOL:::descriptionOL:::keywordsOL:::categoriesOL:::domainsOL:::subdomainsOL
      val snippet = Snippet(topLine, otherLines)
      val newIndices = indices.copy(oSnippet = Some(snippet))
      val newTitle = r.title.copy(oIndices = Some(newIndices))
      r.copy(title = newTitle)
  }

  def extractUris(indices: Indices): Set[String] =
    indices.values.map(_.uri).toSet

  def snippetizeNodeRec(node: Node, level: Int, oParent: Option[Node])
  : Node = {
    val newNode = snippetizeNode(node, level, oParent)
    val newChildren = newNode.children.map {
      case child => snippetizeNodeRec(child, level + 1, Some(newNode))
    }
    newNode.copy(children = newChildren)
  }

  def snippetizeNode(node: Node, level: Int, oParent: Option[Node])
    : Node = {
    node.oIndices match {
      case None => node
      case Some(indices) =>
        // Extract the uris of the indices
        val uris: Set[String] = extractUris(indices)

        // Collect the top-line
        val topLine = Line(Source.toc, node.bestLabel(),
          collectThere(node.oSpots, uris, getShift(node)), 2 + level)

        // Collect the other-lines "depth-first"
        val otherLines: List[Line] = node.children.flatMap {
          case child => collectThereAndBelow(child, uris, level + 1)
        }

        // Create the new node
        val snippet = Snippet(topLine, otherLines)
        val newIndices = indices.copy(oSnippet = Some(snippet))

        node.copy(oIndices = Some(newIndices))
    }
  }

  def collectThere(oSpots: Option[List[Spot]], uris: Set[String], shift: Int)
  : List[Index] = oSpots match {
    case None => Nil
    case Some(spots) => spots.flatMap(collectIndices(_, uris, shift))
  }

  def collectThereAndBelow(node: Node, uris: Set[String], level: Int)
  : List[Line] = {
    // Collect the indices from the candidates of this entry
    val xs = collectThere(node.oSpots, uris, getShift(node)) match {
      case Nil => Nil
      case indices => List(Line(Source.toc, node.bestLabel(), indices, 2 + level))
    }

    // Collect below
    xs:::node.children.flatMap {
      case child => collectThereAndBelow(child, uris, level + 1)
    }
  }

  def getShift(node: Node): Int = node.bestLabel().length - node.label.length

  /*
   * Warning, due to the prettifier, the spots {start, end} pointers
   * are shifted. ex: 'bla' becomes 'chapter 1: bla'
   */
  def collectIndices(spot: Spot, uris: Set[String], shift: Int): List[Index] = {
    // Filter the candidates the match the uris
    val candidates = spot.candidates.filter {
      case candidate => uris.contains(candidate.uri)
    }

    // Extract the indices
    candidates match {
      case Nil => Nil
      case cs => cs.map {
        case candidate => Index(candidate.uri, spot.start + shift, spot.end + shift)
      }
    }
  }
}
