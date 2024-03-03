package de.umr.pace.clusterediting.heuristic;

import java.util.Objects;

public class Edge implements Comparable {
    public Node a, b;
    private int hash;
    Edge(Node a, Node b) {
        if (a.compareTo(b) < 0) {
            this.a = a;
            this.b = b;
        } else {
            this.a = b;
            this.b = a;
        }
        hash = Objects.hash(this.a, this.b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        Edge edge = (Edge) o;
        return a.equals(edge.a) &&
                b.equals(edge.b);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public int compareTo(Object o) {
        Edge other = (Edge) o;
        if (a.equals(other.a)) {
            return b.compareTo(other.b);
        }
        if (a.equals(other.b)) {
            return b.compareTo(other.a);
        }
        if (b.equals(other.b)) {
            return a.compareTo(other.a);
        }
        if (b.equals(other.a)) {
            return a.compareTo(other.b);
        }
        return 0;
    }

    @Override
    public String toString() {
        return a.toString() + ", " + b.toString();
    }
}
