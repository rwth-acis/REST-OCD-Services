package i5.las2peer.services.ocd.utils;

import java.util.Map;

/**
 * A common interface for all factories using descriptor objects to produce instances of different classes.
 * A descriptor defines the class to instantiate although some descriptors may not correspond to a class instantiatable by the factory.
 * @author Sebastian
 *
 * @param <T> The common superclass type implementing the Parameterizable interface of the classes to instantiate.
 * @param <D> The type of the descriptor objects which define the subclass to instantiate.
 */
public interface ConditionalParameterizableFactory<T extends Parameterizable, D> {
	
	/**
	 * States whether a descriptor object defines an instantiatable subclass of T.
	 * @param descriptor The descriptor object.
	 * @return TRUE, if the object corresponds to an instantiatable subclass, otherwise FALSE.
	 */
	public boolean isInstantiatable(D descriptor);
	
	/**
	 * Produces a new object instance.
	 * @param descriptor The descriptor defining the subclass to instantiate.
	 * @param parameters The parameters to pass to the created object.
	 * @return The created object.
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access of the instance occured
	 */
	public T getInstance(D descriptor, Map<String, String> parameters) throws InstantiationException, IllegalAccessException;
	
}