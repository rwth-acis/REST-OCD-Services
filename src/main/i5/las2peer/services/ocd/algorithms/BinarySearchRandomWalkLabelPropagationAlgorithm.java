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
 * Implements a custom extended version of the Random Walk Label Propagation Algorithm.
 * Handles directed and weighted graphs.
 * For unweighted, undirected graphs, it behaves the same as the original.
 */
public class BinarySearchRandomWalkLabelPropagationAlgorithm implements OcdAlgorithm {

	/**
	 * The iteration bound for the leadership calculation phase. The default
	 * value is 1000. Must be greater than 0.
	 */
	private int randomWalkIterationBound = 1000;
	/**
	 * The precision factor for the leadership calculation phase.
	 * The phase ends when the infinity norm of the difference between the updated vector and
	 * the previous one is smaller than this factor.
	 * The default value is 0.001. Must be greater than 0 and smaller than infinity.
	 * Recommended are values close to 0.
	 */
	private double randomWalkPrecisionFactor = 0.001;
	
	/*
	 * PARAMETER NAMES
	 */
	
	protected static final String LEADERSHIP_PRECISION_FACTOR_NAME = "leadershipPrecisionFactor";
	
	protected static final String LEADERSHIP_ITERATION_BOUND_NAME = "leadershipIterationBound";
	
