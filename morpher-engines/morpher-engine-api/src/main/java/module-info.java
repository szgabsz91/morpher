@SuppressWarnings("module")
module com.github.szgabsz91.morpher.engines.api {

    requires transitive com.github.szgabsz91.morpher.analyzeragents.api;
    requires transitive com.github.szgabsz91.morpher.core;

    requires protobuf.java;

    exports com.github.szgabsz91.morpher.engines.api;
    exports com.github.szgabsz91.morpher.engines.api.model;

}
