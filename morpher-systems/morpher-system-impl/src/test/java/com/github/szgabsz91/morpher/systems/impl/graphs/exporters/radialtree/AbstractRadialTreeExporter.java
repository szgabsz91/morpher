package com.github.szgabsz91.morpher.systems.impl.graphs.exporters.radialtree;

import com.github.szgabsz91.morpher.systems.impl.graphs.exporters.IGraphExporter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@SuppressWarnings("PMD.LoggerIsNotStaticFinal")
public abstract class AbstractRadialTreeExporter implements IGraphExporter {

    private static final String TEMPLATE_PATH = "templates/radial-tree.html.vm";

    protected static final String PARAMETER_WIDTH = "width";
    protected static final String PARAMETER_HEIGHT = "height";
    protected static final String PARAMETER_TRANSLATE_X = "translateX";
    protected static final String PARAMETER_TRANSLATE_Y = "translateY";
    protected static final String PARAMETER_TEXT_NEEDED = "textNeeded";

    private final Path htmlFile;
    private final Logger logger;

    public AbstractRadialTreeExporter(Path htmlFile, Class<? extends AbstractRadialTreeExporter> clazz) {
        this.htmlFile = htmlFile;
        this.logger = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void export(Map<List<String>, Long> originalRouteMap) throws IOException {
        Map<List<String>, Long> routeMap = this.preprocessRoutes(originalRouteMap);

        TreeNode startNode = new TreeNode(START, 0L);
        List<TreeNode> nodesToProcess = List.of(startNode);

        while (!nodesToProcess.isEmpty()) {
            nodesToProcess = nodesToProcess
                    .stream()
                    .map(node -> node.addRoutes(routeMap))
                    .flatMap(Collection::stream)
                    .collect(toList());
        }

        startNode.process(this::preprocessTree);

        List<TreeNode> leaves = new ArrayList<>();
        startNode.process(treeNode -> {
            if (treeNode.isLeaf()) {
                leaves.add(treeNode);
            }
        });
        Set<List<String>> routes = leaves
                .stream()
                .map(leaf -> {
                    List<String> affixTypes = new ArrayList<>();
                    TreeNode currentNode = leaf;
                    while (currentNode != null) {
                        affixTypes.add(0, currentNode.affixType);
                        currentNode = currentNode.parent;
                    }
                    return affixTypes;
                })
                .collect(toSet());
        this.logger.info("Generated {} routes", routes.size());
        int maximumDeth = routes
                .stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);
        this.logger.info("Maximum depth: {}", maximumDeth);
        JsonArray nodes = generateNodes(routes);

        Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        Velocity.init();
        VelocityContext context = new VelocityContext();
        context.put("nodes", nodes.toString());
        this.getVelocityParameters().forEach(context::put);
        Template template = Velocity.getTemplate(TEMPLATE_PATH);
        StringWriter templateWriter = new StringWriter();
        template.merge(context, templateWriter);
        String html = templateWriter.toString();

        try (BufferedWriter writer = Files.newBufferedWriter(this.htmlFile, StandardCharsets.UTF_8)) {
            writer.write(html);
            writer.newLine();
        }

        Desktop
                .getDesktop()
                .browse(this.htmlFile.toUri());
    }

    protected Map<List<String>, Long> preprocessRoutes(Map<List<String>, Long> routeMap) {
        return routeMap;
    }

    protected void preprocessTree(TreeNode startNode) {

    }

    protected abstract Map<String, Object> getVelocityParameters();

    private JsonArray generateNodes(Set<List<String>> routes) {
        JsonArray nodes = new JsonArray();
        routes
                .stream()
                .flatMap(route -> {
                    return IntStream.range(1, route.size() + 1)
                            .mapToObj(toIndex -> route.subList(0, toIndex));
                })
                .distinct()
                .map(id -> {
                    JsonObject node = new JsonObject();
                    node.addProperty("id", String.join(".", id));
                    return node;
                })
                .forEach(nodes::add);
        return nodes;
    }

