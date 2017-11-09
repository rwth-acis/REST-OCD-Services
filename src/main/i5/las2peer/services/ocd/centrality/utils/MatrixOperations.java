package i5.las2peer.services.ocd.centrality.utils;

import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

/**
 * @author Tobias
 *
 */
public class MatrixOperations {
	
	/**
	 * Calculates the eigenvector corresponding to the absolute greatest eigenvalue of the given matrix
	 * 
	 * @param matrix Matrix used for the calculation
	 * @return Principal eigenvector
	 * @throws InterruptedException
	 */
	public static Vector calculatePrincipalEigenvector(Matrix matrix) throws InterruptedException {
		Vector x = new BasicVector(matrix.columns());
		for(int i = 0; i < x.length(); i++) {
			x.set(i, 1.0);
		}

		for(int i = 0; i < 50; i++) { // iterations set rather small, so the method does not take that long for bigger matrices
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			x = matrix.multiply(x);
			double norm = norm(x);
			x = x.divide(norm);
		}	
		return x;
	}
	
	/**
	 * Calculates the absolute value of the the principal eigenvalue of the given matrix
	 * 
	 * @param matrix Matrix used for the calculation
	 * @return Absolute value of the principal eigenvalue
	 * @throws InterruptedException
	 */
	public static double calculateAbsolutePrincipalEigenvalue(Matrix matrix) throws InterruptedException {
		Vector x = calculatePrincipalEigenvector(matrix);
		return norm(matrix.multiply(x));
	}
	
	/**
	 * Calculates the stationary distribution of the Markov chain corresponding to the given transition matrix.
	 * The stationary distribution is given by a left eigenvector of the transition matrix with eigenvalue 1.
	 * 
	 * @param matrix Transition matrix of the Markov chain
	 * @return Stationary distribution of the Markov chain as a vector
	 * @throws InterruptedException
	 */
	public static Vector calculateStationaryDistribution(Matrix matrix) throws InterruptedException {
		// The matrix is transposed so the power iteration gives us a left eigenvector
		matrix = matrix.transpose();	
		Vector x = calculatePrincipalEigenvector(matrix);	
		x = matrix.multiply(x);
		// The entries are divided by the sum of all entries so the sum is 1
		x = x.divide(x.sum());
		
		return x;
	}
	
	/**
	 * Calculates the euclidean norm of the given vector
	 * 
	 * @param v The vector that is normalized
	 * @return Euclidean norm of the vector
	 */
	public static double norm(Vector v) {
		double squareSum = 0.0;
		for(int i = 0; i < v.length(); i++) {
			squareSum += v.get(i) * v.get(i);
		}
		return Math.sqrt(squareSum);
	}
}
