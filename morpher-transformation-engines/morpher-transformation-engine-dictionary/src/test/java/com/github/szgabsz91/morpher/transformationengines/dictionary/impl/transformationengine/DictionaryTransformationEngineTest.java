package com.github.szgabsz91.morpher.transformationengines.dictionary.impl.transformationengine;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.api.model.TransformationEngineResponse;
import com.github.szgabsz91.morpher.transformationengines.dictionary.protocolbuffers.DictionaryTransformationEngineMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DictionaryTransformationEngineTest {

    private AffixType affixType;
    private DictionaryTransformationEngine dictionaryTransformationEngine;

    @BeforeEach
    public void setUp() {
        this.affixType = AffixType.of("AFF");
        this.dictionaryTransformationEngine = new DictionaryTransformationEngine(this.affixType);
    }

    @Test
    public void testConstructor() {
        assertThat(this.dictionaryTransformationEngine.getAffixType()).isEqualTo(this.affixType);
        assertThat(this.dictionaryTransformationEngine.getForwardsDictionary()).isEmpty();
        assertThat(this.dictionaryTransformationEngine.getBackwardsDictionary()).isEmpty();
    }

    @Test
    public void testGetForwardsDictionary() {
        this.dictionaryTransformationEngine.learn(TrainingSet.of(WordPair.of("a", "b")));
        Map<Word, Word> result = this.dictionaryTransformationEngine.getForwardsDictionary();
        assertThat(result).hasSize(1);
    }

    @Test
    public void testGetBackwardsDictionary() {
        this.dictionaryTransformationEngine.learn(TrainingSet.of(WordPair.of("a", "b")));
        Map<Word, Word> result = this.dictionaryTransformationEngine.getBackwardsDictionary();
        assertThat(result).hasSize(1);
    }

    @Test
    public void testSize() {
        this.dictionaryTransformationEngine.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        assertThat(this.dictionaryTransformationEngine.size()).isEqualTo(2);
    }

    @Test
    public void testTransform() {
        WordPair wordPair = WordPair.of("a", "b");
        this.dictionaryTransformationEngine.learn(TrainingSet.of(wordPair));
        Optional<TransformationEngineResponse> response = this.dictionaryTransformationEngine.transform(wordPair.getLeftWord());
        assertThat(response).hasValue(TransformationEngineResponse.singleton(wordPair.getRightWord()));
        Word unknownWord = Word.of("c");
        assertThat(this.dictionaryTransformationEngine.transform(unknownWord)).isEmpty();
    }

    @Test
    public void testTransformBack() {
        WordPair wordPair = WordPair.of("a", "b");
        this.dictionaryTransformationEngine.learn(TrainingSet.of(wordPair));
        Optional<TransformationEngineResponse> response = this.dictionaryTransformationEngine.transformBack(wordPair.getRightWord());
        assertThat(response).hasValue(TransformationEngineResponse.singleton(wordPair.getLeftWord()));
        Word unknownWord = Word.of("c");
        assertThat(this.dictionaryTransformationEngine.transformBack(unknownWord)).isEmpty();
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        this.dictionaryTransformationEngine.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Any message = Any.pack(this.dictionaryTransformationEngine.toMessage());

        DictionaryTransformationEngine dictionaryTransformationEngine = new DictionaryTransformationEngine(null);
        dictionaryTransformationEngine.fromMessage(message);

        assertThat(dictionaryTransformationEngine.getAffixType()).isEqualTo(this.affixType);
        assertThat(dictionaryTransformationEngine.transform(Word.of("a"))).hasValue(TransformationEngineResponse.singleton(Word.of("b")));
        assertThat(dictionaryTransformationEngine.transform(Word.of("c"))).hasValue(TransformationEngineResponse.singleton(Word.of("d")));
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(DictionaryTransformationEngineMessage.newBuilder().build()));
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> this.dictionaryTransformationEngine.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not an DictionaryTransformationEngineMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        this.dictionaryTransformationEngine.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Path file = Files.createTempFile("transformation-engine", "dictionary");

        try {
            this.dictionaryTransformationEngine.saveTo(file);
            DictionaryTransformationEngine dictionaryTransformationEngine = new DictionaryTransformationEngine(null);
            dictionaryTransformationEngine.loadFrom(file);
            assertThat(dictionaryTransformationEngine.getAffixType()).isEqualTo(this.affixType);
            assertThat(dictionaryTransformationEngine.getForwardsDictionary()).hasSize(2);
        }
        finally {
            Files.delete(file);
        }
    }

}
