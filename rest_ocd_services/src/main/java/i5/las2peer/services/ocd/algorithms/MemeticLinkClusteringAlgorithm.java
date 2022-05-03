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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Random;
import java.util.Stack;
import java.util.AbstractMap.SimpleEntry;

import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer.PopulationSize;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

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
		// Global variables
		final double initialDiversity;
		int treeSize = 13;
		int mutationProbability = 5;
		int localSearchProbability = 10;
		int genWithoutImprovement = 35;
		boolean termination = false;

		Random rand = new Random();
		MLinkPopulation population = new MLinkPopulation(treeSize);
		final MLinkIndividual solution;
		HashMap<Integer,HashSet<Node>> communitySet;
		CustomGraph encoding = removeDoubleEdges(graph);
		

		// Initialize population
		for(int i = 0; i < treeSize; i++){
			MLinkAgent agent = new MLinkAgent();
			for(int j = 0; j < 6; j++){
				// put bound on 3 if label propagation works
				int init = rand.nextInt(2);
				// TODO: Label propagation doesn't work
				if(init == 0){
					agent.addIndividual(localExpansionEigen(encoding));
				} else if(init == 1){
					agent.addIndividual(localExpansion(encoding));
				} else {
					agent.addIndividual(labelPropagation(encoding));
				}
			}
			population.addAgent(agent);
		}
		population.swapUp();
		population.swapUp();
		
		// Save the initial diversity to compare to later diversity changes
		initialDiversity = population.calcDiversity();

		// Memetic algorithm
		int counter = 0;
		int debug = 0;
		double lastFitness = population.getAgent(0).getPocket().getFitness();
		while(!termination){
			System.out.println("iteration: " + debug);
			debug++;
			for(int i = 0; i < treeSize; i++){
				MLinkAgent curAgent = population.getAgent(i);
				SimpleEntry<MLinkIndividual,MLinkIndividual> parents;
				double diversity = population.calcDiversity();
				if(diversity < initialDiversity/2){
					parents = population.farSelect(i);
				} else {
					parents = population.closeSelect(i);
				}
				MLinkIndividual offspring = crossover(parents);
				offspring.mutate(mutationProbability);
				
				if(rand.nextInt(100) < localSearchProbability){
					offspring.localSearch();
				}
				curAgent.addIndividual(offspring);
				population.swapUp();
			}
			// Check if termination criteria is met
			double newFitness = population.getAgent(0).getPocket().getFitness();
			if(newFitness == lastFitness){
				counter++;
			} else {
				counter = 0;
				lastFitness = newFitness;
			}
			if(counter == genWithoutImprovement){
				termination = true;
			}

		}

		solution = population.getAgent(0).getPocket();
		communitySet = solution.getNodeCommunity();
		communitySet = postProcessing(communitySet, encoding);
		Matrix membershipMatrix = getMembershipMatrix(communitySet, encoding, solution.getCommunities().keySet().size());
		return new Cover(graph, membershipMatrix);

	}
	/**
	 * Creates a copy of the original graph and removes the undirected doubled edges
	 * @param graph the graph to be copied 
	 * @return a copy with max. 1 edge between each node
	 */
	public CustomGraph removeDoubleEdges(CustomGraph graph){
		CustomGraph encoding = new CustomGraph(graph);
		Edge[] edgesArray = encoding.getEdgeArray();
		ArrayList<Edge> edges = new ArrayList<Edge>(Arrays.asList(edgesArray));
		while(!edges.isEmpty()){
			Edge tmp = edges.remove(0);
			Node source = tmp.source();
			Node target = tmp.target();
			Edge reversed = target.getEdgeTo(source);
			if(reversed != null){
				encoding.removeEdge(reversed);
				edges.remove(reversed);
			}
		}
		return encoding;
	}
	/**
	 * Uniform Crossover operator
	 * @param parents tuple of two parents
	 * @return New individual created out of the two parents
	 */
	public MLinkIndividual crossover(SimpleEntry<MLinkIndividual,MLinkIndividual> parents){
		MLinkIndividual parent1 = parents.getKey();
		MLinkIndividual parent2 = parents.getValue();
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
			int node = new Random().nextInt(size);
			Node selected = notVisited.remove(node);
			int newLabel = labels.get(selected);
			if(selected.neighbors().size() > 0){
				newLabel = getMostFrequentLabel(labels, selected);
			}
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

	public HashMap<Integer,HashSet<Node>> postProcessing(HashMap<Integer,HashSet<Node>> communitySet,CustomGraph graph){
		HashMap<Node,HashSet<Integer>> nodes = new HashMap<Node,HashSet<Integer>>();
		HashMap<Node,HashSet<Integer>> updatedNodes  = new HashMap<Node,HashSet<Integer>>();
		for(Node n : graph.getNodeArray()){
			nodes.put(n, new HashSet<Integer>());
			updatedNodes.put(n, new HashSet<Integer>());
		}
		for(Integer community : communitySet.keySet()){
			for(Node n : communitySet.get(community)){
				nodes.get(n).add(community);
				updatedNodes.get(n).add(community);
			}
		}
        // Look at every node with more than 1 community and check if the node adds to the intra density of the community
		
		for(Node n : nodes.keySet()){
			// Check if Node is part of more than 1 community
			if(nodes.get(n).size() < 2 ){
				continue;
			} 
			// Check for communities consisting of only 1 node
			ArrayList<Integer> delCommunities = new ArrayList<Integer>();
			for(Integer com : nodes.get(n)){
				if(communitySet.get(com).size() == 1){
					communitySet.remove(com);
					delCommunities.add(com);					
				}
			}
			for(Integer com : delCommunities){
				nodes.get(n).remove(com);
			}

			// Check again if node is part of multiple communities after deletion process
			if(nodes.get(n).size() < 2 ){
				continue;
			}   

            int bestCommunity = -1;
            double bestCommunityIntra = -1;
			int communityCount = updatedNodes.get(n).size();

			// Check for every community of Node n if it adds to the intra density and remove if not
            for(Integer com : nodes.get(n)){
                HashSet<Node> nodeRemoved = new HashSet<>(communitySet.get(com));
                nodeRemoved.remove(n);
				HashMap<Node,HashSet<Integer>> copyNodes = new HashMap<Node,HashSet<Integer>>(updatedNodes);
				copyNodes.put(n,new HashSet<Integer>(copyNodes.get(n)));
				copyNodes.get(n).remove(com);
				double removedIntra = intraDensity(nodeRemoved, copyNodes);
				double normalIntra;
				if(communitySet.get(com).size() == 2){
					normalIntra = 0;
				} else {
					normalIntra = intraDensity(communitySet.get(com), updatedNodes);
				}
				

				// If the intra Density is greater without the node remove it from the community
				if(removedIntra > normalIntra || (communitySet.get(com).size() == 2 && removedIntra == normalIntra)){
					communitySet.get(com).remove(n);
					updatedNodes.get(n).remove(com);
					communityCount = communityCount - 1;
					if(bestCommunityIntra < removedIntra){
						bestCommunityIntra = removedIntra;
						bestCommunity = com;
					}
				}
            }

			// If the node is now part of no community add it to the one with the least intra density loss
            if(updatedNodes.get(n).isEmpty()){
                communitySet.get(bestCommunity).add(n);
                updatedNodes.get(n).add(bestCommunity);
            }
		}
		return communitySet;
	} 	
	/**
	 * Calculates the intra density for a graph with community distribution
	 * @param nodes Set of the current nodes inside the community	
	 * @param communities HashMap with nodes and corresponding communities
	 * @return intra density
	 */
	public double intraDensity(HashSet<Node> nodes, HashMap<Node,HashSet<Integer>> communities){
		double count = 0;
		for(Node n : nodes){
			EdgeCursor edges = n.edges();
			for(int i = 0; i < edges.size(); i++){
				Node target = edges.edge().target();
				Node source = edges.edge().source();
				HashSet<Integer> intersection = new HashSet<Integer>(communities.get(source));
				intersection.retainAll(communities.get(target));
				if(!intersection.isEmpty()){
					count++;
				}
				edges.cyclicNext();
			}
		}
		return 2*((count/2)/nodes.size());
	}
	/**
	 *  Intra density that puts more weight on the amount of edges
	 * @param nodes Set of the current nodes inside the community
	 * @param communities HashMap with nodes and corresponding communities
	 * @return intra density
	 */
	public double intraDensity2(HashSet<Node> nodes, HashMap<Node,HashSet<Integer>> communities){
		double count = 0;
		for(Node n : nodes){
			EdgeCursor edges = n.edges();
			for(int i = 0; i < edges.size(); i++){
				Node target = edges.edge().target();
				Node source = edges.edge().source();
				HashSet<Integer> intersection = new HashSet<Integer>(communities.get(source));
				intersection.retainAll(communities.get(target));
				if(!intersection.isEmpty()){
					count++;
				}
				edges.cyclicNext();
			}
		}
		return 2*((count/2)/Math.pow(nodes.size(),2));
	}

	
	/**
	 * Creates a membership matrix for the giben Map
	 * @param communitySet Map with nodes and their communities
	 * @param graph	initial graph
	 * @param communityNumber amount of communities
	 * @return membership matrix
	 */
	public Matrix getMembershipMatrix(HashMap<Integer,HashSet<Node>> communitySet, CustomGraph graph, int communityNumber){
		Matrix membershipMatrix = new Basic2DMatrix(graph.nodeCount(),communityNumber);
		for(Integer comm : communitySet.keySet()){
			for(Node n : communitySet.get(comm)){
				membershipMatrix.set(n.index(), comm, 1);
			}
		}
		return membershipMatrix;
	}
}