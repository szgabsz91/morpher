package com.github.szgabsz91.morpher.methods.lattice.impl;

import com.github.szgabsz91.morpher.core.io.Serializer;
import com.github.szgabsz91.morpher.core.model.WordPair;
import com.github.szgabsz91.morpher.core.utils.Timer;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianAttributedCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.HungarianSimpleCharacterRepository;
import com.github.szgabsz91.morpher.methods.api.characters.repositories.ICharacterRepository;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.DoubleConsonantWordConverter;
import com.github.szgabsz91.morpher.methods.api.wordconverterts.IWordConverter;
import com.github.szgabsz91.morpher.methods.lattice.converters.LatticeConverter;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.ILatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.builders.MinimalLatticeBuilder;
import com.github.szgabsz91.morpher.methods.lattice.impl.nodes.Node;
import com.github.szgabsz91.morpher.methods.lattice.impl.rules.Rule;
import com.github.szgabsz91.morpher.methods.lattice.impl.testutils.IOUtils;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.ITrainingSetProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.IWordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.TrainingSetProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.WordPairProcessor;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.AttributeBasedCostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.impl.trainingsetprocessor.costcalculator.ICostCalculator;
import com.github.szgabsz91.morpher.methods.lattice.protocolbuffers.LatticeMessage;
import com.github.szgabsz91.morpher.methods.lattice.utils.ExcludeDuringBuild;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@ExcludeDuringBuild
public class LatticeStatisticsTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(HungarianSimpleCharacterRepository.get(), HungarianAttributedCharacterRepository.get())
                .map(characterRepository -> {
                    IWordConverter wordConverter = new DoubleConsonantWordConverter();
                    ICostCalculator costCalculator = new AttributeBasedCostCalculator();
                    IWordPairProcessor wordPairProcessor = new WordPairProcessor.Builder()
                            .characterRepository(characterRepository)
                            .wordConverter(wordConverter)
                            .costCalculator(costCalculator)
                            .build();
                    ITrainingSetProcessor trainingSetProcessor = new TrainingSetProcessor(wordPairProcessor);
                    ILatticeBuilder latticeBuilder = new MinimalLatticeBuilder(characterRepository, wordConverter);
                    LatticeConverter latticeConverter = new LatticeConverter();
                    latticeConverter.setCharacterRepository(characterRepository);
                    latticeConverter.setWordConverter(wordConverter);
                    Serializer<Lattice, LatticeMessage> serializer = new Serializer<>(latticeConverter, latticeBuilder.getLattice());
                    String filename = "build/build-statistics-" + characterRepository.getClass().getSimpleName().toLowerCase() + ".csv";
                    try {
                        BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));
                        return Arguments.of(
                                characterRepository,
                                writer,
                                trainingSetProcessor,
                                latticeBuilder,
                                serializer
                        );
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void test(
            ICharacterRepository characterRepository,
            BufferedWriter writer,
            ITrainingSetProcessor trainingSetProcessor,
            ILatticeBuilder latticeBuilder,
            Serializer<Lattice, LatticeMessage> serializer) throws IOException {
        Timer timer = new Timer();

        writer.write("Number of Input Word Pairs;Number of Nodes;Build Time [s];Node with Most Children;Number of Most Children;Longest Path;Average Search Time [s];Serialization Time [s];Deserialization Time [s];File Size [byte]");
        writer.newLine();

        for (int wordPairCount = 100; ; wordPairCount += 100) {
            List<WordPair> wordPairs = LatticeTestHelper.getList(wordPairCount);
            Set<Rule> rules = trainingSetProcessor.induceRules(wordPairs);

            long startTime = System.nanoTime();
            latticeBuilder.addRules(rules);
            long endTime = System.nanoTime();
            Lattice originalLattice = latticeBuilder.getLattice();

            Path file = IOUtils.getRandomFile();
            timer.start();
            serializer.serialize(originalLattice, file);
            timer.stop();
            double serializationTime = timer.getSeconds();
            timer.reset();
            timer.start();
            Lattice lattice = serializer.deserialize(file);
            timer.stop();
            double deserializationTime = timer.getSeconds();
            long fileSize = Files.size(file);

            double buildTimeInSeconds = (endTime - startTime) / 1_000_000_000.0;
            int size = lattice.size();
            Node nodeWithMostChildren = lattice.getNodeWithMostChildren();
            int mostChildrenCount = nodeWithMostChildren.getChildren().size();
            long longestPath = lattice.getLeafLevel();

            double averageSearchTime = LatticeTestHelper.testLatticeWithWordPairs(lattice, wordPairs);

            writer.write(String.valueOf(wordPairCount));
            writer.write(';');
            writer.write(String.valueOf(size));
            writer.write(';');
            writer.write(String.valueOf(buildTimeInSeconds));
            writer.write(';');
            writer.write(nodeWithMostChildren.toString());
            writer.write(';');
            writer.write(String.valueOf(mostChildrenCount));
            writer.write(';');
            writer.write(String.valueOf(longestPath));
            writer.write(';');
            writer.write(String.valueOf(averageSearchTime));
            writer.write(';');
            writer.write(String.valueOf(serializationTime));
            writer.write(';');
            writer.write(String.valueOf(deserializationTime));
            writer.write(';');
            writer.write(String.valueOf(fileSize));
            writer.newLine();
            writer.flush();
        }
    }

}
