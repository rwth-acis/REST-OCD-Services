package i5.las2peer.services.ocd.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.la4j.matrix.Matrix;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.Termmatrix;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.algorithms.utils.Similarities;
import i5.las2peer.services.ocd.algorithms.utils.Point;
import y.base.Node;
import i5.las2peer.services.ocd.algorithms.utils.Cluster;
import i5.las2peer.services.ocd.algorithms.utils.CostFunction;
import i5.las2peer.services.ocd.algorithms.utils.Clustering;

public class CostFunctionOptimizationClusteringAlgorithm implements OcdAlgorithm{
	
	
	/////////////////
	////Variables////
	/////////////////
	
	/**
	 * The upper bound for the number of clusters possible. Computing Clusterings between
	 * 1 and the bound, to find the number of clusters with the smallest costs. Default value 50.
	 */
	
	private int maximumK = 50;
	
	/**
	 * The threshold to determine whether a node is assigned to another community,
	 * because its distance to that community centroid is smaller that the given threshold.
	 * Default value 0.5.
	 */
	private double beta = 0.5;
	
	////////////////////////
	//// Parameter Names////
	////////////////////////
	
	public static final String MAXIMUM_K_NAME = "maximumK";
	public static final String OVERLAPPING_THRESHOLD_NAME = "overlappingThreshold";
	
	////////////////////
	//// Constructor////
	////////////////////
	
