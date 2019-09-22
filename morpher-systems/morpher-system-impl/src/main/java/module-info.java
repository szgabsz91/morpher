@SuppressWarnings("module")
module com.github.szgabsz91.morpher.systems.impl {

    requires com.github.szgabsz91.morpher.analyzeragents.api;
    requires com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.engines.api;
    requires transitive com.github.szgabsz91.morpher.systems.api;

    requires org.slf4j;
    requires protobuf.java;

    exports com.github.szgabsz91.morpher.systems.impl;

}
