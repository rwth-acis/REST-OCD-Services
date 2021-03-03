package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.algorithms.utils.MaximalCliqueGraphRepresentation;
import i5.las2peer.services.ocd.algorithms.utils.Ant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import java.lang.Double; 
import java.lang.Math;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;
import y.base.Edge;
import y.base.Node;
import y.base.NodeCursor;

/**
* The original version of the overlapping community detection algorithm introduced in 2020
* by Ping Ji, Shanxin Zhang, Zhiping Zhou.
* @author Marlene Damm
*/
//TODO description of the algorithm
public class AntColonyOptimizationAlgorithm implements OcdAlgorithm {
	
	
	private static int maxIterations = 100;
	
	/**
	 * maximal clique encoding. the integer represents the number of the clique and the Hashset stores the 
	 * clique members
	 */
	private HashMap<Integer,HashSet<Node>> maxClq;
	
	/**
	 * number of ants/subproblems to solve. Default value: 1000
	 */
	private static int M = 10;
	  
	/**
	 * Positive integer associated with M. Helps to find uniformly distributed weight vector. Should be at least as large as M.  
	 */
	private static int H = M*100; 
	
	/**
	 * Number of  ants in groups. The value should be in between 0 and M. 
	 */
	private static int K = 5; 
	
	/**
	 * number of nodes in the graph
	 */
	private static int nodeNr;
	  
	/**
	 * Rate of the pheromone persistence
	 */
	private static double rho = 0.5;

	/**
	 * Threshold determines the edges which are in the clique graph. It should be in between 0 and 1 with
	 * 1 being no edges in the clique graph and 0 being no edge will be left out. Setting this threshold 
	 * to 0 will slow down the performance. Since good thresholds are not stated in the paper the threshold 
	 * should be proven experimentally. 
	 */
	private double threshold = 0.2; 
	
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
	 * saves all best found community solutions
	 */
	private List<Vector> newtoEP;
	
	/**
	 * Heuristic information matrix: shows how similar to nodes. Nodes which are more similar are more likely to be in 
	 * the same community. The values are between 0 and 1 which 0 being not connected and 1 being very similar. 
	 */
	private Matrix heuristic; 
	
	/**
	* The number of nearest neighbors considered in a neighborhood
	*/
	private static int nearNbors = 2; 
	
	/**
	 * Indicates the influence of the pheromone information matrix to the solution construction. The higher alpha the bigger is the influence.
	 */
	private static double alpha = 5; 
	
	/**
	 * Indicates the influence of the heuristic information matrix to the solution construction. The higher beta the bigger is the influence. 
	 */
	private static double beta = 4; 
	
	/**
	 * reference point for the minimal objective function values found so far 
	 */
	private static Vector refPoint;
	
	/**
	 * threshold to filter out path randomly. used in solution construction and between 0 and 1
	 */
	private static double R = 0.2;
	
	
	
	/*
	 * PARAMETER NAMES
	 */
	protected static final String MAX_ITERATIONS = "maximum iterations";
	
	protected static final String NUMBER_OF_ANTS = "number of ants/subproblems";
			
	protected static final String EVAPORATION_FACTOR = "evaportation factor";
	
	protected static final String MCR_THRESHOLD = "Threshold to filter out edges";
	
	protected static final String NUMMER_OF_NEIGHBORS = "Number of nearest neighbors to be considered in a neighborhood";
	
	public AntColonyOptimizationAlgorithm() {}
	
