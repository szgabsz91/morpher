package com.github.szgabsz91.morpher.engines.impl.impl.transformationengineholders;

import com.github.szgabsz91.morpher.engines.impl.transformationengineholders.ITransformationEngineHolder;
import com.github.szgabsz91.morpher.transformationengines.api.IBidirectionalTransformationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class EagerTransformationEngineHolderTest {

    private IBidirectionalTransformationEngine<?> transformationEngine;
    private ITransformationEngineHolder holder;

    @BeforeEach
    public void setUp() {
        this.transformationEngine = mock(IBidirectionalTransformationEngine.class);
        this.holder = new EagerTransformationEngineHolder(transformationEngine);
    }

    @Test
    public void testClose() {
        this.holder.close();
    }

    @Test
    public void testGet() {
        IBidirectionalTransformationEngine<?> result = this.holder.get();
        assertThat(result).isSameAs(this.transformationEngine);
    }

    @Test
    public void testSave() {
        this.holder.save(null);
    }

    @Test
    public void testClear() {
        this.holder.clear();
    }

}
