package i5.las2peer.services.ocd.algorithms.utils;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;

public class Similarities {
	
	public double cosineSim(Array2DRowRealMatrix matrix){
		double res = 0;
		double dot = 0;
		double normS = 0;
		double normR = 0;
		ArrayRealVector sender = (ArrayRealVector)matrix.getRowVector(0);
		ArrayRealVector receiver = (ArrayRealVector)matrix.getRowVector(1);
		
		dot = sender.dotProduct(receiver);
		normS = sender.getNorm();
		normR = receiver.getNorm();
		
		if(normS != 0 && normR != 0){
			res = dot / (normS * normR);
		}
		
		return res;
	}
	
	public double cosineSim(ArrayRealVector v, ArrayRealVector u){
		double res = 0;
		double dot = 0;
		double normV = 0;
		double normU = 0;
		
		dot = v.dotProduct(u);
		normV = v.getNorm();
		normU = u.getNorm();
		
		if(normV != 0 && normU != 0){
			res = dot / (normV * normU);
		}
		
		return res;
	}

}
