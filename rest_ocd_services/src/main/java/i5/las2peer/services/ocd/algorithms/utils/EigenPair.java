package i5.las2peer.services.ocd.algorithms.utils;


import org.la4j.vector.Vector;

/**
 * This class is used in FuzzyCMeansSpectralClustering algorithm and purpose of
 * it is to hold a pair of an Eigenvalue and a corresponding Eigenvector
 *
 */
public class EigenPair {
	
	private Double eigenValue;
	private Vector eigenVector;
	
	public EigenPair(Double eigenValue, Vector eigenVector) {
		
		super();
		this.eigenValue = eigenValue;
		this.eigenVector = eigenVector;
	}

	public Double getEigenValue() {
		
		return eigenValue;
		
	}

	public void setEigenValue(double eigenValue) {
		
		this.eigenValue = eigenValue;
		
	}

	public Vector getEigenVector() {
		
		return eigenVector;
		
	}

	public void setEigenVector(Vector eigenVector) {
		
		this.eigenVector = eigenVector;
		
	}

	@Override
	public int hashCode() {
		
		int hashEigenValue = eigenValue != null ? eigenValue.hashCode() : 0;
		int hashEigenVector = eigenVector != null ? eigenVector.hashCode() : 0;
		
		return (hashEigenValue + hashEigenVector) * hashEigenVector + hashEigenValue;
		
	}

	@Override
	public String toString() {
		
		return this.eigenValue + " : " + this.eigenVector;
		
	}


}
