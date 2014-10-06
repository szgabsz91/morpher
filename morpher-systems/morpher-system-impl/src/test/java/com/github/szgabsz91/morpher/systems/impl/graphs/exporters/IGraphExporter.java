package com.github.szgabsz91.morpher.systems.impl.graphs.exporters;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IGraphExporter {

    String START = "START";

    void export(Map<List<String>, Long> routeMap) throws IOException;

}
