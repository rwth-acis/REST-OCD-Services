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
import org.la4j.matrix.sparse.CRSMatrix;
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
import org.apache.commons.math3.optim.linear.NoFeasibleSolutionException;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;
import org.apache.commons.math3.optim.linear.PivotSelectionRule;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

//import scpsolver.problems.LinearProgram;
//import scpsolver.problems.LPSolution;
//import scpsolver.problems.LPWizard;
//import scpsolver.problems.LPWizardConstraint;
//import scpsolver.lpsolver.LinearProgramSolver;
//import scpsolver.lpsolver.SolverFactory;
//import scpsolver.constraints.LinearConstraint;
//import scpsolver.constraints.*;


import org.ojalgo.OjAlgoUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * Implements The LEMON Local Spectral Clustering Algorithm by Yi et al. Heavily
 * oriented on https://github.com/YixuanLi/LEMON Handles Unweighted(for now),
 * Directed/Undirected graphs
 */
public class LocalSpectralClusteringAlgorithm implements OcdAlgorithm {

	/*
	 * The index list of the graph node seed set
	 */
	private ArrayList<Integer> commaSeparatedSeedSet = new ArrayList<Integer>(Arrays.asList(0));

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

	protected static final String COMMA_SEPARATED_SEED_SET_NAME = "commaSeparatedSeedSet";

	protected static final String MINIMUM_COMMUNITY_SIZE_NAME = "minimumCommunitySize";

	protected static final String MAXIMUM_COMMUNITY_SIZE_NAME = "maximumCommunitySize";

	protected static final String EXPANSION_STEP_SIZE_NAME = "expansionStepSize";

	protected static final String BIASED_NAME = "biased";

	protected static final String subspaceDimensionENSION_NAME = "subspaceDimension";

