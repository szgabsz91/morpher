package com.github.szgabsz91.morpher.languagehandlers.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class LemmaMapTest {

    @Test
    public void testEntrySet() {
        Map<Word, Set<AffixType>> map = Map.of(
                Word.of("a"), Set.of(AffixType.of("/AFF1"), AffixType.of("/AFF2")),
                Word.of("b"), Set.of(AffixType.of("/AFF3"), AffixType.of("/AFF4"))
        );
        LemmaMap lemmaMap = LemmaMap.of(map);
        Set<Map.Entry<Word, Set<AffixType>>> entrySet = lemmaMap.entrySet();
        assertThat(entrySet).containsExactlyInAnyOrder(
                Map.entry(Word.of("a"), Set.of(AffixType.of("/AFF1"), AffixType.of("/AFF2"))),
                Map.entry(Word.of("b"), Set.of(AffixType.of("/AFF3"), AffixType.of("/AFF4")))
        );
    }

}
