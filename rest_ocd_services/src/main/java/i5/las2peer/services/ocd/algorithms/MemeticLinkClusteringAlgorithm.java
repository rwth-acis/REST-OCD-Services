package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.MLinkAgent;
import i5.las2peer.services.ocd.algorithms.utils.MLinkIndividual;
import i5.las2peer.services.ocd.algorithms.utils.MLinkPopulation;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.measures.EigenvectorCentrality;
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
import java.util.AbstractMap.SimpleEntry;

import java.lang.Double; 
import java.lang.Math;

import org.apache.commons.exec.ExecuteException;
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
        // MLinkPopulation pop = new MLinkPopulation();
        // MLinkAgent agent = new MLinkAgent();
        // MLinkIndividual indi = new MLinkIndividual(hMap);
		MLinkIndividual labelProp = labelPropagation(graph);
		// MLinkIndividual localExp = localExpansion(graph);
		// MLinkIndividual eigenVek = localExpansionEigen(graph);
		// indi.mutation();
		double fitness = labelProp.getFitness();
		int ld = 0;
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
	 * Translates the community of nodes to a MLinkIndividual
	 * @param labels Nodes with the according Community as a label
	 * @return individual 
	 */
	public MLinkIndividual translateToIndividual(HashMap<Node,Integer> labels){
		HashMap<Edge,Edge> genes = new HashMap<Edge,Edge>();
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
		Set<Node> isQueued = new HashSet<Node>();
		Stack<SimpleEntry<Edge,Node>> queue = new Stack<SimpleEntry<Edge,Node>>();
		Set<Edge> sharedEdges = new HashSet<Edge>();
		for(Integer l : labelNodes.keySet()){
			Set<Node> commNodes = labelNodes.get(l);
			Node start = commNodes.iterator().next();
			queue.add(new SimpleEntry<Edge,Node>(null,start));
			isQueued.add(start);
			while(!queue.empty()){
				SimpleEntry<Edge,Node> curEntry = queue.pop();
				Node curNode = curEntry.getValue();
				checkedNodes.add(curNode);
				// Check if the current node has neighbors that weren't already checked
				NodeCursor nghb = curNode.neighbors();
				boolean hasUncheckedNeighbor = false;
				for(int i = 0; i < nghb.size(); i++){
					if(!checkedNodes.contains(nghb.node())){
						hasUncheckedNeighbor = true;
						break;
					} else if(labels.get(nghb.node()) != labels.get(curNode)){
						hasUncheckedNeighbor = true;
						break;
					}
					nghb.cyclicNext();
				}
				if(!hasUncheckedNeighbor){
					continue;
				}

				if(curNode.degree() != 0){
					EdgeCursor adjEdges = curNode.edges();
					Edge last = null;
					Edge cur;
					Edge first = curEntry.getKey();
					// Set first on an Edge that is inside the current community
					if(first == null){
						for(int i = 0; i < adjEdges.size(); i++){
							cur = adjEdges.edge();
							Node other = (curNode == cur.source())? cur.target():cur.source();
							if(labels.get(other) == labels.get(curNode)){
								first = cur;
								adjEdges.cyclicNext();
								break;
							} else if(!checkedNodes.contains(other) || !sharedEdges.contains(cur)) {
								first = cur;
							}
							adjEdges.cyclicNext();
						}
						if(first == null){
							break;
						}

						// Put Root edge on itself
						genes.put(first,first);
						Node other = (curNode == first.source())? first.target():first.source();
						if(labels.get(other) == labels.get(curNode) && !isQueued.contains(other)){
							queue.add(new SimpleEntry<Edge,Node>(first, other));
							isQueued.add(other);
						}
					}
					last = first;
					sharedEdges.add(first);
					// Create a circle with the last edge pointing on the first
					for(int i = 0; i < adjEdges.size(); i++){
						adjEdges.cyclicNext();
						cur = adjEdges.edge();
						if(first == cur){
							continue;
						}
						Node other = (curNode == cur.source())? cur.target():cur.source();
						if(labels.get(cur.source()) == labels.get(cur.target())){
							if(!checkedNodes.contains(other)){
								if(!isQueued.contains(other)){
									queue.add(new SimpleEntry<Edge,Node>(cur, other));
									isQueued.add(other);
								}
								if(last != first){
									genes.put(last, cur);		
								}		
								last = cur;
							}
						} else {
							// check whether the connected node is the target or source
							// then check if adjacent node was already checked and if act accordingly to split the shared edges with 50% chance
							if(!checkedNodes.contains(other)){
								if(rand.nextInt(100) < 49){
									sharedEdges.add(cur);
									genes.put(cur, first);
								}
							} else if(!sharedEdges.contains(cur)){
								sharedEdges.add(cur);
								genes.put(cur, first);
							}
						}
					}
					if(last != first){
						genes.put(last, first);
					}
					

				}
			}
		}
		return new MLinkIndividual(genes);
	}


	/**
	 * Label Propagation
	 * @param graph initial graph
	 * @return Individual generated with label propagation
	 */
	public MLinkIndividual labelPropagation(CustomGraph graph){
		HashMap<Node,Integer> labels = new HashMap<Node,Integer>();
		Node[] nodes = graph.getNodeArray();
		// Each node receives a unique label
		for(int i = 0; i < nodes.length; i++){
			labels.put(nodes[i], i);
		}
		ArrayList<Node> notVisited = new ArrayList<Node>(Arrays.asList(nodes));

		// reassign new labels for each node
		while(!notVisited.isEmpty()){
			int size = notVisited.size();
			int node = new Random(5).nextInt(size);
			Node selected = notVisited.get(node);
			notVisited.remove(node);
			int newLabel = getMostFrequentLabel(labels, selected);
			labels.put(selected, newLabel);
		}

		return translateToIndividual(labels);
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
		int randLabel = new Random(10).nextInt(labelSize);
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
	/**
	 * Local Expansion with random seed
	 * @param graph initial graph
	 * @return	returns new MLinkIndividual 
	 */
	public MLinkIndividual localExpansion(CustomGraph graph){
		HashMap<Node,Integer> communities = new HashMap<Node,Integer>();
		Node[] nodeArr = graph.getNodeArray();
		ArrayList<Node> nodes = new ArrayList<Node>(Arrays.asList(nodeArr));
		int curComm = 0;
		Random rand = new Random();

		while(!nodes.isEmpty()){
			//select random seed node;
			int seedIndex = rand.nextInt(nodes.size());
			Node seed = nodes.get(seedIndex);
			communities.put(seed, curComm);
			nodes.remove(seed);

			// Create natural community and remove nodes
			NodeCursor neighbors = seed.neighbors();
			for(int i = 0; i < neighbors.size(); i++){
				Node cur = neighbors.node();
				communities.put(cur,curComm);
				nodes.remove(cur);
				neighbors.cyclicNext();
			}
			curComm++;
		}
		return translateToIndividual(communities);
	}

	public MLinkIndividual localExpansionEigen(CustomGraph graph){
		try{
			EigenvectorCentrality eigenVectorCentrality = new EigenvectorCentrality();
			CentralityMap centralities = eigenVectorCentrality.getValues(graph);
			HashMap<Node,Integer> communities = new HashMap<Node,Integer>();
			ArrayList<Node> nodes = new ArrayList<Node>(Arrays.asList(graph.getNodeArray()));
			int curComm = 0;
			
			while(!nodes.isEmpty()){
				// Select seed based on Eigenvector centrality
				Node seed = nodes.get(0);
				for(int i = 1; i < nodes.size(); i++){
					if(centralities.getNodeValue(seed) < centralities.getNodeValue(nodes.get(i))){
						seed = nodes.get(i);
					}
				}
				communities.put(seed, curComm);
				nodes.remove(seed);
				// Create natural community and remove nodes
				NodeCursor neighbors = seed.neighbors();
				for(int i = 0; i < neighbors.size(); i++){
					Node cur = neighbors.node();
					communities.put(cur,curComm);
					nodes.remove(cur);
					neighbors.cyclicNext();
				}
				curComm++;
			}

			return translateToIndividual(communities);

		} catch(Exception e){
			System.out.println(e);
			return null;
		}
		
		


	}

}