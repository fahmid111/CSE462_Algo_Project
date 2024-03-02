package de.umr.pace.clusterediting.exact;

import java.util.*;

public class Edge implements Comparable {
    static public Stack<Edge> toBlockEdges = new Stack<>();
    static public Stack<Edge> twinBlockEdge = new Stack<>();
    static public Stack<Edge> edgesWithNoCommon = new Stack<>();
    static public Edge[] edgeWithNoP3 = null;
    static public int edgeStartWthP3 = 0;

    static public Edge[] markedNonMarkedEdges = null;
    static public int markedEdgeCount = 0;
    static public int notBlockedNotMarkedStartPosition = 0;

    public final Node a, b;
    public int posAInArrayOfB = -1;
    public int posBInArrayOfA = -1;

    public int commonNeighbors = 0;
    public int nonCommonNeighbors = 0;

    public boolean isEdge = false;
    public boolean isEdited = false;
    public boolean isVisited = false;

    public boolean isBlocked;
    public boolean isMarked;
    public boolean isBlockedByBranch = false;

    int positionInMarkedNonMarkedArray = -1;
    int positionInEdgeNoP3Array = -1;
    final int hash;

    Edge(Node a, Node b) {
        if (a.compareTo(b) < 0) {
            this.a = a;
            this.b = b;
        } else {
            this.a = b;
            this.b = a;
        }
        hash = Objects.hash(a.getID(), b.getID());
    }

    static public void initializeStaticVariables(Graph graph) {
        Edge.markedNonMarkedEdges = graph.edgeMarkedNotMarkedAux;
        Edge.markedEdgeCount = 0;
        Edge.notBlockedNotMarkedStartPosition = 0;
        Edge.toBlockEdges.clear();
        Edge.edgesWithNoCommon.clear();
        Edge.twinBlockEdge.clear();

        edgeWithNoP3 = new Edge[markedNonMarkedEdges.length];
        int end = markedNonMarkedEdges.length - 1;
        edgeStartWthP3 = 0;
        for (Edge e : markedNonMarkedEdges) {
            if (e.p3sInLowerBoundCount == 0 && e.p3sNotInLowerBoundCount == 0) {
                e.positionInEdgeNoP3Array = edgeStartWthP3;
                edgeWithNoP3[edgeStartWthP3++] = e;
            } else {
                e.positionInEdgeNoP3Array = end;
                edgeWithNoP3[end--] = e;
            }
        }
    }

    public void moveToEdgeWithNoP3() {
        edgeWithNoP3[positionInEdgeNoP3Array] = edgeWithNoP3[edgeStartWthP3];
        edgeWithNoP3[edgeStartWthP3] = this;
        edgeWithNoP3[positionInEdgeNoP3Array].positionInEdgeNoP3Array = positionInEdgeNoP3Array;
        this.positionInEdgeNoP3Array = edgeStartWthP3;
        edgeStartWthP3++;
    }

    public void moveToEdgeWithP3() {
        edgeStartWthP3--;
        edgeWithNoP3[positionInEdgeNoP3Array] = edgeWithNoP3[edgeStartWthP3];
        edgeWithNoP3[edgeStartWthP3] = this;
        edgeWithNoP3[positionInEdgeNoP3Array].positionInEdgeNoP3Array = positionInEdgeNoP3Array;
        this.positionInEdgeNoP3Array = edgeStartWthP3;
    }

