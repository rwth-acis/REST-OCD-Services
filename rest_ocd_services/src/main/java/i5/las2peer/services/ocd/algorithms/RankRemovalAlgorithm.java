package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.measures.PageRank;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmException;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmExecutor;
import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.utils.Pair;
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

    // TODO: Check if boundaries of parameters are needed

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
        if (parameters.containsKey(WEIGHT_FUNCTION_IS_NAME)) {
            // TODO: complete
            // selectedWeightFunctionIS = ;
            parameters.remove(WEIGHT_FUNCTION_IS_NAME);
        }

        if (parameters.containsKey(SET_DIFFERENCE_FUNCTION_NAME)) {
            // TODO: complete
            // selectedSetDifferenceFunction = ;
            parameters.remove(SET_DIFFERENCE_FUNCTION_NAME);
        }

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

        if (parameters.containsKey(WEIGHT_FUNCTION_RARE_NAME)) {
            // TODO: complete
            // selectedWeightFunctionRaRe = ;
            parameters.remove(WEIGHT_FUNCTION_RARE_NAME);
        }

        if (parameters.containsKey(RANKING_FUNCTION_NAME)) {
            // TODO: complete
            // selectedRankingFunction = ;
            parameters.remove(RANKING_FUNCTION_NAME);
        }

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

        parameters.put(WEIGHT_FUNCTION_IS_NAME, selectedWeightFunctionIS.toString());
        parameters.put(SET_DIFFERENCE_FUNCTION_NAME, selectedSetDifferenceFunction.toString());
        parameters.put(EPSILON_NAME, Double.toString(epsilon));
        parameters.put(MAX_FAIL_NAME, Integer.toString(max_fail));
        parameters.put(MIN_CLUSTER_SIZE_NAME, Integer.toString(minClusterSize));
        parameters.put(MAX_CLUSTER_SIZE_NAME, Integer.toString(maxClusterSize));
        parameters.put(H1_NAME, Double.toString(h1));
        parameters.put(H2_NAME, Double.toString(h2));
        parameters.put(WEIGHT_FUNCTION_RARE_NAME, selectedWeightFunctionRaRe.toString());
        parameters.put(RANKING_FUNCTION_NAME, selectedRankingFunction.toString());
        parameters.put(MIN_CORE_SIZE_NAME, Integer.toString(minCoreSize));
        parameters.put(MAX_CORE_SIZE_NAME, Integer.toString(maxCoreSize));
        parameters.put(T_NAME, Integer.toString(t));

        return parameters;
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // Main algorithm method
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    // Global List of removed nodes
    List<Node> R = Collections.emptyList();

    // TODO: Add method description
    // TODO: Check try catch

    @Override
    public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException, OcdMetricException {
        // Resulting cover will be assigned here
        Cover outputCover = null;

        // Create adjacency matrix of the graph
        boolean[][] adjacencyMatrix = createAdjacencyMatrix(graph);

        // List of all found cluster cores
        List<CustomGraph> clusterCores = new ArrayList<>(Collections.emptyList());

        // Detect all connected components in the graph
        List<CustomGraph> connectedComponents = getConnectedComponents(graph);

        // List of detected communities using Rank Removal
        List<CustomGraph> communitiesRaRe;

        // Call method clusterComponent for every connected component
        for (int i = 0; i < connectedComponents.size(); i++) {
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
            for (int j = 0; j < communitiesRaRe.size(); i++) {
                // Add the node i in R to the graph cluster j
                CustomGraph extendedCore = communitiesRaRe.get(j);

                extendedCore = reAddNode(extendedCore, R.get(i), adjacencyMatrix);

                // TODO: Does immediately adjacent mean only adjacent to the original cluster?
                // Check if nodes in R are immediately adjacent to the cluster cores or if they increase their weight
                if (isAdjacent(graph, communitiesRaRe.get(i), R.get(i)) || calculateWeight(extendedCore, graph) > calculateWeight(clusterCores.get(j), graph)) {
                    // Add node to cluster in list of cluster cores
                    communitiesRaRe.set(j, extendedCore);
                }
            }
        }

        // List of detected communities using Iterative Scan
        List<CustomGraph> communitiesIS = Collections.emptyList();

        Random r = new Random();
        // Number of algorithm restarts that yield no new local maxima
        int fails = 0;

        boolean[] maxima = new boolean[communitiesRaRe.size()];

        // Call Iterative Scan algorithm on output of Rank Removal
        // Stop if the last max_fail restarts yield no new local maxima
        while (fails < max_fail) {
            int seed = r.nextInt(communitiesRaRe.size() - 0) + 0;
            // Increase fails variable if the received random seed was already checked
            if (!maxima[seed]) {
                maxima[seed] = true;
                fails = 0;
                communitiesIS.add(iterativeScan(graph, communitiesRaRe.get(seed), adjacencyMatrix));
            } else {
                fails++;
            }
        }

        // TODO: Create output cover
        // Create cover out of list communitiesIS

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
    public List<CustomGraph> getConnectedComponents(CustomGraph graph) {
        // List of connected components
        GraphProcessor processor = new GraphProcessor();
        List<Pair<CustomGraph, Map<Node, Node>>> connectedComponentsList = processor.divideIntoConnectedComponents(graph);

        // Add graphs of all connected components to list
        List<CustomGraph> connectedComponents = new ArrayList<>(Collections.emptyList());
        for (int i = 0; i < connectedComponentsList.size(); i++) {
            connectedComponents.add((connectedComponentsList.get(i)).getFirst());
        }

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
    public List<CustomGraph> clusterComponent(CustomGraph graph) throws InterruptedException, CentralityAlgorithmException {
        Node node;

        List<CustomGraph> clusterCores = new ArrayList<>(Collections.emptyList());

        if (graph.nodeCount() > maxCoreSize) {
            for (int i = 0; i < t; i++) {
                // Calculate the highest ranking node
                node = getHighestRankingNode(graph);
                // Add the highest ranking node to set R
                R.add(node);
                // Remove the highest ranking node
                graph.removeNode(node);
            }
            // Detect all connected components in connectedComponent
            List<CustomGraph> connectedComponents = getConnectedComponents(graph);

            for (int i = 0; i < connectedComponents.size(); i++) {
                clusterCores.addAll(clusterComponent(connectedComponents.get(i)));
            }

        } else if (minCoreSize <= graph.nodeCount() && graph.nodeCount() <= maxCoreSize) {
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
        Node node = null;

        // Array of all nodes in the graph
        Node[] nodes = graph.getNodeArray();

        // Execute pageRank for all nodes of the graph
        PageRank algorithm = new PageRank();
        CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
        CentralityMap pageRanks = executor.execute(graph, algorithm);

        // Find the node with the highest pageRank in graph
        for (int i = 0; i < graph.nodeCount(); i++) {
            if (node == null || pageRanks.getNodeValue(nodes[i]) > pageRanks.getNodeValue(node)) {
                node = nodes[i];
            }
        }

        return node;
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

    // TODO: Refactor using created adjacency matrix

    /**
     * This method checks if there exists an edge between a node of the provided
     * graph and the provided node. If yes, the method return true. Else it
     * returns false.
     *
     * @param graph             The graph for which the adjacency is checked
     * @param clusterCore       Cluster of nodes that should be checked
     * @param node              Node that is checked
     * @return                  if the passed node is adjacent to the passed cluster core
     */
    public boolean isAdjacent(CustomGraph graph, CustomGraph clusterCore, Node node) {
        NodeCursor clusterCoreNodes = clusterCore.nodes();
        while (clusterCoreNodes.ok()) {
            if (graph.containsEdge(clusterCoreNodes.node(), node)) {
                return true;
            }
            clusterCoreNodes.next();
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
    public double calculateWeight(CustomGraph graph, CustomGraph cluster) {
        // Weight variable initialized with an unusual value
        double weight = -10000;
        switch (selectedWeightFunctionRaRe) {
            case INTERNAL_EDGE_PROBABILITY -> weight = calculateInternalEdgeProbability(cluster);
            case EDGE_RATIO -> weight = calculateEdgeRatio(graph, cluster);
            case INTENSITY_RATIO -> weight = calculateIntensityRatio(graph, cluster);
            default -> System.out.println("This weight function does not exist!");
        }

        return weight;
    }

    // TODO: Check functionality for undirected graphs

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
     * @param cluster           Cluster to which the passed node should be added
     * @param node              Node that should be added to the passed cluster
     * @param adjacencyMatrix   Adjacency matrix of the original graph
     * @return                  Cluster with added node if adjacent
     */
    public CustomGraph reAddNode(CustomGraph cluster, Node node, boolean[][] adjacencyMatrix) {
        // Iterate over every entry for node in the adjacency matrix
        for (int i = 0; i < adjacencyMatrix.length; i++) {

            // Check if edge existed in original graph
            if (adjacencyMatrix[node.index()][i]) {
                NodeCursor clusterNodes = cluster.nodes();

                while (clusterNodes.ok()) {
                    Node currNode = clusterNodes.node();

                    if (currNode.index() == i) {
                        if (!cluster.contains(node)) {
                            // Add node to cluster
                            cluster.reInsertNode(node);

                            // Add edge to cluster
                            cluster.createEdge(node, currNode);

                            if (cluster.isDirected()) {
                                // Add edge in other direction if it existed in the original graph
                                if (adjacencyMatrix[i][node.index()]) {
                                    cluster.createEdge(currNode, node);
                                }
                            } else {
                                // Add edge in other direction if cluster is undirected
                                cluster.createEdge(currNode, node);
                            }

                        }
                    }
                }
            }
        }

        return cluster;
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
    public double calculatePenalty(CustomGraph graph, CustomGraph cluster) {
        double penalty = 0;

        if (cluster.nodeCount() > maxClusterSize || minClusterSize < cluster.nodeCount()) {
            double minPenalty = (h1 * (minClusterSize-cluster.nodeCount())/(minClusterSize - 1));
            double maxPenalty = (h2 * (cluster.nodeCount() - maxClusterSize)/(graph.nodeCount() - maxClusterSize));

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
    public CustomGraph iterativeScan(CustomGraph graph, CustomGraph cluster, boolean[][] adjacencyMatrix) {
        // Highest known weight of the cluster
        double w = calculateWeight(graph, cluster);
        boolean increased = true;

        NodeCursor graphNodes = graph.nodes();

        while (increased) {
            while (graphNodes.ok()) {
                Node node = graphNodes.node();

                CustomGraph tempCluster = cluster;

                if (cluster.contains(node)) {
                    tempCluster.removeNode(node);
                } else {
                    tempCluster = reAddNode(tempCluster, node, adjacencyMatrix);
                }

                if (calculatePenalty(graph, tempCluster) > calculatePenalty(graph, cluster)) {
                    cluster = tempCluster;
                }

                graphNodes.next();
            }

            double newWeight = calculatePenalty(graph, cluster);
            if (newWeight == w) {
                increased = false;
            } else {
                w = newWeight;
            }
        }

        return cluster;
    }
}
