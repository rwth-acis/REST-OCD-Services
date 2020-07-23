package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import i5.las2peer.services.ocd.algorithms.utils.LEMONArrayListIndexComparator;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.dense.BasicVector;
import org.la4j.decomposition.SingularValueDecompositor;
import org.la4j.decomposition.EigenDecompositor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;

import scpsolver.problems.LinearProgram;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.constraints.LinearConstraint;
import scpsolver.constraints.*;

/**
 * Implements The LEMON Local Spectral Clustering Algorithm by Yi et al. Heavily
 * oriented on https://github.com/YixuanLi/LEMON Handles Unweighted(for now),
 * Directed/Undirected graphs
 */
public class LocalSpectralClusteringAlgorithm implements OcdAlgorithm {

	/*
	 * The index list of the graph node seed set
	 */
	ArrayList<Integer> seedSet; 
	
	/*
	 * The minimum possible community size
	 */
	int minimumCommunitySize = 50;
	
	/*
	 * The maximum possible community size
	 */
	int maximumCommunitySize = 100;
	
	/*
	 * The number of nodes to be added with each expansion step
	 * 
	 * Setting this higher can increase performance but too high values might
	 * decrease the community quality.
	 */
	private int expansionStepSize = 6;

	/*
	 * Specifies whether the initial probabilities for the starting nodes should be
	 * based on their degree (true) or all be equal (false)
	 */
	private boolean biased = true;

	// ADVANCED PARAMETERS

	/*
	 * Specifies the subspace dimension size.
	 * 
	 * Essentially alters the number of successive random walks and eigenvectors.
	 * Raising this potentially yields better communities, but can greatly decrease
	 * performance
	 */
	private int subspaceDimension = 3;

	/*
	 * The number of steps a random Walk will take.
	 * 
	 * Can yield better communities, but setting it too high might achieve the
	 * opposite and also impact performance
	 */
	private int randomWalkSteps = 3;

	/*
	 * PARAMETER NAMES
	 */

	protected static final String SEED_SET_NAME = "seedSet";
	
	protected static final String MINIMUM_COMMUNITY_SIZE_NAME = "minimumCommunitySize";
	
	protected static final String MAXIMUM_COMMUNITY_SIZE_NAME = "maximumCommunitySize";
	
	protected static final String EXPANSION_STEP_SIZE_NAME = "expansionStepSize";
	
	protected static final String BIASED_NAME = "biased";
	
	protected static final String SUBSPACE_DIMENSION_NAME = "subspaceDimension";
	
	protected static final String RANDOM_WALK_STEPS_NAME = "randomWalkSteps";
	
