package graph.filters;

import graph.nodes.Node;

import java.util.Collection;

public class Degree extends NodeFilter {
    Collection<String> m_whiteList;
    int m_k;

    public Degree(Collection<String> whiteList, int k) {
        m_whiteList = whiteList;
        m_k = k;
    }

    @Override
    protected boolean cdt(Node node) {
        String nodeId = node.getId();
        boolean whitelisted = m_whiteList.contains(nodeId);
        int inDegree = node.inDegree();
        int outDegree = node.outDegree();

        if(!whitelisted && (inDegree + outDegree) < m_k) {
            return false;
        }

        return true;
    }
}
