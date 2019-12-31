package com.github.szgabsz91.morpher.transformationengines.lattice.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WordConverterTypeTest {

    @Test
    public void testValueOf() {
        assertThat(WordConverterType.valueOf("IDENTITY")).isEqualTo(WordConverterType.IDENTITY);
        assertThat(WordConverterType.valueOf("DOUBLE_CONSONANT")).isEqualTo(WordConverterType.DOUBLE_CONSONANT);
    }

}
