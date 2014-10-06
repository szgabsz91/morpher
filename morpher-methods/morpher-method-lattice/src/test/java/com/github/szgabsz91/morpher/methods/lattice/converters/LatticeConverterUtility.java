package com.github.szgabsz91.morpher.methods.lattice.converters;

import com.github.szgabsz91.morpher.core.model.FrequencyAwareWordPair;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.methods.api.characters.ICharacter;
import com.github.szgabsz91.morpher.methods.api.characters.attributes.IAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.statistics.IAttributeStatistics;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;

import java.util.List;
import java.util.Set;

public class LatticeConverterUtility {

    public static class CustomCharacterRepositoryForIllegalAccessException implements ICharacterRepository {

        private static ICharacterRepository get() {
            return new CustomCharacterRepositoryForIllegalAccessException();
        }

        @Override
        public ICharacter getCharacter(String letter) {
            return null;
        }

        @Override
        public String getLetter(ICharacter character) {
            return null;
        }

        @Override
        public ICharacter getCharacter(Set<? extends IAttribute> attributes) {
            return null;
        }

        @Override
        public String getLetter(Set<? extends IAttribute> attributes) {
            return null;
        }

        @Override
        public ICharacter getStartCharacter() {
            return null;
        }

        @Override
        public ICharacter getEndCharacter() {
            return null;
        }

        @Override
        public IAttributeStatistics getAttributeStatistics() {
            return null;
        }

        @Override
        public double calculateSimilarity(ICharacter character1, ICharacter character2) {
            return 0.0;
        }

    }

    public static class CustomWordConverterForIllegalAccessException implements IWordConverter {

        private CustomWordConverterForIllegalAccessException() {

        }

        @Override
        public String convert(String string) {
            return null;
        }

        @Override
        public Word convert(Word word) {
            return null;
        }

        @Override
        public List<ICharacter> convert(Word word, ICharacterRepository characterRepository) {
            return null;
        }

        @Override
        public WordPair convert(WordPair wordPair) {
            return null;
        }

        @Override
        public FrequencyAwareWordPair convert(FrequencyAwareWordPair wordPair) {
            return null;
        }

        @Override
        public String convertBack(String string) {
            return null;
        }

        @Override
        public Word convertBack(Word word) {
            return null;
        }

        @Override
        public Word convertBack(List<ICharacter> characters, ICharacterRepository characterRepository) {
            return null;
        }

        @Override
        public WordPair convertBack(WordPair wordPair) {
            return null;
        }

    }

}
