package com.github.szgabsz91.morpher.transformationengines.dictionary.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.transformationengines.dictionary.config.DictionaryTransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.dictionary.protocolbuffers.DictionaryTransformationEngineConfigurationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class DictionaryTransformationEngineConfigurationConverterTest {

    private DictionaryTransformationEngineConfigurationConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new DictionaryTransformationEngineConfigurationConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParse() throws IOException {
        DictionaryTransformationEngineConfiguration configuration = new DictionaryTransformationEngineConfiguration();

        DictionaryTransformationEngineConfigurationMessage message = this.converter.convert(configuration);
        assertThat(message).isNotNull();

        DictionaryTransformationEngineConfiguration result = this.converter.convertBack(message);
        assertThat(result).isNotNull();

        Path file = Files.createTempFile("transformation-engine", "dictionary");
        try {
            Serializer<DictionaryTransformationEngineConfiguration, DictionaryTransformationEngineConfigurationMessage> serializer = new Serializer<>(this.converter, configuration);
            serializer.serialize(configuration, file);
            DictionaryTransformationEngineConfigurationMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
        }
    }

}
