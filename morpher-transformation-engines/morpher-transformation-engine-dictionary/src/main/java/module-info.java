@SuppressWarnings("module")
module com.github.szgabsz91.morpher.transformationengines.dictionary {

    requires transitive com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.transformationengines.api;

    requires com.google.protobuf;
    requires lombok;

    exports com.github.szgabsz91.morpher.transformationengines.dictionary;
    exports com.github.szgabsz91.morpher.transformationengines.dictionary.config;
    exports com.github.szgabsz91.morpher.transformationengines.dictionary.protocolbuffers;

    provides com.github.szgabsz91.morpher.transformationengines.api.factories.IAbstractTransformationEngineFactory with com.github.szgabsz91.morpher.transformationengines.dictionary.impl.transformationengine.DictionaryAbstractTransformationEngineFactory;

}
