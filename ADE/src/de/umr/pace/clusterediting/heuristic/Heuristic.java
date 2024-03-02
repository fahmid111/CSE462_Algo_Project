package de.umr.pace.clusterediting.heuristic;

import java.util.*;

import static de.umr.pace.clusterediting.exact.ClusterEditingSolver.upperBoundsFromHeuristic;

public class Heuristic {
    public static Graph graph = new Graph();
    static int nodeCounter = 0;

    static public void solve() {
        ce();
    }

    public static Set<Edge> heuristic(Graph graph) {
        Map<Node, Integer> cost = calculateCosts(graph);

        Set<Edge> res = new HashSet<>();
        boolean hasProgress = true;
        while (hasProgress) {
            int bestValue = Integer.MAX_VALUE;
            Node bestNode = null;
            for (Node u : cost.keySet()) {
                int costU = cost.get(u);
                if (costU < bestValue && costU != 0) {
                    bestNode = u;
                    bestValue = costU;
                }
            }
            if (bestNode == null) hasProgress = false;
            else {
                nodeCounter++;
                res.addAll(cliqueOpt(bestNode));
                res.addAll(makeClique(bestNode, cost));
            }
        }

        return res;
    }

    static Set<Edge> ce() {
        Graph[] components = graph.getComponents();
        Set<Edge> result = new HashSet<>();
        Set<Edge> result_local = new HashSet<>();

        for (Graph graph : components) {
            Graph copyGraph = getCopyOf(graph);
            Set<Edge> edit = heuristic(graph);
            result.addAll(edit);
            Set<Edge> result_local_component = doLocalOpt(copyGraph, makeClusters(graph));
            result_local.addAll(result_local_component);
            result_local_component.size();
            upperBoundsFromHeuristic.add(result_local_component);
        }
        return result_local;
    }

    public static Set<Edge> cliqueOpt(Node cliqueNode) {
        Set<Edge> res = new HashSet<>();

        Set<Node> clique = new HashSet<>();
        clique.add(cliqueNode);
        clique.addAll(cliqueNode.getNeighbors());
        Set<Node> outerRing = new HashSet<>();

        for (Node cliqueNeighbor : cliqueNode.getNeighbors()) {
            for (Node pot_outerRing : cliqueNeighbor.getNeighbors()) {
                if (!clique.contains(pot_outerRing)) outerRing.add(pot_outerRing);
            }
        }
        boolean haveProgress = true;
        while (haveProgress) {
            haveProgress = false;

            for (Node outerNode : outerRing) {
                int cliqueNeighborCounter = 0;
                int outerRingNeighborCounter = 0;
                int otherNeighborCounter = 0;

                for (Node neighbor : outerNode.getNeighbors()) {
                    if (clique.contains(neighbor)) cliqueNeighborCounter++;
                    else if (outerRing.contains(neighbor)) outerRingNeighborCounter++;
                    else otherNeighborCounter++;
                }

                int connectCost = clique.size() - cliqueNeighborCounter;

                if (connectCost + otherNeighborCounter + outerRingNeighborCounter <= cliqueNeighborCounter) {
                    addEdge(cliqueNode, outerNode);
                    res.add(new Edge(cliqueNode, outerNode));
                    for (Node neighbor : outerNode.getNeighbors()) {
                        if (!outerRing.contains(neighbor) && !clique.contains(neighbor)) outerRing.add(neighbor);
                    }
                    clique.add(outerNode);
                    outerRing.remove(outerNode);
                    haveProgress = true;
                    break;
                }
            }

        }


        return res;
    }

