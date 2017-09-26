package i5.las2peer.services.ocd.cd.simulation.dynamic;

import java.io.Serializable;

import i5.las2peer.services.ocd.cd.simulation.Agent;
import i5.las2peer.services.ocd.cd.simulation.Simulation;

/**
 * Dynamics are used to update the nodes strategies. The concrete update rules
 * have to be implemented as sub classes.
 * 
 */
public abstract class Dynamic implements Serializable {

	/////////////// Attributes ///////////

	private static final long serialVersionUID = 1L;

	/**
	 * parameters of the dynamic.
	 */
	private double[] values;

	/////////////// Constructor //////////

	protected Dynamic() {
		this(1.5);
	}
	
	protected Dynamic(double value) {
		this(new double[]{value});
	}
	
	protected Dynamic(double[] values) {
		this.values = values;
	}
	
	/////////////// Methods ///////////////	

	public double[] getValues() {
		return this.values;
	}

	/////////////// Override ///////////////

	/**
	 * determines the concrete update rule dynamic it have to be
	 * implemented in the sub classes
	 * 	 
	 */
	public abstract boolean getNewStrategy(Agent agent, Simulation simulation);
	
	
	/**
	 * assign a dynamic type to a concrete subclass 
	 */
	public DynamicType getDynamicType() {
		return DynamicType.getType(this.getClass());
	}

}
