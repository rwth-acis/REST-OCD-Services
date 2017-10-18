package i5.las2peer.services.ocd.cooperation.data.mapping;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Embeddable;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import i5.las2peer.services.ocd.cooperation.data.table.TableLineInterface;
import i5.las2peer.services.ocd.cooperation.data.table.TableRow;

@Embeddable
public class Correlation implements TableLineInterface {

	///////// Entity Fields ///////////

	/**
	 * Covariance
	 */
	@Basic
	private double covariance;

	/**
	 * Pearson correlation coefficient
	 */
	@Basic
	private double pearson;

	/**
	 * Spearman's rank correlation coefficient
	 */
	@Basic
	private double spearman;

	/**
	 * Kendall rank correlation coefficient
	 */
	@Basic
	private double kendall;
	
	public Correlation() {

	}

	public Correlation(double[] val1, double[] val2) {

		correlateNormalized(val1, val2);
	}

	public Correlation(List<Double> valList1, List<Double> valList2) {

		int val1Size = valList1.size();
		int val2Size = valList2.size();
		double[] val1 = new double[val1Size];
		double[] val2 = new double[val2Size];

		for (int i = 0; i < val1Size; i++) {
			val1[i] = valList1.get(i);
			val2[i] = valList2.get(i);
		}

		correlateNormalized(val1, val2);
	}

	/////////// Calculations ///////////

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

	public void correlate(double[] val1, double[] val2) {

		if(val1.length < 2 || val2.length < 2) {
			this.covariance = -1;
			this.pearson = -1;
			this.spearman = -1;
			this.kendall = -1;
			return;
		}
			
		this.covariance = calculateCovariance(val1, val2);
		this.pearson = calculatePearsons(val1, val2);
		this.spearman = calculateSpearmans(val1, val2);
		this.kendall = calculateKendalls(val1, val2);
	}

	public void correlateNormalized(double[] val1, double[] val2) {
		
		val1 = normalize(val1);
		val2 = normalize(val2);
		correlate(val1, val2);
	}

	protected double calculateCovariance(double[] val1, double[] val2) {
						
		Covariance correlation = new Covariance();
		return correlation.covariance(val1, val2);
	}

	protected double calculatePearsons(double[] val1, double[] val2) {

		PearsonsCorrelation correlation = new PearsonsCorrelation();
		return correlation.correlation(val1, val2);
	}

	protected double calculateSpearmans(double[] val1, double[] val2) {

		SpearmansCorrelation correlation = new SpearmansCorrelation();
		return correlation.correlation(val1, val2);
	}

	protected double calculateKendalls(double[] val1, double[] val2) {

		KendallsCorrelation correlation = new KendallsCorrelation();
		return correlation.correlation(val1, val2);
	}

	protected double calculateWilcoxons(double[] val1, double[] val2) {

		WilcoxonSignedRankTest correlation = new WilcoxonSignedRankTest();
		return correlation.wilcoxonSignedRankTest(val1, val2, true);
	}

	protected double calculateMannWhitneyUs(double[] val1, double[] val2) {

		MannWhitneyUTest correlation = new MannWhitneyUTest();
		return correlation.mannWhitneyUTest(val1, val2);
	}

	protected double calculateKullLeiblerDivergence(double[] val1, double[] val2) {

		double value = 0.0;
		for (int i = 0; i < val1.length; ++i) {
			if (val1[i] == 0.0 || val2[i] == 0.0) {
				continue;
			}
			value += val1[i] * Math.log(val1[i] / val2[i]);
		}
		return value / Math.log(2); 
	}
	
	
	//////////// Getter /////////////

	@JsonProperty
	public double getCovariance() {
		return this.covariance;
	}

	@JsonProperty
	public double getPearson() {
		return this.pearson;
	}

	@JsonProperty
	public double getSpearman() {
		return this.spearman;
	}

	@JsonProperty
	public double getKendall() {
		return this.kendall;
	}
	
	///// Setter /////

	@JsonSetter
	public void setCovariance(double covariance) {
		this.covariance = covariance;
	}

	@JsonSetter
	public void setPearson(double pearson) {
		this.pearson = pearson;
	}

	@JsonSetter
	public void setSpearman(double spearman) {
		this.spearman = spearman;
	}

	@JsonSetter
	public void setKendall(double kendall) {
		this.kendall = kendall;
	}

	/////////// Print ///////////

	@Override
	public TableRow toTableLine() {

		TableRow line = new TableRow();
		line.add(getCovariance()).add(getPearson()).add(getSpearman()).add(getKendall());
		return line;
	}

	public static TableRow toHeadLine() {

		TableRow line = new TableRow();
		line.add("covariance").add("pearsons").add("spearmans").add("kendall");
		return line;

	}

}
