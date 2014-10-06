package com.github.szgabsz91.morpher.methods.tasr.impl.method;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.methods.api.IInflectionMethod;
import com.github.szgabsz91.morpher.methods.api.IMorpherMethod;
import com.github.szgabsz91.morpher.methods.tasr.config.TASRMethodConfiguration;
import com.github.szgabsz91.morpher.methods.tasr.protocolbuffers.TASRMethodConfigurationMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TASRAbstractMethodFactoryTest {

    @Test
    public void testDefaultConstructor() {
        TASRAbstractMethodFactory abstractMethodFactory = new TASRAbstractMethodFactory();
        assertThat(abstractMethodFactory.getConfiguration()).isNull();
    }

    @Test
    public void testOneArgConstructor() {
        TASRMethodConfiguration configuration = new TASRMethodConfiguration();
        TASRAbstractMethodFactory abstractMethodFactory = new TASRAbstractMethodFactory(configuration);
        assertThat(abstractMethodFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testGetBidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        TASRAbstractMethodFactory abstractMethodFactory = new TASRAbstractMethodFactory(new TASRMethodConfiguration());
        Supplier<IMorpherMethod<?>> supplier = abstractMethodFactory.getBidirectionalFactory(affixType);
        IMorpherMethod<?> morpherMethod = supplier.get();
        assertThat(morpherMethod).isInstanceOf(TASRMethod.class);
        assertThat(morpherMethod.getAffixType()).isEqualTo(affixType);
        TASRMethod tasrMethod = (TASRMethod) morpherMethod;
        assertThat(tasrMethod.isUnidirectional()).isFalse();
    }

    @Test
    public void testGetBidirectionalFactoryWithoutConfiguration() {
        TASRAbstractMethodFactory abstractMethodFactory = new TASRAbstractMethodFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractMethodFactory.getBidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetUnidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        TASRAbstractMethodFactory abstractMethodFactory = new TASRAbstractMethodFactory(new TASRMethodConfiguration());
        Supplier<IInflectionMethod<?>> supplier = abstractMethodFactory.getUnidirectionalFactory(affixType);
        IInflectionMethod<?> inflectionMethod = supplier.get();
        assertThat(inflectionMethod).isInstanceOf(TASRMethod.class);
        assertThat(inflectionMethod.getAffixType()).isEqualTo(affixType);
        TASRMethod tasrMethod = (TASRMethod) inflectionMethod;
        assertThat(tasrMethod.isUnidirectional()).isTrue();
    }

    @Test
    public void testGetUnidirectionalFactoryWithoutConfiguration() {
        TASRAbstractMethodFactory abstractMethodFactory = new TASRAbstractMethodFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractMethodFactory.getUnidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetConfigurationAndSetConfiguration() {
        TASRAbstractMethodFactory abstractMethodFactory = new TASRAbstractMethodFactory();
        assertThat(abstractMethodFactory.getConfiguration()).isNull();
        TASRMethodConfiguration configuration = new TASRMethodConfiguration();
        abstractMethodFactory.setConfiguration(configuration);
        assertThat(abstractMethodFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        TASRMethodConfiguration configuration = new TASRMethodConfiguration();
        TASRAbstractMethodFactory abstractMethodFactory = new TASRAbstractMethodFactory(configuration);
        Any message = Any.pack(abstractMethodFactory.toMessage());

        TASRMethodConfiguration configuration2 = new TASRMethodConfiguration();
        TASRAbstractMethodFactory result = new TASRAbstractMethodFactory(configuration2);
        result.fromMessage(message);

        assertThat(result.getConfiguration()).isNotSameAs(configuration2);
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(TASRMethodConfigurationMessage.newBuilder().build()));
        TASRMethodConfiguration configuration = new TASRMethodConfiguration();
        TASRAbstractMethodFactory abstractMethodFactory = new TASRAbstractMethodFactory(configuration);
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> abstractMethodFactory.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not a TASRMethodConfigurationMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        TASRAbstractMethodFactory abstractMethodFactory = new TASRAbstractMethodFactory(new TASRMethodConfiguration());
        Path file = Files.createTempFile("morpher", "tasr");

        try {
            abstractMethodFactory.saveTo(file);
            TASRAbstractMethodFactory loadedAbstractMethodFactory = new TASRAbstractMethodFactory();
            loadedAbstractMethodFactory.loadFrom(file);
            assertThat(loadedAbstractMethodFactory.getConfiguration()).isNotNull();
        }
        finally {
            Files.delete(file);
        }
    }
    
}
