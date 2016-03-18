package mysql;

import graph.DirectedGraph;
import graph.nodes.Node;
import utils.Constants;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class GraphFactory {
    public static DirectedGraph batch(Collection<String> URIs, int k, int chunkSize,
                                      boolean undirected) {
        DirectedGraph digraph = new DirectedGraph();

        for(String uri: URIs) {
            // create initial nodes
            Node node = new Node(uri);
            digraph.addNode(node);
        }

        // Iterate over distance k
        List<String> visited = new ArrayList<String>();
        List<String> unvisited = new ArrayList<String>(new HashSet<String>(URIs));
        Set<String> newLinks = new HashSet<String>();
        for(int i = 1; i <= k; i++) {
            System.out.println(String.format(
                    "GraphFactory: Exploring %s entities at i = %s",
                    String.valueOf(unvisited.size()),
                    String.valueOf(i)
                    ));

            // Explore nodes by chunks
            int nbChunks = (int) Math.ceil(((double) unvisited.size()) / ((double) chunkSize));
            for(int chunkNo = 0; chunkNo < nbChunks; chunkNo++) {
                int chunkBegin = chunkNo * chunkSize;
                int chunkEnd = Math.min(unvisited.size() - 1, (chunkNo + 1) * chunkSize - 1);

                // get chunk URIs
                Collection<String> chunkURIs = unvisited.subList(chunkBegin, chunkEnd + 1);

                // fetch neighbors
                try {
                    // Create statement
                    String jointIds = QueriesUtils.escapeAndJoin(chunkURIs);
                    String queryFn = Constants.Mysql.QueriesPath.queryOutLinks;
                    String query = QueriesUtils.read(queryFn,
                                Arrays.asList(
                                        jointIds
                                ));
                    ResultSet rs = QueriesUtils.execute(query);

                    while(rs.next()) {
                        String a = rs.getString("A").toLowerCase();
                        String b = rs.getString("B").toLowerCase();
                        Double score = rs.getDouble("Complete");
                        // is there still a iteration to go?
                        if(i < k && score > 10.5) {
                            newLinks.add(a);
                            newLinks.add(b);
                        }

                        Node nodeA = digraph.getOrCreate(a);
                        Node nodeB = digraph.getOrCreate(b);
                        digraph.addEdge(a, b);
                        if(undirected) {
                            digraph.addEdge(b, a);
                        }

                        // Save wlm weight
                        if(nodeA != null) {
                            nodeA.addEdgeAttr(
                                    b,
                                    Constants.Graph.Edges.Attribute.completeWlm,
                                    score);
                            if(undirected) {
                                nodeB.addEdgeAttr(
                                        a,
                                        Constants.Graph.Edges.Attribute.completeWlm,
                                        score);
                            }
                        } else {
                            // it happens rarely:
                            // the node was added, but is not found
                            // hash collision !?
                            System.out.println(String.format(
                                    "%s: %s not found",
                                    GraphFactory.class.toString(),
                                    a
                            ));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // is there still a iteration to go?
            if(i < k) {
                // update set of (unvisited, visited, newLinks) URIs
                visited.addAll(unvisited);
                unvisited.clear();
                newLinks.removeAll(visited);
                unvisited.addAll(newLinks);
                newLinks.clear();
            }
        }

        // empty everything
        visited.clear();
        unvisited.clear();
        newLinks.clear();

        System.out.println(String.format(
                "GraphFactory: produced graph with %s entitites",
                String.valueOf(digraph.nbNodes())
        ));
        return digraph;
    }

    public static DirectedGraph batch(Collection<String> URIs, int k, boolean undirected) {
        return GraphFactory.batch(URIs, k, Integer.MAX_VALUE, undirected);
    }

    public static DirectedGraph oneByOne(Collection<String> URIs, int k, boolean undirected) {
        return GraphFactory.batch(URIs, k, 1, undirected);
    }

}
