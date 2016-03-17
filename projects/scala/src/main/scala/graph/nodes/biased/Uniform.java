package graph.nodes.biased;

import graph.DirectedGraph;
import graph.nodes.Weight;

import java.util.Collection;

public class Uniform extends Weight {
    Collection<String> m_biased;
    int m_nbBiases;

    public Uniform(DirectedGraph graph, Collection<String> biased) {
        super(graph);

        m_nbBiases = biased.size();
        m_biased = biased;
    }

    @Override
    public Double of(String nodeId) {
        if(m_biased.contains(nodeId)) {
            if(m_nbBiases > 0) {
                return 1.d / (double) m_nbBiases;
            } else {
                return 0.d;
            }
        } else {
            return 0.d;
        }
    }
}