	/**
	 * Executes the algorithm on a connected graph.
	 * Implementations of this method should allow to be interrupted.
	 * I.e. they should periodically check the thread for interrupts (/TODO)
	 * and throw an InterruptedException if an interrupt was detected.
	 * @param graph An at least weakly connected graph whose community structure will be detected.
	 * @return A cover for the input graph containing the community structure.
	 * @throws OcdAlgorithmException If the execution failed.
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
		// construct the maximal clique graph and initialize the parameters
		CustomGraph MCR = representationScheme(graph);
		List<Ant> ants = initialization(MCR);
		
		// constructions of the Pareto Front (Pareto optimal solutions) 
		for(int i = 0; i < maxIterations; i++) {
			constructSolution(MCR,ants); 
			updatePheromoneMatrix(MCR, ants); 
			updateCurrentSolution(ants); 
		}
		
		// select solution from the Pareto Front EP by modularity Q
		Iterator<Vector> it = EP.keySet().iterator();
		Vector fini_sol = new BasicVector();
		double Q = 0; //modularity
		Node[] nodes = MCR.getNodeArray();
		double edgeNr = MCR.edgeCount();  
		while(it.hasNext()) { 
			Vector i = it.next();
			Vector curr = EP.get(i); 
			double Q_new = 0;
			boolean first = true; 
			
			for(Node n1: nodes) {
				for(Node n2: nodes) {
					if(curr.get(n1.index()) == curr.get(n2.index())) { // nodes are in the same community 
						if(MCR.containsEdge(n1,n2)) {
							Q_new += 1;
						}
						Q_new -= n1.inDegree()*n2.inDegree()/edgeNr; // - k_j*k_i/(edges)
					}
				}
			}
			
			Q_new = Q_new/2*edgeNr; // new modularity
			
			if(Q_new > Q || first == true) {
				Q = Q_new; 
				fini_sol = curr; 
				first = false;
			}
			break; 
		}
		System.out.println(EP.keySet());
		System.out.println(EP.size());
		System.out.println(fini_sol);
		return decodeMaximalCliques(graph, fini_sol);
		
	}
	
// --------------------------------------------------------------------------------------------------------------------------------------------------
// preparations Maximal Clique Graph construction and initialization of the parameters
// --------------------------------------------------------------------------------------------------------------------------------------------------
/**
 	* The Representation Scheme of this algorithm is a Maximal Clique Scheme. This is done to have a less complex community search process, since cliques in the 
	 * original graph tend to be in the same community anyways. After the search for maximal cliques, inter-clique edges are filtered out if the cliques are loosly
	 * connected.
	 * @param graph to make an Maximal Clique Graph from
	 * @return encoded input graph
	 */
	protected CustomGraph representationScheme(CustomGraph graph) {
		// maximal clique search 
		MaximalCliqueGraphRepresentation MCR = new MaximalCliqueGraphRepresentation();
		maxClq = MCR.cliques(graph);
				
		// determining the link strength in between the cliques
		Matrix lkstrgth = linkStrength(graph, maxClq);
				
		//creating the encoding
		nodeNr = maxClq.size(); 
		CustomGraph encoding = new CustomGraph(); 
		for(int i = 0; i < nodeNr; i++) {//creating clique nodes
				encoding.createNode(); 
		}
		for(Node n1: encoding.getNodeArray()) { // creating clique edges 
			int i1 = n1.index();
			for(Node n2: encoding.getNodeArray()) {
				int i2 = n2.index();
				double ls = lkstrgth.get(i1, i2);
				if(ls>=threshold) { // leaving out weak edges
					Edge e1 = encoding.createEdge(n1, n2);
					Edge e2 = encoding.createEdge(n2, n1);
					encoding.setEdgeWeight(e1, ls);
					encoding.setEdgeWeight(e2, ls);
				}
			}
		}
		
		return encoding; 
				
	}

