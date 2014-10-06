package com.github.szgabsz91.morpher.engines.impl.impl.methodholders;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.methods.api.IMorpherMethod;
import com.github.szgabsz91.morpher.methods.api.factories.IAbstractMethodFactory;
import com.github.szgabsz91.morpher.methods.astra.config.ASTRAMethodConfiguration;
import com.github.szgabsz91.morpher.methods.astra.config.SearcherType;
import com.github.szgabsz91.morpher.methods.astra.impl.method.ASTRAMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LazyMorpherMethodHolderTest {

    private ASTRAMethodConfiguration configuration;
    private IMorpherMethod<?> morpherMethod;
    private IAbstractMethodFactory<?, ?> abstractMethodFactory;
    private LazyMorpherMethodHolder holder;

    @BeforeEach
    public void setUp() throws IOException {
        AffixType affixType = AffixType.of("AFF");
        this.configuration = new ASTRAMethodConfiguration.Builder()
                .searcherType(SearcherType.SEQUENTIAL)
                .build();
        this.morpherMethod = new ASTRAMethod(true, affixType, configuration);
        this.abstractMethodFactory = mock(IAbstractMethodFactory.class);
        when(this.abstractMethodFactory.getBidirectionalFactory(affixType)).thenReturn(() -> morpherMethod);
        this.holder = new LazyMorpherMethodHolder(affixType, abstractMethodFactory, null);
    }

    @AfterEach
    public void tearDown() {
        this.holder.close();
    }

    @Test
    public void testGetWithMethodNotInMemoryAndNullFile() {
        IMorpherMethod<?> result = this.holder.get();
        assertThat(result).isSameAs(this.morpherMethod);
    }

    @Test
    public void testGetWithMethodNotInMemoryAndNonExistentFile() throws IOException {
        this.holder.save(this.morpherMethod);
        Path file = this.holder.getFile();
        Files.delete(file);
        IMorpherMethod<?> result = this.holder.get();
        assertThat(result).isSameAs(this.morpherMethod);
    }

    @Test
    public void testGetWithMethodNotInMemoryAndExistentFile() throws IOException {
        this.holder.save(this.morpherMethod);
        IMorpherMethod<?> result = this.holder.get();
        assertThat(result).isSameAs(this.morpherMethod);
    }

    @Test
    public void testGetWithMethodNotInMemoryAndInvalidFile() throws IOException {
        this.holder.save(this.morpherMethod);
        Path file = this.holder.getFile();
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("something");
        }
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.holder.get());
        assertThat(exception).hasMessage("Cannot load morpher method from file " + file.toAbsolutePath());
        assertThat(exception).hasCauseExactlyInstanceOf(ZipException.class);
    }

    @Test
    public void testGetWithMethodInMemory() {
        this.holder.save(this.morpherMethod);
        this.holder.get();
        IMorpherMethod<?> result = this.holder.get();
        assertThat(result).isSameAs(this.morpherMethod);
    }

    @Test
    public void testSaveWithNullFile() {
        this.holder.save(this.morpherMethod);
        assertThat(this.holder.getFile()).isNotNull();
        assertThat(this.holder.getFile()).exists();
    }

    @Test
    public void testSaveWithNonExistentFile() throws IOException {
        this.holder.save(this.morpherMethod);
        Path file = this.holder.getFile();
        Files.delete(file);
        this.holder.save(this.morpherMethod);
        file = this.holder.getFile();
        assertThat(file).isNotNull();
        assertThat(file).exists();
    }

    @Test
    public void testSaveWithExistentFile() {
        this.holder.save(this.morpherMethod);
        this.holder.save(this.morpherMethod);
        Path file = this.holder.getFile();
        assertThat(file).isNotNull();
        assertThat(file).exists();
    }

    @Test
    public void testClear() {
        this.holder.save(this.morpherMethod);
        this.holder.get();
        assertThat(this.holder.getMorpherMethod()).isNotNull();
        this.holder.clear();
        assertThat(this.holder.getMorpherMethod()).isNull();
    }

    @Test
    public void testClose() {
        this.holder.close();
    }

}
