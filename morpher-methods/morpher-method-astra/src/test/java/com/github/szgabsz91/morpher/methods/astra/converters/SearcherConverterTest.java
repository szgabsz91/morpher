package com.github.szgabsz91.morpher.methods.astra.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.methods.astra.config.AtomicRuleFitnessCalculatorType;
import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import com.github.szgabsz91.morpher.methods.astra.impl.factories.ComponentFactory;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.atomicrule.DefaultAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.atomicrule.AbstractSmoothAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.atomicrule.GlobalSmoothAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.rules.AtomicRule;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.ISearcher;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.ParallelSearcher;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.PrefixTreeSearcher;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.SequentialSearcher;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.AtomicRuleFitnessCalculatorTypeMessage;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.SearcherMessage;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.SearcherTypeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SearcherConverterTest {

    private SearcherConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new SearcherConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParseWithSequentialSearcher() throws IOException {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.SEQUENTIAL, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        searcher.addAtomicRules(Set.of(new AtomicRule("prefix", "from", "to", "postfix", 1)));

        SearcherMessage searcherMessage = this.converter.convert(searcher);
        assertThat(searcherMessage.getType()).isEqualTo(SearcherTypeMessage.SEQUENTIAL);
        assertThat(searcherMessage.getUnidirectional()).isTrue();
        assertThat(searcherMessage.getAtomicRuleFitnessCalculatorType()).isEqualTo(AtomicRuleFitnessCalculatorTypeMessage.DEFAULT);
        assertThat(searcherMessage.getExponentialFactor()).isZero();
        assertThat(searcherMessage.getAtomicRulesCount()).isOne();

        ISearcher result = this.converter.convertBack(searcherMessage);
        assertThat(result).isInstanceOf(SequentialSearcher.class);
        assertThat(result.isUnidirectional()).isTrue();
        assertThat(result.getAtomicRuleFitnessCalculator()).isInstanceOf(DefaultAtomicRuleFitnessCalculator.class);
        assertThat(result.getRuleGroups()).hasSize(1);
        assertThat(result.getRuleGroups().iterator().next().getAtomicRules()).hasSize(1);

        Path file = Files.createTempFile("morpher", "astra");
        try {
            Serializer<ISearcher, SearcherMessage> serializer = new Serializer<>(this.converter, searcher);
            serializer.serialize(searcher, file);
            SearcherMessage resultingSearcherMessage = this.converter.parse(file);
            assertThat(resultingSearcherMessage).isEqualTo(searcherMessage);
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testConvertAndConvertBackAndParseWithParallelSearcher() throws IOException {
        double exponentialFactor = 2.0;
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PARALLEL, true, AtomicRuleFitnessCalculatorType.SMOOTH_GLOBAL, exponentialFactor);
        searcher.addAtomicRules(Set.of(new AtomicRule("prefix", "from", "to", "postfix", 1)));

        SearcherMessage searcherMessage = this.converter.convert(searcher);
        assertThat(searcherMessage.getType()).isEqualTo(SearcherTypeMessage.PARALLEL);
        assertThat(searcherMessage.getUnidirectional()).isTrue();
        assertThat(searcherMessage.getAtomicRuleFitnessCalculatorType()).isEqualTo(AtomicRuleFitnessCalculatorTypeMessage.SMOOTH_GLOBAL);
        assertThat(searcherMessage.getExponentialFactor()).isEqualTo(exponentialFactor);
        assertThat(searcherMessage.getAtomicRulesCount()).isOne();

        ISearcher result = this.converter.convertBack(searcherMessage);
        assertThat(result).isInstanceOf(ParallelSearcher.class);
        assertThat(result.isUnidirectional()).isTrue();
        assertThat(result.getAtomicRuleFitnessCalculator()).isInstanceOf(GlobalSmoothAtomicRuleFitnessCalculator.class);
        AbstractSmoothAtomicRuleFitnessCalculator smoothAtomicRuleFitnessCalculator = (AbstractSmoothAtomicRuleFitnessCalculator) result.getAtomicRuleFitnessCalculator();
        assertThat(smoothAtomicRuleFitnessCalculator.getExponentialFactor()).isEqualTo(exponentialFactor);
        assertThat(result.getRuleGroups()).hasSize(1);
        assertThat(result.getRuleGroups().iterator().next().getAtomicRules()).hasSize(1);

        Path file = Files.createTempFile("morpher", "astra");
        try {
            Serializer<ISearcher, SearcherMessage> serializer = new Serializer<>(this.converter, searcher);
            serializer.serialize(searcher, file);
            SearcherMessage resultingSearcherMessage = this.converter.parse(file);
            assertThat(resultingSearcherMessage).isEqualTo(searcherMessage);
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testConvertAndConvertBackAndParseWithPrefixTreeSearcher() throws IOException {
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.PREFIX_TREE, true, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0);
        searcher.addAtomicRules(Set.of(new AtomicRule("prefix", "from", "to", "postfix", 1)));

        SearcherMessage searcherMessage = this.converter.convert(searcher);
        assertThat(searcherMessage.getType()).isEqualTo(SearcherTypeMessage.PREFIX_TREE);
        assertThat(searcherMessage.getUnidirectional()).isTrue();
        assertThat(searcherMessage.getAtomicRuleFitnessCalculatorType()).isEqualTo(AtomicRuleFitnessCalculatorTypeMessage.DEFAULT);
        assertThat(searcherMessage.getExponentialFactor()).isZero();
        assertThat(searcherMessage.getAtomicRulesCount()).isOne();

        ISearcher result = this.converter.convertBack(searcherMessage);
        assertThat(result).isInstanceOf(PrefixTreeSearcher.class);
        assertThat(result.isUnidirectional()).isTrue();
        assertThat(result.getAtomicRuleFitnessCalculator()).isInstanceOf(DefaultAtomicRuleFitnessCalculator.class);
        assertThat(result.getRuleGroups()).hasSize(1);
        assertThat(result.getRuleGroups().iterator().next().getAtomicRules()).hasSize(1);

        Path file = Files.createTempFile("morpher", "astra");
        try {
            Serializer<ISearcher, SearcherMessage> serializer = new Serializer<>(this.converter, searcher);
            serializer.serialize(searcher, file);
            SearcherMessage resultingSearcherMessage = this.converter.parse(file);
            assertThat(resultingSearcherMessage).isEqualTo(searcherMessage);
        }
        finally {
            Files.delete(file);
        }
    }

}
