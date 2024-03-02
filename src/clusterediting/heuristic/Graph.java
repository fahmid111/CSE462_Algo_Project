package de.umr.pace.clusterediting.heuristic;

import java.util.*;

public class Graph {
    List<Node> nodes = new ArrayList<>();

    public void initEdge(int a, int b) {
        Node nodeA, nodeB;
        if (nodes.size() < a + 1) {
            nodeA = new Node(a);
            nodes.add(nodeA);
        } else nodeA = nodes.get(a);
        if (nodes.size() < b + 1) {
            nodeB = new Node(b);
            nodes.add(nodeB);
        } else nodeB = nodes.get(b);
        nodeA.addNeighbor(nodeB);
        nodeB.addNeighbor(nodeA);
    }

    Graph[] getComponents() {
        Set<Node> visited = new HashSet<>();
        List<Graph> components = new ArrayList<>();
        for (Node n : nodes) {
            if (visited.contains(n)) continue;
            Graph c = new Graph();
            Set<Node> componentNodes = dfs(n, new HashSet<>());
            c.nodes.addAll(componentNodes);
            visited.addAll(componentNodes);
            components.add(c);
        }

        Graph[] res = new Graph[components.size()];
        for (int i = 0; i < components.size(); i++) res[i] = components.get(i);
        return res;
    }

    private Set<Node> dfs(Node n, Set<Node> nodes) {
        nodes.add(n);
        for (Node neighbor : n.getNeighbors()) {
            if (!nodes.contains(neighbor)) dfs(neighbor, nodes);
        }
        return nodes;
    }

}
