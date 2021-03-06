@SuppressWarnings("module")
module com.github.szgabsz91.morpher.engines.impl {

    requires com.github.szgabsz91.morpher.languagehandlers.api;
    requires transitive com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.engines.api;
    requires transitive com.github.szgabsz91.morpher.transformationengines.api;

    requires org.slf4j;
    requires com.google.protobuf;

    exports com.github.szgabsz91.morpher.engines.impl;
    exports com.github.szgabsz91.morpher.engines.impl.transformationengineholders;
    exports com.github.szgabsz91.morpher.engines.impl.transformationengineholderfactories;
    exports com.github.szgabsz91.morpher.engines.impl.impl.probability;

}
