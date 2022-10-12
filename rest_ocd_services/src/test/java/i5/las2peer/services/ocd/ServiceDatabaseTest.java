package i5.las2peer.services.ocd;

import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.utils.Database;
import i5.las2peer.services.ocd.utils.DatabaseConfig;

public class ServiceDatabaseTest {
	
	
	private static Database database;

	//@BeforeClass
	public static void clearDatabase() {
		DatabaseConfig.setConfigFile(false);
		database = new Database();
		System.out.println("-------------------START------------------");
	}
	
	//@Test
	public void test() {
		List<Integer> l = new ArrayList<Integer>();
		l.add(1);l.add(2);
		List<Cover> covers = database.getCovers("cralem", "", l, null, 5, 5);
		System.out.println(covers.toString());
		database.printDB();
		database.deleteDatabase();
	}

}
