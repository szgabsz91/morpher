package com.github.szgabsz91.morpher.methods.tasr.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.methods.tasr.config.TASRMethodConfiguration;
import com.github.szgabsz91.morpher.methods.tasr.protocolbuffers.TASRMethodConfigurationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class TASRMethodConfigurationConverterTest {

    private TASRMethodConfigurationConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new TASRMethodConfigurationConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParse() throws IOException {
        TASRMethodConfiguration configuration = new TASRMethodConfiguration();

        TASRMethodConfigurationMessage message = this.converter.convert(configuration);
        assertThat(message).isNotNull();

        TASRMethodConfiguration result = this.converter.convertBack(message);
        assertThat(result).isNotNull();

        Path file = Files.createTempFile("morpher", "tasr");
        try {
            Serializer<TASRMethodConfiguration, TASRMethodConfigurationMessage> serializer = new Serializer<>(this.converter, configuration);
            serializer.serialize(configuration, file);
            TASRMethodConfigurationMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
        }
    }

}
