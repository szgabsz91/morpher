package com.github.szgabsz91.morpher.core.io;

import com.google.protobuf.GeneratedMessageV3;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SerializerTest {

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        Graph graph = createGraph();
        Path file = Paths.get("build/output.pb");

        @SuppressWarnings("unchecked")
        IConverter<Graph, Graph.GraphMessage> converter = mock(IConverter.class);
        Serializer<Graph, Graph.GraphMessage> serializer = new Serializer<>(converter, graph);

        // Serialization
        Graph.GraphMessage graphMessage = mock(Graph.GraphMessage.class);
        when(converter.convert(graph)).thenReturn(graphMessage);
        serializer.serialize(graph, file);
        verify(converter).convert(graph);
        verify(graphMessage).writeTo(any(OutputStream.class));

        // Deserialization
        when(converter.parse(file)).thenReturn(graphMessage);
        when(converter.convertBack(graphMessage)).thenReturn(graph);
        Graph result = serializer.deserialize(file);
        assertThat(result).isEqualTo(graph);
        verify(converter).parse(file);
        verify(converter).convertBack(graphMessage);
    }

    @Test
    public void testSerializeAndDeserializeWithCustomLogicAndFalseResult() throws IOException {
        CustomSerializerDeserializerWithFalseResult object = new CustomSerializerDeserializerWithFalseResult();
        Path file = Paths.get("build/output.pb");

        @SuppressWarnings("unchecked")
        IConverter<CustomSerializerDeserializerWithFalseResult, CustomSerializerDeserializerWithFalseResult.Message> converter = mock(IConverter.class);
        Serializer<CustomSerializerDeserializerWithFalseResult, CustomSerializerDeserializerWithFalseResult.Message> serializer = new Serializer<>(converter, object);

        // Serialization
        CustomSerializerDeserializerWithFalseResult.Message message = mock(CustomSerializerDeserializerWithFalseResult.Message.class);
        when(converter.convert(object)).thenReturn(message);
        serializer.serialize(object, file);
        verify(converter).convert(object);
        verify(message).writeTo(any(OutputStream.class));

        // Deserialization
        when(converter.parse(file)).thenReturn(message);
        when(converter.convertBack(message)).thenReturn(object);
        CustomSerializerDeserializerWithFalseResult result = serializer.deserialize(file);
        assertThat(result).isEqualTo(object);
        verify(converter).parse(file);
        verify(converter).convertBack(message);
    }

    @Test
    public void testSerializeAndDeserializeWithCustomLogicAndTrueResult() throws IOException {
        String id = "hello";
        CustomSerializerDeserializerWithTrueResult object = new CustomSerializerDeserializerWithTrueResult(id);
        Path file = Paths.get("build/output.pb");

        @SuppressWarnings("unchecked")
        IConverter<CustomSerializerDeserializerWithTrueResult, CustomSerializerDeserializerWithTrueResult.Message> converter = mock(IConverter.class);
        Serializer<CustomSerializerDeserializerWithTrueResult, CustomSerializerDeserializerWithTrueResult.Message> serializer = new Serializer<>(converter, new CustomSerializerDeserializerWithTrueResult("other"));

        try {
            // Serialization
            serializer.serialize(object, file);
            verify(converter, never()).convert(any());

            // Deserialization
            CustomSerializerDeserializerWithTrueResult result = serializer.deserialize(file);
            assertThat(result.getId()).isEqualTo(id);
            verify(converter, never()).convertBack(any());
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testSerializeAndDeserializeWithNoConvertBackSupportAndSavable() throws IOException {
        Path file = Paths.get("build/output.pb");

        @SuppressWarnings("unchecked")
        IConverter<ISavable, GeneratedMessageV3> converter = mock(IConverter.class);
        ISavable savable = mock(ISavable.class);
        Serializer<ISavable, GeneratedMessageV3> serializer = new Serializer<>(converter, savable);

        when(converter.isConvertBackSupported()).thenReturn(false);

        ISavable result = serializer.deserialize(file);
        assertThat(result).isSameAs(savable);
        verify(converter).isConvertBackSupported();
        verify(savable).loadFrom(file);
    }

    @Test
    public void testSerializeAndDeserializeWithConvertBackSupportAndNoSavable() throws IOException {
        Path file = Paths.get("build/output.pb");

        @SuppressWarnings("unchecked")
        IConverter<CustomNonSavable, CustomNonSavable.Message> converter = mock(IConverter.class);
        CustomNonSavable nonSavable = mock(CustomNonSavable.class);
        CustomNonSavable.Message message = mock(CustomNonSavable.Message.class);
        CustomNonSavable.Message expectedMessage = mock(CustomNonSavable.Message.class);
        CustomNonSavable expected = mock(CustomNonSavable.class);
        Serializer<CustomNonSavable, CustomNonSavable.Message> serializer = new Serializer<>(converter, nonSavable);

        when(converter.convert(nonSavable)).thenReturn(message);
        doNothing().when(message).writeTo(any(OutputStream.class));
        when(converter.isConvertBackSupported()).thenReturn(true);
        when(converter.parse(file)).thenReturn(expectedMessage);
        when(converter.convertBack(expectedMessage)).thenReturn(expected);

        try {
            // Serialization
            serializer.serialize(nonSavable, file);
            verify(converter).convert(nonSavable);
            verify(message).writeTo(any(OutputStream.class));

            // Deserialization
            CustomNonSavable result = serializer.deserialize(file);
            assertThat(result).isSameAs(expected);
            verify(converter).isConvertBackSupported();
            verify(converter).parse(file);
            verify(converter).convertBack(expectedMessage);
        }
        finally {
            Files.delete(file);
        }
    }

    @Test
    public void testZipFolderAndUnzipFolder() throws IOException {
        Path originalFolder = Paths.get("build/folder-original");
        Files.createDirectory(originalFolder);
        Path fileInOriginalFolder = Paths.get(originalFolder.toAbsolutePath().toString(), "file.txt");
        String lineInFileInOriginalFolder = "line";
        try (BufferedWriter writer = Files.newBufferedWriter(fileInOriginalFolder, StandardCharsets.UTF_8)) {
            writer.write(lineInFileInOriginalFolder);
        }
        Path zipFile = Paths.get("build/folder-original.zip");
        Path newFolder = Paths.get("build/folder-new");
        Files.createDirectory(newFolder);
        Path fileInNewFolder = Paths.get(newFolder.toAbsolutePath().toString(), "file.txt");

        try {
            Serializer.zipFolder(originalFolder, zipFile);
            Serializer.unzipFolder(zipFile, newFolder);

            String lineInFileInNewFolder = null;
            try (BufferedReader reader = Files.newBufferedReader(fileInNewFolder, StandardCharsets.UTF_8)) {
                lineInFileInNewFolder = reader.readLine();
            }
            assertThat(lineInFileInNewFolder).isEqualTo(lineInFileInOriginalFolder);
        }
        finally {
            Files.delete(zipFile);
            Serializer.deleteFolder(originalFolder);
            Serializer.deleteFolder(newFolder);
        }
    }

    @Test
    public void testZipFolderAndUnzipFolderWithExistingZipFile() throws IOException {
        Path originalFolder = Paths.get("build/folder-original");
        Files.createDirectory(originalFolder);
        Path fileInOriginalFolder = Paths.get(originalFolder.toAbsolutePath().toString(), "file.txt");
        String lineInFileInOriginalFolder = "line";
        try (BufferedWriter writer = Files.newBufferedWriter(fileInOriginalFolder, StandardCharsets.UTF_8)) {
            writer.write(lineInFileInOriginalFolder);
        }
        Path zipFile = Paths.get("build/folder-original.zip");
        Files.createFile(zipFile);
        Path newFolder = Paths.get("build/folder-new");
        Files.createDirectory(newFolder);
        Path fileInNewFolder = Paths.get(newFolder.toAbsolutePath().toString(), "file.txt");

        try {
            Serializer.zipFolder(originalFolder, zipFile);
            Serializer.unzipFolder(zipFile, newFolder);

            String lineInFileInNewFolder = null;
            try (BufferedReader reader = Files.newBufferedReader(fileInNewFolder, StandardCharsets.UTF_8)) {
                lineInFileInNewFolder = reader.readLine();
            }
            assertThat(lineInFileInNewFolder).isEqualTo(lineInFileInOriginalFolder);
        }
        finally {
            Files.delete(zipFile);
            Serializer.deleteFolder(originalFolder);
            Serializer.deleteFolder(newFolder);
        }
    }

    private Graph createGraph() {
        Node a = new Node("a");
        Node b = new Node("b");

        a.setNext(b);
        b.setNext(a);

        return new Graph(a);
    }

}
