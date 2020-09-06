package com.github.szgabsz91.morpher.languagehandlers.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationTokenizerResultTest {

    @Test
    public void testConstructorAndGetters() {
        String expression = "expression";
        String grammaticalForm = "grammatical-form";
        String lemma = "lemma";
        int frequency = 1;
        AnnotationTokenizerResult result = new AnnotationTokenizerResult(expression, grammaticalForm, lemma, frequency);
        assertThat(result.getExpression()).isEqualTo(expression);
        assertThat(result.getGrammaticalForm()).isEqualTo(grammaticalForm);
        assertThat(result.getLemma()).isEqualTo(lemma);
        assertThat(result.getAffixTypes()).isEmpty();
        assertThat(result.getFrequency()).isEqualTo(frequency);
    }

    @Test
    public void testAddAffixType() {
        AnnotationTokenizerResult result = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1);
        AffixType affixType = AffixType.of("AFF");
        result.addAffixType(affixType);
        assertThat(result.getAffixTypes()).containsExactly(affixType);
    }

    @Test
    public void testDeepCopy() {
        AnnotationTokenizerResult result = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1);
        AffixType affixType = AffixType.of("AFF");
        result.addAffixType(affixType);
        AnnotationTokenizerResult clone = result.deepCopy();
        result.addAffixType(AffixType.of("AFF2"));
        assertThat(clone.getExpression()).isEqualTo(result.getExpression());
        assertThat(clone.getGrammaticalForm()).isEqualTo(result.getGrammaticalForm());
        assertThat(clone.getLemma()).isEqualTo(result.getLemma());
        assertThat(clone.getAffixTypes()).containsExactly(affixType);
        assertThat(clone.getFrequency()).isEqualTo(result.getFrequency());
    }

    @Test
    public void testCompareToWithNegativeResult() {
        AnnotationTokenizerResult result1 = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1);
        result1.addAffixType(AffixType.of("AFF1"));
        result1.addAffixType(AffixType.of("AFF2"));
        AnnotationTokenizerResult result2 = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1);
        result2.addAffixType(AffixType.of("AFF3"));
        int result = result1.compareTo(result2);
        assertThat(result).isNegative();
    }

    @Test
    public void testCompareToWithZeroResult() {
        AnnotationTokenizerResult result1 = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1);
        result1.addAffixType(AffixType.of("AFF1"));
        AnnotationTokenizerResult result2 = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1);
        result2.addAffixType(AffixType.of("AFF2"));
        result2.addAffixType(AffixType.of("AFF3"));
        int result = result1.compareTo(result2);
        assertThat(result).isPositive();
    }

    @Test
    public void testCompareToWithPositiveResult() {
        AnnotationTokenizerResult result1 = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1);
        result1.addAffixType(AffixType.of("AFF1"));
        AnnotationTokenizerResult result2 = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1);
        result2.addAffixType(AffixType.of("AFF2"));
        int result = result1.compareTo(result2);
        assertThat(result).isZero();
    }

    @Test
    public void testEquals() {
        AnnotationTokenizerResult result1 = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1);
        AnnotationTokenizerResult result2 = new AnnotationTokenizerResult("expression2", "grammatical", "lemma", 1);
        AnnotationTokenizerResult result3 = new AnnotationTokenizerResult("expression", "grammatical2", "lemma", 1);
        AnnotationTokenizerResult result4 = new AnnotationTokenizerResult("expression", "grammatical", "lemma2", 1);
        AnnotationTokenizerResult result5 = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1);
        result5.addAffixType(AffixType.of("AFF"));
        AnnotationTokenizerResult result6 = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 2);
        AnnotationTokenizerResult result7 = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1);

        assertThat(result1.equals(result1)).isTrue();
        assertThat(result1.equals(null)).isFalse();
        assertThat(result1).isNotEqualTo("string");
        assertThat(result1).isNotEqualTo(result2);
        assertThat(result1).isNotEqualTo(result3);
        assertThat(result1).isNotEqualTo(result4);
        assertThat(result1).isNotEqualTo(result5);
        assertThat(result1).isNotEqualTo(result6);
        assertThat(result1).isEqualTo(result7);
    }

    @Test
    public void testHashCode() {
        AnnotationTokenizerResult result = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1);
        result.addAffixType(AffixType.of("AFF"));
        int hashCode = result.hashCode();
        int expected = Objects.hash(result.getExpression(), result.getGrammaticalForm(), result.getLemma(), result.getAffixTypes(), result.getFrequency());
        assertThat(hashCode).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        AnnotationTokenizerResult result = new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1);
        result.addAffixType(AffixType.of("AFF"));
        assertThat(result).hasToString("AnnotationTokenizerResult[" + result.getExpression() + ", " + result.getGrammaticalForm() + ", " + result.getLemma() + ", " + result.getAffixTypes() + ", " + result.getFrequency() + ']');
    }

}
