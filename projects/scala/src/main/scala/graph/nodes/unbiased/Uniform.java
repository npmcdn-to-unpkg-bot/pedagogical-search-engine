package graph.nodes.unbiased;

import graph.DirectedGraph;
import graph.nodes.Weight;

public class Uniform extends Weight {
    int m_nbNodes;

    public Uniform(DirectedGraph graph) {
        super(graph);

        m_nbNodes = m_graph.nbNodes();
    }

    @Override
    public Double of(String nodeId) {
        return 1.d / (double) m_nbNodes;
    }
}
