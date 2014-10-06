package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class LemmatizationInputWithAffixTypesTest {

    @Test
    public void testOfAndGetters() {
        Word input = Word.of("input");
        List<AffixType> affixTypes = List.of(AffixType.of("<PLUR>"));
        LemmatizationInputWithAffixTypes lemmatizationInputWithAffixTypes = LemmatizationInputWithAffixTypes.of(input, affixTypes);
        assertThat(lemmatizationInputWithAffixTypes.getInput()).isEqualTo(input);
        assertThat(lemmatizationInputWithAffixTypes.getAffixTypes()).isEqualTo(affixTypes);
    }

    @Test
    public void testEquals() {
        LemmatizationInputWithAffixTypes lemmatizationInputWithAffixTypes1 = LemmatizationInputWithAffixTypes.of(Word.of("input"), List.of());
        LemmatizationInputWithAffixTypes lemmatizationInputWithAffixTypes2 = LemmatizationInputWithAffixTypes.of(Word.of("input2"), List.of());
        LemmatizationInputWithAffixTypes lemmatizationInputWithAffixTypes3 = LemmatizationInputWithAffixTypes.of(Word.of("input"), List.of(AffixType.of("<PLUR>")));

        assertThat(lemmatizationInputWithAffixTypes1).isEqualTo(lemmatizationInputWithAffixTypes1);
        assertThat(lemmatizationInputWithAffixTypes1).isNotEqualTo(null);
        assertThat(lemmatizationInputWithAffixTypes1).isNotEqualTo("string");
        assertThat(lemmatizationInputWithAffixTypes1).isNotEqualTo(lemmatizationInputWithAffixTypes2);
        assertThat(lemmatizationInputWithAffixTypes1).isNotEqualTo(lemmatizationInputWithAffixTypes3);
    }

    @Test
    public void testHashCode() {
        Word input = Word.of("input");
        List<AffixType> affixTypes = List.of(AffixType.of("<PLUR>"));
        LemmatizationInputWithAffixTypes lemmatizationInputWithAffixTypes = LemmatizationInputWithAffixTypes.of(input, affixTypes);
        int result = lemmatizationInputWithAffixTypes.hashCode();
        assertThat(result).isEqualTo(Objects.hash(Objects.hash(input), affixTypes));
    }

    @Test
    public void testToString() {
        Word input = Word.of("input");
        List<AffixType> affixTypes = List.of(AffixType.of("<PLUR>"));
        LemmatizationInputWithAffixTypes lemmatizationInputWithAffixTypes = LemmatizationInputWithAffixTypes.of(input, affixTypes);
        assertThat(lemmatizationInputWithAffixTypes).hasToString("LemmatizationInputWithAffixTypes[input=" + input + ", affixTypes=" + affixTypes + ']');
    }

}
