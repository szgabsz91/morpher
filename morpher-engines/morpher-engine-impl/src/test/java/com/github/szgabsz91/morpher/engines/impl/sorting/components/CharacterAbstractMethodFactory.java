package com.github.szgabsz91.morpher.engines.impl.sorting.components;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.methods.api.IInflectionMethod;
import com.github.szgabsz91.morpher.methods.api.IMorpherMethod;
import com.github.szgabsz91.morpher.methods.api.factories.IAbstractMethodFactory;
import com.github.szgabsz91.morpher.methods.api.factories.IMethodConfiguration;
import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessageV3;

import java.nio.file.Path;
import java.util.Random;
import java.util.function.Supplier;

public class CharacterAbstractMethodFactory implements IAbstractMethodFactory<IMethodConfiguration, GeneratedMessageV3> {

    private String character;
    private Random random;

    public CharacterAbstractMethodFactory(String character, Random random) {
        this.character = character;
        this.random = random;
    }

    public CharacterAbstractMethodFactory() {

    }

    @Override
    public IMethodConfiguration getConfiguration() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setConfiguration(IMethodConfiguration configuration) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Supplier<IMorpherMethod<?>> getBidirectionalFactory(AffixType affixType) {
        if (this.character == null || this.random == null) {
            throw new IllegalStateException("Fields must not be null");
        }

        return () -> new CharacterMorpherMethod(character, random);
    }

    @Override
    public Supplier<IInflectionMethod<?>> getUnidirectionalFactory(AffixType affixType) {
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
