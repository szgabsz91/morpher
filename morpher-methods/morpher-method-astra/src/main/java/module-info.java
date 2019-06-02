module com.github.szgabsz91.morpher.methods.astra {

    requires com.github.szgabsz91.morpher.core;
    requires com.github.szgabsz91.morpher.methods.api;

    requires combinatoricslib3;
    requires org.apache.commons.collections4;
    requires org.apache.commons.lang3;
    requires protobuf.java;
    requires slf4j.api;

    exports com.github.szgabsz91.morpher.methods.astra;
    exports com.github.szgabsz91.morpher.methods.astra.config;
    exports com.github.szgabsz91.morpher.methods.astra.protocolbuffers;

    provides com.github.szgabsz91.morpher.methods.api.factories.IAbstractMethodFactory with com.github.szgabsz91.morpher.methods.astra.impl.method.ASTRAAbstractMethodFactory;

}
