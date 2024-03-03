package i5.las2peer.services.ocd.algorithms;


import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import org.la4j.vector.Vector;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.WeakClique;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.metrics.OcdMetricException;

import java.util.*;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

/**
 * This class holds weak clique percolation algorithm, implementation of which
 * is heavily based on the paper written by Xingui Zhan, Congtao Wang, Yansen Su, Linqiang Pan and Hai-Feng Zhang:
 * A Fast Overlapping Community Detection Algorithm Based on Weak Cliques for Largte-Scale Networks
 * https://doi.org/10.1109/TCSS.2017.2749282
 * The variable naming and algorithm steps are consistent with the ones used in the paper,
 * unless explicitly mentioned in the code.
 */
public class WeakCliquePercolationMethodAlgorithm implements OcdAlgorithm {

	/**
	 * Threshold to use for merging the weak cliques, which should be at least 0
	 */
	private double threshold = 1.1;
	
	/*
	 * PARAMETER NAMES
	 */
	
	public static final String THRESHOLD_NAME = "threshold";
	
	
	/**
	 * Default constructor that returns algorithm instance with default parameter values
	 */
	public WeakCliquePercolationMethodAlgorithm() {
		
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) {
		
		if (parameters.containsKey(THRESHOLD_NAME)) {
			threshold = Double.parseDouble(parameters.get(THRESHOLD_NAME));
			if (threshold < 0) {
				throw new IllegalArgumentException("threshold must be at least 0!");
			}
			parameters.remove(THRESHOLD_NAME);
		}
		
		if (parameters.size() > 0) {
			throw new IllegalArgumentException("Too many input parameters!");
		}
		
	}

	@Override
	public Map<String, String> getParameters() {
		
		Map<String, String> parameters =  new HashMap<String, String>();
		parameters.put(THRESHOLD_NAME, Double.toString(threshold));
		return parameters;
		
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph)
			throws OcdAlgorithmException, InterruptedException, OcdMetricException {
		
			
		// create adjacency matrix from the input graph
		Matrix adjacency_matrix = createAdjacencyMatrix(graph);
		
		HashSet<WeakClique> identified_communities = wCPM(adjacency_matrix, threshold);
		//System.out.println("weak cliques after merging: " + identified_communities);
		
		// build a community matrix with a row for each node
		Matrix community_matrix = buildCoverMatrix(identified_communities, adjacency_matrix.rows());
		
		Cover resulting_cover = new Cover(graph, community_matrix);
		
		//System.out.println("****** Number of found communities: " + identified_communities.size() + " ******");
		return resulting_cover;
		
	}

