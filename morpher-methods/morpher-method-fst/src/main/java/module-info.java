@SuppressWarnings("module")
module com.github.szgabsz91.morpher.methods.fst {

    requires transitive com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.methods.api;

    requires lucene.core;
    requires org.slf4j;
    requires protobuf.java;

    exports com.github.szgabsz91.morpher.methods.fst;
    exports com.github.szgabsz91.morpher.methods.fst.config;
    exports com.github.szgabsz91.morpher.methods.fst.protocolbuffers;

    provides com.github.szgabsz91.morpher.methods.api.factories.IAbstractMethodFactory with com.github.szgabsz91.morpher.methods.fst.impl.method.FSTAbstractMethodFactory;

}
