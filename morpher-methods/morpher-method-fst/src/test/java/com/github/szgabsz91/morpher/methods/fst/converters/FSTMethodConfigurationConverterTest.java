package com.github.szgabsz91.morpher.methods.fst.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.methods.fst.config.FSTMethodConfiguration;
import com.github.szgabsz91.morpher.methods.fst.protocolbuffers.FSTMethodConfigurationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class FSTMethodConfigurationConverterTest {

    private FSTMethodConfigurationConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new FSTMethodConfigurationConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParse() throws IOException {
        FSTMethodConfiguration configuration = new FSTMethodConfiguration();

        FSTMethodConfigurationMessage message = this.converter.convert(configuration);
        assertThat(message).isNotNull();

        FSTMethodConfiguration result = this.converter.convertBack(message);
        assertThat(result).isNotNull();

        Path file = Files.createTempFile("morpher", "fst");
        try {
            Serializer<FSTMethodConfiguration, FSTMethodConfigurationMessage> serializer = new Serializer<>(this.converter, configuration);
            serializer.serialize(configuration, file);
            FSTMethodConfigurationMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
        }
    }

}
