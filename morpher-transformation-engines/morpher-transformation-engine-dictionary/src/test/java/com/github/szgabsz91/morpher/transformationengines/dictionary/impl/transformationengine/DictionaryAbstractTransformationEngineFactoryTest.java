package com.github.szgabsz91.morpher.transformationengines.dictionary.impl.transformationengine;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.transformationengines.api.IBidirectionalTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.api.IForwardsTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.dictionary.config.DictionaryTransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.dictionary.protocolbuffers.DictionaryTransformationEngineConfigurationMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DictionaryAbstractTransformationEngineFactoryTest {

    @Test
    public void testDefaultConstructor() {
        DictionaryAbstractTransformationEngineFactory abstractTransformationEngineFactory = new DictionaryAbstractTransformationEngineFactory();
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isNull();
    }

    @Test
    public void testOneArgConstructor() {
        DictionaryTransformationEngineConfiguration configuration = new DictionaryTransformationEngineConfiguration();
        DictionaryAbstractTransformationEngineFactory abstractTransformationEngineFactory = new DictionaryAbstractTransformationEngineFactory(configuration);
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testGetBidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        DictionaryAbstractTransformationEngineFactory abstractTransformationEngineFactory = new DictionaryAbstractTransformationEngineFactory(new DictionaryTransformationEngineConfiguration());
        Supplier<IBidirectionalTransformationEngine<?>> supplier = abstractTransformationEngineFactory.getBidirectionalFactory(affixType);
        IBidirectionalTransformationEngine<?> transformationEngine = supplier.get();
        assertThat(transformationEngine).isInstanceOf(DictionaryTransformationEngine.class);
        assertThat(transformationEngine.getAffixType()).isEqualTo(affixType);
    }

    @Test
    public void testGetBidirectionalFactoryWithoutConfiguration() {
        DictionaryAbstractTransformationEngineFactory abstractTransformationEngineFactory = new DictionaryAbstractTransformationEngineFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractTransformationEngineFactory.getBidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetUnidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        DictionaryAbstractTransformationEngineFactory abstractTransformationEngineFactory = new DictionaryAbstractTransformationEngineFactory(new DictionaryTransformationEngineConfiguration());
        Supplier<IForwardsTransformationEngine<?>> supplier = abstractTransformationEngineFactory.getUnidirectionalFactory(affixType);
        IForwardsTransformationEngine<?> transformationEngine = supplier.get();
        assertThat(transformationEngine).isInstanceOf(DictionaryTransformationEngine.class);
        assertThat(transformationEngine.getAffixType()).isEqualTo(affixType);
    }

    @Test
    public void testGetUnidirectionalFactoryWithoutConfiguration() {
        DictionaryAbstractTransformationEngineFactory abstractTransformationEngineFactory = new DictionaryAbstractTransformationEngineFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractTransformationEngineFactory.getUnidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetConfigurationAndSetConfiguration() {
        DictionaryAbstractTransformationEngineFactory abstractTransformationEngineFactory = new DictionaryAbstractTransformationEngineFactory();
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isNull();
        DictionaryTransformationEngineConfiguration configuration = new DictionaryTransformationEngineConfiguration();
        abstractTransformationEngineFactory.setConfiguration(configuration);
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        DictionaryTransformationEngineConfiguration configuration = new DictionaryTransformationEngineConfiguration();
        DictionaryAbstractTransformationEngineFactory abstractTransformationEngineFactory = new DictionaryAbstractTransformationEngineFactory(configuration);
        Any message = Any.pack(abstractTransformationEngineFactory.toMessage());

        DictionaryTransformationEngineConfiguration configuration2 = new DictionaryTransformationEngineConfiguration();
        DictionaryAbstractTransformationEngineFactory result = new DictionaryAbstractTransformationEngineFactory(configuration2);
        result.fromMessage(message);

        assertThat(result.getConfiguration()).isNotSameAs(configuration2);
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(DictionaryTransformationEngineConfigurationMessage.newBuilder().build()));
        DictionaryTransformationEngineConfiguration configuration = new DictionaryTransformationEngineConfiguration();
        DictionaryAbstractTransformationEngineFactory abstractTransformationEngineFactory = new DictionaryAbstractTransformationEngineFactory(configuration);
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> abstractTransformationEngineFactory.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not a DictionaryTransformationEngineConfigurationMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        DictionaryAbstractTransformationEngineFactory abstractTransformationEngineFactory = new DictionaryAbstractTransformationEngineFactory(new DictionaryTransformationEngineConfiguration());
        Path file = Files.createTempFile("transformation-engine", "dictionary");

        try {
            abstractTransformationEngineFactory.saveTo(file);
            DictionaryAbstractTransformationEngineFactory loadedAbstractTransformationEngineFactory = new DictionaryAbstractTransformationEngineFactory();
            loadedAbstractTransformationEngineFactory.loadFrom(file);
            assertThat(loadedAbstractTransformationEngineFactory.getConfiguration()).isNotNull();
        }
        finally {
            Files.delete(file);
        }
    }

}
