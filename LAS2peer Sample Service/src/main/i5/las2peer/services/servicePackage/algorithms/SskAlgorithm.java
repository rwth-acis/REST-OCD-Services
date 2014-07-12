package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphType;

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

public class SskAlgorithm implements OcdAlgorithm {
	
	private int randomWalkIterationBound;
	private double randomWalkPrecisionFactor;
	private int membershipsIterationBound;
	private double membershipsPrecisionFactor;
	
	public SskAlgorithm() {
		randomWalkIterationBound = 1000;
		membershipsIterationBound = 1000;
		randomWalkPrecisionFactor = 0.001;
		membershipsPrecisionFactor = 0.001;
	}
	
	public SskAlgorithm(int randomWalkIterationBound, double randomWalkPrecisionFactor,
			int membershipsIterationBound, double membershipsPrecisionFactor) {
		this.randomWalkIterationBound = randomWalkIterationBound;
		this.randomWalkPrecisionFactor = randomWalkPrecisionFactor;
		this.membershipsPrecisionFactor = membershipsPrecisionFactor;
		this.membershipsIterationBound = membershipsIterationBound;
	}
	
	@Override
	public Algorithm getAlgorithm() {
		return Algorithm.SSK_ALGORITHM;
	}
	
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> types = new HashSet<GraphType>();
		types.add(GraphType.DIRECTED);
		types.add(GraphType.WEIGHTED);
		return types;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) {
		Matrix transitionMatrix = calculateTransitionMatrix(graph);
		Vector totalInfluences = executeRandomWalk(transitionMatrix);
		Map<Node, Integer> leaders = determineGlobalLeaders(graph, transitionMatrix, totalInfluences);
		Matrix memberships = calculateMemberships(graph, leaders);
		return new Cover(graph, memberships, getAlgorithm());
	}
	
	protected Matrix calculateMemberships(CustomGraph graph, Map<Node, Integer> leaders) {
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
	
	protected Matrix initMembershipCoefficientMatrix(CustomGraph graph, Map<Node, Integer> leaders) {
		Matrix coefficients = new CCSMatrix(graph.nodeCount(), graph.nodeCount());
		EdgeCursor edges = graph.edges();
		Edge edge;
		while(edges.ok()) {
			edge = edges.edge();
			coefficients.set(edge.target().index(), edge.source().index(), graph.getEdgeWeight(edge));
			edges.next();
		}
		Vector column;
		double norm;
		for(int i=0; i<coefficients.columns(); i++) {
			column = coefficients.getColumn(i);
			norm = column.fold(Vectors.mkManhattanNormAccumulator());
			if(norm > 0) {
				coefficients.setColumn(i, column.divide(norm));
			}
		}
		return coefficients;
	}
	
	/*
	 * Returns the global leaders of the graph.
	 * @param graph The graph being analyzed.
	 * @param transitionMatrix The transition matrix used for the random walk.
	 * @param totalInfluences The total influences determined via the random walk
	 * @return The global leaders of the graph. The community index of each leader node is 
	 * derivable from the mapping. Note that several leaders may belong to the same
	 * community.
	 */
	protected Map<Node, Integer> determineGlobalLeaders(CustomGraph graph, Matrix transitionMatrix, Vector totalInfluences){
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
	
	/*
	 * Executes a random walk on the transition matrix and returns the total node influences. 
	 * @param transitionMatrix The transition matrix.
	 * @return A vector containing the total influence of each node under the corresponding node index.
	 */
	protected Vector executeRandomWalk(Matrix transitionMatrix) {
		Vector vec1 = new BasicVector(transitionMatrix.columns());
		for(int i=0; i<vec1.length(); i++) {
			vec1.set(i, 1.0 / vec1.length());
		}
		Vector vec2 = new BasicVector(vec1.length());
		for(int i=0; vec1.subtract(vec2).fold(Vectors.mkInfinityNormAccumulator()) > randomWalkPrecisionFactor / (double)vec1.length()
				&& i < randomWalkIterationBound; i++) {
			vec2 = new BasicVector(vec1);
			vec1 = transitionMatrix.multiply(vec1);
		}
		return vec1;
	}
	
	/*
	 * Calculates the transition matrix for the random walk phase. 
	 * @param graph The graph being analyzed.
	 * @return The normalized transition matrix.
	 */
	protected Matrix calculateTransitionMatrix(CustomGraph graph) {
		Matrix transitionMatrix = new CCSMatrix(graph.nodeCount(), graph.nodeCount());
		NodeCursor nodes = graph.nodes();
		Node node;
		NodeCursor predecessors;
		Node predecessor;
		while(nodes.ok()) {
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
			column = transitionMatrix.getColumn(i);
			norm = column.fold(Vectors.mkManhattanNormAccumulator());
			if(norm > 0) {
				transitionMatrix.setColumn(i, column.divide(norm));
			}
		}
		return transitionMatrix;
	}
	
	/*
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
