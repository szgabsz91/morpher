package com.github.szgabsz91.morpher.analyzeragents.hunmorph.converters;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.impl.markov.FullMarkovModel;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.protocolbuffers.MarkovModelMessage;
import com.github.szgabsz91.morpher.analyzeragents.hunmorph.protocolbuffers.MarkovModelRouteMessage;
import com.github.szgabsz91.morpher.core.io.Serializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public class FullMarkovModelConverterTest {

    private FullMarkovModelConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new FullMarkovModelConverter();
    }

    @Test
    public void testConvertAndConvertBackAndParse() throws IOException {
        FullMarkovModel markovModel = new FullMarkovModel();
        markovModel.add(List.of(AffixType.of("AFF1"), AffixType.of("AFF2")), 2L);
        markovModel.add(List.of(AffixType.of("AFF3"), AffixType.of("AFF4")), 3L);

        MarkovModelMessage message = this.converter.convert(markovModel);
        assertThat(message.getRoutesList()).containsExactly(
                MarkovModelRouteMessage.newBuilder()
                        .addAllAffixTypes(List.of("AFF1", "AFF2"))
                        .setRelativeFrequency(2L)
                        .build(),
                MarkovModelRouteMessage.newBuilder()
                        .addAllAffixTypes(List.of("AFF3", "AFF4"))
                        .setRelativeFrequency(3L)
                        .build()
        );

        FullMarkovModel result = this.converter.convertBack(message);
        Map<List<AffixType>, Long> routes = result.getRoutes().entrySet()
                .stream()
                .map(entry -> {
                    List<AffixType> affixTypes = entry.getKey()
                            .stream()
                            .map(FullMarkovModel.Node::getAffixType)
                            .collect(toList());
                    long frequency = entry.getValue();
                    return Map.entry(affixTypes, frequency);
                })
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertThat(routes).containsExactly(
                Map.entry(List.of(AffixType.of("AFF1"), AffixType.of("AFF2")), 2L),
                Map.entry(List.of(AffixType.of("AFF3"), AffixType.of("AFF4")), 3L)
        );

        Path file = Files.createTempFile("agent", "markovModel");
        try {
            Serializer<FullMarkovModel, MarkovModelMessage> serializer = new Serializer<>(this.converter, markovModel);
            serializer.serialize(markovModel, file);
            MarkovModelMessage resultingMessage = this.converter.parse(file);
            assertThat(resultingMessage).isEqualTo(message);
        }
        finally {
            Files.delete(file);
        }
    }
    
}