	/**
	 * Initialization of ants, the weight vector, pheromone information matrix, heuristic information matrix and initial solutions
	 * "MOEA/D: A Multiobjective Evolutionary Algorithm Based on Decomposition" by Qingfu Zhang et al. for reference.
	 * @param graph
	 * @param nodes
	 * @throws InterruptedException 
	 */
	protected List<Ant> initialization(CustomGraph graph) throws InterruptedException {
		EP = new HashMap<Vector,Vector>(); 
		
		//Initializing Ants
		List<Ant> ants = new ArrayList<Ant>(); 
		for(int i = 0; i < M; i++) {
			Ant a = new Ant(i);
			ants.add(a);
		}
		
		// initializing the values to choose from 
		List<Double> H_values = new ArrayList<Double>();
		for(int i = 0; i <= H; i++) {
			double hlp = ((double)i)/H;
			H_values.add(i,hlp);
		}
		
		//initialization of the weight vectors of the subproblems/ants
		Random rand = new Random();
		List<Vector> lambdas = new ArrayList<Vector>(); 
		for(int i = 0; i < M; i++) {
			double rVal = H_values.get(rand.nextInt(H));
			double[] hlp = {rVal,1-rVal};
			Vector v = new BasicVector(hlp);
			ants.get(i).setWeight(v);
			lambdas.add(v);
		}
		
		// find the T closest neighbors
		for(Ant a1: ants) {
			Map<Double,Integer> euclDist = new HashMap<Double,Integer>();
			Vector lda1 = a1.getWeight(); 
			int j = 0; 
			for(Ant a2: ants) { // calculate the euclidian distance for two vectors  
				if(a1 == a2) {
					continue; 
				}
				int ind = a2.number; 
				Vector lda2 = a2.getWeight();
				double eucl = Math.sqrt(Math.pow(lda2.get(0) - lda1.get(0), 2) + Math.pow(lda2.get(1) - lda1.get(1), 2));
				if(j < nearNbors) { // if not nearNbors solutions have been found
					euclDist.put(eucl,ind);
					j++; 
				} else {
					Iterator<Double> it = euclDist.keySet().iterator(); 
					boolean replace = false; 
					double maxEucl = it.next(); 
					while(it.hasNext()) { // compare the entries found so far to the current vector
						double comp_eucl = it.next();
						if(comp_eucl > eucl) { // as soon as the euclidian distance of the current vector is smaller then the entry in the table -> replace that entry
							if(comp_eucl > maxEucl) { // make sure to replace the biggest euclidian dist
								maxEucl = comp_eucl; 
							}
							replace = true; 
						}
					}	
					if(replace == true) {
						euclDist.remove(maxEucl);
						euclDist.put(eucl,ind); 
					}
				}
			}
			Iterator<Double> it = euclDist.keySet().iterator(); //convert HashMap into ArrayList 
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			while(it.hasNext()) {
				tmp.add(euclDist.get(it.next()));
			}
			a1.setNeighbors(tmp);
		}
		
		
		//initialization of help weight vectors of the group generation
		List<Vector> hlp_lambdas = new ArrayList<Vector>(); 
		for(int i = 0; i <= K; i++) {
			double rVal = H_values.get(rand.nextInt(H));
			double[] hlp = {rVal,1-rVal};
			Vector v = new BasicVector(hlp);
			hlp_lambdas.add(v);
		}
		
		// grouping the ants in K groups
		for(Ant a1: ants) {
			Vector lda1 = a1.getWeight(); 
			int minID = 0; 
			double minEucl = Math.sqrt(Math.pow(hlp_lambdas.get(0).get(0) - lda1.get(0), 2) + Math.pow(hlp_lambdas.get(0).get(1) - lda1.get(1), 2)); 
			for(int j = 1; j < K; j++) { // calculate the euclidian distance for two vectors  
				Vector v = hlp_lambdas.get(j);
				double eucl = Math.sqrt(Math.pow(v.get(0) - lda1.get(0), 2) + Math.pow(v.get(1) - lda1.get(1), 2));
				if(minEucl > eucl) {
					minID = j;
					minEucl = eucl; 
				}
			}
			a1.setGroup(minID);
		}
		
		// fill in the heuristic information matrix
		heuristic = new Basic2DMatrix(nodeNr,nodeNr);  
		Matrix neighbors = graph.getNeighbourhoodMatrix();
		Node[] nodes = graph.getNodeArray();
		for(int i = 0; i < nodeNr-1; i++) {
			Vector nbor1 = neighbors.getRow(i);
			double nborsum1 = nbor1.sum(); // sum(A_i) --> edge weights considered
			double mu1 = nborsum1/nodeNr; // mean
			double deg1 = nodes[i].inDegree(); // number of neighbors
			double std1 = (deg1*Math.pow(1-mu1, 2)+(nodeNr-deg1)*Math.pow(mu1, 2))/nodeNr; //variance
			std1 = Math.sqrt(std1); // standard deviation
			
			nbor1 = nbor1.subtract(mu1); // preparation for the covariance 
			for(int j = i+1; j < nodeNr; j++) {
				Vector nbor2 = neighbors.getRow(j); 
				double nborsum2 = nbor2.sum(); // sum(A_j) --> edge weights considered
				double mu2 = nborsum2/nodeNr; // mean
				double deg2 = nodes[j].inDegree(); // number of neighbors
				double std2 = (deg2*Math.pow(1-mu2, 2)+(nodeNr-deg2)*Math.pow(mu2, 2))/nodeNr;  //variance
				std2 = Math.sqrt(std2); // standard deviation
				
				// compute covariance
				nbor2 = nbor2.subtract(mu2); // preparation for the covariance 
				double cov = 0;
				for(int k = 0; k < nodeNr; k++) {
					cov += nbor1.get(k)*nbor2.get(k);
				}
				
				double pearson = -cov/(nodeNr*std1*std2); // negative pearson correlation coefficient
				double h = 1/(1+Math.pow(Math.E, pearson)); // heuristic information value for nodes i, j 
				if(h < 0 || !graph.containsEdge(nodes[i], nodes[j])) {
					h = 0; 
				}
				heuristic.set(i, j, h);
				heuristic.set(j, i, h); 
				
			}
		}
		
		//initialize the pheromone matrices 
		pheromones = new ArrayList<Matrix>(); 
		double[][] p = new double[nodeNr][nodeNr]; 
		for(Node n1: nodes) {
			for(Node n2: nodes) {
				if(graph.containsEdge(n1, n2)) {
					p[n1.index()][n2.index()] = initialPheromones;
				}
			}
		}	
		Matrix pheromone = new Basic2DMatrix(p);
		for(int i = 0; i < K; i++) {
			pheromones.add(pheromone);
		}
		 
		//initial solution -> each clique is a community 
		for(Ant a: ants) {
			Vector v = new BasicVector(nodeNr);
			for(int i = 0; i < nodeNr; i++) {
				v.set(i, i); 
			}
			a.setSolution(v);
		}
		
		//Reference Point & Fitness Values of the current solution
		Vector fitness = new BasicVector(2); 
		Vector sol = ants.get(0).getSolution(); 
		double NRA = negativeRatioAssociation(graph, sol);
		double CR = cutRatio(graph, sol);
		fitness.set(0, NRA);
		fitness.set(1, CR);
		// all the ants have same initial solutions -> so same fitness
		for(Ant a: ants) {
			a.setFitness(fitness);
		}
		//set reference point (best values for NRA, CR found so far)
		refPoint = fitness;
		
		return ants; 
		
	}
	
