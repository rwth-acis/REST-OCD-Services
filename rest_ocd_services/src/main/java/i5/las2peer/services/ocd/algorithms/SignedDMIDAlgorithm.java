package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.util.*;

import org.graphstream.graph.implementations.MultiNode;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

/**
 * Implements SignedDMID algorithm by M. Shahriari, S. Krott, and R. Klamma:
 * Disassortative degree mixing and information diffusion for overlapping community detection in social networks (dmid)
 * https://doi.org/10.1145/2740908.2741696
 * Handles directed and signed graphs.
 * 
 * @author YLi
 */
public class SignedDMIDAlgorithm implements OcdAlgorithm {
	/**
	 * The number of iterations of network coordination game. Default value is
	 * 4. Must be in (1,POSITIVE_INFINITY)
	 */
	private double iterationStep = 4;
	/**
	 * The weight between disassortativity and effective degree value is 0.5.
	 * Must be in (0, 1).
	 */
	private double disassortativityEffectiveDegreeWeight = 0.5;

	/*
	 * PARAMETER NAMES
	 */

	public static final String ITERATION_STEP_NAME = "iterationStep";

	public static final String DISASSORTATIVITY_EFFECTIVE_DEGREE_WEIGHT_NAME = "disassortativityEffectiveDegreeWeight";

