@SuppressWarnings("module")
module com.github.szgabsz91.morpher.languagehandlers.api {

    requires transitive com.github.szgabsz91.morpher.core;

    requires com.google.protobuf;

    exports com.github.szgabsz91.morpher.languagehandlers.api;
    exports com.github.szgabsz91.morpher.languagehandlers.api.model;

}