package com.github.szgabsz91.morpher.systems.impl.graphs.exporters.radialtree;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class FullRadialTreeExporter extends AbstractRadialTreeExporter {

    private static final String FILENAME = "full-radial-tree.html";

    public FullRadialTreeExporter(Path buildFolder) {
        super(Paths.get(buildFolder.toAbsolutePath().toString(), FILENAME), FullRadialTreeExporter.class);
    }

    @Override
    protected Map<String, Object> getVelocityParameters() {
        return Map.of(
                PARAMETER_WIDTH, 790,
                PARAMETER_HEIGHT, 880,
                PARAMETER_TRANSLATE_X, 400,
                PARAMETER_TRANSLATE_Y, 440,
                PARAMETER_TEXT_NEEDED, false
        );
    }

}
