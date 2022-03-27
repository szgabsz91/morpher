package com.github.szgabsz91.morpher.systems.impl.graphs.exporters.forcedirectedgraph;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class PartialForceDirectedGraphExporter extends AbstractForceDirectedGraphExporter {

    private static final String FILENAME = "partial-force-directed-graph.html";

    public PartialForceDirectedGraphExporter(Path buildFolder) {
        super(Paths.get(buildFolder.toAbsolutePath().toString(), FILENAME));
    }

    @Override
    protected Set<List<String>> filterRoutes(Map<List<String>, Long> routeMap) {
        TreeNode startNode = new TreeNode(START);
        TreeNode nodeToProcess = startNode;

        while (nodeToProcess != null) {
            nodeToProcess = nodeToProcess.process(routeMap);
        }

        return startNode.getLeaves()
                .stream()
                .map(leaf -> {
                    List<String> affixTypes = new ArrayList<>();
                    TreeNode currentNode = leaf;
                    while (!currentNode.affixType.equals(START)) {
                        affixTypes.add(0, currentNode.affixType);
                        currentNode = currentNode.parent;
                    }
                    return affixTypes;
                })
                .collect(toUnmodifiableSet());
    }

    private static class TreeNode {

        private TreeNode parent;
        private TreeNode winnerChild;
        private TreeNode loserChild1;
        private TreeNode loserChild2;
        private String affixType;

        private TreeNode(String affixType) {
            this.affixType = affixType;
        }

        private void setWinnerChild(TreeNode winnerChild) {
            this.winnerChild = winnerChild;
            winnerChild.parent = this;
        }

        private void setLoserChild1(TreeNode loserChild1) {
            this.loserChild1 = loserChild1;
            loserChild1.parent = this;
        }

        private void setLoserChild2(TreeNode loserChild2) {
            this.loserChild2 = loserChild2;
            loserChild2.parent = this;
        }

        private TreeNode process(Map<List<String>, Long> routeMap) {
            if (this.affixType.equals(START)) {
                processStart(routeMap);
            }
            else {
                processNormal(routeMap);
            }

            return this.winnerChild;
        }

        private void processStart(Map<List<String>, Long> routeMap) {
            Map<String, Long> posRelativeFrequencyMap = new HashMap<>();
            routeMap
                    .forEach((affixTypes, relativeFrequency) -> {
                        String pos = affixTypes.get(0);
                        Long existingRelativeFrequency = posRelativeFrequencyMap.computeIfAbsent(pos, k -> 0L);
                        posRelativeFrequencyMap.put(pos, existingRelativeFrequency + relativeFrequency);
                    });
            List<Map.Entry<String, Long>> entryList = posRelativeFrequencyMap
                    .entrySet()
                    .stream()
                    .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed())
                    .limit(3L)
                    .toList();
            if (entryList.size() >= 1) {
                this.setWinnerChild(new TreeNode(entryList.get(0).getKey()));
            }
            if (entryList.size() >= 2) {
                this.setLoserChild1(new TreeNode(entryList.get(1).getKey()));
            }
            if (entryList.size() >= 3) {
                this.setLoserChild2(new TreeNode(entryList.get(2).getKey()));
            }
        }

        private void processNormal(Map<List<String>, Long> routeMap) {
            List<String> prefix = getPrefix();
            List<Map.Entry<List<String>, Long>> entryList = routeMap
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
                    .limit(3L)
                    .toList();
            if (entryList.size() >= 1) {
                this.setWinnerChild(new TreeNode(last(entryList.get(0).getKey())));
            }
            if (entryList.size() >= 2) {
                this.setLoserChild1(new TreeNode(last(entryList.get(1).getKey())));
            }
            if (entryList.size() >= 3) {
                this.setLoserChild2(new TreeNode(last(entryList.get(2).getKey())));
            }
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
            return this.winnerChild == null && this.loserChild1 == null && this.loserChild2 == null;
        }

        private Set<TreeNode> getLeaves() {
            Set<TreeNode> leaves = new HashSet<>();
            this.process(treeNode -> {
                if (treeNode.isLeaf()) {
                    leaves.add(treeNode);
                }
            });
            return leaves;
        }

        private void process(Consumer<TreeNode> consumer) {
            consumer.accept(this);
            if (this.winnerChild != null) {
                this.winnerChild.process(consumer);
            }
            if (this.loserChild1 != null) {
                this.loserChild1.process(consumer);
            }
            if (this.loserChild2 != null) {
                this.loserChild2.process(consumer);
            }
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

            if (node.winnerChild != null) {
                toString(node.winnerChild, sb, level + 1);
            }

            if (node.loserChild1 != null) {
                toString(node.loserChild1, sb, level + 1);
            }

            if (node.loserChild2 != null) {
                toString(node.loserChild2, sb, level + 1);
            }
        }

    }

}
