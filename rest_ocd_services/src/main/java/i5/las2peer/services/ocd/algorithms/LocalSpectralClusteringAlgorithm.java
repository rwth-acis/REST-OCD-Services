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

import java.util.*;

import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;

//import scpsolver.problems.LinearProgram;
//import scpsolver.problems.LPSolution;
//import scpsolver.problems.LPWizard;
//import scpsolver.problems.LPWizardConstraint;
//import scpsolver.lpsolver.LinearProgramSolver;
//import scpsolver.lpsolver.SolverFactory;
//import scpsolver.constraints.LinearConstraint;
//import scpsolver.constraints.*;


import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * Implements The LEMON Local Spectral Clustering Algorithm by Y Li, K He, K Kloster, D Bindel, J Hopcroft:
 * Local spectral clustering for overlapping community detection
 * https://doi.org/10.1145/3106370
 * Heavily oriented on https://github.com/YixuanLi/LEMON. Handles Unweighted(for now),
 * Directed/Undirected graphs
 */
public class LocalSpectralClusteringAlgorithm implements OcdAlgorithm {

	/**
	 * The index list of the graph node seed set
	 */
	private ArrayList<String> commaSeparatedSeedSet = new ArrayList<String>(Arrays.asList("NodeName"));

	/**
	 * The minimum possible community size
	 */
	private int minimumCommunitySize = 1;

	/**
	 * The maximum possible community size
	 */
	private int maximumCommunitySize = 100;

	/**
	 * The number of nodes to be added with each expansion step
	 * 
	 * Setting this higher can increase performance but too high values might
	 * decrease the community quality.
	 */
	private int expansionStepSize = 6;

	/**
	 * Specifies whether the initial probabilities for the starting nodes should be
	 * based on their degree (true) or all be equal (false)
	 */
	private boolean biased = true;

	// ADVANCED PARAMETERS

	/**
	 * Specifies the subspace dimension size.
	 * 
	 * Essentially alters the number of successive random walks and eigenvectors.
	 * Raising this potentially yields better communities, but can greatly decrease
	 * performance
	 */
	private int subspaceDimension = 3;

	/**
	 * The number of steps a random Walk will take.
	 * 
	 * Can yield better communities, but setting it too high might achieve the
	 * opposite and also impact performance
	 */
	private int randomWalkSteps = 3;

	/*
	 * PARAMETER NAMES
	 */

	public static final String COMMA_SEPARATED_SEED_SET_NAME = "commaSeparatedSeedSet";

	public static final String MINIMUM_COMMUNITY_SIZE_NAME = "minimumCommunitySize";

	public static final String MAXIMUM_COMMUNITY_SIZE_NAME = "maximumCommunitySize";

	public static final String EXPANSION_STEP_SIZE_NAME = "expansionStepSize";

	public static final String BIASED_NAME = "biased";

	public static final String SUBSPACE_DIMENSION_NAME = "subspaceDimension";

