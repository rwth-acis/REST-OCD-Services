package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.TestGraphFactory;

import org.junit.Ignore;
import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;

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
public class RandomWalkLabelPropagationAlgorithmTest {

	@Test
	public void testRandomWalkExecution1() {
		Matrix transitionMatrix = new Basic2DMatrix(2, 2);
		transitionMatrix.set(0, 0, 0.9);
		transitionMatrix.set(0, 1, 0.5);
		transitionMatrix.set(1, 0, 0.1);
		transitionMatrix.set(1, 1, 0.5);
		System.out.println("Transition Matrix:");
		System.out.println(transitionMatrix.transpose());
		RandomWalkLabelPropagationAlgorithm algo = new RandomWalkLabelPropagationAlgorithm(0.05);
		Vector vec = algo.executeRandomWalk(transitionMatrix);
		System.out.println("Steady State Vector:");
		System.out.println(vec);
	}
	
	@Ignore
	@Test
	public void testRandomWalkExecution2() {
		Matrix transitionMatrix = new Basic2DMatrix(11, 11);
		transitionMatrix.set(1, 0, 4.0 / (4*4.0 + 3.0));
		transitionMatrix.set(2, 0, 4.0 / (4*4.0 + 3.0));
		transitionMatrix.set(3, 0, 4.0 / (4*4.0 + 3.0));
		transitionMatrix.set(4, 0, 4.0 / (4*4.0 + 3.0));
		transitionMatrix.set(10, 0, 3.0 / (4*4.0 + 3.0));
		transitionMatrix.set(0, 1, 1.0);
		transitionMatrix.set(0, 2, 1.0);
		transitionMatrix.set(0, 3, 1.0);
		transitionMatrix.set(0, 4, 1.0);
		transitionMatrix.set(6, 5, 4.0 / (4*4.0 + 3.0));
		transitionMatrix.set(7, 5, 4.0 / (4*4.0 + 3.0));
		transitionMatrix.set(8, 5, 4.0 / (4*4.0 + 3.0));
		transitionMatrix.set(9, 5, 4.0 / (4*4.0 + 3.0));
		transitionMatrix.set(10, 5, 3.0 / (4*4.0 + 3.0));
		transitionMatrix.set(5, 6, 1.0);
		transitionMatrix.set(5, 7, 1.0);
		transitionMatrix.set(5, 8, 1.0);
		transitionMatrix.set(5, 9, 1.0);
		transitionMatrix.set(0, 10, 0.5);
		transitionMatrix.set(5, 10, 0.5);
		System.out.println("Transition Matrix:");
		System.out.println(transitionMatrix.transpose());
		RandomWalkLabelPropagationAlgorithm algo = new RandomWalkLabelPropagationAlgorithm(0.2);
		Vector vec = algo.executeRandomWalk(transitionMatrix);
		System.out.println("Steady State Vector:");
		System.out.println(vec);
	}
	
	@Ignore
	@Test
	public void testRandomWalkLabelPropagationAlgorithm() {
		CustomGraph graph = TestGraphFactory.getTwoCommunitiesGraph();
		RandomWalkLabelPropagationAlgorithm algo = new RandomWalkLabelPropagationAlgorithm(0.2);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.getMemberships());
	}
	
	@Ignore
	@Test
	public void testRandomWalkLabelPropagationAlgorithm2() {
		CustomGraph graph = TestGraphFactory.getAperiodicTwoCommunitiesGraph();
		RandomWalkLabelPropagationAlgorithm algo = new RandomWalkLabelPropagationAlgorithm(0.2);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.getMemberships());
	}
	
	@Test
	public void testRandomWalkLabelPropagationAlgorithmOnSawmill() {
		CustomGraph graph = TestGraphFactory.getSawmillGraph();
		RandomWalkLabelPropagationAlgorithm algo = new RandomWalkLabelPropagationAlgorithm(0.2);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println("Memberships:");
		System.out.println(cover.getMemberships());
	}

}
