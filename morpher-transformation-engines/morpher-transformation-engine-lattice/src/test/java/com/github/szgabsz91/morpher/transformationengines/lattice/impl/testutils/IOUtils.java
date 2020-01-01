package com.github.szgabsz91.morpher.transformationengines.lattice.impl.testutils;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.google.protobuf.GeneratedMessageV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public final class IOUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);

    private IOUtils() {

    }

    public static <TSource, TTarget extends GeneratedMessageV3> TSource serializeAndDeserialize(
            TSource object,
            Serializer<TSource, TTarget> serializer) throws IOException {
        Path file = getRandomFile();
        serializer.serialize(object, file);
        long fileSize = Files.size(file);
        LOGGER.info("File size: {} bytes", fileSize);
        TSource result = serializer.deserialize(file);
        Files.delete(file);
        return result;
    }

    public static Path getRandomFile() {
        UUID uuid = UUID.randomUUID();
        return Paths.get("build/" + uuid + ".pb");
    }

}
