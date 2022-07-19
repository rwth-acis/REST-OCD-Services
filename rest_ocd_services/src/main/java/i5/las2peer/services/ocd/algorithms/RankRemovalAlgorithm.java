package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.measures.PageRank;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmException;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmExecutor;
import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.utils.Pair;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import y.base.*;

import java.util.*;

/**
 * The original version of the algorithm Rank Removal was published by Baumes et al. in 2005:
 * Finding communities by clustering a graph into overlapping sub graphs
 * ISBN: 972-99353-6-X
 *
 * The algorithm Rank Removal disconnects a graph into smaller sub graphs by removing sp called
 * "high ranking" or important nodes which are determined by some metric, until all clusters
 * of the graph have a specific size. Subsequently, the algorithm starts adding the removed
 * nodes back to some clusters if they are immediately adjacent or increase the clusters weight.
 *
 * @author Jan Mortell
 */
public class RankRemovalAlgorithm implements OcdAlgorithm {

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // User specified input parameters and default values for Iterative Scan
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Enum of different available weight functions. These can be chosen in a dropdown in the web client
     */
    private enum weightFunction {
        INTERNAL_EDGE_PROBABILITY,
        EDGE_RATIO,
        INTENSITY_RATIO
    }

    /**
     * Selected weight function Iterative Scan
     * default value: edge ratio
     */
    private weightFunction selectedWeightFunctionIS = weightFunction.EDGE_RATIO;

    /**
     * Enum of different available set-difference functions. These can be chosen in a dropdown in the web client
     */
    private enum setDifferenceFunction {
        HAMMING_DISTANCE,
        PERCENTAGE_NON_OVERLAP
    }

    /**
     * Selected set-difference function
     * default value: hamming distance
     */
    private setDifferenceFunction selectedSetDifferenceFunction = setDifferenceFunction.HAMMING_DISTANCE;

    /**
     * Size of set neighborhood
     * default value: 1
     */
    private double epsilon = 1;

    /**
     * Number of unsuccessful algorithm restarts that do not yield new maximal clusters
     * default value: 5
     */
    private int max_fail = 5;

    /**
     * Minimum cluster size. Smaller clusters get penalized using the penalty function Pen(C)
     * default value: 5
     */
    private int minClusterSize = 5;

    /**
     * Maximum cluster size. Bigger clusters get penalized using the penalty function Pen(C)
     * default value: 20
     */
    private int maxClusterSize = 20;

    /**
     * Penalty multiplier for clusters that are too small
     * default value: 0.1
     */
    private double h1 = 0.1;

    /**
     * Penalty multiplier for clusters that are too large
     * default value: 1
     */
    private double h2 = 1;

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // User specified input parameters and default values for Rank Removal
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Selected weight function Rank Removal
     * default value: edge ratio
     */
    private weightFunction selectedWeightFunctionRaRe = weightFunction.EDGE_RATIO;

    /**
     * Enum of different available ranking functions. These can be chosen un a dropdown in the web client
     */
    private enum rankingFunction {
        OUT_DEGREE,
        PAGE_RANK
    }

    /**
     * Selected ranking function
     * default value: page rank
     */
    private rankingFunction selectedRankingFunction = rankingFunction.PAGE_RANK;

    /**
     * Minimum core size
     * default value: 3
     */
    private int minCoreSize = 3;

    /**
     * Maximum core size
     * default value: 15
     */
    private int maxCoreSize = 15;

    /**
     * Number of important nodes to remove
     * default value: 15
     */
    private int t = 15;

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // Input parameter names for Iterative Scan
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    protected static final String WEIGHT_FUNCTION_IS_NAME = "weightFunctionIS";

    protected static final String SET_DIFFERENCE_FUNCTION_NAME = "setDifferenceFunction";

    protected static final String EPSILON_NAME = "epsilon";

    protected static final String MAX_FAIL_NAME = "maxFail";

    protected static final String MIN_CLUSTER_SIZE_NAME = "minClusterSize";

    protected static final String MAX_CLUSTER_SIZE_NAME = "maxClusterSize";

    protected static final String H1_NAME = "h1";

    protected static final String H2_NAME = "h2";

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // Input parameter names for Rank Removal
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    protected static final String WEIGHT_FUNCTION_RARE_NAME = "weightFunctionRaRe";

    protected static final String RANKING_FUNCTION_NAME = "rankingFunction";

