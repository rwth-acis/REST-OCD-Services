package i5.las2peer.services.ocd.graphs.properties;

import java.security.InvalidParameterException;

/**
 * This enum contains all computable graph properties.
 *
 */
public enum GraphProperty {

	DENSITY(Density.class, 0),
	AVERAGE_DEGREE(AverageDegree.class, 1),
	DEGREE_DEVIATION(DegreeDeviation.class,	2),
	CLUSTERING_COEFFICIENT(ClusteringCoefficient.class, 3);

	/**
	 * the int representation of the property
	 */
	private final int id;

	/**
	 * The class corresponding to the property.
	 */
	private final Class<? extends GraphPropertyAbstract> propertyClass;

	/**
	 * Creates a new instance.
	 * 
	 * @param propertyClass
	 *            Defines the propertyClass attribute.
	 * @param id
	 *            Defines the id attribute.
	 */
	GraphProperty(Class<? extends GraphPropertyAbstract> propertyClass, int id) {

		this.id = id;
		this.propertyClass = propertyClass;
	}
	
	/**
	 * Returns the CustomGraphProperty subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	public Class<? extends GraphPropertyAbstract> getPropertyClass() {
		return this.propertyClass;
	}
	
	/**
	 * @return The unique id of the type.
	 */
	public int getId() {
		return id;
	}
	
	/**	 * 
	 * @return The number of values
	 */
	public static int size() {
		return values().length;
	}
	
	/**
	 * Returns the type corresponding to an id.
	 * @param id The id.
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

}
