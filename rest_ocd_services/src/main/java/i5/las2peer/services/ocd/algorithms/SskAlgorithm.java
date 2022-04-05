package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.dense.BasicVector;
import org.la4j.vector.functor.VectorAccumulator;
import org.la4j.vector.sparse.CompressedVector;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;

/**
 * Implement the algorithm by A. Stanoev, D. Smilkov, and L. Kocarev:
 * Identifying communities by influence dynamics in social networks
 * https://doi.org/10.1103/PhysRevE.84.046102
 */
public class SskAlgorithm implements OcdAlgorithm {
	
	/**
	 * The iteration bound for the leadership calculation phase.
	 * The default value is 1000. Must be greater than 0.
	 */
	private int leadershipIterationBound = 1000;
	/**
	 * The precision factor for the leadership calculation phase.
	 * The phase ends when the infinity norm of the difference between the updated vector
	 * and the previous one is smaller than this factor.
	 * The default value is 0.001. Must be greater than 0 and smaller than infinity.
	 * Recommended are values close to 0.
	 */
	private double leadershipPrecisionFactor = 0.001;
	/**
	 * The iteration bound for the membership assignation phase.
	 * The default value is 1000. Must be greater than 0.
	 */
	private int membershipsIterationBound = 1000;
	/**
	 * The precision factor for the membership assignation phase.
	 * The phase ends when the infinity norm of the difference between the updated membership
	 * matrix and the previous one is smaller than this factor.
	 * The default value is 0.001. Must be greater than 0 and smaller than infinity.
	 * Recommended are values close to 0.
	 */
	private double membershipsPrecisionFactor = 0.001;
	
	/*
	 * PARAMETER NAMES
	 */
	
	protected static final String LEADERSHIP_PRECISION_FACTOR_NAME = "leadershipPrecisionFactor";
	
	protected static final String LEADERSHIP_ITERATION_BOUND_NAME = "leadershipIterationBound";
	
	protected static final String MEMBERSHIPS_PRECISION_FACTOR_NAME = "membershipsPrecisionFactor";
			
	protected static final String MEMBERSHIPS_ITERATION_BOUND_NAME = "membershipsIterationBound";
	
	/**
	 * Creates a standard instance of the algorithm.
	 * All attributes are assigned their default values.
	 */
	public SskAlgorithm() {
	}
	
