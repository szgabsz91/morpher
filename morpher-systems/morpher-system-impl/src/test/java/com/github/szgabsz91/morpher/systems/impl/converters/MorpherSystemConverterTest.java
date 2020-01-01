package com.github.szgabsz91.morpher.systems.impl.converters;

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
import com.github.szgabsz91.morpher.systems.api.model.MorpherSystemResponse;
import com.github.szgabsz91.morpher.systems.impl.MorpherSystemBuilder;
import com.github.szgabsz91.morpher.systems.impl.impl.MorpherSystem;
import com.github.szgabsz91.morpher.systems.impl.protocolbuffers.MorpherSystemMessage;
import com.github.szgabsz91.morpher.transformationengines.api.factories.IAbstractTransformationEngineFactory;
import com.github.szgabsz91.morpher.transformationengines.astra.IASTRATransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.astra.config.ASTRATransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.astra.config.SearcherType;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.transformationengine.ASTRAAbstractTransformationEngineFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MorpherSystemConverterTest {

    private static final double FITNESS_THRESHOLD = 0.4;
    private static final int MAXIMUM_NUMBER_OF_RESPONSES = 1;

    private MorpherSystemConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new MorpherSystemConverter();
    }

    @Test
    public void testIsConvertBackSupported() {
        boolean result = this.converter.isConvertBackSupported();
        assertThat(result).isFalse();
    }

    @Test
    public void testConvert() throws IOException {
        // Convert
        Path tempFile = Files.createTempFile("morpher", "system");

        try {
            MorpherSystem morpherSystem = (MorpherSystem) new MorpherSystemBuilder()
                    .withLanguage(Language.of("hu"), createMorpherEngine(WordPair.of("alma", "almát")))
                    .build();
            MorpherSystemMessage morpherSystemMessage = this.converter.convert(morpherSystem);
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(Files.newOutputStream(tempFile))) {
                morpherSystemMessage.writeTo(gzipOutputStream);
            }

            // Recreate
            IMorpherSystem result = new MorpherSystemBuilder()
                    .withLanguage(Language.of("hu"), createMorpherEngine())
                    .build();
            result.loadFrom(tempFile);

            // Assert
            MorpherSystemResponse morpherSystemResponse = result.analyze(new LanguageAwareAnalysisInput(Language.of("hu"), AnalysisInput.of(Word.of("almát"))));
            List<MorpherEngineResponse> morpherEngineResponses = morpherSystemResponse.getMorpherEngineResponses();
            assertThat(morpherEngineResponses).hasSize(1);
            MorpherEngineResponse morpherEngineResponse = morpherEngineResponses.get(0);
            assertThat(morpherEngineResponse.getOutput()).hasToString("alma");
        }
        finally {
            Files.delete(tempFile);
        }
    }

    @Test
    public void testConvertBack() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.converter.convertBack(MorpherSystemMessage.newBuilder().build()));
        assertThat(exception).hasMessage("Converting back is not supported for Morpher Systems");
    }

    @Test
    public void testParse() throws IOException {
        // Convert
        Path tempFile = Files.createTempFile("morpher", "system");

        try {
            MorpherSystem morpherSystem = (MorpherSystem) new MorpherSystemBuilder()
                    .withLanguage(Language.of("hu"), createMorpherEngine(WordPair.of("alma", "almát")))
                    .build();
            MorpherSystemMessage morpherSystemMessage = this.converter.convert(morpherSystem);
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(Files.newOutputStream(tempFile))) {
                morpherSystemMessage.writeTo(gzipOutputStream);
            }

            // Assert
            MorpherSystemMessage resultingMessage = this.converter.parse(tempFile);
            assertThat(resultingMessage).isEqualTo(morpherSystemMessage);
        }
        finally {
            Files.delete(tempFile);
        }
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
