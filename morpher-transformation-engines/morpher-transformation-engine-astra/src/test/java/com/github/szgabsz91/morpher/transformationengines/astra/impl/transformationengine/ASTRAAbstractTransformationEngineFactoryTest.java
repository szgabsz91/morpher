package com.github.szgabsz91.morpher.transformationengines.astra.impl.transformationengine;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.transformationengines.api.IBidirectionalTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.api.IForwardsTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.astra.config.ASTRATransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.astra.config.SearcherType;
import com.github.szgabsz91.morpher.transformationengines.astra.protocolbuffers.ASTRATransformationEngineConfigurationMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ASTRAAbstractTransformationEngineFactoryTest {

    @Test
    public void testDefaultConstructor() {
        ASTRAAbstractTransformationEngineFactory abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory();
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isNull();
    }

    @Test
    public void testOneArgConstructor() {
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .build();
        ASTRAAbstractTransformationEngineFactory abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory(configuration);
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isEqualTo(configuration);
    }

    @Test
    public void testGetBidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .build();
        ASTRAAbstractTransformationEngineFactory abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory(configuration);
        Supplier<IBidirectionalTransformationEngine<?>> supplier = abstractTransformationEngineFactory.getBidirectionalFactory(affixType);
        IBidirectionalTransformationEngine<?> transformationEngine = supplier.get();
        assertThat(transformationEngine).isInstanceOf(ASTRATransformationEngine.class);
        assertThat(transformationEngine.getAffixType()).isEqualTo(affixType);
        ASTRATransformationEngine astraTransformationEngine = (ASTRATransformationEngine) transformationEngine;
        assertThat(astraTransformationEngine.isUnidirectional()).isFalse();
    }

    @Test
    public void testGetBidirectionalFactoryWithoutConfiguration() {
        ASTRAAbstractTransformationEngineFactory abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractTransformationEngineFactory.getBidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetUnidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .build();
        ASTRAAbstractTransformationEngineFactory abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory(configuration);
        Supplier<IForwardsTransformationEngine<?>> supplier = abstractTransformationEngineFactory.getUnidirectionalFactory(affixType);
        IForwardsTransformationEngine<?> forwardsTransformationEngine = supplier.get();
        assertThat(forwardsTransformationEngine).isInstanceOf(ASTRATransformationEngine.class);
        assertThat(forwardsTransformationEngine.getAffixType()).isEqualTo(affixType);
        ASTRATransformationEngine astraTransformationEngine = (ASTRATransformationEngine) forwardsTransformationEngine;
        assertThat(astraTransformationEngine.isUnidirectional()).isTrue();
    }

    @Test
    public void testGetUnidirectionalFactoryWithoutConfiguration() {
        ASTRAAbstractTransformationEngineFactory abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractTransformationEngineFactory.getUnidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetConfigurationAndSetConfiguration() {
        ASTRAAbstractTransformationEngineFactory abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory();
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isNull();
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.SEQUENTIAL)
                .build();
        abstractTransformationEngineFactory.setConfiguration(configuration);
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        ASTRATransformationEngineConfiguration configuration1 = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .build();
        ASTRAAbstractTransformationEngineFactory abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory(configuration1);
        Any message = Any.pack(abstractTransformationEngineFactory.toMessage());

        ASTRATransformationEngineConfiguration configuration2 = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        ASTRAAbstractTransformationEngineFactory result = new ASTRAAbstractTransformationEngineFactory(configuration2);
        result.fromMessage(message);

        assertThat(result.getConfiguration()).isEqualTo(configuration1);
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(ASTRATransformationEngineConfigurationMessage.newBuilder().build()));
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .build();
        ASTRAAbstractTransformationEngineFactory abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory(configuration);
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> abstractTransformationEngineFactory.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not an ASTRATransformationEngineConfigurationMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .build();
        ASTRAAbstractTransformationEngineFactory abstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory(configuration);
        Path file = Files.createTempFile("transformation-engine", "astra");

        try {
            abstractTransformationEngineFactory.saveTo(file);
            ASTRAAbstractTransformationEngineFactory loadedAbstractTransformationEngineFactory = new ASTRAAbstractTransformationEngineFactory();
            loadedAbstractTransformationEngineFactory.loadFrom(file);
            assertThat(loadedAbstractTransformationEngineFactory.getConfiguration()).isEqualTo(configuration);
        }
        finally {
            Files.delete(file);
        }
    }

}
