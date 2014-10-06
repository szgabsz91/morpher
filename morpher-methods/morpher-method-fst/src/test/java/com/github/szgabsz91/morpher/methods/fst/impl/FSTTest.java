package com.github.szgabsz91.morpher.methods.fst.impl;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FSTTest {

    @Test
    public void testGetWordPairsWithEmptyTransducer() {
        FST fst = new FST();
        List<WordPair> wordPairs = fst.getWordPairs();
        assertThat(wordPairs).isEmpty();
    }

    @Test
    public void testGetWordPairsWithNonEmptyTransducer() {
        FST fst = new FST();
        Set<WordPair> expected = Set.of(
                WordPair.of("a", "b"),
                WordPair.of("c", "d")
        );
        fst.learn(expected);
        List<WordPair> wordPairs = fst.getWordPairs();
        assertThat(wordPairs).hasSize(2);
        assertThat(wordPairs).containsAll(expected);
    }

    @Test
    public void testSizeWithEmptyTransducer() {
        FST fst = new FST();
        int result = fst.size();
        assertThat(result).isZero();
    }

    @Test
    public void testSizeWithNonEmptyTransducer() {
        FST fst = new FST();
        fst.learn(WordPair.of("a", "b"));
        int result = fst.size();
        assertThat(result).isEqualTo(2);
    }

    @Test
    public void testTransformWithEmptyTransducer() {
        FST fst = new FST();
        Word input = Word.of("a");
        Optional<MethodResponse> response = fst.transform(input);
        assertThat(response).isEmpty();
    }

    @Test
    public void testTransformWithNonEmptyTransducer() {
        FST fst = new FST();
        Word input = Word.of("a");
        MethodResponse expected = MethodResponse.singleton(Word.of("b"));
        fst.learn(WordPair.of(input, expected.getResults().get(0).getWord()));
        Optional<MethodResponse> response = fst.transform(input);
        assertThat(response).hasValue(expected);
    }

    @Test
    public void testTransformWithUnknownInput() {
        FST fst = new FST();
        fst.learn(WordPair.of("a", "b"));
        Word input = Word.of("c");
        Optional<MethodResponse> response = fst.transform(input);
        assertThat(response).isEmpty();
    }

    @Test
    public void testLearnAndBuildPhasesAfterEachOther() {
        FST fst = new FST();
        Set<WordPair> batch1 = Set.of(
                WordPair.of("a", "b"),
                WordPair.of("c", "d")
        );
        WordPair extraWordPair = WordPair.of("e", "f");
        fst.learn(batch1);
        batch1.forEach(wordPair -> assertThat(fst.transform(wordPair.getLeftWord())).hasValue(MethodResponse.singleton(wordPair.getRightWord())));
        fst.learn(extraWordPair);
        batch1.forEach(wordPair -> assertThat(fst.transform(wordPair.getLeftWord())).hasValue(MethodResponse.singleton(wordPair.getRightWord())));
        assertThat(fst.transform(extraWordPair.getLeftWord())).hasValue(MethodResponse.singleton(extraWordPair.getRightWord()));
    }

    @Test
    public void testLearnWithAmbiguousWordPairs() {
        FST fst = new FST();
        Set<WordPair> wordPairs = new LinkedHashSet<>(List.of(
                WordPair.of("a", "b"),
                WordPair.of("a", "c")
        ));
        fst.learn(wordPairs);
        assertThat(fst.transform(Word.of("a"))).hasValue(MethodResponse.singleton(Word.of("b")));
    }

    @Test
    public void testToStringTransformWithEmptyTransducer() {
        FST fst = new FST();
        assertThat(fst).hasToString("EmptyFST");
    }

    @Test
    public void testToStringTransformWithNonEmptyTransducer() {
        FST fst = new FST();
        fst.learn(WordPair.of("a", "b"));
        assertThat(fst.toString()).contains("graph").contains("rank");
    }

}
