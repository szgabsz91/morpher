package com.github.szgabsz91.morpher.methods.tasr.impl.method;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.github.szgabsz91.morpher.methods.tasr.impl.tree.TASRTree;
import com.github.szgabsz91.morpher.methods.tasr.impl.tree.TASRTreeNode;
import com.github.szgabsz91.morpher.methods.tasr.protocolbuffers.TASRMethodMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TASRMethodTest {

    private AffixType affixType;
    private TASRMethod tasrMethod;

    @BeforeEach
    public void setUp() {
        this.affixType = AffixType.of("AFF");
        this.tasrMethod = new TASRMethod(false, this.affixType);
    }

    @Test
    public void testGetters() {
        assertThat(this.tasrMethod.isUnidirectional()).isFalse();
        assertThat(this.tasrMethod.getAffixType()).isEqualTo(this.affixType);
        TASRTree inflectionTree = this.tasrMethod.getInflectionTree();
        TASRTree lemmatizationTree = this.tasrMethod.getLemmatizationTree();
        assertThat(inflectionTree).isNotNull();
        assertThat(lemmatizationTree).isNotNull();
    }

    @Test
    public void testSizeWithUnidirectionalTASRMethod() {
        TASRMethod tasrMethod = new TASRMethod(true, null);
        assertThat(tasrMethod.isUnidirectional()).isTrue();
        tasrMethod.learn(TrainingSet.of(WordPair.of("abc", "abd")));
        int result = tasrMethod.size();
        assertThat(result).isEqualTo(tasrMethod.getInflectionTree().size());
    }

    @Test
    public void testSizeWithBidirectionalTASRMethod() {
        assertThat(this.tasrMethod.isUnidirectional()).isFalse();
        this.tasrMethod.learn(TrainingSet.of(WordPair.of("abc", "abd")));
        int result = this.tasrMethod.size();
        assertThat(result).isEqualTo((this.tasrMethod.getInflectionTree().size() + this.tasrMethod.getLemmatizationTree().size()) / 2);
    }

    @Test
    public void testInflectWithNonEmptyLeftHandSuffix() {
        WordPair wordPair = WordPair.of("y", "id");
        this.tasrMethod.learn(TrainingSet.of(wordPair));
        Optional<MethodResponse> response = tasrMethod.inflect(Word.of("pay"));
        assertThat(response).hasValue(MethodResponse.singleton(Word.of("paid")));
    }

    @Test
    public void testInflectWithEmptyLeftHandSuffix() {
        Set<FrequencyAwareWordPair> wordPairs = Set.of(FrequencyAwareWordPair.of("", "ed"));
        this.tasrMethod.learn(TrainingSet.of(wordPairs));
        Optional<MethodResponse> response = tasrMethod.inflect(Word.of("jump"));
        assertThat(response).hasValue(MethodResponse.singleton(Word.of("jumped")));
    }

    @Test
    public void testLemmatizeWithUnidirectionalTASRMethod() {
        TASRMethod tasrMethod = new TASRMethod(true, null);
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> tasrMethod.lemmatize(null));
        assertThat(exception).hasMessage("Unidirectional TASR method can only inflect but not lemmatize");
    }

    @Test
    public void testLemmatizeWithBidirectionalTASRMethod() {
        Set<FrequencyAwareWordPair> wordPairs = Set.of(FrequencyAwareWordPair.of("y", "id"));
        this.tasrMethod.learn(TrainingSet.of(wordPairs));
        Optional<MethodResponse> response = tasrMethod.lemmatize(Word.of("paid"));
        assertThat(response).hasValue(MethodResponse.singleton(Word.of("pay")));
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        this.tasrMethod.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Any message = Any.pack(this.tasrMethod.toMessage());

        TASRMethod tasrMethod = new TASRMethod(true, null);
        tasrMethod.fromMessage(message);

        assertThat(tasrMethod.isUnidirectional()).isEqualTo(this.tasrMethod.isUnidirectional());
        assertThat(tasrMethod.getAffixType()).isEqualTo(this.affixType);
        assertThat(tasrMethod.inflect(Word.of("a"))).hasValue(MethodResponse.singleton(Word.of("b")));
        assertThat(tasrMethod.inflect(Word.of("c"))).hasValue(MethodResponse.singleton(Word.of("d")));
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(TASRMethodMessage.newBuilder().build()));
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> this.tasrMethod.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not a TASRMethodMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        Set<FrequencyAwareWordPair> wordPairs = Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        );
        this.tasrMethod.learn(TrainingSet.of(wordPairs));
        Path file = Files.createTempFile("morpher", "tasr");
        try {
            this.tasrMethod.saveTo(file);
            TASRMethod tasrMethod = new TASRMethod(false, null);
            tasrMethod.loadFrom(file);
            assertThat(tasrMethod.isUnidirectional()).isEqualTo(this.tasrMethod.isUnidirectional());
            assertThat(tasrMethod.getAffixType()).isEqualTo(this.affixType);
            assertThat(tasrMethod.getInflectionTree().size()).isEqualTo(this.tasrMethod.getInflectionTree().size());
            assertThat(tasrMethod.getLemmatizationTree().size()).isEqualTo(this.tasrMethod.getLemmatizationTree().size());
            wordPairs.forEach(wordPair -> {
                Word rootForm = wordPair.getLeftWord();
                Word inflectedForm = wordPair.getRightWord();
                assertThat(tasrMethod.inflect(rootForm)).hasValue(MethodResponse.singleton(inflectedForm));
                assertThat(tasrMethod.lemmatize(inflectedForm)).hasValue(MethodResponse.singleton(rootForm));
            });
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testGetTree() {
        Set<FrequencyAwareWordPair> wordPairs = Set.of(FrequencyAwareWordPair.of("jump", "jumped"));
        this.tasrMethod.learn(TrainingSet.of(wordPairs));
        TASRTree tree = tasrMethod.getInflectionTree();
        assertThat(tree).isNotNull();
        TASRTreeNode root = tree.getRoot();
        assertThat(root.getWinningSuffixRule()).hasValueSatisfying(suffixRule -> assertThat(suffixRule.getLeftHandSuffix()).isEmpty());
        assertThat(root.getWinningSuffixRule()).hasValueSatisfying(suffixRule -> assertThat(suffixRule.getRightHandSuffix()).isEqualTo("ed"));
        assertThat(root.getChildren()).hasSize(1);
        TASRTreeNode child = root.getChildren().iterator().next();
        assertThat(child.getWinningSuffixRule()).hasValueSatisfying(suffixRule -> assertThat(suffixRule.getLeftHandSuffix()).isEqualTo("p"));
        assertThat(child.getWinningSuffixRule()).hasValueSatisfying(suffixRule -> assertThat(suffixRule.getRightHandSuffix()).isEqualTo("ped"));
        assertThat(child.getChildren()).hasSize(1);
        TASRTreeNode grandChild = child.getChildren().iterator().next();
        assertThat(grandChild.getWinningSuffixRule()).hasValueSatisfying(suffixRule -> assertThat(suffixRule.getLeftHandSuffix()).isEqualTo("mp"));
        assertThat(grandChild.getWinningSuffixRule()).hasValueSatisfying(suffixRule -> assertThat(suffixRule.getRightHandSuffix()).isEqualTo("mped"));
        assertThat(grandChild.getChildren()).hasSize(1);
        TASRTreeNode grandGrandChild = grandChild.getChildren().iterator().next();
        assertThat(grandGrandChild.getWinningSuffixRule()).hasValueSatisfying(suffixRule -> assertThat(suffixRule.getLeftHandSuffix()).isEqualTo("ump"));
        assertThat(grandGrandChild.getWinningSuffixRule()).hasValueSatisfying(suffixRule -> assertThat(suffixRule.getRightHandSuffix()).isEqualTo("umped"));
        assertThat(grandGrandChild.getChildren()).hasSize(1);
        TASRTreeNode grandGrandGrandChild = grandGrandChild.getChildren().iterator().next();
        assertThat(grandGrandGrandChild.getWinningSuffixRule()).hasValueSatisfying(suffixRule -> assertThat(suffixRule.getLeftHandSuffix()).isEqualTo("jump"));
        assertThat(grandGrandGrandChild.getWinningSuffixRule()).hasValueSatisfying(suffixRule -> assertThat(suffixRule.getRightHandSuffix()).isEqualTo("jumped"));
        assertThat(grandGrandGrandChild.getChildren()).isEmpty();
    }

    @Test
    public void testInflectWithUnknownInputWord() {
        Set<FrequencyAwareWordPair> wordPairs = Set.of(
                FrequencyAwareWordPair.of("alma", "almát"),
                FrequencyAwareWordPair.of("malma", "malmát"),
                FrequencyAwareWordPair.of("kefe", "kefét")
        );
        this.tasrMethod.learn(TrainingSet.of(wordPairs));
        Word input = Word.of("valami");
        Optional<MethodResponse> response = this.tasrMethod.inflect(input);
        assertThat(response).isEmpty();
    }

}