    protected static final String MIN_CORE_SIZE_NAME = "minCoreSize";

    protected static final String MAX_CORE_SIZE_NAME = "maxCoreSize";

    protected static final String T_NAME = "t";

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // Default constructor that returns an instance of the algorithm with default parameter values
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    public RankRemovalAlgorithm() {

    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // Setters for the parameters of both algorithms
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    // TODO: Check if boundaries of parameters are needed, Check if set-difference function and epsilon necessary

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
        /*if (parameters.containsKey(WEIGHT_FUNCTION_IS_NAME)) {
            selectedWeightFunctionIS = weightFunction.valueOf(WEIGHT_FUNCTION_IS_NAME);
            parameters.remove(WEIGHT_FUNCTION_IS_NAME);
        }*/

        /*if (parameters.containsKey(SET_DIFFERENCE_FUNCTION_NAME)) {
            selectedSetDifferenceFunction = setDifferenceFunction.valueOf(SET_DIFFERENCE_FUNCTION_NAME);
            parameters.remove(SET_DIFFERENCE_FUNCTION_NAME);
        }*/

        if (parameters.containsKey(EPSILON_NAME)) {
            epsilon = Double.parseDouble(parameters.get(EPSILON_NAME));
            if (epsilon < 1) {
                throw new IllegalArgumentException("The parameter epsilon should be at least 1!");
            }
            parameters.remove(EPSILON_NAME);
        }

        if (parameters.containsKey(MAX_FAIL_NAME)) {
            max_fail = Integer.parseInt(parameters.get(MAX_FAIL_NAME));
            parameters.remove(MAX_FAIL_NAME);
        }

        if (parameters.containsKey(MIN_CLUSTER_SIZE_NAME)) {
            minClusterSize = Integer.parseInt(parameters.get(MIN_CLUSTER_SIZE_NAME));
            parameters.remove(MIN_CLUSTER_SIZE_NAME);
        }

        if (parameters.containsKey(MAX_CLUSTER_SIZE_NAME)) {
            maxClusterSize = Integer.parseInt(parameters.get(MAX_CLUSTER_SIZE_NAME));
            if (maxClusterSize < minClusterSize) {
                throw new IllegalArgumentException("The parameter maxClusterSize must be greater or equal than minClusterSize!");
            }
            parameters.remove(MAX_CLUSTER_SIZE_NAME);
        }

        if (parameters.containsKey(H1_NAME)) {
            h1 = Double.parseDouble(parameters.get(H1_NAME));
            parameters.remove(H1_NAME);
        }

        if (parameters.containsKey(H2_NAME)) {
            h2 = Double.parseDouble(parameters.get(H2_NAME));
            parameters.remove(H2_NAME);
        }

        /*if (parameters.containsKey(WEIGHT_FUNCTION_RARE_NAME)) {
            selectedWeightFunctionRaRe = weightFunction.valueOf(WEIGHT_FUNCTION_RARE_NAME);
            parameters.remove(WEIGHT_FUNCTION_RARE_NAME);
        }*/

        /*if (parameters.containsKey(RANKING_FUNCTION_NAME)) {
            selectedRankingFunction = rankingFunction.valueOf(RANKING_FUNCTION_NAME);
            parameters.remove(RANKING_FUNCTION_NAME);
        }*/

        if (parameters.containsKey(MIN_CORE_SIZE_NAME)) {
            minCoreSize = Integer.parseInt(parameters.get(MIN_CORE_SIZE_NAME));
            parameters.remove(MIN_CORE_SIZE_NAME);
        }

        if (parameters.containsKey(MAX_CORE_SIZE_NAME)) {
            maxCoreSize = Integer.parseInt(parameters.get(MAX_CORE_SIZE_NAME));
            if (maxCoreSize < minCoreSize) {
                throw new IllegalArgumentException("The parameter maxCoreSize mus be greater or equal then minCoreSize!");
            }
            parameters.remove(MAX_CORE_SIZE_NAME);
        }

        if (parameters.containsKey(T_NAME)) {
            t = Integer.parseInt(parameters.get(T_NAME));
            parameters.remove(T_NAME);
        }

        if (parameters.size() > 0) {
            throw new IllegalArgumentException("Invalid number of parameters! Too many.");
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // Getters for the parameters of both algorithms
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        // parameters.put(WEIGHT_FUNCTION_IS_NAME, selectedWeightFunctionIS.toString());
        // parameters.put(SET_DIFFERENCE_FUNCTION_NAME, selectedSetDifferenceFunction.toString());
        parameters.put(EPSILON_NAME, Double.toString(epsilon));
        parameters.put(MAX_FAIL_NAME, Integer.toString(max_fail));
        parameters.put(MIN_CLUSTER_SIZE_NAME, Integer.toString(minClusterSize));
        parameters.put(MAX_CLUSTER_SIZE_NAME, Integer.toString(maxClusterSize));
        parameters.put(H1_NAME, Double.toString(h1));
        parameters.put(H2_NAME, Double.toString(h2));
        // parameters.put(WEIGHT_FUNCTION_RARE_NAME, selectedWeightFunctionRaRe.toString());
        // parameters.put(RANKING_FUNCTION_NAME, selectedRankingFunction.toString());
        parameters.put(MIN_CORE_SIZE_NAME, Integer.toString(minCoreSize));
        parameters.put(MAX_CORE_SIZE_NAME, Integer.toString(maxCoreSize));
        parameters.put(T_NAME, Integer.toString(t));

        return parameters;
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // Main algorithm method
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    // Global List of removed nodes
    List<Integer> R = new ArrayList<>(Collections.emptyList());

    /**
     * The main algorithm method returning a cover
     *
     * @param graph                     An at least weakly connected graph whose community structure will be detected.
     * @return                          A cover for the passed graph
     * @throws OcdAlgorithmException    if no solution is found
     * @throws InterruptedException     if thread was interrupted
     * @throws OcdMetricException       if the metric execution failed
     */
    @Override
    public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException, OcdMetricException {
        // Resulting cover will be assigned here
        Cover outputCover;

        // Create adjacency matrix of the graph
        boolean[][] adjacencyMatrix = createAdjacencyMatrix(graph);

        // List of all found cluster cores
        List<Pair<CustomGraph, Map<Node, Node>>> clusterCores = new ArrayList<>(Collections.emptyList());

        // Detect all connected components in the graph
        List<Pair<CustomGraph, Map<Node, Node>>> connectedComponents = getConnectedComponents(graph);

        // List of detected communities using Rank Removal
        List<Pair<CustomGraph, Map<Node, Node>>> communitiesRaRe;

        // Call method clusterComponent for every connected component
        for (int i = 0; i < connectedComponents.size(); i++) {

            if(Thread.interrupted()) {
                throw new InterruptedException();
            }

            try {
                clusterCores.addAll(clusterComponent(connectedComponents.get(i)));
            } catch (CentralityAlgorithmException e) {
                System.out.println("Cluster component failed!");
                throw new RuntimeException(e);
            }
        }


        // In the following cluster cores will be build up to form communities
        communitiesRaRe = clusterCores;

        // Add removed nodes back to cluster cores
        for (int i = 0; i < R.size(); i++) {

            if(Thread.interrupted()) {
                throw new InterruptedException();
            }

            for (int j = 0; j < communitiesRaRe.size(); j++) {

                if(Thread.interrupted()) {
                    throw new InterruptedException();
                }

                // Add the node i in R to the graph cluster j
                Pair<CustomGraph, Map<Node, Node>> extendedCore = communitiesRaRe.get(j);

                // Create cluster where node from R is added back to cluster from communitiesRaRe
                extendedCore = reAddNode(extendedCore, R.get(i), adjacencyMatrix);

                // Check if nodes in R are immediately adjacent to the cluster cores or if they increase their weight
                if (isAdjacent(communitiesRaRe.get(j), R.get(i), adjacencyMatrix) || calculateWeight(graph, extendedCore) > calculateWeight(graph, communitiesRaRe.get(j))) {
                    // Add node to cluster in list of cluster cores
                    communitiesRaRe.set(j, extendedCore);
                }
            }
        }

        if (communitiesRaRe.isEmpty()) {
            System.out.println("Algorithm Rank Removal could not find communities!");
            throw new OcdAlgorithmException();
        }

        System.out.println("communitieisRaRe: " + communitiesRaRe.toString() + "\n");

        // List of detected communities using Iterative Scan
        List<Pair<CustomGraph, Map<Node, Node>>> communitiesIS = new ArrayList<>(Collections.emptyList());

        Random r = new Random();
        // Number of algorithm restarts that yield no new local maxima
        int fails = 0;

        boolean[] maxima = new boolean[communitiesRaRe.size()];

        // Call Iterative Scan algorithm on output of Rank Removal
        // Stop if the last max_fail restarts yield no new local maxima
        while (fails < max_fail) {

            if(Thread.interrupted()) {
                throw new InterruptedException();
            }

            int seed = r.nextInt(communitiesRaRe.size());

            // Increase fails variable if the received random seed was already checked
            if (!maxima[seed]) {
                maxima[seed] = true;
                fails = 0;
                communitiesIS.add(iterativeScan(graph, communitiesRaRe.get(seed), adjacencyMatrix));
            } else {
                fails++;
            }
        }

        if (communitiesIS.isEmpty()) {
            throw new OcdAlgorithmException();
        }

        System.out.println("communitiesIS: " + communitiesIS.toString()  + "\n");

        // Create membership matrix
        Matrix membershipMatrix = createMembershipMatrix(graph, communitiesIS);

        // Create cover out of list communitiesIS
        outputCover = new Cover(graph, membershipMatrix);

        return outputCover;
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // Algorithm cover creation type
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public CoverCreationType getAlgorithmType() {
        return CoverCreationType.RANK_REMOVAL_AND_ITERATIVE_SCAN_ALGORITHM;
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // Setter for all possible compatible graph types
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public Set<GraphType> compatibleGraphTypes() {
        Set<GraphType> compatibilities = new HashSet<GraphType>();
        compatibilities.add(GraphType.DIRECTED);

        return compatibilities;
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // Algorithm methods
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * This method detects all connected components inside a given graph
     * and returns them in a set. Therefore, it uses depth first search (dfs)
     * for undirected graphs and
     *
     * @param graph     Graph containing at least one connected component
     * @return          List of connected components in passed graph
     */
    public List<Pair<CustomGraph, Map<Node, Node>>> getConnectedComponents(CustomGraph graph) {
        // List of connected components
        GraphProcessor processor = new GraphProcessor();

        List<Pair<CustomGraph, Map<Node, Node>>> connectedComponents = processor.divideIntoConnectedComponents(graph);

        return connectedComponents;
    }

    /**
     * This method takes a connected component as input and checks if its
     * number of nodes is below maxCoreSize. If not, t nodes are removed
     * and added to set R. This process is repeated until the connected
     * component is either in between the boundaries minCoreSize and maxCoreSize
     * or disconnected in multiple connected components. The method gets
     * recursively executed on all emerging connected components.
     *
     * @param graph     Connected component as custom graph
     * @return          List of clusters of the passed graph with the right size
     */
    public List<Pair<CustomGraph, Map<Node, Node>>> clusterComponent(Pair<CustomGraph, Map<Node, Node>> graph) throws InterruptedException, CentralityAlgorithmException {
        List<Pair<CustomGraph, Map<Node, Node>>> clusterCores = new ArrayList<>(Collections.emptyList());

        if (graph.getFirst().nodeCount() > maxCoreSize) {
            for (int i = 0; i < t; i++) {
                Node node;
                // Calculate the highest ranking node in the passed cluster
                node = getHighestRankingNode(graph.getFirst());
                // Add index of corresponding node in original graph to R
                R.add(graph.getSecond().get(node).index());
                // Remove the highest ranking node and corresponding mapping
                graph.getFirst().removeNode(node);
                graph.getSecond().remove(node);
            }

            // Detect all connected components in connectedComponent
            List<Pair<CustomGraph, Map<Node, Node>>> connectedComponents = getConnectedComponents(graph.getFirst());

            if (connectedComponents.size() != 1) {
                // Map nodes in new connected components to original graph instead of old cluster
                for (int clusterCount = 0; clusterCount < connectedComponents.size(); clusterCount++) {
                    NodeCursor clusterNodes = connectedComponents.get(clusterCount).getFirst().nodes();

                    // Iterate over every node in the selected cluster
                    while (clusterNodes.ok()) {
                        Node clusterNode = clusterNodes.node();

                        // Get node from the original cluster
                        Node valueNode = connectedComponents.get(clusterCount).getSecond().get(clusterNode);

                        // Map node in new cluster to corresponding node in original graph
                        connectedComponents.get(clusterCount).getSecond().put(clusterNode, graph.getSecond().get(valueNode));
                        connectedComponents.get(clusterCount).getSecond().remove(valueNode);

                        clusterNodes.next();
                    }
                }
            }

            for (int i = 0; i < connectedComponents.size(); i++) {
                clusterCores.addAll(clusterComponent(connectedComponents.get(i)));
            }

        } else if (minCoreSize <= graph.getFirst().nodeCount() && graph.getFirst().nodeCount() <= maxCoreSize) {
            // Mark connectedComponent as cluster core
            clusterCores.add(graph);
        }

        return clusterCores;
    }

    /**
     * This method determines the node with the highest ranking
     * of the graph by using the given ranking function
     *
     * @param graph     Input graph
     * @return          Node with the highest ranking in graph
     */
    public Node getHighestRankingNode(CustomGraph graph) throws InterruptedException, CentralityAlgorithmException {
        // Highest ranking node to be assigned
        Node node = null;

        switch (selectedRankingFunction) {
            case PAGE_RANK -> node = calculatePageRank(graph);
            case OUT_DEGREE -> node = calculateDegree(graph);
            default -> System.out.println("The selected ranking function does not exist!");
        }

        return node;
    }

    /**
     * This method calculates the pageRank of every node in a graph
     * and returns the node with the highest pageRank in a graph
     *
     * @param graph     Cluster where the node with the highest pagerank is searched in
     * @return          Node with the highest page rank in the passed graph cluster
     */
    public Node calculatePageRank(CustomGraph graph) throws InterruptedException, CentralityAlgorithmException {
        Node returnNode = null;

        double maxValue = 0;

        // Execute pageRank for all nodes of the graph
        PageRank algorithm = new PageRank();
        CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
        CentralityMap pageRanks = executor.execute(graph, algorithm);

        Map<String, Double> pageRank = pageRanks.getMap();

        // Find the node with the highest pageRank in graph
        NodeCursor nodes = graph.nodes();

        while (nodes.ok()) {
            Node currNode = nodes.node();

            double newValue = pageRank.get(graph.getNodeName(currNode));
            if (newValue > maxValue) {
                maxValue = newValue;
                returnNode = currNode;
            }

            nodes.next();
        }

        return returnNode;
    }

    /**
     * This method calculates the degree of every node
     * in the passed graph and returns the node with the
     * highest out degree
     *
     * @param graph     graph in which the node with the highest degree is searched
     * @return          node with the highest degree in the passed graph cluster
     */
    public Node calculateDegree(CustomGraph graph) {
        Node node = null;
        // Array of all nodes in the graph
        Node[] nodes = graph.getNodeArray();

        // Find the node with the highest out degree in the graph
        for (int i = 0; i < nodes.length; i++) {
            if (node == null || nodes[i].degree() > node.degree()) {
                node = nodes[i];
            }
        }

        return node;
    }

    /**
     * This method checks if there exists an edge between a node of the provided
     * graph and the provided node. If yes, the method return true. Else it
     * returns false.
     *
     * @param clusterCore       Cluster of nodes that should be checked
     * @param nodeIndex         Index of node that is checked
     * @param adjacencyMatrix   Adjacency matrix of original graph
     * @return                  if the passed node is adjacent to the passed cluster core
     */
    public boolean isAdjacent(Pair<CustomGraph, Map<Node, Node>> clusterCore, int nodeIndex, boolean[][] adjacencyMatrix) {
        NodeCursor clusterNodes = clusterCore.getFirst().nodes();

        // Iterate over every node in the cluster
        while (clusterNodes.ok()) {
            Node currNode = clusterNodes.node();

            // Node is adjacent to the cluster if there exists at least one edge between the two
            if (adjacencyMatrix[nodeIndex][clusterCore.getSecond().get(currNode).index()] || adjacencyMatrix[clusterCore.getSecond().get(currNode).index()][nodeIndex]) {
                return true;
            }

            clusterNodes.next();
        }

        return false;
    }

    /**
     * This method returns an adjacency matrix of the passed graph. If
     * the entry [i][j] of the matrix is true, there is a directed edge between
     * the nodes i and j in the graph.
     *
     * @param graph     graph of which the adjacency matrix should be created
     * @return          adjacency matrix of the passed graph
     */
    public boolean[][] createAdjacencyMatrix(CustomGraph graph) {
        // Initialize empty matrix
        boolean[][] matrix = new boolean[graph.nodeCount()][graph.nodeCount()];

        EdgeCursor edges = graph.edges();

        while (edges.ok()) {
            Edge edge = edges.edge();
            matrix[edge.source().index()][edge.target().index()] = true;
            edges.next();
        }

        return matrix;
    }

    /**
     * This method calls the chosen weight function and return the calculated
     * weight of the passed graph.
     *
     * @param graph     original graph of the cluster sub graph
     * @param cluster   cluster of which the weight should be calculated
     * @return          weight of the passed cluster
     */
    public double calculateWeight(CustomGraph graph, Pair<CustomGraph, Map<Node, Node>> cluster) {
        // Weight variable initialized with an unusual value
        double weight = -10000;
        switch (selectedWeightFunctionRaRe) {
            case INTERNAL_EDGE_PROBABILITY -> weight = calculateInternalEdgeProbability(cluster.getFirst());
            case EDGE_RATIO -> weight = calculateEdgeRatio(graph, cluster.getFirst());
            case INTENSITY_RATIO -> weight = calculateIntensityRatio(graph, cluster.getFirst());
            default -> System.out.println("This weight function does not exist!");
        }

        return weight;
    }

    /**
     * This method returns the number of outgoing edges between the passed cluster
     * and the original graph.
     *
     * @param graph     original graph of the passed cluster
     * @param cluster   cluster of which the out degree is calculated
     * @return          out degree of the passed cluster
     */
    public int calculateClusterOutDegree(CustomGraph graph, CustomGraph cluster) {
        // Number of outgoing edges from the passed cluster
        int outDegree = 0;

        EdgeCursor edges = graph.edges();

        // Loop iterates over every edge in the passed original graph
        while (edges.ok()) {
            Edge edge = edges.edge();

            // Increase outDegree if edge has source in cluster and target not in cluster (implies target in graph but not in cluster)
            if (cluster.contains(edge.source()) && !cluster.contains(edge.target())) {
                outDegree++;
            }

            edges.next();
        }

        return outDegree;
    }

    /**
     * This method returns the number of incoming edges between the passed cluster
     * and the original graph.
     *
     * @param graph     original graph of the passed cluster
     * @param cluster   cluster of which the in degree is calculated
     * @return          in degree of the passed cluster
     */
    public int calculateClusterInDegree(CustomGraph graph, CustomGraph cluster) {
        // Number of incoming edges from the passed cluster
        int inDegree = 0;

        EdgeCursor edges = graph.edges();

        // Loop iterates over every edge in the passed original graph
        while (edges.ok()) {
            Edge edge = edges.edge();

            // Increase inDegree if edge has source outside the cluster and target in the cluster (implies target in cluster but not in graph)
            if (cluster.contains(edge.target()) && !cluster.contains(edge.source())) {
                inDegree++;
            }

            edges.next();
        }

        return inDegree;
    }

    /**
     * This method calculates the degree of the passed cluster by summing
     * up the in and out degree of the cluster.
     *
     * @param graph     the original graph of the passed cluster
     * @param cluster   the cluster of which the degree is calculated
     * @return          the degree of the passed cluster
     */
    public int calculateClusterDegree(CustomGraph graph, CustomGraph cluster) {
        return calculateClusterOutDegree(graph, cluster) + calculateClusterInDegree(graph, cluster);
    }

    /**
     * This method calculate the external edge intensity of a
     * passed graph cluster.
     *
     * @param graph     The passed graph cluster
     * @param cluster   the cluster of which the external edge intensity is calculated
     * @return          The external edge intensity of the cluster
     */
    public double calculateExternalEdgeIntensity(CustomGraph graph, CustomGraph cluster) {
        return (float)calculateClusterDegree(graph, cluster)/(2 * cluster.nodeCount() * (graph.nodeCount() - cluster.nodeCount()));
    }

    /**
     * This method calculate the internal edge intensity of a
     * passed graph cluster.
     *
     * @param graph     The passed graph cluster
     * @return          The internal edge intensity of the cluster
     */
    public double calculateInternalEdgeIntensity(CustomGraph graph) {
        return (float)graph.edgeCount()/(graph.nodeCount() * (graph.nodeCount() - 1));
    }

    /**
     * This method returns the internal edge-probability of a given graph cluster
     * that is equal to the internal edge intensity.
     *
     * @param graph     the graph cluster of which the internal edge-probability is calculated
     * @return          the internal edge-probability of the passed graph cluster
     */
    public double calculateInternalEdgeProbability(CustomGraph graph) {
        return calculateInternalEdgeIntensity(graph);
    }

    /**
     * This method calculates the edge ratio of a given cluster,
     * corresponding to an original graph.
     *
     * @param graph     the original graph of the passed cluster
     * @param cluster   cluster of which the edge ratio is calculated
     * @return          the edge ratio of the passed cluster
     */
    public double calculateEdgeRatio(CustomGraph graph, CustomGraph cluster) {
        return graph.edgeCount()/(graph.edgeCount() + calculateExternalEdgeIntensity(graph, cluster));
    }

    /**
     * This method calculates the intensity ratio of the passed graph cluster
     *
     * @param graph     the original graph of the passed graph cluster
     * @param cluster   the graph cluster of which the intensity ratio is calculated
     * @return          the intensity ratio of the passed graph cluster
     */
    public double calculateIntensityRatio(CustomGraph graph, CustomGraph cluster) {
        return calculateInternalEdgeIntensity(graph)/(calculateInternalEdgeIntensity(graph) + calculateExternalEdgeIntensity(graph, cluster));
    }

    /**
     *  This method adds a node back to a cluster if it is adjacent to the cluster
     *  in the original graph.
     *
     * @param cluster           Cluster to which the passed node should be added and corresponding mapping to original graph
     * @param nodeIndex         Node that should be added to the passed cluster
     * @param adjacencyMatrix   Adjacency matrix of the original graph
     * @return                  Cluster with added node if adjacent
     */
    public Pair<CustomGraph, Map<Node, Node>> reAddNode(Pair<CustomGraph, Map<Node, Node>> cluster, int nodeIndex, boolean[][] adjacencyMatrix) {
        // Whether the passed cluster is directed or not
        boolean directed = cluster.getFirst().isDirected();

        // Iterate over every entry for node in the adjacency matrix
        for (int i = 0; i < adjacencyMatrix.length; i++) {

            // Check if edge existed in original graph
            if (adjacencyMatrix[nodeIndex][i]) {    // => Node to be reinserted and Node i in original graph are connected
                NodeCursor clusterNodes = cluster.getFirst().nodes();

                // Iterate over every node in the passed cluster
                while (clusterNodes.ok()) {
                    // Node in the cluster
                    Node currNode = clusterNodes.node();
                    // Corresponding node in the original graph
                    Node corrNode = cluster.getSecond().get(clusterNodes.node());

                    // If edge existed, add node and edges
                    if (corrNode.index() == i) {
                        if (!cluster.getFirst().contains(currNode)) {
                            // Create new node
                            Node newNode = cluster.getFirst().createNode();

                            // Create edges
                            if (!directed) {
                                cluster.getFirst().createEdge(newNode, currNode);
                                cluster.getFirst().createEdge(currNode, newNode);
                            } else {
                                cluster.getFirst().createEdge(newNode, currNode);

                                if (adjacencyMatrix[i][nodeIndex]) {
                                    cluster.getFirst().createEdge(currNode, newNode);
                                }
                            }
                        }
                    }

                    clusterNodes.next();
                }
            }
        }

        return cluster;
    }

    /**
     * This method creates a membership matrix, indicating the membership
     * of the nodes in graph to the communities in the list clusters.
     *
     * @param graph         The original graph containing all relevant nodes
     * @param clusters      A list of all detected communities in the Graph
     * @return              A membership matrix
     */
    public Matrix createMembershipMatrix(CustomGraph graph, List<Pair<CustomGraph, Map<Node, Node>>> clusters) {
        // Membership matrix
        Matrix membershipMatrix = new Basic2DMatrix(graph.nodeCount(), clusters.size());

        NodeCursor nc = graph.nodes();
        // Iterate over every node in the graph, check if it contained in any cluster, if yes, set corresponding entry to 1
        while (nc.ok()) {
            Node node = nc.node();

            for (int i = 0; i < clusters.size(); i++) {
                if (clusters.get(i).getSecond().containsValue(node)) {
                    membershipMatrix.set(node.index(), i, 1);
                }
            }

            nc.next();
        }

        return membershipMatrix;
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // Algorithm methods for Iterative Scan
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * This method calculates the penalty for a given cluster that
     * is subtracted from the weight of the cluster. If the size
     * of the cluster is outside the boundaries defined by the parameters
     * minClusterSize and maxClusterSize, a penalty is subtracted from
     * the clusters weight. Else, 0 is subtracted.
     *
     * @param graph     original graph of the passed cluster
     * @param cluster   cluster of which the weight should be calculated
     * @return          the weight of the given cluster subtracted by the calculated penalty
     */
    public double calculatePenalty(CustomGraph graph, Pair<CustomGraph, Map<Node, Node>> cluster) {
        double penalty = 0;

        if (cluster.getFirst().nodeCount() > maxClusterSize || minClusterSize < cluster.getFirst().nodeCount()) {
            double minPenalty = (h1 * (minClusterSize-cluster.getFirst().nodeCount())/(minClusterSize - 1));
            double maxPenalty = (h2 * (cluster.getFirst().nodeCount() - maxClusterSize)/(graph.nodeCount() - maxClusterSize));

            penalty = Math.max(minPenalty, maxPenalty);
        }

        return (calculateWeight(graph, cluster) - Math.max(0, penalty));
    }

    /**
     * This method refines passed graph clusters according to the description
     * of Baumes et al. Therefore, nodes from graph are added to or removed from
     * the cluster until the weight of the cluster does not improve anymore.
     *
     * @param graph             original graph of the passed cluster
     * @param cluster           cluster that should be refined
     * @param adjacencyMatrix   Adjacency matrix of passed graph
     * @return                  refined cluster
     */
    public Pair<CustomGraph, Map<Node, Node>> iterativeScan(CustomGraph graph, Pair<CustomGraph, Map<Node, Node>> cluster, boolean[][] adjacencyMatrix) {
        // Highest known weight of the cluster
        double w = calculateWeight(graph, cluster);
        boolean increased = true;
        // Cluster to be returned
        Pair<CustomGraph, Map<Node, Node>> finalCluster = cluster;
        // Create reverse map of cluster map that maps nodes in original graph to nodes in cluster
        Map<Node, Node> reverseMap = new HashMap<>();

        NodeCursor cNodes = cluster.getFirst().nodes();

        while (cNodes.ok()) {
            Node n = cNodes.node();

            reverseMap.put(cluster.getSecond().get(n), n);

            cNodes.next();
        }

        // Node array of original graph for drawing edges from nodes added to the cluster
        Node[] nodeArr = graph.getNodeArray();

        NodeCursor graphNodes = graph.nodes();

        // As long as the weight of the current cluster increases
        while (increased) {

            // Create temporary cluster
            Pair<CustomGraph, Map<Node, Node>> tempCluster = finalCluster;

            // For every node in graph, check if it increases the weight of the cluster by either adding or removing
            while (graphNodes.ok()) {
                Node graphNode = graphNodes.node();

                // Check if node is already part of the cluster
                if (reverseMap.containsKey(graphNode)) {
                    // Delete node and corresponding map entry
                    tempCluster.getFirst().removeNode(reverseMap.get(graphNode));
                    tempCluster.getSecond().remove(reverseMap.get(graphNode));
                } else {
                    // Add node to cluster, stays unconnected in case no neighbours from original graph in cluster
                    Node newNode = tempCluster.getFirst().createNode();
                    tempCluster.getSecond().put(newNode, graphNode);
                    // Update reverse map
                    reverseMap.put(graphNode, newNode);
                    // Search in adjacency matrix for available edges
                    for (int i = 0; i < adjacencyMatrix.length; i++) {
                        // If edge existed in original graph
                        if (adjacencyMatrix[graphNode.index()][i]) {
                            // Check whether cluster contains neighbour from original graph
                            if (tempCluster.getSecond().containsValue(nodeArr[i])) {
                                tempCluster.getFirst().createEdge(newNode, reverseMap.get(nodeArr[i]));
                            }
                        }
                        // Check for edge in other direction
                        if (adjacencyMatrix[i][graphNode.index()]) {
                            if (tempCluster.getSecond().containsValue(nodeArr[i])) {
                                tempCluster.getFirst().createEdge(reverseMap.get(nodeArr[i]), newNode);
                            }
                        }
                    }
                }

                if (calculatePenalty(graph, tempCluster) > calculatePenalty(graph, finalCluster)) {
                    finalCluster = tempCluster;
                }

                graphNodes.next();
            }

            // Continue loop until w does not improve
            double newWeight = calculatePenalty(graph, finalCluster);
            if (newWeight == w) {
                increased = false;
            } else {
                w = newWeight;
            }
        }

        return finalCluster;
    }
}
