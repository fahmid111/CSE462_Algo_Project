package de.umr.pace.clusterediting.exact;

import de.umr.pace.clusterediting.heuristic.Heuristic;
import de.umr.pace.clusterediting.reduction.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ClusterEditingSolver {
    static private Graph graph = new Graph();
    static public int nodeCounter = 0, treeNodeCounter = 0;
    static CType lastCType = null;
    static List<ReductionRule> initialReductionRules = new LinkedList<>();
    static List<ReductionRule> reductionRules = new LinkedList<>();
    static public List<Set<de.umr.pace.clusterediting.heuristic.Edge>> upperBoundsFromHeuristic = new LinkedList<>();

    static final boolean exhaustiveReductions = false;

    static {
        initialReductionRules.add(new ManyNeighbors());
        initialReductionRules.add(new P3NeighborhoodA());
        initialReductionRules.add(new P3NeighborhoodB());
        reductionRules.add(new EdgeNoCommonBreak());
        reductionRules.add(new BlockTBEExhaustive());
        reductionRules.add(new ManyNeighbors());
        reductionRules.add(new P3NeighborhoodA());
        reductionRules.add(new P3NeighborhoodB());
        reductionRules.add(new BlockTBEExhaustive());
        reductionRules.add(new OneDegreeUW());
        reductionRules.add(new EdgeNoCommonBreak());
    }

    static void solve() {
        Set<Edge> result = ce();
        IO.writeResult(result);
    }

    static int staticK = 0;
    static final int kStepSize = 3;
    static int greatestKNotSolvable = 0;
    static List<Edge> smallestResFound = null;
    static double upperBoundRatio = 0.9;
    static public int actionCount = 0;

    static void doBlockReduction(List<Node> nodes, Graph g) {
        for (int i = 0; i < nodes.size(); i++) {
            Node n1 = nodes.get(i);
            for (int j = i + 1; j < nodes.size(); j++) {
                Node n2 = nodes.get(j);
                if (g.areNeighbors(n1, n2)) continue;
                int count = 0;
                if (n1.degree > n2.degree) {
                    for (int k = 0; k < n2.degree; k++) {
                        if (g.areNeighbors(n1, n2.neighborsArray[k])) count++;
                        if (count == 2) break;
                    }
                } else {
                    for (int k = 0; k < n1.degree; k++) {
                        if (g.areNeighbors(n2, n1.neighborsArray[k])) count++;
                        if (count == 2) break;
                    }
                }
                if (count < 2) {
                    g.getEdge(n1, n2).isBlocked = true;
                    g.getEdge(n1, n2).blockSwap();
                }
            }
        }
    }

    static int currentGraphNodesSize = -1;

    static public LowerBound currentLB;

    static Set<Edge> ce() {
        Graph[] components = graph.getComponents();
        Set<Edge> result = new HashSet<>();
        for (Graph g : components) {

            currentGraphNodesSize = g.nodes.size();
            Edge.initializeStaticVariables(g);
            ManyNeighbors.appliedSize = 0;
            doBlockReduction(g.nodes, g);

            Set<de.umr.pace.clusterediting.heuristic.Edge> upperBoundSet = upperBoundsFromHeuristic.remove(0);
            int upperBound = upperBoundSet.size();
            LowerBound lowerBound = new LowerBound(upperBound, kStepSize);
            currentLB = lowerBound;
            staticK = g.initP3Store(lowerBound);
            greatestKNotSolvable = staticK - 1;

            if (staticK == upperBound) {
                result.addAll(upperBoundSet.stream().map(edge -> new Edge(graph.nodes.get(edge.a.getID()), graph.nodes.get(edge.b.getID()))).collect(Collectors.toList()));
                continue;
            }

            staticK = Math.max((int) (((1 - upperBoundRatio) * staticK + upperBoundRatio * upperBound) + 0.5), staticK);
            staticK = Math.min(staticK, upperBound - 1);
            staticK = upperBound - 1;
            smallestResFound = null;

            List<Edge> initialReductionResult = new LinkedList<>();
            while (true) {
                int resSize = initialReductionResult.size();
                boolean progress = true;
                while (progress) {
                    progress = false;
                    for (ReductionRule rule : initialReductionRules) {
                        Stack<Edge> res = rule.apply(g, upperBound, true);
                        if (exhaustiveReductions && !res.isEmpty()) progress = true;
                        initialReductionResult.addAll(res);
                    }

                }

                if (resSize == initialReductionResult.size()) break;
            }
            while (true) {
                treeNodeCounter = 0;

                branch(g, initialReductionResult.size());

                if (smallestResFound != null) {
                    result.addAll(initialReductionResult);
                    result.addAll(smallestResFound);
                    break;
                } else {
                    if (staticK == upperBound - 1) {
                        result.addAll(upperBoundSet.stream().map(edge -> new Edge(graph.nodes.get(edge.a.getID()), graph.nodes.get(edge.b.getID()))).collect(Collectors.toList()));
                        break;
                    }
                    greatestKNotSolvable = staticK;
                    staticK = Math.min(staticK + kStepSize, upperBound - 1);
                }
            }

        }

        return result;
    }

    public static int depthCounter = 0;

    private static void revertReductionRules(Stack<Stack<Edge>> reductionRuleResults, Graph graph) {
        while (!reductionRuleResults.isEmpty()) {
            reductionRules.get((reductionRuleResults.size() - 1) % reductionRules.size()).revert(reductionRuleResults.pop(), graph);
        }
    }

    public static int ruleDepthCounter = 0;
    static Stack<P3> stack = new Stack<>();


    static boolean branch(Graph g, int costs) {
        ++treeNodeCounter;
        ++nodeCounter;
        ++depthCounter;
        ++ruleDepthCounter;
        int k = staticK - costs;
        if (k < 0 || !g.instanceSolvableWithK(k, g)) return false;

        Stack<Stack<Edge>> reductionRuleResults = new Stack<>();
        boolean progress = true;
        while (progress) {
            progress = false;
            for (ReductionRule rule : reductionRules) {

                Stack<Edge> result = rule.apply(g, k, false);
                if (result == null) {
                    revertReductionRules(reductionRuleResults, g);
                    return false;
                }

                if (exhaustiveReductions && !result.isEmpty())
                    progress = true;

                reductionRuleResults.push(result);
                if (rule.increaseCost()) {
                    costs += result.size();
                    k -= result.size();
                }
                if (k < 0) {
                    revertReductionRules(reductionRuleResults, g);
                    return false;
                }
            }
        }

        P3 p3 = g.getC();
        if (p3 == null) {
            if (smallestResFound == null) smallestResFound = new LinkedList<>();
            else smallestResFound.clear();
            g.mods.stream().filter(x -> !x.blockedEdge).forEach(mod -> smallestResFound.add(mod.e));
            if (smallestResFound.size() == greatestKNotSolvable + 1) {
                return true;
            } else {
                staticK = smallestResFound.size() - 1;
                revertReductionRules(reductionRuleResults, g);
                return false;
            }
        }
        stack.push(p3);

        P3 p3X = g.p3Store.lastXP3;
        Edge vx = defineTypeAndReturnVX(g, p3, p3X);

        boolean applyBlock = currentLB.markedP3sSize == k;
        int blockCount = 0;
        if (applyBlock) {
            if (!p3.uw.isBlocked && p3.uw.p3sInLowerBoundCount == 0) {
                blockEdgeBranch(p3.uw, g);
                ++blockCount;
            }
            if (!p3.uv.isBlocked && p3.uv.p3sInLowerBoundCount == 0) {
                blockEdgeBranch(p3.uv, g);
                ++blockCount;
            }
            if (!p3.vw.isBlocked && p3.vw.p3sInLowerBoundCount == 0) {
                blockEdgeBranch(p3.vw, g);
                ++blockCount;
            }
            if (p3X != null) {
                if (!p3X.uv.isBlocked && p3X.uv.p3sInLowerBoundCount == 0) {
                    blockEdgeBranch(p3X.uv, g);
                    ++blockCount;
                }
                if (!p3X.vw.isBlocked && p3X.vw.p3sInLowerBoundCount == 0) {
                    blockEdgeBranch(p3X.vw, g);
                    ++blockCount;
                }
                Edge middleEdge = g.getEdge(p3.middleNode, p3X.middleNode);
                if (!middleEdge.isBlocked && middleEdge.p3sInLowerBoundCount == 0) {
                    blockEdgeBranch(middleEdge, g);
                    ++blockCount;
                }
            }
        }
        boolean res = false;
        switch (lastCType) {
            case C1:
                res = branchCase1(g, costs, p3);
                break;
            case C2:
                res = branchCase2(g, costs, p3, p3X, vx);
                break;
            case C3:
                res = branchCase3(g, costs, p3, p3X, vx);
                break;
        }

        if (!res) {
            if (applyBlock) {
                for (int i = 0; i < blockCount; i++) {
                    g.revertChange();
                }
            }
            revertReductionRules(reductionRuleResults, g);
        }

        stack.pop();
        --depthCounter;
        return res;
    }

    static Edge defineTypeAndReturnVX(Graph graph, P3 p1, P3 p2) {
        if (graph.p3Store.lastXP3 != null) {
            Node v = p1.middleNode;
            Node x = p2.middleNode;
            if (graph.areNeighbors(v, x)) lastCType = CType.C2;
            else lastCType = CType.C3;
            return graph.getEdge(v, x);
        } else {
            lastCType = CType.C1;
            return null;
        }
    }

    static public boolean blockEdgeBranch(Edge e, Graph g) {
        boolean blockSuccess = !e.isBlocked;
        if (blockSuccess) {
            e.isBlocked = true;
            e.isBlockedByBranch = true;
            g.blockEdge(e);
        }
        return blockSuccess;
    }

    static public boolean blockEdgeReduction(Edge e, Graph g) {
        boolean blockSuccess = !e.isBlocked;
        if (blockSuccess) {
            e.isBlocked = true;
            g.blockEdge(e);
        }
        return blockSuccess;
    }

    static private boolean blockTwoEdgesBranch(Edge e1, Edge e2, Graph g) {
        if (!e2.isBlocked && blockEdgeBranch(e1, g)) {
            return blockEdgeBranch(e2, g);
        }
        return false;
    }

    static public boolean unBlockEdge(Edge e, Graph g) {
        e.isBlocked = false;
        e.isBlockedByBranch = false;
        g.unBlockEdge(e);
        return false;
    }

    static boolean branchCase1(Graph g, int costs, P3 p3) {
        Edge uv = p3.uv, vw = p3.vw;
        boolean branchA, branchB;
        boolean blockedUW = blockEdgeBranch(p3.uw, g);

        if ((branchA = blockEdgeBranch(uv, g)) && branchRemove(uv, costs + 1, g)) return true;
        if ((branchB = blockEdgeBranch(vw, g)) && branchRemove(vw, costs + 1, g)) return true;

        if (branchB) unBlockEdge(vw, g);
        if (branchA) unBlockEdge(uv, g);
        if (blockedUW) unBlockEdge(p3.uw, g);
        return false;
    }

    static boolean branchCase2(Graph g, int costs, P3 p3, P3 p3x, Edge vx) {
        Edge uv = p3.uv, vw = p3.vw, uw = p3.uw, ux = p3x.uv, wx = p3x.vw;
        boolean branchA, branchB, branchC;
        //--BRANCH A
        if ((branchA = blockEdgeBranch(uw, g)) && branchAdd(uw, costs + 1, g)) return true;

        //---- BRANCH B
        boolean branchBA;
        boolean branchBB;
        if ((branchB = blockEdgeBranch(uv, g))) {
            g.removeEdge(uv);

            if ((branchBA = blockEdgeBranch(ux, g)) && branchRemove(ux, costs + 2, g)) return true;
            if ((branchBB = blockTwoEdgesBranch(vx, wx, g)) && branchRemoveDouble(wx, vx, costs + 3, g))
                return true;
            if (branchBB) {
                unBlockEdge(wx, g);
                unBlockEdge(vx, g);
            }
            if (branchBA) unBlockEdge(ux, g);
            g.revertChange();//uv
        }
        //---- BRANCH C
        boolean branchCA;
        boolean branchCB;
        if (branchC = blockEdgeBranch(vw, g)) {
            g.removeEdge(vw);

            if ((branchCA = blockEdgeBranch(wx, g)) && branchRemove(wx, costs + 2, g)) return true;
            if ((branchCB = blockTwoEdgesBranch(ux, vx, g)) && branchRemoveDouble(ux, vx, costs + 3, g))
                return true;

            if (branchCB) {
                unBlockEdge(vx, g);
                unBlockEdge(ux, g);
            }
            if (branchCA) unBlockEdge(wx, g);
            g.revertChange();//vw
        }

        if (branchC) unBlockEdge(vw, g);
        if (branchB) unBlockEdge(uv, g);
        if (branchA) unBlockEdge(uw, g);
        return false;
    }

    static boolean branchCase3(Graph g, int costs, P3 p3, P3 p3x, Edge vx) {
        Edge uv = p3.uv, vw = p3.vw, uw = p3.uw, ux = p3x.uv, wx = p3x.vw;

        boolean branchA, branchB, branchC;
        //--- BRANCH A
        if ((branchA = blockEdgeBranch(uv, g)) && branchRemove(uv, costs + 1, g)) return true;

        //--- BRANCH B
        boolean branchBA;
        boolean branchBB;
        if (branchB = blockEdgeBranch(vw, g)) {
            g.removeEdge(vw);

            if ((branchBA = blockEdgeBranch(ux, g)) && branchRemove(ux, costs + 2, g)) return true;
            if ((branchBB = blockTwoEdgesBranch(vx, wx, g)) && branchAddAndRemove(vx, wx, costs + 3, g))
                return true;

            if (branchBB) {
                unBlockEdge(wx, g);
                unBlockEdge(vx, g);
            }
            if (branchBA) unBlockEdge(ux, g);
            g.revertChange();//vw
        }

        //--- BRANCH C
        boolean branchCA;
        boolean branchCB;
        if (branchC = blockEdgeBranch(uw, g)) {
            g.addEdge(uw);

            if ((branchCA = blockTwoEdgesBranch(ux, wx, g)) && branchRemoveDouble(ux, wx, costs + 3, g))
                return true;
            if ((branchCB = blockEdgeBranch(vx, g)) && branchAdd(vx, costs + 2, g)) return true;

            if (branchCB) unBlockEdge(vx, g);
            if (branchCA) {
                unBlockEdge(wx, g);
                unBlockEdge(ux, g);
            }
            g.revertChange();//uw
        }

        if (branchC) unBlockEdge(uw, g);
        if (branchB) unBlockEdge(vw, g);
        if (branchA) unBlockEdge(uv, g);

        return false;
    }

    static boolean branchRemove(Edge edge, int costs, Graph g) {
        g.removeEdge(edge);
        if (branch(g, costs)) return true;
        g.revertChange();
        return false;
    }

    static boolean branchAdd(Edge edge, int costs, Graph g) {
        g.addEdge(edge);
        if (branch(g, costs)) return true;
        g.revertChange();
        return false;
    }

    static boolean branchAddAndRemove(Edge edgeAdd, Edge edgeRemove, int costs, Graph g) {
        g.addEdge(edgeAdd);
        g.removeEdge(edgeRemove);
        if (branch(g, costs)) return true;
        g.revertChange();
        g.revertChange();
        return false;
    }

    static boolean branchRemoveDouble(Edge edge1, Edge edge2, int costs, Graph g) {
        g.removeEdge(edge1);
        g.removeEdge(edge2);
        if (branch(g, costs)) return true;
        g.revertChange();
        g.revertChange();
        return false;
    }

    public static void main(String... args) {

        try {
            graph = IO.parseGraph();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        Heuristic.solve();
        solve();
    }
}