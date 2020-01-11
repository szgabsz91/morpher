package com.github.szgabsz91.morpher.engines.impl;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.services.ServiceProvider;
import com.github.szgabsz91.morpher.engines.api.IMorpherEngine;
import com.github.szgabsz91.morpher.engines.api.model.InflectionInput;
import com.github.szgabsz91.morpher.engines.api.model.Mode;
import com.github.szgabsz91.morpher.engines.api.model.MorpherEngineResponse;
import com.github.szgabsz91.morpher.engines.api.model.ProbabilisticStep;
import com.github.szgabsz91.morpher.engines.impl.impl.MorpherEngine;
import com.github.szgabsz91.morpher.engines.impl.impl.probability.MultiplyProbabilityCalculator;
import com.github.szgabsz91.morpher.engines.impl.transformationengineholderfactories.EagerTransformationEngineHolderFactory;
import com.github.szgabsz91.morpher.languagehandlers.api.ILanguageHandler;
import com.github.szgabsz91.morpher.languagehandlers.api.model.ProbabilisticAffixType;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.IHunmorphLanguageHandler;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.HunmorphLanguageHandler;
import com.github.szgabsz91.morpher.transformationengines.api.factories.IAbstractTransformationEngineFactory;
import com.github.szgabsz91.morpher.transformationengines.api.factories.ITransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.astra.IASTRATransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.astra.config.ASTRATransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.astra.config.SearcherType;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.transformationengine.ASTRAAbstractTransformationEngineFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MorpherEngineBuilderTest {

    private ServiceProvider serviceProvider;

    @BeforeEach
    @SuppressWarnings("rawtypes")
    public void setUp() {
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
        this.serviceProvider = new ServiceProvider(serviceLoader);
    }

    @Test
    public void testBuildWithoutLoading() {
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        MorpherEngineBuilder<ITransformationEngineConfiguration> morpherEngineBuilder = new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .transformationEngineHolderFactory(new EagerTransformationEngineHolderFactory())
                .transformationEngineQualifier(IASTRATransformationEngine.QUALIFIER)
                .transformationEngineConfiguration(configuration)
                .languageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .probabilityCalculator(new MultiplyProbabilityCalculator());

        try (IMorpherEngine<?> morpherEngine = morpherEngineBuilder.build()) {
            assertThat(morpherEngine).isNotNull();
        }
    }

    @Test
    public void testBuildWithLoading() {
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        MorpherEngineBuilder<ASTRATransformationEngineConfiguration> morpherEngineBuilder = new MorpherEngineBuilder<ASTRATransformationEngineConfiguration>()
                .serviceProvider(serviceProvider)
                .transformationEngineHolderFactory(new EagerTransformationEngineHolderFactory())
                .transformationEngineQualifier(IASTRATransformationEngine.QUALIFIER)
                .transformationEngineConfiguration(configuration)
                .languageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .probabilityCalculator(new MultiplyProbabilityCalculator())
                .loadFrom(Paths.get("src/test/resources/simple-morpher-engine.pb"));

        try (IMorpherEngine<?> morpherEngine = morpherEngineBuilder.build()) {
            List<MorpherEngineResponse> responses = morpherEngine.inflect(new InflectionInput(Word.of("alma"), Set.of(AffixType.of("<CAS<ACC>>"))));
            assertThat(responses).hasSize(1);
            MorpherEngineResponse response = responses.get(0);
            assertThat(response.getMode()).isEqualTo(Mode.INFLECTION);
            assertThat(response.getInput()).isEqualTo(Word.of("alma"));
            assertThat(response.getOutput()).isEqualTo(Word.of("almát"));
            assertThat(response.getPos()).isEqualTo(ProbabilisticAffixType.of(AffixType.of("/NOUN"), 1.0));
            assertThat(response.getAffixTypeChainProbability()).isEqualTo(0.5);
            assertThat(response.getAggregatedWeight()).isOne();
            assertThat(response.getSteps()).containsExactly(
                    new ProbabilisticStep(Word.of("alma"), Word.of("almát"), AffixType.of("<CAS<ACC>>"), 0.5, 1.0, 0.5)
            );
        }
    }

    @Test
    public void testBuildWithLoadingAndNonExistentFolder() {
        Path file = Paths.get("src/test/resources/complex-morpher-engine");
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        MorpherEngineBuilder<ITransformationEngineConfiguration> morpherEngineBuilder = new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .transformationEngineHolderFactory(new EagerTransformationEngineHolderFactory())
                .transformationEngineQualifier(IASTRATransformationEngine.QUALIFIER)
                .transformationEngineConfiguration(configuration)
                .languageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .probabilityCalculator(new MultiplyProbabilityCalculator())
                .loadFrom(file);
        IllegalStateException exception = assertThrows(IllegalStateException.class, morpherEngineBuilder::build);
        assertThat(exception).hasMessage("Cannot load state from " + file.toAbsolutePath());
    }

    @Test
    public void testBuildWithNullConfiguration() {
        new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .transformationEngineHolderFactory(new EagerTransformationEngineHolderFactory())
                .transformationEngineQualifier(IASTRATransformationEngine.QUALIFIER)
                .languageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .probabilityCalculator(new MultiplyProbabilityCalculator())
                .build();
    }

    @Test
    public void testBuildWithNullTransformationEngineHolderFactory() {
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        MorpherEngineBuilder<ITransformationEngineConfiguration> morpherEngineBuilder = new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .transformationEngineHolderFactory(null)
                .transformationEngineQualifier(IASTRATransformationEngine.QUALIFIER)
                .transformationEngineConfiguration(configuration)
                .languageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .probabilityCalculator(new MultiplyProbabilityCalculator());

        try (IMorpherEngine<?> morpherEngine = morpherEngineBuilder.build()) {
            assertThat(morpherEngine).isNotNull();
        }
    }

    @Test
    public void testBuildWithNullProbabilityCalculator() {
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        MorpherEngineBuilder<ITransformationEngineConfiguration> morpherEngineBuilder = new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .transformationEngineHolderFactory(new EagerTransformationEngineHolderFactory())
                .transformationEngineQualifier(IASTRATransformationEngine.QUALIFIER)
                .transformationEngineConfiguration(configuration)
                .languageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .probabilityCalculator(null);

        try (IMorpherEngine<?> morpherEngine = morpherEngineBuilder.build()) {
            assertThat(morpherEngine).isNotNull();
        }
    }

    @Test
    public void testBuildWithMinimumAggregatedWeightThreshold() {
        double minimumAggregatedWeightThreshold = 2.0;

        MorpherEngineBuilder<ITransformationEngineConfiguration> builder = new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .transformationEngineHolderFactory(new EagerTransformationEngineHolderFactory())
                .probabilityCalculator(new MultiplyProbabilityCalculator())
                .minimumAggregatedWeightThreshold(minimumAggregatedWeightThreshold);

        try (IMorpherEngine<?> engine = builder.build()) {
            assertThat(((MorpherEngine) engine).getMinimumAggregatedWeightThreshold()).isEqualTo(minimumAggregatedWeightThreshold);
        }
    }

}
