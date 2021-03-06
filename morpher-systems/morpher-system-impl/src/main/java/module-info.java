@SuppressWarnings("module")
module com.github.szgabsz91.morpher.systems.impl {

    requires com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.engines.api;
    requires com.github.szgabsz91.morpher.languagehandlers.api;
    requires transitive com.github.szgabsz91.morpher.systems.api;

    requires org.slf4j;
    requires com.google.protobuf;

    exports com.github.szgabsz91.morpher.systems.impl;

}
