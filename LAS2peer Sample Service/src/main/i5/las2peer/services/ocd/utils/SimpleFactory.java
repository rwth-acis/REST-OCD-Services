package i5.las2peer.services.ocd.utils;

public interface SimpleFactory<T, D> {
	
	public T getInstance(D descriptor) throws InstantiationException, IllegalAccessException;
	
}
