package i5.las2peer.services.ocd.cooperation.data.simulation;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import org.apache.commons.math3.stat.StatUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import i5.las2peer.services.ocd.cooperation.data.table.TableRow;

/**
 * Objects of this class hold statistical computations for a given double array.
 * Can be added to every entity as embeddable.
 *
 */
@Embeddable
public class Evaluation implements Serializable {

	private static final long serialVersionUID = 1L;

	///////// Entity Fields ///////////

	@Basic
	private double average;

	@Basic
	private double variance;

	@Basic
	private double deviation;

	@Basic
	private double maximum;

	@Basic
	private double minimum;

	/////////// Constructor ///////////
	
	/**
	 * Creates a empty instance. Used for persistence and testing purposes.
	 */
	public Evaluation() {

	}
	
	/**
	 * Creates a new instance and initialize it with a double array.
	 * 
	 * @param values 
	 */
	public Evaluation(double[] values) {

		evaluate(values);
	}
	
	/**
	 * Creates a new instance and initialize it with a double list.
	 * 
	 * @param values 
	 */
	public Evaluation(List<Double> list) {

		double[] values = new double[list.size()];
		for (int i = 0; i < list.size(); i++)
			values[i] = list.get(i);
		evaluate(values);
	}

	/////////// Calculations ///////////

	public void evaluate(double[] values) {

		this.average = calculateAverageValue(values);
		this.variance = calculateVariance(values, average);
		this.deviation = calculateStandartDeviation(variance);
		this.maximum = calculateMax(values);
		this.minimum = calculateMin(values);
	}

	private double calculateAverageValue(double[] values) {

		if (values == null || values.length == 0)
			return 0.0;

		double average = StatUtils.mean(values);
		return average;

	}

	private double calculateVariance(double[] values, double average) {

		if (values == null || values.length == 0)
			return 0.0;

		double variance = StatUtils.variance(values, average);
		return variance;
	}

	private double calculateStandartDeviation(double varianz) {
		
		return Math.sqrt(varianz);
	}

	private double calculateMax(double[] values) {
		
		if (values == null || values.length == 0)
			return 0.0;
		
		return StatUtils.max(values);
	}

	private double calculateMin(double[] values) {
		
		if (values == null || values.length == 0)
			return 0.0;
		
		return StatUtils.min(values);
	}

	//////////// Getter /////////////

	@JsonProperty
	public double getAverage() {
		return average;
	}

	@JsonProperty
	public double getVariance() {
		return this.variance;
	}

	@JsonProperty
	public double getDeviation() {
		return this.deviation;
	}

	@JsonProperty
	public double getMax() {
		return maximum;
	}

	@JsonProperty
	public double getMin() {
		return minimum;
	}

	////// Setter //////

	@JsonSetter
	public void setAverage(double average) {
		this.average = average;
	}

	@JsonSetter
	public void setVariance(double variance) {
		this.variance = variance;
	}

	@JsonSetter
	public void setDeviation(double deviation) {
		this.deviation = deviation;
	}

	@JsonSetter
	public void setMax(double max) {
		this.maximum = max;
	}

	@JsonSetter
	public void setMin(double min) {
		this.minimum = min;
	}

	/////////// Table ///////////

	public TableRow toTableLine() {

		TableRow line = new TableRow();
		line.add(getAverage()).add(getDeviation());
		return line;
	}

	public static TableRow toHeadLine() {

		TableRow line = new TableRow();
		line.add("avg").add("std");
		return line;
	}

}
