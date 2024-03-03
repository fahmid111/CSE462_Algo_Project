package de.umr.pace.clusterediting.exact;

import java.util.Objects;

public class Node implements Comparable {
    private final int id;
    public int posIndex = -1;
    public Node[] neighborsArray = new Node[20];
    public int degree = 0;

    Node(int id) {
        this.id = id;
    }

    public MarkedP3[] markedP3sWithMiddleNode = new MarkedP3[20];
    public int markedP3sWithMiddleNodeSize = 0;

    void addP3Middle(MarkedP3 markedP3) {
        if (markedP3sWithMiddleNodeSize == markedP3sWithMiddleNode.length) {
            MarkedP3[] oldArray = markedP3sWithMiddleNode;
            markedP3sWithMiddleNode = new MarkedP3[(int) (markedP3sWithMiddleNodeSize + 1.5)];
            System.arraycopy(oldArray, 0, markedP3sWithMiddleNode, 0, oldArray.length);
        }
        markedP3sWithMiddleNode[markedP3sWithMiddleNodeSize] = markedP3;
        markedP3.positionOfMiddleNode = markedP3sWithMiddleNodeSize;
        markedP3sWithMiddleNodeSize++;
    }

    void removeP3Middle(MarkedP3 markedP3) {
        markedP3sWithMiddleNodeSize--;
        if (markedP3sWithMiddleNodeSize != markedP3.positionOfMiddleNode) {
            markedP3sWithMiddleNode[markedP3.positionOfMiddleNode] = markedP3sWithMiddleNode[markedP3sWithMiddleNodeSize];
            markedP3sWithMiddleNode[markedP3sWithMiddleNodeSize].positionOfMiddleNode = markedP3.positionOfMiddleNode;
        }
        markedP3sWithMiddleNode[markedP3sWithMiddleNodeSize] = null;
        markedP3.positionOfMiddleNode = -1;
    }

    public void expandIfNecessary() {
        if (degree == neighborsArray.length) {
            Node[] oldArray = neighborsArray;
            neighborsArray = new Node[(int) (degree + 1.5)];
            System.arraycopy(oldArray, 0, neighborsArray, 0, oldArray.length);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id == node.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Object o) {
        Node other = (Node) o;
        return id - other.id;
    }

    public int getID() {
        return id;
    }

    @Override
    public String toString() {
        return "" + id;
    }

    public int degree() {
        return degree;
    }
}
