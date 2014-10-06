package com.github.szgabsz91.morpher.methods.lattice.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IdentityWordConverter;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.ITrainingSetProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.IWordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.TrainingSetProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.WordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.DefaultCostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.CharacterRepositoryTypeMessage;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.CostCalculatorTypeMessage;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.TrainingSetProcessorMessage;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.WordConverterTypeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainingSetProcessorConverterTest {

    private TrainingSetProcessorConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new TrainingSetProcessorConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParseWithAdvancedComponents() throws IOException {
        ICharacterRepository characterRepository = HungarianAttributedCharacterRepository.get();
        IWordConverter wordConverter = new DoubleConsonantWordConverter();
        ICostCalculator costCalculator = new AttributeBasedCostCalculator();
        int maximalContextSize = 5;
        IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .maximalContextSize(maximalContextSize)
                .build();
        TrainingSetProcessor trainingSetProcessor = new TrainingSetProcessor(wordPairProcessor);
        trainingSetProcessor.induceRules(Set.of(WordPair.of("a", "b")));

        TrainingSetProcessorMessage trainingSetProcessorMessage = this.converter.convert(trainingSetProcessor);
        assertThat(trainingSetProcessorMessage.getCostCalculatorType()).isEqualTo(CostCalculatorTypeMessage.ATTRIBUTE_BASED);
        assertThat(trainingSetProcessorMessage.getCharacterRepositoryType()).isEqualTo(CharacterRepositoryTypeMessage.ATTRIBUTED);
        assertThat(trainingSetProcessorMessage.getWordConverterType()).isEqualTo(WordConverterTypeMessage.DOUBLE_CONSONANT);
        assertThat(trainingSetProcessorMessage.getMaximalContextSize()).isEqualTo(maximalContextSize);
        assertThat(trainingSetProcessorMessage.getTransformationListsList()).hasSize(2);
        assertThat(trainingSetProcessorMessage.getFrequencyMapMap()).hasSize(2);

        TrainingSetProcessor result = (TrainingSetProcessor) this.converter.convertBack(trainingSetProcessorMessage);
        assertThat(result.getWordPairProcessor()).isNotNull();
        assertThat(result.getWordPairProcessor()).isInstanceOf(WordPairProcessor.class);
        WordPairProcessor resultingWordPairProcessor = (WordPairProcessor) result.getWordPairProcessor();
        assertThat(resultingWordPairProcessor.getCharacterRepository()).isInstanceOf(HungarianAttributedCharacterRepository.class);
        assertThat(resultingWordPairProcessor.getCostCalculator()).isInstanceOf(AttributeBasedCostCalculator.class);
        assertThat(resultingWordPairProcessor.getMaximalContextSize()).isEqualTo(maximalContextSize);
        assertThat(resultingWordPairProcessor.getWordConverter()).isInstanceOf(DoubleConsonantWordConverter.class);
        assertThat(result.getFrequencyMap()).isEqualTo(trainingSetProcessor.getFrequencyMap());

        Serializer<ITrainingSetProcessor, TrainingSetProcessorMessage> serializer = new Serializer<>(this.converter, trainingSetProcessor);
        Path file = Files.createTempFile("morpher", "lattice");
        try {
            serializer.serialize(trainingSetProcessor, file);
            TrainingSetProcessorMessage resultingTrainingSetProcessorMessage = this.converter.parse(file);
            assertThat(resultingTrainingSetProcessorMessage).isEqualTo(trainingSetProcessorMessage);
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testConvertAndConvertBackAndParseWithBasicComponents() throws IOException {
        ICharacterRepository characterRepository = HungarianSimpleCharacterRepository.get();
        IWordConverter wordConverter = new IdentityWordConverter();
        ICostCalculator costCalculator = new DefaultCostCalculator();
        int maximalContextSize = 10;
        IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                .characterRepository(characterRepository)
                .wordConverter(wordConverter)
                .costCalculator(costCalculator)
                .maximalContextSize(maximalContextSize)
                .build();
        TrainingSetProcessor trainingSetProcessor = new TrainingSetProcessor(wordPairProcessor);
        trainingSetProcessor.induceRules(Set.of(WordPair.of("a", "b")));

        TrainingSetProcessorMessage trainingSetProcessorMessage = this.converter.convert(trainingSetProcessor);
        assertThat(trainingSetProcessorMessage.getCostCalculatorType()).isEqualTo(CostCalculatorTypeMessage.DEFAULT);
        assertThat(trainingSetProcessorMessage.getCharacterRepositoryType()).isEqualTo(CharacterRepositoryTypeMessage.SIMPLE);
        assertThat(trainingSetProcessorMessage.getWordConverterType()).isEqualTo(WordConverterTypeMessage.IDENTITY);
        assertThat(trainingSetProcessorMessage.getMaximalContextSize()).isEqualTo(maximalContextSize);
        assertThat(trainingSetProcessorMessage.getTransformationListsList()).hasSize(1);
        assertThat(trainingSetProcessorMessage.getFrequencyMapMap()).hasSize(1);

        TrainingSetProcessor result = (TrainingSetProcessor) this.converter.convertBack(trainingSetProcessorMessage);
        assertThat(result.getWordPairProcessor()).isNotNull();
        assertThat(result.getWordPairProcessor()).isInstanceOf(WordPairProcessor.class);
        WordPairProcessor resultingWordPairProcessor = (WordPairProcessor) result.getWordPairProcessor();
        assertThat(resultingWordPairProcessor.getCharacterRepository()).isInstanceOf(HungarianSimpleCharacterRepository.class);
        assertThat(resultingWordPairProcessor.getCostCalculator()).isInstanceOf(DefaultCostCalculator.class);
        assertThat(resultingWordPairProcessor.getMaximalContextSize()).isEqualTo(maximalContextSize);
        assertThat(resultingWordPairProcessor.getWordConverter()).isInstanceOf(IdentityWordConverter.class);
        assertThat(result.getFrequencyMap()).isEqualTo(trainingSetProcessor.getFrequencyMap());

        Serializer<ITrainingSetProcessor, TrainingSetProcessorMessage> serializer = new Serializer<>(this.converter, trainingSetProcessor);
        Path file = Files.createTempFile("morpher", "lattice");
        try {
            serializer.serialize(trainingSetProcessor, file);
            TrainingSetProcessorMessage resultingTrainingSetProcessorMessage = this.converter.parse(file);
            assertThat(resultingTrainingSetProcessorMessage).isEqualTo(trainingSetProcessorMessage);
        }
        finally {
            Files.delete(file);
        }
    }

}
