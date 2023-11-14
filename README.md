# [![Morpher](https://raw.githubusercontent.com/szgabsz91/morpher/master/docs/images/morpher.png "Morpher")](https://github.com/szgabsz91/morpher)

[![Build Status](https://img.shields.io/circleci/project/github/szgabsz91/morpher/master.svg)](https://circleci.com/gh/szgabsz91/workflows/morpher)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.szgabsz91/morpher-core)](https://central.sonatype.com/artifact/com.github.szgabsz91/morpher-core)
[![License](https://img.shields.io/github/license/szgabsz91/morpher.svg)](https://github.com/szgabsz91/morpher/blob/master/LICENSE)

Morpher is a general purpose framework for inflection generation and morphological analysis, that lets you plug in
different transformation engine implementations for learning transformation rules of a single affix type,
and language dependent analyzer agents.

The main parts of the framework are:

* [Morpher Core](morpher-core)
* [Morpher Transformation Engines](morpher-transformation-engines)
* [Morpher Language Handlers](morpher-language-handlers)
* [Morpher Engines](morpher-engines)
* [Morpher Systems](morpher-systems)

The project is published to [Maven Central](https://search.maven.org/search?q=morpher).
To see what the latest artifact versions are, check the badges in the subproject README files.

Morpher also has an [API project](https://github.com/szgabsz91/morpher-api) that publishes its main operations using
REST services, and a [client application](https://github.com/szgabsz91/morpher-client) that consumes these services.
