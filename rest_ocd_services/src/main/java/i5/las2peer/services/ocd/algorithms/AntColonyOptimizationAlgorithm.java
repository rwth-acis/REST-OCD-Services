package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.metrics.ModularityMetric;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.algorithms.utils.MaximalCliqueSearch;
import i5.las2peer.services.ocd.algorithms.utils.Ant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import java.lang.Double; 
import java.lang.Math;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

/**
* The original version of the overlapping community detection algorithm introduced in 2020 by Ping Ji, Shanxin Zhang, Zhiping Zhou:
* Overlapping  community detection based on maximal clique and multi-objective ant colony optimization
* https://doi.org/10.1109/CCDC49329.2020.9164506
* @author Marlene Damm
*/
//TODO description of the algorithm
public class AntColonyOptimizationAlgorithm implements OcdAlgorithm {
	
	
	private static int maxIterations = 5;
	
	/**
	 * maximal clique encoding. the integer represents the number of the clique and the Hashset stores the 
	 * clique members
	 */
	private HashMap<Integer, HashSet<Node>> maxClq;
	
	/**
	 * number of ants/subproblems to solve. Must be at least 2 (Otherwise it will result in a division by 0). 
	 */
	private static int numberOfAnts = 2;
	
	/**
	 * Number of groups to cluster the ants in. The value should be in between 2 and M. Must be at least 2 
	 */
	private static int numberOfGroups = 2;
	  
	/**
	 * Rate of the pheromone persistence
	 */
	private static double persistenceFactor = 0.8;

	/**
	 * Contains pheromones matrix of each group of ants to get hold of the current pheromones in the graph. 
	 * Each cell of the matrix stands for an edge. The higher the pheromone concentration the more likely it will be 
	 * that an ant visits this edge.   
	 */
	private List<Matrix> pheromones; 
	
	/**
	 * initial pheromone level
	 */
	private static int initialPheromones = 100; 
	  
	/**
	 * saves all best found community solutions (Pareto-Front)
	 */
	private HashMap<Vector, Vector> EP;
	
	/**
	 * Heuristic information matrix: shows how similar to nodes. Nodes which are more similar are more likely to be in 
	 * the same community. The values are between 0 and 1 which 0 being not connected and 1 being very similar. 
	 */
	private Matrix heuristic; 
	
	/**
	* The number of nearest neighbors considered in a neighborhood
	*/
	private static int numberOfNeighbors = 1;
	
	/**
	 * reference point for the minimal objective function values found so far 
	 */
	private static Vector refPoint;
	
	/**
	 * This treshold is used to speed up the Used in update and has to be between 0 and 1
	 */
	private static double R = 0.2;
	
	/*
	 * PARAMETER NAMES
	 */
	public static final String MAX_ITERATIONS_NAME = "maxIterations";

	public static final String NUMBER_OF_ANTS_NAME = "numberOfAnts";

	public static final String PERSISTENCE_FACTOR_NAME = "persistenceFactor";

	public static final String NUMBER_OF_NEIGHBORS_NAME = "numberOfNeighbors";

	public static final String INITIAL_PHEROMONES_NAME = "initialPheromones";

	public static final String NUMBER_OF_GROUPS_NAME = "numberOfGroups";
	
	public AntColonyOptimizationAlgorithm() {}
	
