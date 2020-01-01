package com.github.szgabsz91.morpher.transformationengines.lattice.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LatticeBuilderTypeTest {

    @Test
    public void testValueOf() {
        assertThat(LatticeBuilderType.valueOf("COMPLETE")).isEqualTo(LatticeBuilderType.COMPLETE);
        assertThat(LatticeBuilderType.valueOf("CONSISTENT")).isEqualTo(LatticeBuilderType.CONSISTENT);
        assertThat(LatticeBuilderType.valueOf("MAXIMAL_CONSISTENT")).isEqualTo(LatticeBuilderType.MAXIMAL_CONSISTENT);
        assertThat(LatticeBuilderType.valueOf("MINIMAL")).isEqualTo(LatticeBuilderType.MINIMAL);
    }

}
