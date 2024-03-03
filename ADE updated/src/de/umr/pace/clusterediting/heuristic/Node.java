package de.umr.pace.clusterediting.heuristic;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Node implements Comparable {
    private final int id;
    private final Set<Node> neighbors = new HashSet<>();

    int clusterID = -1;

    Node(int id) {
        this.id = id;
    }

    Set<Node> getNeighbors() {
        return neighbors;
    }

    void addNeighbor(Node n) {
        neighbors.add(n);
    }

    void removeNeighbor(Node n) {
        neighbors.remove(n);
    }

    boolean hasNeighbor(Node n) {
        return neighbors.contains(n);
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
        return ""+id;
    }

    public int degree(){ return neighbors.size();}

}
