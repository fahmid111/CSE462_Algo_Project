package de.umr.pace.clusterediting.exact;

import java.util.*;

public class LowerBound {
    public MarkedP3[] markedP3sArray;
    public int markedP3sSize;
    public MarkedP3[] candidateArray;
    public int candidateArraySize;
    public MarkedP3[] updatedCandidateArray;
    public int updatedCandidateArraySize;

    LowerBound(int upperBound, int kStepSize) {
        markedP3sArray = new MarkedP3[upperBound + kStepSize + 200];
        for (int i = 0; i < markedP3sArray.length; i++) {
            markedP3sArray[i] = new MarkedP3(i);
        }
        markedP3sSize = 0;
    }

    public static boolean isInBound(P3 p3) {
        return p3.markedP3 != null;
    }

    private void doOneExchanges() {
        for (int i = markedP3sSize - 1; i >= 0; i--) {
            MarkedP3 mP3 = markedP3sArray[i];
            P3 markedP3 = mP3.belongingP3;
            if (mP3.belongingOneDegreesSize != 0) {
                P3 p3BestValue = mP3.belongingOneDegreesArray[0];
                for (int j = 1; j < mP3.belongingOneDegreesSize; j++) {
                    P3 p3 = mP3.belongingOneDegreesArray[j];
                    if (comparatorExchangeP3s.compare(p3, p3BestValue) < 0) p3BestValue = p3;
                }
                if (comparatorExchangeP3s.compare(p3BestValue, markedP3) < 0) {
                    removeP3FromBound(markedP3);
                    addP3InBound(p3BestValue);
                }
            }
        }
    }

    private void doOneExchangesOn(P3 markedP3) {
        MarkedP3 mP3 = markedP3.markedP3;
        if (mP3.belongingOneDegreesSize != 0) {
            P3 p3BestValue = mP3.belongingOneDegreesArray[0];
            for (int j = 1; j < mP3.belongingOneDegreesSize; j++) {
                P3 p3 = mP3.belongingOneDegreesArray[j];
                if (comparatorExchangeP3s.compare(p3, p3BestValue) < 0) p3BestValue = p3;
            }
            if (comparatorExchangeP3s.compare(p3BestValue, markedP3) < 0) {
                removeP3FromBound(markedP3);
                addP3InBound(p3BestValue);
            }
        }
    }

    P3[] greedyArray = new P3[50];

    boolean instanceNOTSolvableWithKGreedy(int k, P3[][] p3sByBlockedEdges, int[] sizes) {
        if (sizes[3] != 0) return true;
        int bound = markedP3sSize;
        int greedySize = 0;
        for (int i = p3sByBlockedEdges.length - 1; i >= 0; --i) {
            for (int j = 0; j < sizes[i]; j++) {
                P3 p3 = p3sByBlockedEdges[i][j];
                if (!p3.uv.isMarked && !p3.vw.isMarked && !p3.uw.isMarked) {
                    if (greedySize == greedyArray.length) {
                        P3[] oldArray = greedyArray;
                        greedyArray = new P3[(int) (oldArray.length * 1.5)];
                        System.arraycopy(oldArray, 0, greedyArray, 0, oldArray.length);
                    }
                    greedyArray[greedySize++] = p3;
                }
            }
        }
        Arrays.sort(greedyArray, 0, greedySize, comparatorExchangeP3s);
        for (int i = 0; i < greedySize; i++) {
            P3 p3 = greedyArray[i];
            if (!p3.uv.isMarked && !p3.vw.isMarked && !p3.uw.isMarked) {
                addP3InBound(p3);
                ++bound;
            }
        }
        return bound > k;
    }

