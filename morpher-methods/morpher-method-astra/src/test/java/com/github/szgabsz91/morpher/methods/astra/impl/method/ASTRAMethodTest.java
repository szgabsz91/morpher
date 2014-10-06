package com.github.szgabsz91.morpher.methods.astra.impl.method;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.github.szgabsz91.morpher.methods.astra.config.ASTRAMethodConfiguration;
import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.ASTRAMethodMessage;
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

public class ASTRAMethodTest {

    private AffixType affixType;
    private ASTRAMethodConfiguration configuration;
    private ASTRAMethod unidirectionalASTRAMethod;
    private ASTRAMethod bidirectionalASTRAMethod;

    @BeforeEach
    public void setUp() {
        this.affixType = AffixType.of("AFF");
        this.configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        this.unidirectionalASTRAMethod = new ASTRAMethod(true, this.affixType, configuration);
        this.bidirectionalASTRAMethod = new ASTRAMethod(false, this.affixType, configuration);
    }

    @Test
    public void testConstructorAndGettersWithUnidirectionalMethod() {
        assertThat(this.unidirectionalASTRAMethod.getAffixType()).isEqualTo(this.affixType);
        assertThat(this.unidirectionalASTRAMethod.isUnidirectional()).isTrue();
        assertThat(this.unidirectionalASTRAMethod.getAstra()).isNotNull();
    }

    @Test
    public void testConstructorAndGettersWithBidirectionalMethod() {
        assertThat(this.bidirectionalASTRAMethod.getAffixType()).isEqualTo(this.affixType);
        assertThat(this.bidirectionalASTRAMethod.isUnidirectional()).isFalse();
        assertThat(this.bidirectionalASTRAMethod.getAstra()).isNotNull();
    }

    @Test
    public void testSize() {
        this.unidirectionalASTRAMethod.learn(TrainingSet.of(WordPair.of("a", "b")));
        int result = this.unidirectionalASTRAMethod.size();
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void testInflect() {
        WordPair wordPair = WordPair.of("abc", "adc");
        this.unidirectionalASTRAMethod.learn(TrainingSet.of(wordPair));
        Word input = wordPair.getLeftWord();
        MethodResponse expected = MethodResponse.singleton(wordPair.getRightWord());
        Optional<MethodResponse> response = this.unidirectionalASTRAMethod.inflect(input);
        assertThat(response).hasValue(expected);
    }

    @Test
    public void testLemmatizeWithUnidirectionalMethod() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.unidirectionalASTRAMethod.lemmatize(null));
        assertThat(exception).hasMessage("Unidirectional ASTRA method can only inflect but not lemmatize");
    }

    @Test
    public void testLemmatizeWithBidirectionalMethod() {
        WordPair wordPair = WordPair.of("abc", "adc");
        this.bidirectionalASTRAMethod.learn(TrainingSet.of(wordPair));
        Word input = wordPair.getRightWord();
        MethodResponse expected = MethodResponse.singleton(wordPair.getLeftWord());
        Optional<MethodResponse> response = this.bidirectionalASTRAMethod.lemmatize(input);
        assertThat(response).hasValue(expected);
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        this.unidirectionalASTRAMethod.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Any message = Any.pack(this.unidirectionalASTRAMethod.toMessage());

        ASTRAMethod astraMethod = new ASTRAMethod(true, null, this.configuration);
        astraMethod.fromMessage(message);

        assertThat(astraMethod.getAffixType()).isEqualTo(this.affixType);
        assertThat(astraMethod.inflect(Word.of("a"))).hasValue(MethodResponse.singleton(Word.of("b")));
        assertThat(astraMethod.inflect(Word.of("c"))).hasValue(MethodResponse.singleton(Word.of("d")));
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(ASTRAMethodMessage.newBuilder().build()));
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> this.unidirectionalASTRAMethod.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not an ASTRAMethodMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        this.unidirectionalASTRAMethod.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Path file = Files.createTempFile("morpher", "astra");
        ASTRAMethod astraMethod;

        try {
            this.unidirectionalASTRAMethod.saveTo(file);
            astraMethod = new ASTRAMethod(false, null, this.configuration);
            astraMethod.loadFrom(file);
            assertThat(astraMethod.isUnidirectional()).isEqualTo(this.unidirectionalASTRAMethod.isUnidirectional());
            assertThat(astraMethod.getAffixType()).isEqualTo(this.affixType);
            assertThat(astraMethod.getAstra()).isNotNull();
            assertThat(astraMethod.inflect(Word.of("a"))).hasValue(MethodResponse.singleton(Word.of("b")));
            assertThat(astraMethod.inflect(Word.of("c"))).hasValue(MethodResponse.singleton(Word.of("d")));
        }
        finally {
            Files.delete(file);
        }
    }

}
