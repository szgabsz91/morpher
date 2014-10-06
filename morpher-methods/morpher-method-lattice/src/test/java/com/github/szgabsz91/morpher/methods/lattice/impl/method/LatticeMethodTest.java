package com.github.szgabsz91.morpher.methods.lattice.impl.method;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.github.szgabsz91.morpher.methods.lattice.config.CharacterRepositoryType;
import com.github.szgabsz91.morpher.methods.lattice.config.CostCalculatorType;
import com.github.szgabsz91.morpher.methods.lattice.config.LatticeBuilderType;
import com.github.szgabsz91.morpher.methods.lattice.config.LatticeMethodConfiguration;
import com.github.szgabsz91.morpher.methods.lattice.config.WordConverterType;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.ILatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.MinimalLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.ITrainingSetProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.TrainingSetProcessor;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.LatticeMethodMessage;
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

public class LatticeMethodTest {

    private AffixType affixType;
    private LatticeMethodConfiguration configuration;
    private LatticeMethod unidirectionalLatticeMethod;
    private LatticeMethod bidirectionalLatticeMethod;

    @BeforeEach
    public void setUp() {
        this.affixType = AffixType.of("AFF");
        this.configuration = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .wordConverterType(WordConverterType.IDENTITY)
                .unlimitedMaximalContextSize()
                .build();
        this.unidirectionalLatticeMethod = new LatticeMethod(true, this.affixType, configuration);
        this.bidirectionalLatticeMethod = new LatticeMethod(false, this.affixType, configuration);
    }

    @Test
    public void testConstructorAndGettersWithUnidirectionalMethod() {
        assertThat(this.unidirectionalLatticeMethod.isUnidirectional()).isTrue();
        assertThat(this.unidirectionalLatticeMethod.getAffixType()).isEqualTo(this.affixType);
        assertThat(this.unidirectionalLatticeMethod.getInflectionTrainingSetProcessor()).isNotNull();
        assertThat(this.unidirectionalLatticeMethod.getInflectionLatticeBuilder()).isNotNull();
        assertThat(this.unidirectionalLatticeMethod.getLemmatizationTrainingSetProcessor()).isNull();
        assertThat(this.unidirectionalLatticeMethod.getLemmatizationLatticeBuilder()).isNull();
    }

    @Test
    public void testConstructorAndGettersWithBidirectionalMethod() {
        assertThat(this.bidirectionalLatticeMethod.isUnidirectional()).isFalse();
        assertThat(this.bidirectionalLatticeMethod.getAffixType()).isEqualTo(this.affixType);
        assertThat(this.bidirectionalLatticeMethod.getInflectionTrainingSetProcessor()).isNotNull();
        assertThat(this.bidirectionalLatticeMethod.getInflectionLatticeBuilder()).isNotNull();
        assertThat(this.bidirectionalLatticeMethod.getLemmatizationTrainingSetProcessor()).isNotNull();
        assertThat(this.bidirectionalLatticeMethod.getLemmatizationLatticeBuilder()).isNotNull();
    }

    @Test
    public void testUnidirectional() {
        ITrainingSetProcessor inflectionTrainingSetProcessor = new TrainingSetProcessor(null);
        ILatticeBuilder inflectionLatticeBuilder = new MinimalLatticeBuilder(null, null);
        LatticeMethod latticeMethod = LatticeMethod.unidirectional(this.affixType, inflectionTrainingSetProcessor, inflectionLatticeBuilder);
        assertThat(latticeMethod.isUnidirectional()).isTrue();
        assertThat(latticeMethod.getAffixType()).isEqualTo(this.affixType);
        assertThat(latticeMethod.getInflectionTrainingSetProcessor()).isSameAs(inflectionTrainingSetProcessor);
        assertThat(latticeMethod.getInflectionLatticeBuilder()).isSameAs(inflectionLatticeBuilder);
        assertThat(latticeMethod.getLemmatizationTrainingSetProcessor()).isNull();
        assertThat(latticeMethod.getLemmatizationLatticeBuilder()).isNull();
    }

    @Test
    public void testBidirectional() {
        ITrainingSetProcessor inflectionTrainingSetProcessor = new TrainingSetProcessor(null);
        ILatticeBuilder inflectionLatticeBuilder = new MinimalLatticeBuilder(null, null);
        ITrainingSetProcessor lemmatizationTrainingSetProcessor = new TrainingSetProcessor(null);
        ILatticeBuilder lemmatizationLatticeBuilder = new MinimalLatticeBuilder(null, null);
        LatticeMethod latticeMethod = LatticeMethod.bidirectional(this.affixType, inflectionTrainingSetProcessor, inflectionLatticeBuilder, lemmatizationTrainingSetProcessor, lemmatizationLatticeBuilder);
        assertThat(latticeMethod.isUnidirectional()).isFalse();
        assertThat(latticeMethod.getAffixType()).isEqualTo(this.affixType);
        assertThat(latticeMethod.getInflectionTrainingSetProcessor()).isSameAs(inflectionTrainingSetProcessor);
        assertThat(latticeMethod.getInflectionLatticeBuilder()).isSameAs(inflectionLatticeBuilder);
        assertThat(latticeMethod.getLemmatizationTrainingSetProcessor()).isSameAs(lemmatizationTrainingSetProcessor);
        assertThat(latticeMethod.getLemmatizationLatticeBuilder()).isSameAs(lemmatizationLatticeBuilder);
    }

