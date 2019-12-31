package com.github.szgabsz91.morpher.transformationengines.tasr.converters;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.api.model.TransformationEngineResponse;
import com.github.szgabsz91.morpher.transformationengines.tasr.impl.transformationengine.TASRTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.tasr.protocolbuffers.TASRTransformationEngineMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TASRTransformationEngineConverterTest {

    private TASRTransformationEngineConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new TASRTransformationEngineConverter();
    }

    @Test
    public void testConvertAndConvertBackWithUnidirectionalTASRTransformationEngine() {
        AffixType affixType = AffixType.of("AFF");
        TASRTransformationEngine tasrTransformationEngine = new TASRTransformationEngine(true, affixType);
        Set<FrequencyAwareWordPair> wordPairs = createWordPairs();
        tasrTransformationEngine.learn(TrainingSet.of(wordPairs));
        wordPairs.forEach(wordPair -> {
            Word rootForm = wordPair.getLeftWord();
            Word inflectedForm = wordPair.getRightWord();
            assertThat(tasrTransformationEngine.transform(rootForm)).hasValue(TransformationEngineResponse.singleton(inflectedForm));
            assertThrows(UnsupportedOperationException.class, () -> tasrTransformationEngine.transformBack(inflectedForm));
        });
        TASRTransformationEngineMessage tasrTransformationEngineMessage = this.converter.convert(tasrTransformationEngine);
        assertThat(tasrTransformationEngineMessage.getUnidirectional()).isTrue();
        assertThat(tasrTransformationEngineMessage.getAffixType()).isEqualTo(affixType.toString());
        assertThat(tasrTransformationEngineMessage.hasForwardsTree()).isTrue();
        assertThat(tasrTransformationEngineMessage.hasBackwardsTree()).isFalse();
        TASRTransformationEngine rebuiltTASRTransformationEngine = this.converter.convertBack(tasrTransformationEngineMessage);
        assertThat(rebuiltTASRTransformationEngine.isUnidirectional()).isEqualTo(tasrTransformationEngine.isUnidirectional());
        assertThat(rebuiltTASRTransformationEngine.getAffixType()).isEqualTo(tasrTransformationEngine.getAffixType());
        wordPairs.forEach(wordPair -> {
            Word rootForm = wordPair.getLeftWord();
            Word inflectedForm = wordPair.getRightWord();
            assertThat(rebuiltTASRTransformationEngine.transform(rootForm)).hasValue(TransformationEngineResponse.singleton(inflectedForm));
            assertThrows(UnsupportedOperationException.class, () -> tasrTransformationEngine.transformBack(inflectedForm));
        });
    }

    @Test
    public void testConvertAndConvertBackWithBidirectionalTASRTransformationEngine() {
        AffixType affixType = AffixType.of("AFF");
        TASRTransformationEngine tasrTransformationEngine = new TASRTransformationEngine(false, affixType);
        Set<FrequencyAwareWordPair> wordPairs = createWordPairs();
        tasrTransformationEngine.learn(TrainingSet.of(wordPairs));
        wordPairs.forEach(wordPair -> {
            Word rootForm = wordPair.getLeftWord();
            Word inflectedForm = wordPair.getRightWord();
            assertThat(tasrTransformationEngine.transform(rootForm)).hasValue(TransformationEngineResponse.singleton(inflectedForm));
            assertThat(tasrTransformationEngine.transformBack(inflectedForm)).hasValue(TransformationEngineResponse.singleton(rootForm));
        });
        TASRTransformationEngineMessage tasrTransformationEngineMessage = this.converter.convert(tasrTransformationEngine);
        assertThat(tasrTransformationEngineMessage.getUnidirectional()).isFalse();
        assertThat(tasrTransformationEngineMessage.getAffixType()).isEqualTo(affixType.toString());
        assertThat(tasrTransformationEngineMessage.hasForwardsTree()).isTrue();
        assertThat(tasrTransformationEngineMessage.hasBackwardsTree()).isTrue();
        TASRTransformationEngine rebuiltTASRTransformationEngine = this.converter.convertBack(tasrTransformationEngineMessage);
        assertThat(rebuiltTASRTransformationEngine.isUnidirectional()).isEqualTo(tasrTransformationEngine.isUnidirectional());
        assertThat(rebuiltTASRTransformationEngine.getAffixType()).isEqualTo(tasrTransformationEngine.getAffixType());
        wordPairs.forEach(wordPair -> {
            Word rootForm = wordPair.getLeftWord();
            Word inflectedForm = wordPair.getRightWord();
            assertThat(rebuiltTASRTransformationEngine.transform(rootForm)).hasValue(TransformationEngineResponse.singleton(inflectedForm));
            assertThat(tasrTransformationEngine.transformBack(inflectedForm)).hasValue(TransformationEngineResponse.singleton(rootForm));
        });
    }

    @Test
    public void testParse() throws IOException {
        AffixType affixType = AffixType.of("AFF");
        TASRTransformationEngine tasrTransformationEngine = new TASRTransformationEngine(false, affixType);
        WordPair wordPair = WordPair.of("leaf", "leaves");
        tasrTransformationEngine.learn(TrainingSet.of(wordPair));
        Path file = Files.createTempFile("transformation-engine", "tasr");
        try {
            tasrTransformationEngine.saveTo(file);
            TASRTransformationEngineMessage tasrTransformationEngineMessage = this.converter.parse(file);
            TASRTransformationEngine result = this.converter.convertBack(tasrTransformationEngineMessage);
            assertThat(result.isUnidirectional()).isEqualTo(tasrTransformationEngine.isUnidirectional());
            assertThat(result.getAffixType()).isEqualTo(affixType);
            assertThat(result.transform(wordPair.getLeftWord())).hasValue(TransformationEngineResponse.singleton(wordPair.getRightWord()));
            assertThat(result.transformBack(wordPair.getRightWord())).hasValue(TransformationEngineResponse.singleton(wordPair.getLeftWord()));
        }
        finally {
            Files.delete(file);
        }
    }

    private Set<FrequencyAwareWordPair> createWordPairs() {
        /*
         * root
         *     a
         *         c
         *     b
         *         d
         *             f
         *         e
         */
        return Set.of(
                FrequencyAwareWordPair.of("", "1"),
                FrequencyAwareWordPair.of("a", "a2"),
                FrequencyAwareWordPair.of("ac", "ac3"),
                FrequencyAwareWordPair.of("b", "b4"),
                FrequencyAwareWordPair.of("bd", "bd5"),
                FrequencyAwareWordPair.of("bdf", "bdf6"),
                FrequencyAwareWordPair.of("be", "be7")
        );
    }

}
