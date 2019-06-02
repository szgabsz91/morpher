package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class InflectionInputTest {

    @Test
    public void testConstructorAndGetters() {
        Word input = Word.of("input");
        Set<AffixType> affixTypes = Set.of(AffixType.of("AFF1"), AffixType.of("AFF2"));
        InflectionInput inflectionInput = new InflectionInput(input, affixTypes);
        assertThat(inflectionInput.getInput()).isEqualTo(input);
        assertThat(inflectionInput.getAffixTypes()).isEqualTo(affixTypes);
    }

    @Test
    public void testEquals() {
        InflectionInput inflectionInput1 = new InflectionInput(Word.of("input"), Set.of(AffixType.of("AFF")));
        InflectionInput inflectionInput2 = new InflectionInput(Word.of("input2"), Set.of(AffixType.of("AFF")));
        InflectionInput inflectionInput3 = new InflectionInput(Word.of("input"), Set.of(AffixType.of("AFF2")));
        InflectionInput inflectionInput4 = new InflectionInput(Word.of("input"), Set.of(AffixType.of("AFF")));

        assertThat(inflectionInput1).isEqualTo(inflectionInput1);
        assertThat(inflectionInput1).isNotEqualTo(null);
        assertThat(inflectionInput1).isNotEqualTo("string");
        assertThat(inflectionInput1).isNotEqualTo(inflectionInput2);
        assertThat(inflectionInput1).isNotEqualTo(inflectionInput3);
        assertThat(inflectionInput1).isEqualTo(inflectionInput4);
    }

    @Test
    public void testHashCode() {
        InflectionInput inflectionInput = new InflectionInput(Word.of("input"), Set.of(AffixType.of("AFF")));
        int result = inflectionInput.hashCode();
        int expected = 31 * inflectionInput.getInput().hashCode() + inflectionInput.getAffixTypes().hashCode();
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        InflectionInput inflectionInput = new InflectionInput(Word.of("input"), Set.of(AffixType.of("AFF")));
        assertThat(inflectionInput).hasToString("InflectionInput[input=" + inflectionInput.getInput() + ", affixTypes=" + inflectionInput.getAffixTypes() + ']');
    }

}
