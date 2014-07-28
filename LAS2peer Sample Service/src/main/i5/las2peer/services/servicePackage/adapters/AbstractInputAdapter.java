package i5.las2peer.services.servicePackage.adapters;

import java.io.Reader;

public abstract class AbstractInputAdapter implements InputAdapter {

	protected Reader reader;
	
	@Override
	public Reader getReader() {
		return reader;
	}

	@Override
	public void setReader(Reader reader) {
		this.reader = reader;
	}
	
}
