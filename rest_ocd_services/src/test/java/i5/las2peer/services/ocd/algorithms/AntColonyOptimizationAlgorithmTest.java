package i5.las2peer.services.ocd.algorithms;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.OCDAParameterTestReq;
import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.SelfLoopsGraphTestReq;
import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.UndirectedGraphTestReq;
import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.ZeroWeightsGraphTestReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.la4j.matrix.Matrix;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.Ant;
import i5.las2peer.services.ocd.algorithms.utils.MaximalCliqueSearch;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.ModularityMetric;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.graphstream.graph.Node;

import static i5.las2peer.services.ocd.algorithms.AntColonyOptimizationAlgorithm.*;
import static org.junit.jupiter.api.Assertions.fail;

public class AntColonyOptimizationAlgorithmTest implements UndirectedGraphTestReq, ZeroWeightsGraphTestReq, SelfLoopsGraphTestReq, OCDAParameterTestReq {

    OcdAlgorithm algo;

    @BeforeEach
    public void setup() {
        algo = new AntColonyOptimizationAlgorithm();
    }

    @Override
    public OcdAlgorithm getAlgorithm() {
        return algo;
    }

    /**
     * Auto-Completed by ChatGPT
     * Executes AntColonyOptimizationAlgorithm on a graph with a mix of zero and non-zero weights.
     * This test uses a high number of ants and iterations to ensure robustness in complex scenarios.
     * The number of groups and neighbors is set to a higher value to test the algorithm's performance with multiple partitions and interactions.
     */
    @Test
    public void zeroWeightsGraphTest1() throws Exception {
        try {
            // Don't modify
            CustomGraph zeroWeightsGraph = OcdTestGraphFactory.getZeroAndNonZeroWeightMixGraph();
            // Don't modify
            Map<String, String> parameters = new HashMap<>();
            // Chosen for thorough search
            parameters.put(MAX_ITERATIONS_NAME, "100");
            // High number to simulate a complex environment
            parameters.put(NUMBER_OF_ANTS_NAME, "10");
            // More than 2 to test multiple partitions
            parameters.put(NUMBER_OF_GROUPS_NAME, "3");
            // Mid-range to balance exploration and exploitation
            parameters.put(PERSISTENCE_FACTOR_NAME, "0.5");
            // Higher initial pheromones for a stronger start
            parameters.put(INITIAL_PHEROMONES_NAME, "200");
            // To test interactions among multiple neighbors
            parameters.put(NUMBER_OF_NEIGHBORS_NAME, "3");
            // Don't modify
            getAlgorithm().setParameters(parameters);
            // Don't modify
            Cover cover = getAlgorithm().detectOverlappingCommunities(zeroWeightsGraph);
            //assertTrue(cover.getCommunities().size() >= 1); 
        } catch (Throwable t) {
            // Don't modify
            fail("Test failed due to an exception or assertion error: " + t.getMessage());
            // Don't modify
            throw t;
        }
    }

    /**
     * Auto-Completed by ChatGPT
     * Executes AntColonyOptimizationAlgorithm on a bipartite graph with self-loops.
     * This test uses lower number of ants and higher persistence factor to check the algorithm's handling of self-reinforcement and memorization.
     */
    @Test
    public void selfLoopsGraphTest1() throws Exception {
        try {
            // Don't modify
            CustomGraph selfLoopsGraph = OcdTestGraphFactory.getBipartiteGraphWithSelfLoops();
            // Don't modify
            Map<String, String> parameters = new HashMap<>();
            // Moderate to limit computation
            parameters.put(MAX_ITERATIONS_NAME, "20");
            // Lower number for simpler interactions
            parameters.put(NUMBER_OF_ANTS_NAME, "3");
            // Default value for basic bipartite partition
            parameters.put(NUMBER_OF_GROUPS_NAME, "2");
            // High to test effect of pheromone persistence
            parameters.put(PERSISTENCE_FACTOR_NAME, "0.9");
            // Slightly higher for noticeable pheromone trails
            parameters.put(INITIAL_PHEROMONES_NAME, "150");
            // Testing with a simple neighborhood
            parameters.put(NUMBER_OF_NEIGHBORS_NAME, "2");
            // Don't modify
            getAlgorithm().setParameters(parameters);
            // Don't modify
            Cover cover = getAlgorithm().detectOverlappingCommunities(selfLoopsGraph);
            //assertTrue(cover.getCommunities().size() >= 1);
        } catch (Throwable t) {
            // Don't modify
            fail("Test failed due to an exception or assertion error: " + t.getMessage());
            // Don't modify
            throw t;
        }
    }

