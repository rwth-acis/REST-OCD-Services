package i5.las2peer.services.ocd.utils;

import static org.junit.Assert.*;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

public class DatabaseConfigTest {
	private static DatabaseConfig dc = new DatabaseConfig();



	@Test
	public void getPropertiesTest() {
		Properties props = dc.getConfigProperties();
		System.out.println("HOST:"+props.getProperty("HOST"));
		assertEquals("127.0.0.1", props.getProperty("HOST"));
		
		System.out.println("PORT:"+props.getProperty("PORT"));
		assertEquals("8529", props.getProperty("PORT"));
		
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
