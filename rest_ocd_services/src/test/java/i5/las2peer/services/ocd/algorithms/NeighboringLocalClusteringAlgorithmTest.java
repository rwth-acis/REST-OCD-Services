package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.Assert.assertEquals;


public class NeighboringLocalClusteringAlgorithmTest {

    @Test
    public void testCreateAdjacencyList() throws FileNotFoundException, AdapterException, InterruptedException {
        CustomGraph graph = OcdTestGraphFactory.getPaperGraph();
        NeighboringLocalClusteringAlgorithm algo = new NeighboringLocalClusteringAlgorithm();
        Matrix createdAdjacencyMatrix = graph.getNeighbourhoodMatrix();

        HashMap<Integer, ArrayList<Integer>> createdAdjacencyList = algo.createAdjacencyList(createdAdjacencyMatrix);
        HashMap<Integer, ArrayList<Integer>> expectedAdjacencyList = new HashMap<>();

        ArrayList<Integer> list_0 = new ArrayList<>();
        list_0.addAll(Arrays.asList(1, 3, 4, 5, 8));
        ArrayList<Integer> list_1 = new ArrayList<>();
        list_1.addAll(Arrays.asList(0, 2, 6, 7, 9));
        ArrayList<Integer> list_2 = new ArrayList<>();
        list_2.addAll(Arrays.asList(1, 6, 7, 9, 12));
        ArrayList<Integer> list_3 = new ArrayList<>();
        list_3.addAll(Arrays.asList(0, 4, 5, 8));
        ArrayList<Integer> list_4 = new ArrayList<>();
        list_4.addAll(Arrays.asList(0, 3, 5, 8, 11));
        ArrayList<Integer> list_5 = new ArrayList<>();
        list_5.addAll(Arrays.asList(0, 3, 4, 8));
        ArrayList<Integer> list_6 = new ArrayList<>();
        list_6.addAll(Arrays.asList(1, 2, 7, 9));
        ArrayList<Integer> list_7 = new ArrayList<>();
        list_7.addAll(Arrays.asList(1, 2, 6, 9));
        ArrayList<Integer> list_8 = new ArrayList<>();
        list_8.addAll(Arrays.asList(0, 3, 4, 5, 10));
        ArrayList<Integer> list_9 = new ArrayList<>();
        list_9.addAll(Arrays.asList(1, 2, 6, 7, 13));
        ArrayList<Integer> list_10 = new ArrayList<>();
        list_10.addAll(Arrays.asList(8, 11));
        ArrayList<Integer> list_11 = new ArrayList<>();
        list_11.addAll(Arrays.asList(4, 10));
        ArrayList<Integer> list_12 = new ArrayList<>();
        list_12.addAll(Arrays.asList(2, 13));
        ArrayList<Integer> list_13 = new ArrayList<>();
        list_13.addAll(Arrays.asList(9, 12));

        expectedAdjacencyList.put(0, list_0);
        expectedAdjacencyList.put(1, list_1);
        expectedAdjacencyList.put(2, list_2);
        expectedAdjacencyList.put(3, list_3);
        expectedAdjacencyList.put(4, list_4);
        expectedAdjacencyList.put(5, list_5);
        expectedAdjacencyList.put(6, list_6);
        expectedAdjacencyList.put(7, list_7);
        expectedAdjacencyList.put(8, list_8);
        expectedAdjacencyList.put(9, list_9);
        expectedAdjacencyList.put(10, list_10);
        expectedAdjacencyList.put(11, list_11);
        expectedAdjacencyList.put(12, list_12);
        expectedAdjacencyList.put(13, list_13);

        assertEquals(expectedAdjacencyList, createdAdjacencyList);
    }