	/**
	 * Creates a standard instance of the algorithm. All attributes are assigned
	 * there default values.
	 */
	public LocalSpectralClusteringAlgorithm() {
	}

	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.LOCAL_SPECTRAL_CLUSTERING_ALGORITHM;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.ZERO_WEIGHTS);
		compatibilities.add(GraphType.DIRECTED);
		return compatibilities;
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(SEED_SET_NAME,
		seedSet.toString());
		parameters.put(MINIMUM_COMMUNITY_SIZE_NAME,
		Integer.toString(minimumCommunitySize));
		parameters.put(MAXIMUM_COMMUNITY_SIZE_NAME,
		Integer.toString(maximumCommunitySize));
		parameters.put(EXPANSION_STEP_SIZE_NAME,
		Integer.toString(expansionStepSize));
		parameters.put(BIASED_NAME,
		Boolean.toString(biased));
		parameters.put(SUBSPACE_DIMENSION_NAME,
		Integer.toString(subspaceDimension));
		parameters.put(RANDOM_WALK_STEPS_NAME,
		Integer.toString(randomWalkSteps));
		return parameters;
	}

	@Override // TODO: Check
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.containsKey(SEED_SET_NAME)) {
			String seedString = parameters.get(SEED_SET_NAME);
			if(seedString.matches("[,0-9]"))
			{
				throw new IllegalArgumentException();
			}
						
			String[] seedSetString = seedString.split(",");
			for(String str : seedSetString)
			{
				seedSet.add(Integer.parseInt(str));
			}
			if(seedSet.size() <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(MINIMUM_COMMUNITY_SIZE_NAME);
		}
		if(parameters.containsKey(MINIMUM_COMMUNITY_SIZE_NAME)) {
			minimumCommunitySize = Integer.parseInt(parameters.get(MINIMUM_COMMUNITY_SIZE_NAME));
			if(minimumCommunitySize <= seedSet.size() || minimumCommunitySize <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(MINIMUM_COMMUNITY_SIZE_NAME);
		}
		if(parameters.containsKey(MAXIMUM_COMMUNITY_SIZE_NAME)) {
			maximumCommunitySize = Integer.parseInt(parameters.get(MAXIMUM_COMMUNITY_SIZE_NAME));
			if(maximumCommunitySize <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(MAXIMUM_COMMUNITY_SIZE_NAME);
		}
		if(parameters.containsKey(EXPANSION_STEP_SIZE_NAME)) {
			expansionStepSize = Integer.parseInt(parameters.get(EXPANSION_STEP_SIZE_NAME));
			if(expansionStepSize <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(EXPANSION_STEP_SIZE_NAME);
		}
		if(parameters.containsKey(BIASED_NAME)) {
			biased = Boolean.parseBoolean(parameters.get(BIASED_NAME));
			//if(expansionStepSize <= 0) {
			//	throw new IllegalArgumentException();
			//}
			parameters.remove(BIASED_NAME);
		}
		if(parameters.containsKey(SUBSPACE_DIMENSION_NAME)) {
			subspaceDimension = Integer.parseInt(parameters.get(SUBSPACE_DIMENSION_NAME));
			if(subspaceDimension <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(SUBSPACE_DIMENSION_NAME);
		}
		if(parameters.containsKey(RANDOM_WALK_STEPS_NAME)) {
			randomWalkSteps = Integer.parseInt(parameters.get(RANDOM_WALK_STEPS_NAME));
			if(randomWalkSteps <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(RANDOM_WALK_STEPS_NAME);
		}
		if (parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}

	@Override // TODO: Implement
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
		Matrix graphAdjacencyMatrix = getAdjacencyMatrixWithIdentity(graph);
		
		Map<Integer, Double> members = seed_expand_auto(graphAdjacencyMatrix, ArrayList<Integer> seedset, int min_comm_size,
				int max_comm_size, expansionStepSize, subspaceDimension, randomWalkSteps, biased);
		
		Matrix coverMatrix = new Basic2DMatrix(graphAdjacencyMatrix.rows(),1);
		coverMatrix = coverMatrix.blank();
		for(Map.Entry<Integer, Double> entry : members.entrySet())
		{
			coverMatrix.set(entry.getKey(), 0, entry.getValue());
		}
		
		// TODO: Implement
		Cover c = new Cover(graph, coverMatrix);
		return c;
	}

	/**
	 * TODO: This method should probably be somewhere else, find out where TODO:
	 * Apparently nodes are identified by node names here? Check if indices are also
	 * fine. Converts a Graph into an Adjacency Matrix where every node has
	 * self-directed edges. The rows mark the nodes and the columns entries for that
	 * row are either 1 or 0 when there is an edge or not.
	 * 
	 * @param CustomGraph
	 * 
	 * @return Adjacency Matrix
	 * 
	 */
	public Matrix getAdjacencyMatrixWithIdentity(CustomGraph graph) {

		int size = graph.nodeCount();
		Matrix adjacencyMatrix = new CCSMatrix(size, size);
		adjacencyMatrix = adjacencyMatrix.blank();

		for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
			Edge edge = ec.edge();
			Node source = edge.source();
			Node target = edge.target();

			// int sourceId = Integer.valueOf(graph.getNodeName(source));
			// int targetId = Integer.valueOf(graph.getNodeName(target));

			if (source.index() != target.index()) {
				adjacencyMatrix.set(source.index(), target.index(), 1);
			}
		}

		// Self-directed edges
		for (int i = 0; i < size; i++) {
			adjacencyMatrix.set(i, i, 1);
		}

		return adjacencyMatrix;
	}

	// Use basic vector for now, enables faster fetch/store, but zeroes take up less
	// memory
	// n >= startingNodes.size()
	public Vector set_initial_prob(int n, ArrayList<Integer> startingNodes) {
		Vector v = new BasicVector(n);
		v = v.blank();

		for (int node : startingNodes) {
			v.set(node, 1. / startingNodes.size());
		}

		return v;
	}

	// n >= startingNodes.size()
	public Vector set_initial_prob_proportional(int n, Map<Integer, Integer> degreeMap,
			ArrayList<Integer> startingNodes) {
		Vector v = new BasicVector(n);
		v = v.blank();

		double degreeSum = 0;
		for (int node : startingNodes) {
			degreeSum += degreeMap.get(node);
		}

		for (int node : startingNodes) {
			v.set(node, degreeMap.get(node) / degreeSum);
		}

		return v;
	}

	// Computes the symmetric normalized laplacian matrix of a given graphs
	// adjacency matrix TODO: Check
	@Deprecated
	public Matrix adj_to_Laplacian(Matrix graphAdjacencyMatrix) {

		int n = graphAdjacencyMatrix.rows(); // Could also be columns, is nxn

		// Represents the degree matrix
		Vector sqrtDegrees = new BasicVector(n);
		sqrtDegrees = sqrtDegrees.blank();

		for (int i = 0; i < n; i++) {
			sqrtDegrees.set(i, Math.sqrt(graphAdjacencyMatrix.getRow(i).sum()));
		}

		Matrix symmetricNormalizedLaplacianMatrix = new Basic2DMatrix();
		for (int i = 0; i < n; i++) {
			for (int j = 0; i < n; i++) {
				if (i == j) {
					if (sqrtDegrees.get(i) != 0) {
						symmetricNormalizedLaplacianMatrix.set(i, j, 1);
					} else {
						symmetricNormalizedLaplacianMatrix.set(i, j, 0);
					}
				}
				symmetricNormalizedLaplacianMatrix.set(i, j,
						-graphAdjacencyMatrix.get(i, j) / (sqrtDegrees.get(i) * sqrtDegrees.get(j)));
			}
		}

		return symmetricNormalizedLaplacianMatrix;
	}

	// Computes the normalized adjacency matrix as defined by Li's Paper:
	// (D^(-1/2))*(A+I)*(D^(-1/2)) of a given graph
	// With I being the identity, D being the graphs degree and A the graphs
	// adjacency matrix
	// However in an actual normalized laplacian matrix the diagonal is one when the
	// degree of a node is >0 else 0,
	// so TODO: which of these give the desired result?
	public Matrix adjToNormAdj(Matrix graphAdjacencyMatrix) {

		int n = graphAdjacencyMatrix.rows(); // Could also be columns, is nxn

		// Represents the degree matrix D^(-1/2)
		Matrix sqrtDegrees = new CCSMatrix(n, n);
		sqrtDegrees = sqrtDegrees.blank();
		for (int i = 0; i < n; i++) {
			sqrtDegrees.set(i, i, 1.0 / Math.sqrt(graphAdjacencyMatrix.getRow(i).sum()));
		}

		// Represents the matrix (A+I)
		Matrix normalizedAdjacencyMatrix = graphAdjacencyMatrix;

		// Represents the matrix (D^(-1/2))*(A+I)*(D^(-1/2))
		normalizedAdjacencyMatrix = sqrtDegrees.multiply(normalizedAdjacencyMatrix.multiply(sqrtDegrees));

		return normalizedAdjacencyMatrix;
	}

	// cluster: a list of node ids that form a community.
	// Calculate the conductance of the cut A and complement of A. TODO: Check
	public double cal_conductance(Matrix graphAdjacencyMatrix, List<Integer> cluster) {

		Matrix clusterAdjacencyMatrix = new Basic2DMatrix(cluster.size(), graphAdjacencyMatrix.columns());
		for (int index : cluster) {
			clusterAdjacencyMatrix.setRow(index, graphAdjacencyMatrix.getRow(index));
		}

		Matrix subgraphAdjacencyMatrix = new Basic2DMatrix(cluster.size(), cluster.size());
		for (int index : cluster) {
			subgraphAdjacencyMatrix.setColumn(index, graphAdjacencyMatrix.getColumn(index));
		}

		double cutsize = clusterAdjacencyMatrix.sum() - subgraphAdjacencyMatrix.sum();
		double denominator = Math.min(clusterAdjacencyMatrix.sum(),
				graphAdjacencyMatrix.sum() - clusterAdjacencyMatrix.sum());
		double conductance = cutsize / denominator;

		return conductance;
	}

	// Sample rate=neg value, biased default=true TODO: Check and refine
	public Matrix sample_graph(HashMap<Integer, ArrayList<Integer>> neighbourMap, int node_number,
			Map<Integer, Integer> degree_sequence, int starting_node, double sample_rate, boolean biased) {

		ArrayList<Integer> initial = new ArrayList<Integer>();
		initial.add(starting_node);

		Vector prob_distribution;

		if (biased) {
			prob_distribution = set_initial_prob_proportional(node_number, degree_sequence, initial);
		} else {
			prob_distribution = set_initial_prob(node_number, initial);
		}

		HashSet<Integer> subgraph = new HashSet<Integer>();
		subgraph.add(starting_node);
		HashSet<Integer> RW_graph = new HashSet<Integer>();
		RW_graph.add(starting_node);

		ArrayList<Integer> neighbours = new ArrayList<Integer>();

		for (int j = 0; j < 30; j++) {
			Vector original_distribution = new BasicVector(prob_distribution);

			for (Integer node : RW_graph) {
				neighbours.add(node);

				double divided_prob = original_distribution.get(node) / (double) (neighbours.size());
				prob_distribution.set(node, prob_distribution.get(node) - original_distribution.get(node));

				for (Integer v : neighbours) {
					prob_distribution.set(v, prob_distribution.get(v) + divided_prob);
				}

				RW_graph.addAll(neighbours);

				if (RW_graph.size() >= 7000) {
					break;
				}
			}
		}

		for (int i = 0; i < 30; i++) {
			for (Integer node : subgraph) {

				neighbours = neighbourMap.get(node);
				subgraph.addAll(neighbours);
				if (subgraph.size() >= 7000) {
					break;
				}
			}
		}

		int index = 0;
		HashMap<Integer, Integer> RW_dict = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> RW_dict_reverse = new HashMap<Integer, Integer>();
		ArrayList<Double> sub_prob_distribution = new ArrayList<Double>(subgraph.size());

		for (Integer node : subgraph) {
			RW_dict.put(node, index); // mapping from whole graph to subgraph
			RW_dict_reverse.put(index, node); // mapping from subgraph to whole graph

			sub_prob_distribution.set(index, (prob_distribution.get(node)));

			index++;
		}

		// new = [0 for k in range(node_number)]
		int new_graph_size = 3000;

		List<Integer> nodes_in_new_graph = new ArrayList<Integer>(3000);
		// TODO: Check if included one too many through new_graph_size
		// Returns the nodes indices of the new_graph_size -many biggest values of
		// sub_prob_distribution in array form
		LEMONArrayListIndexComparator comparator = new LEMONArrayListIndexComparator(sub_prob_distribution);
		nodes_in_new_graph = comparator.createIndexArrayList();
		Collections.sort(nodes_in_new_graph, comparator);
		nodes_in_new_graph = nodes_in_new_graph.subList(0, new_graph_size);
		// list(np.argsort(sub_prob_distribution)[::-1][:new_graph_size]);
		// argsort returns the index order in array form so that the array would be
		// sorted small->big
		// [::-1] reverses the WHOLE array
		// [:new_graph_size] then gets all items of that reversed array until
		// new_graph_size
		// So, we get the indices of the new_graph_size -many biggest values in array
		// form

		ArrayList<Integer> nodes_in_new_graph_ori_index = new ArrayList<Integer>();
		for (Integer v : nodes_in_new_graph) {
			nodes_in_new_graph_ori_index.add(RW_dict_reverse.get(v));
		}

		HashSet<Integer> nodes_in_new_graph_ori_index_set = new HashSet<Integer>();
		nodes_in_new_graph_ori_index_set.addAll(nodes_in_new_graph_ori_index);

		// print "The size of sampled graph is", len(node_in_new_graph)
		Matrix new_graph = new Basic2DMatrix(new_graph_size, new_graph_size);
		new_graph = new_graph.blank();
		for (int k = 0; k < new_graph_size; k++) {
			new_graph.set(k, k, 1);
		}

		HashMap<Integer, Integer> map_dict = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> map_dict_reverse = new HashMap<Integer, Integer>();
		int count = 0;
		for (int i = 0; i < node_number; i++) {
			if (nodes_in_new_graph_ori_index_set.contains(i)) {
				map_dict.put(count, i); // from new to original
				map_dict_reverse.put(i, count); // from original to new
				count++;
			}
		}
		for (int i = 0; i < new_graph_size; i++) {
			for (int j = 0; j < new_graph_size; j++) {
				int a = map_dict.get(i);
				int b = map_dict.get(j);
				if (neighbourMap.get(b).contains(a)) {
					new_graph.set(i, j, 1.0);
					new_graph.set(j, i, 1.0);
				}
			}

		}

		// print "new graph returned"
		sample_rate = new_graph_size / (double) (node_number);

		return new_graph;// , map_dict, map_dict_reverse, sample_rate, new_graph_size TODO: These
							// parameters are probably still needed somewhere
	}

	// Given a list of node indices in the new graph after sampling, return the list
	// of indices that the nodes correspond
	// to in the original graph before sampling.
	public ArrayList<Integer> map_from_new_to_ori(ArrayList<Integer> nodelist, HashMap<Integer, Integer> map_dict) {

		ArrayList<Integer> mapped_list = new ArrayList<Integer>();
		for (Integer node : nodelist) {
			mapped_list.add(map_dict.get(node));
		}

		return mapped_list;
	}

	// Given a list of node indices in the original graph before sampling, return
	// the list of indices that the nodes correspond
	// to in the new graph after sampling.
	public ArrayList<Integer> map_from_ori_to_new(ArrayList<Integer> nodelist,
			HashMap<Integer, Integer> map_dict_reverse) {

		ArrayList<Integer> mapped_list = new ArrayList<Integer>();
		for (Integer node : nodelist) {
			if (map_dict_reverse.containsKey(node)) {
				mapped_list.add(map_dict_reverse.get(node));
			} else {
				mapped_list.add(1000000000); // set the value to be infinity(Yeah sure, very infinite TODO: Fix that,
												// negative value should be less hacky) when the key cannot be found in
												// the dictionary
			}
		}
		return mapped_list;
	}

	// Pretty much based on the scipy orth() solution, no clue if that works atm
	// TODO: Check
	@Deprecated
	public Matrix createOrthonormalOld(Matrix matrix) {
		SingularValueDecompositor svdCompositor = new SingularValueDecompositor(matrix);
		// Looks like (U=unitary,D=diagonal,V=unitary)
		Matrix[] svdResult = svdCompositor.decompose();
		Matrix U = svdResult[0];
		Matrix V = svdResult[2];
		Matrix D = svdResult[1];

		// TODO: Check whether #rows=#columns for U,V
		int m = U.rows();
		int n = V.rows();

		double rcond = (1.0 - Double.MIN_VALUE) * Math.max(m, n);

		double tol = D.max() * rcond;

		double num = 0.0;
		for (int i = 0; i < D.rows(); i++) {
			if (D.get(i, i) > tol) {
				num += D.get(i, i);
			}
		}

		Matrix orthonormalMatrix = new Basic2DMatrix(U.rows(), (int) num);
		for (int j = 0; j < (int) num; j++) {
			orthonormalMatrix.setColumn(j, U.getColumn(j));
		}

		return orthonormalMatrix;
	}

	// Get eigenvectormatrix and normalize it to make it an orthonormal basis TODO:
	// Check if that works as intended
	public Matrix createOrthonormal(Matrix matrix) {
		EigenDecompositor eigenDecomp = new EigenDecompositor(matrix);

		Matrix eigenMatrix = eigenDecomp.decompose()[0];

		for (int j = 0; j < eigenMatrix.rows(); j++) {
			Vector a = eigenMatrix.getColumn(j);
			double norm1 = a.fold(Vectors.mkManhattanNormAccumulator());
			Vector normalized = a.divide(norm1);

			eigenMatrix.setColumn(j, normalized);
		}

		return eigenMatrix;
	}

	// Start a random walk with probability distribution p_initial.
	// Transition matrix needs to be calculated according to adjacent matrix G.
	public Matrix random_walk(Matrix graphAdjacencyMatrix, Vector initial_prob, int subspace_dim, int walk_steps) {

		// Transform the adjacency matrix to a "normalized" adjacency matrix
		Matrix normalizedAdjacencyMatrix = adjToNormAdj(graphAdjacencyMatrix);

		Matrix probMatrix = new Basic2DMatrix(graphAdjacencyMatrix.rows(), subspace_dim);
		probMatrix = probMatrix.blank();
		for (int i = 0; i < probMatrix.rows(); i++) {
			probMatrix.set(i, 0, initial_prob.get(i));
		}

		// TODO: Check if that following term actually resembles P*probMatrix[i-1]:
		// np.dot(probMatrix[:,i-1], P));
		for (int i = 1; i < subspace_dim; i++) {
			probMatrix.setColumn(i, normalizedAdjacencyMatrix.multiply(probMatrix.getColumn(i - 1)));
		}

		Matrix orthProbMatrix = createOrthonormal(probMatrix);

		for (int i = 0; i < walk_steps; i++) {
			Matrix temp = orthProbMatrix.transpose().multiply(normalizedAdjacencyMatrix);
			orthProbMatrix = createOrthonormal(temp.transpose());
		}

		return orthProbMatrix;
	}

	// TODO: Although more good looking like this, realise with math3
	public ArrayList<Double> min_one_norm(Matrix matrix, ArrayList<Integer> initial_seed, ArrayList<Integer> seed) {

		double weight_initial = 1 / (double) (initial_seed.size());
		double weight_later_added = weight_initial / 0.5;
		int difference = seed.size() - initial_seed.size();
		int rows = matrix.rows();
		int columns = matrix.columns();

		LPWizard lpw = new LPWizard();

		// min ||y||_1
		lpw.setMinProblem(true);
		for (int i = 0; i < rows; i++) {
			lpw.plus("y" + String.valueOf(i), 1.0);
		}

		// y == V_kl * x //TODO: Check if that works as intended
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; i++) {
				lpw.addConstraint("cV_" + String.valueOf(i), 0.0, "==").plus("y" + String.valueOf(i), 1.0)
						.plus("x" + String.valueOf(i), -matrix.get(i, j));
			}
		}

		// y >= 0
		for (int i = 0; i < rows; i++) {
			lpw.addConstraint("c0_" + String.valueOf(i), 0.0, "<=").plus("y" + String.valueOf(i), 1.0);
		}

		// y >= 1 , y element of initialSeed
		for (int i : initial_seed) {
			lpw.addConstraint("c1_" + String.valueOf(i), 1.0, "<=").plus("y" + String.valueOf(i), 1.0);
		}

		// y >= 1+ weight_later_added*difference , y element of seed
		for (int i : seed) {
			lpw.addConstraint("c1_" + String.valueOf(i), 1.0 + weight_later_added * difference, "<=")
					.plus("y" + String.valueOf(i), 1.0);
		}

		LPSolution solution = lpw.solve();

		ArrayList<Double> v = new ArrayList<Double>(rows);

		for (int i = 0; i < rows; i++) {
			v.set(i, solution.getDouble("y" + String.valueOf(i)));
		}

		return v;
	}

	public int global_minimum(ArrayList<Double> sequence, int start_index, double[] global_conductance, int iteration) {

		int detected_size = sequence.size();
		int seq_length = sequence.size();
		global_conductance[iteration] = sequence.get(seq_length - 2);
		for (int x = 0; x < 40; x++) {
			sequence.add(0.0);
		}
		for (int i = 0; i < (seq_length - 40); i++) {
			if (sequence.get(i) < sequence.get(i - 1) && sequence.get(i) < sequence.get(i + 1)) {
				int count_larger = 0;
				int count_smaller = 0;
				for (int j = 1; j < 32; j++) {
					if (sequence.get(i + 1 + j) > sequence.get(i + 1)) {
						count_larger++;
					}
				}
				for (int k = 1; k < 32; k++) {
					if (sequence.get(i - 1 - k) > sequence.get(i - 1)) {
						count_smaller++;
					}
				}
				if (count_larger >= 18 && count_smaller >= 18) {
					detected_size = i + start_index;
					global_conductance[iteration] = sequence.get(i);
					break;
				}
			}
		}

		return detected_size;
	}

	// Actual algorithm
	// Have the defaults be
	// expand_step=neg_val, subspace_dim=neg_val, walk_steps=neg_val, biased=True
	public Map<Integer, Double> seed_expand_auto(Matrix graphAdjacencyMatrix, ArrayList<Integer> seedset, int min_comm_size,
			int max_comm_size, int expand_step, int subspace_dim, int walk_steps, boolean biased) {

		int n = graphAdjacencyMatrix.rows();

		HashMap<Integer, Integer> degree = new HashMap<Integer, Integer>();
		for (int i = 0; i < n; i++) {
			degree.put(i, (int) (graphAdjacencyMatrix.getRow(i).sum()));
		}

		Vector initial_prob;
		// Random walk starting from seed nodes:
		if (biased) {
			initial_prob = set_initial_prob_proportional(n, degree, seedset);
		} else {
			initial_prob = set_initial_prob(n, seedset);
		}

		Matrix orthProbMatrix = random_walk(graphAdjacencyMatrix, initial_prob, subspace_dim, walk_steps);
		ArrayList<Integer> initial_seed = seedset;

		// Initialization
		ArrayList<Integer> detected = seedset;
		int rows = orthProbMatrix.rows();
		int columns = orthProbMatrix.columns();
		ArrayList<Integer> seed = seedset;
		int step = expand_step;

		// TODO: Scores not really needed in actual algorithm, maybe keep but elsewhere
		ArrayList<Double> F1_scores = new ArrayList<Double>();
		ArrayList<Double> Jaccard_scores = new ArrayList<Double>();

		Map<Integer, Double> detected_comm = new HashMap<Integer, Double>();

		double global_conductance[] = new double[30];
		global_conductance[29] = 1000000; // set the last element to be infinitely large
		global_conductance[28] = 1000000;
		boolean flag = true;

		ArrayList<Double> F1_score_return = new ArrayList<Double>();
		ArrayList<Double> Jaccard_score_return = new ArrayList<Double>();

		int iteration = 0;
		while (flag) {
			ArrayList<Double> temp = min_one_norm(orthProbMatrix, initial_seed, seed); // TODO: Sorted here in original,
																						// should not be necessary but
																						// check if that is a problem

			List<Integer> sorted_top = new ArrayList<Integer>(temp.size());
			// TODO: Check if included one too many through new_graph_size
			// Returns the nodes indices of the new_graph_size -many biggest values of
			// sub_prob_distribution in array form
			LEMONArrayListIndexComparator comparator = new LEMONArrayListIndexComparator(temp);
			sorted_top = comparator.createIndexArrayList();
			Collections.sort(sorted_top, comparator);
			sorted_top = sorted_top.subList(0, step);

			for (int i : sorted_top) {
				if (!detected.contains(i)) {
					detected.add(i);
				}
			}
			seed = detected;

			ArrayList<Double> conductance_record = new ArrayList<Double>(max_comm_size - min_comm_size + 1);
			conductance_record.set(max_comm_size - min_comm_size, 0.0);

			// community_size = [0] TODO:Find out what the hell that was for, only one
			// occurrence in original

			LEMONArrayListIndexComparator tmpcomparator = new LEMONArrayListIndexComparator(temp);
			ArrayList<Integer> tempIndDesc = comparator.createIndexArrayList();
			Collections.sort(tempIndDesc, tmpcomparator);

			for (int i = min_comm_size; i <= max_comm_size; i++) {
				List<Integer> candidate_comm = tempIndDesc.subList(0, i);
				conductance_record.set(i - min_comm_size, cal_conductance(graphAdjacencyMatrix, candidate_comm));
			}

			int detected_size = global_minimum(conductance_record, min_comm_size, global_conductance, iteration);

			step += expand_step;

			if (biased) {
				initial_prob = set_initial_prob_proportional(n, degree, seedset);
			} else {
				initial_prob = set_initial_prob(graphAdjacencyMatrix.rows(), seedset);
			}

			orthProbMatrix = random_walk(graphAdjacencyMatrix, initial_prob, subspace_dim, walk_steps);

			if (detected_size != 0) {
				for(int i=0; i<detected_size; i++)
				{
					detected_comm.put(tempIndDesc.get(i), temp.get(tempIndDesc.get(i)));
				}
			}
			// else:
			// F1_score = 0
			// Jind = 0
			// Probably only needed for print, but check if needed for more

			if (global_conductance[iteration - 1] <= global_conductance[iteration]
					&& global_conductance[iteration - 1] <= global_conductance[iteration - 2]) {
				flag = false;
			}

			iteration++;
		}

		return detected_comm;
	}

	// TODO: Maybe implement the score functions
}
