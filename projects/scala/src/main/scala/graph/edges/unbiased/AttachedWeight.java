package graph.edges.unbiased;

import graph.DirectedGraph;
import graph.Transformation;
import graph.edges.Weight;
import graph.Node;


public class AttachedWeight extends Weight {
    private String m_attr;

    public AttachedWeight(DirectedGraph graph, String srcAttr, String dstAttr) {
        super(graph);

        m_attr = dstAttr;
        Transformation.normalizeEdgeFeatureOut(graph, srcAttr, m_attr);
    }

    public AttachedWeight(DirectedGraph graph, String srcAttr) {
        this(graph, srcAttr, srcAttr + "Normalized");
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
