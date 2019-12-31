package com.github.szgabsz91.morpher.transformationengines.fst.impl.transformationengine;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.transformationengines.api.IBidirectionalTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.api.IForwardsTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.fst.config.FSTTransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.fst.protocolbuffers.FSTTransformationEngineConfigurationMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FSTAbstractTransformationEngineFactoryTest {

    @Test
    public void testDefaultConstructor() {
        FSTAbstractTransformationEngineFactory abstractTransformationEngineFactory = new FSTAbstractTransformationEngineFactory();
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isNull();
    }

    @Test
    public void testOneArgConstructor() {
        FSTTransformationEngineConfiguration configuration = new FSTTransformationEngineConfiguration();
        FSTAbstractTransformationEngineFactory abstractTransformationEngineFactory = new FSTAbstractTransformationEngineFactory(configuration);
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testGetBidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        FSTAbstractTransformationEngineFactory abstractTransformationEngineFactory = new FSTAbstractTransformationEngineFactory(new FSTTransformationEngineConfiguration());
        Supplier<IBidirectionalTransformationEngine<?>> supplier = abstractTransformationEngineFactory.getBidirectionalFactory(affixType);
        IBidirectionalTransformationEngine<?> transformationEngine = supplier.get();
        assertThat(transformationEngine).isInstanceOf(FSTTransformationEngine.class);
        assertThat(transformationEngine.getAffixType()).isEqualTo(affixType);
        FSTTransformationEngine fstTransformationEngine = (FSTTransformationEngine) transformationEngine;
        assertThat(fstTransformationEngine.isUnidirectional()).isFalse();
    }

    @Test
    public void testGetBidirectionalFactoryWithoutConfiguration() {
        FSTAbstractTransformationEngineFactory abstractTransformationEngineFactory = new FSTAbstractTransformationEngineFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractTransformationEngineFactory.getBidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetUnidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        FSTAbstractTransformationEngineFactory abstractTransformationEngineFactory = new FSTAbstractTransformationEngineFactory(new FSTTransformationEngineConfiguration());
        Supplier<IForwardsTransformationEngine<?>> supplier = abstractTransformationEngineFactory.getUnidirectionalFactory(affixType);
        IForwardsTransformationEngine<?> forwardsTransformationEngine = supplier.get();
        assertThat(forwardsTransformationEngine).isInstanceOf(FSTTransformationEngine.class);
        assertThat(forwardsTransformationEngine.getAffixType()).isEqualTo(affixType);
        FSTTransformationEngine fstTransformationEngine = (FSTTransformationEngine) forwardsTransformationEngine;
        assertThat(fstTransformationEngine.isUnidirectional()).isTrue();
    }

    @Test
    public void testGetUnidirectionalFactoryWithoutConfiguration() {
        FSTAbstractTransformationEngineFactory abstractTransformationEngineFactory = new FSTAbstractTransformationEngineFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractTransformationEngineFactory.getUnidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetConfigurationAndSetConfiguration() {
        FSTAbstractTransformationEngineFactory abstractTransformationEngineFactory = new FSTAbstractTransformationEngineFactory();
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isNull();
        FSTTransformationEngineConfiguration configuration = new FSTTransformationEngineConfiguration();
        abstractTransformationEngineFactory.setConfiguration(configuration);
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        FSTTransformationEngineConfiguration configuration = new FSTTransformationEngineConfiguration();
        FSTAbstractTransformationEngineFactory abstractTransformationEngineFactory = new FSTAbstractTransformationEngineFactory(configuration);
        Any message = Any.pack(abstractTransformationEngineFactory.toMessage());

        FSTTransformationEngineConfiguration configuration2 = new FSTTransformationEngineConfiguration();
        FSTAbstractTransformationEngineFactory result = new FSTAbstractTransformationEngineFactory(configuration2);
        result.fromMessage(message);

        assertThat(result.getConfiguration()).isNotSameAs(configuration2);
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(FSTTransformationEngineConfigurationMessage.newBuilder().build()));
        FSTTransformationEngineConfiguration configuration = new FSTTransformationEngineConfiguration();
        FSTAbstractTransformationEngineFactory abstractTransformationEngineFactory = new FSTAbstractTransformationEngineFactory(configuration);
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> abstractTransformationEngineFactory.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not an FSTTransformationEngineConfigurationMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        FSTAbstractTransformationEngineFactory abstractTransformationEngineFactory = new FSTAbstractTransformationEngineFactory(new FSTTransformationEngineConfiguration());
        Path file = Files.createTempFile("transformation-engine", "fst");

        try {
            abstractTransformationEngineFactory.saveTo(file);
            FSTAbstractTransformationEngineFactory loadedAbstractTransformationEngineFactory = new FSTAbstractTransformationEngineFactory();
            loadedAbstractTransformationEngineFactory.loadFrom(file);
            assertThat(loadedAbstractTransformationEngineFactory.getConfiguration()).isNotNull();
        }
        finally {
            Files.delete(file);
        }
    }

}
