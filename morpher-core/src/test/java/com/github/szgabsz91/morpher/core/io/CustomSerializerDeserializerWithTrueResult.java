package com.github.szgabsz91.morpher.core.io;

import com.google.protobuf.GeneratedMessageV3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CustomSerializerDeserializerWithTrueResult implements ICustomSerializer, ICustomDeserializer {

    private String id;

    public CustomSerializerDeserializerWithTrueResult(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean serialize(Path file) {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(this.id);
            writer.newLine();
        }
        catch (IOException e) {
            throw new IllegalStateException("Cannot write file " + file, e);
        }
        return true;
    }

    @Override
    public boolean deserialize(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            this.id = reader.readLine();
        }
        catch (IOException e) {
            throw new IllegalStateException("Cannot read file " + file, e);
        }
        return true;
    }

    @SuppressWarnings("serial")
    public static class Message extends GeneratedMessageV3 {

        @Override
        protected FieldAccessorTable internalGetFieldAccessorTable() {
            return null;
        }

        @Override
        protected CustomSerializerDeserializerWithFalseResult.Message.Builder<?> newBuilderForType(BuilderParent parent) {
            return null;
        }

        @Override
        public CustomSerializerDeserializerWithFalseResult.Message.Builder<?> newBuilderForType() {
            return null;
        }

        @Override
        public CustomSerializerDeserializerWithFalseResult.Message.Builder<?> toBuilder() {
            return null;
        }

        @Override
        public CustomSerializerDeserializerWithFalseResult.Message getDefaultInstanceForType() {
            return null;
        }

    }

}
