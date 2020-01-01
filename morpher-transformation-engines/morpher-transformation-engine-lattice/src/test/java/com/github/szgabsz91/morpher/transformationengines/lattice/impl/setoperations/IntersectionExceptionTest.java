package com.github.szgabsz91.morpher.transformationengines.lattice.impl.setoperations;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntersectionExceptionTest {

    @Test
    public void testConstructor() {
        String message = "message";
        IntersectionException exception = new IntersectionException(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

}
