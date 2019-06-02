package com.github.szgabsz91.morpher.analyzeragents.hunmorph.converters;

import com.github.szgabsz91.morpher.analyzeragents.hunmorph.impl.HunmorphAnalyzerAgent;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.impl.markov.FullMarkovModel;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.protocolbuffers.HunmorphAnalyzerAgentMessage;
import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class HunmorphAnalyzerAgentConverterTest {

    private HunmorphAnalyzerAgentConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new HunmorphAnalyzerAgentConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParse() throws IOException {
        HunmorphAnalyzerAgent agent = new HunmorphAnalyzerAgent();
        agent.setMarkovModel(new FullMarkovModel());
        agent.analyze(FrequencyAwareWord.of("tollat"));

        HunmorphAnalyzerAgentMessage message = this.converter.convert(agent);
        assertThat(message.getAnnotationTokenizerResultMapCount()).isEqualTo(1);
        assertThat(message.getMarkovModel().getRoutesCount()).isEqualTo(1);
        assertThat(message.getLemmaMapCount()).isEqualTo(1);

        HunmorphAnalyzerAgent result = this.converter.convertBack(message);
        assertThat(result.getAnnotationTokenizerResultMap()).hasSize(1);
        assertThat(result.getMarkovModel()).isNotNull();
        assertThat(result.getMarkovModel()).isInstanceOf(FullMarkovModel.class);
        assertThat(result.getReversedMarkovModel()).isNotNull();
        assertThat(result.getReversedMarkovModel()).isInstanceOf(FullMarkovModel.class);
        FullMarkovModel fullMarkovModel = (FullMarkovModel) result.getMarkovModel();
        assertThat(fullMarkovModel.getRoutes()).hasSize(1);
        FullMarkovModel reversedFullMarkovModel = (FullMarkovModel) result.getReversedMarkovModel();
        assertThat(reversedFullMarkovModel.getRoutes()).hasSize(1);
        assertThat(result.getLemmaMap()).hasSize(1);

        Path file = Files.createTempFile("agent", "hunmorph");
        try {
            Serializer<HunmorphAnalyzerAgent, HunmorphAnalyzerAgentMessage> serializer = new Serializer<>(this.converter, result);
            serializer.serialize(agent, file);
            HunmorphAnalyzerAgentMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
            agent.close();
            result.close();
        }
    }

}