    @Test
    public void testGetSimilarityMatrix() throws FileNotFoundException, AdapterException, InterruptedException {
        CustomGraph graph = OcdTestGraphFactory.getPaperGraph();
        int nodeCount = graph.getNodeCount();
        NeighboringLocalClusteringAlgorithm algo = new NeighboringLocalClusteringAlgorithm();
        Matrix createdAdjacencyMatrix = graph.getNeighbourhoodMatrix();
        HashMap<Integer, ArrayList<Integer>> createdAdjacencyList = algo.createAdjacencyList(createdAdjacencyMatrix);

        HashMap<Integer, HashMap<Integer, Double>> createdSimilarityMatrix = algo.getSimilarityMatrix(createdAdjacencyList);
        HashMap<Integer, HashMap<Integer, Double>> expectedSimilarityMatrix = new HashMap<>();

        for(int i = 0; i < nodeCount; i++){
            ArrayList<Integer> neighbours_i = createdAdjacencyList.get(i);
            HashMap<Integer, Double> sim_i = new HashMap<>();
            for(int j = 0; j < nodeCount; j++){
                ArrayList<Integer> neighbours_j = createdAdjacencyList.get(j);
                Set<Integer> union = new HashSet<>(neighbours_i);
                Set<Integer> intersection = new HashSet<>(neighbours_i);
                union.addAll(neighbours_j);
                intersection.retainAll(neighbours_j);
                double value = (double) intersection.size() / union.size();
                sim_i.put(j, value);
            }
            expectedSimilarityMatrix.put(i, sim_i);
        }

        assertEquals(expectedSimilarityMatrix, createdSimilarityMatrix);
    }

    @Test
    public void testGetCommunities() throws FileNotFoundException, AdapterException, InterruptedException {
        NeighboringLocalClusteringAlgorithm algo = new NeighboringLocalClusteringAlgorithm();
        HashMap<Integer, ArrayList<Integer>> communities = new HashMap<>();
        ArrayList<Integer> community_0 = new ArrayList<>();
        community_0.addAll(Arrays.asList(3, 5));
        ArrayList<Integer> community_1 = new ArrayList<>();
        community_1.addAll(Arrays.asList(3, 4, 6));
        ArrayList<Integer> community_2 = new ArrayList<>();
        community_2.addAll(Arrays.asList(0, 1));
        ArrayList<Integer> community_3 = new ArrayList<>();
        community_3.addAll(Arrays.asList(1, 2, 3));
        communities.put(0, community_0);
        communities.put(1, community_1);
        communities.put(2, community_2);
        communities.put(3, community_3);

        ArrayList<Integer> createdCommunities_n = algo.getCommunities(communities, 3);
        ArrayList<Integer> expectedCommunities_n = new ArrayList<>();
        expectedCommunities_n.addAll(Arrays.asList(0, 1, 3));

        assertEquals(expectedCommunities_n, createdCommunities_n);
    }

    @Test
    public void testGetConnection() throws FileNotFoundException, AdapterException, InterruptedException {
        CustomGraph graph = OcdTestGraphFactory.getPaperGraph();
        NeighboringLocalClusteringAlgorithm algo = new NeighboringLocalClusteringAlgorithm();
        Matrix createdAdjacencyMatrix = graph.getNeighbourhoodMatrix();
        ArrayList<Integer> community = new ArrayList<>();
        community.addAll(Arrays.asList(0, 3, 4, 5, 8, 10, 11));
        ArrayList<Integer> overlappingNodes = new ArrayList<>();
        overlappingNodes.addAll(Arrays.asList(0, 1));

        int createdConnections = algo.getConnections(createdAdjacencyMatrix, overlappingNodes, 3, community);
        int expectedConnections = 3;

        assertEquals(expectedConnections, createdConnections);
    }

    @Test
    public void testDijkstraAlgorithm() throws FileNotFoundException, AdapterException, InterruptedException {
        CustomGraph graph = OcdTestGraphFactory.getPaperGraph();
        NeighboringLocalClusteringAlgorithm algo = new NeighboringLocalClusteringAlgorithm();
        Matrix createdAdjacencyMatrix = graph.getNeighbourhoodMatrix();

        int[][] createdShortestPaths = algo.dijkstraAlgorithm(createdAdjacencyMatrix);
        int[][] expectedShortestPaths = {
                {0, 1, 2, 1, 1, 1, 2, 2, 1, 2, 2, 2, 3, 3},
                {1, 0, 1, 2, 2, 2, 1, 1, 2, 1, 3, 3, 2, 2},
                {2, 1, 0, 3, 3, 3, 1, 1, 3, 1, 4, 4, 1, 2},
                {1, 2, 3, 0, 1, 1, 3, 3, 1, 3, 2, 2, 4, 4},
                {1, 2, 3, 1, 0, 1, 3, 3, 1, 3, 2, 1, 4, 4},
                {1, 2, 3, 1, 1, 0, 3, 3, 1, 3, 2, 2, 4, 4},
                {2, 1, 1, 3, 3, 3, 0, 1, 3, 1, 4, 4, 2, 2},
                {2, 1, 1, 3, 3, 3, 1, 0, 3, 1, 4, 4, 2, 2},
                {1, 2, 3, 1, 1, 1, 3, 3, 0, 3, 1, 2, 4, 4},
                {2, 1, 1, 3, 3, 3, 1, 1, 3, 0, 4, 4, 2, 1},
                {2, 3, 4, 2, 2, 2, 4, 4, 1, 4, 0, 1, 5, 5},
                {2, 3, 4, 2, 1, 2, 4, 4, 2, 4, 1, 0, 5, 5},
                {3, 2, 1, 4, 4, 4, 2, 2, 4, 2, 5, 5, 0, 1},
                {3, 2, 2, 4, 4, 4, 2, 2, 4, 1, 5, 5, 1, 0}
        };

        assertEquals(expectedShortestPaths, createdShortestPaths);
    }

