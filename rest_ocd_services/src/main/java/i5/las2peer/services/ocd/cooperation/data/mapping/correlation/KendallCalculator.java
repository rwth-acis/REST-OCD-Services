package i5.las2peer.services.ocd.cooperation.data.mapping.correlation;

import org.apache.commons.math3.stat.correlation.KendallsCorrelation;

public class KendallCalculator extends Calculator {

	@Override
	public double correlate(double[] val1, double[] val2) {

		KendallsCorrelation kendall = new KendallsCorrelation();
		double result = kendall.correlation(val1, val2);
		return result;

	}

}
