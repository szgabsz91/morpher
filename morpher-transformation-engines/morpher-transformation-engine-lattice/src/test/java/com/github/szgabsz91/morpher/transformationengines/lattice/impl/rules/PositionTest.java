package com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PositionTest {

    @Test
    public void testIdentity() {
        Position identity = Position.identity();
        assertThat(identity.getIndex()).isEqualTo(0);
        assertThat(identity.isIdentity()).isTrue();
    }

    @Test
    public void testGetIndex() {
        int index = 5;
        Position position = Position.of(index);
        assertThat(position.getIndex()).isEqualTo(index);
    }

    @Test
    public void testIsIdentity() {
        Position identity = Position.identity();
        assertThat(identity.isIdentity()).isTrue();

        Position nonIdentity = Position.of(1);
        assertThat(nonIdentity.isIdentity()).isFalse();
    }

    @Test
    public void testEquals() {
        Position position1 = Position.of(4);
        Position position2 = Position.of(5);

        assertThat(position1.equals(position1)).isTrue();
        assertThat(position1.equals(null)).isFalse();
        assertThat(position1).isNotEqualTo("string");
        assertThat(position1).isNotEqualTo(position2);
    }

    @Test
    public void testHashCode() {
        int index = 5;
        Position position = Position.of(index);
        assertThat(position.hashCode()).isEqualTo(index);
    }

    @Test
    public void testToString() {
        Position position = Position.of(5);
        assertThat(position).hasToString("[5]");
    }

}
