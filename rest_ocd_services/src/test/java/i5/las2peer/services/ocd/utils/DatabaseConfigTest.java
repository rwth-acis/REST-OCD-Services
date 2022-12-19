package i5.las2peer.services.ocd.utils;

import static org.junit.Assert.*;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DatabaseConfigTest {
	private static DatabaseConfig dc = new DatabaseConfig();
	
	@Test
	public void test() {
		System.out.println("config vor dbconfigtest : " + dc.getConfigProperties().toString());
		DatabaseConfig.setConfigFile(false);
		System.out.println("config nachdem sie auf normale db gesetzt wurde : " + dc.getConfigProperties().toString());
	}


	@Test
	public void getPropertiesTest() {
		Properties props = dc.getConfigProperties();
		System.out.println("HOST:"+props.getProperty("HOST"));
		assertEquals("127.0.0.1", props.getProperty("HOST"));
		
		System.out.println("PORT:"+props.getProperty("PORT"));
		assertEquals("8529", props.getProperty("PORT"));
		
		System.out.println("USER:"+props.getProperty("USER"));
		assertEquals("root", props.getProperty("USER"));
		
		System.out.println("PASSWORD:"+props.getProperty("PASSWORD"));
		assertEquals("password", props.getProperty("PASSWORD"));
		
		System.out.println("DATABASENAME:"+props.getProperty("DATABASENAME"));
		assertEquals("ocd_db", props.getProperty("DATABASENAME"));
		
	}

}