    @Test
    public void testGetMembershipMatrix() throws FileNotFoundException, AdapterException, InterruptedException {
        CustomGraph graph = OcdTestGraphFactory.getPaperGraph();
        int nodeCount = graph.getNodeCount();
        NeighboringLocalClusteringAlgorithm algo = new NeighboringLocalClusteringAlgorithm();
        Matrix createdAdjacencyMatrix = graph.getNeighbourhoodMatrix();
        ConcurrentHashMap<Integer, ArrayList<Integer>> communities = new ConcurrentHashMap<>();
        ArrayList<Integer> community_0 = new ArrayList<>();
        community_0.addAll(Arrays.asList(1, 2, 6, 7, 9, 12, 13));
        ArrayList<Integer> community_1 = new ArrayList<>();
        community_1.addAll(Arrays.asList(0, 3, 4, 5, 8, 10, 11));
        communities.put(0, community_0);
        communities.put(1, community_1);

        Matrix createdMembershipMatrix = algo.getMembershipMatrix(createdAdjacencyMatrix, communities);
        Matrix expectedMembershipMatrix = new Basic2DMatrix(nodeCount, communities.size());
        expectedMembershipMatrix.blank();
        expectedMembershipMatrix.set(0, 1, 1.0);
        expectedMembershipMatrix.set(1, 0, 1.0);
        expectedMembershipMatrix.set(2, 0, 1.0);
        expectedMembershipMatrix.set(3, 1, 1.0);
        expectedMembershipMatrix.set(4, 1, 1.0);
        expectedMembershipMatrix.set(5, 1, 1.0);
        expectedMembershipMatrix.set(6, 0, 1.0);
        expectedMembershipMatrix.set(7, 0, 1.0);
        expectedMembershipMatrix.set(8, 1, 1.0);
        expectedMembershipMatrix.set(9, 0, 1.0);
        expectedMembershipMatrix.set(10, 1, 1.0);
        expectedMembershipMatrix.set(11, 1, 1.0);
        expectedMembershipMatrix.set(12, 0, 1.0);
        expectedMembershipMatrix.set(13, 0, 1.0);

        assertEquals(expectedMembershipMatrix, createdMembershipMatrix);
    }

    @Test
    public void testGetCentralNodeSet() throws AdapterException, InterruptedException {
        CustomGraph graph = OcdTestGraphFactory.getPaperGraph();
        NeighboringLocalClusteringAlgorithm algo = new NeighboringLocalClusteringAlgorithm();
        Matrix createdAdjacencyMatrix = graph.getNeighbourhoodMatrix();
        HashMap<Integer, ArrayList<Integer>> createdAdjacencyList = algo.createAdjacencyList(createdAdjacencyMatrix);
        HashMap<Integer, HashMap<Integer, Double>> createdSimilarityMatrix = algo.getSimilarityMatrix(createdAdjacencyList);

        HashSet<Integer> createdCentralNodes = algo.getCentralNodeSet(createdAdjacencyList, createdSimilarityMatrix);
        HashSet<Integer> expectedCentralNodes = new HashSet<>();
        expectedCentralNodes.addAll(Arrays.asList(3, 6));

        assertEquals(expectedCentralNodes, createdCentralNodes);
    }

