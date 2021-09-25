package i5.las2peer.services.ocd.algorithms.utils;

import java.util.Comparator;

/**
 * This class represents a comparator for EigenPair type. The comparison occurs
 * based on the Eigenvalues, which is necessary finding firt K eigenvectors for
 * FuzzyCMeansSpectralClustering method
 *
 */
public class EigenPairComparator implements Comparator<EigenPair> {

	@Override
	public int compare(EigenPair o1, EigenPair o2) {
		
		return o1.getEigenValue().compareTo(o2.getEigenValue());
		
	}

	

}
