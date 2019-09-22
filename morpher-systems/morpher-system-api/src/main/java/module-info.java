@SuppressWarnings("module")
module com.github.szgabsz91.morpher.systems.api {

    requires transitive com.github.szgabsz91.morpher.analyzeragents.api;
    requires transitive com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.engines.api;

    exports com.github.szgabsz91.morpher.systems.api;
    exports com.github.szgabsz91.morpher.systems.api.model;

}
