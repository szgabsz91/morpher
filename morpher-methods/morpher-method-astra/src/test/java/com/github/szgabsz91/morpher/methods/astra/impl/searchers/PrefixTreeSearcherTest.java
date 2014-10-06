package com.github.szgabsz91.morpher.methods.astra.impl.searchers;

import com.github.szgabsz91.morpher.methods.astra.config.AtomicRuleFitnessCalculatorType;
import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import com.github.szgabsz91.morpher.methods.astra.impl.factories.ComponentFactory;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.atomicrule.IAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.rules.AtomicRule;
import com.github.szgabsz91.morpher.methods.astra.impl.rules.AtomicRuleCandidate;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PrefixTreeSearcherTest {

    @Test
    public void testIsUnidirectional() {
        ISearcher unidirectionalSearcher = ComponentFactory.createSearcher(SearcherType.PREFIX_TREE, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        assertThat(unidirectionalSearcher.isUnidirectional()).isTrue();
        ISearcher bidirectionalSearcher = ComponentFactory.createSearcher(SearcherType.PREFIX_TREE, false, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        assertThat(bidirectionalSearcher.isUnidirectional()).isFalse();
    }

    @Test
    public void testSize() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PREFIX_TREE, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        searcher.addAtomicRules(Set.of(
                new AtomicRule("a", "b", "c", "d", 1),
                new AtomicRule("e", "f", "g", "h", 1),
                new AtomicRule("i", "j", "k", "l", 1)
        ));
        assertThat(searcher.size()).isEqualTo(3);
    }

    @Test
    public void testGetRuleGroups() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PREFIX_TREE, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        searcher.addAtomicRules(Set.of(
                new AtomicRule("a", "b", "c", "d", 1),
                new AtomicRule("e", "f", "g", "h", 1),
                new AtomicRule("i", "j", "k", "l", 1)
        ));
        assertThat(searcher.getRuleGroups()).hasSize(3);
    }

    @Test
    public void testSearchAtomicRuleCandidatesForInflection() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PREFIX_TREE, false, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        searcher.addAtomicRules(Set.of(atomicRule));
        AtomicRuleCandidate[] atomicRuleCandidates = searcher.searchAtomicRuleCandidatesForInflection("abd");
        assertThat(atomicRuleCandidates).hasSize(1);
        AtomicRuleCandidate atomicRuleCandidate = atomicRuleCandidates[0];
        AtomicRule result = atomicRuleCandidate.getAtomicRule();
        assertThat(result).isSameAs(atomicRule);
    }

    @Test
    public void testSearchAtomicRuleCandidatesForLemmatization() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PREFIX_TREE, false, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        searcher.addAtomicRules(Set.of(atomicRule));
        AtomicRuleCandidate[] atomicRuleCandidates = searcher.searchAtomicRuleCandidatesForLemmatization("acd");
        assertThat(atomicRuleCandidates).hasSize(1);
        AtomicRuleCandidate atomicRuleCandidate = atomicRuleCandidates[0];
        AtomicRule result = atomicRuleCandidate.getAtomicRule();
        assertThat(result).isSameAs(atomicRule);
    }

    @Test
    public void testSearchAtomicRuleCandidatesForInflectionWithUnidirectionalMethod() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PREFIX_TREE, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        searcher.addAtomicRules(Set.of(atomicRule));
        AtomicRuleCandidate[] atomicRuleCandidates = searcher.searchAtomicRuleCandidatesForInflection("abd");
        assertThat(atomicRuleCandidates).hasSize(1);
        AtomicRuleCandidate atomicRuleCandidate = atomicRuleCandidates[0];
        AtomicRule result = atomicRuleCandidate.getAtomicRule();
        assertThat(result).isSameAs(atomicRule);
    }

    @Test
    public void testSearchAtomicRuleCandidatesForLemmatizationWithUnidirectionalMethod() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PREFIX_TREE, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        searcher.addAtomicRules(Set.of(atomicRule));
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> searcher.searchAtomicRuleCandidatesForLemmatization("acd"));
        assertThat(exception).hasMessage("Unidirectional PrefixTreeSearcher cannot be used for lemmatization");
    }

    @Test
    public void testRemoveAtomicRule() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PREFIX_TREE, false, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> searcher.removeAtomicRule(null));
        assertThat(exception).hasMessage("Currently this operation is not supported");
    }

    @Test
    public void testCacheInvalidation() {
        IAtomicRuleFitnessCalculator atomicRuleFitnessCalculator = mock(IAtomicRuleFitnessCalculator.class);
        ISearcher searcher = new PrefixTreeSearcher(atomicRuleFitnessCalculator, true);
        AtomicRule atomicRule = new AtomicRule("a", "b", "c", "d", 1);
        searcher.addAtomicRules(Set.of(atomicRule));
        verify(atomicRuleFitnessCalculator).invalidateCache();
    }

}
