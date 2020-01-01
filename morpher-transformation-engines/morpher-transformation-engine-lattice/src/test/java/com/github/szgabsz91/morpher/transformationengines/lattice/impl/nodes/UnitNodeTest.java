package com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnitNodeTest {

    private Node node;

    @BeforeEach
    public void setUp() {
        this.node = new UnitNode();
    }

    @Test
    public void testIsUnit() {
        assertThat(node.isUnit()).isTrue();
        assertThat(node.isInconsistent()).isTrue();
    }

    @Test
    public void testToString() {
        assertThat(node).hasToString("1");
    }

}
