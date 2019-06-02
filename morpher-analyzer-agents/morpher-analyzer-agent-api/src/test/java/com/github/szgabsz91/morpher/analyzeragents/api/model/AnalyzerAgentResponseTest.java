package com.github.szgabsz91.morpher.analyzeragents.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.WordPair;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AnalyzerAgentResponseTest {

    @Test
    public void testOfAndGetter() {
        Map<AffixType, Set<FrequencyAwareWordPair>> map = Map.of(AffixType.of("AFF"), Set.of(FrequencyAwareWordPair.of("x", "y")));
        AnalyzerAgentResponse response = AnalyzerAgentResponse.of(map);
        assertThat(response.getWordPairMap()).isSameAs(map);
    }

    @Test
    public void testEquals() {
        AnalyzerAgentResponse response1 = AnalyzerAgentResponse.of(Map.of(AffixType.of("AFF"), Set.of(FrequencyAwareWordPair.of("x", "y"))));
        AnalyzerAgentResponse response2 = AnalyzerAgentResponse.of(Map.of(AffixType.of("AFF2"), Set.of(FrequencyAwareWordPair.of("x", "y"))));
        AnalyzerAgentResponse response3 = AnalyzerAgentResponse.of(Map.of(AffixType.of("AFF"), Set.of(FrequencyAwareWordPair.of("x", "y"))));

        assertThat(response1).isEqualTo(response1);
        assertThat(response1).isNotEqualTo(null);
        assertThat(response1).isNotEqualTo("string");
        assertThat(response1).isNotEqualTo(response2);
        assertThat(response1).isEqualTo(response3);
    }

    @Test
    public void testHashCode() {
        AnalyzerAgentResponse response = AnalyzerAgentResponse.of(Map.of(AffixType.of("AFF"), Set.of(FrequencyAwareWordPair.of("x", "y"))));
        int result = response.hashCode();
        assertThat(result).isEqualTo(response.getWordPairMap().hashCode());
    }

    @Test
    public void testToString() {
        AnalyzerAgentResponse response = AnalyzerAgentResponse.of(Map.of(AffixType.of("AFF"), Set.of(FrequencyAwareWordPair.of("x", "y"))));
        assertThat(response).hasToString("AnalyzerAgentResponse[wordPairMap=" + response.getWordPairMap() + "]");
    }

}
