package com.github.szgabsz91.morpher.transformationengines.tasr.impl.rules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SuffixRuleTest {

    @Test
    public void testTwoArgumentConstructor() {
        String leftHandSuffix = "a";
        String rightHandSuffix = "b";
        SuffixRule suffixRule = new SuffixRule(leftHandSuffix, rightHandSuffix);
        assertThat(suffixRule.getLeftHandSuffix()).isEqualTo(leftHandSuffix);
        assertThat(suffixRule.getRightHandSuffix()).isEqualTo(rightHandSuffix);
        assertThat(suffixRule.getFrequency()).isEqualTo(1);
    }

    @Test
    public void testThreeArgumentConstructor() {
        String leftHandSuffix = "a";
        String rightHandSuffix = "b";
        int frequency = 100;
        SuffixRule suffixRule = new SuffixRule(leftHandSuffix, rightHandSuffix, frequency);
        assertThat(suffixRule.getLeftHandSuffix()).isEqualTo(leftHandSuffix);
        assertThat(suffixRule.getRightHandSuffix()).isEqualTo(rightHandSuffix);
        assertThat(suffixRule.getFrequency()).isEqualTo(frequency);
    }

    @Test
    public void testIncrementFrequency() {
        String left = "left";
        String right = "right";
        SuffixRule suffixRule = new SuffixRule(left, right);

        assertThat(suffixRule.getLeftHandSuffix()).isEqualTo(left);
        assertThat(suffixRule.getRightHandSuffix()).isEqualTo(right);
        assertThat(suffixRule.getFrequency()).isEqualTo(1);

        suffixRule.incrementFrequency();
        assertThat(suffixRule.getFrequency()).isEqualTo(2);
    }

    @Test
    public void testEquals() {
        SuffixRule suffixRule1 = new SuffixRule("left1", "right1");
        SuffixRule suffixRule2 = new SuffixRule("left2", "right1");
        SuffixRule suffixRule3 = new SuffixRule("left1", "right2");

        assertThat(suffixRule1.equals(suffixRule1)).isTrue();
        assertThat(suffixRule1).isNotEqualTo(null);
        assertThat(suffixRule1).isNotEqualTo("string");
        assertThat(suffixRule1).isNotEqualTo(suffixRule2);
        assertThat(suffixRule1).isNotEqualTo(suffixRule3);
    }

    @Test
    public void testHashCode() {
        SuffixRule suffixRule = new SuffixRule("left", "right");
        int expected = 31 * suffixRule.getLeftHandSuffix().hashCode() + suffixRule.getRightHandSuffix().hashCode();
        assertThat(suffixRule.hashCode()).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        SuffixRule suffixRule = new SuffixRule("left", "right");
        suffixRule.incrementFrequency();
        assertThat(suffixRule).hasToString(suffixRule.getLeftHandSuffix() + " --> " + suffixRule.getRightHandSuffix() + " (" + suffixRule.getFrequency() + ")");
    }

}
