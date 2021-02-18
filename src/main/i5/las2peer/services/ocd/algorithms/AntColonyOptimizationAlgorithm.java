package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.algorithms.utils.MaximalCliqueGraphRepresentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.lang.Double; 
import java.lang.Math;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.dense.BasicVector;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;

/**
* The original version of the overlapping community detection algorithm introduced in 2020
* by Ping Ji, Shanxin Zhang, Zhiping Zhou.
* @author Marlene Damm
*/
//TODO description of the algorithm
public class AntColonyOptimizationAlgorithm implements OcdAlgorithm {
	
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
	 * Number of objective functions used in this algorithm. The proposed algorithm by Ji et al uses 2 objective functions. So we recommend to this parameter to be 2. 
	 */
	private int objectFkt = 2;
	
	/**
	 * saves all best found community solutions
	 */
	private List<Node> EP;
	
	/**
	 * Heuristic information matrix: shows how similar to nodes. Nodes which are more similar are more likely to be in 
	 * the same community. The values are between 0 and 1 which 0 being not connected and 1 being very similar. 
	 * 
	 */
	private Matrix heuristic; 
	
	
	public AntColonyOptimizationAlgorithm() {
		//TODO
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
	 */
	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) 
			throws OcdAlgorithmException, InterruptedException {
		// maximal clique search 
		MaximalCliqueGraphRepresentation MCR = new MaximalCliqueGraphRepresentation();
		HashMap<Integer,HashSet<Node>> maxClq = MCR.cliques(graph);
		
		// determining the link strength in between the cliques
		Matrix lkstrgth = linkStrength(graph, maxClq);
		
		//creating the encoding
		int clqNr = maxClq.size(); 
		CustomGraph encoding = new CustomGraph(); 
		for(int i = 0; i < clqNr; i++) {//creating clique nodes
			encoding.createNode(); 
		}
		Node[] nodes = encoding.getNodeArray();
		for(Node n1: nodes) { // creating clique edges 
			int i1 = n1.index();
			for(Node n2: nodes) {
				int i2 = n2.index();
				if(lkstrgth.get(i1, i2)>=threshold) { // leaving out weak edges
					encoding.createEdge(n1, n2);
				}
			}
		}
		
		//initialization of the weight vectors of the subproblems 
		List<Vector> lambda = new ArrayList<Vector>(); //contains the weight vector for the M sub-problems 
		double[] v_help = {0,1};
		Vector v = new BasicVector(v_help);
		
		for(int i = 0; i < M; i++) {
			lambda.add(v);
		}
		
		// fill in the heuristic information matrix
		heuristic = new Basic2DMatrix(clqNr,clqNr);  
		int nodeNr = graph.nodeCount(); 
		int edgeNr = graph.edgeCount();
		
		for(int i = 0; i < nodeNr; i++) {
			double nborsNr1 = nodes[i].degree(); 
			double mu1 = nborsNr1/nodeNr; 
			double std1 = (nborsNr1*Math.pow(1-mu1, 2)+(nodeNr-nborsNr1)*Math.pow(mu1, 2))/nodeNr;
			std1 = Math.sqrt(std1); 
			for(int j = 0; j < nodeNr; j++) {
				double nborsNr2 = nodes[j].degree(); 
				double mu2 = nborsNr2/nodeNr; 
				double std2 = (nborsNr2*Math.pow(1-mu2, 2)+(nodeNr-nborsNr2)*Math.pow(mu2, 2))/nodeNr;
				std1 = Math.sqrt(std2); 
				
				//double pearson = ()/(nodeNr*std1*std2);
		
				double n = 1/(1+Math.E);
				
				heuristic.set(i, j, n);
			}
		}
		
		//TODO add Ant colony Optimization here
		
		return new Cover(graph);
		
	}
	
	
	//TODO add commentaries here
	protected Matrix linkStrength(CustomGraph graph, HashMap<Integer,HashSet<Node>> maxClq) {
		int clqNr = maxClq.size(); 
		Matrix lkstrgth = new Basic2DMatrix(clqNr,clqNr);
		
		for(int i = 0; i < clqNr; i++) {
			HashSet<Node> clq1 = maxClq.get(i);
			double clq1Size = clq1.size();
			for(int j = i + 1; j < clqNr; j++) {
				HashSet<Node> clq2 = maxClq.get(j);
				double clq2Size = clq2.size();
				
				HashSet<Node> diff12 = new HashSet<Node>(clq1); 
				diff12.removeAll(clq2);
				double diff12size = diff12.size();
				
				double cdDist1 = 0;
				for(Node v1: diff12) {
					for(Node v2: clq2) {
						cdDist1 += CzechkanowskiDice(graph, v1, v2); 
					}
				}
				
				HashSet<Node> diff21 = new HashSet<Node>(clq2); 
				diff21.removeAll(clq1);
				double diff21size = diff21.size();
				
				double cdDist2 = 0;
				for(Node v1: diff21) {
					for(Node v2: clq1) {
						cdDist2 += CzechkanowskiDice(graph, v1, v2); 
					}
				}
				
				double lstr = cdDist2/(diff21size*clq1Size)*cdDist1/(diff12size*clq2Size);
				lstr = Math.sqrt(lstr);
				lkstrgth.set(i, j, lstr);
				
			}
		}
		
		return lkstrgth;
	}
	
