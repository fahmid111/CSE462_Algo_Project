package de.umr.pace.clusterediting.exact;

import java.util.*;

public class P3Store {
    private final Stack<P3Edit> edits = new Stack<>();
    private final Graph g;
    P3 lastXP3 = null;
    public int[] sizesStatic;

    public final LowerBound lb;

    P3Store(Graph g, LowerBound lb) {
        this.g = g;
        this.lb = lb;

        p3sByBlockedEdgesArray[0] = new P3[100000];
        p3sByBlockedEdgesArray[1] = new P3[10000];
        p3sByBlockedEdgesArray[2] = new P3[2000];
        p3sByBlockedEdgesArray[3] = new P3[10];
        sizesStatic = sizes;
        findAllP3s(g);
    }

    P3[][] getAllP3s() {
        return p3sByBlockedEdgesArray;
    }

    private final int[] sizes = new int[4];
    private final P3[][] p3sByBlockedEdgesArray = new P3[4][];

    public void addP3InP3sByBlockedEdges(P3 p3) {
        int blockedEdges = p3.blockedEdgeCount;
        expandIfNecessary(blockedEdges);
        p3sByBlockedEdgesArray[blockedEdges][sizes[blockedEdges]] = p3;
        p3.positionInP3sByBlockedEdges = sizes[blockedEdges]++;
    }

    public void removeP3FromP3sByBlockedEdges(P3 p3) {
        int blockedEdges = p3.blockedEdgeCount;
        int position = p3.positionInP3sByBlockedEdges;

        sizes[blockedEdges]--;
        if (position != sizes[blockedEdges]) {
            p3sByBlockedEdgesArray[blockedEdges][position] = p3sByBlockedEdgesArray[blockedEdges][sizes[blockedEdges]];
            p3sByBlockedEdgesArray[blockedEdges][p3.positionInP3sByBlockedEdges].positionInP3sByBlockedEdges = p3.positionInP3sByBlockedEdges;
        }
        p3sByBlockedEdgesArray[blockedEdges][sizes[blockedEdges]] = null;
    }

    public void expandIfNecessary(int index) {
        if (p3sByBlockedEdgesArray[index].length == sizes[index]) {
            P3[] oldArray = p3sByBlockedEdgesArray[index];
            p3sByBlockedEdgesArray[index] = new P3[(int) (oldArray.length * 1.5)];
            System.arraycopy(oldArray, 0, p3sByBlockedEdgesArray[index], 0, oldArray.length);
        }
    }

    P3 getC() {
        P3 p3 = getP3();
        if (p3 == null) {
            lastXP3 = null;
            return p3;
        }

        P3[] p3s;
        P3 bestP3 = null;
        int mostBlocked = 0;

        p3s = p3.uw.p3sNotInLowerBoundArray;
        for (int i = 0; i < p3.uw.p3sNotInLowerBoundCount; i++) {
            P3 p3_2 = p3s[i];
            if (p3 != p3_2 && (bestP3 == null || p3_2.isBetterBranchP3Than(bestP3) || p3_2.blockedEdgeCount > mostBlocked)) {
                bestP3 = p3_2;
                mostBlocked = p3_2.blockedEdgeCount;
            }
        }
        p3s = p3.uw.p3sInLowerBoundArray;
        for (int i = 0; i < p3.uw.p3sInLowerBoundCount; i++) {
            P3 p3_2 = p3s[i];
            if (p3 != p3_2 && (bestP3 == null || p3_2.isBetterBranchP3Than(bestP3) || p3_2.blockedEdgeCount > mostBlocked)) {
                bestP3 = p3_2;
                mostBlocked = p3_2.blockedEdgeCount;
            }
        }

        lastXP3 = bestP3;
        return p3;
    }

