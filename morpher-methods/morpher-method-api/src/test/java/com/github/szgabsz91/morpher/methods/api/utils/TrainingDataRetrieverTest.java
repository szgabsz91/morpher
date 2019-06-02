package com.github.szgabsz91.morpher.methods.api.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TrainingDataRetrieverTest {

    @Test
    public void testConstructor() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<TrainingDataRetriever> constructor = TrainingDataRetriever.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        try {
            constructor.newInstance();
        }
        finally {
            constructor.setAccessible(false);
        }
    }

    @Test
    public void testGetTrainingDataFile() {
        Path file = TrainingDataRetriever.getTrainingDataFile("CAS(ACC).csv");
        assertThat(file).isNotNull();
        assertThat(Files.exists(file))
                .withFailMessage("The CAS(ACC).csv file is not found")
                .isTrue();
    }

    @Test
    public void testGetTrainingDataFileWithNonExistentFile() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> TrainingDataRetriever.getTrainingDataFile("UNKNOWN"));
        assertThat(exception).hasMessage("Cannot find the specified file");
    }

}
