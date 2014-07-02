package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphType;
import i5.las2peer.services.servicePackage.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.sparse.CompressedVector;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;

public class LinkCommunitiesAlgorithm implements
		OverlappingCommunityDetectionAlgorithm {

	
	
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.WEIGHTED);
		compatibilities.add(GraphType.DIRECTED);
		return compatibilities;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) {
		/*
		 * Initializes the variables.
		 */
		List<Vector> linkageDegrees = calculateLinkageDegrees(graph);
		Map<Integer, Set<Edge>> communityEdges = new HashMap<Integer, Set<Edge>>();
		Map<Integer, Set<Node>> communityNodes = new HashMap<Integer, Set<Node>>();
		Map<Integer, Double> communityLinkDensities = new HashMap<Integer, Double>();
		Pair<Integer, Integer> mostSimilarPair;
		Set<Edge> firstCommunityEdges;
		Set<Edge> secondCommunityEdges;
		double currentPartitionDensity = 0;
		double maxPartitionDensity = 0;
		double firstLinkDensity;
		double secondLinkDensity;
		double newLinkDensity;
		Map<Integer, Set<Edge>> densestPartition = new HashMap<Integer, Set<Edge>>();
		/*
		 * Initializes the dendrogram construction
		 */
		Matrix similarities = calculateEdgeSimilarities(linkageDegrees);
		initDendrogramCreation(graph, communityEdges, communityNodes, communityLinkDensities);
		/*
		 * Constructs the dendrogram and determines the edge partition
		 * with the highest partition density.
		 */
		while(similarities.columns() > 1) {
			mostSimilarPair = determineMostSimilarCommunityPair(similarities);
			similarities = updateSimilarities(similarities, mostSimilarPair);
			firstCommunityEdges = communityEdges.get(mostSimilarPair.getFirst());
			secondCommunityEdges = communityEdges.get(mostSimilarPair.getSecond());
			firstCommunityEdges.addAll(secondCommunityEdges);
			communityEdges.remove(mostSimilarPair.getSecond());
			Set<Node> firstCommunityNodes = communityNodes.get(mostSimilarPair.getFirst());
			Set<Node> secondCommunityNodes = communityNodes.get(mostSimilarPair.getSecond());
			firstCommunityNodes.addAll(secondCommunityNodes);
			communityNodes.remove(mostSimilarPair.getSecond());
			firstLinkDensity = communityLinkDensities.get(mostSimilarPair.getFirst());
			secondLinkDensity = communityLinkDensities.get(mostSimilarPair.getSecond());
			newLinkDensity = calculateLinkDensity(firstCommunityEdges.size(), firstCommunityNodes.size());
			communityLinkDensities.put(mostSimilarPair.getFirst(), newLinkDensity);
			communityLinkDensities.remove(mostSimilarPair.getSecond());
			currentPartitionDensity += newLinkDensity - firstLinkDensity - secondLinkDensity;
			if(currentPartitionDensity > maxPartitionDensity) {
				maxPartitionDensity = currentPartitionDensity;
				densestPartition = new HashMap<Integer, Set<Edge>>(communityEdges);
			}
		}
		return calculatePartitionCover(graph, densestPartition);
	}

	
	/*
	 * Calculates a edge similarity matrix containing a similarity value for each edge pair. 
	 * @param linkageDegrees The linkage degree vectors calculated for each node.
	 * @return A similarity matrix S containing the edge similarity for two edges e_ik, e_jk with i>j in 
	 * the entry S_ij.
	 */
	private Matrix calculateEdgeSimilarities(List<Vector> linkageDegrees) {
		Matrix similarities = new CCSMatrix(linkageDegrees.size(), linkageDegrees.size());
		List<Double> lengths = new ArrayList<Double>();
		double length;
		for(int i=0; i<linkageDegrees.size(); i++) {
			Vector vec = linkageDegrees.get(i);
			length = vec.fold(Vectors.mkEuclideanNormAccumulator());
			lengths.add(length);
		}
		double innerProduct;
		double similarity;
		for(int i=0; i<linkageDegrees.size(); i++) {
			Vector vecI = linkageDegrees.get(i);
			for(int j=0; j<i; j++) {
				Vector vecJ = linkageDegrees.get(j);
				innerProduct = vecI.innerProduct(vecJ);
				similarity = innerProduct /
						(Math.pow(lengths.get(i), 2) + Math.pow(lengths.get(j), 2) - innerProduct);
				similarities.set(i, j, similarity);
			}
		}
		return similarities;
	}
	
	
	/*
	 * Calculates the linkage degree vectors for all nodes.
	 * The linkage degrees are required for the calculation of edge similarity
	 * and originally referred to as the A matrix.
	 * @param graph The graph being analyzed.
	 * @return The linkage degree vector of each node, accessible via the list index that
	 * corresponds to the node index.
	 */
	private List<Vector> calculateLinkageDegrees(CustomGraph graph) {
		List<Vector> linkageDegrees = new ArrayList<Vector>();
		NodeCursor nodeIt1 = graph.nodes();
		int successorCount;
		double linkageDegree;
		double reflexiveLinkageDegree;
		while(nodeIt1.ok()) {
			Vector degreeVector = new CompressedVector(graph.nodeCount());
			Node node = nodeIt1.node();
			EdgeCursor outEdges = node.outEdges();
			successorCount = outEdges.size();
			/*
			 * Used to calculate the reflexive A_ii entry.
			 */
			reflexiveLinkageDegree = 0;
			while(outEdges.ok()) {
				Edge outEdge = outEdges.edge();
				linkageDegree = graph.getEdgeWeight(outEdge) / successorCount;
				degreeVector.set(outEdge.target().index(), linkageDegree);
				reflexiveLinkageDegree += linkageDegree;
				outEdges.next();
			}
			degreeVector.set(node.index(), reflexiveLinkageDegree);
			linkageDegrees.add(degreeVector);
			nodeIt1.next();
		}
		return linkageDegrees;
	}
	
	/*
	 * Identifies the two edge communities which are the most similar. 
	 * @param similarities The similarity matrix.
	 * @return A pair with the indices of the identified communities.
	 */
	private Pair<Integer, Integer> determineMostSimilarCommunityPair(Matrix similarities) {
		Pair<Integer, Integer> mostSimilarPair = new Pair<Integer, Integer>();
		double maxSimilarity = Double.NEGATIVE_INFINITY;
		double currentSimilarity;
		for(int i=1; i<similarities.rows(); i++) {
			for(int j=0; j<i; j++) {
				currentSimilarity = similarities.get(i, j);
				if(currentSimilarity > maxSimilarity) {
					maxSimilarity = currentSimilarity;
					mostSimilarPair.setFirst(i);
					mostSimilarPair.setSecond(j);
				}
			}
		}
		return mostSimilarPair;
	}

	/*
	 * Updates the similarity matrix when the two edge communities given by mostSimilarPair are merged. 
	 * @param similarities The similarity matrix.
	 * @param mostSimilarPair A pair containing the indices of the communities that are merged.
	 * @return The updated similarity matrix.
	 */
	private Matrix updateSimilarities(Matrix similarities, Pair<Integer, Integer> mostSimilarPair) {
		int first = mostSimilarPair.getFirst();
		int second = mostSimilarPair.getSecond();
		int[] newIndices = new int[similarities.rows() - 1];
		double maxSimilarity;
		for(int i=0; i<similarities.columns(); i++) {
			if(i != second) {
				if(i < first) {
					maxSimilarity = Math.max(similarities.get(first, i), similarities.get(second, i));
					similarities.set(first, i, maxSimilarity);
				}
				else if (i > first) {
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
	
	/*
	 * Initializes variables for the dendrogram creation.
	 * @param graph The graph being analyzed.
	 * @param communityEdges An edge partition indicating the edge communities.
	 * @param communityNodes A node cover derived from the edge partition.
	 * @param communityLinkDensities The link densities of all edge communities.
	 */
	private void initDendrogramCreation(CustomGraph graph, Map<Integer, Set<Edge>> communityEdges,
			Map<Integer, Set<Node>> communityNodes, Map<Integer, Double> communityLinkDensities) {
		EdgeCursor edges = graph.edges();
		Set<Edge> initEdgeSet;
		Set<Node> initNodeSet;
		while(edges.ok()) {
			Edge edge = edges.edge();
			initEdgeSet = new HashSet<Edge>();
			initEdgeSet.add(edge);
			communityEdges.put(edge.index(), initEdgeSet);
			initNodeSet = new HashSet<Node>();
			initNodeSet.add(edge.source());
			initNodeSet.add(edge.target());
			communityNodes.put(edge.index(), initNodeSet);
			communityLinkDensities.put(edge.index(), 0d);
			edges.next();
		}
	}
	
	/*
	 * Calculates the weighted link density of a community. 
	 * @param edgeCount The number of community edges.
	 * @param nodeCount The number of community nodes.
	 * @return The weighted link density.
	 */
	private double calculateLinkDensity(int edgeCount, int nodeCount) {
		int denominator = (nodeCount - 2) * (nodeCount - 1);
		return (double)(edgeCount * (edgeCount - (nodeCount - 1))) / (double)denominator;
	}

	/*
	 * Derives a cover from an edge partition.
	 * @param graph The graph being analyzed.
	 * @param partition The edge partition from which the cover will be derived.
	 * @return A normalized cover of the graph.
	 */
	private Cover calculatePartitionCover(CustomGraph graph, Map<Integer, Set<Edge>> partition) {
		Matrix memberships = new CCSMatrix(graph.nodeCount(), partition.size());
		double belongingFactor;
		double edgeWeight;
		for(int i=0; i<partition.size(); i++) {
			for(Edge edge : partition.get(i)) {
				edgeWeight = graph.getEdgeWeight(edge);
				belongingFactor = memberships.get(edge.target().index(), i) + edgeWeight;
				memberships.set(edge.target().index(), i, belongingFactor);
				belongingFactor = memberships.get(edge.source().index(), i) + edgeWeight;
				memberships.set(edge.source().index(), i, belongingFactor);
			}
		}
		Cover cover = new Cover(graph, memberships);
		cover.doNormalize();
		return cover;
	}
	
}
