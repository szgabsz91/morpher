package com.github.szgabsz91.morpher.engines.api.model;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class InflectionInputTest {

    @Test
    public void testConstructorAndGetters() {
        Word input = Word.of("input");
        Set<AffixType> affixTypes = Set.of(AffixType.of("AFF1"), AffixType.of("AFF2"));
        InflectionInput inflectionInput = new InflectionInput(input, affixTypes);
        assertThat(inflectionInput.getInput()).isEqualTo(input);
        assertThat(inflectionInput.getAffixTypes()).isEqualTo(affixTypes);
    }

}
