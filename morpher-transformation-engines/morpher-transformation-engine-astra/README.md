# Morpher Transformation Engine ASTRA

[![Maven Central](https://img.shields.io/maven-central/v/com.github.szgabsz91/morpher-transformation-engine-astra)](https://central.sonatype.com/artifact/com.github.szgabsz91/morpher-transformation-engine-astra)

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
