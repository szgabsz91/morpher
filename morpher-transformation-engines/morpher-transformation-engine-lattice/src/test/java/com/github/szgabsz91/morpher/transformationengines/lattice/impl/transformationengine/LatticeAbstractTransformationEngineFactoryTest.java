package com.github.szgabsz91.morpher.transformationengines.lattice.impl.transformationengine;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.transformationengines.api.IBidirectionalTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.api.IForwardsTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.CharacterRepositoryType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.CostCalculatorType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.LatticeBuilderType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.LatticeTransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.WordConverterType;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.LatticeTransformationEngineConfigurationMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LatticeAbstractTransformationEngineFactoryTest {

    @Test
    public void testDefaultConstructor() {
        LatticeAbstractTransformationEngineFactory abstractTransformationEngineFactory = new LatticeAbstractTransformationEngineFactory();
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isNull();
    }

    @Test
    public void testOneArgConstructor() {
        LatticeTransformationEngineConfiguration configuration = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(3)
                .build();
        LatticeAbstractTransformationEngineFactory abstractTransformationEngineFactory = new LatticeAbstractTransformationEngineFactory(configuration);
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isEqualTo(configuration);
    }

    @Test
    public void testGetBidirectionalFactory() {
        LatticeTransformationEngineConfiguration configuration = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(3)
                .build();
        AffixType affixType = AffixType.of("AFF");
        LatticeAbstractTransformationEngineFactory abstractTransformationEngineFactory = new LatticeAbstractTransformationEngineFactory(configuration);
        Supplier<IBidirectionalTransformationEngine<?>> supplier = abstractTransformationEngineFactory.getBidirectionalFactory(affixType);
        IBidirectionalTransformationEngine<?> transformationEngine = supplier.get();
        assertThat(transformationEngine).isInstanceOf(LatticeTransformationEngine.class);
        assertThat(transformationEngine.getAffixType()).isEqualTo(affixType);
        LatticeTransformationEngine latticeTransformationEngine = (LatticeTransformationEngine) transformationEngine;
        assertThat(latticeTransformationEngine.isUnidirectional()).isFalse();
    }

    @Test
    public void testGetBidirectionalFactoryWithoutConfiguration() {
        LatticeAbstractTransformationEngineFactory abstractTransformationEngineFactory = new LatticeAbstractTransformationEngineFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractTransformationEngineFactory.getBidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetUnidirectionalFactory() {
        LatticeTransformationEngineConfiguration configuration = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(3)
                .build();
        AffixType affixType = AffixType.of("AFF");
        LatticeAbstractTransformationEngineFactory abstractTransformationEngineFactory = new LatticeAbstractTransformationEngineFactory(configuration);
        Supplier<IForwardsTransformationEngine<?>> supplier = abstractTransformationEngineFactory.getUnidirectionalFactory(affixType);
        IForwardsTransformationEngine<?> transformationEngine = supplier.get();
        assertThat(transformationEngine).isInstanceOf(LatticeTransformationEngine.class);
        LatticeTransformationEngine latticeTransformationEngine = (LatticeTransformationEngine) transformationEngine;
        assertThat(latticeTransformationEngine.isUnidirectional()).isTrue();
    }

    @Test
    public void testGetUnidirectionalFactoryWithoutConfiguration() {
        LatticeAbstractTransformationEngineFactory abstractTransformationEngineFactory = new LatticeAbstractTransformationEngineFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractTransformationEngineFactory.getUnidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetConfigurationAndSetConfiguration() {
        LatticeAbstractTransformationEngineFactory abstractTransformationEngineFactory = new LatticeAbstractTransformationEngineFactory();
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isNull();

        LatticeTransformationEngineConfiguration configuration = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(3)
                .build();
        abstractTransformationEngineFactory.setConfiguration(configuration);
        assertThat(abstractTransformationEngineFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        LatticeTransformationEngineConfiguration configuration = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(3)
                .build();
        LatticeAbstractTransformationEngineFactory abstractTransformationEngineFactory = new LatticeAbstractTransformationEngineFactory(configuration);
        Any message = Any.pack(abstractTransformationEngineFactory.toMessage());

        LatticeTransformationEngineConfiguration configuration2 = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(4)
                .build();
        LatticeAbstractTransformationEngineFactory result = new LatticeAbstractTransformationEngineFactory(configuration2);
        result.fromMessage(message);

        assertThat(result.getConfiguration()).isEqualTo(configuration);
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(LatticeTransformationEngineConfigurationMessage.newBuilder().build()));
        LatticeTransformationEngineConfiguration configuration = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(3)
                .build();
        LatticeAbstractTransformationEngineFactory abstractTransformationEngineFactory = new LatticeAbstractTransformationEngineFactory(configuration);
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> abstractTransformationEngineFactory.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not a LatticeTransformationEngineConfigurationMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        LatticeTransformationEngineConfiguration configuration = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(3)
                .build();
        LatticeAbstractTransformationEngineFactory abstractTransformationEngineFactory = new LatticeAbstractTransformationEngineFactory(configuration);
        Path file = Files.createTempFile("transformation-engine", "lattice");

        try {
            abstractTransformationEngineFactory.saveTo(file);
            LatticeAbstractTransformationEngineFactory loadedAbstractTransformationEngineFactory = new LatticeAbstractTransformationEngineFactory();
            loadedAbstractTransformationEngineFactory.loadFrom(file);
            assertThat(loadedAbstractTransformationEngineFactory.getConfiguration()).isEqualTo(configuration);
        }
        finally {
            Files.delete(file);
        }
    }

}
