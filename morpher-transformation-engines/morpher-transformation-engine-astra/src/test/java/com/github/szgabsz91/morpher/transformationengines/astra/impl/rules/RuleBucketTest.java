package com.github.szgabsz91.morpher.transformationengines.astra.impl.rules;

import org.junit.jupiter.api.Test;

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

}
