package com.github.szgabsz91.morpher.engines.impl.impl.transformationengineholders;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.transformationengines.api.IBidirectionalTransformationEngine;
import com.github.szgabsz91.morpher.transformationengines.api.factories.IAbstractTransformationEngineFactory;
import com.github.szgabsz91.morpher.transformationengines.astra.config.ASTRATransformationEngineConfiguration;
import com.github.szgabsz91.morpher.transformationengines.astra.config.SearcherType;
import com.github.szgabsz91.morpher.transformationengines.astra.impl.transformationengine.ASTRATransformationEngine;
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

public class LazyTransformationEngineHolderTest {

    private IBidirectionalTransformationEngine<?> transformationEngine;
    private LazyTransformationEngineHolder holder;

    @BeforeEach
    public void setUp() {
        AffixType affixType = AffixType.of("AFF");
        ASTRATransformationEngineConfiguration configuration = new ASTRATransformationEngineConfiguration.Builder()
                .searcherType(SearcherType.SEQUENTIAL)
                .build();
        this.transformationEngine = new ASTRATransformationEngine(true, affixType, configuration);
        IAbstractTransformationEngineFactory<?, ?> abstractTransformationEngineFactory = mock(IAbstractTransformationEngineFactory.class);
        when(abstractTransformationEngineFactory.getBidirectionalFactory(affixType)).thenReturn(() -> transformationEngine);
        this.holder = new LazyTransformationEngineHolder(affixType, abstractTransformationEngineFactory, null);
    }

    @AfterEach
    public void tearDown() {
        this.holder.close();
    }

    @Test
    public void testGetWithTransformationEngineNotInMemoryAndNullFile() {
        IBidirectionalTransformationEngine<?> result = this.holder.get();
        assertThat(result).isSameAs(this.transformationEngine);
    }

    @Test
    public void testGetWithTransformationEngineNotInMemoryAndNonExistentFile() throws IOException {
        this.holder.save(this.transformationEngine);
        Path file = this.holder.getFile();
        Files.delete(file);
        IBidirectionalTransformationEngine<?> result = this.holder.get();
        assertThat(result).isSameAs(this.transformationEngine);
    }

    @Test
    public void testGetWithTransformationEngineNotInMemoryAndExistentFile() {
        this.holder.save(this.transformationEngine);
        IBidirectionalTransformationEngine<?> result = this.holder.get();
        assertThat(result).isSameAs(this.transformationEngine);
    }

    @Test
    public void testGetWithTransformationEngineNotInMemoryAndInvalidFile() throws IOException {
        this.holder.save(this.transformationEngine);
        Path file = this.holder.getFile();
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("something");
        }
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.holder.get());
        assertThat(exception).hasMessage("Cannot load transformation engine from file " + file.toAbsolutePath());
        assertThat(exception).hasCauseExactlyInstanceOf(ZipException.class);
    }

    @Test
    public void testGetWithTransformationEngineInMemory() {
        this.holder.save(this.transformationEngine);
        this.holder.get();
        IBidirectionalTransformationEngine<?> result = this.holder.get();
        assertThat(result).isSameAs(this.transformationEngine);
    }

    @Test
    public void testSaveWithNullFile() {
        this.holder.save(this.transformationEngine);
        assertThat(this.holder.getFile()).isNotNull();
        assertThat(this.holder.getFile()).exists();
    }

    @Test
    public void testSaveWithNonExistentFile() throws IOException {
        this.holder.save(this.transformationEngine);
        Path file = this.holder.getFile();
        Files.delete(file);
        this.holder.save(this.transformationEngine);
        file = this.holder.getFile();
        assertThat(file).isNotNull();
        assertThat(file).exists();
    }

    @Test
    public void testSaveWithExistentFile() {
        this.holder.save(this.transformationEngine);
        this.holder.save(this.transformationEngine);
        Path file = this.holder.getFile();
        assertThat(file).isNotNull();
        assertThat(file).exists();
    }

    @Test
    public void testClear() {
        this.holder.save(this.transformationEngine);
        this.holder.get();
        assertThat(this.holder.getTransformationEngine()).isNotNull();
        this.holder.clear();
        assertThat(this.holder.getTransformationEngine()).isNull();
    }

    @Test
    public void testClose() {
        this.holder.close();
    }

}