	/** Measures the link strength in between the maximal cliques. 
	 * 
	 * @param graph
	 * @param maxClq output of the MaximalCliqueGraphRepresentation
	 * @return Matrix of link strength in  between the nodes
	 */
	protected Matrix linkStrength(CustomGraph graph, HashMap<Integer,HashSet<Node>> maxClq) {
		int clqNr = maxClq.size(); 
		Matrix lkstrgth = new Basic2DMatrix(clqNr,clqNr);
		
		for(int i = 0; i < clqNr; i++) { 
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
						cdDist1 += CzechkanowskiDice(graph, v1, v2);  // Czechkanowski Dice Distance of the difference and clique 2
					}
				}
				
				HashSet<Node> diff21 = new HashSet<Node>(clq2); 
				diff21.removeAll(clq1);
				double diff21size = diff21.size(); // size of clique 2 without nodes from clique 1 
				
				double cdDist2 = 0;
				for(Node v1: diff21) {
					for(Node v2: clq1) {
						cdDist2 += CzechkanowskiDice(graph, v1, v2); // Czechkanowski Dice Distance of the difference and clique 1
					}
				}
				
				double lstr = cdDist2/(diff21size*clq1Size)*cdDist1/(diff12size*clq2Size);
				lstr = Math.sqrt(lstr);
				lkstrgth.set(i, j, lstr); // set matrix (entries have a triangular form)
				
			}
		}
		return lkstrgth;
	}
	
	/**
	 * Version of the adjusted Czechkanowski/Sorensen Dice Distance. The number of neighbors is changed to the average if it lay below the average. 
	 * @param graph a graph from which v1 and v2 are taken
	 * @param v1 node which is in a clique
	 * @param v2 node which is not in the same clique as v1
	 * @return adjusted Czechkanowski Dice distance
	 */
	protected double CzechkanowskiDice(CustomGraph graph, Node v1, Node v2) {
		NodeCursor nbors1 = v1.neighbors();
		NodeCursor nbors2 = v2.neighbors(); 

		int nbor1size = nbors1.size()/2; 
		int nbor2size = nbors2.size()/2; 
		
		double olapsize = 0; 
		
		for(int i = 0 ; i <nbors1.size(); i++) {
			Node n1 = nbors1.node();
			for(int j = 0 ; j <nbors1.size(); j++) {
				Node n2 = nbors2.node(); 
			
				if(n2 == n1) {
					olapsize++;
					break; 
				}
				
				nbors2.cyclicNext();
			}

			nbors1.cyclicNext();
		}
		double edgeNr = graph.edgeCount()/2;
		double nodeNr = graph.nodeCount(); 
		double avgDegr = 2*edgeNr/nodeNr;
		double tmp1 = avgDegr - nbor1size; 
		double tmp2 = avgDegr - nbor2size; 
		
		double lmbd1 = Double.max(0, tmp1);
		double lmbd2 = Double.max(0, tmp2);
		
		return olapsize/(lmbd1 + nbor1size + lmbd2 + nbor2size);
	}
	
