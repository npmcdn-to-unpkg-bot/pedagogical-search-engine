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
          // Index the tocs
          val newOTocs = r.oTocs.map(_.map(toc => {
            val newNodes = indexNodes(toc.nodes)(digraph)
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

  private def indexNodes(nodes: Nodes)(implicit digraph: DirectedGraph)
  : Nodes = nodes match {
    case Nil => Nil
    case _ => nodes.map(node => {
      // Index children
      val newChildren = indexNodes(node.children)

      // Index the current Node
      val subNodes = node::node.childrenRec()
      val uris = skim(
        mergeOptions2List(subNodes.map(_.oSpots): _*)
          .flatten
          .flatMap(_.candidates)
      ).map(_.uri).toSet

      // Using pagerank
      println(s"node $node")
      val nodeIndices = index(digraph, uris)

      // Create the new node
      node.copy(
        children = newChildren,
        oIndices = Some(nodeIndices)
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
      val toKeep = math.ceil(uris.size.toDouble * 0.8).toInt
      val topNodes = digraph.getNodes.asScala.toList
        .sortBy(-_.getScore).take(toKeep)
      println(s"taking $toKeep, uris: {$uris}")
      topNodes.map(println(_))
      val topScores = rescaleD(
        normalizeD(
          topNodes.map(_.getScore().toDouble)
        )
      )
      val topURIs = topNodes.map(_.getId())

      topURIs.zip(topScores).map(p => Index(p._1, p._2))
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
    val digraph = GraphFactory.connect(uris.asJava)

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
          GraphFactory.smart2(uris2.asJava),
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
