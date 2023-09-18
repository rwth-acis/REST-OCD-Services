package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.ExtendedModularityMetric;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.metrics.StatisticalMeasure;
import i5.las2peer.services.ocd.test_interfaces.ocda.DirectedGraphTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.UndirectedGraphTestReq;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;

import org.graphstream.graph.Node;

/*
LDAV=ones(1,num_vertices(SparseGraph));
LDAV=LDAV./num_vertices(SparseGraph);
LDAVNext=LDAV;
NumOfIterations=0; % it shows number of iterations to converge...
diff=1;
while(diff>0.00001)
    temp=LDAV*NDATM;
    LDAV=LDAVNext;
    LDAVNext=temp;
    diff=norm(LDAVNext-LDAV);
    NumOfIterations=NumOfIterations+1
    
end

NDATM normalized DA matrix
LDAV DA vector
 */

/*
 * Test Class for the Random Walk Label Propagation Algorithm
 */
public class RandomWalkLabelPropagationAlgorithmTest implements DirectedGraphTestReq, UndirectedGraphTestReq {

	OcdAlgorithm algo;

	@BeforeEach
	public void setup() {
		algo = new RandomWalkLabelPropagationAlgorithm();
	}

	@Override
	public OcdAlgorithm getAlgorithm() {
		return algo;
	}


	@Disabled //TODO: remove 555
	@Test
	public void testRandomWalkExecution() throws OcdAlgorithmException, InterruptedException {
		Matrix transitionMatrix = new Basic2DMatrix(2, 2);
		transitionMatrix.set(0, 0, 0.9);
		transitionMatrix.set(0, 1, 0.5);
		transitionMatrix.set(1, 0, 0.1);
		transitionMatrix.set(1, 1, 0.5);
		//System.out.println("Transition Matrix:");
		//System.out.println(transitionMatrix.transpose());
		RandomWalkLabelPropagationAlgorithm algo = new RandomWalkLabelPropagationAlgorithm();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(RandomWalkLabelPropagationAlgorithm.PROFITABILITY_DELTA_NAME, Double.toString(0.05));
		parameters.put(RandomWalkLabelPropagationAlgorithm.LEADERSHIP_ITERATION_BOUND_NAME, Integer.toString(1000));
		parameters.put(RandomWalkLabelPropagationAlgorithm.LEADERSHIP_PRECISION_FACTOR_NAME, Double.toString(0.001));
		algo.setParameters(parameters);
		Vector vec = algo.executeRandomWalk(transitionMatrix);
		//System.out.println("Steady State Vector:");
		//System.out.println(vec);
	}

	@Disabled //TODO: remove 555
	@Test
	public void testWithKnownResults() throws OcdAlgorithmException, InterruptedException {
		//System.out.println("Known Result Test");
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		RandomWalkLabelPropagationAlgorithm algo = new RandomWalkLabelPropagationAlgorithm();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(RandomWalkLabelPropagationAlgorithm.PROFITABILITY_DELTA_NAME, Double.toString(0.1));
		parameters.put(RandomWalkLabelPropagationAlgorithm.LEADERSHIP_ITERATION_BOUND_NAME, Integer.toString(1000));
		parameters.put(RandomWalkLabelPropagationAlgorithm.LEADERSHIP_PRECISION_FACTOR_NAME, Double.toString(0.001));
		algo.setParameters(parameters);
		Matrix disassortativityMatrix = algo.getTransposedDisassortativityMatrix(graph);
		//System.out.println("DA M:\n" + disassortativityMatrix);
		Vector disassortativityVector = algo.executeRandomWalk(disassortativityMatrix);
		//System.out.println("DA Vec:\n" + disassortativityVector);
		Vector leadershipVector = algo.getLeadershipValues(graph,
				disassortativityVector);
		//System.out.println("LS Vec:\n" + leadershipVector);
		Map<Node, Double> followerMap = algo.getFollowerDegrees(graph,
				leadershipVector);
		//System.out.println("Follower Degs:\n" + followerMap);
		List<Node> leaders = algo.getGlobalLeaders(followerMap);
		//System.out.println("Leaders:\n" + leaders);
		Cover cover = algo.labelPropagationPhase(graph, leaders);
		//System.out.println(cover);
	}

	@Disabled //TODO: remove 555
	@Test
	public void testOnAperiodicTwoCommunities() throws OcdAlgorithmException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		RandomWalkLabelPropagationAlgorithm algo = new RandomWalkLabelPropagationAlgorithm();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(RandomWalkLabelPropagationAlgorithm.PROFITABILITY_DELTA_NAME, Double.toString(0.2));
		parameters.put(RandomWalkLabelPropagationAlgorithm.LEADERSHIP_ITERATION_BOUND_NAME, Integer.toString(1000));
		parameters.put(RandomWalkLabelPropagationAlgorithm.LEADERSHIP_PRECISION_FACTOR_NAME, Double.toString(0.001));
		algo.setParameters(parameters);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());
	}

	@Disabled //TODO: remove 555
	@Test
	public void testOnSawmill() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		RandomWalkLabelPropagationAlgorithm algo = new RandomWalkLabelPropagationAlgorithm();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(RandomWalkLabelPropagationAlgorithm.PROFITABILITY_DELTA_NAME, Double.toString(0.2));
		parameters.put(RandomWalkLabelPropagationAlgorithm.LEADERSHIP_ITERATION_BOUND_NAME, Integer.toString(1000));
		parameters.put(RandomWalkLabelPropagationAlgorithm.LEADERSHIP_PRECISION_FACTOR_NAME, Double.toString(0.001));
		algo.setParameters(parameters);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.toString());	
	}

	@Disabled //TODO: remove 555
	@Test
	public void testProfitabilityDelta() throws OcdAlgorithmException, AdapterException, FileNotFoundException, OcdMetricException, InterruptedException {
		System.out.println();
		System.out.println();
		System.out.println("Test Profitability Delta");
		double[] profitabililtyDeltas = new double[11];
		profitabililtyDeltas[0] = 0.050;
		profitabililtyDeltas[1] = 0.075;
		profitabililtyDeltas[2] = 0.100;
		profitabililtyDeltas[3] = 0.125;
		profitabililtyDeltas[4] = 0.150;
		profitabililtyDeltas[5] = 0.175;
		profitabililtyDeltas[6] = 0.200;
		profitabililtyDeltas[7] = 0.225;
		profitabililtyDeltas[8] = 0.250;
		profitabililtyDeltas[9] = 0.275;
		profitabililtyDeltas[10] = 0.300;
		StatisticalMeasure metric = new ExtendedModularityMetric();
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		for(int i=0; i < profitabililtyDeltas.length; i++) {
			System.out.println("Delta: " + profitabililtyDeltas[i]);
			RandomWalkLabelPropagationAlgorithm algo = new RandomWalkLabelPropagationAlgorithm();
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put(RandomWalkLabelPropagationAlgorithm.PROFITABILITY_DELTA_NAME, Double.toString(profitabililtyDeltas[i]));
			parameters.put(RandomWalkLabelPropagationAlgorithm.LEADERSHIP_ITERATION_BOUND_NAME, Integer.toString(1000));
			parameters.put(RandomWalkLabelPropagationAlgorithm.LEADERSHIP_PRECISION_FACTOR_NAME, Double.toString(0.001));
			algo.setParameters(parameters);
			Cover cover = algo.detectOverlappingCommunities(graph);
			metric.measure(cover);
			System.out.println(cover.toString());
		}
	}

}
