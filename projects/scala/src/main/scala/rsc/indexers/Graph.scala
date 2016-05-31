package rsc.indexers

import graph.edges.unbiased.AttachedWeight
import graph.{DirectedGraph, Pagerank, Utils}
import mysql.GraphFactory
import rsc.Resource
import rsc.Types.{Nodes, Spots}
import rsc.attributes.Candidate.{Spotlight => CSpotlight}
import utils.Constants
import utils.Math._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class Graph(_ec: ExecutionContext,
            coreMaxSize: Int = 25,
            fizzFactor: Double = 1,
            spotlightCoreThreshold: Double = 0.5) {
  // Types for logic
  /*
   * Represents an URI with its relative context.
   * Relative to its origin: a resource title or a toc entry.
   * With depth from origin and priority classification:
   *  1 title
   *  2 toc
   *  3 meta
   *  4 description
   *
   * The classification helps to decide what is the most important
   * contextual URI - the most relevant in the current context.
   */
  case class CUri(uri: String, priority: Int, depth: Int)

  // Types for better readability
  type GroupedCUris = List[CUri]
  type Choices = List[GroupedCUris]
  type Bias = Double

  // Make the execution context implicit
  implicit val ec = _ec

  // Label associated with a CUri
  val CUriLabel = "context-uri"

  // Fixed tuning parameters
  val miniGraphMinWLM = 9.0
  val expandedGraphMinWLM = 10.5
  val prDumpingFactor = 0.8

  // Private methods
  private def choicesFrom(nodes: Nodes, offset: Int, blackList: Set[String], threshold: Double)
  : Choices = {
    // Extract sub-nodes and their depth
    val pairs = nodes.flatMap(node => {
      (node, offset)::node.childrenWithDepth(offset + 1)
    })

    // Get the choices from each spot of each node
    val nodesChoices = pairs.flatMap {
      case (node, depth) =>
        node.oSpots match {
          case None => Nil
          case Some(spots) => choicesFromSpots(spots, priority = 2, depth, blackList, threshold)
        }
    }
    nodesChoices
  }

  private def choicesFrom(r: Resource, blackList: Set[String], threshold: Double)
  : Choices = {
    // Extract the choices
    val titleChoices: Choices = r.title.oSpots match {
      case None => Nil
      case Some(spots) => choicesFromSpots(spots, priority = 1, depth = 0, blackList, threshold)
    }

    val tocsChoices: Choices = r.oTocs match {
      case None => Nil
      case Some(tocs) => tocs.flatMap(toc => {
        // Collect the nodes
        val nodes = tocs.flatMap(_.nodes)

        // The root node has a depth of 1
        choicesFrom(nodes, offset = 1, blackList, threshold)
      })
    }

    val keywordsSpots: Spots = r.oKeywords.map(xs => xs.flatMap(_.oSpots).flatten).getOrElse(Nil)
    val categoriesSpots: Spots = r.oCategories.map(xs => xs.flatMap(_.oSpots).flatten).getOrElse(Nil)
    val domainsSpots: Spots = r.oDomains.map(xs => xs.flatMap(_.oSpots).flatten).getOrElse(Nil)
    val subdomainsSpots: Spots = r.oSubdomains.map(xs => xs.flatMap(_.oSpots).flatten).getOrElse(Nil)

    val metaSpots: Spots = keywordsSpots ++ categoriesSpots ++ domainsSpots ++ subdomainsSpots
    val metaChoices: Choices = choicesFromSpots(metaSpots, priority = 3, depth = 2, blackList, threshold)

    // Seeds from descriptions
    val descriptionSpots: Spots = r.oDescriptions.map(xs => xs.flatMap(_.oSpots).flatten).getOrElse(Nil)
    val descriptionChoices: Choices = choicesFromSpots(descriptionSpots, priority = 4, depth = 2, blackList, threshold)

    titleChoices ++ tocsChoices ++ metaChoices ++ descriptionChoices
  }

  private def choicesFromSpots(spots: Spots, priority: Int, depth: Int, blackList: Set[String], threshold: Double)
  : Choices = {
    spots.flatMap(spot => {
      // Does the spot contains an element from the blacklist?
      val suspect = spot.candidates.exists(candidate => {
        blackList.contains(candidate.uri)
      })

      // If no, produce the group of context-uris
      suspect match {
        case true => Nil
        case false =>
          val filtered = spot.candidates.filter {
            case CSpotlight(_, _, scores, _) => scores.finalScore >= threshold
          }
          val groupedCUris = filtered.map(candidate => {
            CUri(candidate.uri, priority, depth)
          })
          groupedCUris :: Nil
      }
    })
  }

  /*
   * Decides the non-zero biases by looking at the graph and the choices.
   */
  private def decideBiases(expanded: DirectedGraph, coreCUris: List[CUri], choices: Choices)
  : Map[CUri, Bias] = {
    // Define bias values
    val coreBias: Double = 1
    val choiceBias: Double = 0.1

    // Core biases
    val coreBiases: List[(CUri, Bias)] = coreCUris.map(curi => (curi, coreBias))

    // Biases from choices
    val choicesBiases: List[(CUri, Bias)] = choices.flatMap(choice => {
      // Find in the graph each candidate
      val found = choice.flatMap(candidate => {
        expanded.contains(candidate.uri) match {
          case false => Nil
          case true =>
            val node = expanded.getNode(candidate.uri)
            (candidate, node) :: Nil
        }
      })

      // Select the best candidate found (if any)
      found match {
        case Nil => Nil
        case xs =>
          val sorted = xs.sortBy(-_._2.undirectedDegree())
          val best = sorted.head
          (best._1, choiceBias) :: Nil
      }
    })

    (coreBiases ++ choicesBiases).toMap
  }

  private def mergeBiases(biases: Map[CUri, Bias])
  : Map[String, (CUri, Bias)] = {
    // Group the biases by uri
    biases.groupBy(_._1.uri).map {
      case (uri, group) =>
        // Choose the context URI
        val sorted = group.toList.sortBy {
          case (curi, bias) => (-bias, curi.priority, curi.depth)
        }
        (uri, sorted.head)
    }
  }

  /*
   * Creates a minimum graph with only the core concepts
   */
  private def miniGraphMapping(r: Resource)
  : Map[String, CUri] = {
    val choices = choicesFrom(r, Set[String](), spotlightCoreThreshold)
    val pairs = choices.flatMap(groupedCUris => {
      groupedCUris.map {
        case curi => (curi.uri, curi)
      }
    })

    // Select the best Context URI for each string
    val grouped = pairs.groupBy(_._1)
    val distinct = grouped.map {
      case (uri, xs) =>
        val sorted = xs.sortBy {
          case (_, curi) => (curi.priority, curi.depth)
        }
        sorted.head
    }

    distinct
  }
  private def miniGraph(r: Resource)
  : Future[DirectedGraph] = {
    // Build the graph
    val mapping = miniGraphMapping(r)
    val futureMiniGraph = GraphFactory.follow1(mapping.keySet, miniGraphMinWLM)

    futureMiniGraph map {
      case digraph =>
        // Attach the Context Uri to each node
        digraph.getNodes.asScala.foreach(node => {
          val uri = node.getId
          node.addNodeAttr(CUriLabel, mapping(uri))
        })

        // Skim the graph
        def nodeValue(node: graph.nodes.Node): Int = {
          // Intuitive heuristic, more complex could be designed (Machine Learning?)
          node.totalNeighbors()
        }

        def bestCC(digraph: DirectedGraph)
        : Set[graph.nodes.Node] = {
          Utils.connectedComponents(digraph.getNodes.asScala) match {
            case Nil => Set()
            case ccs =>
              val sorted = ccs.toList.sortBy(g => -g.toList.map(n => nodeValue(n)).sum)
              sorted.head
          }
        }

        def filterMaxCC(directedGraph: DirectedGraph) = {
          val uris = bestCC(digraph).map(_.getId)
          digraph.removeNodesNotIn(uris.asJava)
        }

        def skimStep(directedGraph: DirectedGraph) = {
          // Remove lonely and dangling nodes
          digraph.removeNodes(2)

          // Take the largest Connected Component
          filterMaxCC(directedGraph)
        }

        def removeStep(directedGraph: DirectedGraph) = {
          val nodes = digraph.getNodes.asScala.toList
          val sorted = nodes.sortBy(node => {
            val seed = node.getNodeAttr(CUriLabel).asInstanceOf[CUri]
            // node with fewest priority (, degree) that leaves
            (-seed.priority, nodeValue(node))
          })
          val target = sorted.head.getId
          digraph.removeNode(target)
        }

        skimStep(digraph)
        while (digraph.nbNodes() > this.coreMaxSize) {
          removeStep(digraph)
          skimStep(digraph)
        }

        digraph
    }
  }


  // Public methods
  def index(r: Resource): Future[Option[Resource]] = {
    // Create the core graph
    miniGraph(r) flatMap {
      case miniGraph =>
        // Save the core Context URIs
        val coreNodes = miniGraph.getNodes.asScala
        val coreCUris = coreNodes.map(_.getNodeAttr(CUriLabel).asInstanceOf[CUri])

        // Create an expanded graph
        val coreUris = coreCUris.map(_.uri).toSet
        GraphFactory.follow2(coreUris, expandedGraphMinWLM).map {
          case expanded => (expanded, coreCUris, coreUris)
        }
    } map {
      case (expanded, coreCUris, coreUris) =>
        // Index the title
        val titleChoices = choicesFrom(r, blackList = coreUris, threshold = 0)
        val biasesMap = decideBiases(expanded, coreCUris.toList, titleChoices)
        val mergedBiases = mergeBiases(biasesMap)

        pageRankOn(expanded, mergedBiases) match {
          case Nil => None
          case indices =>
            // Index the table of contents
            val newOTocs = r.oTocs.map(tocs => {
              tocs.map(toc => {
                val newNodes = indexNodes(toc.nodes)(expanded, coreCUris.toList)
                toc.copy(nodes = newNodes)
              })
            })

            // Create the new resource
            Some(
              r.copy(
                title = r.title.copy(oIndices = Some(new Indices(indices))),
                oTocs = newOTocs,
                oIndexer = Some(Indexer.Graph)
              )
            )
        }
    }
  }

  private def indexNodes(nodes: Nodes)(implicit graph: DirectedGraph, coreCUris: List[CUri])
  : Nodes = nodes match {
    case Nil => Nil
    case _ => nodes.map(node => {
      // Index children
      val newChildren = indexNodes(node.children)

      // Index the current entry
      val choices = choicesFrom(List(node), offset = 0, blackList = Set(), threshold = 0)
      val biases = decideBiases(graph, coreCUris, choices)
      val merged = mergeBiases(biases)

      // Using pagerank
      val oIndices = pageRankOn(graph, merged) match {
        case Nil => None
        case indices => Some(new Indices(indices))
      }

      // Create the new node
      node.copy(
        children = newChildren,
        oIndices = oIndices
      )
    })
  }

  def pageRankOn(digraph: DirectedGraph, biases: Map[String, (CUri, Bias)])
  : List[Index] = digraph.nbNodes() match {
    case zero if zero == 0 => Nil
    case _ =>
      // Create a mapping: URI -> Context URI
      val mapping: Map[String, CUri] = biases.map {
        case (uri, (curi, _)) => (uri, curi)
      }

      // Run the PageRank algorithm
      val biasesForJava: Map[String, java.lang.Double] = biases.map {
        case (uri, (_, bias)) => (uri, new java.lang.Double(bias))
      }
      val nWeight = new graph.nodes.biased.Tunable(digraph, biasesForJava.asJava)
      val eWeight = new AttachedWeight(
        digraph,
        Constants.Graph.Edges.Attribute.completeWlm,
        Constants.Graph.Edges.Attribute.normalizedCwlm)

      Pagerank.weighted(digraph, nWeight, eWeight, prDumpingFactor)

      // Produce the indices scores
      val nbIndices = math.floor(biases.size.toDouble * this.fizzFactor)
      val topNodes = digraph.getNodes.asScala.toList
        .sortBy(-_.getScore).take(nbIndices.toInt)

      val r1 = rescaleD(topNodes.map(_.getScore.toDouble))
      val r2 = rescaleD(r1.map(s => math.exp(s)))
      val r3 = r2.map(s => s * (math.log(1 + math.log(1 + biases.size.toDouble)) + 0.2))
      val scores = topNodes.zip(r3).map {
        case (node, score) =>
          val nodeId = node.getId
          mapping.contains(nodeId) match {
            // Candidates score is proportional to depth
            case true =>
              val depth = mapping(nodeId).depth
              score / (depth.toDouble + 1)

            case false => score
          }
      }

      // Produce the indices
      topNodes.zip(scores).map(p => {
        val node = p._1
        val score = p._2

        Index(node.getId, score)
      })
  }

  def saveGraph(digraph: DirectedGraph, name: String): Unit = {
    digraph.toJSONFile(
      digraph.getIDs.asScala.toList.asJava,
      s"$name.json",
      Constants.Graph.Edges.Attribute.normalizedCwlm)
  }
}
