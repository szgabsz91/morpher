package com.github.szgabsz91.morpher.transformationengines.lattice.converters;

import com.github.szgabsz91.morpher.transformationengines.lattice.config.LatticeBuilderType;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.Lattice;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.CompleteLatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.ConsistentLatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.ILatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.MaximalConsistentLatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.builders.MinimalLatticeBuilder;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.transformationengines.lattice.protocolbuffers.LatticeBuilderTypeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LatticeBuilderTypeConverterTest {

    private LatticeBuilderTypeConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new LatticeBuilderTypeConverter();
    }

    @Test
    public void testConvert() {
        assertThat(this.converter.convert(LatticeBuilderType.COMPLETE)).isEqualTo(LatticeBuilderTypeMessage.COMPLETE);
        assertThat(this.converter.convert(LatticeBuilderType.CONSISTENT)).isEqualTo(LatticeBuilderTypeMessage.CONSISTENT);
        assertThat(this.converter.convert(LatticeBuilderType.MAXIMAL_CONSISTENT)).isEqualTo(LatticeBuilderTypeMessage.MAXIMAL_CONSISTENT);
        assertThat(this.converter.convert(LatticeBuilderType.MINIMAL)).isEqualTo(LatticeBuilderTypeMessage.MINIMAL);
    }

    @Test
    public void testFromImplementation() {
        assertThat(this.converter.fromImplementation(new CompleteLatticeBuilder(null, null))).isEqualTo(LatticeBuilderTypeMessage.COMPLETE);
        assertThat(this.converter.fromImplementation(new ConsistentLatticeBuilder(null, null))).isEqualTo(LatticeBuilderTypeMessage.CONSISTENT);
        assertThat(this.converter.fromImplementation(new MaximalConsistentLatticeBuilder(null, null))).isEqualTo(LatticeBuilderTypeMessage.MAXIMAL_CONSISTENT);
        assertThat(this.converter.fromImplementation(new MinimalLatticeBuilder(null, null))).isEqualTo(LatticeBuilderTypeMessage.MINIMAL);
        assertThat(this.converter.fromImplementation(new CustomLatticeBuilder())).isEqualTo(LatticeBuilderTypeMessage.MINIMAL);
    }

    @Test
    public void testConvertBack() {
        assertThat(this.converter.convertBack(LatticeBuilderTypeMessage.COMPLETE)).isEqualTo(LatticeBuilderType.COMPLETE);
        assertThat(this.converter.convertBack(LatticeBuilderTypeMessage.CONSISTENT)).isEqualTo(LatticeBuilderType.CONSISTENT);
        assertThat(this.converter.convertBack(LatticeBuilderTypeMessage.MAXIMAL_CONSISTENT)).isEqualTo(LatticeBuilderType.MAXIMAL_CONSISTENT);
        assertThat(this.converter.convertBack(LatticeBuilderTypeMessage.MINIMAL)).isEqualTo(LatticeBuilderType.MINIMAL);
    }

    @Test
    public void testToImplementation() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.converter.toImplementation(null));
        assertThat(exception).hasMessage("Not supported operation");
    }

    private static class CustomLatticeBuilder implements ILatticeBuilder {

        @Override
        public Lattice getLattice() {
            return null;
        }

        @Override
        public void addRule(Rule rule) {

        }

        @Override
        public void addRules(Set<Rule> rules) {

        }

        @Override
        public boolean skipNodeInserting(Node node, Set<Node> children) {
            return false;
        }

        @Override
        public boolean onNodeBecomingInconsistent(Lattice lattice, Node node) {
            return false;
        }

        @Override
        public void onNodeInserted(Lattice lattice, Node node) {

        }
        
    }
    
}
