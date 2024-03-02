package de.umr.pace.clusterediting.reduction;

import de.umr.pace.clusterediting.exact.*;

import java.util.*;

/**
 * A class implementing the many neighbors rule.
 */
public class ManyNeighbors extends ReductionRule {

    private P3[] p3CountingArray = new P3[100];
    private int p3CountingArraySize = 0;
    private static final boolean checkForBlockedEdges = true;
    private Edge[] visitAux = new Edge[200];
    private Edge[] appliedEdges = new Edge[30];
    static public int appliedSize = 0;
    int actionStamp = 0;

    public ManyNeighbors() {
        super(Strategy.ALWAYS);
    }

    @Override
    protected Stack<Edge> execute(Graph graph, int k, boolean isInitialApplication) {
        if (!isInitialApplication && ClusterEditingSolver.nodeCounter % 4 != 0)
            return executeWithLastUsedDepth(graph, k);

        actionStamp = ClusterEditingSolver.actionCount;
        Stack<Edge> result = new Stack<>();
        List<Node> nodes = graph.getNodes();

        if (nodes.size() <= k - ClusterEditingSolver.currentLB.markedP3sSize) return result;
        if (graph.p3Store.instanceNOTSolvableWithKGreedy(k)) {
            revert(result, graph);
            return null;
        }
        for (int i = Edge.edgeStartWthP3; i < Edge.edgeWithNoP3.length; i++) {
            Edge e = Edge.edgeWithNoP3[i];
            if (!applyManyNeighborRuleWithBound(e.a, e.b, k, result, graph)) return null;
        }

        return result;
    }

    @Override
    public void revert(Stack<Edge> result, Graph graph) {
        if (appliedSize + result.size() >= appliedEdges.length) {
            Edge[] oldArray = this.appliedEdges;
            this.appliedEdges = new Edge[(int) ((oldArray.length + result.size()) * 1.4)];
            System.arraycopy(oldArray, 0, this.appliedEdges, 0, oldArray.length);
        }
        for (Edge edge : result) appliedEdges[appliedSize++] = edge;
        super.revert(result, graph);
    }

    protected Stack<Edge> executeWithLastUsedDepth(Graph graph, int k) {
        Stack<Edge> result = new Stack<>();
        List<Node> nodes = graph.getNodes();
        int lastDepth = this.lastUsedDepth.get(lastUsedDepth.size() - 1);
        int visitSize = 0;


        for (int i = lastDepth; i < graph.mods.size(); i++) {
            Graph.Mod mod = graph.mods.get(i);
            if (!mod.blockedEdge) {
                Edge edge = mod.e;

                if (!edge.isVisited) {
                    if (!applyManyNeighborRuleWithBound(edge.a, edge.b, k, result, graph)) {
                        for (int v = 0; v < visitSize; v++) visitAux[v].isVisited = false;
                        return null;
                    }
                    edge.isVisited = true;
                    if (visitSize == visitAux.length) {
                        Edge[] oldArray = this.visitAux;
                        this.visitAux = new Edge[(int) (oldArray.length * 1.5)];
                        System.arraycopy(oldArray, 0, this.visitAux, 0, oldArray.length);
                    }
                    visitAux[visitSize++] = edge;
                }

                for (Node node : nodes) {
                    if (node == edge.a || node == edge.b) continue;
                    Edge edge1 = graph.getEdge(edge.a, node);
                    Edge edge2 = graph.getEdge(edge.b, node);

                    if (!edge1.isVisited) {
                        if (!applyManyNeighborRuleWithBound(edge.a, node, k, result, graph)) {
                            for (int v = 0; v < visitSize; v++) visitAux[v].isVisited = false;
                            return null;
                        }
                        edge1.isVisited = true;
                        if (visitSize == visitAux.length) {
                            Edge[] oldArray = this.visitAux;
                            this.visitAux = new Edge[(int) (oldArray.length * 1.5)];
                            System.arraycopy(oldArray, 0, this.visitAux, 0, oldArray.length);
                        }
                        visitAux[visitSize++] = edge1;
                    }
                    if (!edge2.isVisited) {
                        if (!applyManyNeighborRuleWithBound(edge.b, node, k, result, graph)) {
                            for (int v = 0; v < visitSize; v++) visitAux[v].isVisited = false;
                            return null;
                        }
                        edge2.isVisited = true;
                        if (visitSize == visitAux.length) {
                            Edge[] oldArray = this.visitAux;
                            this.visitAux = new Edge[(int) (oldArray.length * 1.5)];
                            System.arraycopy(oldArray, 0, this.visitAux, 0, oldArray.length);
                        }
                        visitAux[visitSize++] = edge2;
                    }
                }
            }
        }

        for (int i = 0; i < appliedSize; i++) {
            Edge e = appliedEdges[i];
            if (visitSize == visitAux.length) {
                Edge[] oldArray = this.visitAux;
                this.visitAux = new Edge[(int) (oldArray.length * 1.5)];
                System.arraycopy(oldArray, 0, this.visitAux, 0, oldArray.length);
            }
            visitAux[visitSize++] = e;
            if (!applyManyNeighborRuleWithBound(e.a, e.b, k, result, graph)) {
                for (int v = 0; v < visitSize; v++) visitAux[v].isVisited = false;
                return null;
            }
        }
        appliedSize = 0;

        for (int v = 0; v < visitSize; v++) visitAux[v].isVisited = false;
        return result;
    }

