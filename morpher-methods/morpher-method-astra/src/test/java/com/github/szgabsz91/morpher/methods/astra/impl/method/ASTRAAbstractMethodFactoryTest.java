package com.github.szgabsz91.morpher.methods.astra.impl.method;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.methods.astra.config.ASTRAMethodConfiguration;
import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import com.github.szgabsz91.morpher.methods.api.IInflectionMethod;
import com.github.szgabsz91.morpher.methods.api.IMorpherMethod;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.ASTRAMethodConfigurationMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ASTRAAbstractMethodFactoryTest {

    @Test
    public void testDefaultConstructor() {
        ASTRAAbstractMethodFactory abstractMethodFactory = new ASTRAAbstractMethodFactory();
        assertThat(abstractMethodFactory.getConfiguration()).isNull();
    }

    @Test
    public void testOneArgConstructor() {
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .build();
        ASTRAAbstractMethodFactory abstractMethodFactory = new ASTRAAbstractMethodFactory(configuration);
        assertThat(abstractMethodFactory.getConfiguration()).isEqualTo(configuration);
    }

    @Test
    public void testGetBidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .build();
        ASTRAAbstractMethodFactory abstractMethodFactory = new ASTRAAbstractMethodFactory(configuration);
        Supplier<IMorpherMethod<?>> supplier = abstractMethodFactory.getBidirectionalFactory(affixType);
        IMorpherMethod<?> morpherMethod = supplier.get();
        assertThat(morpherMethod).isInstanceOf(ASTRAMethod.class);
        assertThat(morpherMethod.getAffixType()).isEqualTo(affixType);
        ASTRAMethod astraMethod = (ASTRAMethod) morpherMethod;
        assertThat(astraMethod.isUnidirectional()).isFalse();
    }

    @Test
    public void testGetBidirectionalFactoryWithoutConfiguration() {
        ASTRAAbstractMethodFactory abstractMethodFactory = new ASTRAAbstractMethodFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractMethodFactory.getBidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetUnidirectionalFactory() {
        AffixType affixType = AffixType.of("AFF");
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .build();
        ASTRAAbstractMethodFactory abstractMethodFactory = new ASTRAAbstractMethodFactory(configuration);
        Supplier<IInflectionMethod<?>> supplier = abstractMethodFactory.getUnidirectionalFactory(affixType);
        IInflectionMethod<?> inflectionMethod = supplier.get();
        assertThat(inflectionMethod).isInstanceOf(ASTRAMethod.class);
        assertThat(inflectionMethod.getAffixType()).isEqualTo(affixType);
        ASTRAMethod astraMethod = (ASTRAMethod) inflectionMethod;
        assertThat(astraMethod.isUnidirectional()).isTrue();
    }

    @Test
    public void testGetUnidirectionalFactoryWithoutConfiguration() {
        ASTRAAbstractMethodFactory abstractMethodFactory = new ASTRAAbstractMethodFactory();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> abstractMethodFactory.getUnidirectionalFactory(null));
        assertThat(exception).hasMessage("No configuration is provided");
    }

    @Test
    public void testGetConfigurationAndSetConfiguration() {
        ASTRAAbstractMethodFactory abstractMethodFactory = new ASTRAAbstractMethodFactory();
        assertThat(abstractMethodFactory.getConfiguration()).isNull();
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.SEQUENTIAL)
                .build();
        abstractMethodFactory.setConfiguration(configuration);
        assertThat(abstractMethodFactory.getConfiguration()).isSameAs(configuration);
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        ASTRAMethodConfiguration configuration1 = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .build();
        ASTRAAbstractMethodFactory abstractMethodFactory = new ASTRAAbstractMethodFactory(configuration1);
        Any message = Any.pack(abstractMethodFactory.toMessage());

        ASTRAMethodConfiguration configuration2 = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .build();
        ASTRAAbstractMethodFactory result = new ASTRAAbstractMethodFactory(configuration2);
        result.fromMessage(message);

        assertThat(result.getConfiguration()).isEqualTo(configuration1);
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(ASTRAMethodConfigurationMessage.newBuilder().build()));
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .build();
        ASTRAAbstractMethodFactory abstractMethodFactory = new ASTRAAbstractMethodFactory(configuration);
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> abstractMethodFactory.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not an ASTRAMethodConfigurationMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PARALLEL)
                .build();
        ASTRAAbstractMethodFactory abstractMethodFactory = new ASTRAAbstractMethodFactory(configuration);
        Path file = Files.createTempFile("morpher", "astra");

        try {
            abstractMethodFactory.saveTo(file);
            ASTRAAbstractMethodFactory loadedAbstractMethodFactory = new ASTRAAbstractMethodFactory();
            loadedAbstractMethodFactory.loadFrom(file);
            assertThat(loadedAbstractMethodFactory.getConfiguration()).isEqualTo(configuration);
        }
        finally {
            Files.delete(file);
        }
    }

}
