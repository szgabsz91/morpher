package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class AnalysisInputWithAffixTypesTest {

    @Test
    public void testOfAndGetters() {
        Word input = Word.of("input");
        List<AffixType> affixTypes = List.of(AffixType.of("<PLUR>"));
        AnalysisInputWithAffixTypes analysisInputWithAffixTypes = AnalysisInputWithAffixTypes.of(input, affixTypes);
        assertThat(analysisInputWithAffixTypes.getInput()).isEqualTo(input);
        assertThat(analysisInputWithAffixTypes.getAffixTypes()).isEqualTo(affixTypes);
    }

    @Test
    public void testEquals() {
        AnalysisInputWithAffixTypes analysisInputWithAffixTypes1 = AnalysisInputWithAffixTypes.of(Word.of("input"), List.of());
        AnalysisInputWithAffixTypes analysisInputWithAffixTypes2 = AnalysisInputWithAffixTypes.of(Word.of("input2"), List.of());
        AnalysisInputWithAffixTypes analysisInputWithAffixTypes3 = AnalysisInputWithAffixTypes.of(Word.of("input"), List.of(AffixType.of("<PLUR>")));

        assertThat(analysisInputWithAffixTypes1).isEqualTo(analysisInputWithAffixTypes1);
        assertThat(analysisInputWithAffixTypes1).isNotEqualTo(null);
        assertThat(analysisInputWithAffixTypes1).isNotEqualTo("string");
        assertThat(analysisInputWithAffixTypes1).isNotEqualTo(analysisInputWithAffixTypes2);
        assertThat(analysisInputWithAffixTypes1).isNotEqualTo(analysisInputWithAffixTypes3);
    }

    @Test
    public void testHashCode() {
        Word input = Word.of("input");
        List<AffixType> affixTypes = List.of(AffixType.of("<PLUR>"));
        AnalysisInputWithAffixTypes analysisInputWithAffixTypes = AnalysisInputWithAffixTypes.of(input, affixTypes);
        int result = analysisInputWithAffixTypes.hashCode();
        assertThat(result).isEqualTo(Objects.hash(Objects.hash(input), affixTypes));
    }

    @Test
    public void testToString() {
        Word input = Word.of("input");
        List<AffixType> affixTypes = List.of(AffixType.of("<PLUR>"));
        AnalysisInputWithAffixTypes analysisInputWithAffixTypes = AnalysisInputWithAffixTypes.of(input, affixTypes);
        assertThat(analysisInputWithAffixTypes).hasToString("AnalysisInputWithAffixTypes[input=" + input + ", affixTypes=" + affixTypes + ']');
    }

}
