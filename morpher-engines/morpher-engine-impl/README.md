# Morpher Engine Impl

Morpher Engine Impl is a Morpher Engine implementation framework that can

* be trained using a set of word,
* incorporate new positive examples incrementally on-the-fly,
* inflect and lemmatize using `IMorpherMethod` implementations,
* handle multiple affix types,
* incrementally learn the correlation between word-ending substrings and affix types,
* incrementally learn the possibility of one affix type coming after/before another one,
* incrementally learn inflection and lemmatization rules from examples,
* incrementally learn lemmas,
* be serialized and deserialized.
