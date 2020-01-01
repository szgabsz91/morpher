package com.github.szgabsz91.morpher.transformationengines.fst.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.transformationengines.fst.config.FSTTransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.fst.protocolbuffers.FSTTransformationEngineConfigurationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class FSTTransformationEngineConfigurationConverterTest {

    private FSTTransformationEngineConfigurationConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new FSTTransformationEngineConfigurationConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParse() throws IOException {
        FSTTransformationEngineConfiguration configuration = new FSTTransformationEngineConfiguration();

        FSTTransformationEngineConfigurationMessage message = this.converter.convert(configuration);
        assertThat(message).isNotNull();

        FSTTransformationEngineConfiguration result = this.converter.convertBack(message);
        assertThat(result).isNotNull();

        Path file = Files.createTempFile("transformation-engine", "fst");
        try {
            Serializer<FSTTransformationEngineConfiguration, FSTTransformationEngineConfigurationMessage> serializer = new Serializer<>(this.converter, configuration);
            serializer.serialize(configuration, file);
            FSTTransformationEngineConfigurationMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
        }
    }

}
