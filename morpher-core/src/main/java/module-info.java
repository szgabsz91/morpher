@SuppressWarnings("module")
module com.github.szgabsz91.morpher.core {

    requires lombok;
    requires org.slf4j;
    requires transitive protobuf.java;
    requires zip4j;

    exports com.github.szgabsz91.morpher.core.io;
    exports com.github.szgabsz91.morpher.core.model;
    exports com.github.szgabsz91.morpher.core.protocolbuffers;
    exports com.github.szgabsz91.morpher.core.services;
    exports com.github.szgabsz91.morpher.core.utils;

}
