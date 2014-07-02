package i5.las2peer.services.servicePackage.adapters;

public abstract class AbstractAdapter implements Adapter {

	protected String filename;
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
}
