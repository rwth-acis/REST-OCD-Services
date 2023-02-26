package i5.las2peer.services.ocd.utils;

import java.io.FileWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;

public class DatabaseConfig {

	private static final String PATH = "ocd/arangoDB/";
	private static final String FILENAME = "config.properties";
	private static final File CONFIG_FILE = new File(PATH+FILENAME);
	private static Properties props = new Properties();

	public Properties getConfigProperties() {
		try {
			FileInputStream inputStream = new FileInputStream(CONFIG_FILE);
			props.load(inputStream);
		}catch(IOException e) {
			e.printStackTrace();
		}
		return props;
	}

}
