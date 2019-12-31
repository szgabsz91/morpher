package com.github.szgabsz91.morpher.engines.impl.transformationengineholderfactories;

import com.github.szgabsz91.morpher.engines.impl.impl.transformationengineholders.LazyTransformationEngineHolder;
import com.github.szgabsz91.morpher.engines.impl.transformationengineholders.ITransformationEngineHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LazyTransformationEngineHolderFactoryTest {

    private ITransformationEngineHolderFactory factory;

    @BeforeEach
    public void setUp() {
        this.factory = new LazyTransformationEngineHolderFactory();
    }

    @Test
    public void testCreate() {
        ITransformationEngineHolder holder = this.factory.create(null, null, null);
        assertThat(holder).isInstanceOf(LazyTransformationEngineHolder.class);
    }

}
