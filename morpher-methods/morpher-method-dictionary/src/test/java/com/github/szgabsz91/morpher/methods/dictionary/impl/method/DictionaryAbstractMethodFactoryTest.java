package com.github.szgabsz91.morpher.methods.dictionary.impl.method;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.methods.api.IInflectionMethod;
import com.github.szgabsz91.morpher.methods.api.IMorpherMethod;
import com.github.szgabsz91.morpher.methods.dictionary.config.DictionaryMethodConfiguration;
import com.github.szgabsz91.morpher.methods.dictionary.protocolbuffers.DictionaryMethodConfigurationMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DictionaryAbstractMethodFactoryTest {

    @Test
    public void testDefaultConstructor() {
        DictionaryAbstractMethodFactory abstractMethodFactory = new DictionaryAbstractMethodFactory();
        assertThat(abstractMethodFactory.getConfiguration()).isNull();
    }

    @Test
    public void testOneArgConstructor() {
        DictionaryMethodConfiguration configuration = new DictionaryMethodConfiguration();
        DictionaryAbstractMethodFactory abstractMethodFactory = new DictionaryAbstractMethodFactory(configuration);
        assertThat(abstractMethodFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testGetBidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        DictionaryAbstractMethodFactory abstractMethodFactory = new DictionaryAbstractMethodFactory(new DictionaryMethodConfiguration());
        Supplier<IMorpherMethod<?>> supplier = abstractMethodFactory.getBidirectionalFactory(affixType);
        IMorpherMethod<?> dictionaryMethod = supplier.get();
        assertThat(dictionaryMethod).isInstanceOf(DictionaryMethod.class);
        assertThat(dictionaryMethod.getAffixType()).isEqualTo(affixType);
    }

    @Test
    public void testGetBidirectionalFactoryWithoutConfiguration() {
        DictionaryAbstractMethodFactory abstractMethodFactory = new DictionaryAbstractMethodFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractMethodFactory.getBidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetUnidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        DictionaryAbstractMethodFactory abstractMethodFactory = new DictionaryAbstractMethodFactory(new DictionaryMethodConfiguration());
        Supplier<IInflectionMethod<?>> supplier = abstractMethodFactory.getUnidirectionalFactory(affixType);
        IInflectionMethod<?> dictionaryMethod = supplier.get();
        assertThat(dictionaryMethod).isInstanceOf(DictionaryMethod.class);
        assertThat(dictionaryMethod.getAffixType()).isEqualTo(affixType);
    }

    @Test
    public void testGetUnidirectionalFactoryWithoutConfiguration() {
        DictionaryAbstractMethodFactory abstractMethodFactory = new DictionaryAbstractMethodFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractMethodFactory.getUnidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetConfigurationAndSetConfiguration() {
        DictionaryAbstractMethodFactory abstractMethodFactory = new DictionaryAbstractMethodFactory();
        assertThat(abstractMethodFactory.getConfiguration()).isNull();
        DictionaryMethodConfiguration configuration = new DictionaryMethodConfiguration();
        abstractMethodFactory.setConfiguration(configuration);
        assertThat(abstractMethodFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        DictionaryMethodConfiguration configuration = new DictionaryMethodConfiguration();
        DictionaryAbstractMethodFactory abstractMethodFactory = new DictionaryAbstractMethodFactory(configuration);
        Any message = Any.pack(abstractMethodFactory.toMessage());

        DictionaryMethodConfiguration configuration2 = new DictionaryMethodConfiguration();
        DictionaryAbstractMethodFactory result = new DictionaryAbstractMethodFactory(configuration2);
        result.fromMessage(message);

        assertThat(result.getConfiguration()).isNotSameAs(configuration2);
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(DictionaryMethodConfigurationMessage.newBuilder().build()));
        DictionaryMethodConfiguration configuration = new DictionaryMethodConfiguration();
        DictionaryAbstractMethodFactory abstractMethodFactory = new DictionaryAbstractMethodFactory(configuration);
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> abstractMethodFactory.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not a DictionaryMethodConfigurationMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        DictionaryAbstractMethodFactory abstractMethodFactory = new DictionaryAbstractMethodFactory(new DictionaryMethodConfiguration());
        Path file = Files.createTempFile("morpher", "dictionary");

        try {
            abstractMethodFactory.saveTo(file);
            DictionaryAbstractMethodFactory loadedAbstractMethodFactory = new DictionaryAbstractMethodFactory();
            loadedAbstractMethodFactory.loadFrom(file);
            assertThat(loadedAbstractMethodFactory.getConfiguration()).isNotNull();
        }
        finally {
            Files.delete(file);
        }
    }

}
