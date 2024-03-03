package i5.las2peer.services.ocd.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.*;

/**
 * Implements the algorithm by Y. Chen, X.L. Wang, B. Yuan and B.Z. Tang:
 * Overlapping community detection in networks with positive and negative links
 * https://doi.org/10.1088/1742-5468/2014/03/p03021
 * @author YLi
 */
public class SignedProbabilisticMixtureAlgorithm implements OcdAlgorithm {
	/**
	 * The number of trials. The default value is 3. Must be greater than 0.
	 */
	private int trialCount = 3;
	/**
	 * The number of communities to assign nodes to. The default value is 6. Must be greater than 0.
	 */
	private int communityCount = 6;
	/**
	 * Minimum difference between cover likelihoods to continue with E and M steps
	 */
	private double minLikelihoodDifference = 1;
	
	private Matrix edgeProbabilities;	 // edge_community wrs
	private Matrix nodeProbabilities; // community_node 0ri

	/*
	 * PARAMETER NAMES
	 */
	public static final String TRIAL_COUNT_NAME = "trialCount";
	public static final String COMMUNITY_COUNT_NAME = "communityCount";
	public static final String MIN_LIKELIHOOD_DIFFERENCE_NAME = "minLikelihoodDifference";
	

	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.SIGNED_PROBABILISTIC_MIXTURE_ALGORITHM;
	}

	@Override
	public void setParameters(Map<String, String> parameters) {
		if (parameters.containsKey(TRIAL_COUNT_NAME)) {
			trialCount = Integer.parseInt(parameters.get(TRIAL_COUNT_NAME));
			parameters.remove(TRIAL_COUNT_NAME);
			if (trialCount <= 0) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.containsKey(COMMUNITY_COUNT_NAME)) {
			communityCount = Integer.parseInt(parameters.get(COMMUNITY_COUNT_NAME));
			parameters.remove(COMMUNITY_COUNT_NAME);
			if (communityCount <= 0) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.containsKey(MIN_LIKELIHOOD_DIFFERENCE_NAME)) {
			minLikelihoodDifference = Double.parseDouble(parameters.get(MIN_LIKELIHOOD_DIFFERENCE_NAME));
			parameters.remove(MIN_LIKELIHOOD_DIFFERENCE_NAME);
			if (minLikelihoodDifference <= 0) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(TRIAL_COUNT_NAME, Integer.toString(trialCount));
		parameters.put(COMMUNITY_COUNT_NAME, Integer.toString(communityCount));
		parameters.put(MIN_LIKELIHOOD_DIFFERENCE_NAME, Double.toString(minLikelihoodDifference));
		return parameters;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.NEGATIVE_WEIGHTS);
		compatibilities.add(GraphType.WEIGHTED);
		return compatibilities;
	}

	/**
	 * Creates a standard instance of the algorithm. All attributes are assigned
	 * their default values.
	 */
	public SignedProbabilisticMixtureAlgorithm() {

	}

	public SignedProbabilisticMixtureAlgorithm(int trials, int communities, double minLikelihoodDiff) {
		this.trialCount = trials;
		this.communityCount = communities;
		this.minLikelihoodDifference = minLikelihoodDiff;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
			try {	
				initialProbabilitiesRandom(graph);//TODO: Function to calculate initial probabilities other than completely random
				Matrix bestEdgeProbabilities = new CCSMatrix(edgeProbabilities);	 // edge_community wrs
				Matrix bestNodeProbabilities = new CCSMatrix(nodeProbabilities); 
				
				double maxLogLikelihood = -Double.MAX_VALUE;			
				
				for(int i=0; i<trialCount; i++) {				
					double logLikelihoodPrev = -Double.MAX_VALUE;
					double logLikelihood = -Double.MAX_VALUE + 1;
					
//DEBUG					int iterator = 0;
					
					while(logLikelihood > logLikelihoodPrev && Math.abs(logLikelihood - logLikelihoodPrev) >= minLikelihoodDifference) { //TODO: Necessary difference subject to change, maybe let the user choose?																		
						emStep(graph);
						logLikelihoodPrev = logLikelihood;
						logLikelihood = calculateLikelihood(graph);
						
//DEBUG						System.out.println(i + ": " + logLikelihood);
//DEBUG						for(int u=0; u<k;u++) {
//DEBUG							System.out.print(nodeProbabilities.getRow(u).sum() + ",");
//DEBUG						}
//DEBUG						System.out.println();
//DEBUG						System.out.println(nodeProbabilities);
//DEBUG						System.out.println(edgeProbabilities.sum());
//DEBUG						System.out.println(edgeProbabilities);
												
						if(Thread.interrupted()) 
						{
							throw new InterruptedException();
						}
						
//DEBUG						if(iterator == 20) {
//DEBUG							break;
//DEBUG						}
//DEBUG						iterator++;
					}
					if(logLikelihood > maxLogLikelihood) {
//DEBUG						System.out.println("BEST: " + logLikelihood);
						maxLogLikelihood = logLikelihood;
						bestEdgeProbabilities = new CCSMatrix(edgeProbabilities);
						bestNodeProbabilities = new CCSMatrix(nodeProbabilities);
					}
					
					initialProbabilitiesRandom(graph); //TODO: Function to calculate initial probabilities other than completely random
				}				
				edgeProbabilities = bestEdgeProbabilities;
				nodeProbabilities = bestNodeProbabilities;
				
				Matrix membershipMatrix = getMembershipMatrix(graph);
				Cover cover = new Cover(graph, membershipMatrix);
				return cover;
			} catch (InterruptedException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				throw new OcdAlgorithmException(e);
			}		
	}		
	
	/**
	 * Sets the initial edge and node selection probabilities with random values based on system time in nanoseconds
	 * @param graph The graph the algorithm runs on
	 */
	public void initialProbabilitiesRandom(CustomGraph graph) {
		Random rndGenerator = new Random();
		
		edgeProbabilities = new CCSMatrix(communityCount,communityCount);
		nodeProbabilities = new CCSMatrix(communityCount,graph.getNodeCount());
		
		// Set w_rs
		for(int r=0; r<communityCount; r++) {
			for(int s=0; s<communityCount; s++) {
				rndGenerator.setSeed(System.nanoTime());
				edgeProbabilities.set(r, s, rndGenerator.nextDouble() *100 + 1.0);
			}
		}
		
		// Set 0_ri
		Node nodes[] = graph.nodes().toArray(Node[]::new);
		for(int r=0; r<communityCount; r++) {
			for(int i=0; i<graph.getNodeCount(); i++) {
					rndGenerator.setSeed(System.nanoTime());
					nodeProbabilities.set(r, nodes[i].getIndex(), rndGenerator.nextDouble() *100 + 1.0);
			}
			nodeProbabilities.setRow(r, nodeProbabilities.getRow(r).multiply(1.0 / nodeProbabilities.getRow(r).sum()));			
		}
	}
	
	/**
	 * Calculates the membership values for every node by dividing the product between all outgoing edge probabilities and the selection probability for this node through the sum of all those products 
	 * @param graph The graph the algorithm is run on
	 * @return the membership matrix. Nodes are rows, communities columns
	 */
	public Matrix getMembershipMatrix(CustomGraph graph) {
		Matrix membershipMatrix = new CCSMatrix(graph.getNodeCount(), communityCount);
		for(Node node : graph.nodes().toArray(Node[]::new)) {
			double outgoingProbSum[] = new double[communityCount];
			double allProbSum = 0.0;
			
			for(int r=0; r<communityCount; r++) {
				outgoingProbSum[r] = 0.0;
				for(int s=0; s<communityCount; s++) {
					outgoingProbSum[r] += edgeProbabilities.get(r, s) * nodeProbabilities.get(r, node.getIndex());
				}
				allProbSum += outgoingProbSum[r];
			}
			
			for(int r=0; r<communityCount; r++) {
				membershipMatrix.set(node.getIndex(), r, (allProbSum != 0 ? outgoingProbSum[r] / allProbSum : 1.0/communityCount));
			}
		}
		return membershipMatrix;
	}
	
	/**
	 * Calculates the E and the M Step for the algorithm.
	 * In the E step, the probability for a positive and a negative edge for a given community or between two given communities are calculated
	 * In the M step, the probabilities for each edge to select each two or one communities and the probability for each community to select each node are then re-calculated
	 * @param graph The graph the algorithm is run on
	 */
	public void emStep(CustomGraph graph) {
		
		//E STEP
		HashMap<Edge, Vector> posEdgeHiddenCommProbs = new HashMap<Edge, Vector>(); //q_ijr
		HashMap<Edge, Matrix> negEdgeHiddenCommProbs = new HashMap<Edge, Matrix>(); //Q_ijrs (r!=s), 0 (r=s)
		for(Edge edge : graph.edges().toArray(Edge[]::new)) {
			if(graph.getEdgeWeight(edge) >= 0) {
				Vector positiveProbabilities = new BasicVector(communityCount);
				double previousEdgeProbsSum = 0.0;
				for(int r=0; r<communityCount; r++) {
					previousEdgeProbsSum += edgeProbabilities.get(r, r)*nodeProbabilities.get(r, edge.getSourceNode().getIndex())*nodeProbabilities.get(r, edge.getTargetNode().getIndex()); // E_r(wrr*0ri*0rj)
				}
				for(int r=0; r<communityCount; r++) {
					positiveProbabilities.set(r, (previousEdgeProbsSum == 0.0 ? Double.MIN_VALUE : edgeProbabilities.get(r, r)*nodeProbabilities.get(r, edge.getSourceNode().getIndex())*nodeProbabilities.get(r, edge.getTargetNode().getIndex()) / previousEdgeProbsSum)); // wrr*0ri*0rj / E_r(wrr*0ri*0rj)
				}
				posEdgeHiddenCommProbs.put(edge, positiveProbabilities);
			}
			else {
				Matrix negativeProbabilities = new CCSMatrix(communityCount,communityCount);
				double previousEdgeProbsSum = 0.0;
				for(int r=0; r<communityCount; r++) {
					for(int s=0; s<communityCount; s++) {
						if(r!=s) {
							previousEdgeProbsSum += edgeProbabilities.get(r, s)*nodeProbabilities.get(r, edge.getSourceNode().getIndex())*nodeProbabilities.get(s, edge.getTargetNode().getIndex()); // E_rs(wrs*0ri*0sj)
						}
						else 
						{
							previousEdgeProbsSum += 0;
						}
					}
				}
				for(int r=0; r<communityCount; r++) {
					for(int s=0; s<communityCount; s++) {
						if(r!=s) {
							negativeProbabilities.set(r,s, (previousEdgeProbsSum == 0.0 ? Double.MIN_VALUE : edgeProbabilities.get(r, s)*nodeProbabilities.get(r, edge.getSourceNode().getIndex())*nodeProbabilities.get(s, edge.getTargetNode().getIndex()) / previousEdgeProbsSum)); // wrs*0ri*0sj / E_rs(wrs*0ri*0sj)
						}
						else 
						{
							negativeProbabilities.set(r,s,0); //case for r==s
						}
					}
				}
				negEdgeHiddenCommProbs.put(edge, negativeProbabilities);
			}
		}
		
		//M STEP
		Matrix edgeHiddenProbSums = new CCSMatrix(communityCount,communityCount); // w_rs, on diagonal: w_rr
		Matrix nodeHiddenProbSums = new CCSMatrix(communityCount,graph.getNodeCount()); // 0_ri
		for(int r=0; r<communityCount; r++) {
			for(int s=0; s<communityCount; s++) {				
				for(Edge edge : graph.edges().toArray(Edge[]::new)) {
					if(r!=s && graph.getEdgeWeight(edge) < 0)
					{
						edgeHiddenProbSums.set(r,s, edgeHiddenProbSums.get(r, s) + negEdgeHiddenCommProbs.get(edge).get(r,s) * (-graph.getEdgeWeight(edge))); // E_ij(Q_ijrs * A^-_ij) for w_rs
						
						nodeHiddenProbSums.set(r, edge.getSourceNode().getIndex(), nodeHiddenProbSums.get(r, edge.getSourceNode().getIndex()) + negEdgeHiddenCommProbs.get(edge).get(r,s) * (-graph.getEdgeWeight(edge))); // E_js(Q_ijrs * A^-_ij) for 0_ri
					}
					else if(r==s && graph.getEdgeWeight(edge) >= 0)
					{
						edgeHiddenProbSums.set(r,r, edgeHiddenProbSums.get(r, r) + posEdgeHiddenCommProbs.get(edge).get(r) * graph.getEdgeWeight(edge)); // E_ij(q_ijr * A^+_ij) for w_rr
						
						nodeHiddenProbSums.set(r, edge.getSourceNode().getIndex(), nodeHiddenProbSums.get(r, edge.getSourceNode().getIndex()) + posEdgeHiddenCommProbs.get(edge).get(r) * graph.getEdgeWeight(edge)); // E_j(q_ijr * A^+_ij) for 0_ri
					}
				}
			}
		}

		double edgeHiddenProbSumsSum = edgeHiddenProbSums.sum(); // E_ijr(Q_ijrs * A^-_ij) + E_ijr(q_ijr * A^+_ij)
		
		for(int r=0; r<communityCount; r++) {
			for(int s=0; s<communityCount; s++) {
				// No distinction between pos/neg necessary, values of matrix already take that into account
				edgeProbabilities.set(r, s, edgeHiddenProbSums.get(r,s) / edgeHiddenProbSumsSum); // Calculate w_rr and w_rs
			}
		}
		
		double nodeHiddenProbSumsSum[] = new double[communityCount];
		for(int r=0; r<communityCount; r++) {
			nodeHiddenProbSumsSum[r] = nodeHiddenProbSums.getRow(r).sum(); // E_ij(Q_ijrs * A^-_ij) + E_ij(q_ijr * A^+_ij) for each r
		}
		
		for(int r=0; r<communityCount; r++) {
			for(Node node : graph.nodes().toArray(Node[]::new)) {
				nodeProbabilities.set(r, node.getIndex(), nodeHiddenProbSums.get(r, node.getIndex()) / nodeHiddenProbSumsSum[r] ); // Calculate 0_ri				
			}			
		}
		
	}
	
	/**
	 * Calculates the log-likelihood for the current cover, this is used as a stop criterion for the EM-Step
	 * @param graph The graph the algorithm is run on
	 * @return the log-likelihood for the current cover (double)
	 */
	public double calculateLikelihood(CustomGraph graph) {
		double logLikelihood = 0.0;
		
		for(Edge edge : graph.edges().toArray(Edge[]::new)) {
			double communityProbSum = 0.0;
			if(graph.getEdgeWeight(edge) < 0) 
			{
				for(int r=0; r<communityCount; r++) {
					for(int s=0; s<communityCount; s++) {
						if(r!=s) 
						{
							communityProbSum += edgeProbabilities.get(r, s) * nodeProbabilities.get(r, edge.getSourceNode().getIndex()) * nodeProbabilities.get(s, edge.getTargetNode().getIndex());
						}
					}
				}
			}
			else 
			{
				for(int r=0; r<communityCount; r++) {
					communityProbSum += edgeProbabilities.get(r, r) * nodeProbabilities.get(r, edge.getSourceNode().getIndex()) * nodeProbabilities.get(r, edge.getTargetNode().getIndex());
				}
			}
			logLikelihood += (graph.getEdgeWeight(edge) > 0 ? graph.getEdgeWeight(edge) : -graph.getEdgeWeight(edge)) * (communityProbSum == 0.0 ? -Double.MAX_VALUE : Math.log(communityProbSum));			
		}
		
		return logLikelihood;
	}
}
