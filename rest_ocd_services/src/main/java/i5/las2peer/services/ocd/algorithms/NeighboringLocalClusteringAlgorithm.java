package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.*;
import org.graphstream.graph.Node;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import i5.las2peer.services.ocd.graphs.DescriptiveVisualization;

/**
 * Implementation of the Neighboring Local Clustering ALgorithm for overlapping community detection
 * This implementation is based on the definition by Yan Wang, Qiong Chen, Lili Yang, Sen Yang, Kai He and Xuping Xie:
 * Overlapping Structures Detection in Protein-Protein Interaction Networks Using Community Detection Algorithm Based on Neighbor Clustering Coefficient
 * https://doi.org/10.3389/fgene.2021.689515
 * @author Ibrahim Saifadin
 */
public class NeighboringLocalClusteringAlgorithm implements OcdAlgorithm {
    /**
     * The similarity threshold alpha
     * Default value is 0.3
     * Must be in [0, 1]
     */
    private double similarityThresholdAlpha = 0.3;

    /**
     * The pruning threshold prune
     * Default value is 0.3
     * Must be in [0, 1]
     */
    private double pruningThreshold = 0.3;

    /**
     * The community magnetic interference coefficient GF
     * GF is the coefficient of CMI theory used to revise the value of influence F
     * Default value is 1.0
     * Must be in (0, infinity), usually |E| / |V| is a good choice
     */
    private double communityMagneticInterferenceCoefficientGF = 1.0;

    // PARAMETER NAMES
    protected static final String SIMILARITY_THRESHOLD_ALPHA = "similarity threshold alpha";

    protected static final String PRUNING_THRESHOLD = "pruning threshold";

    protected static final String COMMUNITY_MAGNETIC_INTERFERENCE_COEFFICIENT_GF  = "community magnetic interference coefficient GF";

    /**
     * Creates a standard instance of the algorithm
     * All attributes are assigned their default values
     */
    public NeighboringLocalClusteringAlgorithm() {
    }

    @Override
    public CoverCreationType getAlgorithmType() {
        return CoverCreationType.NEIGHBORING_LOCAL_CLUSTERING_ALGORITHM;
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(SIMILARITY_THRESHOLD_ALPHA, Double.toString(similarityThresholdAlpha));
        parameters.put(PRUNING_THRESHOLD, Double.toString(pruningThreshold));
        parameters.put(COMMUNITY_MAGNETIC_INTERFERENCE_COEFFICIENT_GF, Double.toString(communityMagneticInterferenceCoefficientGF));
        return parameters;
    }

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
        if (parameters.containsKey(SIMILARITY_THRESHOLD_ALPHA)) {
            similarityThresholdAlpha = Double.parseDouble(parameters.get(SIMILARITY_THRESHOLD_ALPHA));
            if (similarityThresholdAlpha < 0 || similarityThresholdAlpha > 1) {
                throw new IllegalArgumentException();
            }
            parameters.remove(SIMILARITY_THRESHOLD_ALPHA);
        }
        if (parameters.containsKey(PRUNING_THRESHOLD)) {
            pruningThreshold = Double.parseDouble(parameters.get(PRUNING_THRESHOLD));
            if (pruningThreshold < 0 || pruningThreshold > 1) {
                throw new IllegalArgumentException();
            }
            parameters.remove(PRUNING_THRESHOLD);
        }
        if (parameters.containsKey(COMMUNITY_MAGNETIC_INTERFERENCE_COEFFICIENT_GF)) {
            communityMagneticInterferenceCoefficientGF = Double.parseDouble(parameters.get(COMMUNITY_MAGNETIC_INTERFERENCE_COEFFICIENT_GF));
            if (communityMagneticInterferenceCoefficientGF < 0) {
                throw new IllegalArgumentException();
            }
            parameters.remove(COMMUNITY_MAGNETIC_INTERFERENCE_COEFFICIENT_GF);
        }
        if (parameters.size() > 0) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Set<GraphType> compatibleGraphTypes() {
        Set<GraphType> compatibilities = new HashSet<>();
        return compatibilities;
    }

    /* DV: variables */
    public DescriptiveVisualization dv = new DescriptiveVisualization();
    public HashMap<Integer, Double> nodeNumericalValues = new HashMap<>();
    public HashMap<Integer, String> nodeStringValues = new HashMap<>();
    public HashMap<ArrayList<Integer>, String> edgeStringValues = new HashMap<>();

    @Override
    public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
        // The adjacency matrix A of the input graph
        Matrix A = graph.getNeighbourhoodMatrix();

        // The adjacency list of the input graph
        HashMap<Integer, ArrayList<Integer>> adjacencyList = createAdjacencyList(A);

