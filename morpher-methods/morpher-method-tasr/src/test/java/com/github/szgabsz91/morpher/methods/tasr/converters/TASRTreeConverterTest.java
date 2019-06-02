package com.github.szgabsz91.morpher.methods.tasr.converters;

import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.tasr.impl.tree.TASRTree;
import com.github.szgabsz91.morpher.methods.tasr.protocolbuffers.TASRTreeMessage;
import com.github.szgabsz91.morpher.methods.tasr.protocolbuffers.TASRTreeNodeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TASRTreeConverterTest {

    private TASRTreeConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new TASRTreeConverter();
    }

    @Test
    public void testConvertAndConvertBack() {
        TASRTree tasrTree = createTree();
        TASRTreeMessage tasrTreeMessage = this.converter.convert(tasrTree);
        assertThat(tasrTreeMessage.getLastNodeId()).isEqualTo(tasrTree.size() - 1);
        assertThat(tasrTreeMessage.getNodesList()).hasSize(tasrTree.size());
        for (TASRTreeNodeMessage tasrTreeNodeMessage : tasrTreeMessage.getNodesList()) {
            assertThat(tasrTreeNodeMessage.getFirstCharacter()).isIn(
                    "",
                    "a",
                    "c",
                    "b",
                    "d",
                    "f",
                    "e"
            );
        }
        TASRTree rebuiltTASRTree = this.converter.convertBack(tasrTreeMessage);
        assertThat(rebuiltTASRTree.getRoot()).isEqualTo(tasrTree.getRoot());
        assertThat(rebuiltTASRTree.getNodes()).isEqualTo(tasrTree.getNodes());
    }

    @Test
    public void testParse() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.converter.parse(null));
        assertThat(exception).hasMessage("Suffix rules cannot be saved and loaded individually");
    }

    private TASRTree createTree() {
        /*
         * root
         *     a
         *         c
         *     b
         *         d
         *             f
         *         e
         */
        TASRTree tree = new TASRTree();
        tree.learn(Set.of(
                WordPair.of("", "0"),
                WordPair.of("a", "0"),
                WordPair.of("ac", "0"),
                WordPair.of("b", "0"),
                WordPair.of("bd", "0"),
                WordPair.of("bdf", "0"),
                WordPair.of("be", "0")
        ));
        return tree;
    }

}
