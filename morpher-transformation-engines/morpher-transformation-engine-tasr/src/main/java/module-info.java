@SuppressWarnings("module")
module com.github.szgabsz91.morpher.transformationengines.tasr {

    requires com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.transformationengines.api;

    requires org.slf4j;
    requires com.google.protobuf;

    exports com.github.szgabsz91.morpher.transformationengines.tasr;
    exports com.github.szgabsz91.morpher.transformationengines.tasr.config;
    exports com.github.szgabsz91.morpher.transformationengines.tasr.protocolbuffers;

    provides com.github.szgabsz91.morpher.transformationengines.api.factories.IAbstractTransformationEngineFactory with com.github.szgabsz91.morpher.transformationengines.tasr.impl.transformationengine.TASRAbstractTransformationEngineFactory;

}