// --------------------------------------------------------------------------------------------------------------------------------------------------
// construction of the solutions und update of Pareto Front
// --------------------------------------------------------------------------------------------------------------------------------------------------
/**
 	* construct a new solution for each ant
	 * @param graph to find new soutions on 
	 * @param ants population of ants
	 * @throws InterruptedException 
	 */
	protected void constructSolution(CustomGraph graph, List<Ant> ants) throws InterruptedException {
		Random rand = new Random();
		newtoEP = new ArrayList<Vector>(M); 
		Node[] nodes = graph.getNodeArray(); 
		for(Ant ant: ants) {
			newtoEP.add(new BasicVector(nodeNr));
			Matrix phi = new Basic2DMatrix(nodeNr,nodeNr); 
			int group = ant.getGroup();
			Matrix m = pheromones.get(group); 
			Vector weight = ant.getWeight();
			Vector sol = ant.getSolution(); 
			HashMap<Vector, Vector> new_solutions = new HashMap<Vector,Vector>();
			for(int i = 0; i<nodeNr; i++) {
				for(int j = 0; j < nodeNr; j++) {
					double update = m.get(i, j)+1/(1+TchebyehoffDecomposition(sol, weight))*isEdgeinSol(graph, sol, i, j);
					phi.set(i, j, Math.pow(update, alpha)*Math.pow(heuristic.get(i, j), beta));
				}
			}
				
			for(int i = 0; i<nodeNr; i++) {
				int maxId = 0; 
				Vector new_sol = sol.copy(); 
				
				if(rand.nextFloat() < R) {
					double maxphi = 0; 
					for(int j = 0; j < nodeNr; j++) {
						double phi_ij = phi.get(i, j);
						if(phi_ij > maxphi) { // find maximum in phi
							maxphi = phi_ij; 
							maxId = j;
						}
					}
				} else {
					// calculate the probability to put node i into the community of node j
					Set<Node> nbors = graph.getNeighbours(nodes[i]); 
					double sum_nbor = 0; 
					for(int j = 0; j < nodeNr; j++) {
						if(nbors.contains(nodes[j])) {
							sum_nbor += phi.get(i, j); // sum all values in phi 
						}
					}
					// probability to put node i in community of node j
					HashMap<Double,Integer> v = new HashMap<Double, Integer>(); 
					for(int j = 0; j < nodeNr; j++) {
						if(nbors.contains(nodes[j])) {
							v.put(phi.get(i, j)/sum_nbor,j);
						}
					}
					
					Iterator<Double> it = v.keySet().iterator();
					
					double prob = 0; 
					while(it.hasNext()) {
						double next = it.next();
						if(next < prob) {
							if(next < 0) {
							System.out.println(next);}
							continue; 
						}
						maxId = (int) v.get(next); 
						prob = next; 
					}
					
				}
				new_sol.set(i, sol.get(maxId));
				Vector fitness = new BasicVector(2);
				fitness.set(0, negativeRatioAssociation(graph, new_sol));
				fitness.set(1, cutRatio(graph, new_sol));
				new_solutions.put(fitness, new_sol);
			}
			Iterator<Vector> it = new_solutions.keySet().iterator();
			Vector bestFit = it.next(); 
			while(it.hasNext()) {
				Vector v = it.next();
				double NRA = v.get(0); 
				double CR = v.get(1); 
				if(NRA <= bestFit.get(0) && CR <= bestFit.get(1)) {
					bestFit = v; 
				}
			}

			updateEP(ant, new_solutions.get(bestFit), bestFit);
			ant.setSolution(new_solutions.get(bestFit));
			ant.setFitness(bestFit);
		}
	}
	
	/**
	 * Updates the set of the Pareto front/ optimal solutions EP. Checks whether the new solution is dominated 
	 * by old solutions in EP. If the new solution is not dominated by any of the old solutions, add the new solution
	 * to EP and remove all solutions dominated by the new solution. 
	 * @param new_sol new found solution
	 * @param fitness fitness value of the found solution
	 */
	protected void updateEP(Ant ant, Vector new_sol, Vector fitness) {		
		if(EP.isEmpty()) {
			EP.put(fitness, new_sol); 
			return; 
		}
		HashMap<Vector, Vector> EP_new = new HashMap<Vector, Vector>(); // updated EP
		Iterator<Vector> it = EP.keySet().iterator(); 
		while(it.hasNext()) {// is the new solution dominated by any vector in EP 
			Vector fitEP = it.next(); 
			double NRAEP = fitEP.get(0);
			double CREP = fitEP.get(1);
			double NRA = fitness.get(0); 
			double CR = fitness.get(1);
			// new_sol is dominated (already found a better solution) -> new solution will not be added to EP
			if((NRAEP < NRA && CREP <= CR) || (CREP < CR && NRAEP <= NRA)) {
				return; 
			}
			// vectors not dominated by fitness stay in EP
			if((NRAEP > NRA && CREP < CR) || (CREP > CR && NRAEP < NRA)) {
				EP_new.put(fitEP, EP.get(fitEP));  
			}
		}
		EP = EP_new; 
		EP.put(fitness, new_sol); 
		newtoEP.set(ant.number, new_sol); // keep track of the newly added solutions for the update of the pheromone matrix
		
		//update reference point
		if(refPoint.get(0) > fitness.get(0)) {
			refPoint.set(0, fitness.get(0));
		}
		if(refPoint.get(1) > fitness.get(1)) {
			refPoint.set(1, fitness.get(1));
		}
	
	}
	
