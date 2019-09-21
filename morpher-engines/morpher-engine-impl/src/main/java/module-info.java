module com.github.szgabsz91.morpher.engines.impl {

    requires com.github.szgabsz91.morpher.analyzeragents.api;
    requires com.github.szgabsz91.morpher.core;
    requires com.github.szgabsz91.morpher.engines.api;
    requires com.github.szgabsz91.morpher.methods.api;

    requires org.slf4j;
    requires protobuf.java;

    exports com.github.szgabsz91.morpher.engines.impl;
    exports com.github.szgabsz91.morpher.engines.impl.methodholderfactories;
    exports com.github.szgabsz91.morpher.engines.impl.impl.probability;
    exports com.github.szgabsz91.morpher.engines.impl.protocolbuffers to protobuf.java;

}
