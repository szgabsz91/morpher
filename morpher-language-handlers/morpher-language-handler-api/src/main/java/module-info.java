@SuppressWarnings("module")
module com.github.szgabsz91.morpher.languagehandlers.api {

    requires transitive com.github.szgabsz91.morpher.core;

    requires lombok;
    requires protobuf.java;

    exports com.github.szgabsz91.morpher.languagehandlers.api;
    exports com.github.szgabsz91.morpher.languagehandlers.api.model;

}