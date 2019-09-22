@SuppressWarnings("module")
module com.github.szgabsz91.morpher.methods.lattice {

    requires com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.methods.api;

    requires org.apache.commons.collections4;
    requires org.slf4j;
    requires protobuf.java;

    exports com.github.szgabsz91.morpher.methods.lattice;
    exports com.github.szgabsz91.morpher.methods.lattice.config;
    exports com.github.szgabsz91.morpher.methods.lattice.protocolbuffers;

    provides com.github.szgabsz91.morpher.methods.api.factories.IAbstractMethodFactory with com.github.szgabsz91.morpher.methods.lattice.impl.method.LatticeAbstractMethodFactory;

}
