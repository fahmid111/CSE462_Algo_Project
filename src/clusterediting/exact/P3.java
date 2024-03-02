package de.umr.pace.clusterediting.exact;

import java.util.Comparator;

public class P3 {
    public final Edge uv, vw, uw;
    public final int id;

    public int positionInUVArray = -1;
    public int positionInVWArray = -1;
    public int positionInUWArray = -1;
    public int positionInP3sByBlockedEdges = -1;
    public int blockedEdgeCount = 0;
    public int positionInOneDegrees = -1;

    private static int idCounter;
    final public Node middleNode;
    public boolean isActive = true;

    final int degreeOfNodesCount;
    final int maxDegreeOfNodes;

    final int createdInDepth;
    public MarkedP3 markedP3 = null;

    P3(Edge a, Edge b, Edge c) {
        if (a.compareTo(b) < 0) {
            this.uv = a;
            this.vw = b;
        } else {
            this.uv = b;
            this.vw = a;
        }
        this.uw = c;

        this.id = idCounter;
        idCounter++;
        this.middleNode = getMiddleNode();
        calculateMatchingEdgeCount();
        createdInDepth = ClusterEditingSolver.depthCounter;
        degreeOfNodesCount = uw.a.degree() + uw.b.degree() + middleNode.degree();
        maxDegreeOfNodes = Math.max(Math.max(uw.a.degree(), uw.b.degree()), middleNode.degree());
    }

    static final Comparator<P3> comparatorGetP3 = new Comparator<P3>() {
        @Override
        public int compare(P3 o1, P3 o2) {
            int res;
            res = o1.calculateNeighborsCount() - o2.calculateNeighborsCount();
            if (res != 0) return res;
            res = o1.calculateDegreeSum() - o2.calculateDegreeSum();
            if (res != 0) return res;
            res = o1.calculateFScore() - o2.calculateFScore();
            if (res != 0) return res;
            return o1.maxDegreeOfNodes - o2.maxDegreeOfNodes;
        }
    };

    boolean isBetterBranchP3Than(P3 p3){
        return comparatorGetP3.compare(p3, this) < 0;
    }

    P3[] p3sInLowerBoundArray = new P3[3];
    int p3sInLowerBoundSize = 0;

    public void addP3InCorrespondingLowerBoundList(P3 p3) {
        p3sInLowerBoundArray[p3sInLowerBoundSize++] = p3;
    }

    public void removeP3FromCorrespondingLowerBoundList(P3 p3) {
        for (int i = 0; i < p3sInLowerBoundSize; i++) {
            P3 p3InLowerBound = p3sInLowerBoundArray[i];
            if (p3 == p3InLowerBound) {
                p3sInLowerBoundSize--;
                if (p3sInLowerBoundSize != i) p3sInLowerBoundArray[i] = p3sInLowerBoundArray[p3sInLowerBoundSize];
                return;
            }
        }
    }

    public int calculateFScore(){
        int res = 0;
        if (!uv.isBlocked) res += uv.commonNeighbors;
        if (!vw.isBlocked) res += vw.commonNeighbors;
        if (!uw.isBlocked) res += uw.nonCommonNeighbors;
        return res;
    }

    public void clearP3InCorrespondingLowerBoundList() {
        p3sInLowerBoundSize = 0;
    }

    public int calculateNeighborsCount() {
        return ((uv.isBlocked) ? 0 : uv.p3sInLowerBoundCount + uv.p3sNotInLowerBoundCount
                + ((vw.isBlocked) ? 0 : vw.p3sInLowerBoundCount + vw.p3sNotInLowerBoundCount)
                + ((uw.isBlocked) ? 0 : uw.p3sInLowerBoundCount + uw.p3sNotInLowerBoundCount));
    }

    public int calculateDegreeSum(){
        return uv.a.degree + uv.b.degree + middleNode.degree;
    }
    private void calculateMatchingEdgeCount() {
        if (uv.isBlocked) ++blockedEdgeCount;
        if (vw.isBlocked) ++blockedEdgeCount;
        if (uw.isBlocked) ++blockedEdgeCount;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return id;
    }

    private Node getMiddleNode() {
        if (!uv.a.equals(uw.a) && !uv.a.equals(uw.b)) {
            return uv.a;
        } else {
            return uv.b;
        }
    }

    boolean hasNotNeighbor(P3 p3) {
        return (uw.isBlocked || uw != p3.uw)
                && (uv.isBlocked || (uv != p3.uv && uv != p3.vw))
                && (vw.isBlocked || (vw != p3.uv && vw != p3.vw));
    }

    @Override
    public String toString() {
        return "P3{" + id + '}';
    }
}