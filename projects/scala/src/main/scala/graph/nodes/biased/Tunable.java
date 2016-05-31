package graph.nodes.biased;

import graph.DirectedGraph;
import graph.nodes.Weight;

import java.util.Map;

public class Tunable extends Weight {
    private Map<String, Double> m_biases;
    private Double m_sumBiases;

    public Tunable(DirectedGraph graph, Map<String, Double> biases) {
        super(graph);

        m_biases = biases;

        // Compute the sum
        Double sum = 0.d;
        for(Double bias: biases.values()) {
            sum += bias;
        }

        m_sumBiases = sum;
    }

    @Override
    public Double of(String nodeId) {
        if(m_biases.containsKey(nodeId)) {
            if(m_sumBiases > 0) {
                return m_biases.get(nodeId) / m_sumBiases;
            } else {
                return 0.d;
            }
        } else {
            return 0.d;
        }
    }
}
