package com.github.szgabsz91.morpher.methods.api.characters.sounds;

import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.IVowelAttribute;
import com.github.szgabsz91.morpher.methods.api.characters.sounds.attributes.vowel.Length;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

public class SoundUtilsTest {

    @Test
    public void testConstructorForCoverage() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<SoundUtils> constructor = SoundUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        try {
            constructor.newInstance();
        }
        finally {
            constructor.setAccessible(false);
        }
    }

    @Test
    public void testHashCode() {
        Vowel vowel = Vowel.create();
        int expected = new HashSet<>(vowel.getAttributes()).hashCode();
        int result = SoundUtils.hashCode(vowel);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testToStringWithRealLetter() {
        String letter = "a";
        Vowel vowel = (Vowel) HungarianAttributedCharacterRepository.get().getCharacter(letter);
        String result = SoundUtils.toString(vowel);
        assertThat(result).isEqualTo(letter);
    }

    @Test
    public void testToStringWithNonRealLetter() {
        Vowel vowel = Vowel.create(Length.LONG);
        String result = SoundUtils.toString(vowel);
        Map<Class<? extends IVowelAttribute>, IVowelAttribute> attributeMap = vowel.getAttributeMap();
        Collection<IVowelAttribute> attributes = attributeMap.values();
        String expectedInnerPart = attributes
                .stream()
                .map(IVowelAttribute::toString)
                .collect(joining(", "));
        String expected = "[" + expectedInnerPart + "]";
        assertThat(result).isEqualTo(expected);
    }

}
