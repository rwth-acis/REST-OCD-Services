package i5.las2peer.services.ocd.adapters.visualOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.graphstream.stream.file.FileSinkSVG;

public class SvgVisualOutputAdapter extends AbstractVisualOutputAdapter {
	
	@Override
	public void writeGraph(CustomGraph graph) throws AdapterException {
		FileSinkSVG fileSink = new FileSinkSVG();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		// Writes out the graph using the IOHandler
		try {
			fileSink.writeAll(graph, outStream);
			String outString = outStream.toString();
			writer.write(outString);
		}
		catch(IOException e) {
			throw new AdapterException(e);
		} finally {
			try {
				outStream.close();
			}
			catch(IOException e) {
			}
			try {
				writer.close();
			}
			catch(IOException e) {
			}
		}
	}

}
