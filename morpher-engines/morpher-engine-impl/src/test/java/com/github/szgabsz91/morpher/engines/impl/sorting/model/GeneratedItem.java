package com.github.szgabsz91.morpher.engines.impl.sorting.model;

import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.engines.api.model.PreanalyzedTrainingItem;

import java.util.List;
import java.util.Objects;

public class GeneratedItem {

    private final List<PreanalyzedTrainingItem> preanalyzedTrainingItems;
    private final WordPair wordPair;

    public GeneratedItem(List<PreanalyzedTrainingItem> preanalyzedTrainingItems, WordPair wordPair) {
        this.preanalyzedTrainingItems = preanalyzedTrainingItems;
        this.wordPair = wordPair;
    }

    public List<PreanalyzedTrainingItem> getPreanalyzedTrainingItems() {
        return preanalyzedTrainingItems;
    }

    public WordPair getWordPair() {
        return wordPair;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneratedItem that = (GeneratedItem) o;
        return Objects.equals(preanalyzedTrainingItems, that.preanalyzedTrainingItems) &&
                Objects.equals(wordPair, that.wordPair);
    }

    @Override
    public int hashCode() {
        return Objects.hash(preanalyzedTrainingItems, wordPair);
    }

    @Override
    public String toString() {
        return "GeneratedItem[wordPair=" + wordPair + ']';
    }

}
