package com.github.szgabsz91.morpher.analyzeragents.hunmorph.impl;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.analyzeragents.api.model.AnnotationTokenizerResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HunmorphAnnotationTokenizerTest {

    private HunmorphAnnotationTokenizer tokenizer;

    @BeforeEach
    public void setUp() {
        this.tokenizer = new HunmorphAnnotationTokenizer();
    }

    @Test
    public void testTokenizeWithNoun() {
        String lemma = "lemma";
        String expression = lemma + "/NOUN" + HunmorphAnnotationTokenizer.KNOWN_TOKENS
                .stream()
                .filter(token -> !token.startsWith("/"))
                .collect(joining());
        String grammaticalForm = "grammatical";
        int frequency = 3;
        AnnotationTokenizerResult result = tokenizer.tokenize(expression, grammaticalForm, frequency);
        assertThat(result.getGrammaticalForm()).isEqualTo(grammaticalForm);
        assertThat(result.getLemma()).isEqualTo(lemma);
        assertThat(result.getAffixTypes()).hasSize(326);
        assertThat(result.getFrequency()).isEqualTo(frequency);
    }

    @Test
    public void testTokenizeWithVerbInPlural() {
        String lemma = "lemma";
        String expression = lemma + "/VERB<PLUR>";
        String grammaticalForm = "grammatical";
        int frequency = 3;
        AnnotationTokenizerResult result = tokenizer.tokenize(expression, grammaticalForm, frequency);
        result = tokenizer.preprocess(result);
        assertThat(result.getGrammaticalForm()).isEqualTo(grammaticalForm);
        assertThat(result.getLemma()).isEqualTo(lemma);
        assertThat(result.getAffixTypes()).containsExactly(AffixType.of("/VERB"), AffixType.of("<VPLUR>"));
        assertThat(result.getFrequency()).isEqualTo(frequency);
    }

    @Test
    public void testTokenizeWithSapiens() {
        AnnotationTokenizerResult result = tokenizer.tokenize("sapiens/NOUN", "grammatical", 1);
        assertThat(result).isNull();
    }

    @Test
    public void testTokenizeWithPreverb() {
        AnnotationTokenizerResult result = tokenizer.tokenize("/PREV", "grammatical", 1);
        assertThat(result).isNull();
    }

    @Test
    public void testTokenizeWithSuperSuperlat() {
        AnnotationTokenizerResult result = tokenizer.tokenize("/ADJ[SUPER-SUPERLAT]", "grammatical", 1);
        assertThat(result.getAffixTypes()).containsExactly(AffixType.of("/ADJ"), AffixType.of("[SUPERSUPERLAT]"));
    }

    @Test
    public void testTokenizeWithUnknownToken() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> tokenizer.tokenize("/ADJ[UNKNOWN]", "grammatical", 1));
        assertThat(exception).hasMessage("Unknown token found: [UNKNOWN]");
    }

    @Test
    public void testGetSupportedAffixTypes() {
        Set<String> supportedAffixTypes = tokenizer.getSupportedAffixTypes();
        Set<String> expected = new HashSet<>(tokenizer.getSupportedAffixTypes());
        assertThat(supportedAffixTypes).isEqualTo(expected);
    }

    @Test
    public void testPreprocessWithImel() {
        AnnotationTokenizerResult result = tokenizer.tokenize("/ímél<CAS<ACC>>", "grammatical", 1);
        result = tokenizer.preprocess(result);
        assertThat(result.getAffixTypes()).containsExactly(AffixType.of("<CAS<ACC>>"));
    }

    @Test
    public void testPreprocessWithPLURANP() {
        AnnotationTokenizerResult result = tokenizer.tokenize("/NOUN<PLUR<ANP>>", "grammatical", 1);
        result = tokenizer.preprocess(result);
        assertThat(result.getAffixTypes()).containsExactly(AffixType.of("/NOUN"), AffixType.of("<ANP<PLUR>>"));
    }

    @Test
    public void testPreprocessWithPluralNoun() {
        AnnotationTokenizerResult result = tokenizer.tokenize("/NOUN<PLUR>", "grammatical", 1);
        result = tokenizer.preprocess(result);
        assertThat(result.getAffixTypes()).containsExactly(AffixType.of("/NOUN"), AffixType.of("<PLUR>"));
    }

    @Test
    public void testWithPreverb() {
        String expression = "vissza/PREV+szerez/VERB";
        String grammaticalForm = "visszaszerez";
        int frequency = 3;
        AnnotationTokenizerResult result = tokenizer.tokenize(expression, grammaticalForm, frequency);
        assertThat(result.getLemma()).isEqualTo("szerez");
        assertThat(result.getAffixTypes()).containsExactly(
                AffixType.of("/VERB"),
                AffixType.of("<PREVERB<VISSZA>>")
        );
        assertThat(result.getGrammaticalForm()).isEqualTo(grammaticalForm);
        assertThat(result.getExpression()).isEqualTo(expression);
        assertThat(result.getFrequency()).isEqualTo(frequency);
    }

    @Test
    public void testWithPreverbAndOtherAffixTypes() {
        String expression = "vissza/PREV+sodor/VERB<PAST>";
        String grammaticalForm = "visszasodort";
        int frequency = 3;
        AnnotationTokenizerResult result = tokenizer.tokenize(expression, grammaticalForm, frequency);
        assertThat(result.getLemma()).isEqualTo("sodor");
        assertThat(result.getAffixTypes()).containsExactly(
                AffixType.of("/VERB"),
                AffixType.of("<PREVERB<VISSZA>>"),
                AffixType.of("<PAST>")
        );
        assertThat(result.getGrammaticalForm()).isEqualTo(grammaticalForm);
        assertThat(result.getExpression()).isEqualTo(expression);
        assertThat(result.getFrequency()).isEqualTo(frequency);
    }

    @Test
    public void testWithPreverbAndOtherAffixTypesWithSquareBrackets() {
        String expression = "vissza/PREV+sodor/VERB[GERUND]<PAST>";
        String grammaticalForm = "visszasodort";
        int frequency = 3;
        AnnotationTokenizerResult result = tokenizer.tokenize(expression, grammaticalForm, frequency);
        assertThat(result.getLemma()).isEqualTo("sodor");
        assertThat(result.getAffixTypes()).containsExactly(
                AffixType.of("/VERB"),
                AffixType.of("<PREVERB<VISSZA>>"),
                AffixType.of("[GERUND]"),
                AffixType.of("<PAST>")
        );
        assertThat(result.getGrammaticalForm()).isEqualTo(grammaticalForm);
        assertThat(result.getExpression()).isEqualTo(expression);
        assertThat(result.getFrequency()).isEqualTo(frequency);
    }

}