    private boolean applyManyNeighborRuleWithBound(Node u, Node v, int k, Stack<Edge> result, Graph graph) {
        Edge e = graph.getEdge(u, v);
        boolean isEdge = graph.areNeighbors(u, v);
        k -= result.size();
        if (u.degree + v.degree - (isEdge ? 2 : 0) <= k - ClusterEditingSolver.currentLB.markedP3sSize)
            return true;

        int commonNeighborsCount = e.commonNeighbors, nonCommonNeighborsCount = e.nonCommonNeighbors;
        if (commonNeighborsCount > k && nonCommonNeighborsCount > k) {
            revert(result, graph);
            return false;
        }

        if (isEdge && nonCommonNeighborsCount > k) {
            if (e.isBlocked) {
                revert(result, graph);
                return false;
            }
            return removeEdge(graph, k, result, e);
        } else if (!isEdge && commonNeighborsCount > k) {
            if (e.isBlocked) {
                revert(result, graph);
                return false;
            }
            return addEdge(graph, k, result, e);
        } else {
            int nonCommonLowerBound, commonLowerBound;

            if (isEdge && nonCommonNeighborsCount > k - ClusterEditingSolver.currentLB.markedP3sSize + (!checkForBlockedEdges || !e.isBlocked ? e.p3sInLowerBoundCount : 0)) {
                int minSize = k - nonCommonNeighborsCount + 1;
                p3CountingArraySize = 0;
                if (checkForBlockedEdges) {
                    if (!e.isBlocked) {
                        for (int i = 0; i < e.p3sInLowerBoundCount; i++) {
                            P3 p3 = e.p3sInLowerBoundArray[i];
                            if (p3CountingArraySize == p3CountingArray.length) expandArray();
                            p3CountingArray[p3CountingArraySize++] = p3;
                            p3.markedP3.visited = true;
                        }
                    }
                }
                if ((!checkForBlockedEdges || ClusterEditingSolver.currentLB.markedP3sSize - p3CountingArraySize >= minSize)
                        && removeFromMarkedP3sNonCommon(u, v, graph, minSize)
                        && removeFromMarkedP3sNonCommon(v, u, graph, minSize)) {
                    for (int i = 0; i < p3CountingArraySize; i++) p3CountingArray[i].markedP3.visited = false;
                    nonCommonLowerBound = ClusterEditingSolver.currentLB.markedP3sSize - p3CountingArraySize;
                    if (nonCommonNeighborsCount > k - nonCommonLowerBound) {
                        if (e.isBlocked) {
                            revert(result, graph);
                            return false;
                        }
                        return removeEdge(graph, k, result, e);
                    }
                } else {
                    for (int i = 0; i < p3CountingArraySize; i++) p3CountingArray[i].markedP3.visited = false;
                }
            }

            if (!isEdge && commonNeighborsCount > k - ClusterEditingSolver.currentLB.markedP3sSize + (!checkForBlockedEdges || !e.isBlocked ? e.p3sInLowerBoundCount : 0)) {
                int minSize = k - commonNeighborsCount + 1;
                p3CountingArraySize = 0;
                if (checkForBlockedEdges) {
                    if (!e.isBlocked) {
                        for (int i = 0; i < e.p3sInLowerBoundCount; i++) {
                            P3 p3 = e.p3sInLowerBoundArray[i];
                            if (p3CountingArraySize == p3CountingArray.length) expandArray();
                            p3CountingArray[p3CountingArraySize++] = p3;
                            p3.markedP3.visited = true;
                        }
                    }
                }
                if ((!checkForBlockedEdges || ClusterEditingSolver.currentLB.markedP3sSize - p3CountingArraySize >= minSize)
                        && removeFromMarkedP3sCommon(u, v, graph, minSize)) {
                    for (int i = 0; i < p3CountingArraySize; i++) p3CountingArray[i].markedP3.visited = false;
                    commonLowerBound = ClusterEditingSolver.currentLB.markedP3sSize - p3CountingArraySize;
                    if (commonNeighborsCount > k - commonLowerBound) {
                        if (e.isBlocked) {
                            revert(result, graph);
                            return false;
                        }
                        return addEdge(graph, k, result, e);
                    }
                } else {
                    for (int i = 0; i < p3CountingArraySize; i++) p3CountingArray[i].markedP3.visited = false;
                }
            }
        }
        return true;
    }