    public void updateCommonsAndNonCommons(Graph g, boolean add) {
            if (add) {
                for (Node n : g.nodes) {
                    if (a == n || b == n) continue;
                    Edge a_n = g.getEdge(a, n);
                    Edge b_n = g.getEdge(b, n);
                    if (!a_n.isEdge) {
                        if (!b_n.isEdge) { //!a_n !b_n
                            a_n.nonCommonNeighbors++;
                            b_n.nonCommonNeighbors++;
                        } else {//!a_n b_n
                            a_n.commonNeighbors++;
                            a_n.nonCommonNeighbors--;
                            b_n.nonCommonNeighbors++;
                        }
                    } else {
                        if (b_n.isEdge) {//a_n b_n
                            a_n.commonNeighbors++;
                            a_n.nonCommonNeighbors--;
                            b_n.commonNeighbors++;
                            b_n.nonCommonNeighbors--;
                            if (a_n.nonCommonNeighbors == 0 && !a_n.isBlocked && !g.isRevert) twinBlockEdge.add(a_n);
                            if (b_n.nonCommonNeighbors == 0 && !b_n.isBlocked && !g.isRevert) twinBlockEdge.add(b_n);
                        } else {//a_n !b_n
                            a_n.nonCommonNeighbors++;
                            b_n.commonNeighbors++;
                            b_n.nonCommonNeighbors--;
                        }
                    }
                }
            } else {
                for (Node n : g.nodes) {
                    if (a == n || b == n) continue;
                    Edge a_n = g.getEdge(a, n);
                    Edge b_n = g.getEdge(b, n);
                    if (!a_n.isEdge) {
                        if (!b_n.isEdge) { //!a_n !b_n
                            a_n.nonCommonNeighbors--;
                            b_n.nonCommonNeighbors--;
                        } else {//!a_n b_n
                            b_n.nonCommonNeighbors--;
                            a_n.commonNeighbors--;
                            a_n.nonCommonNeighbors++;
                            if (a_n.commonNeighbors == 1 && !a_n.isBlocked && !g.isRevert) toBlockEdges.add(a_n);
                            if (b_n.nonCommonNeighbors == 0 && !b_n.isBlocked && !g.isRevert) twinBlockEdge.add(b_n);
                        }
                    } else {
                        if (b_n.isEdge) {//a_n b_n
                            a_n.commonNeighbors--;
                            a_n.nonCommonNeighbors++;
                            b_n.commonNeighbors--;
                            b_n.nonCommonNeighbors++;
                            if (a_n.commonNeighbors == 0 && a_n.isEdited && !g.isRevert) edgesWithNoCommon.add(a_n);
                            if (b_n.commonNeighbors == 0 && b_n.isEdited && !g.isRevert) edgesWithNoCommon.add(b_n);
                        } else {//a_n !b_n
                            a_n.nonCommonNeighbors--;
                            b_n.commonNeighbors--;
                            b_n.nonCommonNeighbors++;
                            if (b_n.commonNeighbors == 1 && !b_n.isBlocked && !g.isRevert) toBlockEdges.add(b_n);
                            if (a_n.nonCommonNeighbors == 0 && !a_n.isBlocked && !g.isRevert) twinBlockEdge.add(a_n);
                        }
                    }
                }
            }
    }

    public void setMarkedTrue() {
        Edge aux = markedNonMarkedEdges[markedEdgeCount];
        markedNonMarkedEdges[markedEdgeCount] = this;
        markedNonMarkedEdges[this.positionInMarkedNonMarkedArray] = aux;
        aux.positionInMarkedNonMarkedArray = this.positionInMarkedNonMarkedArray;
        this.positionInMarkedNonMarkedArray = markedEdgeCount;
        markedEdgeCount++;
        isMarked = true;
        aux.blockSwap();
    }

    public void setMarkedFalse() {
        if (!isMarked) return;
        markedEdgeCount--;
        swapWith(markedEdgeCount);
        isMarked = false;
        if (!isBlocked) {
            this.unBlockSwap();
        }
    }

    public void setMarkedFalseAndBlock() {
        if (isMarked) {
            markedEdgeCount--;
            swapWith(markedEdgeCount);
            isMarked = false;
        } else {
            blockSwap();
        }
    }

    public void blockSwap() {
        swapWith(notBlockedNotMarkedStartPosition);
        notBlockedNotMarkedStartPosition++;
    }

    public void unBlockSwap() {
        notBlockedNotMarkedStartPosition--;
        swapWith(notBlockedNotMarkedStartPosition);
    }

    private void swapWith(int index) {
        Edge aux = markedNonMarkedEdges[index];
        markedNonMarkedEdges[index] = this;
        markedNonMarkedEdges[this.positionInMarkedNonMarkedArray] = aux;
        aux.positionInMarkedNonMarkedArray = this.positionInMarkedNonMarkedArray;
        this.positionInMarkedNonMarkedArray = index;
    }

