package graph;

import graph.filters.NodeFilter;
import graph.nodes.Node;
import utils.Constants;

import java.util.*;

public class Transformation {
    public static void normalizeNodeFeature(DirectedGraph graph,
                                            String srcAttr,
                                            String dstAttr) {
        Collection<Node> nodes = graph.getNodes();

        // get sum
        double sum = 0.d;
        for(Node node: nodes) {
            double weight = (Double) node.getNodeAttr(srcAttr);
            sum += weight;
        }

        // normalize biases
        if(sum != 0.d) {
            for(Node node: nodes) {
                double weight = (Double) node.getNodeAttr(srcAttr);
                node.addNodeAttr(dstAttr, weight / sum);
            }
        }
    }

    public static void normalizeEdgeFeatureOut(DirectedGraph graph,
                                               String srcAttr,
                                               String dstAttr) {
        Collection<Node> nodes = graph.getNodes();

        // for each node
        for(Node aNode: nodes) {
            String a = aNode.getId();

            // Normalize its edges
            double minWeight = Double.MAX_VALUE;
            Map<String, Double> weights = new HashMap<String, Double>();
            for(Node bNode: aNode.getOut()) {
                String b = bNode.getId();
                double weight = (Double) aNode.getEdgeAttr(b, srcAttr);
                weights.put(b, weight);

                // minWeight for future shift
                minWeight = Math.min(minWeight, weight);
            }

            // Shift weights, they must be all strictly positive
            if(minWeight < 0) {
                double eps = 1;
                double shift = minWeight * (-1) + eps;
                for(String b: weights.keySet()) {
                    // Compute new score
                    double newScore = weights.get(b) + shift;

                    // Save it
                    weights.put(b, newScore);
                }
            }

            // Compute sum for future normalization
            double sum = 0.d;
            for(String b: weights.keySet()) {
                double score = weights.get(b);
                sum += Math.abs(score);
            }

            // Normalize to 1 and Save
            for(String b: weights.keySet()) {
                double newScore = weights.get(b);
                if(sum > 0) {
                    newScore = newScore / sum;
                }

                aNode.addEdgeAttr(b, dstAttr, newScore);
            }
        }
    }

    /**
     * Remove nodes if one of the filter does return false on it.
     */
    public static void filterNodes(DirectedGraph graph, Collection<NodeFilter> filter) {
        // Spot nodes to remove
        Collection<String> toRemove = new ArrayList<String>();

        for(Node node: graph.getNodes()) {
            for(NodeFilter f: filter) {
                if(!f.filter(node)) {
                    toRemove.add(node.getId());
                    break;
                }
            }
        }

        // remove them
        graph.removeNodes(toRemove);
    }

    public static void filterNodes(DirectedGraph graph, NodeFilter filter) {
        filterNodes(graph, Arrays.asList(filter));
    }





    // Normalization methods
    public static void normalizeCWLM(DirectedGraph graph) {
        normalizeEdgeFeatureOut(graph,
                Constants.Graph.Edges.Attribute.completeWlm,
                Constants.Graph.Edges.Attribute.normalizedCwlm);
    }
}
