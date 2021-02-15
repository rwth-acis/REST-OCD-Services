package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.dense.BasicVector;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;

/**
* The original version of the overlapping community detection algorithm introduced in 2020
* by Ping Ji, Shanxin Zhang, Zhiping Zhou.
*/
//todo description of the algorithm
public class AntColonyOptimizationAlgorithm implements OcdAlgorithm {
	
	
	private double pheromones; 
	
	
	/** 
	 * Defines the number of subproblems solved which need to be minimized
	 */
	private int subproblems = 2; 
	
	/**
	 * Number of objective functions used in this algorithm. The proposed algorithm by Ji et al 
	 * uses 2 objective functions. So we recommend to this parameter to be 2. 
	 */
	private int objectFkt = 2;
	
	public AntColonyOptimizationAlgorithm() {
		//todo
	}
	
	/**
	 * Executes the algorithm on a connected graph.
	 * Implementations of this method should allow to be interrupted.
	 * I.e. they should periodically check the thread for interrupts
	 * and throw an InterruptedException if an interrupt was detected.
	 * @param graph An at least weakly connected graph whose community structure will be detected.
	 * @return A cover for the input graph containing the community structure.
	 * @throws OcdAlgorithmException If the execution failed.
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) 
			throws OcdAlgorithmException, InterruptedException {

		Matrix memberships = ();
		return new Cover(graph, memberships);
		//todo
		
	}
	
	/**
	 * Returns a log representing the concrete algorithm execution.
	 * @return The log.
	 */
	@Override
	public CoverCreationType getAlgorithmType(){
		return CoverCreationType.ANT_COLONY_OPTIMIZATION;
	}
	
	/**
	 * Returns all graph types the algorithm is compatible with.
	 * @return The compatible graph types.
	 * An empty set if the algorithm is not compatible with any type.
	 */
	public Set<GraphType> compatibleGraphTypes(){
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.ZERO_WEIGHTS);
		return compatibilities;
	};
	
	@Override
	public Map<String, String> getParameters() {
	}

	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		
	}
	
	/**
	 * Initialize parameters and convert the graph into a maximal clique graph representation
	 */
	protected void initialize() {
		//todo
	}
	
	protected void constructSolution() {
		//todo
	}
	
	protected void updateEP() {
		//todo
	}
	
	protected void updatePheromoneMatrix() {
		//todo
	}
	
	protected void updateCurrentSolution() {
		//todo
	}
	
	
}