    boolean instanceSolvableWithK(int k, P3[][] p3sByBlockedEdges, int[] sizes) {
        if (instanceNOTSolvableWithKGreedy(k, p3sByBlockedEdges, sizes)) return false;
        int bound = markedP3sSize;

        doOneExchanges();

        //- -- - Local Search
        while (updatedCandidateArraySize != 0) {
            P3 candidate = updatedCandidateArray[0].belongingP3;
            removeUpdatedCandidate(updatedCandidateArray[0]);
            P3[] exchangeP3s = calculateIndependentSet(candidate.markedP3);

            if (exchangeP3s == null) continue;

            removeP3FromBound(candidate);
            for (P3 exchangeP3 : exchangeP3s) addP3InBound(exchangeP3);
            bound += exchangeP3s.length - 1;
            for (P3 exchangeP3 : exchangeP3s) doOneExchangesOn(exchangeP3);
        }
        if (bound > k) return false;

        if (k == bound) {
            lbkSize = 0;
            for (int i = Edge.markedEdgeCount; i < Edge.markedNonMarkedEdges.length; i++) {
                Edge e = Edge.markedNonMarkedEdges[i];
                for (int j = 0; j < e.p3sNotInLowerBoundCount; j++) {
                    P3 p3 = e.p3sNotInLowerBoundArray[j];
                    if (!p3.isActive) continue;
                    if (isOneDegree(p3)) {
                        for (int l = 0; l < lbkSize; l++) {
                            P3 exchangeP3 = lbkArray[l];
                            if (getCandidateOfOneDegree(exchangeP3) == getCandidateOfOneDegree(p3) && !areNeighborsWhenKIsLowerBound(exchangeP3, p3)) {
                                for (int o = 0; o < lbkSize; o++) lbkArray[o].isActive = true;
                                return false;
                            }
                        }
                        if (lbkSize == 10000) {
                            for (int o = 0; o < lbkSize; o++) lbkArray[o].isActive = true;
                            return true;
                        }
                        if (lbkSize == lbkArray.length) expandLbkArray();
                        lbkArray[lbkSize++] = p3;
                        p3.isActive = false;
                    }
                }
            }
            for (int i = 0; i < lbkSize; i++) lbkArray[i].isActive = true;
        }
        return true;
    }

    P3[] lbkArray = new P3[100];
    int lbkSize = 0;

    private void expandLbkArray() {
        P3[] oldArray = lbkArray;
        lbkArray = new P3[(int) (oldArray.length * 1.5)];
        System.arraycopy(oldArray, 0, lbkArray, 0, oldArray.length);
    }

    private boolean areNeighborsWhenKIsLowerBound(P3 p3a, P3 p3b) {
        return !p3a.uw.isBlocked && p3a.uw.isMarked && p3a.uw == p3b.uw
                || !p3a.uv.isBlocked && p3a.uv.isMarked && (p3a.uv == p3b.uv || p3a.uv == p3b.vw)
                || !p3a.vw.isBlocked && p3a.vw.isMarked && (p3a.vw == p3b.uv || p3a.vw == p3b.vw);
    }

    static private boolean isOneDegree(P3 p3) {
        return p3.p3sInLowerBoundSize == 1;
    }

    static private P3 getCandidateOfOneDegree(P3 p3) {
        return p3.p3sInLowerBoundArray[0];
    }

    static private boolean isCandidate(P3 p3) {
        return p3.markedP3.belongingOneDegreesSize >= 2;
    }

    private List<P3> getFilteredCandidates() {
        List<P3> candidates = new LinkedList<>();
        for (int i = 0; i < markedP3sSize; i++)
            if (isCandidate(markedP3sArray[i].belongingP3)) candidates.add(markedP3sArray[i].belongingP3);
        return candidates;
    }

    public void expandIfNecessary() {
        if (markedP3sSize == markedP3sArray.length) {
            MarkedP3[] oldArray = markedP3sArray;
            markedP3sArray = new MarkedP3[oldArray.length + 500];
            System.arraycopy(oldArray, 0, markedP3sArray, 0, oldArray.length);
            for (int i = markedP3sSize; i < markedP3sArray.length; i++) {
                markedP3sArray[i] = new MarkedP3(i);
            }
        }
    }

    void addP3InBound(P3 p3) {
        expandIfNecessary();

        p3.markedP3 = markedP3sArray[markedP3sSize];
        markedP3sArray[markedP3sSize].belongingP3 = p3;
        markedP3sSize++;
        p3.markedP3.startPositionOfNewExChangeP3s = 0;

        if (!p3.uv.isBlocked) p3.uv.setMarkedTrue();
        if (!p3.vw.isBlocked) p3.vw.setMarkedTrue();
        if (!p3.uw.isBlocked) p3.uw.setMarkedTrue();

        p3.uv.removeFromP3sNotInLowerBoundWithThisEdgeAsUV(p3);
        p3.vw.removeFromP3sNotInLowerBoundWithThisEdgeAsVW(p3);
        p3.uw.removeFromP3sNotInLowerBoundWithThisEdgeAsUW(p3);

        p3.uv.addToP3sInLowerBoundWithThisEdgeAsUV(p3);
        p3.vw.addToP3sInLowerBoundWithThisEdgeAsVW(p3);
        p3.uw.addToP3sInLowerBoundWithThisEdgeAsUW(p3);

        addP3InBoundLocalSearch(p3);
        p3.middleNode.addP3Middle(p3.markedP3);
    }

