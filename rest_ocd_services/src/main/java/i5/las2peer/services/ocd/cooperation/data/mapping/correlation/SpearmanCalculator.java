package i5.las2peer.services.ocd.cooperation.data.mapping.correlation;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

public class SpearmanCalculator extends Calculator {

	@Override
	public double correlate(double[] val1, double[] val2) {
		
		SpearmansCorrelation correlation = new SpearmansCorrelation();
		return correlation.correlation(val1, val2);

	}

}
