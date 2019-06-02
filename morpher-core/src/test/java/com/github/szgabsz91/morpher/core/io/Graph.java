package com.github.szgabsz91.morpher.core.io;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;

public class Graph {

    private final Node node;

    public Graph(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public static class GraphMessage extends GeneratedMessageV3 {

        @Override
        protected FieldAccessorTable internalGetFieldAccessorTable() {
            return null;
        }

        @Override
        protected Message.Builder newBuilderForType(BuilderParent parent) {
            return null;
        }

        @Override
        public Message.Builder newBuilderForType() {
            return null;
        }

        @Override
        public Message.Builder toBuilder() {
            return null;
        }

        @Override
        public Message getDefaultInstanceForType() {
            return null;
        }

    }

}