	public static final String RANDOM_WALK_STEPS_NAME = "randomWalkSteps";

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
		return compatibilities;
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(COMMA_SEPARATED_SEED_SET_NAME, commaSeparatedSeedSet.toString().substring(1, commaSeparatedSeedSet.toString().length()-1));
		parameters.put(MINIMUM_COMMUNITY_SIZE_NAME, Integer.toString(minimumCommunitySize));
		parameters.put(MAXIMUM_COMMUNITY_SIZE_NAME, Integer.toString(maximumCommunitySize));
		parameters.put(EXPANSION_STEP_SIZE_NAME, Integer.toString(expansionStepSize));
		parameters.put(BIASED_NAME, Boolean.toString(biased));
		parameters.put(SUBSPACE_DIMENSION_NAME, Integer.toString(subspaceDimension));
		parameters.put(RANDOM_WALK_STEPS_NAME, Integer.toString(randomWalkSteps));
		return parameters;
	}

	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if (parameters.containsKey(COMMA_SEPARATED_SEED_SET_NAME)) {
			String seedString = parameters.get(COMMA_SEPARATED_SEED_SET_NAME);
			//if (seedString.matches("[^,0-9]+")) {
			//	throw new IllegalArgumentException();
			//}

			// DEBUG: System.out.println("seed: " + seedString);
			ArrayList<String> seedSetTmp = new ArrayList<String>();

			String[] seedSetString = seedString.split(",");
			for (String str : seedSetString) {
				seedSetTmp.add(str);
			}
			if (seedSetTmp.size() != 0) {
				commaSeparatedSeedSet = seedSetTmp;
			}
			parameters.remove(COMMA_SEPARATED_SEED_SET_NAME);
		}
		if (parameters.containsKey(MINIMUM_COMMUNITY_SIZE_NAME)) {
			minimumCommunitySize = Integer.parseInt(parameters.get(MINIMUM_COMMUNITY_SIZE_NAME));			
			if (minimumCommunitySize <= 0) {
				throw new IllegalArgumentException();
			}
			if(minimumCommunitySize < commaSeparatedSeedSet.size())
			{
				minimumCommunitySize = commaSeparatedSeedSet.size();
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
			// DEBUG: System.out.println("params: " + parameters.toString());
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
		
		ArrayList<Integer> commaSeparatedSeedIndexSet = new ArrayList<Integer>(commaSeparatedSeedSet.size());
		Node[] graphNodes = graph.nodes().toArray(Node[]::new);
		for(String seed : commaSeparatedSeedSet)
		{
			boolean found = false;
			for(Node node : graphNodes)
			{
				System.out.println("seed="+seed + " | node.getIndex()=" + node.getIndex() );
				if(graph.getNodeName(node).equals(seed))
				{
					commaSeparatedSeedIndexSet.add(node.getIndex());
					found = true;
					break;
				}
			}
			if(!found) {
				throw new OcdAlgorithmException("Could not find a seed node: " + seed);
			}
		}
		
		
		Matrix graphAdjacencyMatrix = getAdjacencyMatrixWithIdentity(graph);
		
		Map<Integer, Double> members = seedSetExpansion(graphAdjacencyMatrix, commaSeparatedSeedIndexSet, minimumCommunitySize,
				maximumCommunitySize, expansionStepSize, subspaceDimension, randomWalkSteps, biased);

		Matrix coverMatrix = new Basic2DMatrix(graphAdjacencyMatrix.rows(), 2);
		coverMatrix = coverMatrix.blank();
		for(int i=0; i<graphAdjacencyMatrix.rows(); i++)
		{
			coverMatrix.set(i, 1, 1);
		}
		
		Double maxVal = 0.0;
		// Calculate maximum value to get all entries to a value from 0 to 1
		for(Map.Entry<Integer, Double> entry : members.entrySet())
		{
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			entry.setValue(Math.log10(entry.getValue()+1)); //TODO: Decide whether to remove or keep for better representation
			if(entry.getValue() > maxVal)
			{
				maxVal = entry.getValue();
			}
		}
		// DEBUG: System.out.println("Max: " + maxVal);
		for (Map.Entry<Integer, Double> entry : members.entrySet()) {
			// DEBUG: System.out.println(entry.getKey() + ": " + entry.getValue() + " , " + entry.getValue()/maxVal);
			double normVal = entry.getValue()/maxVal;
			coverMatrix.set(entry.getKey(), 0, normVal);
			coverMatrix.set(entry.getKey(), 1, 1.0 - normVal);
		}

		// DEBUG: System.out.println("DETECTED FINAL:" + members.keySet().toString());
		// DEBUG: System.out.println("DETECTED FINAL:" + members.values().toString());

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
	 * @throws InterruptedException if the thread was interrupted
	 */
	public Matrix getAdjacencyMatrixWithIdentity(CustomGraph graph) throws InterruptedException {

		int size = graph.getNodeCount();
		Matrix adjacencyMatrix = new CCSMatrix(size, size); //TOD: Maybe CCS is the problem?
		adjacencyMatrix = adjacencyMatrix.blank();
		Iterator<Edge> ecIt = graph.edges().iterator();
		while (ecIt.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Edge edge = ecIt.next();
			Node source = edge.getSourceNode();
			Node target = edge.getTargetNode();

			if (source.getIndex() != target.getIndex()) {
				adjacencyMatrix.set(source.getIndex(), target.getIndex(), 1);
			}
		}

		// Self-directed edges
		for (int i = 0; i < size; i++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			adjacencyMatrix.set(i, i, 1);
		}
		
		// DEBUG: System.out.println("Adjmat:\n" + adjacencyMatrix);
		return adjacencyMatrix;
	}

	/**
	 * Sets the initial probability for every node to be in the community
	 * Here, it is unbiased, so every node will have 1/#startingNodes
	 * 
	 * @param n Number of nodes n in graph
	 * @param startingNodes ArrayList of starting node indices
	 * 
	 * @return A vector containing all initial probabilities.
	 */
	public Vector setInitialProbability(int n, ArrayList<Integer> startingNodes) {
		Vector v = new BasicVector(n);
		v = v.blank();

		for (int node : startingNodes) {
			v.set(node, 1. / startingNodes.size());
		}

		return v;
	}

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
	public Vector setInitialProbabilityProportional(int n, Map<Integer, Integer> degreeMap,
			ArrayList<Integer> startingNodes) {
		Vector v = new BasicVector(n);
		v = v.blank();

		double degreeSum = 0;
		for (int node : startingNodes) {
			try {
				degreeSum += degreeMap.get(node);
			}
			catch(NullPointerException err)
			{
				throw err;
			}
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

		// DEBUG: System.out.println("Matrix setup");
		// Represents the degree matrix D^(-1/2)
		Matrix sqrtDegrees = new Basic2DMatrix(n, n);
		sqrtDegrees = sqrtDegrees.blank();
		for (int i = 0; i < n; i++) {			
			sqrtDegrees.set(i, i, 1.0 / Math.sqrt(graphAdjacencyMatrix.getRow(i).sum()));
		}

		// Represents the matrix (A+I)
		Matrix normalizedAdjacencyMatrix = graphAdjacencyMatrix;
		
		// DEBUG: System.out.println("Matrix mult opt begin");
		//(A+I)*(D^(-1/2)) = R
		for(int j=0; j<n; j++) {
			normalizedAdjacencyMatrix.setColumn(j, normalizedAdjacencyMatrix.getColumn(j).multiply(sqrtDegrees.get(j, j)));
		}
		
		//(D^(-1/2))*R
		for(int i=0; i<n; i++)
		{			
			normalizedAdjacencyMatrix.setRow(i, normalizedAdjacencyMatrix.getRow(i).multiply(sqrtDegrees.get(i,i)));
		}
		// DEBUG: System.out.println("Matrix mult opt end");

		return normalizedAdjacencyMatrix;
	}

	/**
	 * Computes the conductance of a given node cluster by
	 * dividing the cut that results from the cluster through
	 * the minimum of the clusters edges and the remaining edges in the graph.
	 * The conductance measures the fraction of the incident edges leaving the subgraph.
	 * 
	 * @param graphAdjacencyMatrix A graphs adjacency matrix (with self directed edges)
	 * @param cluster A node cluster given by a list of indices
	 * @param graphAdjacencyMatrixRowSums Row sums of the graphs adajacency matrix
	 * @param graphAdjacencyMatrixSum Sum of the adjacency matrix's values
	 * 
	 * @return A conductance of floating point value
	 * @throws InterruptedException if the thread was interrupted
	 */
	public double calculateConductance(Matrix graphAdjacencyMatrix, List<Integer> cluster, double graphAdjacencyMatrixSum, double[] graphAdjacencyMatrixRowSums) throws InterruptedException{

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
		
		double clusterAdjacencyMatrixSum = 0;
		for(int i=0; i<cluster.size(); i++)
		{
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			
			clusterAdjacencyMatrixSum += graphAdjacencyMatrixRowSums[clusterArray[i]];
		}
		double cutsize = clusterAdjacencyMatrixSum - subgraphAdjacencyMatrix.sum();
		double denominator = Math.min(clusterAdjacencyMatrixSum,
				graphAdjacencyMatrixSum - clusterAdjacencyMatrixSum);
		double conductance = Double.MAX_VALUE;
		if (denominator != 0.0) {
			conductance = cutsize / denominator;
		}

		return conductance;
	}

	// Pretty much based on the scipy orth() solution
	/**
	 * Computes the eigenvalues of the Krylov subspace by
	 * single value decomposition
	 * 
	 * @param matrix A matrix
	 * 
	 * @return A matrix of orthonormal vectors
	 * @throws InterruptedException if the thread was interrupted
	 */
	public Matrix createOrthonormal(Matrix matrix) throws InterruptedException{
		// DEBUG: System.out.println("Mat\n" + matrix.toString() + "mat\n");

		SingularValueDecompositor svdCompositor = new SingularValueDecompositor(matrix);
		// Looks like (U=orthogonal columns, D=diagonal,V=orthogonal columns)
		Matrix[] svdResult = svdCompositor.decompose();
		Matrix U = svdResult[0];
		Matrix V = svdResult[2];
		Matrix D = svdResult[1];
		
		int m = U.rows();
		int n = V.columns();

		double rcond = Double.MIN_VALUE * Math.max(m, n);

		double tol = D.max() * rcond;

		double num = 0.0;
		for (int i = 0; i < D.rows(); i++) {
			if (D.get(i, i) > tol) {
				num += D.get(i, i);
			}
		}

		num +=1;
		if((int)num > U.columns())
		{
			num = U.columns();
		}
		
		Matrix orthonormalMatrix = new Basic2DMatrix(U.rows(), (int) num);
		for (int j = 0; j < (int) num; j++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			
			Vector a = U.getColumn(j);
			double norm = a.fold(Vectors.mkManhattanNormAccumulator());
			Vector normalizedColumn = a.divide(norm);

			orthonormalMatrix.setColumn(j, normalizedColumn);
			// DEBUG: System.out.println(normalizedColumn.toString());
		}

		// DEBUG: System.out.println(orthonormalMatrix.toString());

		return orthonormalMatrix;
	}

	/**
	 * Starts a random walk 
	 * 
	 * @param graphAdjacencyMatrix A graphs adjacency matrix
	 * @param initialProbability A vector of initial node probability
	 * @param subspaceDimension The size of the subspace dimension
	 * @param randomWalkSteps Number of random walk steps that are going to be taken 
	 * 
	 * @return The probability matrix with n probability vectors resulting from n random walks
	 * @throws InterruptedException if the thread was interrupted
	 */
	public Matrix randomWalk(Matrix graphAdjacencyMatrix, Vector initialProbability, int subspaceDimension, int randomWalkSteps) throws InterruptedException{
		Matrix normalizedAdjacencyMatrix = adjToNormAdj(graphAdjacencyMatrix);
		
		Matrix probMatrix = new Basic2DMatrix(graphAdjacencyMatrix.rows(), subspaceDimension);
		probMatrix = probMatrix.blank();
		for (int i = 0; i < probMatrix.rows(); i++) {
			probMatrix.set(i, 0, initialProbability.get(i));
		}

		for (int i = 1; i < subspaceDimension; i++) {
			probMatrix.setColumn(i, normalizedAdjacencyMatrix.multiply(probMatrix.getColumn(i - 1)));
		}
		Matrix orthProbMatrix = createOrthonormal(probMatrix);

		for (int i = 0; i < randomWalkSteps; i++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			
			Matrix temp = orthProbMatrix.transpose().multiply(normalizedAdjacencyMatrix);
			orthProbMatrix = createOrthonormal(temp.transpose());
		}

		return orthProbMatrix;
	}

	/**
	 * Computes the minimum one norm for a subgraph and its seed nodes, essentially solves the linear programming problem:
	 * 		min ||y||_1
	 * 		y == V_k,l * x
	 * 		y &gt;= 0
	 * 		y_initialSeed &gt;= 1
	 * 		y_seed &gt;= 1 + weightLaterAdded*difference
	 * from the paper.
	 * 
	 * @param matrix A probability matrix
	 * @param initialSeed A vector of the initial seed node indices
	 * @param seed A vector of the all seed node indices
	 * @param graphAdjacencyMatrix the graphs adjacency matrix
	 * 
	 * @return A vector which entries signify the likelihood for nodes to belong in a community (the larger the number the larger the likelihood, not a probability vector)
	 */
	public ArrayList<Double> minimumOneNorm(Matrix matrix, ArrayList<Integer> initialSeed, ArrayList<Integer> seed, Matrix graphAdjacencyMatrix) {

		// DEBUG: System.out.println("SEEDS: " + seed.toString());
		
		double weightInitial = 1 / (double) (initialSeed.size());
		double weightLaterAdded = weightInitial / 0.5;
		int difference = seed.size() - initialSeed.size();
		int rows = matrix.rows();
		int columns = matrix.columns();

		// min ||y||_1
		double[] term = new double[rows + columns];
		ArrayList<Variable> vars = new ArrayList<Variable>(rows + columns);

		// DEBUG: System.out.println("MINMAT\n" + matrix.toString() + "\n" + matrix.getColumn(0).sum() + "MINMAT\n");
		// DEBUG: System.out.println("INITIALSEED\n" + initialSeed.toString() + "\nINITIALSEED\n");

		for (int i = 0; i < rows; i++) {
//APACHE			term[i] = 1.0; // Add y's to objective function
			vars.add(new Variable("y_" + i).lower(0.0).weight(1.0));
		}
		for (int j = 0; j < columns; j++) {
//APACHE			term[rows + j] = 0.0; // Leave out x's from objective function
			vars.add(new Variable("x_" + j).weight(0.0));
		}

		LinearObjectiveFunction objective = new LinearObjectiveFunction(term, 0);
		ExpressionsBasedModel model = new ExpressionsBasedModel(vars);

		ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
		

		// y == V_kl * x
		for (int constraintRow = 0; constraintRow < rows; constraintRow++) {
			Expression expr = model.addExpression("cEQ_" + constraintRow).lower(0.0).upper(0.0);
			
//APACHE			double[] constTerm = new double[rows + columns];
			for (int i = 0; i < rows; i++) {
				if (i == constraintRow) {
//APACHE					constTerm[i] = 1.0;
					expr.set(vars.get(i), 1.0);
				} else {
//APACHE					constTerm[i] = 0.0;
					expr.set(vars.get(i), 0.0);
				}
			}
			for (int j = 0; j < columns; j++) {
				
				if(-matrix.get(constraintRow, j) != 0.0)
				{
//APACHE					constTerm[rows + j] = -matrix.get(constraintRow, j);
					expr.set(vars.get(rows + j), -matrix.get(constraintRow, j));
					
				}
				else
				{
//APACHE					constTerm[rows+j] = 0.0;
					expr.set(vars.get(rows + j), 0.0);
				}
				
			}
//APACHE			constraints.add(new LinearConstraint(constTerm, Relationship.EQ, 0.0));		
		}

		// y >= 0
		for (int constraintRow = 0; constraintRow < rows; constraintRow++) {
			Expression expr = model.addExpression("cGEQ0_" + constraintRow).lower(0.0);
			
//APACHE			double[] constTerm = new double[rows + columns];
			for (int i = 0; i < rows; i++) {
				if (i == constraintRow) {
//APACHE					constTerm[i] = 1.0;
					expr.set(vars.get(i), 1.0);
				} else {
//APACHE					constTerm[i] = 0.0;
					expr.set(vars.get(i), 0.0);
				}
			}
			for (int j = 0; j < columns; j++) {
//APACHE				constTerm[rows + j] = 0.0;
				expr.set(vars.get(rows + j), 0.0);
			}
//APACHE			constraints.add(new LinearConstraint(constTerm, Relationship.GEQ, 0.0));
		}
		
		// y >= 1 , y element of initialSeed
		for (int constraintRow = 0; constraintRow < rows; constraintRow++) {
			if (initialSeed.contains(constraintRow)) {
				Expression expr = model.addExpression("cGEQ1_" + constraintRow).lower(1.0);
			
//APACHE				double[] constTerm = new double[rows + columns];
				for (int i = 0; i < rows; i++) {
					if (i == constraintRow && initialSeed.contains(constraintRow)) {
//APACHE						constTerm[i] = 1.0;
						expr.set(vars.get(i), 1.0);
					} else {
//APACHE						constTerm[i] = 0.0;
						expr.set(vars.get(i), 0.0);
					}
				}
				for (int j = 0; j < columns; j++) {
//APACHE					constTerm[rows + j] = 0.0;
					expr.set(vars.get(rows + j), 0.0);
				}					
//APACHE				constraints.add(new LinearConstraint(constTerm, Relationship.GEQ, 1.0));	
			}
		}

		// y >= 1+ weightLaterAdded*difference , y element of seed
		for (int constraintRow = 0; constraintRow < rows; constraintRow++) {
			if (seed.contains(constraintRow)) {
				Expression expr = model.addExpression("cGEQ1b_" + constraintRow).lower(1.0 + weightLaterAdded * difference);
				
//APACHE				double[] constTerm = new double[rows + columns];
				for (int i = 0; i < rows; i++) {
					if (i == constraintRow && seed.contains(i)) {
//APACHE						constTerm[i] = 1.0;
						expr.set(vars.get(i), 1.0);
					} else {
//APACHE						constTerm[i] = 0.0;
						expr.set(vars.get(i), 0.0);
					}
				}
				for (int j = 0; j < columns; j++) {
//APACHE					constTerm[rows + j] = 0.0;
					expr.set(vars.get(rows + j), 0.0);
				}				
//APACHE				constraints.add(new LinearConstraint(constTerm, Relationship.GEQ, 1.0 + weightLaterAdded * difference));
			}
		}
	
		ArrayList<Double> v = new ArrayList<Double>(rows);
		
//APACHE		PointValuePair solutionApache = null;
		Optimisation.Result result;
		try {
//APACHE
//			try {
//				//solutionApache = new SimplexSolver(1.0e-8, 10, 1.0e-15).optimize(objective, new LinearConstraintSet(constraints), GoalType.MINIMIZE, PivotSelectionRule.BLAND, new NonNegativeConstraint(false));
//				for (int i = 0; i < rows; i++) {
//					if(neighboursAndSeeds.get(i) != null)//////////////////
//					{
//						//System.out.println("SEED " + i + ": " + solutionApache.getPoint()[i]);///////////////
//						if(i == 19)//////////////////////
//						{
//							System.out.println("x36: " + solutionApache.getPoint()[36]);
//						}
//						
//						v.add(i, solutionApache.getPoint()[i]);
//						throw new java.lang.NullPointerException();/////////////
//					}
//					else///////////////////
//					{
//						v.add(i, 0.0);
//					}
//				}
//			}
//			catch(Exception err) //If Apache's SimplexSolver does not find a solution, use Ojalgo's different solvers instead
//APACHE			{
				// DEBUG: System.out.println("Apache Solver failed, trying ojalgo...");
				result = model.minimise();
//APACHE				v = new ArrayList<Double>(rows);
				
				for (int i = 0; i < rows; i++) {
					// DEBUG: System.out.println("SEED " + i + ": " + result.doubleValue(i));
					v.add(i, result.doubleValue(i));
				}
//APACHE			}
		}
		catch(Exception err) {
			// DEBUG: System.out.println(matrix);
			throw err;
		}

		return v;
	}

	/**
	 * Finds the "global" minimum within a sequence of range of 32 conductances of communities starting from entry 0
	 * This global sequence minimum is then also added to the global conductance list, as its community will be adopted for the further process
	 * 
	 * @param sequence A sequence of (conductance) values
	 * @param startIndex The index at which the looked at portion would actually start
	 * @param globalConductance An array of the 30 most recent global conductances
	 * @param iteration The current iteration number of the algorithm
	 * 
	 * @return The global minimum of the sequence
	 * @throws InterruptedException if the thread was interrupted
	 */
	public int globalMinimum(ArrayList<Double> sequence, int startIndex, double[] globalConductance, int iteration) throws InterruptedException{

		int detectedSize = sequence.size();
		int sequenceLength = sequence.size();

		globalConductance[Math.floorMod(iteration, globalConductance.length)] = sequence
				.get(Math.floorMod(sequenceLength - 2, sequenceLength));
		
		for (int x = 0; x < 40; x++) {
			sequence.add(0.0);
		}
		for (int i = 0; i < sequenceLength; i++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			
			if (sequence.get(i) < sequence.get(Math.floorMod(i - 1, sequenceLength))
					&& sequence.get(i) < sequence.get(i + 1)) { // Is local Minimum
				int countLarger = 0;
				int countSmaller = 0;
				for (int j = 1; j < 32; j++) { // Calculate how long values get continuously larger afterwards
					if (sequence.get(i + 1 + j) > sequence.get(i + 1)) {
						countLarger++;
					}
				}
				for (int k = 1; k < 32; k++) { // Calculate how long values get continuously larger before
					if (sequence.get(Math.floorMod(i - 1 - k, sequenceLength)) > sequence
							.get(Math.floorMod(i - 1, sequenceLength))) {
						countSmaller++;
					}
				}
				if (countLarger >= 18 && countSmaller >= 18) {
					detectedSize = i + startIndex;
					globalConductance[Math.floorMod(iteration, globalConductance.length)] = sequence.get(i);
					break;
				}
			}
		}
		
		return detectedSize;
	}

	/**
	 * The actual ocd algorithm (Local Expansion via Minimum One Norm)
	 * 
	 * @param graphAdjacencyMatrix A graphs adjacency matrix (with self-directed edges)
	 * @param seedset The set of seed node indices
	 * @param minimumCommunitySize Minimum size of a community
	 * @param maximumCommunitySize Maximum size of a community
	 * @param expansionStepSize (maximum) number of added nodes per iteration
	 * @param subspaceDimension Dimension of the krylov subspace, i.e. number of random walks per iteration
	 * @param randomWalkSteps Number of steps per random walk
	 * @param biased Boolean to tell whether node degree is taken into account for initial probabilities or not
	 * 
	 * @return A map of node indices and their likelihood to be in a community
	 * @throws InterruptedException if the thread was interrupted
	 */
	public Map<Integer, Double> seedSetExpansion(Matrix graphAdjacencyMatrix, ArrayList<Integer> seedset,
			int minimumCommunitySize, int maximumCommunitySize, int expansionStepSize, int subspaceDimension, int randomWalkSteps, boolean biased) throws InterruptedException{

		int n = graphAdjacencyMatrix.rows();
		
		double graphAdjacencyMatrixSum = graphAdjacencyMatrix.sum();
		double[] graphAdjacencyMatrixRowSums = new double[n];
		for(int i=0; i<n; i++)
		{
			graphAdjacencyMatrixRowSums[i]=graphAdjacencyMatrix.getRow(i).sum();
		}
		
		HashMap<Integer, Integer> degree = new HashMap<Integer, Integer>();
		for (int i = 0; i < n; i++) {
			degree.put(i, (int) (graphAdjacencyMatrix.getRow(i).sum()));
		}

		Vector initialProbability;
		// Random walk starting from seed nodes:
		// DEBUG: System.out.println("SEEDS: " + seedset.toString());
		if (biased) {
			initialProbability = setInitialProbabilityProportional(n, degree, seedset);
		} else {
			initialProbability = setInitialProbability(n, seedset);
		}

		// DEBUG: System.out.println("Initial walk begin");
		Matrix orthProbMatrix = randomWalk(graphAdjacencyMatrix, initialProbability, subspaceDimension, randomWalkSteps);
		ArrayList<Integer> initialSeed = seedset;
		// DEBUG: System.out.println("initial walk end");
		
		// Initialization
		ArrayList<Integer> detected = new ArrayList<Integer>(seedset);
		int rows = orthProbMatrix.rows();
		int columns = orthProbMatrix.columns();
		ArrayList<Integer> seed = new ArrayList<Integer>(seedset);
		int step = expansionStepSize;

		if (maximumCommunitySize > n) {
			maximumCommunitySize = n;
		}
		if (minimumCommunitySize > n) {
			minimumCommunitySize = 1;
		}

		Map<Integer, Double> detectedCommunity = new HashMap<Integer, Double>();

		double globalConductance[] = new double[30];
		// Set the last two elements to be infinitely large
		globalConductance[29] = Double.MAX_VALUE; 
		globalConductance[28] = Double.MAX_VALUE;
		boolean flag = true;

		int iteration = 0;
		while (flag) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			
			// DEBUG: System.out.println("Start Norm procedure");
			ArrayList<Double> temp = minimumOneNorm(orthProbMatrix, initialSeed, seed, graphAdjacencyMatrix); 
			
			List<Integer> sortedTop = new ArrayList<Integer>(temp.size());

			// TODO: Check if included one too many through new_graph_size
			// Compute a list of the node indices ranked by descending norm value
			LEMONArrayListIndexComparator comparator = new LEMONArrayListIndexComparator(temp);
			sortedTop = comparator.createIndexArrayList();
			Collections.sort(sortedTop, comparator);
			if (step <= sortedTop.size()) {
				sortedTop = sortedTop.subList(0, step);
			}

			for (int i : sortedTop) {
				if (!detected.contains(i)) {
					detected.add(i);
				}
			}
			seed = detected;

			// Calculate the conductance
			// DEBUG: System.out.println("Calculate the Conductance");
			ArrayList<Double> conductanceRecord = new ArrayList<Double>((maximumCommunitySize - minimumCommunitySize + 1));
			for (int i = 0; i <= maximumCommunitySize - minimumCommunitySize; i++) {
				conductanceRecord.add(i, 0.0);
			}

			comparator = new LEMONArrayListIndexComparator(temp);
			ArrayList<Integer> tempIndDesc = comparator.createIndexArrayList();
			Collections.sort(tempIndDesc, comparator);
			
			for (int i = minimumCommunitySize; i <= maximumCommunitySize; i++) {
				List<Integer> candidateCommunity = new ArrayList<Integer>(maximumCommunitySize);
				
				candidateCommunity = tempIndDesc.subList(0, i);
				conductanceRecord.set(i - minimumCommunitySize, calculateConductance(graphAdjacencyMatrix, candidateCommunity, graphAdjacencyMatrixSum, graphAdjacencyMatrixRowSums));
			}
			// DEBUG: System.out.println("CONDUCTANCES: " + conductanceRecord.toString());

			// Receive best community size according to conductance
			// DEBUG: System.out.println("Start minimum");
			int detectedSize = globalMinimum(conductanceRecord, minimumCommunitySize, globalConductance, iteration);
			// DEBUG: System.out.println("End minimum");

			step += expansionStepSize;

			if (biased) {
				initialProbability = setInitialProbabilityProportional(n, degree, seedset);
			} else {
				initialProbability = setInitialProbability(graphAdjacencyMatrix.rows(), seedset);
			}

			// DEBUG: System.out.println("Random walk begin");
			orthProbMatrix = randomWalk(graphAdjacencyMatrix, initialProbability, subspaceDimension, randomWalkSteps);
			// DEBUG: System.out.println("Random walk end");

			if (globalConductance[Math.floorMod(iteration - 1, globalConductance.length)] <= globalConductance[Math
					.floorMod(iteration, globalConductance.length)]
					&& globalConductance[Math.floorMod(iteration - 1,
							globalConductance.length)] <= globalConductance[Math.floorMod(iteration - 2,
									globalConductance.length)]) { // If the global conductance has increased after a local minimum
				// DEBUG: System.out.println("Conductance: " + Arrays.toString(globalConductance));
				flag = false;
			}

			if (detectedSize != 0 && flag) {
				for (int i = 0; i < detectedSize; i++) {
					detectedCommunity.put(tempIndDesc.get(i), temp.get(tempIndDesc.get(i)));
				}
			}

			iteration++;
		}

		// DEBUG: System.out.println("Done");
		return detectedCommunity;
	}

	// TODO: Maybe implement the score functions from the example for further debug purposes
}
