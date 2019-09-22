package com.github.szgabsz91.morpher.core.io;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;

import java.nio.file.Path;

public class CustomSerializerDeserializerWithFalseResult implements ICustomSerializer, ICustomDeserializer {

    @Override
    public boolean serialize(Path file) {
        return false;
    }

    @Override
    public boolean deserialize(Path file) {
        return false;
    }

    @SuppressWarnings("serial")
    public static class Message extends GeneratedMessageV3 {

        @Override
        protected FieldAccessorTable internalGetFieldAccessorTable() {
            return null;
        }

        @Override
        protected Message.Builder<?> newBuilderForType(BuilderParent parent) {
            return null;
        }

        @Override
        public Message.Builder<?> newBuilderForType() {
            return null;
        }

        @Override
        public Message.Builder<?> toBuilder() {
            return null;
        }

        @Override
        public Message getDefaultInstanceForType() {
            return null;
        }

    }

}
