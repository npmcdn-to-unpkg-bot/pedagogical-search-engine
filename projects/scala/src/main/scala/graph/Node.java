package graph;

import java.util.*;

public class Node implements Comparable<Node> {
    private String m_id = null;
    private Map<String, Node> m_in = new HashMap<String, Node>();
    private Map<String, Node> m_out = new HashMap<String, Node>();
    private HashMap<String, Object> m_nodeAttr = new HashMap<String, Object>();
    private HashMap<String, HashMap<String, Object>> m_edgeAttr = new HashMap<String, HashMap<String, Object>>();
    private Object None = new Object();

    public Node(String id, List<Node> in, List<Node> out) {
        m_id = id;
        addIn(in);
        addOut(out);
    }

    public Node(String id) {
        this(id, new ArrayList<Node>(), new ArrayList<Node>());
    }

    public Collection<String> getConnectedNodeIds() {
        Collection<String> c = new ArrayList<String>();
        c.addAll(m_in.keySet());
        c.addAll(m_out.keySet());
        return c;
    }

    public void addEdgeAttr(String dstNode, String attrName, Object o) {
        if(!m_edgeAttr.containsKey(dstNode)) {
            m_edgeAttr.put(dstNode, new HashMap<String, Object>());
        }
        m_edgeAttr.get(dstNode).put(attrName, o);
    }

    public void addEdgeAttr(String dstNode, String attrName) {
        addEdgeAttr(dstNode, attrName, None);
    }

    public void addNodeAttr(String name, Object o) {
        m_nodeAttr.put(name, o);
    }

    public void addNodeAttr(String name) {
        addNodeAttr(name, None);
    }

    public boolean hasNodeAttr(String name) {
        return m_nodeAttr.containsKey(name);
    }

    public boolean hasEdgeAttr(String dstNode, String attrName) {
        return m_edgeAttr.containsKey(dstNode) && m_edgeAttr.get(dstNode).containsKey(attrName);
    }

    public Object getNodeAttr(String name) {
        if(hasNodeAttr(name)) {
            return m_nodeAttr.get(name);
        } else {
            return null;
        }
    }
    public Object getEdgeAttr(String dstNode, String attrName) {
        if(m_edgeAttr.containsKey(dstNode)) {
            if(m_edgeAttr.get(dstNode).containsKey(attrName)) {
                return m_edgeAttr.get(dstNode).get(attrName);
            }
        }
        return null;
    }

    public void setScore(Double d) {
        m_nodeAttr.put("score", d);
    }

    public Double getScore() {
        return (Double) getNodeAttr("score");
    }

    public Double getScoreOrZero() {
        Object o = getNodeAttr("score");
        if(o == null) {
            return 0.d;
        } else {
            return (Double) getNodeAttr("score");
        }
    }

    public String getId() {
        return m_id;
    }

    public int inDegree() {
        return m_in.size();
    }

    public int outDegree() {
        return m_out.size();
    }

    public int totalDegree() {
        return inDegree() + outDegree();
    }

    public Collection<Node> getIn() {
        return m_in.values();
    }

    public Collection<Node> getOut() {
        return m_out.values();
    }


    public void addIn(Collection<Node> nodes) {
        for(Node node: nodes) {
            m_in.put(node.getId(), node);
        }
    }

    public void addIn(Node node) {
        addIn(Arrays.asList(node));
    }

    public void addOut(Collection<Node> nodes) {
        for(Node node: nodes) {
            m_out.put(node.getId(), node);
        }
    }

    public void addOut(Node node) {
        addOut(Arrays.asList(node));
    }

    public boolean hasIn(String id) {
        return m_in.containsKey(id);
    }

    public boolean hasOut(String id) {
        return m_out.containsKey(id);
    }

    public boolean has(String id) {
        return (hasIn(id) || hasOut(id));
    }

    public boolean removeIn(Collection<String> ids) {
        boolean b = false;
        for(String id: ids) {
            if(hasIn(id)) {
                m_in.remove(id);
                b = true;
            }
        }
        return b;
    }
    public boolean removeIn(String id) {
        return removeIn(Arrays.asList(id));
    }

    public boolean removeOut(Collection<String> ids) {
        boolean b = false;
        for(String id: ids) {
            if (hasOut(id)) {
                m_out.remove(id);

                // remove associated properties
                if(m_edgeAttr.containsKey(id)) {
                    m_edgeAttr.remove(id);
                }

                b = true;
            }
        }
        return b;
    }

    public boolean removeOut(String id) {
        return removeOut(Arrays.asList(id));
    }

    public boolean remove(Collection<String> ids) {
        boolean b = false;
        for(String id: ids) {
            if(hasIn(id)) {
                removeIn(id);
                b = true;
            }
            if(hasOut(id)) {
                removeOut(id);
                b = true;
            }
        }
        return b;
    }

    public boolean remove(String id) {
        return remove(Arrays.asList(id));
    }

    public String toString() {
//        String sin = "";
//        String sout = "";
//        for(Node in: getIn()) {
//            sin += String.format("%s/", in.m_id);
//        }
//        for(Node out: getOut()) {
//            sout += String.format("%s/", out.m_id);
//        }
        return String.format("%f %s (%d,%d)",
                getScoreOrZero(),
                m_id,
                inDegree(),
                outDegree());
    }

    public int compareTo(Node node) {
        return getScoreOrZero().compareTo(node.getScoreOrZero());
    }
}
