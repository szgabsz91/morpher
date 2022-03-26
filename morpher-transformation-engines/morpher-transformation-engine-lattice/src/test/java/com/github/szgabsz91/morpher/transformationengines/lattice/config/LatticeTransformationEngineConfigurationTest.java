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

}
