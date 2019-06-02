package com.github.szgabsz91.morpher.methods.astra.impl.method;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.model.ProbabilisticWord;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.github.szgabsz91.morpher.methods.astra.config.ASTRAMethodConfiguration;
import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ASTRAMethodMultipleResponseTest {

    @Test
    public void test() {
        // Creation
        ASTRAMethodConfiguration configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.PREFIX_TREE)
                .maximumNumberOfResponses(2)
                .build();
        ASTRAMethod astraMethod = new ASTRAMethod(false, AffixType.of("AFF"), configuration);

        // Training
        Set<FrequencyAwareWordPair> wordPairs = Set.of(
                FrequencyAwareWordPair.of("abc", "abcd"),
                FrequencyAwareWordPair.of("abc", "abce")
        );
        TrainingSet trainingSet = TrainingSet.of(wordPairs);
        astraMethod.learn(trainingSet);

        // Verification
        Word input = Word.of("abc");
        Optional<MethodResponse> optionalMethodResponse = astraMethod.inflect(input);
        assertThat(optionalMethodResponse).isPresent();
        MethodResponse methodResponse = optionalMethodResponse.get();
        assertThat(methodResponse.getResults()).containsExactlyInAnyOrder(
                ProbabilisticWord.of(Word.of("abcd"), 1.0),
                ProbabilisticWord.of(Word.of("abce"), 1.0)
        );
    }

}
