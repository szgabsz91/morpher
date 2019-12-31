package com.github.szgabsz91.morpher.languagehandlers.hunmorph.converters;

import com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.HunmorphLanguageHandler;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.markov.FullMarkovModel;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.protocolbuffers.HunmorphLanguageHandlerMessage;
import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class HunmorphLanguageHandlerConverterTest {

    private HunmorphLanguageHandlerConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new HunmorphLanguageHandlerConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParse() throws IOException {
        HunmorphLanguageHandler languageHandler = new HunmorphLanguageHandler();
        languageHandler.setMarkovModel(new FullMarkovModel());
        languageHandler.analyze(FrequencyAwareWord.of("tollat"));

        HunmorphLanguageHandlerMessage message = this.converter.convert(languageHandler);
        assertThat(message.getAnnotationTokenizerResultMapCount()).isEqualTo(1);
        assertThat(message.getMarkovModel().getRoutesCount()).isEqualTo(1);
        assertThat(message.getLemmaMapCount()).isEqualTo(1);

        HunmorphLanguageHandler result = this.converter.convertBack(message);
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

        Path file = Files.createTempFile("language-handler", "hunmorph");
        try {
            Serializer<HunmorphLanguageHandler, HunmorphLanguageHandlerMessage> serializer = new Serializer<>(this.converter, result);
            serializer.serialize(languageHandler, file);
            HunmorphLanguageHandlerMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
            languageHandler.close();
            result.close();
        }
    }

}
