package i5.las2peer.services.ocd.adapters.visualOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import y.io.IOHandler;
import y.view.Graph2D;

import yext.svg.io.SVGIOHandler;

public class SvgVisualOutputAdapter extends AbstractVisualOutputAdapter {
	
	@Override
	public void writeGraph(Graph2D graph) throws AdapterException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		// Writes out the graph using the IOHandler
		IOHandler ioh = new SVGIOHandler();
		try {
			ioh.write(graph, outStream);
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
