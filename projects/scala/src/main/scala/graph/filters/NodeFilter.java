package graph.filters;

import graph.nodes.Node;

public abstract class NodeFilter {
    boolean m_inverse = false;

    public NodeFilter not() {
        m_inverse = !m_inverse;
        return this;
    }

    public boolean filter(Node node) {
        return cdt(node) ^ m_inverse;
    }

    protected abstract boolean cdt(Node node);
}
