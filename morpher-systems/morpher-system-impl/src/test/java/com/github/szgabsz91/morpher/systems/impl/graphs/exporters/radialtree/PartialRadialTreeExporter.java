package com.github.szgabsz91.morpher.systems.impl.graphs.exporters.radialtree;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableMap;

public class PartialRadialTreeExporter extends AbstractRadialTreeExporter {

    private static final String FILENAME = "partial-radial-tree.html";
    private static final int MAXIMUM_NODE_LEVEL = 5;

    public PartialRadialTreeExporter(Path buildFolder) {
        super(Paths.get(buildFolder.toAbsolutePath().toString(), FILENAME), PartialRadialTreeExporter.class);
    }

    @Override
    protected Map<List<String>, Long> preprocessRoutes(Map<List<String>, Long> routeMap) {
        return routeMap.entrySet()
                .stream()
                .map(entry -> {
                    List<String> affixTypes = entry.getKey()
                            .stream()
                            .map(affixType -> {
                                return affixType
                                        .replaceAll("<", "")
                                        .replaceAll(">", "")
                                        .replaceAll("\\[", "")
                                        .replaceAll("]", "")
                                        .replaceAll("/", "")
                                        .replaceAll("_", "");
                            })
                            .toList();
                    long relativeFrequency = entry.getValue();
                    return Map.entry(affixTypes, relativeFrequency);
                })
                .collect(toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    protected void preprocessTree(TreeNode treeNode) {
        if (treeNode.getLevel() == MAXIMUM_NODE_LEVEL) {
            treeNode.getChildren().clear();
        }

        Stream<TreeNode> childStream = treeNode.getChildren()
                .stream()
                .sorted(Comparator.<TreeNode>comparingLong(TreeNode::getRelativeFrequency).reversed());
        if (!treeNode.getAffixType().equals(START)) {
            childStream = childStream.limit(10L);
        }
        treeNode.setChildren(childStream.toList());
    }

    @Override
    protected Map<String, Object> getVelocityParameters() {
        return Map.of(
                PARAMETER_WIDTH, 1100,
                PARAMETER_HEIGHT, 1100,
                PARAMETER_TRANSLATE_X, 570,
                PARAMETER_TRANSLATE_Y, 560,
                PARAMETER_TEXT_NEEDED, true
        );
    }

}
