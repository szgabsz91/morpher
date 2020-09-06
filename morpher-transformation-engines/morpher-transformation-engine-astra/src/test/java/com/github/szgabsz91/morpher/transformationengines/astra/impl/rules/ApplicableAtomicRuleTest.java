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

    @Test
    public void testSetLeftIndex() {
        int leftIndex = 1;
        IAtomicRuleComponentAccessor atomicRuleComponentAccessor = StraightAtomicRuleComponentAccessor.get();
        ApplicableAtomicRule applicableAtomicRule = new ApplicableAtomicRule(atomicRuleComponentAccessor, new AtomicRule("a", "b", "c", "d", 1), 0.5, leftIndex + 1, 0);
        assertThat(applicableAtomicRule.getLeftIndex()).isNotEqualTo(leftIndex);
        applicableAtomicRule.setLeftIndex(leftIndex);
        assertThat(applicableAtomicRule.getLeftIndex()).isEqualTo(leftIndex);
    }

    @Test
    public void testSetRightIndex() {
        int rightIndex = 1;
        IAtomicRuleComponentAccessor atomicRuleComponentAccessor = StraightAtomicRuleComponentAccessor.get();
        ApplicableAtomicRule applicableAtomicRule = new ApplicableAtomicRule(atomicRuleComponentAccessor, new AtomicRule("a", "b", "c", "d", 1), 0.5, 0, rightIndex + 1);
        assertThat(applicableAtomicRule.getRightIndex()).isNotEqualTo(rightIndex);
        applicableAtomicRule.setRightIndex(rightIndex);
        assertThat(applicableAtomicRule.getRightIndex()).isEqualTo(rightIndex);
    }

    @Test
    public void testEquals() {
        IAtomicRuleComponentAccessor atomicRuleComponentAccessor = StraightAtomicRuleComponentAccessor.get();
        ApplicableAtomicRule applicableAtomicRule1 = new ApplicableAtomicRule(atomicRuleComponentAccessor, new AtomicRule("a", "b", "c", "d", 1), 0.5, 1, 2);
        ApplicableAtomicRule applicableAtomicRule2 = new ApplicableAtomicRule(atomicRuleComponentAccessor, new AtomicRule("b", "b", "c", "d", 1), 0.6, 1, 2);
        ApplicableAtomicRule applicableAtomicRule3 = new ApplicableAtomicRule(atomicRuleComponentAccessor, new AtomicRule("a", "b", "c", "d", 1), 0.7, 100, 2);
        ApplicableAtomicRule applicableAtomicRule4 = new ApplicableAtomicRule(atomicRuleComponentAccessor, new AtomicRule("a", "b", "c", "d", 1), 0.8, 1, 100);
        ApplicableAtomicRule applicableAtomicRule5 = new ApplicableAtomicRule(atomicRuleComponentAccessor, new AtomicRule("a", "b", "c", "d", 1), 0.9, 1, 2);

        assertThat(applicableAtomicRule1.equals(applicableAtomicRule1)).isTrue();
        assertThat(applicableAtomicRule1).isEqualTo(applicableAtomicRule5);
        assertThat(applicableAtomicRule1.equals(null)).isFalse();
        assertThat(applicableAtomicRule1).isNotEqualTo("string");
        assertThat(applicableAtomicRule1).isNotEqualTo(applicableAtomicRule2);
        assertThat(applicableAtomicRule1).isNotEqualTo(applicableAtomicRule3);
        assertThat(applicableAtomicRule1).isNotEqualTo(applicableAtomicRule4);
    }

    @Test
    public void testHashCode() {
        IAtomicRuleComponentAccessor atomicRuleComponentAccessor = StraightAtomicRuleComponentAccessor.get();
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        int leftIndex = 1;
        int rightIndex = 2;
        ApplicableAtomicRule applicableAtomicRule = new ApplicableAtomicRule(atomicRuleComponentAccessor, atomicRule, 1.0, leftIndex, rightIndex);
        int result = applicableAtomicRule.hashCode();

        int expected = atomicRule.hashCode();
        expected = 31 * expected + leftIndex;
        expected = 31 * expected + rightIndex;

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToStringWithNullFitness() {
        IAtomicRuleComponentAccessor atomicRuleComponentAccessor = StraightAtomicRuleComponentAccessor.get();
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        double fitness = 0.5;
        int leftIndex = 1;
        int rightIndex = 2;
        ApplicableAtomicRule applicableAtomicRule = new ApplicableAtomicRule(atomicRuleComponentAccessor, atomicRule, fitness, leftIndex, rightIndex);
        assertThat(applicableAtomicRule).hasToString("ApplicableAtomicRule[" + leftIndex + ", " + rightIndex + ", " + atomicRule + " (" + fitness + ")" + "]");
    }

}
