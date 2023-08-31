package i5.las2peer.services.ocd.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class DatabaseConfigTest {
	private static DatabaseConfig dc = new DatabaseConfig();



	@Test
	public void getPropertiesTest() throws IOException {
		Properties testProps = new Properties();
		FileInputStream inputStream = new FileInputStream("ocd/arangoDB/config.properties");
		testProps.load(inputStream);

		Properties props = dc.getConfigProperties();
		System.out.println("HOST:"+props.getProperty("HOST"));
		assertEquals(testProps.getProperty("HOST"), props.getProperty("HOST"));
		
		System.out.println("PORT:"+props.getProperty("PORT"));
		assertEquals(testProps.getProperty("PORT"), props.getProperty("PORT"));
		
//		System.out.println("USER:"+props.getProperty("USER"));
//		assertEquals("root", props.getProperty("USER"));
		
//		System.out.println("PASSWORD:"+props.getProperty("PASSWORD"));
//		assertEquals("password", props.getProperty("PASSWORD"));
		
		System.out.println("DATABASENAME:"+props.getProperty("DATABASENAME"));
		String dbName = props.getProperty("DATABASENAME");
		assertEquals("ocdDB",dbName );

		System.out.println("TESTDATABASENAME:"+props.getProperty("TESTDATABASENAME"));
		assertEquals("testDB", props.getProperty("TESTDATABASENAME"));
		
	}

}