    public static Set<Edge> makeClique(Node cliqueNode, Map<Node, Integer> cost) {
        Set<Edge> res = new HashSet<>();

        List<Node> neighbors_node = new ArrayList<>(cliqueNode.getNeighbors());
        for (Node v : neighbors_node) {
            List<Node> neighbors_v = new ArrayList<>(v.getNeighbors());
            for (Node w : neighbors_v) {
                if (!cliqueNode.hasNeighbor(w) && cliqueNode.getID() != w.getID()) {
                    res.add(new Edge(v, w));
                    removeAndUpdate(v, w, cost);
                }
            }
        }

        cost.put(cliqueNode, 0);
        for (Node neighbor_1 : cliqueNode.getNeighbors()) {
            cost.put(neighbor_1, 0);

            for (Node neighbor_2 : cliqueNode.getNeighbors()) {
                if (neighbor_1.getID() < neighbor_2.getID() && !neighbor_1.hasNeighbor(neighbor_2)) {
                    res.add(new Edge(neighbor_1, neighbor_2));
                    addEdge(neighbor_1, neighbor_2);
                }
            }
        }
        return res;
    }


    public static Set<Edge> doLocalOpt(Graph graph, List<Set<Node>> clusters) {
        for (int i = 0; i < clusters.size(); i++) {
            Set<Node> cluster = clusters.get(i);
            for (Node node : cluster) node.clusterID = i;
        }

        boolean hasAdvance = true;
        int rounds = 0;
        while (hasAdvance && rounds < 4) {
            rounds++;
            hasAdvance = false;
            boolean haveProgress = true;
            while (haveProgress) {
                haveProgress = false;

                while (removeVertex(clusters)) {
                    haveProgress = true;
                }

                while (moveVertexLocalOpt(clusters)) {
                    haveProgress = true;
                }

                while (mergeClustersLocalOpt(clusters)) {
                    haveProgress = true;
                }

            }

            boolean progress2 = true;
            Collections.shuffle(clusters);
            for (int l = 0; l < clusters.size(); l++) {
                Set<Node> cluster = clusters.get(l);
                for (Node node : cluster) node.clusterID = l;
            }
            while (progress2) {
                progress2 = false;
                while (moveVertexLocalOptTie(clusters)){
                    progress2 = true;
                    hasAdvance = true;
                }
                if (mergeClustersLocalOptTie(clusters) ) {
                    progress2 = true;
                    hasAdvance = true;
                }
            }
            counter = 0;
        }

        //make RESULT
        Set<Edge> res = new HashSet<>();
        List<Node> nodes = graph.nodes;

        for (int i = 0; i < nodes.size(); i++) {
            Node node_1 = nodes.get(i);
            for (int j = i + 1; j < nodes.size(); j++) {
                Node node_2 = nodes.get(j);
                boolean sameClusters = node_1.clusterID == node_2.clusterID;
                if ((node_1.hasNeighbor(node_2) && !sameClusters)
                        || (!node_1.hasNeighbor(node_2) && sameClusters)) {
                    res.add(new Edge(node_1, node_2));
                }
            }
        }
        return res;
    }

