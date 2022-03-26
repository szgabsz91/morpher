package com.github.szgabsz91.morpher.transformationengines.astra.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ASTRATransformationEngineConfigurationTest {

    @Test
    public void testBuildWithMaximumResponseProbabilityDifferenceThreshold() {
        double maximumResponseProbabilityDifferenceThreshold = 2.0;
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .maximumResponseProbabilityDifferenceThreshold(maximumResponseProbabilityDifferenceThreshold)
                .build();
        assertThat(configuration.getMaximumResponseProbabilityDifferenceThreshold()).isEqualTo(maximumResponseProbabilityDifferenceThreshold);
    }

    @Test
    public void testExponentialFactorWithNegativeValue() {
        ASTRATransformationEngineConfiguration.Builder builder = new ASTRATransformationEngineConfiguration.Builder();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.exponentialFactor(-0.1));
        assertThat(exception).hasMessage("Exponential factor cannot be negative");
    }

    @Test
    public void testBuilderWithNonDefaultMaximumNumberOfResponses() {
        SearcherType searcherType = SearcherType.PARALLEL;
        int maximumNumberOfResponses = 2;
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(searcherType)
                .maximumNumberOfResponses(maximumNumberOfResponses)
                .build();
        assertThat(configuration.getSearcherType()).isEqualTo(SearcherType.PARALLEL);
        assertThat(configuration.getMaximumNumberOfResponses()).isEqualTo(maximumNumberOfResponses);
        assertThat(configuration.getMinimumSupportThreshold()).isNull();
        assertThat(configuration.getMinimumWordFrequencyThreshold()).isNull();
        assertThat(configuration.getMinimumAggregatedSupportThreshold()).isNull();
        assertThat(configuration.getMinimumContextLength()).isNull();
        assertThat(configuration.getMaximumNumberOfGeneratedAtomicRules()).isNull();
        assertThat(configuration.getExponentialFactor()).isZero();
    }

    @Test
    public void testBuilderWithDefaultMaximumNumberOfResponses() {
        SearcherType searcherType = SearcherType.PARALLEL;
        int minimumSupportThreshold = 3;
        int minimumWordFrequencyThreshold = 4;
        int minimumAggregatedSupportThreshold = 5;
        int minimumContextLength = 6;
        int maximumNumberOfGeneratedAtomicRules = 7;
        double exponentialFactor = 8.9;
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(searcherType)
                .minimumSupportThreshold(minimumSupportThreshold)
                .minimumWordFrequencyThreshold(minimumWordFrequencyThreshold)
                .minimumAggregatedSupportThreshold(minimumAggregatedSupportThreshold)
                .minimumContextLength(minimumContextLength)
                .maximumNumberOfGeneratedAtomicRules(maximumNumberOfGeneratedAtomicRules)
                .exponentialFactor(exponentialFactor)
                .build();
        assertThat(configuration.getSearcherType()).isEqualTo(SearcherType.PARALLEL);
        assertThat(configuration.getMaximumNumberOfResponses()).isOne();
        assertThat(configuration.getMinimumSupportThreshold()).isEqualTo(minimumSupportThreshold);
        assertThat(configuration.getMinimumWordFrequencyThreshold()).isEqualTo(minimumWordFrequencyThreshold);
        assertThat(configuration.getMinimumAggregatedSupportThreshold()).isEqualTo(minimumAggregatedSupportThreshold);
        assertThat(configuration.getMinimumContextLength()).isEqualTo(minimumContextLength);
        assertThat(configuration.getMaximumNumberOfGeneratedAtomicRules()).isEqualTo(maximumNumberOfGeneratedAtomicRules);
        assertThat(configuration.getExponentialFactor()).isEqualTo(exponentialFactor);
    }

    @Test
    public void testBuilderWithZeroMaximumNumberOfResponses() {
        ASTRATransformationEngineConfiguration.Builder builder = new ASTRATransformationEngineConfiguration.Builder();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.maximumNumberOfResponses(0));
        assertThat(exception).hasMessage("Property maximumNumberOfResponses must be a positive integer, but it was 0");
    }

    @Test
    public void testBuilderWithNoSearcherType() {
        ASTRATransformationEngineConfiguration.Builder builder = new ASTRATransformationEngineConfiguration.Builder();
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception).hasMessage("Property searcherType cannot be null");
    }

    @Test
    public void testBuilderWithDefaultAtomicRuleFitnessCalculator() {
        ASTRATransformationEngineConfiguration astraTransformationEngineConfiguration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .build();
        assertThat(astraTransformationEngineConfiguration.getAtomicRuleFitnessCalculatorType()).isEqualTo(AtomicRuleFitnessCalculatorType.DEFAULT);
        assertThat(astraTransformationEngineConfiguration.getExponentialFactor()).isZero();
    }

    @Test
    public void testBuilderWithLocalSmoothAtomicRuleFitnessCalculator() {
        AtomicRuleFitnessCalculatorType atomicRuleFitnessCalculatorType = AtomicRuleFitnessCalculatorType.SMOOTH_LOCAL;
        double exponentialFactor = 2.0;
        ASTRATransformationEngineConfiguration astraTransformationEngineConfiguration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .atomicRuleFitnessCalculatorType(atomicRuleFitnessCalculatorType)
                .exponentialFactor(exponentialFactor)
                .build();
        assertThat(astraTransformationEngineConfiguration.getAtomicRuleFitnessCalculatorType()).isEqualTo(atomicRuleFitnessCalculatorType);
        assertThat(astraTransformationEngineConfiguration.getExponentialFactor()).isEqualTo(exponentialFactor);
    }

    @Test
    public void testBuilderWithGlobalSmoothAtomicRuleFitnessCalculator() {
        AtomicRuleFitnessCalculatorType atomicRuleFitnessCalculatorType = AtomicRuleFitnessCalculatorType.SMOOTH_GLOBAL;
        double exponentialFactor = 3.0;
        ASTRATransformationEngineConfiguration astraTransformationEngineConfiguration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .atomicRuleFitnessCalculatorType(atomicRuleFitnessCalculatorType)
                .exponentialFactor(exponentialFactor)
                .build();
        assertThat(astraTransformationEngineConfiguration.getAtomicRuleFitnessCalculatorType()).isEqualTo(atomicRuleFitnessCalculatorType);
        assertThat(astraTransformationEngineConfiguration.getExponentialFactor()).isEqualTo(exponentialFactor);
    }

}
