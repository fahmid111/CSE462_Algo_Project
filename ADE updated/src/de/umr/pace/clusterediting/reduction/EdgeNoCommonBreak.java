package de.umr.pace.clusterediting.reduction;

import de.umr.pace.clusterediting.exact.Edge;
import de.umr.pace.clusterediting.exact.Graph;

import java.util.Stack;

/**
 * Created by marci on 07.06.2017.
 
    * This reduction rule is used to break the search if there is an edge with no common neighbors
    * and it is edited.
 */
public class EdgeNoCommonBreak extends ReductionRule {

    /**
     * sets non-edges {u, v} to forbidden if u and v do not have common neighbours
     */
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
