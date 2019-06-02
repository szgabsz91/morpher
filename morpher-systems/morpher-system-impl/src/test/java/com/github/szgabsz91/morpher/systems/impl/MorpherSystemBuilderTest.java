package com.github.szgabsz91.morpher.systems.impl;

import com.github.szgabsz91.morpher.analyzeragents.api.IAnalyzerAgent;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.IHunmorphAnalyzerAgent;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.impl.HunmorphAnalyzerAgent;
import com.github.szgabsz91.morpher.core.model.Corpus;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.core.services.ServiceProvider;
import com.github.szgabsz91.morpher.engines.api.IMorpherEngine;
import com.github.szgabsz91.morpher.engines.api.model.LemmatizationInput;
import com.github.szgabsz91.morpher.engines.api.model.MorpherEngineResponse;
import com.github.szgabsz91.morpher.engines.impl.MorpherEngineBuilder;
import com.github.szgabsz91.morpher.engines.impl.impl.probability.MultiplyProbabilityCalculator;
import com.github.szgabsz91.morpher.engines.impl.methodholderfactories.EagerMorpherMethodHolderFactory;
import com.github.szgabsz91.morpher.methods.api.factories.IAbstractMethodFactory;
import com.github.szgabsz91.morpher.methods.astra.IASTRAMethod;
import com.github.szgabsz91.morpher.methods.astra.config.ASTRAMethodConfiguration;
import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import com.github.szgabsz91.morpher.methods.astra.impl.method.ASTRAAbstractMethodFactory;
import com.github.szgabsz91.morpher.systems.api.IMorpherSystem;
import com.github.szgabsz91.morpher.systems.api.model.Language;
import com.github.szgabsz91.morpher.systems.api.model.LanguageAwareLemmatizationInput;
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
        List<MorpherEngineResponse> huResponses = morpherSystem.lemmatize(new LanguageAwareLemmatizationInput(Language.of("hu"), LemmatizationInput.of(Word.of("almát")))).getMorpherEngineResponses();
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
        List<MorpherEngineResponse> huResponses = morpherSystem.lemmatize(new LanguageAwareLemmatizationInput(Language.of("hu"), LemmatizationInput.of(Word.of("almát")))).getMorpherEngineResponses();
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
        IMorpherEngine<?> morpherEngine = new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .methodHolderFactory(new EagerMorpherMethodHolderFactory())
                .methodQualifier(IASTRAMethod.QUALIFIER)
                .methodConfiguration(configuration)
                .analyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER)
                .probabilityCalculator(new MultiplyProbabilityCalculator())
                .build();
        if (wordPair != null) {
            morpherEngine.learn(Corpus.of(wordPair.getRightWord()));
        }
        return morpherEngine;
    }

}
