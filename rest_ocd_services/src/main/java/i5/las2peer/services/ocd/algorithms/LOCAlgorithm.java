package i5.las2peer.services.ocd.algorithms;

import java.util.*;

import org.graphstream.graph.Node;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import i5.las2peer.services.ocd.algorithms.utils.MaximalCliqueSearch;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.la4j.vector.Vector;

import i5.las2peer.services.ocd.graphs.DescriptiveVisualization;

/**
 * Implementation of the Local Optimization Algorithm based on Cliques by Jian Ma and Jianping:
 * Local Optimization for Clique-Based Overlapping Community Detection in Complex Networks
 * https://doi.org/10.1109/ACCESS.2019.2962751
 */
public class LOCAlgorithm implements OcdAlgorithm {

	/**
	Parameter to control the size of a community. The larger the value of alpha,
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
				throw new IllegalArgumentException("alpha must be greater than 0");
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

	/* DV: variables */
	public boolean visualize = false;
	public DescriptiveVisualization dv = new DescriptiveVisualization();
	public HashMap<Integer, Double> nodeNumericalValues = new HashMap<>();
	public HashMap<Integer, String> nodeStringValues = new HashMap<>();
	public HashSet<HashMap<Integer, String>> nodeStringValuesList = new HashSet<>();
	public HashSet<HashMap<Integer, String>> nodeStringValuesList2 = new HashSet<>();
	public HashSet<HashMap<Integer, String>> nodeStringValuesList3 = new HashSet<>();
	public HashSet<HashMap<Integer, Double>> nodeNumericalValuesList = new HashSet<>();
	public HashSet<HashMap<Integer, Double>> nodeNumericalValuesList2 = new HashSet<>();

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
		visualize = DescriptiveVisualization.getVisualize();
		if(visualize) {
			/* DV: set description file and delimiter */
			dv.setDescriptions("LOC.txt", ";");
			dv.addComponent(graph);
			HashMap<Integer, String> labels = new HashMap<>();
			for (int i = 0; i < graph.getNodeCount(); i++) {
				ArrayList<Integer> neighbors_i = new ArrayList<>();
				for (Node neighbor : graph.getNeighbours(graph.getNode(i))){
					neighbors_i.add(dv.getRealNode(neighbor.getIndex()));
				}
				labels.put(dv.getRealNode(i), "neighbors: " + neighbors_i);
			}
			/* DV: set node labels to the neighboring nodes */
			dv.setNodeLabels(labels);
		}

		//gives every node its local density value
		HashMap<Node, Integer> localDensityMap = getLocalDensityMap(graph);
		if(visualize) {
			for (Node node : localDensityMap.keySet()){
				nodeNumericalValues.put(node.getIndex(), Double.valueOf(localDensityMap.get(node)));
			}
			/* DV: set node local density values */
			dv.setNodeNumericalValues(1, nodeNumericalValues);
			nodeNumericalValues.clear();
		}

		//calculates the cliques of size at least k
		HashMap<Integer, HashSet<Node>> cliques = getCliques(graph);

		//Variables
		Set<Set<Node>> communitys = new HashSet<Set<Node>>();
		Set<Node> cluster = new HashSet<Node>();
		Set<Node> neighbors = new HashSet<Node>();
		Node maxLocalDensityNode = null;
		double maxNodeFitness = Double.NEGATIVE_INFINITY;
		double currentNodeFitness;
		Node fittestNode = null;
		boolean negativeNodeExist = true;
		int terminierungLD = graph.getNodeCount() + 5;
		int terminierungNeighbors = graph.getNodeCount() + 5;

