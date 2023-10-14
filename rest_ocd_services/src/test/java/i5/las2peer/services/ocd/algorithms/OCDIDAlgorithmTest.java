package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.junit.Ignore;
import org.junit.Test;

import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import java.util.*;

import static org.junit.Assert.assertEquals;


public class OCDIDAlgorithmTest {
    @Test
    public void testClusteringCoeff() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm(); // instance of OCDID algorithm
        CustomGraph graph = get5NodesTestGraph();

        //general case where some neighbours are connected to each other others not
        double actualCC1 = ocdid.clusteringCoeff(graph.getNode(0), 2);
        double expectedCC = 2.0 / 3;
        assertEquals(expectedCC, actualCC1 , 0.0001);

        //Edge Case: node where all neighbours are connected with each other
        double actualCC2 = ocdid.clusteringCoeff(graph.getNode(2), 1);
        assertEquals(1.0, actualCC2 , 0.0001);

        //Edge Case: node with only one neighbour
        double actualCC3 = ocdid.clusteringCoeff(graph.getNode(4), 0);
        assertEquals(0.0, actualCC3 , 0.0001);
    }

    @Test
    public void testJaccardCoeff() throws  InterruptedException{
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm(); // instance of OCDID algorithm
        CustomGraph graph = get5NodesTestGraph();

        //the two nodes share all neighbours
        double actualJS1 = ocdid.jaccardCoeff(graph, graph.getNode(0), graph.getNode(1));
        assertEquals(1.0, actualJS1 , 0.0001);

        //the two nodes do not share all neighbours
        double actualJS2 = ocdid.jaccardCoeff(graph, graph.getNode(0), graph.getNode(3));
        double expectedJS2 = 3.0 / 5;
        assertEquals(expectedJS2, actualJS2 , 0.0001);
    }

    @Test
    public void testContact_strength() throws  InterruptedException{
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm(); // instance of OCDID algorithm
        CustomGraph graph = get5NodesTestGraph();

        //the two nodes share all neighbours
        double actualCS1 = ocdid.contact_strength(graph, graph.getNode(0), graph.getNode(1), 2);
        assertEquals(1.0, actualCS1 , 0.0001);

        //the two nodes do not share all neighbours
        double actualCS2 = ocdid.contact_strength(graph, graph.getNode(0), graph.getNode(3), 2);
        assertEquals(0.5, actualCS2 , 0.0001);
    }

    @Test
    public void testTriangles() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm(); // instance of OCDID algorithm
        CustomGraph graph = get5NodesTestGraph();

        //general case where some neighbours are connected to each other others not
        double actualT_v1 = ocdid.triangles(graph, graph.getNode(0));
        assertEquals(2.0, actualT_v1 , 0.0001);

        //Edge Case: node where all neighbours are connected with each other
        double actualT_v2 = ocdid.triangles(graph, graph.getNode(2));
        assertEquals(1.0, actualT_v2 , 0.0001);

        //Edge Case: node with only one neighbour
        double actualT_v = ocdid.triangles(graph, graph.getNode(4));
        assertEquals(0.0, actualT_v , 0.0001);
    }

    @Test
    public void testContainsEdge() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm(); // instance of OCDID algorithm
        CustomGraph graph = get5NodesTestGraph();
        boolean containsEdge1 = ocdid.containsEdge(graph, 0, 1);
        assertEquals(true, containsEdge1);

        boolean containsEdge2 = ocdid.containsEdge(graph, 2, 3);
        assertEquals(false, containsEdge2);
    }

    @Test
    public void testAvgDegreeNeighbours() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm(); // instance of OCDID algorithm
        CustomGraph graph = get5NodesTestGraph();

        double avgD1 = ocdid.avgDegreeNeighbours(graph, graph.getNode(0));
        double expectedavgD1 = 8.0 / 3;
        assertEquals(expectedavgD1, avgD1, 0.0001);

        //Edge case: node with only one neighbour
        double avgD2 = ocdid.avgDegreeNeighbours(graph, graph.getNode(4));
        assertEquals(3.0, avgD2, 0.0001);
    }

    @Test
    public void testAvgSimilarityNeighbours() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm(); // instance of OCDID algorithm
        CustomGraph graph = get5NodesTestGraph();

        double[][] JS = new double[5][5];
        JS[0][1]= 1.0;
        JS[1][0]= 1.0;
        JS[0][2]= 3.0 / 4;
        JS[2][0]= 3.0 / 4;
        JS[0][3]= 3.0 / 5;
        JS[3][0]= 3.0 / 5;
        JS[1][2]= 3.0 / 4;
        JS[2][1]= 3.0 / 4;
        JS[1][3]= 3.0 / 5;
        JS[3][1]= 3.0 / 5;
        JS[3][4]= 2.0 / 4;
        JS[4][3]= 2.0 / 4;

        double avgS1 = ocdid.avgSimilarityNeighbours(graph, graph.getNode(0), JS);
        double expectedavgS1 = (1.0 + 3.0 / 4 + 3.0 / 5) / 3;
        assertEquals(expectedavgS1, avgS1, 0.0001);

        //Edge case: node with only one neighbour
        double avgS2 = ocdid.avgSimilarityNeighbours(graph, graph.getNode(4), JS);
        double expectedavgS2 = 2.0 / 4;
        assertEquals(expectedavgS2, avgS2, 0.0001);
    }

    @Ignore
    @Test
    public void testCd() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm();
        CustomGraph graph = get5NodesTestGraph();

        double[] informationList = new double[7];
        informationList[0] = 2.0 / 3;
        informationList[1] = 2.0 / 3;
        informationList[2] = 2.0 / 3;
        informationList[3] = 1.0 / 3;
        informationList[4] = 0.0;

        Matrix expectedMatrix = new Basic2DMatrix(5, 5);
        expectedMatrix.set(0, 1, 1);
        expectedMatrix.set(1, 1, 1);
        expectedMatrix.set(2, 1, 1);

        Matrix actualMatrix = ocdid.cd(graph, informationList);
        assertEquals(expectedMatrix, actualMatrix);
    }
    //@Ignore
    @Test
    public void testOcd() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm();
        CustomGraph graph = get5NodesTestGraph();

        Matrix communities = new Basic2DMatrix(5, 5);
        communities.set(0, 1, 1);
        communities.set(1, 1, 1);
        communities.set(2, 1, 1);

        double[][] I_uv = new double[5][5];

        Matrix expectedMatrix = new Basic2DMatrix(5, 5);
        expectedMatrix.set(0, 1, 1);
        expectedMatrix.set(1, 1, 1);
        expectedMatrix.set(2, 1, 1);
        expectedMatrix.set(3, 1, 1);
        expectedMatrix.set(4, 1, 1);

        Matrix actualMatrix = ocdid.ocd(graph, communities, I_uv);
        assertEquals(expectedMatrix, actualMatrix);
    }
    @Test
    public void testBoundaryNodes() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm(); // instance of OCDID algorithm

        //test if nodes with no community will be added
        CustomGraph graph = get5NodesTestGraph();

        Matrix communities = new Basic2DMatrix(5, 5);
        communities.set(0, 1, 1);
        communities.set(1, 1, 1);
        communities.set(2, 1, 1);
        communities.set(3, 1, 1);

        Set<Node> actualBoundaryNodes = ocdid.boundaryNodes(graph, communities);
        Set<Node> expectedBoundaryNodes = new HashSet<>();
        expectedBoundaryNodes.add(graph.getNode(4));
        assertEquals(expectedBoundaryNodes, actualBoundaryNodes);

        //test if nodes with neighbours which are in different then the node will be added
        CustomGraph graph2 = get7NodesTestGraph();

        Matrix communities2 = new Basic2DMatrix(7, 7);
        communities2.set(0, 1, 1);
        communities2.set(1, 1, 1);
        communities2.set(2, 1, 1);
        communities2.set(3, 2, 1);
        communities2.set(4, 2, 1);
        communities2.set(5, 2, 1);
        communities2.set(6, 2, 1);

        Set<Node> actualBoundaryNodes2 = ocdid.boundaryNodes(graph2, communities2);
        Set<Node> expectedBoundaryNodes2 = new HashSet<>();
        expectedBoundaryNodes2.add(graph2.getNode(2));
        expectedBoundaryNodes2.add(graph2.getNode(3));
        assertEquals(expectedBoundaryNodes2, actualBoundaryNodes2);
    }
    @Test
    public void testComputeNC() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm(); // instance of OCDID algorithm
        CustomGraph graph = get5NodesTestGraph();

        Matrix communities = new Basic2DMatrix(5, 5);
        communities.set(0, 1, 1);
        communities.set(0, 3, 1);
        communities.set(1, 1, 1);
        communities.set(2, 3, 1);
        communities.set(3, 0, 1);
        communities.set(3, 1, 1);
        communities.set(4, 0, 1);

        int nodeID = 0;
        Set<Node> neighbours = new HashSet<Node>();
        neighbours.add(graph.getNode(1));
        neighbours.add(graph.getNode(2));
        neighbours.add(graph.getNode(3));
        Set<Integer> actualNC1 = ocdid.computeNC(communities, nodeID, neighbours);
        Set<Integer> expectedNC1 = new HashSet<Integer>();
        expectedNC1.add(0);
        assertEquals(expectedNC1, actualNC1);
    }
    @Test
    public void testGetCommunityMembers() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm(); // instance of OCDID algorithm
        CustomGraph graph = get5NodesTestGraph();

        Matrix inputMatrix = new Basic2DMatrix(5, 5);
        inputMatrix.set(1, 1, 1);
        inputMatrix.set(2, 3, 1);
        inputMatrix.set(3, 0, 1);
        inputMatrix.set(3, 1, 1);

        //community with two members
        Set<Node> actualMembers = ocdid.getCommunityMembers(graph, inputMatrix, 1);
        Set<Node> expectedMembers = new HashSet<>();
        expectedMembers.add(graph.getNode(1));
        expectedMembers.add(graph.getNode(3));
        assertEquals(expectedMembers, actualMembers);

        //community with zero members
        Set<Node> actualMembers2 = ocdid.getCommunityMembers(graph, inputMatrix, 2);
        Set<Node> expectedMembers2 = new HashSet<>();
        assertEquals(expectedMembers2, actualMembers2);
    }
    @Test
    public void testGetMemberships() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm();

        Matrix inputMatrix = new Basic2DMatrix(4, 4);
        inputMatrix.set(1, 1, 1);
        inputMatrix.set(2, 3, 1);
        inputMatrix.set(3, 0, 1);
        inputMatrix.set(3, 1, 1);

        List<Integer> expectedMemberships1 = new ArrayList<>();
        List<Integer> actualMemberships1 = ocdid.getMemberships(inputMatrix, 0);
        assertEquals(expectedMemberships1, actualMemberships1);

        List<Integer> expectedMemberships2 = new ArrayList<>();
        expectedMemberships2.add(0);
        expectedMemberships2.add(1);
        List<Integer> actualMemberships2 = ocdid.getMemberships(inputMatrix, 3);
        assertEquals(expectedMemberships2, actualMemberships2);
    }

    @Test
    public void testBelongingDegree() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm();
        CustomGraph graph = get7NodesTestGraph();

        Set<Node> neighbours = new HashSet<Node>();
        neighbours.add(graph.getNode(0));
        neighbours.add(graph.getNode(1));
        neighbours.add(graph.getNode(3));

        Set<Node> communityMembers = new HashSet<>();
        communityMembers.add(graph.getNode(3));
        communityMembers.add(graph.getNode(4));
        communityMembers.add(graph.getNode(5));
        communityMembers.add(graph.getNode(6));

        double[][] I_uv = new double[7][7];
        I_uv[0][2] = 0.1250;
        I_uv[1][2] = 0.1250;
        I_uv[4][3] = 0.0493;
        I_uv[5][3] = 0.0680;
        I_uv[6][3] = 0.0493;

        double actualBD = ocdid.belongingDegree(graph.getNode(2), neighbours, communityMembers, I_uv);
        double expectedBD = 0.5 * (0.0 + (1.0/3));
        assertEquals(expectedBD, actualBD, 0.001);
    }
    @Test
    public void testToMembershipMatrix() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm();
        Matrix inputMatrix = new Basic2DMatrix(4, 4);
        inputMatrix.set(0, 3, 1);
        inputMatrix.set(1, 1, 1);
        inputMatrix.set(2, 3, 1);
        inputMatrix.set(3, 0, 1);
        inputMatrix.set(3, 1, 1);

        Matrix expectedMatrix = new Basic2DMatrix(4, 3);
        expectedMatrix.set(0, 2, 1);
        expectedMatrix.set(1, 1, 1);
        expectedMatrix.set(2, 2, 1);
        expectedMatrix.set(3, 0, 1);
        expectedMatrix.set(3, 1, 1);

        Matrix actualMatrix = ocdid.toMembershipMatrix(inputMatrix);
        assertEquals(expectedMatrix, actualMatrix);
    }


    @Test
    public void testOnGraphFromOCDIDPaper() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm(); // instance of OCDID algorithm

        // Set parameters
        Map<String, String> inputParams = new HashMap<String, String>();
        inputParams.put("thresholdOCDID", "0.00001");
        inputParams.put("thresholdCD", "0.002");
        inputParams.put("thresholdOCD", "0.2");
        ocdid.setParameters(inputParams);

        CustomGraph graph = getGraphFromOCDIDPaper();
        try {
            Cover c = ocdid.detectOverlappingCommunities(graph);
            System.out.println(c.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOn5NodesTestGraph() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm();
        CustomGraph graph = get5NodesTestGraph();

        Matrix membershipMatrix = new Basic2DMatrix(5, 1);
        membershipMatrix.set(0, 0, 1);
        membershipMatrix.set(1, 0, 1);
        membershipMatrix.set(2, 0, 1);
        membershipMatrix.set(3, 0, 1);
        membershipMatrix.set(4, 0, 1);

        Cover expectedCover = new Cover(graph, membershipMatrix);
        System.out.println("Expected Cover:");
        System.out.println(expectedCover.toString());

        Cover actualCover = ocdid.detectOverlappingCommunities(graph);
        System.out.println("Actual Cover:");
        System.out.println(actualCover.toString());
    }

    @Test
    public void testOnAperiodicTwoCommunitiesGraph() throws InterruptedException {
        OCDIDAlgorithm ocdid = new OCDIDAlgorithm(); // instance of OCDID algorithm

        // Set parameters
        Map<String, String> inputParams = new HashMap<String, String>();
        inputParams.put("thresholdOCDID", "0.001");
        inputParams.put("thresholdCD", "0.001");
        inputParams.put("thresholdOCD", "0.2");
        ocdid.setParameters(inputParams);

        CustomGraph sawmill = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
        GraphProcessor processor = new GraphProcessor();
        processor.makeUndirected(sawmill);

        try {
            Cover c = ocdid.detectOverlappingCommunities(sawmill);
            System.out.println(c.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static CustomGraph getGraphFromOCDIDPaper() {
        // Creates new graph
        CustomGraph graph = new CustomGraph();
        Node n[] = new Node[18];
        for (int i = 0; i < 18; i++) {
            n[i] = graph.addNode(Integer.toString(i));
            graph.setNodeName(n[i], Integer.toString(i));
        }
        // Creates edges
        graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
        graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
        graph.addEdge(UUID.randomUUID().toString(), n[1], n[4]);
        graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
        graph.addEdge(UUID.randomUUID().toString(), n[2], n[4]);
        graph.addEdge(UUID.randomUUID().toString(), n[2], n[5]);
        graph.addEdge(UUID.randomUUID().toString(), n[2], n[15]);
        graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
        graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
        graph.addEdge(UUID.randomUUID().toString(), n[3], n[6]);
        graph.addEdge(UUID.randomUUID().toString(), n[4], n[6]);
        graph.addEdge(UUID.randomUUID().toString(), n[5], n[13]);
        graph.addEdge(UUID.randomUUID().toString(), n[5], n[14]);
        graph.addEdge(UUID.randomUUID().toString(), n[6], n[7]);
        graph.addEdge(UUID.randomUUID().toString(), n[6], n[11]);
        graph.addEdge(UUID.randomUUID().toString(), n[6], n[13]);
        graph.addEdge(UUID.randomUUID().toString(), n[7], n[8]);
        graph.addEdge(UUID.randomUUID().toString(), n[7], n[9]);
        graph.addEdge(UUID.randomUUID().toString(), n[7], n[10]);
        graph.addEdge(UUID.randomUUID().toString(), n[7], n[11]);
        graph.addEdge(UUID.randomUUID().toString(), n[8], n[9]);
        graph.addEdge(UUID.randomUUID().toString(), n[8], n[10]);
        graph.addEdge(UUID.randomUUID().toString(), n[9], n[10]);
        graph.addEdge(UUID.randomUUID().toString(), n[9], n[11]);
        graph.addEdge(UUID.randomUUID().toString(), n[10], n[11]);
        graph.addEdge(UUID.randomUUID().toString(), n[10], n[12]);
        graph.addEdge(UUID.randomUUID().toString(), n[11], n[12]);
        graph.addEdge(UUID.randomUUID().toString(), n[12], n[13]);
        graph.addEdge(UUID.randomUUID().toString(), n[12], n[17]);
        graph.addEdge(UUID.randomUUID().toString(), n[13], n[14]);
        graph.addEdge(UUID.randomUUID().toString(), n[13], n[15]);
        graph.addEdge(UUID.randomUUID().toString(), n[13], n[16]);
        graph.addEdge(UUID.randomUUID().toString(), n[13], n[17]);
        graph.addEdge(UUID.randomUUID().toString(), n[14], n[15]);
        graph.addEdge(UUID.randomUUID().toString(), n[14], n[16]);
        graph.addEdge(UUID.randomUUID().toString(), n[14], n[17]);
        graph.addEdge(UUID.randomUUID().toString(), n[15], n[16]);
        graph.addEdge(UUID.randomUUID().toString(), n[15], n[0]);
        graph.addEdge(UUID.randomUUID().toString(), n[16], n[17]);
        graph.addEdge(UUID.randomUUID().toString(), n[16], n[0]);
        graph.addEdge(UUID.randomUUID().toString(), n[17], n[0]);

        Iterator<Edge> edges = graph.edges().iterator();
        while(edges.hasNext()) {
            Edge edge = edges.next();
            graph.setEdgeWeight(edge, 1);
        }
        GraphProcessor processor = new GraphProcessor();
        processor.makeCompatible(graph, new HashSet<GraphType>());
        GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
        log.setStatus(ExecutionStatus.COMPLETED);
        graph.setCreationMethod(log);
        processor.makeUndirected(graph);
        return graph;
    }

    public static CustomGraph get5NodesTestGraph() {
        CustomGraph graph = new CustomGraph();
        Node n[] = new Node[5];
        for (int i = 0; i < 5; i++) {
            n[i] = graph.addNode(Integer.toString(i));
            graph.setNodeName(n[i], Integer.toString(i));
        }

        graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
        graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
        graph.addEdge(UUID.randomUUID().toString(), n[0], n[3]);
        graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
        graph.addEdge(UUID.randomUUID().toString(), n[1], n[3]);
        graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);

        Iterator<Edge> edges = graph.edges().iterator();
        while(edges.hasNext()) {
            Edge edge = edges.next();
            graph.setEdgeWeight(edge, 1);
        }
        GraphProcessor processor = new GraphProcessor();
        processor.makeCompatible(graph, new HashSet<GraphType>());
        GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
        log.setStatus(ExecutionStatus.COMPLETED);
        graph.setCreationMethod(log);
        processor.makeUndirected(graph);
        return graph;
    }

    public static CustomGraph get7NodesTestGraph() {
        CustomGraph graph = new CustomGraph();
        Node n[] = new Node[7];
        for (int i = 0; i < 7; i++) {
            n[i] = graph.addNode(Integer.toString(i));
            graph.setNodeName(n[i], Integer.toString(i));
        }

        graph.addEdge(UUID.randomUUID().toString(), n[0], n[1]);
        graph.addEdge(UUID.randomUUID().toString(), n[0], n[2]);
        graph.addEdge(UUID.randomUUID().toString(), n[1], n[2]);
        graph.addEdge(UUID.randomUUID().toString(), n[2], n[3]);
        graph.addEdge(UUID.randomUUID().toString(), n[3], n[4]);
        graph.addEdge(UUID.randomUUID().toString(), n[3], n[5]);
        graph.addEdge(UUID.randomUUID().toString(), n[3], n[6]);
        graph.addEdge(UUID.randomUUID().toString(), n[4], n[5]);
        graph.addEdge(UUID.randomUUID().toString(), n[5], n[6]);

        Iterator<Edge> edges = graph.edges().iterator();
        while(edges.hasNext()) {
            Edge edge = edges.next();
            graph.setEdgeWeight(edge, 1);
        }
        GraphProcessor processor = new GraphProcessor();
        processor.makeCompatible(graph, new HashSet<GraphType>());
        GraphCreationLog log = new GraphCreationLog(GraphCreationType.UNDEFINED, new HashMap<String, String>());
        log.setStatus(ExecutionStatus.COMPLETED);
        graph.setCreationMethod(log);
        processor.makeUndirected(graph);
        return graph;
    }
}
