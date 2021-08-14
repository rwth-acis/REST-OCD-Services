package i5.las2peer.services.ocd.cooperation.simulation.termination;

public class ConditionFactory {

	/**
	 * Creates a break condition with default parameters
	 * 
	 * @param conditionEnum the condition enum
	 * @return the condition
	 */
	public Condition build(ConditionType conditionEnum) {

		if (conditionEnum == null || conditionEnum.equals(ConditionType.UNKNOWN))
			throw new IllegalArgumentException("unknown break condition");

		Condition conditionClass = null;
		try {
			conditionClass = conditionEnum.getEnumClass().newInstance();
			return conditionClass;
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new IllegalArgumentException("cant create condition");
	}

	/**
	 * Creates a break condition with given parameters
	 * 
	 * @param conditionEnum the condition enum
	 * @param parameters the parameters
	 * @return the condition
	 */
	public Condition build(ConditionType conditionEnum, int[] parameters) {

		Condition condition = build(conditionEnum);
		
		if (parameters == null)
			return condition;

		try {			
			condition.setParameters(parameters);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw e;
		}
		
		return condition;
	}

}
