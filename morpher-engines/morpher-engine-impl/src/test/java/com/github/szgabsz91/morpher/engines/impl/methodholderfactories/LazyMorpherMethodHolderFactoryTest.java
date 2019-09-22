package com.github.szgabsz91.morpher.engines.impl.methodholderfactories;

import com.github.szgabsz91.morpher.engines.impl.methodholders.IMorpherMethodHolder;
import com.github.szgabsz91.morpher.engines.impl.impl.methodholders.LazyMorpherMethodHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LazyMorpherMethodHolderFactoryTest {

    private IMorpherMethodHolderFactory factory;

    @BeforeEach
    public void setUp() {
        this.factory = new LazyMorpherMethodHolderFactory();
    }

    @Test
    public void testCreate() {
        IMorpherMethodHolder holder = this.factory.create(null, null, null);
        assertThat(holder).isInstanceOf(LazyMorpherMethodHolder.class);
    }

}
