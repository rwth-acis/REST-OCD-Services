package i5.las2peer.services.ocd.cooperation.data.table;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


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
