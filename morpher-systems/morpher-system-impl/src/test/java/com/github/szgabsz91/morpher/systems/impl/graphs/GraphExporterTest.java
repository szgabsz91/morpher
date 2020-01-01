package com.github.szgabsz91.morpher.systems.impl.graphs;

import com.github.szgabsz91.morpher.systems.impl.graphs.exporters.IGraphExporter;
import com.github.szgabsz91.morpher.systems.impl.graphs.exporters.forcedirectedgraph.FullForceDirectedGraphExporter;
import com.github.szgabsz91.morpher.systems.impl.graphs.exporters.forcedirectedgraph.PartialForceDirectedGraphExporter;
import com.github.szgabsz91.morpher.systems.impl.graphs.exporters.radialtree.FullRadialTreeExporter;
import com.github.szgabsz91.morpher.systems.impl.graphs.exporters.radialtree.PartialRadialTreeExporter;
import com.github.szgabsz91.morpher.systems.impl.utils.ExcludeDuringBuild;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@ExcludeDuringBuild
public class GraphExporterTest {

    private static final Path FOLDER_BUILD = Paths.get("data/graphs");

    private static Map<List<String>, Long> routeMap;

    @BeforeAll
    public static void setUpClass() throws IOException {
        Files.createDirectories(FOLDER_BUILD);

        routeMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(GraphExporterTest.class.getResourceAsStream("affix-type-chains.csv"), StandardCharsets.UTF_8))) {
            routeMap = reader
                    .lines()
                    .map(line -> {
                        return Arrays.stream(line.split(","))
                                .map(affixType -> {
                                    if (affixType.contains("PREVERB")) {
                                        return "PREVERB";
                                    }
                                    if (affixType.contains("<POSTP")) {
                                        return "POSTP";
                                    }
                                    return affixType;
                                })
                                .collect(toList());
                    })
                    .collect(groupingBy(Function.identity(), counting()));
        }
    }

    @Test
    public void testExportFullForceDirectedGraph() throws IOException {
        IGraphExporter graphExporter = new FullForceDirectedGraphExporter(FOLDER_BUILD);
        graphExporter.export(routeMap);
    }

    @Test
    public void testExportPartialForceDirectedGraph() throws IOException {
        IGraphExporter graphExporter = new PartialForceDirectedGraphExporter(FOLDER_BUILD);
        graphExporter.export(routeMap);
    }

    @Test
    public void testExportPartialRadialTree() throws IOException {
        IGraphExporter graphExporter = new PartialRadialTreeExporter(FOLDER_BUILD);
        graphExporter.export(routeMap);
    }

    @Test
    public void testExportFullRadialTree() throws IOException {
        IGraphExporter graphExporter = new FullRadialTreeExporter(FOLDER_BUILD);
        graphExporter.export(routeMap);
    }

}