	@Override
	public CoverCreationType getAlgorithmType() {
		
		return CoverCreationType.WEAK_CLIQUE_PERCOLATION_METHOD_ALGORITHM;
		
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		return compatibilities;
	}
	
	
	/**
	 * This method finds the priority of a node located at row i specified as input
	 * parameter. The calculation is based on the paper "A Fast Overlapping
	 * Community Detection Algorithm Based on Weak Cliques for Large-Scale
	 * Networks". Variable names chosen are also consistent with the paper
	 * 
	 * @param adjacency_matrix       Adjacency matrix of the network where the input node belong
	 * @param u                      Input node for which the priority value should be found
	 * @return                       Double value representing priority of the input node
	 */
	public double priorityOfNode(Matrix adjacency_matrix, int u) {
		
		double p_u = 0;
		
		int m_u = 0;// number of links in between neighbours of input node u
		int k = 0; // degree of node input node u
		
		ArrayList<Integer> neighbour_indices = new ArrayList<Integer>();
		
		Vector neighbours = adjacency_matrix.getRow(u); // this row corresponds to the input node neighbour info
		

		// find indices of neighbours of node u and degree of node u
		for (int z = 0; z < neighbours.length(); z ++) {
			
			if (neighbours.get(z) > 0) { // if entry is > 0 then there is an edge between input node and this node
				
				neighbour_indices.add(z); // add neighbour index to the list of neighbours
				k++;
				
			} 
		}
		

		// find number of links between neighbours of node i
		for (int i = 0; i < neighbour_indices.size(); i++) {
			
			for (int j = 0; j < neighbour_indices.size(); j++) {
				
				if ( neighbour_indices.get(i) != u && neighbour_indices.get(j) != u && adjacency_matrix.get(neighbour_indices.get(i), neighbour_indices.get(j)) > 0) { // only consider neighbors, not u itelf (no self edges)
					
					// if there is an edge between neighbours of node u, increase m_u
					m_u++;
					
				}
			}
		}
		
		m_u = m_u / 2; // due to adjacency matrix being symmetric
		p_u = (double)(m_u + k ) / (k + 1);
		
//		System.out.println("neighbours: " + neighbours);
//		System.out.println("neighbour_indices: " + neighbour_indices);
//		System.out.println("k: " + k); 
//		System.out.println("m_u: " + m_u);
//		System.out.println("p_u: " + p_u); 
		
		return p_u;
	}
	
	
	/**
	 * This method finds Salton index between nodes u and v. The larger index
	 * indicates that nodes share more common neighbours (u and v are indices of
	 * node rows in network). Implementation is based on the paper "A Fast
	 * Overlapping Community Detection Algorithm Based on Weak Cliques for
	 * Large-Scale Networks" Variable names chosen are also consistent with the
	 * paper
	 * 
	 * @param network       Matrix representation of the network between nodes of which the Salton index should be calculated
	 * @param u             Index of a node row, acting as a node identifier
	 * @param v             Index of a node row, acting as a node identifier
	 * @return              Double value representing Salton index between the nodes u and v 
	 */
	public double saltonIndex(Matrix network, int u, int v) {
		
		double si_uv = 0.0; // Salton index between nodes u and v
		
		int n_u = 0; // number of neighbors of node u
		int n_v = 0; // number of neighbors of node v
		int shared_neighbour_count = 0; // number of shared neighbours of u and v
		
		Vector u_neighbors = network.getRow(u);
		Vector v_neighbors = network.getRow(v);
		
		// find the number of neighbours for nodes u and v
		for (int i = 0; i < network.columns(); i++) {
			
			int both = 0; // if both nodes have same neighbor this will be 2
			if (u_neighbors.get(i) > 0) {
				n_u++;
				both++;
			}
			if (v_neighbors.get(i) > 0) {
				n_v++;
				both++;
			}
			if (both == 2) { // in this case the neighbour at index i is shared between u and v

				shared_neighbour_count++;
				
			}
		}
		

		// this if statement is protection against 0 division
		if (shared_neighbour_count == 0) {
			
			si_uv = 0;
			
		} else {
			
			si_uv = (double) shared_neighbour_count / Math.sqrt((n_u * n_v));
			
		}
		
//		System.out.println(" ****** nodes " + u + " " + v + " ********* ");
//		System.out.println("n_u: " + n_u);
//		System.out.println("n_v: " + n_v);
//		System.out.println("shared neighbor count: " + shared_neighbour_count);
//		System.out.println("salton index: " + si_uv);
//		System.out.println("----------------------------------");
		
		return si_uv;
		
	}
	
	
	/**
	 * This method finds weak cliques as described in the paper "A Fast Overlapping
	 * Community Detection Algorithm Based on Weak Cliques for Large-Scale
	 * Networks". It corresponds to 'Algorithm 2' in the paper. Variable names
	 * chosen are also consistent with the paper
	 * 
	 * @param network      Matrix representation of the network
	 * @return Set of      weak cliques
	 */
	public HashSet<WeakClique> identifyWeakClique(Matrix network) {
		
		/*
		 * This will hold set of weak cliques, where each weak cliques will be
		 * represented as a set of nodes belonging to the weak clique
		 */
		HashSet<WeakClique> WClique = new HashSet<WeakClique>();
		
		HashMap<Integer, Double> P = new HashMap<Integer, Double>(); // priorities for different nodes (keys are node
																		// indices)
		ArrayList<Integer> V = new ArrayList<Integer>(); // node indices to be consistent with the paper
		
		// compute priority of each node 
		for (int i = 0; i < network.rows(); i++) {
			
			P.put(i, priorityOfNode(network, i)); // add priority of each node to the list
			V.add(i); // add node index to the list of nodes
			
		}
		
		while (!V.isEmpty()) {
			
			int u = findMaxEntryKey(P).getKey(); // index of a node with highest priority
			
			Vector u_neighborhood_vector = network.getRow(u);
			
			HashMap<Integer, Double> SI = new HashMap<Integer, Double>(); // salton index for each neighbour v of node u
			
			// calculate salton index for each neighbour v of node u and store them in map SI, keys of which are indices of neighbors of u and values are corresponding salton index values 
			for (int v = 0; v< u_neighborhood_vector.length(); v++) {
				
				if (u_neighborhood_vector.get(v) > 0) { // if there is an edge between u and v
					
					SI.put(v, saltonIndex(network, u, v));
					
				}
			}
				
			WeakClique W_u = new WeakClique(); // this set will hold node indices 
			W_u.add(u);
			
			if (SI.isEmpty()) {
				
				P.remove(u);
				
			}
			
			while (!SI.isEmpty()) {
				
				// select node v which has maximal similarity with node u in set SI
				int v = findMaxEntryKey(SI).getKey(); // index of the node most similar to node u
				
				W_u.add(u); // add node u
				W_u.add(v); // add node v
				W_u.addAll(findSharedNeighbors(network, u, v)); // add nodes shared by u and v
				
				for (int key : W_u.getNodes()) {
					
					SI.remove(key);
			
				}
				
			}
						
			WClique.add(W_u);  // add weak clique W_u to the set of found weak cliques WClique
			V.removeAll(W_u.getNodes()); // remove the nodes already considered from the set of nodes
			for (int key : W_u.getNodes()) {
				
				// remove nodes that were already considered from P so that they are not selected again
				P.remove(key);
				
			}
			
		}
		
		
	//	System.out.println("identified weak cliques " + WClique);

		return WClique; 
		
	}
	
	
	/**
	 * This method merges weak cliques as described in the paper "A Fast Overlapping
	 * Community Detection Algorithm Based on Weak Cliques for Large-Scale
	 * Networks". It corresponds to 'Algorithm 3' in the paper. Variable names
	 * chosen are also consistent with the paper, with the exception of T being
	 * called threshold which is more intuitive. Also, all_wcliques and network
	 * variables were added to represent a set of all weak cliques and the
	 * underlying network respectively.
	 * 
	 * @param wclique         Weak clique which should be merged with its neighbours
	 * @param S               Set of weak cliques which holds merged weak cliques
	 * @param threshold       Threshold value to determined whether similarity between two weak cliques warrants the merge
	 * @param all_wcliques    Set of all weak cliques that were identified in the network (potential merge candidates are selected from here)
	 * @param network         Matrix representation of the network
	 */
	public void Merge(WeakClique wclique, HashSet<WeakClique> S, double threshold, HashSet<WeakClique> all_wcliques, Matrix network) {
		
		WeakClique Comm = new WeakClique();
		
		ArrayList<WeakClique> Container = new ArrayList<WeakClique>();
		
		Comm.addAll(wclique.getNodes());
		
		Container.add(wclique);	
		
		while (!Container.isEmpty()) {

			// select a weak clique from Container
			WeakClique temp = Container.get(0); // take first element in Container

			// for each neighbour of temp
			for (WeakClique neighbor_temp : findNeighboringWeakCliques(temp, all_wcliques, network)) {
				
				// if neighbor is not visited and if similarity is at least as high as the threshold			
				if (!neighbor_temp.isVisited() && weakCliqueSimilarity(network, temp, neighbor_temp) >= threshold) {
					
					Comm.addAll(neighbor_temp.getNodes()); 
					Container.add(neighbor_temp);
					neighbor_temp.setVisited(true);	
					
				}

			}
			
			Container.remove(temp);

		}	
		
		S.add(Comm);
		
	}
	
	
	/**
	 * This method is a helper method to identify neighbouring weak cliques of a
	 * given weak clique. This is necessary in order to find the candidates with
	 * which wclique can be merged. Weak cliques A and B are defined as neighbours
	 * if they share at least one node.
	 * 
	 * @param wclique         Weak clique neighbours of which should be found
	 * @param all_wcliques    Set of all weak cliques found in the network
	 * @param network         Matrix representation of the network
	 * @return                Set of neighbouring weak cliques, for the input weak clique (wclique)
	 */
	public HashSet<WeakClique> findNeighboringWeakCliques(WeakClique wclique, HashSet<WeakClique> all_wcliques, Matrix network) {
		
		HashSet<WeakClique> neighbors = new HashSet<WeakClique>();
		boolean proceed = true; // helper variable to get out of nested loops efficiently
		
		for (WeakClique potential_neighbor : all_wcliques) {
			
			/*
			 * for each weak clique, check if it is neighbor of wclique i.e. if there is at
			 * least one edge between it and wclique
			 */
			if (potential_neighbor != wclique) { // wclique is not neighbor of itself
				
				for (int i : potential_neighbor.getNodes()) {
					
					// if at least one node is shared, weak cliques are considered as neighbors
					if(wclique.contains(i)) {
						neighbors.add(potential_neighbor);
						break;
					}

				}
				
			}
			
		}
		
		return neighbors;
		
	}
	

