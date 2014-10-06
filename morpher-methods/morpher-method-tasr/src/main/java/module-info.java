module com.github.szgabsz91.morpher.methods.tasr {

    requires com.github.szgabsz91.morpher.core;
    requires com.github.szgabsz91.morpher.methods.api;

    requires protobuf.java;
    requires slf4j.api;

    exports com.github.szgabsz91.morpher.methods.tasr;
    exports com.github.szgabsz91.morpher.methods.tasr.config;
    exports com.github.szgabsz91.morpher.methods.tasr.protocolbuffers;

    provides com.github.szgabsz91.morpher.methods.api.factories.IAbstractMethodFactory with com.github.szgabsz91.morpher.methods.tasr.impl.method.TASRAbstractMethodFactory;

}
