package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import org.junit.Ignore;
import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;

import y.base.Node;

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
	public void testRandomWalkExecution() {
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
	public void testOnTwoCommunities() {
		CustomGraph graph = OcdTestGraphFactory.getTwoCommunitiesGraph();
		RandomWalkLabelPropagationAlgorithm algo = new RandomWalkLabelPropagationAlgorithm(0.2);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.getMemberships());
	}
	
	@Ignore
	@Test
	public void testOnAperiodicTwoCommunities() {
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		RandomWalkLabelPropagationAlgorithm algo = new RandomWalkLabelPropagationAlgorithm(0.2);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println(cover.getMemberships());
	}
	
	@Test
	public void testOnSawmill() {
		CustomGraph graph = OcdTestGraphFactory.getSawmillGraph();
		RandomWalkLabelPropagationAlgorithm algo = new RandomWalkLabelPropagationAlgorithm(0.2);
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println("Memberships:");
		Matrix memberships = cover.getMemberships();
		Node[] nodes = graph.getNodeArray();
		for(int i=0; i<graph.nodeCount(); i++) {
			System.out.println(graph.getNodeName(nodes[i]) + ": ");
			//System.out.println(memberships.getRow(i));
		}
		System.out.println(memberships);	
	}

}
