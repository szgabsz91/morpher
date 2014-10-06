module com.github.szgabsz91.morpher.methods.api {

    requires com.github.szgabsz91.morpher.core;

    requires combinatoricslib3;
    requires protobuf.java;

    exports com.github.szgabsz91.morpher.methods.api;
    exports com.github.szgabsz91.morpher.methods.api.factories;
    exports com.github.szgabsz91.morpher.methods.api.characters;
    exports com.github.szgabsz91.morpher.methods.api.characters.attributes;
    exports com.github.szgabsz91.morpher.methods.api.characters.converters;
    exports com.github.szgabsz91.morpher.methods.api.characters.repositories;
    exports com.github.szgabsz91.morpher.methods.api.characters.sounds;
    exports com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.consonant;
    exports com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel;
    exports com.github.szgabsz91.morpher.methods.api.characters.statistics;
    exports com.github.szgabsz91.morpher.methods.api.model;
    exports com.github.szgabsz91.morpher.methods.api.protocolbuffers;
    exports com.github.szgabsz91.morpher.methods.api.utils;
    exports com.github.szgabsz91.morpher.methods.api.wordconverterts;

}