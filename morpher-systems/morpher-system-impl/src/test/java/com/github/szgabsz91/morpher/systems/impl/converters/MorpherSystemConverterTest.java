package com.github.szgabsz91.morpher.systems.impl.converters;

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
import com.github.szgabsz91.morpher.systems.api.model.MorpherSystemResponse;
import com.github.szgabsz91.morpher.systems.impl.MorpherSystemBuilder;
import com.github.szgabsz91.morpher.systems.impl.impl.MorpherSystem;
import com.github.szgabsz91.morpher.systems.impl.protocolbuffers.MorpherSystemMessage;
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
            MorpherSystemResponse morpherSystemResponse = result.lemmatize(new LanguageAwareLemmatizationInput(Language.of("hu"), LemmatizationInput.of(Word.of("almát"))));
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
