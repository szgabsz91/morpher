package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InflectionOrderedInputTest {

    @Test
    public void testConstructorAndGetters() {
        Word input = Word.of("input");
        List<AffixType> affixTypes = List.of(AffixType.of("AFF1"), AffixType.of("AFF2"));
        InflectionOrderedInput inflectionOrderedInput = new InflectionOrderedInput(input, affixTypes);
        assertThat(inflectionOrderedInput.getInput()).isEqualTo(input);
        assertThat(inflectionOrderedInput.getAffixTypes()).isEqualTo(affixTypes);
    }

}
