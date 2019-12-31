package com.github.szgabsz91.morpher.transformationengines.astra.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SearcherTypeTest {

    @Test
    public void testValueOf() {
        assertThat(SearcherType.valueOf("SEQUENTIAL")).isEqualTo(SearcherType.SEQUENTIAL);
        assertThat(SearcherType.valueOf("PARALLEL")).isEqualTo(SearcherType.PARALLEL);
        assertThat(SearcherType.valueOf("PREFIX_TREE")).isEqualTo(SearcherType.PREFIX_TREE);
    }

}
