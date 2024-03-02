package de.umr.pace.clusterediting.exact;

import java.util.*;

public class Graph {
    public int initialM = 0;

    List<Node> nodes = new ArrayList<>();
    Edge[][] edges;
    Edge[] edgeMarkedNotMarkedAux;
    public Stack<Mod> mods = new Stack<>();
    public P3Store p3Store;

    public boolean isRevert = false;

    static public class Mod {
        public Edge e;
        public boolean blockedEdge;//when true, only blocked this Edge; when false edge was removed or added

        public Mod(Edge e, boolean blockedEdge) {
            this.e = e;
            this.blockedEdge = blockedEdge;
        }
    }

    void initEdge(int a, int b) {
        Node nodeA, nodeB;
        if (nodes.size() < a + 1) {
            nodeA = new Node(a);
            nodes.add(nodeA);
        } else nodeA = nodes.get(a);
        if (nodes.size() < b + 1) {
            nodeB = new Node(b);
            nodes.add(nodeB);
        } else nodeB = nodes.get(b);
        nodeA.expandIfNecessary();
        nodeA.neighborsArray[nodeA.degree++] = nodeB;
        nodeB.expandIfNecessary();
        nodeB.neighborsArray[nodeB.degree++] = nodeA;
    }

    int initP3Store(LowerBound lb) {
        p3Store = new P3Store(this, lb);
        return p3Store.calculateLowerBound(this);
    }

    public boolean areNeighbors(Node node1, Node node2) {
        return edges[node1.posIndex][node2.posIndex].isEdge;
    }

    public Graph addEdge(Edge e) {
        mods.push(new Mod(e, false));
        addEdgeAux(e);
        e.updateCommonsAndNonCommons(this, true);
        p3Store.editEdge(e, true);
        return this;
    }

    private void addEdgeAux(Edge e) {
        e.a.expandIfNecessary();
        e.a.neighborsArray[e.a.degree] = e.b;
        e.posBInArrayOfA = e.a.degree;
        e.a.degree++;

        e.b.expandIfNecessary();
        e.b.neighborsArray[e.b.degree] = e.a;
        e.posAInArrayOfB = e.b.degree;
        e.b.degree++;

        e.isEdge = true;
        e.isEdited = !isRevert;
        ClusterEditingSolver.actionCount++;
    }

    public Graph removeEdge(Edge e) {
        mods.push(new Mod(e, false));
        removeEdgeAux(e);
        e.updateCommonsAndNonCommons(this, false);
        p3Store.editEdge(e, false);
        return this;
    }

    private void removeEdgeAux(Edge e) {
        e.a.degree--;
        if (e.a.degree != e.posBInArrayOfA) {
            e.a.neighborsArray[e.posBInArrayOfA] = e.a.neighborsArray[e.a.degree];
            Edge changedEdge = getEdge(e.a, e.a.neighborsArray[e.posBInArrayOfA]);
            if (e.a == changedEdge.a) {
                changedEdge.posBInArrayOfA = e.posBInArrayOfA;
            } else {
                changedEdge.posAInArrayOfB = e.posBInArrayOfA;
            }
        }
        e.a.neighborsArray[e.a.degree] = null;
        e.posBInArrayOfA = -1;

        e.b.degree--;
        if (e.b.degree != e.posAInArrayOfB) {
            e.b.neighborsArray[e.posAInArrayOfB] = e.b.neighborsArray[e.b.degree];
            Edge changedEdge = getEdge(e.b, e.b.neighborsArray[e.posAInArrayOfB]);
            if (e.b == changedEdge.a) {
                changedEdge.posBInArrayOfA = e.posAInArrayOfB;
            } else {
                changedEdge.posAInArrayOfB = e.posAInArrayOfB;
            }
        }
        e.b.neighborsArray[e.b.degree] = null;
        e.posAInArrayOfB = -1;

        e.isEdge = false;
        e.isEdited = !isRevert;
        ClusterEditingSolver.actionCount++;
    }

    public void revertChange() {
        isRevert = true;
        Mod mod = mods.peek();
        if (mod != null) {
            if (mod.blockedEdge) {
                ClusterEditingSolver.unBlockEdge(mod.e, this);
            } else {
                p3Store.revertChange();
                toggleEdge(mods.pop().e);
            }
        }
        isRevert = false;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void blockEdge(Edge e) {
        mods.push(new Mod(e, true));
        p3Store.blockEdge(e);
    }

    public void unBlockEdge(Edge e) {
        mods.pop();
        p3Store.unBlockEdge(e);
    }

    public P3[][] getAllP3s() {
        return p3Store.getAllP3s();
    }

    private void toggleEdge(Edge e) {
        if (areNeighbors(e.a, e.b)) {
            removeEdgeAux(e);
            e.updateCommonsAndNonCommons(this, false);
        } else {
            addEdgeAux(e);
            e.updateCommonsAndNonCommons(this, true);
        }
    }

    P3 getC() {
        return p3Store.getC();
    }

    public Edge getEdge(Node n1, Node n2) {
        return edges[n1.posIndex][n2.posIndex];
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
            for (int i = 0; i < c.nodes.size(); i++)
                c.nodes.get(i).posIndex = i;
            c.edges = Edge.createEdgeArrayAndInitializeIndexes(c.nodes, c);
            c.edgeMarkedNotMarkedAux = Edge.markedNonMarkedEdges;
        }
        Graph[] res = new Graph[components.size()];
        for (int i = 0; i < components.size(); i++) res[i] = components.get(i);
        return res;
    }

    private Set<Node> dfs(Node n, Set<Node> nodes) {
        nodes.add(n);
        for (int i = 0; i < n.degree; i++) {
            Node neighbor = n.neighborsArray[i];
            if (!nodes.contains(neighbor)) dfs(neighbor, nodes);
        }
        return nodes;
    }

    boolean instanceSolvableWithK(int k, Graph g) {
        return p3Store.instanceSolvableWithK(k);
    }
}
