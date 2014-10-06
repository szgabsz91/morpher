package com.github.szgabsz91.morpher.methods.lattice.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LatticeBuilderTypeTest {

    @Test
    public void testValueOf() {
        assertThat(LatticeBuilderType.valueOf("FULL")).isEqualTo(LatticeBuilderType.FULL);
        assertThat(LatticeBuilderType.valueOf("HOMOGENEOUS")).isEqualTo(LatticeBuilderType.HOMOGENEOUS);
        assertThat(LatticeBuilderType.valueOf("MAXIMAL_HOMOGENEOUS")).isEqualTo(LatticeBuilderType.MAXIMAL_HOMOGENEOUS);
        assertThat(LatticeBuilderType.valueOf("MINIMAL")).isEqualTo(LatticeBuilderType.MINIMAL);
    }

}
