package com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.hyphenation;

import com.github.szgabsz91.morpher.languagehandlers.hunmorph.utils.ExcludeDuringBuild;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@ExcludeDuringBuild
public class PyphenHyphenatorFullTest {

    private static final Path INPUT = Paths.get("../data/ocamorph-results.csv");
    private static final Path OUTPUT = Paths.get("build/pyphen-hyphenator.csv");

    @Test
    public void test() throws IOException {
        try (PyphenHyphenator hyphenator = new PyphenHyphenator();
             BufferedReader reader = Files.newBufferedReader(INPUT, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(OUTPUT, StandardCharsets.UTF_8)) {
            reader
                    .lines()
                    .map(line -> line.split(",")[0])
                    .map(Word::of)
                    .distinct()
                    .forEach(word -> {
                        String[] result = hyphenator.hyphenate(word);

                        if (result.length > 0) {
                            List<String> resultList = Arrays.asList(result);

                            try {
                                writer.write(word.toString());
                                writer.write(',');
                                writer.write(String.join("-", resultList));
                                writer.newLine();
                            }
                            catch (IOException e) {
                                throw new IllegalStateException("Cannot write out result " + Arrays.toString(result) + " for word " + word, e);
                            }
                        }
                    });
        }
    }

}
