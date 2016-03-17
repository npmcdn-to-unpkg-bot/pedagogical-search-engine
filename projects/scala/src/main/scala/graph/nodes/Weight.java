package graph.nodes;

import graph.DirectedGraph;
import graph.Node;

import java.util.Comparator;

public abstract class Weight implements Comparator<String> {

    protected DirectedGraph m_graph;

    public Weight(DirectedGraph graph) {
        m_graph = graph;
    }

    public abstract Double of(String nodeId);

    public int compare(String a, String b) {
        return this.of(a).compareTo(this.of(b));
    }

    public void saveOnGraph(String attr) {
        for(Node node: m_graph.getNodes()) {
            node.addNodeAttr(
                    attr,
                    this.of(node.getId())
            );
        }
    }
}
