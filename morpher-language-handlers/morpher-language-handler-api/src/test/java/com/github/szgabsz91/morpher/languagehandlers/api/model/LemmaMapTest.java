package com.github.szgabsz91.morpher.languagehandlers.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
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

    @Test
    public void testEquals() {
        LemmaMap lemmaMap1 = LemmaMap.of(Map.of(Word.of("lemma1"), Set.of()));
        LemmaMap lemmaMap2 = LemmaMap.of(Map.of(Word.of("lemma2"), Set.of()));
        LemmaMap lemmaMap3 = LemmaMap.of(Map.of(Word.of("lemma1"), Set.of(AffixType.of("AFF"))));
        LemmaMap lemmaMap4 = LemmaMap.of(Map.of(Word.of("lemma1"), Set.of()));

        assertThat(lemmaMap1.equals(lemmaMap1)).isTrue();
        assertThat(lemmaMap1.equals(null)).isFalse();
        assertThat(lemmaMap1).isNotEqualTo("string");
        assertThat(lemmaMap1).isNotEqualTo(lemmaMap2);
        assertThat(lemmaMap1).isNotEqualTo(lemmaMap3);
        assertThat(lemmaMap1).isEqualTo(lemmaMap4);
    }

    @Test
    public void testHashCode() {
        LemmaMap lemmaMap = LemmaMap.of(Map.of(Word.of("lemma"), Set.of()));
        int result = lemmaMap.hashCode();
        int expected = Objects.hash(lemmaMap.entrySet().stream().collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToString() {
        LemmaMap lemmaMap = LemmaMap.of(Map.of(Word.of("lemma"), Set.of()));
        assertThat(lemmaMap).hasToString("LemmaMap[" + lemmaMap.entrySet().stream().collect(toMap(Map.Entry::getKey, Map.Entry::getValue)) + "]");
    }

}
