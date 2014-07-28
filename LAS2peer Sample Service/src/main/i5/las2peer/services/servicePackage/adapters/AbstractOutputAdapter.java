package i5.las2peer.services.servicePackage.adapters;

import java.io.Writer;

public abstract class AbstractOutputAdapter implements OutputAdapter {

	protected Writer writer;
	
	@Override
	public Writer getWriter() {
		return writer;
	}
	
	@Override
	public void setWriter(Writer writer) {
		this.writer = writer;
	}
	
}
