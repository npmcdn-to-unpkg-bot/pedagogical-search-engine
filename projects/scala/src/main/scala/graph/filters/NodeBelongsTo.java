package graph.filters;

import graph.Node;

import java.util.Collection;

public class NodeBelongsTo extends NodeFilter {
    Collection<String> m_ids;

    public NodeBelongsTo(Collection<String> ids) {
        m_ids = ids;
    }

    @Override
    protected boolean cdt(Node node) {
        return m_ids.contains(node.getId());
    }
}
