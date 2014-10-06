package com.github.szgabsz91.morpher.systems.impl.impl;

import com.github.szgabsz91.morpher.analyzeragents.api.IAnalyzerAgent;
import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.analyzeragents.api.model.AnnotationTokenizerResult;
import com.github.szgabsz91.morpher.analyzeragents.api.model.LemmaMap;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.IHunmorphAnalyzerAgent;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.impl.HunmorphAnalyzerAgent;
import com.github.szgabsz91.morpher.core.io.ICustomDeserializer;
import com.github.szgabsz91.morpher.core.model.Corpus;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.core.services.ServiceProvider;
import com.github.szgabsz91.morpher.engines.api.IMorpherEngine;
import com.github.szgabsz91.morpher.engines.api.model.InflectionInput;
import com.github.szgabsz91.morpher.engines.api.model.InflectionOrderedInput;
import com.github.szgabsz91.morpher.engines.api.model.LemmatizationInput;
import com.github.szgabsz91.morpher.engines.api.model.MorpherEngineResponse;
import com.github.szgabsz91.morpher.engines.api.model.PreanalyzedTrainingItem;
import com.github.szgabsz91.morpher.engines.api.model.PreanalyzedTrainingItems;
import com.github.szgabsz91.morpher.engines.hunmorph.HunmorphMorpherEngine;
import com.github.szgabsz91.morpher.engines.impl.MorpherEngineBuilder;
import com.github.szgabsz91.morpher.engines.impl.impl.probability.MultiplyProbabilityCalculator;
import com.github.szgabsz91.morpher.engines.impl.methodholderfactories.EagerMorpherMethodHolderFactory;
import com.github.szgabsz91.morpher.engines.impl.methodholderfactories.LazyMorpherMethodHolderFactory;
import com.github.szgabsz91.morpher.methods.api.factories.IAbstractMethodFactory;
import com.github.szgabsz91.morpher.methods.api.factories.IMethodConfiguration;
import com.github.szgabsz91.morpher.methods.astra.IASTRAMethod;
import com.github.szgabsz91.morpher.methods.astra.config.ASTRAMethodConfiguration;
import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import com.github.szgabsz91.morpher.methods.astra.impl.method.ASTRAAbstractMethodFactory;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.ASTRAMethodMessage;
import com.github.szgabsz91.morpher.systems.api.IMorpherSystem;
import com.github.szgabsz91.morpher.systems.api.model.Language;
import com.github.szgabsz91.morpher.systems.api.model.LanguageAwareCorpus;
import com.github.szgabsz91.morpher.systems.api.model.LanguageAwareInflectionInput;
import com.github.szgabsz91.morpher.systems.api.model.LanguageAwareInflectionOrderedInput;
import com.github.szgabsz91.morpher.systems.api.model.LanguageAwareLemmaMap;
import com.github.szgabsz91.morpher.systems.api.model.LanguageAwareLemmatizationInput;
import com.github.szgabsz91.morpher.systems.api.model.LanguageAwarePreanalyzedTrainingItems;
import com.github.szgabsz91.morpher.systems.api.model.MorpherSystemResponse;
import com.github.szgabsz91.morpher.systems.impl.MorpherSystemBuilder;
import com.github.szgabsz91.morpher.systems.impl.protocolbuffers.MorpherSystemMessage;
import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MorpherSystemTest {

    private static final double FITNESS_THRESHOLD = 0.4;
    private static final int MAXIMUM_NUMBER_OF_RESPONSES = 1;
    private static final Language LANGUAGE_HU = Language.of("hu");
    private static final Language LANGUAGE_EN = Language.of("en");

    private MorpherSystem morpherSystem;

    @BeforeEach
    public void setUp() {
        this.morpherSystem = (MorpherSystem) new MorpherSystemBuilder()
                .withLanguage(LANGUAGE_HU, createMorpherEngine(WordPair.of("alma", "almát")))
                .build();
    }

    @AfterEach
    public void tearDown() {
        this.morpherSystem.close();
    }

    @Test
    public void testGetMorpherEngineMap() {
        Map<Language, IMorpherEngine<?>> morpherEngineMap = this.morpherSystem.getMorpherEngineMap();
        assertThat(morpherEngineMap).hasSize(1);
        assertThat(morpherEngineMap).containsKey(LANGUAGE_HU);
    }

    @Test
    public void testLearnWithCorpusAndExistingLanguage() {
        // Learn
        Corpus corpus = Corpus.of(Word.of("asztalt"));
        LanguageAwareCorpus languageAwareCorpus = new LanguageAwareCorpus(LANGUAGE_HU, corpus);
        this.morpherSystem.learn(languageAwareCorpus);

        // Assert
        MorpherSystemResponse morpherSystemResponse = this.morpherSystem.lemmatize(new LanguageAwareLemmatizationInput(LANGUAGE_HU, LemmatizationInput.of(Word.of("asztalt"))));
        List<MorpherEngineResponse> morpherEngineResponses = morpherSystemResponse.getMorpherEngineResponses();
        assertThat(morpherEngineResponses).hasSize(1);
        MorpherEngineResponse morpherEngineResponse = morpherEngineResponses.get(0);
        assertThat(morpherEngineResponse.getOutput()).hasToString("asztal");
    }

    @Test
    public void testLearnWithCorpusAndNonExistentLanguage() {
        LanguageAwareCorpus languageAwareCorpus = new LanguageAwareCorpus(LANGUAGE_EN, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.morpherSystem.learn(languageAwareCorpus));
        assertThat(exception).hasMessage("Language en is not present in this system");
    }

    @Test
    public void testLearnWithPreanalyzedTrainingItemsAndExistingLanguage() {
        // Learn
        AnnotationTokenizerResult annotationTokenizerResult = new AnnotationTokenizerResult("expression", "asztalt", "asztal", 1);
        annotationTokenizerResult.getAffixTypes().addAll(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>")));
        FrequencyAwareWordPair wordPair = FrequencyAwareWordPair.of("asztal", "asztalt");
        PreanalyzedTrainingItems preanalyzedTrainingItems = PreanalyzedTrainingItems.of(Set.of(
                new PreanalyzedTrainingItem(annotationTokenizerResult, wordPair)
        ));
        LanguageAwarePreanalyzedTrainingItems languageAwarePreanalyzedTrainingItems = new LanguageAwarePreanalyzedTrainingItems(LANGUAGE_HU, preanalyzedTrainingItems);
        this.morpherSystem.learn(languageAwarePreanalyzedTrainingItems);

        // Assert
        MorpherSystemResponse morpherSystemResponse = this.morpherSystem.lemmatize(new LanguageAwareLemmatizationInput(LANGUAGE_HU, LemmatizationInput.of(Word.of("asztalt"))));
        List<MorpherEngineResponse> morpherEngineResponses = morpherSystemResponse.getMorpherEngineResponses();
        assertThat(morpherEngineResponses).hasSize(1);
        MorpherEngineResponse morpherEngineResponse = morpherEngineResponses.get(0);
        assertThat(morpherEngineResponse.getOutput()).hasToString("asztal");
    }

    @Test
    public void testLearnWithPreanalyzedTrainingItemsAndNonExistentLanguage() {
        LanguageAwarePreanalyzedTrainingItems languageAwarePreanalyzedTrainingItems = new LanguageAwarePreanalyzedTrainingItems(LANGUAGE_EN, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.morpherSystem.learn(languageAwarePreanalyzedTrainingItems));
        assertThat(exception).hasMessage("Language en is not present in this system");
    }

    @Test
    public void testLearnWithLemmaMapAndExistingLanguage() {
        // Learn
        LemmaMap lemmaMap = LemmaMap.of(Map.of(
                Word.of("balma"), Set.of(AffixType.of("/NOUN"))
        ));
        LanguageAwareLemmaMap languageAwareLemmaMap = new LanguageAwareLemmaMap(LANGUAGE_HU, lemmaMap);
        this.morpherSystem.learn(languageAwareLemmaMap);

        // Assert
        MorpherSystemResponse morpherSystemResponse = this.morpherSystem.lemmatize(new LanguageAwareLemmatizationInput(LANGUAGE_HU, LemmatizationInput.of(Word.of("balmát"))));
        List<MorpherEngineResponse> morpherEngineResponses = morpherSystemResponse.getMorpherEngineResponses();
        assertThat(morpherEngineResponses).hasSize(1);
        MorpherEngineResponse morpherEngineResponse = morpherEngineResponses.get(0);
        assertThat(morpherEngineResponse.getOutput()).hasToString("balma");
    }

    @Test
    public void testLearnWithLemmaMapAndNonExistentLanguage() {
        LanguageAwareLemmaMap languageAwareLemmaMap = new LanguageAwareLemmaMap(LANGUAGE_EN, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.morpherSystem.learn(languageAwareLemmaMap));
        assertThat(exception).hasMessage("Language en is not present in this system");
    }

    @Test
    public void testInflectWithUnorderedInputAndExistingLanguage() {
        InflectionInput inflectionInput = new InflectionInput(Word.of("alma"), Set.of(AffixType.of("<CAS<ACC>>")));
        LanguageAwareInflectionInput languageAwareInflectionInput = new LanguageAwareInflectionInput(LANGUAGE_HU, inflectionInput);
        MorpherSystemResponse morpherSystemResponse = this.morpherSystem.inflect(languageAwareInflectionInput);
        Language language = morpherSystemResponse.getLanguage();
        assertThat(language).hasToString("hu");
        List<MorpherEngineResponse> morpherEngineResponses = morpherSystemResponse.getMorpherEngineResponses();
        assertThat(morpherEngineResponses).hasSize(1);
        MorpherEngineResponse morpherEngineResponse = morpherEngineResponses.get(0);
        assertThat(morpherEngineResponse.getOutput()).hasToString("almát");
    }

    @Test
    public void testInflectWithUnorderedInputAndNonExistentLanguage() {
        LanguageAwareInflectionInput languageAwareInflectionInput = new LanguageAwareInflectionInput(LANGUAGE_EN, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.morpherSystem.inflect(languageAwareInflectionInput));
        assertThat(exception).hasMessage("Language en is not present in this system");
    }

    @Test
    public void testInflectWithOrderedInputAndExistingLanguage() {
        InflectionOrderedInput inflectionOrderedInput = new InflectionOrderedInput(Word.of("alma"), List.of(AffixType.of("<CAS<ACC>>")));
        LanguageAwareInflectionOrderedInput languageAwareInflectionOrderedInput = new LanguageAwareInflectionOrderedInput(LANGUAGE_HU, inflectionOrderedInput);
        MorpherSystemResponse morpherSystemResponse = this.morpherSystem.inflect(languageAwareInflectionOrderedInput);
        Language language = morpherSystemResponse.getLanguage();
        assertThat(language).hasToString("hu");
        List<MorpherEngineResponse> morpherEngineResponses = morpherSystemResponse.getMorpherEngineResponses();
        assertThat(morpherEngineResponses).hasSize(1);
        MorpherEngineResponse morpherEngineResponse = morpherEngineResponses.get(0);
        assertThat(morpherEngineResponse.getOutput()).hasToString("almát");
    }

    @Test
    public void testInflectWithOrderedInputAndNonExistentLanguage() {
        LanguageAwareInflectionOrderedInput languageAwareInflectionOrderedInput = new LanguageAwareInflectionOrderedInput(LANGUAGE_EN, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.morpherSystem.inflect(languageAwareInflectionOrderedInput));
        assertThat(exception).hasMessage("Language en is not present in this system");
    }

    @Test
    public void testLemmatizeWithExistingLanguage() {
        LemmatizationInput lemmatizationInput = LemmatizationInput.of(Word.of("almát"));
        LanguageAwareLemmatizationInput languageAwareLemmatizationInput = new LanguageAwareLemmatizationInput(LANGUAGE_HU, lemmatizationInput);
        MorpherSystemResponse morpherSystemResponse = this.morpherSystem.lemmatize(languageAwareLemmatizationInput);
        Language language = morpherSystemResponse.getLanguage();
        assertThat(language).hasToString("hu");
        List<MorpherEngineResponse> morpherEngineResponses = morpherSystemResponse.getMorpherEngineResponses();
        assertThat(morpherEngineResponses).hasSize(1);
        MorpherEngineResponse morpherEngineResponse = morpherEngineResponses.get(0);
        assertThat(morpherEngineResponse.getOutput()).hasToString("alma");
    }

    @Test
    public void testLemmatizeWithNonExistentLanguage() {
        LanguageAwareLemmatizationInput languageAwareLemmatizationInput = new LanguageAwareLemmatizationInput(LANGUAGE_EN, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.morpherSystem.lemmatize(languageAwareLemmatizationInput));
        assertThat(exception).hasMessage("Language en is not present in this system");
    }

    @Test
    public void testGetSupportedLanguages() {
        Set<Language> supportedLanguages = this.morpherSystem.getSupportedLanguages();
        assertThat(supportedLanguages).containsExactlyInAnyOrder(LANGUAGE_HU);
    }

    @Test
    public void testGetSupportedAffixTypesWithExistingLanguage() {
        Set<AffixType> affixTypes = this.morpherSystem.getSupportedAffixTypes(LANGUAGE_HU);
        assertThat(affixTypes).hasSize(339);
    }

    @Test
    public void testGetSupportedAffixTypesWithNonExistentLanguage() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> this.morpherSystem.getSupportedAffixTypes(LANGUAGE_EN));
        assertThat(exception).hasMessage("Language en is not present in this system");
    }

    @Test
    public void testSaveToAndLoadFromAndIsDirty() throws IOException {
        Path tempFile = Files.createTempFile("morpher", "system");

        try {
            // Assert dirty system
            assertThat(this.morpherSystem.isDirty()).isTrue();

            // Save
            this.morpherSystem.saveTo(tempFile);
            assertThat(this.morpherSystem.isDirty()).isFalse();

            // Load
            IMorpherSystem result = new MorpherSystemBuilder()
                    .withLanguage(LANGUAGE_HU, createMorpherEngine(WordPair.of("asztal", "asztalt")))
                    .build();
            result.loadFrom(tempFile);
            assertThat(result.isDirty()).isFalse();
        }
        finally {
            Files.delete(tempFile);
        }
    }

    @Test
    public void testLoadFromWithInvalidMessage() throws IOException {
        Path tempFile = Files.createTempFile("morpher", "system");

        try {
            MorpherSystemMessage morpherSystemMessage = MorpherSystemMessage.newBuilder()
                    .putEngineMap("hu", Any.pack(ASTRAMethodMessage.newBuilder().build()))
                    .build();
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(Files.newOutputStream(tempFile))) {
                morpherSystemMessage.writeTo(gzipOutputStream);
            }

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.morpherSystem.loadFrom(tempFile));
            assertThat(exception).hasMessage("Cannot load Morpher Engine for language hu from " + morpherSystemMessage.getEngineMapMap().get("hu"));
            assertThat(exception).hasCauseExactlyInstanceOf(InvalidProtocolBufferException.class);
        }
        finally {
            Files.delete(tempFile);
        }
    }

    @Test
    public void testSerializeDeserialize() throws IOException {
        Path file = Files.createTempFile("morpher", "system");

        try {
            Language language = Language.of("hu");
            IMorpherSystem morpherSystem = new MorpherSystemBuilder()
                    .withLanguage(language, createMorpherEngine(WordPair.of("alma", "almát"), true))
                    .build();
            boolean serializationResult = morpherSystem.serialize(file);
            assertThat(serializationResult).isTrue();

            IMorpherSystem resultingMorpherSystem = new MorpherSystemBuilder()
                    .withLanguage(language, createMorpherEngine(WordPair.of("toll", "tollat"), true))
                    .build();
            boolean deserializationResult = resultingMorpherSystem.deserialize(file);
            assertThat(deserializationResult).isTrue();

            MorpherSystemResponse response = resultingMorpherSystem.lemmatize(new LanguageAwareLemmatizationInput(language, LemmatizationInput.of(Word.of("almát"))));
            List<MorpherEngineResponse> morpherEngineResponses = response.getMorpherEngineResponses();
            assertThat(morpherEngineResponses).hasSize(1);
            MorpherEngineResponse morpherEngineResponse = morpherEngineResponses.get(0);
            assertThat(morpherEngineResponse.getOutput()).hasToString("alma");
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testSerializeWithNonCustomSerializableEngine() throws IOException {
        Path file = Files.createTempFile("morpher", "system");

        try {
            Language language = Language.of("hu");
            IMorpherSystem morpherSystem = new MorpherSystemBuilder()
                    .withLanguage(language, createHunmorphEngine())
                    .build();
            boolean result = morpherSystem.serialize(file);

            assertThat(result).isFalse();
            assertThat(file).exists();
            assertThat(Files.readAllLines(file)).isEmpty();
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testDeserializeWithNonCustomDeserializableEngine() throws IOException {
        Path file = Files.createTempFile("morpher", "system");

        try {
            Language language = Language.of("hu");
            IMorpherSystem morpherSystem = new MorpherSystemBuilder()
                    .withLanguage(language, createMorpherEngine(WordPair.of("alma", "almát"), true))
                    .build();
            morpherSystem.serialize(file);

            IMorpherSystem resultingMorpherSystem = new MorpherSystemBuilder()
                    .withLanguage(language, createHunmorphEngine())
                    .build();
            boolean result = resultingMorpherSystem.deserialize(file);
            assertThat(result).isFalse();
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testDeserializeWithNonLazyEngine() throws IOException {
        Path file = Files.createTempFile("morpher", "system");

        try {
            Language language = Language.of("hu");
            IMorpherSystem morpherSystem = new MorpherSystemBuilder()
                    .withLanguage(language, createMorpherEngine(WordPair.of("alma", "almát"), true))
                    .build();
            morpherSystem.serialize(file);

            IMorpherSystem resultingMorpherSystem = new MorpherSystemBuilder()
                    .withLanguage(language, createMorpherEngine(WordPair.of("toll", "tollat")))
                    .build();
            boolean result = resultingMorpherSystem.deserialize(file);
            assertThat(result).isFalse();

            MorpherSystemResponse response = resultingMorpherSystem.lemmatize(new LanguageAwareLemmatizationInput(language, LemmatizationInput.of(Word.of("tollat"))));
            List<MorpherEngineResponse> morpherEngineResponses = response.getMorpherEngineResponses();
            assertThat(morpherEngineResponses).hasSize(1);
            MorpherEngineResponse morpherEngineResponse = morpherEngineResponses.get(0);
            assertThat(morpherEngineResponse.getOutput()).hasToString("toll");
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testDeserializeWithLazyNonCustomDeserializableEngine() throws IOException {
        Path file = Files.createTempFile("morpher", "system");

        try {
            Language language = Language.of("hu");
            IMorpherSystem morpherSystem = new MorpherSystemBuilder()
                    .withLanguage(language, createMorpherEngine(WordPair.of("alma", "almát"), true))
                    .build();
            morpherSystem.serialize(file);

            @SuppressWarnings("unchecked")
            IMorpherEngine<GeneratedMessageV3> mockEngine = mock(IMorpherEngine.class);
            when(mockEngine.isEager()).thenReturn(false);
            IMorpherSystem resultingMorpherSystem = new MorpherSystemBuilder()
                    .withLanguage(language, mockEngine)
                    .build();
            boolean result = resultingMorpherSystem.deserialize(file);
            assertThat(result).isFalse();
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testDeserializeWithLazyCustomDeserializableEngineAndFalseResult() throws IOException {
        Path file = Files.createTempFile("morpher", "system");

        try {
            Language language = Language.of("hu");
            IMorpherSystem morpherSystem = new MorpherSystemBuilder()
                    .withLanguage(language, createMorpherEngine(WordPair.of("alma", "almát"), true))
                    .build();
            morpherSystem.serialize(file);

            @SuppressWarnings("unchecked")
            IMorpherEngine<GeneratedMessageV3> mockEngine = new CustomEngine();
            IMorpherSystem resultingMorpherSystem = new MorpherSystemBuilder()
                    .withLanguage(language, mockEngine)
                    .build();
            boolean result = resultingMorpherSystem.deserialize(file);
            assertThat(result).isFalse();
        }
        finally {
            Files.delete(file);
        }
    }

    private IMorpherEngine<?> createHunmorphEngine() {
        return new HunmorphMorpherEngine();
    }

    private IMorpherEngine<?> createMorpherEngine(WordPair wordPair) {
        return createMorpherEngine(wordPair, false);
    }

    private IMorpherEngine<?> createMorpherEngine(WordPair wordPair, boolean lazy) {
        Function<Class<?>, Stream<? extends ServiceLoader.Provider<?>>> serviceLoader = clazz -> {
            if (clazz.equals(IAbstractMethodFactory.class)) {
                ServiceLoader.Provider provider = mock(ServiceLoader.Provider.class);
                when(provider.type()).thenReturn(ASTRAAbstractMethodFactory.class);
                when(provider.get()).thenReturn(new ASTRAAbstractMethodFactory());
                return Stream.<ServiceLoader.Provider<?>>of(provider);
            }

            if (clazz.equals(IAnalyzerAgent.class)) {
                ServiceLoader.Provider provider = mock(ServiceLoader.Provider.class);
                when(provider.type()).thenReturn(HunmorphAnalyzerAgent.class);
                when(provider.get()).thenReturn(new HunmorphAnalyzerAgent());
                return Stream.<ServiceLoader.Provider<?>>of(provider);
            }

            return Stream.empty();
        };
        final ServiceProvider serviceProvider = new ServiceProvider(serviceLoader);
        final ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .fitnessThreshold(FITNESS_THRESHOLD)
                .maximumNumberOfResponses(MAXIMUM_NUMBER_OF_RESPONSES)
                .build();
        MorpherEngineBuilder<IMethodConfiguration> morpherEngineBuilder = new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .methodQualifier(IASTRAMethod.QUALIFIER)
                .methodConfiguration(configuration)
                .analyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER)
                .probabilityCalculator(new MultiplyProbabilityCalculator());
        if (lazy) {
            morpherEngineBuilder = morpherEngineBuilder.methodHolderFactory(new LazyMorpherMethodHolderFactory());
        }
        else {
            morpherEngineBuilder = morpherEngineBuilder.methodHolderFactory(new EagerMorpherMethodHolderFactory());
        }
        IMorpherEngine<?> morpherEngine = morpherEngineBuilder.build();
        if (wordPair != null) {
            morpherEngine.learn(Corpus.of(wordPair.getRightWord()));
        }
        return morpherEngine;
    }

    private static class CustomEngine implements IMorpherEngine<GeneratedMessageV3>, ICustomDeserializer {

        @Override
        public void close() {

        }

        @Override
        public boolean isEager() {
            return false;
        }

        @Override
        public boolean isLazy() {
            return false;
        }

        @Override
        public void learn(Corpus corpus) {

        }

        @Override
        public void learn(PreanalyzedTrainingItems preanalyzedTrainingItems) {

        }

        @Override
        public void learn(LemmaMap lemmaMap) {

        }

        @Override
        public List<MorpherEngineResponse> inflect(InflectionInput inflectionInput) {
            return null;
        }

        @Override
        public List<MorpherEngineResponse> inflect(InflectionOrderedInput inflectionOrderedInput) {
            return null;
        }

        @Override
        public List<MorpherEngineResponse> lemmatize(LemmatizationInput lemmatizationInput) {
            return null;
        }

        @Override
        public Set<AffixType> getSupportedAffixTypes() {
            return null;
        }

        @Override
        public boolean isDirty() {
            return false;
        }

        @Override
        public void clean() {

        }

        @Override
        public GeneratedMessageV3 toMessage() {
            return null;
        }

        @Override
        public void fromMessage(GeneratedMessageV3 generatedMessageV3) {

        }

        @Override
        public void fromMessage(Any message) throws InvalidProtocolBufferException {

        }

        @Override
        public void saveTo(Path file) throws IOException {

        }

        @Override
        public void loadFrom(Path file) throws IOException {

        }

        @Override
        public boolean deserialize(Path file) {
            return false;
        }

    }

}
