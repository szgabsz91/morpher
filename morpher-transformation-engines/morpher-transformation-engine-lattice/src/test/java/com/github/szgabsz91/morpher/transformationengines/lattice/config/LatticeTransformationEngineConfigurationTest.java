package com.github.szgabsz91.morpher.transformationengines.lattice.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LatticeTransformationEngineConfigurationTest {

    @Test
    public void testConstructorAndGettersWithMissingLatticeBuilderType() {
        LatticeTransformationEngineConfiguration.Builder builder = new LatticeTransformationEngineConfiguration.Builder()
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(5);
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception).hasMessage("No lattice builder type provided");
    }

    @Test
    public void testConstructorAndGettersWithMissingWordConverterType() {
        LatticeTransformationEngineConfiguration.Builder builder = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .unlimitedMaximalContextSize();
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception).hasMessage("No word converter type provided");
    }

    @Test
    public void testConstructorAndGettersWithMissingCostCalculatorType() {
        LatticeTransformationEngineConfiguration.Builder builder = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .unlimitedMaximalContextSize();
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception).hasMessage("No cost calculator type provided");
    }

    @Test
    public void testConstructorAndGettersWithMissingCharacterRepositoryType() {
        LatticeTransformationEngineConfiguration.Builder builder = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .unlimitedMaximalContextSize();
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception).hasMessage("No character repository type provided");
    }

    @Test
    public void testConstructorAndGettersWithMissingMaximalContextSize() {
        LatticeTransformationEngineConfiguration.Builder builder = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE);
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception).hasMessage("No maximal context size provided");
    }

    @Test
    public void testConstructorAndGettersWithUnlimitedMaximalContextSize() {
        LatticeTransformationEngineConfiguration configuration = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .unlimitedMaximalContextSize()
                .build();
        assertThat(configuration.getLatticeBuilderType()).isEqualTo(LatticeBuilderType.MINIMAL);
        assertThat(configuration.getWordConverterType()).isEqualTo(WordConverterType.IDENTITY);
        assertThat(configuration.getCostCalculatorType()).isEqualTo(CostCalculatorType.DEFAULT);
        assertThat(configuration.getCharacterRepositoryType()).isEqualTo(CharacterRepositoryType.SIMPLE);
        assertThat(configuration.getMaximalContextSize()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void testConstructorAndGettersWithLimitedMaximalContextSize() {
        LatticeTransformationEngineConfiguration configuration = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(5)
                .build();
        assertThat(configuration.getLatticeBuilderType()).isEqualTo(LatticeBuilderType.MINIMAL);
        assertThat(configuration.getWordConverterType()).isEqualTo(WordConverterType.IDENTITY);
        assertThat(configuration.getCostCalculatorType()).isEqualTo(CostCalculatorType.DEFAULT);
        assertThat(configuration.getCharacterRepositoryType()).isEqualTo(CharacterRepositoryType.SIMPLE);
        assertThat(configuration.getMaximalContextSize()).isEqualTo(5);
    }
    
    @Test
    public void testEquals() {
        LatticeTransformationEngineConfiguration configuration1 = createConfiguration(LatticeBuilderType.MINIMAL, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, 5);
        LatticeTransformationEngineConfiguration configuration2 = createConfiguration(LatticeBuilderType.COMPLETE, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, 5);
        LatticeTransformationEngineConfiguration configuration3 = createConfiguration(LatticeBuilderType.MINIMAL, WordConverterType.DOUBLE_CONSONANT, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, 5);
        LatticeTransformationEngineConfiguration configuration4 = createConfiguration(LatticeBuilderType.MINIMAL, WordConverterType.IDENTITY, CostCalculatorType.ATTRIBUTE_BASED, CharacterRepositoryType.SIMPLE, 5);
        LatticeTransformationEngineConfiguration configuration5 = createConfiguration(LatticeBuilderType.MINIMAL, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.ATTRIBUTED, 5);
        LatticeTransformationEngineConfiguration configuration6 = createConfiguration(LatticeBuilderType.MINIMAL, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, 10);
        LatticeTransformationEngineConfiguration configuration7 = createConfiguration(LatticeBuilderType.MINIMAL, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, 5);
        
        assertThat(configuration1).isEqualTo(configuration1);
        assertThat(configuration1).isNotEqualTo(null);
        assertThat(configuration1).isNotEqualTo("string");
        assertThat(configuration1).isNotEqualTo(configuration2);
        assertThat(configuration1).isNotEqualTo(configuration3);
        assertThat(configuration1).isNotEqualTo(configuration4);
        assertThat(configuration1).isNotEqualTo(configuration5);
        assertThat(configuration1).isNotEqualTo(configuration6);
        assertThat(configuration1).isEqualTo(configuration7);
    }
    
    @Test
    public void testHashCode() {
        LatticeTransformationEngineConfiguration configuration = createConfiguration(LatticeBuilderType.MINIMAL, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, 5);
        int result = configuration.hashCode();
        int expected = configuration.getLatticeBuilderType().hashCode();
        expected = 31 * expected + configuration.getWordConverterType().hashCode();
        expected = 31 * expected + configuration.getCostCalculatorType().hashCode();
        expected = 31 * expected + configuration.getCharacterRepositoryType().hashCode();
        expected = 31 * expected + configuration.getMaximalContextSize();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testTosTring() {
        LatticeTransformationEngineConfiguration configuration = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(5)
                .build();
        String expected = "LatticeTransformationEngineConfiguration[" +
                "latticeBuilderType=" + configuration.getLatticeBuilderType() +
                ", wordConverterType=" + configuration.getWordConverterType() +
                ", costCalculatorType=" + configuration.getCostCalculatorType() +
                ", characterRepositoryType=" + configuration.getCharacterRepositoryType() +
                ", maximalContextSize=" + configuration.getMaximalContextSize() + "]";
        assertThat(configuration).hasToString(expected);
    }
    
    private static LatticeTransformationEngineConfiguration createConfiguration(
            LatticeBuilderType latticeBuilderType,
            WordConverterType wordConverterType,
            CostCalculatorType costCalculatorType,
            CharacterRepositoryType characterRepositoryType,
            int maximalContextSize) {
        return new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(latticeBuilderType)
                .wordConverterType(wordConverterType)
                .costCalculatorType(costCalculatorType)
                .characterRepositoryType(characterRepositoryType)
                .maximalContextSize(maximalContextSize)
                .build();
    }

}
