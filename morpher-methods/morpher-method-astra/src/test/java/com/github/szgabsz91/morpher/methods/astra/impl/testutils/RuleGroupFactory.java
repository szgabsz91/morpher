package com.github.szgabsz91.morpher.methods.astra.impl.testutils;

import com.github.szgabsz91.morpher.methods.astra.impl.rules.AtomicRule;
import com.github.szgabsz91.morpher.methods.astra.impl.rules.RuleGroup;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

public final class RuleGroupFactory {

    private RuleGroupFactory() {

    }

    public static RuleGroup createRuleGroup(String context, Function<String, RuleGroup> ruleGroupFactory, AtomicRule... atomicRules) {
        RuleGroup ruleGroup = ruleGroupFactory.apply(context);
        Arrays.stream(atomicRules).forEach(ruleGroup::addAtomicRule);
        return ruleGroup;
    }

    public static RuleGroup createRuleGroup(AtomicRule... atomicRules) {
        if (atomicRules.length == 0) {
            throw new UnsupportedOperationException("The RuleGroup should contain at least 1 AtomicRule");
        }

        AtomicRule firstAtomicRule = atomicRules[0];
        RuleGroup ruleGroup = RuleGroup.straight(firstAtomicRule.getContext());
        Stream.of(atomicRules)
                .forEach(ruleGroup::addAtomicRule);

        return ruleGroup;
    }

}