    @Test
    public void testGetCentralEdgeSets() throws AdapterException, InterruptedException {
        CustomGraph graph = OcdTestGraphFactory.getPaperGraph();
        NeighboringLocalClusteringAlgorithm algo = new NeighboringLocalClusteringAlgorithm();
        Matrix createdAdjacencyMatrix = graph.getNeighbourhoodMatrix();
        HashMap<Integer, ArrayList<Integer>> createdAdjacencyList = algo.createAdjacencyList(createdAdjacencyMatrix);
        HashMap<Integer, HashMap<Integer, Double>> createdSimilarityMatrix = algo.getSimilarityMatrix(createdAdjacencyList);
        HashSet<Integer> createdCentralNodeSet = algo.getCentralNodeSet(createdAdjacencyList, createdSimilarityMatrix);

        HashMap<Integer, ArrayList<ArrayList<Integer>>> createdCentralEdgeSets = algo.getCentralEdgeSets(createdCentralNodeSet, createdSimilarityMatrix, createdAdjacencyList);
        HashMap<Integer, ArrayList<ArrayList<Integer>>> expectedCentralEdgeSets = new HashMap<>();

        ArrayList<ArrayList<Integer>> centralEdgeSet1 = new ArrayList<>();
        ArrayList<Integer> ce1 = new ArrayList<>();
        ce1.addAll(Arrays.asList(3, 5));
        centralEdgeSet1.add(ce1);
        ArrayList<ArrayList<Integer>> centralEdgeSet2 = new ArrayList<>();
        ArrayList<Integer> ce2 = new ArrayList<>();
        ce2.addAll(Arrays.asList(6, 7));
        centralEdgeSet2.add(ce2);

        int[][] nonCE = {{0, 1}, {0, 3}, {0, 4}, {0, 5}, {0, 8}, {1, 2}, {1, 6}, {1, 7}, {1, 9}, {2, 6}, {2, 7}, {2, 9}, {2, 12}, {3, 4}, {3, 8}, {4, 5}, {4, 8}, {4, 11}, {5, 8}, {6, 9}, {7, 9}, {8, 10}, {9, 13}, {10, 11}, {12, 13}};
        ArrayList<ArrayList<Integer>> nonCentralEdges = new ArrayList<>();
        for (int i = 0; i < nonCE.length; i++){
            ArrayList<Integer> nce = new ArrayList<>();
            nce.add(nonCE[i][0]);
            nce.add(nonCE[i][1]);
            nonCentralEdges.add(nce);
        }

        for (int centralNode : createdCentralNodeSet){
            if (centralNode == 3){
                expectedCentralEdgeSets.put(centralNode, centralEdgeSet1);
            }
            if (centralNode == 6){
                expectedCentralEdgeSets.put(centralNode, centralEdgeSet2);
            }
        }
        expectedCentralEdgeSets.put(7, nonCentralEdges);

        assertEquals(expectedCentralEdgeSets, createdCentralEdgeSets);
    }

    @Test
    public void testGetCommunitiesAndOverlappingNodes() throws AdapterException, InterruptedException {
        CustomGraph graph = OcdTestGraphFactory.getPaperGraph();
        NeighboringLocalClusteringAlgorithm algo = new NeighboringLocalClusteringAlgorithm();
        Matrix createdAdjacencyMatrix = graph.getNeighbourhoodMatrix();
        HashMap<Integer, ArrayList<Integer>> createdAdjacencyList = algo.createAdjacencyList(createdAdjacencyMatrix);
        HashMap<Integer, HashMap<Integer, Double>> createdSimilarityMatrix = algo.getSimilarityMatrix(createdAdjacencyList);
        HashSet<Integer> createdCentralNodeSet = algo.getCentralNodeSet(createdAdjacencyList, createdSimilarityMatrix);
        HashMap<Integer, ArrayList<ArrayList<Integer>>> createdCentralEdgeSets = algo.getCentralEdgeSets(createdCentralNodeSet, createdSimilarityMatrix, createdAdjacencyList);

        HashMap<Integer, ArrayList<Integer>> createdCommunitiesAndOverlappingNodes = algo.getCommunitiesAndOverlappingNodes(createdAdjacencyMatrix, createdCentralEdgeSets, createdAdjacencyList);
        HashMap<Integer, ArrayList<Integer>> expectedCommunitiesAndOverlappingNodes = new HashMap<>();

        ArrayList<Integer> community3 = new ArrayList<>();
        community3.addAll(Arrays.asList(0, 1, 3, 4, 5, 8, 10, 11));
        ArrayList<Integer> community6 = new ArrayList<>();
        community6.addAll(Arrays.asList(0, 1, 2, 6, 7, 9, 12, 13));
        ArrayList<Integer> overlappingNodes = new ArrayList<>();
        overlappingNodes.addAll(Arrays.asList(0, 1));

        expectedCommunitiesAndOverlappingNodes.put(3, community3);
        expectedCommunitiesAndOverlappingNodes.put(6, community6);
        expectedCommunitiesAndOverlappingNodes.put(7, overlappingNodes);

        assertEquals(expectedCommunitiesAndOverlappingNodes, createdCommunitiesAndOverlappingNodes);
    }

