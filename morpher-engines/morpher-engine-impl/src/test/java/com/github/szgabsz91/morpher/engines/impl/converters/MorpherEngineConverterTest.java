package com.github.szgabsz91.morpher.engines.impl.converters;

import com.github.szgabsz91.morpher.analyzeragents.api.IAnalyzerAgent;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.IHunmorphAnalyzerAgent;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.impl.HunmorphAnalyzerAgent;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.impl.markov.FullMarkovModel;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.protocolbuffers.HunmorphAnalyzerAgentMessage;
import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.Corpus;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.services.ServiceProvider;
import com.github.szgabsz91.morpher.engines.api.model.LemmatizationInput;
import com.github.szgabsz91.morpher.engines.impl.impl.MorpherEngine;
import com.github.szgabsz91.morpher.engines.impl.impl.probability.IProbabilityCalculator;
import com.github.szgabsz91.morpher.engines.impl.impl.probability.MinMaxProbabilityCalclator;
import com.github.szgabsz91.morpher.engines.impl.impl.probability.MultiplyProbabilityCalculator;
import com.github.szgabsz91.morpher.engines.impl.methodholderfactories.EagerMorpherMethodHolderFactory;
import com.github.szgabsz91.morpher.engines.impl.methodholderfactories.IMorpherMethodHolderFactory;
import com.github.szgabsz91.morpher.engines.impl.protocolbuffers.MorpherEngineMessage;
import com.github.szgabsz91.morpher.engines.impl.protocolbuffers.ProbabilityCalculatorTypeMessage;
import com.github.szgabsz91.morpher.methods.api.factories.IAbstractMethodFactory;
import com.github.szgabsz91.morpher.methods.astra.IASTRAMethod;
import com.github.szgabsz91.morpher.methods.astra.config.ASTRAMethodConfiguration;
import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import com.github.szgabsz91.morpher.methods.astra.impl.method.ASTRAAbstractMethodFactory;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.ASTRAMethodConfigurationMessage;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.SearcherTypeMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MorpherEngineConverterTest {

    private ServiceProvider serviceProvider;
    private MorpherEngineConverter converter;

    @BeforeEach
    public void setUp() {
        Function<Class<?>, Stream<? extends ServiceLoader.Provider<?>>> serviceLoader = clazz -> {
            if (clazz.equals(IMorpherMethodHolderFactory.class)) {
                ServiceLoader.Provider provider = mock(ServiceLoader.Provider.class);
                when(provider.type()).thenReturn(EagerMorpherMethodHolderFactory.class);
                when(provider.get()).thenReturn(new EagerMorpherMethodHolderFactory());
                return Stream.<ServiceLoader.Provider<?>>of(provider);
            }

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
        this.converter = new MorpherEngineConverter(serviceProvider);
    }

    @Test
    public void testConvertAndCovertBackAndParse() throws IOException {
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        ASTRAAbstractMethodFactory abstractMethodFactory = new ASTRAAbstractMethodFactory(configuration);
        IAnalyzerAgent<HunmorphAnalyzerAgentMessage> analyzerAgent = new HunmorphAnalyzerAgent();
        IProbabilityCalculator probabilityCalculator = new MultiplyProbabilityCalculator();
        MorpherEngine engine = new MorpherEngine(this.serviceProvider, new EagerMorpherMethodHolderFactory(), abstractMethodFactory, analyzerAgent, probabilityCalculator, null);

        Word word = Word.of("almák");
        engine.learn(Corpus.of(word));
        engine.lemmatize(LemmatizationInput.of(word));

        MorpherEngineMessage message = this.converter.convert(engine);
        assertThat(message.getAbstractMethodFactoryQualifier()).isEqualTo(IASTRAMethod.QUALIFIER);
        assertThat(message.getAnalyzerAgentQualifier()).isEqualTo(IHunmorphAnalyzerAgent.QUALIFIER);
        assertThat(message.getProbabilityCalculatorType()).isEqualTo(ProbabilityCalculatorTypeMessage.MULTIPLY);

        MorpherEngine result = this.converter.convertBack(message);
        assertThat(result.getAbstractMethodFactory()).isInstanceOf(ASTRAAbstractMethodFactory.class);
        assertThat(result.getAnalyzerAgent()).isInstanceOf(HunmorphAnalyzerAgent.class);
        assertThat(result.getProbabilityCalculator()).isInstanceOf(MultiplyProbabilityCalculator.class);

        Path file = Files.createTempFile("morpher", "engine");
        try {
            Serializer<MorpherEngine, MorpherEngineMessage> serializer = new Serializer<>(this.converter, engine);
            serializer.serialize(engine, file);
            MorpherEngineMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
            analyzerAgent.close();
            engine.close();
            result.close();
        }
    }

    @Test
    public void testConvertBackWithInvalidAbstractMethodFactory() {
        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setMethodHolderFactoryQualifier(EagerMorpherMethodHolderFactory.class.getName())
                .setAbstractMethodFactoryQualifier(IASTRAMethod.QUALIFIER)
                .setAbstractMethodFactory(Any.pack(MorpherEngineMessage.newBuilder().build()))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot load abstract method factory");
        assertThat(exception).hasCauseExactlyInstanceOf(InvalidProtocolBufferException.class);
    }

    @Test
    public void testConvertBackWithInvalidAnalyzerAgent() {
        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setMethodHolderFactoryQualifier(EagerMorpherMethodHolderFactory.class.getName())
                .setAbstractMethodFactoryQualifier(IASTRAMethod.QUALIFIER)
                .setAbstractMethodFactory(Any.pack(ASTRAMethodConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setAnalyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER)
                .setAnalyzerAgent(Any.pack(MorpherEngineMessage.newBuilder().build()))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot load analyzer agent");
        assertThat(exception).hasCauseExactlyInstanceOf(InvalidProtocolBufferException.class);
    }

    @Test
    public void testConvertBackWithInvalidMethod() {
        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setMethodHolderFactoryQualifier(EagerMorpherMethodHolderFactory.class.getName())
                .setAbstractMethodFactoryQualifier(IASTRAMethod.QUALIFIER)
                .setAbstractMethodFactory(Any.pack(ASTRAMethodConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setAnalyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER)
                .setAnalyzerAgent(Any.pack(HunmorphAnalyzerAgentMessage.newBuilder().setMarkovModelClassName(FullMarkovModel.class.getName()).build()))
                .putAllMethodMap(Map.of("<PLUR>", Any.pack(MorpherEngineMessage.newBuilder().build())))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot load method for <PLUR>");
        assertThat(exception).hasCauseExactlyInstanceOf(InvalidProtocolBufferException.class);
    }

    @Test
    public void testConvertBackWithInvalidMethodHolderFactoryQualifierForClassNotFoundException() {
        String classString = "non-existent";

        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setMethodHolderFactoryQualifier(classString)
                .setAbstractMethodFactoryQualifier(IASTRAMethod.QUALIFIER)
                .setAbstractMethodFactory(Any.pack(ASTRAMethodConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setAnalyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER)
                .setAnalyzerAgent(Any.pack(HunmorphAnalyzerAgentMessage.newBuilder().setMarkovModelClassName(FullMarkovModel.class.getName()).build()))
                .putAllMethodMap(Map.of("<PLUR>", Any.pack(MorpherEngineMessage.newBuilder().build())))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot instantiate class " + classString);
        assertThat(exception).hasCauseExactlyInstanceOf(ClassNotFoundException.class);
    }

    @Test
    public void testConvertBackWithInvalidMethodHolderFactoryQualifierForIllegalAccessException() {
        Class<?> methodHolderFactoryClass = PrivateMethodHolderFactory.class;

        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setMethodHolderFactoryQualifier(methodHolderFactoryClass.getName())
                .setAbstractMethodFactoryQualifier(IASTRAMethod.QUALIFIER)
                .setAbstractMethodFactory(Any.pack(ASTRAMethodConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setAnalyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER)
                .setAnalyzerAgent(Any.pack(HunmorphAnalyzerAgentMessage.newBuilder().setMarkovModelClassName(FullMarkovModel.class.getName()).build()))
                .putAllMethodMap(Map.of("<PLUR>", Any.pack(MorpherEngineMessage.newBuilder().build())))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot instantiate class " + methodHolderFactoryClass.getName());
        assertThat(exception).hasCauseExactlyInstanceOf(IllegalAccessException.class);
    }

    @Test
    public void testConvertBackWithInvalidMethodHolderFactoryQualifierForInvocationTargetException() {
        Class<?> methodHolderFactoryClass = ErroneousMethodHolderFactory.class;

        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setMethodHolderFactoryQualifier(methodHolderFactoryClass.getName())
                .setAbstractMethodFactoryQualifier(IASTRAMethod.QUALIFIER)
                .setAbstractMethodFactory(Any.pack(ASTRAMethodConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setAnalyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER)
                .setAnalyzerAgent(Any.pack(HunmorphAnalyzerAgentMessage.newBuilder().setMarkovModelClassName(FullMarkovModel.class.getName()).build()))
                .putAllMethodMap(Map.of("<PLUR>", Any.pack(MorpherEngineMessage.newBuilder().build())))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot instantiate class " + methodHolderFactoryClass.getName());
        assertThat(exception).hasCauseExactlyInstanceOf(InvocationTargetException.class);
    }

    @Test
    public void testConvertBackWithInvalidMethodHolderFactoryQualifierForInstantiationException() {
        Class<?> methodHolderFactoryClass = AbstractMethodHolderFactory.class;

        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setMethodHolderFactoryQualifier(methodHolderFactoryClass.getName())
                .setAbstractMethodFactoryQualifier(IASTRAMethod.QUALIFIER)
                .setAbstractMethodFactory(Any.pack(ASTRAMethodConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setAnalyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER)
                .setAnalyzerAgent(Any.pack(HunmorphAnalyzerAgentMessage.newBuilder().setMarkovModelClassName(FullMarkovModel.class.getName()).build()))
                .putAllMethodMap(Map.of("<PLUR>", Any.pack(MorpherEngineMessage.newBuilder().build())))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot instantiate class " + methodHolderFactoryClass.getName());
        assertThat(exception).hasCauseExactlyInstanceOf(InstantiationException.class);
    }

    @Test
    public void testConvertBackWithInvalidMethodHolderFactoryQualifierForNoSuchMethodException() {
        Class<?> methodHolderFactoryClass = ComplexMethodHolderFactory.class;

        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setMethodHolderFactoryQualifier(methodHolderFactoryClass.getName())
                .setAbstractMethodFactoryQualifier(IASTRAMethod.QUALIFIER)
                .setAbstractMethodFactory(Any.pack(ASTRAMethodConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setAnalyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER)
                .setAnalyzerAgent(Any.pack(HunmorphAnalyzerAgentMessage.newBuilder().setMarkovModelClassName(FullMarkovModel.class.getName()).build()))
                .putAllMethodMap(Map.of("<PLUR>", Any.pack(MorpherEngineMessage.newBuilder().build())))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot instantiate class " + methodHolderFactoryClass.getName());
        assertThat(exception).hasCauseExactlyInstanceOf(NoSuchMethodException.class);
    }

    @Test
    public void testConvertWithLazyConverter() {
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        ASTRAAbstractMethodFactory abstractMethodFactory = new ASTRAAbstractMethodFactory(configuration);
        IAnalyzerAgent<HunmorphAnalyzerAgentMessage> analyzerAgent = new HunmorphAnalyzerAgent();
        IProbabilityCalculator probabilityCalculator = new MultiplyProbabilityCalculator();
        MorpherEngine engine = new MorpherEngine(this.serviceProvider, new EagerMorpherMethodHolderFactory(), abstractMethodFactory, analyzerAgent, probabilityCalculator, null);
        engine.learn(Corpus.of(Word.of("almát")));

        MorpherEngineConverter converter = new MorpherEngineConverter(this.serviceProvider, true);
        MorpherEngineMessage message = converter.convert(engine);

        assertThat(message.getMethodMapCount()).isZero();
    }

    @Test
    public void testConvertBackWithLazyConverter() {
        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setMethodHolderFactoryQualifier(EagerMorpherMethodHolderFactory.class.getName())
                .setAbstractMethodFactoryQualifier(IASTRAMethod.QUALIFIER)
                .setAbstractMethodFactory(Any.pack(ASTRAMethodConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setAnalyzerAgentQualifier(IHunmorphAnalyzerAgent.QUALIFIER)
                .setAnalyzerAgent(Any.pack(HunmorphAnalyzerAgentMessage.newBuilder().setMarkovModelClassName(FullMarkovModel.class.getName()).build()))
                .putAllMethodMap(Map.of("<PLUR>", Any.pack(MorpherEngineMessage.newBuilder().build())))
                .build();

        MorpherEngineConverter converter = new MorpherEngineConverter(this.serviceProvider, true);
        MorpherEngine engine = converter.convertBack(message);

        assertThat(engine.getMethodHolderMap()).isEmpty();
    }

    @Test
    public void testConvertWithMinMaxProbabilityCalculator() {
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        ASTRAAbstractMethodFactory abstractMethodFactory = new ASTRAAbstractMethodFactory(configuration);
        IAnalyzerAgent<HunmorphAnalyzerAgentMessage> analyzerAgent = new HunmorphAnalyzerAgent();
        IProbabilityCalculator probabilityCalculator = new MinMaxProbabilityCalclator();
        MorpherEngine engine = new MorpherEngine(this.serviceProvider, new EagerMorpherMethodHolderFactory(), abstractMethodFactory, analyzerAgent, probabilityCalculator, null);
        engine.learn(Corpus.of(Word.of("almát")));

        MorpherEngineConverter converter = new MorpherEngineConverter(this.serviceProvider, true);
        MorpherEngineMessage message = converter.convert(engine);

        assertThat(message.getMethodMapCount()).isZero();
    }

    @Test
    public void testConvertAndConvertBackWithMinimumAggregatedWeightThreshold() {
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        ASTRAAbstractMethodFactory abstractMethodFactory = new ASTRAAbstractMethodFactory(configuration);
        IAnalyzerAgent<HunmorphAnalyzerAgentMessage> analyzerAgent = new HunmorphAnalyzerAgent();
        IProbabilityCalculator probabilityCalculator = new MinMaxProbabilityCalclator();
        double minimumAggregatedWeightThreshold = 2.0;
        MorpherEngine engine = new MorpherEngine(this.serviceProvider, new EagerMorpherMethodHolderFactory(), abstractMethodFactory, analyzerAgent, probabilityCalculator, minimumAggregatedWeightThreshold);

        MorpherEngineConverter converter = new MorpherEngineConverter(this.serviceProvider, true);
        MorpherEngineMessage message = converter.convert(engine);
        assertThat(message.getMinimumAggregatedWeightThreshold().getValue()).isEqualTo(minimumAggregatedWeightThreshold);

        MorpherEngine result = converter.convertBack(message);
        assertThat(result.getMinimumAggregatedWeightThreshold()).isEqualTo(message.getMinimumAggregatedWeightThreshold().getValue());
    }

    private static class PrivateMethodHolderFactory {

        private PrivateMethodHolderFactory() {

        }

    }

    private static class ErroneousMethodHolderFactory {

        public ErroneousMethodHolderFactory() {
            throw new IllegalStateException();
        }

    }

    private static abstract class AbstractMethodHolderFactory {

        public AbstractMethodHolderFactory() {

        }

    }

    private static class ComplexMethodHolderFactory {

        public ComplexMethodHolderFactory(String key) {

        }

    }

}
