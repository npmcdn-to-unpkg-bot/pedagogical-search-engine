package rsc.indexers

import graph.edges.unbiased.AttachedWeight
import graph.{DirectedGraph, Pagerank, Utils}
import mysql.GraphFactory
import rsc.Resource
import rsc.Types.{Nodes, Indices}
import rsc.attributes.Candidate.{Candidate, Spotlight}
import rsc.toc.Node
import utils.Constants
import utils.Utils.mergeOptions2List
import utils.Math._

import scala.collection.JavaConverters._

class Graph {
  def index(r: Resource): Option[Resource] = {
    // Create the seed graph
    val digraph = seedGraph(r)

    return None

    /*
    constructGraph(r).flatMap { case (digraph, urisToDepth) => {
      val allowedUris = urisToDepth.keySet
      // Index Title
      index(digraph, urisToDepth) match {
        case Nil => None
        case titleIndices => {
          /*// Save the graph for analysis
          digraph.toJSONFile(
            allUris.toList.asJava,
            "graph.json",
            Constants.Graph.Edges.Attribute.normalizedCwlm)
          // */

          // Index the tocs
          val newOTocs = r.oTocs.map(_.map(toc => {
            val newNodes = indexNodes(toc.nodes)(digraph, allowedUris)
            toc.copy(nodes = newNodes)
          }))

          // Create the new resource
          Some(
            r.copy(
              title = r.title.copy(oIndices = Some(titleIndices)),
              oTocs = newOTocs
              //oIndexer = Some(Indexer.Graph)
            )
          )
        }
      }
    }
    }
    */
  }

  /**
    * In case of conflict (same uri appearing in multiple entries),
    * the entry with the lowest toc depth wins.
    */
  private def urisFromNodes(nodesWithDepth: List[(Node, Int)], allowedUris: Set[String])
  : Map[String, Int] = {
    mergeOptions2List(nodesWithDepth.map(p => p._1.oSpots.map(spots => (p, spots))): _*)
      .flatMap(p => p._2.map(spot => (p._1, spot)))
      .flatMap(p => p._2.candidates.map(candidate => (p._1, candidate)))
      .filterNot(p => willBeSkimed(p._2, 0.5))
      .map(p => (p._2.uri, p._1._2))
      .filter(p => allowedUris.contains(p._1))
      .groupBy(_._1)
      .map(g => (g._1, g._2.map(_._2).min))
  }

  private def indexNodes(nodes: Nodes)(implicit digraph: DirectedGraph, allowedUris: Set[String])
  : Nodes = nodes match {
    case Nil => Nil
    case _ => nodes.map(node => {
      // Index children
      val newChildren = indexNodes(node.children)

      // Index the current Node
      val subNodes = (node, 0) :: node.childrenWithDepth(offset = 1)

      // Get the uris from the candidates senses allowed
      val urisToDepth = urisFromNodes(subNodes, allowedUris)

      // Using pagerank
      println(s"node $node")
      val oIndices = index(digraph, urisToDepth) match {
        case Nil => None
        case indices => Some(indices)
      }

      // Create the new node
      node.copy(
        children = newChildren,
        oIndices = oIndices
      )
    })
  }

  private def index(digraph: DirectedGraph, urisToDepth: Map[String, Int])
  : Indices = urisToDepth.size match {
    case zero if zero == 0 => Nil
    case urisSize => {
      // Run the pagerank
      val nWeight = new graph.nodes.biased.Uniform(digraph, urisToDepth.keys.toList.asJava)
      val eWeight = new AttachedWeight(
        digraph,
        Constants.Graph.Edges.Attribute.completeWlm,
        Constants.Graph.Edges.Attribute.normalizedCwlm)

      Pagerank.weighted(digraph, nWeight, eWeight, 0.8)

      // Produce the indices
      val topNodes = digraph.getNodes.asScala.toList
        .sortBy(-_.getScore).take(urisSize)
      val topScores1 = rescaleD(
        rescaleD(topNodes.map(_.getScore().toDouble)
        ).map(s => math.exp(s))
      ).map(s => s * (math.log(1 + math.log(1 + urisSize.toDouble)) + 0.2))

      val topScores2 = topNodes.zip(topScores1).map {
        case (node, score) => {
          val nodeId = node.getId()
          urisToDepth.contains(nodeId) match {
            case true => {
              val depth = urisToDepth(nodeId)
              (score / (depth.toDouble + 1))
            }
            case false => (score / 2)
          }
        }
      }

      topNodes.zip(topScores2).map(p => {
        val node = p._1
        val score = p._2
        println(s"$node -> $score")
        Index(node.getId(), score)
      })
    }
  }

  val seedValue = "seed-value"

  def seedGraph(r: Resource): DirectedGraph = {
    // Build the graph
    val seeds = getSeeds(r)
    val mapping = mergeSeeds(seeds)
    val digraph = GraphFactory.connect1Smart(mapping.keySet.asJava, 9.0)

    // Attach the seed to each node
    digraph.getNodes.asScala.map(node => {
      val uri = node.getId
      node.addNodeAttr(seedValue, mapping(uri))
    })

    // Skim the graph
    def nodeValue(node: graph.nodes.Node): Int = {
      node.totalNeighbors()
    }

    def bestCC(digraph: DirectedGraph)
    : Set[graph.nodes.Node] = {
      Utils.connectedComponents(digraph.getNodes.asScala) match {
        case Nil => Set()
        case ccs => {
          val sorted = ccs.toList.sortBy(g => -g.toList.map(n => nodeValue(n)).sum)
          sorted.head
        }
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
      val nodes = digraph.getNodes().asScala.toList
      val sorted = nodes.sortBy(node => {
        val seed = node.getNodeAttr(seedValue).asInstanceOf[Seed]
        // node with fewest priority (, degree) that leaves
        (-seed.priority, nodeValue(node))
      })
      val target = sorted.head.getId()
      digraph.removeNode(target)

      // todo: remove
      println(s"remove 1: ${sorted.head.getNodeAttr(seedValue).asInstanceOf[Seed]} ${nodeValue(sorted.head)}")
    }

    skimStep(digraph)
    while (digraph.nbNodes() > 25) {
      removeStep(digraph)
      skimStep(digraph)
    }

    // todo: remove
    saveGraph(digraph)

    digraph
  }

  def saveGraph(digraph: DirectedGraph): Unit = {
    digraph.toJSONFile(
      digraph.getIDs().asScala.toList.asJava,
      "graph.json",
      Constants.Graph.Edges.Attribute.normalizedCwlm)
  }

  case class Seed(candidate: Candidate, priority: Int, depth: Int)

  def mergeSeeds(seeds: Set[Seed])
  : Map[String, Seed] = {
    // Lowest depth wins
    val groups = seeds.groupBy(_.candidate.uri)
    groups.map {
      case (uri, xs) => (uri, xs.toList.sortBy(_.depth).head)
    }
  }

  def getSeeds(r: Resource): Set[Seed] = {

    def skimSpecial(seeds: Set[Seed])(threshold: Double)
    : Set[Seed] = seeds.filterNot(s => willBeSkimed(s.candidate, threshold))

    // Seeds from title
    val titleSeeds: Set[Seed] = skimSpecial {
      val spots = mergeOptions2List(r.title.oSpots).flatten
      val candidates = spots.flatMap(_.candidates)
      candidates.map(c => Seed(c, 1, 0)).toSet
    }(0.5)

    // Seeds from table of contents
    val tocsSeeds: Set[Seed] = skimSpecial {
      val oSeeds = r.oTocs.map(tocs => {
        val allCandidates = tocs.flatMap(toc => {
          // Root node has depth 1
          val nodes = toc.nodesWithDepth(1)
          val oSpots = nodes.map {
            case (node, depth) => {
              node.oSpots.map(spots => {
                spots.map(spot => (spot, depth))
              })
            }
          }
          val spots = mergeOptions2List(oSpots: _*).flatten
          val candidates = spots.flatMap {
            case (spot, depth) => spot.candidates.map(c => (c, depth))
          }
          candidates
        })

        allCandidates.map {
          case (candidate, depth) => Seed(candidate, 2, depth)
        }
      })
      mergeOptions2List(oSeeds).flatten.toSet
    }(0.5)

    // Seeds from metadata
    val metaSeeds: Set[Seed] = skimSpecial {
      val spots = mergeOptions2List(
        r.oKeywords.map(ks => mergeOptions2List(ks.map(_.oSpots): _*).flatten),
        r.oCategories.map(cs => mergeOptions2List(cs.map(_.oSpots): _*).flatten),
        r.oDomains.map(os => mergeOptions2List(os.map(_.oSpots): _*).flatten),
        r.oSubdomains.map(ds => mergeOptions2List(ds.map(_.oSpots): _*).flatten)
      ).flatten
      val candidates = spots.flatMap(_.candidates)
      // .. have depth 2
      candidates.map(c => Seed(c, 3, 2)).toSet
    }(0.95)

    // Seeds from descriptions
    val descrSeeds: Set[Seed] = skimSpecial {
      val spots = mergeOptions2List {
        r.oDescriptions.map(ds => mergeOptions2List(ds.map(_.oSpots): _*).flatten)
      }.flatten
      val candidates = spots.flatMap(_.candidates)
      // .. have depth 2
      candidates.map(c => Seed(c, 4, 2)).toSet
    }(0.95)

    (titleSeeds ++ tocsSeeds ++ metaSeeds ++ descrSeeds)
  }

  def willBeSkimed(c: Candidate, threshold: Double): Boolean = c match {
    case Spotlight(_, _, scores, _) => (scores.finalScore < threshold)
  }
}
