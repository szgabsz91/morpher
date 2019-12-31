package com.github.szgabsz91.morpher.transformationengines.lattice.converters;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.CharacterRepositoryType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.CostCalculatorType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.LatticeBuilderType;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.LatticeTransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.lattice.config.WordConverterType;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.CharacterRepositoryTypeMessage;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.CostCalculatorTypeMessage;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.LatticeBuilderTypeMessage;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.LatticeTransformationEngineConfigurationMessage;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.WordConverterTypeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class LatticeTransformationEngineConfigurationConverterTest {

    private LatticeTransformationEngineConfigurationConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new LatticeTransformationEngineConfigurationConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParse() throws IOException {
        LatticeTransformationEngineConfiguration configuration = new LatticeTransformationEngineConfiguration.Builder()
                .latticeBuilderType(LatticeBuilderType.MINIMAL)
                .wordConverterType(WordConverterType.IDENTITY)
                .costCalculatorType(CostCalculatorType.DEFAULT)
                .characterRepositoryType(CharacterRepositoryType.SIMPLE)
                .maximalContextSize(5)
                .build();

        LatticeTransformationEngineConfigurationMessage message = this.converter.convert(configuration);
        assertThat(message).isNotNull();
        assertThat(message.getLatticeBuilderType()).isEqualTo(LatticeBuilderTypeMessage.MINIMAL);
        assertThat(message.getWordConverterType()).isEqualTo(WordConverterTypeMessage.IDENTITY);
        assertThat(message.getCostCalculatorType()).isEqualTo(CostCalculatorTypeMessage.DEFAULT);
        assertThat(message.getCharacterRepositoryType()).isEqualTo(CharacterRepositoryTypeMessage.SIMPLE);
        assertThat(message.getMaximalContextSize()).isEqualTo(configuration.getMaximalContextSize());

        LatticeTransformationEngineConfiguration result = this.converter.convertBack(message);
        assertThat(result).isNotNull();
        assertThat(result.getLatticeBuilderType()).isEqualTo(configuration.getLatticeBuilderType());
        assertThat(result.getWordConverterType()).isEqualTo(configuration.getWordConverterType());
        assertThat(result.getCostCalculatorType()).isEqualTo(configuration.getCostCalculatorType());
        assertThat(result.getCharacterRepositoryType()).isEqualTo(configuration.getCharacterRepositoryType());
        assertThat(result.getMaximalContextSize()).isEqualTo(configuration.getMaximalContextSize());

        Path file = Files.createTempFile("transformation-engine", "lattice");
        try {
            Serializer<LatticeTransformationEngineConfiguration, LatticeTransformationEngineConfigurationMessage> serializer = new Serializer<>(this.converter, configuration);
            serializer.serialize(configuration, file);
            LatticeTransformationEngineConfigurationMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
        }
    }

}
