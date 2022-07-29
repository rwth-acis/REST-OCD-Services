package i5.las2peer.services.ocd.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.Collections;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;


import i5.las2peer.services.ocd.algorithms.utils.MaximalCliqueSearch;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import y.base.Edge;
import y.base.Node;
import y.base.NodeCursor;
import y.base.Graph;

public class LOCAlgorithm implements OcdAlgorithm{
	
	/**
	Parameter to control the size of a community. The larger the value of α,
	the smaller the community size is.
	The default value is 1. Must be greater than 0.
	 */
	private double alpha = 1.0;
	/**
	A value to determine the size of the used k-cliques.
	The default value is 3. Must be at least 3.
	Recommended are values between 3 and 6.
	*/
	private int k = 3;
	/**
	The cutoff distance that determines the local density of a node.
	The default value is 1. Must be at least 1.
	Recommended are values between 1 and 3.
	 */
	private int dc = 1;
	
	/*
	 * PARAMETER NAMES
	 */
	
	protected static final String  ALPHA_NAME = "alpha";
	
	protected static final String K_NAME = "k";
	
	protected static final String DC_NAME = "dc";
	
	
	/**
	 * Creates a standard instance of the algorithm.
	 * All attributes are assigned their default values.
	 */
	public LOCAlgorithm() {
	}
	
	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.LOC_ALGORITHM;
	}
	
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		return compatibilities;
	}
	@Override
	public Map<String, String> getParameters() {
	Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(ALPHA_NAME, Double.toString(alpha));
		parameters.put(K_NAME, Integer.toString(k));
		parameters.put(DC_NAME, Integer.toString(dc));
		return parameters;
	}

	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.containsKey(ALPHA_NAME)) {
			alpha = Double.parseDouble(parameters.get(ALPHA_NAME));
			if(alpha <= 0) {
				throw new IllegalArgumentException("alpha must be at least 0");
			}
			parameters.remove(ALPHA_NAME);
		}
		if(parameters.containsKey(K_NAME)) {
			k = Integer.parseInt(parameters.get(K_NAME));
			if(k < 3) {
				throw new IllegalArgumentException("k must be at least 3");
			}
			parameters.remove(K_NAME);
		}
		if(parameters.containsKey(DC_NAME)) {
			dc = Integer.parseInt(parameters.get(DC_NAME));
			if(dc < 1) {
				throw new IllegalArgumentException("dc must be at least 1");
			}
			parameters.remove(DC_NAME);
		}
		if (parameters.size() > 0) {
			throw new IllegalArgumentException("Too many input parameters!");
		}
	}
	
	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph)throws OcdAlgorithmException, InterruptedException {
		//gives every node its local density value
		HashMap<Node, Integer> localDensityMap = getLocalDensityMap(graph);
		//calculates the cliques of size at least k
		//Step 1
		HashMap<Integer,HashSet<Node>> cliques = getCliques(graph);
		
		//Variables 
		Set<Set<Node>> communitys = new HashSet<Set<Node>>();
		Set<Node> cluster = new HashSet<Node>();
		Set<Node> neighbors = new HashSet<Node>();
		Node maxLocalDensityNode = null;
		double maxNodeFitness = Double.NEGATIVE_INFINITY;
		System.out.println("Negative infinity : " + maxNodeFitness);
		double currentNodeFitness;
		Node fittestNode = null;
		boolean negativeNodeExist = true;
		int terminierungLD = graph.nodeCount() + 5;
		int terminierungNeighbors = graph.nodeCount() + 5;
		
		while(!localDensityMap.isEmpty()) {
			
			//Save termination
			terminierungLD--;		// termination variable (not important)
			if (terminierungLD <0) {
				System.out.println("LOC Algorithm only terminated because of termination variable");
				break;
			}
			//Start iteration with fresh cluster
			cluster.clear();

			maxLocalDensityNode = getMaxValueNodeInt(localDensityMap);

			cluster.add(maxLocalDensityNode);
			System.out.println("MaxLocalDensityNode : " + maxLocalDensityNode.toString());
			terminierungNeighbors = graph.nodeCount() + 1;
			while(terminierungNeighbors > 0) {		// while(true) funktioniert auch
				terminierungNeighbors--;			// termination variable (not important)
				neighbors = getClusterNeighbors(cluster, localDensityMap, graph);
				System.out.println("Nachbarn : "+ neighbors.toString());
				if(neighbors.isEmpty()) {
					//remove nodes in clique from localDensityMap
					for (Node clusterNode : cluster) {
						if(localDensityMap.containsKey(clusterNode)) {
							localDensityMap.remove(clusterNode);
						}
					}
					System.out.println("Community gefunden und neighbors sind leer : " + cluster.toString());
					//add the node cluster to found communities
					addClusterToCommunitys(communitys, cluster);
					System.out.println("Communitys aktuell : " + communitys.toString());
					break;
						
				}
				else {
					//Nachbarknoten mit höchstem Fitnesswert finden
					//Step 4
					System.out.println("Step 4");
					maxNodeFitness = Double.NEGATIVE_INFINITY;
					currentNodeFitness = maxNodeFitness;
					for(Node neighbor : neighbors) { 
						currentNodeFitness = getNodeFitness(neighbor, cluster, graph);
						if(currentNodeFitness > maxNodeFitness) {
							fittestNode = neighbor;
							maxNodeFitness = currentNodeFitness;
						}
					}
					System.out.println("FittestNode : " + fittestNode.toString() + " mit NodeFitness : " + maxNodeFitness + "bezüglich cluster" + cluster.toString());
					if(maxNodeFitness >= 0) {	//der knoten und die knoten seiner cliquen werden zum cluster hinzugefügt
						//Step 5
						System.out.println("Step 5");
						cluster.add(fittestNode);
						addCliqueNodesToCluster(fittestNode, cluster, cliques);
						
						//remove nodes with negative fitnessvalue
						
						System.out.println("Cluster before node removal :" + cluster.toString());
						negativeNodeExist = true;
						while(negativeNodeExist) {
							//Thread handler ?
							if(Thread.interrupted()) {
								throw new InterruptedException();
							}
							negativeNodeExist = false;
							for(Node node : cluster) {
								System.out.println(node.toString());
								System.out.println(cluster.toString());
								System.out.println("Step 6");
								if(getNodeFitness(node, cluster, graph) < 0) {
									//Step 7
									System.out.println("Step 7");
									cluster.remove(node);
									negativeNodeExist = true;
									break;
								}
							}
							
						}
						System.out.println("Kein negativer knoten gefunden");
						
					}
					else {
						//remove nodes in clique from localDensityMap
						for (Node clusterNode : cluster) {
							if(localDensityMap.containsKey(clusterNode)) {
								localDensityMap.remove(clusterNode);
							}
						}
						addClusterToCommunitys(communitys, cluster);
						System.out.println("Community gefunden weil alle nachbarn negativ : " + cluster.toString());
						System.out.println("Communitys :" + communitys.toString());
						break;
					}
				}
			}
			
		}		
			
		System.out.println("Communitys ganz am Ende: " + communitys.toString());
		Matrix membershipMatrix = getMemberships(communitys, graph);
		return new Cover(graph, membershipMatrix);
	}
	
	
	
	
	public HashMap<Node, Integer> getLocalDensityMap(CustomGraph graph)throws InterruptedException {
		//TODO Funktion für dc = 1 ect. optimieren
		Matrix[] m = new Matrix[dc];
		
		m[0] = graph.getNeighbourhoodMatrix();
		
		//get identity Matrix
		Matrix result = identity(m[0].rows());
		
		// Alle Matrizen für 0 - dc bestimmen
		for (int i = 1; i<=dc-1; i++) {
			m[i] = m[i-1].multiply(m[0]);
		}
		
		for (Matrix matrix : m) {
			result = result.add(matrix);
		}
				
		HashMap<Node, Integer> ldm = new HashMap<Node, Integer>();
		
		NodeCursor nodes = graph.nodes();
		Node node;
		int sum = 0;
		int nodenumber = 0;
		while (nodes.ok()) {
			node = nodes.node();
			for(int i = 0; i < result.columns(); i++){
				if (result.get(nodenumber, i) > 0){
					sum += 1;
				}	
			}
			ldm.put(node,  sum);
			nodenumber += 1;
			nodes.next();
			sum = 0;
		}
		
		return ldm;
	}
	
	//returns the set of all Nodes that are neighbours of the given cluster
	public Set<Node> getClusterNeighbors(Set<Node> cluster, HashMap<Node, Integer> localDensityMap, CustomGraph graph){
		Set<Node> neighbours = new HashSet<Node>();
		for(Node clusterNode : cluster) {
			NodeCursor nodes = clusterNode.successors();
			while (nodes.ok()) {
				Node neighbourNode = nodes.node();
				if(!cluster.contains(neighbourNode) && localDensityMap.containsKey(neighbourNode)) {
					neighbours.add(neighbourNode);
				}
				nodes.next();
			}
		}
		return neighbours;
	}
	
	
	public HashMap<Integer,HashSet<Node>> getCliques(CustomGraph graph){
		
		MaximalCliqueSearch maxCliqueSearch = new MaximalCliqueSearch();
		HashMap<Integer,HashSet<Node>> cliques = maxCliqueSearch.cliques(graph);
		Iterator<Integer> iterator = cliques.keySet().iterator();
		int i;
		while(iterator.hasNext()) {
			i = (int)iterator.next();
			if(cliques.get(i).size() < k) {
				iterator.remove();	
			}
		}
		return cliques;
	}

	public void addCliqueNodesToCluster(Node fittestNode, Set<Node> cluster, HashMap<Integer,HashSet<Node>> cliques) {
		HashSet<Node> clique;
		for(int i : cliques.keySet()) {
			clique = cliques.get(i);
			if(clique.contains(fittestNode)) {
				for(Node node : clique) {
					cluster.add(node);
				}
			}
		}
	}
	
	//returns the Fitnessvalue of the given Node depending on the given cluster
	public double getNodeFitness(Node node, Set<Node> originalCluster, CustomGraph graph) {
		//TODO funktion auch ohne das kopieren möglich?

		Set<Node> cluster = new HashSet<Node>();
		for(Node n : originalCluster) {
			cluster.add(n);
		}
		
		cluster.remove(node);
		double fitnessWithoutNode = getFitness(cluster, graph);	// fitness of the cluster WITHOUT the node
		cluster.add(node);
		double fitnessWithNode = getFitness(cluster, graph); 	// fitness of the cluster WITH the node
	
		return fitnessWithNode - fitnessWithoutNode;
	}
	
	public double getFitness(Set<Node> cluster, CustomGraph graph) {
		double k_in = (double)getKIn(cluster, graph);
		double edgeCount = (double)getClusterEdgeCount(cluster, graph);
		return (k_in)/Math.pow(edgeCount, alpha);
	}
	
	public int getKIn(Set<Node> cluster, CustomGraph graph) {
		int sum = 0;
		Node node;
		for (Node clusterNode : cluster) {
			NodeCursor nodes = clusterNode.successors();	//KIn : 2 times number of all edges in the cluster
			while (nodes.ok()) {
				node = nodes.node();
				if(cluster.contains(node)) {sum++;}
				nodes.next();
			}
		}
		return sum;
	}
	
	/**
	 * Determines the membership matrix through a random walk process.
	 * @param graph The graph being analyzed.
	 * @param communitys A set of all communitys that were found
	 * @return The membership matrix.
	 */
	
	//	at throws InterruptedException if the thread was interrupted
	public Matrix getMemberships(Set<Set<Node>> communitys, CustomGraph graph) {
		Matrix membershipMatrix = new Basic2DMatrix(graph.nodeCount(), communitys.size());
		int i = 0;
		for(Set<Node> community : communitys) {
			for (Node node : community) {
				membershipMatrix.set(node.index(), i , 1);
			}
			i++;
		}
		return membershipMatrix;
	}
	
	
	public void addClusterToCommunitys(Set<Set<Node>> communitys, Set<Node> cluster) {
		Set<Node> community = new HashSet<Node>();
		for (Node node : cluster) {
			community.add(node);
		}
		communitys.add(community);
	}
	
	
	public int getClusterEdgeCount(Set<Node> cluster, CustomGraph graph) {
		int sum = 0;
		for (Node node : cluster) {		//(K_in +K_out) from paper is computed together
			sum += node.outDegree();
		}
		return sum;
	}
	
	
	
	//Bibliothek sollte das tun
	private Basic2DMatrix identity(int size) {
		double[][] array = new double[size][size];
        for (int i = 0; i < size; i++) {
            array[i][i] = 1;
        }
        return new Basic2DMatrix(array);
    }
	
	//returns the Node with the highest integer value on the HashMap
	public Node getMaxValueNodeInt(HashMap<Node, Integer> map) throws IllegalArgumentException{
		if (map.isEmpty()){
			throw new IllegalArgumentException("HashMap (double) ist Leer");
		}
		Set<Node> set = map.keySet();
		Node ret = set.iterator().next();
		int maxValueInMap = map.get(ret);  //return max double value in the Hashmap
        for (Node key : set) {  
            if (map.get(key) > maxValueInMap) {
            	maxValueInMap = map.get(key);
            	ret = key;   
            }
        }
        return ret;
	}
	
}