    static public Edge[][] createEdgeArrayAndInitializeIndexes(List<Node> nodes, Graph g) {
        Edge[][] edges = new Edge[nodes.size()][nodes.size()];
        markedNonMarkedEdges = new Edge[((nodes.size() * nodes.size() + nodes.size()) / 2) - nodes.size()];
        int posCounter = 0;
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Edge edge = new Edge(nodes.get(i), nodes.get(j));
                edges[i][j] = edge;
                edges[j][i] = edge;
                markedNonMarkedEdges[posCounter] = edge;
                edge.positionInMarkedNonMarkedArray = posCounter++;
            }
            Node node = nodes.get(i);
            for (int j = 0; j < node.degree; j++) {
                Node neighbor = node.neighborsArray[j];
                Edge e = edges[node.posIndex][neighbor.posIndex];
                if (e.a == node) e.posBInArrayOfA = j;
                else e.posAInArrayOfB = j;
                e.isEdge = true;
                g.initialM++;
            }
        }
        g.initialM = g.initialM / 2;

            for (int i = 0; i < nodes.size(); i++) {
                Node u = nodes.get(i);
                for (int j = i + 1; j < nodes.size(); j++) {
                    Node v = nodes.get(j);
                    Edge e = edges[nodes.get(i).posIndex][nodes.get(j).posIndex];

                    int commonNeighborsCount = 0;

                    Node smallerDegreeNode = (u.degree < v.degree) ? u : v;
                    Node biggerDegreeNode = (u.degree >= v.degree) ? u : v;
                    for (int k = 0; k < smallerDegreeNode.degree; k++) {
                        Node neighbor = smallerDegreeNode.neighborsArray[k];
                        if (neighbor == biggerDegreeNode) continue;
                        if (edges[neighbor.posIndex][biggerDegreeNode.posIndex].isEdge) {
                            commonNeighborsCount++;
                        }
                    }
                    e.commonNeighbors = commonNeighborsCount;
                    e.nonCommonNeighbors = u.degree + v.degree - commonNeighborsCount - commonNeighborsCount;
                    if (e.isEdge) e.nonCommonNeighbors -= 2;
                }
            }
        return edges;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public int compareTo(Object o) {
        Edge other = (Edge) o;
        if (a.equals(other.a)) {
            return b.compareTo(other.b);
        }
        if (a.equals(other.b)) {
            return b.compareTo(other.a);
        }
        if (b.equals(other.b)) {
            return a.compareTo(other.a);
        }
        if (b.equals(other.a)) {
            return a.compareTo(other.b);
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Edge{" + a.toString() + ", " + b.toString()
                + ", isMarked: " + isMarked
                + ", isBlocked: " + isBlocked
                + ", isEdge: " + isEdge
                + "}";
    }

    public P3[] p3sInLowerBoundArray = new P3[10];
    public P3[] p3sNotInLowerBoundArray = new P3[20];
    public int p3sInLowerBoundCount = 0;
    public int p3sNotInLowerBoundCount = 0;

    // - - - InLowerBound
    public void expandInLowerBoundArrayIfNecessaryAndAdd(P3 p3) {
        if (this.p3sInLowerBoundCount + 1 == this.p3sInLowerBoundArray.length) {
            P3[] oldArray = this.p3sInLowerBoundArray;
            this.p3sInLowerBoundArray = new P3[(int) (oldArray.length * 1.5)];
            System.arraycopy(oldArray, 0, this.p3sInLowerBoundArray, 0, oldArray.length);
        }
        this.p3sInLowerBoundArray[this.p3sInLowerBoundCount++] = p3;
    }

    public void addToP3sInLowerBoundWithThisEdgeAsUV(P3 p3) {
        p3.positionInUVArray = this.p3sInLowerBoundCount;
        expandInLowerBoundArrayIfNecessaryAndAdd(p3);
    }

    public void addToP3sInLowerBoundWithThisEdgeAsVW(P3 p3) {
        p3.positionInVWArray = this.p3sInLowerBoundCount;
        expandInLowerBoundArrayIfNecessaryAndAdd(p3);
    }

    public void addToP3sInLowerBoundWithThisEdgeAsUW(P3 p3) {
        p3.positionInUWArray = this.p3sInLowerBoundCount;
        expandInLowerBoundArrayIfNecessaryAndAdd(p3);
    }

    public void removeFromLowerBoundArrayAux(Edge edge, int positionInArray) {
        this.p3sInLowerBoundCount -= 1;
        if (this.p3sInLowerBoundCount > 0 && positionInArray < this.p3sInLowerBoundCount) {
            P3 lastElement = this.p3sInLowerBoundArray[this.p3sInLowerBoundCount];
            if (lastElement.uv == edge) {
                lastElement.positionInUVArray = positionInArray;
            } else if (lastElement.vw == edge) {
                lastElement.positionInVWArray = positionInArray;
            } else if (lastElement.uw == edge) {
                lastElement.positionInUWArray = positionInArray;
            }
            this.p3sInLowerBoundArray[positionInArray] = lastElement;
        }
        this.p3sInLowerBoundArray[this.p3sInLowerBoundCount] = null;
    }

    public void removeFromP3sInLowerBoundWithThisEdgeAsUV(P3 p3) {
        removeFromLowerBoundArrayAux(p3.uv, p3.positionInUVArray);
    }

    public void removeFromP3sInLowerBoundWithThisEdgeAsVW(P3 p3) {
        removeFromLowerBoundArrayAux(p3.vw, p3.positionInVWArray);
    }

    public void removeFromP3sInLowerBoundWithThisEdgeAsUW(P3 p3) {
        removeFromLowerBoundArrayAux(p3.uw, p3.positionInUWArray);
    }

    // - - - NotInLowerBound
    public void expandInNotLowerBoundArrayIfNecessaryAndAdd(P3 p3) {
        if (this.p3sNotInLowerBoundCount + 1 == this.p3sNotInLowerBoundArray.length) {
            P3[] oldArray = this.p3sNotInLowerBoundArray;
            this.p3sNotInLowerBoundArray = new P3[(int) (oldArray.length * 1.5)];
            System.arraycopy(oldArray, 0, this.p3sNotInLowerBoundArray, 0, oldArray.length);
        }
        this.p3sNotInLowerBoundArray[this.p3sNotInLowerBoundCount++] = p3;
    }

    public void addToP3sNotInLowerBoundWithThisEdgeAsUV(P3 p3) {
        p3.positionInUVArray = this.p3sNotInLowerBoundCount;
        expandInNotLowerBoundArrayIfNecessaryAndAdd(p3);
    }

    public void addToP3sNotInLowerBoundWithThisEdgeAsVW(P3 p3) {
        p3.positionInVWArray = this.p3sNotInLowerBoundCount;
        expandInNotLowerBoundArrayIfNecessaryAndAdd(p3);
    }

    public void addToP3sNotInLowerBoundWithThisEdgeAsUW(P3 p3) {
        p3.positionInUWArray = this.p3sNotInLowerBoundCount;
        expandInNotLowerBoundArrayIfNecessaryAndAdd(p3);
    }

    public void removeFromNotLowerBoundArrayAux(Edge edge, int positionInArray) {
        this.p3sNotInLowerBoundCount -= 1;

        if (this.p3sNotInLowerBoundCount > 0 && positionInArray < this.p3sNotInLowerBoundCount) {
            P3 lastElement = this.p3sNotInLowerBoundArray[this.p3sNotInLowerBoundCount];
            if (lastElement.uv == edge) {
                lastElement.positionInUVArray = positionInArray;
            } else if (lastElement.vw == edge) {
                lastElement.positionInVWArray = positionInArray;
            } else if (lastElement.uw == edge) {
                lastElement.positionInUWArray = positionInArray;
            }
            this.p3sNotInLowerBoundArray[positionInArray] = lastElement;
        }
        this.p3sNotInLowerBoundArray[this.p3sNotInLowerBoundCount] = null;
    }

    public void removeFromP3sNotInLowerBoundWithThisEdgeAsUV(P3 p3) {
        removeFromNotLowerBoundArrayAux(p3.uv, p3.positionInUVArray);
    }

    public void removeFromP3sNotInLowerBoundWithThisEdgeAsVW(P3 p3) {
        removeFromNotLowerBoundArrayAux(p3.vw, p3.positionInVWArray);
    }

    public void removeFromP3sNotInLowerBoundWithThisEdgeAsUW(P3 p3) {
        removeFromNotLowerBoundArrayAux(p3.uw, p3.positionInUWArray);
    }

}