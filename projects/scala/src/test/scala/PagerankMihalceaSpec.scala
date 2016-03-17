import java.util.Arrays

import graph.edges.unbiased.AttachedWeight
import graph.{Pagerank, Node, DirectedGraph}
import scala.math.abs

import org.scalatest._

/**
  * From the book
  * title: Graph-based Natural Language Processing and Information Retrieval
  * author: Rada Mihalcea; Dragomir Radev
  * chapter: 5.2 Pagerank
  */
class PagerankMihalceaSpec extends FlatSpec with Matchers {
  // Precision epsilon
  val eps = 0.001

  def createGraph = {
    val A: String = "A"
    val B: String = "B"
    val C: String = "C"
    val D: String = "D"
    val E: String = "E"
    val F: String = "F"
    val G: String = "G"
    val digraph: DirectedGraph = new DirectedGraph
    val nA: Node = new Node(A)
    val nB: Node = new Node(B)
    val nC: Node = new Node(C)
    val nD: Node = new Node(D)
    val nE: Node = new Node(E)
    val nF: Node = new Node(F)
    val nG: Node = new Node(G)
    digraph.addNodes(Arrays.asList(nA, nB, nC, nD, nE, nF, nG))
    digraph.addEdge(A, B)
    digraph.addEdge(A, D)
    digraph.addEdge(B, D)
    digraph.addEdge(B, E)
    digraph.addEdge(C, A)
    digraph.addEdge(C, F)
    digraph.addEdge(D, C)
    digraph.addEdge(D, F)
    digraph.addEdge(D, G)
    digraph.addEdge(D, E)
    // The following node appears in the book but is an error
    // digraph.addEdge(E, G)
    digraph.addEdge(G, F)
    nA.setScore(0.25)
    nB.setScore(0.25)
    nC.setScore(0.25)
    nD.setScore(0.25)
    nE.setScore(0.25)
    nF.setScore(0.25)
    nG.setScore(0.25)
    digraph
  }

  "Pagerank" should "handle di-graphs" in {
    val digraph = createGraph

    val nWeight = new graph.nodes.unbiased.Uniform(digraph)
    val eWeight = new graph.edges.unbiased.Uniform(digraph)

    Pagerank.weighted(digraph, nWeight, eWeight, 0.85)

    val a = digraph.getNode("A").getScoreOrZero
    val b = digraph.getNode("B").getScoreOrZero
    val c = digraph.getNode("C").getScoreOrZero
    val d = digraph.getNode("D").getScoreOrZero
    val e = digraph.getNode("E").getScoreOrZero
    val f = digraph.getNode("F").getScoreOrZero
    val g = digraph.getNode("G").getScoreOrZero

    // Test scores
    abs(a - 0.035) should be < eps
    abs(b - 0.036) should be < eps
    abs(c - 0.033) should be < eps
    abs(d - 0.052) should be < eps
    abs(e - 0.048) should be < eps
    abs(f - 0.074) should be < eps
    abs(g - 0.033) should be < eps
  }

  "Pagerank" should "handle graphs" in {
    val g = createGraph.undirect

    // run pagerank
    val nWeight = new graph.nodes.unbiased.Uniform(g)
    val eWeight = new graph.edges.unbiased.Uniform(g)

    Pagerank.weighted(g, nWeight, eWeight, 0.85)

    // Test scores
    val sA = g.getNode("A").getScoreOrZero
    val sB = g.getNode("B").getScoreOrZero
    val sC = g.getNode("C").getScoreOrZero
    val sD = g.getNode("D").getScoreOrZero
    val sE = g.getNode("E").getScoreOrZero
    val sF = g.getNode("F").getScoreOrZero
    val sG = g.getNode("G").getScoreOrZero

    abs(sA - sC) should be < eps
    abs(sB - sF) should be < eps
    abs(sG - sE) should be < eps
    sG should be < sC
    sC should be < sF
    sF should be < sD
  }

  "Pagerank" should "handle weighted di-graphs" in {
    val digraph = createGraph

    // attach weights
    val attr: String = "weight"
    digraph.addEdgeAttr("A", "B", attr, 2.toDouble)
    digraph.addEdgeAttr("A", "D", attr, 1.toDouble)
    digraph.addEdgeAttr("B", "D", attr, 3.toDouble)
    digraph.addEdgeAttr("B", "E", attr, 9.toDouble)
    digraph.addEdgeAttr("C", "A", attr, 3.toDouble)
    digraph.addEdgeAttr("C", "F", attr, 4.toDouble)
    digraph.addEdgeAttr("D", "C", attr, 2.toDouble)
    digraph.addEdgeAttr("D", "E", attr, 2.toDouble)
    digraph.addEdgeAttr("D", "F", attr, 5.toDouble)
    digraph.addEdgeAttr("D", "G", attr, 3.toDouble)
    digraph.addEdgeAttr("E", "G", attr, 5.toDouble)
    digraph.addEdgeAttr("G", "F", attr, 1.toDouble)

    // run pagerank
    val nWeight = new graph.nodes.unbiased.Uniform(digraph)
    val eWeight = new AttachedWeight(digraph, attr);

    Pagerank.weighted(digraph, nWeight, eWeight, 0.85)

    // Assert final scores
    val a = digraph.getNode("A").getScoreOrZero
    val b = digraph.getNode("B").getScoreOrZero
    val c = digraph.getNode("C").getScoreOrZero
    val d = digraph.getNode("D").getScoreOrZero
    val e = digraph.getNode("E").getScoreOrZero
    val f = digraph.getNode("F").getScoreOrZero
    val g = digraph.getNode("G").getScoreOrZero
    abs(a - 0.031) should be < eps
    abs(b - 0.039) should be < eps
    abs(c - 0.027) should be < eps
    abs(d - 0.039) should be < eps
    abs(e - 0.052) should be < eps
    abs(f - 0.073) should be < eps
    abs(g - 0.030) should be < eps
  }
}