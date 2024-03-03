package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.ClizzInfluenceNodesVectorProcedure;
import i5.las2peer.services.ocd.algorithms.utils.ClizzLeadershipVectorProcedure;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.functor.VectorAccumulator;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

/**
 * The original version of the overlapping community detection algorithm introduced in 2012 by H.J. Li, J. Zhang, Z.P. Liu, L. Chen and X.S. Zhang:
 * Identifying overlapping communities in social networks using multi-scale local information expansion
 * https://doi.org/10.1140/epjb/e2012-30015-5
 * Handles weighted and directed graphs. Edge weights are transformed to obtain a distance based interpretation 
 * from an influence based interpretation. The new weight w'(e) of an edge e is defined as w_max(G) + w_min(G) - w(e),
 * where w_max(G) and w_min(G) are the maximum and minimum edge weight of the graph an w(e) the edge's original weight.
 */
public class ClizzAlgorithm implements OcdAlgorithm {

	/**
	 * The influence range of each node.
	 * Determines the distance in which a node has influence on other nodes and can become their leader.
	 * A node A will have influence on a node B if the shortest distance from B to A is
	 * less than 3 / SQRT(2) times the influence factor.
	 * The default value is 0.9. Must be greater than 0.
	 */
	private double influenceFactor = 0.9;
	/**
	 * The iteration bound for the membership calculation phase.
	 * The default value is 1000. Must be greater than 0.
	 */
	private int membershipsIterationBound = 1000;
	/**
	 * The precision factor for the membership assignation phase.
	 * The phase ends when the infinity norm of the difference between the updated membership
	 * matrix and the previous one is smaller than this factor.
	 * The default value is 0.001. Must be greater than 0 and smaller than infinity.
	 */
	private double membershipsPrecisionFactor = 0.001;
	/*
	 * The distanceBound corresponding to the influenceFactor.
	 */
	private double distanceBound;
	
	/*
	 * PARAMETER NAMES
	 */
	
	public static final String INFLUENCE_FACTOR_NAME = "influenceFactor";

	public static final String MEMBERSHIPS_PRECISION_FACTOR_NAME = "membershipsPrecisionFactor";

	public static final String MEMBERSHIPS_ITERATION_BOUND_NAME = "membershipsIterationBound";
	
