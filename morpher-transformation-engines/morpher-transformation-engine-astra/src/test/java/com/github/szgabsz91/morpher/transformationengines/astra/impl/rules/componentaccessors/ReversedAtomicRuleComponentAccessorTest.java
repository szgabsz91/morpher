package com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.componentaccessors;

import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.AtomicRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReversedAtomicRuleComponentAccessorTest {

    private IAtomicRuleComponentAccessor atomicRuleComponentAccessor;
    private AtomicRule atomicRule;

    @BeforeEach
    public void setUp() {
        this.atomicRuleComponentAccessor = ReversedAtomicRuleComponentAccessor.get();
        this.atomicRule = new AtomicRule("a", "b", "c", "d", 1);
    }

    @Test
    public void testGetContext() {
        String context = this.atomicRuleComponentAccessor.getContext(atomicRule);
        assertThat(context).isEqualTo("acd");
    }

    @Test
    public void testGetPrefix() {
        String prefix = this.atomicRuleComponentAccessor.getPrefix(atomicRule);
        assertThat(prefix).isEqualTo("a");
    }

    @Test
    public void testGetChangingSubstring() {
        String changingSubstring = this.atomicRuleComponentAccessor.getChangingSubstring(atomicRule);
        assertThat(changingSubstring).isEqualTo("c");
    }

    @Test
    public void testGetReplacementString() {
        String replacementString = this.atomicRuleComponentAccessor.getReplacementString(atomicRule);
        assertThat(replacementString).isEqualTo("b");
    }

    @Test
    public void testGetPostfix() {
        String postfix = this.atomicRuleComponentAccessor.getPostfix(atomicRule);
        assertThat(postfix).isEqualTo("d");
    }

    @Test
    public void testToString() {
        assertThat(this.atomicRuleComponentAccessor).hasToString("REVERSED");
    }

}
