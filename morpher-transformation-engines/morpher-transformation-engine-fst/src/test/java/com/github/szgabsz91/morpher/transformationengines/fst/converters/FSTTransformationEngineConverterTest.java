package com.github.szgabsz91.morpher.transformationengines.fst.converters;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.core.protocolbuffers.WordPairMessage;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.api.model.TransformationEngineResponse;
import com.github.szgabsz91.morpher.transformationengines.fst.impl.transformationengine.FSTTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.fst.protocolbuffers.FSTTransformationEngineMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FSTTransformationEngineConverterTest {

    private FSTTransformationEngineConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new FSTTransformationEngineConverter();
    }

    @Test
    public void testConvert() {
        AffixType affixType = AffixType.of("AFF");
        FSTTransformationEngine fstTransformationEngine = new FSTTransformationEngine(true, affixType);
        WordPair wordPair = WordPair.of("a", "b");
        fstTransformationEngine.learn(TrainingSet.of(wordPair));
        FSTTransformationEngineMessage fstTransformationEngineMessage = this.converter.convert(fstTransformationEngine);
        assertThat(fstTransformationEngineMessage.getUnidirectional()).isTrue();
        assertThat(fstTransformationEngineMessage.getAffixType()).isEqualTo(affixType.toString());
        assertThat(fstTransformationEngineMessage.getWordPairsCount()).isEqualTo(1);
        WordPairMessage wordPairMessage = fstTransformationEngineMessage.getWordPairs(0);
        assertThat(wordPairMessage.getLeftWord()).isEqualTo(wordPair.getLeftWord().toString());
        assertThat(wordPairMessage.getRightWord()).isEqualTo(wordPair.getRightWord().toString());
    }

    @Test
    public void testConvertBackWithUnidirectionalTransformationEngine() {
        AffixType affixType = AffixType.of("AFF");
        WordPair wordPair = WordPair.of("a", "b");
        FSTTransformationEngineMessage fstTransformationEngineMessage = FSTTransformationEngineMessage.newBuilder()
                .setUnidirectional(true)
                .setAffixType(affixType.toString())
                .addWordPairs(WordPairMessage.newBuilder().setLeftWord(wordPair.getLeftWord().toString()).setRightWord(wordPair.getRightWord().toString()))
                .build();
        FSTTransformationEngine fstTransformationEngine = this.converter.convertBack(fstTransformationEngineMessage);
        assertThat(fstTransformationEngine.isUnidirectional()).isTrue();
        assertThat(fstTransformationEngine.getAffixType()).isEqualTo(affixType);
        List<WordPair> wordPairs = fstTransformationEngine.getWordPairs();
        assertThat(wordPairs.size()).isEqualTo(1);
        WordPair result = wordPairs.get(0);
        assertThat(result).isEqualTo(wordPair);
        assertThat(fstTransformationEngine.transform(wordPair.getLeftWord())).hasValue(TransformationEngineResponse.singleton(wordPair.getRightWord()));
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> fstTransformationEngine.transformBack(wordPair.getRightWord()));
        assertThat(exception).hasMessage("Unidirectional FST transformation engine can only transform words forwards but not backwards");
    }

    @Test
    public void testConvertBackWithBidirectionalTransformationEngine() {
        AffixType affixType = AffixType.of("AFF");
        WordPair wordPair = WordPair.of("a", "b");
        FSTTransformationEngineMessage fstTransformationEngineMessage = FSTTransformationEngineMessage.newBuilder()
                .setUnidirectional(false)
                .setAffixType(affixType.toString())
                .addWordPairs(WordPairMessage.newBuilder().setLeftWord(wordPair.getLeftWord().toString()).setRightWord(wordPair.getRightWord().toString()))
                .build();
        FSTTransformationEngine fstTransformationEngine = this.converter.convertBack(fstTransformationEngineMessage);
        assertThat(fstTransformationEngine.isUnidirectional()).isFalse();
        assertThat(fstTransformationEngine.getAffixType()).isEqualTo(affixType);
        List<WordPair> wordPairs = fstTransformationEngine.getWordPairs();
        assertThat(wordPairs.size()).isEqualTo(1);
        WordPair result = wordPairs.get(0);
        assertThat(result).isEqualTo(wordPair);
        assertThat(fstTransformationEngine.transform(wordPair.getLeftWord())).hasValue(TransformationEngineResponse.singleton(wordPair.getRightWord()));
        assertThat(fstTransformationEngine.transformBack(wordPair.getRightWord())).hasValue(TransformationEngineResponse.singleton(wordPair.getLeftWord()));
    }

    @Test
    public void testParse() throws IOException {
        AffixType affixType = AffixType.of("AFF");
        FSTTransformationEngine fstTransformationEngine = new FSTTransformationEngine(false, affixType);
        WordPair wordPair = WordPair.of("a", "b");
        fstTransformationEngine.learn(TrainingSet.of(wordPair));
        Path file = Files.createTempFile("transformation-engine", "fst");

        try {
            fstTransformationEngine.saveTo(file);
            FSTTransformationEngineMessage fstTransformationEngineMessage = this.converter.parse(file);
            FSTTransformationEngine result = this.converter.convertBack(fstTransformationEngineMessage);
            assertThat(result.isUnidirectional()).isEqualTo(fstTransformationEngine.isUnidirectional());
            assertThat(result.getAffixType()).isEqualTo(affixType);
            assertThat(result.getWordPairs()).isEqualTo(fstTransformationEngine.getWordPairs());
        }
        finally {
            Files.delete(file);
        }
    }

}
