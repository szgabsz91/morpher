# Morpher Engine Impl

[![jcenter](https://api.bintray.com/packages/szgabsz91/maven/morpher-engine-impl/images/download.svg)](https://bintray.com/szgabsz91/maven/morpher-engine-impl/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.szgabsz91/morpher-engine-impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.szgabsz91/morpher-engine-impl)

Morpher Engine Impl is a Morpher Engine implementation framework that can

* be trained using a set of word,
* incorporate new positive examples incrementally on-the-fly,
* inflect and analyze using `ITransformationEngine` implementations,
* handle multiple affix types,
* incrementally learn the correlation between word-ending substrings and affix types,
* incrementally learn the possibility of one affix type coming after/before another one,
* incrementally learn inflection and analysis rules from examples,
* incrementally learn lemmas,
* be serialized and deserialized.
