package com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.hyphenation;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.protocolbuffers.HyphenatorMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CachingHyphenatorTest {

    private IHyphenator cachingHyphenator;
    private PyphenHyphenator internalHyphenator;

    @BeforeEach
    public void setUp() {
        this.internalHyphenator = mock(PyphenHyphenator.class);
        this.cachingHyphenator = new CachingHyphenator(internalHyphenator);
    }

    @AfterEach
    public void tearDown() {
        this.cachingHyphenator.close();
    }

    @Test
    public void testDefaultCosntructor() {
        CachingHyphenator hyphenator = new CachingHyphenator();
        assertThat(hyphenator.getInternalHyphenator()).isInstanceOf(PyphenHyphenator.class);
    }

    @Test
    public void testHyphenate() {
        Word input = Word.of("input");
        String[] result = new String[] { "output", "result" };
        when(this.internalHyphenator.hyphenate(input)).thenReturn(result);

        assertThat(this.cachingHyphenator.hyphenate(input)).isEqualTo(result);
        assertThat(this.cachingHyphenator.hyphenate(input)).isEqualTo(result);
        assertThat(this.cachingHyphenator.getLastSyllable(input)).hasValue(result[1]);
        assertThat(this.cachingHyphenator.getLastSyllable(input)).hasValue(result[1]);

        verify(this.internalHyphenator, times(1)).hyphenate(input);
    }

    @Test
    public void testToMessageAndFromMessage() throws InvalidProtocolBufferException {
        Word input = Word.of("input");
        String[] result = new String[] { "output", "result" };
        when(this.internalHyphenator.hyphenate(input)).thenReturn(result);
        when(this.internalHyphenator.toMessage()).thenReturn(HyphenatorMessage.newBuilder().build());
        this.cachingHyphenator.hyphenate(input);

        HyphenatorMessage message = this.cachingHyphenator.toMessage();
        assertThat(message.getCacheMap()).hasSize(1);
        assertThat(message.getCacheMap()).containsExactly(Map.entry(input.toString(), result[0] + '|' + result[1]));

        CachingHyphenator hyphenator = new CachingHyphenator();
        hyphenator.fromMessage(Any.pack(message));
        assertThat(hyphenator.getInternalHyphenator()).isNotNull();
        assertThat(hyphenator.getCache()).containsExactly(Map.entry(input, result));
    }

    @Test
    public void testFromMessageWithInvalidProtocolBuffer() {
        Any message = Any.pack(Any.pack(HyphenatorMessage.newBuilder().build()));
        InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> this.cachingHyphenator.fromMessage(message));
        assertThat(exception).hasMessage("The provided message is not a HyphenatorMessage: " + message);
    }

}
