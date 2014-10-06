package com.github.szgabsz91.morpher.systems.impl.graphs.exporters.forcedirectedgraph;

import com.github.szgabsz91.morpher.systems.impl.graphs.exporters.IGraphExporter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractForceDirectedGraphExporter implements IGraphExporter {

    private static final String TEMPLATE_PATH = "templates/force-directed-graph.html.vm";

    private final Path graphFile;

    AbstractForceDirectedGraphExporter(Path graphFile) {
        this.graphFile = graphFile;
    }

    @Override
    public void export(Map<List<String>, Long> routeMap) throws IOException {
        Set<List<String>> routes = filterRoutes(routeMap);

        JsonObject graphObject = generateGraph(routes);

        Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        Velocity.init();
        VelocityContext context = new VelocityContext();
        context.put("graph", graphObject.toString());
        Template template = Velocity.getTemplate(TEMPLATE_PATH);
        StringWriter templateWriter = new StringWriter();
        template.merge(context, templateWriter);
        String html = templateWriter.toString();

        try (BufferedWriter writer = Files.newBufferedWriter(this.graphFile, StandardCharsets.UTF_8)) {
            writer.write(html);
            writer.newLine();
        }

        Desktop
                .getDesktop()
                .browse(this.graphFile.toUri());
    }

    protected abstract Set<List<String>> filterRoutes(Map<List<String>, Long> routes);

    private static JsonObject generateGraph(Set<List<String>> routes) {
        JsonObject graph = new JsonObject();

        JsonArray nodes = generateNodes(routes);
        graph.add("nodes", nodes);

        JsonArray links = generateLinks(routes);
        graph.add("links", links);

        return graph;
    }

    private static JsonArray generateNodes(Set<List<String>> routes) {
        JsonArray nodes = new JsonArray();
        nodes.add(createNode(START));

        routes
                .stream()
                .flatMap(Collection::stream)
                .distinct()
                .map(AbstractForceDirectedGraphExporter::createNode)
                .forEach(nodes::add);

        return nodes;
    }

    private static JsonObject createNode(String affixType) {
        JsonObject node = new JsonObject();
        node.addProperty("id", affixType);
        return node;
    }

    private static JsonArray generateLinks(Set<List<String>> routes) {
        JsonArray links = new JsonArray();

        routes.forEach(route -> {
            route.add(0, START);

            for (int i = 0; i < route.size() - 1; i++) {
                links.add(createLink(route.get(i), route.get(i + 1)));
            }
        });

        return links;
    }

    private static JsonObject createLink(String source, String target) {
        JsonObject link = new JsonObject();
        link.addProperty("source", source);
        link.addProperty("target", target);
        return link;
    }

}