	@Override
	public CoverCreationType getAlgorithmType() {

		return CoverCreationType.SSK_ALGORITHM;
	}
	
	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(LEADERSHIP_ITERATION_BOUND_NAME, Integer.toString(leadershipIterationBound));
		parameters.put(LEADERSHIP_PRECISION_FACTOR_NAME, Double.toString(leadershipPrecisionFactor));
		parameters.put(MEMBERSHIPS_ITERATION_BOUND_NAME, Integer.toString(membershipsIterationBound));
		parameters.put(MEMBERSHIPS_PRECISION_FACTOR_NAME, Double.toString(membershipsPrecisionFactor));
		return parameters;
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.containsKey(LEADERSHIP_ITERATION_BOUND_NAME)) {
			leadershipIterationBound = Integer.parseInt(parameters.get(LEADERSHIP_ITERATION_BOUND_NAME));
			if(leadershipIterationBound <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(LEADERSHIP_ITERATION_BOUND_NAME);
		}
		if(parameters.containsKey(LEADERSHIP_PRECISION_FACTOR_NAME)) {
			leadershipPrecisionFactor = Double.parseDouble(parameters.get(LEADERSHIP_PRECISION_FACTOR_NAME));
			if(leadershipPrecisionFactor <= 0 || leadershipPrecisionFactor == Double.POSITIVE_INFINITY) {
				throw new IllegalArgumentException();
			}
			parameters.remove(LEADERSHIP_PRECISION_FACTOR_NAME);
		}
		if(parameters.containsKey(MEMBERSHIPS_ITERATION_BOUND_NAME)) {
			membershipsIterationBound = Integer.parseInt(parameters.get(MEMBERSHIPS_ITERATION_BOUND_NAME));
			if(membershipsIterationBound <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(MEMBERSHIPS_ITERATION_BOUND_NAME);
		}
		if(parameters.containsKey(MEMBERSHIPS_PRECISION_FACTOR_NAME)) {
			membershipsPrecisionFactor = Double.parseDouble(parameters.get(MEMBERSHIPS_PRECISION_FACTOR_NAME));
			if(membershipsPrecisionFactor <= 0 || membershipsPrecisionFactor == Double.POSITIVE_INFINITY) {
				throw new IllegalArgumentException();
			}
			parameters.remove(MEMBERSHIPS_PRECISION_FACTOR_NAME);
		}
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> types = new HashSet<GraphType>();
		types.add(GraphType.DIRECTED);
		types.add(GraphType.WEIGHTED);
		return types;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws InterruptedException {
		Matrix transitionMatrix = calculateTransitionMatrix(graph);
		Vector totalInfluences = executeRandomWalk(transitionMatrix);
		Map<Node, Integer> leaders = determineGlobalLeaders(graph, transitionMatrix, totalInfluences);
		Matrix memberships = calculateMemberships(graph, leaders);
		return new Cover(graph, memberships);
	}
	
	/**
	 * Determines the membership matrix through a random walk process.
	 * @param graph The graph being analyzed.
	 * @param leaders A mapping from the community leader nodes to the indices of their communities.
	 * @return The membership matrix.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Matrix calculateMemberships(CustomGraph graph, Map<Node, Integer> leaders) throws InterruptedException {
		Matrix coefficients = initMembershipCoefficientMatrix(graph, leaders);
		Matrix memberships;
		Matrix updatedMemberships = initMembershipMatrix(graph, leaders);
		Vector membershipContributionVector;
		Vector updatedMembershipVector;
		NodeCursor nodes = graph.nodes();
		Node node;
		NodeCursor successors;
		Node successor;
		double coefficient;
		int iteration = 0;
		do {
			memberships = updatedMemberships;
			updatedMemberships = new CCSMatrix(memberships.rows(), memberships.columns());
			while(nodes.ok()) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				node = nodes.node();
				if(!leaders.keySet().contains(node)) {
					successors = node.successors();
					updatedMembershipVector = new CompressedVector(memberships.columns());
					while(successors.ok()) {
						successor = successors.node();
						coefficient = coefficients.get(successor.index(), node.index());
						membershipContributionVector = memberships.getRow(successor.index()).multiply(coefficient);
						updatedMembershipVector = updatedMembershipVector.add(membershipContributionVector);
						successors.next();
					}
					updatedMemberships.setRow(node.index(), updatedMembershipVector);
				}
				else {
					updatedMemberships.set(node.index(), leaders.get(node), 1);
				}
				nodes.next();
			}
			nodes.toFirst();
			iteration++;
		} while (getMaxDifference(updatedMemberships, memberships) > membershipsPrecisionFactor
				&& iteration < membershipsIterationBound);
		return memberships;
	}
	
	/**
	 * Returns the maximum difference between two matrices.
	 * It is calculated entry-wise as the greatest absolute value
	 * of any entry in the difference among the two matrices.
	 * @param matA The first matrix.
	 * @param matB The second matrix.
	 * @return The maximum difference.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected double getMaxDifference(Matrix matA, Matrix matB) throws InterruptedException {
		Matrix diffMatrix = matA.subtract(matB);
		double maxDifference = 0;
		double curDifference;
		VectorAccumulator accumulator = Vectors.mkInfinityNormAccumulator();
		for(int i=0; i<diffMatrix.columns(); i++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			curDifference = diffMatrix.getColumn(i).fold(accumulator);
			if(curDifference > maxDifference) {
				maxDifference = curDifference;
			}
		}
		return maxDifference;
	}
	
	/**
	 * Initializes the membership matrix for the memberships assignation phase.
	 * Leader nodes are set to belong entirely to their own community. All other nodes
	 * have equal memberships for all communities.
	 * @param graph The graph being analyzed.
	 * @param leaders A mapping from the leader nodes to their community indices.
	 * @return The initial membership matrix.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Matrix initMembershipMatrix(CustomGraph graph, Map<Node, Integer> leaders) throws InterruptedException {
		int communityCount = Collections.max(leaders.values()) + 1;
		Matrix memberships = new CCSMatrix(graph.nodeCount(), communityCount);
		NodeCursor nodes = graph.nodes();
		Node node;
		while(nodes.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			node = nodes.node();
			if(leaders.keySet().contains(node)) {
				memberships.set(node.index(), leaders.get(node), 1);
			}
			else {
				for(int i=0; i<memberships.columns(); i++) {
					memberships.set(node.index(), i, 1d / communityCount);
				}
			}
			nodes.next();
		}
		return memberships;
	}
	
	/**
	 * Initializes the membership coefficient matrix C for the memberships assignation phase.
	 * The coefficient of the membership vector of node i for the calculation of the updated
	 * memberships of node j is stored in entry C_ij, where i and j are the node indices.
	 * @param graph The graph being analyzed.
	 * @param leaders A mapping from the leader nodes to their community indices.
	 * @return The membership coefficient matrix.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Matrix initMembershipCoefficientMatrix(CustomGraph graph, Map<Node, Integer> leaders) throws InterruptedException {
		Matrix coefficients = new CCSMatrix(graph.nodeCount(), graph.nodeCount());
		EdgeCursor edges = graph.edges();
		Edge edge;
		while(edges.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			edge = edges.edge();
			coefficients.set(edge.target().index(), edge.source().index(), graph.getEdgeWeight(edge));
			edges.next();
		}
		Vector column;
		double norm;
		for(int i=0; i<coefficients.columns(); i++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			column = coefficients.getColumn(i);
			norm = column.fold(Vectors.mkManhattanNormAccumulator());
			if(norm > 0) {
				coefficients.setColumn(i, column.divide(norm));
			}
		}
		return coefficients;
	}
	
	/**
	 * Returns the global leaders of the graph.
	 * @param graph The graph being analyzed.
	 * @param transitionMatrix The transition matrix used for the random walk.
	 * @param totalInfluences The total influences determined via the random walk
	 * @return The global leaders of the graph. The community index of each leader node is 
	 * derivable from the mapping. Note that several leaders may belong to the same
	 * community.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Map<Node, Integer> determineGlobalLeaders(CustomGraph graph, Matrix transitionMatrix, Vector totalInfluences) throws InterruptedException{
		NodeCursor nodes = graph.nodes();
		Node node;
		NodeCursor successors;
		Node successor;
		double relativeInfluence;
		double maxRelativeInfluence;
		List<Node> maxRelativeInfluenceNeighbors;
		Map<Node, Integer> communityLeaders = new HashMap<Node, Integer>();
		List<Node> currentCommunityLeaders;
		double nodeInfluenceOnNeighbor;
		double neighborInfluenceOnNode;
		int communityCount = 0;
		while(nodes.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			node = nodes.node();
			successors = node.successors();
			maxRelativeInfluence = Double.NEGATIVE_INFINITY;
			maxRelativeInfluenceNeighbors = new ArrayList<Node>();
			while(successors.ok()) {
				successor = successors.node();
				relativeInfluence = transitionMatrix.get(successor.index(), node.index());
				if(relativeInfluence >= maxRelativeInfluence) {
					if(relativeInfluence > maxRelativeInfluence) {
						maxRelativeInfluenceNeighbors.clear();
						maxRelativeInfluence = relativeInfluence;
					}
					maxRelativeInfluenceNeighbors.add(successor);
				}
				successors.next();
			}
			currentCommunityLeaders = new ArrayList<Node>();
			currentCommunityLeaders.add(node);
			for(int i=0; i<maxRelativeInfluenceNeighbors.size(); i++) {
				Node maxRelativeInfluenceNeighbor = maxRelativeInfluenceNeighbors.get(i);
				nodeInfluenceOnNeighbor = totalInfluences.get(node.index())
						* transitionMatrix.get(node.index(), maxRelativeInfluenceNeighbor.index());
				neighborInfluenceOnNode = totalInfluences.get(maxRelativeInfluenceNeighbor.index())
						* maxRelativeInfluence;
				if(neighborInfluenceOnNode > nodeInfluenceOnNeighbor) {
					/*
					 * Not a leader
					 */
					currentCommunityLeaders.clear();
					break;
				}
				else if (neighborInfluenceOnNode == nodeInfluenceOnNeighbor) {
					/*
					 * There are potentially several community leaders.
					 */
					if(maxRelativeInfluenceNeighbor.index() < node.index()) {
						/*
						 * Will detected community leaders only once in the iteration over the
						 * node with the lowest index.
						 */
						currentCommunityLeaders.clear();
						break;
					}
					else {
						/*
						 * Node has the lowest index of the potential leaders for the current
						 * community. The additional potential leader is added.
						 */
						currentCommunityLeaders.add(maxRelativeInfluenceNeighbor);
					}
				}
			}
			for(int i=0; i<currentCommunityLeaders.size(); i++) {
				communityLeaders.put(currentCommunityLeaders.get(i), communityCount);
			}
			if(currentCommunityLeaders.size() > 0) {
				communityCount++;
			}
			nodes.next();
		}
		return communityLeaders;
	}
	
	/**
	 * Executes a random walk on the transition matrix and returns the total node influences. 
	 * @param transitionMatrix The transition matrix.
	 * @return A vector containing the total influence of each node under the corresponding node index.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Vector executeRandomWalk(Matrix transitionMatrix) throws InterruptedException {
		Vector vec1 = new BasicVector(transitionMatrix.columns());
		for(int i=0; i<vec1.length(); i++) {
			vec1.set(i, 1.0 / vec1.length());
		}
		Vector vec2 = new BasicVector(vec1.length());
		for(int i=0; vec1.subtract(vec2).fold(Vectors.mkInfinityNormAccumulator()) > leadershipPrecisionFactor
				&& i < leadershipIterationBound; i++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			vec2 = new BasicVector(vec1);
			vec1 = transitionMatrix.multiply(vec1);
		}
		return vec1;
	}
	
	/**
	 * Calculates the transition matrix for the random walk phase. 
	 * @param graph The graph being analyzed.
	 * @return The normalized transition matrix.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Matrix calculateTransitionMatrix(CustomGraph graph) throws InterruptedException {
		Matrix transitionMatrix = new CCSMatrix(graph.nodeCount(), graph.nodeCount());
		NodeCursor nodes = graph.nodes();
		Node node;
		NodeCursor predecessors;
		Node predecessor;
		while(nodes.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			node = nodes.node();
			predecessors = node.predecessors();
			while(predecessors.ok()) {
				predecessor = predecessors.node();
				transitionMatrix.set(node.index(), predecessor.index(), calculateTransitiveLinkWeight(graph, node, predecessor));
				predecessors.next();
			}
			nodes.next();
		}
		Vector column;
		double norm;
		for(int i=0; i<transitionMatrix.columns(); i++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			column = transitionMatrix.getColumn(i);
			norm = column.fold(Vectors.mkManhattanNormAccumulator());
			if(norm > 0) {
				transitionMatrix.setColumn(i, column.divide(norm));
			}
		}
		return transitionMatrix;
	}
	
	/**
	 * Calculates the transitive link weight from source to target.
	 * Note that target must be a successor of source.
	 * @param graph The graph being analyzed.
	 * @param target The link target.
	 * @param source The link source.
	 * @return The transitive link weight from source to target.
	 */
	protected double calculateTransitiveLinkWeight(CustomGraph graph, Node target, Node source) {
		NodeCursor successors = source.successors();
		Node successor;
		double transitiveLinkWeight = 0;
		double linkWeight;
		Edge targetEdge;
		while(successors.ok()) {
			successor = successors.node();
			if(successor != target) {
				targetEdge = successor.getEdgeTo(target);
				if(targetEdge != null) {
					/*
					 * Contribution to the transitive link weight is chosen as the minimum weight
					 * of the two triangle edges.
					 */
					linkWeight = graph.getEdgeWeight(source.getEdgeTo(successor));
					linkWeight = Math.min(linkWeight, graph.getEdgeWeight(targetEdge));
					transitiveLinkWeight += linkWeight;
				}
			}
			else {
				transitiveLinkWeight += graph.getEdgeWeight(source.getEdgeTo(target));
			}
			successors.next();
		}
		return transitiveLinkWeight;
	}
	
}
