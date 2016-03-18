package agents

import graph.Pagerank
import graph.edges.unbiased.AttachedWeight
import mysql.GraphFactory
import utils.Constants
import scala.collection.JavaConverters._

object IndexWithGraphs {
  def main(args: Array[String]): Unit = {

    val uris = List("support_vector_machine", "positive-definite_kernel",
      "linear_classifier", "feature_vector", "")
    val digraph = GraphFactory.noDangling(uris.asJava)
    val nWeight = new graph.nodes.biased.Uniform(digraph, uris.asJava)
    val eWeight = new AttachedWeight(
      digraph,
      Constants.Graph.Edges.Attribute.completeWlm,
      Constants.Graph.Edges.Attribute.normalizedCwlm)

    Pagerank.weighted(digraph, nWeight, eWeight, 0.8)
    val nodes = digraph.getNodes.asScala
    print(digraph)

    // visualization
    //digraph.toJSONFile(uris.asJava, "test.json", Constants.Graph.Edges.Attribute.normalizedCwlm)
  }
}
