package com.github.szgabsz91.morpher.transformationengines.tasr.impl.tree;

import com.github.szgabsz91.morpher.transformationengines.tasr.impl.rules.SuffixRule;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TASRTreeNodeTest {

    @Test
    public void testConstructorAndGettersWithNormalTreeNode() {
        int id = 1;
        char firstCharacter = 'a';
        TASRTreeNode parent = TASRTreeNode.root(2);
        TASRTreeNode node = new TASRTreeNode(id, firstCharacter, parent);

        assertThat(node.getId()).isEqualTo(id);
        assertThat(node.getFirstCharacter()).isEqualTo(firstCharacter);
        assertThat(node.getSuffixRules()).isNotNull();
        assertThat(node.getSuffixRules()).isEmpty();
        assertThat(node.getParent()).isEqualTo(parent);
        assertThat(node.getChildren()).isNotNull();
        assertThat(node.getChildren()).isEmpty();

        assertThat(parent.getChild(firstCharacter)).hasValue(node);
    }

    @Test
    public void testConstructorAndGettersWithRootTreeNode() {
        int id = 1;
        TASRTreeNode root = TASRTreeNode.root(id);

        assertThat(root.getId()).isEqualTo(id);
        assertThat(root.getFirstCharacter()).isEqualTo('\0');
        assertThat(root.getSuffixRules()).isNotNull();
        assertThat(root.getSuffixRules()).isEmpty();
        assertThat(root.getParent()).isNull();
        assertThat(root.getChildren()).isNotNull();
        assertThat(root.getChildren()).isEmpty();
    }

    @Test
    public void testGetWinningSuffixRules() {
        TASRTreeNode node = new TASRTreeNode(1, 'a', null);
        node.addSuffixRule(createSuffixRule("ave", "aved", 2));
        node.addSuffixRule(createSuffixRule("ave", "x", 1));
        Optional<SuffixRule> optionalSuffixRule = node.getWinningSuffixRule();
        assertThat(optionalSuffixRule.isPresent()).isTrue();
        SuffixRule suffixRule = optionalSuffixRule.get();
        assertThat(suffixRule.getLeftHandSuffix()).isEqualTo("ave");
        assertThat(suffixRule.getRightHandSuffix()).isEqualTo("aved");
    }

    @Test
    public void testAddSuffixRuleWithDifferentLeftHandSuffix() {
        TASRTreeNode node = new TASRTreeNode(1, 'a', null);
        SuffixRule suffixRule1 = new SuffixRule("a", "b");
        SuffixRule suffixRule2 = new SuffixRule("x", "y");
        node.addSuffixRule(suffixRule1);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> node.addSuffixRule(suffixRule2));
        assertThat(exception).hasMessage(suffixRule2 + " starts with x instead of a");
    }

    @Test
    public void testAddSuffixRuleWithNonDifferentLeftHandSuffix() {
        TASRTreeNode node = new TASRTreeNode(1, 'x', null);
        SuffixRule suffixRule1 = new SuffixRule("x", "y");
        SuffixRule suffixRule2 = new SuffixRule("x", "z");
        SuffixRule suffixRule3 = new SuffixRule("x", "w");
        node.addSuffixRule(suffixRule1);
        node.addSuffixRule(suffixRule2);
        node.addSuffixRule(suffixRule3);
        node.addSuffixRule(suffixRule2);
        assertThat(node.getSuffixRules()).hasSize(3);
        assertThat(node.getSuffixRules()).contains(createSuffixRule("x", "y", 1));
        assertThat(node.getSuffixRules()).contains(createSuffixRule("x", "z", 2));
        assertThat(node.getSuffixRules()).contains(createSuffixRule("x", "w", 1));
    }

    @Test
    public void testAddChildAndGetChildAndGetChildren() {
        TASRTreeNode node = TASRTreeNode.root(1);
        assertThat(node.getChildren()).isEmpty();
        assertThat(node.getChild('a')).isNotPresent();

        // Add a
        TASRTreeNode childA = new TASRTreeNode(2, 'a', node);
        assertThat(node.getChildren()).hasSize(1);
        assertThat(node.getChild('a')).hasValue(childA);

        // Add b
        TASRTreeNode childB = new TASRTreeNode(3, 'b', node);
        assertThat(node.getChildren()).hasSize(2);
        assertThat(node.getChild('a')).hasValue(childA);
        assertThat(node.getChild('b')).hasValue(childB);

        // Add a
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> node.addChild('a', null));
        assertThat(exception).hasMessage("The given character already has an associated child node");
    }

    @Test
    public void testSetParent() {
        TASRTreeNode oldParent = TASRTreeNode.root(1);
        TASRTreeNode newParent = TASRTreeNode.root(2);
        TASRTreeNode node = new TASRTreeNode(3, 'a', oldParent);
        assertThat(node.getParent()).isEqualTo(oldParent);
        assertThat(oldParent.getChild('a')).hasValue(node);
        assertThat(newParent.getChild('a')).isNotPresent();
        node.setParent(newParent);
        assertThat(node.getParent()).isEqualTo(newParent);
        assertThat(oldParent.getChild('a')).isNotPresent();
        assertThat(newParent.getChild('a')).hasValue(node);
    }

    @Test
    public void testCalculateSizeOfSubtree() {
        /*
         * root
         *     a
         *         c
         *     b
         *         d
         *             f
         *         e
         */
        TASRTreeNode root = TASRTreeNode.root(1);
        TASRTreeNode a = new TASRTreeNode(2, 'a', root);
        TASRTreeNode c = new TASRTreeNode(3, 'c', a);
        TASRTreeNode b = new TASRTreeNode(4, 'b', root);
        TASRTreeNode d = new TASRTreeNode(5, 'd', b);
        TASRTreeNode f = new TASRTreeNode(6, 'f', d);
        TASRTreeNode e = new TASRTreeNode(7, 'e', b);

        int result = root.calculateSizeOfSubtree();
        assertThat(result).isEqualTo(7);
    }

    @Test
    public void testTraverse() {
        /*
         * root
         *     a
         *         c
         *     b
         *         d
         *             f
         *         e
         */
        TASRTreeNode root = TASRTreeNode.root(1);
        root.addSuffixRule(new SuffixRule("", "0"));
        TASRTreeNode a = new TASRTreeNode(2, 'a', root);
        a.addSuffixRule(new SuffixRule("a", "0"));
        TASRTreeNode c = new TASRTreeNode(3, 'c', a);
        c.addSuffixRule(new SuffixRule("c", "0"));
        TASRTreeNode b = new TASRTreeNode(4, 'b', root);
        b.addSuffixRule(new SuffixRule("b", "0"));
        TASRTreeNode d = new TASRTreeNode(5, 'd', b);
        d.addSuffixRule(new SuffixRule("d", "0"));
        TASRTreeNode f = new TASRTreeNode(6, 'f', d);
        f.addSuffixRule(new SuffixRule("f", "0"));
        TASRTreeNode e = new TASRTreeNode(7, 'e', b);
        e.addSuffixRule(new SuffixRule("e", "0"));

        Optional<TASRTreeNode> result = root.traverse(treeNode -> {
            Optional<SuffixRule> optionalSuffixRule = treeNode.getWinningSuffixRule();
            SuffixRule suffixRule = optionalSuffixRule.get();
            String leftHandSuffix = suffixRule.getLeftHandSuffix();
            return Optional.ofNullable(leftHandSuffix.equals("d") ? treeNode : null);
        });
        assertThat(result).hasValueSatisfying(treeNode -> assertThat(treeNode.getWinningSuffixRule().get().getLeftHandSuffix()).isEqualTo("d"));
    }

    @Test
    public void testEquals() {
        TASRTreeNode node1 = new TASRTreeNode(1, 'a', null);
        TASRTreeNode node2 = new TASRTreeNode(2, 'a', null);
        TASRTreeNode node3 = new TASRTreeNode(1, 'a', null);

        assertThat(node1.equals(node1)).isTrue();
        assertThat(node1.equals(null)).isFalse();
        assertThat(node1).isNotEqualTo("string");
        assertThat(node1).isNotEqualTo(node2);
        assertThat(node1).isEqualTo(node3);
    }

    @Test
    public void testHashCode() {
        TASRTreeNode node = new TASRTreeNode(1, 'a', null);
        assertThat(node.hashCode()).isEqualTo(node.getId());
    }

    @Test
    public void testToString() {
        TASRTreeNode node = new TASRTreeNode(1, 'a', null);
        SuffixRule suffixRule1 = new SuffixRule("a", "b");
        SuffixRule suffixRule2 = new SuffixRule("a", "c");
        node.addSuffixRule(suffixRule1);
        node.addSuffixRule(suffixRule2);
        assertThat(node).hasToString("TASRTreeNode[id=" + node.getId() + ", suffixRules=" + node.getSuffixRules() + "]");
    }

    private static SuffixRule createSuffixRule(String leftHandSuffix, String rightHandSuffix, int frequency) {
        SuffixRule suffixRule = new SuffixRule(leftHandSuffix, rightHandSuffix);
        while (suffixRule.getFrequency() != frequency) {
            suffixRule.incrementFrequency();
        }
        return suffixRule;
    }

}
