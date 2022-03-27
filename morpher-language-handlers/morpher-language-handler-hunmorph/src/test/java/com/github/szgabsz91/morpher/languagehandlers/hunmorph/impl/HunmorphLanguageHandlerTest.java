package com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl;

import com.github.szgabsz91.morpher.languagehandlers.api.model.AffixTypeChain;
import com.github.szgabsz91.morpher.languagehandlers.api.model.AnnotationTokenizerResult;
import com.github.szgabsz91.morpher.languagehandlers.api.model.LanguageHandlerResponse;
import com.github.szgabsz91.morpher.languagehandlers.api.model.LemmaMap;
import com.github.szgabsz91.morpher.languagehandlers.api.model.ProbabilisticAffixType;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.markov.FullMarkovModel;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.markov.IMarkovModel;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.model.HunmorphResult;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWord;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.protocolbuffers.HunmorphLanguageHandlerMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
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
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HunmorphLanguageHandlerTest {

    private HunmorphLanguageHandler languageHandler;
    private HunmorphWordProcessor wordProcessor;

    @BeforeEach
    public void setUp() {
        this.wordProcessor = mock(HunmorphWordProcessor.class);
        this.languageHandler = new HunmorphLanguageHandler(this.wordProcessor);
    }

    @AfterEach
    public void tearDown() {
        this.languageHandler.close();
    }

    @Test
    @SuppressWarnings("try")
    public void testDefaultConstructor() {
        try (HunmorphLanguageHandler languageHandler = new HunmorphLanguageHandler()) {
            // Do nothing
        }
    }

    @Test
    public void testGettersAndSetters() {
        Map<String, List<AnnotationTokenizerResult>> map = Map.of("x", List.of(new AnnotationTokenizerResult("expression", "grammatical", "lemma", 1)));
        assertThat(this.languageHandler.getAnnotationTokenizerResultMap()).isEmpty();
        this.languageHandler.setAnnotationTokenizerResultMap(map);
        assertThat(this.languageHandler.getAnnotationTokenizerResultMap()).isSameAs(map);
    }

    @Test
    public void testLearnAnnotationTokenizerResults() {
        Map<String, List<AnnotationTokenizerResult>> annotationTokenizerResults1 = Map.ofEntries(
                Map.entry("lemma1", new ArrayList<>(List.of(new AnnotationTokenizerResult("expression1", "grammatical1", "lemma1", 1)))),
                Map.entry("lemma2", new ArrayList<>(List.of(new AnnotationTokenizerResult("expression21", "grammatical21", "lemma21", 1))))
        );
        this.languageHandler.learnAnnotationTokenizerResults(annotationTokenizerResults1);

        Map<String, List<AnnotationTokenizerResult>> annotationTokenizerResults2 = Map.of(
                "lemma2", new ArrayList<>(List.of(new AnnotationTokenizerResult("expression22", "grammatical22", "lemma22", 1)))
        );
        this.languageHandler.learnAnnotationTokenizerResults(annotationTokenizerResults2);

        Map<String, List<AnnotationTokenizerResult>> expected = new HashMap<>(annotationTokenizerResults1);
        expected.get("lemma2").addAll(annotationTokenizerResults2.get("lemma2"));
        assertThat(this.languageHandler.getAnnotationTokenizerResultMap()).isEqualTo(expected);
    }

    @Test
    public void testLearnAffixTypeChains() {
        /*
         * - START:2
         *     - /POS1:1
         *         - AFF11:1
         *             - AFF12:1
         *                 - END:1
         *     - /POS2:1
         *         - AFF21:1
         *             - AFF22:1
         *                 - END:1
         */
        Set<List<AffixType>> affixTypeChains = Set.of(
                List.of(AffixType.of("/POS1"), AffixType.of("AFF11"), AffixType.of("AFF12")),
                List.of(AffixType.of("/POS2"), AffixType.of("AFF21"), AffixType.of("AFF22"))
        );
        this.languageHandler.learnAffixTypeChains(affixTypeChains);
        assertThat(this.languageHandler.calculateProbabilities(List.of(AffixType.of("AFF11"), AffixType.of("AFF12"))).getProbability()).isEqualTo(0.5);
        assertThat(this.languageHandler.calculateProbabilities(List.of(AffixType.of("AFF21"), AffixType.of("AFF22"))).getProbability()).isEqualTo(0.5);
    }

    @Test
    public void testLearnLemmas() {
        Map<Word, Set<AffixType>> lemmaMap = Map.ofEntries(
                Map.entry(Word.of("word1"), Set.of(AffixType.of("AFF1"))),
                Map.entry(Word.of("word2"), Set.of(AffixType.of("AFF2"), AffixType.of("AFF3")))
        );
        Map<String, Set<AffixType>> stringLemmaMap = lemmaMap.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey().toString(), entry.getValue()))
                .collect(toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        assertThat(this.languageHandler.getLemmaMap()).isEmpty();
        this.languageHandler.learnLemmas(LemmaMap.of(lemmaMap));
        assertThat(this.languageHandler.getLemmaMap()).isEqualTo(stringLemmaMap);
    }

    @Test
    public void testAnalyzeInternallyWithNoOutputLines() {
        FrequencyAwareWord input = FrequencyAwareWord.of("almákat");
        when(this.wordProcessor.process(input.toString()))
                .thenReturn(Optional.of(new HunmorphResult(input.toString(), List.of())));

        List<AnnotationTokenizerResult> annotationTokenizerResults = this.languageHandler.analyzeInternally(input);
        assertThat(annotationTokenizerResults).isEmpty();
    }

    @Test
    public void testAnalyzeInternallyWithOneOutputLine() {
        FrequencyAwareWord input = FrequencyAwareWord.of("almákat");
        String outputLine = "alma/NOUN<PLUR><CAS<ACC>>";
        when(this.wordProcessor.process(input.getWord().toString(), false))
                .thenReturn(Optional.of(new HunmorphResult(input.getWord().toString(), List.of(
                        outputLine
                ))));

        List<AnnotationTokenizerResult> annotationTokenizerResults = this.languageHandler.analyzeInternally(input);
        assertThat(annotationTokenizerResults).hasSize(1);
        AnnotationTokenizerResult annotationTokenizerResult = annotationTokenizerResults.get(0);
        assertThat(annotationTokenizerResult.getExpression()).isEqualTo(outputLine);
        assertThat(annotationTokenizerResult.getGrammaticalForm()).isEqualTo(input.getWord().toString());
        assertThat(annotationTokenizerResult.getLemma()).isEqualTo("alma");
        assertThat(annotationTokenizerResult.getAffixTypes()).containsExactly(
                AffixType.of("/NOUN"),
                AffixType.of("<PLUR>"),
                AffixType.of("<CAS<ACC>>")
        );
    }

    @Test
    public void testAnalyzeInternallyWithGuessMode() {
        FrequencyAwareWord input = FrequencyAwareWord.of("habablát");
        String outputLine = "hababla?NOUN<CAS<ACC>>";
        when(this.wordProcessor.process(input.getWord().toString(), true))
                .thenReturn(Optional.of(new HunmorphResult(input.getWord().toString(), List.of(
                        outputLine
                ))));

        List<AnnotationTokenizerResult> annotationTokenizerResults = this.languageHandler.analyzeInternally(input, true);
        assertThat(annotationTokenizerResults).hasSize(1);
        AnnotationTokenizerResult annotationTokenizerResult = annotationTokenizerResults.get(0);
        assertThat(annotationTokenizerResult.getExpression()).isEqualTo(outputLine);
        assertThat(annotationTokenizerResult.getGrammaticalForm()).isEqualTo(input.getWord().toString());
        assertThat(annotationTokenizerResult.getLemma()).isEqualTo("hababla");
        assertThat(annotationTokenizerResult.getAffixTypes()).containsExactly(
                AffixType.of("/NOUN"),
                AffixType.of("<CAS<ACC>>")
        );
    }

    @Test
    public void testAnalyzeWithOneWord() {
        when(this.wordProcessor.process("almáitokkal", false))
                .thenReturn(Optional.of(new HunmorphResult("almáitokkal", List.of("alma/NOUN<PLUR>"))));
        LanguageHandlerResponse response = this.languageHandler.analyze(FrequencyAwareWord.of("almáitokkal"));
        assertThat(response.getWordPairMap()).contains(Map.entry(AffixType.of("<PLUR>"), Set.of(FrequencyAwareWordPair.of("alma", "almáitokkal"))));
    }

    @Test
    public void testAnalyzeWithMultipleWords() {
        when(this.wordProcessor.process("almáitokkal"))
                .thenReturn(Optional.of(new HunmorphResult("almáitokkal", List.of("alma/NOUN<PLUR>"))));
        when(this.wordProcessor.process("tollat"))
                .thenReturn(Optional.of(new HunmorphResult("tollat", List.of("toll/NOUN<CAS<ACC>>"))));
        LanguageHandlerResponse response = this.languageHandler.analyze(Set.of(
                FrequencyAwareWord.of("almáitokkal"),
                FrequencyAwareWord.of("tollat")
        ));
        assertThat(response.getWordPairMap()).contains(
                Map.entry(AffixType.of("<PLUR>"), Set.of(FrequencyAwareWordPair.of("alma", "almáitokkal"))),
                Map.entry(AffixType.of("<CAS<ACC>>"), Set.of(FrequencyAwareWordPair.of("toll", "tollat")))
        );
    }

    @Test
    public void testAnalyzeWithMissingMiddle() {
        when(this.wordProcessor.process("x", false))
                .thenReturn(Optional.of(new HunmorphResult("x", List.of("a/NOUN<CAS<ACC>>"))));
        when(this.wordProcessor.process("xx", false))
                .thenReturn(Optional.of(new HunmorphResult("xx", List.of("a/NOUN<CAS<ACC>><PLUR>"))));
        when(this.wordProcessor.process("xxx", false))
                .thenReturn(Optional.of(new HunmorphResult("xxx", List.of("a/NOUN<CAS<ACC>><PLUR><CAS<INS>>"))));

        LanguageHandlerResponse response1 = this.languageHandler.analyze(FrequencyAwareWord.of("x"));
        assertThat(response1.getWordPairMap()).containsExactly(Map.entry(AffixType.of("<CAS<ACC>>"), Set.of(FrequencyAwareWordPair.of("a", "x"))));
        LanguageHandlerResponse response2 = this.languageHandler.analyze(FrequencyAwareWord.of("xxx"));
        assertThat(response2.getWordPairMap()).isEmpty();
        LanguageHandlerResponse response3 = this.languageHandler.analyze(FrequencyAwareWord.of("xx"));
        assertThat(response3.getWordPairMap()).containsExactly(
                Map.entry(AffixType.of("<PLUR>"), Set.of(FrequencyAwareWordPair.of("x", "xx"))),
                Map.entry(AffixType.of("<CAS<INS>>"), Set.of(FrequencyAwareWordPair.of("xx", "xxx")))
        );
    }

    @Test
    public void testAnalyzeWithAlreadyAnalyzedWord() {
        FrequencyAwareWord input = FrequencyAwareWord.of("x");
        when(this.wordProcessor.process(input.getWord().toString(), false))
                .thenReturn(Optional.of(new HunmorphResult(input.getWord().toString(), List.of("a/NOUN<CAS<ACC>>"))));

        FullMarkovModel markovModel = (FullMarkovModel) this.languageHandler.getMarkovModel();

        // First occurrence
        assertThat(this.languageHandler.analyze(input).getWordPairMap()).containsExactly(
                Map.entry(AffixType.of("<CAS<ACC>>"), Set.of(FrequencyAwareWordPair.of("a", "x")))
        );
        assertThat(markovModel.getCandidates(List.of(AffixType.of("/NOUN")))).containsExactly(
                ProbabilisticAffixType.of(AffixType.of("<CAS<ACC>>"), 1.0)
        );
        assertThat(this.languageHandler.getLemmaMap()).containsExactly(Map.entry("a", Set.of(AffixType.of("/NOUN"))));

        // Second occurrence
        assertThat(this.languageHandler.analyze(input).getWordPairMap()).isEmpty();
        assertThat(markovModel.getCandidates(List.of(AffixType.of("/NOUN")))).containsExactly(
                ProbabilisticAffixType.of(AffixType.of("<CAS<ACC>>"), 1.0)
        );
        assertThat(this.languageHandler.getLemmaMap()).containsExactly(
                Map.entry("a", Set.of(AffixType.of("/NOUN")))
        );

        // Third occurrence (with set)
        assertThat(this.languageHandler.analyze(Set.of(input)).getWordPairMap()).isEmpty();
        assertThat(markovModel.getCandidates(List.of(AffixType.of("/NOUN")))).containsExactly(
                ProbabilisticAffixType.of(AffixType.of("<CAS<ACC>>"), 1.0)
        );
        assertThat(this.languageHandler.getLemmaMap()).containsExactly(
                Map.entry("a", Set.of(AffixType.of("/NOUN")))
        );
    }

    @Test
    public void testGetAnalysisCandidates() {
        List<AffixType> affixTypes = List.of(AffixType.of("AFF1"), AffixType.of("AFF2"));
        List<ProbabilisticAffixType> expectedCandidates = Collections.singletonList(
                ProbabilisticAffixType.of(AffixType.of("AFF3"), 1.0)
        );

        IMarkovModel reversedMarkovModel = mock(IMarkovModel.class);
        this.languageHandler.setReversedMarkovModel(reversedMarkovModel);
        when(reversedMarkovModel.getCandidates(affixTypes))
                .thenReturn(expectedCandidates);

        List<ProbabilisticAffixType> result = this.languageHandler.getAnalysisCandidates(affixTypes);
        assertThat(result).isEqualTo(expectedCandidates);
        verify(reversedMarkovModel).getCandidates(affixTypes);
    }

    @Test
    public void testGetEndingAnalysisCandidateWithInvalidEnding() {
        List<AffixType> affixTypes = List.of(AffixType.of("AFF1"), AffixType.of("AFF2"));
        List<ProbabilisticAffixType> candidates = List.of(
                ProbabilisticAffixType.of(AffixType.of("AFF1"), 0.5),
                ProbabilisticAffixType.of(AffixType.of("AFF2"), 0.6)
        );

        IMarkovModel reversedMarkovModel = mock(IMarkovModel.class);
        when(reversedMarkovModel.getCandidates(affixTypes))
                .thenReturn(candidates);
        this.languageHandler.setReversedMarkovModel(reversedMarkovModel);

        ProbabilisticAffixType result = this.languageHandler.getEndingAnalysisCandidate(affixTypes);
        assertThat(result.getAffixType()).isEqualTo(IMarkovModel.END);
        assertThat(result.getProbability()).isZero();
        verify(reversedMarkovModel).getCandidates(affixTypes);
    }

    @Test
    public void testGetEndingAnalysisCandidateWithValidEnding() {
        List<AffixType> affixTypes = List.of(AffixType.of("AFF1"), AffixType.of("AFF2"));
        ProbabilisticAffixType endingProbabilisticAffixType = ProbabilisticAffixType.of(IMarkovModel.END, 0.6);
        List<ProbabilisticAffixType> candidates = List.of(
                ProbabilisticAffixType.of(AffixType.of("AFF1"), 0.5),
                endingProbabilisticAffixType
        );

        IMarkovModel reversedMarkovModel = mock(IMarkovModel.class);
        when(reversedMarkovModel.getCandidates(affixTypes))
                .thenReturn(candidates);
        this.languageHandler.setReversedMarkovModel(reversedMarkovModel);

        ProbabilisticAffixType result = this.languageHandler.getEndingAnalysisCandidate(affixTypes);
        assertThat(result).isEqualTo(endingProbabilisticAffixType);
        verify(reversedMarkovModel).getCandidates(affixTypes);
    }

    @Test
    public void testGetPOSCandidatesWithKnownLemma() {
        Word lemma = Word.of("lemma");

        when(this.wordProcessor.process("lemmákat", false))
                .thenReturn(Optional.of(new HunmorphResult("lemmákat", List.of(
                        lemma.toString() + "/NOUN<PLUR>",
                        lemma.toString() + "/VERB<PAST>"
                ))));
        when(this.wordProcessor.process("mást", false))
                .thenReturn(Optional.of(new HunmorphResult("mást", List.of(
                        "más/NOUN<PAST>"
                ))));
        this.languageHandler.analyze(FrequencyAwareWord.of("lemmákat"));
        this.languageHandler.analyze(FrequencyAwareWord.of("mást"));

        List<ProbabilisticAffixType> result = this.languageHandler.getPOSCandidates(lemma);
        assertThat(result).containsExactlyInAnyOrder(
                ProbabilisticAffixType.of(AffixType.of("/NOUN"), 2 / 3.0),
                ProbabilisticAffixType.of(AffixType.of("/VERB"), 1 / 3.0)
        );
    }

    @Test
    public void testGetPOSCandidatesWithUnknownLemma() {
        List<ProbabilisticAffixType> result = this.languageHandler.getPOSCandidates(Word.of("x"));
        assertThat(result).isEmpty();
    }

    @Test
    public void testIsPOS() {
        assertThat(this.languageHandler.isPOS(AffixType.of("NO"))).isFalse();
        assertThat(this.languageHandler.isPOS(AffixType.of("/YES"))).isTrue();
    }

    @Test
    public void testIsAffixTypeChainValid() {
        IMarkovModel markovModel = mock(IMarkovModel.class);
        this.languageHandler.setMarkovModel(markovModel);

        List<AffixType> affixTypes = List.of(
                AffixType.of("/VERB"),
                AffixType.of("<VPLUR>"),
                AffixType.of("<PAST>")
        );
        when(markovModel.isAffixTypeChainValid(affixTypes))
                .thenReturn(true);
        boolean result = this.languageHandler.isAffixTypeChainValid(affixTypes);

        assertThat(result).isTrue();
        verify(markovModel).isAffixTypeChainValid(affixTypes);
    }

    @Test
    public void testSortAffixTypes() {
        when(this.wordProcessor.process("bak"))
                .thenReturn(Optional.of(new HunmorphResult("bak", List.of("ba/NOUN<VPLUR><PAST>"))));
        when(this.wordProcessor.process("bakat"))
                .thenReturn(Optional.of(new HunmorphResult("bakat", List.of("ba/NOUN<VPLUR><PAST><CAS<ACC>><CAS<INS>>"))));
        when(this.wordProcessor.process("bakattal"))
                .thenReturn(Optional.of(new HunmorphResult("bakattal", List.of("ba/NOUN<VPLUR><PAST><CAS<ACC>><CAS<INS>><CAS<ALL>><CAS<ABL>>"))));
        when(this.wordProcessor.process("xyz"))
                .thenReturn(Optional.of(new HunmorphResult("xyz", List.of("x/VERB<VPLUR><PAST>"))));

        this.languageHandler.analyze(new LinkedHashSet<>(List.of(
                FrequencyAwareWord.of("bak"),
                FrequencyAwareWord.of("bakat"),
                FrequencyAwareWord.of("bakattal"),
                FrequencyAwareWord.of("xyz")
        )));

        IMarkovModel markovModel = mock(IMarkovModel.class);
        this.languageHandler.setMarkovModel(markovModel);

        Set<AffixType> affixTypeSet = new LinkedHashSet<>(List.of(
                AffixType.of("<VPLUR>"),
                AffixType.of("<PAST>")
        ));
        List<AffixTypeChain> expected = List.of(AffixTypeChain.of(Collections.emptyList(), -100.0));
        when(markovModel.sortAffixTypes(eq(affixTypeSet), any()))
                .thenReturn(expected);
        List<AffixTypeChain> result = this.languageHandler.sortAffixTypes(null, affixTypeSet);
        assertThat(result).isEqualTo(expected);
        verify(markovModel).sortAffixTypes(eq(affixTypeSet), any());
    }

    @Test
    public void testCalculateProbabilitiesWithKnownPos() {
        IMarkovModel markovModel = mock(IMarkovModel.class);
        this.languageHandler.setMarkovModel(markovModel);

        List<AffixType> affixTypes = List.of(AffixType.of("AFF1"), AffixType.of("AFF2"));
        List<ProbabilisticAffixType> probabilisticAffixTypes = new ArrayList<>(List.of(
                ProbabilisticAffixType.of(AffixType.of("/NOUN"), 1.0),
                ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5)
        ));
        AffixTypeChain expected = AffixTypeChain.of(probabilisticAffixTypes, 0.2);
        when(markovModel.calculateProbabilities(affixTypes))
                .thenReturn(expected);

        AffixTypeChain result = this.languageHandler.calculateProbabilities(affixTypes);
        assertThat(result).isEqualTo(expected);
        assertThat(result.getAffixTypes().get(0).getAffixType()).isNotEqualTo(AffixType.of("/UNKNOWN"));
        verify(markovModel).calculateProbabilities(affixTypes);
    }

    @Test
    public void testCalculateProbabilitiesWithUnknownPos() {
        IMarkovModel markovModel = mock(IMarkovModel.class);
        this.languageHandler.setMarkovModel(markovModel);

        List<AffixType> affixTypes = List.of(AffixType.of("AFF1"), AffixType.of("AFF2"));
        List<ProbabilisticAffixType> probabilisticAffixTypes = new ArrayList<>(List.of(ProbabilisticAffixType.of(AffixType.of("AFF"), 0.5)));
        AffixTypeChain expected = AffixTypeChain.of(probabilisticAffixTypes, 0.2);
        when(markovModel.calculateProbabilities(affixTypes))
                .thenReturn(expected);

        AffixTypeChain result = this.languageHandler.calculateProbabilities(affixTypes);
        assertThat(result).isEqualTo(expected);
        assertThat(result.getAffixTypes().get(0)).isEqualTo(ProbabilisticAffixType.of(AffixType.of("/UNKNOWN"), 0.0));
        verify(markovModel).calculateProbabilities(affixTypes);
    }

    @Test
    public void testGetSupportedAffixTypes() {
        Set<AffixType> supportedAffixTypes = new HashSet<>(this.languageHandler.getSupportedAffixTypes());
        Set<AffixType> expected = HunmorphAnnotationTokenizer.KNOWN_TOKENS
                .stream()
                .filter(affixType -> !affixType.startsWith("/"))
                .map(AffixType::of)
                .collect(toUnmodifiableSet());
        assertThat(supportedAffixTypes).isEqualTo(expected);
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        when(this.wordProcessor.process("almát"))
                .thenReturn(Optional.of(new HunmorphResult("almát", List.of("alma/NOUN<CAS<ACC>>"))));
        this.languageHandler.analyze(FrequencyAwareWord.of("almát"));

        Any message = Any.pack(this.languageHandler.toMessage());

        try (HunmorphLanguageHandler languageHandler = new HunmorphLanguageHandler()) {
            languageHandler.fromMessage(message);
            assertThat(languageHandler.getAnnotationTokenizerResultMap()).isEqualTo(this.languageHandler.getAnnotationTokenizerResultMap());
        }
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(HunmorphLanguageHandlerMessage.newBuilder().build()));
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> this.languageHandler.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not a HunmorphLanguageHandlerMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        when(this.wordProcessor.process("x"))
                .thenReturn(Optional.of(new HunmorphResult("x", List.of("a/NOUN<CAS<ACC>>"))));
        when(this.wordProcessor.process("xx"))
                .thenReturn(Optional.of(new HunmorphResult("xx", List.of("a/NOUN<CAS<ACC>><PLUR>"))));
        when(this.wordProcessor.process("xxx"))
                .thenReturn(Optional.of(new HunmorphResult("xxx", List.of("a/NOUN<CAS<ACC>><PLUR><CAS<INS>>"))));

        this.languageHandler.analyze(FrequencyAwareWord.of("x"));
        this.languageHandler.analyze(FrequencyAwareWord.of("xxx"));
        this.languageHandler.analyze(FrequencyAwareWord.of("xx"));

        Map<String, List<AnnotationTokenizerResult>> map = this.languageHandler.getAnnotationTokenizerResultMap();
        Path tempFile = Files.createTempFile("language-handler", "hunmorph");

        try {
            this.languageHandler.saveTo(tempFile);
            try (HunmorphLanguageHandler languageHandler = new HunmorphLanguageHandler()) {
                languageHandler.loadFrom(tempFile);
                assertThat(languageHandler.getAnnotationTokenizerResultMap()).isEqualTo(map);
            }
        }
        finally {
            Files.delete(tempFile);
        }
    }

}
