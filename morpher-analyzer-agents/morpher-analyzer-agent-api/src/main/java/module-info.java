@SuppressWarnings("module")
module com.github.szgabsz91.morpher.analyzeragents.api {

    requires transitive com.github.szgabsz91.morpher.core;

    requires protobuf.java;

    exports com.github.szgabsz91.morpher.analyzeragents.api;
    exports com.github.szgabsz91.morpher.analyzeragents.api.model;

}