package com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.hyphenation;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.protocolbuffers.HyphenatorMessage;
import com.google.protobuf.Any;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PyphenHyphenatorTest {

    private IHyphenator hyphenator;
    private Path tempFile;

    @BeforeEach
    public void setUp() throws IOException {
        this.hyphenator = new PyphenHyphenator();
        this.tempFile = Files.createTempFile("language-handler", "hyphenator");
    }

    @AfterEach
    public void tearDown() throws IOException {
        this.hyphenator.close();
        Files.delete(this.tempFile);
    }

    @Test
    public void testCloseWithAlreadyDeletedPythonFile() {
        IHyphenator hyphenator = new PyphenHyphenator();
        hyphenator.close();
        hyphenator.close();
    }

    @Test
    public void testHyphenate() {
        String[] result = this.hyphenator.hyphenate(Word.of("keserűt"));
        assertThat(result).containsExactly("ke", "se", "rűt");
    }

    @Test
    public void testHyphenateWithEmptyWord() {
        String[] result = this.hyphenator.hyphenate(Word.of(""));
        assertThat(result).isEmpty();
    }

    @Test
    public void testHyphenateWithNullWord() {
        String[] result = this.hyphenator.hyphenate(null);
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetLastSyllable() {
        Optional<String> result = this.hyphenator.getLastSyllable(Word.of("almáitokkal"));
        assertThat(result).hasValue("kal");
    }

    @Test
    public void testGetLastSyllableWithEmptyHyphenationResult() {
        Optional<String> result = this.hyphenator.getLastSyllable(null);
        assertThat(result).isNotPresent();
    }

    @Test
    public void testToMessageAndFromMessage() {
        HyphenatorMessage hyphenatorMessage = this.hyphenator.toMessage();
        assertThat(hyphenatorMessage).isNotNull();
        PyphenHyphenator hyphenator = new PyphenHyphenator();
        hyphenator.fromMessage(hyphenatorMessage);
        hyphenator.fromMessage(Any.pack(hyphenatorMessage));
    }

}
