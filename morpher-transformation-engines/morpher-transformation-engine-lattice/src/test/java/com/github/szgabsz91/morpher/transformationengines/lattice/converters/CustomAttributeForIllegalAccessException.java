package com.github.szgabsz91.morpher.transformationengines.lattice.converters;

import com.github.szgabsz91.morpher.transformationengines.api.characters.attributes.IAttribute;

public enum CustomAttributeForIllegalAccessException implements IAttribute {

    VALUE("v");

    private final String value;

    CustomAttributeForIllegalAccessException(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    private static CustomAttributeForIllegalAccessException factory(String string) {
        return null;
    }

}
