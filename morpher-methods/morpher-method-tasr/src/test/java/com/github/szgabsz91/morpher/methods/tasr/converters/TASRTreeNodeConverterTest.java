package com.github.szgabsz91.morpher.methods.tasr.converters;

import com.github.szgabsz91.morpher.methods.tasr.impl.rules.SuffixRule;
import com.github.szgabsz91.morpher.methods.tasr.impl.tree.TASRTreeNode;
import com.github.szgabsz91.morpher.methods.tasr.protocolbuffers.SuffixRuleMessage;
import com.github.szgabsz91.morpher.methods.tasr.protocolbuffers.TASRTreeNodeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TASRTreeNodeConverterTest {

    private TASRTreeNodeConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new TASRTreeNodeConverter();
    }

    @Test
    public void testConvert() {
        TASRTreeNode root = TASRTreeNode.root(0);
        TASRTreeNode node = new TASRTreeNode(1, 'a', root);
        SuffixRule bSuffixRule = new SuffixRule("a", "b", 2);
        SuffixRule cSuffixRule = new SuffixRule("a", "c", 3);
        node.addSuffixRule(bSuffixRule);
        node.addSuffixRule(cSuffixRule);
        TASRTreeNodeMessage tasrTreeNodeMessage = this.converter.convert(node);
        assertThat(tasrTreeNodeMessage.getId()).isEqualTo(node.getId());
        assertThat(tasrTreeNodeMessage.getFirstCharacter()).isEqualTo(Character.toString(node.getFirstCharacter()));
        assertThat(tasrTreeNodeMessage.getParentNodeId()).isEqualTo(root.getId());
        assertThat(tasrTreeNodeMessage.getSuffixRulesList()).hasSize(2);
        SuffixRuleMessage bSuffixRuleMessage = tasrTreeNodeMessage.getSuffixRules(0).getRightHandSuffix().equals("b") ? tasrTreeNodeMessage.getSuffixRules(0) : tasrTreeNodeMessage.getSuffixRules(1);
        assertThat(bSuffixRuleMessage.getLeftHandSuffix()).isEqualTo(bSuffixRule.getLeftHandSuffix());
        assertThat(bSuffixRuleMessage.getRightHandSuffix()).isEqualTo(bSuffixRule.getRightHandSuffix());
        assertThat(bSuffixRuleMessage.getFrequency()).isEqualTo(bSuffixRule.getFrequency());
        SuffixRuleMessage cSuffixRuleMessage = tasrTreeNodeMessage.getSuffixRules(0).getRightHandSuffix().equals("c") ? tasrTreeNodeMessage.getSuffixRules(0) : tasrTreeNodeMessage.getSuffixRules(1);
        assertThat(cSuffixRuleMessage.getLeftHandSuffix()).isEqualTo(cSuffixRule.getLeftHandSuffix());
        assertThat(cSuffixRuleMessage.getRightHandSuffix()).isEqualTo(cSuffixRule.getRightHandSuffix());
        assertThat(cSuffixRuleMessage.getFrequency()).isEqualTo(cSuffixRule.getFrequency());
    }

    @Test
    public void testConvertWithRoot() {
        TASRTreeNode root = TASRTreeNode.root(1);
        TASRTreeNodeMessage rootMessage = this.converter.convert(root);
        assertThat(rootMessage.getFirstCharacter()).isEmpty();
    }

    @Test
    public void testConvertBack() {
        SuffixRuleMessage bSuffixRuleMessage = SuffixRuleMessage.newBuilder()
                .setLeftHandSuffix("a")
                .setRightHandSuffix("b")
                .setFrequency(2)
                .build();
        SuffixRuleMessage cSuffixRuleMessage = SuffixRuleMessage.newBuilder()
                .setLeftHandSuffix("a")
                .setRightHandSuffix("c")
                .setFrequency(3)
                .build();
        TASRTreeNodeMessage tasrTreeNodeMessage = TASRTreeNodeMessage.newBuilder()
                .setId(1)
                .setFirstCharacter("a")
                .setParentNodeId(2)
                .addAllSuffixRules(Set.of(bSuffixRuleMessage, cSuffixRuleMessage))
                .build();
        TASRTreeNode tasrTreeNode = this.converter.convertBack(tasrTreeNodeMessage);
        assertThat(tasrTreeNode.getId()).isEqualTo(tasrTreeNodeMessage.getId());
        assertThat(tasrTreeNode.getFirstCharacter()).isEqualTo(tasrTreeNodeMessage.getFirstCharacter().charAt(0));
        assertThat(tasrTreeNode.getParent()).isNull();
        assertThat(tasrTreeNode.getSuffixRules()).hasSize(2);
        List<SuffixRule> suffixRuleList = new ArrayList<>(tasrTreeNode.getSuffixRules());
        SuffixRule bSuffixRule = suffixRuleList.get(0).getRightHandSuffix().equals("b") ? suffixRuleList.get(0) : suffixRuleList.get(1);
        assertThat(bSuffixRule.getLeftHandSuffix()).isEqualTo(bSuffixRuleMessage.getLeftHandSuffix());
        assertThat(bSuffixRule.getRightHandSuffix()).isEqualTo(bSuffixRuleMessage.getRightHandSuffix());
        assertThat(bSuffixRule.getFrequency()).isEqualTo(bSuffixRuleMessage.getFrequency());
        SuffixRule cSuffixRule = suffixRuleList.get(0).getRightHandSuffix().equals("c") ? suffixRuleList.get(0) : suffixRuleList.get(1);
        assertThat(cSuffixRule.getLeftHandSuffix()).isEqualTo(cSuffixRuleMessage.getLeftHandSuffix());
        assertThat(cSuffixRule.getRightHandSuffix()).isEqualTo(cSuffixRuleMessage.getRightHandSuffix());
        assertThat(cSuffixRule.getFrequency()).isEqualTo(cSuffixRuleMessage.getFrequency());
    }

    @Test
    public void testConvertBackWithRoot() {
        TASRTreeNodeMessage rootMessage = TASRTreeNodeMessage.newBuilder()
                .setFirstCharacter("")
                .build();
        TASRTreeNode root = this.converter.convertBack(rootMessage);
        assertThat(root.getFirstCharacter()).isEqualTo('\0');
    }

    @Test
    public void testParse() {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> this.converter.parse(null));
        assertThat(exception).hasMessage("Suffix rules cannot be saved and loaded individually");
    }

}
