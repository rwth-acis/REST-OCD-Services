package i5.las2peer.services.ocd.algorithms;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

import i5.las2peer.services.ocd.algorithms.utils.CoordinatePoint;
import i5.las2peer.services.ocd.algorithms.utils.EigenPair;
import i5.las2peer.services.ocd.algorithms.utils.EigenPairComparator;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.metrics.OcdMetricException;

import java.util.*;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.FuzzyKMeansClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.la4j.decomposition.EigenDecompositor;

import org.graphstream.graph.Edge;


/**
 * This class holds spectral clustering algorithm which uses fuzzy clustering to
 * detect overlapping communities by Shihua Zhang, Rui-Sheng, Wang and Xiang-Sun Zhang:
 * Identification of overlapping community structure in complex networks using fuzzy c-means clustering
 * https://doi.org/10.1016/j.physa.2006.07.023
 * It also uses modifications based on the paper "A Tutorial on Spectral Clustering" written by Ulrike von Luxburg.
 * The algorithm also has some extra modifications such as giving the user a choice between
 * optimizing the cluster quantity or not, as well as using a custom threshold
 * for creating clusters or alternatively creating clusters through apache commons FuzzyKMeansClusterer class
 *
 */
public class FuzzyCMeansSpectralClusteringAlgorithm implements OcdAlgorithm {
	
	/**
	 * Maximum number of clusters the algorithm should consider
	 */
	private int k = 20;
	

	/**
	 * True if it is desired to check modularity function for each cluster size
	 * between 2 and K and choose the best K. This is more time consuming but
	 * provides better results.
	 */
	private boolean optimizeClusterQuantity = true;
	
	/**
	 * Degree of cluster fuzziness, which should be at least 1, as described on https://home.apache.org/~luc/commons-math-3.6-RC2-site/jacoco/org.apache.commons.math3.ml.clustering/FuzzyKMeansClusterer.java.html
	 */
	private double fuzziness = 1.1;

	/**
	 * Custom threshold value to determine membership within a cluster. If this value is 0, then the chosen clusters will be automatic and independent of this threshold value. If this value is set to > 0 it will be used to build the membership matrix based on the belongingness factors. In case this value is set, it should be between 0 and 1
	 */
	private double customThreshold = 0.1;
	
	/**
	 * Convergence criteria for clustering algorithm as described in https://home.apache.org/~luc/commons-math-3.6-RC2-site/jacoco/org.apache.commons.math3.ml.clustering/FuzzyKMeansClusterer.java.html
	 */
	private double epsilon = 1e-3;
	
	/**
	 * Maximum number of iterations for the clustering algorithm as described in https://home.apache.org/~luc/commons-math-3.6-RC2-site/jacoco/org.apache.commons.math3.ml.clustering/FuzzyKMeansClusterer.java.html
	 */
	private int maxIterations = -1;
	
	
	/*
	 * PARAMETER NAMES
	 */
	public static final String K_NAME = "k";
	
	public static final String OPTIMIZE_CLUSTER_QUANTITY_NAME = "optimizeClusterQuantity";

	public static final String FUZZINESS_NAME = "fuzziness";

	public static final String CUSTOM_THRESHOLD_NAME = "customThreshold";

	public static final String EPSILON_NAME = "epsilon";

	public static final String MAX_ITERATIONS_NAME = "maxIterations";
	
	
	/**
	 * Default constructor that returns algorithm instance with default parameter values
	 */
	public FuzzyCMeansSpectralClusteringAlgorithm() {
		
	}
	

	/**
	 * Setter for the algorithm parameters
	 */
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		
		if (parameters.containsKey(K_NAME)) {
			k = Integer.parseInt(parameters.get(K_NAME));
			if (k < 2) {
				throw new IllegalArgumentException("k should be at least 2");
			}
			parameters.remove(K_NAME);
		}
		
		if (parameters.containsKey(OPTIMIZE_CLUSTER_QUANTITY_NAME)) {
			optimizeClusterQuantity = Boolean.parseBoolean(parameters.get(OPTIMIZE_CLUSTER_QUANTITY_NAME));
			parameters.remove(OPTIMIZE_CLUSTER_QUANTITY_NAME);
		}
		
