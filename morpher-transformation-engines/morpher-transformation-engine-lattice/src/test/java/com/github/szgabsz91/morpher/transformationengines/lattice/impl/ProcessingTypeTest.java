package com.github.szgabsz91.morpher.transformationengines.lattice.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessingTypeTest {

    @Test
    public void testValueOf() {
        assertProcessingType(ProcessingType.PARENT_LIST_SEARCH);
        assertProcessingType(ProcessingType.CHILD_LIST_SEARCH);
        assertProcessingType(ProcessingType.FREQUENCY_UPDATE);
        assertProcessingType(ProcessingType.GENERAL_PROCESSING);
    }

    private static void assertProcessingType(ProcessingType processingType) {
        assertThat(ProcessingType.valueOf(processingType.toString())).isEqualTo(processingType);
    }

}
