package i5.las2peer.services.ocd.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;

import i5.las2peer.services.ocd.algorithms.utils.Clustering;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.Termmatrix;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

public class SVDCostFuncOptClusteringAlgorithm implements OcdAlgorithm{
	/////////////////
	////Variables////
	/////////////////
	
	/**
	* The upper bound for the number of clusters possible. Computing Clusterings between
	* 1 and the bound, to find the number of clusters with the smallest costs. Default value 50.
	*/
	
	private int maximumK = 50;
	
	////////////////////////
	//// Parameter Names////
	////////////////////////
	
	protected static final String MAXIMUM_K_SVD_NAME = "maximumK";
	
	////////////////////
	//// Constructor////
	////////////////////
	
	public SVDCostFuncOptClusteringAlgorithm(){
	
	}
	
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.CONTENT_UNLINKED);
		return compatibilities;
	}
	
	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.SVD_COST_FUNC_OPT_CLUSTERING_ALGORITHM;
	}
	
	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(MAXIMUM_K_SVD_NAME, Integer.toString(maximumK));
		return parameters;
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.containsKey(MAXIMUM_K_SVD_NAME)){
			maximumK = Integer.parseInt(parameters.get(MAXIMUM_K_SVD_NAME));
			if(maximumK <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(MAXIMUM_K_SVD_NAME);
		}
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException,InterruptedException {
		Termmatrix termMat = new Termmatrix(graph);
		termMat.SVD();
		Clustering opt = new Clustering();
		Clustering temp = new Clustering();
		CostFunctionOptimizationClusteringAlgorithm alg = new CostFunctionOptimizationClusteringAlgorithm();
		if(maximumK > termMat.getNodeIdList().size()){
			return null;
		}
		for(int i = 1; i <= maximumK; i++){
			opt = alg.gradDescClustering(termMat, i);
			temp = alg.gradDescClustering(termMat, i+1);
			if(opt.getCosts() > temp.getCosts()){
				opt = temp;
			}
		}
		Matrix membershipMatrix = opt.createMembershipMatrix(graph);
		Cover res = new Cover(graph,membershipMatrix);
		return res;
	}
}
