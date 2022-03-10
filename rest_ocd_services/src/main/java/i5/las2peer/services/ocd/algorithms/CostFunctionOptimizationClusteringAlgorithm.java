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
import i5.las2peer.services.ocd.metrics.ExecutionTime;
import i5.las2peer.services.ocd.algorithms.utils.Similarities;
import i5.las2peer.services.ocd.algorithms.utils.Point;
import y.base.Node;
import i5.las2peer.services.ocd.algorithms.utils.Cluster;
import i5.las2peer.services.ocd.algorithms.utils.CostFunction;
import i5.las2peer.services.ocd.algorithms.utils.Clustering;

/**
 * Implements one of the algorithms by Sabrina Haefele conceived in the thesis:
 * Overlapping Community Detection Based on Content and Authority
 * Some of these algorithms are included in the paper by Shahriari, Mohsen and Haefele, Sabrina and Klamma, Ralf:
 * Contextualized versus structural overlapping communities in social media
 */
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
	private double beta = 0.7;
	
	/**
	 * Boolean variable if the svd version of the algorithm should be used.
	 */
	
	private boolean svd = false;
	
	////////////////////////
	//// Parameter Names////
	////////////////////////
	
	public static final String MAXIMUM_K_NAME = "maximumK";
	public static final String OVERLAPPING_THRESHOLD_NAME = "overlappingThreshold";
	public static final String SVD_NAME = "svd";
	
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
		parameters.put(SVD_NAME, Boolean.toString(svd));
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
		if(parameters.containsKey(SVD_NAME)){
			svd = Boolean.parseBoolean(parameters.get(SVD_NAME));
			parameters.remove(SVD_NAME);
		}
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException,InterruptedException {
		Termmatrix termMat = new Termmatrix(graph);
		graph.setTermMatrix(termMat);
		ExecutionTime time = new ExecutionTime();
		time.start();
		
		if(svd){
			double min = 0;
			Array2DRowRealMatrix m = new  Array2DRowRealMatrix(termMat.SVD().getData());
			for(int ind = 0; ind < m.getRowDimension(); ind++){
				double temp = m.getRowVector(ind).getMinValue();
				if(temp < min){
					min = temp;
				}
			}
			for(int j = 0; j < m.getRowDimension(); j++){
				m.setRowVector(j,m.getRowVector(j).mapAdd(min));
				m.setRowVector(j,m.getRowVector(j).mapMultiply(1000));
			}
			termMat.setMatrix(m);
		}
		Clustering opt = new Clustering();
		Clustering temp = new Clustering();
		if(maximumK > termMat.getNodeIdList().size()){
			return null;
		}
		opt = gradDescClustering(termMat, 2);
		for(int i = 3; i <= maximumK; i++){
			temp = gradDescClustering(termMat, i);
			if(opt.getCosts() > temp.getCosts() && !temp.containsEmpty()){
				opt = temp;
			}
		}
		opt = overlappCommunities(opt, termMat.getNodeIdList());
		Matrix membershipMatrix = opt.createMembershipMatrix(graph);
		Cover res = new Cover(graph,membershipMatrix);
		res.setSimCosts(opt.getCosts());
		time.stop();
		time.setCoverExecutionTime(res);
		return res;
	}
	
	

	protected Clustering gradDescClustering(Termmatrix termMat, int k){
		Clustering clustering = new Clustering();
		//Clustering temp = new Clustering();
		CostFunction costFunc = new CostFunction();
		Random randGen = new Random();
		int vectorLength = termMat.getMatrix().getColumnDimension();
		boolean change = true;
		double costs = 0;
		
		//initialize clusters with centroids randomly generated
		for(int i = 0; i < k; i++){
			Cluster c = new Cluster();
			ArrayRealVector cent = new ArrayRealVector(vectorLength);
			int index = randGen.nextInt(termMat.getNodeIdList().size()-1);
			cent = (ArrayRealVector) termMat.getMatrix().getRowVector(index);
			c.setCentroid(cent);
			clustering.addCluster(c);
		}
		
		//assign nodes and update centroids
		//clustering.setClustering(assignCluster(clustering.getClustering(),termMat.getMatrix(), termMat.getNodeIdList()));
		//clustering.setCosts(costFunc.valueNode(clustering.getClustering(), termMat.getNodeIdList().size()));
		
		//assign nodes and update centroids until centroids don't change anymore
		while(change){
		//while(true){
			//temp.setClustering(updateCentroids(clustering.getClustering()));
			//temp.clearCluster();
			//temp.setClustering(assignCluster(temp.getClustering(),termMat.getMatrix(), termMat.getNodeIdList()));
			//temp.setCosts(costFunc.valueNode(temp.getClustering(), termMat.getNodeIdList().size()));
			clustering.setClustering(assignCluster(clustering.getClustering(),termMat.getMatrix(), termMat.getNodeIdList()));
			LinkedList<ArrayRealVector> centroids = getCentroids(clustering.getClustering());
			clustering.setClustering(updateCentroids(clustering.getClustering()));
			change = different(centroids,getCentroids(clustering.getClustering()));
			
			/*if(clustering.getCosts() == temp.getCosts()){
				break;
			}
			if(clustering.getCosts() > temp.getCosts()){
				clustering.setClustering(temp.getClustering());
				clustering.setCosts(temp.getCosts());
			}*/
		}
		
		//k++;
		costs = costFunc.value(clustering.getClustering(), termMat.getNodeIdList().size());
		clustering.setCosts(costs);
		
		
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
			cent = cent.subtract(costFunc.derivativeValue(curr));
			//cent = cent.add(costFunc.derivativeValue(curr));
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
			ArrayRealVector vectorA = a.get(i);
			ArrayRealVector vectorB = b.get(i);
			double dif = vectorA.subtract(vectorB).getNorm();
			dif = Math.abs(dif);
			if(dif >= 0.0002){
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
	
	private Clustering overlappCommunities(Clustering c, LinkedList<Node> nodes) {
		//Clustering opt = c;
		LinkedList<Point> points = new LinkedList<Point>();
		LinkedList<Cluster> clusters = c.getClustering();
		//LinkedList<Cluster> optC = opt.getClustering();
		CostFunction f = new CostFunction();
		double dist = 2;
		
		
		for(Iterator<Cluster> it = clusters.iterator(); it.hasNext();){
			Cluster curr = it.next();
			points.addAll(curr.getPoints());
		}
		
		for(Iterator<Point> it1 = points.iterator(); it1.hasNext(); ){
				Point p = it1.next();
				for(Iterator<Cluster> it2 = clusters.iterator(); it2.hasNext();){
					Cluster clust = it2.next();
					if(!clust.getPoints().contains(p)){
						dist = distanceCosSim(clust.getCentroid(),p.getCoordinates());
						if(dist <= beta){
							clust.assignPoint(p);
							
						}
						dist = 2;
					}
				}
			}
		c.setCosts(f.value(c.getClustering(), nodes.size()));
		
		return c;
	}

}
