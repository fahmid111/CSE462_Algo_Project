package de.umr.pace.clusterediting.exact;

public class MarkedP3 {
    P3[] belongingOneDegreesArray = new P3[20];
    int belongingOneDegreesSize = 0;

    int index;
    public P3 belongingP3 = null;
    int candidateIndex = -1;
    int updatedCandidateIndex  = -1;

    public boolean visited = false;
    public int positionOfMiddleNode = -1;

    public int startPositionOfNewExChangeP3s = 0;

    MarkedP3(int index) {
        this.index = index;
    }

    void addOneDegree(P3 p3){
        if (p3.positionInOneDegrees != -1 && belongingOneDegreesArray[p3.positionInOneDegrees] == p3) return;
        if (belongingOneDegreesSize == belongingOneDegreesArray.length){
            P3[] oldArray = belongingOneDegreesArray;
            belongingOneDegreesArray = new P3[(int) (belongingOneDegreesSize * 1.5)];
            System.arraycopy(oldArray, 0, belongingOneDegreesArray, 0, belongingOneDegreesSize);
        }
        belongingOneDegreesArray[belongingOneDegreesSize] = p3;
        p3.positionInOneDegrees = belongingOneDegreesSize;
        belongingOneDegreesSize++;
    }

    void removeOneDegree(P3 p3){
        if (p3.positionInOneDegrees == -1) return;
        if (belongingOneDegreesSize == startPositionOfNewExChangeP3s) startPositionOfNewExChangeP3s--;
        belongingOneDegreesSize--;
        if (p3.positionInOneDegrees != belongingOneDegreesSize) {
            if (p3.positionInOneDegrees >= startPositionOfNewExChangeP3s){
                belongingOneDegreesArray[p3.positionInOneDegrees] = belongingOneDegreesArray[belongingOneDegreesSize];
                belongingOneDegreesArray[p3.positionInOneDegrees].positionInOneDegrees = p3.positionInOneDegrees;
            }else{
                startPositionOfNewExChangeP3s--;
                belongingOneDegreesArray[p3.positionInOneDegrees] = belongingOneDegreesArray[startPositionOfNewExChangeP3s];
                belongingOneDegreesArray[startPositionOfNewExChangeP3s] = belongingOneDegreesArray[belongingOneDegreesSize];
                belongingOneDegreesArray[p3.positionInOneDegrees].positionInOneDegrees = p3.positionInOneDegrees;
                belongingOneDegreesArray[startPositionOfNewExChangeP3s].positionInOneDegrees = startPositionOfNewExChangeP3s;
            }
        }
        belongingOneDegreesArray[belongingOneDegreesSize] = null;
        p3.positionInOneDegrees = -1;
    }

    void clearOneDegrees(){
        for (int i = 0; i < belongingOneDegreesSize; i++) {
            belongingOneDegreesArray[i].positionInOneDegrees = -1;
            belongingOneDegreesArray[i] = null;
        }
        belongingOneDegreesSize = 0;
    }

    @Override
    public String toString() {
        return "MarkedP3{  " + belongingP3 + "  }";
    }
}