    protected static class TreeNode {

        private TreeNode parent;
        private List<TreeNode> children;
        private String affixType;
        private long relativeFrequency;

        private TreeNode(String affixType, long relativeFrequency) {
            this.parent = null;
            this.children = new ArrayList<>();
            this.affixType = affixType;
            this.relativeFrequency = relativeFrequency;
        }

        protected List<TreeNode> getChildren() {
            return children;
        }

        private void addChild(TreeNode treeNode) {
            this.children.add(treeNode);
            treeNode.parent = this;
        }

        protected void setChildren(List<TreeNode> children) {
            this.children = children;
        }

        protected int getLevel() {
            int level = -1;
            TreeNode currentNode = this;

            while (currentNode != null) {
                level++;
                currentNode = currentNode.parent;
            }

            return level;
        }

        protected String getAffixType() {
            return affixType;
        }

        protected long getRelativeFrequency() {
            return relativeFrequency;
        }

        private void process(Consumer<TreeNode> consumer) {
            consumer.accept(this);
            this.children.forEach(child -> child.process(consumer));
        }

        private List<TreeNode> addRoutes(Map<List<String>, Long> routeMap) {
            if (this.affixType.equals(START)) {
                processStart(routeMap);
            }
            else {
                processNormal(routeMap);
            }

            return this.children;
        }

        private void processStart(Map<List<String>, Long> routeMap) {
            Map<String, Long> posRelativeFrequencyMap = new HashMap<>();
            routeMap
                    .forEach((affixTypes, relativeFrequency) -> {
                        String pos = affixTypes.get(0);
                        Long existingRelativeFrequency = posRelativeFrequencyMap.computeIfAbsent(pos, k -> 0L);
                        posRelativeFrequencyMap.put(pos, existingRelativeFrequency + relativeFrequency);
                    });
            posRelativeFrequencyMap
                    .entrySet()
                    .stream()
                    .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed())
                    .forEach(entry -> {
                        this.addChild(new TreeNode(entry.getKey(), entry.getValue()));
                    });
        }

        private void processNormal(Map<List<String>, Long> routeMap) {
            List<String> prefix = getPrefix();
            routeMap
                    .entrySet()
                    .stream()
                    .filter(entry -> {
                        List<String> affixTypes = entry.getKey();
                        if (prefix.size() > affixTypes.size()) {
                            return false;
                        }
                        List<String> affixTypesPrefix = affixTypes.subList(0, prefix.size());
                        return affixTypesPrefix.equals(prefix) &&
                                affixTypes.size() == prefix.size() + 1;
                    })
                    .sorted(Comparator.<Map.Entry<List<String>, Long>>comparingLong(Map.Entry::getValue).reversed())
                    .forEach(entry -> {
                        this.addChild(new TreeNode(last(entry.getKey()), entry.getValue()));
                    });
        }

        private List<String> getPrefix() {
            List<String> prefix = new ArrayList<>();
            TreeNode currentNode = this;
            while (!currentNode.affixType.equals(START)) {
                prefix.add(0, currentNode.affixType);
                currentNode = currentNode.parent;
            }
            return prefix;
        }

        private static <T> T last(List<T> list) {
            return list.get(list.size() - 1);
        }

        private boolean isLeaf() {
            return this.children.isEmpty();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TreeNode treeNode = (TreeNode) o;
            return Objects.equals(parent, treeNode.parent) &&
                    Objects.equals(affixType, treeNode.affixType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parent, affixType);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(this, sb, 0);
            return sb.toString();
        }

        private void toString(TreeNode node, StringBuilder sb, int level) {
            for (int i = 0; i < level; i++) {
                sb.append("  ");
            }

            String newLine = System.getProperty("line.separator");
            sb.append(node.affixType);
            sb.append(newLine);

            this.children.forEach(child -> toString(child, sb, level + 1));
        }

    }

}
