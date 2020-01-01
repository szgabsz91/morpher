package com.github.szgabsz91.morpher.transformationengines.tasr.impl.transformationengine;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.transformationengines.api.IBidirectionalTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.api.IForwardsTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.tasr.config.TASRTransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.tasr.protocolbuffers.TASRTransformationEngineConfigurationMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TASRAbstractTransformationEngineFactoryTest {

    @Test
    public void testDefaultConstructor() {
        TASRAbstractTransformationEngineFactory abstractTransformationEngineFactory = new TASRAbstractTransformationEngineFactory();
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isNull();
    }

    @Test
    public void testOneArgConstructor() {
        TASRTransformationEngineConfiguration configuration = new TASRTransformationEngineConfiguration();
        TASRAbstractTransformationEngineFactory abstractTransformationEngineFactory = new TASRAbstractTransformationEngineFactory(configuration);
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testGetBidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        TASRAbstractTransformationEngineFactory abstractTransformationEngineFactory = new TASRAbstractTransformationEngineFactory(new TASRTransformationEngineConfiguration());
        Supplier<IBidirectionalTransformationEngine<?>> supplier = abstractTransformationEngineFactory.getBidirectionalFactory(affixType);
        IBidirectionalTransformationEngine<?> transformationEngine = supplier.get();
        assertThat(transformationEngine).isInstanceOf(TASRTransformationEngine.class);
        assertThat(transformationEngine.getAffixType()).isEqualTo(affixType);
        TASRTransformationEngine tasrTransformationEngine = (TASRTransformationEngine) transformationEngine;
        assertThat(tasrTransformationEngine.isUnidirectional()).isFalse();
    }

    @Test
    public void testGetBidirectionalFactoryWithoutConfiguration() {
        TASRAbstractTransformationEngineFactory abstractTransformationEngineFactory = new TASRAbstractTransformationEngineFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractTransformationEngineFactory.getBidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetUnidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        TASRAbstractTransformationEngineFactory abstractTransformationEngineFactory = new TASRAbstractTransformationEngineFactory(new TASRTransformationEngineConfiguration());
        Supplier<IForwardsTransformationEngine<?>> supplier = abstractTransformationEngineFactory.getUnidirectionalFactory(affixType);
        IForwardsTransformationEngine<?> forwardsTransformationEngine = supplier.get();
        assertThat(forwardsTransformationEngine).isInstanceOf(TASRTransformationEngine.class);
        assertThat(forwardsTransformationEngine.getAffixType()).isEqualTo(affixType);
        TASRTransformationEngine tasrTransformationEngine = (TASRTransformationEngine) forwardsTransformationEngine;
        assertThat(tasrTransformationEngine.isUnidirectional()).isTrue();
    }

    @Test
    public void testGetUnidirectionalFactoryWithoutConfiguration() {
        TASRAbstractTransformationEngineFactory abstractTransformationEngineFactory = new TASRAbstractTransformationEngineFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractTransformationEngineFactory.getUnidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetConfigurationAndSetConfiguration() {
        TASRAbstractTransformationEngineFactory abstractTransformationEngineFactory = new TASRAbstractTransformationEngineFactory();
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isNull();
        TASRTransformationEngineConfiguration configuration = new TASRTransformationEngineConfiguration();
        abstractTransformationEngineFactory.setConfiguration(configuration);
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        TASRTransformationEngineConfiguration configuration = new TASRTransformationEngineConfiguration();
        TASRAbstractTransformationEngineFactory abstractTransformationEngineFactory = new TASRAbstractTransformationEngineFactory(configuration);
        Any message = Any.pack(abstractTransformationEngineFactory.toMessage());

        TASRTransformationEngineConfiguration configuration2 = new TASRTransformationEngineConfiguration();
        TASRAbstractTransformationEngineFactory result = new TASRAbstractTransformationEngineFactory(configuration2);
        result.fromMessage(message);

        assertThat(result.getConfiguration()).isNotSameAs(configuration2);
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(TASRTransformationEngineConfigurationMessage.newBuilder().build()));
        TASRTransformationEngineConfiguration configuration = new TASRTransformationEngineConfiguration();
        TASRAbstractTransformationEngineFactory abstractTransformationEngineFactory = new TASRAbstractTransformationEngineFactory(configuration);
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> abstractTransformationEngineFactory.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not a TASRTransformationEngineConfigurationMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        TASRAbstractTransformationEngineFactory abstractTransformationEngineFactory = new TASRAbstractTransformationEngineFactory(new TASRTransformationEngineConfiguration());
        Path file = Files.createTempFile("transformation-engine", "tasr");

        try {
            abstractTransformationEngineFactory.saveTo(file);
            TASRAbstractTransformationEngineFactory loadedAbstractTransformationEngineFactory = new TASRAbstractTransformationEngineFactory();
            loadedAbstractTransformationEngineFactory.loadFrom(file);
            assertThat(loadedAbstractTransformationEngineFactory.getConfiguration()).isNotNull();
        }
        finally {
            Files.delete(file);
        }
    }
    
}
