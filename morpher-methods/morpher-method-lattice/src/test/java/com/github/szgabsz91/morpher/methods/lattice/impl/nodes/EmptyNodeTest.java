package com.github.szgabsz91.morpher.methods.lattice.impl.nodes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmptyNodeTest {

    private Node node;

    @BeforeEach
    public void setUp() {
        this.node = new EmptyNode();
    }

    @Test
    public void testIsEmpty() {
        assertThat(node.isEmpty()).isTrue();
        assertThat(node.isInhomogeneous()).isFalse();
    }

    @Test
    public void testToString() {
        assertThat(node).hasToString("EMPTY");
    }

}
