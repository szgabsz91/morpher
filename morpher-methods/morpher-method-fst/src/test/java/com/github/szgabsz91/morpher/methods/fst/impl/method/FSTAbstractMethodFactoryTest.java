package com.github.szgabsz91.morpher.methods.fst.impl.method;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.methods.api.IInflectionMethod;
import com.github.szgabsz91.morpher.methods.api.IMorpherMethod;
import com.github.szgabsz91.morpher.methods.fst.config.FSTMethodConfiguration;
import com.github.szgabsz91.morpher.methods.fst.protocolbuffers.FSTMethodConfigurationMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FSTAbstractMethodFactoryTest {

    @Test
    public void testDefaultConstructor() {
        FSTAbstractMethodFactory abstractMethodFactory = new FSTAbstractMethodFactory();
        assertThat(abstractMethodFactory.getConfiguration()).isNull();
    }

    @Test
    public void testOneArgConstructor() {
        FSTMethodConfiguration configuration = new FSTMethodConfiguration();
        FSTAbstractMethodFactory abstractMethodFactory = new FSTAbstractMethodFactory(configuration);
        assertThat(abstractMethodFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testGetBidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        FSTAbstractMethodFactory abstractMethodFactory = new FSTAbstractMethodFactory(new FSTMethodConfiguration());
        Supplier<IMorpherMethod<?>> supplier = abstractMethodFactory.getBidirectionalFactory(affixType);
        IMorpherMethod<?> morpherMethod = supplier.get();
        assertThat(morpherMethod).isInstanceOf(FSTMethod.class);
        assertThat(morpherMethod.getAffixType()).isEqualTo(affixType);
        FSTMethod fstMethod = (FSTMethod) morpherMethod;
        assertThat(fstMethod.isUnidirectional()).isFalse();
    }

    @Test
    public void testGetBidirectionalFactoryWithoutConfiguration() {
        FSTAbstractMethodFactory abstractMethodFactory = new FSTAbstractMethodFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractMethodFactory.getBidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetUnidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        FSTAbstractMethodFactory abstractMethodFactory = new FSTAbstractMethodFactory(new FSTMethodConfiguration());
        Supplier<IInflectionMethod<?>> supplier = abstractMethodFactory.getUnidirectionalFactory(affixType);
        IInflectionMethod<?> inflectionMethod = supplier.get();
        assertThat(inflectionMethod).isInstanceOf(FSTMethod.class);
        assertThat(inflectionMethod.getAffixType()).isEqualTo(affixType);
        FSTMethod fstMethod = (FSTMethod) inflectionMethod;
        assertThat(fstMethod.isUnidirectional()).isTrue();
    }

    @Test
    public void testGetUnidirectionalFactoryWithoutConfiguration() {
        FSTAbstractMethodFactory abstractMethodFactory = new FSTAbstractMethodFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractMethodFactory.getUnidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetConfigurationAndSetConfiguration() {
        FSTAbstractMethodFactory abstractMethodFactory = new FSTAbstractMethodFactory();
        assertThat(abstractMethodFactory.getConfiguration()).isNull();
        FSTMethodConfiguration configuration = new FSTMethodConfiguration();
        abstractMethodFactory.setConfiguration(configuration);
        assertThat(abstractMethodFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        FSTMethodConfiguration configuration = new FSTMethodConfiguration();
        FSTAbstractMethodFactory abstractMethodFactory = new FSTAbstractMethodFactory(configuration);
        Any message = Any.pack(abstractMethodFactory.toMessage());

        FSTMethodConfiguration configuration2 = new FSTMethodConfiguration();
        FSTAbstractMethodFactory result = new FSTAbstractMethodFactory(configuration2);
        result.fromMessage(message);

        assertThat(result.getConfiguration()).isNotSameAs(configuration2);
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(FSTMethodConfigurationMessage.newBuilder().build()));
        FSTMethodConfiguration configuration = new FSTMethodConfiguration();
        FSTAbstractMethodFactory abstractMethodFactory = new FSTAbstractMethodFactory(configuration);
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> abstractMethodFactory.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not an FSTMethodConfigurationMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        FSTAbstractMethodFactory abstractMethodFactory = new FSTAbstractMethodFactory(new FSTMethodConfiguration());
        Path file = Files.createTempFile("morpher", "fst");

        try {
            abstractMethodFactory.saveTo(file);
            FSTAbstractMethodFactory loadedAbstractMethodFactory = new FSTAbstractMethodFactory();
            loadedAbstractMethodFactory.loadFrom(file);
            assertThat(loadedAbstractMethodFactory.getConfiguration()).isNotNull();
        }
        finally {
            Files.delete(file);
        }
    }

}