	/**
	 * This method finds a set of weak cliques that represent communities, as
	 * described in the paper "A Fast Overlapping Community Detection Algorithm
	 * Based on Weak Cliques for Large-Scale Networks". It corresponds to 'Algorithm
	 * 1' in the paper. Variable names chosen are also consistent with the paper,
	 * except for the input variable threshold, which is used instead of T (as it's
	 * more intuitive)
	 * 
	 * @param network          Matrix representation of the network
	 * @param threshold        Threshold value to determined whether similarity between two weak cliques warrants the merge
	 * @return                 Set of weak cliques that represent communities
	 * @throws InterruptedException when the method execution is interrupted
	 */
	public HashSet<WeakClique> wCPM(Matrix network, double threshold) throws InterruptedException {
		
		HashSet<WeakClique> S = new HashSet<WeakClique>();
		
		HashSet<WeakClique> WClique = identifyWeakClique(network);
		
		// all the weak cliques initially unvisited
		for (WeakClique wclique : WClique) {
			
			wclique.setVisited(false);
			
		}
		
		for (WeakClique wclique : WClique) {
			
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			
			// if weak clique is not visited, merge it with its neighboring weak cliques
			if (!wclique.isVisited()) {
				
				Merge(wclique, S, threshold, WClique, network); 
				
			}
			
		}
		
		return S;
	}
	
	
	/**
	 * This method finds weak clique similarity as described in the paper "A Fast Overlapping
	 * Community Detection Algorithm Based on Weak Cliques for Large-Scale
	 * Networks".
	 * @param network     Matrix representing the graph/network
	 * @param C_1         Set of nodes (indices) belonging to a weak clique
	 * @param C_2         Set of nodes (indices) belonging to a weak clique
	 * @return            Value representing similarity score between the input weak cliques
	 */
	public double weakCliqueSimilarity(Matrix network, WeakClique C_1, WeakClique C_2) {
		
		double WS_c1_c2 = 0;
			
		int min_wclique_size = Math.min(C_1.size(), C_2.size()); // size of the smaller of the two input cliques
	
		int number_of_shared_nodes = 0; // number of nodes shared between C1 and C2
		int number_of_edges_between_wcliques = 0; // number of edges between C1 and C2		
		
		// count nodes that are contained in both C_1 and C_2. It is sufficient to just look at one of C_1 or C_2
		for (int i : C_1.getNodes()) {
			
			if (C_2.contains(i)) {

				number_of_shared_nodes++;
				
			}
			
		}
		
		for (int i : C_1.getNodes()) {
			
			for (int j : C_2.getNodes()) {
				
				// only look at edges that are between disjoint nodes
				if (!C_2.contains(i) && !C_1.contains(j)) {
					
					if (network.get(i, j) > 0) {
						
				
						number_of_edges_between_wcliques++;
						
					}
					
				}
				
			}
			
		}
			
		WS_c1_c2 = ((double)number_of_shared_nodes + number_of_edges_between_wcliques) / ((double) min_wclique_size);
		
//		System.out.println("# of shared nodes: " + number_of_shared_nodes);
//		System.out.println("# of inbetween edges: " + number_of_edges_between_wcliques);
//		System.out.println("weak clique similarity: " +  WS_c1_c2); 
//		System.out.println("-------------------------------------"); 
		
		return WS_c1_c2;
		
	}
	
	
	/**
	 * This method finds shared neighbors between two input nodes, based on the adjacency matrix given
	 * @param network    Matrix representing the graph/network
	 * @param u          A node in the network
	 * @param v          A node in the network
	 * @return           A set that holds indices of shared neighbors
	 */
	public HashSet<Integer> findSharedNeighbors(Matrix network, int u, int v){
		
		HashSet<Integer> shared_neighbor_indices = new HashSet<Integer>();
		
		Vector u_neighbors = network.getRow(u); // neighborhood vector of u
		Vector v_neighbors = network.getRow(v); // neighborhood vector of v
		
		for (int i = 0; i < network.columns(); i++) {
			
			if (u_neighbors.get(i) > 0 && v_neighbors.get(i) > 0) { // if node i is neighbor to both nodes u and v
				
				shared_neighbor_indices.add(i); // add node i to shared neighbors list
				
			}
				
		}
		
		return shared_neighbor_indices;
		
	}
	
