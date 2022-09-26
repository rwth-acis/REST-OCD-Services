package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import y.base.*;

import java.util.*;
import java.util.function.ToDoubleFunction;


/**
 * This class holds label propagation algorithmus with neighbor node influence by
 * Meilian Lu, Zhenglin Zhang, Zhihe Qu, and Yu Kang described in the paper
 * LPANNI: Overlapping Community Detection Using Label Propagation in Large-Scale Complex Networks
 * DOI: 10.1109/TKDE.2018.2866424
 */
public class LabelPropagationAlgorithmWithNeighborNodeInfluence implements
        OcdAlgorithm {

    /**
     * Maximum iteration number T.
     */
    private static int t = 100;

    /**
     * Parameter name
     */

    protected static final String MAX_ITERATION_NUMBER = "maximum iteration number";

    /**
     * Creates a instance of the algorithm.
     */
    public LabelPropagationAlgorithmWithNeighborNodeInfluence() {
    }

    @Override
    public CoverCreationType getAlgorithmType() {
        return CoverCreationType.LABEL_PROPAGATION_ALGORITHM_WITH_NEIGHBOR_NODE_INFLUENCE;
    }

    @Override
    public Set<GraphType> compatibleGraphTypes() {
        Set<GraphType> compatibilities = new HashSet<>();
        compatibilities.add(GraphType.WEIGHTED);
        return compatibilities;
    }

    /**
     * Setter for the algorithm parameters
     */

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
        if (parameters.containsKey(MAX_ITERATION_NUMBER)) {
            t = Integer.parseInt(parameters.get(MAX_ITERATION_NUMBER));
            if (t < 1) {
                throw new IllegalArgumentException();
            }
            parameters.remove(MAX_ITERATION_NUMBER);
        }
        if (parameters.size() > 0) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Getter for the algorithm parameters.
     */

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(MAX_ITERATION_NUMBER, Integer.toString(t));
        return parameters;
    }

    /**
     * Global variables to hold the NI, Sim and NNI values ​​for all nodes of a graph.
     */

    Map<Node, Double> niToNodes = new HashMap<>();
    MultiKeyMap<Integer, Double> simToNodesPair = new MultiKeyMap<>();
    MultiKeyMap<Integer, Double> nniToNodesPair = new MultiKeyMap<>();

    /**
     * Getter for the edges of a graph.
     */

    private Set<Edge> getEdges(CustomGraph graph) {
        return new HashSet<>(graph.getEdgeList());
    }

    /**
     * Getter for the nodes of a graph.
     */

    private Set<Node> getNodes(CustomGraph graph) {
        return new HashSet<>(new NodeList(graph.nodes()));
    }

    /**
     * Getter for the neighbors of a given node.
     */

    private Set<Node> getNeighbors(Node node) {
        return new HashSet<>(new NodeList(node.neighbors()));
    }

    /**
     * Calculates the k value for a node of a weighted graph.
     * This value is necessary for calculating NI in a weighted graph.
     *
     * @param graph the given graph
     * @param node  the node for which the k value needs to be calculated
     * @return the k value for the given node
     */

    protected double calculateKWeighted(CustomGraph graph, Node node) {
        Set<Node> neighbors = getNeighbors(node);
        double sum = 0.0;
        for (Node n : neighbors) {
            double weight = graph.getEdgeWeight(node.getEdgeFrom(n));
            sum += weight;
        }
        return sum;
    }

    /**
     * Calculates the NI values for all nodes of a weighted graph.
     *
     * @param graph the given graph
     */

    protected void calculateNIForWeighted(CustomGraph graph) {
        Set<Node> nodes = getNodes(graph);

        double minNI = graph.E() * graph.E();
        double maxNI = 0;

        for (Node curr : nodes) {
            Set<Node> neighbors = getNeighbors(curr);
            int triangleCount = 0;
            for (Node i : neighbors) {
                double weightI = graph.getEdgeWeight(curr.getEdgeFrom(i));
                for (Node j : neighbors) {
                    if ((i.index() < j.index()) && graph.containsEdge(i, j)) {
                        double weightJ = graph.getEdgeWeight(curr.getEdgeFrom(j));
                        double weightIJ = graph.getEdgeWeight(i.getEdgeFrom(j));
                        double result = (weightI + weightJ + weightIJ) / 3.0;
                        triangleCount += result;
                    }
                }
            }
            double ni = calculateKWeighted(graph, curr) + triangleCount;
            niToNodes.put(curr, ni);
            minNI = Math.min(minNI, ni);
            maxNI = Math.max(maxNI, ni);
        }
        for (Node curr : nodes) {
            double newNI = 0.5 + 0.5 * ((niToNodes.get(curr) - minNI) / (maxNI - minNI));
            niToNodes.put(curr, newNI);
        }
    }

    /**
     * Calculates the NI values for all nodes of a given unweighted graph.
     *
     * @param graph the given graph
     */

    protected void calculateNI(CustomGraph graph) {

        Set<Node> nodes = getNodes(graph);

        double minNI = 2 * graph.N();
        double maxNI = 0;

        for (Node curr : nodes) {
            Set<Node> neighbors = getNeighbors(curr);
            int triangleCount = 0;
            for (Node i : neighbors) {
                for (Node j : neighbors) {
                    if ((i.index() < j.index()) && graph.containsEdge(i, j)) {
                        triangleCount += 1;
                    }
                }
            }

            double ni = (curr.degree() / 2.0) + triangleCount;
            niToNodes.put(curr, ni);
            minNI = Math.min(minNI, ni);
            maxNI = Math.max(maxNI, ni);

        }

        for (Node curr : nodes) {
            double newNI = 0.5 + 0.5 * ((niToNodes.get(curr) - minNI) / (maxNI - minNI));
            niToNodes.put(curr, newNI);
        }
    }

    /**
     * Calculates the Sim values ​​for all nodes of the graph.
     *
     * @param graph the given graph
     */

    protected void calculateSim(CustomGraph graph) {
        MultiKeyMap<Integer, Double> sToNodesPair = new MultiKeyMap<>();
        Map<Integer, ArrayList<Double>> sToNode = new HashMap<>();
        Set<Edge> edges = getEdges(graph);
        double s;
        for (Edge edge : edges) {
            Node u = edge.source();
            Node v = edge.target();
            if (!sToNode.containsKey(u.index())) {
                sToNode.put(u.index(), new ArrayList<>());
            }

            if (graph.isWeighted()) {
                s = calculateSWeighted(graph, edge, u, v);
            } else {
                s = calculateS(graph, u, v);
            }
            sToNode.get(u.index()).add(s);
            sToNodesPair.put(u.index(), v.index(), s);
        }

        for (Edge edge : edges) {
            Node u = edge.source();
            Node v = edge.target();
            Double sumForU = 0.0;
            Double sumForV = 0.0;
            for (Double uValue : sToNode.get(u.index())) {
                sumForU += uValue;
            }
            for (Double vValue : sToNode.get(v.index())) {
                sumForV += vValue;
            }
            Double sim = sToNodesPair.get(v.index(), u.index()) / Math.sqrt(sumForU * sumForV);
            simToNodesPair.put(u.index(), v.index(), sim);

        }
    }

    /**
     * Calculates the s value for two nodes of an unweighted graph.
     *
     * @param graph the given graph
     * @param u     the source node of an edge
     * @param v     the target node of an edge
     * @return
     */

    private double calculateS(CustomGraph graph, Node u, Node v) {
        Set<Node> neighborsFromU = getNeighbors(u);
        double s;
        s = 1.0;
        for (Node i : neighborsFromU) {
            if (graph.containsEdge(i, v)) {
                s += 0.5;
            }
        }
        for (Node i : neighborsFromU) {
            if (i != v) {
                Set<Node> neighborsFromI = getNeighbors(i);
                for (Node j : neighborsFromI) {
                    if (graph.containsEdge(j, v) && (j != u)) {
                        s += 1d / 3;
                    }
                }
            }
        }
        return s;
    }

    /**
     * Calculates the s value for two nodes of an weighted graph.
     *
     * @param graph the given graph
     * @param edge  the given edge
     * @param u     the source node of the edge
     * @param v     the target node of the edge
     * @return
     */

    private double calculateSWeighted(CustomGraph graph, Edge edge, Node u, Node v) {
        Set<Node> neighborsFromU = getNeighbors(u);
        double s;
        s = graph.getEdgeWeight(edge);
        for (Node i : neighborsFromU) {
            if (graph.containsEdge(i, v)) {
                s += ((graph.getEdgeWeight(u.getEdgeFrom(i)) + graph.getEdgeWeight(i.getEdgeFrom(v))) / 2.0);
            }
        }
        for (Node i : neighborsFromU) {
            if (i != v) {
                Set<Node> neighborsFromI = getNeighbors(i);
                for (Node j : neighborsFromI) {
                    if (graph.containsEdge(j, v) && (j != u)) {
                        s += ((graph.getEdgeWeight(u.getEdgeFrom(i)) + graph.getEdgeWeight(i.getEdgeFrom(j)) + graph.getEdgeWeight(j.getEdgeFrom(v))) / 3.0);
                    }
                }
            }
        }
        return s;
    }

    /**
     * Calculates the Sim values ​​for all nodes of the graph.
     *
     * @param graph the given graph
     */

    protected void calculateNNI(CustomGraph graph) {
        Set<Node> nodes = getNodes(graph);
        for (Node u : nodes) {
            ArrayList<Double> simList = new ArrayList<>();
            Set<Node> neighborsFromU = getNeighbors(u);
            for (Node i : neighborsFromU) {
                simList.add(simToNodesPair.get(u.index(), i.index()));
            }

            double maxSim = Collections.max(simList);
            for (Node v : getNeighbors(u)) {
                double nni = Math.sqrt(niToNodes.get(v) * (simToNodesPair.get(v.index(), u.index()) / maxSim));
                nniToNodesPair.put(v.index(), u.index(), nni);
            }
        }
    }

    /**
     * Initializes all nodes of the given graph.
     *
     * @param graph   the given graph
     * @param initMap A map containing all nodes after initialization. The dominant label dominantC,
     *                the corresponding membership coefficient dominantB and the size of the current
     *                label set labelSetSize are stored for each node.
     */

    protected void initNodes(CustomGraph graph, Map<Integer, Map<String, Double>> initMap) {
        Set<Integer> nodes = graph.getNodeIds();
        for (Integer curr : nodes) {
            initMap.computeIfAbsent(curr, key -> {
                Map<String, Double> nodeMap = new HashMap<>();
                nodeMap.put("dominantC", Double.valueOf(curr));
                nodeMap.put("dominantB", 1.0);
                nodeMap.put("labelSetSize", 1.0);
                return nodeMap;
            });
//            if (!initMap.containsKey(curr)) {
//                //initMap.put(curr, Map.of("dominantC", Double.valueOf(curr), "dominantB", 1.0, "labelSetSize", 1.0));
//                Map<String, Double> nodeMap = new HashMap<>();
//                nodeMap.put("dominantC", Double.valueOf(curr));
//                nodeMap.put("dominantB", 1.0);
//                nodeMap.put("labelSetSize", 1.0);
//                initMap.put(curr, nodeMap);
//            }
        }
    }

    /**
     * Sorts the elements of a map in ascending order by value.
     *
     * @param map the map to be sorted
     * @return the sorted map
     */

    static SortedSet<Map.Entry<Node, Double>> entriesSortedByValues(Map<Node, Double> map) {
        SortedSet<Map.Entry<Node, Double>> sortedEntries = new TreeSet<>(
                Comparator.comparingDouble((ToDoubleFunction<Map.Entry<Node, Double>>) Map.Entry::getValue).thenComparingInt(e -> e.getKey().index())
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    /**
     * Clears the community matrix of zero entries.
     *
     * @param communities the matrix containing entries for all nodes and all communities.
     *                    If a node belongs to a community, the entry contains the membership coefficient.
     *                    If the node does not belong to the community, there is a zero in this entry.
     * @return cleaned matrix.
     */

    private Matrix formCommunities(Matrix communities) {
        Matrix finalCommunities = new Basic2DMatrix(communities.rows(), communities.columns());
        int currColumn = 0;
        for (int i = 0; i < communities.columns(); i++) {
            if (communities.getColumn(i).sum() > 0) {
                finalCommunities.setColumn(currColumn++, communities.getColumn(i));
            }
        }
        finalCommunities = finalCommunities.slice(0, 0, communities.rows(), currColumn);
        return finalCommunities;
    }

    /**
     * The main method, which represents the whole processing of the algorithm.
     *
     * @param graph An at least weakly connected graph whose community structure will be detected.
     * @return cover representing communities
     */

    @Override
    public Cover detectOverlappingCommunities(CustomGraph graph) throws
            OcdAlgorithmException, InterruptedException, OcdMetricException {
        Map<Integer, Map<String, Double>> dominantMap = new HashMap<>();
        Map<Integer, Map<Integer, Double>> communitiesMap = new HashMap<>();

        // The first phase of the algorithm. Calculate NI, Sim, NNI for all nodes of the given graph, sort the nodes.
        if (graph.isWeighted()) {
            calculateNIForWeighted(graph);
        } else {
            calculateNI(graph);
        }
        calculateSim(graph);
        calculateNNI(graph);
        initNodes(graph, dominantMap);
        SortedSet<Map.Entry<Node, Double>> sortedNodes = entriesSortedByValues(niToNodes);

        // The second phase of the algorithm. Label Propagation.
        int temp = 0;
        while (temp < t) {
            // If there are no changes of the label set in an iteration, the value remains false and the algorithm terminates.
            boolean change = false;
            // Updating labels in ascending order by NI.
            for (Map.Entry<Node, Double> entry : sortedNodes) {
                Node u = entry.getKey();
                Map<Integer, Double> bMap = new HashMap<>();
                Set<Node> neighbors = getNeighbors(u);
                for (Node neighbor : neighbors) {
                    Integer c = dominantMap.get(neighbor.index()).get("dominantC").intValue();
                    Double b = dominantMap.get(neighbor.index()).get("dominantB");
                    double bMapValue;
                    if (bMap.containsKey(c)) {
                        bMapValue = bMap.get(c) + b * nniToNodesPair.get(neighbor.index(), u.index());

                    } else {
                        bMapValue = b * nniToNodesPair.get(neighbor.index(), u.index());
                    }
                    bMap.put(c, bMapValue);
                }
                double totalBNNI = bMap.values().stream().mapToDouble(value -> value).sum();
                double threshold = 1.0 / bMap.size();
                double maxB = 0.0;
                List<Integer> candidateDominant = new ArrayList<>();
                Integer labelSetSize = 0;
                Map<Integer, Double> bMapList = new HashMap<>();

                // Normalize the belonging coefficient of the labels, delete the labels with coefficient under the threshold value,
                // determine the candidate for dominant label.
                for (Map.Entry<Integer, Double> bMapEntry : bMap.entrySet()) {
                    double newB = bMapEntry.getValue() / totalBNNI;
                    if (newB >= threshold) {
                        labelSetSize += 1;
                        bMapList.put(bMapEntry.getKey(), newB);
                        if (newB > maxB) {
                            candidateDominant.clear();
                            maxB = newB;
                            candidateDominant.add(bMapEntry.getKey());
                        } else if (newB == maxB) {
                            candidateDominant.add(bMapEntry.getKey());
                        }
                    }
                }
                communitiesMap.put(u.index(), bMapList);

                // Update the candidate for dominant label.
                if (!candidateDominant.contains(dominantMap.get(u.index()).get("dominantC").intValue())) {
                    Map<String, Double> tempMap = dominantMap.computeIfAbsent(u.index(), key -> new HashMap<>());
                    tempMap.put("dominantC", Double.valueOf(candidateDominant.get(0)));
                    dominantMap.put(u.index(), tempMap);
                    change = true;
                }

                // Update the size of the label set.
                if (!labelSetSize.equals(dominantMap.get(u.index()).get("labelSetSize").intValue())) {
                    Map<String, Double> tempMap = dominantMap.computeIfAbsent(u.index(), key -> new HashMap<>());
                    tempMap.put("labelSetSize", Double.valueOf(labelSetSize));
                    dominantMap.put(u.index(), tempMap);
                    change = true;
                }
            }

            // If there are no changes in the iteration, the label propagation is finished.
            if (!change) {
                break;
            }
            temp += 1;
        }

        // Create a matrix and fill it with the entries from CommunitiesMap.

        Matrix communities = new Basic2DMatrix(graph.nodeCount(), graph.nodeCount());

        for (Map.Entry<Integer, Map<Integer, Double>> entryCurrNode : communitiesMap.entrySet()) {
            for (Map.Entry<Integer, Double> label : entryCurrNode.getValue().entrySet()) {
                communities.set(entryCurrNode.getKey(), label.getKey(), label.getValue());
            }
        }

        // Clear the matrix of zero entries and form communities.
        Matrix result = formCommunities(communities);
        return new Cover(graph, result);
    }
}







