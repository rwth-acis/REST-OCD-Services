package i5.las2peer.services.ocd;

import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;

import i5.las2peer.services.ocd.graphs.OcdPersistenceLoadException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.utils.Database;
import i5.las2peer.services.ocd.utils.DatabaseConfig;

public class ServiceDatabaseTest {
	
	
	private static Database database;

	@BeforeClass
	public static void clearDatabase() {
		database = new Database(true);
	}


	@AfterClass
	public static void deleteDatabase() {
		database.deleteDatabase();
	}
	
	@Test
	public void test() throws OcdPersistenceLoadException {
		List<Integer> l = new ArrayList<Integer>();
		l.add(1);l.add(2);
		List<Cover> covers = database.getCovers("cralem", "", l, null, 5, 5);
		System.out.println(covers.toString());
		database.printDB();
		database.deleteDatabase();
	}

}
