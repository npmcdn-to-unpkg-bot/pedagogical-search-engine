package toc;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tree.Node;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static <E> Node<E> expandFlatEntries(List<FlatEntry<E>> entries) {
        // Create root node
        Node<E> root = new Node<E>(null);
        ArrayList<Pair<FlatEntry<E>, Node<E>>> processed = new ArrayList<Pair<FlatEntry<E>, Node<E>>>();

        for(int i = 0; i < entries.size(); i++) {
            FlatEntry<E> currentFTE = entries.get(i);
            int currentLevel = currentFTE.getLevel();

            // Create Node
            Node<E> currentNode = new Node(currentFTE.getContent());

            // Search Parent
            Node<E> parent = null;
            for(int j = processed.size()-1; j >= 0 && parent == null; j--) {
                Pair<FlatEntry<E>, Node<E>> inspectedEntry = processed.get(j);
                if(inspectedEntry.getValue0().getLevel() < currentLevel) {
                    parent = inspectedEntry.getValue1();
                }
            }
            if(parent == null) {
                parent = root;
            }

            // Attach
            parent.addChild(currentNode);

            // Save Node as processed
            processed.add(new Pair<FlatEntry<E>, Node<E>>(currentFTE, currentNode));
        }
        return root;
    }
}
