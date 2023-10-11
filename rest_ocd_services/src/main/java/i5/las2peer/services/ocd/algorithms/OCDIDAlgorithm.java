package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.graphs.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
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
     * Creates a standard instance of the algorithm. All attributes are assigned there default values.
     */
    public OCDIDAlgorithm() {
    }

    @Override
    public Cover detectOverlappingCommunities(CustomGraph graph) throws InterruptedException{
        //Initialization of information
        double d_max = graph.getMaxWeightedInDegree();      //max degree of graph
        int nodeCount = graph.getNodeCount();               //number od nodes
        double[][] I_uv = new double[nodeCount][nodeCount];              //Information flow from u to v

        double[] I_v = new double[nodeCount];              //List of current information of the nodes
        int[] triangles = new int[nodeCount];
        double[] CC = new double[nodeCount];
        double[] avg_d = new double[nodeCount];
        double[] avg_s = new double[nodeCount];
        double[][] JS = new double[nodeCount][nodeCount];
        double[][] CS = new double[nodeCount][nodeCount];

        Iterator<Node> nodesIt = graph.nodes().iterator();
        Node node;
        while(nodesIt.hasNext()) {
            node = nodesIt.next();
            int nodeID = node.getIndex();
            triangles[nodeID] = triangles(graph, node);

            for (Node neighbour : graph.getNeighbours(node)) {
                JS[nodeID][neighbour.getIndex()]=jaccardCoeff(graph, node, neighbour);
                CS[nodeID][neighbour.getIndex()]=contact_strength(graph, node, neighbour, triangles[nodeID]);
            }

            CC[nodeID] = clusteringCoeff(node, triangles[nodeID]);
            avg_d[nodeID] = avgDegreeNeighbours(graph, node);
            avg_s[nodeID] = avgSimilarityNeighbours(graph, node, JS);

            double degree = (node.getDegree()/2);
            I_v[nodeID] = (degree * CC[nodeID]) / d_max;
        }

        //Spread information
        double I_max = 1;
        while (I_max > thresholdOCDID) {
            I_max = 0;
            double[] I_new = I_v.clone();
            Iterator<Edge> edgesIt = graph.edges().iterator();
            Edge edge;
            while(edgesIt.hasNext()) {
                edge = edgesIt.next();
                Node node1 = edge.getSourceNode();
                Node node2 = edge.getTargetNode();
                int node1ID = node1.getIndex();
                int node2ID = node2.getIndex();

                if (I_v[node1ID] > I_v[node2ID]) { // information flows from node1 to node2
                    double I_vu = (Math.exp(I_v[node1ID] - I_v[node2ID]) - 1) * ((1 / (1 + Math.exp(-5 * CC[node1ID] * CC[node2ID]))) - 0.5) * JS[node1ID][node2ID] * CS[node1ID][node2ID];
                    double I_vu_cost = (Math.exp(I_v[node1ID] - I_v[node2ID]) - 1) * (1 - JS[node1ID][node2ID]) * (avg_s[node2ID] / avg_d[node2ID]);
                    double I_in = I_vu - I_vu_cost;

                    if (I_in > 0) {
                        I_new[node2ID] += I_in;
                        I_uv[node1ID][node2ID] += I_in;
                        if (I_in > I_max) {
                            I_max = I_in;
                        }
                    }
                }
            }
            I_v = I_new;
        }

        //Community detection
        Matrix communities = cd(graph, I_v);
        Matrix overlapping_communities = ocd_fair_and_good(graph, communities, I_uv);

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
     * @param node the node for which a clustering coefficient is to be calculated
     * @param t_v the amount of triangles for the node, which equals the amount of existing edges
     * @return The clustering coefficient
     * @throws InterruptedException if the thread was interrupted
     */
    private double clusteringCoeff(Node node, int t_v) throws InterruptedException {
        int neighboursCount = (node.getDegree() / 2);
        if (neighboursCount < 2) {
            return 0.0;
        }

        double possibleEdges = (neighboursCount * (neighboursCount - 1)) / 2;
        double existingEdges = t_v;

        return existingEdges / possibleEdges;
    }

    /**
     * Calculates the jaccard similarity between two nodes
     *
     * @param graph the graph in which the nodes are located
     * @param node1 the node for which the jaccard similarity to a neighbouring should be calculated
     * @param node2 a neighbouring node of node1
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
     * Calculates the contact strength between node1 and node2
     *
     * @param graph in which the nodes are located
     * @param node1 the node with a smaller information volume
     * @param node2 the node with a higher information volume
     * @param trianglesNode1 the amount of triangles for node1
     * @return the contact strength
     * @throws InterruptedException if the thread was interrupted
     */
    private double contact_strength(CustomGraph graph, Node node1, Node node2, int trianglesNode1) throws InterruptedException {
        int T_node1 = trianglesNode1;

        //count common neighbours
        int commonNeighboursCount = 0;
        for (Node neighbour1 : graph.getNeighbours(node1)) {
            for (Node neighbour2 : graph.getNeighbours(node2)) {
                if (neighbour1.equals(neighbour2)) {
                    commonNeighboursCount++;
                }
            }
        }

        //compute contact strength
        if(T_node1 > 0) {
            return ((double) commonNeighboursCount) / ((double) T_node1);
        }else {
            return 0.0;
        }
    }

    /**
     * Calculates the amount of triangles for a node
     *
     * @param graph the graph in which the node is located
     * @param node the node for which the amount of triangles should be calculated
     * @return The number of triangles for the node
     * @throws InterruptedException if the thread was interrupted
     */
    private int triangles(CustomGraph graph, Node node) throws InterruptedException {
        int T_v = 0;
        for (Node neighbour1 : graph.getNeighbours(node)) {
            for (Node neighbour2 : graph.getNeighbours(node)) {
                if (neighbour1 != neighbour2 && containsEdge(graph, neighbour1, neighbour2)) {
                    T_v++;
                }
            }
        }
        return T_v / 2;
    }

    /**
     * Checks if an edge between two specific nodes exists in the graph
     *
     * @param graph the graph in which the nodes are located
     * @param node1 the node for which the existence of an edge to node2 should be checked
     * @param node2 analogous to node1
     * @return true if the graph contains an edge between node1 and node2, false otherwise
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
     * @param graph the graph in which the node is located
     * @param node the node for which the average degree of the neighbouring nodes should be calculated
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
        }else {
            return ((double) totalDegree) / ((double) neighbourCount);
        }
    }

    /**
     * Calculates the average jaccard similarity coefficient of neighbouring nodes of specific node have.
     *
     * @param graph the graph in which the node is located
     * @param node the node for which the average jaccard similarity coefficient of the neighbouring nodes should be calculated
     * @param JS a two-dimensional List of the jaccard similarity coefficients
     * @return the average similarity the neighbouring nodes
     * @throws InterruptedException if the thread was interrupted
     */
    private double avgSimilarityNeighbours(CustomGraph graph, Node node, double[][] JS) throws InterruptedException{
        double neighbourCount = 0;
        double totalSimilarity = 0;
        for (Node neighbour : graph.getNeighbours(node)) {
            double neighbourSimilarity = JS[node.getIndex()][neighbour.getIndex()];
            neighbourCount++;
            totalSimilarity += neighbourSimilarity;
        }
        if (neighbourCount == 0) {
            return 0.0;
        }else {
            return totalSimilarity / neighbourCount;
        }
    }

    /**
     * Community detection method that assigns nodes with the same information volume to the same community.
     *
     * @param graph the graph on which the community detection should be performed
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
            int nodeID = node.getIndex();
            if (getMemberships(communities, nodeID).isEmpty()) {
                for (Node neighbour : graph.getNeighbours(node)) {
                    double informationNode = informationList[nodeID];
                    int neighbourID = neighbour.getIndex();
                    double informationNeighbour = informationList[neighbourID];
                    List<Integer> communitiesNode = getMemberships(communities, nodeID);
                    List<Integer> communitiesNeighbour = getMemberships(communities, neighbourID);

                    if (Math.abs(informationNode - informationNeighbour) < thresholdCD) {
                        if (!communitiesNeighbour.isEmpty()) {
                            if (!communitiesNode.isEmpty()) {
                                int communityNode = communitiesNode.get(0);
                                int communityNeighbour = communitiesNeighbour.get(0);
                                for(int row=0; row < nodeCount; row++){
                                    if(communities.get(row,communityNode) == 1) {
                                        communities.set(row, communityNode, 0);
                                        communities.set(row, communityNeighbour, 1);
                                    }
                                }
                            } else {
                                communities.set(nodeID, communitiesNeighbour.get(0), 1);
                            }
                        }else{
                            if (!communitiesNode.isEmpty()) {
                                communities.set(neighbourID, communitiesNode.get(0), 1);
                            } else {
                                communities.set(nodeID, neighbourID, 1);
                                communities.set(neighbourID, neighbourID, 1);
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
     * @param graph the graph on which the overlapping community detection should be performed
     * @param communities A matrix that represents the belongingness of nodes to communities, the rows correspond to
     *                    node ids and the columns to communities
     * @param I_uv A two-dimensional list corresponding to the information volume that was exchanges between two nodes
     *             at time t. For example I_uv[ID_node1][ID_node2] corresponds to the total amount of information flow between
     *             node1 and node2
     * @return The detected communities in the form of a matrix. In the matrix the rows corresponds to the nodes and the
     *         columns correspond to the community a node was assigned. The matrix allows to have empty columns.
     * @throws InterruptedException if the thread was interrupted
     */
    private Matrix ocd(CustomGraph graph, Matrix communities, double[][] I_uv) throws InterruptedException{
        List<Node> BN = boundaryNodes(graph, communities);

        for (Node node : BN) {
            int nodeID = node.getIndex();

            Set<Node> neighbours = graph.getNeighbours(node);
            Set<Integer> NC = computeNC(communities, nodeID, neighbours);

            double hightesB = 0.0;                                      //extension so that all nodes are assigned
            int communityOfHighestB = 0;

            for (int community : NC) {
                List<Node> communityMembers = getCommunityMembers(graph, communities, community);
                double B = belongingDegree(node, neighbours, communityMembers, I_uv);

                if (B > thresholdOCD) {
                    communities.set(nodeID, community, 1);
                }

                if(B > hightesB){                                       //extension so that all nodes are assigned
                    communityOfHighestB = community;
                }
            }
            if (getMemberships(communities, nodeID).isEmpty()){        //extension so that all nodes are assigned
                if (hightesB > 0.0) {
                    communities.set(nodeID, communityOfHighestB, 1);
                }else{                                                                //there was no information flow from or to the node then it becomes an own community
                    communities.set(nodeID, nodeID, 1);
                }
            }
        }
        return communities;
    }

    private Matrix ocd_fair(CustomGraph graph, Matrix communities, double[][] I_uv) throws InterruptedException{
        List<Node> BN = boundaryNodes(graph, communities);
        Matrix oc = communities.copy();

        for (Node node : BN) {
            int nodeID = node.getIndex();

            Set<Node> neighbours = graph.getNeighbours(node);
            Set<Integer> NC = computeNC(oc, nodeID, neighbours);

            double hightesB = 0.0;                                      //extension so that all nodes are assigned
            int communityOfHighestB = 0;

            for (int community : NC) {
                List<Node> communityMembers = getCommunityMembers(graph, communities, community);
                double B = belongingDegree(node, neighbours, communityMembers, I_uv);

                if (B > thresholdOCD) {
                    oc.set(nodeID, community, 1);
                }

                if(B > hightesB){                                       //extension so that all nodes are assigned
                    communityOfHighestB = community;
                }
            }
            if (getMemberships(communities, nodeID).isEmpty()){        //extension so that all nodes are assigned
                if (hightesB > 0.0) {
                    oc.set(nodeID, communityOfHighestB, 1);
                }else{                                                                //there was no information flow from or to the node then it becomes an own community
                    oc.set(nodeID, nodeID, 1);
                }
            }
        }
        return oc;
    }

    private Matrix ocd_fair_and_good(CustomGraph graph, Matrix communities, double[][] I_uv) throws InterruptedException{
        List<Node> BN = boundaryNodes(graph, communities);
        boolean changes = true;
        while(changes){
            changes=false;
            for (Node node : BN) {
                int nodeID = node.getIndex();

                Set<Node> neighbours = graph.getNeighbours(node);
                Set<Integer> NC = computeNC(communities, nodeID, neighbours);

                double hightesB = 0.0;                                      //extension so that all nodes are assigned
                int communityOfHighestB = 0;

                for (int community : NC) {
                    List<Node> communityMembers = getCommunityMembers(graph, communities, community);
                    double B = belongingDegree(node, neighbours, communityMembers, I_uv);

                    if (B > thresholdOCD) {
                        communities.set(nodeID, community, 1);
                        changes = true;
                    }

                    if(B > hightesB){                                       //extension so that all nodes are assigned
                        communityOfHighestB = community;
                    }
                    if (getMemberships(communities, nodeID).isEmpty()){        //extension so that all nodes are assigned
                        if (hightesB > 0.0) {
                            communities.set(nodeID, communityOfHighestB, 1);
                            changes = true;
                        }
                    }
                }
            }
        }
        for (Node node : BN) {
            List<Integer> communitiesNode = getMemberships(communities, node.getIndex());
            int nodeID = node.getIndex();
            if (communitiesNode.isEmpty()) {
                communities.set(nodeID, nodeID, 1);                                         //add nodes with no community
            }
        }
        return communities;
    }

    /**
     * Determines a list consisting of boundary nodes and nodes with no community. This list consists all nodes that are
     * candidates to be assigned to a community in the next step
     *
     * @param graph the graph in which the boundary nodes are searched for
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
     * Computes the community set to which the nodes neighbours belong
     *
     * @param communities A matrix corresponding to the detected communities
     * @param nodeID The id of the node
     * @return The set of communities the nodes neighbours belong to
     * @throws InterruptedException if the thread was interrupted
     */
    private Set<Integer> computeNC(Matrix communities, int nodeID, Set<Node> neighbours) throws InterruptedException{
        Set<Integer> NC = new HashSet<>();
        for(Node neighbour : neighbours){
            Set<Integer> communitiesOfNeighbour = new HashSet<>(getMemberships(communities, neighbour.getIndex()));
            Set<Integer> communitiesOfNode = new HashSet<>(getMemberships(communities, nodeID));
            communitiesOfNeighbour.removeAll(communitiesOfNode);
            NC.addAll(communitiesOfNeighbour);
        }
        return NC;
    }

    /**
     * Determines the members of a community
     *
     * @param graph the graph in which the community members are searched for
     * @param communities the matrix corresponding to the detected communities
     * @param community the community which members are searched for
     * @return A List of nodes containing the members of the input community
     * @throws InterruptedException if the thread was interrupted
     */
    private List<Node> getCommunityMembers(CustomGraph graph, Matrix communities, int community) throws InterruptedException {
        List<Node> communityMembers = new ArrayList<>();
        Iterator<Node> nodesIt = graph.nodes().iterator();

        while (nodesIt.hasNext()) {
            Node node = nodesIt.next();
            if (communities.get(node.getIndex(), community) == 1) {
                communityMembers.add(node);
            }
        }

        return communityMembers;
    }

    /**
     * Looks up the communities a node is assigned to
     *
     * @param matrix The matrix corresponding to the detected communities
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
     * Calculates the belonging degree of a node to a community
     *
     * @param node the node for which the belonging degree should be calculated
     * @param neighbours the neighbours of the node
     * @param communityMembers the members of the community to which the belonging degree should be calculated
     * @param I_uv A two-dimensional list corresponding to the information volume that was exchanges between two nodes
     *             during the information simulation. For example I_uv[ID_node1][ID_node2] corresponds to the total
     *             amount of information flow between node1 and node2
     * @return The belonging degree of the input node to the input community
     * @throws InterruptedException if the thread was interrupted
     */
    private double belongingDegree(Node node, Set<Node> neighbours, List<Node> communityMembers, double[][] I_uv) throws InterruptedException{
        //compute BT
        List<Node> intersection = new ArrayList<>();

        for (Node neighbour : neighbours) {
            if (communityMembers.contains(neighbour)) {
                intersection.add(neighbour);
            }
        }
        double BT = intersection.size() / (double) (node.getDegree()/2);

        //compute BI
        double IsumIntersection = 0;
        double IsumNeighbours = 0;
        int nodeID = node.getIndex();
        for (Node neighbour : intersection) {
            int neighbourID = neighbour.getIndex();
            IsumIntersection += (I_uv[nodeID][neighbourID] + I_uv[neighbourID][nodeID]);
        }
        for (Node neighbour : neighbours) {
            int neighbourID = neighbour.getIndex();
            IsumNeighbours += (I_uv[nodeID][neighbourID] + I_uv[neighbourID][nodeID]);
        }
        double BI = 0.0;
        if(IsumIntersection != 0 && IsumNeighbours != 0) {
            BI = IsumIntersection / IsumNeighbours;
        }

        return 0.5 * (BI + BT);
    }

    /**
     * Transforms the input matrix to a membership matrix by deleting all empty columns
     *
     * @param oc A matrix consisting of the overlapping communities where empty columns are possible
     * @return A matrix where all empty columns where deleted, now the number of columns corresponds to the number of communities
     * @throws InterruptedException if the thread was interrupted
     */
    private Matrix toMembershipMatrix(Matrix oc) throws InterruptedException{
        int numRows = oc.rows();
        int numCols = oc.columns();

        boolean[] keepColumns = new boolean[numCols];
        int remainingColumnsCount = 0;

        for (int col = 0; col < numCols; col++) {
            for (int row = 0; row < numRows; row++) {
                if (oc.get(row, col) != 0) {
                    keepColumns[col] = true;
                    remainingColumnsCount++;
                    break;
                }
            }
        }

        Matrix membershipMatrix = new Basic2DMatrix(numRows, remainingColumnsCount);
        int newCol = 0;
        for (int col = 0; col < numCols; col++) {
            if (keepColumns[col]) {
                for (int row = 0; row < numRows; row++) {
                    membershipMatrix.set(row, newCol, oc.get(row, col));
                }
                newCol++;
            }
        }

        return membershipMatrix;
    }
}