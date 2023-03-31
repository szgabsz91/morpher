@SuppressWarnings("module")
module com.github.szgabsz91.morpher.transformationengines.astra {

    requires com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.transformationengines.api;

    requires combinatoricslib3;
    requires lombok;
    requires org.apache.commons.collections4;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires protobuf.java;

    exports com.github.szgabsz91.morpher.transformationengines.astra;
    exports com.github.szgabsz91.morpher.transformationengines.astra.config;
    exports com.github.szgabsz91.morpher.transformationengines.astra.protocolbuffers;

    provides com.github.szgabsz91.morpher.transformationengines.api.factories.IAbstractTransformationEngineFactory with com.github.szgabsz91.morpher.transformationengines.astra.impl.transformationengine.ASTRAAbstractTransformationEngineFactory;

}
