package com.github.szgabsz91.morpher.methods.lattice.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LatticeMethodConfigurationTest {

    @Test
    public void testConstructorAndGettersWithMissingLatticeBuilderType() {
        LatticeMethodConfiguration.Builder builder = new LatticeMethodConfiguration.Builder()
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(5);
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception).hasMessage("No lattice builder type provided");
    }

    @Test
    public void testConstructorAndGettersWithMissingWordConverterType() {
        LatticeMethodConfiguration.Builder builder = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .unlimitedMaximalContextSize();
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception).hasMessage("No word converter type provided");
    }

    @Test
    public void testConstructorAndGettersWithMissingCostCalculatorType() {
        LatticeMethodConfiguration.Builder builder = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .unlimitedMaximalContextSize();
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception).hasMessage("No cost calculator type provided");
    }

    @Test
    public void testConstructorAndGettersWithMissingCharacterRepositoryType() {
        LatticeMethodConfiguration.Builder builder = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .unlimitedMaximalContextSize();
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception).hasMessage("No character repository type provided");
    }

    @Test
    public void testConstructorAndGettersWithMissingMaximalContextSize() {
        LatticeMethodConfiguration.Builder builder = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE);
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception).hasMessage("No maximal context size provided");
    }

    @Test
    public void testConstructorAndGettersWithUnlimitedMaximalContextSize() {
        LatticeMethodConfiguration configuration = new LatticeMethodConfiguration.Builder()
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
        LatticeMethodConfiguration configuration = new LatticeMethodConfiguration.Builder()
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
        LatticeMethodConfiguration configuration1 = createConfiguration(LatticeBuilderType.MINIMAL, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, 5);
        LatticeMethodConfiguration configuration2 = createConfiguration(LatticeBuilderType.FULL, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, 5);
        LatticeMethodConfiguration configuration3 = createConfiguration(LatticeBuilderType.MINIMAL, WordConverterType.DOUBLE_CONSONANT, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, 5);
        LatticeMethodConfiguration configuration4 = createConfiguration(LatticeBuilderType.MINIMAL, WordConverterType.IDENTITY, CostCalculatorType.ATTRIBUTE_BASED, CharacterRepositoryType.SIMPLE, 5);
        LatticeMethodConfiguration configuration5 = createConfiguration(LatticeBuilderType.MINIMAL, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.ATTRIBUTED, 5);
        LatticeMethodConfiguration configuration6 = createConfiguration(LatticeBuilderType.MINIMAL, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, 10);
        LatticeMethodConfiguration configuration7 = createConfiguration(LatticeBuilderType.MINIMAL, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, 5);
        
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
        LatticeMethodConfiguration configuration = createConfiguration(LatticeBuilderType.MINIMAL, WordConverterType.IDENTITY, CostCalculatorType.DEFAULT, CharacterRepositoryType.SIMPLE, 5);
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
        LatticeMethodConfiguration configuration = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(5)
                .build();
        String expected = "LatticeMethodConfiguration[" +
                "latticeBuilderType=" + configuration.getLatticeBuilderType() +
                ", wordConverterType=" + configuration.getWordConverterType() +
                ", costCalculatorType=" + configuration.getCostCalculatorType() +
                ", characterRepositoryType=" + configuration.getCharacterRepositoryType() +
                ", maximalContextSize=" + configuration.getMaximalContextSize() + "]";
        assertThat(configuration).hasToString(expected);
    }
    
    private static LatticeMethodConfiguration createConfiguration(
            LatticeBuilderType latticeBuilderType,
            WordConverterType wordConverterType,
            CostCalculatorType costCalculatorType,
            CharacterRepositoryType characterRepositoryType,
            int maximalContextSize) {
        return new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(latticeBuilderType)
                .wordConverterType(wordConverterType)
                .costCalculatorType(costCalculatorType)
                .characterRepositoryType(characterRepositoryType)
                .maximalContextSize(maximalContextSize)
                .build();
    }

}