	public CostFunctionOptimizationClusteringAlgorithm(){
		
	}
	
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.CONTENT_UNLINKED);
		return compatibilities;
	}
	
	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.COST_FUNC_OPT_CLUSTERING_ALGORITHM;
	}
	
	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(MAXIMUM_K_NAME, Integer.toString(maximumK));
		parameters.put(OVERLAPPING_THRESHOLD_NAME, Double.toString(beta));
		return parameters;
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.containsKey(MAXIMUM_K_NAME)){
			maximumK = Integer.parseInt(parameters.get(MAXIMUM_K_NAME));
			if(maximumK <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(MAXIMUM_K_NAME);
		}
		if(parameters.containsKey(OVERLAPPING_THRESHOLD_NAME)){
			beta = Double.parseDouble(parameters.get(OVERLAPPING_THRESHOLD_NAME));
			if(beta < 0 || beta > 1 ){
				throw new IllegalArgumentException();
			}
			parameters.remove(OVERLAPPING_THRESHOLD_NAME);
		}
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException,InterruptedException {
		Termmatrix termMat = new Termmatrix(graph);
		Clustering opt = new Clustering();
		Clustering temp = new Clustering();
		if(maximumK > termMat.getNodeIdList().size()){
			return null;
		}
		opt = gradDescClustering(termMat, 1);
		for(int i = 2; i <= maximumK; i++){
			temp = gradDescClustering(termMat, i);
			if(opt.getCosts() > temp.getCosts() && !temp.containsEmpty()){
				opt = temp;
			}
		}
		opt = overlappCommunities(opt);
		Matrix membershipMatrix = opt.createMembershipMatrix(graph);
		Cover res = new Cover(graph,membershipMatrix);
		return res;
	}
	
	

	protected Clustering gradDescClustering(Termmatrix termMat, int k){
		Clustering clustering = new Clustering();
		Clustering temp = new Clustering();
		CostFunction costFunc = new CostFunction();
		Random randGen = new Random();
		int vectorLength = termMat.getMatrix().getColumnDimension();
		boolean change = true;
		double costs = 0;
		
		//initialize clusters with centroids randomly generated
		for(int i = 0; i < k; i++){
			Cluster c = new Cluster();
			ArrayRealVector cent = new ArrayRealVector(vectorLength);
			for(int j = 0; j < vectorLength; j++){
				cent.setEntry(j, randGen.nextDouble());
			}
			c.setCentroid(cent);
			clustering.addCluster(c);
		}
		
		//assign nodes and update centroids
		clustering.setClustering(assignCluster(clustering.getClustering(),termMat.getMatrix(), termMat.getNodeIdList()));
		clustering.setCosts(costFunc.valueNode(clustering.getClustering(), termMat.getNodeIdList().size()));
		
		//assign nodes and update centroids until costs don't change
		//while(change){
		while(true){
			temp.setClustering(updateCentroids(clustering.getClustering()));
			temp.clearCluster();
			temp.setClustering(assignCluster(temp.getClustering(),termMat.getMatrix(), termMat.getNodeIdList()));
			temp.setCosts(costFunc.valueNode(temp.getClustering(), termMat.getNodeIdList().size()));
			//LinkedList<ArrayRealVector> centroids = getCentroids(clustering.getClustering());
			//clustering.setClustering(updateCentroids(clustering.getClustering()));
			//change = different(centroids,getCentroids(clustering.getClustering()));
			//costs = costFunc.value(clustering.getClustering(), termMat.getNodeIdList().size());
			//clustering.setCosts(costs);
			if(clustering.getCosts() == temp.getCosts()){
				break;
			}
			if(clustering.getCosts() > temp.getCosts()){
				clustering.setClustering(temp.getClustering());
				clustering.setCosts(temp.getCosts());
			}
		}
		
		//k++;
		
		
		
		return clustering;
	}
	
	private LinkedList<Cluster> assignCluster(LinkedList<Cluster> clustering, Array2DRowRealMatrix matrix, LinkedList<Node> nodeList){
		double dist = 0;
		double tempDist = 0;
		int index;
		Cluster match;
				 		
		for(int i = 0; i < matrix.getRowDimension(); i++){

			ArrayRealVector vector = (ArrayRealVector) matrix.getRowVector(i);
			Point point = new Point(nodeList.get(i),vector);
			Iterator<Cluster> it = clustering.iterator();
			match = it.next();
			dist = distanceCosSim(match.getCentroid(), vector);
			index = clustering.indexOf(match);
			
			while(it.hasNext()){
				Cluster curr = it.next();
				tempDist = distanceCosSim(curr.getCentroid(), vector);
				if(tempDist <= dist){
					match = curr;
					dist = tempDist;
					index = clustering.indexOf(match);
				}
			}
			match.assignPoint(point);
			clustering.set(index, match);
		}
		
		return clustering;
	}
	
	private LinkedList<Cluster> updateCentroids(LinkedList<Cluster> clust){
		CostFunction costFunc = new CostFunction();
		for(Iterator<Cluster> it = clust.iterator(); it.hasNext();){
			Cluster curr = it.next();
			ArrayRealVector cent = curr.getCentroid();
			cent = cent.add(costFunc.derivativeValue(curr));
			curr.setCentroid(cent);
		}
		
		return clust;
	}
	
	private LinkedList<ArrayRealVector> getCentroids(LinkedList<Cluster> clust){
		LinkedList<ArrayRealVector> res = new LinkedList<ArrayRealVector>();
		for(Iterator<Cluster> it = clust.iterator(); it.hasNext();){
			res.add(it.next().getCentroid());
		}
		return res;
	}
	
	private boolean different(LinkedList<ArrayRealVector> a, LinkedList<ArrayRealVector> b){
		for(int i = 0; i < a.size(); i++){
			if(!a.get(i).equals(b.get(i))){
				return true;
			}
		}
		return false;
	}
	
	private double distanceCosSim(ArrayRealVector v, ArrayRealVector u){
		double dist = 0;
		Similarities sim = new Similarities();
		
		dist = 1 - sim.cosineSim(v, u);
		
		return dist;
	}
	
	private Clustering overlappCommunities(Clustering c) {
		Clustering opt = c;
		LinkedList<Cluster> clusters = c.getClustering();
		LinkedList<Cluster> optC = opt.getClustering();
		CostFunction f = new CostFunction();
		int numbNode = 0;
		double dist = 2;
		for(Iterator<Cluster> it = clusters.iterator(); it.hasNext();){
			Cluster curr = it.next();
			numbNode += curr.getPoints().size();
			for(Iterator<Point> it1 = curr.getPoints().iterator(); it1.hasNext(); ){
				Point p = it1.next();
				for(Iterator<Cluster> it2 = optC.iterator(); it2.hasNext();){
					Cluster clust = it2.next();
					if(!clust.equals(curr)){
					dist = distanceCosSim(clust.getCentroid(),p.getCoordinates());
					}
					if(dist <= beta){
						clust.assignPoint(p);
						dist = 2;
					}
				}
			}
			
		}
		opt.setCosts(f.valueNode(opt.getClustering(), numbNode));
		
		return opt;
	}

}
