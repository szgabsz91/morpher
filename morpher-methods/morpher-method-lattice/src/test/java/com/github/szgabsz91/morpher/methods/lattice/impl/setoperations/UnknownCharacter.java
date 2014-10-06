package com.github.szgabsz91.morpher.methods.lattice.impl.setoperations;

import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;

import java.util.Collection;

public class UnknownCharacter implements ICharacter {

    @Override
    public IAttribute get(Class<? extends IAttribute> clazz) {
        return null;
    }

    @Override
    public Collection<? extends IAttribute> getAttributes() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isStart() {
        return false;
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public String toString() {
        return "UNKNOWN";
    }

}