	/**
	 * Creates a standard instance of the algorithm.
	 * All attributes are assigned their default values.
	 */
	public ClizzAlgorithm() {
		distanceBound = 3d * influenceFactor / Math.sqrt(2d);
		distanceBound = Math.floor(distanceBound);
	}
	
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.WEIGHTED);
		compatibilities.add(GraphType.DIRECTED);
		return compatibilities;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph)
			throws OcdAlgorithmException, InterruptedException {
		Matrix distances = calculateNodeDistances(graph);
		Map<Node, Double> leadershipValues = calculateLeadershipValues(graph, distances);
		Map<Node, Integer> leaders = determineCommunityLeaders(graph, distances, leadershipValues);
		Matrix memberships = calculateMemberships(graph, leaders);
		return new Cover(graph, memberships);
	}
	
	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(INFLUENCE_FACTOR_NAME, Double.toString(influenceFactor));
		parameters.put(MEMBERSHIPS_ITERATION_BOUND_NAME, Integer.toString(membershipsIterationBound));
		parameters.put(MEMBERSHIPS_PRECISION_FACTOR_NAME, Double.toString(membershipsPrecisionFactor));
		return parameters;
	}

	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.containsKey(INFLUENCE_FACTOR_NAME)) {
			influenceFactor = Double.parseDouble(parameters.get(INFLUENCE_FACTOR_NAME));
			if(influenceFactor <= 0) {
				throw new IllegalArgumentException();
			}
			distanceBound = 3d * influenceFactor / Math.sqrt(2d);
			distanceBound = Math.floor(distanceBound);
			parameters.remove(INFLUENCE_FACTOR_NAME);
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
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.CLIZZ_ALGORITHM;
	}
	
	/**
	 * Determines the membership matrix through a random walk process.
	 * @param graph The graph being analyzed.
	 * @param leaders A mapping from the community leader nodes to the indices of their communities.
	 * @return The membership matrix.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Matrix calculateMemberships(CustomGraph graph, Map<Node, Integer> leaders) throws InterruptedException {
		Matrix memberships;
		Matrix updatedMemberships = initMembershipMatrix(graph, leaders);
		Vector membershipContributionVector;
		Vector updatedMembershipVector;
		Iterator<Node> nodesIt = graph.iterator();
		Node node;
		Set<Node> successors;
		int iteration = 0;
		do {
			memberships = updatedMemberships;
			updatedMemberships = new CCSMatrix(memberships.rows(), memberships.columns());
			while(nodesIt.hasNext()) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				node = nodesIt.next();
				if(!leaders.keySet().contains(node)) {
					successors = graph.getSuccessorNeighbours(node);
					updatedMembershipVector = memberships.getRow(node.getIndex());
					for(Node successor : successors) {
						membershipContributionVector = memberships.getRow(successor.getIndex());
						updatedMembershipVector = updatedMembershipVector.add(membershipContributionVector);
					}
					updatedMemberships.setRow(node.getIndex(), updatedMembershipVector.divide(1 + successors.size()));
				}
				else {
					updatedMemberships.set(node.getIndex(), leaders.get(node), 1);
				}
			}
			nodesIt = graph.iterator();
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
		Matrix memberships = new CCSMatrix(graph.getNodeCount(), communityCount);
		Iterator<Node> nodesIt = graph.iterator();
		Node node;
		while(nodesIt.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			node = nodesIt.next();
			if(leaders.keySet().contains(node)) {
				memberships.set(node.getIndex(), leaders.get(node), 1);
			}
			else {
				for(int i=0; i<memberships.columns(); i++) {
					memberships.set(node.getIndex(), i, 1d / communityCount);
				}
			}
		}
		return memberships;
	}
	
	/**
	 * Determines the community leaders and their community indices. 
	 * @param graph The graph being analyzed.
	 * @param distances The distance matrix corresponding the graph.
	 * @param leadershipValues A mapping from the graph's nodes to their leadership values.
	 * @return A mapping from the leader nodes to their community indices. Note that multiple
	 * leaders may have the same community index.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Map<Node, Integer> determineCommunityLeaders(CustomGraph graph, Matrix distances, Map<Node, Double> leadershipValues) throws InterruptedException {
		Node[] nodeArray =  graph.nodes().toArray(Node[]::new);
		Map<Node, Integer> communityLeaders = new HashMap<Node, Integer>();
		int communityCount = 0;
		Set<Node> leaders = determineLeaders(graph, distances, leadershipValues);
		Iterator<Node> leaderIt = leaders.iterator();
		Node leader;
		Node influenceNode;
		while(leaderIt.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			leader = leaderIt.next();
			communityLeaders.put(leader, communityCount);
			leaders.remove(leader);
			for(Integer i : getInfluenceNodes(distances.getRow(leader.getIndex()), distances.getColumn(leader.getIndex()))) {
				influenceNode = nodeArray[i];
				if(leaders.contains(influenceNode)) {
					communityLeaders.put(influenceNode, communityCount);
					leaders.remove(influenceNode);
				}
			}
			/*
			 * Iterator is reset to avoid side effects from element removal.
			 */
			leaderIt = leaders.iterator();
			communityCount++;
		}
		return communityLeaders;
	}
	
	/**
	 * Returns the leader nodes of the graph.
	 * @param graph The graph being analyzed.
	 * @param distances The distance matrix.
	 * @param leadershipValues The nodes' leadership values.
	 * @return The nodes which are community leaders.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Set<Node> determineLeaders(CustomGraph graph, Matrix distances, Map<Node, Double> leadershipValues) throws InterruptedException {
		Set<Node> leaders = new HashSet<Node>();
		Iterator<Node> nodesIt = graph.iterator();
		Node[] nodeArray = graph.nodes().toArray(Node[]::new);
		Node node;
		while(nodesIt.hasNext()) {
			leaders.add(nodesIt.next());
		}
		nodesIt = graph.iterator();
		while(nodesIt.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			node = nodesIt.next();
			if(leaders.contains(node)) {
				double nodeLeadershipValue = leadershipValues.get(node);
				for(Integer i : getInfluenceNodes(distances.getRow(node.getIndex()), distances.getColumn(node.getIndex()))) {
					if(leadershipValues.get(nodeArray[i]) > nodeLeadershipValue) {
						leaders.remove(node);
						break;
					}
				}
			}
		}
		return leaders;
	}
	
	/**
	 * Calculates the leadership values of all nodes.
	 * @param graph The graph being analyzed.
	 * @param distances A matrix d containing the distance from node i to node j in d_ij.
	 * If two nodes are further apart than the distance defined through the influence factor,
	 * their distance is 0 but to be interpreted as infinity.
	 * @return The leadership indices of all nodes.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Map<Node, Double> calculateLeadershipValues(CustomGraph graph, Matrix distances) throws InterruptedException {
		Iterator<Node> nodesIt = graph.iterator();
		Node node;
		Map<Node, Double> leadershipValues = new HashMap<Node, Double>();
		while(nodesIt.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			node = nodesIt.next();
			leadershipValues.put(node, getLeadershipValue(distances.getColumn(node.getIndex())));
		}
		return leadershipValues;
	}
	
	/**
	 * Determines the directed node distances for all node pairs.
	 * For node pairs that are further apart than the influence range the distance is returned as 0.
	 * This is due to efficiency issues but should be interpreted as infinity.
	 * @param graph The graph being analyzed.
	 * @return A matrix d containing the distance from node i to node j in the entry d_ij, 
	 * where i and j are node indices.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Matrix calculateNodeDistances(CustomGraph graph) throws InterruptedException {
		Iterator<Node> nodesIt = graph.iterator();
		Node node;
		Iterator<Node> predecessorsIt;
		Node predecessor;
		double edgeWeight;
		double minEdgeWeight = graph.getMinEdgeWeight();
		double maxEdgeWeight = graph.getMaxEdgeWeight();
		Map<Node, Double> influencedNodeDistances = new HashMap<Node, Double>();
		Map<Node, Double> candidateNodeDistances = new HashMap<Node, Double>();
		Matrix nodeDistances = new CCSMatrix(graph.getNodeCount(), graph.getNodeCount());
		Node closestCandidate;
		double closestCandidateDistance;
		double updatedDistance;
		while(nodesIt.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			/*
			 * Initializes node distances.
			 */
			influencedNodeDistances.clear();
			candidateNodeDistances.clear();
			node = nodesIt.next();
			influencedNodeDistances.put(node, 0d);
			/*
			 * Initializes node predecessors.
			 */
			predecessorsIt = graph.getPredecessorNeighbours(node).iterator();
			while(predecessorsIt.hasNext()) {
				predecessor = predecessorsIt.next();
				edgeWeight = graph.getEdgeWeight(node.getEdgeFrom(predecessor));
				candidateNodeDistances.put(predecessor, getEdgeLength(edgeWeight, minEdgeWeight, maxEdgeWeight));
			}
			/*
			 * Determines node distances to predecessors.
			 */
			closestCandidateDistance = 0;
			while(closestCandidateDistance <= distanceBound) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				closestCandidate = null;
				closestCandidateDistance = Double.POSITIVE_INFINITY;
				for(Map.Entry<Node, Double> entry : candidateNodeDistances.entrySet()) {
					if(entry.getValue() < closestCandidateDistance && entry.getValue() <= distanceBound) {
						closestCandidateDistance = entry.getValue();
						closestCandidate = entry.getKey();
					}
				}
				/*
				 * Updates candidate distances.
				 */
				if(closestCandidateDistance <= distanceBound) {
					influencedNodeDistances.put(closestCandidate, closestCandidateDistance);
					candidateNodeDistances.remove(closestCandidate);
					predecessorsIt = graph.getPredecessorNeighbours(closestCandidate).iterator();
					while(predecessorsIt.hasNext()) {
						predecessor = predecessorsIt.next();
						edgeWeight = graph.getEdgeWeight(closestCandidate.getEdgeFrom(predecessor));
						updatedDistance = closestCandidateDistance + getEdgeLength(edgeWeight, minEdgeWeight, maxEdgeWeight);
						if(candidateNodeDistances.containsKey(predecessor)) {
							updatedDistance = Math.min(updatedDistance, candidateNodeDistances.get(predecessor));
							candidateNodeDistances.put(predecessor, updatedDistance);
						}
						else if(!influencedNodeDistances.containsKey(predecessor)) {
							candidateNodeDistances.put(predecessor, updatedDistance);
						}
					}
				}
				
			}
			/*
			 * Sets node distances.
			 */
			influencedNodeDistances.remove(node);
			for(Map.Entry<Node, Double> entry : influencedNodeDistances.entrySet()) {
				nodeDistances.set(entry.getKey().getIndex(), node.getIndex(), entry.getValue());
			}
		}
		return nodeDistances;
	}
	
	/**
	 * Returns the leadership value of a node.
	 * @param nodeInDistances The distances to the node from other nodes.
	 * Contains in entry i the length of the path from node with index i.
	 * @return The node's leadership value.
	 */
	protected double getLeadershipValue(Vector nodeInDistances) {
		ClizzLeadershipVectorProcedure leadershipProcedure = new ClizzLeadershipVectorProcedure(influenceFactor);
		nodeInDistances.eachNonZero(leadershipProcedure);
		return leadershipProcedure.getLeadershipIndex();
	}
	
	/**
	 * Returns the indices of all nodes within the influence range of a node.
	 * This includes connections in either direction, i.e. also the nodes exerting influence on the node.
	 * @param nodeOutDistances The distances from the examined node to any other node. The distance
	 * must be in the entry corresponding the target node index. 0 is interpreted as infinity,
	 * any distance greater than 0 indicates that the examined node is being influenced by the corresponding 
	 * node.
	 * @param nodeInDistances The distances to the examined node from any other node. The distance
	 * must be in the entry corresponding the target node index. 0 is interpreted as infinity,
	 * any distance greater than 0 indicates that the examined node is being influenced by the corresponding 
	 * node.
	 * @return The indices of all influencing nodes.
	 */
	protected Set<Integer> getInfluenceNodes(Vector nodeOutDistances, Vector nodeInDistances) {
		ClizzInfluenceNodesVectorProcedure influenceNodesProcedure = new ClizzInfluenceNodesVectorProcedure();
		nodeOutDistances.eachNonZero(influenceNodesProcedure);
		/*
		 * Note that even though a vector procedure generally is probably not intended to run multiple times and even
		 * on different vectors, here is made explicit use of this possibility.
		 */
		nodeInDistances.eachNonZero(influenceNodesProcedure);
		return influenceNodesProcedure.getInfluencingNodeIndices();
	}
	
	/**
	 * Calculates the length of an edge in terms of a distance based interpretation (a high value
	 * means two nodes belong together only loosely) rather than an influence based interpretation
	 * (a high value means two nodes belong together closely).
	 * @param edgeWeight The original edge weight.
	 * @param minEdgeWeight The smallest edge weight greater 0 of the examined graph.
	 * @param maxEdgeWeight The maximum edge weight of the examined graph.
	 * @return The calculated edge length
	 */
	protected double getEdgeLength(double edgeWeight, double minEdgeWeight, double maxEdgeWeight) {
		return maxEdgeWeight + minEdgeWeight - edgeWeight;
	}

}
