package i5.las2peer.services.ocd.algorithms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang3.SystemUtils;

import java.util.Scanner;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.benchmarks.OcdBenchmarkException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.Node;
import y.base.Edge;
import y.base.EdgeCursor;

import java.security.SecureRandom;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.*;

/**
 * @author YLi
 */
public class SignedProbabilisticMixtureAlgorithm implements OcdAlgorithm {
	/**
	 * Path of the directory reserved for the application.
	 */
	private static final String DirectoryPath = "ocd/spm/";
	/**
	 * Used for synchronization purposes. Executes the application execution.
	 */
	private static DefaultExecutor executor = new DefaultExecutor();
	/**
	 * Path of the application for Linux based on the source codes of Chen et. al.
	 */
	/**
	 * The number of trials. The default value is 3. Must be greater than 0.
	 */
	private int n = 3;
	/**
	 * The number of communities to assign nodes to. The default value is 6. Must be greater than 0.
	 */
	private int k = 6;
	
	private Matrix edgeProbabilities;	 // edge_community wrs
	private Matrix nodeProbabilities; // community_node 0ri

	/*
	 * PARAMETER NAMES
	 */
	protected final String TRIALCOUNT_NAME = "n";
	protected final String COMMUNITYCOUNT_NAME = "k";

	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.SIGNED_PROBABILISTIC_MIXTURE_ALGORITHM;
	}

	@Override
	public void setParameters(Map<String, String> parameters) {
		if (parameters.containsKey(TRIALCOUNT_NAME)) {
			n = Integer.parseInt(parameters.get(TRIALCOUNT_NAME));
			parameters.remove(TRIALCOUNT_NAME);
			if (n <= 0) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.containsKey(COMMUNITYCOUNT_NAME)) {
			k = Integer.parseInt(parameters.get(COMMUNITYCOUNT_NAME));
			parameters.remove(COMMUNITYCOUNT_NAME);
			if (k <= 0) {
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
		parameters.put(TRIALCOUNT_NAME, Integer.toString(n));
		parameters.put(COMMUNITYCOUNT_NAME, Integer.toString(k));
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

	public SignedProbabilisticMixtureAlgorithm(int n, int k) {
		this.n = n;
		this.k = k;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
		synchronized (executor) {
			try {				
				//TODO: Function to calculate initial probabilities
				
				
				double logLikelihoodPrev = 0.0;
				double logLikelihood = 0.0;
				while(Math.abs(logLikelihood - logLikelihoodPrev) >= 0.01) { //TODO: Necessary difference subject to change, maybe let the user choose?
					emStep(graph);
					logLikelihoodPrev = logLikelihood;
					logLikelihood = calculateLikelihood(graph);
				}

				Matrix membershipMatrix = getMembershipMatrix();
				Cover cover = new Cover(graph, membershipMatrix); //TODO: Function for membership matrix creation
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
	}		
	
	/**
	 * Calculates the membership values for every node by dividing the product between all outgoing edge probabilities and the selection probability for this node through the sum of all those products 
	 * @param graph The graph the algorithm is run on
	 * @return the membership matrix. Nodes are rows, communities columns
	 */
	public Matrix getMembershipMatrix(CustomGraph graph) {
		Matrix membershipMatrix = new CCSMatrix(graph.nodeCount(), k);
		for(Node node : graph.getNodeArray()) {
			double outgoingProbSum[] = new double[k];
			double allProbSum = 0.0;
			
			for(int r=0; r<k; r++) {
				outgoingProbSum[r] = 0.0;
				for(int s=0; s<k; s++) {
					outgoingProbSum[r] += edgeProbabilities.get(r, s) * nodeProbabilities.get(r, node.index());
				}
				allProbSum += outgoingProbSum[r];
			}
			
			for(int r=0; r<k; r++) {
				membershipMatrix.set(node.index(), r, outgoingProbSum[r] / allProbSum);
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
		for(Edge edge : graph.getEdgeArray()) {			
			if(graph.getEdgeWeight(edge) >= 0) {
				Vector positiveProbabilities = new BasicVector(k);
				//Matrix edgeProbabilityMatrix = edgeProbabilities.get(edge);
				double previousEdgeProbsSum = 0.0;
				for(int r=0; r<k; r++) {
					previousEdgeProbsSum += edgeProbabilities.get(r, r)*nodeProbabilities.get(r, edge.source().index())*nodeProbabilities.get(r, edge.target().index()); //E_r(wrr*0ri*0rj)
				}
				for(int r=0; r<k; r++) {
					positiveProbabilities.set(r, edgeProbabilities.get(r, r)*nodeProbabilities.get(r, edge.source().index())*nodeProbabilities.get(r, edge.target().index()) / previousEdgeProbsSum); // wrr*0ri*0rj / E_r(wrr*0ri*0rj)
				}
				posEdgeHiddenCommProbs.put(edge, positiveProbabilities);
			}
			else {
				Matrix negativeProbabilities = new CCSMatrix(k,k);
				//Matrix edgeProbabilityMatrix = edgeProbabilities.get(edge);
				double previousEdgeProbsSum = 0.0;
				for(int r=0; r<k; r++) {
					for(int s=0; s<k; s++) {
						previousEdgeProbsSum += edgeProbabilities.get(r, s)*nodeProbabilities.get(r, edge.source().index())*nodeProbabilities.get(s, edge.target().index()); // E_rs(wrs*0ri*0sj)
					}
				}
				for(int r=0; r<k; r++) {
					for(int s=0; s<k; s++) {
						if(r!=s) {
							negativeProbabilities.set(r,s, edgeProbabilities.get(r, s)*nodeProbabilities.get(r, edge.source().index())*nodeProbabilities.get(s, edge.target().index()) / previousEdgeProbsSum); // wrs*0ri*0sj / E_rs(wrs*0ri*0sj)
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
		
		//M STEP TODO:
		Matrix edgeHiddenProbSums = new CCSMatrix(k,k); // w_rs, on diagonal: w_rr
		Matrix nodeHiddenProbSums = new CCSMatrix(k,graph.nodeCount()); // 0_ri
		for(int r=0; r<k; r++) {
			for(int s=0; s<k; s++) {				
				for(Edge edge : graph.getEdgeArray()) {			
					if(r!=s && graph.getEdgeWeight(edge) < 0)
					{
						edgeHiddenProbSums.set(r,s, edgeHiddenProbSums.get(r, s) + negEdgeHiddenCommProbs.get(edge).get(r,s) * graph.getEdgeWeight(edge)); // E_ij(Q_ijrs * A^-_ij) for w_rs
						
						nodeHiddenProbSums.set(r, edge.source().index(), nodeHiddenProbSums.get(r, edge.source().index()) + negEdgeHiddenCommProbs.get(edge).get(r,s) * graph.getEdgeWeight(edge)); // E_js(Q_ijrs * A^-_ij) for 0_ri
					}
					else if(r==s && graph.getEdgeWeight(edge) >= 0)
					{
						edgeHiddenProbSums.set(r,r, edgeHiddenProbSums.get(r, r) + posEdgeHiddenCommProbs.get(edge).get(r) * graph.getEdgeWeight(edge)); // E_ij(q_ijr * A^+_ij) for w_rr
						
						nodeHiddenProbSums.set(r, edge.source().index(), nodeHiddenProbSums.get(r, edge.source().index()) + posEdgeHiddenCommProbs.get(edge).get(r) * graph.getEdgeWeight(edge)); // E_j(q_ijr * A^+_ij) for 0_ri
					}
				}
			}
		}
		
		double edgeHiddenProbSumsSum = edgeHiddenProbSums.sum(); // E_ijr(Q_ijrs * A^-_ij) + E_ijr(q_ijr * A^+_ij)
		
		for(int r=0; r<k; r++) {
			for(int s=0; s<k; s++) {
				// No distinction between pos/neg necessary, values of matrix already take that into account
				edgeProbabilities.set(r, s, edgeHiddenProbSums.get(r,s) / edgeHiddenProbSumsSum); // Calculate w_rr and w_rs
			}
		}
		
		double nodeHiddenProbSumsSum[] = new double[k];
		for(int r=0; r<k; r++) {
			nodeHiddenProbSumsSum[r] = nodeHiddenProbSums.getRow(r).sum(); // E_ij(Q_ijrs * A^-_ij) + E_ij(q_ijr * A^+_ij) for each r
		}
		
		for(int r=0; r<k; r++) {
			for(Node node : graph.getNodeArray()) {
				nodeProbabilities.set(r, node.index(), nodeHiddenProbSums.get(r, node.index()) / nodeHiddenProbSumsSum[r] ); // Calculate 0_ri
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
		
		for(Edge edge : graph.getEdgeArray()) {
			double communityProbSum = 0.0;
			if(graph.getEdgeWeight(edge) < 0) 
			{
				for(int r=0; r<k; r++) {
					for(int s=0; s<k; s++) {
						if(r!=s) 
						{
							communityProbSum += edgeProbabilities.get(r, s) * nodeProbabilities.get(r, edge.source().index()) * nodeProbabilities.get(s, edge.target().index());
						}
					}
				}
			}
			else 
			{
				for(int r=0; r<k; r++) {
					communityProbSum += edgeProbabilities.get(r, r) * nodeProbabilities.get(r, edge.source().index()) * nodeProbabilities.get(r, edge.target().index());
				}
			}
			logLikelihood += graph.getEdgeWeight(edge) * Math.log(communityProbSum);
		}
		
		return logLikelihood;
	}
}
