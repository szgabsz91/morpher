@SuppressWarnings("module")
module com.github.szgabsz91.morpher.languagehandlers.hunmorph {

    requires com.github.szgabsz91.morpher.languagehandlers.api;
    requires com.github.szgabsz91.morpher.core;

    requires org.slf4j;
    requires transitive com.google.protobuf;

    exports com.github.szgabsz91.morpher.languagehandlers.hunmorph;
    exports com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl to com.github.szgabsz91.morpher.engines.hunmorph;
    exports com.github.szgabsz91.morpher.languagehandlers.hunmorph.protocolbuffers;

    provides com.github.szgabsz91.morpher.languagehandlers.api.ILanguageHandler with com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.HunmorphLanguageHandler;

}
