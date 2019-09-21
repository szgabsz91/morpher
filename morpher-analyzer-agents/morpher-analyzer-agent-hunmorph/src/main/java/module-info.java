module com.github.szgabsz91.morpher.analyzeragents.hunmorph {

    requires com.github.szgabsz91.morpher.analyzeragents.api;
    requires com.github.szgabsz91.morpher.core;

    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires protobuf.java;

    exports com.github.szgabsz91.morpher.analyzeragents.hunmorph;
    exports com.github.szgabsz91.morpher.analyzeragents.hunmorph.impl to com.github.szgabsz91.morpher.engines.hunmorph;
    exports com.github.szgabsz91.morpher.analyzeragents.hunmorph.protocolbuffers;

    provides com.github.szgabsz91.morpher.analyzeragents.api.IAnalyzerAgent with com.github.szgabsz91.morpher.analyzeragents.hunmorph.impl.HunmorphAnalyzerAgent;

}
