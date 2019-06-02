package com.github.szgabsz91.morpher.methods.lattice.converters;

import com.github.szgabsz91.morpher.methods.lattice.config.LatticeBuilderType;
import com.github.szgabsz91.morpher.methods.lattice.impl.Lattice;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.FullLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.HomogeneousLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.ILatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.MaximalHomogeneousLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.MinimalLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.LatticeBuilderTypeMessage;
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
        assertThat(this.converter.convert(LatticeBuilderType.FULL)).isEqualTo(LatticeBuilderTypeMessage.FULL);
        assertThat(this.converter.convert(LatticeBuilderType.HOMOGENEOUS)).isEqualTo(LatticeBuilderTypeMessage.HOMOGENEOUS);
        assertThat(this.converter.convert(LatticeBuilderType.MAXIMAL_HOMOGENEOUS)).isEqualTo(LatticeBuilderTypeMessage.MAXIMAL_HOMOGENEOUS);
        assertThat(this.converter.convert(LatticeBuilderType.MINIMAL)).isEqualTo(LatticeBuilderTypeMessage.MINIMAL);
    }

    @Test
    public void testFromImplementation() {
        assertThat(this.converter.fromImplementation(new FullLatticeBuilder(null, null))).isEqualTo(LatticeBuilderTypeMessage.FULL);
        assertThat(this.converter.fromImplementation(new HomogeneousLatticeBuilder(null, null))).isEqualTo(LatticeBuilderTypeMessage.HOMOGENEOUS);
        assertThat(this.converter.fromImplementation(new MaximalHomogeneousLatticeBuilder(null, null))).isEqualTo(LatticeBuilderTypeMessage.MAXIMAL_HOMOGENEOUS);
        assertThat(this.converter.fromImplementation(new MinimalLatticeBuilder(null, null))).isEqualTo(LatticeBuilderTypeMessage.MINIMAL);
        assertThat(this.converter.fromImplementation(new CustomLatticeBuilder())).isEqualTo(LatticeBuilderTypeMessage.MINIMAL);
    }

    @Test
    public void testConvertBack() {
        assertThat(this.converter.convertBack(LatticeBuilderTypeMessage.FULL)).isEqualTo(LatticeBuilderType.FULL);
        assertThat(this.converter.convertBack(LatticeBuilderTypeMessage.HOMOGENEOUS)).isEqualTo(LatticeBuilderType.HOMOGENEOUS);
        assertThat(this.converter.convertBack(LatticeBuilderTypeMessage.MAXIMAL_HOMOGENEOUS)).isEqualTo(LatticeBuilderType.MAXIMAL_HOMOGENEOUS);
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
        public boolean onNodeBecomingInhomogeneous(Lattice lattice, Node node) {
            return false;
        }

        @Override
        public void onNodeInserted(Lattice lattice, Node node) {

        }
        
    }
    
}
