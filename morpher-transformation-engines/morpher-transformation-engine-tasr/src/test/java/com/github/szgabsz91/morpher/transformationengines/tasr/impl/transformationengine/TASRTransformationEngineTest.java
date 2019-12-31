package com.github.szgabsz91.morpher.transformationengines.tasr.impl.transformationengine;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.transformationengines.api.model.TrainingSet;
import com.github.szgabsz91.morpher.transformationengines.api.model.TransformationEngineResponse;
import com.github.szgabsz91.morpher.transformationengines.tasr.impl.tree.TASRTree;
import com.github.szgabsz91.morpher.transformationengines.tasr.impl.tree.TASRTreeNode;
import com.github.szgabsz91.morpher.transformationengines.tasr.protocolbuffers.TASRTransformationEngineMessage;
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

public class TASRTransformationEngineTest {

    private AffixType affixType;
    private TASRTransformationEngine tasrTransformationEngine;

    @BeforeEach
    public void setUp() {
        this.affixType = AffixType.of("AFF");
        this.tasrTransformationEngine = new TASRTransformationEngine(false, this.affixType);
    }

    @Test
    public void testGetters() {
        assertThat(this.tasrTransformationEngine.isUnidirectional()).isFalse();
        assertThat(this.tasrTransformationEngine.getAffixType()).isEqualTo(this.affixType);
        TASRTree forwardsTree = this.tasrTransformationEngine.getForwardsTree();
        TASRTree backwardsTree = this.tasrTransformationEngine.getBackwardsTree();
        assertThat(forwardsTree).isNotNull();
        assertThat(backwardsTree).isNotNull();
    }

    @Test
    public void testSizeWithUnidirectionalTASRTransformationEngine() {
        TASRTransformationEngine tasrTransformationEngine = new TASRTransformationEngine(true, null);
        assertThat(tasrTransformationEngine.isUnidirectional()).isTrue();
        tasrTransformationEngine.learn(TrainingSet.of(WordPair.of("abc", "abd")));
        int result = tasrTransformationEngine.size();
        assertThat(result).isEqualTo(tasrTransformationEngine.getForwardsTree().size());
    }

    @Test
    public void testSizeWithBidirectionalTASRTransformationEngine() {
        assertThat(this.tasrTransformationEngine.isUnidirectional()).isFalse();
        this.tasrTransformationEngine.learn(TrainingSet.of(WordPair.of("abc", "abd")));
        int result = this.tasrTransformationEngine.size();
        assertThat(result).isEqualTo((this.tasrTransformationEngine.getForwardsTree().size() + this.tasrTransformationEngine.getBackwardsTree().size()) / 2);
    }

    @Test
    public void testTransformWithNonEmptyLeftHandSuffix() {
        WordPair wordPair = WordPair.of("y", "id");
        this.tasrTransformationEngine.learn(TrainingSet.of(wordPair));
        Optional<TransformationEngineResponse> response = tasrTransformationEngine.transform(Word.of("pay"));
        assertThat(response).hasValue(TransformationEngineResponse.singleton(Word.of("paid")));
    }

    @Test
    public void testTransformWithEmptyLeftHandSuffix() {
        Set<FrequencyAwareWordPair> wordPairs = Set.of(FrequencyAwareWordPair.of("", "ed"));
        this.tasrTransformationEngine.learn(TrainingSet.of(wordPairs));
        Optional<TransformationEngineResponse> response = tasrTransformationEngine.transform(Word.of("jump"));
        assertThat(response).hasValue(TransformationEngineResponse.singleton(Word.of("jumped")));
    }