	// --------------------------------------------------------------------------------------------------------------------------------------------------
	// override important methods from the parent class
	// --------------------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Returns all graph types the algorithm is compatible with.
	 * @return The compatible graph types.
	 * An empty set if the algorithm is not compatible with any type.
	 */
	public Set<GraphType> compatibleGraphTypes(){
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.ZERO_WEIGHTS);
		compatibilities.add(GraphType.SELF_LOOPS);
		return compatibilities;
	};	
	
	@Override
	public Map<String,String> getParameters(){
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(MAX_ITERATIONS_NAME, Integer.toString(maxIterations));
		parameters.put(NUMBER_OF_ANTS_NAME, Integer.toString(numberOfAnts));
		parameters.put(PERSISTENCE_FACTOR_NAME, Double.toString(persistenceFactor));
		parameters.put(NUMBER_OF_NEIGHBORS_NAME, Integer.toString(numberOfNeighbors));
		parameters.put(INITIAL_PHEROMONES_NAME, Integer.toString(initialPheromones));
		parameters.put(NUMBER_OF_GROUPS_NAME, Integer.toString(numberOfGroups));
		return parameters;
	}
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.containsKey(MAX_ITERATIONS_NAME)) {
			maxIterations = Integer.parseInt(parameters.get(MAX_ITERATIONS_NAME));
			if(maxIterations <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(MAX_ITERATIONS_NAME);
		}
		if(parameters.containsKey(NUMBER_OF_ANTS_NAME)) {
			numberOfAnts = Integer.parseInt(parameters.get(NUMBER_OF_ANTS_NAME));
			if(numberOfAnts <= 1) {
				throw new IllegalArgumentException();
			}
			parameters.remove(NUMBER_OF_ANTS_NAME);
		}
		if(parameters.containsKey(PERSISTENCE_FACTOR_NAME)) {
			persistenceFactor = Double.parseDouble(parameters.get(PERSISTENCE_FACTOR_NAME));
			if(persistenceFactor < 0 || persistenceFactor > 1) {
				throw new IllegalArgumentException();
			}
			parameters.remove(PERSISTENCE_FACTOR_NAME);
		}
		
		if(parameters.containsKey(NUMBER_OF_NEIGHBORS_NAME)) {
			numberOfNeighbors = Integer.parseInt(parameters.get(NUMBER_OF_NEIGHBORS_NAME));
			if(numberOfNeighbors < 1 || numberOfNeighbors > numberOfAnts) {
				throw new IllegalArgumentException();
			}
			parameters.remove(NUMBER_OF_NEIGHBORS_NAME);
		}
	
		if(parameters.containsKey(INITIAL_PHEROMONES_NAME)) {
			initialPheromones = Integer.parseInt(parameters.get(INITIAL_PHEROMONES_NAME));
			if(initialPheromones < 100) {
				throw new IllegalArgumentException();
			}
			parameters.remove(INITIAL_PHEROMONES_NAME);
		}
		
		if(parameters.containsKey(NUMBER_OF_GROUPS_NAME)) {
			numberOfGroups = Integer.parseInt(parameters.get(NUMBER_OF_GROUPS_NAME));
			if(numberOfGroups < 1) {
				throw new IllegalArgumentException();
			}
			parameters.remove(NUMBER_OF_GROUPS_NAME);
		}
		
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Returns a log representing the concrete algorithm execution.
	 * @return The log.
	 */
	@Override
	public CoverCreationType getAlgorithmType(){
		return CoverCreationType.ANT_COLONY_OPTIMIZATION;
	}
	
	
	/**
	 * Executes the algorithm on a connected graph.
	 * Implementations of this method should allow to be interrupted.
	 * I.e. they should periodically check the thread for interrupts (/TODO)
	 * and throw an InterruptedException if an interrupt was detected.
	 * @param graph An at least weakly connected graph whose community structure will be detected.
	 * @return A cover for the input graph containing the community structure.
	 * @throws OcdAlgorithmException If the execution failed.
	 * @throws InterruptedException If the executing thread was interrupted.
	 * @throws OcdMetricException If the metric execution failed.
	 */
	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException, OcdMetricException {
		// construct the maximal clique graph and initialize the parameters
		CustomGraph MCR = representationScheme(graph);
		int nodeNr = MCR.getNodeCount();
		List<Ant> ants = initialization(MCR, nodeNr);
		
		for(int i = 0; i < maxIterations; i++) {// constructions of the Pareto Front (Pareto optimal solutions) 
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			update(MCR, ants, nodeNr);	
		}
		
		
		return decodeMaximalCliques(graph, nodeNr);	
		
	}
	
// --------------------------------------------------------------------------------------------------------------------------------------------------
// preparations Maximal Clique Graph construction and initialization of the parameters
// --------------------------------------------------------------------------------------------------------------------------------------------------

	/**
 	* The Representation Scheme of this algorithm is a Maximal Clique Scheme. This is done to have a less complex community search process, since cliques in the 
	 * original graph tend to be in the same community anyways. After the search for maximal cliques, inter-clique edges are filtered out if the cliques are loosely
	 * connected.
	 * @param graph to make an Maximal Clique Graph from
	 * @return encoded input graph
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	protected CustomGraph representationScheme(CustomGraph graph) throws InterruptedException {
		// maximal clique search 
		MaximalCliqueSearch MCR = new MaximalCliqueSearch();
		maxClq = MCR.cliques(graph);
			
		// determining the link strength in between the cliques
		Matrix lkstrgth = linkStrength(graph, maxClq);
		double wtr = lkstrgth.sum()/(lkstrgth.rows()*(lkstrgth.columns())); // threshold to filter out weak links
		
		//creating the encoding
		int nodes = maxClq.size(); 
		CustomGraph encoding = new CustomGraph(); 
		for(int i = 0; i < nodes; i++) {//creating clique nodes
				encoding.addNode(Integer.toString(i));
		}
		for(Node n1:  encoding.nodes().toArray(Node[]::new)) { // creating clique edges
			int i1 = n1.getIndex();
			for(Node n2:  encoding.nodes().toArray(Node[]::new)) {
				int i2 = n2.getIndex();
				double ls = lkstrgth.get(i1, i2);
				if(ls>=wtr) { // filter out weak edges
					Edge e1 = encoding.addEdge(UUID.randomUUID().toString(),n1, n2);
					Edge e2 = encoding.addEdge(UUID.randomUUID().toString(),n2, n1);
					encoding.setEdgeWeight(e1, ls);
					encoding.setEdgeWeight(e2, ls);
				}
			}
		}
		return encoding; 
	}
	
	/** Measures the link strength in between the maximal cliques. 
	 * 
	 * @param graph Original undirected graph
	 * @param maxClq output of the MaximalCliqueGraphRepresentation
	 * @return Matrix of link strength of the clique edges
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	protected Matrix linkStrength(CustomGraph graph, HashMap<Integer,HashSet<Node>> maxClq) throws InterruptedException {
		int clqNr = maxClq.size(); 
		Matrix lkstrgth = new Basic2DMatrix(clqNr,clqNr);
		
		for(int i = 0; i < clqNr-1; i++) { 
			HashSet<Node> clq1 = maxClq.get(i); // select clique 1
			double clq1Size = clq1.size();
			for(int j = i + 1; j < clqNr; j++) {
				HashSet<Node> clq2 = maxClq.get(j); // select clique 2
				double clq2Size = clq2.size();
				
				HashSet<Node> diff12 = new HashSet<Node>(clq1); 
				diff12.removeAll(clq2); 
				double diff12size = diff12.size(); // size of clique 1 without nodes from clique 2
				
				double cdDist1 = 0;
				for(Node v1: diff12) {
					for(Node v2: clq2) {
						cdDist1 += CzechkanowskiDice(graph, v1, v2);  // Czechkanowski/Sorensen Dice Distance of the difference and clique 2
					}
				}
				
				HashSet<Node> diff21 = new HashSet<Node>(clq2); 
				diff21.removeAll(clq1);
				double diff21size = diff21.size(); // size of clique 2 without nodes from clique 1 
				
				double cdDist2 = 0;
				for(Node v1: diff21) {
					for(Node v2: clq1) {
						cdDist2 += CzechkanowskiDice(graph, v1, v2); // Czechkanowski/Sorensen Dice Distance of the difference and clique 1
					}
				}
				
				double lstr = Math.sqrt(cdDist2/(diff21size*clq1Size)*cdDist1/(diff12size*clq2Size));
				lkstrgth.set(i, j, lstr); // set link strength matrix
				lkstrgth.set(j, i, lstr);
			}
		}
		return lkstrgth;
	}
	
	/**
	 * Version of the adjusted Czechkanowski/Sorensen Dice Distance. The degree is corrected to the average if it lays below the average. 
	 * @param graph a graph from which v1 and v2 are taken
	 * @param v1 node which is in a clique
	 * @param v2 node which is not in the same clique as v1
	 * @return Czechkanowski Dice distance
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	protected double CzechkanowskiDice(CustomGraph graph, Node v1, Node v2) throws InterruptedException {
		Node[] nbors1 = graph.getNeighbours(v1).toArray(Node[]::new);
		Node[] nbors2 = graph.getNeighbours(v2).toArray(Node[]::new);

		int nbor1size = nbors1.length/2;
		int nbor2size = nbors2.length/2;
		
		// compute the shared neighbors
		double olapsize = 0;
		for(int i = 0; i <nbors1.length; i++) {
			Node n1 = nbors1[i];
			for(int j = 0, j1=0 ; j <nbors1.length; j++,j1++) {
				if(j1 >= nbors2.length) {
					j1=0;
				}
				Node n2 = nbors2[j1];
			
				if(n2 == n1) {
					olapsize++;
					break; 
				}
			}
		}
		
		// compute the distance
		double edgeNr = graph.getEdgeCount()/2;
		double nodeNr = graph.getNodeCount();
		double avgDegr = 2*edgeNr/nodeNr;
		double tmp1 = avgDegr - nbor1size; 
		double tmp2 = avgDegr - nbor2size; 
		
		double lmbd1 = Double.max(0, tmp1);
		double lmbd2 = Double.max(0, tmp2);
		
		return olapsize/(lmbd1 + nbor1size + lmbd2 + nbor2size);
	}
	
	/**
	 * Initialization of ants, the weight vector, pheromone information matrix, heuristic information matrix and initial solutions, fitness values 
	 * and the set for the Pareto front
	 * @param graph Maximal Clique Graph
	 * @param nodeNr number of nodes
	 * @throws InterruptedException if interrupted
	 * @return A list of initialized ants
	 */
	protected List<Ant> initialization(CustomGraph graph, int nodeNr) throws InterruptedException {
		EP = new HashMap<Vector,Vector>(); 

		// creating ants and setting parameters
		List<Ant> ants = new ArrayList<Ant>(); 

		// generate initial communities
		Vector sol = new BasicVector(nodeNr);
		for(int j = 0; j < nodeNr; j++) { //each node has its own community
				sol.set(j,j); 
		}
		// compute initial fitness of the solution 
		Vector fitness = fitnessCalculations(graph, sol, nodeNr);
		
		//set reference point
		refPoint = fitness; 
		int preGroup = 0; // group of the previous ant 		
		for(int i = 0; i < numberOfAnts; i++) {
			// creating ants 
			Ant a1 = new Ant(i);
			ants.add(a1);
			
			//set initial solution
			a1.setSolution(sol);	
			
			// fitness of the solution
			a1.setFitness(fitness);
			
			// setting weight vectors
			double rVal = (double)i/(numberOfAnts -1);
			double[] hlp = {rVal,1-rVal};
			Vector weight = new BasicVector(hlp);
			a1.setWeight(weight);
			
			//initialization of the groups
			double eucl1 = Math.sqrt(Math.pow((double) preGroup/(numberOfGroups -1) - weight.get(0), 2) + Math.pow(1-(double)preGroup/(numberOfGroups -1) - weight.get(1), 2));
			double eucl2 = Math.sqrt(Math.pow((double)(preGroup+1)/(numberOfGroups -1) - weight.get(0), 2) + Math.pow(1 - (double)(preGroup+1)/(numberOfGroups -1) - weight.get(1), 2));
			if(eucl1 <= eucl2) {
				a1.setGroup(preGroup);	  
			} else {
				a1.setGroup(preGroup+1);
				preGroup++; 
			}
			
			// neighborhood computations
			Collection<Integer> neighbors = new HashSet<Integer>();
			int dis = 1; // distance to the value 
			for(int j = 1; j <= numberOfNeighbors;) {
				if(i-dis >= 0) {
					neighbors.add(i-dis); 
					j++; 
				}
				if((i+dis) < numberOfAnts && numberOfNeighbors >= j) {
					neighbors.add(i+dis); 
					j++;
				}	
				dis++; 
			}	
			a1.setNeighbors(neighbors);
		}	
		
		// fill in the heuristic information matrix and the pheromone matrix
		heuristic = new Basic2DMatrix(nodeNr,nodeNr);  //  heuristic information matrix
		
		Matrix neighbors = graph.getNeighbourhoodMatrix();
		Node[] nodes = graph.nodes().toArray(Node[]::new);
		pheromones = new ArrayList<Matrix>(); 
		double[][] p = new double[nodeNr][nodeNr]; 
		for(int i = 0; i < nodeNr-1; i++) {
			Node n1 = nodes[i];
			Vector nbor1 = neighbors.getRow(i);
			double nborsum1 = nbor1.sum(); // sum(A_i) --> edge weights considered
			double mu1 = nborsum1/nodeNr; // mean
			double std1 = 0; 
			for(int j = 0; j < nodeNr; j++) {
				double nbor = nbor1.get(j); 
				std1 += Math.pow(nbor-mu1, 2);
			} 
			std1 = Math.sqrt(std1/nodeNr); // standard deviation
			
			nbor1 = nbor1.subtract(mu1); // preparation for the covariance 
			for(int j = i+1; j < nodeNr; j++) {
				Node n2 = nodes[j];
				double h; // heuristic for edge ij
				if(!n1.hasEdgeToward(n2)) {
					h = 0; 
				} else {
				Vector nbor2 = neighbors.getRow(j); 
				double nborsum2 = nbor2.sum(); // sum(A_j) --> edge weights considered
				double mu2 = nborsum2/nodeNr; // mean
				double std2 = 0; 
				for(int k = 0; k < nodeNr; k++) {
					double nbor = nbor2.get(k); 
					std2 += Math.pow(nbor-mu2, 2);
				} 
				std2 = Math.sqrt(std2/nodeNr); // standard deviation
				
				// compute covariance
				nbor2 = nbor2.subtract(mu2); // preparation for the covariance 
				double cov = 0;
				for(int k = 0; k < nodeNr; k++) {
					cov += nbor1.get(k)*nbor2.get(k);
				}
				
				double pearson = -cov/(nodeNr*std1*std2); // negative pearson correlation coefficient
				h = 1/(1+Math.pow(Math.E, pearson)); // heuristic information value for nodes i, j 
				
				// set initial pheromone matrix
				p[n1.getIndex()][n2.getIndex()] = initialPheromones;
				p[n2.getIndex()][n1.getIndex()] = initialPheromones;
				}				
				
				// set heuristic information matrix
				heuristic.set(i, j, h);
				heuristic.set(j, i, h); 
			}
		}
		
		// set pheromone matrices
		Matrix pheromone = new Basic2DMatrix(p);
		for(int i = 0; i < numberOfGroups; i++) {
			pheromones.add(pheromone); 
		}
		return ants; 
	}
	
// --------------------------------------------------------------------------------------------------------------------------------------------------
// construction of the solutions and update of Pareto Front
// --------------------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Searches for better solutions by annealing local search and updates if a better solution is found. Updates the set of 
	 * the Pareto front/ optimal solutions EP. Checks whether the new solution is dominated by old solutions in EP. 
	 * If the new solution is not dominated by any of the old solutions, add the new solution to EP and remove all 
	 * solutions dominated by the new solution. Also computes the limits of the pheromone matrix the next iteration 
	 * of the algorithm. It updates the pheromone matrices and exchange neighborhood information. 
	 * @param graph Maximal clique graph
	 * @param ants List of all ants
	 * @param nodeNr number of nodes
	 * @throws InterruptedException  If the thread was interrupted.
	 */
	protected void update(CustomGraph graph, List<Ant> ants, int nodeNr) throws InterruptedException {	
		double g_min = 0; // Minimum Tchebyeheff Decomposition
		double newSolNr = 0; // number of solutions added in EP
		for(int i = 0; i < numberOfAnts; i++) {
			Ant ant = ants.get(i); 
			constructSolution(graph, ant, nodeNr);
			ant.setFalseNew_sol();
			// weighted annealing local search
			Random rand = new Random(); 
			Vector old_sol = ant.getSolution();
			Vector old_fit = ant.getFitness(); 
			Vector new_fit =  new BasicVector(); ;
			Vector weight = ant.getFitness(); 
			Vector new_sol = new BasicVector(); 
			double tc_final = tchebycheffDecomposition(old_fit, weight);
			for(double T = 100; T > 0.4;){ // parameter from the paper
				T = 0.5*T; 
				for(int j = 0; j < 5; j++) {
					constructSolution(graph, ant, nodeNr); 
					new_sol = ant.getSolution();
					new_fit = ant.getFitness();
					double tc_new = tchebycheffDecomposition(new_fit, weight);
					Vector VectorF = weight.multiply(tc_new-tchebycheffDecomposition(old_fit, weight)); 
					double F = VectorF.sum();
					if(F >= 0 || rand.nextDouble() < Math.pow(Math.E,(-F/T))) {
						old_sol = new_sol; 
						old_fit = new_fit;
						tc_final = tc_new;
					}
				}
			}
			ant.setSolution(old_sol);
			ant.setFitness(old_fit);
			
			if (ant.number == 0) { // minimum tchebycheff Decomposition
				g_min = tc_final; 
			} else {
				if(g_min > tc_final) {
					g_min = tc_final; 
				}
			}
			
			//update EP 
			if(EP.isEmpty()) {
				EP.put(new_fit, new_sol); 
				return; 
			}		
			HashMap<Vector, Vector> EP_new = new HashMap<Vector, Vector>(); // updated EP
			Iterator<Vector> it = EP.keySet().iterator(); 
			// fitness of the new solution
			double NRA = new_fit.get(0); 
			double CR = new_fit.get(1);
			boolean added = true; 
			
			while(it.hasNext()) {// is the new solution dominated by any vector in EP 
				Vector fitEP = it.next(); 
				// fitness of the solution in EP
				double NRAEP = fitEP.get(0);
				double CREP = fitEP.get(1);

				// new_sol is dominated (already found a better solution) -> new solution will not be added to EP
				if((NRAEP < NRA && CREP <= CR) || (CREP < CR && NRAEP <= NRA)) {
					added = false; 
					break; 
				}
				// vectors not dominated by fitness stay in EP
				if((NRAEP > NRA && CREP < CR) || (CREP > CR && NRAEP < NRA)) {
					EP_new.put(fitEP, EP.get(fitEP));  
				}
			}
			
			if(added == true) {//change EP
				EP = EP_new; 
				EP.put(new_fit, new_sol); 
				newSolNr++;
				ant.setTrueNew_sol();
				
				//update reference point
				if(refPoint.get(0) > new_fit.get(0)) {
					refPoint.set(0, new_fit.get(0));
				}
				if(refPoint.get(1) > new_fit.get(1)) {
					refPoint.set(1, new_fit.get(1));
				}
		
			}	
		}
		
		// neighborhood interaction
		List<Double> tcList = new ArrayList<Double>(); 
		List<Ant> used = new ArrayList<Ant>(); 
		for(int i = 0; i < numberOfAnts; i++) {
			Ant ant = ants.get(i);
			Vector weight = ant.getWeight();
			Vector fit = ant.getFitness();
			
			// get the parameter for setting the limits of the pheromone level in this iteration
			double tc = tchebycheffDecomposition(fit, weight);
			tcList.add(tc); 
			
			// exchange of neighborhood information and getting better results
			Collection<Integer> neighbors = ant.getNeighbors();
			Ant replNeigh = new Ant();
			boolean replace = false; 
			for(int j: neighbors) { 
				Ant neighbor = ants.get(j);
				Vector fit_nbor = neighbor.getFitness();
				double tc_nbor = tchebycheffDecomposition(fit_nbor, weight);
	
				// solution was not used before and neighbor solution is better -> replace solution
				if(tc > tc_nbor && !used.contains(neighbor)) { 
					ant.setSolution(neighbor.getSolution());
					replNeigh = neighbor;
					replace = true; 
				}
			}
			if(replace) {
				used.add(replNeigh); // solution cannot be used to replace twice
			}
		}
		
	    // set pheromone matrix
		double[] limits = new double[2]; 
		limits[1] = (newSolNr + 1)/((1 - persistenceFactor)*(1 + g_min));
		limits[0] =	0.001 * limits[1]; 	
		for(int k = 0; k < numberOfGroups; k++) {
			Matrix m = new Basic2DMatrix(nodeNr, nodeNr);
			Matrix persist = pheromones.get(k).multiply(persistenceFactor); // persistence of the pheromones on a path
			for(int i = 0; i < nodeNr; i++) { // end of edge
				for(int j = i+1; j < nodeNr; j++) { // end of edge
					double delta = 0; 
					for(int l = 0; l < numberOfAnts; l++) {
						Ant ant = ants.get(l);
						if(ant.getGroup() == k && ant.getNew_sol()) {
							Vector sol = ant.getSolution(); 
							delta +=  isEdgeinSol(graph, sol, i, j)/(1. + tcList.get(l)); // changed pheromones on a path
						}
					}
					double result = delta + persist.get(i, j);
					if (result > limits[0]) {
						if(result < limits[1]) {
							m.set(i, j, result); // deposit + evaporation 
							m.set(j, i, result);
						}
						else { // too high pheromone concentration
							m.set(i, j, limits[1]);  
							m.set(j, i, limits[1]);
						}
					}
					else { // too low pheromone concentration
						m.set(i, j, limits[0]);  
						m.set(j, i, limits[0]);
					}
				}
			}
			pheromones.set(k, m);
		}
	}
	
 	/** Construct a new solution for an ant
	 * @param graph maximal clique graph
	 * @param ant a single ant
	 * @param nodeNr number of nodes
	 * @throws InterruptedException If the thread was interrupted.
	 */
	protected void constructSolution(CustomGraph graph, Ant ant, int nodeNr) throws InterruptedException {
		Random rand = new Random();
		Node[] nodes = graph.nodes().toArray(Node[]::new);
		
		ant.setFalseNew_sol();
		Matrix phi = new Basic2DMatrix(nodeNr,nodeNr);  
		int group = ant.getGroup();
		Matrix m = pheromones.get(group); 
		Vector weight = ant.getWeight();
		Vector fit = ant.getFitness(); 
		Vector sol = ant.getSolution(); 
		double tc = 1+tchebycheffDecomposition(fit, weight); // tchebycheff Decomposition + 1
		HashMap<Vector, Vector> new_solutions = new HashMap<Vector,Vector>(); // set of all solutions and their fitness values
		for(int j = 0; j<nodeNr; j++) {
			for(int k = j+1; k < nodeNr; k++) {
				double mq = m.get(j, k);
				double es = isEdgeinSol(graph, sol, j, k);
				double update = mq + es/tc;
				double result = Math.pow(update, 5)* Math.pow(heuristic.get(j, k), 4); 
				phi.set(k, j, result);
				phi.set(j, k, result);
			}
		}
			
		for(int j = 0; j<nodeNr; j++) {
			int maxId = 0; 
			Vector new_sol = sol.copy(); 
			
			if(rand.nextFloat() < R) {
				double maxphi = 0; 
				for(int k = 0; k < nodeNr; k++) {
					double phi_jk = phi.get(j, k);
					if(phi_jk >= maxphi) { // find maximum in phi
						maxphi = phi_jk; 
						maxId = k;
					}
				}
			} else {// calculate the probability to put node i into the community of node j
				Set<Node> nbors = graph.getNeighbours(nodes[j]); 
				double sum_nbor = 0; 
				for(int k = 0; k < nodeNr; k++) {
					if(nbors.contains(nodes[k])) {
						sum_nbor += phi.get(j, k); // sum all values in phi 
					}
				}
				// probability to put node i in community of node j
				HashMap<Double,Integer> v = new HashMap<Double, Integer>(); 
				for(int k = 0; k < nodeNr; k++) {
					if(nbors.contains(nodes[k])) {
						v.put(phi.get(j, k)/sum_nbor,k);
					}
				}
				
				// select object according to probability
				double sum = 0; 
				for(double n: v.keySet()) {
					sum += n; 
				}
				double r = rand.nextDouble()*sum;
				double cumsum = 0; 
				for(double n: v.keySet()) {
					cumsum += n; 
					if(r < cumsum) {
						maxId = v.get(n); 
					} else {
						break; 
					}
				}
			}
			new_sol.set(j, sol.get(maxId));				
			new_solutions.put(fitnessCalculations(graph,new_sol, nodeNr), new_sol);
		}
		
		// find the best solution
		boolean first = true; 
		Vector bestFit = new BasicVector(2); 
		for(Vector key: new_solutions.keySet()) {
			double NRA = key.get(0); 
			double CR = key.get(1); 
			if(NRA <= bestFit.get(0) && CR <= bestFit.get(1)|| first == true) {
				bestFit = key; 
				first = false;
			}
		}
		ant.setSolution(new_solutions.get(bestFit));
		ant.setFitness(bestFit);
	}
	
	/**
	 *  computes the Tchebycheff decomposition of a solution
	 * @param fitness vector of the solution
	 * @param lambda weight vector of the solution
	 * @return result of the Tschebyeheff decomposition
	 */
	protected double tchebycheffDecomposition(Vector fitness, Vector lambda) {
		double NRA_ratio = lambda.get(0)*Math.abs(fitness.get(0)-refPoint.get(0));
		double CR_ratio = lambda.get(1)*Math.abs(fitness.get(1)-refPoint.get(1));

		return Math.max(NRA_ratio, CR_ratio); 
	}
	
	/**
	 * Checks whether the edge (k,l) is contained in solution
	 * @param graph the examined graph
	 * @param sol solution vector
	 * @param k index of a node
	 * @param l index of another node
	 * @return whether edge (k,l) is contained in solution sol
	 */
	protected double isEdgeinSol(CustomGraph graph, Vector sol, int k, int l) {
		if(graph.getNode(l).hasEdgeToward(graph.getNode(k)) && sol.get(k) == sol.get(l)) { //TODO: Check if this behaves the same as previous
			return 1; 
		}
		return 0;  
	}	
	
// --------------------------------------------------------------------------------------------------------------------------------------------------
// metric calculations 	
// --------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Evaluation of the cover of a graph. This measures the intra-link sparesity and should be minimized (Negative Ratio Association).  
	 * This measures the inter-link density and should be minimized (Cut Ratio). 
	 * @param graph maximal clique graph
	 * @param sol solution vector
	 * @param nodeNr number of nodes
	 * @return Vector v of the two metrics: v(0) = Negative Ratio Association, v(1) = Cut Ratio 
	 */
	protected Vector fitnessCalculations(CustomGraph graph, Vector sol, int nodeNr) {
		int comNr = (int) sol.max() + 1; // starts with community 0
		
		List<Vector> members= new ArrayList<Vector>(); // prepare arrays of member of each community
		Vector v_hlp = new BasicVector(nodeNr);
		for(int j = 0; j < comNr; j++) {
			members.add(v_hlp);
		}
		
		double[] ones = new double[nodeNr]; // complementary vector 1-vector
		Arrays.fill(ones, 1);
		Vector one = new BasicVector(ones);
		for(int j = 0; j < nodeNr; j++) {
			int com = (int)sol.get(j);
			Vector comVec = new BasicVector();
			comVec = members.get(com).copy(); //separate the vector per community
			comVec.set(j, 1);
			members.set(com, comVec);
		}
			
		double NRA = 0; 
		double CR = 0;
		for(int i = 0; i < comNr; i++) {
			Vector v = members.get(i);
			Vector v_compl = one.subtract(v); // calculate inverse of v
			if(v.sum() == 0) {  // community vanished in the process of OCD
				continue; 
			}
			NRA -= cliqueInterconectivity(graph, v, v, nodeNr)/v.sum();
			CR += cliqueInterconectivity(graph, v, v_compl, nodeNr)/v.sum();
		}
		
		Vector fitness = new BasicVector(2); 
		fitness.set(0, NRA); 
		fitness.set(1, CR);
		
		return fitness;
	}
	
	/** 
	 * Measure for the inter-connectivity of two communities (can also be the same communities!)
	 * @param graph maximal clique graph
	 * @param com1 - community 1
	 * @param com2 - community 2
	 * @param nodeNr number of nodes
	 * @return shared edges between two communities
	 */
	protected double cliqueInterconectivity(CustomGraph graph, Vector com1, Vector com2, int nodeNr) {
			double L = 0; // counter of edges in between the communities
			Node[] nodes = graph.nodes().toArray(Node[]::new);
			for(int i = 0; i < nodeNr; i++) { 
				if(com1.get(i) == 0) { // filters out all nodes within a community from the community vector
					continue;
				}
				Node n1 = nodes[i]; 
				for(int j = 0; j < nodeNr; j++) {
					if(com2.get(j) == 0) { // filters out all nodes within a community from the community vector
						continue;
					}
					Node n2 = nodes[j];
					if (n1.hasEdgeToward(n2)) { // if two nodes from these two communities are connected by an edge
						L += graph.getEdgeWeight(n1.getEdgeToward(n2)); //TODO: Check if this behaves the same as before
					}
				}
			}
			return L;
		}

// --------------------------------------------------------------------------------------------------------------------------------------------------
// decoding the maximal clique graph into the original graph
// --------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Transfer the solutions of the maximal clique graph into a solution for the original graph
	 * Since we created the Pareto Front of optimal solutions, we choose here the solution which has the highest modularity after the decoding
	 * @param graph original graph (not Maximal Clique Graph!)
	 * @param nodeNr number of nodes
	 * @return Cover of the original graph
	 * @throws OcdAlgorithmException if no solution is found
	 * @throws InterruptedException if thread was interrupted
	 * @throws OcdMetricException if the metric execution failed
	 */
	protected Cover decodeMaximalCliques(CustomGraph graph, int nodeNr) throws OcdAlgorithmException, InterruptedException, OcdMetricException {
		//System.out.println("Number of Solutions: " + EP.size());

		if(EP.isEmpty()) { // no solution found during OCD
			throw new OcdAlgorithmException(); 
		}
		
		Cover bestCov = new Cover(graph); 
		double Q = 0; // modularity
		
		ModularityMetric MM = new ModularityMetric();
		HashSet<Node> inCommunity = new HashSet<Node>();
		Node[] nodes = graph.nodes().toArray(Node[]::new);
		for(Vector sol: EP.values()) {
			// find out how many communities are there
			List<Vector> membershipMatrixVectors = new ArrayList<Vector>(nodeNr);
			List<Integer> com = new ArrayList<Integer>();
			
			for(int i = 0; i < graph.getNodeCount(); i++) {
				inCommunity.add(nodes[i]); 
			}
			for(int i = 0; i < nodeNr; i++) {
				int com1 = (int) sol.get(i);
				if(!com.contains(com1)) {
					com.add(com1);	
				}
			}
		
			for(int i = 0; i < nodeNr; i++) { // create empty membership vectors
				Vector v = new BasicVector(graph.getNodeCount());
				membershipMatrixVectors.add(i,v);
			}
	
			// prepare membership matrix
			for(Entry<Integer, HashSet<Node>> entry: maxClq.entrySet()) {
				int member = (int) sol.get(entry.getKey()); // index of the solution community
				for(Node n: entry.getValue()){ 
					inCommunity.remove(n);  
					Vector v = membershipMatrixVectors.get(member);
					v.set(n.getIndex(), 1);
					membershipMatrixVectors.set(member, v);  // set node in community 
				}
			}
			
			// all nodes which are not in a community because they are not in a maximal clique are put in the community
			for(Node n: inCommunity) {
				Set<Node> neighbors = graph.getNeighbours(n); 
				for(Node neighbor: neighbors) {
					int id = neighbor.getIndex();
					for(int i = 0; i < membershipMatrixVectors.size(); i++) {
						Vector v = membershipMatrixVectors.get(i);
						if(v.get(id) == 1) {
							v.set(n.getIndex(), 1);
							break; 
						}
					}
				}
				
			}
			
			// set membership matrix
			Matrix membershipMatrix = new Basic2DMatrix(graph.getNodeCount(),com.size());
			for(int i = 0; i < com.size(); i++) {
				membershipMatrix.setColumn(i, membershipMatrixVectors.get(com.get(i)));
			}
			
			//generate Cover
			Cover c = new Cover(graph,membershipMatrix); 
			
			double Q_c = MM.measure(c);
			System.out.println("modularity: " + Q_c);
			if(Q < Q_c) {
				Q = Q_c; 
				bestCov = c; 
			}
		}
		
		return bestCov;
	}
	
}