    @Test
    public void testGetOptimizedCommunities() throws AdapterException, InterruptedException {
        CustomGraph graph = OcdTestGraphFactory.getPaperGraph();
        NeighboringLocalClusteringAlgorithm algo = new NeighboringLocalClusteringAlgorithm();
        Matrix createdAdjacencyMatrix = graph.getNeighbourhoodMatrix();
        HashMap<Integer, ArrayList<Integer>> createdAdjacencyList = algo.createAdjacencyList(createdAdjacencyMatrix);
        HashMap<Integer, HashMap<Integer, Double>> createdSimilarityMatrix = algo.getSimilarityMatrix(createdAdjacencyList);
        HashSet<Integer> createdCentralNodeSet = algo.getCentralNodeSet(createdAdjacencyList, createdSimilarityMatrix);
        HashMap<Integer, ArrayList<ArrayList<Integer>>> createdCentralEdgeSets = algo.getCentralEdgeSets(createdCentralNodeSet, createdSimilarityMatrix, createdAdjacencyList);
        HashMap<Integer, ArrayList<Integer>> createdCommunitiesAndOverlappingNodes = algo.getCommunitiesAndOverlappingNodes(createdAdjacencyMatrix, createdCentralEdgeSets, createdAdjacencyList);

        ConcurrentHashMap<Integer, ArrayList<Integer>> createdOptimizedCommunities = algo.getOptimizedCommunities(createdAdjacencyMatrix, createdCommunitiesAndOverlappingNodes);
        HashMap<Integer, ArrayList<Integer>> expectedOptimizedCommunities = new HashMap<>();

        ArrayList<Integer> com1 = new ArrayList<>();
        com1.addAll(Arrays.asList(0, 3, 4, 5, 8, 10, 11));
        ArrayList<Integer> com2 = new ArrayList<>();
        com2.addAll(Arrays.asList(1, 2, 6, 7, 9, 12, 13));

        expectedOptimizedCommunities.put(3, com1);
        expectedOptimizedCommunities.put(6, com2);

        assertEquals(expectedOptimizedCommunities, createdOptimizedCommunities);
    }

    @Test
    public void testDetectOverlappingCommunities() throws AdapterException, OcdAlgorithmException, InterruptedException {
        CustomGraph graph = OcdTestGraphFactory.getPaperGraph();
        int nodeCount = graph.getNodeCount();
        NeighboringLocalClusteringAlgorithm algo = new NeighboringLocalClusteringAlgorithm();
        Cover cover = algo.detectOverlappingCommunities(graph);

        Matrix matrix = cover.getMemberships();
        Matrix expectedMatrix = new Basic2DMatrix(14, 2);

        expectedMatrix.set(0, 0, 1.0);
        expectedMatrix.set(3, 0, 1.0);
        expectedMatrix.set(4, 0, 1.0);
        expectedMatrix.set(5, 0, 1.0);
        expectedMatrix.set(8, 0, 1.0);
        expectedMatrix.set(10, 0, 1.0);
        expectedMatrix.set(11, 0, 1.0);
        expectedMatrix.set(1, 1, 1.0);
        expectedMatrix.set(2, 1, 1.0);
        expectedMatrix.set(6, 1, 1.0);
        expectedMatrix.set(7, 1, 1.0);
        expectedMatrix.set(9, 1, 1.0);
        expectedMatrix.set(12, 1, 1.0);
        expectedMatrix.set(13, 1, 1.0);

        double controlSum = 0;
        for (int i = 0; i < nodeCount; i++) {
            for (int j = 0; j < 2; j++) {
                controlSum += expectedMatrix.get(i, j) - matrix.get(i, j);
            }
        }

        assertEquals(graph, cover.getGraph());
        assertEquals(0, controlSum, 0.0);
    }

}