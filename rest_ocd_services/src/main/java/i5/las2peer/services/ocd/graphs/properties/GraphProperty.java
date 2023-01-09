package i5.las2peer.services.ocd.graphs.properties;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * This enum contains all computable graph properties.
 *
 */
public enum GraphProperty {

	SIZE("Size", Size.class, 0),

	DENSITY("Density", Density.class, 1),

	AVERAGE_DEGREE("Average_Degree", AverageDegree.class, 2),

	DEGREE_DEVIATION("Degree_Deviation", DegreeDeviation.class, 3),

	CLUSTERING_COEFFICIENT("Clustering_Coefficient", ClusteringCoefficient.class, 4);

	/**
	 * the int representation of the property
	 */
	private final int id;
	
	private final String humanRead;
	/**
	 * The class corresponding to the property.
	 */
	private final Class<? extends AbstractProperty> propertyClass;

	/**
	 * Creates a new instance.
	 * 
	 * @param propertyClass
	 *            Defines the propertyClass attribute.
	 * @param id
	 *            Defines the id attribute.
	 */
	GraphProperty(String string, Class<? extends AbstractProperty> propertyClass, int id) {

		this.id = id;
		this.propertyClass = propertyClass;
		this.humanRead = string;
	}

	/**
	 * Returns the CustomGraphProperty subclass corresponding to the type.
	 * 
	 * @return The corresponding class.
	 */
	public Class<? extends AbstractProperty> getPropertyClass() {
		return this.propertyClass;
	}

	/**
	 * @return The unique id of the type.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Return the Property as human readable string
	 * 
	 * @return the property string
	 */
	public String humanRead() {
		return this.humanRead;
	}
	
	/**
	 * *
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
	public static GraphProperty lookupProperty(int id) {
		for (GraphProperty property : GraphProperty.values()) {
			if (id == property.getId()) {
				return property;
			}
		}
		throw new InvalidParameterException();
	}

	/**
	 * Returns a list of property values for a given CustomGraph
	 * 
	 * @param graph CustomGraph	
	 * @return property list
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	public static List<Double> getPropertyList(CustomGraph graph) throws InterruptedException {

		List<Double> properties = new ArrayList<>(size());
		for (int i = 0; i < size(); i++) {
			AbstractProperty property = null;
			try {
				property = lookupProperty(i).getPropertyClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			properties.add(i, property.calculate(graph));
		}
		return properties;
	}

}
