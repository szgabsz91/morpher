package com.github.szgabsz91.morpher.transformationengines.astra.impl.searchers;

import com.github.szgabsz91.morpher.transformationengines.astra.config.AtomicRuleFitnessCalculatorType;
import com.github.szgabsz91.morpher.transformationengines.astra.config.SearcherType;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.factories.ComponentFactory;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.fitnesscalculators.atomicrule.IAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.AtomicRule;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.AtomicRuleCandidate;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.rules.RuleGroup;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ParallelSearcherTest {

    @Test
    public void testIsUnidirectional() {
        ISearcher unidirectionalSearcher = ComponentFactory.createSearcher(SearcherType.PARALLEL, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        assertThat(unidirectionalSearcher.isUnidirectional()).isTrue();
        ISearcher bidirectionalSearcher = ComponentFactory.createSearcher(SearcherType.PARALLEL, false, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        assertThat(bidirectionalSearcher.isUnidirectional()).isFalse();
    }

    @Test
    public void testSize() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PARALLEL, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        searcher.addAtomicRules(Set.of(
                new AtomicRule("a", "b", "c", "d", 1),
                new AtomicRule("e", "f", "g", "h", 1),
                new AtomicRule("i", "j", "k", "l", 1)
        ));
        assertThat(searcher.size()).isEqualTo(3);
    }

    @Test
    public void testGetRuleGroups() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PARALLEL, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        searcher.addAtomicRules(Set.of(
                new AtomicRule("a", "b", "c", "d", 1),
                new AtomicRule("e", "f", "g", "h", 1),
                new AtomicRule("i", "j", "k", "l", 1)
        ));
        assertThat(searcher.getRuleGroups()).hasSize(3);
    }

    @Test
    public void testSearchAtomicRuleCandidatesForForwardsTransformation() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PARALLEL, false, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        searcher.addAtomicRules(Set.of(atomicRule));
        AtomicRuleCandidate[] atomicRuleCandidates = searcher.searchAtomicRuleCandidatesForForwardsTransformation("abd");
        assertThat(atomicRuleCandidates).hasSize(1);
        AtomicRuleCandidate atomicRuleCandidate = atomicRuleCandidates[0];
        AtomicRule result = atomicRuleCandidate.getAtomicRule();
        assertThat(result).isSameAs(atomicRule);
    }

    @Test
    public void testSearchAtomicRuleCandidatesForBackwardsTransformation() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PARALLEL, false, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        searcher.addAtomicRules(Set.of(atomicRule));
        AtomicRuleCandidate[] atomicRuleCandidates = searcher.searchAtomicRuleCandidatesForBackwardsTransformation("acd");
        assertThat(atomicRuleCandidates).hasSize(1);
        AtomicRuleCandidate atomicRuleCandidate = atomicRuleCandidates[0];
        AtomicRule result = atomicRuleCandidate.getAtomicRule();
        assertThat(result).isSameAs(atomicRule);
    }

    @Test
    public void testSearchAtomicRuleCandidatesForForwardsTransformationWithUnidirectionalTransformationEngine() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PARALLEL, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        searcher.addAtomicRules(Set.of(atomicRule));
        AtomicRuleCandidate[] atomicRuleCandidates = searcher.searchAtomicRuleCandidatesForForwardsTransformation("abd");
        assertThat(atomicRuleCandidates).hasSize(1);
        AtomicRuleCandidate atomicRuleCandidate = atomicRuleCandidates[0];
        AtomicRule result = atomicRuleCandidate.getAtomicRule();
        assertThat(result).isSameAs(atomicRule);
    }

    @Test
    public void testSearchAtomicRuleCandidatesForBackwardsTransformationWithUnidirectionalTransformationEngine() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PARALLEL, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        searcher.addAtomicRules(Set.of(atomicRule));
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> searcher.searchAtomicRuleCandidatesForBackwardsTransformation("acd"));
        assertThat(exception).hasMessage("Unidirectional ParallelSearcher cannot be used for backwards transformation");
    }

    @Test
    public void testRemoveAtomicRule() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PARALLEL, false, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        AtomicRule atomicRule1 = new AtomicRule("a", "b", "c", "d", 1);
        AtomicRule atomicRule2 = new AtomicRule("ab", "", "c", "d", 1);
        AtomicRule atomicRule3 = new AtomicRule("a", "b", "", "cd", 1);
        AtomicRule atomicRule4 = new AtomicRule("e", "f", "g", "h", 1);
        searcher.addAtomicRules(Set.of(atomicRule1, atomicRule2, atomicRule3, atomicRule4));
        searcher.removeAtomicRule(atomicRule1);
        searcher.removeAtomicRule(atomicRule2);
        searcher.removeAtomicRule(atomicRule3);
        RuleGroup expectedRuleGroup = RuleGroup.straight("efh");
        expectedRuleGroup.addAtomicRule(atomicRule4);
        assertThat(searcher.getRuleGroups()).containsExactly(expectedRuleGroup);
        AtomicRuleCandidate[] forwardsTransformationCandidates = searcher.searchAtomicRuleCandidatesForForwardsTransformation("efh");
        assertThat(forwardsTransformationCandidates).containsExactly(new AtomicRuleCandidate(null, atomicRule4, 1.0));
        AtomicRuleCandidate[] backwardsTransformationCandidates = searcher.searchAtomicRuleCandidatesForForwardsTransformation("efh");
        assertThat(backwardsTransformationCandidates).containsExactly(new AtomicRuleCandidate(null, atomicRule4, 1.0));
    }

    @Test
    public void testCacheInvalidation() {
        IAtomicRuleFitnessCalculator atomicRuleFitnessCalculator = mock(IAtomicRuleFitnessCalculator.class);
        ISearcher searcher = new ParallelSearcher(atomicRuleFitnessCalculator, true);
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        searcher.addAtomicRules(Set.of(atomicRule));
        verify(atomicRuleFitnessCalculator).invalidateCache();
    }
    
}
