package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.engines.api.model.MorpherEngineResponse;
import com.github.szgabsz91.morpher.languagehandlers.api.model.ProbabilisticAffixType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MorpherSystemResponseTest {

    @Test
    public void testConstructorAndGetters() {
        Language language = Language.of("code");
        List<MorpherEngineResponse> morpherEngineResponses = List.of(createMorpherEngineResponse());
        MorpherSystemResponse morpherSystemResponse = new MorpherSystemResponse(language, morpherEngineResponses);
        assertThat(morpherSystemResponse.getLanguage()).isEqualTo(language);
        assertThat(morpherSystemResponse.getMorpherEngineResponses()).isEqualTo(morpherEngineResponses);
    }

    private static MorpherEngineResponse createMorpherEngineResponse() {
        return MorpherEngineResponse.inflectionResponse(
                Word.of("input"),
                Word.of("output"),
                ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5),
                0.5,
                List.of()
        );
    }

}
