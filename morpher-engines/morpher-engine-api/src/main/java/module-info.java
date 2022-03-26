@SuppressWarnings("module")
module com.github.szgabsz91.morpher.engines.api {

    requires transitive com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.languagehandlers.api;

    requires com.google.protobuf;
    requires lombok;

    exports com.github.szgabsz91.morpher.engines.api;
    exports com.github.szgabsz91.morpher.engines.api.model;

}
