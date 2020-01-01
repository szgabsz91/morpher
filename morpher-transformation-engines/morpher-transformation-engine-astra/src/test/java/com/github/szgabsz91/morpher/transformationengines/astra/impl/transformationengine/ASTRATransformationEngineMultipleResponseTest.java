package com.github.szgabsz91.morpher.transformationengines.astra.impl.transformationengine;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.transformationengines.api.model.TransformationEngineResponse;
import com.github.szgabsz91.morpher.transformationengines.api.model.ProbabilisticWord;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.astra.config.ASTRATransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.astra.config.SearcherType;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ASTRATransformationEngineMultipleResponseTest {

    @Test
    public void test() {
        // Creation
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .maximumNumberOfResponses(2)
                .build();
        ASTRATransformationEngine astraTransformationEngine = new ASTRATransformationEngine(false, AffixType.of("AFF"), configuration);

        // Training
        Set<FrequencyAwareWordPair> wordPairs = Set.of(
                FrequencyAwareWordPair.of("abc", "abcd"),
                FrequencyAwareWordPair.of("abc", "abce")
        );
        TrainingSet trainingSet = TrainingSet.of(wordPairs);
        astraTransformationEngine.learn(trainingSet);

        // Verification
        Word input = Word.of("abc");
        Optional<TransformationEngineResponse> optionalTransformationEngineResponse = astraTransformationEngine.transform(input);
        assertThat(optionalTransformationEngineResponse).isPresent();
        TransformationEngineResponse transformationEngineResponse = optionalTransformationEngineResponse.get();
        assertThat(transformationEngineResponse.getResults()).containsExactlyInAnyOrder(
                ProbabilisticWord.of(Word.of("abcd"), 1.0),
                ProbabilisticWord.of(Word.of("abce"), 1.0)
        );
    }

}
