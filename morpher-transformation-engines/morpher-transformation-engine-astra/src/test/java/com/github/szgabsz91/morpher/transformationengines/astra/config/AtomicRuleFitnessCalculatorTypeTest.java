package com.github.szgabsz91.morpher.transformationengines.astra.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AtomicRuleFitnessCalculatorTypeTest {

    @Test
    public void testValueOf() {
        assertThat(AtomicRuleFitnessCalculatorType.valueOf("DEFAULT")).isEqualTo(AtomicRuleFitnessCalculatorType.DEFAULT);
        assertThat(AtomicRuleFitnessCalculatorType.valueOf("SMOOTH_LOCAL")).isEqualTo(AtomicRuleFitnessCalculatorType.SMOOTH_LOCAL);
        assertThat(AtomicRuleFitnessCalculatorType.valueOf("SMOOTH_GLOBAL")).isEqualTo(AtomicRuleFitnessCalculatorType.SMOOTH_GLOBAL);
    }

}
