package graph.filters;

import graph.nodes.Node;

public class NodeInDegree extends NodeFilter {
    int m_degree;

    public NodeInDegree(int degree) {
        m_degree = degree;
    }

    @Override
    protected boolean cdt(Node node) {
        return node.inDegree() >= m_degree;
    }
}
