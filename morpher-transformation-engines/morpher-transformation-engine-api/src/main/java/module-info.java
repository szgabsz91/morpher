@SuppressWarnings("module")
module com.github.szgabsz91.morpher.transformationengines.api {

    requires transitive com.github.szgabsz91.morpher.core;

    requires combinatoricslib3;
    requires com.google.protobuf;
    requires lombok;

    exports com.github.szgabsz91.morpher.transformationengines.api;
    exports com.github.szgabsz91.morpher.transformationengines.api.factories;
    exports com.github.szgabsz91.morpher.transformationengines.api.characters;
    exports com.github.szgabsz91.morpher.transformationengines.api.characters.attributes;
    exports com.github.szgabsz91.morpher.transformationengines.api.characters.converters;
    exports com.github.szgabsz91.morpher.transformationengines.api.characters.repositories;
    exports com.github.szgabsz91.morpher.transformationengines.api.characters.sounds;
    exports com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.consonant;
    exports com.github.szgabsz91.morpher.transformationengines.api.characters.sounds.attributes.vowel;
    exports com.github.szgabsz91.morpher.transformationengines.api.characters.statistics;
    exports com.github.szgabsz91.morpher.transformationengines.api.model;
    exports com.github.szgabsz91.morpher.transformationengines.api.protocolbuffers;
    exports com.github.szgabsz91.morpher.transformationengines.api.utils;
    exports com.github.szgabsz91.morpher.transformationengines.api.wordconverters;

}