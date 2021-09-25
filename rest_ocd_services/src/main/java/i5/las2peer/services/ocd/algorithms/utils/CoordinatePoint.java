package i5.las2peer.services.ocd.algorithms.utils;

import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.la4j.vector.Vector;

/**
 * This class instance represents coordinates for each node. The coordinates are
 * built based on community belongingness values for each community for a given
 * node. These coordinates can be used for clustering, similar to coordinates of
 * Euclidean space. Each coordinate point also has an id (representing the node)
 * which helps with identifying/accessing the nodes
 *
 */
public class CoordinatePoint implements Clusterable {

	private double[] point;
	private int id;

	public CoordinatePoint(double[] point) {
		
		super();
		this.point = point;
	}
	
	/**
	 * Constructor based on an ArrayList
	 * @param row     ArrayList representing coordinates
	 * @param id      id for the CoordinatePoint
	 */
	public CoordinatePoint(ArrayList<Double> row, int id) {
		
		super();
		this.point = new double[row.size()];
		
		for (int i = 0; i < row.size(); i++) {
			
			point[i] = row.get(i);
			
		}
		
		this.id = id;
	}
	
	/**
	 * Constructor based on a Vector
	 * @param row     Vector representing coordinates
	 * @param id      id for the CoordinatePoint
	 */
	public CoordinatePoint(Vector row, int id) {
		
		this.point = new double[row.length()];
		
		for (int i = 0; i < row.length(); i++) {
			
			point[i] = row.get(i);
			
		}
		
		this.id = id;
	}
	
	@Override
	public double[] getPoint() {
		
		return this.point;
		
	}

	public int getId() {
		
		return id;
		
	}


	@Override
	public String toString() {
		
		return Arrays.toString(point);
		
	}

}
