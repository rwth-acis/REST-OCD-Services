package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.graphs.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiNode;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implements the algorithm to the OCDID (Overlapping Community Detection based on Information Dynamics) method, by Z. Sun, B. Wang, J. Sheng,Z. Yu, J. Shao:
 * https://doi.org/10.1109/ACCESS.2018.2879648
 * Handles undirected and unweighted graphs.
 */
public class OCDIDAlgorithm implements OcdAlgorithm {

    /**
     * The threshold value used for spreading the information in the network.
     */
    private double thresholdOCDID = 0.001;
    /**
     * The threshold value used in the community detection phase of the algorithm.
     */
    private double thresholdCD = 0.001;
    /**
     * The threshold value used in the overlapping community detection phase of the algorithm.
     */
    private double thresholdOCD = 0.2;

    /*
     * PARAMETER NAMES
     */

    protected static final String THRESHOLD_OCDID_NAME = "thresholdOCDID";

    protected static final String THRESHOLD_CD_NAME = "thresholdCD";

    protected static final String THRESHOLD_OCD_NAME = "thresholdOCD";

    /**
     * Creates a standard instance of the algorithm. All attributes are assigned
     * there default values.
     */
    public OCDIDAlgorithm() {
    }

    @Override
    public Cover detectOverlappingCommunities(CustomGraph graph) throws InterruptedException{
        //compute initial information and values needed later
        double d_max = graph.getMaxWeightedInDegree();      //max degree of graph
        int nodeCount = graph.getNodeCount();               //number od nodes
        List<double[][]> I_uv = new ArrayList<>();          //Information flow from u to v at time t

        //compute initial Information
        double[] I_v = new double[nodeCount];              //List of current information of the nodes
       
        Iterator<Node> nodesIt = graph.nodes().iterator();
        Node node;
        while(nodesIt.hasNext()) {
            node = nodesIt.next();
            double degree = (node.getDegree()/2);
            double clusteringCoeff = clusteringCoeff(graph, node);
            I_v[node.getIndex()] = (degree * clusteringCoeff) / d_max;
        }

        //spread information
        double I_max = 1;
        while (I_max > thresholdOCDID) {
            double[][] I_uv_t = new double[nodeCount][nodeCount];
            I_max = 0;
            double[] I_new = I_v.clone();
            Iterator<Edge> edgesIt = graph.edges().iterator();
            Edge edge;
            while(edgesIt.hasNext()) {
                edge = edgesIt.next();
                Node node1 = edge.getSourceNode();
                Node node2 = edge.getTargetNode();
                double JS = jaccardCoeff(graph, node1, node2);
                double CC_node1 = clusteringCoeff(graph, node1);
                double CC_node2 = clusteringCoeff(graph, node2);

                if (I_v[node1.getIndex()] < I_v[node2.getIndex()]) { // 2->1
                    double CS = contact_strength(graph, node1, node2);
                    double avg_degree_neighbours = avgDegreeNeighbours(graph, node1);
                    double avg_similarity_neighbours = avgSimilarityNeighbours(graph, node1);

                    double I_vu = (Math.exp(I_v[node2.getIndex()] - I_v[node1.getIndex()]) - 1) * ((1 / (1 + Math.exp(-5 * CC_node1 * CC_node2))) - 0.5) * JS * CS;
                    double I_vu_cost = (Math.exp(I_v[node2.getIndex()] - I_v[node1.getIndex()]) - 1) * (1 - JS) * (avg_similarity_neighbours / avg_degree_neighbours);
                    double I_in = I_vu - I_vu_cost;

                    if (I_in > 0) {
                        I_new[node1.getIndex()] += I_in;
                        I_uv_t[node2.getIndex()][node1.getIndex()] = I_in;
                        if (I_in > I_max) {
                            I_max = I_in;
                        }
                    }
                } else if (I_v[node1.getIndex()] > I_v[node2.getIndex()]) { // 1->2
                    double CS = contact_strength(graph, node2, node1);
                    double avg_degree_neighbours = avgDegreeNeighbours(graph, node2);
                    double avg_similarity_neighbours = avgSimilarityNeighbours(graph, node2);

                    double I_vu = (Math.exp(I_v[node1.getIndex()] - I_v[node2.getIndex()]) - 1) * ((1 / (1 + Math.exp(-5 * CC_node1 * CC_node2))) - 0.5) * JS * CS;
                    double I_vu_cost = (Math.exp(I_v[node2.getIndex()] - I_v[node1.getIndex()]) - 1) * (1 - JS) * (avg_similarity_neighbours / avg_degree_neighbours);
                    double I_in = I_vu - I_vu_cost;

                    if (I_in > 0) {
                        I_new[node2.getIndex()] += I_in;
                        I_uv_t[node1.getIndex()][node2.getIndex()] = I_in;
                        if (I_in > I_max) {
                            I_max = (int) I_in;
                        }
                    }
                }
            }
            I_v = I_new;
            I_uv.add(I_uv_t);
        }

        //community detection
        Matrix communities = cd(graph, I_v);
        Matrix overlapping_communities = ocd(graph, communities, I_uv);

        Matrix membershipMatrix = toMembershipMatrix(overlapping_communities);
        return new Cover(graph, membershipMatrix);
    }

