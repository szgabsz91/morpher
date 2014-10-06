package com.github.szgabsz91.morpher.analyzeragents.hunmorph.impl;

import com.github.szgabsz91.morpher.analyzeragents.api.model.AnalyzerAgentResponse;
import com.github.szgabsz91.morpher.analyzeragents.api.model.AnnotationTokenizerResult;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWord;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

public class HunmorphAnalyzerAgentFunctionalTest {

    private static final List<FrequencyAwareWord> WORDS = List.of(
            FrequencyAwareWord.of("almáitokkal"),
            FrequencyAwareWord.of("alma"),
            FrequencyAwareWord.of("almák"),
            FrequencyAwareWord.of("almái"),
            FrequencyAwareWord.of("almáitok"),
            FrequencyAwareWord.of("almákat"),
            FrequencyAwareWord.of("almákkal"),
            FrequencyAwareWord.of("almát")
    );

    private HunmorphAnalyzerAgent agent;
    private Path temporaryFile;

    @BeforeEach
    public void setUp() throws IOException {
        this.agent = new HunmorphAnalyzerAgent();
        this.temporaryFile = Files.createTempFile("agent", "hunmorph");
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.delete(this.temporaryFile);
        this.agent.close();
    }

    @Test
    public void testBatch() throws IOException {
        List<FrequencyAwareWord> wordList = new ArrayList<>(WORDS);
        Collections.shuffle(wordList);
        Set<FrequencyAwareWord> words = new LinkedHashSet<>(wordList);
        AnalyzerAgentResponse response = this.agent.analyze(words);
        this.reload();
        assertResult(response);
    }

    @Test
    public void testPartialBatch() throws IOException {
        List<FrequencyAwareWord> wordList = new ArrayList<>(WORDS);
        Collections.shuffle(wordList);
        Set<FrequencyAwareWord> words1 = new LinkedHashSet<>(wordList.subList(0, 3));
        Set<FrequencyAwareWord> words2 = new LinkedHashSet<>(wordList.subList(3, 6));
        Set<FrequencyAwareWord> words3 = new LinkedHashSet<>(wordList.subList(6, 8));
        Map<AffixType, Set<FrequencyAwareWordPair>> wordPairMap = new HashMap<>();
        for (Set<FrequencyAwareWord> words : List.of(words1, words2, words3)) {
            AnalyzerAgentResponse response = this.agent.analyze(words);
            this.reload();
            merge(wordPairMap, response.getWordPairMap());
        }
        assertResult(wordPairMap);
    }

    @Test
    public void testRegression() throws IOException {
        Set<FrequencyAwareWord> words1 = new LinkedHashSet<>(List.of(
                FrequencyAwareWord.of("almát"),
                FrequencyAwareWord.of("almákat"),
                FrequencyAwareWord.of("almáitok")
        ));
        Set<FrequencyAwareWord> words2 = new LinkedHashSet<>(List.of(
                FrequencyAwareWord.of("alma"),
                FrequencyAwareWord.of("almáitokkal"),
                FrequencyAwareWord.of("almákkal")
        ));
        Set<FrequencyAwareWord> words3 = new LinkedHashSet<>(List.of(
                FrequencyAwareWord.of("almák"),
                FrequencyAwareWord.of("almái")
        ));
        Map<AffixType, Set<FrequencyAwareWordPair>> wordPairMap = new HashMap<>();
        for (Set<FrequencyAwareWord> words : List.of(words1, words2, words3)) {
            AnalyzerAgentResponse response = this.agent.analyze(words);
            this.reload();
            merge(wordPairMap, response.getWordPairMap());
        }
        assertResult(wordPairMap);
    }

    @Test
    public void testWordByWord() throws IOException {
        List<FrequencyAwareWord> wordList = new ArrayList<>(WORDS);
        Collections.shuffle(wordList);
        Map<AffixType, Set<FrequencyAwareWordPair>> wordPairMap = new HashMap<>();
        for (FrequencyAwareWord word : wordList) {
            AnalyzerAgentResponse response = this.agent.analyze(word);
            this.reload();
            merge(wordPairMap, response.getWordPairMap());
        }
        assertResult(wordPairMap);
    }

    @Test
    public void testWordByWordUsingSingletonSets() throws IOException {
        List<FrequencyAwareWord> wordList = new ArrayList<>(WORDS);
        Collections.shuffle(wordList);
        Map<AffixType, Set<FrequencyAwareWordPair>> wordPairMap = new HashMap<>();
        for (FrequencyAwareWord word : wordList) {
            AnalyzerAgentResponse response = this.agent.analyze(Set.of(word));
            this.reload();
            merge(wordPairMap, response.getWordPairMap());
        }
        assertResult(wordPairMap);
    }

    @Test
    public void testWithUnknownWord() {
        AnalyzerAgentResponse response = this.agent.analyze(FrequencyAwareWord.of("xyz"));
        assertThat(response.getWordPairMap()).isEmpty();
    }

    @Test
    public void testAnalyzeInternallyRegression() {
        List<AnnotationTokenizerResult> annotationTokenizerResults = this.agent.analyzeInternally(FrequencyAwareWord.of("őrizetlenül"));
        assertThat(annotationTokenizerResults).hasSize(5);
    }

    private void assertResult(AnalyzerAgentResponse response) {
        assertResult(response.getWordPairMap());
    }

    private void assertResult(Map<AffixType, Set<FrequencyAwareWordPair>> wordPairMap) {
        assertThat(wordPairMap).containsExactly(
                entry(AffixType.of("<PLUR>"), Set.of(
                        FrequencyAwareWordPair.of("alma", "almák")
                )),
                entry(AffixType.of("<CAS<INS>>"), Set.of(
                        FrequencyAwareWordPair.of("almáitok", "almáitokkal"),
                        FrequencyAwareWordPair.of("almák", "almákkal")
                )),
                entry(AffixType.of("<CAS<ACC>>"), Set.of(
                        FrequencyAwareWordPair.of("alma", "almát"),
                        FrequencyAwareWordPair.of("almák", "almákat")
                )),
                entry(AffixType.of("<PLUR><POSS<2><PLUR>>"), Set.of(
                        FrequencyAwareWordPair.of("alma", "almáitok")
                )),
                entry(AffixType.of("<POSS>"), Set.of(
                        FrequencyAwareWordPair.of("alom", "alma")
                )),
                entry(AffixType.of("<PLUR><POSS>"), Set.of(
                        FrequencyAwareWordPair.of("alma", "almái")
                ))
        );
    }

    private static <K, V> void merge(Map<K, Set<V>> target, Map<K, Set<V>> source) {
        source.forEach((key, values) -> {
            Set<V> existingValues = target.computeIfAbsent(key, k -> new HashSet<>());
            existingValues.addAll(values);
        });
    }

    private void reload() throws IOException {
        this.agent.saveTo(this.temporaryFile);
        this.agent.close();
        this.agent = new HunmorphAnalyzerAgent();
        this.agent.loadFrom(this.temporaryFile);
    }

}
