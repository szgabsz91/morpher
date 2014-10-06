module com.github.szgabsz91.morpher.methods.lattice {

    requires com.github.szgabsz91.morpher.core;
    requires com.github.szgabsz91.morpher.methods.api;

    requires org.apache.commons.collections4;
    requires protobuf.java;
    requires slf4j.api;

    exports com.github.szgabsz91.morpher.methods.lattice;
    exports com.github.szgabsz91.morpher.methods.lattice.config;
    exports com.github.szgabsz91.morpher.methods.lattice.protocolbuffers;

    provides com.github.szgabsz91.morpher.methods.api.factories.IAbstractMethodFactory with com.github.szgabsz91.morpher.methods.lattice.impl.method.LatticeAbstractMethodFactory;

}
