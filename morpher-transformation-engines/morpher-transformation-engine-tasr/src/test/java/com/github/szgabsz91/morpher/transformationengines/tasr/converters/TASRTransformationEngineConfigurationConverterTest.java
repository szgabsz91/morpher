package com.github.szgabsz91.morpher.transformationengines.tasr.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.transformationengines.tasr.config.TASRTransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.tasr.protocolbuffers.TASRTransformationEngineConfigurationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class TASRTransformationEngineConfigurationConverterTest {

    private TASRTransformationEngineConfigurationConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new TASRTransformationEngineConfigurationConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParse() throws IOException {
        TASRTransformationEngineConfiguration configuration = new TASRTransformationEngineConfiguration();

        TASRTransformationEngineConfigurationMessage message = this.converter.convert(configuration);
        assertThat(message).isNotNull();

        TASRTransformationEngineConfiguration result = this.converter.convertBack(message);
        assertThat(result).isNotNull();

        Path file = Files.createTempFile("transformation-engine", "tasr");
        try {
            Serializer<TASRTransformationEngineConfiguration, TASRTransformationEngineConfigurationMessage> serializer = new Serializer<>(this.converter, configuration);
            serializer.serialize(configuration, file);
            TASRTransformationEngineConfigurationMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
        }
    }

}
