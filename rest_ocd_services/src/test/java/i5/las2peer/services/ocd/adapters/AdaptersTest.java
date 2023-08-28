package i5.las2peer.services.ocd.adapters;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Test;

public class AdaptersTest {

	@Test
	public void testLineBreakN() throws IOException {
		String lines = "1 0 1\n2 0 1";
		Reader reader = new StringReader(lines);
		List<String> line = Adapters.readLine(reader);
		while(line.size()>0) {
			System.out.println(line);
			line = Adapters.readLine(reader);
		}
	}
	
	@Test
	public void testLineBreakR() throws IOException {
		String lines = "1 0 1\r2 0 1";
		Reader reader = new StringReader(lines);
		List<String> line = Adapters.readLine(reader);
		while(line.size()>0) {
			System.out.println(line);
			line = Adapters.readLine(reader);
		}
	}
	
	@Test
	public void testLineBreakRN() throws IOException {
		String lines = "1 0 1\r\n2 0 1";
		Reader reader = new StringReader(lines);
		List<String> line = Adapters.readLine(reader);
		while(line.size()>0) {
			System.out.println(line);
			line = Adapters.readLine(reader);
		}
	}

}
