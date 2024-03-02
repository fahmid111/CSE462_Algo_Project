package de.umr.pace.clusterediting.reduction;

import de.umr.pace.clusterediting.exact.*;

import java.util.Stack;

public class OneDegreeUW extends ReductionRule {

    public OneDegreeUW() {
        super(Strategy.ALWAYS);
    }

    private boolean noCommonNeighbors(Edge e) {
        return e.commonNeighbors == 0;
    }

    @Override
    protected Stack<Edge> execute(Graph graph, int k, boolean isInitialApplication) {
        Stack<Edge> res = new Stack<>();

        boolean progress = true;
        while (progress) {
            progress = false;
            for (Node n : graph.getNodes()) {
                if (n.degree() == 1) {
                    Edge e = graph.getEdge(n, n.neighborsArray[0]);
                    for (int i = 0; i <e.p3sNotInLowerBoundCount; i++) {
                        P3 p3 = e.p3sNotInLowerBoundArray[i];
                        if (p3.uv == e) {
                            if (!p3.vw.isBlocked && noCommonNeighbors(p3.vw)) {
                                if (!removeEdge(graph, k, res, p3.vw)) return null;
                                else --k;
                                progress = true;
                            }
                        } else {
                            if (!p3.uv.isBlocked && noCommonNeighbors(p3.uv)) {
                                if (!removeEdge(graph, k, res, p3.uv)) return null;
                                else --k;
                                progress = true;
                            }
                        }
                    }
                    for (int i = 0; i <e.p3sInLowerBoundCount; i++) {
                        P3 p3 = e.p3sInLowerBoundArray[i];
                        if (p3.uv == e) {
                            if (!p3.vw.isBlocked && noCommonNeighbors(p3.vw)) {
                                if (!removeEdge(graph, k, res, p3.vw)) return null;
                                else --k;
                                progress = true;
                            }
                        } else {
                            if (!p3.uv.isBlocked && noCommonNeighbors(p3.uv)) {
                                if (!removeEdge(graph, k, res, p3.uv)) return null;
                                else --k;
                                progress = true;
                            }
                        }
                    }
                }
            }
        }

        return res;
    }
}
