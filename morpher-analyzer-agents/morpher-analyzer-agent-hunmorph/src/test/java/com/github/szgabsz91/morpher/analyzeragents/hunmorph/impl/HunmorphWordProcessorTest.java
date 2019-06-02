package com.github.szgabsz91.morpher.analyzeragents.hunmorph.impl;

import com.github.szgabsz91.morpher.analyzeragents.hunmorph.impl.model.HunmorphResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class HunmorphWordProcessorTest {

    private HunmorphWordProcessor hunmorphWordProcessor;

    @BeforeEach
    public void setUp() {
        this.hunmorphWordProcessor = new HunmorphWordProcessor();
    }

    @AfterEach
    public void tearDown() {
        this.hunmorphWordProcessor.close();
    }

    @Test
    public void testProcess() {
        String input = "keserűt";
        Optional<HunmorphResult> optionalResult = this.hunmorphWordProcessor.process(input);
        assertThat(optionalResult).isPresent();
        HunmorphResult result = optionalResult.get();
        assertThat(result.getGrammaticalForm()).isEqualTo(input);
        assertThat(result.getOutputLines()).containsExactly("keserű/ADJ<CAS<ACC>>", "keserű/NOUN<CAS<ACC>>");
    }

    @Test
    public void testProcessWithNoGuessMode() {
        String input = "habablát";
        Optional<HunmorphResult> optionalResult = this.hunmorphWordProcessor.process(input, false);
        assertThat(optionalResult).isNotPresent();
    }

    @Test
    public void testProcessWithGuessMode() {
        String input = "habablát";
        Optional<HunmorphResult> optionalResult = this.hunmorphWordProcessor.process(input, true);
        assertThat(optionalResult).isPresent();
        HunmorphResult result = optionalResult.get();
        assertThat(result.getGrammaticalForm()).isEqualTo(input);
        assertThat(result.getOutputLines()).containsExactly(
                "hababl?NOUN<POSS><CAS<ACC>>",
                "hababla?NOUN<CAS<ACC>>",
                "habablá?NOUN<CAS<ACC>>",
                "habablát?NOUN"
        );
    }

    @Test
    public void testCloseWithIOException() {
        HunmorphWordProcessor hunmorphWordProcessor = new HunmorphWordProcessor();
        hunmorphWordProcessor.close();
        hunmorphWordProcessor.close();
    }

}
