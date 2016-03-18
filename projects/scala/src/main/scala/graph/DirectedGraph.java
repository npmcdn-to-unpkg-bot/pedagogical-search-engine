package graph;

import graph.nodes.Node;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import utils.Files;

import java.io.FileNotFoundException;
import java.util.*;

public class DirectedGraph {
    private Map<String, Node> m_nodes = new HashMap<String, Node>();
    private Collection<Node> m_cacheVar1 = null;

    public DirectedGraph(Collection<Node> nodes) {
        addNodes(nodes);
    }

    public DirectedGraph() {
        this(new ArrayList<Node>());
    }

    public Collection<Node> getNodes() {
        return m_nodes.values();
    }

    public Collection<Node> getNonDanglingNodes() {
        // .. trick, this function is only used in pagerank
        // todo: then implement it in the pagerank or elsewhere
        if(m_cacheVar1 == null) {
            Collection<Node> nodes = new ArrayList<Node>();
            for(Node node: m_nodes.values()) {
                if(node.outDegree() > 0) {
                    nodes.add(node);
                }
            }
            m_cacheVar1 = nodes;
        }
        return m_cacheVar1;
    }

    public boolean contains(String id) {
        return m_nodes.containsKey(id);
    }

    public Collection<String> getIDs() {
        return m_nodes.keySet();
    }

    public DirectedGraph undirect() {
        for(Node nodeA: m_nodes.values()) {
            for(Node nodeB: nodeA.getOut()) {
                addEdge(nodeB.getId(), nodeA.getId());
            }
        }
        return this;
    }

    public Node getNode(String id) {
        if(m_nodes.containsKey(id)) {
            return m_nodes.get(id);
        } else {
            return null;
        }
    }

    public void addNodes(Collection<Node> nodes) {
        for(Node node: nodes) {
            m_nodes.put(node.getId(), node);
        }
    }

    public void addNode(Node node) {
        addNodes(Arrays.asList(node));
    }

    public Node getOrCreate(String id) {
        Node n = getNode(id);
        if(n == null) {
            n = new Node(id);
            addNode(n);
        }
        return n;
    }

    private void removeNodesOnly(Collection<String> ids) {
        for(String id: ids) {
            m_nodes.remove(id);
        }
    }

    private void removeNodeOnly(String id) {
        removeNodesOnly(Arrays.asList(id));
    }

    public void removeNodes(Collection<String> ids) {
        for(Node n: m_nodes.values()) {
            n.remove(ids);
        }
        removeNodesOnly(ids);
    }

    public void removeNode(String id) {
        removeNodes(Arrays.asList(id));
    }

    public void removeEdges(Collection<ImmutablePair<String, String>> idPairs) {
        for(ImmutablePair<String, String> p: idPairs) {
            getNode(p.getLeft()).removeOut(p.getRight());
            getNode(p.getRight()).removeIn(p.getLeft());
        }
    }

    public void removeEdge(String fromId, String toId) {
        removeEdges(Arrays.asList(new ImmutablePair<String, String>(fromId, toId)));
    }

    public void removeEdgeInBothDirections(Collection<ImmutablePair<String, String>> idPairs) {
        for(Pair<String, String> p: idPairs) {
            getNode(p.getLeft()).remove(p.getRight());
            getNode(p.getRight()).remove(p.getLeft());
        }
    }

    public void removeEdgeInBothDirections(String fromId, String toId) {
        removeEdgeInBothDirections(Arrays.asList(new ImmutablePair<String, String>(fromId, toId)));
    }

    public boolean hasEdge(String fromId, String toId) {
        Node n1 = getNode(fromId);
        Node n2 = getNode(toId);
        if(n1 != null & n2 != null) {
            return n1.hasOut(n2.getId()) && n2.hasIn(n1.getId());
        } else {
            return false;
        }
    }

    public boolean addEdge(String fromId, String toId) {
        Node n1 = getNode(fromId);
        Node n2 = getNode(toId);
        if(n1 != null & n2 != null) {
            n1.addOut(n2);
            n2.addIn(n1);
            return true;
        } else {
            return false;
        }
    }

