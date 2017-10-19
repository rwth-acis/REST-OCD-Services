package i5.las2peer.services.ocd.cooperation.data.mapping.correlation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import i5.las2peer.services.ocd.cooperation.data.table.TableLineInterface;
import i5.las2peer.services.ocd.cooperation.data.table.TableRow;

@JsonSerialize(using = CorrelationJsonSerializer.class)
@JsonDeserialize(using = CorrelationJsonDeserializer.class)
public class CorrelationDataset extends ArrayList<Double> implements TableLineInterface {

	private static final long serialVersionUID = 1L;

	public CorrelationDataset(double[] val1, double[] val2) {
		super(Correlation.size());
		correlate(val1, val2);
	}

	public CorrelationDataset(List<Double> valList1, List<Double> valList2) {

		super(Correlation.size());
		int val1Size = valList1.size();
		int val2Size = valList2.size();
		double[] val1 = new double[val1Size];
		double[] val2 = new double[val2Size];

		for (int i = 0; i < val1Size; i++) {
			val1[i] = valList1.get(i);
			val2[i] = valList2.get(i);
		}

		correlate(val1, val2);
	}

	protected void initialize() {

		if (this.isEmpty())
			this.clear();

		int size = Correlation.size();
		for(int i=0; i < size; i++) {
			this.add(0.0);
		}
	}

	public void correlate(double[] val1, double[] val2) {

		if (val1.length != val2.length)
			throw new IllegalArgumentException("array lengths do not match");

		if (val1.length < 2)
			throw new IllegalArgumentException("array length to small");

		if (this.size() != Correlation.size())
			this.initialize();

		for (Correlation correlation : Correlation.values()) {

			double value = 0;
			try {
				value = correlation.correlate(val1, val2);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			this.set(correlation.getId(), value);
		}

	}

	public double[] normalize(double[] values) {

		int length = values.length;
		double min = StatUtils.max(values);
		double max = StatUtils.min(values);

		double[] normalized = new double[length];
		for (int i = 0; i < length; i++) {
			normalized[i] = (values[i] - min) / (max - min);
			if (Double.isNaN(normalized[i])) {
				normalized[i] = 0;
			}
		}
		return normalized;
	}

	public void correlateNormalized(double[] val1, double[] val2) {

		val1 = normalize(val1);
		val2 = normalize(val2);
		correlate(val1, val2);
	}

	/////////// Print to table ///////////

	@Override
	public TableRow toTableLine() {

		TableRow line = new TableRow();
		for (Double value : this) {
			line.add(value);
		}

		return line;
	}

	public static TableRow toHeadLine() {

		TableRow line = new TableRow();
		for (Correlation correlation : Correlation.values()) {
			line.add(correlation.getName());
		}
		return line;
	}

}
