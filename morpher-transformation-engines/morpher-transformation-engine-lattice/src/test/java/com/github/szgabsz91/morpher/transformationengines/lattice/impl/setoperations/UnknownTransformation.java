package com.github.szgabsz91.morpher.transformationengines.lattice.impl.setoperations;

import com.github.szgabsz91.morpher.transformationengines.api.characters.ICharacter;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.ITransformation;

import java.util.List;

public class UnknownTransformation implements ITransformation {

    @Override
    public boolean isInconsistent() {
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
