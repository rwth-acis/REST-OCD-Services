package i5.las2peer.services.ocd.cd.data.table;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import i5.las2peer.services.ocd.cd.data.simulation.SimulationSeries;

@RunWith(MockitoJUnitRunner.class)
public class PrinterTest {
	
	@Mock SimulationSeries simulation;
	@Mock Table table;
	
	@Test
	public void printerTest() {
		
		TablePrinter printer = new TablePrinter();
		Table table = new Table();		
		TableRow row1 = new TableRow();
		TableRow row2 = new TableRow();
		TableRow row3 = new TableRow();		
		row1.add("aaa").add("1234").add("yxcv").add("vvv");
		row2.add("bbb").add("125").add("qwer").add("www");
		row3.add("ccc").add("654").add("asdf").add("zzz");		
		table.add(row1);
		table.add(row2);
		table.add(row3);		
	}
	
}
