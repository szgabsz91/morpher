package com.github.szgabsz91.morpher.methods.dictionary.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.methods.dictionary.config.DictionaryMethodConfiguration;
import com.github.szgabsz91.morpher.methods.dictionary.protocolbuffers.DictionaryMethodConfigurationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class DictionaryMethodConfigurationConverterTest {

    private DictionaryMethodConfigurationConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new DictionaryMethodConfigurationConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParse() throws IOException {
        DictionaryMethodConfiguration configuration = new DictionaryMethodConfiguration();

        DictionaryMethodConfigurationMessage message = this.converter.convert(configuration);
        assertThat(message).isNotNull();

        DictionaryMethodConfiguration result = this.converter.convertBack(message);
        assertThat(result).isNotNull();

        Path file = Files.createTempFile("morpher", "dictionary");
        try {
            Serializer<DictionaryMethodConfiguration, DictionaryMethodConfigurationMessage> serializer = new Serializer<>(this.converter, configuration);
            serializer.serialize(configuration, file);
            DictionaryMethodConfigurationMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
        }
    }

}