    void addP3InBoundLocalSearch(P3 markedP3) {
        if (!markedP3.uv.isBlocked) addP3InBoundLocalSearchAux(markedP3, markedP3.uv);
        if (!markedP3.vw.isBlocked) addP3InBoundLocalSearchAux(markedP3, markedP3.vw);
        if (!markedP3.uw.isBlocked) addP3InBoundLocalSearchAux(markedP3, markedP3.uw);
    }

    void addP3InBoundLocalSearchAux(P3 markedP3, Edge edge) {
        for (int i = 0; i < edge.p3sNotInLowerBoundCount; i++) {
            P3 neighbor = edge.p3sNotInLowerBoundArray[i];

            if (isOneDegree(neighbor)) {
                updateRemoveFromOneDegree(getCandidateOfOneDegree(neighbor), neighbor);
            }
            neighbor.addP3InCorrespondingLowerBoundList(markedP3);
            if (isOneDegree(neighbor)) {
                updateCandidatesAddInOneDegree(markedP3, neighbor);
            }
        }
    }

    void removeP3FromBound(P3 p3) {
        p3.middleNode.removeP3Middle(p3.markedP3);
        markedP3sSize--;
        removeCandidate(p3.markedP3);
        removeUpdatedCandidate(p3.markedP3);

        int index = p3.markedP3.index;
        if (index != markedP3sSize) {
            MarkedP3 aux = markedP3sArray[index];

            markedP3sArray[index] = markedP3sArray[markedP3sSize];
            markedP3sArray[markedP3sSize] = aux;

            markedP3sArray[index].index = index;
            markedP3sArray[markedP3sSize].index = markedP3sSize;
        }
        p3.markedP3.belongingP3 = null;
        p3.markedP3 = null;

        p3.uv.setMarkedFalse();
        p3.vw.setMarkedFalse();
        p3.uw.setMarkedFalse();

        p3.uv.removeFromP3sInLowerBoundWithThisEdgeAsUV(p3);
        p3.vw.removeFromP3sInLowerBoundWithThisEdgeAsVW(p3);
        p3.uw.removeFromP3sInLowerBoundWithThisEdgeAsUW(p3);

        p3.uv.addToP3sNotInLowerBoundWithThisEdgeAsUV(p3);
        p3.vw.addToP3sNotInLowerBoundWithThisEdgeAsVW(p3);
        p3.uw.addToP3sNotInLowerBoundWithThisEdgeAsUW(p3);

        if (!p3.uv.isBlocked) removeFromBoundUpdateOneDegrees(p3, p3.uv);
        if (!p3.vw.isBlocked) removeFromBoundUpdateOneDegrees(p3, p3.vw);
        if (!p3.uw.isBlocked) removeFromBoundUpdateOneDegrees(p3, p3.uw);

        markedP3sArray[markedP3sSize].clearOneDegrees();
    }

    void removeFromBoundUpdateOneDegrees(P3 wasMarkedP3, Edge edge) {
        for (int i = 0; i < edge.p3sNotInLowerBoundCount; i++) {
            P3 neighbor = edge.p3sNotInLowerBoundArray[i];
            if (neighbor == wasMarkedP3) continue;
            neighbor.removeP3FromCorrespondingLowerBoundList(wasMarkedP3);
            if (isOneDegree(neighbor)) {
                P3 potCandidate = getCandidateOfOneDegree(neighbor);
                updateCandidatesAddInOneDegree(potCandidate, neighbor);
            }
        }
    }

