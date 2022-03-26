package com.github.szgabsz91.morpher.transformationengines.astra.impl.rules;

import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.componentaccessors.ReversedAtomicRuleComponentAccessor;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.componentaccessors.StraightAtomicRuleComponentAccessor;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

import static com.github.szgabsz91.morpher.transformationengines.astra.impl.testutils.RuleGroupFactory.createRuleGroup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RuleGroupTest {

    @Test
    public void testStraightRuleGroupGetters() {
        String context = "context";
        RuleGroup ruleGroup = RuleGroup.straight(context);
        assertThat(ruleGroup.getAtomicRules()).isEmpty();
        assertThat(ruleGroup.getAtomicRuleComponentAccessor()).isInstanceOf(StraightAtomicRuleComponentAccessor.class);
        assertThat(ruleGroup.getContext()).isEqualTo(context);
        assertThat(ruleGroup.getSupport()).isZero();
    }

    @Test
    public void testReversedRuleGroupGetters() {
        String context = "context";
        RuleGroup ruleGroup = RuleGroup.reversed(context);
        assertThat(ruleGroup.getAtomicRules()).isEmpty();
        assertThat(ruleGroup.getAtomicRuleComponentAccessor()).isInstanceOf(ReversedAtomicRuleComponentAccessor.class);
        assertThat(ruleGroup.getContext()).isEqualTo(context);
        assertThat(ruleGroup.getSupport()).isZero();
    }

    @Test
    public void testAddAtomicRuleWithInvalidContextAndStraightRuleGroup() {
        String context = "abc";
        RuleGroup ruleGroup = RuleGroup.straight(context);
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ruleGroup.addAtomicRule(atomicRule));
        assertThat(exception).hasMessage("The given atomic rule " + atomicRule + " has a different context than " + context);
    }

    @Test
    public void testAddAtomicRuleWithInvalidContextAndReversedRuleGroup() {
        String context = "abc";
        RuleGroup ruleGroup = RuleGroup.reversed(context);
        AtomicRule atomicRule = new AtomicRule("a", "c", "b", "d", 1);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ruleGroup.addAtomicRule(atomicRule));
        assertThat(exception).hasMessage("The given atomic rule " + atomicRule + " has a different context than " + context);
    }

    @Test
    public void testAddAtomicRuleWithExistingAtomicRuleAndStraightRuleGroup() {
        String context = "abc";
        RuleGroup ruleGroup = RuleGroup.straight(context);
        AtomicRule atomicRule = new AtomicRule("a", "b", "d", "c", 1);
        ruleGroup.addAtomicRule(atomicRule);
        ruleGroup.addAtomicRule(atomicRule);
        assertThat(ruleGroup.getAtomicRules()).hasSize(1);
        AtomicRule result = ruleGroup.getAtomicRules().iterator().next();
        assertThat(result.getSupport()).isEqualTo(2);
    }

    @Test
    public void testAddAtomicRuleWithExistingAtomicRuleAndReversedRuleGroup() {
        String context = "abc";
        RuleGroup ruleGroup = RuleGroup.reversed(context);
        AtomicRule atomicRule = new AtomicRule("a", "d", "b", "c", 1);
        ruleGroup.addAtomicRule(atomicRule);
        ruleGroup.addAtomicRule(atomicRule);
        assertThat(ruleGroup.getAtomicRules()).hasSize(1);
        AtomicRule result = ruleGroup.getAtomicRules().iterator().next();
        assertThat(result.getSupport()).isEqualTo(2);
    }

    @Test
    public void testAddAtomicRuleWithNonExistingAtomicRuleAndStraightRuleGroup() {
        String context = "abc";
        AtomicRule atomicRule1 = new AtomicRule("a", "b", "d", "c", 1);
        AtomicRule atomicRule2 = new AtomicRule("ab", "", "d", "c", 1);
        RuleGroup ruleGroup = RuleGroup.straight(context);
        ruleGroup.addAtomicRule(atomicRule1);
        ruleGroup.addAtomicRule(atomicRule2);
        assertThat(ruleGroup.getAtomicRules()).hasSize(2);
        assertThat(ruleGroup.getAtomicRules()).matches(atomicRules -> {
            return StreamSupport.stream(atomicRules.spliterator(), false)
                    .allMatch(atomicRule -> atomicRule.getSupport() == 1);
        });
        assertThat(ruleGroup.getAtomicRules()).contains(atomicRule1, atomicRule2);
    }

    @Test
    public void testAddAtomicRuleWithNonExistingAtomicRuleAndReversedRuleGroup() {
        String context = "abc";
        AtomicRule atomicRule1 = new AtomicRule("a", "d", "b", "c", 1);
        AtomicRule atomicRule2 = new AtomicRule("ab", "d", "", "c", 1);
        RuleGroup ruleGroup = RuleGroup.reversed(context);
        ruleGroup.addAtomicRule(atomicRule1);
        ruleGroup.addAtomicRule(atomicRule2);
        assertThat(ruleGroup.getAtomicRules()).hasSize(2);
        assertThat(ruleGroup.getAtomicRules()).matches(atomicRules -> {
            return StreamSupport.stream(atomicRules.spliterator(), false)
                    .allMatch(atomicRule -> atomicRule.getSupport() == 1);
        });
        assertThat(ruleGroup.getAtomicRules()).contains(atomicRule1, atomicRule2);
    }

    @Test
    public void testToStringWithStraightRuleGroup() {
        RuleGroup ruleGroup = createRuleGroup("abd", RuleGroup::straight, new AtomicRule("a", "b", "c", "d", 1));
        assertThat(ruleGroup)
                .hasToString("RuleGroup[" + ruleGroup.getContext() + "," + " " + ruleGroup.getAtomicRules() + "]");
    }

    @Test
    public void testToStringWithReversedRuleGroup() {
        RuleGroup ruleGroup = createRuleGroup("abd", RuleGroup::reversed, new AtomicRule("a", "c", "b", "d", 1));
        assertThat(ruleGroup)
                .hasToString("ReversedRuleGroup[" + ruleGroup.getContext() + "," + " " + ruleGroup.getAtomicRules() + "]");
    }

}
