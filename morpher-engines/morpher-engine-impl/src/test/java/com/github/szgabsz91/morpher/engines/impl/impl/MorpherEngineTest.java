package com.github.szgabsz91.morpher.engines.impl.impl;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Corpus;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWord;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.core.services.ServiceProvider;
import com.github.szgabsz91.morpher.engines.api.model.*;
import com.github.szgabsz91.morpher.engines.impl.impl.probability.IProbabilityCalculator;
import com.github.szgabsz91.morpher.engines.impl.impl.probability.MultiplyProbabilityCalculator;
import com.github.szgabsz91.morpher.engines.impl.protocolbuffers.MorpherEngineMessage;
import com.github.szgabsz91.morpher.engines.impl.transformationengineholderfactories.EagerTransformationEngineHolderFactory;
import com.github.szgabsz91.morpher.engines.impl.transformationengineholderfactories.ITransformationEngineHolderFactory;
import com.github.szgabsz91.morpher.engines.impl.transformationengineholderfactories.LazyTransformationEngineHolderFactory;
import com.github.szgabsz91.morpher.languagehandlers.api.ILanguageHandler;
import com.github.szgabsz91.morpher.languagehandlers.api.model.AnnotationTokenizerResult;
import com.github.szgabsz91.morpher.languagehandlers.api.model.LemmaMap;
import com.github.szgabsz91.morpher.languagehandlers.api.model.ProbabilisticAffixType;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.HunmorphAnnotationTokenizer;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.HunmorphLanguageHandler;
import com.github.szgabsz91.morpher.transformationengines.api.factories.IAbstractTransformationEngineFactory;
import com.github.szgabsz91.morpher.transformationengines.astra.config.ASTRATransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.astra.config.SearcherType;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.transformationengine.ASTRAAbstractTransformationEngineFactory;
import com.github.szgabsz91.morpher.transformationengines.astra.protocolbuffers.ASTRATransformationEngineMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MorpherEngineTest {

    private MorpherEngine engine;
    private Path temporaryFolder;

    @BeforeEach
    public void setUp() {
        this.engine = this.createMorpherEngine();
        this.temporaryFolder = Paths.get("build/morpher-engine");
    }

    @AfterEach
    public void tearDown() throws IOException {
        this.engine.close();
        Files.walkFileTree(this.temporaryFolder, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path folder, final IOException exception) throws IOException {
                Files.delete(folder);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Test
    public void testIsEager() throws IOException {
        this.reload();

        boolean result = this.engine.isEager();
        assertThat(result).isTrue();
    }

    @Test
    public void testIsLazy() throws IOException {
        this.reload();

        boolean result = this.engine.isLazy();
        assertThat(result).isFalse();
    }

    @Test
    public void testLearnWithPreanalyzedTrainingItemsWithNonEmptyWordPairMap() throws IOException {
        AnnotationTokenizerResult annotationTokenizerResult = new AnnotationTokenizerResult("alma/NOUN<CAS<ACC>>", "almát", "alma", 1);
        annotationTokenizerResult.addAffixType(AffixType.of("/NOUN"));
        annotationTokenizerResult.addAffixType(AffixType.of("<CAS<ACC>>"));
        Set<PreanalyzedTrainingItem> preanalyzedTrainingItems = Set.of(
                new PreanalyzedTrainingItem(annotationTokenizerResult, FrequencyAwareWordPair.of("alma", "almát"))
        );
        this.engine.learn(PreanalyzedTrainingItems.of(preanalyzedTrainingItems));

        assertThat(this.engine.isDirty()).isTrue();
        this.reload();

        List<MorpherEngineResponse> responses = this.engine.analyze(AnalysisInput.of(Word.of("almát")));
        assertThat(responses).hasSize(1);
        MorpherEngineResponse response = responses.get(0);
        assertThat(response.getOutput()).isEqualTo(Word.of("alma"));
        assertThat(response.getSteps()).hasSize(1);
        ProbabilisticStep step = response.getSteps().get(0);
        assertThat(step.getAffixType()).isEqualTo(AffixType.of("<CAS<ACC>>"));
        assertThat(response.getPos().getAffixType()).isEqualTo(AffixType.of("/NOUN"));
    }

    @Test
    public void testLearnWithPreanalyzedTrainingItemsWithEmptyWordPairMap() throws IOException {
        Set<PreanalyzedTrainingItem> preanalyzedTrainingItems = Collections.emptySet();
        this.engine.learn(PreanalyzedTrainingItems.of(preanalyzedTrainingItems));

        assertThat(this.engine.isDirty()).isFalse();
        this.reload();

        List<MorpherEngineResponse> responses = this.engine.analyze(AnalysisInput.of(Word.of("almát")));
        assertThat(responses).isEmpty();
    }

    @Test
    public void testLearnWithLemmas() throws IOException {
        Map<Word, Set<AffixType>> lemmaMap = Map.ofEntries(
                Map.entry(Word.of("word1"), Set.of(AffixType.of("AFF1"))),
                Map.entry(Word.of("word2"), Set.of(AffixType.of("AFF2"), AffixType.of("AFF3")))
        );
        Map<String, Set<AffixType>> stringLemmaMap = lemmaMap.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey().toString(), entry.getValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        HunmorphLanguageHandler languageHandler = (HunmorphLanguageHandler) this.engine.getLanguageHandler();
        assertThat(languageHandler.getLemmaMap()).isEmpty();
        this.engine.learn(LemmaMap.of(lemmaMap));
        this.reload();
        assertThat(languageHandler.getLemmaMap()).isEqualTo(stringLemmaMap);
    }

    @Test
    public void testAnalyzeWithKnownPosOfLemma() throws IOException {
        /*
         * - START:6
         *     - <PLUR>:2
         *         - /NOUN:2
         *             - END:2
         *     - [PERF_PART]:1
         *         - /VERB:1
         *             - END:1
         *     - <PAST>:1
         *         - /VERB:1
         *             - END:1
         *     - <CAS<ACC>>:2
         *         - <PLUR>:2
         *             - /NOUN:2
         *                 - END:2
         */
        this.engine.learn(Corpus.of(Set.of(
                FrequencyAwareWord.of("almákat"),
                FrequencyAwareWord.of("malmokat"),
                FrequencyAwareWord.of("almák")
        )));
        this.engine.learn(Corpus.of(Word.of("malmok")));
        this.engine.learn(Corpus.of(Word.of("ugrott")));
        this.reload();

        Word input = Word.of("almákat");
        List<MorpherEngineResponse> result = this.engine.analyze(AnalysisInput.of(input));

        assertThat(result).hasSize(1);
        MorpherEngineResponse response = result.get(0);
        assertThat(response.getMode()).isEqualTo(Mode.ANALYSIS);
        assertThat(response.getInput()).isEqualTo(input);
        assertThat(response.getOutput()).isEqualTo(Word.of("alma"));
        assertThat(response.getPos()).isEqualTo(ProbabilisticAffixType.of(AffixType.of("/NOUN"), 1.0));
        assertThat(response.getAffixTypeChainProbability()).isEqualTo(1 / 3.0);
        assertThat(response.getSteps()).hasSize(2);
        ProbabilisticStep step1 = response.getSteps().get(0);
        assertThat(step1.getInput()).isEqualTo(Word.of("almákat"));
        assertThat(step1.getOutput()).isEqualTo(Word.of("almák"));
        assertThat(step1.getAffixType()).isEqualTo(AffixType.of("<CAS<ACC>>"));
        assertThat(step1.getAffixTypeProbability()).isEqualTo(1 / 3.0);
        assertThat(step1.getOutputWordProbability()).isOne();
        assertThat(step1.getAggregatedProbability()).isEqualTo(1 / 3.0);
        ProbabilisticStep step2 = response.getSteps().get(1);
        assertThat(step2.getInput()).isEqualTo(Word.of("almák"));
        assertThat(step2.getOutput()).isEqualTo(Word.of("alma"));
        assertThat(step2.getAffixType()).isEqualTo(AffixType.of("<PLUR>"));
        assertThat(step2.getAffixTypeProbability()).isOne();
        assertThat(step2.getOutputWordProbability()).isOne();
        assertThat(step2.getAggregatedProbability()).isOne();
    }

    @Test
    public void testAnalyzeWithNoResponse() throws IOException {
        this.engine.learn(Corpus.of(Set.of(FrequencyAwareWord.of("almát"))));
        this.reload();

        Word input = Word.of("balmát");
        List<MorpherEngineResponse> result = this.engine.analyze(AnalysisInput.of(input));
        assertThat(result).isEmpty();
    }

    @Test
    public void testInflectWithOrderedInput() throws IOException {
        this.engine.learn(Corpus.of(Set.of(
                FrequencyAwareWord.of("almákat"),
                FrequencyAwareWord.of("malmokat"),
                FrequencyAwareWord.of("almák")
        )));
        this.engine.learn(Corpus.of(Word.of("malmok")));
        this.reload();

        Word input = Word.of("alma");
        List<MorpherEngineResponse> responses = this.engine.inflect(new InflectionOrderedInput(input, List.of(AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>"))));
        assertThat(responses).hasSize(1);
        MorpherEngineResponse response = responses.get(0);
        assertThat(response.getMode()).isEqualTo(Mode.INFLECTION);
        assertThat(response.getInput()).isEqualTo(input);
        assertThat(response.getOutput()).isEqualTo(Word.of("almákat"));
        assertThat(response.getPos()).isEqualTo(ProbabilisticAffixType.of(AffixType.of("/NOUN"), 1.0));
        assertThat(response.getAffixTypeChainProbability()).isEqualTo(0.5);
        assertThat(response.getSteps()).hasSize(2);
        ProbabilisticStep step1 = response.getSteps().get(0);
        assertThat(step1.getInput()).isEqualTo(Word.of("alma"));
        assertThat(step1.getOutput()).isEqualTo(Word.of("almák"));
        assertThat(step1.getAffixType()).isEqualTo(AffixType.of("<PLUR>"));
        assertThat(step1.getAffixTypeProbability()).isOne();
        assertThat(step1.getOutputWordProbability()).isOne();
        assertThat(step1.getAggregatedProbability()).isOne();
        ProbabilisticStep step2 = response.getSteps().get(1);
        assertThat(step2.getInput()).isEqualTo(Word.of("almák"));
        assertThat(step2.getOutput()).isEqualTo(Word.of("almákat"));
        assertThat(step2.getAffixType()).isEqualTo(AffixType.of("<CAS<ACC>>"));
        assertThat(step2.getAffixTypeProbability()).isEqualTo(0.5);
        assertThat(step2.getOutputWordProbability()).isOne();
        assertThat(step2.getAggregatedProbability()).isEqualTo(0.5);
    }

    @Test
    public void testInflectWithUnorderedInput() throws IOException {
        /*
         * - START:6
         *     - /VERB:2
         *         - [PERF_PART]:1
         *             - END:1
         *         - <PAST>:1
         *             - END:1
         *     - /NOUN:4
         *         - <PLUR>:4
         *             - <CAS<ACC>>:2
         *                 - END:2
         *             - END:2
         */
        this.engine.learn(Corpus.of(Set.of(
                FrequencyAwareWord.of("almákat"),
                FrequencyAwareWord.of("malmokat"),
                FrequencyAwareWord.of("almák"),
                FrequencyAwareWord.of("futott")
        )));
        this.engine.learn(Corpus.of(Word.of("malmok")));
        this.reload();

        Word input = Word.of("alma");
        List<MorpherEngineResponse> result = this.engine.inflect(new InflectionInput(input, new LinkedHashSet<>(List.of(AffixType.of("<CAS<ACC>>"), AffixType.of("<PLUR>")))));
        assertThat(result).hasSize(1);
        MorpherEngineResponse response = result.get(0);
        assertThat(response.getMode()).isEqualTo(Mode.INFLECTION);
        assertThat(response.getInput()).isEqualTo(input);
        assertThat(response.getOutput()).isEqualTo(Word.of("almákat"));
        assertThat(response.getPos()).isEqualTo(ProbabilisticAffixType.of(AffixType.of("/NOUN"), 2 / 3.0));
        assertThat(response.getAffixTypeChainProbability()).isEqualTo(1 / 3.0);
        assertThat(response.getSteps()).hasSize(2);
        ProbabilisticStep step1 = response.getSteps().get(0);
        assertThat(step1.getInput()).isEqualTo(Word.of("alma"));
        assertThat(step1.getOutput()).isEqualTo(Word.of("almák"));
        assertThat(step1.getAffixType()).isEqualTo(AffixType.of("<PLUR>"));
        assertThat(step1.getAffixTypeProbability()).isOne();
        assertThat(step1.getOutputWordProbability()).isOne();
        assertThat(step1.getAggregatedProbability()).isOne();
        ProbabilisticStep step2 = response.getSteps().get(1);
        assertThat(step2.getInput()).isEqualTo(Word.of("almák"));
        assertThat(step2.getOutput()).isEqualTo(Word.of("almákat"));
        assertThat(step2.getAffixType()).isEqualTo(AffixType.of("<CAS<ACC>>"));
        assertThat(step2.getAffixTypeProbability()).isEqualTo(0.5);
        assertThat(step2.getOutputWordProbability()).isOne();
        assertThat(step2.getAggregatedProbability()).isEqualTo(0.5);
    }

    @Test
    public void testInflectWithUnorderedInputAndUnknownRoute() throws IOException {
        this.engine.learn(Corpus.of(Word.of("almák")));
        this.reload();

        List<MorpherEngineResponse> responses = this.engine.inflect(new InflectionInput(Word.of("toll"), Set.of(AffixType.of("<CAS<ACC>>"))));
        assertThat(responses).isEmpty();
    }

    @Test
    public void testInflectWithOrderedInputAndUnknownRoute() throws IOException {
        this.engine.learn(Corpus.of(Word.of("almák")));
        this.reload();

        Word word = Word.of("toll");
        List<MorpherEngineResponse> responses = this.engine.inflect(new InflectionOrderedInput(word, List.of(AffixType.of("<CAS<ACC>>"))));
        assertThat(responses).isEmpty();
    }

    @Test
    public void testInflectWithOrderedInputAndNonInflectableTemporaryWord() throws IOException {
        this.engine.learn(Corpus.of(Word.of("almát")));
        this.reload();

        List<MorpherEngineResponse> responses = this.engine.inflect(new InflectionOrderedInput(Word.of("alma"), List.of(AffixType.of("<CAS<ACC>>"), AffixType.of("<PAST>"))));
        assertThat(responses).isEmpty();
    }

    @Test
    public void testGetSupportedAffixTypes() throws IOException {
        this.reload();

        Set<AffixType> supportedAffixTypes = new HashSet<>(this.engine.getSupportedAffixTypes());
        Set<AffixType> expected = HunmorphAnnotationTokenizer.KNOWN_TOKENS
                .stream()
                .filter(affixType -> !affixType.startsWith("/"))
                .map(AffixType::of)
                .collect(toSet());
        assertThat(supportedAffixTypes).isEqualTo(expected);
    }

    @Test
    public void testIsDirty() throws IOException {
        // Originally not dirty
        assertThat(this.engine.isDirty()).isFalse();

        // Learning a word makes the engine dirty
        this.engine.learn(Corpus.of(Word.of("almát")));
        assertThat(this.engine.isDirty()).isTrue();

        // Saving the engine makes it not dirty
        this.engine.saveTo(this.temporaryFolder);
        assertThat(this.engine.isDirty()).isFalse();

        // Learning another word makes the engine dirty
        this.engine.learn(Corpus.of(Word.of("almák")));
        assertThat(this.engine.isDirty()).isTrue();

        // Loading the engine makes it not dirty
        this.engine.loadFrom(this.temporaryFolder);
        assertThat(this.engine.isDirty()).isFalse();

        // Learning the same word again does not make it dirty
        this.engine.learn(Corpus.of(Word.of("almát")));
        assertThat(this.engine.isDirty()).isFalse();

        // Learning something to make it dirty
        this.engine.learn(Corpus.of(Word.of("asztalt")));
        assertThat(this.engine.isDirty()).isTrue();

        // Clean it
        this.engine.clean();
        assertThat(this.engine.isDirty()).isFalse();
    }

    @Test
    public void testToMessageFromMessageWithMorpherEngineMessage() throws IOException {
        this.engine.learn(Corpus.of(Word.of("almát")));

        // Convert to message
        MorpherEngineMessage message = this.engine.toMessage();
        Any anyMessage = Any.pack(message);

        // Reload
        this.reload();

        // Convert back from message
        this.engine.fromMessage(anyMessage);
        assertThat(this.engine.isDirty()).isFalse();

        // Assert
        List<MorpherEngineResponse> responses = this.engine.inflect(new InflectionInput(Word.of("alma"), Set.of(AffixType.of("<CAS<ACC>>"))));
        assertThat(responses).hasSize(1);
        MorpherEngineResponse response = responses.get(0);
        assertThat(response.getOutput()).hasToString("almát");
    }

    @Test
    public void testFromMessageWithNonMorpherEngineMessage() throws IOException {
        // Reload
        this.reload();

        // Convert back from message
        Any anyMessage = Any.pack(ASTRATransformationEngineMessage.newBuilder().build());
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> this.engine.fromMessage(anyMessage));
        assertThat(exception.getMessage()).startsWith("The provided message is not a MorpherEngineMessage: ");
    }

    @Test
    public void testSerializeDeserialize() throws IOException {
        this.reload();
        Path file = Files.createTempFile("morpher", "engine");

        try {
            MorpherEngine engine = createMorpherEngine(true);
            engine.learn(Corpus.of(Word.of("almát")));
            boolean serializationResult = engine.serialize(file);
            assertThat(serializationResult).isTrue();

            MorpherEngine result = createMorpherEngine(true);
            boolean deserializationResult = result.deserialize(file);
            assertThat(deserializationResult).isTrue();

            List<MorpherEngineResponse> responses = result.analyze(AnalysisInput.of(Word.of("almát")));
            assertThat(responses).hasSize(1);
            MorpherEngineResponse response = responses.get(0);
            assertThat(response.getOutput()).hasToString("alma");
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testProbabilityCalculationForSimpleInflection() throws IOException {
        this.engine.learn(Corpus.of(Set.of(
                FrequencyAwareWord.of("alma"),
                FrequencyAwareWord.of("almát"),
                FrequencyAwareWord.of("almák"),
                FrequencyAwareWord.of("almákat"),
                FrequencyAwareWord.of("evett")
        )));
        this.reload();

        assertProbabilities(
                (inputWord, affixTypes) -> this.engine.inflect(new InflectionOrderedInput(inputWord, affixTypes)),
                affixTypes -> 0,
                (probabilisticAffixTypes, pos) -> probabilisticAffixTypes.add(0, pos),
                new AssertProbabilityModel(
                        WordPair.of("eszik", "evett"),
                        ProbabilisticAffixType.of(AffixType.of("/VERB"), 2 / 7.0),
                        ProbabilisticAffixType.of(AffixType.of("[PERF_PART]"), 0.5)
                ),
                new AssertProbabilityModel(
                        WordPair.of("eszik", "evett"),
                        ProbabilisticAffixType.of(AffixType.of("/VERB"), 2 / 7.0),
                        ProbabilisticAffixType.of(AffixType.of("<PAST>"), 0.5)
                ),
                new AssertProbabilityModel(
                        WordPair.of("alma", "almák"),
                        ProbabilisticAffixType.of(AffixType.of("/NOUN"), 5 / 7.0),
                        ProbabilisticAffixType.of(AffixType.of("<PLUR>"), 2 / 5.0)
                ),
                new AssertProbabilityModel(
                        WordPair.of("alma", "almákat"),
                        ProbabilisticAffixType.of(AffixType.of("/NOUN"), 5 / 7.0),
                        ProbabilisticAffixType.of(AffixType.of("<PLUR>"), 2 / 5.0),
                        ProbabilisticAffixType.of(AffixType.of("<CAS<ACC>>"), 0.5)
                ),
                new AssertProbabilityModel(
                        WordPair.of("alma", "almát"),
                        ProbabilisticAffixType.of(AffixType.of("/NOUN"), 5 / 7.0),
                        ProbabilisticAffixType.of(AffixType.of("<CAS<ACC>>"), 1 / 5.0)
                ),
                new AssertProbabilityModel(
                        WordPair.of("alom", "alma"),
                        ProbabilisticAffixType.of(AffixType.of("/NOUN"), 5 / 7.0),
                        ProbabilisticAffixType.of(AffixType.of("<POSS>"), 2 / 5.0)
                ),
                new AssertProbabilityModel(
                        WordPair.of("alom", "almát"),
                        ProbabilisticAffixType.of(AffixType.of("/NOUN"), 5 / 7.0),
                        ProbabilisticAffixType.of(AffixType.of("<POSS>"), 2 / 5.0),
                        ProbabilisticAffixType.of(AffixType.of("<CAS<ACC>>"), 0.5)
                )
        );
    }

    @Test
    public void testProbabilityCalculationForSimpleAnalysis() throws IOException {
        this.engine.learn(Corpus.of(Set.of(
                FrequencyAwareWord.of("alma"),
                FrequencyAwareWord.of("almát"),
                FrequencyAwareWord.of("almák"),
                FrequencyAwareWord.of("almákat"),
                FrequencyAwareWord.of("evett")
        )));
        this.reload();

        assertProbabilities(
                (inputWord, affixTypes) -> this.engine.analyze(AnalysisInput.of(inputWord)),
                affixTypes -> affixTypes.size() - 1,
                List::add,
                new AssertProbabilityModel(
                        WordPair.of("almák", "alma"),
                        ProbabilisticAffixType.of(AffixType.of("<PLUR>"), 1 / 7.0),
                        ProbabilisticAffixType.of(AffixType.of("/NOUN"), 1.0)
                ),
                new AssertProbabilityModel(
                        WordPair.of("evett", "eszik"),
                        ProbabilisticAffixType.of(AffixType.of("[PERF_PART]"), 1 / 7.0),
                        ProbabilisticAffixType.of(AffixType.of("/VERB"), 1.0)
                ),
                new AssertProbabilityModel(
                        WordPair.of("evett", "eszik"),
                        ProbabilisticAffixType.of(AffixType.of("<PAST>"), 1 / 7.0),
                        ProbabilisticAffixType.of(AffixType.of("/VERB"), 1.0)
                ),
                new AssertProbabilityModel(
                        WordPair.of("almát", "alma"),
                        ProbabilisticAffixType.of(AffixType.of("<CAS<ACC>>"), 3 / 7.0),
                        ProbabilisticAffixType.of(AffixType.of("/NOUN"), 1 / 3.0)
                ),
                new AssertProbabilityModel(
                        WordPair.of("almákat", "alma"),
                        ProbabilisticAffixType.of(AffixType.of("<CAS<ACC>>"), 3 / 7.0),
                        ProbabilisticAffixType.of(AffixType.of("<PLUR>"), 1 / 3.0),
                        ProbabilisticAffixType.of(AffixType.of("/NOUN"), 1.0)
                ),
                new AssertProbabilityModel(
                        WordPair.of("almát", "alom"),
                        ProbabilisticAffixType.of(AffixType.of("<CAS<ACC>>"), 3 / 7.0),
                        ProbabilisticAffixType.of(AffixType.of("<POSS>"), 1 / 3.0),
                        ProbabilisticAffixType.of(AffixType.of("/NOUN"), 1.0)
                ),
                new AssertProbabilityModel(
                        WordPair.of("alma", "alom"),
                        ProbabilisticAffixType.of(AffixType.of("<POSS>"), 1 / 7.0),
                        ProbabilisticAffixType.of(AffixType.of("/NOUN"), 1.0)
                )
        );
    }

    @Test
    public void testAnalyzeWithAffixTypes() throws IOException {
        this.engine.learn(Corpus.of(Set.of(
                FrequencyAwareWord.of("alma"),
                FrequencyAwareWord.of("almát"),
                FrequencyAwareWord.of("almák"),
                FrequencyAwareWord.of("almákat"),
                FrequencyAwareWord.of("evett")
        )));
        this.reload();

        Word input = Word.of("almákat");
        List<AffixType> affixTypes = List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>"));
        AnalysisInputWithAffixTypes analysisInputWithAffixTypes = AnalysisInputWithAffixTypes.of(input, affixTypes);
        List<MorpherEngineResponse> analysisResponses = this.engine.analyze(analysisInputWithAffixTypes);

        assertThat(analysisResponses).hasSize(1);
        MorpherEngineResponse analysisResponse = analysisResponses.get(0);
        assertThat(analysisResponse.getOutput()).hasToString("alma");
    }

    @Test
    public void testInflectWithMinimumAggregatedWeightThreshold() throws IOException {
        MorpherEngine engine = createMorpherEngine(0.8);
        engine.learn(Corpus.of(Word.of("almát")));
        engine.learn(LemmaMap.of(Map.of(Word.of("kalma"), Set.of(AffixType.of("/NOUN")))));
        AnnotationTokenizerResult annotationTokenizerResult = new AnnotationTokenizerResult("kalma/NOUN<CAS<ACC>>", "kalmi", "kalma", 1);
        annotationTokenizerResult.addAffixType(AffixType.of("/NOUN"));
        annotationTokenizerResult.addAffixType(AffixType.of("<CAS<ACC>>"));
        engine.learn(PreanalyzedTrainingItems.of(Set.of(
                new PreanalyzedTrainingItem(annotationTokenizerResult, FrequencyAwareWordPair.of("kalma", "kalmi"))
        )));
        this.reload();

        Word input = Word.of("kalma");
        Set<AffixType> affixTypes = Set.of(AffixType.of("<CAS<ACC>>"));
        InflectionInput inflectionInput = new InflectionInput(input, affixTypes);
        List<MorpherEngineResponse> responses = engine.inflect(inflectionInput);
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAggregatedWeight()).isOne();
        assertThat(responses.get(0).getOutput()).hasToString("kalmi");
    }

    @Test
    public void testInflectWithOrderedInputAndMinimumAggregatedWeightThreshold() throws IOException {
        MorpherEngine engine = createMorpherEngine(0.8);
        engine.learn(Corpus.of(Word.of("almát")));
        engine.learn(LemmaMap.of(Map.of(Word.of("kalma"), Set.of(AffixType.of("/NOUN")))));
        AnnotationTokenizerResult annotationTokenizerResult = new AnnotationTokenizerResult("kalma/NOUN<CAS<ACC>>", "kalmi", "kalma", 1);
        annotationTokenizerResult.addAffixType(AffixType.of("/NOUN"));
        annotationTokenizerResult.addAffixType(AffixType.of("<CAS<ACC>>"));
        engine.learn(PreanalyzedTrainingItems.of(Set.of(
                new PreanalyzedTrainingItem(annotationTokenizerResult, FrequencyAwareWordPair.of("kalma", "kalmi"))
        )));
        this.reload();

        Word input = Word.of("kalma");
        List<AffixType> affixTypes = List.of(AffixType.of("<CAS<ACC>>"));
        InflectionOrderedInput inflectionOrderedInput = new InflectionOrderedInput(input, affixTypes);
        List<MorpherEngineResponse> responses = engine.inflect(inflectionOrderedInput);
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAggregatedWeight()).isOne();
        assertThat(responses.get(0).getOutput()).hasToString("kalmi");
    }

    @Test
    public void testAnalyzeWithMinimumAggregatedWeightThreshold() throws IOException {
        MorpherEngine engine = createMorpherEngine(0.8);
        engine.learn(Corpus.of(Word.of("almát")));
        engine.learn(LemmaMap.of(Map.of(Word.of("kal"), Set.of(AffixType.of("/NOUN")))));
        AnnotationTokenizerResult annotationTokenizerResult = new AnnotationTokenizerResult("xkal/NOUN<CAS<ACC>>", "xkalmát", "xkal", 1);
        annotationTokenizerResult.addAffixType(AffixType.of("/NOUN"));
        annotationTokenizerResult.addAffixType(AffixType.of("<CAS<ACC>>"));
        engine.learn(PreanalyzedTrainingItems.of(Set.of(
                new PreanalyzedTrainingItem(annotationTokenizerResult, FrequencyAwareWordPair.of("xkal", "xkalmát"))
        )));
        this.reload();

        Word input = Word.of("kalmát");
        AnalysisInput analysisInput = AnalysisInput.of(input);
        List<MorpherEngineResponse> responses = engine.analyze(analysisInput);
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAggregatedWeight()).isEqualTo(0.875);
        assertThat(responses.get(0).getOutput()).hasToString("kal");
    }

    private void reload() throws IOException {
        this.engine.saveTo(this.temporaryFolder);
        this.engine.close();
        this.engine = this.createMorpherEngine();
        this.engine.loadFrom(this.temporaryFolder);
    }

    private MorpherEngine createMorpherEngine() {
        return createMorpherEngine(false, null);
    }

    private MorpherEngine createMorpherEngine(Double minimumAggregatedWeightThreshold) {
        return createMorpherEngine(false, minimumAggregatedWeightThreshold);
    }

    private MorpherEngine createMorpherEngine(boolean lazy) {
        return createMorpherEngine(lazy, null);
    }

    @SuppressWarnings("rawtypes")
    private MorpherEngine createMorpherEngine(boolean lazy, Double minimumAggregatedWeightThreshold) {
        Function<Class<?>, Stream<? extends ServiceLoader.Provider<?>>> serviceLoader = clazz -> {
            if (clazz.equals(IAbstractTransformationEngineFactory.class)) {
                ServiceLoader.Provider provider = mock(ServiceLoader.Provider.class);
                when(provider.type()).thenReturn(ASTRAAbstractTransformationEngineFactory.class);
                when(provider.get()).thenReturn(new ASTRAAbstractTransformationEngineFactory());
                return Stream.<ServiceLoader.Provider<?>>of(provider);
            }

            if (clazz.equals(ILanguageHandler.class)) {
                ServiceLoader.Provider provider = mock(ServiceLoader.Provider.class);
                when(provider.type()).thenReturn(HunmorphLanguageHandler.class);
                when(provider.get()).thenReturn(new HunmorphLanguageHandler());
                return Stream.<ServiceLoader.Provider<?>>of(provider);
            }

            return Stream.empty();
        };
        ServiceProvider serviceProvider = new ServiceProvider(serviceLoader);
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        IAbstractTransformationEngineFactory<?, ?> abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory(configuration);
        HunmorphLanguageHandler languageHandler = new HunmorphLanguageHandler();
        IProbabilityCalculator probabilityCalculator = new MultiplyProbabilityCalculator();
        ITransformationEngineHolderFactory transformationEngineHolderFactory = lazy ? new LazyTransformationEngineHolderFactory() : new EagerTransformationEngineHolderFactory();
        return new MorpherEngine(serviceProvider, transformationEngineHolderFactory, abstractTransformationEngineFactory, languageHandler, probabilityCalculator, minimumAggregatedWeightThreshold);
    }

    private void assertProbabilities(
            BiFunction<Word, List<AffixType>, List<MorpherEngineResponse>> morpherEngineResponsesFactory,
            Function<List<AffixType>, Integer> posIndexFactory,
            BiConsumer<List<ProbabilisticAffixType>, ProbabilisticAffixType> posAdder,
            AssertProbabilityModel... assertProbabilityModels) {
        for (AssertProbabilityModel assertProbabilityModel : assertProbabilityModels) {
            WordPair wordPair = assertProbabilityModel.getWordPair();
            Word inputWord = wordPair.getLeftWord();
            Word expectedWord = wordPair.getRightWord();
            List<ProbabilisticAffixType> expectedProbabilisticAffixTypes = assertProbabilityModel.getProbabilisticAffixTypes();
            List<AffixType> affixTypes = expectedProbabilisticAffixTypes
                    .stream()
                    .map(ProbabilisticAffixType::getAffixType)
                    .collect(toList());
            int posIndex = posIndexFactory.apply(affixTypes);
            affixTypes.remove(posIndex);
            List<MorpherEngineResponse> morpherEngineResponses = morpherEngineResponsesFactory.apply(inputWord, affixTypes);
            assertThat(morpherEngineResponses)
                    .withFailMessage("Empty response for %s - %s", inputWord, affixTypes)
                    .isNotEmpty();
            MorpherEngineResponse morpherEngineResponse = morpherEngineResponses
                    .stream()
                    .filter(response -> response.getOutput().equals(expectedWord) && response.getSteps().stream().map(ProbabilisticStep::getAffixType).collect(toList()).containsAll(affixTypes))
                    .findFirst()
                    .get();
            List<ProbabilisticAffixType> probabilisticAffixTypesFromSteps = morpherEngineResponse.getSteps()
                    .stream()
                    .map(probabilisticStep -> ProbabilisticAffixType.of(probabilisticStep.getAffixType(), probabilisticStep.getAffixTypeProbability()))
                    .collect(toList());
            List<ProbabilisticAffixType> resultingProbabilisticAffixTypes = new ArrayList<>(morpherEngineResponse.getSteps().size() + 1);
            resultingProbabilisticAffixTypes.addAll(probabilisticAffixTypesFromSteps);
            posAdder.accept(resultingProbabilisticAffixTypes, morpherEngineResponse.getPos());
            assertThat(resultingProbabilisticAffixTypes).isEqualTo(expectedProbabilisticAffixTypes);
            double resultingProbability = morpherEngineResponse.getAffixTypeChainProbability();
            assertThat(resultingProbability)
                    .withFailMessage("Probability for %s is 0", morpherEngineResponse)
                    .isPositive();
        }
    }

    private static class AssertProbabilityModel {

        private final WordPair wordPair;
        private final List<ProbabilisticAffixType> probabilisticAffixTypes;

        private AssertProbabilityModel(WordPair wordPair, ProbabilisticAffixType... probabilisticAffixTypes) {
            this.wordPair = wordPair;
            this.probabilisticAffixTypes = Arrays.asList(probabilisticAffixTypes);
        }

        private WordPair getWordPair() {
            return wordPair;
        }

        private List<ProbabilisticAffixType> getProbabilisticAffixTypes() {
            return probabilisticAffixTypes;
        }

    }

}