// --------------------------------------------------------------------------------------------------------------------------------------------------
// metric calculations 	
// --------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Evaluation of the cover of a graph. This measures the intra-link sparesity and should be minimized. 
	 * @param graph
	 * @param cover to evaluate on the graph
	 * @return negative Ratio Association
	 */
	protected double negativeRatioAssociation(CustomGraph graph, Vector sol) {
		double NRA = 0; 
		int comNr = (int) sol.max() +1; // starts with community 0
		
		List<Vector> members= new ArrayList<Vector>(); // prepare arrays of member of each community
		Vector v_hlp = new BasicVector(nodeNr);
		for(int j = 0; j < comNr; j++) {
			members.add(v_hlp);
		}
		
		for(int j = 0; j < nodeNr; j++) {
			int com = (int)sol.get(j);
			Vector v = new BasicVector();
			v = members.get(com).copy(); //separate the vector per community
			v.set(j, 1);
			members.set(com, v);
		}
		
		for(int i = 0; i < comNr; i++) {
			Vector v = members.get(i);
			if(v.sum() == 0) {  // community vanished in the process of OCD
				continue; 
			}
			NRA -= cliqueInterconectivity(graph, v, v)/v.sum();
		}
		
		return NRA;
	}
	
	/**
	 * Evaluation of the cover of a graph. This measures the inter-link density and should be minimized. 
	 * @param graph
	 * @param cover to evaluate on the graph
	 * @return Cut Ratio
	 */
	protected double cutRatio(CustomGraph graph, Vector sol) {
		double CR = 0; 
		int comNr = (int) sol.max()+1; 
		
		// help list to identify which node is in which community
		List<Vector> members= new ArrayList<Vector>();
		Vector v_hlp = new BasicVector(nodeNr);
		for(int j = 0; j < comNr; j++) {
			members.add(v_hlp);
		}
		
		// complementary vector 1-vector
		double[] ones = new double[nodeNr];
		Arrays.fill(ones, 1);
		Vector one = new BasicVector(ones);
		for(int j = 0; j < sol.length(); j++) {
			int com = (int)sol.get(j);
			Vector v = members.get(com).copy(); //separate the vector per community
			v.set(j, 1);
			members.set(com, v);
		}

		for(int i = 0; i<comNr; i++) {
			Vector v = members.get(i); 
			Vector v_compl = one.subtract(v); // calculate inverse of v
			if(v.sum() == 0) {
				continue; 
			}
			CR += cliqueInterconectivity(graph, v, v_compl)/v.sum();
		}
		
		return CR;
	}
	
	/** 
	 * Measure for the interconnectivity of two communities (can also be the same communities!)
	 * @param graph 
	 * @param com1 - community 1
	 * @param com2 - community 2
	 * @return shared edges between two communities
	 */
	protected double cliqueInterconectivity(CustomGraph graph, Vector com1, Vector com2) {
			double L = 0; // counter of edges in between the communities
			int com1Len = com1.length(); 
			Node[] nodes = graph.getNodeArray(); 
			for(int i = 0; i < com1Len; i++) { // iterates over all nodes
				if(com1.get(i) == 0) { // filters out all nodes within a community from the community vector
					continue;
				}
				Node n1 = nodes[i]; 
				for(int j = 0; j < com1Len; j++) { // iterates over all nodes
					if(com2.get(j) == 0) { // filters out all nodes within a community from the community vector
						continue;
					}
					Node n2 = nodes[j];
					
					if (graph.containsEdge(n1, n2)) { // if two nodes from these two communities are connected by an edge
						L += graph.getEdgeWeight(n1.getEdgeTo(n2)); 
					}
				}
			}
			return L;
		}