    void addCreatedP3(P3 p3) {
        p3.clearP3InCorrespondingLowerBoundList();
        if (!p3.uv.isBlocked)
            for (int i = 0; i < p3.uv.p3sInLowerBoundCount; i++)
                p3.addP3InCorrespondingLowerBoundList(p3.uv.p3sInLowerBoundArray[i]);
        if (!p3.vw.isBlocked)
            for (int i = 0; i < p3.vw.p3sInLowerBoundCount; i++)
                p3.addP3InCorrespondingLowerBoundList(p3.vw.p3sInLowerBoundArray[i]);
        if (!p3.uw.isBlocked)
            for (int i = 0; i < p3.uw.p3sInLowerBoundCount; i++)
                p3.addP3InCorrespondingLowerBoundList(p3.uw.p3sInLowerBoundArray[i]);

        if (isOneDegree(p3)) {
            P3 potCandidate = getCandidateOfOneDegree(p3);
            updateCandidatesAddInOneDegree(potCandidate, p3);
        }
    }

    void removeAffectedP3(P3 p3) {
        if (isInBound(p3)) {
            removeP3FromBound(p3);
        } else {
            if (isOneDegree(p3)) {
                P3 candidate = getCandidateOfOneDegree(p3);
                updateRemoveFromOneDegree(candidate, p3);
            }
        }
    }

    void blockEdge(Edge e) {
        P3[] p3s = e.p3sInLowerBoundArray;
        for (int i = 0; i < e.p3sInLowerBoundCount; i++) {
            P3 p3 = p3s[i];
            for (int index = 0; index < e.p3sNotInLowerBoundCount; index++) {
                P3 neighbor = e.p3sNotInLowerBoundArray[index];
                p3.markedP3.removeOneDegree(neighbor);//potential OneDegree
                neighbor.removeP3FromCorrespondingLowerBoundList(p3);
                if (isOneDegree(neighbor)) {
                    updateCandidatesAddInOneDegree(getCandidateOfOneDegree(neighbor), neighbor);
                }
            }

            if (p3.markedP3.belongingOneDegreesSize < 2) {
                removeCandidate(p3.markedP3);
                removeUpdatedCandidate(p3.markedP3);
            }
        }
        e.setMarkedFalseAndBlock();
    }

    void unBlockEdge(Edge e) {
        P3[] p3s = e.p3sInLowerBoundArray;
        e.unBlockSwap();
        for (int i = e.p3sInLowerBoundCount - 1; i >= 0; i--) {
            removeP3FromBound(p3s[i]);
        }
    }

    private void updateCandidatesAddInOneDegree(P3 candidate, P3 oneDegreeP3) {
        candidate.markedP3.addOneDegree(oneDegreeP3);
        int size = candidate.markedP3.belongingOneDegreesSize;
        if (size == 2) addCandidate(candidate.markedP3);
        if (size >= 2) addUpdatedCandidate(candidate.markedP3);
    }

    private void updateRemoveFromOneDegree(P3 candidate, P3 oldOneDegree) {
        candidate.markedP3.removeOneDegree(oldOneDegree);

        if (!isCandidate(candidate)) {
            removeCandidate(candidate.markedP3);
            removeUpdatedCandidate(candidate.markedP3);
        }
    }

    public void resetHelpData() {
        candidateArray = new MarkedP3[500];
        candidateArraySize = 0;
        updatedCandidateArray = new MarkedP3[500];
        updatedCandidateArraySize = 0;
    }

