package com.github.szgabsz91.morpher.methods.astra.converters;

import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import com.github.szgabsz91.morpher.methods.astra.protocolbuffers.SearcherTypeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SearcherTypeConverterTest {

    private SearcherTypeConverter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new SearcherTypeConverter();
    }

    @Test
    public void testConvert() {
        assertThat(converter.convert(SearcherType.SEQUENTIAL)).isEqualTo(SearcherTypeMessage.SEQUENTIAL);
        assertThat(converter.convert(SearcherType.PARALLEL)).isEqualTo(SearcherTypeMessage.PARALLEL);
        assertThat(converter.convert(SearcherType.PREFIX_TREE)).isEqualTo(SearcherTypeMessage.PREFIX_TREE);
    }

    @Test
    public void testConvertBack() {
        assertThat(converter.convertBack(SearcherTypeMessage.SEQUENTIAL)).isEqualTo(SearcherType.SEQUENTIAL);
        assertThat(converter.convertBack(SearcherTypeMessage.PARALLEL)).isEqualTo(SearcherType.PARALLEL);
        assertThat(converter.convertBack(SearcherTypeMessage.PREFIX_TREE)).isEqualTo(SearcherType.PREFIX_TREE);
    }

}
