package com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ZeroNodeTest {

    private Node node;

    @BeforeEach
    public void setUp() {
        this.node = new ZeroNode();
    }

    @Test
    public void testIsZero() {
        assertThat(node.isZero()).isTrue();
        assertThat(node.isInconsistent()).isFalse();
    }

    @Test
    public void testToString() {
        assertThat(node).hasToString("0");
    }

}
