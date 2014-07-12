package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.algorithms.utils.ClizzInfluenceNodesVectorProcedure;
import i5.las2peer.services.servicePackage.algorithms.utils.ClizzLeadershipVectorProcedure;
import i5.las2peer.services.servicePackage.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphType;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.functor.VectorAccumulator;

import y.base.Node;
import y.base.NodeCursor;

/*
 * The overlapping community detection algorithm introduced in 2012
 * by H.J. Li, J. Zhang, Z.P. Liu, L. Chen and X.S. Zhang.
 */
public class ClizzAlgorithm implements OcdAlgorithm {

	/**
	 * The influence range of each node.
	 * Determines the distance in which a node has influence
	 * on other nodes and can become their leader.
	 */
	private double influenceFactor = Double.POSITIVE_INFINITY;
	
	private int membershipsIterationBound;
	private double membershipsPrecisionFactor;
	
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.WEIGHTED);
		compatibilities.add(GraphType.DIRECTED);
		return compatibilities;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph)
			throws OcdAlgorithmException {
		Matrix distances = calculateNodeDistances(graph);
		Map<Node, Double> leadershipValues = calculateLeadershipValues(graph, distances);
		Map<Node, Integer> leaders = determineCommunityLeaders(graph, distances, leadershipValues);
		Matrix memberships = calculateMemberships(graph, leaders);
		return new Cover(graph, memberships, Algorithm.CLIZZ_ALGORITHM);
	}

	@Override
	public Algorithm getAlgorithm() {
		return Algorithm.CLIZZ_ALGORITHM;
	}
	
	protected Matrix calculateMemberships(CustomGraph graph, Map<Node, Integer> leaders) {
		Matrix memberships;
		Matrix updatedMemberships = initMembershipMatrix(graph, leaders);
		Vector membershipContributionVector;
		Vector updatedMembershipVector;
		NodeCursor nodes = graph.nodes();
		Node node;
		NodeCursor successors;
		Node successor;
		int iteration = 0;
		do {
			memberships = updatedMemberships;
			updatedMemberships = new CCSMatrix(memberships.rows(), memberships.columns());
			while(nodes.ok()) {
				node = nodes.node();
				if(!leaders.keySet().contains(node)) {
					successors = node.successors();
					updatedMembershipVector = memberships.getRow(node.index());
					while(successors.ok()) {
						successor = successors.node();
						membershipContributionVector = memberships.getRow(successor.index());
						updatedMembershipVector = updatedMembershipVector.add(membershipContributionVector);
						successors.next();
					}
					updatedMemberships.setRow(node.index(), updatedMembershipVector.divide(1 + successors.size()));
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
	
	protected double getMaxDifference(Matrix matA, Matrix matB) {
		Matrix diffMatrix = matA.subtract(matB);
		double maxDifference = 0;
		double curDifference;
		VectorAccumulator accumulator = Vectors.mkInfinityNormAccumulator();
		for(int i=0; i<diffMatrix.columns(); i++) {
			curDifference = diffMatrix.getColumn(i).fold(accumulator);
			if(curDifference > maxDifference) {
				maxDifference = curDifference;
			}
		}
		return maxDifference;
	}
	
	protected Matrix initMembershipMatrix(CustomGraph graph, Map<Node, Integer> leaders) {
		int communityCount = Collections.max(leaders.values()) + 1;
		Matrix memberships = new CCSMatrix(graph.nodeCount(), communityCount);
		NodeCursor nodes = graph.nodes();
		Node node;
		while(nodes.ok()) {
			node = nodes.node();
			if(leaders.keySet().contains(node)) {
				memberships.set(node.index(), leaders.get(node), 1);
			}
			else {
				for(int i=0; i<memberships.columns(); i++) {
					memberships.set(node.index(), i, 1d / (double)communityCount);
				}
			}
			nodes.next();
		}
		return memberships;
	}
	
	/*
	 * Determines the community leaders of all communities. 
	 * @param graph The graph being analyzed.
	 * @param distances The distance matrix corresponding the graph.
	 * @param leadershipValues A mapping from the graph's nodes to their leadership values.
	 * @return A mapping from the leader nodes to their community indices. Note that multiple
	 * leaders may have the same community index.
	 */
	private Map<Node, Integer> determineCommunityLeaders(CustomGraph graph, Matrix distances, Map<Node, Double> leadershipValues) {
		NodeCursor nodes = graph.nodes();
		Node node;
		Node[] nodeArray = graph.getNodeArray();
		double nodeLeadershipValue;
		double influencerLeadershipIndex;
		Set<Node> currentLeaders = new HashSet<Node>();
		Map<Node, Integer> communityLeaders = new HashMap<Node, Integer>();
		int communityCount = 0;
		while(nodes.ok()) {
			node = nodes.node();
			nodeLeadershipValue = leadershipValues.get(node);
			currentLeaders.clear();
			currentLeaders.add(node);
			for(Integer i : getInfluenceNodes(distances.getRow(node.index()), distances.getColumn(node.index()))) {
				influencerLeadershipIndex = leadershipValues.get(nodeArray[i]);
				if(influencerLeadershipIndex > nodeLeadershipValue) {
					currentLeaders.clear();
					break;
				}
				else if(influencerLeadershipIndex == nodeLeadershipValue) {
					if(node.index() < i) {
						currentLeaders.add(nodeArray[i]);
					}
					/*
					 * Grouped leaders are only added in the iteration over the first corresponding leader.
					 */
					else {
						currentLeaders.clear();
						break;
					}
				}
			}
			if(currentLeaders.size() > 0) {
				for(Node leader : currentLeaders) {
					communityLeaders.put(leader, communityCount);
				}
				communityCount++;
			}
		}
		return communityLeaders;
	}
	
	/*
	 * Calculates the leadership values of all nodes.
	 * @param graph The graph being analyzed.
	 * @param distances A matrix d containing the distance from node i to node j in d_ij.
	 * If two nodes are further apart than the distance defined through the influence factor,
	 * their distance is 0 but to be interpreted as infinity.
	 * @return The leadership indices of all nodes.
	 */
	private Map<Node, Double> calculateLeadershipValues(CustomGraph graph, Matrix distances) {
		NodeCursor nodes = graph.nodes();
		Node node;
		Map<Node, Double> leadershipValues = new HashMap<Node, Double>();
		while(nodes.ok()) {
			node = nodes.node();
			leadershipValues.put(node, getLeadershipValue(distances.getColumn(node.index())));
			nodes.next();
		}
		return leadershipValues;
	}
	
	/*
	 * Determines the directed node distances for all node pairs.
	 * For node pairs that are further apart than the influence range the distance is returned as 0.
	 * This is due to efficiency issues but should be interpreted as infinity.
	 * @param graph The graph being analyzed.
	 * @return A matrix d containing the distance from node i to node j in the entry d_ij, 
	 * where i and j are node indices.
	 */
	private Matrix calculateNodeDistances(CustomGraph graph) {
		NodeCursor nodes = graph.nodes();
		Node node;
		NodeCursor predecessors;
		Node predecessor;
		Map<Node, Double> influencedNodeDistances = new HashMap<Node, Double>();
		Map<Node, Double> candidateNodeDistances = new HashMap<Node, Double>();
		Matrix nodeDistances = new CCSMatrix(graph.nodeCount(), graph.nodeCount());
		Node closestCandidate;
		double closestCandidateDistance;
		double updatedDistance;
		double distanceBound = 3d * influenceFactor / Math.sqrt(2d);
		while(nodes.ok()) {
			/*
			 * Initializes node distances.
			 */
			influencedNodeDistances.clear();
			candidateNodeDistances.clear();
			node = nodes.node();
			influencedNodeDistances.put(node, 0d);
			predecessors = node.predecessors();
			while(predecessors.ok()) {
				predecessor = predecessors.node();
				candidateNodeDistances.put(predecessor, graph.getEdgeWeight(node.getEdgeFrom(predecessor)));
				predecessors.next();
			}
			/*
			 * Determines node distances to predecessors.
			 */
			closestCandidateDistance = 0;
			while(closestCandidateDistance <= distanceBound) {
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
					predecessors = closestCandidate.predecessors();
					while(predecessors.ok()) {
						predecessor = predecessors.node();
						updatedDistance = closestCandidateDistance + graph.getEdgeWeight(closestCandidate.getEdgeFrom(predecessor));
						if(candidateNodeDistances.containsKey(predecessor)) {
							updatedDistance = Math.min(updatedDistance, candidateNodeDistances.get(predecessor));
							candidateNodeDistances.put(predecessor, updatedDistance);
						}
						else if(!influencedNodeDistances.containsKey(predecessor)) {
							candidateNodeDistances.put(predecessor, updatedDistance);
						}
						predecessors.next();
					}
				}
				
			}
			/*
			 * Sets node distances.
			 */
			influencedNodeDistances.remove(node);
			for(Map.Entry<Node, Double> entry : influencedNodeDistances.entrySet()) {
				nodeDistances.set(entry.getKey().index(), node.index(), entry.getValue());
			}
			nodes.next();
		}
		return nodeDistances;
	}
	
	/*
	 * Returns the leadership value of a node.
	 * @param nodeInDistances The distances to the node from other nodes.
	 * Contains in entry i the length of the path from node with index i.
	 * @return The node's leadership value.
	 */
	private double getLeadershipValue(Vector nodeInDistances) {
		ClizzLeadershipVectorProcedure leadershipProcedure = new ClizzLeadershipVectorProcedure(influenceFactor);
		nodeInDistances.eachNonZero(leadershipProcedure);
		return leadershipProcedure.getLeadershipIndex();
	}
	
	/*
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
	private Set<Integer> getInfluenceNodes(Vector nodeOutDistances, Vector nodeInDistances) {
		ClizzInfluenceNodesVectorProcedure influenceNodesProcedure = new ClizzInfluenceNodesVectorProcedure();
		nodeOutDistances.eachNonZero(influenceNodesProcedure);
		/*
		 * Note that even though a vector procedure generally is probably not intended to run multiple times and even
		 * on different vectors, here is made explicit use of this possibility.
		 */
		nodeInDistances.eachNonZero(influenceNodesProcedure);
		return influenceNodesProcedure.getInfluencingNodeIndices();
	}

}
