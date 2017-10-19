package i5.las2peer.services.ocd.cooperation.simulation.termination;

public class ConditionFactory {

	/**
	 * Creates a break condition with default parameters
	 * 
	 * @param condition
	 * @return
	 */
	public Condition build(ConditionType conditionEnum) {

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
	 * @param condition
	 * @return
	 */
	public Condition build(ConditionType conditionEnum, int[] parameters) {

		Condition conditionClass = null;
		try {
			conditionClass = conditionEnum.getEnumClass().newInstance();
			conditionClass.setParameters(parameters);
			return conditionClass;
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new IllegalArgumentException("cant create condition");
	}

}
