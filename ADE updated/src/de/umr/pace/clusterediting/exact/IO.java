package de.umr.pace.clusterediting.exact;

import de.umr.pace.clusterediting.heuristic.Heuristic;

import java.io.*;
import java.util.*;


public class IO {
    public static int vertexCount = 0;
    public static List<String> nodes = new ArrayList<>();

    static void writeResult(Set<Edge> result) {
        for (Edge edge : result) {
            System.out.println(nodes.get(edge.a.getID()) + " " + nodes.get(edge.b.getID()));
        }
    }

    static Graph parseGraph() throws IOException {
        de.umr.pace.clusterediting.heuristic.Graph graphHeuristic = new de.umr.pace.clusterediting.heuristic.Graph();
        Heuristic.graph = graphHeuristic;

        Graph graph = new Graph();

        Map<String, Integer> nodeMappings = new HashMap<>();
        BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder nodeABuilder = new StringBuilder(), nodeBBuilder = new StringBuilder();
        while (true) {
            boolean readingA = false, readingB = false;
            while (true) {
                int val;
                if ((val = bi.read()) == -1) return graph;
                char c = (char) val;

                if (c == '\n') break;
                if (c == '#' || c == 'p'|| c == 'c'|| c == '\r') {
                    // Read until end of line or end of input
                    while (true) {
                        if ((val = bi.read()) == -1) return graph;
                        c = (char) val;
                        if (c == '\n') break;
                    }
                    break;
                }
                if (c == ' ' || c == '\t') {
                    if (readingA) {
                        readingA = false;
                        readingB = true;
                        nodeBBuilder.delete(0, nodeBBuilder.length());
                        continue;
                    }
                    if (readingB) {
                        // Read until end of line or end of input
                        while (true) {
                            if ((val = bi.read()) == -1) return graph;
                            c = (char) val;
                            if (c == '\n') break;
                        }
                        break;
                    }
                    continue;
                }
                if (!readingA && !readingB) {
                    nodeABuilder.delete(0, nodeABuilder.length());
                    readingA = true;
                }
                if (readingA) nodeABuilder.append(c);
                if (readingB) nodeBBuilder.append(c);
            }
            if (nodeABuilder.length() == 0 || nodeBBuilder.length() == 0) continue;
            String nodeA = nodeABuilder.toString(), nodeB = nodeBBuilder.toString();


            int a, b;

            if (!nodeMappings.containsKey(nodeA)) {
                nodeMappings.put(nodeA, vertexCount);
                nodes.add(nodeA);
                a = vertexCount++;
            } else {
                a = nodeMappings.get(nodeA);
            }
            if (!nodeMappings.containsKey(nodeB)) {
                nodeMappings.put(nodeB, vertexCount);
                nodes.add(nodeB);
                b = vertexCount++;
            } else {
                b = nodeMappings.get(nodeB);
            }
            graphHeuristic.initEdge(a, b);
            graph.initEdge(a, b);
        }
    }
}
