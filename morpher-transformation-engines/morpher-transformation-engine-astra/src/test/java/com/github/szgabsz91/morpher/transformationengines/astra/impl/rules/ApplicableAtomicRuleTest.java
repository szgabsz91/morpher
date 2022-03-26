package com.github.szgabsz91.morpher.transformationengines.astra.impl.rules;

import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.componentaccessors.IAtomicRuleComponentAccessor;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.componentaccessors.StraightAtomicRuleComponentAccessor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicableAtomicRuleTest {

    @Test
    public void testConstructorAndGetters() {
        IAtomicRuleComponentAccessor atomicRuleComponentAccessor = StraightAtomicRuleComponentAccessor.get();
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        double fitness = 0.5;
        int leftIndex = 1;
        int rightIndex = 2;
        ApplicableAtomicRule applicableAtomicRule = new ApplicableAtomicRule(atomicRuleComponentAccessor, atomicRule, fitness, leftIndex, rightIndex);
        assertThat(applicableAtomicRule.getAtomicRuleComponentAccessor()).isSameAs(atomicRuleComponentAccessor);
        assertThat(applicableAtomicRule.getAtomicRule()).isEqualTo(atomicRule);
        assertThat(applicableAtomicRule.getFitness()).isEqualTo(fitness);
        assertThat(applicableAtomicRule.getLeftIndex()).isEqualTo(leftIndex);
        assertThat(applicableAtomicRule.getRightIndex()).isEqualTo(rightIndex);
    }

}
