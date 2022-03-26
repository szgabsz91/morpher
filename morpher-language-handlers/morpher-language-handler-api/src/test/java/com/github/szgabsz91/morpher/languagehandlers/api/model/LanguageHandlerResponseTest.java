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

}