    private P3 getP3() {
        P3 bestP3InLowerBound = null;
        P3 bestP3NotInLowerBound = null;

        for (int i = p3sByBlockedEdgesArray.length - 1; i >= 0; --i) {
            for (int j = 0; j < sizes[i]; j++) {
                P3 p3 = p3sByBlockedEdgesArray[i][j];
                if (p3.markedP3 == null) {//equivalent to: is NOT in LowerBound
                    if (bestP3NotInLowerBound == null || p3.isBetterBranchP3Than(bestP3NotInLowerBound)) {
                        bestP3NotInLowerBound = p3;
                    }
                } else {
                    if (bestP3InLowerBound == null || p3.isBetterBranchP3Than(bestP3InLowerBound)) {
                        bestP3InLowerBound = p3;
                    }
                }
            }
            if (bestP3InLowerBound != null && bestP3NotInLowerBound != null && bestP3InLowerBound.isBetterBranchP3Than(bestP3NotInLowerBound)) {
                return bestP3InLowerBound;
            }

            if (bestP3NotInLowerBound != null) {
                return bestP3NotInLowerBound;
            }

            if (bestP3InLowerBound != null) {
                return bestP3InLowerBound;
            }
        }
        return null;
    }

    boolean instanceSolvableWithK(int k) {
        return lb.instanceSolvableWithK(k, p3sByBlockedEdgesArray, sizes);
    }

    public boolean instanceNOTSolvableWithKGreedy(int k) {
        return lb.instanceNOTSolvableWithKGreedy(k, p3sByBlockedEdgesArray, sizes);
    }

    int calculateLowerBound(Graph g) {
        return lb.calculateLowerBound(p3sByBlockedEdgesArray, sizes, g);
    }

    // -------
    private void addP3ToEdgeToP3SMap(P3 p3) {
        if (p3.uv.p3sNotInLowerBoundCount == 0 && p3.uv.p3sInLowerBoundCount == 0) {
            p3.uv.moveToEdgeWithP3();
        }
        if (p3.vw.p3sNotInLowerBoundCount == 0 && p3.vw.p3sInLowerBoundCount == 0) {
            p3.vw.moveToEdgeWithP3();
        }
        if (p3.uw.p3sNotInLowerBoundCount == 0 && p3.uw.p3sInLowerBoundCount == 0) {
            p3.uw.moveToEdgeWithP3();
        }

        p3.uv.addToP3sNotInLowerBoundWithThisEdgeAsUV(p3);
        p3.vw.addToP3sNotInLowerBoundWithThisEdgeAsVW(p3);
        p3.uw.addToP3sNotInLowerBoundWithThisEdgeAsUW(p3);
        p3.isActive = true;
    }

    private void removeP3FromEdgeToP3SMap(P3 p3) {
        p3.uv.removeFromP3sNotInLowerBoundWithThisEdgeAsUV(p3);
        p3.vw.removeFromP3sNotInLowerBoundWithThisEdgeAsVW(p3);
        p3.uw.removeFromP3sNotInLowerBoundWithThisEdgeAsUW(p3);
        p3.isActive = false;

        if (p3.uv.p3sNotInLowerBoundCount == 0 && p3.uv.p3sInLowerBoundCount == 0) {
            p3.uv.moveToEdgeWithNoP3();
        }
        if (p3.vw.p3sNotInLowerBoundCount == 0 && p3.vw.p3sInLowerBoundCount == 0) {
            p3.vw.moveToEdgeWithNoP3();
        }
        if (p3.uw.p3sNotInLowerBoundCount == 0 && p3.uw.p3sInLowerBoundCount == 0) {
            p3.uw.moveToEdgeWithNoP3();
        }
    }

    public void blockEdge(Edge e) {
        lb.blockEdge(e);
        for (int i = 0; i < e.p3sInLowerBoundCount; i++) {
            P3 p3 = e.p3sInLowerBoundArray[i];
            removeP3FromP3sByBlockedEdges(p3);
            p3.blockedEdgeCount++;
            addP3InP3sByBlockedEdges(p3);
        }
        for (int i = 0; i < e.p3sNotInLowerBoundCount; i++) {
            P3 p3 = e.p3sNotInLowerBoundArray[i];
            removeP3FromP3sByBlockedEdges(p3);
            p3.blockedEdgeCount++;
            addP3InP3sByBlockedEdges(p3);
        }
    }

