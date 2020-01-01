package com.github.szgabsz91.morpher.engines.impl.sorting.components;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.engines.impl.transformationengineholders.ITransformationEngineHolder;
import com.github.szgabsz91.morpher.transformationengines.api.IBidirectionalTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.api.IForwardsTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.api.factories.IAbstractTransformationEngineFactory;
import com.github.szgabsz91.morpher.transformationengines.api.factories.ITransformationEngineConfiguration;
import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessageV3;

import java.nio.file.Path;
import java.util.Random;
import java.util.function.Supplier;

public class CharacterAbstractTransformationEngineFactory implements IAbstractTransformationEngineFactory<ITransformationEngineConfiguration, GeneratedMessageV3> {

    private String character;
    private Random random;

    public CharacterAbstractTransformationEngineFactory(String character, Random random) {
        this.character = character;
        this.random = random;
    }

    public CharacterAbstractTransformationEngineFactory() {

    }

    @Override
    public ITransformationEngineConfiguration getConfiguration() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setConfiguration(ITransformationEngineConfiguration configuration) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Supplier<IBidirectionalTransformationEngine<?>> getBidirectionalFactory(AffixType affixType) {
        if (this.character == null || this.random == null) {
            throw new IllegalStateException("Fields must not be null");
        }

        return () -> new CharacterTransformationEngine(character, random);
    }

    @Override
    public Supplier<IForwardsTransformationEngine<?>> getUnidirectionalFactory(AffixType affixType) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GeneratedMessageV3 toMessage() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void fromMessage(GeneratedMessageV3 generatedMessageV3) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void fromMessage(Any message) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void saveTo(Path file) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void loadFrom(Path file) {
        throw new UnsupportedOperationException("Not supported");
    }

}
