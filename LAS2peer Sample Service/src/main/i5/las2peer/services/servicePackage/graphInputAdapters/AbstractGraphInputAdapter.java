package i5.las2peer.services.servicePackage.graphInputAdapters;


public abstract class AbstractGraphInputAdapter implements GraphInputAdapter {

	protected String filename;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
