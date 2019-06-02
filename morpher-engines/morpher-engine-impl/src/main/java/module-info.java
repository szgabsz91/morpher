module com.github.szgabsz91.morpher.engines.impl {

    requires com.github.szgabsz91.morpher.analyzeragents.api;
    requires com.github.szgabsz91.morpher.core;
    requires com.github.szgabsz91.morpher.engines.api;
    requires com.github.szgabsz91.morpher.methods.api;

    requires protobuf.java;
    requires slf4j.api;

    exports com.github.szgabsz91.morpher.engines.impl;
    exports com.github.szgabsz91.morpher.engines.impl.methodholderfactories;
    exports com.github.szgabsz91.morpher.engines.impl.impl.probability;
    exports com.github.szgabsz91.morpher.engines.impl.protocolbuffers to protobuf.java;

}
