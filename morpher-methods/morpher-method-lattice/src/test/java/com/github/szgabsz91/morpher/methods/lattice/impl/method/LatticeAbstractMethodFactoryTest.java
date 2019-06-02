package com.github.szgabsz91.morpher.methods.lattice.impl.method;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.methods.api.IInflectionMethod;
import com.github.szgabsz91.morpher.methods.api.IMorpherMethod;
import com.github.szgabsz91.morpher.methods.lattice.config.CharacterRepositoryType;
import com.github.szgabsz91.morpher.methods.lattice.config.CostCalculatorType;
import com.github.szgabsz91.morpher.methods.lattice.config.LatticeBuilderType;
import com.github.szgabsz91.morpher.methods.lattice.config.LatticeMethodConfiguration;
import com.github.szgabsz91.morpher.methods.lattice.config.WordConverterType;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.LatticeMethodConfigurationMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LatticeAbstractMethodFactoryTest {

    @Test
    public void testDefaultConstructor() {
        LatticeAbstractMethodFactory abstractMethodFactory = new LatticeAbstractMethodFactory();
        assertThat(abstractMethodFactory.getConfiguration()).isNull();
    }

    @Test
    public void testOneArgConstructor() {
        LatticeMethodConfiguration configuration = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(3)
                .build();
        LatticeAbstractMethodFactory abstractMethodFactory = new LatticeAbstractMethodFactory(configuration);
        assertThat(abstractMethodFactory.getConfiguration()).isEqualTo(configuration);
    }

    @Test
    public void testGetBidirectionalFactory() {
        LatticeMethodConfiguration configuration = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(3)
                .build();
        AffixType affixType = AffixType.of("AFF");
        LatticeAbstractMethodFactory abstractMethodFactory = new LatticeAbstractMethodFactory(configuration);
        Supplier<IMorpherMethod<?>> supplier = abstractMethodFactory.getBidirectionalFactory(affixType);
        IMorpherMethod<?> morpherMethod = supplier.get();
        assertThat(morpherMethod).isInstanceOf(LatticeMethod.class);
        assertThat(morpherMethod.getAffixType()).isEqualTo(affixType);
        LatticeMethod latticeMethod = (LatticeMethod) morpherMethod;
        assertThat(latticeMethod.isUnidirectional()).isFalse();
    }

    @Test
    public void testGetBidirectionalFactoryWithoutConfiguration() {
        LatticeAbstractMethodFactory abstractMethodFactory = new LatticeAbstractMethodFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractMethodFactory.getBidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetUnidirectionalFactory() {
        LatticeMethodConfiguration configuration = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(3)
                .build();
        AffixType affixType = AffixType.of("AFF");
        LatticeAbstractMethodFactory abstractMethodFactory = new LatticeAbstractMethodFactory(configuration);
        Supplier<IInflectionMethod<?>> supplier = abstractMethodFactory.getUnidirectionalFactory(affixType);
        IInflectionMethod<?> inflectionMethod = supplier.get();
        assertThat(inflectionMethod).isInstanceOf(LatticeMethod.class);
        LatticeMethod latticeMethod = (LatticeMethod) inflectionMethod;
        assertThat(latticeMethod.isUnidirectional()).isTrue();
    }

    @Test
    public void testGetUnidirectionalFactoryWithoutConfiguration() {
        LatticeAbstractMethodFactory abstractMethodFactory = new LatticeAbstractMethodFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractMethodFactory.getUnidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetConfigurationAndSetConfiguration() {
        LatticeAbstractMethodFactory abstractMethodFactory = new LatticeAbstractMethodFactory();
        assertThat(abstractMethodFactory.getConfiguration()).isNull();

        LatticeMethodConfiguration configuration = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(3)
                .build();
        abstractMethodFactory.setConfiguration(configuration);
        assertThat(abstractMethodFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        LatticeMethodConfiguration configuration = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(3)
                .build();
        LatticeAbstractMethodFactory abstractMethodFactory = new LatticeAbstractMethodFactory(configuration);
        Any message = Any.pack(abstractMethodFactory.toMessage());

        LatticeMethodConfiguration configuration2 = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(4)
                .build();
        LatticeAbstractMethodFactory result = new LatticeAbstractMethodFactory(configuration2);
        result.fromMessage(message);

        assertThat(result.getConfiguration()).isEqualTo(configuration);
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(LatticeMethodConfigurationMessage.newBuilder().build()));
        LatticeMethodConfiguration configuration = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(3)
                .build();
        LatticeAbstractMethodFactory abstractMethodFactory = new LatticeAbstractMethodFactory(configuration);
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> abstractMethodFactory.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not a LatticeMethodConfigurationMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        LatticeMethodConfiguration configuration = new LatticeMethodConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(3)
                .build();
        LatticeAbstractMethodFactory abstractMethodFactory = new LatticeAbstractMethodFactory(configuration);
        Path file = Files.createTempFile("morpher", "lattice");

        try {
            abstractMethodFactory.saveTo(file);
            LatticeAbstractMethodFactory loadedAbstractMethodFactory = new LatticeAbstractMethodFactory();
            loadedAbstractMethodFactory.loadFrom(file);
            assertThat(loadedAbstractMethodFactory.getConfiguration()).isEqualTo(configuration);
        }
        finally {
            Files.delete(file);
        }
    }

}