	/**
	 * Creates a standard instance of the algorithm. All attributes are assigned
	 * there default values.
	 */
	public BinarySearchRandomWalkLabelPropagationAlgorithm() {
	}

	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.BINARY_SEARCH_RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.WEIGHTED);
		compatibilities.add(GraphType.DIRECTED);
		return compatibilities;
	}
	
	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(LEADERSHIP_ITERATION_BOUND_NAME, Integer.toString(randomWalkIterationBound));
		parameters.put(LEADERSHIP_PRECISION_FACTOR_NAME, Double.toString(randomWalkPrecisionFactor));
		return parameters;
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.containsKey(LEADERSHIP_ITERATION_BOUND_NAME)) {
			randomWalkIterationBound = Integer.parseInt(parameters.get(LEADERSHIP_ITERATION_BOUND_NAME));
			if(randomWalkIterationBound <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(LEADERSHIP_ITERATION_BOUND_NAME);
		}
		if(parameters.containsKey(LEADERSHIP_PRECISION_FACTOR_NAME)) {
			randomWalkPrecisionFactor = Double.parseDouble(parameters.get(LEADERSHIP_PRECISION_FACTOR_NAME));
			parameters.remove(LEADERSHIP_PRECISION_FACTOR_NAME);
			if(randomWalkPrecisionFactor <= 0 || randomWalkPrecisionFactor == Double.POSITIVE_INFINITY) {
				throw new IllegalArgumentException();
			}
		}
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph)
			throws OcdAlgorithmException, InterruptedException {
		List<Node> leaders = randomWalkPhase(graph);
		return labelPropagationPhase(graph, leaders);
	}

	/*
	 * Executes the random walk phase of the algorithm and returns global
	 * leaders.
	 * 
	 * @param graph The graph whose leaders will be detected.
	 * 
	 * @return A list containing all nodes which are global leaders.
	 */
	protected List<Node> randomWalkPhase(CustomGraph graph)
			throws OcdAlgorithmException, InterruptedException {
		Matrix disassortativityMatrix = getTransposedDisassortativityMatrix(graph);
		Vector disassortativityVector = executeRandomWalk(disassortativityMatrix);
		Vector leadershipVector = getLeadershipValues(graph,
				disassortativityVector);
		Map<Node, Double> followerMap = getFollowerDegrees(graph,
				leadershipVector);
		return getGlobalLeaders(followerMap);
	}

	/*
	 * Returns the transposed normalized disassortativity matrix for the random
	 * walk phase.
	 * 
	 * @param graph The graph whose disassortativity matrix will be derived.
	 * 
	 * @return The transposed normalized disassortativity matrix.
	 */
	protected Matrix getTransposedDisassortativityMatrix(CustomGraph graph) throws InterruptedException {
		/*
		 * Calculates transposed disassortativity matrix in a special sparse
		 * matrix format.
		 */
		Matrix disassortativities = new CCSMatrix(graph.nodeCount(),
				graph.nodeCount());
		EdgeCursor edges = graph.edges();
		double disassortativity;
		Edge edge;
		while (edges.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			edge = edges.edge();
			disassortativity = Math
					.abs(graph.getWeightedInDegree(edge.target())
							- graph.getWeightedInDegree(edge.source()));
			disassortativities.set(edge.target().index(),
					edge.source().index(), disassortativity);
			edges.next();
		}

		/*
		 * Column normalizes transposed disassortativity matrix.
		 */
		double norm;
		Vector column;
		for (int i = 0; i < disassortativities.columns(); i++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			column = disassortativities.getColumn(i);
			norm = column.fold(Vectors.mkManhattanNormAccumulator());
			if (norm > 0) {
				disassortativities.setColumn(i, column.divide(norm));
			}
		}
		return disassortativities;
	}

	/*
	 * Executes the random walk for the random walk phase. The vector is
	 * initialized with a uniform distribution.
	 * 
	 * @param disassortativityMatrix The disassortativity matrix on which the
	 * random walk will be performed.
	 * 
	 * @return The resulting disassortativity vector.
	 */
	protected Vector executeRandomWalk(Matrix disassortativityMatrix)
			throws OcdAlgorithmException, InterruptedException {
		Vector vec1 = new BasicVector(disassortativityMatrix.columns());
		for (int i = 0; i < vec1.length(); i++) {
			vec1.set(i, 1.0 / vec1.length());
		}
		Vector vec2 = new BasicVector(vec1.length());
		int iteration;
		for (iteration = 0; vec1.subtract(vec2).fold(
				Vectors.mkInfinityNormAccumulator()) > randomWalkPrecisionFactor
				&& iteration < randomWalkIterationBound; iteration++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			vec2 = new BasicVector(vec1);
			vec1 = disassortativityMatrix.multiply(vec1);
		}
		if (iteration >= randomWalkIterationBound) {
			throw new OcdAlgorithmException(
					"Random walk iteration bound exceeded: iteration "
							+ iteration);
		}
		return vec1;
	}

	/*
	 * Calculates the leadership values of all nodes for the random walk phase.
	 * 
	 * @param graph The graph containing the nodes.
	 * 
	 * @param disassortativityVector The disassortativity vector calculated
	 * earlier in the random walk phase.
	 * 
	 * @return A vector containing the leadership value of each node in the
	 * entry given by the node index.
	 */
	protected Vector getLeadershipValues(CustomGraph graph,
			Vector disassortativityVector) throws InterruptedException {
		Vector leadershipVector = new BasicVector(graph.nodeCount());
		NodeCursor nodes = graph.nodes();
		Node node;
		double leadershipValue;
		while (nodes.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			node = nodes.node();
			/*
			 * Note: degree normalization is left out since it
			 * does not influence the outcome.
			 */
			leadershipValue = graph.getWeightedInDegree(node)
					* disassortativityVector.get(node.index());
			leadershipVector.set(node.index(), leadershipValue);
			nodes.next();
		}
		return leadershipVector;
	}

	/*
	 * Returns the follower degree of each node for the random walk phase.
	 * 
	 * @param graph The graph containing the nodes.
	 * 
	 * @param leadershipVector The leadership vector previous calculated during
	 * the random walk phase.
	 * 
	 * @return A mapping from the nodes to the corresponding follower degrees.
	 */
	protected Map<Node, Double> getFollowerDegrees(CustomGraph graph,
			Vector leadershipVector) throws InterruptedException {
		Map<Node, Double> followerMap = new HashMap<Node, Double>();
		NodeCursor nodes = graph.nodes();
		/*
		 * Iterates over all nodes to detect their local leader
		 */
		Node node;
		NodeCursor successors;
		double maxInfluence;
		List<Node> leaders = new ArrayList<Node>();
		Node successor;
		Edge successorEdge;
		double successorInfluence;
		Edge nodeEdge;
		double followerDegree;
		while (nodes.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			node = nodes.node();
			successors = node.successors();
			maxInfluence = Double.NEGATIVE_INFINITY;
			leaders.clear();
			/*
			 * Checks all successors for possible leader
			 */
			while (successors.ok()) {
				successor = successors.node();
				successorEdge = node.getEdgeTo(successor);
				successorInfluence = leadershipVector.get(successor.index())
						* graph.getEdgeWeight(successorEdge);
				if (successorInfluence >= maxInfluence) {
					nodeEdge = node.getEdgeFrom(successor);
					/*
					 * Ensures the node itself is not a leader of the successor
					 */
					if (nodeEdge == null
							|| successorInfluence > leadershipVector.get(node
									.index()) * graph.getEdgeWeight(nodeEdge)) {
						if (successorInfluence > maxInfluence) {
							/*
							 * Other nodes have lower influence
							 */
							leaders.clear();
						}
						leaders.add(successor);
						maxInfluence = successorInfluence;
					}
				}
				successors.next();
			}
			if (!leaders.isEmpty()) {
				for (Node leader : leaders) {
					followerDegree = 0;
					if (followerMap.containsKey(leader)) {
						followerDegree = followerMap.get(leader);
					}
					followerMap.put(leader,
							followerDegree += 1d / leaders.size());
				}
			}
			nodes.next();
		}
		return followerMap;
	}

	/*
	 * Returns a list of global leaders for the random walk phase.
	 * 
	 * @param followerMap The mapping from nodes to their follower degrees
	 * previously calculated in the random walk phase.
	 * 
	 * @return A list containing all nodes which are considered to be global
	 * leaders.
	 */
	protected List<Node> getGlobalLeaders(Map<Node, Double> followerMap) throws InterruptedException {
		double averageFollowerDegree = 0;
		for (Double followerDegree : followerMap.values()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			averageFollowerDegree += followerDegree;
		}
		averageFollowerDegree /= followerMap.size();
		List<Node> globalLeaders = new ArrayList<Node>();
		for (Map.Entry<Node, Double> entry : followerMap.entrySet()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (entry.getValue() >= averageFollowerDegree) {
				globalLeaders.add(entry.getKey());
			}
		}
		return globalLeaders;
	}

	/*
	 * Executes the label propagation phase.
	 * 
	 * @param graph The graph which is being analyzed.
	 * 
	 * @param leaders The list of global leader nodes detected during the random
	 * walk phase.
	 * 
	 * @return A cover containing the detected communities.
	 */
	protected Cover labelPropagationPhase(CustomGraph graph, List<Node> leaders) throws InterruptedException {
		/*
		 * Executes the label propagation until all nodes are assigned to at
		 * least one community
		 */
		Map<Node, Map<Node, Integer>> communities = new HashMap<Node, Map<Node, Integer>>();
		Map<Node, Integer> communityMemberships;
		Map<Node, Map<Node, Integer>> bestValidSolution = null;
		double upperProfitabilityBound = 1;
		double lowerProfitabilityBound = 0;
		for(int i=0; i<10; i++) {
			double profitabilityThreshold = (upperProfitabilityBound + lowerProfitabilityBound) / 2d;
			communities = new HashMap<Node, Map<Node, Integer>>();
			for (Node leader : leaders) {
				communityMemberships = executeLabelPropagation(graph, leader, profitabilityThreshold);
				communities.put(leader, communityMemberships);
			}
			if(areAllNodesAssigned(graph, communities)) {				
				bestValidSolution = communities;
				lowerProfitabilityBound = profitabilityThreshold;
			}
			else {
				upperProfitabilityBound = profitabilityThreshold;
			}
		}
		if(bestValidSolution == null) {
			for (Node leader : leaders) {
				communityMemberships = executeLabelPropagation(graph, leader, 0);
				communities.put(leader, communityMemberships);
				bestValidSolution = communities;
			}
		}
		return getMembershipDegrees(graph, bestValidSolution);
	}

	/*
	 * Executes the label propagation for a single leader to identify its
	 * community members.
	 * 
	 * @param graph The graph which is being analyzed.
	 * 
	 * @param leader The leader node whose community members will be identified.
	 * 
	 * @param profitabilityThreshold The threshold value that determines whether
	 * it is profitable for a node to join the community of the leader / assume
	 * its behavior.
	 * 
	 * @return A mapping containing the iteration count for each node that is a
	 * community member. The iteration count indicates, in which iteration the
	 * corresponding node has joint the community.
	 */
	protected Map<Node, Integer> executeLabelPropagation(CustomGraph graph,
			Node leader, double profitabilityThreshold) throws InterruptedException {
		Map<Node, Integer> memberships = new HashMap<Node, Integer>();
		int previousMemberCount;
		int iterationCount = 0;
		/*
		 * Iterates as long as new members assume the behavior.
		 */
		Set<Node> predecessors;
		Iterator<Node> nodeIt;
		Node node;
		double profitability;
		NodeCursor nodeSuccessors;
		Node nodeSuccessor;
		do {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			iterationCount++;
			previousMemberCount = memberships.size();
			predecessors = getBehaviorPredecessors(graph, memberships, leader);
			nodeIt = predecessors.iterator();
			/*
			 * Checks for each predecessor of the leader behavior nodes whether
			 * it assumes the new behavior.
			 */
			while (nodeIt.hasNext()) {
				node = nodeIt.next();
				profitability = 0;
				nodeSuccessors = node.successors();
				while (nodeSuccessors.ok()) {
					nodeSuccessor = nodeSuccessors.node();
					Integer joinIteration = memberships.get(nodeSuccessor);
					if (nodeSuccessor.equals(leader) || 
							( joinIteration != null && joinIteration < iterationCount)) {
						profitability++;
					}
					nodeSuccessors.next();
				}
				if (profitability / (double) nodeSuccessors.size() > profitabilityThreshold) {
					memberships.put(node, iterationCount);
				}
			}
		} while (memberships.size() > previousMemberCount);
		return memberships;
	}

	/*
	 * Returns all predecessors of the nodes which adopted the leader's behavior
	 * (and the leader itself) for the label propagation of each leader.
	 * 
	 * @param graph The graph which is being analyzed.
	 * 
	 * @param memberships The nodes which have adopted leader behavior. Note
	 * that the membership degrees are not examined, any key value is considered
	 * a node with leader behavior.
	 * 
	 * @param leader The node which is leader of the community currently under
	 * examination.
	 * 
	 * @return A set containing all nodes that have not yet assumed leader
	 * behavior, but are predecessors of a node with leader behavior.
	 */
	protected Set<Node> getBehaviorPredecessors(CustomGraph graph,
			Map<Node, Integer> memberships, Node leader) throws InterruptedException {
		Set<Node> neighbors = new HashSet<Node>();
		NodeCursor leaderPredecessors = leader.predecessors();
		Node leaderPredecessor;
		while (leaderPredecessors.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			leaderPredecessor = leaderPredecessors.node();
			if (!memberships.containsKey(leaderPredecessor)) {
				neighbors.add(leaderPredecessor);
			}
			leaderPredecessors.next();
		}
		NodeCursor memberPredecessors;
		Node memberPredecessor;
		for (Node member : memberships.keySet()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			memberPredecessors = member.predecessors();
			while (memberPredecessors.ok()) {
				memberPredecessor = memberPredecessors.node();
				if (!memberPredecessor.equals(leader)
						&& !memberships.containsKey(memberPredecessor)) {
					neighbors.add(memberPredecessor);
				}
				memberPredecessors.next();
			}
		}
		return neighbors;
	}

	/*
	 * Indicates for the label propagation phase whether all nodes have been
	 * assigned to at least one community.
	 * 
	 * @param graph The graph which is being analyzed.
	 * 
	 * @param communities A mapping from the leader nodes to the membership
	 * degrees of that leaders community.
	 * 
	 * @return TRUE when each node has been assigned to at least one community,
	 * and FALSE otherwise.
	 */
	protected boolean areAllNodesAssigned(CustomGraph graph,
			Map<Node, Map<Node, Integer>> communities) throws InterruptedException {
		boolean allNodesAreAssigned = true;
		NodeCursor nodes = graph.nodes();
		boolean nodeIsAssigned;
		Node node;
		while (nodes.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			nodeIsAssigned = false;
			node = nodes.node();
			for (Map.Entry<Node, Map<Node, Integer>> entry : communities
					.entrySet()) {
				if (entry.getValue().containsKey(node)) {
					nodeIsAssigned = true;
					break;
				}
			}
			if (!nodeIsAssigned) {
				allNodesAreAssigned = false;
				break;
			}
			nodes.next();
		}
		return allNodesAreAssigned;
	}

	/*
	 * Returns a cover containing the membership degrees of all nodes.,
	 * calculated from
	 * 
	 * @param graph The graph which is being analyzed.
	 * 
	 * @param communities A mapping from the leader nodes to the iteration count
	 * mapping of their community members.
	 * 
	 * @return A cover containing each nodes membership degree
	 */
	protected Cover getMembershipDegrees(CustomGraph graph,
			Map<Node, Map<Node, Integer>> communities) throws InterruptedException {
		Matrix membershipMatrix = new Basic2DMatrix(graph.nodeCount(),
				communities.size());
		int communityIndex = 0;
		double membershipDegree;
		for (Node leader : communities.keySet()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			membershipMatrix.set(leader.index(), communityIndex, 1.0);
			for (Map.Entry<Node, Integer> entry : communities.get(leader)
					.entrySet()) {
				membershipDegree = 1.0 / Math.pow(entry.getValue(), 2);
				membershipMatrix.set(entry.getKey().index(), communityIndex,
						membershipDegree);
			}
			communityIndex++;
		}
		Cover cover = new Cover(graph, membershipMatrix);
		return cover;
	}
}