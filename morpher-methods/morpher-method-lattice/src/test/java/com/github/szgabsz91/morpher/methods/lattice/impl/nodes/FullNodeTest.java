package com.github.szgabsz91.morpher.methods.lattice.impl.nodes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FullNodeTest {

    private Node node;

    @BeforeEach
    public void setUp() {
        this.node = new FullNode();
    }

    @Test
    public void testIsFull() {
        assertThat(node.isFull()).isTrue();
        assertThat(node.isInhomogeneous()).isTrue();
    }

    @Test
    public void testToString() {
        assertThat(node).hasToString("FULL");
    }

}
