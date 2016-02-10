package i5.las2peer.services.ocd.algorithms.utils;

import org.apache.commons.math3.linear.ArrayRealVector;

import y.base.Node;

public class Point {
	private Node node;
	private ArrayRealVector coord;
	
	///////////////////
	////Constructor////
	///////////////////

	public Point(Node node, ArrayRealVector vector) {
		this.node = node;
		this.coord = vector;
	}
	
	/////////////////////////
	////Getter and Setter////
	/////////////////////////
	
	public void setNode(Node node){
		this.node = node;
	}
	
	public Node getNode(){
		return node;
	}
	
	public void setCoordinates(ArrayRealVector coord){
		this.coord = coord;
	}
	
	public ArrayRealVector getCoordinates(){
		return coord;
	}
}