	/**
	 * Creates a standard instance of the algorithm. All attributes are assigned
	 * their default values.
	 */
	public SignedDMIDAlgorithm() {
	}

	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.SIGNED_DMID_ALGORITHM;
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(ITERATION_STEP_NAME, Double.toString(iterationStep));
		parameters.put(DISASSORTATIVITY_EFFECTIVE_DEGREE_WEIGHT_NAME,
				Double.toString(disassortativityEffectiveDegreeWeight));
		return parameters;
	}

	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if (parameters.containsKey(ITERATION_STEP_NAME)) {
			iterationStep = Double.parseDouble(parameters.get(ITERATION_STEP_NAME));
			if (iterationStep < 2) {
				throw new IllegalArgumentException();
			}
			parameters.remove(ITERATION_STEP_NAME);
		}
		if (parameters.containsKey(DISASSORTATIVITY_EFFECTIVE_DEGREE_WEIGHT_NAME)) {
			disassortativityEffectiveDegreeWeight = Double
					.parseDouble(parameters.get(DISASSORTATIVITY_EFFECTIVE_DEGREE_WEIGHT_NAME));
			parameters.remove(DISASSORTATIVITY_EFFECTIVE_DEGREE_WEIGHT_NAME);
			if (disassortativityEffectiveDegreeWeight < 0 || disassortativityEffectiveDegreeWeight > 1) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.NEGATIVE_WEIGHTS);
		compatibilities.add(GraphType.DIRECTED);
		return compatibilities;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
		Vector leadershipVector = getLeadershipVector(graph);
		List<Node> leaders = leaderFindingPhase(graph, leadershipVector);
		return labelPropagationPhase(graph, leaders, leadershipVector);
	}

	/**
	 * Returns the list of global leaders.
	 * 
	 * @param graph The graph whose leaders will be detected.
	 * @param leadershipVector vector 
	 * 
	 * @return A list containing all nodes which are global leaders.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected List<Node> leaderFindingPhase(CustomGraph graph, Vector leadershipVector)
			throws InterruptedException {
		Map<Node, Integer> localLeader = getLocalLeader(graph, leadershipVector);
		return getGlobalLeader(localLeader);
	}

	/**
	 * Returns a list of global leaders.
	 * 
	 * @param localLeader The mapping from nodes to their follower degrees.
	 * 
	 * @return A list containing all nodes which are considered to be global
	 * leaders.
	 * @throws InterruptedException if the thread was interrupted
	 */
	public List<Node> getGlobalLeader(Map<Node, Integer> localLeader) throws InterruptedException {
		double averageFollowerDegree = 0;
		for (int followerDegree : localLeader.values()) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			averageFollowerDegree += followerDegree;
		}
		averageFollowerDegree /= localLeader.size();
		List<Node> globalLeaders = new ArrayList<Node>();
		for (Map.Entry<Node, Integer> entry : localLeader.entrySet()) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			//If only two local leaders are found, they become both global leaders
			if (localLeader.size() < 3) {
				globalLeaders.add(entry.getKey());
			} else {
				if (entry.getValue() >= averageFollowerDegree) {
					globalLeaders.add(entry.getKey());
				}
			}
		}
		return globalLeaders;
	}

	/**
	 * Returns the mapping from the local leaders to the corresponding follower degrees.
	 * 
	 * @param graph The graph whose local leaders will be detected.
	 * 
	 * @param leadershipVector The leadership vector.
	 * 
	 * @return A mapping from the local leaders to the corresponding follower degrees.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Map<Node, Integer> getLocalLeader(CustomGraph graph, Vector leadershipVector)
			throws InterruptedException {
		Map<Node, Integer> followerMap = new HashMap<Node, Integer>();
		Iterator<Node> nodesIt = graph.iterator();
		Node node;
		/*
		 * Iterates over all nodes to detect their local leader
		 */

		while (nodesIt.hasNext()) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			node = nodesIt.next();
			double leadershipValue = leadershipVector.get(node.getIndex());
			boolean beingLeader = true;
			int followerDegree = 0;
			Set<Node> neighbours = graph.getNeighbours(node);
			for (Node neighbour : neighbours) {
				if (leadershipValue < leadershipVector.get(neighbour.getIndex())) {
					beingLeader = false;
					break;
				}
				/*
				 * If leadershipvalue bigger than all of its
				 * neighbours->calculate follower degree As the node can
				 * positively point to its neighbour and be positively pointed
				 * to by its neighbour, the neighour should not be counted twice
				 */
				boolean ignoreReverse = false;
				if (node.getEdgeFrom(neighbour) != null) {
					if (graph.getEdgeWeight(node.getEdgeFrom(neighbour)) > 0) {
						followerDegree++;
						ignoreReverse = true;
					}
				}
				if (ignoreReverse == false) {
					if (node.getEdgeToward(neighbour) != null) {
						if (graph.getEdgeWeight(node.getEdgeToward(neighbour)) > 0) {
							followerDegree++;
							ignoreReverse = true;
						}
					}
				}
			}
			if (beingLeader == true) {

				followerMap.put(node, followerDegree);
			}
		}

		return followerMap;
	}

	/**
	 * Returns the leadership value vector.
	 * 
	 * @param graph The graph under observation.
	 * 
	 * @return The leadership value vector.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Vector getLeadershipVector(CustomGraph graph) throws InterruptedException {
		int nodeCount = graph.getNodeCount();
		Matrix nodeEDandDASS = new CCSMatrix(nodeCount, 3);
		Vector leadershipVector = new BasicVector(nodeCount);
		Iterator<Node> nodesIt = graph.iterator();
		Node node;
		double effectiveDegreeValue;
		while (nodesIt.hasNext()) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			node = nodesIt.next();
			if (graph.getPositiveInDegree(node) + Math.abs(graph.getNegativeInDegree(node)) != 0) {
				if (graph.getPositiveInDegree(node) - Math.abs(graph.getNegativeInDegree(node)) < 0) {
					effectiveDegreeValue = 0;
				} else {
					effectiveDegreeValue = (graph.getPositiveInDegree(node)
							- Math.abs(graph.getNegativeInDegree(node)))
							/ (graph.getPositiveInDegree(node) + Math.abs(graph.getNegativeInDegree(node)));
				}
			} else {
				effectiveDegreeValue = 0;
			}
			nodeEDandDASS.set(node.getIndex(), 0, effectiveDegreeValue);
			// Disassortativity, put denominator and nominator in different matrix columns.
			Set<Node> neighbours = graph.getNeighbours(node);
			double nodeDegree = graph.getAbsoluteNodeDegree((MultiNode) node);
			double neighbourDegree = 0;
			for (Node neighbour : neighbours) {
				neighbourDegree = graph.getAbsoluteNodeDegree((MultiNode) neighbour);
				nodeEDandDASS.set(node.getIndex(), 1, nodeEDandDASS.get(node.getIndex(), 1) + nodeDegree + neighbourDegree);
				nodeEDandDASS.set(node.getIndex(), 2, nodeEDandDASS.get(node.getIndex(), 2) + nodeDegree - neighbourDegree);
			}
		}
		for (int i = 0; i < nodeCount; i++) {
			leadershipVector.set(i,
					(1 - disassortativityEffectiveDegreeWeight) * nodeEDandDASS.get(i, 0)
							+ disassortativityEffectiveDegreeWeight * nodeEDandDASS.get(i, 2)
									/ nodeEDandDASS.get(i, 1));
		}
		return leadershipVector;
	}

	/**
	 * Executes the label propagation phase.
	 * 
	 * @param graph The graph which is being analyzed.
	 * 
	 * @param leaders The list of detected global leader nodes.
	 * 
	 * @param ProfitabilityThreshold threshold for adopting a new behavior,which equals to LLD in this
	 * algorithm.
	 * 
	 * @return A cover containing the detected communities.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Cover labelPropagationPhase(CustomGraph graph, List<Node> leaders, Vector ProfitabilityThreshold)
			throws InterruptedException {
		/*
		 * Executes the label propagation until all nodes are assigned to at
		 * least one community
		 */
		Map<Node, Map<Node, Integer>> communities = new HashMap<Node, Map<Node, Integer>>();
		Map<Node, Integer> communityMemberships;
		for (Node leader : leaders) {
			communityMemberships = executeLabelPropagation(graph, leader, ProfitabilityThreshold);
			communityMemberships.remove(leader);
			communities.put(leader, communityMemberships);
		}
		return getMembershipDegrees(graph, communities);
	}

	/**
	 * Executes the label propagation for a single leader to identify its
	 * community members.
	 * 
	 * @param graph The graph which is being analyzed.
	 * 
	 * @param leader The leader node whose community members will be identified.
	 * 
	 * @param profitabilityThreshold The threshold value that determines whether
	 * it is profitable for a node to join the community of the leader / assume
	 * its behavior. In this algorithm, it is equal to the LLD value.
	 * 
	 * @return A mapping containing the iteration count for each node that is a
	 * community member. The iteration count indicates, in which iteration the
	 * corresponding node has joined the community.
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Map<Node, Integer> executeLabelPropagation(CustomGraph graph, Node leader, Vector profitabilityThreshold)
			throws InterruptedException {
		Map<Node, Integer> memberships = new HashMap<Node, Integer>();
		Map<Node, Integer> membershipsToAdd = new HashMap<Node, Integer>();
		Set<Node> NodesAssumingNewLabel = new HashSet<Node>();
		Set<Node> NodesNewLabel = new HashSet<Node>();
		NodesNewLabel.add(leader);
		int iterationCount = 1;
		memberships.put(leader, iterationCount);
		while (NodesNewLabel.size() != 0 & iterationCount < iterationStep) {
			NodesAssumingNewLabel.clear();
			iterationCount++;
			Set<Node> nodesAlreadyStudied = new HashSet<Node>();
			for (Node NodeNewLabel : NodesNewLabel) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}

				Set<Node> neighbours = graph.getNeighbours(NodeNewLabel);
				for (Node neighbour : neighbours) {
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}
					if (nodesAlreadyStudied.contains(neighbour) | memberships.keySet().contains(neighbour)) {
						continue;
					}
					nodesAlreadyStudied.add(neighbour);
					int posNodesWithNewLabel = getPosNodesWithNewLabel(graph, neighbour, memberships.keySet());
					int negNodesWithNewLabel = getNegNodesWithNewLabel(graph, neighbour, memberships.keySet());
					double profitability;
					if ((posNodesWithNewLabel + negNodesWithNewLabel) != 0
							& posNodesWithNewLabel - negNodesWithNewLabel > 0) {
						profitability = (double) (posNodesWithNewLabel - negNodesWithNewLabel)
								/ (double) (posNodesWithNewLabel + negNodesWithNewLabel);
					} else {
						profitability = 0;
					}
					if (profitability >= profitabilityThreshold.get(neighbour.getIndex())) {
						NodesAssumingNewLabel.add(neighbour);
						membershipsToAdd.put(neighbour, iterationCount);
					}
				}
			}
			memberships.putAll(membershipsToAdd);
			NodesNewLabel.clear();
			NodesNewLabel.addAll(NodesAssumingNewLabel);
		}
		return memberships;
	}

	/**
	 * Returns the number of positive neighbours with the new label of a given
	 * node.
	 * 
	 * @param graph The graph where the node resides.
	 * 
	 * @param node The node under observation.
	 * 
	 * @param nodesNewLabel Set of nodes in the graph which have already assumed the
	 * new label.
	 * 
	 * @return The set of positive neighbours with the new label of a give node.
	 * @throws InterruptedException if the thread was interrupted
	 */

	protected int getPosNodesWithNewLabel(CustomGraph graph, Node node, Set<Node> nodesNewLabel)
			throws InterruptedException {
		Set<Node> positiveNeighboursWithNewLabel = new HashSet<Node>();
		Set<Node> positiveNeighbours = graph.getPositiveNeighbours((MultiNode) node);
		for (Node positiveNeighbour : positiveNeighbours) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (nodesNewLabel.contains(positiveNeighbour)) {
				positiveNeighboursWithNewLabel.add(positiveNeighbour);
			}
		}
		return positiveNeighboursWithNewLabel.size();
	}

	/**
	 * Returns the number of negative neighbours with the new label of a given
	 * node.
	 * 
	 * @param graph The graph where the node resides.
	 * 
	 * @param node The node under observation.
	 * 
	 * @param nodesNewLabel Set of nodes in the graph which have already assumed the
	 * new label.
	 * 
	 * @return The set of negative neighbours with the new label of a give node.
	 * @throws InterruptedException if the thread was interrupted
	 */

	protected int getNegNodesWithNewLabel(CustomGraph graph, Node node, Set<Node> nodesNewLabel)
			throws InterruptedException {
		Set<Node> negativeNeighboursWithNewLabel = new HashSet<Node>();
		Set<Node> negativeNeighbours = graph.getNegativeNeighbours((MultiNode) node);
		for (Node negativeNeighbour : negativeNeighbours) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (nodesNewLabel.contains(negativeNeighbour)) {
				negativeNeighboursWithNewLabel.add(negativeNeighbour);
			}
		}
		return negativeNeighboursWithNewLabel.size();
	}

	/**
	 * Returns a cover containing the membership degrees of all nodes.
	 * 
	 * @param graph The graph which is being analyzed.
	 * 
	 * @param communities A mapping from the leader nodes to the iteration count
	 * mapping of their community members.
	 * 
	 * @return A cover containing each node's membership degree
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Cover getMembershipDegrees(CustomGraph graph, Map<Node, Map<Node, Integer>> communities)
			throws InterruptedException {
		Matrix membershipMatrix = new CCSMatrix(graph.getNodeCount(), communities.size());
		int communityIndex = 0;
		double membershipDegree;
		for (Node leader : communities.keySet()) {
			membershipMatrix.set(leader.getIndex(), communityIndex, 1.0);
			for (Map.Entry<Node, Integer> entry : communities.get(leader).entrySet()) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				membershipDegree = 1.0 / Math.pow(entry.getValue(), 2);
				membershipMatrix.set(entry.getKey().getIndex(), communityIndex, membershipDegree);
			}
			communityIndex++;
		}
		Cover cover = new Cover(graph, membershipMatrix);
		return cover;
	}

}