		while(!localDensityMap.isEmpty()) {
			//Save termination
			terminierungLD--;		// termination variable (not important)
			if (terminierungLD <0) {
				System.out.println("LOC Algorithm only terminated because of termination variable");
				break;
			}
			//Start iteration with fresh cluster
			cluster.clear();

			maxLocalDensityNode = getMaxValueNode(localDensityMap);
			if(visualize) {
				nodeStringValues.put(maxLocalDensityNode.getIndex(), "maximal local density");
			}

			cluster.add(maxLocalDensityNode);
			terminierungNeighbors = graph.getNodeCount() + 1;
			while(terminierungNeighbors > 0) {		// while(true) should also work
				terminierungNeighbors--;			// termination variable (not important)
				neighbors = getClusterNeighbors(cluster, localDensityMap, graph);
				if(visualize) {
					HashMap<Integer, String> nodeStringValues2 = new HashMap<>();
					int maxNode = dv.getRealNode(maxLocalDensityNode.getIndex());
					for(Node node : neighbors){
						nodeStringValues2.put(node.getIndex(), "neighbor of cluster " + maxNode);
					}
					nodeStringValues2.put(maxLocalDensityNode.getIndex(), "cluster " + maxNode);
					nodeStringValuesList.add(nodeStringValues2);
				}

				if(neighbors.isEmpty()) {
					//remove nodes in clique from localDensityMap
					for (Node clusterNode : cluster) {
						if(localDensityMap.containsKey(clusterNode)) {
							localDensityMap.remove(clusterNode);
						}
					}
					//add the node cluster to found communities
					addClusterToCommunitys(communitys, cluster);
					break;

				}
				else {
					//find neighbor node with highest fitness value
					maxNodeFitness = Double.NEGATIVE_INFINITY;
					currentNodeFitness = maxNodeFitness;
					HashMap<Integer, Double> nodeNumericalValues2 = new HashMap<>();
					HashMap<Integer, Double> nodeNumericalValues3 = new HashMap<>();
					HashMap<Integer, String> nodeStringValues3 = new HashMap<>();
					HashMap<Integer, String> nodeStringValues4 = new HashMap<>();
					for(Node neighbor : neighbors) {
						currentNodeFitness = getNodeFitness(neighbor, cluster, graph);
						if(visualize) {
							nodeNumericalValues2.put(neighbor.getIndex(), currentNodeFitness);
						}

						if(currentNodeFitness > maxNodeFitness) {
							fittestNode = neighbor;
							maxNodeFitness = currentNodeFitness;
						}
					}
					if(visualize) {
						nodeNumericalValuesList.add(nodeNumericalValues2);
						nodeStringValues3.put(fittestNode.getIndex(), "" + dv.getRealNode(maxLocalDensityNode.getIndex()));
						nodeStringValuesList2.add(nodeStringValues3);
					}

					if(maxNodeFitness >= 0) {	//chosen node and the cliques it belongs to are added to the cluster
						cluster.add(fittestNode);

						addCliqueNodesToCluster(fittestNode, cluster, cliques);

						negativeNodeExist = true;
						while(negativeNodeExist) {		//remove nodes with negative fitnessvalue
							//Thread handler ?
							if(Thread.interrupted()) {
								throw new InterruptedException();
							}
							negativeNodeExist = false;
							for(Node node : cluster) {
								double nodeFitness = getNodeFitness(node, cluster, graph);
								if(nodeFitness < 0) {
									//Step 7
									cluster.remove(node);
									if(visualize) {
										nodeStringValues4.put(node.getIndex(), "remove from cluster of node " + cluster.toArray()[0]);
										nodeNumericalValues3.put(node.getIndex(), nodeFitness);
									}
									negativeNodeExist = true;
									break;
								}
							}
							if(visualize){
								nodeNumericalValuesList2.add(nodeNumericalValues3);
								nodeStringValuesList3.add(nodeStringValues4);
							}
						}
					}
					else {
						//remove nodes in clique from localDensityMap
						for (Node clusterNode : cluster) {
							if(localDensityMap.containsKey(clusterNode)) {
								localDensityMap.remove(clusterNode);
							}
						}
						addClusterToCommunitys(communitys, cluster);
						break;
					}
				}
			}

		}
		Matrix membershipMatrix = getMemberships(communitys, graph);
		Cover cover = new Cover(graph, membershipMatrix);

