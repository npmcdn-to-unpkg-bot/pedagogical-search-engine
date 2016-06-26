package rsc.indexers

import graph.{DirectedGraph, Pagerank}
import graph.edges.unbiased.AttachedWeight
import mysql.GraphFactory
import rsc.Resource
import rsc.Types._
import utils.Constants

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._

class Graph1(_ec: ExecutionContext)
  extends Graph2(_ec, spotlightCoreThreshold = 0.9) {

  // Minimum WLM scores for the links that we follow
  val wlmLimit = 10.5

  // Number of indexes to create
  val nbIndexes = 100


  // Public methods
  override def index(r: Resource)
  : Future[Option[Resource]] = {
    // Create the expanded graph
    val mapping = miniGraphMapping(r)
    val coreUris = mapping.keySet

    GraphFactory.follow2(coreUris, wlmLimit).map {
      case expanded =>
        // Index the entire resource
        val indices = pageRankOn(expanded, coreUris)

        // Index the table of contents
        val newOTocs = r.oTocs.map(tocs => {
          tocs.map(toc => {
            val newNodes = indexNodes(toc.nodes)(expanded)
            toc.copy(nodes = newNodes)
          })
        })

        // Create the new resource
        Some(r.copy(
          title = r.title.copy(oIndices = Some(new Indices(indices))),
          oTocs = newOTocs,
          oIndexer = Some(Indexer.GraphChoiceBased)
        ))
    }
  }

  // Private methods
  def pageRankOn(digraph: DirectedGraph, biases: Set[String])
  : List[Index] = digraph.nbNodes() match {
    case zero if zero == 0 => Nil
    case n =>
      // todo: remove
      println(s"PageRank on $n nodes")

      // Run the simple biased PageRank
      val biasesForJava: Map[String, java.lang.Double] = biases.toList.map {
        case uri => (uri, new java.lang.Double(1))
      }.toMap
      val nWeight = new graph.nodes.biased.Tunable(digraph, biasesForJava.asJava)
      val eWeight = new AttachedWeight(
        digraph,
        Constants.Graph.Edges.Attribute.completeWlm,
        Constants.Graph.Edges.Attribute.normalizedCwlm)

      Pagerank.weighted(digraph, nWeight, eWeight, prDumpingFactor)

      // Produce the indexes
      val topNodes = digraph.getNodes.asScala.toList
        .sortBy(-_.getScore).take(nbIndexes)
      topNodes.map(node => {
        val uri = node.getId
        val score = node.getScore
        new Index(uri, score)
      })
  }

  def indexNodes(nodes: Nodes)(implicit graph: DirectedGraph)
  : Nodes = nodes match {
    case Nil => Nil
    case _ => nodes.map(node => {
      // Index children
      val newChildren = indexNodes(node.children)

      // Index the current entry
      val choices = choicesFrom(
        nodes = List(node),
        offset = 0,
        blackList = Set(),
        threshold = 0
      )
      val uris = choices.flatten.map(_.uri).toSet

      // Using pagerank
      val oIndices = pageRankOn(graph, uris) match {
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
}
