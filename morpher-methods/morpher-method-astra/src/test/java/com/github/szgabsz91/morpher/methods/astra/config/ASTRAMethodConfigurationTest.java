package com.github.szgabsz91.morpher.methods.astra.config;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ASTRAMethodConfigurationTest {

    @Test
    public void testBuildWithMaximumResponseProbabilityDifferenceThreshold() {
        double maximumResponseProbabilityDifferenceThreshold = 2.0;
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .maximumResponseProbabilityDifferenceThreshold(maximumResponseProbabilityDifferenceThreshold)
                .build();
        assertThat(configuration.getMaximumResponseProbabilityDifferenceThreshold()).isEqualTo(maximumResponseProbabilityDifferenceThreshold);
    }

    @Test
    public void testExponentialFactorWithNegativeValue() {
        ASTRAMethodConfiguration.Builder builder = new ASTRAMethodConfiguration.Builder();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.exponentialFactor(-0.1));
        assertThat(exception).hasMessage("Exponential factor cannot be negative");
    }

    @Test
    public void testBuilderWithNonDefaultMaximumNumberOfResponses() {
        SearcherType searcherType = SearcherType.PARALLEL;
        int maximumNumberOfResponses = 2;
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
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
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
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
        ASTRAMethodConfiguration.Builder builder = new ASTRAMethodConfiguration.Builder();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.maximumNumberOfResponses(0));
        assertThat(exception).hasMessage("Property maximumNumberOfResponses must be a positive integer, but it was 0");
    }

    @Test
    public void testBuilderWithNoSearcherType() {
        ASTRAMethodConfiguration.Builder builder = new ASTRAMethodConfiguration.Builder();
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception).hasMessage("Property searcherType cannot be null");
    }

    @Test
    public void testBuilderWithDefaultAtomicRuleFitnessCalculator() {
        ASTRAMethodConfiguration astraMethodConfiguration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .build();
        assertThat(astraMethodConfiguration.getAtomicRuleFitnessCalculatorType()).isEqualTo(AtomicRuleFitnessCalculatorType.DEFAULT);
        assertThat(astraMethodConfiguration.getExponentialFactor()).isZero();
    }

    @Test
    public void testBuilderWithLocalSmoothAtomicRuleFitnessCalculator() {
        AtomicRuleFitnessCalculatorType atomicRuleFitnessCalculatorType = AtomicRuleFitnessCalculatorType.SMOOTH_LOCAL;
        double exponentialFactor = 2.0;
        ASTRAMethodConfiguration astraMethodConfiguration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .atomicRuleFitnessCalculatorType(atomicRuleFitnessCalculatorType)
                .exponentialFactor(exponentialFactor)
                .build();
        assertThat(astraMethodConfiguration.getAtomicRuleFitnessCalculatorType()).isEqualTo(atomicRuleFitnessCalculatorType);
        assertThat(astraMethodConfiguration.getExponentialFactor()).isEqualTo(exponentialFactor);
    }

    @Test
    public void testBuilderWithGlobalSmoothAtomicRuleFitnessCalculator() {
        AtomicRuleFitnessCalculatorType atomicRuleFitnessCalculatorType = AtomicRuleFitnessCalculatorType.SMOOTH_GLOBAL;
        double exponentialFactor = 3.0;
        ASTRAMethodConfiguration astraMethodConfiguration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .atomicRuleFitnessCalculatorType(atomicRuleFitnessCalculatorType)
                .exponentialFactor(exponentialFactor)
                .build();
        assertThat(astraMethodConfiguration.getAtomicRuleFitnessCalculatorType()).isEqualTo(atomicRuleFitnessCalculatorType);
        assertThat(astraMethodConfiguration.getExponentialFactor()).isEqualTo(exponentialFactor);
    }

    @Test
    public void testEquals() {
        ASTRAMethodConfiguration configuration1 = createConfiguration(SearcherType.PARALLEL, 0.5, 1, 4, 5, 6, null, null, AtomicRuleFitnessCalculatorType.DEFAULT, 2.0, 3.0);
        ASTRAMethodConfiguration configuration2 = createConfiguration(SearcherType.PREFIX_TREE, 0.5, 1, 4, 5, 6, null, null, AtomicRuleFitnessCalculatorType.DEFAULT, 2.0, 3.0);
        ASTRAMethodConfiguration configuration3 = createConfiguration(SearcherType.PARALLEL, 0.6, 1, 4, 5, 6, null, null, AtomicRuleFitnessCalculatorType.DEFAULT, 2.0, 3.0);
        ASTRAMethodConfiguration configuration4 = createConfiguration(SearcherType.PARALLEL, 0.5, 2, 4, 5, 6, null, null, AtomicRuleFitnessCalculatorType.DEFAULT, 2.0, 3.0);
        ASTRAMethodConfiguration configuration5 = createConfiguration(SearcherType.PARALLEL, 0.5, 1, 4, 5, 6, 3, null, AtomicRuleFitnessCalculatorType.DEFAULT, 2.0, 3.0);
        ASTRAMethodConfiguration configuration6 = createConfiguration(SearcherType.PARALLEL, 0.5, 1, 4, 5, 6, null, 1, AtomicRuleFitnessCalculatorType.DEFAULT, 2.0, 3.0);
        ASTRAMethodConfiguration configuration7 = createConfiguration(SearcherType.PARALLEL, 0.5, 1, 4, 5, 6, null, null, AtomicRuleFitnessCalculatorType.DEFAULT, 3.0, 3.0);
        ASTRAMethodConfiguration configuration8 = createConfiguration(SearcherType.PARALLEL, 0.5, 1, 4, 5, 6, null, null, AtomicRuleFitnessCalculatorType.SMOOTH_LOCAL, 2.0, 3.0);
        ASTRAMethodConfiguration configuration9 = createConfiguration(SearcherType.PARALLEL, 0.5, 1, 4, 5, 6, null, null, AtomicRuleFitnessCalculatorType.DEFAULT, 2.0, 4.0);
        ASTRAMethodConfiguration configuration10 = createConfiguration(SearcherType.PARALLEL, 0.5, 1, 40, 5, 6, null, null, AtomicRuleFitnessCalculatorType.DEFAULT, 2.0, 3.0);
        ASTRAMethodConfiguration configuration11 = createConfiguration(SearcherType.PARALLEL, 0.5, 1, 4, 50, 6, null, null, AtomicRuleFitnessCalculatorType.DEFAULT, 2.0, 3.0);
        ASTRAMethodConfiguration configuration12 = createConfiguration(SearcherType.PARALLEL, 0.5, 1, 4, 5, 60, null, null, AtomicRuleFitnessCalculatorType.DEFAULT, 2.0, 3.0);
        ASTRAMethodConfiguration configuration13 = createConfiguration(SearcherType.PARALLEL, 0.5, 1, 4, 5, 6, null, null, AtomicRuleFitnessCalculatorType.DEFAULT, 2.0, 3.0);

        assertThat(configuration1).isEqualTo(configuration1);
        assertThat(configuration1).isNotEqualTo(null);
        assertThat(configuration1).isNotEqualTo("string");
        assertThat(configuration1).isNotEqualTo(configuration2);
        assertThat(configuration1).isNotEqualTo(configuration3);
        assertThat(configuration1).isNotEqualTo(configuration4);
        assertThat(configuration1).isNotEqualTo(configuration5);
        assertThat(configuration1).isNotEqualTo(configuration6);
        assertThat(configuration1).isNotEqualTo(configuration7);
        assertThat(configuration1).isNotEqualTo(configuration8);
        assertThat(configuration1).isNotEqualTo(configuration9);
        assertThat(configuration1).isNotEqualTo(configuration10);
        assertThat(configuration1).isNotEqualTo(configuration11);
        assertThat(configuration1).isNotEqualTo(configuration12);
        assertThat(configuration1).isEqualTo(configuration13);
    }

    @Test
    public void testHashCode() {
        ASTRAMethodConfiguration configuration = createConfiguration(SearcherType.PARALLEL, 0.5, 2, 3, 1);
        int result = configuration.hashCode();
        assertThat(result).isEqualTo(Objects.hash(configuration.getSearcherType(), configuration.getFitnessThreshold(), configuration.getMaximumNumberOfResponses(), configuration.getMinimumSupportThreshold(), configuration.getMinimumWordFrequencyThreshold(), configuration.getMinimumAggregatedSupportThreshold(), configuration.getMinimumContextLength(), configuration.getMaximumNumberOfGeneratedAtomicRules(), configuration.getAtomicRuleFitnessCalculatorType(), configuration.getExponentialFactor(), configuration.getMaximumResponseProbabilityDifferenceThreshold()));
    }

    @Test
    public void testToString() {
        ASTRAMethodConfiguration configuration = createConfiguration(SearcherType.PARALLEL, 0.5, 2, 3, 1);
        assertThat(configuration).hasToString("ASTRAMethodConfiguration[searcherType=" + configuration.getSearcherType() + ", fitnessThreshold=" + configuration.getFitnessThreshold() + ", maximumNumberOfResponses=" + configuration.getMaximumNumberOfResponses() + ", minimumSupportThreshold=" + configuration.getMinimumSupportThreshold() + ", minimumWordFrequencyThreshold=" + configuration.getMinimumWordFrequencyThreshold() + ", minimumAggregatedSupportThreshold=" + configuration.getMinimumAggregatedSupportThreshold() + ", minimumContextLength=" + configuration.getMinimumContextLength() + ", maximumNumberOfGeneratedAtomicRules=" + configuration.getMaximumNumberOfGeneratedAtomicRules() + ", atomicRuleFitnessCalculatorType=" + configuration.getAtomicRuleFitnessCalculatorType() + ", exponentialFactor=" + configuration.getExponentialFactor() + ", maximumResponseProbabilityDifferenceThreshold=" + configuration.getMaximumResponseProbabilityDifferenceThreshold() + ']');
    }

    private ASTRAMethodConfiguration createConfiguration(SearcherType searcherType, double fitnessThreshold, int maximumNumberOfResponses, Integer minimumContextLength, Integer maximumNumberOfGeneratedAtomicRules) {
        return createConfiguration(searcherType, fitnessThreshold, maximumNumberOfResponses, null, null, null, minimumContextLength, maximumNumberOfGeneratedAtomicRules, AtomicRuleFitnessCalculatorType.DEFAULT, 0.0, 2.0);
    }

    private ASTRAMethodConfiguration createConfiguration(SearcherType searcherType, double fitnessThreshold, int maximumNumberOfResponses, Integer minimumSupportThreshold, Integer minimumWordFrequencyThreshold, Integer minimumAggregatedSupportThreshold, Integer minimumContextLength, Integer maximumNumberOfGeneratedAtomicRules, AtomicRuleFitnessCalculatorType atomicRuleFitnessCalculatorType, double exponentialFactor, Double maximumResponseProbabilityDifferenceThreshold) {
        ASTRAMethodConfiguration.Builder builder = new ASTRAMethodConfiguration.Builder()
                .searcherType(searcherType)
                .fitnessThreshold(fitnessThreshold)
                .maximumNumberOfResponses(maximumNumberOfResponses)
                .atomicRuleFitnessCalculatorType(atomicRuleFitnessCalculatorType)
                .exponentialFactor(exponentialFactor);
        if (minimumSupportThreshold != null) {
            builder = builder.minimumSupportThreshold(minimumSupportThreshold);
        }
        if (minimumWordFrequencyThreshold != null) {
            builder = builder.minimumWordFrequencyThreshold(minimumWordFrequencyThreshold);
        }
        if (minimumAggregatedSupportThreshold != null) {
            builder = builder.minimumAggregatedSupportThreshold(minimumAggregatedSupportThreshold);
        }
        if (minimumContextLength != null) {
            builder = builder.minimumContextLength(minimumContextLength);
        }
        if (maximumNumberOfGeneratedAtomicRules != null) {
            builder = builder.maximumNumberOfGeneratedAtomicRules(maximumNumberOfGeneratedAtomicRules);
        }
        if (maximumResponseProbabilityDifferenceThreshold != null) {
            builder = builder.maximumResponseProbabilityDifferenceThreshold(maximumResponseProbabilityDifferenceThreshold);
        }
        return builder.build();
    }

}
