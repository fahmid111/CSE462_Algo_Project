package de.umr.pace.clusterediting.reduction;

import de.umr.pace.clusterediting.exact.*;

import java.util.*;

/**
 * A class representing the P3-Neighborhood rule case a).
 */
public class P3NeighborhoodA extends ReductionRule {
    public P3NeighborhoodA() {
        super(Strategy.ALWAYS);
    }

    @Override
    protected Stack<Edge> execute(Graph graph, int k, boolean isInitialApplication) {
        if (!isInitialApplication) return executeWithLastUsedDepth(graph, k);
        else {
            List<Edge> edgesToAdd = new LinkedList<>();
            P3[][] allP3s = graph.getAllP3s();
            for (int i = allP3s.length - 1; i >= 0; i--) {
                for (int j = 0; j < graph.p3Store.sizesStatic[i]; j++) {
                    P3 p3 = allP3s[i][j];
                    if (!p3.uw.isVisited && applyRuleA(p3)) {
                        if (p3.uw.isEdited || p3.uw.isBlockedByBranch) {
                            return null;
                        }
                        if (!p3.uw.isBlocked) {
                            edgesToAdd.add(p3.uw);
                            p3.uw.isVisited = true;
                        }
                    }
                }
            }

            for (Edge e : edgesToAdd) e.isVisited = false;

            Stack<Edge> result = new Stack<>();
            for (Edge edge : edgesToAdd) {
                if (!addEdge(graph, k - result.size(), result, edge)) {
                    return null;
                }
            }
            return result;
        }
    }

    private Stack<Edge> executeWithLastUsedDepth(Graph graph, int k) {
        int lastDepth = this.lastUsedDepth.get(lastUsedDepth.size() - 1);

        List<Edge> edgesToAdd = new LinkedList<>();

        for (int i = lastDepth; i < graph.mods.size(); i++) {
            Graph.Mod mod = graph.mods.get(i);
            if (!mod.blockedEdge) {
                Edge edge = mod.e;
                for (int j = 0; j < edge.p3sNotInLowerBoundCount; j++) {
                    P3 p3 = edge.p3sNotInLowerBoundArray[j];
                    if (!p3.uw.isVisited && applyRuleA(p3)) {
                        if (p3.uw.isEdited || p3.uw.isBlockedByBranch) {
                            return null;
                        }
                        edgesToAdd.add(p3.uw);
                        p3.uw.isVisited = true;
                    }
                }
                for (int j = 0; j < edge.p3sInLowerBoundCount; j++) {
                    P3 p3 = edge.p3sInLowerBoundArray[j];
                    if (!p3.uw.isVisited && applyRuleA(p3)) {
                        if (p3.uw.isEdited || p3.uw.isBlockedByBranch) {
                            return null;
                        }
                        edgesToAdd.add(p3.uw);
                        p3.uw.isVisited = true;
                    }
                }
            }
        }
        for (Edge e : edgesToAdd) e.isVisited = false;

        Stack<Edge> result = new Stack<>();
        for (Edge edge : edgesToAdd) {
            if (!addEdge(graph, k - result.size(), result, edge)) {
                return null;
            }
        }

        return result;
    }

    private static boolean applyRuleA(P3 p3) {
        Node u = p3.uw.a;
        return p3.uw.commonNeighbors == u.degree && p3.uw.nonCommonNeighbors == 0
                && p3.uv.commonNeighbors == u.degree - 1 && p3.uv.nonCommonNeighbors == 1;
    }
}
