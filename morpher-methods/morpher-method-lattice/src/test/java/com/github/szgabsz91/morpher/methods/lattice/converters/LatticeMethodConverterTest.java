package com.github.szgabsz91.morpher.methods.lattice.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.github.szgabsz91.morpher.methods.lattice.config.CharacterRepositoryType;
import com.github.szgabsz91.morpher.methods.lattice.config.CostCalculatorType;
import com.github.szgabsz91.morpher.methods.lattice.config.LatticeBuilderType;
import com.github.szgabsz91.morpher.methods.lattice.config.LatticeMethodConfiguration;
import com.github.szgabsz91.morpher.methods.lattice.config.WordConverterType;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.AbstractLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.method.LatticeMethod;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.TrainingSetProcessor;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.LatticeMethodMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class LatticeMethodConverterTest {

    private LatticeMethodConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new LatticeMethodConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParseWithUnidirectionalMethod() throws IOException {
        CharacterRepositoryType characterRepository = CharacterRepositoryType.SIMPLE;
        WordConverterType wordConverterType = WordConverterType.IDENTITY;
        CostCalculatorType costCalculatorType = CostCalculatorType.DEFAULT;
        LatticeBuilderType latticeBuilderType = LatticeBuilderType.MINIMAL;
        int maximalContextSize = 5;
        LatticeMethodConfiguration configuration = new LatticeMethodConfiguration.Builder()
                .characterRepositoryType(characterRepository)
                .wordConverterType(wordConverterType)
                .costCalculatorType(costCalculatorType)
                .latticeBuilderType(latticeBuilderType)
                .maximalContextSize(maximalContextSize)
                .build();
        AffixType affixType = AffixType.of("AFF");
        LatticeMethod latticeMethod = new LatticeMethod(true, affixType, configuration);
        latticeMethod.learn(TrainingSet.of(WordPair.of("a", "b")));

        LatticeMethodMessage latticeMethodMessage = this.converter.convert(latticeMethod);
        assertThat(latticeMethodMessage.getAffixType()).isEqualTo(affixType.toString());
        assertThat(latticeMethodMessage.getInflectionTrainingSetProcessor()).isNotNull();
        assertThat(latticeMethodMessage.getInflectionTrainingSetProcessor().getTransformationListsCount()).isEqualTo(1);
        assertThat(latticeMethodMessage.getInflectionTrainingSetProcessor().getFrequencyMapCount()).isEqualTo(1);
        assertThat(latticeMethodMessage.getInflectionLatticeBuilder()).isNotNull();
        assertThat(latticeMethodMessage.getInflectionLatticeBuilder().getLattice().getNodeCount()).isEqualTo(3);
        assertThat(latticeMethodMessage.getInflectionLatticeBuilder().getLattice().getNeighborhoodCount()).isEqualTo(2);
        assertThat(latticeMethodMessage.hasLemmatizationTrainingSetProcessor()).isFalse();
        assertThat(latticeMethodMessage.hasLemmatizationLatticeBuilder()).isFalse();

        LatticeMethod result = this.converter.convertBack(latticeMethodMessage);
        assertThat(result.getAffixType()).isEqualTo(affixType);
        TrainingSetProcessor inflectionTrainingSetProcessor = (TrainingSetProcessor) result.getInflectionTrainingSetProcessor();
        assertThat(inflectionTrainingSetProcessor.getFrequencyMap()).hasSize(1);
        AbstractLatticeBuilder inflectionLatticeBuilder = (AbstractLatticeBuilder) result.getInflectionLatticeBuilder();
        assertThat(inflectionLatticeBuilder.getLattice().size()).isEqualTo(3);
        assertThat(result.getLemmatizationTrainingSetProcessor()).isNull();
        assertThat(result.getLemmatizationLatticeBuilder()).isNull();

        Serializer<LatticeMethod, LatticeMethodMessage> serializer = new Serializer<>(this.converter, latticeMethod);
        Path file = Files.createTempFile("morpher", "lattice");
        try {
            serializer.serialize(latticeMethod, file);
            LatticeMethodMessage resultingLatticeMethodMessage = this.converter.parse(file);
            assertThat(resultingLatticeMethodMessage).isEqualTo(latticeMethodMessage);
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testConvertAndConvertBackAndParseWithBidirectionalMethod() throws IOException {
        CharacterRepositoryType characterRepository = CharacterRepositoryType.SIMPLE;
        WordConverterType wordConverterType = WordConverterType.IDENTITY;
        CostCalculatorType costCalculatorType = CostCalculatorType.DEFAULT;
        LatticeBuilderType latticeBuilderType = LatticeBuilderType.MINIMAL;
        int maximalContextSize = 5;
        LatticeMethodConfiguration configuration = new LatticeMethodConfiguration.Builder()
                .characterRepositoryType(characterRepository)
                .wordConverterType(wordConverterType)
                .costCalculatorType(costCalculatorType)
                .latticeBuilderType(latticeBuilderType)
                .maximalContextSize(maximalContextSize)
                .build();
        AffixType affixType = AffixType.of("AFF");
        LatticeMethod latticeMethod = new LatticeMethod(false, affixType, configuration);
        latticeMethod.learn(TrainingSet.of(WordPair.of("a", "b")));

        LatticeMethodMessage latticeMethodMessage = this.converter.convert(latticeMethod);
        assertThat(latticeMethodMessage.getAffixType()).isEqualTo(affixType.toString());
        assertThat(latticeMethodMessage.getInflectionTrainingSetProcessor()).isNotNull();
        assertThat(latticeMethodMessage.getInflectionTrainingSetProcessor().getTransformationListsCount()).isEqualTo(1);
        assertThat(latticeMethodMessage.getInflectionTrainingSetProcessor().getFrequencyMapCount()).isEqualTo(1);
        assertThat(latticeMethodMessage.getInflectionLatticeBuilder()).isNotNull();
        assertThat(latticeMethodMessage.getInflectionLatticeBuilder().getLattice().getNodeCount()).isEqualTo(3);
        assertThat(latticeMethodMessage.getInflectionLatticeBuilder().getLattice().getNeighborhoodCount()).isEqualTo(2);
        assertThat(latticeMethodMessage.getLemmatizationTrainingSetProcessor()).isNotNull();
        assertThat(latticeMethodMessage.getLemmatizationTrainingSetProcessor().getTransformationListsCount()).isEqualTo(1);
        assertThat(latticeMethodMessage.getLemmatizationTrainingSetProcessor().getFrequencyMapCount()).isEqualTo(1);
        assertThat(latticeMethodMessage.getLemmatizationLatticeBuilder()).isNotNull();
        assertThat(latticeMethodMessage.getLemmatizationLatticeBuilder().getLattice().getNodeCount()).isEqualTo(3);
        assertThat(latticeMethodMessage.getLemmatizationLatticeBuilder().getLattice().getNeighborhoodCount()).isEqualTo(2);

        LatticeMethod result = this.converter.convertBack(latticeMethodMessage);
        assertThat(result.getAffixType()).isEqualTo(affixType);
        TrainingSetProcessor inflectionTrainingSetProcessor = (TrainingSetProcessor) result.getInflectionTrainingSetProcessor();
        assertThat(inflectionTrainingSetProcessor.getFrequencyMap()).hasSize(1);
        AbstractLatticeBuilder inflectionLatticeBuilder = (AbstractLatticeBuilder) result.getInflectionLatticeBuilder();
        assertThat(inflectionLatticeBuilder.getLattice().size()).isEqualTo(3);
        TrainingSetProcessor lemmatizationTrainingSetProcessor = (TrainingSetProcessor) result.getLemmatizationTrainingSetProcessor();
        assertThat(lemmatizationTrainingSetProcessor.getFrequencyMap()).hasSize(1);
        AbstractLatticeBuilder lemmatizationLatticeBuilder = (AbstractLatticeBuilder) result.getLemmatizationLatticeBuilder();
        assertThat(lemmatizationLatticeBuilder.getLattice().size()).isEqualTo(3);

        Serializer<LatticeMethod, LatticeMethodMessage> serializer = new Serializer<>(this.converter, latticeMethod);
        Path file = Files.createTempFile("morpher", "lattice");
        try {
            serializer.serialize(latticeMethod, file);
            LatticeMethodMessage resultingLatticeMethodMessage = this.converter.parse(file);
            assertThat(resultingLatticeMethodMessage).isEqualTo(latticeMethodMessage);
        }
        finally {
            Files.delete(file);
        }
    }

}
