package i5.las2peer.services.ocd.cooperation.data.table;

import static org.junit.Assert.*;

import org.junit.Test;

import i5.las2peer.services.ocd.cooperation.data.table.Formatter;

public class FormatterTest {

	@Test
	public void decimalsTest() {
		
		String insert;
		String result;
		Formatter formatter = new Formatter();
		
		insert="0.242352356";
		result=formatter.decimals(insert, 4);
		assertTrue(result.equals("0,2424") || result.equals("0.2424"));		
	}
	
	
}
