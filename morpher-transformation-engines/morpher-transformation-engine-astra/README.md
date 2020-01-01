# Morpher Transformation Engine ASTRA

[![jcenter](https://api.bintray.com/packages/szgabsz91/maven/morpher-transformation-engine-astra/images/download.svg)](https://bintray.com/szgabsz91/maven/morpher-transformation-engine-astra/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.szgabsz91/morpher-transformation-engine-astra/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.szgabsz91/morpher-transformation-engine-astra)

The Atomic String Transformation Rule Assembler (ASTRA) is a position independent transformation rule learning model.

Under the hood we store `RuleGroup` objects that contain one or more `AtomicRule` objects:

```java
public class RuleGroup {

    private final String context;
    private final Set<AtomicRule> atomicRules;
    private final int support;

}
```

```java
public class AtomicRule {

    private final String prefix;
    private final String from;
    private final String to;
    private final String postfix;
    private int support;

}
```

After training, each input word will be transformed using the best matching `AtomicRule`s.
