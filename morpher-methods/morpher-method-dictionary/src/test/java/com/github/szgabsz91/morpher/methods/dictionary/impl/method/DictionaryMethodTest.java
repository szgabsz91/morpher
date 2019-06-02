package com.github.szgabsz91.morpher.methods.dictionary.impl.method;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.github.szgabsz91.morpher.methods.dictionary.protocolbuffers.DictionaryMethodMessage;
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

public class DictionaryMethodTest {

    private AffixType affixType;
    private DictionaryMethod dictionaryMethod;

    @BeforeEach
    public void setUp() {
        this.affixType = AffixType.of("AFF");
        this.dictionaryMethod = new DictionaryMethod(this.affixType);
    }

    @Test
    public void testConstructor() {
        assertThat(this.dictionaryMethod.getAffixType()).isEqualTo(this.affixType);
        assertThat(this.dictionaryMethod.getInflectionDictionary()).isEmpty();
        assertThat(this.dictionaryMethod.getLemmatizationDictionary()).isEmpty();
    }

    @Test
    public void testGetInflectionDictionary() {
        this.dictionaryMethod.learn(TrainingSet.of(WordPair.of("a", "b")));
        Map<Word, Word> result = this.dictionaryMethod.getInflectionDictionary();
        assertThat(result).hasSize(1);
    }

    @Test
    public void testGetLemmatizationDictionary() {
        this.dictionaryMethod.learn(TrainingSet.of(WordPair.of("a", "b")));
        Map<Word, Word> result = this.dictionaryMethod.getLemmatizationDictionary();
        assertThat(result).hasSize(1);
    }

    @Test
    public void testSize() {
        this.dictionaryMethod.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        assertThat(this.dictionaryMethod.size()).isEqualTo(2);
    }

    @Test
    public void testInflect() {
        WordPair wordPair = WordPair.of("a", "b");
        this.dictionaryMethod.learn(TrainingSet.of(wordPair));
        Optional<MethodResponse> response = this.dictionaryMethod.inflect(wordPair.getLeftWord());
        assertThat(response).hasValue(MethodResponse.singleton(wordPair.getRightWord()));
        Word unknownWord = Word.of("c");
        assertThat(this.dictionaryMethod.inflect(unknownWord)).isEmpty();
    }

    @Test
    public void testLemmatize() {
        WordPair wordPair = WordPair.of("a", "b");
        this.dictionaryMethod.learn(TrainingSet.of(wordPair));
        Optional<MethodResponse> response = this.dictionaryMethod.lemmatize(wordPair.getRightWord());
        assertThat(response).hasValue(MethodResponse.singleton(wordPair.getLeftWord()));
        Word unknownWord = Word.of("c");
        assertThat(this.dictionaryMethod.lemmatize(unknownWord)).isEmpty();
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        this.dictionaryMethod.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Any message = Any.pack(this.dictionaryMethod.toMessage());

        DictionaryMethod dictionaryMethod = new DictionaryMethod(null);
        dictionaryMethod.fromMessage(message);

        assertThat(dictionaryMethod.getAffixType()).isEqualTo(this.affixType);
        assertThat(dictionaryMethod.inflect(Word.of("a"))).hasValue(MethodResponse.singleton(Word.of("b")));
        assertThat(dictionaryMethod.inflect(Word.of("c"))).hasValue(MethodResponse.singleton(Word.of("d")));
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(DictionaryMethodMessage.newBuilder().build()));
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> this.dictionaryMethod.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not an DictionaryMethodMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        this.dictionaryMethod.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Path file = Files.createTempFile("morpher", "dictionary");

        try {
            this.dictionaryMethod.saveTo(file);
            DictionaryMethod dictionaryMethod = new DictionaryMethod(null);
            dictionaryMethod.loadFrom(file);
            assertThat(dictionaryMethod.getAffixType()).isEqualTo(this.affixType);
            assertThat(dictionaryMethod.getInflectionDictionary()).hasSize(2);
        }
        finally {
            Files.delete(file);
        }
    }

}