    public Object getEdgeAttr(String fromId, String toId, String attr) {
        return getNode(fromId).getEdgeAttr(toId, attr);
    }

    public void addEdgeAttr(String fromId, String toId, String attr, Object value) {
        getNode(fromId).addEdgeAttr(toId, attr, value);
    }

    public int nbNodes() {
        return m_nodes.size();
    }

    public String toString() {
        String r = "DirectedGraph:\n";

        // Get top k nodes
        List<Node> l = new ArrayList<Node>(m_nodes.values());
        Collections.sort(l);

        // Print top nodes
        int k = Math.min(10, l.size());
        for(int i = l.size()-1; i > (l.size()-k-1); i--) {
            Node node = l.get(i);
            r += String.format("\t%s\n", node.toString());
        }
        return r;
    }

    public void resetScores() {
        for(Node node: getNodes()) {
            node.setScore(0.d);
        }
    }

    public Map<String, Double> getTopSpots(int n, Set<String> whitelist) {
        List<Node> l = new ArrayList<Node>(getNodes());
        Collections.sort(l);

        // Get top nodes
        Map<String, Double> top = new HashMap<String, Double>();
        int selected = 0;
        for(int i = l.size()-1; i > -1 && selected < n; i--) {
            Node node = l.get(i);
            String uri = node.getId();
            Double score = node.getScoreOrZero();

            if(whitelist.contains(uri)) {
                top.put(uri, score);
                selected++;
                /*
                System.out.println(String.format(
                        "Top spot %s: %s %s",
                        String.valueOf(top.size()),
                        uri,
                        String.valueOf(score)

                ));
                */
            }
        }
        return top;
    }

    public List<utils.java.Pair<Node, Double>> getTopNodes(int n) {
        List<Node> l = new ArrayList<Node>(getNodes());
        Collections.sort(l);

        // Get top nodes
        List<utils.java.Pair<Node, Double>> top = new ArrayList<utils.java.Pair<Node, Double>>();
        for(int i = l.size()-1; i > (l.size()-n-1) && i > -1; i--) {
            Node node = l.get(i);
            top.add(new utils.java.Pair<Node, Double>(node, node.getScore()));
        }
        return top;
    }

    public String toJSONGraph(Collection<String> specialNodes, String attr) {
        GraphVisualizer visual = new GraphVisualizer();

        for(Node nodeA: m_nodes.values()) {
            // Create node
            String a = nodeA.getId();
            double aScore = nodeA.getScoreOrZero();
            String aName = String.format("%s, %f", a, aScore);
            visual.addNode(aName);

            // Create out edges
            for(Node nodeB: nodeA.getOut()) {
                String b = nodeB.getId();
                double bScore = nodeB.getScoreOrZero();
                String bName = String.format("%s, %f", b, bScore);

                visual.addNode(bName);

                double weight = 1;
                if(nodeA.hasEdgeAttr(b, attr)) {
                    weight = (Double) nodeA.getEdgeAttr(b, attr);
                }
                visual.addEdge(aName, bName, weight);
            }
        }

        // Color special nodes
        for(String a: specialNodes) {
            Node nodeA = getNode(a);
            double aScore = nodeA.getScoreOrZero();
            String aName = String.format("%s, %f", a, aScore);
            visual.addGroup(aName, 1);
        }

        // Get top k nodes
        List<Node> l = new ArrayList<Node>(m_nodes.values());
        Collections.sort(l);

        // Print top nodes
        int k = 5;
        for(int i = l.size()-1; i > (l.size()-k-1) && i > -1; i--) {
            Node node = l.get(i);
            double score = node.getScoreOrZero();
            String aName = String.format("%s, %f", node.getId(), score);
            visual.addGroup(aName, 2);
        }

        return visual.toJSON();
    }

    public void toJSONFile(Collection<String> whitelist, String fn, String attr) throws FileNotFoundException {
        Files.write(toJSONGraph(whitelist, attr), fn);
    }
}
