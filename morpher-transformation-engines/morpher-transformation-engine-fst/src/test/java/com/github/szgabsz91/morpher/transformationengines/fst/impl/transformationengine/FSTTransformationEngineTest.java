package com.github.szgabsz91.morpher.transformationengines.fst.impl.transformationengine;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.api.model.TransformationEngineResponse;
import com.github.szgabsz91.morpher.transformationengines.fst.protocolbuffers.FSTTransformationEngineMessage;
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

public class FSTTransformationEngineTest {

    private AffixType affixType;
    private FSTTransformationEngine fstTransformationEngine;

    @BeforeEach
    public void setUp() {
        this.affixType = AffixType.of("AFF");
        this.fstTransformationEngine = new FSTTransformationEngine(false, this.affixType);
    }

    @Test
    public void testIsUnidirectionalWithUnidirectionalTransformationEngine() {
        FSTTransformationEngine fstTransformationEngine = new FSTTransformationEngine(true, this.affixType);
        assertThat(fstTransformationEngine.isUnidirectional()).isTrue();
        assertThat(fstTransformationEngine.getAffixType()).isEqualTo(this.affixType);
    }

    @Test
    public void testIsUnidirectionalWithBidirectionalTransformationEngine() {
        assertThat(this.fstTransformationEngine.isUnidirectional()).isFalse();
    }

    @Test
    public void testGetWordPairs() {
        assertThat(this.fstTransformationEngine.getWordPairs()).isEmpty();
        fstTransformationEngine.learn(TrainingSet.of(WordPair.of("a", "b")));
        assertThat(this.fstTransformationEngine.getWordPairs()).hasSize(1);
    }

    @Test
    public void testSizeWithUnidirectionalTransformationEngine() {
        FSTTransformationEngine fstTransformationEngine = new FSTTransformationEngine(true, this.affixType);
        fstTransformationEngine.learn(TrainingSet.of(WordPair.of("a", "b")));
        int result = fstTransformationEngine.size();
        assertThat(result).isEqualTo(2);
    }

    @Test
    public void testSizeWithBidirectionalTransformationEngine() {
        this.fstTransformationEngine.learn(TrainingSet.of(WordPair.of("a", "b")));
        int result = this.fstTransformationEngine.size();
        assertThat(result).isEqualTo(2);
    }

    @Test
    public void testTransform() {
        WordPair wordPair = WordPair.of("a", "b");
        this.fstTransformationEngine.learn(TrainingSet.of(wordPair));
        Optional<TransformationEngineResponse> response = this.fstTransformationEngine.transform(wordPair.getLeftWord());
        assertThat(response).hasValue(TransformationEngineResponse.singleton(wordPair.getRightWord()));
    }

    @Test
    public void testTransformBackWithUnidirectionalTransformationEngine() {
        FSTTransformationEngine fstTransformationEngine = new FSTTransformationEngine(true, this.affixType);
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> fstTransformationEngine.transformBack(Word.of("a")));
        assertThat(exception).hasMessage("Unidirectional FST transformation engine can only transform words forwards but not backwards");
    }

    @Test
    public void testTransformBackWithBidirectionalTransformationEngine() {
        WordPair wordPair = WordPair.of("a", "b");
        this.fstTransformationEngine.learn(TrainingSet.of(wordPair));
        Optional<TransformationEngineResponse> response = this.fstTransformationEngine.transformBack(wordPair.getRightWord());
        assertThat(response).hasValue(TransformationEngineResponse.singleton(wordPair.getLeftWord()));
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        this.fstTransformationEngine.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Any message = Any.pack(this.fstTransformationEngine.toMessage());

        FSTTransformationEngine fstTransformationEngine = new FSTTransformationEngine(true, null);
        fstTransformationEngine.fromMessage(message);

        assertThat(fstTransformationEngine.getAffixType()).isEqualTo(this.affixType);
        assertThat(fstTransformationEngine.transform(Word.of("a"))).hasValue(TransformationEngineResponse.singleton(Word.of("b")));
        assertThat(fstTransformationEngine.transform(Word.of("c"))).hasValue(TransformationEngineResponse.singleton(Word.of("d")));
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(FSTTransformationEngineMessage.newBuilder().build()));
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> this.fstTransformationEngine.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not an FSTTransformationEngineMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        this.fstTransformationEngine.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Path file = Files.createTempFile("transformation-engine", "fst");

        try {
            this.fstTransformationEngine.saveTo(file);
            FSTTransformationEngine fstTransformationEngine = new FSTTransformationEngine(false, null);
            fstTransformationEngine.loadFrom(file);
            assertThat(fstTransformationEngine.isUnidirectional()).isEqualTo(this.fstTransformationEngine.isUnidirectional());
            assertThat(fstTransformationEngine.getAffixType()).isEqualTo(this.affixType);
            assertThat(fstTransformationEngine.getWordPairs()).isEqualTo(this.fstTransformationEngine.getWordPairs());
        }
        finally {
            Files.delete(file);
        }
    }

}
