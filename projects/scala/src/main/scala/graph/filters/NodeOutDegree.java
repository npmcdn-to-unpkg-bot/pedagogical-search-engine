package graph.filters;

import graph.Node;

public class NodeOutDegree extends NodeFilter {

    private int m_degree;

    public NodeOutDegree(int degree) {
        m_degree = degree;
    }

    @Override
    protected boolean cdt(Node node) {
        return node.outDegree() >= m_degree;
    }
}
