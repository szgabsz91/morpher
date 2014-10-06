package com.github.szgabsz91.morpher.methods.lattice.converters;

import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;

public enum CustomAttributeForInvocationTargetException implements IAttribute {

    VALUE("v");

    private final String value;

    CustomAttributeForInvocationTargetException(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static CustomAttributeForIllegalAccessException factory(String string) {
        throw new RuntimeException();
    }

}