		if (parameters.containsKey(FUZZINESS_NAME)) {
			fuzziness = Double.parseDouble(parameters.get(FUZZINESS_NAME));
			if (fuzziness < 1) {
				throw new IllegalArgumentException("Fuzziness factor should be at least 1!");
			}
			parameters.remove(FUZZINESS_NAME);
		}
		
		if (parameters.containsKey(CUSTOM_THRESHOLD_NAME)) {
			customThreshold =  Double.parseDouble(parameters.get(CUSTOM_THRESHOLD_NAME));
			parameters.remove(CUSTOM_THRESHOLD_NAME);
		}
		
		if (parameters.containsKey(EPSILON_NAME)) {
			epsilon = Double.parseDouble(parameters.get(EPSILON_NAME));
			parameters.remove(EPSILON_NAME);
		}
		
		if (parameters.containsKey(MAX_ITERATIONS_NAME)) {
			maxIterations = Integer.parseInt(parameters.get(MAX_ITERATIONS_NAME));
			parameters.remove(MAX_ITERATIONS_NAME);
		}
		
		if (parameters.size() > 0) {
			throw new IllegalArgumentException("Too many input parameters!");
		}
		
	}
	

	/**
	 * Getter for the algorithm parameters
	 */
	@Override
	public Map<String, String> getParameters() {
		
		Map<String, String> parameters = new HashMap<String, String>();
		
		parameters.put(K_NAME, Integer.toString(k));
		parameters.put(OPTIMIZE_CLUSTER_QUANTITY_NAME, Boolean.toString(optimizeClusterQuantity));
		parameters.put(FUZZINESS_NAME, Double.toString(fuzziness));
		parameters.put(CUSTOM_THRESHOLD_NAME, Double.toString(customThreshold));
		parameters.put(EPSILON_NAME, Double.toString(epsilon));
		parameters.put(MAX_ITERATIONS_NAME, Integer.toString(maxIterations));
		
		return parameters;
	}
	

	/**
	 * The main algorithm method which uses helper submethod and returns a Cover representing communities
	 */
	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph)
			throws OcdAlgorithmException, InterruptedException, OcdMetricException {
		
		Cover resulting_cover = null; // output cover will be put here
		
		// Create Matrix weighted adjacency matrix A
		Matrix A = createAdjacencyMatrix(graph);

	
		// create diagonal matrix
		Matrix D = createDiagonalMatrix(A);


		// create Laplacian Matrix
		Matrix L = createLaplacianMatrix(D, A);


		// create L_rw matrix as described in Tutorial on spectral clustering paper
		Matrix L_rw = findL_rw(L, D);


		// find eigenvectors and eigenvalues
		Matrix[] eigenvectors_eigenvalues = decompose(L_rw);

		// sort eigenvalue columns, and their corresponding eigenvectors
		Matrix[] sorted_eigenvectors_eigenvalues = sortEigenPairs(eigenvectors_eigenvalues);


		if (graph.getNodeCount() < 2) {
			throw new OcdAlgorithmException("Graph should have at least two nodes");
		}
		// this condition is needed to avoid a null pointer exception caused when there are
		// not enough nodes in the graph compared to the specified K.
		if(k > graph.getNodeCount()){
			k = graph.getNodeCount();
		}
		int optimal_K = k;

		double modularity = -100000.0; // initially modularity value is set to unrealisticly low number
		double new_modularity = -100000.0;
		Matrix membership_matrix = null; // this will hold membership matrix based on which the cover will be created
		Matrix new_membership_matrix = null; // this is a helper variable to update membership matrix if clusters are optimized
		
		/*
		 * if custom threshold was specified, then clusters will be built based on this
		 * threshold using membership matrix (but not clusters) that was found using FuzzyKmeansClusterer from Apache commons
		 * (https://home.apache.org/~luc/commons-math-3.6-RC2-site/jacoco/org.apache.commons.math3.ml.clustering/FuzzyKMeansClusterer.java.html)
		 */
		if (customThreshold > 0.0) {

			if (optimizeClusterQuantity == true) {

				// If cluster quantity optimization is desired, try out different K values to
				// find the one that results in the highest modularity
				for (int i = 2; i <= k; i++) {

					if (Thread.interrupted()) {
						throw new InterruptedException();
					}

					FuzzyKMeansClusterer<CoordinatePoint> clusterer = getClustererOutput(
							sorted_eigenvectors_eigenvalues[0], i, fuzziness);
					
					new_membership_matrix = realMatrixToMatrix(clusterer.getMembershipMatrix());

					new_modularity = modularityFunction(new_membership_matrix, A, customThreshold);

//					System.out.println("modularity with K = " + i + " and custom threshold = " + customThreshold
//							+ " :   " + new_modularity);
//					System.out.println();

					// if modularity of some cluster size is better that modularity value of cluster
					// sizes seen before, set that cluster size as optimal
					if (new_modularity > modularity) {

						membership_matrix = new_membership_matrix;
						modularity = new_modularity;
						optimal_K = i;
					}
				}
			} else {
				// if cluster quantity optimizaiton is not desired, only find the memberhsip matrix and modularity for one K
				optimal_K = k;

				FuzzyKMeansClusterer<CoordinatePoint> clusterer = getClustererOutput(sorted_eigenvectors_eigenvalues[0],
						k, fuzziness);
				membership_matrix = realMatrixToMatrix(
						getClustererOutput(sorted_eigenvectors_eigenvalues[0], k, fuzziness).getMembershipMatrix());

				modularity = modularityFunction(membership_matrix, A, customThreshold); 
				
//				System.out.println("modularity without optimizing cluster quantity and with K = " + K
//						+ " and custom threshold = " + customThreshold + " is  " + modularity);

			}

			/*
			 * if threshold is 0 then the communities found will not be overlapping and they
			 * will be built using FuzzyKMeansClusterer from Apache commons which can be found at
			 * (https://home.apache.org/~luc/commons-math-3.6-RC2-site/jacoco/org.apache.commons.math3.ml.clustering/FuzzyKMeansClusterer.java.html)
			 */

		} else {

			if (optimizeClusterQuantity == true) {

				// If cluster quantity optimization is desired, try out different K values to
				// find the one that results in the highest modularity function output
				for (int i = 2; i <= k; i++) {

					if (Thread.interrupted()) {
						throw new InterruptedException();
					}

					// Get clusterer which holds the membership matrix with belongingness values
					FuzzyKMeansClusterer<CoordinatePoint> clusterer = getClustererOutput(
							sorted_eigenvectors_eigenvalues[0], i, fuzziness);

					// fill membership matrix by clusters chosen by the clusterer (since threshold
					// is 0.0 if code is here)
					new_membership_matrix = getMembershipMatrixFromClusters(clusterer);

					// Calculate modularity function output for the clusters found above
					new_modularity = modularityFunction(new_membership_matrix, A);

//					System.out.println("modularity with K = " + i + " is " + new_modularity);
//					System.out.println();

					// if modularity of some cluster size is better than modularity value of cluster
					// sizes seen before, set that cluster size as optimal
					if (new_modularity > modularity) {

						membership_matrix = new_membership_matrix;
						modularity = new_modularity;
						optimal_K = i;
					}
				}
				
			} else {
				
				// if cluster quantity optimizaiton is not desired, only find the memberhsip
				// matrix and modularity for one K

				FuzzyKMeansClusterer<CoordinatePoint> clusterer = getClustererOutput(sorted_eigenvectors_eigenvalues[0], k, fuzziness);
				
				membership_matrix = getMembershipMatrixFromClusters(clusterer);
				
				modularity = modularityFunction(membership_matrix, A);
				
//				System.out.println("modularity without optimizing cluster quantity and K = " + K + " is  " + modularity);

			}
			
			

		}

		// build the cover using the input graph and the membership matrix built above
	    resulting_cover = new Cover(graph,membership_matrix); 

//		System.out.println("================== CHOSEN SOLUTION =====================");
//		System.out.println("Optimal K is: " + optimal_K + " optimal modularity is: " + modularity);

		
		return resulting_cover;
	}
	
	

	@Override
	public CoverCreationType getAlgorithmType() {
		
		return CoverCreationType.FUZZY_C_MEANS_SPECTRAL_CLUSTERING_ALGORITHM;
		
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		
		compatibilities.add(GraphType.WEIGHTED);
	
		return compatibilities;
		
	}
	

	/**
	 * This method creates Adjacency matrix that also holds edge weights. If entry
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
	 * This method creates a diagonal matrix with all entries 0 except for the
	 * diagonal entries, which hold the sum of weights of edges for each node
	 * 
	 * @param A     Adjacency matrix based on which the diagonal matrix should be created
	 * @return      Created diagonal matrix 
	 */
	public Matrix createDiagonalMatrix(Matrix A) {
		
		Matrix D = new Basic2DMatrix(A.rows(), A.columns());
		
		for (int i = 0; i < A.rows(); i++) {
			
			D.set(i, i, A.getRow(i).sum()); // set diagonal entries of D to sums of rows of A
			
		}
		
		return D;
		
	}
	
	/**
	 * This method creates Laplacian Matrix L defined as L = D - A
	 * @param D     Diagonal Matrix
	 * @param A     Adjacency Matrix
	 * @return      Laplacian Matrix
	 */
	public Matrix createLaplacianMatrix(Matrix D, Matrix A) {
		
		Matrix L = new Basic2DMatrix(A.rows(), A.columns());
		
		L = D.subtract(A);
		
		return L;
		
	}
	
	
	/**
	 * This methods finds D^-1
	 * @param D     Diagonal Matrix
	 * @return      Matrix representing D^-1
	 */
	public Matrix findInverse(Matrix D) {
	
		
		Matrix D_inv = new Basic2DMatrix(D.rows(), D.columns());
		D_inv = D_inv.blank();
		
		// -1 power of diagonal matrix is diagonal entries replaced with their reciprocal
		for(int i  = 0; i < D.rows(); i++) {
			
			// Matrix is symmetric and only has entries on a diagonal, so we can use 1 dimension to save computational time
			double curr = D.get(i, i);
			
			if (curr != 0) {
				
				D_inv.set(i, i, (1 / curr));
				
			}
		}
		
		return D_inv;
	}
	

	/**
	 * This method finds L_rw as described in "A Tutorial on Spectral Clustering" paper by Ulrike von Luxburg
	 * @param L     Laplacian Matrix
	 * @param D     Diagonal Matrix
	 * @return      L_rw
	 */
	public Matrix findL_rw(Matrix L, Matrix D) {
		
		return (findInverse(D)).multiply(L);
		
	}
	
	
	/**
	 * This method returns a Matrix array with two entries, first is the matrix of
	 * eigenvectors, second is the diagonal matrix of corresponding eigenvalues
	 * 
	 * @param L_rw Matrix to decompose
	 * @return Decomposition of input matrix into its eigenvectors and eigenvalues
	 */
	public Matrix[] decompose(Matrix L_rw) {
		
		EigenDecompositor eigen_decompositor = new EigenDecompositor(L_rw);
		
		return eigen_decompositor.decompose();
		
	}
	
	
	/**
	 * This method sorts eigendecomposition output to easily access first K
	 * eigenvectors (and corresponding eigenvalues)
	 * 
	 * @param eigenvectors_eigenvalues Matrix array with two entries, first is the
	 *                                 matrix of eigenvectors, second is the
	 *                                 diagonal matrix of corresponding eigenvalues
	 * @return sorted Matrix array holding eigenvectors and eigenvalus
	 */
	public Matrix[] sortEigenPairs(Matrix[] eigenvectors_eigenvalues) {

		Matrix[] res = new Matrix[2];
		
		ArrayList<EigenPair> eigenPairs = new ArrayList<EigenPair>();
		
		for (int i = 0; i < eigenvectors_eigenvalues[0].columns(); i++) {

			// fill in pairs of eigenvalues and corresponding eigenvectors
			eigenPairs.add(
					new EigenPair(eigenvectors_eigenvalues[1].get(i, i), eigenvectors_eigenvalues[0].getColumn(i)));

		}
		
		// sort (EigenPairComparator sorts based on eigenvalues)
		Collections.sort(eigenPairs, new EigenPairComparator()); 

		// eigenvectors corresponding to sorted eigenvalues will be stored here
		Matrix sorted_eigenvectors = new Basic2DMatrix(eigenPairs.size(), eigenPairs.size());
		sorted_eigenvectors = sorted_eigenvectors.blank();
		
		// sorted eigenvalues will be stored here
		Matrix sorted_eigenvalues = new Basic2DMatrix(eigenPairs.size(), eigenPairs.size());
		sorted_eigenvalues = sorted_eigenvalues.blank();
		
		for (int i = 0; i < eigenPairs.size(); i++) {

			EigenPair current = eigenPairs.get(i);

			Vector v = new BasicVector(current.getEigenVector());

			sorted_eigenvectors.setColumn(i, v);

			sorted_eigenvalues.set(i, i, current.getEigenValue());

		}
		
		res[0] = sorted_eigenvectors;
		res[1] = sorted_eigenvalues;
		
		return res;
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
	

	/**
	 * This method FuzzyKMeansClsuter, which can be used to get either the membership matrix to be used with custom threshold or to get clusters that were built without custom threshold when customTreshold = 0.0;
	 * @param M          Matrix of sorted eigenvectors
	 * @param K          Cluster size
	 * @param fuzziness  Degree of fuzziness
	 * @return           FuzzyKMeansClsuter that holds clustering results
	 */
	public FuzzyKMeansClusterer<CoordinatePoint> getClustererOutput(Matrix M, int K, double fuzziness) {
		
		ArrayList<ArrayList<Double>> points = new ArrayList<ArrayList<Double>>();

		// for each node, create array which holds row entries which will be used to represent the coordinate of the row (node)
		for (int i = 0; i < M.rows(); i++) {
			
			points.add(new ArrayList<Double>());

			for (int j = 0; j < K; j++) {
				
				points.get(i).add(j, M.get(i, j));

			}
		}
		
		
		ArrayList<CoordinatePoint> coordinate_points = new ArrayList<CoordinatePoint>();
		
		// For each row, create a CoordinatePoint representing this row (node)
		for (int i = 0; i < points.size(); i++) {
			
			coordinate_points.add(new CoordinatePoint(points.get(i), i));  // coordinate point Id will match node id for easy access
			
		}
		
		// cluster the coordinate points representing nodes
		FuzzyKMeansClusterer<CoordinatePoint> clusterer = new FuzzyKMeansClusterer<CoordinatePoint>(K, fuzziness,
				maxIterations, new EuclideanDistance(), epsilon, new JDKRandomGenerator());

		clusterer.cluster(coordinate_points);
		
		return clusterer;
		
	}

	
	/**
	 * This method converts output clusters from FuzzyKMeansClusterer into a membership matrix of type Matrix
	 * @param clusterer     FuzzyKMeansClusterer that holds chosen clusters
	 * @return              Matrix representing memberships to different clusters
	 */
	public Matrix getMembershipMatrixFromClusters(FuzzyKMeansClusterer<CoordinatePoint> clusterer) {
		


		int cluster_index = 0; // this variable will be used to easily distinguish/access different clusters
		
		Matrix membership_matrix = new Basic2DMatrix(clusterer.getDataPoints().size(), clusterer.getClusters().size());
		
		/*
		 * For each cluster in FuzzyKMeansClusterer output, convert the elements of the
		 * cluster to CoordinatePoints and build membership_matrix based on each
		 * CoordinatePoint Id which corresponds to the node (node index)
		 */
		for (CentroidCluster<CoordinatePoint> cluster : clusterer.getClusters()) {
			
			for (CoordinatePoint p : cluster.getPoints()) {
				
				membership_matrix.set(p.getId(), cluster_index, 1);
				
			}
			
			cluster_index++;
			
		}
			
		return membership_matrix;

	}
	
	
	/**
	 * This method calculates A(~V_c, ~V_c) as described in the paper
	 * "Identification of overlapping community structure in complex networks using
	 * fuzzy c-means clustering.
	 * 
	 * @param binary_cluster_membership_vector binary membership vector for some community
	 * @param cluster_belongingness_vector     vector holding belongingness values [0,1] for some community                                        
	 * @param Weights                          Matrix holding weights for edges between all nodes                                    
	 * @return                                 Value representing A(~V_c,~V_c) as described in Fuzzy C means paper
	 */
	public double inClusterDensity(Vector binary_cluster_membership_vector, Vector cluster_belongingness_vector, Matrix Weights) {
		
		double density = 0.0;
		
		ArrayList<Integer> in_cluster_node_indices = new ArrayList<Integer>(); // this will hold indices of the nodes that belong to the cluster
		
		/*
		 * purpose of this loop is to get indices of nodes in the cluster based on membership
		 * vector, in order to reduce computational efficiency and avoid looking at a
		 * whole matrix
		 */
		for (int i = 0; i < binary_cluster_membership_vector.length(); i++) {
			
			if (binary_cluster_membership_vector.get(i) > 0) {
				
				in_cluster_node_indices.add(i);
				
			}
			
		}
		
		// find (u_ic + u_jc)/2 * w(i,j)  
		for (int i = 0; i < in_cluster_node_indices.size(); i++) {
			
			for (int j = 0; j < in_cluster_node_indices.size(); j++) {

				if (i != j) { // no self edges

					int index_i = in_cluster_node_indices.get(i);
					int index_j = in_cluster_node_indices.get(j);

					if (Weights.get(index_i, index_j) > 0) { // if weight is 0, then product will 0 anyway

						density += ((cluster_belongingness_vector.get(index_i)
								+ cluster_belongingness_vector.get(index_j)) / 2) * Weights.get(index_i, index_j);

					}
				}
			}
		}	
		
		return density;
		
	}
	
	
	/**
	 * This method simply returns weights of all entries of a matrix, which is
	 * A(V,V) in Fuzzy C means paper
	 * 
	 * @param      Weights Weight Matrix
	 * @return     Sum of matrix weights
	 */
	public double wholeGraphWeights(Matrix Weights) {
		
		return Weights.sum();
		
	}
	

	/**
	 * This method finds density between a given cluster and the rest of the graph
	 * @param binary_cluster_membership_vector     Binary membership vector for some community
	 * @param cluster_belongingness_vector         Vector holding belongingness values [0,1] for some community
	 * @param Weights                              Matrix holding weights for edges between all nodes
	 * @return                                     Value representing A(~V_c,V_c) as described in Fuzzy C means paper
	 */
	public double inOutClusterDensity(Vector binary_cluster_membership_vector, Vector cluster_belongingness_vector, Matrix Weights) {
		
		double density = 0;
		
		double in_cluster_density = inClusterDensity(binary_cluster_membership_vector, cluster_belongingness_vector, Weights); // density within the cluster
		
		ArrayList<Integer> in_cluster_node_indices = new ArrayList<Integer>(); // this will hold indices of the nodes that belong to the cluster
		ArrayList<Integer> out_cluster_node_indices = new ArrayList<Integer>(); // this will hold indices of the nodes that don't belong to the cluster
		
		for (int i = 0; i < binary_cluster_membership_vector.length(); i++) {
			
			// if cluster_membership_vector entry for a node is > 0 then the node belong to the cluster, otherwise it doesn't
			if (binary_cluster_membership_vector.get(i) > 0) {
				
				in_cluster_node_indices.add(i);
				
			} else {
				
				out_cluster_node_indices.add(i);
				
			}
		}
		
		/*
		 * find density between the cluster and the outside of the cluster. 
		 * ((u_ic + (1- u_jc) / 2) * w(i,j) in Fuzzy C Means paper
		 */
		for (int i = 0; i < in_cluster_node_indices.size(); i++) {
			
			for (int j = 0; j < out_cluster_node_indices.size(); j++) {
				
				int index_i = in_cluster_node_indices.get(i);
				int index_j = out_cluster_node_indices.get(j);
				
				if (Weights.get(index_i, index_j) > 0) { // if weight is 0, then product will 0 anyway

					density += ((cluster_belongingness_vector.get(index_i)
							+ (1 - cluster_belongingness_vector.get(index_j))) / 2) * Weights.get(index_i, index_j);

				}						
			}
		}
		
		density += in_cluster_density;
		return density;
		
	}
	
	
	/**
	 * This method finds binary membership matrix that can have overlapping nodes
	 * @param membership_matrix     Matrix with belongingness values for each node (row) based on which binary membership matrix will be built
	 * @param threshold             Minimum belongingness value for a node to be considered part of the community
	 * @return                      Binary matrix representing membership for each node (row) and community (column) 
	 */
	public Matrix findBinaryOverlappingMembership(Matrix membership_matrix, double threshold) {
		
		Matrix binary_node_membership = new Basic2DMatrix(membership_matrix.rows(), membership_matrix.columns()); // will hold binary membership of each node. Nodes are based on index
		binary_node_membership = binary_node_membership.blank();
		
		// set binary_node_membership[i][j] = 1 if node i belongs to community j
		for (int i = 0; i < membership_matrix.rows(); i++) {

			for (int j = 0; j < membership_matrix.columns(); j++) {
			
				if (membership_matrix.get(i, j) >= threshold) {
					
					binary_node_membership.set(i, j, 1);
					
				}
			}
		}

		return binary_node_membership;
		
	}
	
	
	/**
	 * This method is used to convert RealMatrix to equivalent Matrix, which holds
	 * community belongingness values for each node. The reason for this method is
	 * that to instantiate a Cover, one needs Matrix type
	 * 
	 * @param      membership_matrix RealMatrix with community belongingness values for each node                       
	 * @return     Matrix with community belongingness values for each node
	 */
	public Matrix realMatrixToMatrix(RealMatrix membership_matrix) {
		

		Matrix overlapping_membership = new Basic2DMatrix(membership_matrix.getRowDimension(), membership_matrix.getColumnDimension()); // will hold membership of each node. Nodes are based on index
		overlapping_membership = overlapping_membership.blank();
		
		// set overlapping_membership values to equivalent belongingness values in membership_matrix 
		for (int i = 0; i < membership_matrix.getRowDimension(); i++) {
			
			for (int j = 0; j < membership_matrix.getColumnDimension(); j++) {
				
				overlapping_membership.set(i, j, membership_matrix.getEntry(i, j));
				
			}
		}

		return overlapping_membership;
		
	}
	
	
	/**
	 * This method calculates modularity function ~Q(U_k) as described in Fuzzy C means paper
	 * @param membership_matrix     Matrix holding belongingness values for each node and cluster
	 * @param Weights               Matrix holding edge weights for each edge of the graph
	 * @param threshold             Threshold for node belongingness value for a node to be considered part of the community
	 * @return                      double value representing modularity, should be in range [0,1] with 0 and 1 being trivial cases
	 */
	public double modularityFunction(Matrix membership_matrix, Matrix Weights, double threshold) {
		
		double modular_function_value = 0;  // this will hold ~Q(U_k) as described in fuzzy C means paper

		Matrix binary_node_membership = findBinaryOverlappingMembership(membership_matrix, threshold);
	
		for (int i = 0; i < binary_node_membership.columns(); i++) {
			
			modular_function_value += ( (this.inClusterDensity(binary_node_membership.getColumn(i), new BasicVector(membership_matrix.getColumn(i)), Weights) 
					/ this.wholeGraphWeights(Weights) )
					- (Math.pow( (this.inOutClusterDensity(binary_node_membership.getColumn(i), new BasicVector(membership_matrix.getColumn(i)), Weights) 
							/ this.wholeGraphWeights(Weights) ), 2 ) ));
			
		}
		
		return modular_function_value;
		
	}
	
	
	/**
	 * This method calculates modularity function value, but instead of using
	 * customThreshold to build membership matrix, it uses binary membership matrix
	 * built using belongingness values provided by the clusterer from
	 * getClustererOutput method
	 * 
	 * @param membership_matrix     Binary matrix holding node membership info 
	 * @param Weights               Weights of the edges between the nodes
	 * @return                      Value representing modularity 
	 */
	public double modularityFunction(Matrix membership_matrix, Matrix Weights) {
		
		double modular_function_value = 0;  // this will hold ~Q(U_k) as described in fuzzy C means paper
		
		Matrix binary_node_membership = membership_matrix;

		for (int i = 0; i < binary_node_membership.columns(); i++) {
			
			modular_function_value += ( (this.inClusterDensity(binary_node_membership.getColumn(i), new BasicVector(membership_matrix.getColumn(i)), Weights) 
					/ this.wholeGraphWeights(Weights) )
					- (Math.pow( (this.inOutClusterDensity(binary_node_membership.getColumn(i), new BasicVector(membership_matrix.getColumn(i)), Weights) 
							/ this.wholeGraphWeights(Weights) ), 2 ) ));
		
		}
			
		return modular_function_value;
		
	}

	
}
