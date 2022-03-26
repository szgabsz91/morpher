package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.List;

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

}
