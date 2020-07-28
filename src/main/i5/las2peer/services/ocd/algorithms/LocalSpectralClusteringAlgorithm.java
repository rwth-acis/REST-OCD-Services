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
import java.util.Arrays;
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

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

//import scpsolver.problems.LinearProgram;
//import scpsolver.problems.LPSolution;
//import scpsolver.problems.LPWizard;
//import scpsolver.problems.LPWizardConstraint;
//import scpsolver.lpsolver.LinearProgramSolver;
//import scpsolver.constraints.LinearConstraint;
//import scpsolver.constraints.*;

/**
 * Implements The LEMON Local Spectral Clustering Algorithm by Yi et al. Heavily
 * oriented on https://github.com/YixuanLi/LEMON Handles Unweighted(for now),
 * Directed/Undirected graphs
 */
public class LocalSpectralClusteringAlgorithm implements OcdAlgorithm {

	/*
	 * The index list of the graph node seed set
	 */
	private ArrayList<Integer> seedSet;

	/*
	 * The minimum possible community size
	 */
	private int minimumCommunitySize = 1;

	/*
	 * The maximum possible community size
	 */
	private int maximumCommunitySize = 100;

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
		parameters.put(SEED_SET_NAME, seedSet.toString());
		parameters.put(MINIMUM_COMMUNITY_SIZE_NAME, Integer.toString(minimumCommunitySize));
		parameters.put(MAXIMUM_COMMUNITY_SIZE_NAME, Integer.toString(maximumCommunitySize));
		parameters.put(EXPANSION_STEP_SIZE_NAME, Integer.toString(expansionStepSize));
		parameters.put(BIASED_NAME, Boolean.toString(biased));
		parameters.put(SUBSPACE_DIMENSION_NAME, Integer.toString(subspaceDimension));
		parameters.put(RANDOM_WALK_STEPS_NAME, Integer.toString(randomWalkSteps));
		return parameters;
	}

	@Override // TODO: Check
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if (parameters.containsKey(SEED_SET_NAME)) {
			String seedString = parameters.get(SEED_SET_NAME);
			if (seedString.matches("[^,0-9]*")) {
				throw new IllegalArgumentException();
			}

			seedSet = new ArrayList<Integer>();

			System.out.println(seedString);
			String[] seedSetString = seedString.split(",");
			for (String str : seedSetString) {

				seedSet.add(Integer.parseInt(str));
			}
			if (seedSet.size() == 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(SEED_SET_NAME);
		} else {
			throw new IllegalArgumentException("Please specify a seed set");
		}
		if (parameters.containsKey(MINIMUM_COMMUNITY_SIZE_NAME)) {
			minimumCommunitySize = Integer.parseInt(parameters.get(MINIMUM_COMMUNITY_SIZE_NAME));
			if (minimumCommunitySize <= seedSet.size() || minimumCommunitySize <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(MINIMUM_COMMUNITY_SIZE_NAME);
		}
		if (parameters.containsKey(MAXIMUM_COMMUNITY_SIZE_NAME)) {
			maximumCommunitySize = Integer.parseInt(parameters.get(MAXIMUM_COMMUNITY_SIZE_NAME));
			if (maximumCommunitySize <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(MAXIMUM_COMMUNITY_SIZE_NAME);
		}
		if (parameters.containsKey(EXPANSION_STEP_SIZE_NAME)) {
			expansionStepSize = Integer.parseInt(parameters.get(EXPANSION_STEP_SIZE_NAME));
			if (expansionStepSize <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(EXPANSION_STEP_SIZE_NAME);
		}
		if (parameters.containsKey(BIASED_NAME)) {
			biased = Boolean.parseBoolean(parameters.get(BIASED_NAME));
			// if(expansionStepSize <= 0) {
			// throw new IllegalArgumentException();
			// }
			parameters.remove(BIASED_NAME);
		}
		if (parameters.containsKey(SUBSPACE_DIMENSION_NAME)) {
			subspaceDimension = Integer.parseInt(parameters.get(SUBSPACE_DIMENSION_NAME));
			if (subspaceDimension <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(SUBSPACE_DIMENSION_NAME);
		}
		if (parameters.containsKey(RANDOM_WALK_STEPS_NAME)) {
			randomWalkSteps = Integer.parseInt(parameters.get(RANDOM_WALK_STEPS_NAME));
			if (randomWalkSteps <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(RANDOM_WALK_STEPS_NAME);
		}
		if (parameters.size() > 0) {
			System.out.println(parameters.toString());
			throw new IllegalArgumentException();
		}
	}

	@Override // TODO: Implement
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
		Matrix graphAdjacencyMatrix = getAdjacencyMatrixWithIdentity(graph);

		//getLinkList(graph);

		Map<Integer, Double> members = seed_expand_auto(graphAdjacencyMatrix, seedSet, minimumCommunitySize,
				maximumCommunitySize, expansionStepSize, subspaceDimension, randomWalkSteps, biased);

		Matrix coverMatrix = new Basic2DMatrix(graphAdjacencyMatrix.rows(), 1);
		coverMatrix = coverMatrix.blank();
		for (Map.Entry<Integer, Double> entry : members.entrySet()) {
			coverMatrix.set(entry.getKey(), 0, entry.getValue());
		}

		//System.out.println("DETECTED FINAL:" + members.keySet().toString());
		//System.out.println("DETECTED FINAL:" + members.values().toString());

		// TODO: Implement in a way that probabilities don't get lost here
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
	 * @param graph A graph
	 * 
	 * @return Adjacency Matrix with self-directed edges
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
	/**
	 * Sets the initial probability for every node to be in the community
	 * Here, it is unbiased, so every node will have 1/#startingNodes
	 * 
	 * @param n Number of nodes n in graph
	 * @param startingNodes ArrayList of starting node indices
	 * 
	 * @return A vector containing all initial probabilities.
	 */
	public Vector set_initial_prob(int n, ArrayList<Integer> startingNodes) {
		Vector v = new BasicVector(n);
		v = v.blank();

		for (int node : startingNodes) {
			v.set(node, 1. / startingNodes.size());
		}

		return v;
	}

	// n >= startingNodes.size()
	/**
	 * Sets the initial probability for every node to be in the community
	 * Here, it is biased by node degree, so every node will have nodeDegree/#startingNodes
	 * 
	 * @param n Number of nodes n in graph
	 * @param degreeMap Map of node degrees(index to degree) in graph
	 * @param startingNodes ArrayList of starting node indices
	 * 
	 * @return A vector containing all initial probabilities.
	 */
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

	/**
	 * Computes the normalized adjacency matrix as defined by Li's Paper:
	 * (D^(-1/2))*(A+I)*(D^(-1/2)) of a given graph
	 * with I being the identity, D being the graphs degree and A the graphs adjacency matrix
	 * 
	 * @param graphAdjacencyMatrix A graphs adjacency matrix (with self directed edges)
	 * 
	 * @return A "normalized" adjacency matrix according to the described formula
	 */
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

	//TODO: Check
	/**
	 * Computes the conductace of a given node cluster by
	 * dividing the cut that results from the cluster through
	 * the minimum of the clusters edges and the remaining edges in the graph.
	 * The conductance measures the fraction of the incident edges leaving the subgraph.
	 * 
	 * @param graphAdjacencyMatrix A graphs adjacency matrix (with self directed edges)
	 * @param cluster A node cluster given by a list of indices
	 * 
	 * @return A conductance of floating point value
	 */
	public double cal_conductance(Matrix graphAdjacencyMatrix, List<Integer> cluster) {

		int[] clusterArray = new int[cluster.size()];
		for (int i = 0; i < cluster.size(); i++) {
			clusterArray[i] = cluster.get(i);
		}
		int[] allColumns = new int[graphAdjacencyMatrix.columns()];
		for (int i = 0; i < graphAdjacencyMatrix.columns(); i++) {
			allColumns[i] = i;
		}

		Matrix clusterAdjacencyMatrix = graphAdjacencyMatrix.select(clusterArray, allColumns);

		Matrix subgraphAdjacencyMatrix = graphAdjacencyMatrix.select(clusterArray, clusterArray);

		double cutsize = clusterAdjacencyMatrix.sum() - subgraphAdjacencyMatrix.sum();
		double denominator = Math.min(clusterAdjacencyMatrix.sum(),
				graphAdjacencyMatrix.sum() - clusterAdjacencyMatrix.sum());
		double conductance = Double.MAX_VALUE;
		if (denominator != 0.0) {
			conductance = cutsize / denominator;
		}

		return conductance;
	}

	// Pretty much based on the scipy orth() solution, no clue if that works completely as intended
	// TODO: Check
	
	/**
	 * Computes the eigenvalues of the Krylov subspace by
	 * single value decomposition
	 * 
	 * @param matrix A matrix
	 * 
	 * @return A matrix of orthonormal vectors
	 */
	public Matrix createOrthonormal(Matrix matrix) {
//		System.out.println("Mat\n" + matrix.toString() + "mat\n");

		SingularValueDecompositor svdCompositor = new SingularValueDecompositor(matrix);
		// Looks like (U=unitary,D=diagonal,V=unitary)
		Matrix[] svdResult = svdCompositor.decompose();
		Matrix U = svdResult[0];
		Matrix V = svdResult[2];
		Matrix D = svdResult[1];
		
		// TODO: Check whether #rows=#columns for U,V
		int m = U.rows();
		int n = V.columns();

		double rcond = Double.MIN_VALUE * Math.max(m, n);

		double tol = D.max() * rcond;

		double num = 0.0;
//		System.out.println("tol was: " + tol);
		for (int i = 0; i < D.rows(); i++) {
			if (D.get(i, i) > tol) {
				num += D.get(i, i);
			} else {
				// System.out.println("Dii was: " + D.get(i, i));
			}
		}

		Matrix orthonormalMatrix = new Basic2DMatrix(U.rows(), (int) num + 1);
		for (int j = 0; j <= (int) num; j++) {
			Vector a = U.getColumn(j);
			double norm = a.fold(Vectors.mkManhattanNormAccumulator());
			Vector normalizedColumn = a.divide(norm);

			orthonormalMatrix.setColumn(j, normalizedColumn);
//			System.out.println(normalizedColumn.toString());
		}

		// System.out.println(orthonormalMatrix.toString());

		return orthonormalMatrix;
	}

	// Start a random walk with probability distribution p_initial.
	// Transition matrix needs to be calculated according to adjacent matrix G.
	/**
	 * Starts a random walk 
	 * 
	 * @param graphAdjacencyMatrix A graphs adjacency matrix
	 * @param initial_prob A vector of initial node probability
	 * @param subspace_dim The size of the subspace dimension
	 * @param walk_steps Number of random walk steps that are going to be taken 
	 * 
	 * @return The probability matrix with n probability vectors resulting from n random walks
	 */
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

//		System.out.println("ORTH: " + orthProbMatrix.rows() + " " + orthProbMatrix.columns());

		for (int i = 0; i < walk_steps; i++) {
//			System.out.println("ORTH: " + orthProbMatrix.rows() + " " + orthProbMatrix.columns());
			Matrix temp = orthProbMatrix.transpose().multiply(normalizedAdjacencyMatrix);
			orthProbMatrix = createOrthonormal(temp.transpose());
		}

		return orthProbMatrix;
	}

	// TODO: Check
	/**
	 * Computes the minimum one norm, essentially linear programming 
	 * 
	 * @param matrix A probability matrix
	 * @param initial_seed A vector of the initial seed node indices
	 * @param seed A vector of the current seed node indices
	 * 
	 * @return The probability vector for nodes to belong in a community
	 */
	public ArrayList<Double> min_one_norm(Matrix matrix, ArrayList<Integer> initial_seed, ArrayList<Integer> seed) {

		double weight_initial = 1 / (double) (initial_seed.size());
		double weight_later_added = weight_initial / 0.5;
		int difference = seed.size() - initial_seed.size();
		int rows = matrix.rows();
		int columns = matrix.columns();

		// min ||y||_1
		double[] term = new double[rows + columns];

//		System.out.println("MINMAT\n" + matrix.toString() + "MINMAT\n");
//		System.out.println("SEED\n" + initial_seed.toString() + "\nSEED\n");

		for (int i = 0; i < rows; i++) {
			term[i] = 1.0; // Add y's to objective function
		}
		for (int j = 0; j < columns; j++) {
			term[rows + j] = 0.0; // Leave out x's from objective function
		}

		LinearObjectiveFunction objective = new LinearObjectiveFunction(term, 0);

		List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();

		// y == V_kl * x //TODO: Check if that works as intended
		for (int constraintRow = 0; constraintRow < rows; constraintRow++) {
			double[] constTerm = new double[rows + columns];
			for (int i = 0; i < rows; i++) {
				if (i == constraintRow) {
					constTerm[i] = 1.0;
				} else {
					constTerm[i] = 0.0;
				}
			}
			for (int j = 0; j < columns; j++) {
				constTerm[rows + j] = -matrix.get(constraintRow, j);
			}
			constraints.add(new LinearConstraint(constTerm, Relationship.EQ, 0.0));
		}

		// y >= 0
		for (int constraintRow = 0; constraintRow < rows; constraintRow++) {
			double[] constTerm = new double[rows + columns];
			for (int i = 0; i < rows; i++) {
				if (i == constraintRow) {
					constTerm[i] = 1.0;
					// System.out.println("Set constTerm[" + i + "] to one");
				} else {
					constTerm[i] = 0.0;
				}
			}
			for (int j = 0; j < columns; j++) {
				constTerm[rows + j] = 0.0;
			}
			constraints.add(new LinearConstraint(constTerm, Relationship.GEQ, 0.0));
		}

		// y >= 1 , y element of initialSeed
		for (int constraintRow = 0; constraintRow < rows; constraintRow++) {
			double[] constTerm = new double[rows + columns];
			for (int i = 0; i < rows; i++) {
				if (i == constraintRow && initial_seed.contains(constraintRow)) {
					constTerm[i] = 1.0;
					// System.out.println("Set constTerm[" + i + "] to one " + constTerm[i]);
				} else {
					constTerm[i] = 0.0;
				}
			}
			for (int j = 0; j < columns; j++) {
				constTerm[rows + j] = 0.0;
			}
			if (initial_seed.contains(constraintRow)) {
				constraints.add(new LinearConstraint(constTerm, Relationship.GEQ, 1.0));
			}
		}

		// y >= 1+ weight_later_added*difference , y element of seed
		for (int constraintRow = 0; constraintRow < rows; constraintRow++) {
			double[] constTerm = new double[rows + columns];
			for (int i = 0; i < rows; i++) {
				if (i == constraintRow && seed.contains(i)) {
					constTerm[i] = 1.0;
				} else {
					constTerm[i] = 0.0;
				}
			}
			for (int j = 0; j < columns; j++) {
				constTerm[rows + j] = 0.0;
			}
			if (seed.contains(constraintRow)) {
				constraints
						.add(new LinearConstraint(constTerm, Relationship.GEQ, 1.0 + weight_later_added * difference));
			}
		}

		System.out.println("Begin Norm");

		PointValuePair solution = null;
		solution = new SimplexSolver().optimize(objective, new LinearConstraintSet(constraints), GoalType.MINIMIZE);

		System.out.println("Norm done");

		ArrayList<Double> v = new ArrayList<Double>(rows);

		//System.out.println(Arrays.toString(solution.getPoint()));
		//System.out.println(solution.getPoint().length);

		for (int i = 0; i < rows; i++) {
			if (solution.getPoint()[i] >= 1.0) {
				v.add(i, 1.0);
			} else {
				v.add(i, solution.getPoint()[i]);
			}
		}

		return v;
	}

	/**
	 * Finds the first semi-global minimum within a range of 32 starting from entry 0
	 * 
	 * @param sequence A sequence of (conductance) values
	 * @param start_index The index at which the looked at portion would actually start
	 * @param global_conductance An array of the 30 most recent global conductances
	 * @param iteration The current iteration number of the algorithm
	 * 
	 * @return The global minimum of the sequence
	 */
	public int global_minimum(ArrayList<Double> sequence, int start_index, double[] global_conductance, int iteration) {

		int detected_size = sequence.size();
		int seq_length = sequence.size();

		//System.out.println("TO BE INSERTED: " + sequence.get(Math.floorMod(seq_length - 2, seq_length)));
		//System.out.println("FROM: " + sequence.toString());
		global_conductance[Math.floorMod(iteration, global_conductance.length)] = sequence
				.get(Math.floorMod(seq_length - 2, seq_length));
		
		for (int x = 0; x < 40; x++) {
			sequence.add(0.0);
		}
		for (int i = 0; i < seq_length; i++) {
			if (sequence.get(i) < sequence.get(Math.floorMod(i - 1, seq_length))
					&& sequence.get(i) < sequence.get(i + 1)) {
				int count_larger = 0;
				int count_smaller = 0;
				for (int j = 1; j < 32; j++) {
					if (sequence.get(i + 1 + j) > sequence.get(i + 1)) {
						count_larger++;
					}
				}
				for (int k = 1; k < 32; k++) {
					if (sequence.get(Math.floorMod(i - 1 - k, seq_length)) > sequence
							.get(Math.floorMod(i - 1, seq_length))) {
						count_smaller++;
					}
				}
				if (count_larger >= 18 && count_smaller >= 18) {
					detected_size = i + start_index;
					global_conductance[Math.floorMod(iteration, global_conductance.length)] = sequence.get(i);
					break;
				}
			}
		}
		
		return detected_size;
	}

	/**
	 * The actual ocd algorithm (Local Expansion via Minimum One Norm)
	 * 
	 * @param graphAdjacencyMatrix A graphs adjacency matrix (with self-directed edges)
	 * @param seedset The set of seed node indices
	 * @param min_comm_size Minimum size of a community
	 * @param max_comm_size Maximum size of a community
	 * @param expand_step (maximum) number of added nodes per iteration
	 * @param subspace_dim Dimension of the krylov subspace, i.e. number of random walks per iteration
	 * @param walk_steps Number of steps per random walk
	 * @param biased Boolean to tell whether node degree is taken into account for initial probabilities or not
	 * 
	 * @return A map of node indices and their likelihood to be in a community
	 */
	public Map<Integer, Double> seed_expand_auto(Matrix graphAdjacencyMatrix, ArrayList<Integer> seedset,
			int min_comm_size, int max_comm_size, int expand_step, int subspace_dim, int walk_steps, boolean biased) {

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

		if (max_comm_size > n) {
			max_comm_size = n;
		}
		if (min_comm_size > n) {
			min_comm_size = 1;
		}

		Map<Integer, Double> detected_comm = new HashMap<Integer, Double>();

		double global_conductance[] = new double[30];
		global_conductance[29] = 1000000; // set the last element to be infinitely large
		global_conductance[28] = 1000000;
		boolean flag = true;

		int iteration = 0;
		while (flag && iteration < 50) { // && iteration < 1 && detected_comm.size() < rows) {
			ArrayList<Double> temp = min_one_norm(orthProbMatrix, initial_seed, seed); 
			
			List<Integer> sorted_top = new ArrayList<Integer>(temp.size());

			// TODO: Check if included one too many through new_graph_size
			// Returns the nodes indices of the new_graph_size -many biggest values of
			// sub_prob_distribution in array form
			LEMONArrayListIndexComparator comparator = new LEMONArrayListIndexComparator(temp);
			sorted_top = comparator.createIndexArrayList();
			Collections.sort(sorted_top, comparator);
			if (step <= sorted_top.size()) {
				sorted_top = sorted_top.subList(0, step);
			}

			for (int i : sorted_top) {
				if (!detected.contains(i)) {
					detected.add(i);
				}
			}
			seed = detected;

			ArrayList<Double> conductance_record = new ArrayList<Double>((max_comm_size - min_comm_size + 1));
			for (int i = 0; i <= max_comm_size - min_comm_size; i++) {
				conductance_record.add(i, 0.0);
			}

			LEMONArrayListIndexComparator tmpcomparator = new LEMONArrayListIndexComparator(temp);
			ArrayList<Integer> tempIndDesc = comparator.createIndexArrayList();
			Collections.sort(tempIndDesc, tmpcomparator);

			for (int i = min_comm_size; i <= max_comm_size; i++) {
				List<Integer> candidate_comm = new ArrayList<Integer>(max_comm_size);
				if (i < tempIndDesc.size()) {
					candidate_comm = tempIndDesc.subList(0, i);
				} else {
					candidate_comm = tempIndDesc;
				}
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

			if (global_conductance[Math.floorMod(iteration - 1, global_conductance.length)] <= global_conductance[Math
					.floorMod(iteration, global_conductance.length)]
					&& global_conductance[Math.floorMod(iteration - 1,
							global_conductance.length)] <= global_conductance[Math.floorMod(iteration - 2,
									global_conductance.length)]) {
				flag = false;
			}

			if (detected_size != 0 && flag) {
				for (int i = 0; i < detected_size; i++) {
					detected_comm.put(tempIndDesc.get(i), temp.get(tempIndDesc.get(i)));
				}
			}

			iteration++;
		}

		return detected_comm;
	}

	// TODO: Maybe implement the score functions
}
