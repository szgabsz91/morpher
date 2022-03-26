@SuppressWarnings("module")
module com.github.szgabsz91.morpher.transformationengines.fst {

    requires transitive com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.transformationengines.api;

    requires lombok;
    requires org.apache.lucene.core;
    requires org.slf4j;
    requires com.google.protobuf;

    exports com.github.szgabsz91.morpher.transformationengines.fst;
    exports com.github.szgabsz91.morpher.transformationengines.fst.config;
    exports com.github.szgabsz91.morpher.transformationengines.fst.protocolbuffers;

    provides com.github.szgabsz91.morpher.transformationengines.api.factories.IAbstractTransformationEngineFactory with com.github.szgabsz91.morpher.transformationengines.fst.impl.transformationengine.FSTAbstractTransformationEngineFactory;

}
