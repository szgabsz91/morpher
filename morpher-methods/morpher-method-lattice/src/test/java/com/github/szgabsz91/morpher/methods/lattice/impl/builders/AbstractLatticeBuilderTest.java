package com.github.szgabsz91.morpher.methods.lattice.impl.builders;

import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class AbstractLatticeBuilderTest {

    private CustomLatticeBuilder latticeBuilder;

    @BeforeEach
    public void setUp() {
        this.latticeBuilder = new CustomLatticeBuilder(null, null);
    }

    @Test
    public void testOnNodeBecomingInhomogeneous() {
        latticeBuilder.onNodeBecomingInhomogeneous(null, null);
    }

    private static class CustomLatticeBuilder extends AbstractLatticeBuilder {

        public CustomLatticeBuilder(ICharacterRepository characterRepository, IWordConverter wordConverter) {
            super(characterRepository, wordConverter);
        }

        @Override
        public void addRules(Set<Rule> rules) {

        }

        @Override
        public boolean skipNodeInserting(Node node, Set<Node> children) {
            return false;
        }

    }

}
