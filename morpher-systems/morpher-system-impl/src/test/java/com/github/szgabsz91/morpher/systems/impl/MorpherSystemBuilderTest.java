package com.github.szgabsz91.morpher.systems.impl;

import com.github.szgabsz91.morpher.core.model.Corpus;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.core.services.ServiceProvider;
import com.github.szgabsz91.morpher.engines.api.IMorpherEngine;
import com.github.szgabsz91.morpher.engines.api.model.AnalysisInput;
import com.github.szgabsz91.morpher.engines.api.model.MorpherEngineResponse;
import com.github.szgabsz91.morpher.engines.impl.MorpherEngineBuilder;
import com.github.szgabsz91.morpher.engines.impl.impl.probability.MultiplyProbabilityCalculator;
import com.github.szgabsz91.morpher.engines.impl.transformationengineholderfactories.EagerTransformationEngineHolderFactory;
import com.github.szgabsz91.morpher.languagehandlers.api.ILanguageHandler;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.IHunmorphLanguageHandler;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.HunmorphLanguageHandler;
import com.github.szgabsz91.morpher.systems.api.IMorpherSystem;
import com.github.szgabsz91.morpher.systems.api.model.Language;
import com.github.szgabsz91.morpher.systems.api.model.LanguageAwareAnalysisInput;
import com.github.szgabsz91.morpher.transformationengines.api.factories.IAbstractTransformationEngineFactory;
import com.github.szgabsz91.morpher.transformationengines.astra.IASTRATransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.astra.config.ASTRATransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.astra.config.SearcherType;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.transformationengine.ASTRAAbstractTransformationEngineFactory;
import org.junit.jupiter.api.Test;

import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MorpherSystemBuilderTest {

    private static final double FITNESS_THRESHOLD = 0.4;
    private static final int MAXIMUM_NUMBER_OF_RESPONSES = 1;

    @Test
    public void testBuildWithoutLoading() {
        IMorpherSystem morpherSystem = new MorpherSystemBuilder()
                .withLanguage(Language.of("hu"), createMorpherEngine(WordPair.of("alma", "almát")))
                .build();

        // HU
        List<MorpherEngineResponse> huResponses = morpherSystem.analyze(new LanguageAwareAnalysisInput(Language.of("hu"), AnalysisInput.of(Word.of("almát")))).getMorpherEngineResponses();
        assertThat(huResponses).hasSize(1);
        MorpherEngineResponse huResponse = huResponses.get(0);
        assertThat(huResponse.getOutput()).hasToString("alma");
    }

    @Test
    public void testBuildWithLoading() {
        IMorpherSystem morpherSystem = new MorpherSystemBuilder()
                .withLanguage(Language.of("hu"), createMorpherEngine())
                .loadFrom(Paths.get("src/test/resources/simple-morpher-system.pb"))
                .build();

        // HU
        List<MorpherEngineResponse> huResponses = morpherSystem.analyze(new LanguageAwareAnalysisInput(Language.of("hu"), AnalysisInput.of(Word.of("almát")))).getMorpherEngineResponses();
        assertThat(huResponses).hasSize(1);
        MorpherEngineResponse huResponse = huResponses.get(0);
        assertThat(huResponse.getOutput()).hasToString("alma");
    }

    @Test
    public void testBuildWithLoadingAndNonExistentFile() {
        Path file = Paths.get("src/test/resources/non-existent.pb");
        MorpherSystemBuilder morpherSystemBuilder = new MorpherSystemBuilder()
                .loadFrom(file);
        IllegalStateException exception = assertThrows(IllegalStateException.class, morpherSystemBuilder::build);
        assertThat(exception).hasMessage("Cannot load state from " + file.toAbsolutePath());
        assertThat(exception).hasCauseExactlyInstanceOf(NoSuchFileException.class);
    }

    private IMorpherEngine<?> createMorpherEngine() {
        return createMorpherEngine(null);
    }

    private IMorpherEngine<?> createMorpherEngine(WordPair wordPair) {
        Function<Class<?>, Stream<? extends ServiceLoader.Provider<?>>> serviceLoader = clazz -> {
            if (clazz.equals(IAbstractTransformationEngineFactory.class)) {
                @SuppressWarnings("rawtypes")
                ServiceLoader.Provider provider = mock(ServiceLoader.Provider.class);
                when(provider.type()).thenReturn(ASTRAAbstractTransformationEngineFactory.class);
                when(provider.get()).thenReturn(new ASTRAAbstractTransformationEngineFactory());
                return Stream.<ServiceLoader.Provider<?>>of(provider);
            }

            if (clazz.equals(ILanguageHandler.class)) {
                @SuppressWarnings("rawtypes")
                ServiceLoader.Provider provider = mock(ServiceLoader.Provider.class);
                when(provider.type()).thenReturn(HunmorphLanguageHandler.class);
                when(provider.get()).thenReturn(new HunmorphLanguageHandler());
                return Stream.<ServiceLoader.Provider<?>>of(provider);
            }

            return Stream.empty();
        };
        final ServiceProvider serviceProvider = new ServiceProvider(serviceLoader);
        final ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .fitnessThreshold(FITNESS_THRESHOLD)
                .maximumNumberOfResponses(MAXIMUM_NUMBER_OF_RESPONSES)
                .build();
        IMorpherEngine<?> morpherEngine = new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .transformationEngineHolderFactory(new EagerTransformationEngineHolderFactory())
                .transformationEngineQualifier(IASTRATransformationEngine.QUALIFIER)
                .transformationEngineConfiguration(configuration)
                .languageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .probabilityCalculator(new MultiplyProbabilityCalculator())
                .build();
        if (wordPair != null) {
            morpherEngine.learn(Corpus.of(wordPair.getRightWord()));
        }
        return morpherEngine;
    }

}
