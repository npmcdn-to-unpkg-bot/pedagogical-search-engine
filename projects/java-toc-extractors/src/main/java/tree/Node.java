package tree;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Node<E> {
    private E content = null;
    private ArrayList<Node<E>> children = new ArrayList<Node<E>>();
    public Map<String, Object> attributes = new HashMap<String, Object>();

    public Node(E c) {
        content = c;
    }

    public E getContent() {
        return content;
    }

    public void addChild(Node<E> child) {
        children.add(child);
    }

    public void addChild(E child) {
        children.add(new Node(child));
    }

    public JSONObject toJSON() {
        // Node
        JSONObject o = new JSONObject();
        if(content != null) {
            o.put("content", content.toString());
        }

        // Attributes
        for(Map.Entry<String, Object> e: attributes.entrySet()) {
            o.put(e.getKey(), e.getValue());
        }

        // children
        JSONArray childrenArray = new JSONArray();
        for(Node c: children) {
            childrenArray.put(c.toJSON());
        }
        o.put("children", childrenArray);
        return o;
    }
}