	/**
	 * This is a helper method to find map entry with the maximum value. It is
	 * needed for identifyWeakClique Method.
	 * 
	 * @param map        HashMap where keys represent node indices and values the node priorities
	 * @return           Map entry with the highest value
	 */
	public Map.Entry<Integer, Double> findMaxEntryKey(HashMap<Integer, Double> map) {
		
		Map.Entry<Integer, Double> maxEntry = null;
		
		for (Map.Entry<Integer, Double> mapEntry : map.entrySet()) {
			
			if (maxEntry == null || mapEntry.getValue().compareTo(maxEntry.getValue()) > 0) {
				
				maxEntry = mapEntry;
				
			}
		}
		
		return maxEntry;
		
	}
	
	/**
	 * This method creates Adjacency matrix for a graph. If entry
	 * i,j is 0, then there is no edge between the nodes i,j, if it's positive, then
	 * there is an edge and the value represents the weight
	 * 
	 * @param graph     Graph based on which the adjacency matrix should be built
	 * @return          Adjacency matrix based on the input graph
	 */
	public Matrix createAdjacencyMatrix(CustomGraph graph) {

		Matrix A = new Basic2DMatrix(graph.getNodeCount(), graph.getNodeCount());

		A = A.blank(); // create an empty matrix of size n

		Iterator<Edge> edge_list = graph.edges().iterator(); // added

		while (edge_list.hasNext()) {

			Edge edge = edge_list.next();

			A.set(edge.getSourceNode().getIndex(), edge.getTargetNode().getIndex(), graph.getEdgeWeight(edge));
		}

		return A;
		
	}
	
