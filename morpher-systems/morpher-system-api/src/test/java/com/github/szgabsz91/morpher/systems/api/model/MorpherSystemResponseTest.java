package com.github.szgabsz91.morpher.systems.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.engines.api.model.MorpherEngineResponse;
import com.github.szgabsz91.morpher.languagehandlers.api.model.ProbabilisticAffixType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

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

    @Test
    public void testEquals() {
        MorpherSystemResponse morpherSystemResponse1 = new MorpherSystemResponse(Language.of("code1"), List.of(createMorpherEngineResponse()));
        MorpherSystemResponse morpherSystemResponse2 = new MorpherSystemResponse(Language.of("code2"), List.of(createMorpherEngineResponse()));
        MorpherSystemResponse morpherSystemResponse3 = new MorpherSystemResponse(Language.of("code1"), List.of(createMorpherEngineResponse(), createMorpherEngineResponse()));
        MorpherSystemResponse morpherSystemResponse4 = new MorpherSystemResponse(Language.of("code1"), List.of(createMorpherEngineResponse()));

        assertThat(morpherSystemResponse1).isEqualTo(morpherSystemResponse1);
        assertThat(morpherSystemResponse1).isNotEqualTo(null);
        assertThat(morpherSystemResponse1).isNotEqualTo("string");
        assertThat(morpherSystemResponse1).isNotEqualTo(morpherSystemResponse2);
        assertThat(morpherSystemResponse1).isNotEqualTo(morpherSystemResponse3);
        assertThat(morpherSystemResponse1).isEqualTo(morpherSystemResponse4);
    }

    @Test
    public void testHashCode() {
        MorpherSystemResponse morpherSystemResponse = new MorpherSystemResponse(Language.of("code"), List.of(createMorpherEngineResponse()));
        int result = morpherSystemResponse.hashCode();
        int expected = Objects.hash(morpherSystemResponse.getLanguage(), morpherSystemResponse.getMorpherEngineResponses());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        MorpherSystemResponse morpherSystemResponse = new MorpherSystemResponse(Language.of("code"), List.of(createMorpherEngineResponse()));
        assertThat(morpherSystemResponse).hasToString("MorpherSystemResponse[language=" + morpherSystemResponse.getLanguage() + ", morpherEngineResponses=" + morpherSystemResponse.getMorpherEngineResponses() + "]");
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