        if (DescriptiveVisualization.getVisualize()) {
            /* DV: set description file and delimiter */
            dv.setDescriptions("NLC.txt", ";");
            dv.addComponent(graph);
        }
        if (DescriptiveVisualization.getVisualize()) {
            HashMap<Integer, String> labels = new HashMap<>();
            for (int i = 0; i < graph.getNodeCount(); i++) {
                ArrayList<Integer> neighbours_i = new ArrayList<>();
                for (Node neighbor : graph.getNeighbours(graph.getNode(i))) {
                    neighbours_i.add(dv.getRealNode(neighbor.getIndex()));
                }
                labels.put(dv.getRealNode(i), "neighbours: " + neighbours_i);
            }
            /* DV: set node labels to the neighboring nodes */
            dv.setNodeLabels(labels);
        }

        // The similarity matrix, which contains all sim(u, v) values for nodes u, v in V
        HashMap<Integer, HashMap<Integer, Double>> similarityMatrix = getSimilarityMatrix(adjacencyList);

        // The central node set (Phase 1)
        HashSet<Integer> centralNodeSet = getCentralNodeSet(adjacencyList, similarityMatrix);

        // The central edge sets and the non-central edges in the last entry (Phase 2)
        HashMap<Integer, ArrayList<ArrayList<Integer>>> centralEdgeSets = getCentralEdgeSets(centralNodeSet, similarityMatrix, adjacencyList);

        // The communities, where the non-central edges are assigned to their corrosponding entral edge set and the overlapping nodes in the last entry (Phase 3)
        HashMap<Integer, ArrayList<Integer>> communitiesAndOverlappingNodes = getCommunitiesAndOverlappingNodes(A, centralEdgeSets, adjacencyList);

        // The optimized communities, where the overlapping nodes are assigned to suitable communities (Phase 4)
        ConcurrentHashMap<Integer, ArrayList<Integer>> optimizedCommunities = getOptimizedCommunities(A, communitiesAndOverlappingNodes);

        // The membership matrix, where membershipMatrix[n][c] == 1, iff node n is part of community c
        Matrix membershipMatrix = getMembershipMatrix(A, optimizedCommunities);

        // Build the cover using the input graph and the membership matrix built above
        Cover cover = new Cover(graph, membershipMatrix);

        if (DescriptiveVisualization.getVisualize()) {
            /* DV: set final cover */
            dv.setCover(12, cover);
        }

