package graph;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.Files;

import java.io.FileNotFoundException;

public class GraphVisualizer {
    private JSONObject m_obj = new JSONObject();
    private JSONArray m_nodes = new JSONArray();
    private JSONArray m_edges = new JSONArray();

    public GraphVisualizer() {

    }

    public void addNode(String name, int group) {
        if(findNodeIndex(name) == -1) {
            JSONObject obj = new JSONObject();
            obj.put("name", name);
            if(group != -1) {
                obj.put("group", group);
            }
            m_nodes.put(obj);
        }
    }

    public void addNode(String name) {
        addNode(name, -1);
    }

    public int findNodeIndex(String name) {
        for(int i = 0; i < m_nodes.length(); i++) {
            if(((JSONObject) m_nodes.get(i)).get("name").equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public void addEdge(String a, String b, double weight) {
        JSONObject obj = new JSONObject();
        obj.put("source", findNodeIndex(a));
        obj.put("target", findNodeIndex(b));
        obj.put("weight", weight);
        m_edges.put(obj);
    }

    public void addGroup(String a, int group) {
        int nodeIdx = findNodeIndex(a);
        if(nodeIdx != -1) {
            JSONObject obj = (JSONObject) m_nodes.get(nodeIdx);
            if(obj.has("group")) {
                obj.remove("group");
            }
            obj.put("group", group);
        }
    }

    public void compress() {
        // todo: discard nodes where you can
    }

    public String toJSON() {
        // Construct back object
        m_obj.put("nodes", m_nodes);
        m_obj.put("edges", m_edges);

        return m_obj.toString(2);
    }

    public void toFile(String fn) throws FileNotFoundException {
        Files.write(toJSON(), fn);
    }
}