    @Override
    public CoverCreationType getAlgorithmType() {
        return CoverCreationType.OCDID_ALGORITHM;
    }

    @Override
    public Set<GraphType> compatibleGraphTypes() {
        Set<GraphType> compatibilities = new HashSet<GraphType>();
        return compatibilities;
    }

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
        if(parameters.containsKey(THRESHOLD_OCDID_NAME)) {
            thresholdOCDID = Double.parseDouble(parameters.get(THRESHOLD_OCDID_NAME));
            if(thresholdOCDID < 0) {
                throw new IllegalArgumentException();
            }
            parameters.remove(THRESHOLD_OCDID_NAME);
        }
        if(parameters.containsKey(THRESHOLD_CD_NAME)) {
            thresholdCD = Double.parseDouble(parameters.get(THRESHOLD_CD_NAME));
            if(thresholdCD < 0) {
                throw new IllegalArgumentException();
            }
            parameters.remove(THRESHOLD_CD_NAME);
        }
        if(parameters.containsKey(THRESHOLD_OCD_NAME)) {
            thresholdOCD = Double.parseDouble(parameters.get(THRESHOLD_OCD_NAME));
            if(thresholdOCD < 0) {
                throw new IllegalArgumentException();
            }
            parameters.remove(THRESHOLD_OCD_NAME);
        }
        if(parameters.size() > 0) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(THRESHOLD_OCDID_NAME, Double.toString(thresholdOCDID));
        parameters.put(THRESHOLD_CD_NAME, Double.toString(thresholdCD));
        parameters.put(THRESHOLD_OCD_NAME, Double.toString(thresholdOCD));
        return parameters;
    }

    /**
     * Calculates the clustering coefficient of a node
     *
     * @param graph
     * @param node
     * @return The clustering coefficient
     * @throws InterruptedException if the thread was interrupted
     */
    private double clusteringCoeff(CustomGraph graph, Node node) throws InterruptedException {
        int neighboursCount = (node.getDegree()/2);
        if (neighboursCount < 2) {
            return 0.0;
        }

        double possibleEdges = (neighboursCount * (neighboursCount - 1)) / 2;

        double existingEdges = 0;
        for (Node neighbour1 : graph.getNeighbours(node)) {
            for (Node neighbour2 : graph.getNeighbours(node)) {
                if (neighbour1 != neighbour2 && containsEdge(graph, neighbour1, neighbour2)) {
                    existingEdges++;
                }
            }
        }
        existingEdges /= 2;

        return existingEdges / possibleEdges;
    }

    /**
     * Calculates the jaccard similarity coefficient of a node
     *
     * @param graph
     * @param node1
     * @param node2
     * @return The jaccard similarity coefficient
     * @throws InterruptedException if the thread was interrupted
     */
    private double jaccardCoeff(CustomGraph graph, Node node1, Node node2) throws InterruptedException {
        Set<Node> neighbours1 = new HashSet<>(graph.getNeighbours(node1));
        Set<Node> neighbours2 = new HashSet<>(graph.getNeighbours(node2));

        int intersectionSize = 0;
        for (Node neighbour : neighbours1) {
            if (neighbours2.contains(neighbour)) {
                intersectionSize++;
            }
        }

        int unionSize = neighbours1.size() + neighbours2.size() - intersectionSize;

        if (unionSize == 0) {
            return 0.0;
        } else {
            return (double) intersectionSize / unionSize;
        }
    }

    /**
     * Calculates the contact strength of node1 and node2
     *
     * @param graph
     * @param node1 the node with a smaller information volume
     * @param node2 the node with a higher information volume
     * @return the contact strength
     * @throws InterruptedException if the thread was interrupted
     */
    private double contact_strength(CustomGraph graph, Node node1, Node node2) throws InterruptedException {
        //count common neighbours
        int commonNeighboursCount = 0;
        for (Node neighbour1 : graph.getNeighbours(node1)) {
            for (Node neighbour2 : graph.getNeighbours(node2)) {
                if (neighbour1.equals(neighbour2)) {
                    commonNeighboursCount++;
                }
            }
        }

        //count triangles
        int T_v = 0;
        for (Node neighbour1 : graph.getNeighbours(node1)) {
            for (Node neighbour2 : graph.getNeighbours(node1)) {
                if (neighbour1 != neighbour2 && containsEdge(graph, neighbour1, neighbour2)) {
                    T_v++;
                }
            }
        }
        T_v /= 2;

        //compute contact strength
        if(T_v > 0) {
            return ((double) commonNeighboursCount) / ((double) T_v);
        }else {
            return 0;
        }
    }

    /**
     * Checks if an edge between two specific nodes exists in the graph
     *
     * @param graph
     * @param node1
     * @param node2
     * @return true if the graph conatins an edge between node1 and node2, false otherwise
     * @throws InterruptedException if the thread was interrupted
     */
    private boolean containsEdge(CustomGraph graph, Node node1, Node node2) throws InterruptedException{
        int node1_id = node1.getIndex();
        int node2_id = node2.getIndex();
        Iterator<Edge> edgesIt = graph.edges().iterator();
        Edge edge;
        while(edgesIt.hasNext()) {
            edge = edgesIt.next();
            int source = edge.getSourceNode().getIndex();
            int target = edge.getTargetNode().getIndex();
            if (((source==node1_id) && (target==node2_id)) || ((source==node2_id) && (target==node1_id))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the average degree the neighbouring nodes of specific node have
     *
     * @param graph
     * @param node the node the average degree of the neighbours should be calculated for
     * @return the average degree the neighbouring nodes
     * @throws InterruptedException if the thread was interrupted
     */
    private double avgDegreeNeighbours(CustomGraph graph, Node node) throws InterruptedException{
        int neighbourCount = 0;
        int totalDegree = 0;
        for (Node neighbour : graph.getNeighbours(node)) {
            int neighbourDegree = (neighbour.getDegree() / 2);
            neighbourCount++;
            totalDegree += neighbourDegree;
        }
        if (neighbourCount == 0) {
            return 0.0;
        }
        return ((double) totalDegree) / ((double) neighbourCount);
    }

    /**
     * Calculates the average jaccard similarity coefficient of neighbouring nodes of specific node have.
     *
     * @param graph
     * @param node the node the average similarity of the neighbours should be calculated for
     * @return the average similarity the neighbouring nodes
     * @throws InterruptedException if the thread was interrupted
     */
    private double avgSimilarityNeighbours(CustomGraph graph, Node node) throws InterruptedException{
        double neighbourCount = 0;
        double totalSimilarity = 0;
        for (Node neighbour : graph.getNeighbours(node)) {
            double neighbourSimilarity = jaccardCoeff(graph, node, neighbour);
            neighbourCount++;
            totalSimilarity += neighbourSimilarity;
        }
        if (neighbourCount == 0) {
            return 0.0;
        }
        return totalSimilarity / neighbourCount;
    }

    /**
     * Community detection method that assigns nodes with the same information volume to the same community.
     *
     * @param graph
     * @param informationList A List which index corresponds to the node id and the value at a specific index
     *                        corresponds to the information the node had after the information simulation
     * @return The detected communities in the form of a matrix. In the matrix the rows corresponds to the nodes and the
     *         columns correspond to the community a node was assigned. The matrix allows to have empty columns.
     * @throws InterruptedException if the thread was interrupted
     */
    private Matrix cd(CustomGraph graph, double[] informationList) throws InterruptedException{
        int nodeCount = graph.getNodeCount();                           //number of nodes
        Matrix communities = new Basic2DMatrix(nodeCount, nodeCount);
        Iterator<Node> nodesIt = graph.nodes().iterator();
        Node node;
        while(nodesIt.hasNext()) {
            node = nodesIt.next();
            if (getMemberships(communities, node.getIndex()).isEmpty()) {
                for (Node neighbour : graph.getNeighbours(node)) {
                    double informationNode = informationList[node.getIndex()];
                    double informationNeighbour = informationList[neighbour.getIndex()];

                    if (Math.abs(informationNode - informationNeighbour) < thresholdCD) {
                        if (!getMemberships(communities, neighbour.getIndex()).isEmpty()) {
                            if (!getMemberships(communities, node.getIndex()).isEmpty()) {
                                int communityNode = getMemberships(communities, node.getIndex()).get(0);
                                int communityNeighbour = getMemberships(communities, neighbour.getIndex()).get(0);
                                for(int row=0; row < nodeCount; row++){
                                    if(communities.get(row,communityNode) == 1) {
                                        communities.set(row, communityNode, 0);
                                        communities.set(row, communityNeighbour, 1);
                                    }
                                }
                            } else {
                                communities.set(node.getIndex(), getMemberships(communities, neighbour.getIndex()).get(0), 1);
                            }
                        }else{
                            if (!getMemberships(communities, node.getIndex()).isEmpty()) {
                                communities.set(neighbour.getIndex(), getMemberships(communities, node.getIndex()).get(0), 1);
                            } else {
                                communities.set(node.getIndex(), neighbour.getIndex(), 1);
                                communities.set(neighbour.getIndex(), neighbour.getIndex(), 1);
                            }
                        }
                    }
                }
            }
        }
        return communities;
    }

    /**
     * Overlapping community detection method that assigns nodes to a community if their belonging degree is above a
     * threshold. The belonging degree is calculated for boundary nodes and all nodes with no community.
     *
     * @param graph
     * @param communities A matrix that represents the belongingness of nodes to communities, the rows correspond to
     *                    node ids and the columns to communities
     * @param I_uv A two-dimensional list corresponding to the information volume that was exchanges between two nodes
     *             at time t. For example I_uv.get(time)[ID_node1][ID_node2] corresponds to the information flow between
     *             node1 and node2 at time t.
     * @return The detected communities in the form of a matrix. In the matrix the rows corresponds to the nodes and the
     *         columns correspond to the community a node was assigned. The matrix allows to have empty columns.
     * @throws InterruptedException if the thread was interrupted
     */
    private Matrix ocd(CustomGraph graph, Matrix communities, List<double[][]> I_uv) throws InterruptedException{
        List<Node> BN = boundaryNodes(graph, communities);

        for (Node node : BN) {

            //compute NC
            Set<Integer> NC = new HashSet<>();                  //Community set to which node v neighbours belong
            for(Node neighbour : graph.getNeighbours(node)){
                Set<Integer> communitiesOfNeighbour = new HashSet<>(getMemberships(communities, neighbour.getIndex()));
                Set<Integer> communitiesOfNode = new HashSet<>(getMemberships(communities, node.getIndex()));
                communitiesOfNeighbour.removeAll(communitiesOfNode);
                NC.addAll(communitiesOfNeighbour);
            }

            double hightesB = 0.0;              //extension so that all nodes are assigned
            int communityOfHighestB = 0;
            boolean informationFlowToNeighbours = false;

            for (int community : NC) {
                List<Node> communityMembers = new ArrayList<>();
                Iterator<Node> nodesIt2 = graph.nodes().iterator();
                Node node2;
                while(nodesIt2.hasNext()) {
                    node2 = nodesIt2.next();
                    if (communities.get(node2.getIndex(), community) == 1) {
                        communityMembers.add(node2);
                    }
                }

                List<Node> intersectionNeighboursCommunity = new ArrayList<>(graph.getNeighbours(node));
                intersectionNeighboursCommunity.retainAll(communityMembers);

                double BT = intersectionNeighboursCommunity.size() / (double) (node.getDegree()/2);

                double IsumIntersection = 0;
                double IsumNeighbours = 0;

                for (Node neighbour : intersectionNeighboursCommunity) {
                    for (int time = 0; time < I_uv.size(); time++) {
                        IsumIntersection += (I_uv.get(time)[node.getIndex()][neighbour.getIndex()] + I_uv.get(time)[neighbour.getIndex()][node.getIndex()]);
                    }
                }

                for (Node neighbour : graph.getNeighbours(node)) {
                    for (int time = 0; time < I_uv.size(); time++) {
                        IsumNeighbours += (I_uv.get(time)[node.getIndex()][neighbour.getIndex()] + I_uv.get(time)[neighbour.getIndex()][node.getIndex()]);
                    }
                }

                double BI = IsumIntersection / IsumNeighbours;
                double B = 0.5 * (BI + BT);

                if(B > hightesB){                           //extension so that all nodes are assigned
                    communityOfHighestB = community;
                    informationFlowToNeighbours = true;
                }

                if (B > thresholdOCD && !communityMembers.contains(node)) {
                    communities.set(node.getIndex(), community, 1);
                }
            }
            if (!getMemberships(communities, node.getIndex()).isEmpty()){             //extension so that all nodes are assigned
                if (informationFlowToNeighbours) {
                    communities.set(node.getIndex(), communityOfHighestB, 1);
                }else{                                                                //there was no information flow from or to the node then it becomes an own community
                    communities.set(node.getIndex(), node.getIndex(), 1);
                }
            }
        }
        return communities;
    }

    /**
     * Determines a list consisting of boundary nodes and nodes with no community. This list consists all nodes that are
     * candidates to be assigned to a community in the next step
     *
     * @param graph
     * @param communities A matrix that represents the belongingness of nodes to communities, the rows correspond to
     *                    node ids and the columns to communities
     * @return A list of consisting of boundary nodes and nodes with no community
     * @throws InterruptedException if the thread was interrupted
     */
    private List<Node> boundaryNodes(CustomGraph graph, Matrix communities) throws InterruptedException {
        List<Node> BN = new ArrayList<>();

        Iterator<Node> nodesIt = graph.nodes().iterator();
        Node node;
        while(nodesIt.hasNext()) {
            node = nodesIt.next();
            List<Integer> communitiesNode = getMemberships(communities, node.getIndex());

            if (communitiesNode.isEmpty()){
                BN.add(node);                                           //add nodes with no community
            }else {
                for (Node neighbour : graph.getNeighbours(node)) {
                    List<Integer> communitiesNeighbour = getMemberships(communities, neighbour.getIndex());
                    if (!communitiesNeighbour.isEmpty()) {
                        for (Integer element : communitiesNeighbour) {
                            if (!communitiesNode.contains(element)) {
                                BN.add(node);                           //add boundary nodes
                            }
                        }
                    }
                }
            }
        }
        return BN;
    }

    /**
     * Looks up the communities a node is assigned to
     *
     * @param matrix A matrix corresponding to the detected communities
     * @param nodeId The id of the node that communities are going to be returned
     * @return A list of communities the node with nodeId is currently assigned to
     * @throws InterruptedException if the thread was interrupted
     */
    private List<Integer> getMemberships(Matrix matrix, Integer nodeId) throws InterruptedException {
        List<Integer> commMemberships = new ArrayList<>();
        for (int col = 0; col < matrix.rows(); col++) {
            if (matrix.get(nodeId, col) != 0) {
                commMemberships.add(col);
            }
        }
        return commMemberships;
    }

    /**
     * Transforms the input matrix to a membership matrix by deleting all empty columns
     *
     * @param overlapping_communities A matrix consisting of the overlapping_communities where empty columns are possible
     * @return A matrix where all empty columns where deleted, now the number of columns corresponds to the number of communities
     * @throws InterruptedException if the thread was interrupted
     */
    private Matrix toMembershipMatrix(Matrix overlapping_communities) throws InterruptedException{ //remove columns with consisting of only zeros
        int numRows = overlapping_communities.rows();
        int numCols = overlapping_communities.columns();

        boolean[] keepColumns = new boolean[numCols];

        for (int col = 0; col < numCols; col++) {
            boolean isZeroColumn = true;
            for (int row = 0; row < numRows; row++) {
                if (overlapping_communities.get(row, col) != 0) {
                    isZeroColumn = false;
                    break;
                }
            }
            keepColumns[col] = !isZeroColumn;
        }

        int remainingColumnsCount = 0;
        for (boolean keepColumn : keepColumns) {
            if (keepColumn) {
                remainingColumnsCount++;
            }
        }

        Matrix membershipMatrix = new Basic2DMatrix(numRows, remainingColumnsCount);
        int newCol = 0;
        for (int col = 0; col < numCols; col++) {
            if (keepColumns[col]) {
                for (int row = 0; row < numRows; row++) {
                    membershipMatrix.set(row, newCol, overlapping_communities.get(row, col));
                }
                newCol++;
            }
        }

        return membershipMatrix;
    }
}