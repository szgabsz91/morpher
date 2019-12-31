@SuppressWarnings("module")
module com.github.szgabsz91.morpher.transformationengines.lattice {

    requires com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.transformationengines.api;

    requires org.apache.commons.collections4;
    requires org.slf4j;
    requires protobuf.java;

    exports com.github.szgabsz91.morpher.transformationengines.lattice;
    exports com.github.szgabsz91.morpher.transformationengines.lattice.config;
    exports com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers;

    provides com.github.szgabsz91.morpher.transformationengines.api.factories.IAbstractTransformationEngineFactory with com.github.szgabsz91.morpher.transformationengines.lattice.impl.transformationengine.LatticeAbstractTransformationEngineFactory;

}
