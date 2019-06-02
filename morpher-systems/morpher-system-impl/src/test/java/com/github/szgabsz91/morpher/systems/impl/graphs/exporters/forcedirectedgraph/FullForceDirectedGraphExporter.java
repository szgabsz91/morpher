package com.github.szgabsz91.morpher.systems.impl.graphs.exporters.forcedirectedgraph;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FullForceDirectedGraphExporter extends AbstractForceDirectedGraphExporter {

    private static final String FILENAME = "full-force-directed-graph.html";

    public FullForceDirectedGraphExporter(Path buildFolder) {
        super(Paths.get(buildFolder.toAbsolutePath().toString(), FILENAME));
    }

    @Override
    protected Set<List<String>> filterRoutes(Map<List<String>, Long> routes) {
        return routes.keySet();
    }

}
