package com.github.szgabsz91.morpher.methods.dictionary.converters;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.core.protocolbuffers.WordPairMessage;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.github.szgabsz91.morpher.methods.dictionary.impl.method.DictionaryMethod;
import com.github.szgabsz91.morpher.methods.dictionary.protocolbuffers.DictionaryMethodMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DictionaryMethodConverterTest {

    private DictionaryMethodConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new DictionaryMethodConverter();
    }

    @Test
    public void testConvert() {
        AffixType affixType = AffixType.of("AFF");
        DictionaryMethod dictionaryMethod = new DictionaryMethod(affixType);
        WordPair wordPair = WordPair.of("a", "b");
        dictionaryMethod.learn(TrainingSet.of(wordPair));
        DictionaryMethodMessage dictionaryMethodMessage = this.converter.convert(dictionaryMethod);
        assertThat(dictionaryMethodMessage.getAffixType()).isEqualTo(affixType.toString());
        assertThat(dictionaryMethodMessage.getWordPairsCount()).isEqualTo(1);
        WordPairMessage wordPairMessage = dictionaryMethodMessage.getWordPairs(0);
        assertThat(wordPairMessage.getLeftWord()).isEqualTo(wordPair.getLeftWord().toString());
        assertThat(wordPairMessage.getRightWord()).isEqualTo(wordPair.getRightWord().toString());
    }

    @Test
    public void testConvertBack() {
        AffixType affixType = AffixType.of("AFF");
        WordPair wordPair = WordPair.of("a", "b");
        DictionaryMethodMessage dictionaryMethodMessage = DictionaryMethodMessage.newBuilder()
                .setAffixType(affixType.toString())
                .addWordPairs(WordPairMessage.newBuilder().setLeftWord(wordPair.getLeftWord().toString()).setRightWord(wordPair.getRightWord().toString()))
                .build();
        DictionaryMethod dictionaryMethod = this.converter.convertBack(dictionaryMethodMessage);
        assertThat(dictionaryMethod.getAffixType()).isEqualTo(affixType);
        Map<Word, Word> inflectionDictionary = dictionaryMethod.getInflectionDictionary();
        assertThat(inflectionDictionary.size()).isEqualTo(1);
        Word inflectionResult = inflectionDictionary.get(wordPair.getLeftWord());
        assertThat(inflectionResult).isEqualTo(wordPair.getRightWord());
        Map<Word, Word> lemmatizationDictionary = dictionaryMethod.getLemmatizationDictionary();
        assertThat(lemmatizationDictionary.size()).isEqualTo(1);
        Word lemmatizationResult = lemmatizationDictionary.get(wordPair.getRightWord());
        assertThat(lemmatizationResult).isEqualTo(wordPair.getLeftWord());
    }

    @Test
    public void testParse() throws IOException {
        AffixType affixType = AffixType.of("AFF");
        DictionaryMethod dictionaryMethod = new DictionaryMethod(affixType);
        WordPair wordPair = WordPair.of("a", "b");
        dictionaryMethod.learn(TrainingSet.of(wordPair));
        Path file = Files.createTempFile("morpher", "dictionary");

        try {
            dictionaryMethod.saveTo(file);
            DictionaryMethodMessage dictionaryMethodMessage = this.converter.parse(file);
            DictionaryMethod result = this.converter.convertBack(dictionaryMethodMessage);
            assertThat(result.getAffixType()).isEqualTo(affixType);
            assertThat(result.getInflectionDictionary()).isEqualTo(dictionaryMethod.getInflectionDictionary());
            assertThat(result.getLemmatizationDictionary()).isEqualTo(dictionaryMethod.getLemmatizationDictionary());
        }
        finally {
            Files.delete(file);
        }
    }

}
