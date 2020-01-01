package com.github.szgabsz91.morpher.transformationengines.astra.impl.transformationengine;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.model.TransformationEngineResponse;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.astra.config.ASTRATransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.astra.config.SearcherType;
import com.github.szgabsz91.morpher.transformationengines.astra.protocolbuffers.ASTRATransformationEngineMessage;
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

public class ASTRATransformationEngineTest {

    private AffixType affixType;
    private ASTRATransformationEngineConfiguration configuration;
    private ASTRATransformationEngine unidirectionalASTRATransformationEngine;
    private ASTRATransformationEngine bidirectionalASTRATransformationEngine;

    @BeforeEach
    public void setUp() {
        this.affixType = AffixType.of("AFF");
        this.configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        this.unidirectionalASTRATransformationEngine = new ASTRATransformationEngine(true, this.affixType, configuration);
        this.bidirectionalASTRATransformationEngine = new ASTRATransformationEngine(false, this.affixType, configuration);
    }

    @Test
    public void testConstructorAndGettersWithUnidirectionalTransformationEngine() {
        assertThat(this.unidirectionalASTRATransformationEngine.getAffixType()).isEqualTo(this.affixType);
        assertThat(this.unidirectionalASTRATransformationEngine.isUnidirectional()).isTrue();
        assertThat(this.unidirectionalASTRATransformationEngine.getAstra()).isNotNull();
    }

    @Test
    public void testConstructorAndGettersWithBidirectionalTransformationEngine() {
        assertThat(this.bidirectionalASTRATransformationEngine.getAffixType()).isEqualTo(this.affixType);
        assertThat(this.bidirectionalASTRATransformationEngine.isUnidirectional()).isFalse();
        assertThat(this.bidirectionalASTRATransformationEngine.getAstra()).isNotNull();
    }

    @Test
    public void testSize() {
        this.unidirectionalASTRATransformationEngine.learn(TrainingSet.of(WordPair.of("a", "b")));
        int result = this.unidirectionalASTRATransformationEngine.size();
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void testTransform() {
        WordPair wordPair = WordPair.of("abc", "adc");
        this.unidirectionalASTRATransformationEngine.learn(TrainingSet.of(wordPair));
        Word input = wordPair.getLeftWord();
        TransformationEngineResponse expected = TransformationEngineResponse.singleton(wordPair.getRightWord());
        Optional<TransformationEngineResponse> response = this.unidirectionalASTRATransformationEngine.transform(input);
        assertThat(response).hasValue(expected);
    }

    @Test
    public void testTransformBackWithUnidirectionalTransformationEngine() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.unidirectionalASTRATransformationEngine.transformBack(null));
        assertThat(exception).hasMessage("Unidirectional ASTRA transformation engine can only transform words forwards and not backwards");
    }

    @Test
    public void testTransformBackWithBidirectionalTransformationEngine() {
        WordPair wordPair = WordPair.of("abc", "adc");
        this.bidirectionalASTRATransformationEngine.learn(TrainingSet.of(wordPair));
        Word input = wordPair.getRightWord();
        TransformationEngineResponse expected = TransformationEngineResponse.singleton(wordPair.getLeftWord());
        Optional<TransformationEngineResponse> response = this.bidirectionalASTRATransformationEngine.transformBack(input);
        assertThat(response).hasValue(expected);
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        this.unidirectionalASTRATransformationEngine.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Any message = Any.pack(this.unidirectionalASTRATransformationEngine.toMessage());

        ASTRATransformationEngine astraTransformationEngine = new ASTRATransformationEngine(true, null, this.configuration);
        astraTransformationEngine.fromMessage(message);

        assertThat(astraTransformationEngine.getAffixType()).isEqualTo(this.affixType);
        assertThat(astraTransformationEngine.transform(Word.of("a"))).hasValue(TransformationEngineResponse.singleton(Word.of("b")));
        assertThat(astraTransformationEngine.transform(Word.of("c"))).hasValue(TransformationEngineResponse.singleton(Word.of("d")));
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(ASTRATransformationEngineMessage.newBuilder().build()));
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> this.unidirectionalASTRATransformationEngine.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not an ASTRATransformationEngineMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        this.unidirectionalASTRATransformationEngine.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Path file = Files.createTempFile("transformation-engine", "astra");
        ASTRATransformationEngine astraTransformationEngine;

        try {
            this.unidirectionalASTRATransformationEngine.saveTo(file);
            astraTransformationEngine = new ASTRATransformationEngine(false, null, this.configuration);
            astraTransformationEngine.loadFrom(file);
            assertThat(astraTransformationEngine.isUnidirectional()).isEqualTo(this.unidirectionalASTRATransformationEngine.isUnidirectional());
            assertThat(astraTransformationEngine.getAffixType()).isEqualTo(this.affixType);
            assertThat(astraTransformationEngine.getAstra()).isNotNull();
            assertThat(astraTransformationEngine.transform(Word.of("a"))).hasValue(TransformationEngineResponse.singleton(Word.of("b")));
            assertThat(astraTransformationEngine.transform(Word.of("c"))).hasValue(TransformationEngineResponse.singleton(Word.of("d")));
        }
        finally {
            Files.delete(file);
        }
    }

}
