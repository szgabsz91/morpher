package com.github.szgabsz91.morpher.transformationengines.lattice.impl.transformationengine;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.api.model.TransformationEngineResponse;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.CharacterRepositoryType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.CostCalculatorType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.LatticeBuilderType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.LatticeTransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.WordConverterType;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.ILatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.MinimalLatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.ITrainingSetProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.trainingsetprocessor.TrainingSetProcessor;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.LatticeTransformationEngineMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LatticeTransformationEngineTest {

    private AffixType affixType;
    private LatticeTransformationEngineConfiguration configuration;
    private LatticeTransformationEngine unidirectionalLatticeTransformationEngine;
    private LatticeTransformationEngine bidirectionalLatticeTransformationEngine;

    @BeforeEach
    public void setUp() {
        this.affixType = AffixType.of("AFF");
        this.configuration = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .wordConverterType(WordConverterType.IDENTITY)
                .unlimitedMaximalContextSize()
                .build();
        this.unidirectionalLatticeTransformationEngine = new LatticeTransformationEngine(true, this.affixType, configuration);
        this.bidirectionalLatticeTransformationEngine = new LatticeTransformationEngine(false, this.affixType, configuration);
    }

    @Test
    public void testConstructorAndGettersWithUnidirectionalTransformationEngine() {
        assertThat(this.unidirectionalLatticeTransformationEngine.isUnidirectional()).isTrue();
        assertThat(this.unidirectionalLatticeTransformationEngine.getAffixType()).isEqualTo(this.affixType);
        assertThat(this.unidirectionalLatticeTransformationEngine.getForwardsTrainingSetProcessor()).isNotNull();
        assertThat(this.unidirectionalLatticeTransformationEngine.getForwardsLatticeBuilder()).isNotNull();
        assertThat(this.unidirectionalLatticeTransformationEngine.getBackwardsTrainingSetProcessor()).isNull();
        assertThat(this.unidirectionalLatticeTransformationEngine.getBackwardsLatticeBuilder()).isNull();
    }

    @Test
    public void testConstructorAndGettersWithBidirectionalTransformationEngine() {
        assertThat(this.bidirectionalLatticeTransformationEngine.isUnidirectional()).isFalse();
        assertThat(this.bidirectionalLatticeTransformationEngine.getAffixType()).isEqualTo(this.affixType);
        assertThat(this.bidirectionalLatticeTransformationEngine.getForwardsTrainingSetProcessor()).isNotNull();
        assertThat(this.bidirectionalLatticeTransformationEngine.getForwardsLatticeBuilder()).isNotNull();
        assertThat(this.bidirectionalLatticeTransformationEngine.getBackwardsTrainingSetProcessor()).isNotNull();
        assertThat(this.bidirectionalLatticeTransformationEngine.getBackwardsLatticeBuilder()).isNotNull();
    }

    @Test
    public void testUnidirectional() {
        ITrainingSetProcessor forwardsTrainingSetProcessor = new TrainingSetProcessor(null);
        ILatticeBuilder forwardsLatticeBuilder = new MinimalLatticeBuilder(null, null);
        LatticeTransformationEngine latticeTransformationEngine = LatticeTransformationEngine.unidirectional(this.affixType, forwardsTrainingSetProcessor, forwardsLatticeBuilder);
        assertThat(latticeTransformationEngine.isUnidirectional()).isTrue();
        assertThat(latticeTransformationEngine.getAffixType()).isEqualTo(this.affixType);
        assertThat(latticeTransformationEngine.getForwardsTrainingSetProcessor()).isSameAs(forwardsTrainingSetProcessor);
        assertThat(latticeTransformationEngine.getForwardsLatticeBuilder()).isSameAs(forwardsLatticeBuilder);
        assertThat(latticeTransformationEngine.getBackwardsTrainingSetProcessor()).isNull();
        assertThat(latticeTransformationEngine.getBackwardsLatticeBuilder()).isNull();
    }

    @Test
    public void testBidirectional() {
        ITrainingSetProcessor forwardsTrainingSetProcessor = new TrainingSetProcessor(null);
        ILatticeBuilder forwardsLatticeBuilder = new MinimalLatticeBuilder(null, null);
        ITrainingSetProcessor backwardsTrainingSetProcessor = new TrainingSetProcessor(null);
        ILatticeBuilder backwardsLatticeBuilder = new MinimalLatticeBuilder(null, null);
        LatticeTransformationEngine latticeTransformationEngine = LatticeTransformationEngine.bidirectional(this.affixType, forwardsTrainingSetProcessor, forwardsLatticeBuilder, backwardsTrainingSetProcessor, backwardsLatticeBuilder);
        assertThat(latticeTransformationEngine.isUnidirectional()).isFalse();
        assertThat(latticeTransformationEngine.getAffixType()).isEqualTo(this.affixType);
        assertThat(latticeTransformationEngine.getForwardsTrainingSetProcessor()).isSameAs(forwardsTrainingSetProcessor);
        assertThat(latticeTransformationEngine.getForwardsLatticeBuilder()).isSameAs(forwardsLatticeBuilder);
        assertThat(latticeTransformationEngine.getBackwardsTrainingSetProcessor()).isSameAs(backwardsTrainingSetProcessor);
        assertThat(latticeTransformationEngine.getBackwardsLatticeBuilder()).isSameAs(backwardsLatticeBuilder);
    }

    @Test
    public void testSizeWithUnidirectionalTransformationEngine() {
        int result = this.unidirectionalLatticeTransformationEngine.size();
        assertThat(result).isEqualTo(this.unidirectionalLatticeTransformationEngine.getForwardsLatticeBuilder().getLattice().size());
    }

    @Test
    public void testSizeWithBidirectionalTransformationEngine() {
        int expected = (this.bidirectionalLatticeTransformationEngine.getForwardsLatticeBuilder().getLattice().size() + this.bidirectionalLatticeTransformationEngine.getBackwardsLatticeBuilder().getLattice().size()) / 2;
        int result = this.bidirectionalLatticeTransformationEngine.size();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testTransform() {
        WordPair wordPair = WordPair.of("abc", "adc");
        this.unidirectionalLatticeTransformationEngine.learn(TrainingSet.of(wordPair));
        Word input = wordPair.getLeftWord();
        TransformationEngineResponse expected = TransformationEngineResponse.singleton(wordPair.getRightWord());
        Optional<TransformationEngineResponse> response = this.unidirectionalLatticeTransformationEngine.transform(input);
        assertThat(response).hasValue(expected);
    }

    @Test
    public void testTransformWithUnknownWord() {
        WordPair wordPair = WordPair.of("abc", "adc");
        this.unidirectionalLatticeTransformationEngine.learn(TrainingSet.of(wordPair));
        Word input = Word.of("xxx");
        Optional<TransformationEngineResponse> response = this.unidirectionalLatticeTransformationEngine.transform(input);
        assertThat(response).isEmpty();
    }

    @Test
    public void testTransformBackWithUnidirectionalTransformationEngine() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.unidirectionalLatticeTransformationEngine.transformBack(null));
        assertThat(exception).hasMessage("Unidirectional lattice can only transform forwards");
    }

    @Test
    public void testTransformBackWithBidirectionalTransformationEngine() {
        WordPair wordPair = WordPair.of("abc", "adc");
        this.bidirectionalLatticeTransformationEngine.learn(TrainingSet.of(wordPair));
        Word input = wordPair.getRightWord();
        TransformationEngineResponse expected = TransformationEngineResponse.singleton(wordPair.getLeftWord());
        Optional<TransformationEngineResponse> response = this.bidirectionalLatticeTransformationEngine.transformBack(input);
        assertThat(response).hasValue(expected);
    }

    @Test
    public void testTransformBackWithBidirectionalTransformationEngineAndUnknownWord() {
        WordPair wordPair = WordPair.of("abc", "adc");
        this.bidirectionalLatticeTransformationEngine.learn(TrainingSet.of(wordPair));
        Word input = Word.of("xxx");
        Optional<TransformationEngineResponse> response = this.bidirectionalLatticeTransformationEngine.transformBack(input);
        assertThat(response).isEmpty();
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        this.unidirectionalLatticeTransformationEngine.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Any message = Any.pack(this.unidirectionalLatticeTransformationEngine.toMessage());

        LatticeTransformationEngine latticeTransformationEngine = new LatticeTransformationEngine(true, null, this.configuration);
        latticeTransformationEngine.fromMessage(message);

        assertThat(latticeTransformationEngine.getAffixType()).isEqualTo(this.affixType);
        assertThat(latticeTransformationEngine.transform(Word.of("a"))).hasValue(TransformationEngineResponse.singleton(Word.of("b")));
        assertThat(latticeTransformationEngine.transform(Word.of("c"))).hasValue(TransformationEngineResponse.singleton(Word.of("d")));
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(LatticeTransformationEngineMessage.newBuilder().build()));
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> this.unidirectionalLatticeTransformationEngine.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not a LatticeTransformationEngineMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        this.unidirectionalLatticeTransformationEngine.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Path file = Files.createTempFile("transformation-engine", "lattice");

        try {
            this.unidirectionalLatticeTransformationEngine.saveTo(file);
            LatticeTransformationEngine latticeTransformationEngine = new LatticeTransformationEngine(false, null, this.configuration);
            latticeTransformationEngine.loadFrom(file);
            assertThat(latticeTransformationEngine.isUnidirectional()).isEqualTo(this.unidirectionalLatticeTransformationEngine.isUnidirectional());
            assertThat(latticeTransformationEngine.getAffixType()).isEqualTo(this.affixType);
            assertThat(latticeTransformationEngine.getForwardsTrainingSetProcessor()).isNotNull();
            assertThat(latticeTransformationEngine.getForwardsLatticeBuilder()).isNotNull();
            assertThat(latticeTransformationEngine.getBackwardsTrainingSetProcessor()).isNull();
            assertThat(latticeTransformationEngine.getBackwardsLatticeBuilder()).isNull();
            assertThat(latticeTransformationEngine.transform(Word.of("a"))).hasValue(TransformationEngineResponse.singleton(Word.of("b")));
            assertThat(latticeTransformationEngine.transform(Word.of("c"))).hasValue(TransformationEngineResponse.singleton(Word.of("d")));
        }
        finally {
            Files.delete(file);
        }
    }

}
