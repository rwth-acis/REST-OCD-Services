package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.measures.PageRank;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmException;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmExecutor;
import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.utils.Pair;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;

import java.util.*;


/**
 * The original version of the algorithm Rank Removal was published by Baumes et al. in 2005:
 * Finding communities by clustering a graph into overlapping subgraphs
 * ISBN: 972-99353-6-X
 */

/**
 * @author Jan Mortell
 */

// TODO: Description of the algorithm

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
            if (epsilon <= 0) {
                throw new IllegalArgumentException("The parameter epsilon should be greater than 0!");
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

    // TODO: Add put method for enums

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        // parameters.put(WEIGHT_FUNCTION_IS_NAME, );
        // parameters.put(SET_DIFFERENCE_FUNCTION_NAME, );
        parameters.put(EPSILON_NAME, Double.toString(epsilon));
        parameters.put(MAX_FAIL_NAME, Integer.toString(max_fail));
        parameters.put(MIN_CLUSTER_SIZE_NAME, Integer.toString(minClusterSize));
        parameters.put(MAX_CLUSTER_SIZE_NAME, Integer.toString(maxClusterSize));
        parameters.put(H1_NAME, Double.toString(h1));
        parameters.put(H2_NAME, Double.toString(h2));
        // parameters.put(WEIGHT_FUNCTION_RARE_NAME, );
        // parameters.put(RANKING_FUNCTION_NAME, );
        parameters.put(MIN_CORE_SIZE_NAME, Integer.toString(minCoreSize));
        parameters.put(MAX_CORE_SIZE_NAME, Integer.toString(maxCoreSize));
        parameters.put(T_NAME, Integer.toString(t));

        return parameters;
    }

    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // Main algorithm method
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    // Global set of removed nodes
    Set<Node> R = Collections.emptySet();

    // TODO: Add method description
    // TODO: Check try catch

    @Override
    public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException, OcdMetricException {
        // Resulting cover will be assigned here
        Cover outputCover = null;

        // List of all found cluster cores
        List<CustomGraph> clusterCores = Collections.emptyList();

        // Detect all connected components in the graph
        List<CustomGraph> connectedComponents = getConnectedComponents(graph);

        // Method clusterComponent is called for every connected component
        for (int i = 0; i < connectedComponents.size(); i++) {
            CustomGraph cluster = null;
            try {
                cluster = clusterComponent(connectedComponents.get(i));
            } catch (CentralityAlgorithmException e) {
                throw new RuntimeException(e);
            }
            if (cluster != null) {
                clusterCores.add(cluster);
            }
        }

        // Add removed nodes back to cluster cores
        for (int i = 0; i < R.size(); i++) {
            for (int j = 0; j < clusterCores.size(); i++) {
                if ()
            }
        }

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
     * @return connectedComponents         List of connected components in input graph
     */
    public List<CustomGraph> getConnectedComponents(CustomGraph graph) {
        // List of connected components
        GraphProcessor processor = new GraphProcessor();
        List<Pair<CustomGraph, Map<Node, Node>>> connectedComponentsList = processor.divideIntoConnectedComponents(graph);

        // Add graphs of all connected components to list
        List<CustomGraph> connectedComponents = Collections.emptyList();
        for (int i = 0; i < connectedComponentsList.size(); i++) {
            connectedComponents.add((connectedComponentsList.get(i)).getFirst());
        }

        return connectedComponents;
    }

    /**
     * This method takes a connected component as input and checks if its'
     * number of nodes is below maxCoreSize. If not, t nodes are removed
     * and added to set R. This process is repeated until the connected
     * component is either in between the boundaries minCoreSize and maxCoreSize
     * or disconnected in multiple connected components. The method gets
     * recursively executed on all emerging connected components.
     *
     * @param connectedComponent     Connected component as custom graph
     */
    public CustomGraph clusterComponent(CustomGraph connectedComponent) throws InterruptedException, CentralityAlgorithmException {
        Node node = null;

        if (connectedComponent.nodeCount() > maxCoreSize) {
            for (int i = 0; i < t; i++) {
                // Calculate the highest ranking node
                node = getHighestRankingNode(connectedComponent);
                // Add the highest ranking node to set R
                R.add(node);
                // Remove the highest ranking node
                connectedComponent.removeNode(node);
            }
            // Detect all connected components in connectedComponent
            List<CustomGraph> newConnectedComponents = getConnectedComponents(connectedComponent);

            for (int i = 0; i < newConnectedComponents.size(); i++) {
                clusterComponent(newConnectedComponents.get(i));
            }

        } else if (minCoreSize <= connectedComponent.nodeCount() && connectedComponent.nodeCount() <= maxCoreSize) {
            // Mark connectedComponent as cluster core
            return connectedComponent;
        }

        return null;
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
            case PAGE_RANK:
                node = calculatePageRank(graph);
                break;

            case OUT_DEGREE:
                node = calculateOutDegree(graph);
                break;

            default:
                node = null;
                System.out.println("The selected ranking function does not exist!");
                break;
        }

        return node;
    }

    /**
     * This method calculates the pageRank of every node in a graph
     * and returns the node with the highest pageRank in a graph
     *
     * @param graph
     * @return
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
     * This method calculates the out degree of every node
     * in the passed graph and returns the node with the
     * highest out degree
     *
     * @param graph
     * @return
     */
    public Node calculateOutDegree(CustomGraph graph) {
        Node node = null;
        // Array of all nodes in the graph
        Node[] nodes = graph.getNodeArray();

        // Find the node with the highest out degree in the graph
        for (int i = 0; i < nodes.length; i++) {
            if (node == null || nodes[i].outDegree() > node.outDegree()) {
                node = nodes[i];
            }
        }

        return node;
    }
}
