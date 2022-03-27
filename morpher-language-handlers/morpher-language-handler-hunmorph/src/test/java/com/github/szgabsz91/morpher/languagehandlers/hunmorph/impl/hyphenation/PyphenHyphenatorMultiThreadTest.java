package com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.hyphenation;

import com.github.szgabsz91.morpher.languagehandlers.hunmorph.utils.ExcludeDuringBuild;
import com.github.szgabsz91.morpher.core.model.Word;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import static java.util.stream.Collectors.toUnmodifiableMap;

@ExcludeDuringBuild
public class PyphenHyphenatorMultiThreadTest {

    private static final Path INPUT = Paths.get("../data/ocamorph-results.csv");
    private static final Path TEMP = Paths.get("../data/hyphens.csv");
    private static final Path OUTPUT = Paths.get("build/pyphen-hyphenator.csv");

    @Test
    public void test() throws Exception {
        // Preprocess results
        Map<String, HyphenationResult> previousHyphenationResultMap;
        try (BufferedReader reader = Files.newBufferedReader(TEMP, StandardCharsets.UTF_8)) {
            previousHyphenationResultMap = reader.lines()
                    .filter(line -> !"elősegít".equals(line))
                    .map(line -> line.split(","))
                    .map(lineParts -> new HyphenationResult(lineParts[0], lineParts[1].split("-")))
                    .collect(toUnmodifiableMap(HyphenationResult::getWord, Function.identity()));
        }

        // Process items
        try (PyphenHyphenator hyphenator = new PyphenHyphenator();
             BufferedReader reader = Files.newBufferedReader(INPUT, StandardCharsets.UTF_8);
             HyphenationResultSink sink = new HyphenationResultSink()) {
            Thread sinkThread = new Thread(sink);

            List<String> wordsToProcess = reader
                    .lines()
                    .map(line -> line.split(",")[0])
                    .distinct()
                    .toList();
            sink.setTotalItemSize(wordsToProcess.size());
            sinkThread.start();

            wordsToProcess
                    .parallelStream()
                    .filter(x -> x.equals("leperdíti"))
                    .map(word -> {
                        if (previousHyphenationResultMap.containsKey(word)) {
                            return previousHyphenationResultMap.get(word);
                        }

                        String[] result = hyphenator.hyphenate(Word.of(word));

                        if (result.length > 0) {
                            return new HyphenationResult(word, result);
                        }

                        return null;
                    })
                    .filter(Objects::nonNull)
                    .forEach(sink::add);

            sink.stop();
            sinkThread.join();
        }
    }

    private static class HyphenationResult {

        private final String word;
        private final String[] hyphens;

        private HyphenationResult(String word, String[] hyphens) {
            this.word = word;
            this.hyphens = hyphens;
        }

        public String getWord() {
            return word;
        }

        public String[] getHyphens() {
            return hyphens;
        }

        @Override
        public String toString() {
            return "HyphenationResult{" +
                    "word=" + word +
                    ", hyphens=" + Arrays.toString(hyphens) +
                    '}';
        }

    }

    private static class HyphenationResultSink implements Runnable, AutoCloseable {

        private static final int WAIT_DELAY = 60;
        private static final Logger LOGGER = LoggerFactory.getLogger(HyphenationResultSink.class);

        private final ConcurrentLinkedQueue<HyphenationResult> hyphenationResultQueue;
        private long itemsProcessed;
        private int totalItemSize;
        private boolean stopped;
        private final BufferedWriter writer;

        public HyphenationResultSink() throws IOException {
            this.hyphenationResultQueue = new ConcurrentLinkedQueue<>();
            this.itemsProcessed = 0L;
            this.stopped = false;
            this.writer = Files.newBufferedWriter(OUTPUT, StandardCharsets.UTF_8);
        }

        public void setTotalItemSize(int totalItemSize) {
            this.totalItemSize = totalItemSize;
        }

        public void stop() {
            this.stopped = true;
        }

        public void add(HyphenationResult hyphenationResult) {
            this.hyphenationResultQueue.add(hyphenationResult);
        }

        @Override
        public void close() throws IOException {
            this.writer.close();
        }

        @Override
        public void run() {
            try {
                while (!this.stopped) {
                    this.process();
                    Thread.sleep(WAIT_DELAY * 1000);
                }

                this.process();
            }
            catch (InterruptedException e) {
                LOGGER.warn("Interrupted", e);
            }
        }

        private void process() {
            while (!this.hyphenationResultQueue.isEmpty()) {
                HyphenationResult hyphenationResult = this.hyphenationResultQueue.remove();
                this.itemsProcessed++;

                try {
                    writer.write(hyphenationResult.getWord());
                    writer.write(',');
                    writer.write(String.join("-", hyphenationResult.getHyphens()));
                    writer.newLine();
                }
                catch (IOException e) {
                    LOGGER.error("Cannot write line", e);
                }
            }

            LOGGER.info("{}/{} items processed", this.itemsProcessed, this.totalItemSize);
            try {
                writer.flush();
            }
            catch (IOException e) {
                LOGGER.error("Cannot flush file", e);
            }
        }

    }

}