	/**
	 * Version of the Czechkanowski Dice Distance
	 * @param graph a graph from which v1 and v2 are taken
	 * @param v1 node which is in a clique
	 * @param v2 node which is not in the same clique as v1
	 * @return
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
				
				if(nbors2.ok() == true){
					nbors2.cyclicNext();
				}
				else {
					break;
				}
			}
			
			if(nbors1.ok() == true){
				nbors1.cyclicNext();
			}
			else {
				break;
			}
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
	
	//TODO add commentaries
	protected double negativeRatioAssociation(CustomGraph graph, Cover cover) {
		double NRA = 0; 
		Matrix memberships = cover.getMemberships();
		int cols = memberships.columns(); 
		for(int i = 0; i<cols; i++) {
			Vector v = memberships.getColumn(i); 
			double vSum = cover.getCommunityMemberIndices(i).size();
			
			NRA -= cliqueInterconectivity(graph, v, v)/(2*vSum);
			
		}
		
		return NRA;
	}
	
	//TODO add commentaries
	protected double cutRatio(CustomGraph graph, Cover cover) {
		double CR = 0; 
		Matrix memberships = cover.getMemberships();
		int cols = memberships.columns(); 

		for(int i = 0; i<cols; i++) {
			Vector v = memberships.getColumn(i); 
			List<Integer> comNodes = cover.getCommunityMemberIndices(i); 
			
			double[] one = new double[graph.nodeCount()]; 
			Arrays.fill(one, 1);
			Vector v_compl = new BasicVector(one);
			for(int cn: comNodes) {
				v_compl.set(cn,0);
			}
			
			double vSum = comNodes.size();
			
			CR += cliqueInterconectivity(graph, v, v_compl)/vSum;
		}
		
		return CR;
	}
	
	//TODO add commentaries
	protected double cliqueInterconectivity(CustomGraph graph, Vector com1, Vector com2) {
		double L = 0; 
		int com1Len = com1.length(); 
		Node[] nodes = graph.getNodeArray(); 
		for(int i = 0; i < com1Len; i++) {
			if(com1.get(i) == 0) {
				continue;
			}
			Node n1 = nodes[i]; 
			for(int j = 0; j < com1Len; j++) {
				if(com2.get(j) == 0) {
					continue;
				}
				Node n2 = nodes[j];
				
				if (graph.containsEdge(n1, n2)) {
					L += 1; 
				}
			}
		}
		return L;
	}
	
	protected void initialization() {
		
	}
	
	protected void constructSolution() {
		//TODO
	}
	
	protected void updateEP() {
		//TODO
	}
	
	protected void updatePheromoneMatrix() {
		//TODO
	}
	
	protected void updateCurrentSolution() {
		//TODO
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
		return  new HashMap<String, String>();
	}

	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		
	}
	
	
}

