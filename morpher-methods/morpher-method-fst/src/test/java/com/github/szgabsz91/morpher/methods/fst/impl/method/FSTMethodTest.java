package com.github.szgabsz91.morpher.methods.fst.impl.method;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.github.szgabsz91.morpher.methods.fst.protocolbuffers.FSTMethodMessage;
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

public class FSTMethodTest {

    private AffixType affixType;
    private FSTMethod fstMethod;

    @BeforeEach
    public void setUp() {
        this.affixType = AffixType.of("AFF");
        this.fstMethod = new FSTMethod(false, this.affixType);
    }

    @Test
    public void testIsUnidirectionalWithUnidirectionalMethod() {
        FSTMethod fstMethod = new FSTMethod(true, this.affixType);
        assertThat(fstMethod.isUnidirectional()).isTrue();
        assertThat(fstMethod.getAffixType()).isEqualTo(this.affixType);
    }

    @Test
    public void testIsUnidirectionalWithBidirectionalMethod() {
        assertThat(this.fstMethod.isUnidirectional()).isFalse();
    }

    @Test
    public void testGetWordPairs() {
        assertThat(this.fstMethod.getWordPairs()).isEmpty();
        fstMethod.learn(TrainingSet.of(WordPair.of("a", "b")));
        assertThat(this.fstMethod.getWordPairs()).hasSize(1);
    }

    @Test
    public void testSizeWithUnidirectionalMethod() {
        FSTMethod fstMethod = new FSTMethod(true, this.affixType);
        fstMethod.learn(TrainingSet.of(WordPair.of("a", "b")));
        int result = fstMethod.size();
        assertThat(result).isEqualTo(2);
    }

    @Test
    public void testSizeWithBidirectionalMethod() {
        this.fstMethod.learn(TrainingSet.of(WordPair.of("a", "b")));
        int result = this.fstMethod.size();
        assertThat(result).isEqualTo(2);
    }

    @Test
    public void testInflect() {
        WordPair wordPair = WordPair.of("a", "b");
        this.fstMethod.learn(TrainingSet.of(wordPair));
        Optional<MethodResponse> response = this.fstMethod.inflect(wordPair.getLeftWord());
        assertThat(response).hasValue(MethodResponse.singleton(wordPair.getRightWord()));
    }

    @Test
    public void testLemmatizeWithUnidirectionalMethod() {
        FSTMethod fstMethod = new FSTMethod(true, this.affixType);
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> fstMethod.lemmatize(Word.of("a")));
        assertThat(exception).hasMessage("Unidirectional FST method can only inflect but not lemmatize");
    }

    @Test
    public void testLemmatizeWithBidirectionalMethod() {
        WordPair wordPair = WordPair.of("a", "b");
        this.fstMethod.learn(TrainingSet.of(wordPair));
        Optional<MethodResponse> response = this.fstMethod.lemmatize(wordPair.getRightWord());
        assertThat(response).hasValue(MethodResponse.singleton(wordPair.getLeftWord()));
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        this.fstMethod.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Any message = Any.pack(this.fstMethod.toMessage());

        FSTMethod fstMethod = new FSTMethod(true, null);
        fstMethod.fromMessage(message);

        assertThat(fstMethod.getAffixType()).isEqualTo(this.affixType);
        assertThat(fstMethod.inflect(Word.of("a"))).hasValue(MethodResponse.singleton(Word.of("b")));
        assertThat(fstMethod.inflect(Word.of("c"))).hasValue(MethodResponse.singleton(Word.of("d")));
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(FSTMethodMessage.newBuilder().build()));
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> this.fstMethod.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not an FSTMethodMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        this.fstMethod.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Path file = Files.createTempFile("morpher", "fst");

        try {
            this.fstMethod.saveTo(file);
            FSTMethod fstMethod = new FSTMethod(false, null);
            fstMethod.loadFrom(file);
            assertThat(fstMethod.isUnidirectional()).isEqualTo(this.fstMethod.isUnidirectional());
            assertThat(fstMethod.getAffixType()).isEqualTo(this.affixType);
            assertThat(fstMethod.getWordPairs()).isEqualTo(this.fstMethod.getWordPairs());
        }
        finally {
            Files.delete(file);
        }
    }

}
