package com.github.szgabsz91.morpher.engines.impl;

import com.github.szgabsz91.morpher.analyzeragents.api.IAnalyzerAgent;
import com.github.szgabsz91.morpher.analyzeragents.api.model.ProbabilisticAffixType;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.IHunmorphAnalyzerAgent;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.impl.HunmorphAnalyzerAgent;
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
import com.github.szgabsz91.morpher.engines.impl.methodholderfactories.EagerMorpherMethodHolderFactory;
import com.github.szgabsz91.morpher.methods.api.factories.IAbstractMethodFactory;
import com.github.szgabsz91.morpher.methods.api.factories.IMethodConfiguration;
import com.github.szgabsz91.morpher.methods.astra.IASTRAMethod;
import com.github.szgabsz91.morpher.methods.astra.config.ASTRAMethodConfiguration;
import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import com.github.szgabsz91.morpher.methods.astra.impl.method.ASTRAAbstractMethodFactory;
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
        this.serviceProvider = new ServiceProvider(serviceLoader);
    }

    @Test
    public void testBuildWithoutLoading() {
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        MorpherEngineBuilder<IMethodConfiguration> morpherEngineBuilder = new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .methodHolderFactory(new EagerMorpherMethodHolderFactory())
                .methodQualifier(IASTRAMethod.QUALIFIER)
                .methodConfiguration(configuration)
                .analyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER)
                .probabilityCalculator(new MultiplyProbabilityCalculator());

        try (IMorpherEngine<?> morpherEngine = morpherEngineBuilder.build()) {
            assertThat(morpherEngine).isNotNull();
        }
    }

    @Test
    public void testBuildWithLoading() {
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        MorpherEngineBuilder<ASTRAMethodConfiguration> morpherEngineBuilder = new MorpherEngineBuilder<ASTRAMethodConfiguration>()
                .serviceProvider(serviceProvider)
                .methodHolderFactory(new EagerMorpherMethodHolderFactory())
                .methodQualifier(IASTRAMethod.QUALIFIER)
                .methodConfiguration(configuration)
                .analyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER)
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
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        MorpherEngineBuilder<IMethodConfiguration> morpherEngineBuilder = new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .methodHolderFactory(new EagerMorpherMethodHolderFactory())
                .methodQualifier(IASTRAMethod.QUALIFIER)
                .methodConfiguration(configuration)
                .analyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER)
                .probabilityCalculator(new MultiplyProbabilityCalculator())
                .loadFrom(file);
        IllegalStateException exception = assertThrows(IllegalStateException.class, morpherEngineBuilder::build);
        assertThat(exception).hasMessage("Cannot load state from " + file.toAbsolutePath());
    }

    @Test
    public void testBuildWithNullConfiguration() {
        new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .methodHolderFactory(new EagerMorpherMethodHolderFactory())
                .methodQualifier(IASTRAMethod.QUALIFIER)
                .analyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER)
                .probabilityCalculator(new MultiplyProbabilityCalculator())
                .build();
    }

    @Test
    public void testBuildWithNullMethodHolderFactory() {
        MorpherEngineBuilder<IMethodConfiguration> builder = new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .methodQualifier(IASTRAMethod.QUALIFIER)
                .analyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER);
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception).hasMessage("The method holder factory must not be null");
    }

    @Test
    public void testBuildWithNullProbabilityCalculator() {
        MorpherEngineBuilder<IMethodConfiguration> builder = new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .methodHolderFactory(new EagerMorpherMethodHolderFactory())
                .methodQualifier(IASTRAMethod.QUALIFIER)
                .analyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER);
        IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);
        assertThat(exception).hasMessage("The probability calculator must not be null");
    }

    @Test
    public void testBuildWithMinimumAggregatedWeightThreshold() {
        double minimumAggregatedWeightThreshold = 2.0;

        MorpherEngineBuilder<IMethodConfiguration> builder = new MorpherEngineBuilder<>()
                .serviceProvider(serviceProvider)
                .methodHolderFactory(new EagerMorpherMethodHolderFactory())
                .probabilityCalculator(new MultiplyProbabilityCalculator())
                .minimumAggregatedWeightThreshold(minimumAggregatedWeightThreshold);

        try (IMorpherEngine<?> engine = builder.build()) {
            assertThat(((MorpherEngine) engine).getMinimumAggregatedWeightThreshold()).isEqualTo(minimumAggregatedWeightThreshold);
        }
    }

}