// --------------------------------------------------------------------------------------------------------------------------------------------------
// update of pheromones and simulation of the interaction between the neighbors
// --------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Updates the pheromone matrix: two mechanisms
	 * 1) pheromone evaporation on an edge
	 * 2) pheromone deposit on an edge
	 * @param ants implemented ants
	 */
	protected void updatePheromoneMatrix(CustomGraph graph, List<Ant> ants) {//TODO 
		for(int k = 0; k < K; k++) {
			Matrix m = new Basic2DMatrix(nodeNr, nodeNr);
			Matrix persist = pheromones.get(k).multiply(rho); // persistence of the pheromones on a path
			for(int i = 0; i < nodeNr; i++) { // end of edge
				for(int j = i+1; j < nodeNr; j++) { // end of edge
					double delta = 0; 
					for(Vector v: newtoEP) {
						Ant ant = ants.get(newtoEP.indexOf(v));
						Vector weight = ant.getWeight();
						if(ant.getGroup() == k) {
							Vector fit = ant.getFitness();
							Vector sol = ant.getSolution(); 
							delta += 1/(1 + TchebyehoffDecomposition(fit, weight)) * isEdgeinSol(graph, sol, i, j); // changed pheromones on a path 
						}
						
					}
					m.set(i, j, delta + persist.get(i, j)); // evaporation + deposit
					m.set(j, i, delta + persist.get(j, i));
				}
			}
			pheromones.set(k, m);
		}
		
	}
	
	/**
	 * Simulation of interaction of ants in the same neighborhood. If an ant in the neighborhood finds 
	 * a better solution, then the solution of an ant is replaced by the solution of the neighbor. Each 
	 * neighbor can only replace one solution.  
	 * @param ants 
	 */
	protected void updateCurrentSolution(List<Ant> ants) {
		List<Ant> used = new ArrayList<Ant>(); 
		for(Ant ant: ants) {
			ArrayList<Integer> neighbors = ant.getNeighbors();
			Vector weight = ant.getWeight(); 
			Vector fit = ant.getFitness();
			double tc = TchebyehoffDecomposition(fit, weight);
			for(int i: neighbors) { 
				Ant neighbor = ants.get(i);
				Vector fit_nbor = neighbor.getFitness();
				Vector wei_nbor = neighbor.getWeight();
				double tc_nbor = TchebyehoffDecomposition(fit_nbor, wei_nbor);
	
				// solution was not used before and neighbor solution is better -> replace solution
				if(tc > tc_nbor && !used.contains(neighbor)) { 
					ant.setSolution(neighbor.getSolution());
					used.add(neighbor); // solution cannot be used to replace twice
				}
			}
		}
		
	}
	
	/**
	 *  computes the Tschebyeheff decomposition of a solution
	 * @param fitness vector of the solution
	 * @param lambdas weight vector of the solution
	 * @return result of the Tschebyeheff decomposition
	 */
	protected double TchebyehoffDecomposition(Vector fitness, Vector lambda) {
		double NRA_ratio = fitness.get(0)*Math.abs(lambda.get(0)-refPoint.get(0));
		double CR_ratio = fitness.get(1)*Math.abs(lambda.get(1)-refPoint.get(1));

		return Math.max(NRA_ratio, CR_ratio); 
	}
	
	/**
	 * Checks whether the edge (k,l) is contained in solution
	 * @param sol solution vector
	 * @param k index of a node
	 * @param l index of another node
	 * @return whether edge (k,l) is contained in solution sol
	 */
	protected double isEdgeinSol(CustomGraph graph, Vector sol, int k, int l) {
		if(graph.containsEdge(graph.getNodeArray()[l], graph.getNodeArray()[k])&& sol.get(k) == sol.get(l)) {
			return 1; 
		}
		return 0;  
	}

