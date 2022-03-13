package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.utils.Pair;

import java.util.*;
import java.util.Map.Entry;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.sparse.CompressedVector;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

/**
 * The original standard version of the Link Communities Algorithm.
 * Handles only undirected, unweighted graphs.
 */
public class LinkCommunitiesAlgorithm implements
		OcdAlgorithm {
	
	/**
	 * Creates an instance of the algorithm.
	 */
	public LinkCommunitiesAlgorithm() {
	}
	
	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.LINK_COMMUNITIES_ALGORITHM;
	}
	
	@Override
	public HashMap<String, String> getParameters() {
		return new HashMap<String, String>();
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		return compatibilities;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws InterruptedException {
		/*
		 * Initializes the variables.
		 */
		List<Set<Edge>> communityEdges = new ArrayList<Set<Edge>>();
		List<Set<Node>> communityNodes = new ArrayList<Set<Node>>();
		List<Double> communityLinkDensities = new ArrayList<Double>();
		List<Pair<Integer, Integer>> mostSimilarPairs;
		Pair<Integer, Integer> mostSimilarPair;
		Set<Edge> firstCommunityEdges;
		Set<Edge> secondCommunityEdges;
		double currentPartitionDensity = 0;
		double maxPartitionDensity = Double.NEGATIVE_INFINITY;
		double firstLinkDensity;
		double secondLinkDensity;
		double newLinkDensity;
		int firstCommunity;
		int secondCommunity;
		/*
		 * Initializes the dendrogram construction
		 */
		List<Vector> linkageDegrees = calculateLinkageDegrees(graph);
		Matrix similarities = calculateEdgeSimilarities(graph, linkageDegrees);
		initDendrogramCreation(graph, communityEdges, communityNodes, communityLinkDensities);
		List<Set<Edge>> densestPartition = communityEdges;
		/*
		 * Constructs the dendrogram and determines the edge partition
		 * with the highest partition density.
		 */
		while(similarities.columns() > 1) {
			mostSimilarPairs = determineMostSimilarCommunityPairs(similarities);
			for(int i=0; i<mostSimilarPairs.size(); i++) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				mostSimilarPair = mostSimilarPairs.get(i);
				firstCommunity = mostSimilarPair.getFirst();
				secondCommunity = mostSimilarPair.getSecond();
				similarities = updateSimilarities(similarities, mostSimilarPair);
				firstCommunityEdges = communityEdges.get(firstCommunity);
				secondCommunityEdges = communityEdges.get(secondCommunity);
				firstCommunityEdges.addAll(secondCommunityEdges);
				Set<Node> firstCommunityNodes = communityNodes.get(firstCommunity);
				Set<Node> secondCommunityNodes = communityNodes.get(secondCommunity);
				firstCommunityNodes.addAll(secondCommunityNodes);
				firstLinkDensity = communityLinkDensities.get(firstCommunity);
				secondLinkDensity = communityLinkDensities.get(secondCommunity);
				newLinkDensity = calculateLinkDensity(firstCommunityEdges.size(), firstCommunityNodes.size());
				communityLinkDensities.set(firstCommunity, newLinkDensity);
				currentPartitionDensity += newLinkDensity - firstLinkDensity - secondLinkDensity;
			}
			for(int i=0; i<mostSimilarPairs.size(); i++) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				secondCommunity = mostSimilarPairs.get(i).getSecond();
				communityEdges.remove(secondCommunity);
				communityNodes.remove(secondCommunity);
				communityLinkDensities.remove(secondCommunity);
			}
			if(currentPartitionDensity >= maxPartitionDensity) {
				maxPartitionDensity = currentPartitionDensity;
				densestPartition = new ArrayList<Set<Edge>>(communityEdges);
			}
		}
		return calculatePartitionCover(graph, densestPartition);
	}

