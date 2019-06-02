package com.github.szgabsz91.morpher.methods.lattice.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CostCalculatorTypeTest {

    @Test
    public void testValueOf() {
        assertThat(CostCalculatorType.valueOf("DEFAULT")).isEqualTo(CostCalculatorType.DEFAULT);
        assertThat(CostCalculatorType.valueOf("ATTRIBUTE_BASED")).isEqualTo(CostCalculatorType.ATTRIBUTE_BASED);
    }

}