    private boolean removeFromMarkedP3sCommon(Node u, Node v, Graph graph, int minSize) {
        if (u.degree() > v.degree()) {
            Node temp = u;
            u = v;
            v = temp;
        }
        for (int j = 0; j < u.degree; j++) {
            Node neighbor = u.neighborsArray[j];
            if (neighbor != v && graph.areNeighbors(neighbor, v)) {
                Edge critEdge = graph.getEdge(neighbor, u);
                if (!checkForBlockedEdges || !critEdge.isBlocked) {
                    for (int i = 0; i < critEdge.p3sInLowerBoundCount; ++i) {
                        P3 p3 = critEdge.p3sInLowerBoundArray[i];
                        if (p3.markedP3.visited) continue;
                        if (p3CountingArraySize == p3CountingArray.length) expandArray();
                        p3CountingArray[p3CountingArraySize++] = critEdge.p3sInLowerBoundArray[i];
                        p3.markedP3.visited = true;
                        if (ClusterEditingSolver.currentLB.markedP3sSize - p3CountingArraySize < minSize)
                            return false;
                    }
                }
                critEdge = graph.getEdge(neighbor, v);
                if (!checkForBlockedEdges || !critEdge.isBlocked) {
                    for (int i = 0; i < critEdge.p3sInLowerBoundCount; ++i) {
                        P3 p3 = critEdge.p3sInLowerBoundArray[i];
                        if (p3.markedP3.visited) continue;
                        p3.markedP3.visited = true;
                        if (p3CountingArraySize == p3CountingArray.length) expandArray();
                        p3CountingArray[p3CountingArraySize++] = critEdge.p3sInLowerBoundArray[i];
                        if (ClusterEditingSolver.currentLB.markedP3sSize - p3CountingArraySize < minSize)
                            return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean removeFromMarkedP3sNonCommon(Node u, Node v, Graph graph, int minSize) {
        for (int j = 0; j < u.degree; ++j) {
            Node n = u.neighborsArray[j];
            if (n != v && !graph.areNeighbors(v, n)) {
                Edge critEdge = graph.getEdge(n, u);
                if (!checkForBlockedEdges || !critEdge.isBlocked) {
                    for (int i = 0; i < critEdge.p3sInLowerBoundCount; ++i) {
                        P3 p3 = critEdge.p3sInLowerBoundArray[i];
                        if (p3.markedP3.visited) continue;
                        p3.markedP3.visited = true;
                        if (p3CountingArraySize == p3CountingArray.length) expandArray();
                        p3CountingArray[p3CountingArraySize++] = critEdge.p3sInLowerBoundArray[i];
                        if (ClusterEditingSolver.currentLB.markedP3sSize - p3CountingArraySize < minSize)
                            return false;
                    }
                }
                critEdge = graph.getEdge(n, v);
                if (!checkForBlockedEdges || !critEdge.isBlocked) {
                    for (int i = 0; i < critEdge.p3sInLowerBoundCount; ++i) {
                        P3 p3 = critEdge.p3sInLowerBoundArray[i];
                        if (p3.markedP3.visited) continue;
                        p3.markedP3.visited = true;
                        if (p3CountingArraySize == p3CountingArray.length) expandArray();
                        p3CountingArray[p3CountingArraySize++] = critEdge.p3sInLowerBoundArray[i];
                        if (ClusterEditingSolver.currentLB.markedP3sSize - p3CountingArraySize < minSize)
                            return false;
                    }
                }
            }
        }
        return true;
    }

    private void expandArray() {
        P3[] oldArray = this.p3CountingArray;
        this.p3CountingArray = new P3[(int) (oldArray.length * 1.5)];
        System.arraycopy(oldArray, 0, this.p3CountingArray, 0, oldArray.length);
    }

}
