package com.github.szgabsz91.morpher.transformationengines.api.model;

import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.WordPair;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainingSetTest {

    @Test
    public void testOfWithFrequencyAwareWordPairs() {
        TrainingSet trainingSet = TrainingSet.of(Set.of(
                FrequencyAwareWordPair.of("a", "b")
        ));

        Set<FrequencyAwareWordPair> result = trainingSet.getWordPairs();
        Set<FrequencyAwareWordPair> expected = Set.of(
                FrequencyAwareWordPair.of("a", "b")
        );

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testOfWithFrequencyAwareWordPair() {
        TrainingSet trainingSet = TrainingSet.of(
                FrequencyAwareWordPair.of("a", "b")
        );

        Set<FrequencyAwareWordPair> result = trainingSet.getWordPairs();
        Set<FrequencyAwareWordPair> expected = Set.of(
                FrequencyAwareWordPair.of("a", "b")
        );

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testOfWithWordPair() {
        TrainingSet trainingSet = TrainingSet.of(
                WordPair.of("a", "b")
        );

        Set<FrequencyAwareWordPair> result = trainingSet.getWordPairs();
        Set<FrequencyAwareWordPair> expected = Set.of(
                FrequencyAwareWordPair.of("a", "b")
        );

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testAddWordPair() {
        FrequencyAwareWordPair wordPair1 = FrequencyAwareWordPair.of("a", "b");
        TrainingSet trainingSet = TrainingSet.of(new HashSet<>(Set.of(
                wordPair1
        )));
        FrequencyAwareWordPair wordPair2 = FrequencyAwareWordPair.of("c", "d");
        trainingSet.addWordPair(wordPair2);
        Set<FrequencyAwareWordPair> expected = Set.of(
                wordPair1,
                wordPair2
        );
        assertThat(trainingSet.getWordPairs()).isEqualTo(expected);
    }

    @Test
    public void testAddWordPairs() {
        FrequencyAwareWordPair wordPair1 = FrequencyAwareWordPair.of("a", "b");
        TrainingSet trainingSet = TrainingSet.of(new HashSet<>(Set.of(
                wordPair1
        )));
        FrequencyAwareWordPair wordPair2 = FrequencyAwareWordPair.of("c", "d");
        FrequencyAwareWordPair wordPair3 = FrequencyAwareWordPair.of("e", "f");
        Set<FrequencyAwareWordPair> wordPairs = Set.of(wordPair2, wordPair3);
        trainingSet.addWordPairs(wordPairs);
        Set<FrequencyAwareWordPair> expected = Set.of(
                wordPair1,
                wordPair2,
                wordPair3
        );
        assertThat(trainingSet.getWordPairs()).isEqualTo(expected);
    }

    @Test
    public void testEquals() {
        TrainingSet trainingSet1 = TrainingSet.of(Set.of(FrequencyAwareWordPair.of("a", "b")));
        TrainingSet trainingSet2 = TrainingSet.of(Set.of(FrequencyAwareWordPair.of("a", "c")));

        assertThat(trainingSet1.equals(trainingSet1)).isTrue();
        assertThat(trainingSet1).isNotEqualTo(null);
        assertThat(trainingSet1).isNotEqualTo("string");
        assertThat(trainingSet1).isNotEqualTo(trainingSet2);
    }

    @Test
    public void testHashCode() {
        Set<FrequencyAwareWordPair> wordPairs = Set.of(FrequencyAwareWordPair.of("a", "b"), FrequencyAwareWordPair.of("c", "d"));
        TrainingSet trainingSet = TrainingSet.of(wordPairs);
        assertThat(trainingSet.hashCode()).isEqualTo(wordPairs.hashCode());
    }

    @Test
    public void testToString() {
        FrequencyAwareWordPair wordPair1 = FrequencyAwareWordPair.of("a", "b");
        FrequencyAwareWordPair wordPair2 = FrequencyAwareWordPair.of("c", "d");
        TrainingSet trainingSet = TrainingSet.of(Set.of(
                wordPair1,
                wordPair2
        ));
        String newLine = System.getProperty("line.separator");

        try {
            String expected = "TrainingSet[" + newLine + "    " + wordPair1 + newLine + "    " + wordPair2 + newLine + "]";
            assertThat(trainingSet).hasToString(expected);
        }
        catch (AssertionError e) {
            String expected = "TrainingSet[" + newLine + "    " + wordPair2 + newLine + "    " + wordPair1 + newLine + "]";
            assertThat(trainingSet).hasToString(expected);
        }
    }

}
