package com.github.szgabsz91.morpher.languagehandlers.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class LanguageHandlerResponseTest {

    @Test
    public void testOfAndGetter() {
        Map<AffixType, Set<FrequencyAwareWordPair>> map = Map.of(AffixType.of("AFF"), Set.of(FrequencyAwareWordPair.of("x", "y")));
        LanguageHandlerResponse response = LanguageHandlerResponse.of(map);
        assertThat(response.getWordPairMap()).isSameAs(map);
    }

    @Test
    public void testEquals() {
        LanguageHandlerResponse response1 = LanguageHandlerResponse.of(Map.of(AffixType.of("AFF"), Set.of(FrequencyAwareWordPair.of("x", "y"))));
        LanguageHandlerResponse response2 = LanguageHandlerResponse.of(Map.of(AffixType.of("AFF2"), Set.of(FrequencyAwareWordPair.of("x", "y"))));
        LanguageHandlerResponse response3 = LanguageHandlerResponse.of(Map.of(AffixType.of("AFF"), Set.of(FrequencyAwareWordPair.of("x", "y"))));

        assertThat(response1).isEqualTo(response1);
        assertThat(response1).isNotEqualTo(null);
        assertThat(response1).isNotEqualTo("string");
        assertThat(response1).isNotEqualTo(response2);
        assertThat(response1).isEqualTo(response3);
    }

    @Test
    public void testHashCode() {
        LanguageHandlerResponse response = LanguageHandlerResponse.of(Map.of(AffixType.of("AFF"), Set.of(FrequencyAwareWordPair.of("x", "y"))));
        int result = response.hashCode();
        assertThat(result).isEqualTo(response.getWordPairMap().hashCode());
    }

    @Test
    public void testToString() {
        LanguageHandlerResponse response = LanguageHandlerResponse.of(Map.of(AffixType.of("AFF"), Set.of(FrequencyAwareWordPair.of("x", "y"))));
        assertThat(response).hasToString("LanguageHandlerResponse[wordPairMap=" + response.getWordPairMap() + "]");
    }

}
