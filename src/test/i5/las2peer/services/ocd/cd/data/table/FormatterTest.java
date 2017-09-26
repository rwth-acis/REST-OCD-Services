package i5.las2peer.services.ocd.cd.data.table;

import static org.junit.Assert.*;

import org.junit.Test;

public class FormatterTest {

	@Test
	public void decimalsTest() {
		
		String insert;
		String result;
		Formatter formatter = new Formatter();
		
		insert="0.242352356";
		result=formatter.decimals(insert, 4);
		assertEquals("0,2424", result);
		
	}
	
	
}
