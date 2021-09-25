package i5.las2peer.services.ocd.cooperation.data.mapping.correlation;

import org.apache.commons.math3.stat.correlation.Covariance;

public class CovarianceCalculator extends Calculator {

	@Override
	public double correlate(double[] val1, double[] val2) {
		
		Covariance correlation = new Covariance();
		return correlation.covariance(val1, val2);

	}

}
