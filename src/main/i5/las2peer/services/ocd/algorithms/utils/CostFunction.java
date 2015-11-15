package i5.las2peer.services.ocd.algorithms.utils;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.math3.analysis.function.Pow;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class CostFunction {
	
	public double value(LinkedList<Cluster> clust, int numbNode){
		double res = 0;
		double tempRes = 0;
		Similarities sim = new Similarities();
		for(Iterator<Cluster> it = clust.iterator(); it.hasNext();){
			Cluster curr = it.next();
			ArrayRealVector cent = curr.getCentroid();
			for(Iterator<Point> iter = curr.getPoints().iterator(); iter.hasNext();){
				Point p = iter.next();
				tempRes = 1 - sim.cosineSim(cent, p.getCoordinates());
				res = res + tempRes;
			}
		}
		
		res = res / numbNode;
		
		return res;
	}
	
	public double valueNode(LinkedList<Cluster> clust, int numbNode){
		double res = 0;
		double tempRes = 0;
		Similarities sim = new Similarities();
		for(Iterator<Cluster> it = clust.iterator(); it.hasNext();){
			Cluster curr = it.next();
			//ArrayRealVector cent = curr.getCentroid();
			for(Iterator<Point> iter = curr.getPoints().iterator(); iter.hasNext();){
				Point p1 = iter.next();
				for(Iterator<Point> iter1 = curr.getPoints().iterator(); iter1.hasNext();){
					Point p2 = iter1.next();
					if(!p2.equals(p1)){
					tempRes = 1 - sim.cosineSim(p2.getCoordinates(), p1.getCoordinates());
					res = res + tempRes;
					}
				}
				
			}
		}
		
		res = res / numbNode;
		
		return res;
	}
	public ArrayRealVector derivativeValue(Cluster c){
		Pow p = new Pow();
		double alpha = 0.5;
		ArrayRealVector cent = c.getCentroid();
		RealVector tempRes = new ArrayRealVector(cent.getDimension());
		RealVector res = new ArrayRealVector(cent.getDimension());
		RealVector temp = new ArrayRealVector(cent.getDimension());
		double normC = cent.getNorm();
		RealVector cDiv = cent.mapDivide(normC);
		double normPow;
		
		
		for(Iterator<Point> it = c.getPoints().iterator(); it.hasNext();){
			Point curr = it.next();
			ArrayRealVector coord = curr.getCoordinates();
			double normU = coord.getNorm();
			normPow = p.value((normU * normC), 2);
			tempRes = coord.mapMultiply(normU).mapMultiply(normC);
			temp = coord.ebeMultiply(cent).ebeMultiply(cDiv);
			tempRes = tempRes.subtract(temp.mapMultiply(normU));
			tempRes = tempRes.mapDivide(normPow);
			res = res.add(tempRes);
		}
		
		return (ArrayRealVector) res.mapMultiply(alpha);
	}
}
