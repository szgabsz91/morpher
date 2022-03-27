package com.github.szgabsz91.morpher.transformationengines.lattice.impl;

import com.github.szgabsz91.morpher.core.model.Word;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.core.utils.Timer;
import com.github.szgabsz91.morpher.transformationengines.api.utils.TrainingDataRetriever;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.ZeroNode;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.UnitNode;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.transformationengines.lattice.impl.rules.transformations.ITransformation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class LatticeTestHelper {

    private static final int WINDOW_SIZE = 100;

    public static List<WordPair> getWindow(int index) throws IOException {
        return getList((index - 1) * WINDOW_SIZE, WINDOW_SIZE);
    }

    static List<WordPair> getList(int limit) throws IOException {
        return getList(0, limit);
    }

    private static List<WordPair> getList(int skip, int limit) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(TrainingDataRetriever.getTrainingDataFile("CAS(ACC).csv"))) {
            Map<Word, List<WordPair>> mapping = reader
                    .lines()
                    .skip(skip)
                    .limit(limit)
                    .map(line -> line.split(","))
                    .map(lineParts -> WordPair.of(lineParts[0], lineParts[1]))
                    .collect(groupingBy(WordPair::getLeftWord));

            return mapping.values()
                    .stream()
                    .map(list -> list.get(0))
                    .toList();
        }
    }

    public static double testLatticeWithWordPairs(Lattice lattice, List<WordPair> wordPairs) {
        Timer timer = new Timer();
        for (WordPair wordPair : wordPairs) {
            Word input = wordPair.getLeftWord();
            Word expected = wordPair.getRightWord();
            Node node = timer.measure(() -> lattice.match(input));
            assertSelectedNode(lattice, node);
            Rule rule = node.getRule();
            Word output = rule.transform(input);
            assertThat(output)
                    .withFailMessage(String.format(
                            "Word Pair #%d is %s: should have been %s but was %s",
                            wordPairs.indexOf(wordPair), wordPair.getLeftWord(), wordPair.getRightWord(), output
                    ))
                    .isEqualTo(expected);
        }
        return timer.getSeconds() / wordPairs.size();
    }

    static void print(Lattice lattice, Path path) throws IOException {
        LatticeWalker walker = new LatticeWalker(lattice);

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            walker.processNodesOnce(node -> {
                try {
                    print(writer, node);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    fail("IOException occurred");
                }
            });

            print(writer, lattice.getZeroNode());
        }
    }

    private static void print(BufferedWriter writer, Node node) throws IOException {
        print(writer, node, 0);

        writer.write("  Parents (");
        writer.write(String.valueOf(node.getParents().size()));
        writer.write(')');
        writer.newLine();
        for (Node parent : node.getParents()) {
            print(writer, parent, 1);
        }

        writer.write("  Children (");
        writer.write(String.valueOf(node.getChildren().size()));
        writer.write(')');
        writer.newLine();
        for (Node child : node.getChildren()) {
            print(writer, child, 1);
        }

        writer.newLine();
        writer.write("==================");
        writer.newLine();
        writer.newLine();
    }

    private static void print(BufferedWriter writer, Node node, int level) throws IOException {
        String whitespace = whitespace(level);

        if (node instanceof UnitNode || node instanceof ZeroNode) {
            writer.write(whitespace);
            writer.write(node.toString());
            writer.newLine();
            return;
        }

        writer.write(whitespace);
        writer.write("- Pattern: ");
        writer.write(node.getRule().getContext().getPrefix().toString());
        writer.write(" | ");
        writer.write(node.getRule().getContext().getCore().toString());
        writer.write(" | ");
        writer.write(node.getRule().getContext().getPostfix().toString());
        writer.write(" | ");
        writer.write(node.getRule().getContext().getFrontPosition().toString());
        writer.write(" | ");
        writer.write(node.getRule().getContext().getBackPosition().toString());
        writer.newLine();
        writer.write(whitespace);
        writer.write("  Transformations: ");
        writer.write(node.getRule().getTransformations().get(0).toString());
        for (int i = 1; i < node.getRule().getTransformations().size(); i++) {
            writer.write(" | ");
            ITransformation transformation = node.getRule().getTransformations().get(i);
            writer.write(transformation.toString());
        }
        if (node.isInconsistent()) {
            writer.newLine();
            writer.write(whitespace);
            writer.write("  inconsistent");
        }
        writer.newLine();
    }

    static void printVerbose(BufferedWriter writer, Node node, int level) throws IOException {
        String whitespace = whitespace(level);

        if (node instanceof UnitNode || node instanceof ZeroNode) {
            writer.write(whitespace);
            writer.write(node.toString());
            writer.newLine();
            return;
        }

        writer.write(whitespace);
        writer.write("- Pattern: ");
        writer.write(node.getPattern().toString());
        writer.newLine();
        writer.write(whitespace);
        writer.write("  Rule:");
        writer.newLine();
        writer.write(whitespace);
        writer.write("      Context:");
        writer.newLine();
        writer.write(whitespace);
        writer.write("          Prefix: ");
        writer.write(node.getRule().getContext().getPrefix().toString());
        writer.newLine();
        writer.write(whitespace);
        writer.write("          Core: ");
        writer.write(node.getRule().getContext().getCore().toString());
        writer.newLine();
        writer.write(whitespace);
        writer.write("          Postfix: ");
        writer.write(node.getRule().getContext().getPostfix().toString());
        writer.newLine();
        writer.write(whitespace);
        writer.write("          Front Position: ");
        writer.write(node.getRule().getContext().getFrontPosition().toString());
        writer.newLine();
        writer.write(whitespace);
        writer.write("          Back Position: ");
        writer.write(node.getRule().getContext().getBackPosition().toString());
        writer.newLine();
        writer.write(whitespace);
        writer.write("      Inconsistent: ");
        writer.write(node.isInconsistent() ? "true" : "false");
        writer.newLine();
        writer.write(whitespace);
        writer.write("      Transformations: ");
        writer.newLine();
        for (ITransformation transformation : node.getRule().getTransformations()) {
            writer.write(whitespace);
            writer.write("          ");
            writer.write(transformation.toString());
            writer.newLine();
        }
    }

    private static String whitespace(int level) {
        StringBuilder sb = new StringBuilder();
        if (level > 0) {
            sb.append("  ");
        }
        for (int i = 0; i < 4 * level; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private static void assertSelectedNode(Lattice lattice, Node node) {
        boolean atLeastOneParentIsInconsistent = node.getParents()
                .stream()
                .anyMatch(Node::isInconsistent);
        assertThat(atLeastOneParentIsInconsistent).isTrue();
        boolean descendantsAreConsistent = collectDescendants(lattice, node)
                .stream()
                .allMatch(Node::isConsistent);
        assertThat(descendantsAreConsistent).isTrue();
        assertThat(node.isConsistent()).isTrue();
    }

    private static Set<Node> collectDescendants(Lattice lattice, Node node) {
        Set<Node> result = new HashSet<>();
        LatticeWalker latticeWalker = new LatticeWalker(lattice);
        latticeWalker.walk(
                node,
                n -> true,
                Node::getChildren,
                ProcessingType.GENERAL_PROCESSING,
                lattice.getNextProcessingStatus(ProcessingType.GENERAL_PROCESSING),
                result::add
        );
        return result;
    }

}
