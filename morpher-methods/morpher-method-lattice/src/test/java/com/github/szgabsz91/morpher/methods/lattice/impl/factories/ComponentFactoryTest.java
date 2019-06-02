package com.github.szgabsz91.morpher.methods.lattice.impl.factories;

import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IdentityWordConverter;
import com.github.szgabsz91.morpher.methods.lattice.config.CharacterRepositoryType;
import com.github.szgabsz91.morpher.methods.lattice.config.CostCalculatorType;
import com.github.szgabsz91.morpher.methods.lattice.config.LatticeBuilderType;
import com.github.szgabsz91.morpher.methods.lattice.config.WordConverterType;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.AbstractLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.FullLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.HomogeneousLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.ILatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.MaximalHomogeneousLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.MinimalLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.TrainingSetProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.WordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.DefaultCostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentFactoryTest {

    @Test
    public void testPrivateConstructor() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
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
    public void testCreateTrainingSetProcessor() {
        TrainingSetProcessor trainingSetProcessor = (TrainingSetProcessor) ComponentFactory.createTrainingSetProcessor(
                CostCalculatorType.ATTRIBUTE_BASED,
                CharacterRepositoryType.ATTRIBUTED,
                WordConverterType.DOUBLE_CONSONANT,
                5
        );
        WordPairProcessor wordPairProcessor = (WordPairProcessor) trainingSetProcessor.getWordPairProcessor();
        assertThat(wordPairProcessor.getCostCalculator()).isInstanceOf(AttributeBasedCostCalculator.class);
        assertThat(wordPairProcessor.getCharacterRepository()).isInstanceOf(HungarianAttributedCharacterRepository.class);
        assertThat(wordPairProcessor.getWordConverter()).isInstanceOf(DoubleConsonantWordConverter.class);
        assertThat(wordPairProcessor.getMaximalContextSize()).isEqualTo(5);
    }

    @Test
    public void testCreateLatticeBuilderWithFull() {
        ILatticeBuilder latticeBuilder = ComponentFactory.createLatticeBuilder(
                LatticeBuilderType.FULL,
                CharacterRepositoryType.ATTRIBUTED,
                WordConverterType.DOUBLE_CONSONANT
        );
        assertThat(latticeBuilder).isInstanceOf(FullLatticeBuilder.class);
        AbstractLatticeBuilder abstractLatticeBuilder = (AbstractLatticeBuilder) latticeBuilder;
        assertThat(abstractLatticeBuilder.getCharacterRepository()).isInstanceOf(HungarianAttributedCharacterRepository.class);
        assertThat(abstractLatticeBuilder.getWordConverter()).isInstanceOf(DoubleConsonantWordConverter.class);
    }

    @Test
    public void testCreateLatticeBuilderWithHomogeneous() {
        ILatticeBuilder latticeBuilder = ComponentFactory.createLatticeBuilder(
                LatticeBuilderType.HOMOGENEOUS,
                CharacterRepositoryType.ATTRIBUTED,
                WordConverterType.DOUBLE_CONSONANT
        );
        assertThat(latticeBuilder).isInstanceOf(HomogeneousLatticeBuilder.class);
        AbstractLatticeBuilder abstractLatticeBuilder = (AbstractLatticeBuilder) latticeBuilder;
        assertThat(abstractLatticeBuilder.getCharacterRepository()).isInstanceOf(HungarianAttributedCharacterRepository.class);
        assertThat(abstractLatticeBuilder.getWordConverter()).isInstanceOf(DoubleConsonantWordConverter.class);
    }

    @Test
    public void testCreateLatticeBuilderWithMaximalHomogeneous() {
        ILatticeBuilder latticeBuilder = ComponentFactory.createLatticeBuilder(
                LatticeBuilderType.MAXIMAL_HOMOGENEOUS,
                CharacterRepositoryType.ATTRIBUTED,
                WordConverterType.DOUBLE_CONSONANT
        );
        assertThat(latticeBuilder).isInstanceOf(MaximalHomogeneousLatticeBuilder.class);
        AbstractLatticeBuilder abstractLatticeBuilder = (AbstractLatticeBuilder) latticeBuilder;
        assertThat(abstractLatticeBuilder.getCharacterRepository()).isInstanceOf(HungarianAttributedCharacterRepository.class);
        assertThat(abstractLatticeBuilder.getWordConverter()).isInstanceOf(DoubleConsonantWordConverter.class);
    }

    @Test
    public void testCreateLatticeBuilderWithMinimal() {
        ILatticeBuilder latticeBuilder = ComponentFactory.createLatticeBuilder(
                LatticeBuilderType.MINIMAL,
                CharacterRepositoryType.ATTRIBUTED,
                WordConverterType.DOUBLE_CONSONANT
        );
        assertThat(latticeBuilder).isInstanceOf(MinimalLatticeBuilder.class);
        AbstractLatticeBuilder abstractLatticeBuilder = (AbstractLatticeBuilder) latticeBuilder;
        assertThat(abstractLatticeBuilder.getCharacterRepository()).isInstanceOf(HungarianAttributedCharacterRepository.class);
        assertThat(abstractLatticeBuilder.getWordConverter()).isInstanceOf(DoubleConsonantWordConverter.class);
    }

    @Test
    public void testCreateWordConverterWithIdentity() {
        IWordConverter wordConverter = ComponentFactory.createWordConverter(WordConverterType.IDENTITY);
        assertThat(wordConverter).isInstanceOf(IdentityWordConverter.class);
    }

    @Test
    public void testCreateWordConverterWithDoubleConsonant() {
        IWordConverter wordConverter = ComponentFactory.createWordConverter(WordConverterType.DOUBLE_CONSONANT);
        assertThat(wordConverter).isInstanceOf(DoubleConsonantWordConverter.class);
    }

    @Test
    public void testCreateCostCalculatorWithDefault() {
        ICostCalculator costCalculator = ComponentFactory.createCostCalculator(CostCalculatorType.DEFAULT);
        assertThat(costCalculator).isInstanceOf(DefaultCostCalculator.class);
    }

    @Test
    public void testCreateCostCalculatorWithAttributeBased() {
        ICostCalculator costCalculator = ComponentFactory.createCostCalculator(CostCalculatorType.ATTRIBUTE_BASED);
        assertThat(costCalculator).isInstanceOf(AttributeBasedCostCalculator.class);
    }

    @Test
    public void testCreateCharacterRepositoryWithSimple() {
        ICharacterRepository characterRepository = ComponentFactory.createCharacterRepository(CharacterRepositoryType.SIMPLE);
        assertThat(characterRepository).isInstanceOf(HungarianSimpleCharacterRepository.class);
    }

    @Test
    public void testCreateCharacterRepositoryWithAttributed() {
        ICharacterRepository characterRepository = ComponentFactory.createCharacterRepository(CharacterRepositoryType.ATTRIBUTED);
        assertThat(characterRepository).isInstanceOf(HungarianAttributedCharacterRepository.class);
    }

}
