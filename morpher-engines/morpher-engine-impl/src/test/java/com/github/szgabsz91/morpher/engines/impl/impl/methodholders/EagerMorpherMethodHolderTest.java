package com.github.szgabsz91.morpher.engines.impl.impl.methodholders;

import com.github.szgabsz91.morpher.engines.impl.methodholders.IMorpherMethodHolder;
import com.github.szgabsz91.morpher.methods.api.IMorpherMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class EagerMorpherMethodHolderTest {

    private IMorpherMethod<?> morpherMethod;
    private IMorpherMethodHolder holder;

    @BeforeEach
    public void setUp() {
        this.morpherMethod = mock(IMorpherMethod.class);
        this.holder = new EagerMorpherMethodHolder(morpherMethod);
    }

    @Test
    public void testClose() {
        this.holder.close();
    }

    @Test
    public void testGet() {
        IMorpherMethod<?> result = this.holder.get();
        assertThat(result).isSameAs(this.morpherMethod);
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
