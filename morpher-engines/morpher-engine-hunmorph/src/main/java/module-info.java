@SuppressWarnings("module")
module com.github.szgabsz91.morpher.engines.hunmorph {

    requires transitive com.github.szgabsz91.morpher.analyzeragents.api;
    requires com.github.szgabsz91.morpher.analyzeragents.hunmorph;
    requires transitive com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.engines.api;

    requires protobuf.java;

    exports com.github.szgabsz91.morpher.engines.hunmorph;

    uses com.github.szgabsz91.morpher.analyzeragents.api.IAnalyzerAgent;

}