        return cover;
    }

    /**
     * This method creates the adjacency list of the adjacency matrix
     * Entry adjacencyList.get(i) represents the neighborhood of node v_i
     * @param adjacencyMatrix is the adjacency matrix on which the adjacency list should be built
     * @return the adjacency list based on the adjacency matrix
     */
    public HashMap<Integer, ArrayList<Integer>> createAdjacencyList(Matrix adjacencyMatrix) {
        HashMap<Integer, ArrayList<Integer>> adjacencyList = new HashMap<>();
        for (int i = 0; i < adjacencyMatrix.columns(); i++) {
            ArrayList<Integer> neighbors = new ArrayList<>();
            for (int j = 0; j < adjacencyMatrix.columns(); j++) {
                if (adjacencyMatrix.get(i, j) == 1) {
                    neighbors.add(j);
                }
            }
            adjacencyList.put(i, neighbors);
        }
        return adjacencyList;
    }

    /**
     * This method determines the similarity matrix for the nodes in the input graph
     * Entry similarityMatrix.get(i).get(j) represents sim(v_i, v_j)
     * @param adjacencyList is the adjacency list for which the similarity matrix should be built
     * @return the similarity matrix
     */
    public static HashMap<Integer, HashMap<Integer, Double>> getSimilarityMatrix(HashMap<Integer, ArrayList<Integer>> adjacencyList) {
        HashMap<Integer, HashMap<Integer, Double>> similarityMatrix = new HashMap<>();
        for (int i : adjacencyList.keySet()) {
            List<Integer> neighbours_i = adjacencyList.get(i);
            HashMap<Integer, Double> similarity = new HashMap<>();
            for (int j : adjacencyList.keySet()) {
                List<Integer> neighbours_j = adjacencyList.get(j);
                Set<Integer> union = new HashSet<>(neighbours_i);
                Set<Integer> intersection = new HashSet<>(neighbours_i);
                for (int el : neighbours_j) {
                    if (!neighbours_i.contains(el)) {
                        union.add(el);
                    }
                }
                intersection.retainAll(neighbours_j);
                double value = (double) intersection.size() / union.size();
                similarity.put(j, value);
            }
            similarityMatrix.put(i, similarity);
        }
        return similarityMatrix;
    }

    /**
     * This method uses Dijkstra's algorithm to create the shortest path matrix for the nodes
     * Entry shortestPathMatrix[i][j] represents the shortest path from node v_i to node v_j
     * @param A is the adjacency matrix of the input graph
     * @return the shortest path matrix
     */
    public int[][] dijkstraAlgorithm(Matrix A) {
        int inf = Integer.MAX_VALUE;
        int n = A.columns();
        int[][] shortestPathMatrix = new int[n][n];

        for (int i = 0; i < n; i++) {
            int[] distance_i = new int[n];
            Arrays.fill(distance_i, inf);
            distance_i[i] = 0;
            boolean[] visited = new boolean[n];
            PriorityQueue<Integer> queue = new PriorityQueue<>((x, y) -> distance_i[x] - distance_i[y]);
            queue.offer(i);

            while (!queue.isEmpty()) {
                int currentNode = queue.poll();
                if (visited[currentNode]) {
                    continue;
                }
                visited[currentNode] = true;
                for (int j = 0; j < n; j++) {
                    if (A.get(currentNode, j) == 1) {
                        int oldCost = distance_i[j];
                        int newCost = distance_i[currentNode] + 1;
                        if (newCost < oldCost) {
                            distance_i[j] = newCost;
                            queue.offer(j);
                        }
                    }
                }
            }
            shortestPathMatrix[i] = distance_i;
        }
        return shortestPathMatrix;
    }

    /**
     * This method determines the membership matrix of the input graph
     * Entry membershipMatrix.get(n).get(c) == 1, iff node n is in community c
     * @param A is the adjacency matrix of the input graph
     * @param communities are the determined communities of the input graph
     * @return the membership matrix
     */
    public Matrix getMembershipMatrix(Matrix A, ConcurrentHashMap<Integer, ArrayList<Integer>> communities) {
        Matrix membershipMatrix = new Basic2DMatrix(A.columns(), communities.size());
        for (int i = 0; i < A.columns(); i++) {
            int comIndex = 0;
            for (int key : communities.keySet()) {
                ArrayList<Integer> community = communities.get(key);
                if (community.contains(i)) {
                    membershipMatrix.set(i, comIndex, 1);
                }
                comIndex += 1;
            }
        }
        return membershipMatrix;
    }

    /**
     * This method determines the number of connections of overlapping node n and the non-overlapping nodes in community j
     * @param A is the adjacency matrix of the input graph
     * @param overlappingNodes are the determined overlapping nodes of the input graph
     * @param n is the node for which the number of connections will be calculated
     * @param communityJ is the community j
     * @return connections of the node n with the non-overlapping nodes in community j
     */
    public int getConnections(Matrix A, ArrayList<Integer> overlappingNodes, int n, ArrayList<Integer> communityJ) {
        HashSet<Integer> nonOverlappingNodes_j = new HashSet<>(communityJ);
        nonOverlappingNodes_j.removeAll(overlappingNodes);
        int connections = 0;
        for (int j : nonOverlappingNodes_j) {
            if (A.get(n, j) == 1) {
                connections++;
            }
        }
        return connections;
    }

    /**
     * This method determines the communities to which an overlapping node belongs
     * @param communities are the determined communities of the input graph
     * @param n is the node for which the belonging communities will be calculated
     * @return the communities to which the overlapping node belongs
     */
    public ArrayList<Integer> getCommunities(HashMap<Integer, ArrayList<Integer>> communities, int n) {
        ArrayList<Integer> com_n = new ArrayList<>();
        for (int key : communities.keySet()) {
            if (communities.get(key).contains(n)) {
                com_n.add(key);
            }
        }
        return com_n;
    }

    /**
     * This method determines the central node set
     * @param adjacencyList is the adjacency list of the input graph
     * @param similarityMatrix is the similarity matrix of the nodes
     * @return the central node set
     * @throws InterruptedException if the thread was interrupted
     */
    protected HashSet<Integer> getCentralNodeSet(HashMap<Integer, ArrayList<Integer>> adjacencyList, HashMap<Integer, HashMap<Integer, Double>> similarityMatrix) throws InterruptedException {
        // Number of connections between the neighboring nodes of u: K(u)
        HashMap<Integer, Integer> connection_K = new HashMap<>();
        // Local clustering coefficient of u: C(u)
        HashMap<Integer, Double> clusteringCoefficient_C = new HashMap<>();
        for (int i = 0; i < adjacencyList.size(); i++) {
            int connection_i = 0;
            double clusteringCoefficient = 0.0;
            ArrayList<Integer> neighbours_i = adjacencyList.get(i);
            int numberOfNeighbors_i = neighbours_i.size();

            for (int v : neighbours_i) {
                for (int w : neighbours_i) {
                    if (v != w && adjacencyList.get(v).contains(w)) {
                        connection_i++;
                    }
                }
            }
            connection_K.put(i, connection_i / 2);

            if (numberOfNeighbors_i != 1) {
                clusteringCoefficient = (double) 2 * connection_K.get(i) / (numberOfNeighbors_i * (numberOfNeighbors_i - 1));
            }
            clusteringCoefficient_C.put(i, clusteringCoefficient);
        }

        // Influence of each node u (for candidate central nodes): F(u)
        HashMap<Integer, Double> influence_F = new HashMap<>();
        // Influence between two nodes u and v: IB(u, v)
        HashMap<Integer, HashMap<Integer, Double>> influence_IB = new HashMap<>();
        for (int i = 0; i < adjacencyList.size(); i++) {
            ArrayList<Integer> neighbours_i = adjacencyList.get(i);
            HashMap<Integer, Double> influence_IB_i = new HashMap<>();
            for (int j = 0; j < adjacencyList.size(); j++) {
                if (i == j) {
                    influence_IB_i.put(j, 0.0);
                }
                else {
                    ArrayList<Integer> neighbours_j = adjacencyList.get(j);
                    double value = neighbours_i.size() * neighbours_j.size() / ((1 - similarityMatrix.get(i).get(j)) * (1 - similarityMatrix.get(i).get(j)));
                    influence_IB_i.put(j, value);
                }
            }
            influence_IB.put(i, influence_IB_i);
            double influence_F_i = 0.0;
            for (int j : neighbours_i) {
                if (i != j) {
                    influence_F_i += influence_IB.get(i).get(j) * (1 + clusteringCoefficient_C.get(i)) * (1 + clusteringCoefficient_C.get(j));
                }
            }
            influence_F.put(i, influence_F_i);
        }
        if (DescriptiveVisualization.getVisualize()) {
            for (int i : influence_F.keySet()) {
                nodeNumericalValues.put(i, influence_F.get(i));
            }
            /* DV: set influence values */
            dv.setNodeNumericalValues(1, nodeNumericalValues);
            nodeNumericalValues.clear();
        }

        // Select the central nodes
        HashSet<Integer> centralNodes = new HashSet<>();
        for (int i = 0; i < adjacencyList.size(); i++) {
            ArrayList<Integer> neighbours_i = adjacencyList.get(i);
            boolean centralNode_i = true;
            for (int j : neighbours_i) {
                if (influence_F.get(i) < influence_F.get(j)) {
                    centralNode_i = false;
                    break;
                }
            }
            if (DescriptiveVisualization.getVisualize()) {
                if (centralNode_i) {
                    nodeNumericalValues.put(i, influence_F.get(i));
                }
            }
            for (int node : centralNodes) {
                if (similarityMatrix.get(i).get(node) > similarityThresholdAlpha) {
                    centralNode_i = false;
                    break;
                }
            }
            if (DescriptiveVisualization.getVisualize()) {
                if (centralNode_i) {
                    double maxSim = 0.0;
                    for (int v : centralNodes) {
                        if (maxSim < similarityMatrix.get(i).get(v)) {
                            maxSim = similarityMatrix.get(i).get(v);
                        }
                    }
                    nodeStringValues.put(i, "max sim(" + dv.getRealNode(i) + ", v) = " + maxSim + " <= " + similarityThresholdAlpha);
                }
            }
            if (centralNode_i) {
                centralNodes.add(i);
                double newInfluence_i = 0.0;
                for (int j : neighbours_i) {
                    newInfluence_i += influence_IB.get(i).get(j);
                }
                influence_F.put(i, communityMagneticInterferenceCoefficientGF * newInfluence_i);
            }
        }
        if (DescriptiveVisualization.getVisualize()) {
            /* DV: mark nodes with maximal influence values in neighborhood */
            dv.setNodeNumericalValues(2, nodeNumericalValues);
            nodeNumericalValues.clear();
            /* DV: set similarity values that are less than alpha */
            dv.setNodeStringValues(3, nodeStringValues);
            nodeStringValues.clear();
            for (int i : centralNodes) {
                nodeStringValues.put(i, "CN " + dv.getRealNode(i));
            }
            /* DV: set central nodes */
            dv.setNodeStringValues(4, nodeStringValues);
            nodeStringValues.clear();
        }

        return centralNodes;
    }

    /**
     * This method gives a transformation of the central node set to the corresponding central edge sets
     * @param centralNodeSet is the central node set, where each node corresponds to a central edge set
     * @param similarityMatrix is the similarity matrix for nodes
     * @param adjacencyList is the adjacency list of the input graph
     * @return all central edge sets and the non-central edges in the last entry
     * @throws InterruptedException if the thread was interrupted
     */
    protected HashMap<Integer, ArrayList<ArrayList<Integer>>> getCentralEdgeSets(HashSet<Integer> centralNodeSet, HashMap<Integer, HashMap<Integer, Double>> similarityMatrix, HashMap<Integer, ArrayList<Integer>> adjacencyList) throws InterruptedException {
        HashMap<Integer, ArrayList<ArrayList<Integer>>> centralEdgeSets = new HashMap<>();
        // Determines the average similarity of all central nodes and split their edges into central and non-central edge set
        HashMap<Integer, Double> avgSimilarity = new HashMap<>();
        for (int u : centralNodeSet) {
            double avgSimilarity_u = 0.0;
            ArrayList<Integer> neighbours_u = adjacencyList.get(u);
            for (int v : neighbours_u) {
                avgSimilarity_u += similarityMatrix.get(u).get(v);
            }
            avgSimilarity_u /= neighbours_u.size();
            avgSimilarity.put(u, avgSimilarity_u);
            if (DescriptiveVisualization.getVisualize()) {
                nodeNumericalValues.put(u, avgSimilarity_u);
            }
            ArrayList<ArrayList<Integer>> centralEdges_u = new ArrayList<>();
            for (int v : neighbours_u) {
                if (similarityMatrix.get(u).get(v) > avgSimilarity_u) {
                    ArrayList<Integer> edge = new ArrayList<>();
                    if (u < v) {
                        edge.add(u);
                        edge.add(v);
                    }
                    else {
                        edge.add(v);
                        edge.add(u);
                    }
                    centralEdges_u.add(edge);
                }
            }
            if (DescriptiveVisualization.getVisualize()) {
                /* DV: set average similarity values of the central nodes */
                dv.setNodeNumericalValues(5, nodeNumericalValues);
                nodeNumericalValues.clear();
            }
            centralEdgeSets.put(u, centralEdges_u);
        }
        if (DescriptiveVisualization.getVisualize()) {
            for (int u : centralNodeSet) {
                for (ArrayList<Integer> edge : centralEdgeSets.get(u)) {
                    edgeStringValues.put(edge, "sim(" + dv.getRealNode(edge.get(0)) + ", " + dv.getRealNode(edge.get(1)) + ") = " + similarityMatrix.get(edge.get(0)).get(edge.get(1)) + " > " + avgSimilarity.get(u));
                }
                /* DV: set current central edge sets around the central nodes */
                dv.setEdgeStringValues(6, edgeStringValues);
                edgeStringValues.clear();
            }
        }
        // The remaining edges in the input graph are added to the non-central edge set
        ArrayList<ArrayList<Integer>> nonCentralEdges = new ArrayList<>();
        for (int i = 0; i < adjacencyList.size(); i++) {
            ArrayList<Integer> neighbours_i = adjacencyList.get(i);
            for (int j : neighbours_i) {
                ArrayList<Integer> edge = new ArrayList<>();
                if (i < j) {
                    edge.add(i);
                    edge.add(j);
                }
                else {
                    edge.add(j);
                    edge.add(i);
                }
                if (!nonCentralEdges.contains(edge)) {
                    boolean isNonCentralEdge = true;
                    for (int u : centralNodeSet) {
                        if (centralEdgeSets.get(u).contains(edge)) {
                            isNonCentralEdge = false;
                        }
                    }
                    if (isNonCentralEdge == true) {
                        nonCentralEdges.add(edge);
                    }
                }
            }
        }

        // Set the last entry of centralEdgeSets to the non-central edges
        int maxKey = 0;
        for (int key : centralEdgeSets.keySet()) {
            if (maxKey < key) {
                maxKey = key;
            }
        }
        centralEdgeSets.put(maxKey + 1, nonCentralEdges);
        if (DescriptiveVisualization.getVisualize()) {
            for (ArrayList<Integer> nce : nonCentralEdges) {
                edgeStringValues.put(nce, "NCE");
            }
            /* DV: mark all non-central edges */
            dv.setEdgeStringValues(7, edgeStringValues);
            edgeStringValues.clear();
        }

        return centralEdgeSets;
    }

    /**
     * In this method the non-central edges are clustered into suitable central edge sets
     * @param A is the adjacency matrix of the input graph
     * @param centralEdgeSets are all central edge sets and the non-central edges in the last entry
     * @param adjacencyList is the adjacency list of the input graph
     * @return all determined communities and the overlapping nodes in the last entry
     * @throws InterruptedException if the thread was interrupted
     */
    protected HashMap<Integer, ArrayList<Integer>> getCommunitiesAndOverlappingNodes(Matrix A, HashMap<Integer, ArrayList<ArrayList<Integer>>> centralEdgeSets, HashMap<Integer, ArrayList<Integer>> adjacencyList) throws InterruptedException {
        int nonCentralEdgesKey = 0;
        for (int key : centralEdgeSets.keySet()) {
            if (nonCentralEdgesKey < key) {
                nonCentralEdgesKey = key;
            }
        }
        ArrayList<ArrayList<Integer>> nonCentralEdges = centralEdgeSets.get(nonCentralEdgesKey);
        centralEdgeSets.remove(nonCentralEdgesKey);

        // The edge set of the input graph and the edge indices
        HashMap<Integer, ArrayList<Integer>> edgeSet = new HashMap<>();
        HashMap<ArrayList<Integer>, Integer> edgeIndices = new HashMap<>();
        int edgeIndex = 0;
        for (int i = 0; i < adjacencyList.size(); i++) {
            for (int j : adjacencyList.get(i)) {
                ArrayList<Integer> edge = new ArrayList<>();
                if (i < j) {
                    edge.add(i);
                    edge.add(j);
                    edgeSet.put(edgeIndex, edge);
                    edgeIndices.put(edge, edgeIndex);
                    edgeIndex += 1;
                }
            }
        }

        // The Jaccard matrix, where jaccardMatrix[i][j] represents the jaccard distance of edge e_i to edge e_j
        double[][] jaccardMatrix = new double[edgeSet.size()][edgeSet.size()];
        // The Link matrix, where linkMatrix[i][j] represents the shortest path between the nodes of edge e_i to edge e_j
        int[][] linkMatrix = new int[edgeSet.size()][edgeSet.size()];
        // The shortest path matrix for nodes of the graph
        int[][] shortestPathMatrix = dijkstraAlgorithm(A);
        // The distance matrix for edges of the graph, where distanceMatrix[i][j] = jaccardMatrix[i][j] * linkMatrix[i][j]
        double[][] distanceMatrix = new double[edgeSet.size()][edgeSet.size()];

        for (int key1 = 0; key1 < edgeSet.size(); key1++) {
            List<Integer> edge1 = edgeSet.get(key1);
            List<Integer> neighbours_a = adjacencyList.get(edge1.get(0));
            List<Integer> neighbours_b = adjacencyList.get(edge1.get(1));
            List<Integer> union1 = new ArrayList<>(neighbours_a);
            for (int el : neighbours_b) {
                if (!neighbours_a.contains(el)) {
                    union1.add(el);
                }
            }
            for (int key2 = key1+1; key2 < edgeSet.size(); key2++) {
                List<Integer> edge2 = edgeSet.get(key2);
                List<Integer> neighbours_c = adjacencyList.get(edge2.get(0));
                List<Integer> neighbours_d = adjacencyList.get(edge2.get(1));
                List<Integer> union2 = new ArrayList<>(neighbours_c);
                for (int el : neighbours_d) {
                    if (!neighbours_c.contains(el)) {
                        union2.add(el);
                    }
                }
                List<Integer> intersection = new ArrayList<>(union1);
                intersection.retainAll(union2);

                // Jaccard distance for edge1 and edge2
                double jaccard = 1 - ((double) intersection.size() / (union1.size() + union2.size() - intersection.size()));
                jaccardMatrix[key1][key1] = 0.0;
                jaccardMatrix[key1][key2] = jaccard;
                jaccardMatrix[key2][key1] = jaccard;

                // Shortest path from edge1 to edge2
                int ac = shortestPathMatrix[edge1.get(0)][edge2.get(0)];
                int ad = shortestPathMatrix[edge1.get(0)][edge2.get(1)];
                int bc = shortestPathMatrix[edge1.get(1)][edge2.get(0)];
                int bd = shortestPathMatrix[edge1.get(1)][edge2.get(1)];
                int link = Math.min(Math.min(ac + bd, ad + bc), Math.min(ac + ad, bc + bd));
                linkMatrix[key1][key1] = 0;
                linkMatrix[key1][key2] = link;
                linkMatrix[key2][key1] = link;

                // The distance matrix, where distance_matrix[i][j] = jaccard_matrix[i][j] * link_matrix[i][j] for edge e_i and edge e_j
                double distance = jaccardMatrix[key1][key2] * linkMatrix[key1][key2];
                distanceMatrix[key1][key1] = 0.0;
                distanceMatrix[key1][key2] = distance;
                distanceMatrix[key2][key1] = distance;
            }
        }

        // The average distance matrix DLS, where dlsMatrix.get(e.index()).get(CE.key()) = sum_{v \in CE} distanceMatrix[e][v] / CE.size() for non-central edge e and central edge set CE
        HashMap<Integer, HashMap<Integer, Double>> dlsMatrix = new HashMap<>();
        int nonCentralEdgeIndex = 0;
        for (ArrayList<Integer> nce : nonCentralEdges) {
            double[] nceDistances = distanceMatrix[(edgeIndices.get(nce))];
            HashMap<Integer, Double> dls_nce = new HashMap<>();
            for (int cesKey : centralEdgeSets.keySet()) {
                ArrayList<ArrayList<Integer>> centralEdgeSet = centralEdgeSets.get(cesKey);
                double sum = 0.0;
                for (ArrayList<Integer> centralEdge : centralEdgeSet) {
                    sum += nceDistances[edgeIndices.get(centralEdge)];
                }
                dls_nce.put(cesKey, sum / centralEdgeSet.size());
            }
            dlsMatrix.put(nonCentralEdgeIndex, dls_nce);
            nonCentralEdgeIndex += 1;
        }

        // Add every non-central edge e to the central edge set CE with the smallest average distance DLS(e, CE)
        nonCentralEdgeIndex = 0;
        for (ArrayList<Integer> nce : nonCentralEdges) {
            double minimum_dls = Double.MAX_VALUE;
            for (int key : centralEdgeSets.keySet()) {
                double temp = dlsMatrix.get(nonCentralEdgeIndex).get(key);
                if (temp < minimum_dls) {
                    minimum_dls = temp;
                }
            }
            for (int key : centralEdgeSets.keySet()) {
                if (dlsMatrix.get(nonCentralEdgeIndex).get(key) == minimum_dls) {
                    centralEdgeSets.get(key).add(nce);
                }
            }
            nonCentralEdgeIndex += 1;
        }
        if (DescriptiveVisualization.getVisualize()) {
            HashMap<ArrayList<Integer>, ArrayList<Integer>> assignments = new HashMap<>();
            for (ArrayList<Integer> nce : nonCentralEdges) {
                ArrayList<Integer> assigned = new ArrayList<>();
                for (int ces : centralEdgeSets.keySet()) {
                    if (centralEdgeSets.get(ces).contains(nce)) {
                        assigned.add(ces);
                    }
                }
                assignments.put(nce, assigned);
            }
            for (int ces : centralEdgeSets.keySet()) {
                for (ArrayList<Integer> nce : nonCentralEdges) {
                    if (assignments.get(nce).size() == 1 && centralEdgeSets.get(ces).contains(nce)) {
                        edgeStringValues.put(nce, "CES " + ces);
                    }
                    if (assignments.get(nce).size() > 1 && centralEdgeSets.get(ces).contains(nce)) {
                        edgeStringValues.put(nce, "CES " + assignments.get(nce));
                    }
                }
                /* DV: assign non-central edge to central-edge set with smallest average distance */
                dv.setEdgeStringValues(8, edgeStringValues);
                edgeStringValues.clear();
            }
        }

        // The node identifier indicates in how many central edge sets a node occurs
        HashMap<Integer, Integer> nodeIdentifier = new HashMap<>();
        for (int node = 0; node < adjacencyList.size(); node++) {
            int identified = 0;
            for (int ces_key : centralEdgeSets.keySet()) {
                if (identified < 2) {
                    ArrayList<ArrayList<Integer>> ces = centralEdgeSets.get(ces_key);
                    for (ArrayList<Integer> edge : ces) {
                        if (node == edge.get(0) || node == edge.get(1)) {
                            identified += 1;
                            break;
                        }
                    }
                }
                else {
                    break;
                }
            }
            nodeIdentifier.put(node, identified);
        }

        // The overlapping nodes occur in at least two central edge sets
        ArrayList<Integer> overlappingNodes = new ArrayList<>();
        for (int node = 0; node < adjacencyList.size(); node++) {
            if (nodeIdentifier.get(node) > 1) {
                overlappingNodes.add(node);
            }
        }

        // The corresponding determined communities and the overlapping nodes are in the last entry
        HashMap<Integer, ArrayList<Integer>> communitiesAndOverlappingNodes = new HashMap<>();
        HashSet<Integer> community = new HashSet<>();
        int comIndex = 1;
        HashMap<Integer, ArrayList<Integer>> communities = new HashMap<>();

        for (int key : centralEdgeSets.keySet()) {
            ArrayList<ArrayList<Integer>> centralEdgeSet = centralEdgeSets.get(key);
            for (ArrayList<Integer> centralEdge : centralEdgeSet) {
                if (!community.contains(centralEdge.get(0))) {
                    community.add(centralEdge.get(0));
                }
                if (!community.contains(centralEdge.get(1))) {
                    community.add(centralEdge.get(1));
                }
            }
            ArrayList<Integer> communityList = new ArrayList<>(community);
            Collections.sort(communityList);
            communitiesAndOverlappingNodes.put(key, communityList);

            communities.put(comIndex, communityList);
            comIndex += 1;
            community.clear();
        }

        if (DescriptiveVisualization.getVisualize()) {
            HashMap<Integer, ArrayList<Integer>> comIndices = new HashMap<>();
            for (int i = 0; i < adjacencyList.size(); i++) {
                ArrayList<Integer> com_i = new ArrayList<>();
                for (int key : communities.keySet()) {
                    ArrayList<Integer> com = communities.get(key);
                    if (com.contains(i)) {
                        com_i.add(key);
                    }
                }
                comIndices.put(i, com_i);
            }
            for (int com : communities.keySet()) {
                for (int i = 0; i < adjacencyList.size(); i++) {
                    if (communities.get(com).contains(i) && comIndices.get(i).size() == 1) {
                        nodeStringValues.put(i, "community " + comIndices.get(i).get(0));
                    }
                }
                dv.setNodeStringValues(9, nodeStringValues);
                nodeStringValues.clear();
            }
            for (int i = 0; i < adjacencyList.size(); i++) {
                if (comIndices.get(i).size() > 1) {
                    nodeStringValues.put(i, "communities " + comIndices.get(i));
                }
            }
            /* DV: set the communities to which the nodes currently belong */
            dv.setNodeStringValues(9, nodeStringValues);
            nodeStringValues.clear();

            for (int on : overlappingNodes) {
                nodeStringValues.put(on, "ON");
            }
            /* DV: mark all overlapping nodes */
            dv.setNodeStringValues(10, nodeStringValues);
            nodeStringValues.clear();
        }

        communitiesAndOverlappingNodes.put(nonCentralEdgesKey, overlappingNodes);
        return communitiesAndOverlappingNodes;
    }

    /**
     * Community Optimization: method to optimize the excessive overlapping nodes of the previous step
     * @param A is the adjacency matrix of the input graph
     * @param communitiesAndOverlappingNodes are the determined communities and overlapping nodes of the previous step
     * @return a cover containing the detected communities
     * @throws InterruptedException If the thread was interrupted
     */
    protected ConcurrentHashMap<Integer, ArrayList<Integer>> getOptimizedCommunities(Matrix A, HashMap<Integer, ArrayList<Integer>> communitiesAndOverlappingNodes) throws InterruptedException {
        int overlapping_key = 0;
        for (int key : communitiesAndOverlappingNodes.keySet()) {
            if (overlapping_key < key) {
                overlapping_key = key;
            }
        }
        ArrayList<Integer> overlappingNodes = communitiesAndOverlappingNodes.get(overlapping_key);
        communitiesAndOverlappingNodes.remove(overlapping_key);
        HashMap<Integer, ArrayList<Integer>> communities = communitiesAndOverlappingNodes;

        // Store the communities of each overlapping node
        HashMap<Integer, ArrayList<Integer>> communities_ON = new HashMap<>();
        for (int n : overlappingNodes) {
            ArrayList<Integer> communities_n = getCommunities(communities, n);
            communities_ON.put(n, communities_n);
        }

        // The ratio of the connections between the overlapping nodes and the non-overlapping parts in each community will be determined
        HashMap<Integer, ArrayList<Double>> ratio = new HashMap<>();
        for (int n : overlappingNodes) {
            ArrayList<Integer> communities_n = communities_ON.get(n);
            double sum_connections = 0.0;
            ArrayList<Integer> connection_n_NonOverlap = new ArrayList<>();
            for (int j : communities_n) {
                int connection_n_NonOverlap_j = getConnections(A, overlappingNodes, n, communities.get(j));
                connection_n_NonOverlap.add(connection_n_NonOverlap_j);
                sum_connections += connection_n_NonOverlap_j;
            }
            ArrayList<Double> ratio_j = new ArrayList<>();
            for (int j = 0; j < communities_n.size(); j++) {
                int connection_j = connection_n_NonOverlap.get(j);
                ratio_j.add(connection_j / sum_connections);
            }
            ratio.put(n, ratio_j);
        }

        // Overlapping node n is removed from each community where the ratio between n and the non-overlapping part in the community is less than the pruning threshold
        for (int n : overlappingNodes) {
            ArrayList<Integer> removeFromCommunities = new ArrayList<>();
            for (int j = 0; j < communities_ON.get(n).size(); j++) {
                if (ratio.get(n).get(j) < pruningThreshold) {
                    for (int index = 0; index < communities.get(communities_ON.get(n).get(j)).size(); index++) {
                        if (communities.get(communities_ON.get(n).get(j)).get(index) == n) {
                            removeFromCommunities.add(communities_ON.get(n).get(j));
                            communities.get(communities_ON.get(n).get(j)).remove(index);
                        }
                    }
                }
            }
            if (DescriptiveVisualization.getVisualize()) {
                if (removeFromCommunities.size() > 0) {
                    ArrayList<Integer> removed = new ArrayList<>();
                    for (int r : removeFromCommunities) {
                        removed.add(dv.getRealNode(r));
                    }
                    nodeStringValues.put(n, "remove from community of node " + removed);
                }
            }
        }
        if (DescriptiveVisualization.getVisualize()) {
            /* DV: remove the node from the communities, where the community-ratio is smaller than the pruning threshold */
            dv.setNodeStringValues(11, nodeStringValues);
            nodeStringValues.clear();
        }
        // The overlapping node n which does not belong to any community will be assigned to the community with the largest connection ratio
        for (int n : overlappingNodes) {
            boolean withoutCommunity = true;
            for (int j : communities.keySet()) {
                ArrayList<Integer> community = communities.get(j);
                if (community.contains(n)) {
                    withoutCommunity = false;
                    break;
                }
            }
            if (withoutCommunity) {
                double maximum = Collections.max(ratio.get(n));
                for (int j = 0; j < communities_ON.get(n).size(); j++) {
                    if (ratio.get(n).get(j) == maximum) {
                        communities.get(communities_ON.get(n).get(j)).add(n);
                        Collections.sort(communities.get(communities_ON.get(n).get(j)));
                        break;
                    }
                }
            }
        }

        ConcurrentHashMap<Integer, ArrayList<Integer>> optimizedCommunities = new ConcurrentHashMap<>(communities);

        // The community whose size is less than 3 will be deleted
        for (int com_key : optimizedCommunities.keySet()) {
            ArrayList<Integer> community = optimizedCommunities.get(com_key);
            if (community.size() < 3) {
                optimizedCommunities.remove(com_key);
            }
        }

        return optimizedCommunities;
    }

}