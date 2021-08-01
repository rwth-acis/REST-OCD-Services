package i5.las2peer.services.ocd.cooperation.data.mapping.correlation;

public class KullLeiblerCalculator extends Calculator {

	@Override
	public double correlate(double[] val1, double[] val2) {
		
		double value = 0.0;
		for (int i = 0; i < val1.length; ++i) {
			if (val1[i] == 0.0 || val2[i] == 0.0) {
				continue;
			}
			value += val1[i] * Math.log(val1[i] / val2[i]);
		}
		return value / Math.log(2);
	}

}
