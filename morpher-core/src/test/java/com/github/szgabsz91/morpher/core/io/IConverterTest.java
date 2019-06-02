package com.github.szgabsz91.morpher.core.io;

import com.google.protobuf.GeneratedMessageV3;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class IConverterTest {

    @Test
    public void testIsConvertBackSupported() {
        IConverter<?, ?> converter = new CustomConverter();
        boolean result = converter.isConvertBackSupported();
        assertThat(result).isTrue();
    }

    private static class CustomConverter implements IConverter<Object, GeneratedMessageV3> {

        @Override
        public GeneratedMessageV3 convert(Object o) {
            return null;
        }

        @Override
        public Object convertBack(GeneratedMessageV3 generatedMessageV3) {
            return null;
        }

        @Override
        public GeneratedMessageV3 parse(Path file) {
            return null;
        }

    }

}
