package i5.las2peer.services.ocd;

import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.utils.Database;

public class ServiceDatabaseTest {
	
	
	private static Database database;

	@BeforeAll
	public static void clearDatabase() {
		database = new Database(true);
	}


	@AfterAll
	public static void deleteDatabase() {
		database.deleteDatabase();
	}
	
	@Test
	public void test() {
		List<Integer> l = new ArrayList<Integer>();
		l.add(1);l.add(2);
		List<Cover> covers = database.getCovers("cralem", "", l, null, 5, 5);
		System.out.println(covers.toString());
		database.printDB();
		database.deleteDatabase();
	}

}
