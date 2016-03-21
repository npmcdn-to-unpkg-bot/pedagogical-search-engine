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
    }}
  }

  private def urisFromNodes(nodesWithDepth: List[(Node, Int)], allowedUris: Set[String])
  : Map[String, Int] = {
    mergeOptions2List(nodesWithDepth.map(p => p._1.oSpots.map(spots => (p, spots))): _*)
      .flatMap(p => p._2.map(spot => (p._1, spot)))
      .flatMap(p => p._2.candidates.map(candidate => (p._1, candidate)))
      .filterNot(p => willBeSkimed(p._2))
      .map(p => (p._2.uri, p._1._2))
      .filter(p => allowedUris.contains(p._1))
      .toMap
  }

  private def indexNodes(nodes: Nodes)(implicit digraph: DirectedGraph, allowedUris: Set[String])
  : Nodes = nodes match {
    case Nil => Nil
    case _ => nodes.map(node => {
      // Index children
      val newChildren = indexNodes(node.children)

      // Index the current Node
      val subNodes = (node, 0)::node.childrenWithDepth(offset = 1)

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
        ).map(s => math.exp(2 * s))
      ).map(s => s * math.log(1 + urisSize.toDouble))

      val topScores2 = topNodes.zip(topScores1).map { case (node, score) => {
        val nodeId = node.getId()
        urisToDepth.contains(nodeId) match {
          case true => {
            val depth = urisToDepth(nodeId)
            (score / (depth.toDouble + 1))
          }
          case false => (score / 2)
        }
      }}

      topNodes.zip(topScores2).map(p => {
        val node = p._1
        val score = p._2
        println(s"$node -> $score")
        Index(node.getId(), score)
      })
    }
  }

  def constructGraph(r: Resource): Option[(DirectedGraph, Map[String, Int])] = {
    // Collect the candidate senses
    val topLevelCandidates = mergeOptions2List(
      r.title.oSpots,
      r.oKeywords.map(ks => mergeOptions2List(ks.map(_.oSpots): _*).flatten),
      r.oCategories.map(cs => mergeOptions2List(cs.map(_.oSpots): _*).flatten),
      r.oDomains.map(os => mergeOptions2List(os.map(_.oSpots): _*).flatten),
      r.oSubdomains.map(ds => mergeOptions2List(ds.map(_.oSpots): _*).flatten),

      // .. from Descriptions
      r.oDescriptions.map(ds => mergeOptions2List(ds.map(_.oSpots): _*).flatten)
    ).flatten.flatMap(_.candidates)

    val tocsCandidates = mergeOptions2List(
      // .. from Tocs
      r.oTocs.map(ts => mergeOptions2List(ts.flatMap(_.nodesRec().map(_.oSpots)): _*).flatten)
    ).flatten.flatMap(_.candidates)

    val candidates = skim(topLevelCandidates:::tocsCandidates)

    // Build a first digraph
    val uris = candidates.map(_.uri)
    val digraph = GraphFactory.connect1Smart(uris.asJava, 9.0)

    // Remove lonely and dangling nodes
    digraph.removeNodes(2);

    //* // Save the graph for analysis
    digraph.toJSONFile(
      uris.toList.asJava,
      "graph.json",
      Constants.Graph.Edges.Attribute.normalizedCwlm)
    // */

    // Build a second digraph
    digraph.getNodes.asScala.toList match {
      case Nil => {
        println(s"no nodes: ${uris.size}")
        None
      } // fail if there are no nodes
      case nodes => {
        // Get the principal connected component
        val ccs = Utils.connectedComponents(nodes)
        val biggestCC = ccs.toList.sortBy(-_.size).head

        // Build a better graph
        val uris2 = biggestCC.map(_.getId).toSet
        val digraph2 = GraphFactory.smart2(uris2.asJava, 10.5)

        // Construct allowedUrisWithDepth
        val urisToDepth =
          r.oTocs.map(tocs => urisFromNodes(tocs.flatMap(_.nodesWithDepth()), uris2)) match {
          case None => uris2.map(uri => (uri, 0)).toMap
          case Some(tocsUrisToDepth) => {
            val topLevelUris = topLevelCandidates.map(_.uri).toSet
            // Top-level Uris win over toc uris
            val tocsUrisToDepthFiltered = tocsUrisToDepth.toList.
              filterNot { case (uri, depth) => topLevelUris.contains(uri) }.
              toMap
            val topLevelUrisToDepth = uris2.
              filterNot(uri => tocsUrisToDepthFiltered.contains(uri)).
              map(uri => (uri, 0)).
              toMap
            tocsUrisToDepthFiltered ++ topLevelUrisToDepth
          }
        }
        Some(
          digraph2,
          urisToDepth
        )
      }
    }
  }

  def willBeSkimed(c: Candidate): Boolean = c match {
    case Spotlight(_, _, scores, _) => (scores.finalScore < 0.5)
  }

  def skim(candidates: List[Candidate]): List[Candidate] =
    candidates.filterNot(c => willBeSkimed(c))
}
