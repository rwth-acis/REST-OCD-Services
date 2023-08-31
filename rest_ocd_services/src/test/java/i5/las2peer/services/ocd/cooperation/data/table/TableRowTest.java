package i5.las2peer.services.ocd.cooperation.data.table;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;


public class TableRowTest {
		
	@Test
	public void addString() {		

		TableRow row = new TableRow();
		List<String> result;
		String testString1 = "skainTcskiFH";
		String testString2 = "stASFF";
		String testString3 = "qqqqqqqq";
		
		row.add(testString1);
		result = row.getCells();
		assertEquals(1, result.size());
		assertEquals(testString1, result.get(0));
		
		row.add(testString2);
		result = row.getCells();
		assertEquals(2, result.size());
		assertEquals(testString1, result.get(0));
		assertEquals(testString2, result.get(1));
		
		row.add(testString3);
		result = row.getCells();
		assertEquals(3, result.size());
		assertEquals(testString1, result.get(0));
		assertEquals(testString2, result.get(1));
		assertEquals(testString3, result.get(2));
			
	}
		
	@Test
	public void addDouble() {
		
		TableRow row = new TableRow();
		List<String> result;
		double test1 = 3.5;
		double test2 = 1.5;
		double test3 = 0.5;
		
		row.add(test1);
		result = row.getCells();
		assertEquals(1, result.size());
		assertEquals(String.valueOf(test1), result.get(0));
		
		row.add(test2);
		result = row.getCells();
		assertEquals(2, result.size());
		assertEquals(String.valueOf(test1), result.get(0));
		assertEquals(String.valueOf(test2), result.get(1));
		
		row.add(test3);
		result = row.getCells();
		assertEquals(3, result.size());
		assertEquals(String.valueOf(test1), result.get(0));
		assertEquals(String.valueOf(test2), result.get(1));
		assertEquals(String.valueOf(test3), result.get(2));
				
	}
	
	@Test
	public void addTableRow() {
		

		List<String> result;
		TableRow row = new TableRow();
		
		TableRow row2 = new TableRow();
		String row2String1 = "skdhag";
		String row2String2 = "sdkgjy";
		row2.add(row2String1);
		row2.add(row2String2);
		
		row.add(row2);
		result = row.getCells();
		assertEquals(2, result.size());
		assertEquals(row2String1, result.get(0));
		assertEquals(row2String2, result.get(1));
		
	}
	
	@Test
	public void addAndClear() {		

		TableRow row = new TableRow();
		row.add("1");
		row.add("2");
		row.add("3");
		row.add("4");
		row.add("5");
		assertEquals(5, row.getCells().size());
		row.clear();
		assertEquals(0, row.getCells().size());
	}
	
	@Test
	public void print() {
		
		TableRow row = new TableRow();
		String testString1 = "skainTcskiFH";
		String testString2 = "stASFF";
		String testString3 = "qqqqqqqq";
		
		row.add(testString1);
		row.add(testString2);
		row.add(testString3);
		
		String result = row.print("\t");
		assertEquals(testString1 + "\t" + testString2 + "\t" +  testString3, result);
		
		result = row.print(",");
		assertEquals(testString1 + "," + testString2 + "," +  testString3, result);

		
	}
	

}
