package com.github.szgabsz91.morpher.transformationengines.tasr.impl.tree;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.model.TransformationEngineResponse;
import com.github.szgabsz91.morpher.transformationengines.tasr.impl.rules.SuffixRule;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TASRTreeTest {

    @Test
    public void testGetRootWithEmptyTree() {
        TASRTree tree = new TASRTree();
        assertThat(tree.getRoot()).isNull();
    }

    @Test
    public void testGetRootWithNonEmptyTree() {
        TASRTree tree = new TASRTree();
        tree.learn(WordPair.of("a", "b"));
        assertThat(tree.getRoot()).isNotNull();
    }

    @Test
    public void testSetLastNodeId() {
        TASRTree tree = new TASRTree();
        assertThat(tree.getLastNodeId()).isEqualTo(-1);
        tree.insert(new SuffixRule("", "a"));
        assertThat(tree.getLastNodeId()).isEqualTo(0);
        int newLastNodeId = 100;
        tree.setLastNodeId(newLastNodeId);
        assertThat(tree.getLastNodeId()).isEqualTo(newLastNodeId);
    }

    @Test
    public void testSetRoot() {
        TASRTree tree = new TASRTree();
        assertThat(tree.getRoot()).isNull();
        tree.insert(new SuffixRule("", "a"));
        assertThat(tree.getRoot()).isNotNull();
        TASRTreeNode newRoot = TASRTreeNode.root(100);
        tree.setRoot(newRoot);
        assertThat(tree.getRoot()).isEqualTo(newRoot);
    }

    @Test
    public void testGetNodesWithEmptyTree() {
        TASRTree tree = new TASRTree();
        Set<TASRTreeNode> nodes = tree.getNodes();
        assertThat(nodes).isEmpty();
    }

    @Test
    public void testGetNodesWithNonEmptyTree() {
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
        tree.learn(WordPair.of("", "0"));
        TASRTreeNode root = tree.getRoot();
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

        Set<TASRTreeNode> nodes = tree.getNodes();
        assertThat(nodes).hasSize(7);
        assertThat(nodes).contains(root, a,  b, c, d, e, f);
    }

    @Test
    public void testSize() {
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
        tree.insert(new SuffixRule("", "0"));
        TASRTreeNode root = tree.getRoot();
        TASRTreeNode a = new TASRTreeNode(2, 'a', root);
        TASRTreeNode c = new TASRTreeNode(3, 'c', a);
        TASRTreeNode b = new TASRTreeNode(4, 'b', root);
        TASRTreeNode d = new TASRTreeNode(5, 'd', b);
        TASRTreeNode f = new TASRTreeNode(6, 'f', d);
        TASRTreeNode e = new TASRTreeNode(7, 'e', b);

        int result = tree.size();
        assertThat(result).isEqualTo(7);
    }

    @Test
    public void testTransformWithUnknownWord() {
        TASRTree tree = new TASRTree();
        tree.learn(WordPair.of("abcd", "efgh"));
        Word input = Word.of("x");
        Optional<TransformationEngineResponse> response = tree.transform(input);
        assertThat(response).isEmpty();
    }

    @Test
    public void testTransformWithEmptyLeftHandSuffix() {
        TASRTree tree = new TASRTree();
        WordPair wordPair = WordPair.of("", "x");
        tree.learn(wordPair);
        Optional<TransformationEngineResponse> response = tree.transform(wordPair.getLeftWord());
        assertThat(response).hasValue(TransformationEngineResponse.singleton(wordPair.getRightWord()));
    }

    @Test
    public void testTransformWithNonEmptyLeftHandSuffix() {
        TASRTree tree = new TASRTree();
        WordPair wordPair = WordPair.of("x", "y");
        tree.learn(Set.of(wordPair));
        Optional<TransformationEngineResponse> response = tree.transform(wordPair.getLeftWord());
        assertThat(response).hasValue(TransformationEngineResponse.singleton(wordPair.getRightWord()));
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
        TASRTree tree = new TASRTree();
        tree.learn(WordPair.of("", "0"));
        TASRTreeNode root = tree.getRoot();
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

}
