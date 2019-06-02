module com.github.szgabsz91.morpher.methods.dictionary {

    requires com.github.szgabsz91.morpher.core;
    requires com.github.szgabsz91.morpher.methods.api;

    requires org.apache.commons.lang3;
    requires protobuf.java;

    exports com.github.szgabsz91.morpher.methods.dictionary;
    exports com.github.szgabsz91.morpher.methods.dictionary.config;
    exports com.github.szgabsz91.morpher.methods.dictionary.protocolbuffers;

    provides com.github.szgabsz91.morpher.methods.api.factories.IAbstractMethodFactory with com.github.szgabsz91.morpher.methods.dictionary.impl.method.DictionaryAbstractMethodFactory;

}
