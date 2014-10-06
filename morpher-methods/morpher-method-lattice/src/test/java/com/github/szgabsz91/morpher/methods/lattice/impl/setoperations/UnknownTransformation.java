package com.github.szgabsz91.morpher.methods.lattice.impl.setoperations;

import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.transformations.ITransformation;

import java.util.List;

public class UnknownTransformation implements ITransformation {

    @Override
    public boolean isInhomogeneous() {
        return false;
    }

    @Override
    public int perform(List<ICharacter> characters, int index) {
        return 0;
    }

    @Override
    public String toString() {
        return "UNKNOWN";
    }

}
