package rsc.indexers

import graph.edges.unbiased.AttachedWeight
import graph.{Pagerank, Utils}
import mysql.GraphFactory
import rsc.Resource
import rsc.attributes.Candidate.{Candidate, Spotlight}
import utils.Constants
import utils.Utils.mergeOptions2List

import scala.collection.JavaConverters._

class Graph {
  def index(r: Resource): Option[Resource] = {
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

    // Build the digraph
    val uris = candidates.map(_.uri.toLowerCase)
    val digraph = GraphFactory.connect(uris.asJava)

    // Index the graph
    digraph.getNodes.asScala.toList match {
      case Nil => None // fail if there are no nodes
      case nodes => {
        // Get the principal connected component
        val ccs = Utils.connectedComponents(nodes)
        val biggestCC = ccs.toList.sortBy(-_.size).head

        // Build a better graph
        val uris2 = biggestCC.map(_.getId)
        val digraph2 = GraphFactory.smart2(uris2.asJava)

        // Run the pagerank
        val nWeight = new graph.nodes.biased.Uniform(digraph2, uris2.asJava)
        val eWeight = new AttachedWeight(
          digraph2,
          Constants.Graph.Edges.Attribute.completeWlm,
          Constants.Graph.Edges.Attribute.normalizedCwlm)

        Pagerank.weighted(digraph2, nWeight, eWeight, 0.8)

        // Add the indices to the resource
        val toKeep = math.ceil(biggestCC.size.toDouble * 0.8).toInt
        val topNodes = digraph2.getNodes.asScala.toList
          .sortBy(-_.getScore).take(toKeep)

        topNodes.map(node => Index(node.getId(), node.getScoreOrZero)) match {
          case Nil => None
          case indexes => Some(
            r.copy(
              title = r.title.copy(oIndices = Some(indexes)),
              oIndexer = Some(Indexer.Graph)
            )
          )
        }
      }
    }
  }

  def skim(candidates: List[Candidate]): List[Candidate] = {
    candidates.filterNot(c => c match {
      case Spotlight(_, _, scores, _) => (scores.finalScore < 0.5)
    })
  }
}
