package com.github.szgabsz91.morpher.methods.fst.converters;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.core.protocolbuffers.WordPairMessage;
import com.github.szgabsz91.morpher.methods.api.model.MethodResponse;
import com.github.szgabsz91.morpher.methods.api.model.TrainingSet;
import com.github.szgabsz91.morpher.methods.fst.impl.method.FSTMethod;
import com.github.szgabsz91.morpher.methods.fst.protocolbuffers.FSTMethodMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FSTMethodConverterTest {

    private FSTMethodConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new FSTMethodConverter();
    }

    @Test
    public void testConvert() {
        AffixType affixType = AffixType.of("AFF");
        FSTMethod fstMethod = new FSTMethod(true, affixType);
        WordPair wordPair = WordPair.of("a", "b");
        fstMethod.learn(TrainingSet.of(wordPair));
        FSTMethodMessage fstMethodMessage = this.converter.convert(fstMethod);
        assertThat(fstMethodMessage.getUnidirectional()).isTrue();
        assertThat(fstMethodMessage.getAffixType()).isEqualTo(affixType.toString());
        assertThat(fstMethodMessage.getWordPairsCount()).isEqualTo(1);
        WordPairMessage wordPairMessage = fstMethodMessage.getWordPairs(0);
        assertThat(wordPairMessage.getLeftWord()).isEqualTo(wordPair.getLeftWord().toString());
        assertThat(wordPairMessage.getRightWord()).isEqualTo(wordPair.getRightWord().toString());
    }

    @Test
    public void testConvertBackWithUnidirectionalMethod() {
        AffixType affixType = AffixType.of("AFF");
        WordPair wordPair = WordPair.of("a", "b");
        FSTMethodMessage fstMethodMessage = FSTMethodMessage.newBuilder()
                .setUnidirectional(true)
                .setAffixType(affixType.toString())
                .addWordPairs(WordPairMessage.newBuilder().setLeftWord(wordPair.getLeftWord().toString()).setRightWord(wordPair.getRightWord().toString()))
                .build();
        FSTMethod fstMethod = this.converter.convertBack(fstMethodMessage);
        assertThat(fstMethod.isUnidirectional()).isTrue();
        assertThat(fstMethod.getAffixType()).isEqualTo(affixType);
        List<WordPair> wordPairs = fstMethod.getWordPairs();
        assertThat(wordPairs.size()).isEqualTo(1);
        WordPair result = wordPairs.get(0);
        assertThat(result).isEqualTo(wordPair);
        assertThat(fstMethod.inflect(wordPair.getLeftWord())).hasValue(MethodResponse.singleton(wordPair.getRightWord()));
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> fstMethod.lemmatize(wordPair.getRightWord()));
        assertThat(exception).hasMessage("Unidirectional FST method can only inflect but not lemmatize");
    }

    @Test
    public void testConvertBackWithBidirectionalMethod() {
        AffixType affixType = AffixType.of("AFF");
        WordPair wordPair = WordPair.of("a", "b");
        FSTMethodMessage fstMethodMessage = FSTMethodMessage.newBuilder()
                .setUnidirectional(false)
                .setAffixType(affixType.toString())
                .addWordPairs(WordPairMessage.newBuilder().setLeftWord(wordPair.getLeftWord().toString()).setRightWord(wordPair.getRightWord().toString()))
                .build();
        FSTMethod fstMethod = this.converter.convertBack(fstMethodMessage);
        assertThat(fstMethod.isUnidirectional()).isFalse();
        assertThat(fstMethod.getAffixType()).isEqualTo(affixType);
        List<WordPair> wordPairs = fstMethod.getWordPairs();
        assertThat(wordPairs.size()).isEqualTo(1);
        WordPair result = wordPairs.get(0);
        assertThat(result).isEqualTo(wordPair);
        assertThat(fstMethod.inflect(wordPair.getLeftWord())).hasValue(MethodResponse.singleton(wordPair.getRightWord()));
        assertThat(fstMethod.lemmatize(wordPair.getRightWord())).hasValue(MethodResponse.singleton(wordPair.getLeftWord()));
    }

    @Test
    public void testParse() throws IOException {
        AffixType affixType = AffixType.of("AFF");
        FSTMethod fstMethod = new FSTMethod(false, affixType);
        WordPair wordPair = WordPair.of("a", "b");
        fstMethod.learn(TrainingSet.of(wordPair));
        Path file = Files.createTempFile("morpher", "fst");

        try {
            fstMethod.saveTo(file);
            FSTMethodMessage fstMethodMessage = this.converter.parse(file);
            FSTMethod result = this.converter.convertBack(fstMethodMessage);
            assertThat(result.isUnidirectional()).isEqualTo(fstMethod.isUnidirectional());
            assertThat(result.getAffixType()).isEqualTo(affixType);
            assertThat(result.getWordPairs()).isEqualTo(fstMethod.getWordPairs());
        }
        finally {
            Files.delete(file);
        }
    }

}
