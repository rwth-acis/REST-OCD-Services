package i5.las2peer.services.ocd.cooperation.simulation.dynamic;

/**
 * Constructs {@link Dynamic} objects
 */
public class DynamicFactory {

	public static DynamicFactory getInstance() {
		return new DynamicFactory();
	}

	/**
	 * Creates a {@link Dynamic} matching the specified {@link ConditionType} and
	 * parameter values.
	 * 
	 * @param dynamicType
	 * @param values
	 *            parameters values
	 * @return Dynamic
	 */
	public Dynamic build(DynamicType dynamicType, double[] values) {

		switch (dynamicType) {

		case UNKNOWN:
			throw new IllegalArgumentException("unknown dynamic");
		
		case REPLICATOR:
			if (values == null || values.length != 1)
				throw new IllegalArgumentException("no dynamic parameters specified");
			return (new Replicator(values[0]));

		default:
			try {
				return dynamicType.getDynamicClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

	/**
	 * Creates a {@link Dynamic} matching the specified {@link ConditionType} with
	 * default parameters.
	 * 
	 * @param dynamicType
	 * @return
	 */
	public Dynamic build(DynamicType dynamicType) {
		return build(dynamicType, new double[] { 1.5 });
	}

	/**
	 * Creates a {@link Dynamic} matching the specified {@link ConditionType} and
	 * the parameter value.
	 * 
	 * @param dynamicType
	 * @param value
	 *            parameter value
	 * @return dynamic
	 */
	public Dynamic build(DynamicType dynamicType, double value) {
		return build(dynamicType, new double[] { value });
	}

	/**
	 * Creates a {@link Dynamic} matching the specified dynamic string and
	 * parameter values.
	 * 
	 * @param dynamicString
	 *            specifying dynamicType
	 * @param values
	 *            dynamic parameter values
	 * @return Dynamic
	 */
	public Dynamic build(String dynamicString, double[] values) {

		DynamicType dynamicType = DynamicType.fromString(dynamicString);

		if (dynamicType == DynamicType.UNKNOWN)
			throw new IllegalArgumentException("unknown dynamic");

		return build(dynamicType, values);
	}

}
