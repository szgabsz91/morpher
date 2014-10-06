package com.github.szgabsz91.morpher.methods.tasr.converters;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.github.szgabsz91.morpher.methods.tasr.impl.method.TASRMethod;
import com.github.szgabsz91.morpher.methods.tasr.protocolbuffers.TASRMethodMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TASRMethodConverterTest {

    private TASRMethodConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new TASRMethodConverter();
    }

    @Test
    public void testConvertAndConvertBackWithUnidirectionalTASRMethod() {
        AffixType affixType = AffixType.of("AFF");
        TASRMethod tasrMethod = new TASRMethod(true, affixType);
        Set<FrequencyAwareWordPair> wordPairs = createWordPairs();
        tasrMethod.learn(TrainingSet.of(wordPairs));
        wordPairs.forEach(wordPair -> {
            Word rootForm = wordPair.getLeftWord();
            Word inflectedForm = wordPair.getRightWord();
            assertThat(tasrMethod.inflect(rootForm)).hasValue(MethodResponse.singleton(inflectedForm));
            assertThrows(UnsupportedOperationException.class, () -> tasrMethod.lemmatize(inflectedForm));
        });
        TASRMethodMessage tasrMethodMessage = this.converter.convert(tasrMethod);
        assertThat(tasrMethodMessage.getUnidirectional()).isTrue();
        assertThat(tasrMethodMessage.getAffixType()).isEqualTo(affixType.toString());
        assertThat(tasrMethodMessage.hasInflectionTree()).isTrue();
        assertThat(tasrMethodMessage.hasLemmatizationTree()).isFalse();
        TASRMethod rebuiltTASRMethod = this.converter.convertBack(tasrMethodMessage);
        assertThat(rebuiltTASRMethod.isUnidirectional()).isEqualTo(tasrMethod.isUnidirectional());
        assertThat(rebuiltTASRMethod.getAffixType()).isEqualTo(tasrMethod.getAffixType());
        wordPairs.forEach(wordPair -> {
            Word rootForm = wordPair.getLeftWord();
            Word inflectedForm = wordPair.getRightWord();
            assertThat(rebuiltTASRMethod.inflect(rootForm)).hasValue(MethodResponse.singleton(inflectedForm));
            assertThrows(UnsupportedOperationException.class, () -> tasrMethod.lemmatize(inflectedForm));
        });
    }

    @Test
    public void testConvertAndConvertBackWithBidirectionalTASRMethod() {
        AffixType affixType = AffixType.of("AFF");
        TASRMethod tasrMethod = new TASRMethod(false, affixType);
        Set<FrequencyAwareWordPair> wordPairs = createWordPairs();
        tasrMethod.learn(TrainingSet.of(wordPairs));
        wordPairs.forEach(wordPair -> {
            Word rootForm = wordPair.getLeftWord();
            Word inflectedForm = wordPair.getRightWord();
            assertThat(tasrMethod.inflect(rootForm)).hasValue(MethodResponse.singleton(inflectedForm));
            assertThat(tasrMethod.lemmatize(inflectedForm)).hasValue(MethodResponse.singleton(rootForm));
        });
        TASRMethodMessage tasrMethodMessage = this.converter.convert(tasrMethod);
        assertThat(tasrMethodMessage.getUnidirectional()).isFalse();
        assertThat(tasrMethodMessage.getAffixType()).isEqualTo(affixType.toString());
        assertThat(tasrMethodMessage.hasInflectionTree()).isTrue();
        assertThat(tasrMethodMessage.hasLemmatizationTree()).isTrue();
        TASRMethod rebuiltTASRMethod = this.converter.convertBack(tasrMethodMessage);
        assertThat(rebuiltTASRMethod.isUnidirectional()).isEqualTo(tasrMethod.isUnidirectional());
        assertThat(rebuiltTASRMethod.getAffixType()).isEqualTo(tasrMethod.getAffixType());
        wordPairs.forEach(wordPair -> {
            Word rootForm = wordPair.getLeftWord();
            Word inflectedForm = wordPair.getRightWord();
            assertThat(rebuiltTASRMethod.inflect(rootForm)).hasValue(MethodResponse.singleton(inflectedForm));
            assertThat(tasrMethod.lemmatize(inflectedForm)).hasValue(MethodResponse.singleton(rootForm));
        });
    }

    @Test
    public void testParse() throws IOException {
        AffixType affixType = AffixType.of("AFF");
        TASRMethod tasrMethod = new TASRMethod(false, affixType);
        WordPair wordPair = WordPair.of("leaf", "leaves");
        tasrMethod.learn(TrainingSet.of(wordPair));
        Path file = Files.createTempFile("morpher", "tasr");
        try {
            tasrMethod.saveTo(file);
            TASRMethodMessage tasrMethodMessage = this.converter.parse(file);
            TASRMethod result = this.converter.convertBack(tasrMethodMessage);
            assertThat(result.isUnidirectional()).isEqualTo(tasrMethod.isUnidirectional());
            assertThat(result.getAffixType()).isEqualTo(affixType);
            assertThat(result.inflect(wordPair.getLeftWord())).hasValue(MethodResponse.singleton(wordPair.getRightWord()));
            assertThat(result.lemmatize(wordPair.getRightWord())).hasValue(MethodResponse.singleton(wordPair.getLeftWord()));
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
