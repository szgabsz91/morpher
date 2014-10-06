package com.github.szgabsz91.morpher.methods.lattice.impl.builders;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IndexWrapperTest {

    @Test
    public void testValue() {
        IndexWrapper indexWrapper = new IndexWrapper();
        assertThat(indexWrapper.value()).isEqualTo(0L);
        indexWrapper.increment();
        assertThat(indexWrapper.value()).isEqualTo(1L);
    }

    @Test
    public void testIncrement() {
        IndexWrapper indexWrapper = new IndexWrapper();
        assertThat(indexWrapper.increment()).isEqualTo(1L);
        assertThat(indexWrapper.increment()).isEqualTo(2L);
        assertThat(indexWrapper.increment()).isEqualTo(3L);
        assertThat(indexWrapper.increment()).isEqualTo(4L);
        assertThat(indexWrapper.increment()).isEqualTo(5L);
    }

}
