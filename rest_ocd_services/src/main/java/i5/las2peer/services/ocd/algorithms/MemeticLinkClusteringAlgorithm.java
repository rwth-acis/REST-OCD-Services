package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.MLinkAgent;
import i5.las2peer.services.ocd.algorithms.utils.MLinkIndividual;
import i5.las2peer.services.ocd.algorithms.utils.MLinkPopulation;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Random;
import java.util.Stack;

import java.lang.Double; 
import java.lang.Math;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.sparse.CompressedVector;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;

public class MemeticLinkClusteringAlgorithm implements OcdAlgorithm {
    
	/**
	 * Creates an instance of the algorithm.
	 */
	public MemeticLinkClusteringAlgorithm() {
	}
	
	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.M_LINK;
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
	/**
	 * 
	 */
    public Cover detectOverlappingCommunities(CustomGraph graph) throws InterruptedException {

		//TODO: remove double edges

		int debug = 55;
        Cover mlink = new Cover(graph);
        CustomGraph encoding = graph;
        HashMap<Edge, Edge> hMap = new HashMap<Edge,Edge>(); 
        Edge[] edgeArr = encoding.getEdgeArray();
		System.out.println(edgeArr.length);
		for(int i = 0; i < 2; i++){
			hMap.put(edgeArr[i], edgeArr[i+1]);
		}
		hMap.put(edgeArr[2], edgeArr[0]);
		for(int i = 3; i < 5; i++){
			hMap.put(edgeArr[i], edgeArr[i+1]);
		}
		hMap.put(edgeArr[5],edgeArr[3]);
        MLinkPopulation pop = new MLinkPopulation();
        MLinkAgent agent = new MLinkAgent();
        MLinkIndividual indi = new MLinkIndividual(hMap);
		MLinkIndividual labelProp = labelPropagation(graph);
		indi.mutation();
		// agent.addIndividual(indi);
		// pop.addAgent(agent);
        return mlink;
	}
	/**
	 * 
	 * @param parent1
	 * @param parent2
	 * @return
	 */
	public MLinkIndividual crossover(MLinkIndividual parent1, MLinkIndividual parent2){
		HashMap<Edge,Edge> individual = new HashMap<Edge,Edge>();
		Edge gene;
		int crossProbability = 50;

		Random rand = new Random();
		for(Edge key : parent1.getIndividual().keySet()){
			if(rand.nextInt(101) < crossProbability){
				gene = parent1.getIndividual().get(key);
			} else {
				gene = parent2.getIndividual().get(key);
			}
			individual.put(key, gene);
		}
		return new MLinkIndividual(individual);
	}
	/**
	 * Label Propagation
	 * @param graph initial graph
	 * @return Individual generated with label propagation
	 */
	public MLinkIndividual labelPropagation(CustomGraph graph){
		HashMap<Edge,Edge> genes = new HashMap<Edge,Edge>();
		HashMap<Node,Integer> labels = new HashMap<Node,Integer>();
		Node[] nodes = graph.getNodeArray();
		// Each node receives a unique label
		for(int i = 0; i < nodes.length; i++){
			labels.put(nodes[i], i);
		}
		ArrayList<Node> notVisited = new ArrayList<Node>(Arrays.asList(nodes));

		boolean stop = false;
		// reassign new labels for each node
		while(!stop){
			int size = notVisited.size();
			int node = new Random().nextInt(size);
			Node selected = notVisited.get(node);
			notVisited.remove(node);

			int newLabel = getMostFrequentLabel(labels, selected);
			labels.put(selected, newLabel);
			if(notVisited.isEmpty()){
				stop = true;
			}
		}

		/**
		 * Translation from community of nodes to locus based representation
		 */

		// store nodes with the same label in a hashmap
		HashMap<Integer,Set<Node>> labelNodes = new HashMap<Integer,Set<Node>>();
		for(Node node : labels.keySet()){
			labelNodes.put(labels.get(node), new HashSet<Node>());
		}

		// Fill sets with nodes of the corresponding label
		for(Node n : labels.keySet()){
			labelNodes.get(labels.get(n)).add(n);
		}

		// Locus based representation
		// Assign genes so that they represent the given community of edges
		Random rand = new Random();
		Set<Node> checkedNodes = new HashSet<Node>(); 
		Stack<Node> queue = new Stack<Node>();
		Set<Edge> sharedEdges = new HashSet<Edge>();
		for(Integer l : labelNodes.keySet()){
			Set<Node> commNodes = labelNodes.get(l);
			Node start = commNodes.iterator().next();
			Node pred = start;
			queue.add(start);
			while(!queue.empty()){
				Node curNode = queue.pop();

				// Check if the current node has neighbors that weren't already checked
				NodeCursor nghb = curNode.neighbors();
				boolean hasUncheckedNeighbor = false;
				for(int i = 0; i < nghb.size(); i++){
					if(!checkedNodes.contains(nghb.node())){
						hasUncheckedNeighbor = true;
						break;
					}
					nghb.cyclicNext();
				}
				if(!hasUncheckedNeighbor){
					continue;
				}

				if(curNode.degree() != 0){
					checkedNodes.add(curNode);
					EdgeCursor adjEdges = curNode.edges();
					Edge last;
					Edge cur;
					Edge first = null;
					Edge incoming = getEdge(pred, curNode);
					// Get the first edge that is either
					for(int i = 0; i < adjEdges.size(); i++){
						if(labels.get(adjEdges.edge().source()) == labels.get(adjEdges.edge().target())){
							if(checkedNodes.contains(adjEdges.edge().source()) || checkedNodes.contains(adjEdges.edge().target())){
								first = adjEdges.edge();
								break;
							}
						}
						adjEdges.cyclicNext();
					}
					
					if(first == null){
						for(int i = 0; i < adjEdges.size(); i++){
							if(checkedNodes.contains(adjEdges.edge().source()) || checkedNodes.contains(adjEdges.edge().target())){
								first = adjEdges.edge();
								sharedEdges.add(adjEdges.edge());
								break;
							}
						}
					}
					last = adjEdges.edge();

					for(int i = 0; i < adjEdges.size(); i++){
						adjEdges.cyclicNext();
						cur = adjEdges.edge();
						if(cur == first){
							continue;
						}
						if(labels.get(cur.source()) == labels.get(cur.target())){
							if(!checkedNodes.contains(cur.target()) || !checkedNodes.contains(cur.source())){
								if(curNode == cur.source() && !checkedNodes.contains(cur.target())){
									queue.add(cur.target());
								} else if(!checkedNodes.contains(cur.source())) {
									queue.add(cur.source());
								}
								genes.put(last, cur);
								last = cur;									
							}
						} else {
							// check whether the connected node is the target or source
							// then check if adjacent node was already checked and if act accordingly to split the shared edges with 50% chance
							if(curNode == cur.source()){
								if(!checkedNodes.contains(cur.target()) && !sharedEdges.contains(cur)){
									if(rand.nextInt(100) < 49){
										sharedEdges.add(cur);
										genes.put(cur, first);
									}
								} else if(checkedNodes.contains(cur.target()) && !sharedEdges.contains(cur)){
									sharedEdges.add(cur);
									genes.put(cur, first);
								}
							} else if(!checkedNodes.contains(cur.source()) && !sharedEdges.contains(cur)) {
								if(rand.nextInt(100) < 49){
									sharedEdges.add(cur);
									genes.put(cur, first);
								}
							} else if(checkedNodes.contains(cur.source()) && !sharedEdges.contains(cur)){
								sharedEdges.add(cur);
								genes.put(cur, first);
							}
						}
					}
					if(incoming != null){
						genes.put(last,incoming);
					}
					
				}
				pred = curNode;
			}
		}
		return new MLinkIndividual(genes);
	}
	/**
	 * returns the label with the highes frequency amongst neighbors
	 * @param labels current labels
	 * @param selected selected node
	 * @return new label
	 */
	public int getMostFrequentLabel(HashMap<Node,Integer> labels, Node selected){
		int mostFrequentLabel = -1;
		NodeCursor neighbors = selected.neighbors();
		int size = neighbors.size();
		HashMap<Integer,Integer> labelCount = new HashMap<Integer,Integer>();
		// count neighboring labels and save it in a hashmap
		for(int i = 0; i < size; i++){
			Node neighbor = neighbors.node();
			Integer label = labels.get(neighbor);
			int count = 0;
			if(labelCount.containsKey(label)){
				count = labelCount.get(label) + 1;
			}
			labelCount.put(label, count);
		}
		Integer lastLabel = -1;
		Set<Integer> maxLabels = new HashSet<Integer>();
		// go through neighboring labels and save the ones with the highes frequency
		// choose random if more than 1 label exists with the max frequency
		for(Integer i : labelCount.keySet()){
			if(mostFrequentLabel == -1){
				maxLabels.clear();
				maxLabels.add(i);
				mostFrequentLabel = i;
				lastLabel = labelCount.get(i);
			} else if(labelCount.get(i) == lastLabel){
				maxLabels.add(i);
			} else if(labelCount.get(i) > lastLabel){
				maxLabels.clear();
				maxLabels.add(i);
				mostFrequentLabel = i;
				lastLabel = labelCount.get(i);
			}
		}
		int labelSize = neighbors.size();
		int randLabel = new Random().nextInt(labelSize);
		int i = 0;
		for(Integer l : maxLabels){
			if(i == randLabel){
				mostFrequentLabel = l;
			}
			i++;
		}
		return mostFrequentLabel;
	}
	/**
	 * Returns the edge between the two nodes or null
	 * @param source 
	 * @param target 
	 * @return Edge that connects the two nodes or null
	 */
	public Edge getEdge(Node source, Node target){
		EdgeCursor edges = source.edges();
		Edge res = null;
		if(source != target){
			for(int i = 0; i < edges.size(); i++){
				Edge edge = edges.edge();
				if((edge.source() == source && edge.target() == target) || (edge.source() == target && edge.target() == source)){
					res = edge;
					break;
				}
				edges.cyclicNext();
			}
		}

		return res;
	}
	
	

}