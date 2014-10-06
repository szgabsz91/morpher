package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InflectionOrderedInputTest {

    @Test
    public void testConstructorAndGetters() {
        Word input = Word.of("input");
        List<AffixType> affixTypes = List.of(AffixType.of("AFF1"), AffixType.of("AFF2"));
        InflectionOrderedInput inflectionOrderedInput = new InflectionOrderedInput(input, affixTypes);
        assertThat(inflectionOrderedInput.getInput()).isEqualTo(input);
        assertThat(inflectionOrderedInput.getAffixTypes()).isEqualTo(affixTypes);
    }

    @Test
    public void testEquals() {
        InflectionOrderedInput inflectionOrderedInput1 = new InflectionOrderedInput(Word.of("input"), List.of(AffixType.of("AFF")));
        InflectionOrderedInput inflectionOrderedInput2 = new InflectionOrderedInput(Word.of("input2"), List.of(AffixType.of("AFF")));
        InflectionOrderedInput inflectionOrderedInput3 = new InflectionOrderedInput(Word.of("input"), List.of(AffixType.of("AFF2")));
        InflectionOrderedInput inflectionOrderedInput4 = new InflectionOrderedInput(Word.of("input"), List.of(AffixType.of("AFF")));

        assertThat(inflectionOrderedInput1).isEqualTo(inflectionOrderedInput1);
        assertThat(inflectionOrderedInput1).isNotEqualTo(null);
        assertThat(inflectionOrderedInput1).isNotEqualTo("string");
        assertThat(inflectionOrderedInput1).isNotEqualTo(inflectionOrderedInput2);
        assertThat(inflectionOrderedInput1).isNotEqualTo(inflectionOrderedInput3);
        assertThat(inflectionOrderedInput1).isEqualTo(inflectionOrderedInput4);
    }

    @Test
    public void testHashCode() {
        InflectionOrderedInput inflectionOrderedInput = new InflectionOrderedInput(Word.of("input"), List.of(AffixType.of("AFF")));
        int result = inflectionOrderedInput.hashCode();
        int expected = 31 * inflectionOrderedInput.getInput().hashCode() + inflectionOrderedInput.getAffixTypes().hashCode();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        InflectionOrderedInput inflectionOrderedInput = new InflectionOrderedInput(Word.of("input"), List.of(AffixType.of("AFF")));
        assertThat(inflectionOrderedInput).hasToString("InflectionOrderedInput[input=" + inflectionOrderedInput.getInput() + ", affixTypes=" + inflectionOrderedInput.getAffixTypes() + ']');
    }

}