// --------------------------------------------------------------------------------------------------------------------------------------------------
// decoding the maximal clique graph into the original graph
// --------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Transfer the solution of the Maximal Clique Graph into a solution for the original graph
	 * @param graph original graph (not Maximal Clique Graph!)
	 * @param sol solution vector of the best solution
	 * @return Cover of the original graph 
	 */
	protected Cover decodeMaximalCliques(CustomGraph graph, Vector sol) {
		// find out how many communities are there
		List<Vector> membershipMatrixVectors = new ArrayList<Vector>(nodeNr);
		List<Integer> com = new ArrayList<Integer>();
		
		for(int i = 0; i < nodeNr; i++) {
			int com1 = (int) sol.get(i);
			if(!com.contains(com1)) {
				com.add(com1);	
			}
		}
		
		for(int i = 0; i < nodeNr; i++) {
			Vector v = new BasicVector(graph.nodeCount());
			membershipMatrixVectors.add(i,v);
		}

		// prepare membership matrix
		Iterator<Integer> it = maxClq.keySet().iterator();

		while(it.hasNext()) {
			int ind = it.next(); // index of clique
			HashSet<Node> clique = maxClq.get(ind); 
			int member = (int) sol.get(ind); // index of the solution community
			for(Node n: clique){ 
				Vector v = membershipMatrixVectors.get(member);
				v.set(n.index(), 1);
				membershipMatrixVectors.set(member, v);  // set node in community 
			}
		}
		
		Matrix membershipMatrix = new Basic2DMatrix(graph.nodeCount(),com.size());
		int i = 0;
		for(int cNr: com) {
			membershipMatrix.setColumn(i, membershipMatrixVectors.get(cNr));
			i++;
		}
		
		//generate Cover
		Cover c = new Cover(graph); 
		c.setMemberships(membershipMatrix);
		
		return c;
	}
	

// --------------------------------------------------------------------------------------------------------------------------------------------------
// override important methods from the parent class
// --------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Returns a log representing the concrete algorithm execution.
	 * @return The log.
	 */
	@Override
	public CoverCreationType getAlgorithmType(){
		return CoverCreationType.ANT_COLONY_OPTIMIZATION;
	}
	
	/**
	 * Returns all graph types the algorithm is compatible with.
	 * @return The compatible graph types.
	 * An empty set if the algorithm is not compatible with any type.
	 */
	public Set<GraphType> compatibleGraphTypes(){
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.ZERO_WEIGHTS);
		return compatibilities;
	};
	
	
	@Override
	public Map<String,String> getParameters(){
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(MAX_ITERATIONS, Double.toString(maxIterations));
		parameters.put(NUMBER_OF_ANTS, Integer.toString(M));
		parameters.put(EVAPORATION_FACTOR, Double.toString(rho));
		parameters.put(MCR_THRESHOLD, Double.toString(threshold));
		return parameters;
	}

	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.containsKey(MAX_ITERATIONS)) {
			maxIterations = Integer.parseInt(parameters.get(MAX_ITERATIONS));
			if(maxIterations <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(MAX_ITERATIONS);
		}
		if(parameters.containsKey(NUMBER_OF_ANTS)) {
			M = Integer.parseInt(parameters.get(NUMBER_OF_ANTS));
			if(M <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(NUMBER_OF_ANTS);
		}
		if(parameters.containsKey(EVAPORATION_FACTOR)) {
			rho = Double.parseDouble(parameters.get(EVAPORATION_FACTOR));
			if(rho < 0 || rho > 1) {
				throw new IllegalArgumentException();
			}
			parameters.remove(EVAPORATION_FACTOR);
		}
		
		if(parameters.containsKey(MCR_THRESHOLD)) {
			threshold = Double.parseDouble(parameters.get(MCR_THRESHOLD));
			if(threshold < 0 || threshold > 1) {
				throw new IllegalArgumentException();
			}
			parameters.remove(MCR_THRESHOLD);
		}
		
		if(parameters.containsKey(NUMMER_OF_NEIGHBORS)) {
			nearNbors = Integer.parseInt(parameters.get(NUMMER_OF_NEIGHBORS));
			if(nearNbors < 0 || nearNbors >= nodeNr) {
				throw new IllegalArgumentException();
			}
			parameters.remove(NUMMER_OF_NEIGHBORS);
		}
	
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
	
}

