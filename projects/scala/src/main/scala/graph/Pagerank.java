package graph;

import graph.nodes.Node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Pagerank {
    public static void weighted(DirectedGraph digraph,
                                graph.nodes.Weight nWeight,
                                graph.edges.Weight eWeight,
                                double dampingFactor) {

        Collection<Node> nodes = digraph.getNodes();

        if(nodes.size() != 0) {
            // iteratively compute PageRank
            double epsilon = 1.d / Math.pow(10.d, 5.d);
            int maxIter = 10*1000;
            double maxDelta = epsilon+1.d;
            int iter = 0;
            Map<String, Double> newScores = new HashMap<String, Double>();
            while (maxDelta > epsilon && maxIter > iter) {

                // compute new scores
                maxDelta = -1.d;
                // .. we don't have to clear newScores, they will be override
                for (Node node: nodes) {
                    String nodeId = node.getId();
                    double oldScore = node.getScoreOrZero();
                    double d = dampingFactor;

                    // Random jumper component
                    double randomJump = nWeight.of(nodeId);

                    // Incoming links component
                    double cumulative = 0.d;
                    for(Node inNode: node.getIn()) {
                        double edgeWeight = eWeight.of(inNode.getId(), nodeId);
                        cumulative += edgeWeight * inNode.getScoreOrZero();
                    }
                    double newScore = (1 - d) * randomJump + d * cumulative;

                    // save it
                    newScores.put(nodeId, newScore);
                    maxDelta = Math.max(maxDelta, Math.abs(oldScore - newScore));
                }

                // update scores
                for(Node node: nodes) {
                    node.setScore(newScores.get(node.getId()));
                }

                // iterate
                iter += 1;
            }
        }
    }
}