    static final Comparator<P3> comparatorExchangeP3s = new Comparator<P3>() {
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

    public static void sortP3s(P3[][] p3sByBlockedEdges, int[] sizes) {
        for (int i = p3sByBlockedEdges.length - 1; i >= 0; --i) {
            Arrays.sort(p3sByBlockedEdges[i], 0, sizes[i], comparatorExchangeP3s);
            for (int j = 0; j < sizes[i]; j++) p3sByBlockedEdges[i][j].positionInP3sByBlockedEdges = j;
        }
    }

    int calculateLowerBound(P3[][] p3sByBlockedEdges, int[] sizes, Graph g) {
        resetHelpData();
        int bound = 0;

        sortP3s(p3sByBlockedEdges, sizes);
        //- -- - Greedy packing
        for (int i = p3sByBlockedEdges.length - 1; i >= 0; --i) {
            for (int j = 0; j < sizes[i]; j++) {
                P3 p3 = p3sByBlockedEdges[i][j];
                if (!p3.uv.isMarked && !p3.vw.isMarked && !p3.uw.isMarked) {
                    addP3InBound(p3);
                    ++bound;
                }
            }
        }
        doOneExchanges();

        //- -- - Local Search
        boolean progress = true;
        while (progress) {
            List<P3> candidates = getFilteredCandidates();

            progress = false;
            for (P3 candidate : candidates) {
                if (!isCandidate(candidate)) continue;
                List<P3> exchangeP3s = calculateIndependentSetInitial(candidate.markedP3);
                if (exchangeP3s == null) continue;
                progress = true;
                removeP3FromBound(candidate);
                for (P3 exchangeP3 : exchangeP3s) addP3InBound(exchangeP3);
                for (P3 exchangeP3 : exchangeP3s) doOneExchangesOn(exchangeP3);
                bound += exchangeP3s.size() - 1;
            }
        }

        doOneExchanges();
        progress = true;
        while (progress) {
            List<P3> candidates = getFilteredCandidates();
            progress = false;
            for (P3 candidate : candidates) {
                if (!isCandidate(candidate)) continue;
                List<P3> exchangeP3s = calculateIndependentSetInitial(candidate.markedP3);
                if (exchangeP3s == null) continue;
                progress = true;
                removeP3FromBound(candidate);
                for (P3 exchangeP3 : exchangeP3s) addP3InBound(exchangeP3);
                bound += exchangeP3s.size() - 1;
            }
        }

//        - -- - C4 packing
        List<Edge> addedByC4Packing = new LinkedList<>();
        for (int index = 0; index < markedP3sSize; index++) {
            P3 markedP3 = markedP3sArray[index].belongingP3;
            boolean isCEdgeBlocked = markedP3.uw.isBlocked;
            P3[] notMarkedSameUWP3s = markedP3.uw.p3sNotInLowerBoundArray;
            for (int i = 0; i < markedP3.uw.p3sNotInLowerBoundCount; i++) {
                P3 sameUWp3 = notMarkedSameUWP3s[i];
                Edge vv_;
                if (markedP3.id != sameUWp3.id
                        && (isCEdgeBlocked || !g.areNeighbors(markedP3.middleNode, sameUWp3.middleNode))
                        && !sameUWp3.uv.isMarked && !sameUWp3.vw.isMarked
                        && !(vv_ = g.getEdge(markedP3.middleNode, sameUWp3.middleNode)).isMarked
                ) {
                    if (!sameUWp3.uv.isBlocked) {
                        sameUWp3.uv.isMarked = true;
                        addedByC4Packing.add(sameUWp3.uv);
                    }
                    if (!sameUWp3.vw.isBlocked) {
                        sameUWp3.vw.isMarked = true;
                        addedByC4Packing.add(sameUWp3.vw);
                    }
                    if (!vv_.isBlocked) {
                        vv_.isMarked = true;
                        addedByC4Packing.add(vv_);
                    }
                    ++bound;
                    break;
                }
            }
        }
        for (Edge e : addedByC4Packing) e.isMarked = false;
        return bound;
    }

    void addCandidate(MarkedP3 p3) {
        if (p3.candidateIndex == -1) {
            expandCandidatesIfNecessary();
            candidateArray[candidateArraySize] = p3;
            p3.candidateIndex = candidateArraySize;
            candidateArraySize++;
        }
    }

    void removeCandidate(MarkedP3 p3) {
        if (p3.candidateIndex != -1) {
            candidateArraySize--;
            candidateArray[p3.candidateIndex] = candidateArray[candidateArraySize];
            candidateArray[p3.candidateIndex].candidateIndex = p3.candidateIndex;
            p3.candidateIndex = -1;
        }
    }

    void expandCandidatesIfNecessary() {
        if (candidateArraySize == candidateArray.length) {
            MarkedP3[] oldArray = candidateArray;
            candidateArray = new MarkedP3[(int) (oldArray.length * 1.5)];
            System.arraycopy(oldArray, 0, candidateArray, 0, oldArray.length);
        }
    }

    void addUpdatedCandidate(MarkedP3 p3) {
        if (p3.updatedCandidateIndex == -1) {
            expandUpdatedCandidatesIfNecessary();
            updatedCandidateArray[updatedCandidateArraySize] = p3;
            p3.updatedCandidateIndex = updatedCandidateArraySize;
            updatedCandidateArraySize++;
        }
    }

    void removeUpdatedCandidate(MarkedP3 p3) {
        if (p3.updatedCandidateIndex != -1) {
            updatedCandidateArraySize--;
            updatedCandidateArray[p3.updatedCandidateIndex] = updatedCandidateArray[updatedCandidateArraySize];
            updatedCandidateArray[p3.updatedCandidateIndex].updatedCandidateIndex = p3.updatedCandidateIndex;
            p3.updatedCandidateIndex = -1;
        }
    }

    void expandUpdatedCandidatesIfNecessary() {
        if (updatedCandidateArraySize == updatedCandidateArray.length) {
            MarkedP3[] oldArray = updatedCandidateArray;
            updatedCandidateArray = new MarkedP3[(int) (oldArray.length * 1.5)];
            System.arraycopy(oldArray, 0, updatedCandidateArray, 0, oldArray.length);
        }
    }


    static private P3[] calculateIndependentSet(MarkedP3 candidate) {
        int i = 0;
        int j = candidate.startPositionOfNewExChangeP3s;
        while (i < candidate.startPositionOfNewExChangeP3s || j < candidate.belongingOneDegreesSize) {
            if (i < candidate.startPositionOfNewExChangeP3s) {
                P3 p3_1 = candidate.belongingOneDegreesArray[i];
                i++;
                for (int k = candidate.startPositionOfNewExChangeP3s; k < j; k++) {
                    P3 p3_2 = candidate.belongingOneDegreesArray[k];
                    if (p3_1.hasNotNeighbor(p3_2)) {
                        for (int l = k + 1; l < candidate.belongingOneDegreesSize; l++) {
                            P3 p3_3 = candidate.belongingOneDegreesArray[l];
                            if (p3_3.hasNotNeighbor(p3_1) && p3_3.hasNotNeighbor(p3_2)) {
                                return new P3[]{p3_1, p3_2, p3_3};
                            }
                        }
                        return new P3[]{p3_1, p3_2};
                    }
                }
            }
            if (j < candidate.belongingOneDegreesSize) {
                P3 p3_1 = candidate.belongingOneDegreesArray[j];
                j++;
                for (int k = candidate.startPositionOfNewExChangeP3s; k < j; k++) {
                    P3 p3_2 = candidate.belongingOneDegreesArray[k];
                    if (p3_1.hasNotNeighbor(p3_2)) {
                        for (int l = k + 1; l < candidate.belongingOneDegreesSize; l++) {
                            P3 p3_3 = candidate.belongingOneDegreesArray[l];
                            if (p3_3.hasNotNeighbor(p3_1) && p3_3.hasNotNeighbor(p3_2)) {
                                return new P3[]{p3_1, p3_2, p3_3};
                            }
                        }
                        for (int l = k - candidate.startPositionOfNewExChangeP3s; l < candidate.startPositionOfNewExChangeP3s; l++) {
                            P3 p3_3 = candidate.belongingOneDegreesArray[l];
                            if (p3_3.hasNotNeighbor(p3_1) && p3_3.hasNotNeighbor(p3_2)) {
                                return new P3[]{p3_1, p3_2, p3_3};
                            }
                        }
                        return new P3[]{p3_1, p3_2};
                    }
                }
                for (int k = 0; k < i; k++) {
                    P3 p3_2 = candidate.belongingOneDegreesArray[k];
                    if (p3_1.hasNotNeighbor(p3_2)) {
                        for (int l = k + 1; l < candidate.belongingOneDegreesSize; l++) {
                            P3 p3_3 = candidate.belongingOneDegreesArray[l];
                            if (p3_3.hasNotNeighbor(p3_1) && p3_3.hasNotNeighbor(p3_2)) {
                                return new P3[]{p3_1, p3_2, p3_3};
                            }
                        }
                        return new P3[]{p3_1, p3_2};
                    }
                }
            }

        }

        candidate.startPositionOfNewExChangeP3s = candidate.belongingOneDegreesSize;
        return null;
    }


    static private List<P3> calculateIndependentSetInitial(MarkedP3 candidate) {
        for (int j = 0; j < candidate.belongingOneDegreesSize; j++) {
            P3 p3_2 = candidate.belongingOneDegreesArray[j];
            for (int i = 0; i < j; i++) {
                P3 p3_1 = candidate.belongingOneDegreesArray[i];
                if (p3_1.hasNotNeighbor(p3_2)) {
                    List<P3> result = new LinkedList<>();
                    result.add(p3_1);
                    result.add(p3_2);
                    return result;
                }
            }
        }
        return null;
    }
}