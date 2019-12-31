package com.github.szgabsz91.morpher.engines.impl.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.Corpus;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.services.ServiceProvider;
import com.github.szgabsz91.morpher.engines.api.model.AnalysisInput;
import com.github.szgabsz91.morpher.engines.impl.impl.MorpherEngine;
import com.github.szgabsz91.morpher.engines.impl.impl.probability.IProbabilityCalculator;
import com.github.szgabsz91.morpher.engines.impl.impl.probability.MinMaxProbabilityCalclator;
import com.github.szgabsz91.morpher.engines.impl.impl.probability.MultiplyProbabilityCalculator;
import com.github.szgabsz91.morpher.engines.impl.protocolbuffers.MorpherEngineMessage;
import com.github.szgabsz91.morpher.engines.impl.protocolbuffers.ProbabilityCalculatorTypeMessage;
import com.github.szgabsz91.morpher.engines.impl.transformationengineholderfactories.EagerTransformationEngineHolderFactory;
import com.github.szgabsz91.morpher.engines.impl.transformationengineholderfactories.ITransformationEngineHolderFactory;
import com.github.szgabsz91.morpher.languagehandlers.api.ILanguageHandler;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.IHunmorphLanguageHandler;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.HunmorphLanguageHandler;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.markov.FullMarkovModel;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.protocolbuffers.HunmorphLanguageHandlerMessage;
import com.github.szgabsz91.morpher.transformationengines.api.factories.IAbstractTransformationEngineFactory;
import com.github.szgabsz91.morpher.transformationengines.astra.IASTRATransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.astra.config.ASTRATransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.astra.config.SearcherType;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.transformationengine.ASTRAAbstractTransformationEngineFactory;
import com.github.szgabsz91.morpher.transformationengines.astra.protocolbuffers.ASTRATransformationEngineConfigurationMessage;
import com.github.szgabsz91.morpher.transformationengines.astra.protocolbuffers.SearcherTypeMessage;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MorpherEngineConverterTest {

    private ServiceProvider serviceProvider;
    private MorpherEngineConverter converter;

    @BeforeEach
    @SuppressWarnings("rawtypes")
    public void setUp() {
        Function<Class<?>, Stream<? extends ServiceLoader.Provider<?>>> serviceLoader = clazz -> {
            if (clazz.equals(ITransformationEngineHolderFactory.class)) {
                ServiceLoader.Provider provider = mock(ServiceLoader.Provider.class);
                when(provider.type()).thenReturn(EagerTransformationEngineHolderFactory.class);
                when(provider.get()).thenReturn(new EagerTransformationEngineHolderFactory());
                return Stream.<ServiceLoader.Provider<?>>of(provider);
            }

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
        this.converter = new MorpherEngineConverter(serviceProvider);
    }

    @Test
    public void testConvertAndCovertBackAndParse() throws IOException {
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        ASTRAAbstractTransformationEngineFactory abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory(configuration);
        ILanguageHandler<HunmorphLanguageHandlerMessage> languageHandler = new HunmorphLanguageHandler();
        IProbabilityCalculator probabilityCalculator = new MultiplyProbabilityCalculator();
        MorpherEngine engine = new MorpherEngine(this.serviceProvider, new EagerTransformationEngineHolderFactory(), abstractTransformationEngineFactory, languageHandler, probabilityCalculator, null);

        Word word = Word.of("almák");
        engine.learn(Corpus.of(word));
        engine.analyze(AnalysisInput.of(word));

        MorpherEngineMessage message = this.converter.convert(engine);
        assertThat(message.getAbstractTransformationEngineFactoryQualifier()).isEqualTo(IASTRATransformationEngine.QUALIFIER);
        assertThat(message.getLanguageHandlerQualifier()).isEqualTo(IHunmorphLanguageHandler.QUALIFIER);
        assertThat(message.getProbabilityCalculatorType()).isEqualTo(ProbabilityCalculatorTypeMessage.MULTIPLY);

        MorpherEngine result = this.converter.convertBack(message);
        assertThat(result.getAbstractTransformationEngineFactory()).isInstanceOf(ASTRAAbstractTransformationEngineFactory.class);
        assertThat(result.getLanguageHandler()).isInstanceOf(HunmorphLanguageHandler.class);
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
            languageHandler.close();
            engine.close();
            result.close();
        }
    }

    @Test
    public void testConvertBackWithInvalidAbstractTransformationEngineFactory() {
        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setTransformationEngineHolderFactoryQualifier(EagerTransformationEngineHolderFactory.class.getName())
                .setAbstractTransformationEngineFactoryQualifier(IASTRATransformationEngine.QUALIFIER)
                .setAbstractTransformationEngineFactory(Any.pack(MorpherEngineMessage.newBuilder().build()))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot load abstract transformation engine factory");
        assertThat(exception).hasCauseExactlyInstanceOf(InvalidProtocolBufferException.class);
    }

    @Test
    public void testConvertBackWithInvalidLanguageHandler() {
        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setTransformationEngineHolderFactoryQualifier(EagerTransformationEngineHolderFactory.class.getName())
                .setAbstractTransformationEngineFactoryQualifier(IASTRATransformationEngine.QUALIFIER)
                .setAbstractTransformationEngineFactory(Any.pack(ASTRATransformationEngineConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setLanguageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .setLanguageHandler(Any.pack(MorpherEngineMessage.newBuilder().build()))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot load language handler");
        assertThat(exception).hasCauseExactlyInstanceOf(InvalidProtocolBufferException.class);
    }

    @Test
    public void testConvertBackWithInvalidTransformationEngine() {
        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setTransformationEngineHolderFactoryQualifier(EagerTransformationEngineHolderFactory.class.getName())
                .setAbstractTransformationEngineFactoryQualifier(IASTRATransformationEngine.QUALIFIER)
                .setAbstractTransformationEngineFactory(Any.pack(ASTRATransformationEngineConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setLanguageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .setLanguageHandler(Any.pack(HunmorphLanguageHandlerMessage.newBuilder().setMarkovModelClassName(FullMarkovModel.class.getName()).build()))
                .putAllTransformationEngineMap(Map.of("<PLUR>", Any.pack(MorpherEngineMessage.newBuilder().build())))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot load transformation engine for <PLUR>");
        assertThat(exception).hasCauseExactlyInstanceOf(InvalidProtocolBufferException.class);
    }

    @Test
    public void testConvertBackWithInvalidTransformationEngineHolderFactoryQualifierForClassNotFoundException() {
        String classString = "non-existent";

        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setTransformationEngineHolderFactoryQualifier(classString)
                .setAbstractTransformationEngineFactoryQualifier(IASTRATransformationEngine.QUALIFIER)
                .setAbstractTransformationEngineFactory(Any.pack(ASTRATransformationEngineConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setLanguageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .setLanguageHandler(Any.pack(HunmorphLanguageHandlerMessage.newBuilder().setMarkovModelClassName(FullMarkovModel.class.getName()).build()))
                .putAllTransformationEngineMap(Map.of("<PLUR>", Any.pack(MorpherEngineMessage.newBuilder().build())))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot instantiate class " + classString);
        assertThat(exception).hasCauseExactlyInstanceOf(ClassNotFoundException.class);
    }

    @Test
    public void testConvertBackWithInvalidTransformationEngineHolderFactoryQualifierForIllegalAccessException() {
        Class<?> transformationEngineFactoryClass = PrivateMethodHolderFactory.class;

        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setTransformationEngineHolderFactoryQualifier(transformationEngineFactoryClass.getName())
                .setAbstractTransformationEngineFactoryQualifier(IASTRATransformationEngine.QUALIFIER)
                .setAbstractTransformationEngineFactory(Any.pack(ASTRATransformationEngineConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setLanguageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .setLanguageHandler(Any.pack(HunmorphLanguageHandlerMessage.newBuilder().setMarkovModelClassName(FullMarkovModel.class.getName()).build()))
                .putAllTransformationEngineMap(Map.of("<PLUR>", Any.pack(MorpherEngineMessage.newBuilder().build())))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot instantiate class " + transformationEngineFactoryClass.getName());
        assertThat(exception).hasCauseExactlyInstanceOf(IllegalAccessException.class);
    }

    @Test
    public void testConvertBackWithInvalidTransformationEngineHolderFactoryQualifierForInvocationTargetException() {
        Class<?> transformationEngineFactoryClass = ErroneousMethodHolderFactory.class;

        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setTransformationEngineHolderFactoryQualifier(transformationEngineFactoryClass.getName())
                .setAbstractTransformationEngineFactoryQualifier(IASTRATransformationEngine.QUALIFIER)
                .setAbstractTransformationEngineFactory(Any.pack(ASTRATransformationEngineConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setLanguageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .setLanguageHandler(Any.pack(HunmorphLanguageHandlerMessage.newBuilder().setMarkovModelClassName(FullMarkovModel.class.getName()).build()))
                .putAllTransformationEngineMap(Map.of("<PLUR>", Any.pack(MorpherEngineMessage.newBuilder().build())))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot instantiate class " + transformationEngineFactoryClass.getName());
        assertThat(exception).hasCauseExactlyInstanceOf(InvocationTargetException.class);
    }

    @Test
    public void testConvertBackWithInvalidTransformationEngineHolderFactoryQualifierForInstantiationException() {
        Class<?> transformationEngineFactoryClass = AbstractMethodHolderFactory.class;

        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setTransformationEngineHolderFactoryQualifier(transformationEngineFactoryClass.getName())
                .setAbstractTransformationEngineFactoryQualifier(IASTRATransformationEngine.QUALIFIER)
                .setAbstractTransformationEngineFactory(Any.pack(ASTRATransformationEngineConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setLanguageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .setLanguageHandler(Any.pack(HunmorphLanguageHandlerMessage.newBuilder().setMarkovModelClassName(FullMarkovModel.class.getName()).build()))
                .putAllTransformationEngineMap(Map.of("<PLUR>", Any.pack(MorpherEngineMessage.newBuilder().build())))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot instantiate class " + transformationEngineFactoryClass.getName());
        assertThat(exception).hasCauseExactlyInstanceOf(InstantiationException.class);
    }

    @Test
    public void testConvertBackWithInvalidTransformationEngineHolderFactoryQualifierForNoSuchMethodException() {
        Class<?> transformationEngineFactoryClass = ComplexMethodHolderFactory.class;

        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setTransformationEngineHolderFactoryQualifier(transformationEngineFactoryClass.getName())
                .setAbstractTransformationEngineFactoryQualifier(IASTRATransformationEngine.QUALIFIER)
                .setAbstractTransformationEngineFactory(Any.pack(ASTRATransformationEngineConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setLanguageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .setLanguageHandler(Any.pack(HunmorphLanguageHandlerMessage.newBuilder().setMarkovModelClassName(FullMarkovModel.class.getName()).build()))
                .putAllTransformationEngineMap(Map.of("<PLUR>", Any.pack(MorpherEngineMessage.newBuilder().build())))
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.converter.convertBack(message));
        assertThat(exception).hasMessage("Cannot instantiate class " + transformationEngineFactoryClass.getName());
        assertThat(exception).hasCauseExactlyInstanceOf(NoSuchMethodException.class);
    }

    @Test
    public void testConvertWithLazyConverter() {
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        ASTRAAbstractTransformationEngineFactory abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory(configuration);
        ILanguageHandler<HunmorphLanguageHandlerMessage> languageHandler = new HunmorphLanguageHandler();
        IProbabilityCalculator probabilityCalculator = new MultiplyProbabilityCalculator();
        MorpherEngine engine = new MorpherEngine(this.serviceProvider, new EagerTransformationEngineHolderFactory(), abstractTransformationEngineFactory, languageHandler, probabilityCalculator, null);
        engine.learn(Corpus.of(Word.of("almát")));

        MorpherEngineConverter converter = new MorpherEngineConverter(this.serviceProvider, true);
        MorpherEngineMessage message = converter.convert(engine);

        assertThat(message.getTransformationEngineMapCount()).isZero();
    }

    @Test
    public void testConvertBackWithLazyConverter() {
        MorpherEngineMessage message = MorpherEngineMessage.newBuilder()
                .setTransformationEngineHolderFactoryQualifier(EagerTransformationEngineHolderFactory.class.getName())
                .setAbstractTransformationEngineFactoryQualifier(IASTRATransformationEngine.QUALIFIER)
                .setAbstractTransformationEngineFactory(Any.pack(ASTRATransformationEngineConfigurationMessage.newBuilder().setSearcherType(SearcherTypeMessage.SEQUENTIAL).setMaximumNumberOfResponses(1).build()))
                .setLanguageHandlerQualifier(IHunmorphLanguageHandler.QUALIFIER)
                .setLanguageHandler(Any.pack(HunmorphLanguageHandlerMessage.newBuilder().setMarkovModelClassName(FullMarkovModel.class.getName()).build()))
                .putAllTransformationEngineMap(Map.of("<PLUR>", Any.pack(MorpherEngineMessage.newBuilder().build())))
                .build();

        MorpherEngineConverter converter = new MorpherEngineConverter(this.serviceProvider, true);
        MorpherEngine engine = converter.convertBack(message);

        assertThat(engine.getTransformationEngineHolderMap()).isEmpty();
    }

    @Test
    public void testConvertWithMinMaxProbabilityCalculator() {
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        ASTRAAbstractTransformationEngineFactory abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory(configuration);
        ILanguageHandler<HunmorphLanguageHandlerMessage> languageHandler = new HunmorphLanguageHandler();
        IProbabilityCalculator probabilityCalculator = new MinMaxProbabilityCalclator();
        MorpherEngine engine = new MorpherEngine(this.serviceProvider, new EagerTransformationEngineHolderFactory(), abstractTransformationEngineFactory, languageHandler, probabilityCalculator, null);
        engine.learn(Corpus.of(Word.of("almát")));

        MorpherEngineConverter converter = new MorpherEngineConverter(this.serviceProvider, true);
        MorpherEngineMessage message = converter.convert(engine);

        assertThat(message.getTransformationEngineMapCount()).isZero();
    }

    @Test
    public void testConvertAndConvertBackWithMinimumAggregatedWeightThreshold() {
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        ASTRAAbstractTransformationEngineFactory abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory(configuration);
        ILanguageHandler<HunmorphLanguageHandlerMessage> languageHandler = new HunmorphLanguageHandler();
        IProbabilityCalculator probabilityCalculator = new MinMaxProbabilityCalclator();
        double minimumAggregatedWeightThreshold = 2.0;
        MorpherEngine engine = new MorpherEngine(this.serviceProvider, new EagerTransformationEngineHolderFactory(), abstractTransformationEngineFactory, languageHandler, probabilityCalculator, minimumAggregatedWeightThreshold);

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
