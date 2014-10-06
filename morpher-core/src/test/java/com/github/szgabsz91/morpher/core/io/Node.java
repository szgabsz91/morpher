package com.github.szgabsz91.morpher.core.io;

public class Node {

    private final String label;
    private Node next;

    public Node(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return label.equals(node.label);
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }

    @Override
    public String toString() {
        return "Node[label=" + label + "]";
    }

}
