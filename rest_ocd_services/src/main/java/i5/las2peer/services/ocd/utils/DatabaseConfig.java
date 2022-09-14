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
	private static final String PATH = "./ocd/arangoDB/";
	private static final String FILENAME = "config.properties";
	private static final String TESTFILENAME = "config_test.properties";
	private static final String STANDARD_CONFIG_FILENAME = "standard_config.properties";
	private static final File CONFIG_FILE = new File(PATH+FILENAME);
	private static final File TESTFILE = new File(PATH + TESTFILENAME);
	private static final File STANDARD_CONFIG_FILE = new File(PATH + STANDARD_CONFIG_FILENAME);
	private static Properties props = new Properties();
	
	public void initStandardConfigFile() {
		props.setProperty("HOST", "127.0.0.1");
		props.setProperty("PORT", "8529");
		props.setProperty("USER", "root");
		props.setProperty("PASSWORD", "password");
		props.setProperty("DATABASENAME", "ocd_db");
		try {
			FileOutputStream outputStream = new FileOutputStream(STANDARD_CONFIG_FILE);
			props.store(outputStream, null);
			outputStream.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void initTestFile() {
		props.setProperty("HOST", "127.0.0.1");
		props.setProperty("PORT", "8529");
		props.setProperty("USER", "root");
		props.setProperty("PASSWORD", "password");
		props.setProperty("DATABASENAME", "test_db");
		try {
			FileOutputStream outputStream = new FileOutputStream(TESTFILE);
			props.store(outputStream, null);
			outputStream.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	public static void setConfigFile(boolean testFile) {
		FileInputStream inputStream;
		props.clear();
		try {
			if(testFile) {
				inputStream = new FileInputStream(TESTFILE);
			}
			else {
				inputStream = new FileInputStream(STANDARD_CONFIG_FILE);
			}
			props.load(inputStream);
		}catch(IOException e) {
			e.printStackTrace();
		}
		try {
			FileOutputStream outputStream = new FileOutputStream(CONFIG_FILE);
			props.store(outputStream, null);
			outputStream.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
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