    @Test
    public void testTransformBackWithUnidirectionalTASRTransformationEngine() {
        TASRTransformationEngine tasrTransformationEngine = new TASRTransformationEngine(true, null);
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> tasrTransformationEngine.transformBack(null));
        assertThat(exception).hasMessage("Unidirectional TASR transformation engine can only transform words forwards but not backwards");
    }

    @Test
    public void testTransformBackWithBidirectionalTASRTransformationEngine() {
        Set<FrequencyAwareWordPair> wordPairs = Set.of(FrequencyAwareWordPair.of("y", "id"));
        this.tasrTransformationEngine.learn(TrainingSet.of(wordPairs));
        Optional<TransformationEngineResponse> response = tasrTransformationEngine.transformBack(Word.of("paid"));
        assertThat(response).hasValue(TransformationEngineResponse.singleton(Word.of("pay")));
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        this.tasrTransformationEngine.learn(TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        )));
        Any message = Any.pack(this.tasrTransformationEngine.toMessage());

        TASRTransformationEngine tasrTransformationEngine = new TASRTransformationEngine(true, null);
        tasrTransformationEngine.fromMessage(message);

        assertThat(tasrTransformationEngine.isUnidirectional()).isEqualTo(this.tasrTransformationEngine.isUnidirectional());
        assertThat(tasrTransformationEngine.getAffixType()).isEqualTo(this.affixType);
        assertThat(tasrTransformationEngine.transform(Word.of("a"))).hasValue(TransformationEngineResponse.singleton(Word.of("b")));
        assertThat(tasrTransformationEngine.transform(Word.of("c"))).hasValue(TransformationEngineResponse.singleton(Word.of("d")));
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(TASRTransformationEngineMessage.newBuilder().build()));
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> this.tasrTransformationEngine.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not a TASRTransformationEngineMessage: " + message);
    }

    @Test
    public void testSaveToAndLoadFrom() throws IOException {
        Set<FrequencyAwareWordPair> wordPairs = Set.of(
                FrequencyAwareWordPair.of("a", "b"),
                FrequencyAwareWordPair.of("c", "d")
        );
        this.tasrTransformationEngine.learn(TrainingSet.of(wordPairs));
        Path file = Files.createTempFile("transformation-engine", "tasr");
        try {
            this.tasrTransformationEngine.saveTo(file);
            TASRTransformationEngine tasrTransformationEngine = new TASRTransformationEngine(false, null);
            tasrTransformationEngine.loadFrom(file);
            assertThat(tasrTransformationEngine.isUnidirectional()).isEqualTo(this.tasrTransformationEngine.isUnidirectional());
            assertThat(tasrTransformationEngine.getAffixType()).isEqualTo(this.affixType);
            assertThat(tasrTransformationEngine.getForwardsTree().size()).isEqualTo(this.tasrTransformationEngine.getForwardsTree().size());
            assertThat(tasrTransformationEngine.getBackwardsTree().size()).isEqualTo(this.tasrTransformationEngine.getBackwardsTree().size());
            wordPairs.forEach(wordPair -> {
                Word rootForm = wordPair.getLeftWord();
                Word inflectedForm = wordPair.getRightWord();
                assertThat(tasrTransformationEngine.transform(rootForm)).hasValue(TransformationEngineResponse.singleton(inflectedForm));
                assertThat(tasrTransformationEngine.transformBack(inflectedForm)).hasValue(TransformationEngineResponse.singleton(rootForm));
            });
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testGetTree() {
        Set<FrequencyAwareWordPair> wordPairs = Set.of(FrequencyAwareWordPair.of("jump", "jumped"));
        this.tasrTransformationEngine.learn(TrainingSet.of(wordPairs));
        TASRTree tree = tasrTransformationEngine.getForwardsTree();
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
    public void testTransformWithUnknownInputWord() {
        Set<FrequencyAwareWordPair> wordPairs = Set.of(
                FrequencyAwareWordPair.of("alma", "almát"),
                FrequencyAwareWordPair.of("malma", "malmát"),
                FrequencyAwareWordPair.of("kefe", "kefét")
        );
        this.tasrTransformationEngine.learn(TrainingSet.of(wordPairs));
        Word input = Word.of("valami");
        Optional<TransformationEngineResponse> response = this.tasrTransformationEngine.transform(input);
        assertThat(response).isEmpty();
    }

}
