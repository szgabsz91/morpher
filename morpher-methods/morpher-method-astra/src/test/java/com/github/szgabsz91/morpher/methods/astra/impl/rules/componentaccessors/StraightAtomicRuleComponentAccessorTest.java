package com.github.szgabsz91.morpher.methods.astra.impl.rules.componentaccessors;

import com.github.szgabsz91.morpher.methods.astra.impl.rules.AtomicRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StraightAtomicRuleComponentAccessorTest {

    private IAtomicRuleComponentAccessor atomicRuleComponentAccessor;
    private AtomicRule atomicRule;

    @BeforeEach
    public void setUp() {
        this.atomicRuleComponentAccessor = StraightAtomicRuleComponentAccessor.get();
        this.atomicRule = new AtomicRule("a", "b", "c", "d", 1);
    }

    @Test
    public void testGetContext() {
        String context = this.atomicRuleComponentAccessor.getContext(atomicRule);
        assertThat(context).isEqualTo("abd");
    }

    @Test
    public void testGetPrefix() {
        String prefix = this.atomicRuleComponentAccessor.getPrefix(atomicRule);
        assertThat(prefix).isEqualTo("a");
    }

    @Test
    public void testGetFrom() {
        String from = this.atomicRuleComponentAccessor.getFrom(atomicRule);
        assertThat(from).isEqualTo("b");
    }

    @Test
    public void testGetTo() {
        String to = this.atomicRuleComponentAccessor.getTo(atomicRule);
        assertThat(to).isEqualTo("c");
    }

    @Test
    public void testGetPostfix() {
        String postfix = this.atomicRuleComponentAccessor.getPostfix(atomicRule);
        assertThat(postfix).isEqualTo("d");
    }

    @Test
    public void testToString() {
        assertThat(this.atomicRuleComponentAccessor).hasToString("INFLECTION");
    }

}
