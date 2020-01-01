package com.github.szgabsz91.morpher.transformationengines.api.characters.statistics;

import com.github.szgabsz91.morpher.transformationengines.api.characters.ICharacter;
import com.github.szgabsz91.morpher.transformationengines.api.characters.attributes.IAttribute;

import java.util.Collection;
import java.util.Set;

class CustomCharacter implements ICharacter {

    private final Set<IAttribute> attributes;

    CustomCharacter(final Set<IAttribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public IAttribute get(Class<? extends IAttribute> clazz) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Collection<? extends IAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public boolean isEmpty() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean isStart() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean isEnd() {
        throw new IllegalStateException("Not implemented");
    }

}
