package de.umr.pace.clusterediting.reduction;

import de.umr.pace.clusterediting.exact.*;

import java.util.Stack;

public class BlockTBEExhaustive extends ReductionRule {

    public BlockTBEExhaustive() {
        super(Strategy.ALWAYS);
    }

    Stack<Integer> revertStackAux = new Stack<>();
    int toRevertActions = 0;

    @Override
    protected Stack<Edge> execute(Graph graph, int k, boolean isInitialApplication) {
        Stack<Edge> result = new Stack<>();
        toRevertActions = 0;
        boolean progress = true;
        while (progress) {
            int oldRevertActions = toRevertActions;

            Stack<Edge> stack = Edge.toBlockEdges;
            while (!stack.isEmpty()) {
                Edge edge = stack.pop();
                if (edge.commonNeighbors <= 1 && !edge.isBlocked && !edge.isEdge) {
                    ClusterEditingSolver.blockEdgeReduction(edge, graph);
                    toRevertActions++;
                }
            }

            stack = Edge.twinBlockEdge;
            while (!stack.isEmpty()) {
                Edge edge = stack.pop();
                if (edge.nonCommonNeighbors == 0 && !edge.isBlocked && edge.isEdge) {
                    ClusterEditingSolver.blockEdgeReduction(edge, graph);
                    toRevertActions++;
                }
            }

            P3[][] allP3s = graph.getAllP3s();
            int newK = k;
            while (graph.p3Store.sizesStatic[2] != 0) {
                if (graph.p3Store.sizesStatic[3] != 0 || newK == 0) {
                    revertStackAux.push(toRevertActions);
                    revert(result, graph);
                    return null;
                }
                P3 p3 = allP3s[2][0];

                if (!p3.uv.isBlocked) {
                    if (!removeEdge(graph, newK, result, p3.uv)) {
                        revertStackAux.push(toRevertActions);
                        return null;
                    }
                } else if (!p3.vw.isBlocked) {
                    if (!removeEdge(graph, newK, result, p3.vw)) {
                        revertStackAux.push(toRevertActions);
                        return null;
                    }
                } else if (!p3.uw.isBlocked) {
                    if (!addEdge(graph, newK, result, p3.uw)) {
                        revertStackAux.push(toRevertActions);
                        return null;
                    }
                }
                newK--;
            }
            progress = oldRevertActions != toRevertActions;
        }
        revertStackAux.push(toRevertActions);
        return result;
    }

    @Override
    public void revert(Stack<Edge> result, Graph graph) {
        lastUsedDepth.pop();
        int actions = revertStackAux.pop();
        for (int i = 0; i < actions; i++) {
            graph.revertChange();
        }
    }

    @Override
    boolean addEdge(Graph graph, int k, Stack<Edge> result, Edge edge) {
        if (k == 0 || !ClusterEditingSolver.blockEdgeReduction(edge, graph)) {
            revert(result, graph);
            return false;
        }
        result.push(edge);
        toRevertActions += 2;
        graph.addEdge(edge);
        return true;
    }

    @Override
    boolean removeEdge(Graph graph, int k, Stack<Edge> result, Edge edge) {
        if (k == 0 || !ClusterEditingSolver.blockEdgeReduction(edge, graph)) {
            revert(result, graph);
            return false;
        }
        result.push(edge);
        toRevertActions += 2;
        graph.removeEdge(edge);
        return true;
    }
}
