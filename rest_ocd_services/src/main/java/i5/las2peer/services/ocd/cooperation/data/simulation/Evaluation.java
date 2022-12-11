package i5.las2peer.services.ocd.cooperation.data.simulation;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Embeddable;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentUpdateOptions;
import org.apache.commons.math3.stat.StatUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import i5.las2peer.services.ocd.cooperation.data.table.TableRow;

/**
 * Objects of this class hold statistical computations for a given double array.
 * Can be added to every entity as embeddable.
 *
 */
@Embeddable
public class Evaluation implements Serializable {

	//ArangoDB
	public static final String collectionName = "evaluation";
	public static final String averageColumnName = "AVERAGE";
	public static final String varianceColumnName = "VARIANCE";
	public static final String deviationColumnName = "DEVIATION";
	public static final String maximumColumnName = "MAXIMUM";
	public static final String minimumColumnName = "MINIMUM";
	/////////////////////////

	private static final long serialVersionUID = 1L;

	///////// Entity Fields ///////////

	@JsonProperty
	private double average;

	@JsonProperty
	private double variance;

	@JsonProperty
	private double deviation;

	@JsonProperty
	private double maximum;

	@JsonProperty
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
	 * @param values the values to evaluate with
	 */
	public Evaluation(double[] values) {

		evaluate(values);
	}
	
	/**
	 * Creates a new instance and initialize it with a double list.
	 * 
	 * @param list the list of values to evaluate with
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

	public double getAverage() {return average; }

	public double getVariance() {
		return this.variance;
	}

	public double getDeviation() {
		return this.deviation;
	}

	public double getMaximum() {
		return maximum;
	}

	public double getMinimum() {
		return minimum;
	}

	public void setAverage(double average) {
		this.average = average;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}

	public void setDeviation(double deviation) {
		this.deviation = deviation;
	}

	public void setMaximum(double max) {
		this.maximum = max;
	}

	public void setMinimum(double min) {
		this.minimum = min;
	}


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



	/**
	 * Update column values to be stored in the database.
	 * @param bd       Document holding updated values.
	 * @return         Document with updated values.
	 */
	public BaseDocument updateDocument(BaseDocument bd){
		bd.addAttribute(averageColumnName, this.getAverage());
		bd.addAttribute(varianceColumnName, this.getVariance());
		bd.addAttribute(deviationColumnName, this.getDeviation());
		bd.addAttribute(maximumColumnName, this.getMaximum());
		bd.addAttribute(minimumColumnName, this.getMinimum());
		return bd;
	}

	// Persistence Methods
	public void persist(ArangoDatabase db, String transId) {
		ArangoCollection collection = db.collection(collectionName);
		DocumentCreateOptions createOptions = new DocumentCreateOptions().streamTransactionId(transId);
		BaseDocument bd = new BaseDocument();
		updateDocument(bd);
		collection.insertDocument(bd, createOptions);
		//this.key = bd.getKey(); // if key is assigned before inserting (line above) the value is null
	}

	public void updateDB(ArangoDatabase db, String transId) {
		DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);

		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		updateDocument(bd);
		System.out.println("about to store updated Evaluation with values: " + bd);//TODO:DELETE
		//collection.updateDocument(this.key, bd, updateOptions);  //TODO: DELETE
	}

	public static Evaluation load(String key, ArangoDatabase db, String transId) {
		Evaluation evaluation = new Evaluation();
		ArangoCollection collection = db.collection(collectionName);

		BaseDocument bd = collection.getDocument(key, BaseDocument.class);
		if (bd != null) {
			//evaluation.setKey(bd.getKey());
			evaluation.setAverage((double) bd.getAttribute(averageColumnName));
			evaluation.setVariance((double) bd.getAttribute(varianceColumnName));
			evaluation.setDeviation((double) bd.getAttribute(deviationColumnName));
			evaluation.setMaximum((double) bd.getAttribute(maximumColumnName));
			evaluation.setMinimum((double) bd.getAttribute(minimumColumnName));

		}
		else {
			System.out.println("Evaluation with key " + key + " not found.");
		}
		return evaluation;
	}

	@Override
	public String toString() {
		return "Evaluation{" +
				"average=" + average +
				", variance=" + variance +
				", deviation=" + deviation +
				", maximum=" + maximum +
				", minimum=" + minimum +
				'}';
	}
}