	/**
	 * This method converts set of WeakCliques that hold the community information into a membership Matrix, which can be used to create a cover
	 * @param weak_clique_set       Set of weak cliques that hold node community membership information
	 * @param node_count            Number of nodes in the network (used for Matrix size)
	 * @return                      Matrix representing community memberships for each node
	 */
	public Matrix buildCoverMatrix(HashSet<WeakClique> weak_clique_set, int node_count) {
		
		if (weak_clique_set == null) {
			throw new RuntimeException("Sorry, weak clique set is empty! this should not happen.");
		}
		
		// build a matrix with a row for each node and a column for each community
		Matrix community_matrix = new Basic2DMatrix(node_count, weak_clique_set.size());	

		int community_index = 0; // helper variable to enumerate commnities
		
		for (WeakClique wclique : weak_clique_set) {
			
			if (wclique != null) {
			
			for (int node : wclique.getNodes()) {
				
				/*
				 * set all nodes belonging to the same weak clique to have value of 1 in the
				 * column corresponding to that weak clique
				 */
				community_matrix.set(node, community_index, 1);
								
			}
			
			/*
			 * once all elements of one weak clique have been added, move on to the next
			 * column corrsponding to next weak clique
			 */
			community_index++;
			}
			
		}
		
//		System.out.println("COMMUNITY MATRIX");
//		System.out.println(normalizeRows(community_matrix) );
			
		return community_matrix;
		
	}
	
	/**
	 * This method can be used to normalized Matrix rows
	 * @param M     Matrix rows of which should be normalized
	 * @return      Normalized Matrix
	 */
	public Matrix normalizeRows(Matrix M) {

		// Set each element to the element value divided by the row sum
		for (int i = 0; i < M.rows(); i++) {
			
			double row_sum = M.getRow(i).sum();
			
			for (int j = 0; j < M.columns(); j++) {
							
				M.set(i, j, (M.get(i, j)/row_sum)); 
			
			}		
		}		
		
		return M;		
	}


}
