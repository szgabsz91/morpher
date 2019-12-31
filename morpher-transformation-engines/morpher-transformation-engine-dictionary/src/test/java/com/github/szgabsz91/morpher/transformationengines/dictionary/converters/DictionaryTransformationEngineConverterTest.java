package com.github.szgabsz91.morpher.transformationengines.dictionary.converters;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.core.protocolbuffers.WordPairMessage;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.dictionary.impl.transformationengine.DictionaryTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.dictionary.protocolbuffers.DictionaryTransformationEngineMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DictionaryTransformationEngineConverterTest {

    private DictionaryTransformationEngineConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new DictionaryTransformationEngineConverter();
    }

    @Test
    public void testConvert() {
        AffixType affixType = AffixType.of("AFF");
        DictionaryTransformationEngine dictionaryTransformationEngine = new DictionaryTransformationEngine(affixType);
        WordPair wordPair = WordPair.of("a", "b");
        dictionaryTransformationEngine.learn(TrainingSet.of(wordPair));
        DictionaryTransformationEngineMessage dictionaryTransformationEngineMessage = this.converter.convert(dictionaryTransformationEngine);
        assertThat(dictionaryTransformationEngineMessage.getAffixType()).isEqualTo(affixType.toString());
        assertThat(dictionaryTransformationEngineMessage.getWordPairsCount()).isEqualTo(1);
        WordPairMessage wordPairMessage = dictionaryTransformationEngineMessage.getWordPairs(0);
        assertThat(wordPairMessage.getLeftWord()).isEqualTo(wordPair.getLeftWord().toString());
        assertThat(wordPairMessage.getRightWord()).isEqualTo(wordPair.getRightWord().toString());
    }

    @Test
    public void testConvertBack() {
        AffixType affixType = AffixType.of("AFF");
        WordPair wordPair = WordPair.of("a", "b");
        DictionaryTransformationEngineMessage dictionaryTransformationEngineMessage = DictionaryTransformationEngineMessage.newBuilder()
                .setAffixType(affixType.toString())
                .addWordPairs(WordPairMessage.newBuilder().setLeftWord(wordPair.getLeftWord().toString()).setRightWord(wordPair.getRightWord().toString()))
                .build();
        DictionaryTransformationEngine dictionaryTransformationEngine = this.converter.convertBack(dictionaryTransformationEngineMessage);
        assertThat(dictionaryTransformationEngine.getAffixType()).isEqualTo(affixType);
        Map<Word, Word> forwardsDictionary = dictionaryTransformationEngine.getForwardsDictionary();
        assertThat(forwardsDictionary.size()).isEqualTo(1);
        Word forwardsResult = forwardsDictionary.get(wordPair.getLeftWord());
        assertThat(forwardsResult).isEqualTo(wordPair.getRightWord());
        Map<Word, Word> backwardsDictionary = dictionaryTransformationEngine.getBackwardsDictionary();
        assertThat(backwardsDictionary.size()).isEqualTo(1);
        Word backwardsResult = backwardsDictionary.get(wordPair.getRightWord());
        assertThat(backwardsResult).isEqualTo(wordPair.getLeftWord());
    }

    @Test
    public void testParse() throws IOException {
        AffixType affixType = AffixType.of("AFF");
        DictionaryTransformationEngine dictionaryTransformationEngine = new DictionaryTransformationEngine(affixType);
        WordPair wordPair = WordPair.of("a", "b");
        dictionaryTransformationEngine.learn(TrainingSet.of(wordPair));
        Path file = Files.createTempFile("transformation-engine", "dictionary");

        try {
            dictionaryTransformationEngine.saveTo(file);
            DictionaryTransformationEngineMessage dictionaryTransformationEngineMessage = this.converter.parse(file);
            DictionaryTransformationEngine result = this.converter.convertBack(dictionaryTransformationEngineMessage);
            assertThat(result.getAffixType()).isEqualTo(affixType);
            assertThat(result.getForwardsDictionary()).isEqualTo(dictionaryTransformationEngine.getForwardsDictionary());
            assertThat(result.getBackwardsDictionary()).isEqualTo(dictionaryTransformationEngine.getBackwardsDictionary());
        }
        finally {
            Files.delete(file);
        }
    }

}
