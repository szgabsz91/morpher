module com.github.szgabsz91.morpher.engines.hunmorph {

    requires com.github.szgabsz91.morpher.analyzeragents.api;
    requires com.github.szgabsz91.morpher.analyzeragents.hunmorph;
    requires com.github.szgabsz91.morpher.core;
    requires com.github.szgabsz91.morpher.engines.api;

    requires protobuf.java;

    exports com.github.szgabsz91.morpher.engines.hunmorph;

    uses com.github.szgabsz91.morpher.analyzeragents.api.IAnalyzerAgent;

}
