module com.github.szgabsz91.morpher.core {

    requires protobuf.java;
    requires slf4j.api;
    requires zip4j;
    requires org.junit.jupiter.api;

    exports com.github.szgabsz91.morpher.core.io;
    exports com.github.szgabsz91.morpher.core.model;
    exports com.github.szgabsz91.morpher.core.protocolbuffers;
    exports com.github.szgabsz91.morpher.core.services;
    exports com.github.szgabsz91.morpher.core.utils;

}
