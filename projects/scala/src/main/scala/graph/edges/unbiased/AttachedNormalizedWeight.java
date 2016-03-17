package graph.edges.unbiased;

import graph.DirectedGraph;
import graph.edges.Weight;
import graph.nodes.Node;


public class AttachedNormalizedWeight extends Weight {
    private String m_attr;

    public AttachedNormalizedWeight(DirectedGraph graph, String attr) {
        super(graph);
        m_attr = attr;
    }

    @Override
    public double of(String a, String b) {
        Node nodeA = m_graph.getNode(a);
        if(nodeA.hasEdgeAttr(b, m_attr)) {
            return (Double) nodeA.getEdgeAttr(b, m_attr);
        } else {
            return 0.d;
        }
    }
}
