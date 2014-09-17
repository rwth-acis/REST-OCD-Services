package i5.las2peer.services.ocd.utils;

import java.util.Map;

public interface ConditionalParameterizableFactory<T, D> {
	
	public boolean isInstantiatable(D descriptor);
	
	public T getInstance(D descriptor, Map<String, String> parameters) throws InstantiationException, IllegalAccessException;
	
}