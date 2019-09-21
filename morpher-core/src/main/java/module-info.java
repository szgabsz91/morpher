module com.github.szgabsz91.morpher.core {

    requires org.slf4j;
    requires protobuf.java;
    requires zip4j;
    requires org.junit.jupiter.api;

    exports com.github.szgabsz91.morpher.core.io;
    exports com.github.szgabsz91.morpher.core.model;
    exports com.github.szgabsz91.morpher.core.protocolbuffers;
    exports com.github.szgabsz91.morpher.core.services;
    exports com.github.szgabsz91.morpher.core.utils;

}
