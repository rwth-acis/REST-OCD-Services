package i5.las2peer.services.ocd.utils;

/**
 * A common interface for all factories using descriptor objects to produce instances of different classes.
 * A descriptor defines the class to instantiate.
 * @author Sebastian
 *
 * @param <T> The common superclass type of the classes to instantiate.
 * @param <D> The type of the descriptor objects which define the subclass to instantiate.
 */
public interface SimpleFactory<T, D> {
	
	/**
	 * Produces a new object instance.
	 * @param descriptor The descriptor defining the subclass to instantiate.
	 * @return The created object.
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occured on the instance
	 */
	public T getInstance(D descriptor) throws InstantiationException, IllegalAccessException;
	
}
