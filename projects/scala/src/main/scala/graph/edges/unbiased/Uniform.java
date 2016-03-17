package graph.edges.unbiased;

import graph.DirectedGraph;
import graph.edges.Weight;
import graph.nodes.Node;

public class Uniform extends Weight {
    public Uniform(DirectedGraph graph) {
        super(graph);
    }

    @Override
    public double of(String a, String b) {
        Node nodeA = m_graph.getNode(a);
        return 1.d / (double) nodeA.outDegree();
    }
}
