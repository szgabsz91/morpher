package com.github.szgabsz91.morpher.methods.astra.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.methods.astra.config.AtomicRuleFitnessCalculatorType;
import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import com.github.szgabsz91.morpher.methods.astra.impl.IASTRA;
import com.github.szgabsz91.morpher.methods.astra.impl.factories.ComponentFactory;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.atomicrule.AbstractSmoothAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.fitnesscalculators.atomicrule.LocalSmoothAtomicRuleFitnessCalculator;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.ISearcher;
import com.github.szgabsz91.morpher.methods.astra.impl.searchers.SequentialSearcher;
import com.github.szgabsz91.morpher.methods.astra.impl.testutils.ASTRABuilder;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.ASTRAMessage;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.AtomicRuleFitnessCalculatorTypeMessage;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.SearcherTypeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ASTRAConverterTest {

    private ASTRAConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new ASTRAConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParse() throws IOException {
        AffixType affixType = AffixType.of("AFF");
        double exponentialSearcher = 2.0;
        ISearcher searcher = ComponentFactory.createSearcher(SearcherType.SEQUENTIAL, true, AtomicRuleFitnessCalculatorType.SMOOTH_LOCAL, exponentialSearcher);
        IASTRA astra = ComponentFactory.createASTRA(affixType, searcher, ASTRABuilder.DEFAULT_FITNESS_THRESHOLD, ASTRABuilder.DEFAULT_MAXIMUM_NUMBER_OF_RESPONSES, 10, 11, 12, 3, 1, 2.0);

        ASTRAMessage astraMessage = this.converter.convert(astra);
        assertThat(astraMessage.getAffixType()).isEqualTo(affixType.toString());
        assertThat(astraMessage.getSearcher().getType()).isEqualTo(SearcherTypeMessage.SEQUENTIAL);
        assertThat(astraMessage.getSearcher().getAtomicRuleFitnessCalculatorType()).isEqualTo(AtomicRuleFitnessCalculatorTypeMessage.SMOOTH_LOCAL);
        assertThat(astraMessage.getSearcher().getExponentialFactor()).isEqualTo(exponentialSearcher);
        assertThat(astraMessage.getMaximumResponseProbabilityDifferenceThreshold().getValue()).isEqualTo(2.0);
        assertThat(astraMessage.getMinimumSupportThreshold().getValue()).isEqualTo(astra.getMinimumSupportThreshold());
        assertThat(astraMessage.getMinimumWordFrequencyThreshold().getValue()).isEqualTo(astra.getMinimumWordFrequencyThreshold());
        assertThat(astraMessage.getMinimumAggregatedSupportThreshold().getValue()).isEqualTo(astra.getMinimumAggregatedSupportThreshold());
        assertThat(astraMessage.getMinimumContextLength().getValue()).isEqualTo(astra.getMinimumContextLength());
        assertThat(astraMessage.getMaximumNumberOfGeneratedAtomicRules().getValue()).isEqualTo(astra.getMaximumNumberOfGeneratedAtomicRules());
        assertThat(astraMessage.getMaximumResponseProbabilityDifferenceThreshold().getValue()).isEqualTo(astra.getMaximumResponseProbabilityDifferenceThreshold());

        IASTRA result = this.converter.convertBack(astraMessage);
        assertThat(result.getAffixType()).isEqualTo(affixType);
        assertThat(result.getSearcher()).isInstanceOf(SequentialSearcher.class);
        assertThat(result.getSearcher().getAtomicRuleFitnessCalculator()).isInstanceOf(LocalSmoothAtomicRuleFitnessCalculator.class);
        assertThat(result.getSearcher().getAtomicRuleFitnessCalculator()).isInstanceOf(AbstractSmoothAtomicRuleFitnessCalculator.class);
        AbstractSmoothAtomicRuleFitnessCalculator smoothAtomicRuleFitnessCalculator = (AbstractSmoothAtomicRuleFitnessCalculator) result.getSearcher().getAtomicRuleFitnessCalculator();
        assertThat(smoothAtomicRuleFitnessCalculator.getExponentialFactor()).isEqualTo(exponentialSearcher);
        assertThat(result.getMinimumSupportThreshold()).isEqualTo(astraMessage.getMinimumSupportThreshold().getValue());
        assertThat(result.getMinimumWordFrequencyThreshold()).isEqualTo(astraMessage.getMinimumWordFrequencyThreshold().getValue());
        assertThat(result.getMinimumAggregatedSupportThreshold()).isEqualTo(astraMessage.getMinimumAggregatedSupportThreshold().getValue());
        assertThat(result.getMinimumContextLength()).isEqualTo(astraMessage.getMinimumContextLength().getValue());
        assertThat(result.getMaximumNumberOfGeneratedAtomicRules()).isEqualTo(astraMessage.getMaximumNumberOfGeneratedAtomicRules().getValue());
        assertThat(result.getMaximumResponseProbabilityDifferenceThreshold()).isEqualTo(astraMessage.getMaximumResponseProbabilityDifferenceThreshold().getValue());

        Path file = Files.createTempFile("morpher", "astra");
        try {
            Serializer<IASTRA, ASTRAMessage> serializer = new Serializer<>(this.converter, astra);
            serializer.serialize(astra, file);
            ASTRAMessage resultingASTRAMessage = this.converter.parse(file);
            assertThat(resultingASTRAMessage).isEqualTo(astraMessage);
        }
        finally {
            Files.delete(file);
        }
    }

}
