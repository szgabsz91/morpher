package com.github.szgabsz91.morpher.transformationengines.lattice.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.CharacterRepositoryType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.CostCalculatorType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.LatticeBuilderType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.LatticeTransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.WordConverterType;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.AbstractLatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.TrainingSetProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.transformationengine.LatticeTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.LatticeTransformationEngineMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class LatticeTransformationEngineConverterTest {

    private LatticeTransformationEngineConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new LatticeTransformationEngineConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParseWithUnidirectionalTransformationEngine() throws IOException {
        CharacterRepositoryType characterRepository = CharacterRepositoryType.SIMPLE;
        WordConverterType wordConverterType = WordConverterType.IDENTITY;
        CostCalculatorType costCalculatorType = CostCalculatorType.DEFAULT;
        LatticeBuilderType latticeBuilderType = LatticeBuilderType.MINIMAL;
        int maximalContextSize = 5;
        LatticeTransformationEngineConfiguration configuration = new LatticeTransformationEngineConfiguration.Builder()
                .characterRepositoryType(characterRepository)
                .wordConverterType(wordConverterType)
                .costCalculatorType(costCalculatorType)
                .latticeBuilderType(latticeBuilderType)
                .maximalContextSize(maximalContextSize)
                .build();
        AffixType affixType = AffixType.of("AFF");
        LatticeTransformationEngine latticeTransformationEngine = new LatticeTransformationEngine(true, affixType, configuration);
        latticeTransformationEngine.learn(TrainingSet.of(WordPair.of("a", "b")));

        LatticeTransformationEngineMessage latticeTransformationEngineMessage = this.converter.convert(latticeTransformationEngine);
        assertThat(latticeTransformationEngineMessage.getAffixType()).isEqualTo(affixType.toString());
        assertThat(latticeTransformationEngineMessage.getForwardsTrainingSetProcessor()).isNotNull();
        assertThat(latticeTransformationEngineMessage.getForwardsTrainingSetProcessor().getTransformationListsCount()).isEqualTo(1);
        assertThat(latticeTransformationEngineMessage.getForwardsTrainingSetProcessor().getFrequencyMapCount()).isEqualTo(1);
        assertThat(latticeTransformationEngineMessage.getForwardsLatticeBuilder()).isNotNull();
        assertThat(latticeTransformationEngineMessage.getForwardsLatticeBuilder().getLattice().getNodeCount()).isEqualTo(3);
        assertThat(latticeTransformationEngineMessage.getForwardsLatticeBuilder().getLattice().getNeighborhoodCount()).isEqualTo(2);
        assertThat(latticeTransformationEngineMessage.hasBackwardsTrainingSetProcessor()).isFalse();
        assertThat(latticeTransformationEngineMessage.hasBackwardsLatticeBuilder()).isFalse();

        LatticeTransformationEngine result = this.converter.convertBack(latticeTransformationEngineMessage);
        assertThat(result.getAffixType()).isEqualTo(affixType);
        TrainingSetProcessor forwardsTrainingSetProcessor = (TrainingSetProcessor) result.getForwardsTrainingSetProcessor();
        assertThat(forwardsTrainingSetProcessor.getFrequencyMap()).hasSize(1);
        AbstractLatticeBuilder forwardsLatticeBuilder = (AbstractLatticeBuilder) result.getForwardsLatticeBuilder();
        assertThat(forwardsLatticeBuilder.getLattice().size()).isEqualTo(3);
        assertThat(result.getBackwardsTrainingSetProcessor()).isNull();
        assertThat(result.getBackwardsLatticeBuilder()).isNull();

        Serializer<LatticeTransformationEngine, LatticeTransformationEngineMessage> serializer = new Serializer<>(this.converter, latticeTransformationEngine);
        Path file = Files.createTempFile("transformation-engine", "lattice");
        try {
            serializer.serialize(latticeTransformationEngine, file);
            LatticeTransformationEngineMessage resultingLatticeTransformationEngineMessage = this.converter.parse(file);
            assertThat(resultingLatticeTransformationEngineMessage).isEqualTo(latticeTransformationEngineMessage);
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testConvertAndConvertBackAndParseWithBidirectionalTransformationEngine() throws IOException {
        CharacterRepositoryType characterRepository = CharacterRepositoryType.SIMPLE;
        WordConverterType wordConverterType = WordConverterType.IDENTITY;
        CostCalculatorType costCalculatorType = CostCalculatorType.DEFAULT;
        LatticeBuilderType latticeBuilderType = LatticeBuilderType.MINIMAL;
        int maximalContextSize = 5;
        LatticeTransformationEngineConfiguration configuration = new LatticeTransformationEngineConfiguration.Builder()
                .characterRepositoryType(characterRepository)
                .wordConverterType(wordConverterType)
                .costCalculatorType(costCalculatorType)
                .latticeBuilderType(latticeBuilderType)
                .maximalContextSize(maximalContextSize)
                .build();
        AffixType affixType = AffixType.of("AFF");
        LatticeTransformationEngine latticeTransformationEngine = new LatticeTransformationEngine(false, affixType, configuration);
        latticeTransformationEngine.learn(TrainingSet.of(WordPair.of("a", "b")));

        LatticeTransformationEngineMessage latticeTransformationEngineMessage = this.converter.convert(latticeTransformationEngine);
        assertThat(latticeTransformationEngineMessage.getAffixType()).isEqualTo(affixType.toString());
        assertThat(latticeTransformationEngineMessage.getForwardsTrainingSetProcessor()).isNotNull();
        assertThat(latticeTransformationEngineMessage.getForwardsTrainingSetProcessor().getTransformationListsCount()).isEqualTo(1);
        assertThat(latticeTransformationEngineMessage.getForwardsTrainingSetProcessor().getFrequencyMapCount()).isEqualTo(1);
        assertThat(latticeTransformationEngineMessage.getForwardsLatticeBuilder()).isNotNull();
        assertThat(latticeTransformationEngineMessage.getForwardsLatticeBuilder().getLattice().getNodeCount()).isEqualTo(3);
        assertThat(latticeTransformationEngineMessage.getForwardsLatticeBuilder().getLattice().getNeighborhoodCount()).isEqualTo(2);
        assertThat(latticeTransformationEngineMessage.getBackwardsTrainingSetProcessor()).isNotNull();
        assertThat(latticeTransformationEngineMessage.getBackwardsTrainingSetProcessor().getTransformationListsCount()).isEqualTo(1);
        assertThat(latticeTransformationEngineMessage.getBackwardsTrainingSetProcessor().getFrequencyMapCount()).isEqualTo(1);
        assertThat(latticeTransformationEngineMessage.getBackwardsLatticeBuilder()).isNotNull();
        assertThat(latticeTransformationEngineMessage.getBackwardsLatticeBuilder().getLattice().getNodeCount()).isEqualTo(3);
        assertThat(latticeTransformationEngineMessage.getBackwardsLatticeBuilder().getLattice().getNeighborhoodCount()).isEqualTo(2);

        LatticeTransformationEngine result = this.converter.convertBack(latticeTransformationEngineMessage);
        assertThat(result.getAffixType()).isEqualTo(affixType);
        TrainingSetProcessor forwardsTrainingSetProcessor = (TrainingSetProcessor) result.getForwardsTrainingSetProcessor();
        assertThat(forwardsTrainingSetProcessor.getFrequencyMap()).hasSize(1);
        AbstractLatticeBuilder forwardsLatticeBuilder = (AbstractLatticeBuilder) result.getForwardsLatticeBuilder();
        assertThat(forwardsLatticeBuilder.getLattice().size()).isEqualTo(3);
        TrainingSetProcessor backwardsTrainingSetProcessor = (TrainingSetProcessor) result.getBackwardsTrainingSetProcessor();
        assertThat(backwardsTrainingSetProcessor.getFrequencyMap()).hasSize(1);
        AbstractLatticeBuilder backwardsLatticeBuilder = (AbstractLatticeBuilder) result.getBackwardsLatticeBuilder();
        assertThat(backwardsLatticeBuilder.getLattice().size()).isEqualTo(3);

        Serializer<LatticeTransformationEngine, LatticeTransformationEngineMessage> serializer = new Serializer<>(this.converter, latticeTransformationEngine);
        Path file = Files.createTempFile("transformation-engine", "lattice");
        try {
            serializer.serialize(latticeTransformationEngine, file);
            LatticeTransformationEngineMessage resultingLatticeTransformationEngineMessage = this.converter.parse(file);
            assertThat(resultingLatticeTransformationEngineMessage).isEqualTo(latticeTransformationEngineMessage);
        }
        finally {
            Files.delete(file);
        }
    }

}
