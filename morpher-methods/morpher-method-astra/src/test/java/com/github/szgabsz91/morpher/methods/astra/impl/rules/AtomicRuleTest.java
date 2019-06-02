package com.github.szgabsz91.morpher.methods.astra.impl.rules;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AtomicRuleTest {

    @Test
    public void testConstructorWithFrequencyAndGetters() {
        String prefix = "prefix";
        String from = "from";
        String to = "to";
        String postfix = "postfix";
        String context = prefix + from + postfix;
        int wordFrequency = 5;

        AtomicRule atomicRule = new AtomicRule(prefix, from, to, postfix, wordFrequency);

        assertThat(atomicRule.getPrefix()).isEqualTo(prefix);
        assertThat(atomicRule.getFrom()).isEqualTo(from);
        assertThat(atomicRule.getTo()).isEqualTo(to);
        assertThat(atomicRule.getPostfix()).isEqualTo(postfix);
        assertThat(atomicRule.getContext()).isEqualTo(context);
        assertThat(atomicRule.getSupport()).isEqualTo(1);
        assertThat(atomicRule.getWordFrequency()).isEqualTo(wordFrequency);
        assertThat(atomicRule.getAggregatedSupport()).isEqualTo(wordFrequency);

        atomicRule.merge(atomicRule);
        assertThat(atomicRule.getSupport()).isEqualTo(2);
        assertThat(atomicRule.getWordFrequency()).isEqualTo(2 * wordFrequency);
        assertThat(atomicRule.getAggregatedSupport()).isEqualTo(4 * wordFrequency);
    }

    @Test
    public void testMerge() {
        AtomicRule atomicRule1 = new AtomicRule("$prefix#", "$from#", "$to#", "$postfix#", 2);
        AtomicRule atomicRule2 = new AtomicRule("$prefix#", "$from#", "$to#", "$postfix#", 3);
        atomicRule1.merge(atomicRule2);
        assertThat(atomicRule1.getSupport()).isEqualTo(2);
        assertThat(atomicRule1.getWordFrequency()).isEqualTo(5);
    }

    @Test
    public void testSetSupportAndWordFrequency() {
        AtomicRule atomicRule = new AtomicRule("$prefix#", "$from#", "$to#", "$postfix#", 100, 100);
        atomicRule.setSupportAndWordFrequency(List.of(
                new AtomicRule("$prefix#", "$from#", "$to#", "$postfix#", 2, 3),
                new AtomicRule("$prefix#", "$from#", "$to#", "$postfix#", 4, 5)
        ));
        assertThat(atomicRule.getSupport()).isEqualTo(6);
        assertThat(atomicRule.getWordFrequency()).isEqualTo(8);
    }

    @Test
    public void testReverse() {
        AtomicRule atomicRule = new AtomicRule("$prefix#", "$from#", "$to#", "$postfix#", 3);
        AtomicRule reversed = atomicRule.reverse();
        assertThat(reversed.getPrefix()).isEqualTo("$xiftsop#");
        assertThat(reversed.getFrom()).isEqualTo("$morf#");
        assertThat(reversed.getTo()).isEqualTo("$ot#");
        assertThat(reversed.getPostfix()).isEqualTo("$xiferp#");
        assertThat(reversed.getSupport()).isEqualTo(atomicRule.getSupport());
    }

    @Test
    public void testFullEquals() {
        AtomicRule atomicRule1 = new AtomicRule("prefix1", "from1", "to1", "postfix1", 1, 1);
        AtomicRule atomicRule2 = new AtomicRule("prefix1", "from1", "to1", "postfix1", 1, 1);
        AtomicRule atomicRule3 = new AtomicRule("prefix2", "from1", "to1", "postfix1", 1, 1);
        AtomicRule atomicRule4 = new AtomicRule("prefix1", "from2", "to1", "postfix1", 1, 1);
        AtomicRule atomicRule5 = new AtomicRule("prefix1", "from1", "to2", "postfix1", 1, 1);
        AtomicRule atomicRule6 = new AtomicRule("prefix1", "from1", "to1", "postfix2", 1, 1);
        AtomicRule atomicRule7 = new AtomicRule("prefix1", "from1", "to1", "postfix1", 2, 1);
        AtomicRule atomicRule8 = new AtomicRule("prefix1", "from1", "to1", "postfix1", 1, 2);

        assertThat(atomicRule1.fullEquals(atomicRule1)).isTrue();
        assertThat(atomicRule1.fullEquals(atomicRule2)).isTrue();
        assertThat(atomicRule1.fullEquals(null)).isFalse();
        assertThat(atomicRule1.fullEquals("string")).isFalse();
        assertThat(atomicRule1.fullEquals(atomicRule3)).isFalse();
        assertThat(atomicRule1.fullEquals(atomicRule4)).isFalse();
        assertThat(atomicRule1.fullEquals(atomicRule5)).isFalse();
        assertThat(atomicRule1.fullEquals(atomicRule6)).isFalse();
        assertThat(atomicRule1.fullEquals(atomicRule7)).isFalse();
        assertThat(atomicRule1.fullEquals(atomicRule8)).isFalse();
    }

    @Test
    public void testEquals() {
        AtomicRule atomicRule1 = new AtomicRule("prefix1", "from1", "to1", "postfix1", 1);
        AtomicRule atomicRule2 = new AtomicRule("prefix1", "from1", "to1", "postfix1", 1);
        AtomicRule atomicRule3 = new AtomicRule("prefix2", "from1", "to1", "postfix1", 1);
        AtomicRule atomicRule4 = new AtomicRule("prefix1", "from2", "to1", "postfix1", 1);
        AtomicRule atomicRule5 = new AtomicRule("prefix1", "from1", "to2", "postfix1", 1);
        AtomicRule atomicRule6 = new AtomicRule("prefix1", "from1", "to1", "postfix2", 1);

        assertThat(atomicRule1).isEqualTo(atomicRule1);
        assertThat(atomicRule1).isEqualTo(atomicRule2);
        assertThat(atomicRule1).isNotEqualTo(null);
        assertThat(atomicRule1).isNotEqualTo("string");
        assertThat(atomicRule1).isNotEqualTo(atomicRule3);
        assertThat(atomicRule1).isNotEqualTo(atomicRule4);
        assertThat(atomicRule1).isNotEqualTo(atomicRule5);
        assertThat(atomicRule1).isNotEqualTo(atomicRule6);
    }

    @Test
    public void testHashCode() {
        AtomicRule atomicRule = new AtomicRule("prefix1", "from1", "to1", "postfix1", 1);
        int result = atomicRule.hashCode();

        int expected = 31 * atomicRule.getPrefix().hashCode() + atomicRule.getFrom().hashCode();
        expected = 31 * expected + atomicRule.getTo().hashCode();
        expected = 31 * expected + atomicRule.getPostfix().hashCode();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        AtomicRule atomicRule = new AtomicRule("prefix", "from", "to", "postfix", 1);
        atomicRule.merge(new AtomicRule("prefix", "from", "to", "postfix", 3));
        assertThat(atomicRule).hasToString("AtomicRule[prefix, from, to, postfix, 2, 4]");
    }

}