    /**
     * Auto-Completed by ChatGPT
     * Executes AntColonyOptimizationAlgorithm on an undirected bipartite graph.
     * This test uses a high persistence factor and a lower number of ants to focus on the quality of solutions over a smaller search space.
     */
    @Test
    public void undirectedGraphTest1() throws Exception {
        try {
            // Don't modify
            CustomGraph undirectedGraph = OcdTestGraphFactory.getUndirectedBipartiteGraph();
            // Don't modify
            Map<String, String> parameters = new HashMap<>();
            // Balanced to provide enough iterations for convergence
            parameters.put(MAX_ITERATIONS_NAME, "50");
            // Moderate number to simulate a controlled environment
            parameters.put(NUMBER_OF_ANTS_NAME, "4");
            // Default value to maintain bipartite nature
            parameters.put(NUMBER_OF_GROUPS_NAME, "2");
            // Moderately high to allow pheromone influence
            parameters.put(PERSISTENCE_FACTOR_NAME, "0.7");
            // Moderately high to start with noticeable pheromone trails
            parameters.put(INITIAL_PHEROMONES_NAME, "120");
            // Testing interactions between two neighbors
            parameters.put(NUMBER_OF_NEIGHBORS_NAME, "2");
            // Don't modify
            getAlgorithm().setParameters(parameters);
            // Don't modify
            Cover cover = getAlgorithm().detectOverlappingCommunities(undirectedGraph);
            //assertTrue(cover.getCommunities().size() >= 1);
        } catch (Throwable t) {
            // Don't modify
            fail("Test failed due to an exception or assertion error: " + t.getMessage());
            // Don't modify
            throw t;
        }
    }

    /**
     * tests the function CzechkanowskiDice() which determinates the CD-distance between two nodes
     */
    //TODO: remove 555
    @Disabled
    @Test
    public void testCDDistanceTest() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
        CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph();
        Node[] nmap = graph.nodes().toArray(Node[]::new);
        Node v1 = nmap[1];
        Node v2 = nmap[7];
        AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
        double cddistance = ACO.CzechkanowskiDice(graph, v1, v2);
        //		System.out.println("CD-Distance:");
        //		System.out.println(cddistance);
    }

    /**
     * tests the function LinkStrength() which determinates the interconectivity in between the cliques
     */
    //TODO: remove 555
    @Disabled
    @Test
    public void testLinkStrenghTest() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
        CustomGraph graph = OcdTestGraphFactory.getLinkgraph();
        MaximalCliqueSearch MCl = new MaximalCliqueSearch();
        HashMap<Integer, HashSet<Node>> maxClq = MCl.cliques(graph);
        AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
        Matrix linkStr = ACO.linkStrength(graph, maxClq);
        //		System.out.println("Link Strength Matrix:");
        //		System.out.println(linkStr);
        //		System.out.println(maxClq);
    }

    //TODO: remove 555
    @Disabled
    @Test
    public void testAllSteps() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
        CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph();
        AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
        CustomGraph MCR = ACO.representationScheme(graph);
        List<Ant> ants = ACO.initialization(MCR, 5);
        for (Ant a : ants) {
            ACO.constructSolution(MCR, a, 5);
        }
        ACO.update(MCR, ants, 5);
    }

    //TODO: remove 555
    @Disabled
    @Test
    public void testACO() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException, OcdMetricException {
        CustomGraph graph = OcdTestGraphFactory.getDolphinsGraph();
        AntColonyOptimizationAlgorithm ACO = new AntColonyOptimizationAlgorithm();
        Cover c = ACO.detectOverlappingCommunities(graph);
        //ClizzAlgorithm CA = new ClizzAlgorithm();
        //Cover c1 = CA.detectOverlappingCommunities(graph);
        //System.out.print(c);
        ModularityMetric mod = new ModularityMetric();
        //System.out.println(mod.measure(c1));
        //System.out.println(mod.measure(c));
    }


}
