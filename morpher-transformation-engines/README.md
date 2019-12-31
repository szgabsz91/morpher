# Morpher Transformation Engines

These subprojects contain preimplemented single-affix transformation engine models that can learn the inflection rules
of a single affix type. These transformation engines can be plugged into the Morpher Engine.

The API of the transformation engines that needs to be implemented by all the implementations:

* [Morpher Transformation Engine API](morpher-transformation-engine-api)

The currently available Morpher transformation engine implementations:

* [ASTRA](morpher-transformation-engine-astra)
* [Dictionary](morpher-transformation-engine-dictionary)
* [FST](morpher-transformation-engine-fst)
* [Lattice](morpher-transformation-engine-lattice)
* [TASR](morpher-transformation-engine-tasr)