		/* DV: set values that should be visualized */
		if(visualize){
			/* DV: set nodes with maximal local density */
			dv.setNodeStringValues(2, nodeStringValues);
			nodeStringValues.clear();

			HashMap<String, List<HashMap<Integer, String>>> groupedValues = new HashMap<>();
			for (HashMap<Integer, String> values : nodeStringValuesList) {
				String selectedValue = null;
				for (String value : values.values()) {
					if (!value.startsWith("cluster")) {
						selectedValue = value;
						break;
					}
				}
				if (selectedValue != null) {
					List<HashMap<Integer, String>> group = groupedValues.getOrDefault(selectedValue, new ArrayList<>());
					group.add(values);
					groupedValues.put(selectedValue, group);
				}
			}
			HashMap<Integer, ArrayList<Integer>> groupNodes = new HashMap<>();
			int index = 0;
			for (List<HashMap<Integer, String>> group : groupedValues.values()) {
				HashMap<Integer, String> maxNeighbors = new HashMap<>();
				ArrayList<Integer> nodes = new ArrayList<>();
				for (HashMap<Integer, String> values : group) {
					maxNeighbors.putAll(values);
					nodes.addAll(values.keySet());
				}
				groupNodes.put(index, nodes);
				index++;
				/* DV: set neighbors of nodes with maximal local density */
				dv.setNodeStringValues(3, maxNeighbors);
			}

			List<HashMap<Integer, Double>> allGroupValues = new ArrayList<>();
			for (ArrayList<Integer> nodes : groupNodes.values()) {
				HashMap<Integer, Double> groupValues = new HashMap<>();
				for (HashMap<Integer, Double> values : nodeNumericalValuesList) {
					for (int node : values.keySet()){
						if(nodes.contains(node)){
							groupValues.put(node, values.get(node));
						}
					}
				}
				allGroupValues.add(groupValues);
				/* DV: set node fitness value */
				dv.setNodeNumericalValues(4, groupValues);
			}

			HashMap<Integer, HashMap<Integer, String>> maxCliques = new HashMap<>();
			for (HashMap<Integer, Double> vals : allGroupValues) {
				double max = Double.MIN_VALUE;
				int node = 0;
				for (int i : vals.keySet()){
					if(max < vals.get(i)){
						max = vals.get(i);
						node = i;
					}
				}
				HashMap<Integer, String> res = new HashMap<>();
				for (HashMap<Integer, String> values : nodeStringValuesList2) {
					if(values.keySet().contains(node)){
						res.put(node, "cluster " + values.get(node));
						maxCliques.put(node, res);
						/* DV: mark nodes with maximal fitness value */
						dv.setNodeStringValues(5, res);
					}
				}
			}

			HashMap<Node, ArrayList<Integer>> nodeCliqueInvolvements = new HashMap<>();
			for (int i : cliques.keySet()){
				HashSet<Node> clique_i = cliques.get(i);
				for (Node node : clique_i) {
					if (!nodeCliqueInvolvements.keySet().contains(node)){
						ArrayList<Integer> temp = new ArrayList<>();
						temp.add(i);
						nodeCliqueInvolvements.put(node, temp);
					}
					else{
						if(!nodeCliqueInvolvements.get(node).contains(i)) {
							nodeCliqueInvolvements.get(node).add(i);
						}
					}
					nodeStringValues.put(node.getIndex(), "cliques: " + nodeCliqueInvolvements.get(node));
				}
				/* DV: set all cliques of size at least k, where a node belongs to */
				dv.setNodeStringValues(6, nodeStringValues);
				nodeStringValues.clear();
			}

			for (int key : maxCliques.keySet()){
				HashMap<Integer, String> values = new HashMap<>();
				ArrayList<Integer> involvements = nodeCliqueInvolvements.get(graph.getNode(key));
				for (int i = 0; i < graph.getNodeCount(); i++){
					Node node = graph.getNode(i);
					if(nodeCliqueInvolvements.get(node) != null && involvements != null) {
						for(int el : involvements) {
							if (nodeCliqueInvolvements.get(node).contains(el)) {
								int maxKey = 0;
								for (int m : maxCliques.get(key).keySet()){
									maxKey = m;
									break;
								}
								values.put(node.getIndex(), "clique added to " + maxCliques.get(key).get(maxKey));
								break;
							}
						}
					}
				}
				/* DV: add all cliques of the node with maximal fitness value to current cluster */
				dv.setNodeStringValues(7, values);
			}

			/* DV: remove node from cluster, if its fitness value is negative */
			for (HashMap<Integer, String> values : nodeStringValuesList3){
				dv.setNodeStringValues(8, values);
			}
			for (HashMap<Integer, Double> values : nodeNumericalValuesList2){
				dv.setNodeNumericalValues(8, values);
			}

			HashMap<Node, Integer> nodeToCommunity = new HashMap<>();
			HashMap<Node, Double> nodeMaxMembership = new HashMap<>();
			for (int i = 0; i < graph.getNodeCount(); i++){
				Node n = graph.getNode(i);
				String communities = "[";
				Vector vec = membershipMatrix.getRow(n.getIndex());
				double maxValue = 0.0;
				int maxIndex = 0;
				if(vec.length() == 1){
					communities += 0;
					maxValue = 1.0;
					maxIndex = 0;
				}
				else {
					for (int k = 0; k < vec.length(); k++) {
						if ((double) 1 / vec.length() < vec.get(k)) {
							if (communities.length() == 1) {
								communities += k;
							} else {
								communities += ", " + k;
							}
							if (maxValue < vec.get(k)) {
								maxValue = vec.get(k);
								maxIndex = k;
							}
						}
					}
				}
				communities += "]";
				nodeStringValues.put(n.getIndex(), "communities: " + communities);
				nodeToCommunity.put(n, maxIndex);
				nodeMaxMembership.put(n, maxValue);
			}
			/* DV: set node-memberships */
			dv.setNodeStringValues(9, nodeStringValues);
			nodeStringValues.clear();

			for (int c : nodeToCommunity.values()){
				for (Node n : nodeToCommunity.keySet()){
					if (c == nodeToCommunity.get(n)){
						nodeStringValues.put(n.getIndex(), "maximum membership: " + c + " (" + nodeMaxMembership.get(n) + ")");
					}
				}
				/* DV: set maximal node-membership value */
				dv.setNodeStringValues(10, nodeStringValues);
				nodeStringValues.clear();
			}

			/* DV: set final cover */
			dv.setCover(11, cover);
		}

