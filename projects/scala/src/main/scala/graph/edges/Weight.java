package graph.edges;

import graph.DirectedGraph;

import java.util.ArrayList;
import java.util.List;

public abstract class Weight {

    protected DirectedGraph m_graph;

    public Weight(DirectedGraph graph) {
        m_graph = graph;
    }

    public abstract double of(String a, String b);

    public void saveOnGraph(String attr) {
        List<String> ids = new ArrayList<String>();
        ids.addAll(m_graph.getIDs());

        for(int i = 0; i < ids.size(); i++) {
            for(int j = 0; j < ids.size(); j++) {
                if(i != j) {
                    String id1 = ids.get(i);
                    String id2 = ids.get(j);

                    m_graph.getNode(id1).addEdgeAttr(id2,
                            attr,
                            this.of(id1, id2));
                }
            }
        }
    }
}
