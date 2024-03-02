package de.umr.pace.clusterediting.reduction;

import de.umr.pace.clusterediting.exact.*;

import java.util.*;

/**
 * A class representing the P3-Neighborhood rule case b).
 */
public class P3NeighborhoodB extends ReductionRule {
    public P3NeighborhoodB() {
        super(Strategy.ALWAYS);
    }

    @Override
    protected Stack<Edge> execute(Graph graph, int k, boolean isInitialApplication) {
        if (!isInitialApplication) return executeWithLastUsedDepth(graph, k);
        else {
        List<Edge> edgesToRemove = new LinkedList<>();
        P3[][] allP3s = graph.getAllP3s();
        for (int i = allP3s.length - 1; i >= 0; i--) {
            for (int j = 0; j < graph.p3Store.sizesStatic[i]; j++) {
                P3 p3 = allP3s[i][j];
                Edge e = applyRuleB(p3);
                if (e != null && !e.isVisited) {
                    if (e.isEdited || e.isBlockedByBranch) return null;
                    if (!e.isBlocked) {
                        edgesToRemove.add(e);
                        e.isVisited = true;
                    }
                }
            }
        }
        for (Edge e : edgesToRemove) e.isVisited = false;

        Stack<Edge> result = new Stack<>();
        for (Edge edge : edgesToRemove) {
            if (!removeEdge(graph, k - result.size(), result, edge)) {
                return null;
            }
        }
        return result;
        }
    }

    private Stack<Edge> executeWithLastUsedDepth(Graph graph, int k) {
        int lastDepth = this.lastUsedDepth.get(lastUsedDepth.size() - 1);
        List<Edge> edgesToRemove = new LinkedList<>();

        for (int i = lastDepth; i < graph.mods.size(); i++) {
            Graph.Mod mod = graph.mods.get(i);
            if (!mod.blockedEdge) {
                Edge edge = mod.e;
                for (int j = 0; j < edge.p3sNotInLowerBoundCount; j++) {
                    P3 p3 = edge.p3sNotInLowerBoundArray[j];
                    Edge e = applyRuleB(p3);
                    if (e != null && !e.isVisited) {
                        if (e.isEdited || e.isBlockedByBranch) return null;
                        if (!e.isBlocked) {
                            edgesToRemove.add(e);
                            e.isVisited = true;
                        }
                    }
                }
                for (int j = 0; j < edge.p3sInLowerBoundCount; j++) {
                    P3 p3 = edge.p3sInLowerBoundArray[j];
                    Edge e = applyRuleB(p3);
                    if (e != null && !e.isVisited) {
                        if (e.isEdited || e.isBlockedByBranch) return null;
                        if (!e.isBlocked) {
                            edgesToRemove.add(e);
                            e.isVisited = true;
                        }
                    }
                }
            }
        }
        for (Edge e : edgesToRemove) e.isVisited = false;

        Stack<Edge> result = new Stack<>();
        for (Edge edge : edgesToRemove) {
            if (!removeEdge(graph, k - result.size(), result, edge)) {
                return null;
            }
        }

        return result;
    }

    private static Edge applyRuleB(P3 p3) {
        Node u = p3.uw.a;
        Node w = p3.uw.b;
        boolean b = p3.uv.a.getID() == u.getID() || p3.uv.b.getID() == u.getID();
        Edge uv = b ? p3.uv : p3.vw;
        Edge vw = b ? p3.vw : p3.uv;

        boolean edge_uv = uv.commonNeighbors == u.degree - 1 && uv.nonCommonNeighbors == 1 && vw.commonNeighbors == 0;
        if (edge_uv) return vw;
        boolean edge_vw = vw.commonNeighbors == w.degree - 1 && vw.nonCommonNeighbors == 1 && uv.commonNeighbors == 0;
        if (edge_vw) return uv;
        return null;
    }
}
