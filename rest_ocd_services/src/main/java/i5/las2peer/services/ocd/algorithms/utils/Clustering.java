package i5.las2peer.services.ocd.algorithms.utils;

import java.util.Iterator;
import java.util.LinkedList;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.graphstream.graph.Node;

public class Clustering {
	private double costs;
	private LinkedList<Cluster> cluster;
	
	///////////////////
	////Constructor////
	///////////////////
		
	public Clustering(){
		cluster = new LinkedList<Cluster>();
	}
	
	/////////////////////////
	////Getter and Setter////
	/////////////////////////
	
	public void setCosts(double c){
		this.costs = c;
	}
	
	public double getCosts(){
		return costs;
	}
	
	public void setClustering(LinkedList<Cluster> c){
		this.cluster = c;
	}
	
	public LinkedList<Cluster> getClustering(){
		return cluster;
	}
	
	////////////////////////
	////Update Functions////
	////////////////////////
	
	public void addCluster(Cluster c){
		this.cluster.add(c);
	}
	
	///////////////////////////
	////Converter Functions////
	///////////////////////////

	public Matrix createMembershipMatrix(CustomGraph graph) {
		Matrix membershipMatrix = new Basic2DMatrix(graph.getNodeCount(),
				this.cluster.size());
		int communityIndex = 0;
		for(Iterator<Cluster> it = cluster.iterator(); it.hasNext();){
			Cluster curr = it.next();
			for(Iterator<Point> itp = curr.getPoints().iterator(); itp.hasNext();){
				Point p = itp.next();
				membershipMatrix.set(p.getNode().index(), communityIndex, 1.0);
			}
			communityIndex++;			
		}
		return membershipMatrix;
	}
	
	public Matrix createMembershipMatrixNode(CustomGraph graph){
		Matrix membershipMatrix = new Basic2DMatrix(graph.getNodeCount(), this.cluster.size());
		int communityIndex = 0;
		for(Iterator<Cluster> it = cluster.iterator(); it.hasNext();){
			Cluster curr = it.next();
			for(Node node: curr.getNodes()){
				membershipMatrix.set(node.getIndex(), communityIndex, 1.0);
			}
			communityIndex++;
		}
		return membershipMatrix;
	}

	public boolean containsEmpty() {
		for(Iterator<Cluster> it = cluster.iterator(); it.hasNext();){
			Cluster curr = it.next();
			if(curr.getPoints().isEmpty()){
				return true;
			}
		}
		return false;
	}
	
	public void clearCluster(){
		for(Iterator<Cluster> it = cluster.iterator(); it.hasNext();){
			Cluster curr = it.next();
			curr.clearPoints();
		}
	}
}