private Matrix calculateEdgeSimilarities(CustomGraph graph, List<Vector> linkageDegrees) throws InterruptedException {
		Matrix similarities = new CCSMatrix(graph.getEdgeCount(), graph.getEdgeCount());
		Iterator<Edge> rowEdgesIt = graph.edges().iterator();
		Edge rowEdge;
		Node source;
		Node target;
		List<Integer> edgeIndices = new ArrayList<Integer>();
		Iterator<Edge>  columnEdges;
		Edge columnEdge;
		Edge reverseRowEdge;
		Edge reverseColumnEdge;
		double similarity;
		while(rowEdgesIt.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			rowEdge = rowEdgesIt.next();
			source = rowEdge.getSourceNode();
			target = rowEdge.getTargetNode();
			reverseRowEdge = target.getEdgeToward(source);
			/*
			 * Sets similarities only if they have not been set already for the reverse Edge.
			 */
			if(reverseRowEdge == null || rowEdge.getIndex() < reverseRowEdge.getIndex()) {
				/*
				 * Sets similarities for in and out edges of the row edge target node.
				 */
				edgeIndices.add(rowEdge.getIndex());
				columnEdges = target.edges().iterator();
				while(columnEdges.hasNext()) {
					columnEdge = columnEdges.next();
					if(columnEdge.getIndex() < rowEdge.getIndex()) {
						reverseColumnEdge = columnEdge.getTargetNode().getEdgeToward(columnEdge.getSourceNode());
						if(reverseColumnEdge == null || columnEdge.getIndex() < reverseColumnEdge.getIndex()) {
							similarity = getSimpleSimilarity(source, columnEdge.getOpposite(target));
							similarities.set(rowEdge.getIndex(), columnEdge.getIndex(), similarity);
						}
					}
				}
				/*
				 * Sets similarities for in edges of the row edge source node.
				 * If a reverse edge of the row edge exists, it is set for the out edges also.
				 */
				columnEdges = source.edges().iterator();
				while(columnEdges.hasNext()) {
					columnEdge = columnEdges.next();
					if(columnEdge.getIndex() < rowEdge.getIndex() && columnEdge.getSourceNode() != target) {
						reverseColumnEdge = columnEdge.getTargetNode().getEdgeToward(columnEdge.getSourceNode());
						if(reverseColumnEdge == null || columnEdge.getIndex() < reverseColumnEdge.getIndex()) {
							similarity = getSimpleSimilarity(target, columnEdge.getOpposite(source));
							similarities.set(rowEdge.getIndex(), columnEdge.getIndex(), similarity);
						}
					}
				}
			}
		}
		int[] indices = new int[edgeIndices.size()];
		for(int i=0; i<edgeIndices.size(); i++) {
			indices[i] = edgeIndices.get(i);
		}
		if(indices.length > 0) {
			return similarities.select(indices, indices);
		}
		else {
			return new CCSMatrix(0, 0);
		}
	}
	
	/**
	 * Calculates the linkage degree vectors for all nodes.
	 * The linkage degrees are required for the calculation of edge similarity
	 * and originally referred to as the a vectors.
	 * @param graph The graph being analyzed.
	 * @return The linkage degree vector of each node, accessible via the list index that
	 * corresponds to the node index.
	 */
	private List<Vector> calculateLinkageDegrees(CustomGraph graph) throws InterruptedException {
		List<Vector> linkageDegrees = new ArrayList<Vector>();
		Iterator<Node> nodesIt = graph.nodes().iterator();
		Vector degreeVector;
		Node node;
		Node neighbor;
		Iterator<Edge> edgesIt;
		Edge edge;
		double linkageDegree;
		double neutral;
		double averageWeight;
		while(nodesIt.hasNext()) {
			degreeVector = new CompressedVector(graph.getNodeCount());
			node = nodesIt.next();
			edgesIt = node.edges().iterator();
			while(edgesIt.hasNext()) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				edge = edgesIt.next();
				neighbor = edge.getOpposite(node);
				linkageDegree = degreeVector.get(neighbor.getIndex());
				linkageDegree += graph.getEdgeWeight(edge) / node.getDegree();
				degreeVector.set(neighbor.getIndex(), linkageDegree);
			}
			/*
			 * Calculates the entry corresponding the node index as the average weight
			 * of all incident edges.
			 */
			neutral = 0;
			averageWeight = degreeVector.fold(Vectors.asSumAccumulator(neutral));
			degreeVector.set(node.getIndex(), averageWeight);
			linkageDegrees.add(degreeVector);
		}
		return linkageDegrees;
	}
	
	/**
	 * Identifies the edge community pairs with maximum similarity. 
	 * @param similarities The similarity matrix.
	 * @return A list of pairs with the indices of the identified edge communities. If several pairs are
	 * joined to a bigger community simultaneously, the first index of each pair will
	 * be the lowest index of the corresponding old communities. I.e. all old communities will be projected
	 * on the same new one with the lowest community index.
	 */
	private List<Pair<Integer, Integer>> determineMostSimilarCommunityPairs(Matrix similarities) throws InterruptedException {
		double maxSimilarity = Double.NEGATIVE_INFINITY;
		double currentSimilarity;
		TreeMap<Integer, Integer> mergedCommunities = new TreeMap<Integer, Integer>();
		Set<Integer> updatedCommunities = new HashSet<Integer>();
		int oldCommunity;
		int newCommunity;
		for(int j=0; j<similarities.columns() - 1; j++) {
			for(int i=j+1; i<similarities.rows(); i++) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				currentSimilarity = similarities.get(i, j);
				if(currentSimilarity >= maxSimilarity) {
					if(currentSimilarity > maxSimilarity) {
						mergedCommunities.clear();
						maxSimilarity = currentSimilarity;
					}
					newCommunity = j;
					if(mergedCommunities.containsKey(j)) {
						oldCommunity = mergedCommunities.get(j);
						if(oldCommunity <= newCommunity) {
							newCommunity = oldCommunity;
						}
						else {
							updatedCommunities.add(oldCommunity);
						}
					}
					if(mergedCommunities.containsKey(i)) {
						oldCommunity = mergedCommunities.get(i);
						if(oldCommunity <= newCommunity) {
							newCommunity = oldCommunity;
						}
						else {
							updatedCommunities.add(oldCommunity);
						}
					}
					if(updatedCommunities.size() > 0) {
						for(Entry<Integer, Integer> entry : mergedCommunities.entrySet()) {
							if(updatedCommunities.contains(entry.getValue())) {
								entry.setValue(newCommunity);
							}
						}
					}
					mergedCommunities.put(j, newCommunity);
					mergedCommunities.put(i, newCommunity);
				}
			}
		}
		List<Pair<Integer, Integer>> mostSimilarPairs = new ArrayList<Pair<Integer, Integer>>();
		Entry<Integer, Integer> lastPair;
		while(mergedCommunities.size() > 0) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			lastPair = mergedCommunities.lastEntry();
			if(lastPair.getKey() != lastPair.getValue()) {
				mostSimilarPairs.add(new Pair<Integer, Integer>(lastPair.getValue(), lastPair.getKey()));
			}
			mergedCommunities.remove(lastPair.getKey());
		}
		return mostSimilarPairs;
	}

	/**
	 * Updates the similarity matrix when the two edge communities given by mostSimilarPair are merged. 
	 * @param similarities The similarity matrix.
	 * @param mostSimilarPair A pair containing the indices of the communities that are merged.
	 * @return The updated similarity matrix.
	 */
	private Matrix updateSimilarities(Matrix similarities, Pair<Integer, Integer> mostSimilarPair) throws InterruptedException {
		int first = mostSimilarPair.getFirst();
		int second = mostSimilarPair.getSecond();
		int[] newIndices = new int[similarities.rows() - 1];
		double maxSimilarity;
		for(int i=0; i<similarities.columns(); i++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			if(i != second) {
				if(i <= first) {
					if(i < first) {
						maxSimilarity = Math.max(similarities.get(first, i), similarities.get(second, i));
						similarities.set(first, i, maxSimilarity);
					}
					newIndices[i] = i;
				}
				else {
					if(i < second) {
						maxSimilarity = Math.max(similarities.get(i, first), similarities.get(second, i));
						newIndices[i] = i;
					}
					else {
						maxSimilarity = Math.max(similarities.get(i, first), similarities.get(i, second));
						newIndices[i-1] = i;
					}
					similarities.set(i, first, maxSimilarity);
				}
			}

		}
		return similarities.select(newIndices, newIndices);
	}
	
	/**
	 * Initializes variables for the dendrogram creation.
	 * @param graph The graph being analyzed.
	 * @param communityEdges An edge partition indicating the edge communities.
	 * @param communityNodes A node cover derived from the edge partition.
	 * @param communityLinkDensities The link densities of all edge communities.
	 */
	private void initDendrogramCreation(CustomGraph graph, List<Set<Edge>> communityEdges,
			List<Set<Node>> communityNodes, List<Double> communityLinkDensities) throws InterruptedException {
		Iterator<Edge> edgesIt = graph.edges().iterator();
		Set<Edge> initEdgeSet;
		Set<Node> initNodeSet;
		Edge edge;
		Edge reverseEdge;
		while(edgesIt.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			edge = edgesIt.next();
			reverseEdge = edge.getTargetNode().getEdgeToward(edge.getSourceNode());
			if(reverseEdge == null || edge.getIndex() < reverseEdge.getIndex()) {
				initEdgeSet = new HashSet<Edge>();
				initEdgeSet.add(edge);
				if(reverseEdge != null) {
					initEdgeSet.add(reverseEdge);
				}
				communityEdges.add(initEdgeSet);
				initNodeSet = new HashSet<Node>();
				initNodeSet.add(edge.getSourceNode());
				initNodeSet.add(edge.getTargetNode());
				communityNodes.add(initNodeSet);
				communityLinkDensities.add(0d);
			}
		}
	}
	
	/**
	 * Calculates the weighted link density of a community. 
	 * @param edgeCount The number of community edges.
	 * @param nodeCount The number of community nodes.
	 * @return The weighted link density.
	 */
	private double calculateLinkDensity(int edgeCount, int nodeCount) {
		int denominator = (nodeCount - 2) * (nodeCount - 1);
		return (double)(edgeCount * (edgeCount - (nodeCount - 1))) / (double)denominator;
	}

	/**
	 * Derives a cover from an edge partition.
	 * @param graph The graph being analyzed.
	 * @param partition The edge partition from which the cover will be derived.
	 * @return A normalized cover of the graph.
	 */
	private Cover calculatePartitionCover(CustomGraph graph, List<Set<Edge>> partition) throws InterruptedException {
		Matrix memberships = new CCSMatrix(graph.getNodeCount(), partition.size());
		double belongingFactor;
		double edgeWeight;
		for(int i=0; i<partition.size(); i++) {
			for(Edge edge : partition.get(i)) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				edgeWeight = graph.getEdgeWeight(edge);
				belongingFactor = memberships.get(edge.getTargetNode().getIndex(), i) + edgeWeight;
				memberships.set(edge.getTargetNode().getIndex(), i, belongingFactor);
				belongingFactor = memberships.get(edge.getSourceNode().getIndex(), i) + edgeWeight;
				memberships.set(edge.getSourceNode().getIndex(), i, belongingFactor);
			}
		}
		return new Cover(graph, memberships);
	}
	
	private double getSimpleSimilarity(Node nodeA, Node nodeB) {
		Set<Node> commonNeighbors = new HashSet<Node>();
		Set<Node> totalNeighbors = new HashSet<Node>();
		if(nodeB.getEdgeToward(nodeA) != null) {
			commonNeighbors.add(nodeA);
			commonNeighbors.add(nodeB);
		}
		totalNeighbors.add(nodeA);
		totalNeighbors.add(nodeB);
		/*
		 * Check nodeA neighbors.
		 */
		Iterator<Node> neighborsIt = nodeA.neighborNodes().iterator();
		Node neighbor;
		while(neighborsIt.hasNext()) {
			neighbor = neighborsIt.next();
			if(neighbor.getEdgeBetween(nodeB) != null) {
				commonNeighbors.add(neighbor);
			}
			totalNeighbors.add(neighbor);
		}
		/*
		 * Checks nodeB neighbors.
		 */
		neighborsIt = nodeB.neighborNodes().iterator();
		while(neighborsIt.hasNext()) {
			totalNeighbors.add(neighborsIt.next());
		}
		return (double)commonNeighbors.size() / (double)totalNeighbors.size();
	}
	
}
