package i5.las2peer.services.ocd.cd.data.table;

import java.util.Collection;
import java.util.HashSet;

/**
 * Provide functions to format strings
 */
public class Formatter {

	int places;
	Collection<String> macros;
	
	public Formatter() {
		places = 4;
		macros = new HashSet<>();
	}
	
	///// Decimals /////	
	
	/**
	 * @param string 
	 * @param places the amount of decimal places
	 * @return modified string
	 */
	public String decimals(String string, int places) {
		
		String format = "%." + places + "f";
		return decimals(string, format);
	}

	public String decimals(String string, String format) {

		String result;
		try {
			double decimal = Double.valueOf(string);
			result = String.format(format, decimal);
		} catch (Exception e) {
			return string;
		}
		return result;
	}
	
	public String decimals(String string) {
		
		String format = "%." + places + "f";
		return decimals(string, format);
	}

	///// Macros /////

	public String macros(String string, Collection<String> macros) {
		return macros(string, "\\", macros);
	}

	/**
	 * Add a prefix to every occurrence of the specified macros.
	 * 
	 * @param string the string that is checked for macros
	 * @param prefix added in front of the macro
	 * @param macros a collection of macros that have to be modified
	 * @return the modified string
	 */
	public String macros(String string, String prefix, Collection<String> macros) {

		for (String macro : macros) {
			string = string.replace("/" + macro + "/g", prefix + macro);
		}
		return string;
	}
	
	public String macros(String string, String prefix) {

		for (String macro : macros) {
			string = string.replace("/" + macro + "/g", prefix + macro);
		}
		return string;
	}

}
