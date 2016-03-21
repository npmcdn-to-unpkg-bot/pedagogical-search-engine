package rsc.indexers

import graph.edges.unbiased.AttachedWeight
import graph.{DirectedGraph, Pagerank, Utils}
import mysql.GraphFactory
import rsc.Resource
import rsc.Types.{Nodes, Indices}
import rsc.attributes.Candidate.{Candidate, Spotlight}
import utils.Constants
import utils.Utils.mergeOptions2List
import utils.Math._

import scala.collection.JavaConverters._

class Graph {
  def index(r: Resource): Option[Resource] = {
    constructGraph(r).flatMap { case (digraph, allUris) => {
      // Index Title
      index(digraph, allUris) match {
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
            val newNodes = indexNodes(toc.nodes)(digraph, allUris)
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

  private def indexNodes(nodes: Nodes)(implicit digraph: DirectedGraph, allowedUris: Set[String])
  : Nodes = nodes match {
    case Nil => Nil
    case _ => nodes.map(node => {
      // Index children
      val newChildren = indexNodes(node.children)

      // Index the current Node
      val subNodes = node::node.childrenRec()

      // Get the uris from the candidates senses allowed
      val uris = skim(
        mergeOptions2List(subNodes.map(_.oSpots): _*)
          .flatten
          .flatMap(_.candidates)
      ).map(_.uri).toSet.intersect(allowedUris)

      // Using pagerank
      println(s"node $node")
      val oIndices = index(digraph, uris) match {
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

  private def index(digraph: DirectedGraph, uris: Set[String])
  : Indices = uris.size match {
    case zero if zero == 0 => Nil
    case _ => {
      // Run the pagerank
      val nWeight = new graph.nodes.biased.Uniform(digraph, uris.asJava)
      val eWeight = new AttachedWeight(
        digraph,
        Constants.Graph.Edges.Attribute.completeWlm,
        Constants.Graph.Edges.Attribute.normalizedCwlm)

      Pagerank.weighted(digraph, nWeight, eWeight, 0.8)

      // Produce the indices
      val topNodes = digraph.getNodes.asScala.toList
        .sortBy(-_.getScore).take(uris.size)
      val topScores = rescaleD(
        rescaleD(topNodes.map(_.getScore().toDouble)
        ).map(s => math.exp(2 * s))
      ).map(s => s * math.log(1 + uris.size.toDouble))

      topNodes.zip(topScores).map(p => {
        val node = p._1
        val score = p._2
        println(s"$node -> $score")
        Index(node.getId(), score)
      })
    }
  }

  def constructGraph(r: Resource): Option[(DirectedGraph, Set[String])] = {
    // Collect the candidate senses
    val candidates = skim(mergeOptions2List(
      r.title.oSpots,
      r.oKeywords.map(ks => mergeOptions2List(ks.map(_.oSpots): _*).flatten),
      r.oCategories.map(cs => mergeOptions2List(cs.map(_.oSpots): _*).flatten),
      r.oDomains.map(os => mergeOptions2List(os.map(_.oSpots): _*).flatten),
      r.oSubdomains.map(ds => mergeOptions2List(ds.map(_.oSpots): _*).flatten),

      // .. from Tocs
      r.oTocs.map(ts => mergeOptions2List(ts.flatMap(_.nodesRec().map(_.oSpots)): _*).flatten),

      // .. from Descriptions
      r.oDescriptions.map(ds => mergeOptions2List(ds.map(_.oSpots): _*).flatten)

    ).flatten.flatMap(_.candidates))

    // Build a first digraph
    val uris = candidates.map(_.uri.toLowerCase)
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
        val uris2 = biggestCC.map(_.getId)
        Some(
          GraphFactory.smart2(uris2.asJava, 10.5),
          uris2
        )
      }
    }
  }

  def skim(candidates: List[Candidate]): List[Candidate] = {
    candidates.filterNot(c => c match {
      case Spotlight(_, _, scores, _) => (scores.finalScore < 0.5)
    })
  }
}
