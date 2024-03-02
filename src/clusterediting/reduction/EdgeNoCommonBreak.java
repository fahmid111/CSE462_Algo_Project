package de.umr.pace.clusterediting.reduction;

import de.umr.pace.clusterediting.exact.Edge;
import de.umr.pace.clusterediting.exact.Graph;

import java.util.Stack;

public class EdgeNoCommonBreak extends ReductionRule {
    public EdgeNoCommonBreak() {
        super(Strategy.ALWAYS);
    }

    @Override
    protected Stack<Edge> execute(Graph graph, int k, boolean isInitialApplication) {
        Stack<Edge> stack = Edge.edgesWithNoCommon;
        while (!stack.isEmpty()) {
            Edge edge = stack.pop();
            if (edge.commonNeighbors == 0 && edge.isEdited && edge.isEdge) {
                stack.clear();
                return null;
            }
        }
        return new Stack<>();
    }

}