    @Test
    public void testSizeWithUnidirectionalMethod() {
        int result = this.unidirectionalLatticeMethod.size();
        assertThat(result).isEqualTo(this.unidirectionalLatticeMethod.getInflectionLatticeBuilder().getLattice().size());
    }

    @Test
    public void testSizeWithBidirectionalMethod() {
        int expected = (this.bidirectionalLatticeMethod.getInflectionLatticeBuilder().getLattice().size() + this.bidirectionalLatticeMethod.getLemmatizationLatticeBuilder().getLattice().size()) / 2;
        int result = this.bidirectionalLatticeMethod.size();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testInflect() {
        WordPair wordPair = WordPair.of("abc", "adc");
        this.unidirectionalLatticeMethod.learn(TrainingSet.of(wordPair));
        Word input = wordPair.getLeftWord();
        MethodResponse expected = MethodResponse.singleton(wordPair.getRightWord());
        Optional<MethodResponse> response = this.unidirectionalLatticeMethod.inflect(input);
        assertThat(response).hasValue(expected);
    }

    @Test
    public void testInflectWithUnknownWord() {
        WordPair wordPair = WordPair.of("abc", "adc");
        this.unidirectionalLatticeMethod.learn(TrainingSet.of(wordPair));
        Word input = Word.of("xxx");
        Optional<MethodResponse> response = this.unidirectionalLatticeMethod.inflect(input);
        assertThat(response).isEmpty();
    }

    @Test
    public void testLemmatizeWithUnidirectionalMethod() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.unidirectionalLatticeMethod.lemmatize(null));
        assertThat(exception).hasMessage("Unidirectional Lattice method can only inflect but not lemmatize");
    }

    @Test
    public void testLemmatizeWithBidirectionalMethod() {
        WordPair wordPair = WordPair.of("abc", "adc");
        this.bidirectionalLatticeMethod.learn(TrainingSet.of(wordPair));
        Word input = wordPair.getRightWord();
        MethodResponse expected = MethodResponse.singleton(wordPair.getLeftWord());
        Optional<MethodResponse> response = this.bidirectionalLatticeMethod.lemmatize(input);
        assertThat(response).hasValue(expected);
    }

    @Test
    public void testLemmatizeWithBidirectionalMethodAndUnknownWord() {
        WordPair wordPair = WordPair.of("abc", "adc");
        this.bidirectionalLatticeMethod.learn(TrainingSet.of(wordPair));
        Word input = Word.of("xxx");
        Optional<MethodResponse> response = this.bidirectionalLatticeMethod.lemmatize(input);
        assertThat(response).isEmpty();
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        this.unidirectionalLatticeMethod.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Any message = Any.pack(this.unidirectionalLatticeMethod.toMessage());

        LatticeMethod latticeMethod = new LatticeMethod(true, null, this.configuration);
        latticeMethod.fromMessage(message);

        assertThat(latticeMethod.getAffixType()).isEqualTo(this.affixType);
        assertThat(latticeMethod.inflect(Word.of("a"))).hasValue(MethodResponse.singleton(Word.of("b")));
        assertThat(latticeMethod.inflect(Word.of("c"))).hasValue(MethodResponse.singleton(Word.of("d")));
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(LatticeMethodMessage.newBuilder().build()));
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> this.unidirectionalLatticeMethod.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not a LatticeMethodMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        this.unidirectionalLatticeMethod.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Path file = Files.createTempFile("morpher", "lattice");

        try {
            this.unidirectionalLatticeMethod.saveTo(file);
            LatticeMethod latticeMethod = new LatticeMethod(false, null, this.configuration);
            latticeMethod.loadFrom(file);
            assertThat(latticeMethod.isUnidirectional()).isEqualTo(this.unidirectionalLatticeMethod.isUnidirectional());
            assertThat(latticeMethod.getAffixType()).isEqualTo(this.affixType);
            assertThat(latticeMethod.getInflectionTrainingSetProcessor()).isNotNull();
            assertThat(latticeMethod.getInflectionLatticeBuilder()).isNotNull();
            assertThat(latticeMethod.getLemmatizationTrainingSetProcessor()).isNull();
            assertThat(latticeMethod.getLemmatizationLatticeBuilder()).isNull();
            assertThat(latticeMethod.inflect(Word.of("a"))).hasValue(MethodResponse.singleton(Word.of("b")));
            assertThat(latticeMethod.inflect(Word.of("c"))).hasValue(MethodResponse.singleton(Word.of("d")));
        }
        finally {
            Files.delete(file);
        }
    }

}
