package de.umr.pace.clusterediting.reduction;

import de.umr.pace.clusterediting.exact.ClusterEditingSolver;
import de.umr.pace.clusterediting.exact.Edge;
import de.umr.pace.clusterediting.exact.Graph;

import java.util.Stack;

/**
 * An abstract base class for implementing reduction rules. The application strategy will be handled via an instance of a {@link Strategy}.
 * The application strategy is automatically handled when {@link #apply(Graph, int, boolean)} is called. The graph changes made
 * by an reduction rule can be reverted using {@link #revert(Stack, Graph)}.
 * The affected edges are returned by {@link ReductionRule#apply(Graph, int, boolean)}.
 */
public abstract class ReductionRule {
    private static final Stack<Edge> EMPTY_STACK = new Stack<>();
    protected int successfulApplicationCounter = 0;
    protected int applicationCounter = 0;
    protected int modifiedEdgeCount = 0;
    protected int recognizeDeadBranchCount = 0;
    private final Strategy strategy;
    protected Stack<Integer> lastUsedDepth = new Stack<>();

    public boolean increaseCost(){
        return true;
    }

    /**
     * Creates an reduction rule instance using the given strategy
     * @param strategy the strategy determining when the reduction rule should be applied
     */
    public ReductionRule(Strategy strategy) {
        lastUsedDepth.push(0);
        this.strategy = strategy;
    }

    /**
     * Adds the edge to the graph with the respect to the result set and the already edited edges
     * @param graph the graph to add the edge to
     * @param k the current parameter k
     * @param result the rule result containing the edited edges
     * @param edge the edge to add
     * @return whether the operation of adding the given edge is allowed (in terms of possibly leading to an optimal solution)
     */
    boolean addEdge(Graph graph, int k, Stack<Edge> result, Edge edge) {
        if (k == 0 || !ClusterEditingSolver.blockEdgeReduction(edge, graph)) {
            revert(result, graph);
            return false;
        }
        result.push(edge);
        graph.addEdge(edge);
        return true;
    }

    /**
     * Removes the edge to the graph with the respect to the result set and the already edited edges
     * @param graph the graph to add the edge to
     * @param k the current parameter k
     * @param result the rule result containing the edited edges
     * @param edge the edge to remove
     * @return whether the operation of removing the given edge is allowed (in terms of possibly leading to an optimal solution)
     */
    boolean removeEdge(Graph graph, int k, Stack<Edge> result, Edge edge) {
        if (k == 0 || !ClusterEditingSolver.blockEdgeReduction(edge, graph)) {
            revert(result, graph);
            return false;
        }
        result.push(edge);
        graph.removeEdge(edge);
        return true;
    }

    /**
     * Applies the reduction if the strategy allows it
     * @param graph the graph the reduction rule should be applied to
     * @param k the current k parameter
     * @return the collection of edge edits the current rule made or null if the current branch cannot lead to an optimal solution
     */
    public Stack<Edge> apply(Graph graph, int k, boolean isInitialReduction) {
        int modDepth = graph.mods.size();

        if (isInitialReduction || strategy.shouldApply(graph, k)) {
            applicationCounter++;
            Stack<Edge> result = execute(graph, k, isInitialReduction);
            if (result == null || result.size() > 0) {
                successfulApplicationCounter++;
                if (result == null) {
                    recognizeDeadBranchCount++;
                } else {
                    modifiedEdgeCount++;
                }
            }
            lastUsedDepth.push(modDepth);
            return result;
        }
        lastUsedDepth.push(modDepth);
        return EMPTY_STACK;
    }

    /**
     * Reverts the changes that have been made by a reduction rule
     * @param result the result of the reduction rule that should be reverted
     * @param graph the graph the reduction rule was applied to
     */
    public void revert(Stack<Edge> result, Graph graph) {
        lastUsedDepth.pop();
        while (!result.isEmpty()) {
            Edge edge = result.pop();
            graph.revertChange();
            ClusterEditingSolver.unBlockEdge(edge, graph);
        }
    }

    /**
     * Executes the reduction rule using the given graph
     * @param graph the graph the reduction rule should be applied to
     * @param k the current k parameter
     * @param isInitialApplication whether this reduction rule is applied before entering the search tree
     * @return the set of edge edits the current rule
     */
    protected abstract Stack<Edge> execute(Graph graph, int k, boolean isInitialApplication);

    /**
     * An interface representing the strategy determining whether a reduction rule should be applied or skipped
     */
    interface Strategy {
        /**
         * Determines whether the reduction rule should be executed or skipped
         * @param graph the graph the reduction rule should be applied to
         * @param k the current k parameter
         * @return whether the reduction rule should be executed
         */
        boolean shouldApply(Graph graph, int k);

        Strategy ALWAYS = (graph, k) -> true;
    }
}
