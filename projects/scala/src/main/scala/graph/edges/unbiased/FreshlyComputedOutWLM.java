package graph.edges.unbiased;

import graph.DirectedGraph;
import graph.edges.Weight;
import graph.nodes.Node;
import wlm.Compute;

import java.util.*;

/**
 * WLM considering only out-links
 */
public class FreshlyComputedOutWLM extends Weight {
    Map<String, Map<String, Double>> m_weights = new HashMap<String, Map<String, Double>>();

    public FreshlyComputedOutWLM(DirectedGraph graph) {
        super(graph);

        // Precompute all weights
        Collection<Node> nodes = m_graph.getNodes();

        // .. for each out(node) links
        for(Node n1: nodes) {
            Map<String, Double> n1Weights = new HashMap<String, Double>();

            // Compute A set
            Set<String> A = new HashSet<String>();
            for(Node n2: n1.getOut()) {
                A.add(n2.getId());
            }

            // n1 -> n2 links
            for(Node n2: n1.getOut()) {
                // Compute B set
                Set<String> B = new HashSet<String>();
                for(Node n3: n2.getOut()) {
                    B.add(n3.getId());
                }

                // WLM weight
                double weight = Compute.relatedness(A, B);
                n1Weights.put(n2.getId(), weight);
            }

            // save weights
            m_weights.put(n1.getId(), n1Weights);
        }
    }

    public double notFoundWeight(String n1, String n2) {
        Node a = m_graph.getNode(n1);
        Node b = m_graph.getNode(n2);
        Set<String> A = new HashSet<String>();
        Set<String> B = new HashSet<String>();

        for(Node c: a.getOut()) {
            A.add(c.getId());
        }
        for(Node c: b.getOut()) {
            B.add(c.getId());
        }

        return Compute.relatedness(A, B);
    }

    @Override
    public double of(String a, String b) {
        if(m_weights.containsKey(a)) {
            Map<String, Double> weights =  m_weights.get(a);
            if(weights.containsKey(b)) {
                return weights.get(b);
            } else {
                return notFoundWeight(a, b);
            }
        } else {
            return notFoundWeight(a, b);
        }
    }
}
