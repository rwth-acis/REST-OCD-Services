package i5.las2peer.services.ocd.cooperation.data.mapping.correlation;

import java.security.InvalidParameterException;
import java.util.List;
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
import i5.las2peer.services.ocd.graphs.properties.AbstractProperty;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;
import i5.las2peer.services.ocd.graphs.properties.Size;

public enum Correlation {

	/**
	 * CovarianceCalculator
	 */
	COVARIANCE(CovarianceCalculator.class, 0),

	/**
	 * Pearson correlation coefficient
	 */
	PEARSON(PearsonCalculator.class, 1),

	/**
	 * Spearman's rank correlation coefficient
	 */
	SPEARMAN(SpearmanCalculator.class, 2),

	/**
	 * Kendall rank correlation coefficient
	 */
	KENDALL(KendallCalculator.class, 3);
	
	/**
	 * the int representation of the property
	 */
	private final int id;
	
	/**
	 * The class corresponding to the correlation
	 */
	private final Class<? extends Calculator> correlationClass;
	
	Correlation(Class<? extends Calculator> correlationClass, int id)	{
		this.correlationClass = correlationClass;
		this.id = id;
		
	}
	
	/**
	 * @return The unique id of the correlation
	 */
	public int getId() {
		return id;
	}
	
	public String getName() {
		return this.name().substring(0, 1).toUpperCase() + this.name().substring(1).toLowerCase();
	}
	
	/**
	 * Returns the class corresponding to the correlation
	 * 
	 * @return The corresponding class.
	 */
	public Class<? extends Calculator> getCorrelationClass() {
		return this.correlationClass;
	}
	
	
	/**
	 * Correlates two double arrays
	 * 
	 * @param val1 first array
	 * @param val2 second array
	 * @return correlation value
	 * @throws InstantiationException if instance creation failed
	 * @throws IllegalAccessException if an illegal access occured on the instance
	 */
	public double correlate(double[] val1, double[] val2) throws InstantiationException, IllegalAccessException {
		return this.getCorrelationClass().newInstance().correlate(val1, val2);
	}
	
	/**
	 * 
	 * @return The number of values
	 */
	public static int size() {
		return values().length;
	}
	
	/**
	 * Returns the type corresponding to an id.
	 * 
	 * @param id
	 *            The id.
	 * @return The corresponding type.
	 */
	public static Correlation lookup(int id) {
		for (Correlation correlation: Correlation.values()) {
			if (id == correlation.getId()) {
				return correlation;
			}
		}
		throw new InvalidParameterException();
	}
	
}
