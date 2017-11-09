package i5.las2peer.services.ocd.viewer;

import java.io.StringWriter;
import java.io.Writer;

import javax.ws.rs.core.Response;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.visualOutput.VisualOutputAdapter;
import i5.las2peer.services.ocd.adapters.visualOutput.VisualOutputAdapterFactory;
import i5.las2peer.services.ocd.adapters.visualOutput.VisualOutputFormat;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.utils.RequestHandler;

/**
 * Manages different request-related tasks for the Service Class particularly for the viewer service.
 * Mainly in charge of simple IO tasks and of creating entity managers for persistence purposes.
 * @author Sebastian
 *
 */
public class ViewerRequestHandler extends RequestHandler {
	
	/**
	 * The factory used for creating visual output adapters.
	 */
	private VisualOutputAdapterFactory visualOutputAdapterFactory = new VisualOutputAdapterFactory();
	
	/**
	 * Creates a visual graph output in a specified format.
	 * @param graph The graph.
	 * @param outputFormat The format.
	 * @return The visual graph output.
	 * @throws AdapterException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Response writeGraph(CustomGraph graph, VisualOutputFormat outputFormat) throws AdapterException, InstantiationException, IllegalAccessException {
		VisualOutputAdapter adapter = visualOutputAdapterFactory.getInstance(outputFormat);
    	Writer writer = new StringWriter();
    	adapter.setWriter(writer);
		adapter.writeGraph(graph);
		return Response.ok().entity(writer.toString()).build();
	}
	
	/**
	 * Creates a visual cover output in a specified format.
	 * @param cover The cover.
	 * @param outputFormat The format.
	 * @return The visual cover output.
	 * @throws AdapterException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Response writeCover(Cover cover, VisualOutputFormat outputFormat) throws AdapterException, InstantiationException, IllegalAccessException {
		return writeGraph(cover.getGraph(), outputFormat);
	}
	
	/**
	 * Creates a visual CentralityMap output in a specified format.
	 * @param map The CentralityMap.
	 * @param outputFormat The format.
	 * @return The visual CentralityMap output.
	 * @throws AdapterException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Response writeCentralityMap(CentralityMap map, VisualOutputFormat outputFormat) throws AdapterException, InstantiationException, IllegalAccessException {
		return writeGraph(map.getGraph(), outputFormat);
	}

}