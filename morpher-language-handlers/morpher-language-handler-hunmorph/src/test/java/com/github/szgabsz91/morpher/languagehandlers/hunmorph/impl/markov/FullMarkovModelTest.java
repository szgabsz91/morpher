package com.github.szgabsz91.morpher.languagehandlers.hunmorph.impl.markov;

import com.github.szgabsz91.morpher.core.model.AffixType;
import com.github.szgabsz91.morpher.languagehandlers.api.model.AffixTypeChain;
import com.github.szgabsz91.morpher.languagehandlers.api.model.ProbabilisticAffixType;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.protocolbuffers.MarkovModelMessage;
import com.github.szgabsz91.morpher.languagehandlers.hunmorph.protocolbuffers.MarkovModelRouteMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FullMarkovModelTest {

    @Nested
    public class MarkovModelTest {

        private FullMarkovModel markovModel;

        @BeforeEach
        public void setUp() {
            this.markovModel = new FullMarkovModel();
        }

        @Test
        public void testDefaultConstructorAndGetters() {
            FullMarkovModel.Node startNode = this.markovModel.getStartNode();
            assertThat(startNode).isNotNull();
            assertThat(startNode.getAffixType()).isEqualTo(IMarkovModel.START);

            Set<FullMarkovModel.Node> endNodes = this.markovModel.getNodes(IMarkovModel.END);
            assertThat(endNodes).isNotNull();
            assertThat(endNodes).isEmpty();
        }

        @Test
        public void testAddWithNonEmptyAffixTypeList() {
            /*
             * START:5
             *     X:3
             *         Z:2
             *             END:2
             *         W:1
             *             END:1
             *     Y:2
             *         W:1
             *             END:1
             *         END:1
             */
            this.createTestData().forEach(this.markovModel::add);
            this.assertMarkovModel();
        }

        @Test
        public void testAddWithEmptyAffixTypeList() {
            this.markovModel.add(List.of());

            FullMarkovModel.Node startNode = this.markovModel.getStartNode();
            assertThat(startNode.getChildren()).isEmpty();

            Set<FullMarkovModel.Node> endNodes = this.markovModel.getNodes(IMarkovModel.END);
            assertThat(endNodes.isEmpty());
        }

        @Test
        public void testIsAffixTypeChainValidWithValidOrder() {
            this.markovModel.add(List.of(AffixType.of("POS1"), AffixType.of("AFF1"), AffixType.of("AFF13")));
            this.markovModel.add(List.of(AffixType.of("POS1"), AffixType.of("AFF1"), AffixType.of("AFF12")));
            this.markovModel.add(List.of(AffixType.of("POS2"), AffixType.of("AFF1"), AffixType.of("AFF12")));

            List<AffixType> affixTypeChain = List.of(
                    AffixType.of("POS1"),
                    AffixType.of("AFF1"),
                    AffixType.of("AFF12")
            );
            boolean result = this.markovModel.isAffixTypeChainValid(affixTypeChain);
            assertThat(result).isTrue();
        }

        @Test
        public void testIsAffixTypeChainValidWithInvalidOrder() {
            this.markovModel.add(List.of(AffixType.of("POS1"), AffixType.of("AFF1"), AffixType.of("AFF13")));
            this.markovModel.add(List.of(AffixType.of("POS2"), AffixType.of("AFF1"), AffixType.of("AFF12")));
            this.markovModel.add(List.of(AffixType.of("POS2"), AffixType.of("AFF1"), AffixType.of("AFF12")));

            List<AffixType> affixTypeChain = List.of(
                    AffixType.of("POS1"),
                    AffixType.of("AFF1"),
                    AffixType.of("AFF14")
            );
            boolean result = this.markovModel.isAffixTypeChainValid(affixTypeChain);
            assertThat(result).isFalse();
        }

        @Test
        public void testSortAffixTypes() {
            /*
             * START:6
             *     NOUN:5
             *         ACC:2
             *             PLUR:1
             *                 END:1
             *             END:1
             *         PLUR:3
             *             ACC:2
             *                 END:2
             *             X:1
             *                 END:1
             *     VERB:1
             *         PAST:1
             *             END:1
             */
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>"), AffixType.of("<PLUR>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<X>")));
            this.markovModel.add(List.of(AffixType.of("/VERB"), AffixType.of("<PAST>")));

            List<AffixTypeChain> affixTypeChains = this.markovModel.sortAffixTypes(Set.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>")), affixType -> affixType.toString().startsWith("/"));
            assertThat(affixTypeChains).hasSize(2);

            AffixTypeChain affixTypeChain1 = affixTypeChains
                    .stream()
                    .filter(affixTypeChain -> affixTypeChain.getAffixTypes().stream().map(ProbabilisticAffixType::getAffixType).collect(toList()).get(1).equals(AffixType.of("<PLUR>")))
                    .findFirst()
                    .get();
            assertThat(affixTypeChain1.getAffixTypes()).containsExactly(
                    ProbabilisticAffixType.of(AffixType.of("/NOUN"), 5 / 6.0),
                    ProbabilisticAffixType.of(AffixType.of("<PLUR>"), 3 / 5.0),
                    ProbabilisticAffixType.of(AffixType.of("<CAS<ACC>>"), 2 / 3.0)
            );
            assertThat(affixTypeChain1.getProbability()).isEqualTo(1 / 3.0);

            AffixTypeChain affixTypeChain2 = affixTypeChains
                    .stream()
                    .filter(affixTypeChain -> affixTypeChain.getAffixTypes().stream().map(ProbabilisticAffixType::getAffixType).collect(toList()).get(1).equals(AffixType.of("<CAS<ACC>>")))
                    .findFirst()
                    .get();
            assertThat(affixTypeChain2.getAffixTypes()).containsExactly(
                    ProbabilisticAffixType.of(AffixType.of("/NOUN"), 5 / 6.0),
                    ProbabilisticAffixType.of(AffixType.of("<CAS<ACC>>"), 2 / 5.0),
                    ProbabilisticAffixType.of(AffixType.of("<PLUR>"), 0.5)
            );
            assertThat(affixTypeChain2.getProbability()).isCloseTo(1 / 6.0, Offset.strictOffset(0.000005));
        }

        @Test
        public void testCalculateProbabilitiesWithKnownRoute() {
            /*
             * START:6
             *     NOUN:5
             *         ACC:2
             *             PLUR:1
             *                 END:1
             *             END:1
             *         PLUR:3
             *             ACC:2
             *                 END:2
             *             X:1
             *                 END:1
             *     VERB:1
             *         PAST:1
             *             END:1
             */
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>"), AffixType.of("<PLUR>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<X>")));
            this.markovModel.add(List.of(AffixType.of("/VERB"), AffixType.of("<PAST>")));

            AffixTypeChain affixTypeChain = this.markovModel.calculateProbabilities(List.of(AffixType.of("<CAS<ACC>>"), AffixType.of("<PLUR>")));
            assertThat(affixTypeChain.getAffixTypes()).containsExactly(
                    ProbabilisticAffixType.of(AffixType.of("/NOUN"), 5 / 6.0),
                    ProbabilisticAffixType.of(AffixType.of("<CAS<ACC>>"), 2 / 5.0),
                    ProbabilisticAffixType.of(AffixType.of("<PLUR>"), 0.5)
            );
            assertThat(affixTypeChain.getProbability()).isCloseTo(1 / 6.0, Offset.strictOffset(0.000005));
        }

        @Test
        public void testCalculateProbabilitiesWithUnknownRoute() {
            /*
             * START:6
             *     NOUN:5
             *         ACC:2
             *             PLUR:1
             *                 END:1
             *             END:1
             *         PLUR:3
             *             ACC:2
             *                 END:2
             *             X:1
             *                 END:1
             *     VERB:1
             *         PAST:1
             *             END:1
             */
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>"), AffixType.of("<PLUR>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<X>")));
            this.markovModel.add(List.of(AffixType.of("/VERB"), AffixType.of("<PAST>")));

            AffixTypeChain affixTypeChain = this.markovModel.calculateProbabilities(List.of(AffixType.of("<CAS<ACC>>"), AffixType.of("<X>"), AffixType.of("<Y>")));
            assertThat(affixTypeChain.getAffixTypes()).containsExactly(
                    ProbabilisticAffixType.of(AffixType.of("/NOUN"), 5 / 6.0),
                    ProbabilisticAffixType.of(AffixType.of("<CAS<ACC>>"), 2 / 5.0),
                    ProbabilisticAffixType.of(AffixType.of("<X>"), 0.0),
                    ProbabilisticAffixType.of(AffixType.of("<Y>"), 0.0)
            );
            assertThat(affixTypeChain.getProbability()).isEqualTo(0.0);
        }

        @Test
        public void testCalculateProbabilitiesWithUnknownPos() {
            /*
             * START:6
             *     NOUN:5
             *         ACC:2
             *             PLUR:1
             *                 END:1
             *             END:1
             *         PLUR:3
             *             ACC:2
             *                 END:2
             *             X:1
             *                 END:1
             *     VERB:1
             *         PAST:1
             *             END:1
             */
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>"), AffixType.of("<PLUR>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<X>")));
            this.markovModel.add(List.of(AffixType.of("/VERB"), AffixType.of("<PAST>")));

            AffixTypeChain affixTypeChain = this.markovModel.calculateProbabilities(List.of(AffixType.of("<Y>")));
            assertThat(affixTypeChain.getAffixTypes()).containsExactly(
                    ProbabilisticAffixType.of(AffixType.of("<Y>"), 0.0)
            );
            assertThat(affixTypeChain.getProbability()).isEqualTo(0.0);
        }

        @Test
        public void testCalculateProbabilityWithUnknownRoute() {
            /*
             * START:6
             *     NOUN:5
             *         ACC:2
             *             PLUR:1
             *                 END:1
             *             END:1
             *         PLUR:3
             *             ACC:2
             *                 END:2
             *             X:1
             *                 END:1
             *     VERB:1
             *         PAST:1
             *             END:1
             */
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>"), AffixType.of("<PLUR>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<X>")));
            this.markovModel.add(List.of(AffixType.of("/VERB"), AffixType.of("<PAST>")));

            double result = this.markovModel.calculateProbability(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>"), AffixType.of("<X>"), AffixType.of("<Y>")));
            assertThat(result).isZero();
        }

        @Test
        public void testCalculateProbabilityWithKnownRoute() {
            /*
             * START:6
             *     NOUN:5
             *         ACC:2
             *             PLUR:1
             *                 END:1
             *             END:1
             *         PLUR:3
             *             ACC:2
             *                 END:2
             *             X:1
             *                 END:1
             *     VERB:1
             *         PAST:1
             *             END:1
             */
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>"), AffixType.of("<PLUR>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<X>")));
            this.markovModel.add(List.of(AffixType.of("/VERB"), AffixType.of("<PAST>")));

            double result = this.markovModel.calculateProbability(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>"), AffixType.of("<PLUR>")));
            assertThat(result).isEqualTo(0.5);
        }

        @Test
        public void testGetProbabilityOfPOSWithUnknownPOS() {
            AffixType pos = AffixType.of("/POS");
            ProbabilisticAffixType result = this.markovModel.getProbabilityOfPOS(pos);
            assertThat(result).isEqualTo(ProbabilisticAffixType.of(pos, 0.0));
        }

        @Test
        public void testGetProbabilityOfPOSWithKnownPOS() {
            this.markovModel.add(List.of(AffixType.of("/POS")));
            this.markovModel.add(List.of(AffixType.of("/POS2")));
            this.markovModel.add(List.of(AffixType.of("/POS2")));
            this.markovModel.add(List.of(AffixType.of("/POS3")));

            AffixType pos = AffixType.of("/POS");
            ProbabilisticAffixType result = this.markovModel.getProbabilityOfPOS(pos);
            assertThat(result).isEqualTo(ProbabilisticAffixType.of(pos, 0.25));
        }

        @Test
        public void testGetCandidates() {
            /*
             * START:-1
             *     X:3
             *         Z:1
             *             END:1
             *         W:6
             *             A:1
             *                 END:1
             *             B:2
             *                 END:2
             *             C:3
             *                 END:3
             */
            this.markovModel.add(List.of(AffixType.of("X"), AffixType.of("Z")));
            this.markovModel.add(List.of(AffixType.of("X"), AffixType.of("W"), AffixType.of("A")));
            this.markovModel.add(List.of(AffixType.of("X"), AffixType.of("W"), AffixType.of("B")));
            this.markovModel.add(List.of(AffixType.of("X"), AffixType.of("W"), AffixType.of("B")));
            this.markovModel.add(List.of(AffixType.of("X"), AffixType.of("W"), AffixType.of("C")));
            this.markovModel.add(List.of(AffixType.of("X"), AffixType.of("W"), AffixType.of("C")));
            this.markovModel.add(List.of(AffixType.of("X"), AffixType.of("W"), AffixType.of("C")));

            List<ProbabilisticAffixType> candidates = this.markovModel.getCandidates(List.of(AffixType.of("X"), AffixType.of("W")));
            assertThat(candidates).containsExactlyInAnyOrder(
                    ProbabilisticAffixType.of(AffixType.of("C"), 0.5),
                    ProbabilisticAffixType.of(AffixType.of("B"), 1 / 3.0),
                    ProbabilisticAffixType.of(AffixType.of("A"), 1 / 6.0)
            );
        }

        @Test
        public void testGetCandidatesWithUnknownRoute() {
            /*
             * START:5
             *     X:3
             *         Z:2
             *             END:2
             *         W:1
             *             END:1
             *     Y:2
             *         W:1
             *             END:1
             *         END:1
             */
            this.createTestData().forEach(this.markovModel::add);

            List<ProbabilisticAffixType> candidates = this.markovModel.getCandidates(List.of(AffixType.of("X"), AffixType.of("A")));
            assertThat(candidates).isEmpty();
        }

        @Test
        public void testGetRoutesAndToMessageAndFromMessage() throws InvalidProtocolBufferException {
            /*
             * START:5
             *     X:3
             *         Z:2
             *             END:2
             *         W:1
             *             END:1
             *     Y:2
             *         W:1
             *             END:1
             *         END:1
             */
            this.createTestData().forEach(this.markovModel::add);

            Map<List<AffixType>, Long> routes = this.markovModel.getRoutes().entrySet()
                    .stream()
                    .map(entry -> {
                        List<AffixType> affixTypes = entry.getKey()
                                .stream()
                                .map(FullMarkovModel.Node::getAffixType)
                                .collect(toList());
                        long relativeFrequency = entry.getValue();
                        return Map.entry(affixTypes, relativeFrequency);
                    })
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
            assertThat(routes).containsOnly(
                    Map.entry(List.of(AffixType.of("X"), AffixType.of("Z")), 2L),
                    Map.entry(List.of(AffixType.of("X"), AffixType.of("W")), 1L),
                    Map.entry(List.of(AffixType.of("Y")), 1L),
                    Map.entry(List.of(AffixType.of("Y"), AffixType.of("W")), 1L)
            );

            MarkovModelMessage message = this.markovModel.toMessage();
            assertThat(message.getRoutesList()).containsExactlyInAnyOrder(
                    MarkovModelRouteMessage.newBuilder()
                            .addAllAffixTypes(List.of("X", "Z"))
                            .setRelativeFrequency(2L)
                            .build(),
                    MarkovModelRouteMessage.newBuilder()
                            .addAllAffixTypes(List.of("X", "W"))
                            .setRelativeFrequency(1L)
                            .build(),
                    MarkovModelRouteMessage.newBuilder()
                            .addAllAffixTypes(List.of("Y"))
                            .setRelativeFrequency(1L)
                            .build(),
                    MarkovModelRouteMessage.newBuilder()
                            .addAllAffixTypes(List.of("Y", "W"))
                            .setRelativeFrequency(1L)
                            .build()
            );

            IMarkovModel markovModel = new FullMarkovModel();
            markovModel.fromMessage(Any.pack(message));
            this.assertMarkovModel();
        }

        @Test
        public void testFromMessageWithInvalidProtocolBuffer() {
            Any message = Any.pack(Any.pack(MarkovModelMessage.newBuilder().build()));
            InvalidProtocolBufferException exception = assertThrows(InvalidProtocolBufferException.class, () -> this.markovModel.fromMessage(message));
            assertThat(exception).hasMessage("The provided message is not a MarkovModelMessage: " + message);
        }

        @Test
        public void testToString() {
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<PLUR>"), AffixType.of("<CAS<ACC>>")));
            this.markovModel.add(List.of(AffixType.of("/NOUN"), AffixType.of("<CAS<ACC>>"), AffixType.of("<PLUR>")));

            String expected = String.format(
                    "START:2%n" +
                    "  /NOUN:2%n" +
                    "    <PLUR>:1%n" +
                    "      <CAS<ACC>>:1%n" +
                    "        END:1%n" +
                    "    <CAS<ACC>>:1%n" +
                    "      <PLUR>:1%n" +
                    "        END:1%n"
            );

            assertThat(this.markovModel).hasToString(expected);
        }

        private List<List<AffixType>> createTestData() {
            /*
             * START:5
             *     X:3
             *         Z:2
             *             END:2
             *         W:1
             *             END:1
             *     Y:2
             *         W:1
             *             END:1
             *         END:1
             */
            return List.of(
                    List.of(AffixType.of("X"), AffixType.of("Z")),
                    List.of(AffixType.of("X"), AffixType.of("Z")),
                    List.of(AffixType.of("X"), AffixType.of("W")),
                    List.of(AffixType.of("Y")),
                    List.of(AffixType.of("Y"), AffixType.of("W"))
            );
        }

        private void assertMarkovModel() {
            // START:5
            FullMarkovModel.Node start = this.markovModel.getStartNode();
            assertThat(start).isNotNull();
            assertThat(start.getAffixType()).isEqualTo(IMarkovModel.START);
            assertThat(start.getRelativeFrequency()).isEqualTo(5L);

            FullMarkovModel.Node x = assertNode("START -> X:3", start, AffixType.of("X"), 3L);
            FullMarkovModel.Node z = assertNode("START -> X:3 -> Z:2", x, AffixType.of("Z"), 2L);
            FullMarkovModel.Node end1 = assertNode("START -> X:3 -> Z:2 -> END:2", z, IMarkovModel.END, 2L);
            FullMarkovModel.Node w1 = assertNode("START -> X:3 -> W:1", x, AffixType.of("W"), 1L);
            FullMarkovModel.Node end2 = assertNode("START -> X:3 -> W:1 -> END:1", w1, IMarkovModel.END, 1L);
            FullMarkovModel.Node y = assertNode("START -> Y:2", start, AffixType.of("Y"), 2L);
            FullMarkovModel.Node w2 = assertNode("START -> Y:2 -> W:1", y, AffixType.of("W"), 1L);
            FullMarkovModel.Node end3 = assertNode("START -> Y:2 -> W:1 -> END:1", w2, IMarkovModel.END, 1L);
            FullMarkovModel.Node end4 = assertNode("START -> Y:2 -> END:1", y, IMarkovModel.END, 1L);

            assertThat(this.markovModel.getNodes(IMarkovModel.END)).containsExactlyInAnyOrder(end1, end2, end3, end4);
        }

        private FullMarkovModel.Node assertNode(String route, FullMarkovModel.Node parent, AffixType affixType, long relativeFrequency) {
            Optional<FullMarkovModel.Node> optionalChild = parent.getChild(affixType);
            assertThat(optionalChild)
                    .withFailMessage("No child found: %s", route)
                    .isPresent();
            FullMarkovModel.Node child = optionalChild.get();
            assertThat(child.getAffixType())
                    .withFailMessage("Invalid affix type for %s", route)
                    .isEqualTo(affixType);
            assertThat(child.getRelativeFrequency())
                    .withFailMessage("Invalid relative frequency for %s", route)
                    .isEqualTo(relativeFrequency);
            return child;
        }

    }

    @Nested
    public class NodeTest {

        @Test
        public void testConstructorAndGettersWithParent() {
            AffixType affixType = AffixType.of("AFF");
            long relativeFrequency = 100;
            FullMarkovModel.Node parent = new FullMarkovModel.Node(AffixType.of("AFF2"), 200, null);
            FullMarkovModel.Node node = new FullMarkovModel.Node(affixType, relativeFrequency, parent);
            assertThat(node.getAffixType()).isEqualTo(affixType);
            assertThat(node.getRelativeFrequency()).isEqualTo(relativeFrequency);
            assertThat(node.getParent()).hasValue(parent);
            assertThat(parent.getChildren()).containsExactly(Map.entry(affixType, node));
            assertThat(node.getChildren()).isEmpty();
        }

        @Test
        public void testConstructorAndGettersWithoutParent() {
            AffixType affixType = AffixType.of("AFF");
            long relativeFrequency = 100;
            FullMarkovModel.Node node = new FullMarkovModel.Node(affixType, relativeFrequency, null);
            assertThat(node.getAffixType()).isEqualTo(affixType);
            assertThat(node.getRelativeFrequency()).isEqualTo(relativeFrequency);
            assertThat(node.getParent()).isEmpty();
            assertThat(node.getChildren()).isEmpty();
        }

        @Test
        public void testStartAndGetters() {
            FullMarkovModel.Node node = FullMarkovModel.Node.start();
            assertThat(node.getAffixType()).isEqualTo(IMarkovModel.START);
            assertThat(node.getRelativeFrequency()).isEqualTo(0L);
            assertThat(node.getParent()).isEmpty();
            assertThat(node.getChildren()).isEmpty();
        }

        @Test
        public void testEndAndGetters() {
            FullMarkovModel.Node parent = new FullMarkovModel.Node(AffixType.of("AFF"), 2L, null);
            FullMarkovModel.Node node = FullMarkovModel.Node.end(parent);
            assertThat(node.getAffixType()).isEqualTo(IMarkovModel.END);
            assertThat(node.getRelativeFrequency()).isEqualTo(0L);
            assertThat(node.getParent()).hasValue(parent);
            assertThat(parent.getChildren()).containsExactly(Map.entry(IMarkovModel.END, node));
            assertThat(node.getChildren()).isEmpty();
        }

        @Test
        public void testCreateAndGetters() {
            AffixType affixType = AffixType.of("AFF");
            FullMarkovModel.Node parent = new FullMarkovModel.Node(AffixType.of("AFF2"), 2L, null);
            FullMarkovModel.Node node = FullMarkovModel.Node.create(affixType, parent);
            assertThat(node.getAffixType()).isEqualTo(affixType);
            assertThat(node.getRelativeFrequency()).isEqualTo(0L);
            assertThat(node.getParent()).hasValue(parent);
            assertThat(parent.getChildren()).containsExactly(Map.entry(affixType, node));
            assertThat(node.getChildren()).isEmpty();
        }

        @Test
        public void testIncrementRelativeFrequency() {
            long relativeFrequency = 1L;
            FullMarkovModel.Node node = new FullMarkovModel.Node(AffixType.of("AFF"), relativeFrequency, null);
            node.incrementRelativeFrequency();
            assertThat(node.getRelativeFrequency()).isEqualTo(relativeFrequency + 1L);
        }

        @Test
        public void testIncrementRelativeFrequencyBy() {
            long relativeFrequency = 1L;
            FullMarkovModel.Node node = new FullMarkovModel.Node(AffixType.of("AFF"), relativeFrequency, null);
            long newRelativeFrequency = 100L;
            node.incrementRelativeFrequencyBy(newRelativeFrequency);
            assertThat(node.getRelativeFrequency()).isEqualTo(relativeFrequency + newRelativeFrequency);
        }

        @Test
        public void testGetChildWithEmptyResult() {
            FullMarkovModel.Node node = new FullMarkovModel.Node(AffixType.of("AFF"), 1L, null);
            Optional<FullMarkovModel.Node> optionalChild = node.getChild(AffixType.of("AFF"));
            assertThat(optionalChild).isEmpty();
        }

        @Test
        public void testGetChildWithNonEmptyResult() {
            FullMarkovModel.Node node = new FullMarkovModel.Node(AffixType.of("AFF"), 1L, null);
            AffixType affixType = AffixType.of("AFF2");
            FullMarkovModel.Node child = new FullMarkovModel.Node(affixType, 1L, node);
            Optional<FullMarkovModel.Node> optionalChild = node.getChild(affixType);
            assertThat(optionalChild).hasValue(child);
        }

        @Test
        public void testCompareToWithNegativeResult() {
            FullMarkovModel.Node node1 = new FullMarkovModel.Node(AffixType.of("AFF"), 2L, null);
            FullMarkovModel.Node node2 = new FullMarkovModel.Node(AffixType.of("AFF"), 1L, null);
            int result = node1.compareTo(node2);
            assertThat(result).isNegative();
        }

        @Test
        public void testCompareToWithZeroResult() {
            FullMarkovModel.Node node1 = new FullMarkovModel.Node(AffixType.of("AFF"), 1L, null);
            FullMarkovModel.Node node2 = new FullMarkovModel.Node(AffixType.of("AFF"), 1L, null);
            int result = node1.compareTo(node2);
            assertThat(result).isZero();
        }

        @Test
        public void testCompareToWithPositiveResult() {
            FullMarkovModel.Node node1 = new FullMarkovModel.Node(AffixType.of("AFF"), 1L, null);
            FullMarkovModel.Node node2 = new FullMarkovModel.Node(AffixType.of("AFF"), 2L, null);
            int result = node1.compareTo(node2);
            assertThat(result).isPositive();
        }

        @Test
        public void testEquals() {
            FullMarkovModel.Node node1 = new FullMarkovModel.Node(AffixType.of("AFF"), 1L, FullMarkovModel.Node.start());
            FullMarkovModel.Node node2 = new FullMarkovModel.Node(AffixType.of("AFF2"), 1L, FullMarkovModel.Node.start());
            FullMarkovModel.Node node3 = new FullMarkovModel.Node(AffixType.of("AFF"), 2L, FullMarkovModel.Node.start());
            FullMarkovModel.Node node4 = new FullMarkovModel.Node(AffixType.of("AFF"), 1L, FullMarkovModel.Node.create(AffixType.of("PARENT"), null));
            FullMarkovModel.Node node5 = new FullMarkovModel.Node(AffixType.of("AFF"), 1L, null);
            FullMarkovModel.Node node6 = new FullMarkovModel.Node(AffixType.of("AFF"), 1L, FullMarkovModel.Node.start());

            assertThat(node1).isEqualTo(node1);
            assertThat(node1).isNotEqualTo(null);
            assertThat(node1).isNotEqualTo("string");
            assertThat(node1).isNotEqualTo(node2);
            assertThat(node1).isNotEqualTo(node3);
            assertThat(node1).isNotEqualTo(node4);
            assertThat(node5).isNotEqualTo(node1);
            assertThat(node1).isEqualTo(node6);
        }

        @Test
        public void testHashCode() {
            FullMarkovModel.Node node = new FullMarkovModel.Node(AffixType.of("AFF"), 1L, FullMarkovModel.Node.start());
            int result = node.hashCode();
            int expected = node.getAffixType().hashCode();
            expected = 31 * expected + (int) (node.getRelativeFrequency() ^ (node.getRelativeFrequency() >>> 32));
            expected = 31 * expected + node.getParent().hashCode();
            assertThat(result).isEqualTo(expected);
        }

        @Test
        public void testToString() {
            FullMarkovModel.Node node = new FullMarkovModel.Node(AffixType.of("AFF"), 1L, FullMarkovModel.Node.start());
            assertThat(node).hasToString("Node[" +
                    "affixType=" + node.getAffixType() +
                    ", relativeFrequency=" + node.getRelativeFrequency() +
                    ", parent=" + node.getParent().get() +
                    ']'
            );
        }

    }

}