	protected static final String randomrandomWalkSteps_NAME = "randomWalkSteps";

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
		parameters.put(COMMA_SEPARATED_SEED_SET_NAME, commaSeparatedSeedSet.toString().substring(1, commaSeparatedSeedSet.toString().length()-1));
		parameters.put(MINIMUM_COMMUNITY_SIZE_NAME, Integer.toString(minimumCommunitySize));
		parameters.put(MAXIMUM_COMMUNITY_SIZE_NAME, Integer.toString(maximumCommunitySize));
		parameters.put(EXPANSION_STEP_SIZE_NAME, Integer.toString(expansionStepSize));
		parameters.put(BIASED_NAME, Boolean.toString(biased));
		parameters.put(subspaceDimensionENSION_NAME, Integer.toString(subspaceDimension));
		parameters.put(randomrandomWalkSteps_NAME, Integer.toString(randomWalkSteps));
		return parameters;
	}

	@Override // TODO: Check
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if (parameters.containsKey(COMMA_SEPARATED_SEED_SET_NAME)) {
			String seedString = parameters.get(COMMA_SEPARATED_SEED_SET_NAME);
			if (seedString.matches("[^,0-9]+")) {
				throw new IllegalArgumentException();
			}

			System.out.println(seedString);
			ArrayList<Integer> seedSetTmp = new ArrayList<Integer>();

			//System.out.println(seedString);
			String[] seedSetString = seedString.split(",");
			for (String str : seedSetString) {

				seedSetTmp.add(Integer.parseInt(str));
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
		if (parameters.containsKey(subspaceDimensionENSION_NAME)) {
			subspaceDimension = Integer.parseInt(parameters.get(subspaceDimensionENSION_NAME));
			if (subspaceDimension <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(subspaceDimensionENSION_NAME);
		}
		if (parameters.containsKey(randomrandomWalkSteps_NAME)) {
			randomWalkSteps = Integer.parseInt(parameters.get(randomrandomWalkSteps_NAME));
			if (randomWalkSteps <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(randomrandomWalkSteps_NAME);
		}
		if (parameters.size() > 0) {
			System.out.println(parameters.toString());
			throw new IllegalArgumentException();
		}
	}

	@Override // TODO: Implement
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
		System.out.println(graph.nodeCount());
		Matrix graphAdjacencyMatrix = getAdjacencyMatrixWithIdentity(graph);
		
		//System.out.printf("\n\n graphAdjacencyMatrix.set(%d, %d, 1.0);\n\n", graph.nodeCount(), graph.nodeCount());

		//getLinkList(graph);

		Map<Integer, Double> members = seedSetExpansion(graphAdjacencyMatrix, commaSeparatedSeedSet, minimumCommunitySize,
				maximumCommunitySize, expansionStepSize, subspaceDimension, randomWalkSteps, biased);
		
		// Clear seeds for potential component detection later on
		commaSeparatedSeedSet = new ArrayList<Integer>(Arrays.asList(0));

		Matrix coverMatrix = new Basic2DMatrix(graphAdjacencyMatrix.rows(), 2);
		coverMatrix = coverMatrix.blank();
		for(int i=0; i<graphAdjacencyMatrix.rows(); i++)
		{
			coverMatrix.set(i, 1, 1);
		}
		for (Map.Entry<Integer, Double> entry : members.entrySet()) {
			coverMatrix.set(entry.getKey(), 0, entry.getValue());
			coverMatrix.set(entry.getKey(), 1, Math.abs(1.0 - entry.getValue()));
		}

		//System.out.println("DETECTED FINAL:" + members.keySet().toString());
		//System.out.println("DETECTED FINAL:" + members.values().toString());

		Cover c = new Cover(graph, coverMatrix);
		
		return c;
	}
	
	public void detectOverlappingCommunities(Matrix graphAdjacencyMatrix) throws OcdAlgorithmException, InterruptedException {
		//Matrix graphAdjacencyMatrix = getAdjacencyMatrixWithIdentity(graph);

		//getLinkList(graph);

		Map<Integer, Double> members = seedSetExpansion(graphAdjacencyMatrix, commaSeparatedSeedSet, minimumCommunitySize,
				maximumCommunitySize, expansionStepSize, subspaceDimension, randomWalkSteps, biased);
		
		// Clear seeds for potential component detection later on
		commaSeparatedSeedSet = new ArrayList<Integer>(Arrays.asList(0));

		Matrix coverMatrix = new Basic2DMatrix(graphAdjacencyMatrix.rows(), 2);
		coverMatrix = coverMatrix.blank();
		for(int i=0; i<graphAdjacencyMatrix.rows(); i++)
		{
			coverMatrix.set(i, 1, 1);
		}
		for (Map.Entry<Integer, Double> entry : members.entrySet()) {
			coverMatrix.set(entry.getKey(), 0, entry.getValue());
			coverMatrix.set(entry.getKey(), 1, Math.abs(1.0 - entry.getValue()));
		}

		//System.out.println("DETECTED FINAL:" + members.keySet().toString());
		//System.out.println("DETECTED FINAL:" + members.values().toString());
		System.out.println(coverMatrix.toString());		
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
	public Vector setInitialProbability(int n, ArrayList<Integer> startingNodes) {
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

		for(int i=0; i<n; i++)
		{
			//System.out.println(i + "_prob: " + String.format("%.50f", v.get(i)));
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

		System.out.println("Matrix setup");
		// Represents the degree matrix D^(-1/2)
		Matrix sqrtDegrees = new Basic2DMatrix(n, n);
		sqrtDegrees = sqrtDegrees.blank();
		for (int i = 0; i < n; i++) {
			sqrtDegrees.set(i, i, 1.0 / Math.sqrt(graphAdjacencyMatrix.getRow(i).sum()));
		}

		// Represents the matrix (A+I)
		Matrix normalizedAdjacencyMatrix = graphAdjacencyMatrix;
		
		System.out.println("Matrix mult opt begin");
		//(A+I)*(D^(-1/2)) = R
		for(int j=0; j<n; j++)
		{
			normalizedAdjacencyMatrix.setColumn(j, normalizedAdjacencyMatrix.getColumn(j).multiply(sqrtDegrees.get(j, j)));
		}
		
		//(D^(-1/2))*R
		for(int i=0; i<n; i++)
		{
			normalizedAdjacencyMatrix.setRow(i, normalizedAdjacencyMatrix.getRow(i).multiply(sqrtDegrees.get(i,i)));
		}
		System.out.println("Matrix mult opt end");

		return normalizedAdjacencyMatrix;
	}

	//TODO: Check
	/**
	 * Computes the conductance of a given node cluster by
	 * dividing the cut that results from the cluster through
	 * the minimum of the clusters edges and the remaining edges in the graph.
	 * The conductance measures the fraction of the incident edges leaving the subgraph.
	 * 
	 * @param graphAdjacencyMatrix A graphs adjacency matrix (with self directed edges)
	 * @param cluster A node cluster given by a list of indices
	 * 
	 * @return A conductance of floating point value
	 */
	public double calculateConductance(Matrix graphAdjacencyMatrix, List<Integer> cluster) {

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

		//TODO: Check if num +1 truly gives the correct result
		num +=1;
		if((int)num > U.columns())
		{
			num = U.columns();
		}
		
		Matrix orthonormalMatrix = new Basic2DMatrix(U.rows(), (int) num);
		for (int j = 0; j < (int) num; j++) {
			Vector a = U.getColumn(j);
			double norm = a.fold(Vectors.mkManhattanNormAccumulator());
			Vector normalizedColumn = a.divide(norm);

			orthonormalMatrix.setColumn(j, normalizedColumn);
//			System.out.println(normalizedColumn.toString());
		}

		// System.out.println(orthonormalMatrix.toString());

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
	 */
	public Matrix randomWalk(Matrix graphAdjacencyMatrix, Vector initialProbability, int subspaceDimension, int randomWalkSteps) {
		
		// Transform the adjacency matrix to a "normalized" adjacency matrix
		Matrix normalizedAdjacencyMatrix = adjToNormAdj(graphAdjacencyMatrix);
		
		Matrix probMatrix = new Basic2DMatrix(graphAdjacencyMatrix.rows(), subspaceDimension);
		probMatrix = probMatrix.blank();
		for (int i = 0; i < probMatrix.rows(); i++) {
			probMatrix.set(i, 0, initialProbability.get(i));
		}

		// TODO: Check if that following term actually resembles P*probMatrix[i-1]:
		// np.dot(probMatrix[:,i-1], P));
		for (int i = 1; i < subspaceDimension; i++) {
			probMatrix.setColumn(i, normalizedAdjacencyMatrix.multiply(probMatrix.getColumn(i - 1)));
		}

		Matrix orthProbMatrix = createOrthonormal(probMatrix);
		
//		System.out.println("ORTH: " + orthProbMatrix.rows() + " " + orthProbMatrix.columns());

		for (int i = 0; i < randomWalkSteps; i++) {
//			System.out.println("ORTH: " + orthProbMatrix.rows() + " " + orthProbMatrix.columns());
			Matrix temp = orthProbMatrix.transpose().multiply(normalizedAdjacencyMatrix);
			orthProbMatrix = createOrthonormal(temp.transpose());
		}

		return orthProbMatrix;
	}

	/**
	 * Computes the minimum one norm, essentially linear programming 
	 * 
	 * @param matrix A probability matrix
	 * @param initialSeed A vector of the initial seed node indices
	 * @param seed A vector of the current seed node indices
	 * 
	 * @return The probability vector for nodes to belong in a community
	 */
	public ArrayList<Double> minimumOneNorm(Matrix matrix, ArrayList<Integer> initialSeed, ArrayList<Integer> seed) {

		double weightInitial = 1 / (double) (initialSeed.size());
		double weightLaterAdded = weightInitial / 0.5;
		int difference = seed.size() - initialSeed.size();
		int rows = matrix.rows();
		int columns = matrix.columns();

		// min ||y||_1
		double[] term = new double[rows + columns];
		ArrayList<Variable> vars = new ArrayList<Variable>(rows + columns);

		//System.out.println("MINMAT\n" + matrix.toString() + "\n" + matrix.getColumn(0).sum() + "MINMAT\n");
		//System.out.println("SEED\n" + initialSeed.toString() + "\nSEED\n");

		for (int i = 0; i < rows; i++) {
			term[i] = 1.0; // Add y's to objective function
			vars.add(new Variable("y_" + i).lower(0.0).weight(1.0));
		}
		for (int j = 0; j < columns; j++) {
			term[rows + j] = 0.0; // Leave out x's from objective function
			vars.add(new Variable("x_" + j).weight(0.0));
		}

		LinearObjectiveFunction objective = new LinearObjectiveFunction(term, 0);
		//LinearProgram lp = new LinearProgram(term);
		ExpressionsBasedModel model = new ExpressionsBasedModel(vars);

		ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
		

		// y == V_kl * x //TODO: Check if that works as intended
		for (int constraintRow = 0; constraintRow < rows; constraintRow++) {
			
			Expression expr = model.addExpression("cEQ_" + constraintRow).lower(0.0).upper(0.0);
			
			double[] constTerm = new double[rows + columns];
			for (int i = 0; i < rows; i++) {
				if (i == constraintRow) {
					constTerm[i] = 1.0;
					expr.set(vars.get(i), 1.0);
				} else {
					constTerm[i] = 0.0;
					expr.set(vars.get(i), 0.0);
				}
			}
			if(initialSeed.contains(constraintRow))
			{
				//System.out.printf("y%d (>=1.0)", constraintRow);	
			}
			else
			{
				//System.out.printf("y%d", constraintRow);
			}
			for (int j = 0; j < columns; j++) {
				
				if(-matrix.get(constraintRow, j) != 0.0)
				{
					constTerm[rows + j] = -matrix.get(constraintRow, j);
					expr.set(vars.get(rows + j), -matrix.get(constraintRow, j));
					
					//System.out.printf(" + %.20f *x%d", -matrix.get(constraintRow, j),j);
				}
				else
				{
					constTerm[rows+j] = 0.0;
					expr.set(vars.get(rows + j), 0.0);
				}
				
			}
			//System.out.println(" == 0");
			constraints.add(new LinearConstraint(constTerm, Relationship.EQ, 0.0));
			//constraints.add(new LinearEqualsConstraint(constTerm, 0.0, "cEQ_" + constraintRow));			
	        
		}

		// y >= 0
		for (int constraintRow = 0; constraintRow < rows; constraintRow++) {
			
			Expression expr = model.addExpression("cGEQ0_" + constraintRow).lower(0.0);
			
			double[] constTerm = new double[rows + columns];
			for (int i = 0; i < rows; i++) {
				if (i == constraintRow) {
					constTerm[i] = 1.0;
					expr.set(vars.get(i), 1.0);
				} else {
					constTerm[i] = 0.0;
					expr.set(vars.get(i), 0.0);
				}
			}
			for (int j = 0; j < columns; j++) {
				constTerm[rows + j] = 0.0;
				expr.set(vars.get(rows + j), 1.0);
			}
			constraints.add(new LinearConstraint(constTerm, Relationship.GEQ, 0.0));
			//constraints.add(new LinearBiggerThanEqualsConstraint(constTerm, 0.0, "cGEQ0_" + constraintRow));
		}

		// x in reasonable boundaries
//		for (int constraintRow = rows; constraintRow < rows + columns; constraintRow++) {
//			double[] constTerm = new double[rows + columns];
//			for (int i = 0; i < rows; i++) {
//				constTerm[i] = 0.0;
//			}
//			for (int j = 0; j < columns; j++) {
//				if(j + rows == constraintRow)
//				{
//					constTerm[rows + j] = 1.0;
//				}
//				else
//				{
//					constTerm[rows + j] = 0.0;
//				}
//			}
//			constraints.add(new LinearConstraint(constTerm, Relationship.GEQ, -Double.MAX_VALUE +1));
//			constraints.add(new LinearConstraint(constTerm, Relationship.LEQ, Double.MAX_VALUE -1 ));
//		}
		
		// y >= 1 , y element of initialSeed
		for (int constraintRow = 0; constraintRow < rows; constraintRow++) {
			
			if (initialSeed.contains(constraintRow)) {
				Expression expr = model.addExpression("cGEQ1_" + constraintRow).lower(1.0);
			
				double[] constTerm = new double[rows + columns];
				for (int i = 0; i < rows; i++) {
					if (i == constraintRow && initialSeed.contains(constraintRow)) {
						constTerm[i] = 1.0;
						expr.set(vars.get(i), 1.0);
					} else {
						constTerm[i] = 0.0;
						expr.set(vars.get(i), 0.0);
					}
				}
				for (int j = 0; j < columns; j++) {
					constTerm[rows + j] = 0.0;
					expr.set(vars.get(rows + j), 0.0);
				}
				
				constraints.add(new LinearConstraint(constTerm, Relationship.GEQ, 1.0));
				//constraints.add(new LinearBiggerThanEqualsConstraint(constTerm, 1.0, "GEQ1_cEq" + constraintRow));			
			}
		}

		// y >= 1+ weightLaterAdded*difference , y element of seed
		for (int constraintRow = 0; constraintRow < rows; constraintRow++) {
			
			if (seed.contains(constraintRow)) {
				Expression expr = model.addExpression("cGEQ1b_" + constraintRow).lower(1.0);
				
				double[] constTerm = new double[rows + columns];
				for (int i = 0; i < rows; i++) {
					if (i == constraintRow && seed.contains(i)) {
						constTerm[i] = 1.0;
						expr.set(vars.get(i), 1.0);
					} else {
						constTerm[i] = 0.0;
						expr.set(vars.get(i), 0.0);
					}
				}
				for (int j = 0; j < columns; j++) {
					constTerm[rows + j] = 0.0;
					expr.set(vars.get(rows + j), 0.0);
				}
				
					constraints.add(new LinearConstraint(constTerm, Relationship.GEQ, 1.0 + weightLaterAdded * difference));
			}
		}

		System.out.println("Begin Norm");

		ArrayList<Double> v = new ArrayList<Double>(rows);
		
		PointValuePair solutionApache = null;
		//double solution = new double[rows];
		Optimisation.Result result;
		try {
			try {
				solutionApache = new SimplexSolver(1.0e-8, 10, 1.0e-15).optimize(objective, new LinearConstraintSet(constraints), GoalType.MINIMIZE, PivotSelectionRule.BLAND, new NonNegativeConstraint(false));
				
				for (int i = 0; i < rows; i++) {
					v.add(i, solutionApache.getPoint()[i]);
				}
			}
			catch(Exception err) //If Apache's SimplexSolver does not find a solution, use Ojalgo's different solvers instead
			{
				result = model.minimise();
				
				for (int i = 0; i < rows; i++) {
					v.add(i, result.doubleValue(i));
				}
			}
			//lp.addConstraints(constraints);
			//lp.setMinProblem(true);
						
			
			//LinearProgramSolver solver = SolverFactory.newDefault(); 
			//solution = solver.solve(lp);
		}
		catch(Exception err) {//NoFeasibleSolutionException err) {
//			for(int i=0; i<matrix.rows(); i++)
//			{
//				System.out.println(i + ": " + String.format("%.50f", matrix.getRow(i).sum()) + " > " + (initialSeed.contains(i) ? 1 : 0) + "\n");
//			}
			//System.out.println(matrix);
			throw err;
		}

		System.out.println("Norm done");

				
		return v;
				
	}

	/**
	 * Finds the first semi-global minimum within a range of 32 starting from entry 0
	 * 
	 * @param sequence A sequence of (conductance) values
	 * @param startIndex The index at which the looked at portion would actually start
	 * @param globalConductance An array of the 30 most recent global conductances
	 * @param iteration The current iteration number of the algorithm
	 * 
	 * @return The global minimum of the sequence
	 */
	public int globalMinimum(ArrayList<Double> sequence, int startIndex, double[] globalConductance, int iteration) {

		int detectedSize = sequence.size();
		int sequenceLength = sequence.size();

		//System.out.println("TO BE INSERTED: " + sequence.get(Math.floorMod(sequenceLength - 2, sequenceLength)));
		//System.out.println("FROM: " + sequence.toString());
		globalConductance[Math.floorMod(iteration, globalConductance.length)] = sequence
				.get(Math.floorMod(sequenceLength - 2, sequenceLength));
		
		for (int x = 0; x < 40; x++) {
			sequence.add(0.0);
		}
		for (int i = 0; i < sequenceLength; i++) {
			if (sequence.get(i) < sequence.get(Math.floorMod(i - 1, sequenceLength))
					&& sequence.get(i) < sequence.get(i + 1)) {
				int countLarger = 0;
				int countSmaller = 0;
				for (int j = 1; j < 32; j++) {
					if (sequence.get(i + 1 + j) > sequence.get(i + 1)) {
						countLarger++;
					}
				}
				for (int k = 1; k < 32; k++) {
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
	 */
	public Map<Integer, Double> seedSetExpansion(Matrix graphAdjacencyMatrix, ArrayList<Integer> seedset,
			int minimumCommunitySize, int maximumCommunitySize, int expansionStepSize, int subspaceDimension, int randomWalkSteps, boolean biased) {

		int n = graphAdjacencyMatrix.rows();

		HashMap<Integer, Integer> degree = new HashMap<Integer, Integer>();
		for (int i = 0; i < n; i++) {
			degree.put(i, (int) (graphAdjacencyMatrix.getRow(i).sum()));
		}

		Vector initialProbability;
		// Random walk starting from seed nodes:
		System.out.println("SEEDS: " + seedset.toString());
		if (biased) {
			initialProbability = setInitialProbabilityProportional(n, degree, seedset);
		} else {
			initialProbability = setInitialProbability(n, seedset);
		}

		System.out.println("Initial walk begin");
		Matrix orthProbMatrix = randomWalk(graphAdjacencyMatrix, initialProbability, subspaceDimension, randomWalkSteps);
		ArrayList<Integer> initialSeed = seedset;
		System.out.println("initial walk end");
		
		// Initialization
		ArrayList<Integer> detected = seedset;
		int rows = orthProbMatrix.rows();
		int columns = orthProbMatrix.columns();
		ArrayList<Integer> seed = seedset;
		int step = expansionStepSize;

		if (maximumCommunitySize > n) {
			maximumCommunitySize = n;
		}
		if (minimumCommunitySize > n) {
			minimumCommunitySize = 1;
		}

		Map<Integer, Double> detectedCommunity = new HashMap<Integer, Double>();

		double globalConductance[] = new double[30];
		globalConductance[29] = Double.MAX_VALUE; // set the last element to be infinitely large
		globalConductance[28] = Double.MAX_VALUE;
		boolean flag = true;

		int iteration = 0;
		while (flag) { // && iteration < 50 && detectedCommunity.size() < rows) {
			System.out.println("Start Norm procedure");
			ArrayList<Double> temp = minimumOneNorm(orthProbMatrix, initialSeed, seed); 
			
			List<Integer> sortedTop = new ArrayList<Integer>(temp.size());

			// TODO: Check if included one too many through new_graph_size
			// Returns the nodes indices of the new_graph_size -many biggest values of
			// sub_prob_distribution in array form
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

			ArrayList<Double> conductanceRecord = new ArrayList<Double>((maximumCommunitySize - minimumCommunitySize + 1));
			for (int i = 0; i <= maximumCommunitySize - minimumCommunitySize; i++) {
				conductanceRecord.add(i, 0.0);
			}

			LEMONArrayListIndexComparator tmpcomparator = new LEMONArrayListIndexComparator(temp);
			ArrayList<Integer> tempIndDesc = comparator.createIndexArrayList();
			Collections.sort(tempIndDesc, tmpcomparator);
			
			for (int i = minimumCommunitySize; i <= maximumCommunitySize; i++) {
				List<Integer> candidateCommunity = new ArrayList<Integer>(maximumCommunitySize);
				if (i < tempIndDesc.size()) {
					candidateCommunity = tempIndDesc.subList(0, i);
				} else {
					candidateCommunity = tempIndDesc;
				}
				conductanceRecord.set(i - minimumCommunitySize, calculateConductance(graphAdjacencyMatrix, candidateCommunity));
			}

			System.out.println("Start minimum");
			int detectedSize = globalMinimum(conductanceRecord, minimumCommunitySize, globalConductance, iteration);
			System.out.println("End minimum");

			step += expansionStepSize;

			if (biased) {
				initialProbability = setInitialProbabilityProportional(n, degree, seedset);
			} else {
				initialProbability = setInitialProbability(graphAdjacencyMatrix.rows(), seedset);
			}

			System.out.println("Random walk begin");
			orthProbMatrix = randomWalk(graphAdjacencyMatrix, initialProbability, subspaceDimension, randomWalkSteps);
			System.out.println("Random walk end");

			if (globalConductance[Math.floorMod(iteration - 1, globalConductance.length)] <= globalConductance[Math
					.floorMod(iteration, globalConductance.length)]
					&& globalConductance[Math.floorMod(iteration - 1,
							globalConductance.length)] <= globalConductance[Math.floorMod(iteration - 2,
									globalConductance.length)]) {
				flag = false;
			}

			if (detectedSize != 0 && flag) {
				for (int i = 0; i < detectedSize; i++) {
					detectedCommunity.put(tempIndDesc.get(i), temp.get(tempIndDesc.get(i)));
				}
			}

			iteration++;
		}

		System.out.println("Done");
		return detectedCommunity;
	}

	// TODO: Maybe implement the score functions
}
