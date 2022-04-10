package i5.las2peer.services.ocd.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;

import org.la4j.matrix.Matrix;

import i5.las2peer.services.ocd.algorithms.utils.Cluster;
import i5.las2peer.services.ocd.algorithms.utils.Clustering;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.Termmatrix;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.metrics.ExecutionTime;
import y.base.Node;

/**
 * Implements one of the algorithms by Sabrina Haefele conceived in the thesis:
 * Overlapping Community Detection Based on Content and Authority
 * Some of these algorithms are included in the paper by Shahriari, Mohsen and Haefele, Sabrina and Klamma, Ralf:
 * Contextualized versus structural overlapping communities in social media
 */
public class WordClusteringRefinementAlgorithm implements OcdAlgorithm{
	
	/////////////////
	////Variables////
	/////////////////
	
	/**
	 * Coefficient to determine if two clusters should be merged.
	 */
	private double overlappCoef = 0.5;
	
	private boolean svd = false;
	
	////////////////////////
	//// Parameter Names////
	////////////////////////
	
	public static final String OVERLAPP_COEF_NAME = "overlappingCoefficient";
	
	public static final String SVD_NAME = "svd";
	
	////////////////////
	//// Constructor////
	////////////////////
	
	public WordClusteringRefinementAlgorithm(){
		
	}
	
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.CONTENT_UNLINKED);
		return compatibilities;
	}
	
	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.WORD_CLUSTERING_REF_ALGORITHM;
	}
	
	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(OVERLAPP_COEF_NAME, Double.toString(overlappCoef));
		parameters.put(SVD_NAME, Boolean.toString(svd));
		return parameters;
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.containsKey(OVERLAPP_COEF_NAME)){
			overlappCoef = Double.parseDouble(parameters.get(OVERLAPP_COEF_NAME));
			if(overlappCoef < 0 || overlappCoef > 1 ){
				throw new IllegalArgumentException();
			}
			parameters.remove(OVERLAPP_COEF_NAME);
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
			}
			termMat.setMatrix(m);
		}
		LinkedList<Cluster> resClust = new LinkedList<Cluster>();
		Cluster[] temp = new Cluster[2];
		Clustering opt = new Clustering();
		//
		
		resClust = initializeClustering(termMat);
		temp = findMergeCandidates(resClust);
		
		while(temp != null){
			resClust = mergeClusters(temp, resClust, termMat.getNodeIdList());
			temp = findMergeCandidates(resClust);
		}
		
		opt.setClustering(resClust);
		
		Matrix membershipMatrix = opt.createMembershipMatrixNode(graph);
		Cover res = new Cover(graph,membershipMatrix);
		time.stop();
		time.setCoverExecutionTime(res);

		System.out.println("***   "+ this.getClass().getSimpleName() + " found " + res.communityCount() +" communities   ***");
		return res;
	}

	private Cluster[] findMergeCandidates(LinkedList<Cluster> resClust) {
		//Cluster[] res = new LinkedList<Cluster[]>();
		Cluster[] match = new Cluster[2];
		//double max = 0.0;
		//check each cluster with the rest of the clustering
		for(Iterator<Cluster> it = resClust.iterator(); it.hasNext();){
			Cluster c = it.next();
			//List<Cluster> rest = resClust.subList(resClust.indexOf(c) + 1, resClust.size());
			for(ListIterator<Cluster> li = resClust.listIterator(resClust.indexOf(c) +1); li.hasNext();){
				Cluster cr = li.next();
				//compute overlapping coefficient
				double oc = overlappingCoefficient(c,cr);
				/*if oc of the clusters is greater equal to the threashold 
				and the maximum of the fixed cluster compared to the restlist
				add the clusters to the merging candidates*/
				if(oc >= overlappCoef /*&& oc > max*/){
					//max = oc;
					match[0] = c;
					match[1] = cr;
					return match;
				}
			}
			//res.add(match);
		}
		return null;
	}

	private LinkedList<Cluster> mergeClusters(Cluster[] candidates, LinkedList<Cluster> list, LinkedList<Node> nodeList) {
		int index;
		
		//merging each entry in the candidates list and adding the new cluster to the result list
		//for(Cluster[] entry : candidates){
			LinkedList<Node> n = candidates[1].getNodes();
			Cluster cl = candidates[0];
			list.remove(candidates[0]);
			list.remove(candidates[1]);
			ArrayRealVector temp = cl.getCentroid();
			
			//check each point in the second cluster, if needs to be added 
			for(int i = 0; i <n.size(); i++){
				Node node = n.get(i);
				//index of the tf-idf value of the node in the matrix
				index = nodeList.indexOf(node);
				if(!cl.getNodes().contains(node)){
					//add node to the cluster
					
					//add tf-idf value to the merged clusters vector
					temp.setEntry(index,candidates[1].getCentroid().getEntry(index));
					cl.addNode(node);
				}
			}
			cl.setCentroid(temp);
			
		list.add(cl);
		return list;
	}

	private double overlappingCoefficient(Cluster c, Cluster cr) {
		double max = 0;
		
		double cut = 0;
		double res;
		
		LinkedList<Node> n = c.getNodes();
		LinkedList<Node> n1 = cr.getNodes();
		ArrayRealVector c1 =  c.getCentroid();
		ArrayRealVector c2 = cr.getCentroid();
		
		int sizeC1 = n.size();
		int sizeC2 = n1.size();
		
		LinkedList<Node> temp = new LinkedList<Node>(n);
		
		/*for(int i = 0; i < c1.getDimension(); i++){
			if(c1.getEntry(i) != 0){
				sizeC1++;
				if(c2.getEntry(i) != 0){
					sizeC2++;
					cut++;
				}
			}else{
				if(c2.getEntry(i) != 0){
					sizeC2++;
				}
			}
			
		}*/
		
		if(sizeC1 > sizeC2){
			max = sizeC1;
		}else{
			max = sizeC2;
		}
		
		/*if(n.size() < n1.size()){
			min = n.size();
		}else{
			min = n1.size();
		}*/
		
		//temp now only contains elements that are in p and p1
		temp.retainAll(n1);
		cut = temp.size();
		
		res = cut/max;
		return res;
	}

	private LinkedList<Cluster> initializeClustering(Termmatrix termMat) {
		LinkedList<Cluster> res = new LinkedList<Cluster>();
		Array2DRowRealMatrix matrix = termMat.getMatrix();
		/*for(int i = 0; i < matrix.getRowDimension(); i++){
			Cluster c = new Cluster();
			//retrieve column vector as centroid of the cluster
			ArrayRealVector cent = (ArrayRealVector) matrix.getRowVector(i);
			c.setCentroid(cent);
			//add corresponding node to the cluster 
			Node n = termMat.getNodeIdList().get(i);
			c.addNode(n);
			//add cluster to the result
			res.add(c);
		}*/
		
		for(int i = 0; i < matrix.getColumnDimension(); i++){
			Cluster c = new Cluster();
			//retrieve column vector as centroid of the cluster
			ArrayRealVector cent = (ArrayRealVector) matrix.getColumnVector(i);
			c.setCentroid(cent);
			//add nodes to the cluster
			for(int j = 0; j < cent.getDimension(); j++){
				if(cent.getEntry(j) != 0){
					c.addNode(termMat.getNodeIdList().get(j));
				}
			}
			//add cluster to the result
			res.add(c);
		}
		return res;
	}

}