    public void unBlockEdge(Edge e) {
        lb.unBlockEdge(e);
        for (int i = 0; i < e.p3sInLowerBoundCount; i++) {
            P3 p3 = e.p3sInLowerBoundArray[i];
            removeP3FromP3sByBlockedEdges(p3);
            p3.blockedEdgeCount--;
            addP3InP3sByBlockedEdges(p3);
        }
        for (int i = 0; i < e.p3sNotInLowerBoundCount; i++) {
            P3 p3 = e.p3sNotInLowerBoundArray[i];
            removeP3FromP3sByBlockedEdges(p3);
            p3.blockedEdgeCount--;
            addP3InP3sByBlockedEdges(p3);
        }
    }

    private void addInitializedP3(P3 p3) {
        addP3InP3sByBlockedEdges(p3);
        addP3ToEdgeToP3SMap(p3);
    }

    void addCreatedP3(P3 p3) {
        addInitializedP3(p3);
        lb.addCreatedP3(p3);
    }

    void removeAffectedP3(P3 p3) {
        removeP3FromP3sByBlockedEdges(p3);
        lb.removeAffectedP3(p3);
        removeP3FromEdgeToP3SMap(p3);
    }

    void revertAddP3Connected(P3 p3) {
        addP3InP3sByBlockedEdges(p3);
        addP3ToEdgeToP3SMap(p3);
        lb.addCreatedP3(p3);
    }

    void revertRemoveP3Connected(P3 p3) {
        removeAffectedP3(p3);
    }

    private void findAllP3s(Graph g) {
        for (Node v : g.nodes) {
            Node[] neighbors = v.neighborsArray;
            for (int i = 0; i < v.degree; i++) {
                Node u = neighbors[i];
                for (int j = i + 1; j < v.degree; j++) {
                    Node w = neighbors[j];
                    if (!g.areNeighbors(w, u)) {
                        P3 p3 = new P3(g.getEdge(u, v), g.getEdge(v, w), g.getEdge(u, w));
                        addInitializedP3(p3);
                    }
                }
            }
        }
    }

    private static class P3Edit {
        P3[] removals;
        List<P3> additions;

        P3Edit(P3[] removals, List<P3> additions) {
            this.removals = removals;
            this.additions = additions;
        }
    }

    void editEdge(Edge e, boolean add) {
        P3[] affectedP3s = findAffectedP3s(e);
        for (P3 p3 : affectedP3s)
            removeAffectedP3(p3);
        List<P3> newP3s = findNewP3s(e, add);
        for (P3 p3 : newP3s)
            addCreatedP3(p3);

        edits.push(new P3Edit(affectedP3s, newP3s));
    }

    void revertChange() {
        P3Edit edit = edits.pop();
        for (P3 p3 : edit.additions)
            revertRemoveP3Connected(p3);
        for (P3 p3 : edit.removals)
            revertAddP3Connected(p3);
    }


    private List<P3> findNewP3s(Edge e, boolean add) {
        List<P3> newP3s = new ArrayList<>();
        if (add) {
            for (int i = 0; i < e.a.degree; i++) {
                Node n = e.a.neighborsArray[i];
                if (!n.equals(e.b) && !g.areNeighbors(e.b, n)) {
                    P3 p3 = new P3(e, g.getEdge(e.a, n), g.getEdge(n, e.b));
                    newP3s.add(p3);
                }
            }
            for (int i = 0; i < e.b.degree; i++) {
                Node n = e.b.neighborsArray[i];
                if (!n.equals(e.a) && !g.areNeighbors(e.a, n)) {
                    P3 p3 = new P3(e, g.getEdge(e.b, n), g.getEdge(n, e.a));
                    newP3s.add(p3);
                }
            }
        } else {
            for (int i = 0; i < e.a.degree; i++) {
                Node n = e.a.neighborsArray[i];
                if (g.areNeighbors(e.b, n)) {
                    P3 p3 = new P3(g.getEdge(e.a, n), g.getEdge(n, e.b), e);
                    newP3s.add(p3);
                }
            }
        }
        return newP3s;
    }

    private P3[] findAffectedP3s(Edge e) {
        P3[] affected = new P3[e.p3sInLowerBoundCount + e.p3sNotInLowerBoundCount];
        System.arraycopy(e.p3sNotInLowerBoundArray, 0, affected, 0, e.p3sNotInLowerBoundCount);
        System.arraycopy(e.p3sInLowerBoundArray, 0, affected, e.p3sNotInLowerBoundCount, e.p3sInLowerBoundCount);
        return affected;
    }

}