@SuppressWarnings("module")
module com.github.szgabsz91.morpher.engines.impl {

    requires com.github.szgabsz91.morpher.analyzeragents.api;
    requires transitive com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.engines.api;
    requires transitive com.github.szgabsz91.morpher.methods.api;

    requires org.slf4j;
    requires protobuf.java;

    exports com.github.szgabsz91.morpher.engines.impl;
    exports com.github.szgabsz91.morpher.engines.impl.methodholders;
    exports com.github.szgabsz91.morpher.engines.impl.methodholderfactories;
    exports com.github.szgabsz91.morpher.engines.impl.impl.probability;

}
