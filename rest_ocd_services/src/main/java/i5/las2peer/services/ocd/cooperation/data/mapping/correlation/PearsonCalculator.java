package i5.las2peer.services.ocd.cooperation.data.mapping.correlation;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class PearsonCalculator extends Calculator {

	@Override
	public double correlate(double[] val1, double[] val2) {
		
		PearsonsCorrelation correlation = new PearsonsCorrelation();
		return correlation.correlation(val1, val2);

	}

}
