package com.github.szgabsz91.morpher.transformationengines.astra.impl.rules;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class RuleBucketTest {

    @Test
    public void testDefaultConstructor() {
        RuleBucket ruleBucket = new RuleBucket();
        assertThat(ruleBucket.getApplicableAtomicRules()).isEmpty();
    }

    @Test
    public void testConstructor() {
        ApplicableAtomicRule applicableAtomicRule = new ApplicableAtomicRule(null, null, 1.0, 1, 2);
        RuleBucket ruleBucket = new RuleBucket(applicableAtomicRule);
        assertThat(ruleBucket.getApplicableAtomicRules()).containsExactly(applicableAtomicRule);
    }

    @Test
    public void testAdd() {
        ApplicableAtomicRule applicableAtomicRule = new ApplicableAtomicRule(null, null, 1.0, 1, 2);
        RuleBucket ruleBucket = new RuleBucket();
        ruleBucket.add(applicableAtomicRule);
        assertThat(ruleBucket.getApplicableAtomicRules()).containsExactly(applicableAtomicRule);
    }

    @Test
    public void testOverlapping() {
        RuleBucket ruleBucket = new RuleBucket(new ApplicableAtomicRule(null, null, 1.0, 1, 2));
        assertThat(ruleBucket.isOverlapping(new ApplicableAtomicRule(null, null, 1.0, 3, 3))).isFalse();
        assertThat(ruleBucket.isOverlapping(new ApplicableAtomicRule(null, null, 1.0, 2, 3))).isTrue();
        assertThat(ruleBucket.isOverlapping(new ApplicableAtomicRule(null, null, 1.0, 0, 3))).isTrue();
    }

    @Test
    public void testEquals() {
        RuleBucket ruleBucket1 = new RuleBucket(new ApplicableAtomicRule(null, new AtomicRule("a", "b", "c", "d", 1), 1.0, 1, 2));
        RuleBucket ruleBucket2 = new RuleBucket(new ApplicableAtomicRule(null, new AtomicRule("a", "b", "c", "d", 1), 1.0, 1, 2));
        RuleBucket ruleBucket3 = new RuleBucket(new ApplicableAtomicRule(null, new AtomicRule("a", "b", "c", "e", 1), 1.0, 1, 2));

        assertThat(ruleBucket1).isEqualTo(ruleBucket1);
        assertThat(ruleBucket1).isEqualTo(ruleBucket2);
        assertThat(ruleBucket1).isNotEqualTo(null);
        assertThat(ruleBucket1).isNotEqualTo("string");
        assertThat(ruleBucket1).isNotEqualTo(ruleBucket3);
    }

    @Test
    public void testHashCode() {
        RuleBucket ruleBucket = new RuleBucket(new ApplicableAtomicRule(null, new AtomicRule("a", "b", "c", "d", 1), 1.0, 1, 2));
        int result = ruleBucket.hashCode();
        assertThat(result).isEqualTo(Objects.hash(ruleBucket.getApplicableAtomicRules()));
    }

    @Test
    public void testToString() {
        RuleBucket ruleBucket = new RuleBucket(new ApplicableAtomicRule(null, new AtomicRule("a", "b", "c", "d", 1), 1.0, 1, 2));
        assertThat(ruleBucket).hasToString("RuleBucket[" + ruleBucket.getApplicableAtomicRules() + ']');
    }

}
