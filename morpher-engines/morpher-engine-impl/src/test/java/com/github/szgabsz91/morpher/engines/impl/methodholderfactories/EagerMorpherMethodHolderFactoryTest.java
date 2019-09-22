package com.github.szgabsz91.morpher.engines.impl.methodholderfactories;

import com.github.szgabsz91.morpher.engines.impl.impl.methodholders.EagerMorpherMethodHolder;
import com.github.szgabsz91.morpher.engines.impl.methodholders.IMorpherMethodHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EagerMorpherMethodHolderFactoryTest {

    private IMorpherMethodHolderFactory factory;

    @BeforeEach
    public void setUp() {
        this.factory = new EagerMorpherMethodHolderFactory();
    }

    @Test
    public void testCreate() {
        IMorpherMethodHolder holder = this.factory.create(null, null, null);
        assertThat(holder).isInstanceOf(EagerMorpherMethodHolder.class);
    }

}