    public static boolean moveVertexLocalOpt(List<Set<Node>> clusters) {
        for (Set<Node> cluster : clusters) {
            for (Node node : cluster) {
                Map<Set<Node>, Integer> neighborClusterCounter = new HashMap<>();
                for (Node neighbor : node.getNeighbors()) {
                    Set<Node> neighborCluster = clusters.get(neighbor.clusterID);
                    int currentValue = neighborClusterCounter.getOrDefault(neighborCluster, 0);
                    neighborClusterCounter.put(neighborCluster, currentValue + 1);
                }
                int connection_withinCluster = neighborClusterCounter.getOrDefault(cluster, 0);
                int cost_withinCluster = cluster.size() - connection_withinCluster - 1;
                for (int i = 0; i < clusters.size(); i++) {
                    Set<Node> otherCluster = clusters.get(i);
                    if (!otherCluster.equals(cluster)) {
                        int costs = cost_withinCluster + neighborClusterCounter.getOrDefault(otherCluster, 0);
                        int changeClusterCost = connection_withinCluster + otherCluster.size() - neighborClusterCounter.getOrDefault(otherCluster, 0);
                        if (changeClusterCost < costs) {
                            otherCluster.add(node);
                            cluster.remove(node);
                            node.clusterID = i;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean mergeClustersLocalOpt(List<Set<Node>> clusters) {
        for (int i = 0; i < clusters.size(); i++) {
            Set<Node> cluster_1 = clusters.get(i);
            for (int j = i + 1; j < clusters.size(); j++) {
                Set<Node> cluster_2 = clusters.get(j);
                int cost_merge = 0;
                int edgesBetweenClusters = 0;

                for (Node node : cluster_1) {
                    int neighborCounter = 0;
                    for (Node neighbor : node.getNeighbors()) {
                        if (cluster_2.contains(neighbor)) {
                            edgesBetweenClusters++;
                            neighborCounter++;
                        }
                    }
                    cost_merge += cluster_2.size() - neighborCounter;
                }
                if (edgesBetweenClusters > cost_merge) {

                    int finalI = i;
                    cluster_2.forEach(node -> node.clusterID = finalI);
                    cluster_1.addAll(cluster_2);
                    clusters.remove(cluster_2);

                    for (int l = 0; l < clusters.size(); l++) {
                        Set<Node> cluster = clusters.get(l);
                        for (Node node : cluster) node.clusterID = l;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean mergeClustersLocalOptTie(List<Set<Node>> clusters) {
        for (int i = 0; i < clusters.size(); i++) {
            Set<Node> cluster_1 = clusters.get(i);
            for (int j = i + 1; j < clusters.size(); j++) {
                Set<Node> cluster_2 = clusters.get(j);
                int cost_merge = 0;
                int edgesBetweenClusters = 0;

                for (Node node : cluster_1) {
                    int neighborCounter = 0;
                    for (Node neighbor : node.getNeighbors()) {
                        if (cluster_2.contains(neighbor)) {
                            edgesBetweenClusters++;
                            neighborCounter++;
                        }
                    }
                    cost_merge += cluster_2.size() - neighborCounter;
                }
                if (edgesBetweenClusters >= cost_merge) {
                    int finalI = i;
                    cluster_2.forEach(node -> node.clusterID = finalI);
                    cluster_1.addAll(cluster_2);
                    clusters.remove(cluster_2);

                    for (int l = 0; l < clusters.size(); l++) {
                        Set<Node> cluster = clusters.get(l);
                        for (Node node : cluster) node.clusterID = l;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    static final int limit = 100;
    static int counter = 0;
    static int globalCounter = 0;

    public static boolean moveVertexLocalOptTie(List<Set<Node>> clusters) {
        if (globalCounter >= 2000) return false;

        for (Set<Node> cluster : clusters) {
            for (Node node : cluster) {
                Map<Set<Node>, Integer> neighborClusterCounter = new HashMap<>();
                for (Node neighbor : node.getNeighbors()) {
                    Set<Node> neighborCluster = clusters.get(neighbor.clusterID);
                    int currentValue = neighborClusterCounter.getOrDefault(neighborCluster, 0);
                    neighborClusterCounter.put(neighborCluster, currentValue + 1);
                }
                int connection_withinCluster = neighborClusterCounter.getOrDefault(cluster, 0);
                int cost_withinCluster = cluster.size() - connection_withinCluster - 1;
                for (int i = 0; i < clusters.size(); i++) {
                    Set<Node> otherCluster = clusters.get(i);
                    if (!otherCluster.equals(cluster)) {
                        int costs = cost_withinCluster + neighborClusterCounter.getOrDefault(otherCluster, 0);
                        int changeClusterCost = connection_withinCluster + otherCluster.size() - neighborClusterCounter.getOrDefault(otherCluster, 0);
                        if (changeClusterCost <= costs && counter < limit && globalCounter < 2000) {
                            otherCluster.add(node);
                            cluster.remove(node);
                            node.clusterID = i;
                            counter++;
                            globalCounter++;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean removeVertex(List<Set<Node>> clusters) {
        for (int i = 0; i < clusters.size(); i++) {
            Set<Node> cluster = clusters.get(i);
            for (Node node : cluster) {
                int neighborCounter = 0;
                for (Node neighbor : node.getNeighbors()) {
                    if (neighbor.clusterID == node.clusterID) {
                        neighborCounter++;
                    }
                }
                int notNeighborsInCluster = cluster.size() - neighborCounter - 1;
                if (notNeighborsInCluster >= neighborCounter && cluster.size() != 1) {
                    Set<Node> newCluster = new HashSet<>();
                    newCluster.add(node);
                    cluster.remove(node);
                    node.clusterID = clusters.size();
                    clusters.add(newCluster);
                    return true;
                }
            }
        }
        return false;
    }

    static Map<Node, Node> originalToCopy = new HashMap<>();

    static Graph getCopyOf(Graph graph) {
        Graph c = new Graph();
        for (Node node : graph.nodes) {
            Node copyNode = new Node(node.getID());
            c.nodes.add(copyNode);
            originalToCopy.put(node, copyNode);
        }
        for (Node node : graph.nodes) {
            for (Node neighbor : node.getNeighbors()) {
                originalToCopy.get(node).addNeighbor(originalToCopy.get(neighbor));
            }
        }
        return c;
    }

    static List<Set<Node>> makeClusters(Graph graph) {
        List<Set<Node>> res = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        for (Node node : graph.nodes) {
            if (!visited.contains(node)) {
                Set<Node> cluster = new HashSet<>();
                for (Node neighbor : node.getNeighbors()) {
                    cluster.add(originalToCopy.get(neighbor));
                }
                cluster.add(originalToCopy.get(node));
                visited.addAll(node.getNeighbors());
                visited.add(node);
                res.add(cluster);
            }
        }
        return res;
    }

    public static void removeAndUpdate(Node v, Node w, Map<Node, Integer> cost) {
        removeEdge(v, w);

        int deltaMinusCounter = 0;
        int gammaPlusCounter = 0;
        for (Node neighbor : w.getNeighbors()) {
            if (neighbor.getID() != v.getID()) {
                if (!v.hasNeighbor(neighbor)) {
                    deltaMinusCounter++;
                    cost.put(neighbor, cost.get(neighbor) - 1);
                } else {
                    gammaPlusCounter++;
                    cost.put(neighbor, cost.get(neighbor) + 1);
                }
            }
        }

        int gammaMinusCounter = 0;
        for (Node neighbor : v.getNeighbors()) {
            if (!neighbor.hasNeighbor(w)) {
                gammaMinusCounter++;
                cost.put(neighbor, cost.get(neighbor) - 1);
            }
        }
        cost.put(w, cost.get(w) - deltaMinusCounter - gammaMinusCounter + gammaPlusCounter);
    }

    public static void addEdge(Node n1, Node n2) {
        n1.addNeighbor(n2);
        n2.addNeighbor(n1);
    }

    public static void removeEdge(Node n1, Node n2) {
        n1.removeNeighbor(n2);
        n2.removeNeighbor(n1);
    }

    public static Map<Node, Integer> calculateCosts(Graph graph) {

        Map<Node, Integer> cost = new HashMap<>();
        for (Node u : graph.nodes) {

            int deltaCounter = 0;
            for (Node neighbor_1 : u.getNeighbors()) {
                for (Node neighbor_2 : u.getNeighbors()) {
                    if (neighbor_1.getID() < neighbor_2.getID() && !neighbor_1.hasNeighbor(neighbor_2)) {
                        deltaCounter++;
                    }
                }
            }

            int gammaCounter = 0;

            for (Node v : u.getNeighbors()) {
                for (Node w : v.getNeighbors()) {
                    if (!u.hasNeighbor(w) && u.getID() != w.getID()) {
                        gammaCounter++;
                    }
                }
            }

            cost.put(u, deltaCounter + gammaCounter);
        }
        return cost;
    }
}