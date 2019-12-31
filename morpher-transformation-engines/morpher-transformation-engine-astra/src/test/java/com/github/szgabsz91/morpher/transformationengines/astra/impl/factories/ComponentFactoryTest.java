package com.github.szgabsz91.morpher.transformationengines.astra.impl.factories;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.transformationengines.astra.config.AtomicRuleFitnessCalculatorType;
import com.github.szgabsz91.morpher.transformationengines.astra.config.SearcherType;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.IASTRA;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.fitnesscalculators.atomicrule.DefaultAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.fitnesscalculators.atomicrule.GlobalSmoothAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.fitnesscalculators.atomicrule.LocalSmoothAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.searchers.ISearcher;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.searchers.ParallelSearcher;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.searchers.PrefixTreeSearcher;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.searchers.SequentialSearcher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentFactoryTest {

    @Test
    public void testConstructor() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<ComponentFactory> constructor = ComponentFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        try {
            constructor.newInstance();
        }
        finally {
            constructor.setAccessible(false);
        }
    }

    @Test
    public void testCreateASTRA() {
        AffixType affixType = AffixType.of("AFF");
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.SEQUENTIAL, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        double fitnessThreshold = 0.3;
        int maximumNumberOfResponses = 2;
        int minimumSupportThreshold = 3;
        int minimumWordFrequencyThreshold = 4;
        int minimumAggregatedSupportThreshold = 5;
        int minimumContextLength = 6;
        int maximumNumberOfGeneratedAtomicRules = 7;
        double maximumResponseProbabilityDifferenceThreshold = 8.9;
        IASTRA astra = ComponentFactory.createASTRA(affixType, searcher, fitnessThreshold, maximumNumberOfResponses, minimumSupportThreshold, minimumWordFrequencyThreshold, minimumAggregatedSupportThreshold, minimumContextLength, maximumNumberOfGeneratedAtomicRules, maximumResponseProbabilityDifferenceThreshold);
        assertThat(astra.getAffixType()).isSameAs(affixType);
        assertThat(astra.getSearcher()).isSameAs(searcher);
        assertThat(astra.getFitnessThreshold()).isEqualTo(fitnessThreshold);
        assertThat(astra.getMaximumNumberOfResponses()).isEqualTo(maximumNumberOfResponses);
        assertThat(astra.isUnidirectional()).isEqualTo(searcher.isUnidirectional());
        assertThat(astra.getMinimumSupportThreshold()).isEqualTo(minimumSupportThreshold);
        assertThat(astra.getMinimumWordFrequencyThreshold()).isEqualTo(minimumWordFrequencyThreshold);
        assertThat(astra.getMinimumAggregatedSupportThreshold()).isEqualTo(minimumAggregatedSupportThreshold);
        assertThat(astra.getMinimumContextLength()).isEqualTo(minimumContextLength);
        assertThat(astra.getMaximumNumberOfGeneratedAtomicRules()).isEqualTo(maximumNumberOfGeneratedAtomicRules);
        assertThat(astra.getMaximumResponseProbabilityDifferenceThreshold()).isEqualTo(maximumResponseProbabilityDifferenceThreshold);
    }

    @Test
    public void testCreateSearcherWithSequentialSearcher() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.SEQUENTIAL, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        assertThat(searcher).isInstanceOf(SequentialSearcher.class);
        assertThat(searcher.getAtomicRuleFitnessCalculator()).isInstanceOf(DefaultAtomicRuleFitnessCalculator.class);
        assertThat(searcher.isUnidirectional()).isEqualTo(searcher.isUnidirectional());
    }

    @Test
    public void testCreateSearcherWithParallelSearcher() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PARALLEL, false, AtomicRuleFitnessCalculatorType.SMOOTH_GLOBAL, 2.0);
        assertThat(searcher).isInstanceOf(ParallelSearcher.class);
        assertThat(searcher.getAtomicRuleFitnessCalculator()).isInstanceOf(GlobalSmoothAtomicRuleFitnessCalculator.class);
        assertThat(searcher.isUnidirectional()).isEqualTo(searcher.isUnidirectional());
    }

    @Test
    public void testCreateSearcherWithPrefixTreeSearcher() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PREFIX_TREE, true, AtomicRuleFitnessCalculatorType.SMOOTH_LOCAL, 2.0);
        assertThat(searcher).isInstanceOf(PrefixTreeSearcher.class);
        assertThat(searcher.getAtomicRuleFitnessCalculator()).isInstanceOf(LocalSmoothAtomicRuleFitnessCalculator.class);
        assertThat(searcher.isUnidirectional()).isEqualTo(searcher.isUnidirectional());
    }

    @Test
    public void testCreateSearcherWithPrefixTreeSearcherAndDefaultFitnessFunction() {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PREFIX_TREE, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        assertThat(searcher).isInstanceOf(PrefixTreeSearcher.class);
        assertThat(searcher.getAtomicRuleFitnessCalculator()).isInstanceOf(DefaultAtomicRuleFitnessCalculator.class);
        assertThat(searcher.isUnidirectional()).isEqualTo(searcher.isUnidirectional());
    }

}
