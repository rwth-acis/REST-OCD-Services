package i5.las2peer.services.ocd.cooperation.data.mapping;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import i5.las2peer.services.ocd.cooperation.data.mapping.correlation.CorrelationDataset;
import i5.las2peer.services.ocd.cooperation.data.table.Table;
import i5.las2peer.services.ocd.cooperation.data.table.TableInterface;
import i5.las2peer.services.ocd.cooperation.data.table.TableLineInterface;
import i5.las2peer.services.ocd.cooperation.data.table.TableRow;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;

@MappedSuperclass
public abstract class MappingAbstract implements TableInterface, TableLineInterface {

	/**
	 * The id is used as persistence primary key
	 */
	@Id
	@GeneratedValue
	private long id;

	/**
	 * The name of the mapping
	 */
	@Basic
	String name;

	/**
	 * Correlation between cooperativity and size
	 */
	@ElementCollection
	CorrelationDataset sizeCorrelation;

	/**
	 * Correlation between cooperativity and density
	 */
	@ElementCollection
	CorrelationDataset densityCorrelation;

	/**
	 * Correlation between cooperativity and average degree
	 */
	@ElementCollection
	CorrelationDataset averageDegreeCorrelation;

	/**
	 * Correlation between cooperativity and degree deviation
	 */
	@ElementCollection
	CorrelationDataset degreeDeviationCorrelation;

	/**
	 * Correlation between cooperativity and clustering coefficient
	 */
	@ElementCollection
	CorrelationDataset clusteringCoefficientCorrelation;

	private double[] cooperationValues;

	private double[] payoffValues;

	///// Getter /////

	/**
	 * Returns a unique id.
	 * 
	 * @return the persistence id
	 */
	@JsonIgnore
	public long getId() {
		return this.id;
	}

	/**
	 * Returns the name of this mapping or the id if the mapping have no name
	 * 
	 * @return the name
	 */
	@Override
	@JsonGetter
	public String getName() {
		if (name.isEmpty())
			return String.valueOf(getId());
		return this.name;
	}

	@JsonProperty
	public CorrelationDataset getSizeCorrelation() {
		return sizeCorrelation;
	}

	@JsonProperty
	public CorrelationDataset getDensityCorrelation() {
		return densityCorrelation;
	}

	@JsonProperty
	public CorrelationDataset getAverageDegreeCorrelation() {
		return averageDegreeCorrelation;
	}

	@JsonProperty
	public CorrelationDataset getDegreeDeviationCorrelation() {
		return degreeDeviationCorrelation;
	}

	@JsonProperty
	public CorrelationDataset getClusteringCoefficientCorrelation() {
		return clusteringCoefficientCorrelation;
	}

	/**
	 * Returns the property values in order to compute the correlations
	 * 
	 * @param property
	 *            The property
	 * @return property values array
	 */
	public abstract double[] getPropertyValues(GraphProperty property);

	/**
	 * Returns the cooperation values in order to compute the correlations
	 *
	 * @return cooperation values array
	 */
	public abstract double[] getCooperationValues();

	///// Setter /////

	@JsonSetter
	public void setName(String name) {
		this.name = name;
	}

	@JsonSetter
	public void setSizeCorrelation(CorrelationDataset sizeCorrelation) {
		this.sizeCorrelation = sizeCorrelation;
	}

	@JsonSetter
	public void setDensityCorrelation(CorrelationDataset densityCorrelation) {
		this.densityCorrelation = densityCorrelation;
	}

	@JsonSetter
	public void setAverageDegreeCorrelation(CorrelationDataset averageDegreeCorrelation) {
		this.averageDegreeCorrelation = averageDegreeCorrelation;
	}

	@JsonSetter
	public void setDegreeDeviationCorrelation(CorrelationDataset degreeDeviationCorrelation) {
		this.degreeDeviationCorrelation = degreeDeviationCorrelation;
	}

	@JsonSetter
	public void setClusteringCoefficient(CorrelationDataset clusteringCoefficientCorrelation) {
		this.clusteringCoefficientCorrelation = clusteringCoefficientCorrelation;
	}

	@JsonSetter
	public void setCooperationValues(double[] values) {
		this.cooperationValues = values;
	}

	@JsonSetter
	public void setPayoffValues(double[] values) {
		this.payoffValues = values;
	}

	///// Methods /////

	/**
	 * Checks if the the correlation lists are set
	 * 
	 * @return
	 */
	public boolean isEvaluated() {
		if (sizeCorrelation != null && densityCorrelation != null && averageDegreeCorrelation != null) {
			if (sizeCorrelation.isEmpty() && densityCorrelation.isEmpty() && averageDegreeCorrelation.isEmpty())
				return true;
		}
		return false;
	}

	/**
	 * Create the correlations between the cooperativity values and the property
	 * values.
	 */
	public void correlate() {

		double[] cooperationValues = null;
		try {
			cooperationValues = getCooperationValues();

			if (cooperationValues == null || cooperationValues.length < 1)
				throw new IllegalStateException("no cooperation values found");

			try {
				sizeCorrelation = new CorrelationDataset(cooperationValues, getPropertyValues(GraphProperty.SIZE));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				densityCorrelation = new CorrelationDataset(cooperationValues,
						getPropertyValues(GraphProperty.DENSITY));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				averageDegreeCorrelation = new CorrelationDataset(cooperationValues,
						getPropertyValues(GraphProperty.AVERAGE_DEGREE));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				degreeDeviationCorrelation = new CorrelationDataset(cooperationValues,
						getPropertyValues(GraphProperty.DEGREE_DEVIATION));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				clusteringCoefficientCorrelation = new CorrelationDataset(cooperationValues,
						getPropertyValues(GraphProperty.CLUSTERING_COEFFICIENT));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public TableRow toTableLine() {

		TableRow line = new TableRow();
		line.add(getSizeCorrelation().toTableLine()).add(getDensityCorrelation().toTableLine())
				.add(getAverageDegreeCorrelation().toTableLine()).add(getDegreeDeviationCorrelation().toTableLine())
				.add(getClusteringCoefficientCorrelation().toTableLine());
		return line;
	}

	@Override
	public Table toTable() {
		return new Table();
	}

}
