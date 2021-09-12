package i5.las2peer.services.ocd.cooperation.data.simulation;

public enum GroupType {
	
	Rescaled_PD("Rescaled PD"),
	Rescaled_SD("Rescaled SD");

	
private String read;

GroupType(String read) {
	this.read = read;
}

public String humanRead() {
	return read;
}


public static GroupType fromString(String string) {
	
	for (GroupType type : GroupType.values()) {
		if (string.equalsIgnoreCase(type.name()) || string.equalsIgnoreCase(type.humanRead())) {
			return type;
		}
	}
	return Rescaled_PD;
}
}