package i5.las2peer.services.ocd.cooperation.data.table;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;


public class DataTableTest {
	
	@Test
	public void addTwoDoubleArray() {
		
		double[] val1 = new double[]{1.0,2.0,3.0,4.0};
		double[] val2 = new double[]{1.2,2.3,4.5,2.1};
		
		DataTable table = new DataTable();
		table.add(val1, val2);
		List<TableRow> tableRows = table.getTableRows();
		assertEquals(val1.length, tableRows.size());
		assertEquals(String.valueOf(1.0), tableRows.get(0).getCells().get(0));
		assertEquals(String.valueOf(1.2), tableRows.get(0).getCells().get(1));
		assertEquals(String.valueOf(2.0), tableRows.get(1).getCells().get(0));
		assertEquals(String.valueOf(2.3), tableRows.get(1).getCells().get(1));
		assertEquals(String.valueOf(3.0), tableRows.get(2).getCells().get(0));
		assertEquals(String.valueOf(4.5), tableRows.get(2).getCells().get(1));
		assertEquals(String.valueOf(4.0), tableRows.get(3).getCells().get(0));
		assertEquals(String.valueOf(2.1), tableRows.get(3).getCells().get(1));
		
	}
	
}