		return cover;
	}

	/**
	 * Calculates the local density value for every node in the network
	 * @param graph				The graph being analyzed
	 * @return              	A map of every node and its local density value
	 * @throws InterruptedException when the method execution is interrupted
	 */
	public HashMap<Node, Integer> getLocalDensityMap(CustomGraph graph) throws InterruptedException {
		//TODO optimize funktion for dc=1
		Matrix[] m = new Matrix[dc];

		m[0] = graph.getNeighbourhoodMatrix();

		//get identity Matrix
		Matrix result = identity(m[0].rows());

		//claculate matix from 0 to dc
		for (int i = 1; i<=dc-1; i++) {
			m[i] = m[i-1].multiply(m[0]);
		}

		for (Matrix matrix : m) {
			result = result.add(matrix);
		}

		HashMap<Node, Integer> ldm = new HashMap<Node, Integer>();

		int sum = 0;
		int nodenumber = 0;
        for (Node node : graph.nodes().toArray(Node[]::new)) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			for(int i = 0; i < result.columns(); i++){
				if (result.get(nodenumber, i) > 0){
					sum += 1;
				}
			}
			ldm.put(node,  sum);
			nodenumber += 1;
			sum = 0;
		}
		return ldm;
	}

	/**
	 * Calculates the neighbors of a cluster.
	 * A node n is added to the output iff the following conditions hold:
	 * There is an edge from a node in the cluster set to n AND
	 * n does not belong to the cluster AND
	 * n is a node that is listed in the localDensityMap (is not assigned to a community yet)
	 * @param cluster			The Set of nodes from which the function determines the neighbors
	 * @param localDensityMap	A Map of nodes and local density values, where the nodes have not yet been added to a final community
	 * @param graph				The graph being analyzed
	 * @throws InterruptedException when the method execution is interrupted
	 * @return                 	A Set of nodes
	 */
	public Set<Node> getClusterNeighbors(Set<Node> cluster, HashMap<Node, Integer> localDensityMap, CustomGraph graph) throws InterruptedException{
		Set<Node> neighbours = new HashSet<Node>();
		for(Node clusterNode : cluster) {
			Iterator<Node> successorsIt = graph.getSuccessorNeighbours(clusterNode).iterator();
			while (successorsIt.hasNext()) {
				Node neighbourNode = successorsIt.next();
				if(!cluster.contains(neighbourNode) && localDensityMap.containsKey(neighbourNode)) {
					neighbours.add(neighbourNode);
				}
			}
		}
		return neighbours;
	}

	/**
	 * Calculates the Set of all cliques, that have at least the size of this algorithms parameter k.
	 * @param graph        	  	The graph being analyzed
	 * throws InterruptedException when the method execution is interrupted
	 * @return                 	A Map with an ID and a Set of nodes
	 */
	public HashMap<Integer,HashSet<Node>> getCliques(CustomGraph graph) throws InterruptedException {
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

	/**
	 * The function finds all cliques in which the node "fittestNode" is a member.
	 * Then it adds all the nodes which are a member of these cliques to the cluster.
	 * @param fittestNode		The node with the highest fitness value in respect to the cluster
	 * @param cluster			A set of nodes on which nodes will be added
	 * @param cliques			A map of IDs and a Set of nodes
	 */
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

	/**
	 * Calculates the fitness value of a node in respect to the cluster.
	 * @param node				The node we want to get the fitness value from
	 * @param originalCluster	A set of nodes
	 * @param graph				The graph being analyzed
	 * @return              	A map of every node and its local density value
	 * @throws InterruptedException when the method execution is interrupted
	 */
	public double getNodeFitness(Node node, Set<Node> originalCluster, CustomGraph graph) throws InterruptedException {
		//TODO possible without to copy ?

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

	/**
	 * Calculates the fitness value of a cluster.
	 * @param cluster			A set of nodes
	 * @param graph				The graph being analyzed
	 * @return              	The fitness value
	 */
	public double getFitness(Set<Node> cluster, CustomGraph graph)  throws InterruptedException {
		double k_in = (double)getKIn(cluster, graph);
		double edgeCount = (double)getClusterEdgeCount(cluster, graph);
		return (k_in)/Math.pow(edgeCount, alpha);
	}

	public int getKIn(Set<Node> cluster, CustomGraph graph) throws InterruptedException {
		int sum = 0;
		Node node;
		for (Node clusterNode : cluster) {
			//K_in : 2 times number of all edges in the cluster
			Iterator<Node> successorsIt = graph.getSuccessorNeighbours(clusterNode).iterator();
			while (successorsIt.hasNext()) {
				node = successorsIt.next();
				if(cluster.contains(node)) {sum++;}
			}
		}
		return sum;
	}

	public int getClusterEdgeCount(Set<Node> cluster, CustomGraph graph) {
		int sum = 0;
		for (Node node : cluster) {		//(K_in +K_out) from paper is computed together
			sum += node.getOutDegree();
		}
		return sum;
	}

	/**
	 * Determines the membership matrix with a set of communities.
	 * @param communitys 		A set of all communities that were found
	 * @param graph 			The graph being analyzed
	 * @return 					The membership matrix
	 */
	public Matrix getMemberships(Set<Set<Node>> communitys, CustomGraph graph) {
		Matrix membershipMatrix = new Basic2DMatrix(graph.getNodeCount(), communitys.size());
		int i = 0;
		for(Set<Node> community : communitys) {
			for (Node node : community) {
				membershipMatrix.set(node.getIndex(), i , 1);
			}
			i++;
		}
		return membershipMatrix;
	}

	/**
	 * Adds the cluster as one community to the set of communities.
	 * @param communitys 		A set of communities that has to be filled
	 * @param cluster 			A set of nodes
	 */
	public void addClusterToCommunitys(Set<Set<Node>> communitys, Set<Node> cluster) {
		Set<Node> community = new HashSet<Node>();
		for (Node node : cluster) {
			community.add(node);
		}
		communitys.add(community);
	}

	/**
	 * Finds the node from the map to which the highest value was assigned.
	 * @param map	 			A map of nodes and an associated value
	 * @return 					The node with the highest value
	 * @throws IllegalArgumentException when the given map is empty
	 */
	public Node getMaxValueNode(HashMap<Node, Integer> map) throws IllegalArgumentException{
		if (map.isEmpty()){
			throw new IllegalArgumentException("HashMap is empty");
		}
		Set<Node> set = map.keySet();
		Node ret = set.iterator().next();
		int maxValueInMap = map.get(ret);  //return max value in the Hashmap
        for (Node key : set) {
            if (map.get(key) > maxValueInMap) {
            	maxValueInMap = map.get(key);
            	ret = key;
            }
        }
        return ret;
	}

	//Bibliothek sollte das tun
	private Basic2DMatrix identity(int size) {
		double[][] array = new double[size][size];
        for (int i = 0; i < size; i++) {
            array[i][i] = 1;
        }
        return new Basic2DMatrix(array);
    }

}