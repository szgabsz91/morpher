@SuppressWarnings("module")
module com.github.szgabsz91.morpher.engines.hunmorph {

    requires transitive com.github.szgabsz91.morpher.core;
    requires transitive com.github.szgabsz91.morpher.languagehandlers.api;
    requires com.github.szgabsz91.morpher.languagehandlers.hunmorph;
    requires transitive com.github.szgabsz91.morpher.engines.api;

    requires com.google.protobuf;

    exports com.github.szgabsz91.morpher.engines.hunmorph;

    uses com.github.szgabsz91.morpher.languagehandlers.api.ILanguageHandler;

}
