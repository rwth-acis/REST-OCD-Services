package i5.las2peer.services.ocd;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import i5.las2peer.services.ocd.utils.*;
import i5.las2peer.services.ocd.utils.Error;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.math3.linear.RealMatrix;
import org.la4j.matrix.sparse.CCSMatrix;

import i5.las2peer.api.Context;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.api.execution.ServiceInvocationException; //TODO: Check
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.p2p.AgentNotRegisteredException;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.execution.ExecutionContext;
import i5.las2peer.services.ocd.adapters.centralityInput.CentralityInputFormat;
import i5.las2peer.services.ocd.adapters.centralityOutput.CentralityOutputFormat;
import i5.las2peer.services.ocd.adapters.coverInput.CoverInputFormat;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputFormat;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputFormat;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputFormat;
import i5.las2peer.services.ocd.adapters.visualOutput.VisualOutputFormat;
import i5.las2peer.services.ocd.algorithms.ContentBasedWeightingAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithmFactory;
import i5.las2peer.services.ocd.benchmarks.GroundTruthBenchmark;
import i5.las2peer.services.ocd.benchmarks.OcdBenchmarkFactory;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.data.CentralitySimulationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.data.CentralityMapId;
import i5.las2peer.services.ocd.centrality.evaluation.CorrelationCoefficient;
import i5.las2peer.services.ocd.centrality.evaluation.StatisticalProcessor;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmFactory;
import i5.las2peer.services.ocd.centrality.utils.CentralitySimulation;
import i5.las2peer.services.ocd.centrality.utils.CentralitySimulationFactory;
import i5.las2peer.services.ocd.centrality.utils.MatrixOperations;
import i5.las2peer.services.ocd.cooperation.data.SimulationEntityHandler;
import i5.las2peer.services.ocd.cooperation.data.mapping.MappingFactory;
import i5.las2peer.services.ocd.cooperation.data.mapping.SimulationGroupSetMapping;
import i5.las2peer.services.ocd.cooperation.data.mapping.SimulationSeriesSetMapping;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesGroup;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesGroupMetaData;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesMetaData;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesParameters;
import i5.las2peer.services.ocd.cooperation.simulation.SimulationBuilder;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.DynamicType;
import i5.las2peer.services.ocd.cooperation.simulation.game.GameType;
import i5.las2peer.services.ocd.cooperation.simulation.termination.ConditionType;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CoverId;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomGraphId;
import i5.las2peer.services.ocd.graphs.GraphCreationLog;
import i5.las2peer.services.ocd.graphs.GraphCreationType;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;
import i5.las2peer.services.ocd.metrics.ExecutionTime;
import i5.las2peer.services.ocd.metrics.KnowledgeDrivenMeasure;
import i5.las2peer.services.ocd.metrics.NewmanModularityCombined;
import i5.las2peer.services.ocd.metrics.OcdMetricFactory;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricLogId;
import i5.las2peer.services.ocd.metrics.OcdMetricType;
import i5.las2peer.services.ocd.metrics.StatisticalMeasure;
import i5.las2peer.services.ocd.utils.Error;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import i5.las2peer.services.ocd.utils.InvocationHandler;
import i5.las2peer.services.ocd.utils.ThreadHandler;
import i5.las2peer.services.ocd.viewer.LayoutHandler;
import i5.las2peer.services.ocd.viewer.ViewerRequestHandler;
import i5.las2peer.services.ocd.viewer.layouters.GraphLayoutType;
import i5.las2peer.services.ocd.viewer.painters.CoverPainter;
import i5.las2peer.services.ocd.viewer.painters.CoverPainterFactory;
import i5.las2peer.services.ocd.viewer.painters.CoverPaintingType;
import i5.las2peer.services.ocd.viewer.utils.CentralityVisualizationType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import y.algo.GraphChecker;
import y.base.Graph;

/**
 * 
 * LAS2peer OCD Service Class
 * 
 * Provides the RESTful interface of the overlapping community detection
 * service.
 * 
 * @author Sebastian
 *
 */

@ManualDeployment
@ServicePath("/ocd")
@Api
public class ServiceClass extends RESTService {

	///////////////////////////////////////////////////
	///// Service initialization.
	///////////////////////////////////////////////////

	@Override
	protected void initResources() {
		getResourceConfig().register(RootResource.class);
	}

	public ServiceClass() {

		setFieldValues();

		// instantiate inactivityHandler to regularly remove content of inactive users.
		inactivityHandler = new InactivityHandler(entityHandler, threadHandler, this);
	}

	///////////////////////////////////////////////////////////
	///// ATTRIBUTES
	///////////////////////////////////////////////////////////

	/**
	 * l2p logger
	 */
	private final static L2pLogger logger = L2pLogger.getInstance(ServiceClass.class.getName());
	/**
	 * The thread handler used for algorithm, benchmark and metric execution.
	 */
	private final static ThreadHandler threadHandler = new ThreadHandler();

	/**
	 * The request handler used for simple request-related tasks.
	 */
	private final static ViewerRequestHandler requestHandler = new ViewerRequestHandler();

	/**
	 * The entity handler used for access stored entities.
	 */
	private final static SimulationEntityHandler entityHandler = new SimulationEntityHandler();

	/**
	 * The factory used for creating benchmarks.
	 */
	private final static OcdBenchmarkFactory benchmarkFactory = new OcdBenchmarkFactory();

	/**
	 * The factory used for creating algorithms.
	 */
	private final static OcdAlgorithmFactory algorithmFactory = new OcdAlgorithmFactory();

	/**
	 * The factory used for creating centrality simulations.
	 */
	private final static CentralitySimulationFactory centralitySimulationFactory = new CentralitySimulationFactory();
	
	/**
	 * The factory used for creating centrality algorithms.
	 */
	private final static CentralityAlgorithmFactory centralityAlgorithmFactory = new CentralityAlgorithmFactory();

	/**
	 * The factory used for creating metrics.
	 */
	private final static OcdMetricFactory metricFactory = new OcdMetricFactory();

	/**
	 * The layout handler used for layouting graphs and covers.
	 */
	private final static LayoutHandler layoutHandler = new LayoutHandler();

	/**
	 * The inactivity handler for removing content of inactive users.
	 */
	private static InactivityHandler inactivityHandler;


	/**
	 * Number of days of inactivity allowed, before user content gets removed.
	 */
	private int maxInactiveDays;

	/**
	 * Password to be used to adjust number of days before inactive user content is removed.
	 */
	private String inactivityHandlerPassword;

	//////////////////////////////////////////////////////////////////
	///////// Utility Methods
	//////////////////////////////////////////////////////////////////

	public static String getUserName() {
		return ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	}

	public static String getUserId() {
		return Context.getCurrent().getMainAgent().getIdentifier();
	}

	public int getMaxInactiveDays() { return this.maxInactiveDays; }

	public String getInactivityHandlerPassword(){return this.inactivityHandlerPassword;}

	/**
	 * This method is used to set fresh field values, in case modifications were made in
	 * i5.las2peer.services.ocd.ServiceClass.properties file
	 */
	public void setFreshFieldValues() {
		setFieldValues();
	}


	//////////////////////////////////////////////////////////////////
	///////// REST Service Methods
	//////////////////////////////////////////////////////////////////

	@Api
	@Path("/")
	@SwaggerDefinition(info =
		@Info(title = "LAS2peer OCD Service",
				version = "1.0",
				description = "A RESTful service for overlapping community detection.",
				//termsOfService = "sample-tos.io",
				contact = @Contact(name = "Maximilian Kissgen", email = "maximilian.kissgen@rwth-aachen.de"),
				license = @License(name = "Apache License 2", url = "http://www.apache.org/licenses/LICENSE-2.0")),
			schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS}
	)
	public static class RootResource {

		// get access to the service class
		private final ServiceClass service = (ServiceClass) Context.getCurrent().getService();
		
		/**
		 * Simple function to validate a user login. Basically it only serves as
		 * a "calling point" and does not really validate a user (since this is
		 * done previously by LAS2peer itself, the user does not reach this
		 * method if he or she is not authenticated).
		 * 
		 * @return A confirmation XML.
		 */
		@GET
		@Path("validate")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"special"}, value = "User validation", notes = "Simple function to validate a user login.")
		public Response validateLogin() {
			try {
				// update user inactivity info when user logs in.
				inactivityHandler.refreshUserInactivityData(getUserName());
				return Response.ok(requestHandler.writeConfirmationXml()).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}

		/**
		 * This method can be used to adjust number of days before content of inactive users is removed. This is an
		 * alternative to directly modifying the value in i5.las2peer.services.ocd.ServiceClass.properties file.
		 *
		 * @param contentStr   XML with new inactivity days to set and the access code that allows modifications to take place.
		 * @return             Newly set value of inactive days allowed before content deletion, or Error XML.
		 */
		@POST
		@Path("allowedInactivity")
		@Produces(MediaType.TEXT_PLAIN)
		public Response adjustAllowedInactivity(String contentStr) {

			Map<String, String> parameters;
			String inactivityPassword;
			int allowedInactivityDays;

			try {
				parameters = requestHandler.parseParameters(contentStr);
				if (parameters.get("AccessCode") == null) {
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Access code is required to modify allowed inactivity.");
				}
				String accessCode = parameters.get("AccessCode");

				// read most recent password for inactivityHandler
				service.setFreshFieldValues();
				inactivityPassword = service.getInactivityHandlerPassword();
				if(!accessCode.equals(inactivityPassword)){
					return requestHandler.writeError(Error.INTERNAL, "Access code is invalid.");
				}


				String allowedInactivityDaysString = parameters.get("AllowedInactivityDays");
				if ((allowedInactivityDaysString == null) || (Integer.parseInt(allowedInactivityDaysString) < 0)) {
					return requestHandler.writeError(Error.PARAMETER_INVALID, "AllowedInactivityDays parameter needs to be provided and be a positive integer.");
				}
				allowedInactivityDays = Integer.parseInt(allowedInactivityDaysString);

			} catch (Exception e) {
				requestHandler.log(Level.WARNING, "", e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Invalid parameters provided.");

			}

			// write new allowed inactivity value to ServiceClass properties file
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter("etc/i5.las2peer.services.ocd.ServiceClass.properties"));
				writer.write("maxInactiveDays=" + allowedInactivityDays);
				writer.newLine();
				writer.write("inactivityHandlerPassword=" + inactivityPassword );
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return Response.ok(allowedInactivityDays).build();

		}

		//////////////////////////////////////////////////////////////////////////
		//////////// GRAPHS
		//////////////////////////////////////////////////////////////////////////

		/**
		 * Imports a graph.
		 * 
		 * @param nameStr
		 *            The name for the graph.
		 * @param creationTypeStr
		 *            The creation type the graph was created by.
		 * @param graphInputFormatStr
		 *            The name of the graph input format.
		 * @param doMakeUndirectedStr
		 *            Optional query parameter. Defines whether directed edges
		 *            shall be turned into undirected edges (TRUE) or not.
		 * @param startDateStr
		 *            Optional query parameter. For big graphs start date is the
		 *            date from which the file will start parse.
		 * @param endDateStr
		 *            Optional query parameter. For big graphs end date is the
		 *            date till which the file will parse.
		 * @param involvedUserURIsStr
		 * 			  Optional query parameter. Users to consider for LMS Triplestore import
		 * @param showUserNamesStr
		 * 			  Optional query parameter. Whether to show usernames as node names for LMS Triplestore import
		 * @param indexPathStr
		 *            Optional query parameter. Set index directory.
		 * @param filePathStr
		 *            Optional query parameter. For testing purpose, file
		 *            location of local file can be given.
		 * @param contentStr
		 *            The graph input.
		 * @return A graph id xml. Or an error xml.
		 */
		@POST
		@Path("graphs")
		@Produces(MediaType.TEXT_XML)
		@Consumes(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"import"}, value = "Import Graph", notes = "Imports a graph with various formats possible.")
		public Response createGraph(@DefaultValue("unnamed") @QueryParam("name") String nameStr,
				@DefaultValue("UNDEFINED") @QueryParam("creationType") String creationTypeStr,
				@DefaultValue("GRAPH_ML") @QueryParam("inputFormat") String graphInputFormatStr,
				@DefaultValue("FALSE") @QueryParam("doMakeUndirected") String doMakeUndirectedStr,
				@DefaultValue("2004-01-01") @QueryParam("startDate") String startDateStr,
				@DefaultValue("2004-01-20") @QueryParam("endDate") String endDateStr,
				@DefaultValue("") @QueryParam("involvedUserURIs") String involvedUserURIsStr,
				@DefaultValue("false") @QueryParam("showUserNames") String showUserNamesStr,
				@DefaultValue("indexes") @QueryParam("indexPath") String indexPathStr,
				@DefaultValue("ocd/test/input/stackexAcademia.xml") @QueryParam("filePath") String filePathStr,
				String contentStr) {
			try {
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				GraphInputFormat format;
				CustomGraph graph;
				try {
					format = GraphInputFormat.valueOf(graphInputFormatStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified input format does not exist.");
				}
				GraphCreationType benchmarkType;
				try {
					benchmarkType = GraphCreationType.valueOf(creationTypeStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified input format does not exist.");
				}
				try {
					int subDirName = 0;
					File indexPathDir = new File(indexPathStr);

					if (indexPathDir.exists()) {
						for (String subDir : indexPathDir.list()) {
							if (Integer.parseInt(subDir) == subDirName) {
								subDirName++;
							}
						}
					}
					indexPathStr = indexPathStr + File.separator + String.valueOf(subDirName);

				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.INTERNAL, "Index path exception.");
				}
				try {
					Map<String, String> param = new HashMap<String, String>();
					if (format == GraphInputFormat.NODE_CONTENT_EDGE_LIST || format == GraphInputFormat.XML || format == GraphInputFormat.LMS_TRIPLESTORE) {
						param.put("startDate", startDateStr);
						param.put("endDate", endDateStr);
						if (format == GraphInputFormat.XML) {
							param.put("indexPath", indexPathStr);
							param.put("filePath", filePathStr);
						} else if(format == GraphInputFormat.LMS_TRIPLESTORE) {
							param.put("showUserNames", showUserNamesStr);
							param.put("involvedUserURIs", involvedUserURIsStr);
						} else {
							param.put("path", indexPathStr);
						}
					}
					//else if (format == GraphInputFormat.XGMML) {
						//param.put("key", keyStr);
						//param.put("type1", type1Str);
						//param.put("type2", type2Str);
						//param.put("type3", type3Str);
					//}
					graph = requestHandler.parseGraph(contentStr, format, param);

				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Input graph does not correspond to the specified format.");
				}
				boolean doMakeUndirected;
				try {
					doMakeUndirected = requestHandler.parseBoolean(doMakeUndirectedStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Do make undirected ist not a boolean value.");
				}
				graph.setUserName(username);
				graph.setName(URLDecoder.decode(nameStr, "UTF-8"));
				GraphCreationLog log = new GraphCreationLog(benchmarkType, new HashMap<String, String>());
				log.setStatus(ExecutionStatus.COMPLETED);
				graph.setCreationMethod(log);
				GraphProcessor processor = new GraphProcessor();
				processor.determineGraphTypes(graph);
				if (doMakeUndirected) {
					Set<GraphType> graphTypes = graph.getTypes();
					if (graphTypes.remove(GraphType.DIRECTED)) {
						processor.makeCompatible(graph, graphTypes);
					}
				}
				try {
					entityHandler.storeGraph(graph);
				} catch (Exception e) {
					return requestHandler.writeError(Error.INTERNAL, "Could not store graph");
				}
				return Response.ok(requestHandler.writeId(graph)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}

		/**
		 * Stores big graphs step by step.
		 * 
		 * @param nameStr
		 *            The name for the graph.
		 * @param contentStr
		 *            The graph input.
		 * @return XML containing information about the stored file.
		 */
		@POST
		@Path("storegraph")
		@Produces(MediaType.TEXT_XML)
		@Consumes(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"special"}, value = "Big Graph Import", notes = "Stores a graph step by step.")
		public Response storeGraph(@DefaultValue("unnamed") @QueryParam("name") String nameStr, String contentStr) {
			String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
			File graphDir = new File("tmp" + File.separator + username);
			if (!graphDir.exists()) {
				graphDir.mkdirs();
			}
			File graphFile = new File(graphDir + File.separator + nameStr + ".txt");
			try (FileWriter fileWriter = new FileWriter(graphFile, true);
					BufferedWriter bufferWritter = new BufferedWriter(fileWriter);) {
				if (!graphFile.exists()) {
					graphFile.createNewFile();
				}
				bufferWritter.write(contentStr);
				bufferWritter.newLine();
			} catch (Exception e) {
				requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
			return Response.ok("<?xml version=\"1.0\" encoding=\"UTF-16\"?>" + "<File>" + "<Name>" + graphFile.getName()
					+ "</Name>" + "<Size>" + graphFile.length() + "</Size>" + "<Message>" + "File appned" + "</Message>"
					+ "</File>").build();
		}

		/**
		 * Process the stored graph which was stored by storeGraph api.
		 * 
		 * @param nameStr
		 *            The name for the stored graph.
		 * @param creationTypeStr
		 *            The creation type the graph was created by.
		 * @param graphInputFormatStr
		 *            The name of the graph input format.
		 * @param doMakeUndirectedStr
		 *            Optional query parameter. Defines whether directed edges
		 *            shall be turned into undirected edges (TRUE) or not.
		 * @param startDateStr
		 *            Optional query parameter. For big graphs start date is the
		 *            date from which the file will start parse.
		 * @param endDateStr
		 *            Optional query parameter. For big graphs end date is the
		 *            date till which the file will parse.
		 * @param involvedUserURIsStr
		 * 			  Optional query parameter. Users to consider for LMS Triplestore import
		 * @param showUserNamesStr
		 * 			  Optional query parameter. Whether to show usernames as node names for LMS Triplestore import
		 * @param indexPathStr
		 *            Optional query parameter. Set index directory.
		 * @param filePathStr
		 *            Optional query parameter. For testing purpose, file
		 *            location of local file can be given.
		 * @return A graph id xml. Or an error xml.
		 */
		@POST
		@Path("processgraph")
		@Produces(MediaType.TEXT_XML)
		@Consumes(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"special"}, value = "Process Big Graph", notes = "Process the stored graph.")
		public Response processStoredGraph(@DefaultValue("unnamed") @QueryParam("name") String nameStr,
				@DefaultValue("UNDEFINED") @QueryParam("creationType") String creationTypeStr,
				@DefaultValue("GRAPH_ML") @QueryParam("inputFormat") String graphInputFormatStr,
				@DefaultValue("FALSE") @QueryParam("doMakeUndirected") String doMakeUndirectedStr,
				@DefaultValue("2004-01-01") @QueryParam("startDate") String startDateStr,
				@DefaultValue("2004-01-20") @QueryParam("endDate") String endDateStr,
				@DefaultValue("") @QueryParam("involvedUserURIs") String involvedUserURIsStr,
				@DefaultValue("false") @QueryParam("showUserNames") String showUserNamesStr,
				@DefaultValue("indexes") @QueryParam("indexPath") String indexPathStr,
				@DefaultValue("ocd/test/input/stackexAcademia.xml") @QueryParam("filePath") String filePathStr) {
			String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
			File graphDir = new File("tmp" + File.separator + username);
			File graphFile = new File(graphDir + File.separator + nameStr + ".txt");
			StringBuffer contentStr = new StringBuffer();
			if (!graphFile.exists()) {
				return requestHandler.writeError(Error.INTERNAL, "Graph Does not exists.");
			}
			try (FileReader fileWriter = new FileReader(graphFile);
					BufferedReader bufferedReader = new BufferedReader(fileWriter);) {
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					contentStr.append(line);
					contentStr.append("\n");
				}
			} catch (Exception e) {
				requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
			graphFile.delete();
			return createGraph(nameStr, creationTypeStr, graphInputFormatStr, doMakeUndirectedStr, startDateStr,
					endDateStr,involvedUserURIsStr, showUserNamesStr, indexPathStr, filePathStr, contentStr.toString());
		}

		/**
		 * Returns the ids (or meta information) of multiple graphs.
		 * 
		 * @param firstIndexStr
		 *            Optional query parameter. The result list index of the
		 *            first id to return. Defaults to 0.
		 * @param lengthStr
		 *            Optional query parameter. The number of ids to return.
		 *            Defaults to Long.MAX_VALUE.
		 * @param includeMetaStr
		 *            Optional query parameter. If TRUE, instead of the ids the
		 *            META XML of each graph is returned. Defaults to FALSE.
		 * @param executionStatusesStr
		 *            Optional query parameter. If set only those graphs are
		 *            returned whose creation method has one of the given
		 *            ExecutionStatus names. Multiple status names are separated
		 *            using the "-" delimiter.
		 * @return The graphs. Or an error xml.
		 */

		@GET
		@Path("graphs")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"show"}, value = "Get Graphs Info", notes = "Returns the ids or meta information of multiple graphs.")
		public Response getGraphs(@DefaultValue("0") @QueryParam("firstIndex") String firstIndexStr,
				@DefaultValue("0") @QueryParam("length") String lengthStr,
				@DefaultValue("FALSE") @QueryParam("includeMeta") String includeMetaStr,
				@DefaultValue("") @QueryParam("executionStatuses") String executionStatusesStr) {
			try {
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				List<CustomGraph> queryResults;
				List<Integer> executionStatusIds = new ArrayList<Integer>();
				if (!executionStatusesStr.equals("")) {
					try {
						List<String> executionStatusesStrList = requestHandler
								.parseQueryMultiParam(executionStatusesStr);
						for (String executionStatusStr : executionStatusesStrList) {
							ExecutionStatus executionStatus = ExecutionStatus.valueOf(executionStatusStr);
							executionStatusIds.add(executionStatus.getId());
						}
					} catch (Exception e) {
						requestHandler.log(Level.WARNING, "user: " + username, e);
						return requestHandler.writeError(Error.PARAMETER_INVALID,
								"Specified execution status does not exist.");
					}
				} else {
					for (ExecutionStatus executionStatus : ExecutionStatus.values()) {
						executionStatusIds.add(executionStatus.getId());
					}
				}

				boolean includeMeta;
				try {
					includeMeta = requestHandler.parseBoolean(includeMetaStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "", e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Include meta is not a boolean value.");
				}

				int firstIndex = 0;
				try {
					firstIndex = Integer.parseInt(firstIndexStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "First index is not valid.");
				}

				int length = 0;
				try {
					if (!lengthStr.equals("")) {
						length = Integer.parseInt(lengthStr);
					}
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Length is not valid.");
				}
				queryResults = entityHandler.getGraphs(username, firstIndex, length, executionStatusIds);

				String responseStr;
				if (includeMeta) {
					responseStr = requestHandler.writeGraphMetas(queryResults);
				} else {
					responseStr = requestHandler.writeGraphIds(queryResults);
				}
				return Response.ok(responseStr).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}

		/**
		 * Returns a graph in a specified output format.
		 * 
		 * @param graphIdStr
		 *            The graph id.
		 * @param graphOutputFormatStr
		 *            The name of the graph output format.
		 * @return The graph output. Or an error xml.
		 */
		@GET
		@Produces(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@Path("graphs/{graphId}")
		@ApiOperation(tags = {"export"}, value = "Export Graph", notes = "Returns a graph in a specified output format.")
		public Response getGraph(@DefaultValue("GRAPH_ML") @QueryParam("outputFormat") String graphOutputFormatStr,
				@PathParam("graphId") String graphIdStr) {
			try {
				long graphId;
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				GraphOutputFormat format;
				try {
					graphId = Long.parseLong(graphIdStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
				}
				try {
					format = GraphOutputFormat.valueOf(graphOutputFormatStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified graph output format does not exist.");
				}

				CustomGraph graph = entityHandler.getGraph(username, graphId);
				if (graph == null)
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Graph does not exist: graph id " + graphId);

				return Response.ok(requestHandler.writeGraph(graph, format)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}

		}

		/**
		 * Deletes a graph. All covers based on the graph are removed as well.
		 * If a benchmark is currently calculating the graph the execution is
		 * terminated. If an algorithm is currently calculating a cover based on
		 * the graph it is terminated. If a metric is currently running on a
		 * cover based on the grap it is terminated.
		 * 
		 * @param graphIdStr
		 *            The graph id.
		 * @return A confirmation xml. Or an error xml.
		 */
		@DELETE
		@Path("graphs/{graphId}")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"delete"}, value = "Delete Graph", notes = "Deletes a graph.")
		public Response deleteGraph(@PathParam("graphId") String graphIdStr) {
			try {
				long graphId;
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				try {
					graphId = Long.parseLong(graphIdStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
				}

				try {
					entityHandler.deleteGraph(username, graphId, threadHandler);

				} catch (Exception e) {
					if(e.getMessage() != null) {
						requestHandler.writeError(Error.INTERNAL, "Graph could not be deleted: " + e.getMessage());
					}
					requestHandler.writeError(Error.INTERNAL, "Graph not found");
				}

				return Response.ok(requestHandler.writeConfirmationXml()).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}

		//////////////////////////////////////////////////////////////////////////
		//////////// COVERS
		//////////////////////////////////////////////////////////////////////////

		/**
		 * Imports a cover for an existing graph.
		 * 
		 * @param graphIdStr
		 *            The id of the graph that the cover is based on.
		 * @param nameStr
		 *            A name for the cover.
		 * @param creationTypeStr
		 *            The creation type the cover was created by.
		 * @param coverInputFormatStr
		 *            The name of the input format.
		 * @param contentStr
		 *            The cover input.
		 * @return A cover id xml. Or an error xml.
		 */
		@POST
		@Path("covers/graphs/{graphId}")
		@Produces(MediaType.TEXT_XML)
		@Consumes(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"import"}, value = "Import Cover", notes = "Imports a cover for an existing graph.")
		public Response createCover(@PathParam("graphId") String graphIdStr,
				@DefaultValue("unnamed") @QueryParam("name") String nameStr,
				@DefaultValue("UNDEFINED") @QueryParam("creationType") String creationTypeStr,
				@DefaultValue("LABELED_MEMBERSHIP_MATRIX") @QueryParam("inputFormat") String coverInputFormatStr,
				String contentStr) {
			try {
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				long graphId;
				try {
					graphId = Long.parseLong(graphIdStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
				}
				CoverInputFormat format;
				try {
					format = CoverInputFormat.valueOf(coverInputFormatStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified cover input format does not exist.");
				}
				CoverCreationType algorithmType;
				try {
					algorithmType = CoverCreationType.valueOf(creationTypeStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified algorithm does not exist.");
				}
				Set<GraphType> graphTypes;
				if (algorithmFactory.isInstantiatable(algorithmType)) {
					OcdAlgorithm algorithm;
					algorithm = algorithmFactory.getInstance(algorithmType, new HashMap<String, String>());
					graphTypes = algorithm.compatibleGraphTypes();
				} else {
					graphTypes = new HashSet<GraphType>();
				}
				CoverCreationLog log = new CoverCreationLog(algorithmType, new HashMap<String, String>(), graphTypes);
				log.setStatus(ExecutionStatus.COMPLETED);
				EntityManager em = entityHandler.getEntityManager();
				EntityTransaction tx = em.getTransaction();
				Cover cover;
				try {
					CustomGraph graph;
					try {
						graph = entityHandler.getGraph(username, graphId);
					} catch (Exception e) {
						requestHandler.log(Level.WARNING,
								"user: " + username + ", " + "Graph does not exist: graph id " + graphId);
						return requestHandler.writeError(Error.PARAMETER_INVALID,
								"Graph does not exist: graph id " + graphId);
					}
					try {
						cover = requestHandler.parseCover(contentStr, graph, format);
					} catch (Exception e) {
						requestHandler.log(Level.WARNING, "user: " + username, e);
						return requestHandler.writeError(Error.PARAMETER_INVALID,
								"Input cover does not correspond to the specified format.");
					}
					cover.setCreationMethod(log);
					cover.setName(URLDecoder.decode(nameStr, "UTF-8"));
					tx.begin();
					em.persist(cover);
					tx.commit();
				} catch (RuntimeException e) {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
					throw e;
				}
				em.close();
				return Response.ok(requestHandler.writeId(cover)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
		
		/**
		 * Returns the ids (or meta information) of multiple covers.
		 * 
		 * @param firstIndexStr
		 *            Optional query parameter. The result list index of the
		 *            first id to return. Defaults to 0.
		 * @param lengthStr
		 *            Optional query parameter. The number of ids to return.
		 *            Defaults to Long.MAX_VALUE.
		 * @param includeMetaStr
		 *            Optional query parameter. If TRUE, instead of the ids the
		 *            META XML of each graph is returned. Defaults to FALSE.
		 * @param executionStatusesStr
		 *            Optional query parameter. If set only those covers are
		 *            returned whose creation method status corresponds to one
		 *            of the given ExecutionStatus names. Multiple status names
		 *            are separated using the "-" delimiter.
		 * @param metricExecutionStatusesStr
		 *            Optional query parameter. If set only those covers are
		 *            returned that have a corresponding metric log with a
		 *            status corresponding to one of the given ExecutionStatus
		 *            names. Multiple status names are separated using the "-"
		 *            delimiter.
		 * @param graphIdStr
		 *            Optional query parameter. If set only those covers are
		 *            returned that are based on the corresponding graph.
		 * @return The covers. Or an error xml.
		 */
		@GET
		@Path("covers")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { 
				@ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") 
		})
		@ApiOperation(tags = {"show"}, value = "Get Covers Info",
			notes = "Returns the ids (or meta information) of multiple covers.")
		public Response getCovers(
				@DefaultValue("0") @QueryParam("firstIndex") String firstIndexStr,
				@DefaultValue("") @QueryParam("length") String lengthStr,
				@DefaultValue("FALSE") @QueryParam("includeMeta") String includeMetaStr,
				@DefaultValue("") @QueryParam("executionStatuses") String executionStatusesStr,
				@DefaultValue("") @QueryParam("metricExecutionStatuses") String metricExecutionStatusesStr,
				@DefaultValue("") @QueryParam("graphId") String graphIdStr) 
		{
			try {
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				long graphId = 0;
				if (!graphIdStr.equals("")) {
					try {
						graphId = Long.parseLong(graphIdStr);
					} catch (Exception e) {
						requestHandler.log(Level.WARNING, "user: " + username, e);
						return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
					}
				}
				List<Integer> executionStatusIds = new ArrayList<Integer>();
				if (!executionStatusesStr.equals("")) {
					try {
						List<String> executionStatusesStrList = requestHandler
								.parseQueryMultiParam(executionStatusesStr);
						for (String executionStatusStr : executionStatusesStrList) {
							ExecutionStatus executionStatus = ExecutionStatus.valueOf(executionStatusStr);
							executionStatusIds.add(executionStatus.getId());
						}
					} catch (Exception e) {
						requestHandler.log(Level.WARNING, "user: " + username, e);
						return requestHandler.writeError(Error.PARAMETER_INVALID,
								"Specified execution status does not exist.");
					}
				} else {
					for (ExecutionStatus executionStatus : ExecutionStatus.values()) {
						executionStatusIds.add(executionStatus.getId());
					}
				}
				List<Integer> metricExecutionStatusIds = new ArrayList<Integer>();
				if (!metricExecutionStatusesStr.equals("")) {
					try {
						List<String> metricExecutionStatusesStrList = requestHandler
								.parseQueryMultiParam(metricExecutionStatusesStr);
						for (String executionStatusStr : metricExecutionStatusesStrList) {
							ExecutionStatus executionStatus = ExecutionStatus.valueOf(executionStatusStr);
							metricExecutionStatusIds.add(executionStatus.getId());
						}
					} catch (Exception e) {
						requestHandler.log(Level.WARNING, "user: " + username, e);
						return requestHandler.writeError(Error.PARAMETER_INVALID,
								"Specified metric execution status does not exist.");
					}
				}
				List<Cover> queryResults;
				EntityManager em = entityHandler.getEntityManager();
				/*
				 * Query
				 */
				String queryStr = "SELECT c from Cover c" + " JOIN c." + Cover.GRAPH_FIELD_NAME + " g" + " JOIN c."
						+ Cover.CREATION_METHOD_FIELD_NAME + " a";
				if (!metricExecutionStatusesStr.equals("")) {
					queryStr += " JOIN c." + Cover.METRICS_FIELD_NAME + " m";
				}
				queryStr += " WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username" + " AND a."
						+ CoverCreationLog.STATUS_ID_FIELD_NAME + " IN :execStatusIds";
				if (!metricExecutionStatusesStr.equals("")) {
					queryStr += " AND m." + OcdMetricLog.STATUS_ID_FIELD_NAME + " IN :metricExecStatusIds";
				}
				if (!graphIdStr.equals("")) {
					queryStr += " AND g." + CustomGraph.ID_FIELD_NAME + " = " + graphId;
				}
				/*
				 * Gets each cover only once.
				 */
				queryStr += " GROUP BY c";
				TypedQuery<Cover> query = em.createQuery(queryStr, Cover.class);
				try {
					int firstIndex = Integer.parseInt(firstIndexStr);
					query.setFirstResult(firstIndex);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "First index is not valid.");
				}
				try {
					if (!lengthStr.equals("")) {
						int length = Integer.parseInt(lengthStr);
						query.setMaxResults(length);
					}
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Length is not valid.");
				}
				boolean includeMeta;
				try {
					includeMeta = requestHandler.parseBoolean(includeMetaStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "", e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Include meta is not a boolean value.");
				}
				query.setParameter("username", username);
				query.setParameter("execStatusIds", executionStatusIds);
				if (!metricExecutionStatusesStr.equals("")) {
					query.setParameter("metricExecStatusIds", metricExecutionStatusIds);
				}
				queryResults = query.getResultList();
				em.close();
				String responseStr;
				if (includeMeta) {
					responseStr = requestHandler.writeCoverMetas(queryResults);
				} else {
					responseStr = requestHandler.writeCoverIds(queryResults);
				}
				return Response.ok(responseStr).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
		
		/**
		 * Returns a cover in a specified format.
		 * 
		 * @param graphIdStr
		 *            The id of the graph that the cover is based on.
		 * @param coverIdStr
		 *            The cover id.
		 * @param coverOutputFormatStr
		 *            The cover output format.
		 * @return The cover output. Or an error xml.
		 */
		@GET
		@Produces(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@Path("covers/{coverId}/graphs/{graphId}")
		@ApiOperation(tags = {"export"}, value = "Export Cover", notes = "Returns a cover in a specified format.")
		public Response getCover(@PathParam("graphId") String graphIdStr, @PathParam("coverId") String coverIdStr,
				@DefaultValue("LABELED_MEMBERSHIP_MATRIX") @QueryParam("outputFormat") String coverOutputFormatStr) {
			try {
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				long graphId;
				try {
					graphId = Long.parseLong(graphIdStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
				}
				long coverId;
				try {
					coverId = Long.parseLong(coverIdStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover id is not valid.");
				}
				CoverOutputFormat format;
				try {
					format = CoverOutputFormat.valueOf(coverOutputFormatStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified cover output format does not exist.");
				}

				Cover cover = null;
				try {
					cover = entityHandler.getCover(username, graphId, coverId);
					
					// Paint cover if not yet done when requested type is default XML
					if(format == CoverOutputFormat.DEFAULT_XML && !cover.isPainted()) { 
						CoverPainter painter = (new CoverPainterFactory()).getInstance(CoverPaintingType.PREDEFINED_COLORS);
						painter.doPaint(cover);					
		    		}
					
				} catch (Exception e) {

					requestHandler.log(Level.WARNING, "user: " + username + ", " + "Cover does not exist: cover id "
							+ coverId + ", graph id " + graphId);
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Cover does not exist: cover id " + coverId + ", graph id " + graphId);
				}
				return Response.ok(requestHandler.writeCover(cover, format)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
		
		/**
		 * Deletes a cover. If the cover is still being created by an algorithm,
		 * the algorithm is terminated. If the cover is still being created by a
		 * ground truth benchmark, the benchmark is terminated and the
		 * corresponding graph is deleted as well. If metrics are running on the
		 * cover, they are terminated.
		 * 
		 * @param coverIdStr
		 *            The cover id.
		 * @param graphIdStr
		 *            The graph id of the graph corresponding the cover.
		 * @return A confirmation xml. Or an error xml.
		 */
		@DELETE
		@Path("covers/{coverId}/graphs/{graphId}")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"delete"}, value = "Delete Cover", notes = "Deletes a cover.")
		public Response deleteCover(@PathParam("coverId") String coverIdStr, @PathParam("graphId") String graphIdStr) {
			try {
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				long graphId;
				try {
					graphId = Long.parseLong(graphIdStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
				}
				long coverId;
				try {
					coverId = Long.parseLong(coverIdStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover id is not valid.");
				}

				try {
					entityHandler.deleteCover(username, graphId, coverId, threadHandler);
					return Response.ok(requestHandler.writeConfirmationXml()).build();
				} catch (IllegalArgumentException e) {
					return requestHandler.writeError(Error.PARAMETER_INVALID, e.getMessage());
				} catch (Exception e) {
					requestHandler.log(Level.SEVERE, "", e);
					return requestHandler.writeError(Error.INTERNAL, "Cover not deleted");
				}

			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
		
		//////////////////////////////////////////////////////////////////////////
		//////////// ALGORITHMS
		//////////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new cover by running an algorithm on an existing graph.
		 * 
		 * @param graphIdStr
		 *            The id of the graph to run the algorithm on, must have the
		 *            creation method status completed.
		 * @param nameStr
		 *            The name for the cover.
		 * @param creationTypeStr
		 *            The name of a cover creation type corresponding to an ocd
		 *            algorithm. Defines the algorithm to execute.
		 * @param content
		 *            A parameter xml defining any non-default parameters passed
		 *            to the algorithm.
		 * @param componentNodeCountFilterStr
		 *            Option query parameter. The component node count filter
		 *            applied by the OcdAlgorithmExecutor.
		 * @param contentWeighting
		 *            The boolean value to enable content-based weighting
		 * @return The id of the cover being calculated which is reserved for
		 *         the algorithm result. Or an error xml.
		 */
		@POST
		@Path("covers/graphs/{graphId}/algorithms")
		@Produces(MediaType.TEXT_XML)
		@Consumes(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"execution"}, value = "Run OCD Algorithm", notes = "Creates a new cover by running an algorithm on an existing graph.  \n " +
				"The provided data represents the algorithm parameters and needs to be an XML with the root \"Parameters\" enclosing \"Parameter\" nodes which have both \"Name\" and \"Value\" nodes." +
				"\n To see which parameters of what type are needed for an algorithm, fetch its default parameters")
		public Response runAlgorithm(@PathParam("graphId") String graphIdStr,
				@DefaultValue("unnamed") @QueryParam("name") String nameStr,
				@DefaultValue("SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM") @QueryParam("algorithm") String creationTypeStr,
				String content, @DefaultValue("false") @QueryParam("contentWeighting") String contentWeighting,
				@DefaultValue("0") @QueryParam("componentNodeCountFilter") String componentNodeCountFilterStr) {
			try {
				int componentNodeCountFilter;
				long graphId;
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				CoverCreationType algorithmType;
				try {
					graphId = Long.parseLong(graphIdStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
				}
				try {
					componentNodeCountFilter = Integer.parseInt(componentNodeCountFilterStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Component node count filter is not valid.");
				}
				try {
					algorithmType = CoverCreationType.valueOf(creationTypeStr);
					if (algorithmType == CoverCreationType.UNDEFINED
							|| algorithmType == CoverCreationType.GROUND_TRUTH) {
						requestHandler.log(Level.WARNING, "user: " + username + ", "
								+ "Specified algorithm type is not valid for this request: " + algorithmType.name());
						return requestHandler.writeError(Error.PARAMETER_INVALID,
								"Specified algorithm type is not valid for this request: " + algorithmType.name());
					}
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified algorithm does not exist.");
				}
				OcdAlgorithm algorithm;
				Map<String, String> parameters;
				try {
					parameters = requestHandler.parseParameters(content);
					algorithm = algorithmFactory.getInstance(algorithmType, parameters);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Parameters are not valid.");
				}
				Cover cover;
				EntityManager em = entityHandler.getEntityManager();
				CustomGraphId id = new CustomGraphId(graphId, username);
				CoverCreationLog log;
				synchronized (threadHandler) {
					EntityTransaction tx = em.getTransaction();
					CustomGraph graph;
					try {
						tx.begin();
						graph = em.find(CustomGraph.class, id);
						if (graph == null) {
							requestHandler.log(Level.WARNING,
									"user: " + username + ", " + "Graph does not exist: graph id " + graphId);
							return requestHandler.writeError(Error.PARAMETER_INVALID,
									"Graph does not exist: graph id " + graphId);
						}
						if (graph.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
							requestHandler.log(Level.WARNING,
									"user: " + username + ", "
											+ "Invalid graph creation method status for metric execution: "
											+ graph.getCreationMethod().getStatus().name());
							return requestHandler.writeError(Error.PARAMETER_INVALID,
									"Invalid graph creation method status for metric execution: "
											+ graph.getCreationMethod().getStatus().name());
						}
						boolean weight = Boolean.parseBoolean(contentWeighting);
						if(!graph.isOfType(GraphType.CONTENT_LINKED) && !graph.isOfType(GraphType.CONTENT_UNLINKED) && (weight || (algorithm
								.getAlgorithmType() == CoverCreationType.COST_FUNC_OPT_CLUSTERING_ALGORITHM
								|| algorithm.getAlgorithmType() == CoverCreationType.WORD_CLUSTERING_REF_ALGORITHM))) {
							requestHandler.log(Level.WARNING,
									"user: " + username + ", "
											+ "Content weighted algorithm chosen for non-content graph: "
											+ algorithm.getAlgorithmType().toString() + " " + graph.getTypes() + " " + graph.getPath());
							return requestHandler.writeError(Error.PARAMETER_INVALID,
									"Content weighted algorithm chosen for non-content graph");
						}
						if (weight && (algorithm
								.getAlgorithmType() == CoverCreationType.COST_FUNC_OPT_CLUSTERING_ALGORITHM
								|| algorithm.getAlgorithmType() == CoverCreationType.WORD_CLUSTERING_REF_ALGORITHM)) {
							requestHandler.log(Level.WARNING,
									"user: " + username + ", "
											+ "Invalid algorithm in combination of weighting requested: "
											+ algorithm.getAlgorithmType().toString());
							return requestHandler.writeError(Error.PARAMETER_INVALID,
									"Invalid algorithm in combination of weighting requested");
						}
						if (weight) {
							ContentBasedWeightingAlgorithm weightAlgo = new ContentBasedWeightingAlgorithm();
							graph = weightAlgo.detectOverlappingCommunities(graph, new ExecutionTime());
						}
						cover = new Cover(graph, new CCSMatrix(graph.nodeCount(), 0));
						log = new CoverCreationLog(algorithmType, parameters, algorithm.compatibleGraphTypes());
						cover.setCreationMethod(log);
						cover.setName(URLDecoder.decode(nameStr, "UTF-8"));
						em.persist(cover);
						tx.commit();
					} catch (RuntimeException e) {
						if (tx != null && tx.isActive()) {
							tx.rollback();
						}
						throw e;
					}
					em.close();
					/*
					 * Registers and starts algorithm
					 */
					threadHandler.runAlgorithm(cover, algorithm, componentNodeCountFilter);
				}
				return Response.ok(requestHandler.writeId(cover)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
 
		//////////////////////////////////////////////////////////////////////////
		//////////// CENTRALITY MEASURES
		//////////////////////////////////////////////////////////////////////////
		
		/**
		 * Imports a CentralityMap for an existing graph.
		 * 
		 * @param graphIdStr
		 *            The id of the graph that the cover is based on.
		 * @param nameStr
		 *            A name for the CentralityMap.
		 * @param creationTypeStr
		 *            Specifies the way the centrality was created.
		 * @param centralityInputFormatStr
		 *            The input format of the CentralityMap.
		 * @param contentStr
		 *            The CentralityMap input.
		 * @return A centrality map id xml. Or an error xml.
		 */
		@POST
		@Path("centrality/graphs/{graphId}")
		@Produces(MediaType.TEXT_XML)
		@Consumes(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"import"}, value = "Import Centrality", notes = "Imports a centrality map for an existing graph.")
		public Response importCentralityMap(@PathParam("graphId") String graphIdStr,
				@DefaultValue("unnamed") @QueryParam("name") String nameStr,
				@DefaultValue("UNDEFINED") @QueryParam("creationType") String creationTypeStr,
				@DefaultValue("NODE_VALUE_LIST") @QueryParam("inputFormat") String centralityInputFormatStr,
				String contentStr) {
			try {
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				long graphId;
				try {
					graphId = Long.parseLong(graphIdStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
				}
				CentralityInputFormat format;
				try {
					format = CentralityInputFormat.valueOf(centralityInputFormatStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified centrality input format does not exist.");
				}
				CentralityCreationType creationType;
				try {
					creationType = CentralityCreationType.valueOf(creationTypeStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified creation type does not exist.");
				}
				CentralityCreationLog log = new CentralityCreationLog(null, creationType, null, null);
				log.setStatus(ExecutionStatus.COMPLETED);
				EntityManager em = entityHandler.getEntityManager();
				EntityTransaction tx = em.getTransaction();
				CentralityMap map;
				try {
					CustomGraph graph;
					try {
						graph = entityHandler.getGraph(username, graphId);
					} catch (Exception e) {
						requestHandler.log(Level.WARNING,
								"user: " + username + ", " + "Graph does not exist: graph id " + graphId);
						return requestHandler.writeError(Error.PARAMETER_INVALID,
								"Graph does not exist: graph id " + graphId);
					}
					try {
						map = requestHandler.parseCentralityMap(contentStr, graph, format);
					} catch (Exception e) {
						requestHandler.log(Level.WARNING, "user: " + username, e);
						return requestHandler.writeError(Error.PARAMETER_INVALID,
								"Input centrality data does not correspond to the specified format.");
					}
					map.setCreationMethod(log);
					map.setName(nameStr);
					tx.begin();
					em.persist(map);
					tx.commit();
				} catch (RuntimeException e) {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
					throw e;
				}
				em.close();
				return Response.ok(requestHandler.writeId(map)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
	    
	    /**
	     * Returns the ids (or meta information) of specific centrality maps.
	     * 
	     * @param firstIndexStr 
	     *            Optional query parameter. The result list index of the first 
	     *            id to return. Defaults to 0.
	     * @param lengthStr 
	     *            Optional query parameter. The number of ids to return. 
	     *            Defaults to Long.MAX_VALUE.
	     * @param includeMetaStr 
	     *            Optional query parameter. If TRUE, instead of the ids the 
	     *            META XML of each graph is returned. Defaults to FALSE.
	     * @param executionStatusesStr 
	     *            Optional query parameter. If set only those centrality maps 
	     *            are returned whose creation method status corresponds to 
	     *            one of the given ExecutionStatus names.
	     *            Multiple status names are separated using the "-" delimiter.
	     * @param graphIdStr 
	     *            Optional query parameter. If set only those centrality maps 
	     *            are returned that are based on the corresponding graph.
	     * @return The centrality maps. Or an error xml.
	     */
	    @GET
	    @Path("maps")
	    @Produces(MediaType.TEXT_XML)
	    @ApiResponses(value = {
	    		@ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized")
	    })
		@ApiOperation(tags = {"show"}, value = "Get Centralities Info",
			notes = "Returns the ids (or meta information) of multiple centrality maps.")
	    public Response getCentralityMaps(
	    		@DefaultValue("0") @QueryParam("firstIndex") String firstIndexStr,
	    		@DefaultValue("") @QueryParam("length") String lengthStr,
	    		@DefaultValue("FALSE") @QueryParam("includeMeta") String includeMetaStr,
	    		@DefaultValue("") @QueryParam("executionStatuses") String executionStatusesStr,
	    		@DefaultValue("") @QueryParam("graphId") String graphIdStr)
	    {
	    	try {
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				long graphId = 0;
				if(!graphIdStr.equals("")) {
		    		try {
		    			graphId = Long.parseLong(graphIdStr);
		    		}
		    		catch (Exception e) {
		    			requestHandler.log(Level.WARNING, "user: " + username, e);
						return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
		    		}
				}
				List<Integer> executionStatusIds = new ArrayList<Integer>();
				if(!executionStatusesStr.equals("")) {
		    		try {
		    			List<String> executionStatusesStrList = requestHandler.parseQueryMultiParam(executionStatusesStr);
		    			for(String executionStatusStr : executionStatusesStrList) {
	    					ExecutionStatus executionStatus = ExecutionStatus.valueOf(executionStatusStr);
	    					executionStatusIds.add(executionStatus.getId());
		    			}
		    		}
			    	catch (Exception e) {
			    		requestHandler.log(Level.WARNING, "user: " + username, e);
						return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified execution status does not exist.");
			    	}
				}
				else {
					for(ExecutionStatus executionStatus : ExecutionStatus.values()) {
						executionStatusIds.add(executionStatus.getId());
					}
				}
				List<CentralityMap> queryResults;
				EntityManager em = entityHandler.getEntityManager();
				/*
				 * Query
				 */
				String queryStr = "SELECT c from CentralityMap c"
						+ " JOIN c." + CentralityMap.GRAPH_FIELD_NAME + " g"
						+ " JOIN c." + CentralityMap.CREATION_METHOD_FIELD_NAME + " a";
				queryStr += " WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username"
						+ " AND a." + CentralityCreationLog.STATUS_ID_FIELD_NAME + " IN :execStatusIds";
				if(!graphIdStr.equals("")) {
					queryStr += " AND g." + CustomGraph.ID_FIELD_NAME + " = " + graphId;
				}
				/*
				 * Gets each CentralityMap only once.
				 */
				queryStr += " GROUP BY c";
				TypedQuery<CentralityMap> query = em.createQuery(queryStr, CentralityMap.class);
				try {
					int firstIndex = Integer.parseInt(firstIndexStr);
					query.setFirstResult(firstIndex);
				}
				catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "First index is not valid.");
				}
				try {
					if(!lengthStr.equals("")) {
						int length = Integer.parseInt(lengthStr);
						query.setMaxResults(length);
					}
				}
				catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Length is not valid.");
				}
				boolean includeMeta;
				try {
					includeMeta = requestHandler.parseBoolean(includeMetaStr);
		    	}
				catch (Exception e) {
		    		requestHandler.log(Level.WARNING, "", e);
		    		return requestHandler.writeError(Error.PARAMETER_INVALID, "Include meta is not a boolean value.");
		    	}
				query.setParameter("username", username);
				query.setParameter("execStatusIds", executionStatusIds);
				queryResults = query.getResultList();
				em.close();
				String responseStr;
				if(includeMeta) {
					responseStr = requestHandler.writeCentralityMapMetas(queryResults);
				}
				else {
					responseStr = requestHandler.writeCentralityMapIds(queryResults);
				}
				return Response.ok(responseStr).build();
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	    
	    /**
	     * Creates a new CentralityMap by running a CentralityAlgorithm on an 
	     * existing graph.
	     * 
	     * @param graphIdStr 
	     *            The id of the graph to run the algorithm on, must have the 
	     *            creation method status completed.
	     * @param centralityMeasureTypeStr 
	     *            The name of a CentralityMeasureType corresponding to a 
	     *            CentralityAlgorithm. Defines the CentralityAlgorithm to 
	     *            execute.
	     * @param content 
	     *            String containing the algorithm parameters.
	     * @return The id of the CentralityMap being calculated which is reserved 
	     * for the algorithm result. Or an error xml.
	     */
	    @POST
	    @Path("centrality/graphs/{graphId}/algorithms")
	    @Produces(MediaType.TEXT_XML)
	    @Consumes(MediaType.TEXT_PLAIN)
	    @ApiResponses(value = {
	    		@ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized")
	    })
		@ApiOperation(tags = {"execution"}, value = "Run Centrality",
			notes = "Creates a new centrality map by running a centrality algorithm on an existing graph. \n " +
					"The provided data represents the algorithm parameters and needs to be an XML with the root \"Parameters\" enclosing \"Parameter\" nodes which have both \"Name\" and \"Value\" nodes." +
					"\n To see which parameters of what type are needed for an algorithm, fetch its default parameters")
	    public Response calculateCentrality(
	    		@PathParam("graphId") String graphIdStr,
	    		@DefaultValue("Degree Centrality") @QueryParam("algorithm") String centralityMeasureTypeStr, String content)
	    {
	    	try {
	    		long graphId;
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	    		CentralityMeasureType centralityMeasureType;
	    		try {
	    			graphId = Long.parseLong(graphIdStr);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
	    		try {
	    			centralityMeasureType = CentralityMeasureType.valueOf(centralityMeasureTypeStr);
	    			if(centralityMeasureType == CentralityMeasureType.UNDEFINED) {
	    				requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified centrality measure type is not valid for this request: " + centralityMeasureType.getDisplayName());
	    				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified centrality measure type is not valid for this request: " + centralityMeasureType.getDisplayName());
	    			}
	    		}
		    	catch (Exception e) {
		    		requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified centrality measure does not exist.");
		    	}
	    		CentralityAlgorithm algorithm;
	    		Map<String, String> parameters;
	    		Map<String, String> parametersCopy = new HashMap<String, String>();
	    		try {
	    			parameters = requestHandler.parseParameters(content);
	    			for(String parameter : parameters.keySet()) {
	    				parametersCopy.put(parameter, parameters.get(parameter));
	    			}
	    			algorithm = centralityAlgorithmFactory.getInstance(centralityMeasureType, parameters);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Parameters are not valid.");
	    		}
	    		CentralityMap map;
		    	EntityManager em = entityHandler.getEntityManager();
		    	CustomGraphId id = new CustomGraphId(graphId, username);
		    	synchronized(threadHandler) {
		    		EntityTransaction tx = em.getTransaction();
			    	CustomGraph graph;
			    	CentralityCreationLog log;
			    	try {
			    		tx.begin();
						graph = em.find(CustomGraph.class, id);
				    	if(graph == null) {
				    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Graph does not exist: graph id " + graphId);
							return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph does not exist: graph id " + graphId);
				    	}
				    	if(graph.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
				    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Invalid graph creation method status for centrality algorithm execution: " + graph.getCreationMethod().getStatus().name());
							return requestHandler.writeError(Error.PARAMETER_INVALID, "Invalid graph creation method status for centrality algorithm execution: " + graph.getCreationMethod().getStatus().name());
				    	}
				    	// Some centrality measures cannot be computed or do not give meaningful results on unconnected graphs
				    	if(algorithm.getCentralityMeasureType() == CentralityMeasureType.CURRENT_FLOW_BETWEENNESS || algorithm.getCentralityMeasureType() == CentralityMeasureType.CURRENT_FLOW_CLOSENESS || algorithm.getCentralityMeasureType() == CentralityMeasureType.ECCENTRICITY || algorithm.getCentralityMeasureType() == CentralityMeasureType.CLOSENESS_CENTRALITY) {
							if(!GraphChecker.isConnected((Graph)graph)) {
								return Response.serverError().entity("Show Error: This centrality measure can only be used on a connected network.").build();
							}
						}
				    	map = new CentralityMap(graph);
				    	map.setName(centralityMeasureType.getDisplayName());
				    	log = new CentralityCreationLog(centralityMeasureType, CentralityCreationType.CENTRALITY_MEASURE, parametersCopy, algorithm.compatibleGraphTypes());
				    	map.setCreationMethod(log);
				    	em.persist(map);
						tx.commit();
			    	}
			    	catch( RuntimeException e ) {
						if( tx != null && tx.isActive() ) {
							tx.rollback();
						}
						throw e;
					}
					em.close();

			    	/*
			    	 * Registers and starts algorithm
			    	 */	
					threadHandler.runCentralityAlgorithm(map, algorithm);
		    	}
		    	return Response.ok(requestHandler.writeId(map)).build();
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	    
	    /**
	     * Returns a CentralityMap in a specified format.
	     * 
	     * @param graphIdStr 
	     *            The id of the graph that the CentralityMap is based on.
	     * @param mapIdStr 
	     *            The CentralityMap id.
	     * @param centralityOutputFormatStr 
	     *            The CentralityMap output format.
	     * @param onlyTopNodesStr
	     *            Specifies if only the top nodes should be returned.
	     * @param topNodesNumberStr
	     *            Specifies the number of top nodes that are returned.
	     * @return The CentralityMap output. Or an error xml.
	     */
	    @GET
	    @Produces(MediaType.TEXT_PLAIN)
	    @ApiResponses(value = {
	    		@ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized")
	    })
	    @Path("centrality/{mapId}/graphs/{graphId}")
		@ApiOperation(tags = {"export"}, value = "Export Centrality",
			notes = "Returns a centrality map in a specified format.")
	    public Response getCentralityMap(
	    		@PathParam("graphId") String graphIdStr,
	    		@PathParam("mapId") String mapIdStr,
	    		@DefaultValue("DEFAULT_XML") @QueryParam("outputFormat") String centralityOutputFormatStr,
	    		@DefaultValue("FALSE") @QueryParam("onlyTopNodes") String onlyTopNodesStr,
	    		@DefaultValue("0") @QueryParam("topNodesNumber") String topNodesNumberStr)
	    {
	    	try {
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	    		long graphId;
	    		try {
	    			graphId = Long.parseLong(graphIdStr);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
	    		long mapId;
	    		try {
	    			mapId = Long.parseLong(mapIdStr);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Centrality map id is not valid.");
	    		}
	    		CentralityOutputFormat format;
	    		try {
			    	format = CentralityOutputFormat.valueOf(centralityOutputFormatStr);
	    		}
		    	catch (Exception e) {
		    		requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified centrality output format does not exist.");
		    	}
	    		boolean onlyTopNodes;
	    		try {
	    			onlyTopNodes = requestHandler.parseBoolean(onlyTopNodesStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Only top nodes is not a boolean value.");
	    		}
	    		int topNodesNumber;
	    		try {
	    			topNodesNumber = Integer.parseInt(topNodesNumberStr);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Top nodes number is not valid.");
	    		}
	    		CentralityMap map = entityHandler.getCentralityMap(username, graphId, mapId);
	    		if(onlyTopNodes && topNodesNumber != 0) {
	    			return Response.ok(requestHandler.writeCentralityMapTopNodes(map, topNodesNumber)).build();
	    		}
		    	return Response.ok(requestHandler.writeCentralityMap(map, format)).build();
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	    
	    /**
	     * Deletes a CentralityMap. If the CentralityMap is still being created 
	     * by an algorithm, the algorithm is terminated.
	     * 
	     * @param mapIdStr 
	     *            The CentralityMap id.
	     * @param graphIdStr 
	     *            The graph id of the graph corresponding the CentralityMap.
	     * @return A confirmation xml. Or an error xml.
	     */
	    @DELETE
	    @Path("centrality/{mapId}/graphs/{graphId}")
	    @Produces(MediaType.TEXT_XML)
	    @ApiResponses(value = {
	    		@ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized")
	    })
		@ApiOperation(tags = {"delete"}, value = "Delete Centrality",
			notes = "Deletes a centrality map.")
	    public Response deleteCentralityMap(
	    		@PathParam("mapId") String mapIdStr,
	    		@PathParam("graphId") String graphIdStr)
	    {
	    	try {
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	    		long graphId;
	    		try {
	    			graphId = Long.parseLong(graphIdStr);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
	    		long mapId;
	    		try {
	    			mapId = Long.parseLong(mapIdStr);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Centrality map id is not valid.");
	    		}
	    		
		    	entityHandler.deleteCentralityMap(username, graphId, mapId, threadHandler);
		    	return Response.ok(requestHandler.writeConfirmationXml()).build();
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	    
	    /**
	     * Calculates the principal eigenvalue of the graph's adjacency matrix.
	     * 
	     * @param graphIdStr
	     *            The graph id of the graph.
	     * @return An xml containing the eigenvalue.
	     */
	    @GET
	    @Path("centrality/graphs/{graphId}/eigenvalue")
	    @Produces(MediaType.TEXT_XML)
	    @ApiResponses(value = {
	    		@ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized")
	    })
		@ApiOperation(tags = {"special"}, value = "Get Graph Eigenvalue",
			notes = "Get the absolute principal eigenvalue of the adjacency matrix of the given graph.")
	    public Response getAdjacencyMatrixEigenvalue(
	    		@PathParam("graphId") String graphIdStr)
	    {
	    	double eigenvalue;
	    	try {
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	    		long graphId;
	    		try {
	    			graphId = Long.parseLong(graphIdStr);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}

	    		EntityManager em = entityHandler.getEntityManager();
		    	CustomGraphId id = new CustomGraphId(graphId, username);
		    	synchronized(threadHandler) {
		    		EntityTransaction tx = em.getTransaction();
			    	CustomGraph graph;
			    	try {
			    		tx.begin();
						graph = em.find(CustomGraph.class, id);
				    	if(graph == null) {
				    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Graph does not exist: graph id " + graphId);
							return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph does not exist: graph id " + graphId);
				    	}
				    	if(graph.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
				    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Invalid graph creation method status for eigenvalue calculation: " + graph.getCreationMethod().getStatus().name());
							return requestHandler.writeError(Error.PARAMETER_INVALID, "Invalid graph creation method status for eigenvalue calculation: " + graph.getCreationMethod().getStatus().name());
				    	}
						tx.commit();
			    	}
			    	catch( RuntimeException e ) {
						if( tx != null && tx.isActive() ) {
							tx.rollback();
						}
						throw e;
					}
					em.close();
					eigenvalue = MatrixOperations.calculateAbsolutePrincipalEigenvalue(graph.getNeighbourhoodMatrix());
		    	}	
		    	return Response.ok(requestHandler.writeValueXml(eigenvalue)).build();
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	    
		////////////////////////////////////////////////////////////////////////////
		////////////// CENTRALITY SIMULATIONS
		////////////////////////////////////////////////////////////////////////////
		    
	    /**
	     * Creates a new CentralityMap by running a CentralitySimulation on an existing graph
	     * 
	     * @param graphIdStr 
	     *            The id of the graph that the CentralityMap is based on.
	     * @param simulationTypeStr 
	     *            The name of the CentralitySimulation to execute.
	     * @param content 
	     *            String containing the simulation parameters.
	     * @return The id of the CentralityMap being calculated which is reserved 
	     *            for the algorithm result. Or an error xml.
	     */
	    @POST
	    @Path("centrality/simulation/graphs/{graphId}")
	    @Produces(MediaType.TEXT_XML)
	    @Consumes(MediaType.TEXT_PLAIN)
	    @ApiResponses(value = {
	    		@ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized")
	    })
		@ApiOperation(tags = {"execution"}, value = "Run Centrality Simulation",
			notes = "Runs a centrality simulation on the specified graph. \n " +
					"The provided data represents the algorithm parameters and needs to be an XML with the root \"Parameters\" enclosing \"Parameter\" nodes which have both \"Name\" and \"Value\" nodes." +
					"\n To see which parameters of what type are needed for an algorithm, fetch its default parameters")
	    public Response runCentralitySimulation(
	    		@PathParam("graphId") String graphIdStr, 
	    		@DefaultValue("SIR Simulation") @QueryParam("simulation") String simulationTypeStr, String content)
	    {
	    	try {
	    		long graphId;
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	    		CentralitySimulationType simulationType;
	    		CentralitySimulation simulation;
	    		try {
	    			graphId = Long.parseLong(graphIdStr);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
	    		try {
	    			simulationType = CentralitySimulationType.valueOf(simulationTypeStr);
	    		}
		    	catch (Exception e) {
		    		requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified simulation does not exist.");
		    	}
	    		Map<String, String> parameters;
	    		Map<String, String> parametersCopy = new HashMap<String, String>();
	    		try {
	    			parameters = requestHandler.parseParameters(content);
	    			for(String parameter : parameters.keySet()) {
	    				parametersCopy.put(parameter, parameters.get(parameter));
	    			}
	    			simulation = centralitySimulationFactory.getInstance(simulationType, parameters);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Parameters are not valid.");
	    		}
	    		CentralityMap map;
		    	EntityManager em = entityHandler.getEntityManager();
		    	CustomGraphId id = new CustomGraphId(graphId, username);
		    	synchronized(threadHandler) {
		    		EntityTransaction tx = em.getTransaction();
			    	CustomGraph graph;
			    	CentralityCreationLog log;
			    	try {
			    		tx.begin();
						graph = em.find(CustomGraph.class, id);
				    	if(graph == null) {
				    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Graph does not exist: graph id " + graphId);
							return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph does not exist: graph id " + graphId);
				    	}
				    	if(graph.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
				    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Invalid graph creation method status for simulation execution: " + graph.getCreationMethod().getStatus().name());
							return requestHandler.writeError(Error.PARAMETER_INVALID, "Invalid graph creation method status for simulation execution: " + graph.getCreationMethod().getStatus().name());
				    	}
				    	if(simulation.getSimulationType() == CentralitySimulationType.RANDOM_PACKAGE_TRANSMISSION_UNWEIGHTED) {
							if(!GraphChecker.isConnected((Graph)graph)) {
								return Response.serverError().entity("Show Error: This simulation can only be used on a connected network.").build();
							}
						}
				    	map = new CentralityMap(graph);
				    	map.setName(simulationType.getDisplayName());
				    	log = new CentralityCreationLog(simulationType, CentralityCreationType.SIMULATION, parametersCopy, simulation.compatibleGraphTypes());
				    	map.setCreationMethod(log);
				    	em.persist(map);
						tx.commit();
			    	}
			    	catch( RuntimeException e ) {
						if( tx != null && tx.isActive() ) {
							tx.rollback();
						}
						throw e;
					}
					em.close();
			    	/*
			    	 * Registers and starts algorithm
			    	 */	
					threadHandler.runCentralitySimulation(map, simulation);
		    	}
		    	return Response.ok(requestHandler.writeId(map)).build();
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	    
		////////////////////////////////////////////////////////////////////////////
		////////////// CENTRALITY EVALUATION
		////////////////////////////////////////////////////////////////////////////
		    
	    /**
	     * Calculates the average centrality values given a list of centrality maps.
	     * Mainly meant to get the average of a number of simulation runs.
	     * 
	     * @param graphIdStr 
	     *            The id of the graph that the centrality maps are based on.
	     * @param ids 
	     *            The list of centrality map ids.
		 * @param averageMapName
		 * 			  The average name of a mao
	     * @return The id of the calculated average centrality map.
	     */
	    @GET
	    @Path("evaluation/average/graph/{graphId}/maps")
	    @Produces(MediaType.TEXT_XML)
	    @Consumes(MediaType.TEXT_PLAIN)
	    @ApiResponses(value = {
	    		@ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized")
	    })
		@ApiOperation(tags = {"special"}, value = "Get Average Centrality",
			notes = "Calculates the average centrality values from a list of centrality maps of the same graph.")
	    public Response getAverageCentralityMap(
	    		@PathParam("graphId") String graphIdStr, 
	    		@QueryParam("mapIds") List<Integer> ids,
	    		@QueryParam("mapName") String averageMapName) {
	    	try {
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	        	long graphId;
	        	try {
	    			graphId = Long.parseLong(graphIdStr);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
	        	CustomGraph graph;
	        	EntityManager em = entityHandler.getEntityManager();
	        	CustomGraphId gId = new CustomGraphId(graphId, username);
	        	synchronized(threadHandler) {
	        		EntityTransaction tx = em.getTransaction();
	    	    	try {
	    	    		tx.begin();
	    				graph = em.find(CustomGraph.class, gId);
	    		    	if(graph == null) {
	    		    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Graph does not exist: graph id " + graphId);
	    					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph does not exist: graph id " + graphId);
	    		    	}
	    		    	if(graph.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
	    		    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Invalid graph creation method status for centrality calculation: " + graph.getCreationMethod().getStatus().name());
	    					return requestHandler.writeError(Error.PARAMETER_INVALID, "Invalid graph creation method status for centrality calculation: " + graph.getCreationMethod().getStatus().name());
	    		    	}
	    				tx.commit();
	    	    	}
	    	    	catch( RuntimeException e ) {
	    				if( tx != null && tx.isActive() ) {
	    					tx.rollback();
	    				}
	    				throw e;
	    			}
	    			em.close();
	        	}
	        	
	        	List<CentralityMap> maps = new LinkedList<CentralityMap>();
	        	for(int id : ids) {
	        		long mapId = (long) id;
	        		em = entityHandler.getEntityManager();
	    	    	CentralityMapId cId = new CentralityMapId(mapId, gId);
	    	    	
	    	    	EntityTransaction tx = em.getTransaction();
	    	    	CentralityMap map;
	    	    	try {
	    				tx.begin();
	    				map = em.find(CentralityMap.class, cId);
	    				tx.commit();
	    			}
	    	    	catch( RuntimeException e ) {
	    				if( tx != null && tx.isActive() ) {
	    					tx.rollback();
	    				}
	    				throw e;
	    			}
	    	    	if(map == null) {
	    	    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Centrality map does not exist: Centrality map id " + mapId + ", graph id " + graphId);
	    				return requestHandler.writeError(Error.PARAMETER_INVALID, "Centrality map does not exist: Centrality map id " + mapId + ", graph id " + graphId);
	    	    	}
	    	    	maps.add(map);
	        	}
	        	CentralityMap averageMap = StatisticalProcessor.getAverageMap(graph, maps);
	        	CentralityCreationLog log;
	        	Map<String, String> parameters = new HashMap<String, String>();
	        	parameters.put("Number of measures", Integer.toString(ids.size()));
	        	synchronized(threadHandler) {
			    	EntityTransaction tx = em.getTransaction();
			    	try {
						tx.begin();
						log = new CentralityCreationLog(CentralityMeasureType.UNDEFINED, CentralityCreationType.AVERAGE, parameters, new HashSet<GraphType>(Arrays.asList(GraphType.values())));
						averageMap.setCreationMethod(log);
						averageMap.setName(averageMapName);
						em.persist(averageMap);
						tx.commit();
					}
			    	catch( RuntimeException e ) {
						if( tx != null && tx.isActive() ) {
							tx.rollback();
						}
						throw e;
					}
					em.close();
					threadHandler.createCentralityMap(averageMap, new CentralityMapId(averageMap.getId(), new CustomGraphId(graphId, username)), false);
		    	}
	        	
	        	return Response.ok(requestHandler.writeId(averageMap)).build();
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	    
	    /**
	     * Calculates the correlation matrix by calculating the corresponding correlation
	     * coefficient for each pair of centrality maps from the given list.
	     * 
	     * @param correlationCoefficientStr
	     *            The correlation coefficient that is used.
	     * @param graphIdStr 
	     *            The id of the graph that the centrality maps are based on.
	     * @param mapIds 
	     *            The list of centrality map ids.
	     * @return XML containing the correlation matrix.
	     */
	    @GET
	    @Path("evaluation/correlation/{coefficient}/graph/{graphId}/maps")
	    @Produces(MediaType.TEXT_XML)
	    @Consumes(MediaType.TEXT_PLAIN)
	    @ApiResponses(value = {
	    		@ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized")
	    })
		@ApiOperation(tags = {"special"}, value = "Get Centrality Correlation Matrix",
			notes = "Calculates a correlation matrix from a list of centrality maps on the same graph.")
	    public Response getCorrelation(
	    		@PathParam("coefficient") String correlationCoefficientStr,
	    		@PathParam("graphId") String graphIdStr, 
	    		@QueryParam("mapIds") List<Integer> mapIds) {
	    	try {
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	    		CorrelationCoefficient correlationCoefficient;
	    		try {
		    		correlationCoefficient = CorrelationCoefficient.valueOf(correlationCoefficientStr);
	    		}
		    	catch (Exception e) {
		    		requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified correlation coefficient does not exist.");
		    	}
	        	long graphId;
	        	try {
	    			graphId = Long.parseLong(graphIdStr);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
	        	CustomGraph graph;
	        	EntityManager em = entityHandler.getEntityManager();
	        	CustomGraphId gId = new CustomGraphId(graphId, username);
	        	synchronized(threadHandler) {
	        		EntityTransaction tx = em.getTransaction();
	    	    	try {
	    	    		tx.begin();
	    				graph = em.find(CustomGraph.class, gId);
	    		    	if(graph == null) {
	    		    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Graph does not exist: graph id " + graphId);
	    					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph does not exist: graph id " + graphId);
	    		    	}
	    		    	if(graph.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
	    		    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Invalid graph creation method status for correlation calculation: " + graph.getCreationMethod().getStatus().name());
	    					return requestHandler.writeError(Error.PARAMETER_INVALID, "Invalid graph creation method status for correlation calculation: " + graph.getCreationMethod().getStatus().name());
	    		    	}
	    				tx.commit();
	    	    	}
	    	    	catch( RuntimeException e ) {
	    				if( tx != null && tx.isActive() ) {
	    					tx.rollback();
	    				}
	    				throw e;
	    			}
	    			em.close();
	        	}
	        	List<CentralityMap> maps = new ArrayList<CentralityMap>();
	        	for(int id : mapIds) {
	        		long mapId = (long) id;
	        		em = entityHandler.getEntityManager();
	    	    	CentralityMapId cId = new CentralityMapId(mapId, gId);
	    	    	
	    	    	EntityTransaction tx = em.getTransaction();
	    	    	CentralityMap map;
	    	    	try {
	    				tx.begin();
	    				map = em.find(CentralityMap.class, cId);
	    				tx.commit();
	    			}
	    	    	catch( RuntimeException e ) {
	    				if( tx != null && tx.isActive() ) {
	    					tx.rollback();
	    				}
	    				throw e;
	    			}
	    	    	if(map == null) {
	    	    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Centrality map does not exist: Centrality map id " + mapId + ", graph id " + graphId);
	    				return requestHandler.writeError(Error.PARAMETER_INVALID, "Centrality map does not exist: Centrality map id " + mapId + ", graph id " + graphId);
	    	    	}
	    	    	maps.add(map);
	        	}
		    	RealMatrix correlationMatrix = StatisticalProcessor.getCorrelation(graph, maps, correlationCoefficient);    	        	
	        	return Response.ok(requestHandler.writeCorrelationMatrix(mapIds, correlationMatrix)).build();
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	    
	    /**
	     * Calculates the precision at k for the given centrality maps. The first one is selected as the ground truth.
	     * 
	     * @param kStr 
	     *            Number k of the top nodes that are considered.
	     * @param graphIdStr 
	     *            The id of the graph that the centrality maps are based on.
	     * @param mapIds 
	     *            The list of centrality map ids.
	     * @return XML containing the centrality map ids, names and precision values.
	     */
	    @GET
	    @Path("evaluation/precision/{k}/graph/{graphId}/maps")
	    @Produces(MediaType.TEXT_XML)
	    @Consumes(MediaType.TEXT_PLAIN)
	    @ApiResponses(value = {
	    		@ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized")
	    })
		@ApiOperation(tags = {"special"}, value = "get Centrality Precision",
			notes = "Calculates the precision.")
	    public Response getPrecision(
	    		@PathParam("k") String kStr,
	    		@PathParam("graphId") String graphIdStr,
	    		@QueryParam("mapIds") List<Integer> mapIds) {
	    	try {
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	    		int k;
	    		try {
	    			k = Integer.parseInt(kStr);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Parameter k is not valid.");
	    		}
	        	long graphId;
	        	try {
	    			graphId = Long.parseLong(graphIdStr);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
	        	CustomGraph graph;
	        	EntityManager em = entityHandler.getEntityManager();
	        	CustomGraphId gId = new CustomGraphId(graphId, username);
	        	synchronized(threadHandler) {
	        		EntityTransaction tx = em.getTransaction();
	    	    	try {
	    	    		tx.begin();
	    				graph = em.find(CustomGraph.class, gId);
	    		    	if(graph == null) {
	    		    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Graph does not exist: graph id " + graphId);
	    					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph does not exist: graph id " + graphId);
	    		    	}
	    		    	if(graph.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
	    		    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Invalid graph creation method status for correlation calculation: " + graph.getCreationMethod().getStatus().name());
	    					return requestHandler.writeError(Error.PARAMETER_INVALID, "Invalid graph creation method status for correlation calculation: " + graph.getCreationMethod().getStatus().name());
	    		    	}
	    				tx.commit();
	    	    	}
	    	    	catch( RuntimeException e ) {
	    				if( tx != null && tx.isActive() ) {
	    					tx.rollback();
	    				}
	    				throw e;
	    			}
	    			em.close();
	        	}
	        	List<CentralityMap> maps = new ArrayList<CentralityMap>();
	        	for(int id : mapIds) {
	        		long mapId = (long) id;
	        		em = entityHandler.getEntityManager();
	    	    	CentralityMapId cId = new CentralityMapId(mapId, gId);
	    	    	
	    	    	EntityTransaction tx = em.getTransaction();
	    	    	CentralityMap map;
	    	    	try {
	    				tx.begin();
	    				map = em.find(CentralityMap.class, cId);
	    				tx.commit();
	    			}
	    	    	catch( RuntimeException e ) {
	    				if( tx != null && tx.isActive() ) {
	    					tx.rollback();
	    				}
	    				throw e;
	    			}
	    	    	if(map == null) {
	    	    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Centrality map does not exist: Centrality map id " + mapId + ", graph id " + graphId);
	    				return requestHandler.writeError(Error.PARAMETER_INVALID, "Centrality map does not exist: Centrality map id " + mapId + ", graph id " + graphId);
	    	    	}
	    	    	maps.add(map);
	        	}
	        	CentralityMap groundTruthMap = maps.get(0);
	        	maps = maps.subList(1, maps.size());
		    	double[] precisionVector = StatisticalProcessor.getPrecision(graph, groundTruthMap, maps, k);    	        	
	        	return Response.ok(requestHandler.writePrecisionResult(maps, precisionVector)).build();
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	
		////////////////////////////////////////////////////////////////////////////
		////////////// BENCHMARKS
		////////////////////////////////////////////////////////////////////////////
		
	    /**
	     * Creates a ground truth benchmark cover.
	     * 
	     * @param coverNameStr
	     *            The name for the cover.
	     * @param graphNameStr
	     *            The name for the underlying benchmark graph.
	     * @param creationTypeStr
	     *            The name of a graph creation type corresponding a ground
	     *            truth benchmark.
	     * @param contentStr
	     *            A parameter xml defining any non-default parameters passed
	     *            to the benchmark.
	     * @return The id of the cover being calculated which is reserved for
	     *         the benchmark result (contains also the id of the graph being
	     *         calculated which is reserved for the benchmark result as
	     *         well). Or an error xml.
	     */
	    @POST
	    @Path("graphs/benchmarks")
	    @Produces(MediaType.TEXT_XML)
	    @Consumes(MediaType.TEXT_PLAIN)
	    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized") })
	    @ApiOperation(tags = {"execution"}, value = "Run Benchmark", notes = "Creates a ground truth benchmark cover. \n " +
				"The provided data represents the algorithm parameters and needs to be an XML with the root \"Parameters\" enclosing \"Parameter\" nodes which have both \"Name\" and \"Value\" nodes." +
				"\n To see which parameters of what type are needed for an algorithm, fetch its default parameters")
	    public Response runGroundTruthBenchmark(@DefaultValue("unnamed") @QueryParam("coverName") String coverNameStr,
	    		@DefaultValue("unnamed") @QueryParam("graphName") String graphNameStr,
	    		@DefaultValue("LFR") @QueryParam("benchmark") String creationTypeStr, String contentStr) {
	    	try {
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	    		GraphCreationType benchmarkType;
	    		CoverCreationType coverCreationType;
	    		try {
	    			benchmarkType = GraphCreationType.valueOf(creationTypeStr);
	    			coverCreationType = CoverCreationType.valueOf(creationTypeStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified benchmark does not exist.");
	    		}
	    		Map<String, String> parameters;
	    		GroundTruthBenchmark benchmark;
	    		if (!benchmarkFactory.isInstantiatable(benchmarkType)) {
	    			requestHandler.log(Level.WARNING, "user: " + username + ", "
	    					+ "Specified benchmark is not instantiatable: " + benchmarkType.name());
	    			return requestHandler.writeError(Error.PARAMETER_INVALID,
	    					"Specified benchmark is not instantiatable: " + benchmarkType.name());
	    		} else if (!benchmarkType.correspondsGroundTruthBenchmark()) {
	    			requestHandler.log(Level.WARNING, "user: " + username + ", "
	    					+ "Specified benchmark is not a ground truth benchmark: " + benchmarkType.name());
	    			return requestHandler.writeError(Error.PARAMETER_INVALID,
	    					"Specified benchmark is not a ground truth benchmark: " + benchmarkType.name());
	    		} else {
	    			try {
	    				parameters = requestHandler.parseParameters(contentStr);
	    				benchmark = (GroundTruthBenchmark) benchmarkFactory.getInstance(benchmarkType, parameters);
	    			} catch (Exception e) {
	    				requestHandler.log(Level.WARNING, "user: " + username, e);
	    				return requestHandler.writeError(Error.PARAMETER_INVALID, "Parameters are not valid.");
	    			}
	    		}
	    		EntityManager em = entityHandler.getEntityManager();
	    		CustomGraph graph = new CustomGraph();
	    		graph.setName(URLDecoder.decode(graphNameStr, "UTF-8"));
	    		graph.setUserName(username);
	    		GraphCreationLog log = new GraphCreationLog(benchmarkType, parameters);
	    		log.setStatus(ExecutionStatus.WAITING);
	    		graph.setCreationMethod(log);
	    		Cover cover = new Cover(graph, new CCSMatrix(graph.nodeCount(), 0));
	    		cover.setName(URLDecoder.decode(coverNameStr, "UTF-8"));
	    		CoverCreationLog coverLog = new CoverCreationLog(coverCreationType, parameters,
	    				new HashSet<GraphType>());
	    		coverLog.setStatus(ExecutionStatus.WAITING);
	    		cover.setCreationMethod(coverLog);
	    		synchronized (threadHandler) {
	
	    			EntityTransaction tx = em.getTransaction();
	    			try {
	    				tx.begin();
	    				em.persist(graph);
	    				em.persist(cover);
	    				tx.commit();
	    			} catch (RuntimeException e) {
	    				if (tx != null && tx.isActive()) {
	    					tx.rollback();
	    				}
	    				throw e;
	    			}
	    			em.close();
	    			/*
	    			 * Registers and starts benchmark creation.
	    			 */
	    			threadHandler.runGroundTruthBenchmark(cover, benchmark);
	    		}
	    		return Response.ok(requestHandler.writeId(cover)).build();
	    	} catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	    
		//////////////////////////////////////////////////////////////////////////
		//////////// METRICS
		//////////////////////////////////////////////////////////////////////////
		
	    /**
	     * Runs a statistical measure on a cover and creates the corresponding
	     * log.
	     * 
	     * @param coverIdStr
	     *            The id of the cover, must have the creation method status
	     *            completed.
	     * @param graphIdStr
	     *            The id of the graph corresponding to the cover.
	     * @param metricTypeStr
	     *            A metric type corresponding to a statistical measure.
	     * @param contentStr
	     *            A parameter xml defining any non-default parameters passed
	     *            to the metric.
	     * @return The id of the metric log being calculated which is reserved
	     *         for the metric result (contains also the corresponding cover
	     *         and graph id). Or an error xml.
	     */
	    @POST
	    @Path("covers/{coverId}/graphs/{graphId}/metrics/statistical")
	    @Produces(MediaType.TEXT_XML)
	    @Consumes(MediaType.TEXT_PLAIN)
	    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized") })
	    @ApiOperation(tags = {"execution"}, value = "Run Statistical Metric Measure", notes = "Runs a statistical metric measure on a cover and creates the corresponding log. \n " +
				"The provided data represents the algorithm parameters and needs to be an XML with the root \"Parameters\" enclosing \"Parameter\" nodes which have both \"Name\" and \"Value\" nodes." +
				"\n To see which parameters of what type are needed for an algorithm, fetch its default parameters")
	    public Response runStatisticalMeasure(@PathParam("coverId") String coverIdStr,
	    		@PathParam("graphId") String graphIdStr,
	    		@DefaultValue("EXTENDED_MODULARITY") @QueryParam("metricType") String metricTypeStr,
	    		String contentStr) {
	    	try {
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	    		long graphId;
	    		try {
	    			graphId = Long.parseLong(graphIdStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
	    		long coverId;
	    		try {
	    			coverId = Long.parseLong(coverIdStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover id is not valid.");
	    		}
	    		OcdMetricType metricType;
	    		try {
	    			metricType = OcdMetricType.valueOf(metricTypeStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified metric does not exist.");
	    		}
	    		Map<String, String> parameters;
	    		StatisticalMeasure metric;
	    		if (!metricFactory.isInstantiatable(metricType)) {
	    			requestHandler.log(Level.WARNING, "user: " + username + ", "
	    					+ "Specified metric is not instantiatable: " + metricType.name());
	    			return requestHandler.writeError(Error.PARAMETER_INVALID,
	    					"Specified metric is not instantiatable: " + metricType.name());
	    		} else if (!metricType.correspondsStatisticalMeasure()) {
	    			requestHandler.log(Level.WARNING, "user: " + username + ", "
	    					+ "Specified metric is not a statistical measure: " + metricType.name());
	    			return requestHandler.writeError(Error.PARAMETER_INVALID,
	    					"Specified metric is not a statistical measure: " + metricType.name());
	    		} else {
	    			try {
	    				parameters = requestHandler.parseParameters(contentStr);
	    				metric = (StatisticalMeasure) metricFactory.getInstance(metricType, parameters);
	    			} catch (Exception e) {
	    				requestHandler.log(Level.WARNING, "user: " + username, e);
	    				return requestHandler.writeError(Error.PARAMETER_INVALID, "Parameters are not valid.");
	    			}
	    		}
	
	    		EntityManager em = entityHandler.getEntityManager();
	    		CustomGraphId gId = new CustomGraphId(graphId, username);
	    		CoverId cId = new CoverId(coverId, gId);
	    		/*
	    		 * Finds cover
	    		 */
	    		OcdMetricLog log;
	    		synchronized (threadHandler) {
	    			EntityTransaction tx = em.getTransaction();
	    			Cover cover;
	    			try {
	    				tx.begin();
	    				cover = em.find(Cover.class, cId);
	    				if (cover == null) {
	    					requestHandler.log(Level.WARNING, "user: " + username + ", "
	    							+ "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
	    					return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover does not exist.");
	    				}
	    				if (cover.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
	    					requestHandler.log(Level.WARNING,
	    							"user: " + username + ", "
	    									+ "Invalid cover creation method status for metric execution: "
	    									+ cover.getCreationMethod().getStatus().name());
	    					return requestHandler.writeError(Error.PARAMETER_INVALID,
	    							"Invalid cover creation method status for metric execution: "
	    									+ cover.getCreationMethod().getStatus().name());
	    				}
	    				
	    				boolean compatibleType = false;
	    				if(cover.getGraph().getTypes().isEmpty()) 
	    				{
	    					compatibleType = true;
	    				}
	    				else {
		    				for(GraphType type : cover.getGraph().getTypes()) {
		    					if(metric.compatibleGraphTypes().contains(type))
		    					{
		    						compatibleType = true;
		    						break;
		    					}	    					
		    				}
	    				}
	    				if(!compatibleType) {
							requestHandler.log(Level.WARNING,
									"user: " + username + ", "
											+ "Metric not applicable with graph, needs one of these types: "
											+  metric.compatibleGraphTypes().toString());
							return requestHandler.writeError(Error.PARAMETER_INVALID,
									"Metric not applicable with graph, needs one of these types: " + metric.compatibleGraphTypes().toString());
						}
	    				else if (metric instanceof NewmanModularityCombined && !cover.getGraph().isOfType(GraphType.CONTENT_LINKED) && !cover.getGraph().isOfType(GraphType.CONTENT_UNLINKED))
	    				{
	    					requestHandler.log(Level.WARNING,
									"user: " + username + ", "
											+ "Metric not applicable with graph, needs to be a graph with node content "
											+  metric.compatibleGraphTypes().toString());
							return requestHandler.writeError(Error.PARAMETER_INVALID,
									"Metric not applicable with graph, needs to be a graph with node content");
	    				}
	    				
	    				log = new OcdMetricLog(metricType, 0, parameters, cover);
	    				log.setStatus(ExecutionStatus.WAITING);
	    				cover.addMetric(log);
	    				tx.commit();
	    			} catch (RuntimeException e) {
	    				if (tx != null && tx.isActive()) {
	    					tx.rollback();
	    				}
	    				throw e;
	    			}
	    			threadHandler.runStatisticalMeasure(log, metric, cover);
	    		}
	
	    		return Response.ok(requestHandler.writeId(log)).build();
	    	} catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	
	    /**
	     * Runs a knowledge-driven measure on a cover and creates the
	     * corresponding log.
	     * 
	     * @param coverIdStr
	     *            The id of the cover, must have the creation method status
	     *            completed.
	     * @param graphIdStr
	     *            The id of the graph corresponding to the cover.
	     * @param metricTypeStr
	     *            A metric type corresponding to a knowledge-driven measure.
	     * @param groundTruthCoverIdStr
	     *            The id of the ground truth cover used by the metric, must
	     *            have the creation method status completed. The ground
	     *            truth cover's corresponding graph must be the same as the
	     *            one corresponding to the first cover.
	     * @param contentStr
	     *            A parameter xml defining any non-default parameters passed
	     *            to the algorithm.
	     * @return The id of the metric log being calculated which is reserved
	     *         for the metric result (contains also the corresponding cover
	     *         and graph id). Or an error xml.
	     */
	    @POST
	    @Path("covers/{coverId}/graphs/{graphId}/metrics/knowledgedriven/groundtruth/{groundTruthCoverId}")
	    @Produces(MediaType.TEXT_XML)
	    @Consumes(MediaType.TEXT_PLAIN)
	    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized") })
	    @ApiOperation(tags = {"execution"}, value = "Run Knowledge-Driven Measure", notes = "Runs a knowledge-driven measure on a cover and creates the corresponding log. \n " +
				"The provided data represents the algorithm parameters and needs to be an XML with the root \"Parameters\" enclosing \"Parameter\" nodes which have both \"Name\" and \"Value\" nodes." +
				"\n To see which parameters of what type are needed for an algorithm, fetch its default parameters")
	    public Response runKnowledgeDrivenMeasure(@PathParam("coverId") String coverIdStr,
	    		@PathParam("graphId") String graphIdStr,
	    		@DefaultValue("EXTENDED_NORMALIZED_MUTUAL_INFORMATION") @QueryParam("metricType") String metricTypeStr,
	    		@PathParam("groundTruthCoverId") String groundTruthCoverIdStr, String contentStr) {
	    	try {
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	    		long graphId;
	    		try {
	    			graphId = Long.parseLong(graphIdStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
	    		long coverId;
	    		try {
	    			coverId = Long.parseLong(coverIdStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover id is not valid.");
	    		}
	    		long groundTruthCoverId;
	    		try {
	    			groundTruthCoverId = Long.parseLong(groundTruthCoverIdStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Ground truth cover id is not valid.");
	    		}
	    		OcdMetricType metricType;
	    		try {
	    			metricType = OcdMetricType.valueOf(metricTypeStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified metric does not exist.");
	    		}
	    		Map<String, String> parameters;
	    		KnowledgeDrivenMeasure metric;
	    		if (!metricFactory.isInstantiatable(metricType)) {
	    			requestHandler.log(Level.WARNING, "user: " + username + ", "
	    					+ "Specified metric is not instantiatable: " + metricType.name());
	    			return requestHandler.writeError(Error.PARAMETER_INVALID,
	    					"Specified metric is not instantiatable: " + metricType.name());
	    		} else if (!metricType.correspondsKnowledgeDrivenMeasure()) {
	    			requestHandler.log(Level.WARNING, "user: " + username + ", "
	    					+ "Specified metric is not a knowledge-driven measure: " + metricType.name());
	    			return requestHandler.writeError(Error.PARAMETER_INVALID,
	    					"Specified metric is not a knowledge-driven measure: " + metricType.name());
	    		} else {
	    			try {
	    				parameters = requestHandler.parseParameters(contentStr);
	    				metric = (KnowledgeDrivenMeasure) metricFactory.getInstance(metricType, parameters);
	    			} catch (Exception e) {
	    				requestHandler.log(Level.WARNING, "user: " + username, e);
	    				return requestHandler.writeError(Error.PARAMETER_INVALID, "Parameters are not valid.");
	    			}
	    		}
	    		EntityManager em = entityHandler.getEntityManager();
	    		CustomGraphId gId = new CustomGraphId(graphId, username);
	    		CoverId cId = new CoverId(coverId, gId);
	    		CoverId gtId = new CoverId(groundTruthCoverId, gId);
	    		/*
	    		 * Finds cover
	    		 */
	    		OcdMetricLog log;
	    		synchronized (threadHandler) {
	    			EntityTransaction tx = em.getTransaction();
	    			Cover cover;
	    			Cover groundTruth;
	    			try {
	    				tx.begin();
	    				cover = em.find(Cover.class, cId);
	    				if (cover == null) {
	    					requestHandler.log(Level.WARNING, "user: " + username + ", "
	    							+ "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
	    					return requestHandler.writeError(Error.PARAMETER_INVALID,
	    							"Cover does not exist: cover id " + coverId + ", graph id " + graphId);
	    				}
	    				if (cover.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
	    					requestHandler.log(Level.WARNING,
	    							"user: " + username + ", "
	    									+ "Invalid cover creation method status for metric execution: "
	    									+ cover.getCreationMethod().getStatus().name());
	    					return requestHandler.writeError(Error.PARAMETER_INVALID,
	    							"Invalid cover creation method status for metric execution: "
	    									+ cover.getCreationMethod().getStatus().name());
	    				}
	    				if (groundTruthCoverId != coverId) {
	    					groundTruth = em.find(Cover.class, gtId);
	    					if (groundTruth == null) {
	    						requestHandler.log(Level.WARNING,
	    								"user: " + username + ", " + "Ground truth cover does not exist: cover id "
	    										+ groundTruthCoverId + ", graph id " + graphId);
	    						return requestHandler.writeError(Error.PARAMETER_INVALID,
	    								"Ground truth cover does not exist: cover id " + groundTruthCoverId
	    								+ ", graph id " + graphId);
	    					}
	    				} else {
	    					groundTruth = cover;
	    				}
	    				if (groundTruth.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
	    					requestHandler.log(Level.WARNING,
	    							"user: " + username + ", "
	    									+ "Invalid ground truth cover creation method status for metric execution: "
	    									+ groundTruth.getCreationMethod().getStatus().name());
	    					return requestHandler.writeError(Error.PARAMETER_INVALID,
	    							"Invalid ground truth cover creation method status for metric execution: "
	    									+ groundTruth.getCreationMethod().getStatus().name());
	    				}
	    				log = new OcdMetricLog(metricType, 0, parameters, cover);
	    				log.setStatus(ExecutionStatus.WAITING);
	    				cover.addMetric(log);
	    				tx.commit();
	    			} catch (RuntimeException e) {
	    				if (tx != null && tx.isActive()) {
	    					tx.rollback();
	    				}
	    				throw e;
	    			}
	    			threadHandler.runKnowledgeDrivenMeasure(log, metric, cover, groundTruth);
	    		}
	    		return Response.ok(requestHandler.writeId(log)).build();
	    	} catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	
	    /**
	     * Deletes a metric. If the metric is still in calculation, the
	     * execution is terminated.
	     * 
	     * @param coverIdStr
	     *            The id of the cover that the metric belongs to.
	     * @param graphIdStr
	     *            The id of the graph corresponding to the cover.
	     * @param metricIdStr
	     *            The metric id.
	     * @return A confirmation xml. Or an error xml.
	     */
	    @DELETE
	    @Path("covers/{coverId}/graphs/{graphId}/metrics/{metricId}")
	    @Produces(MediaType.TEXT_XML)
	    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized") })
	    @ApiOperation(tags = {"delete"}, value = "Delete Metric", notes = "Deletes a metric.")
	    public Response deleteMetric(@PathParam("coverId") String coverIdStr, @PathParam("graphId") String graphIdStr,
	    		@PathParam("metricId") String metricIdStr) {
	    	try {
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	    		long graphId;
	    		try {
	    			graphId = Long.parseLong(graphIdStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
	    		long coverId;
	    		try {
	    			coverId = Long.parseLong(coverIdStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover id is not valid.");
	    		}
	    		long metricId;
	    		try {
	    			metricId = Long.parseLong(metricIdStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Metric id is not valid.");
	    		}
	    		EntityManager em = entityHandler.getEntityManager();
	    		CustomGraphId gId = new CustomGraphId(graphId, username);
	    		CoverId cId = new CoverId(coverId, gId);
	    		OcdMetricLogId mId = new OcdMetricLogId(metricId, cId);
	    		EntityTransaction tx = em.getTransaction();
	    		OcdMetricLog log;
	    		/*
	    		 * Deletes the metric.
	    		 */
	    		synchronized (threadHandler) {
	    			tx = em.getTransaction();
	    			try {
	    				tx.begin();
	    				log = em.find(OcdMetricLog.class, mId);
	    				tx.commit();
	    			} catch (RuntimeException e) {
	    				if (tx != null && tx.isActive()) {
	    					tx.rollback();
	    				}
	    				throw e;
	    			}
	    			if (log == null) {
	    				requestHandler.log(Level.WARNING,
	    						"user: " + username + ", " + "Metric does not exist: cover id " + coverId
	    						+ ", graph id " + graphId + ", metric id " + metricId);
	    				return requestHandler.writeError(Error.PARAMETER_INVALID, "Metric does not exist: cover id "
	    						+ coverId + ", graph id " + graphId + ", metric id " + metricId);
	    			}
	    			/*
	    			 * Interrupts metric.
	    			 */
	    			threadHandler.interruptMetric(mId);
	    			/*
	    			 * Removes metric
	    			 */
	    			tx = em.getTransaction();
	    			try {
	    				tx.begin();
	    				log.getCover().removeMetric(log);
	    				em.remove(log);
	    				tx.commit();
	    			} catch (RuntimeException e) {
	    				if (tx != null && tx.isActive()) {
	    					tx.rollback();
	    				}
	    				throw e;
	    			}
	    			em.close();
	    			return Response.ok(requestHandler.writeConfirmationXml()).build();
	    		}
	    	} catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	    
		//////////////////////////////////////////////////////////////////
		///////// VISUALIZATION
		//////////////////////////////////////////////////////////////////
		
	    /**
	     * Returns a visual representation of a cover.
	     * 
	     * @param graphIdStr
	     *            The id of the graph that the cover is based on.
	     * @param coverIdStr
	     *            The id of the cover.
	     * @param graphLayoutTypeStr
	     *            The name of the layout type defining which graph layouter
	     *            to use.
	     * @param coverPaintingTypeStr
	     *            The name of the cover painting type defining which cover
	     *            painter to use.
	     * @param visualOutputFormatStr
	     *            The name of the required output format.
	     * @param doLabelNodesStr
	     *            Optional query parameter. Defines whether nodes will
	     *            receive labels with their names (TRUE) or not (FALSE).
	     * @param doLabelEdgesStr
	     *            Optional query parameter. Defines whether edges will
	     *            receive labels with their weights (TRUE) or not (FALSE).
	     * @param minNodeSizeStr
	     *            Optional query parameter. Defines the minimum size of a
	     *            node. Must be greater than 0.
	     * @param maxNodeSizeStr
	     *            Optional query parameter. Defines the maximum size of a
	     *            node. Must be at least as high as the defined minimum
	     *            size.
	     * @return The visualization. Or an error xml.
	     */
	    @GET
	    @Path("visualization/cover/{coverId}/graph/{graphId}/outputFormat/{VisualOutputFormat}/layout/{GraphLayoutType}/paint/{CoverPaintingType}")
		@ApiOperation(tags = {"visualizations"}, value = "Get Cover Visualization", notes = "Retreives a cover visualization, either in SVG or JSON for Force-Graphs")
		public Response getCoverVisualization(@PathParam("graphId") String graphIdStr,
	    		@PathParam("coverId") String coverIdStr, @PathParam("GraphLayoutType") String graphLayoutTypeStr,
	    		@PathParam("CoverPaintingType") String coverPaintingTypeStr,
	    		@PathParam("VisualOutputFormat") String visualOutputFormatStr,
	    		@DefaultValue("TRUE") @QueryParam("doLabelNodes") String doLabelNodesStr,
	    		@DefaultValue("FALSE") @QueryParam("doLabelEdges") String doLabelEdgesStr,
	    		@DefaultValue("20") @QueryParam("minNodeSize") String minNodeSizeStr,
	    		@DefaultValue("45") @QueryParam("maxNodeSize") String maxNodeSizeStr) {
	    	try {
	    		long graphId;
	    		String username = getUserName();
	    		try {
	    			graphId = Long.parseLong(graphIdStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
	    		long coverId;
	    		try {
	    			coverId = Long.parseLong(coverIdStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover id is not valid.");
	    		}
	    		double minNodeSize;
	    		try {
	    			minNodeSize = Double.parseDouble(minNodeSizeStr);
	    			if (minNodeSize < 0) {
	    				throw new IllegalArgumentException();
	    			}
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Min node size is not valid.");
	    		}
	    		double maxNodeSize;
	    		try {
	    			maxNodeSize = Double.parseDouble(maxNodeSizeStr);
	    			if (maxNodeSize < minNodeSize) {
	    				throw new IllegalArgumentException();
	    			}
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Max node size is not valid.");
	    		}
	    		VisualOutputFormat format;
	    		GraphLayoutType layout;
	    		boolean doLabelNodes;
	    		boolean doLabelEdges;
	    		try {
	    			layout = GraphLayoutType.valueOf(graphLayoutTypeStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified layout does not exist.");
	    		}
	    		CoverPaintingType painting;
	    		try {
	    			painting = CoverPaintingType.valueOf(coverPaintingTypeStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified layout does not exist.");
	    		}
	    		try {
	    			format = VisualOutputFormat.valueOf(visualOutputFormatStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID,
	    					"Specified visual graph output format does not exist.");
	    		}
	    		try {
	    			doLabelNodes = requestHandler.parseBoolean(doLabelNodesStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Label nodes is not a boolean value.");
	    		}
	    		try {
	    			doLabelEdges = requestHandler.parseBoolean(doLabelEdgesStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Label edges is not a boolean value.");
	    		}

	    		Cover cover = entityHandler.getCover(username, graphId, coverId);
	    		if (cover == null) {
	    			requestHandler.log(Level.WARNING, "user: " + username + ", " + "Cover does not exist: cover id "
	    					+ coverId + ", graph id " + graphId);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID,
	    					"Cover does not exist: cover id " + coverId + ", graph id " + graphId);
	    		}
	    		
	    		layoutHandler.doLayout(cover, layout, doLabelNodes, doLabelEdges, minNodeSize, maxNodeSize, painting);
	    		
		    	EntityManager em = entityHandler.getEntityManager();
		    	EntityTransaction tx = em.getTransaction();
		    	try {		
			    	tx.begin();			    	
			    	em.merge(cover);		    		
			    	tx.commit();
		    	} catch (RuntimeException e) {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
					throw e;
		    	}
		    	em.close();
	    		
	    		return requestHandler.writeCover(cover, format);
	    	} catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }

	    /**
	     * Returns a visual representation of a graph.
	     * 
	     * @param graphIdStr
	     *            The id of the graph.
	     * @param graphLayoutTypeStr
	     *            The name of the layout type defining which graph layouter
	     *            to use.
	     * @param visualOutputFormatStr
	     *            The name of the required output format.
	     * @param doLabelNodesStr
	     *            Optional query parameter. Defines whether nodes will
	     *            receive labels with their names (TRUE) or not (FALSE).
	     * @param doLabelEdgesStr
	     *            Optional query parameter. Defines whether edges will
	     *            receive labels with their weights (TRUE) or not (FALSE).
	     * @param minNodeSizeStr
	     *            Optional query parameter. Defines the minimum size of a
	     *            node. Must be greater than 0.
	     * @param maxNodeSizeStr
	     *            Optional query parameter. Defines the maximum size of a
	     *            node. Must be at least as high as the defined minimum
	     *            size.
	     * @return The visualization. Or an error xml.
	     */
	    @GET
	    @Path("visualization/graph/{graphId}/outputFormat/{VisualOutputFormat}/layout/{GraphLayoutType}")
		@ApiOperation(tags = {"visualizations"}, value = "Get Graph Visualization", notes = "Retreives a graph visualization, either in SVG or JSON for Force-Graphs")
		public Response getGraphVisualization(@PathParam("graphId") String graphIdStr,
	    		@PathParam("GraphLayoutType") String graphLayoutTypeStr,
	    		@PathParam("VisualOutputFormat") String visualOutputFormatStr,
	    		@DefaultValue("TRUE") @QueryParam("doLabelNodes") String doLabelNodesStr,
	    		@DefaultValue("FALSE") @QueryParam("doLabelEdges") String doLabelEdgesStr,
	    		@DefaultValue("20") @QueryParam("minNodeSize") String minNodeSizeStr,
	    		@DefaultValue("45") @QueryParam("maxNodeSize") String maxNodeSizeStr) {
	    	try {
	    		long graphId;
	    		String username = getUserName();
	    		try {
	    			graphId = Long.parseLong(graphIdStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
	    		double minNodeSize;
	    		try {
	    			minNodeSize = Double.parseDouble(minNodeSizeStr);
	    			if (minNodeSize < 0) {
	    				throw new IllegalArgumentException();
	    			}
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Min node size is not valid.");
	    		}
	    		double maxNodeSize;
	    		try {
	    			maxNodeSize = Double.parseDouble(maxNodeSizeStr);
	    			if (maxNodeSize < minNodeSize) {
	    				throw new IllegalArgumentException();
	    			}
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Max node size is not valid.");
	    		}
	    		VisualOutputFormat format;
	    		GraphLayoutType layout;
	    		boolean doLabelNodes;
	    		boolean doLabelEdges;
	    		try {
	    			layout = GraphLayoutType.valueOf(graphLayoutTypeStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified layout does not exist.");
	    		}
	    		try {
	    			format = VisualOutputFormat.valueOf(visualOutputFormatStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID,
	    					"Specified visual graph output format does not exist.");
	    		}
	    		try {
	    			doLabelNodes = requestHandler.parseBoolean(doLabelNodesStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Label nodes is not a boolean value.");
	    		}
	    		try {
	    			doLabelEdges = requestHandler.parseBoolean(doLabelEdgesStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Label edges is not a boolean value.");
	    		}

	    		CustomGraph graph = entityHandler.getGraph(username, graphId);
	    		if (graph == null) {
	    			requestHandler.log(Level.WARNING,
	    					"user: " + username + ", " + "Graph does not exist: graph id " + graphId);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID,
	    					"Graph does not exist: graph id " + graphId);
	    		}

	    		layoutHandler.doLayout(graph, layout, doLabelNodes, doLabelEdges, minNodeSize, maxNodeSize);
	    		return requestHandler.writeGraph(graph, format);
	    	} catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }

	    /**
	     * Returns a visual representation of a CentralityMap.
	     * 
	     * @param graphIdStr 
	     *            The id of the graph that the CentralityMap is based on.
	     * @param centralityMapIdStr 
	     *            The id of the CentralityMap.
	     * @param graphLayoutTypeStr 
	     *            The name of the layout type defining which graph layouter to use.
	     * @param centralityVisualizationTypeStr 
	     *            The type of visualization to represent the centrality values.
	     * @param visualOutputFormatStr 
	     *            The name of the required output format.
	     * @param doLabelNodesStr 
	     *            Optional query parameter. Defines whether nodes will receive labels with their names (TRUE) or not (FALSE).
	     * @param showEdgeWeightsStr 
	     *            Optional query parameter. Defines whether edges will receive labels with their weights (TRUE) or not (FALSE) (but only if the graph is weighted).
	     * @return The visualization. Or an error xml.
	     */
	    @GET
	    @Path("visualization/centralityMap/{centralityMapId}/graph/{graphId}/outputFormat/{VisualOutputFormat}/layout/{GraphLayoutType}/centralityVisualization/{CentralityVisualizationType}")
		@ApiOperation(tags = {"visualizations"}, value = "Get Centrality Visualization", notes = "Retreives a centrality visualization, either in SVG or JSON for Force-Graphs")
		public Response getCentralityMapVisualization(
	    		@PathParam("graphId") String graphIdStr, 
	    		@PathParam("centralityMapId") String centralityMapIdStr,
	    		@PathParam("GraphLayoutType") String graphLayoutTypeStr,
	    		@PathParam("CentralityVisualizationType") String centralityVisualizationTypeStr,
	    		@PathParam("VisualOutputFormat") String visualOutputFormatStr,
	    		@DefaultValue("TRUE") @QueryParam("doLabelNodes") String doLabelNodesStr,
	    		@DefaultValue("TRUE") @QueryParam("showEdgeWeights") String showEdgeWeightsStr) {
	    	try {
	    		long graphId;
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	    		try {
	    			graphId = Long.parseLong(graphIdStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
	    		long centralityMapId;
	    		try {
	    			centralityMapId = Long.parseLong(centralityMapIdStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "CentralityMap id is not valid.");
	    		}
	    		VisualOutputFormat format;
	    		GraphLayoutType layout;
	    		boolean doLabelNodes;
	    		boolean doLabelEdges;
	    		try {
	    			layout = GraphLayoutType.valueOf(graphLayoutTypeStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified layout does not exist.");
	    		}
	    		CentralityVisualizationType centralityVisualizationType;
	    		try {
	    			centralityVisualizationType = CentralityVisualizationType.valueOf(centralityVisualizationTypeStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified centrality visualization type does not exist.");
	    		}
	    		try {
	    			format = VisualOutputFormat.valueOf(visualOutputFormatStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified visual graph output format does not exist.");
	    		}
	    		try {
	    			doLabelNodes = requestHandler.parseBoolean(doLabelNodesStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Label nodes is not a boolean value.");
	    		}
	    		try {
	    			doLabelEdges = requestHandler.parseBoolean(showEdgeWeightsStr);
	    		} catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "", e);
	    			return requestHandler.writeError(Error.PARAMETER_INVALID, "Label edges is not a boolean value.");
	    		}
	    		EntityManager em = entityHandler.getEntityManager();
	    		CustomGraphId gId = new CustomGraphId(graphId, username);
	    		CentralityMapId cId = new CentralityMapId(centralityMapId, gId);
	    		EntityTransaction tx = em.getTransaction();
	    		CentralityMap map;
	    		try {
	    			tx.begin();
	    			map = em.find(CentralityMap.class, cId);
	    			if(map == null) {
	    				requestHandler.log(Level.WARNING, "user: " + username + ", " + "CentralityMap does not exist: CentralityMap id " + centralityMapId + ", graph id " + graphId);
	    				return requestHandler.writeError(Error.PARAMETER_INVALID, "CentralityMap does not exist: CentralityMap id " + centralityMapId + ", graph id " + graphId);
	    			}
	    			tx.commit();
	    		} catch( RuntimeException e ) {
	    			if( tx != null && tx.isActive() ) {
	    				tx.rollback();
	    			}
	    			throw e;
	    		}
	    		em.close();
	    		if(doLabelEdges) {
	    			doLabelEdges = map.getGraph().getTypes().contains(GraphType.WEIGHTED) ? true : false;
	    		}
	    		layoutHandler.doLayout(map, layout, doLabelNodes, doLabelEdges, centralityVisualizationType);
	    		return requestHandler.writeCentralityMap(map, format);
	    	} catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	    
		//////////////////////////////////////////////////////////////////////////
		//////////// DEFAULT PARAMETERS
		//////////////////////////////////////////////////////////////////////////
		    
	    /**
		 * Returns the default parameters of an algorithm.
		 * 
		 * @param coverCreationTypeStr
		 *            A cover creation type corresponding to an ocd algorithm.
		 * @return A parameter xml. Or an error xml.
		 */
		@GET
		@Path("algorithms/{CoverCreationType}/parameters/default")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"defaults"}, value = "Return Default Algo Params", notes = "Returns the default parameters of an algorithm.")
		public Response getAlgorithmDefaultParams(@PathParam("CoverCreationType") String coverCreationTypeStr) {
			try {
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				CoverCreationType creationType;
				try {
					creationType = CoverCreationType.valueOf(coverCreationTypeStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified cover creation type does not exist.");
				}
				if (!algorithmFactory.isInstantiatable(creationType)) {
					requestHandler.log(Level.WARNING, "user: " + username + ", "
							+ "Specified cover creation type is not instantiatable: " + creationType.name());
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified cover creation type is not instantiatable: " + creationType.name());
				} else {
					OcdAlgorithm defaultInstance = algorithmFactory.getInstance(creationType,
							new HashMap<String, String>());
					return Response.ok(requestHandler.writeParameters(defaultInstance.getParameters())).build();
				}
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
		
		/**
	     * Returns the default parameters of a CentralityAlgorithm.
	     * 
	     * @param centralityMeasureTypeStr 
	     *            A CentralityMeasureType corresponding to a CentralityAlgorithm.
	     * @return A parameter xml. Or an error xml.
	     */
	    @GET
	    @Path("centralities/{CentralityMeasureType}/parameters/default")
	    @Produces(MediaType.TEXT_XML)
	    @ApiResponses(value = {
	    		@ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized")
	    })
		@ApiOperation(tags = {"defaults"}, value = "Return Default Centrality params",
			notes = "Returns the default parameters of a centrality measure.")
	    public Response getCentralityAlgorithmDefaultParams(
	    		@PathParam("CentralityMeasureType") String centralityMeasureTypeStr)
	    {
	    	try {
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	    		CentralityMeasureType centralityMeasureType;
	    		try {
	    			centralityMeasureType = CentralityMeasureType.valueOf(centralityMeasureTypeStr);
	    		}
		    	catch (Exception e) {
		    		requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified centrality measure type does not exist.");
		    	}
				if(!centralityAlgorithmFactory.isInstantiatable(centralityMeasureType)) {
					requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified centrality measure type is not instantiatable: " + centralityMeasureType.getDisplayName());
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified centrality measure type is not instantiatable: " + centralityMeasureType.getDisplayName());
				}
				else {
					CentralityAlgorithm defaultInstance = centralityAlgorithmFactory.getInstance(centralityMeasureType, new HashMap<String, String>());
					return Response.ok(requestHandler.writeParameters(defaultInstance.getParameters())).build();
				}
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	    
	    /**
	     * Returns the default parameters of a CentralitySimulation.
	     * 
	     * @param simulationTypeStr A simulation type corresponding to a CentralitySimulation.
	     * @return A parameter xml. Or an error xml.
	     */
	    @GET
	    @Path("centralitysimulations/{SimulationType}/parameters/default")
	    @Produces(MediaType.TEXT_XML)
	    @ApiResponses(value = {
	    		@ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized")
	    })
		@ApiOperation(tags = {"defaults"}, value = "Return Default CentralitySim Params",
			notes = "Returns the default parameters of a centrality simulation.")
	    public Response getSimulationDefaultParams(
	    		@PathParam("SimulationType") String simulationTypeStr)
	    {
	    	try {
	    		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
	    		CentralitySimulationType simulationType;
	    		try {
	    			simulationType = CentralitySimulationType.valueOf(simulationTypeStr);
	    		}
		    	catch (Exception e) {
		    		requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified simulation type does not exist.");
		    	}
				if(!centralitySimulationFactory.isInstantiatable(simulationType)) {
					requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified simulation type is not instantiatable: " + simulationType.getDisplayName());
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified simulation type is not instantiatable: " + simulationType.getDisplayName());
				}
				else {
					CentralitySimulation defaultInstance = centralitySimulationFactory.getInstance(simulationType, new HashMap<String, String>());
					return Response.ok(requestHandler.writeParameters(defaultInstance.getParameters())).build();
				}
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	
		/**
		 * Returns the default parameters of a benchmark.
		 * 
		 * @param graphCreationTypeStr
		 *            A graph creation type corresponding to an ocd benchmark.
		 * @return A parameter xml. Or an error xml.
		 */
		@GET
		@Path("benchmarks/{GraphCreationType}/parameters/default")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"defaults"}, value = "Return default benchmark Params", notes = "Returns the default parameters of a benchmark.")
		public Response getBenchmarkDefaultParams(@PathParam("GraphCreationType") String graphCreationTypeStr) {
			try {
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				GraphCreationType creationType;
				try {
					creationType = GraphCreationType.valueOf(graphCreationTypeStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified graph creation type does not exist.");
				}
				if (!benchmarkFactory.isInstantiatable(creationType)) {
					requestHandler.log(Level.WARNING, "user: " + username + ", "
							+ "Specified graph creation type is not instantiatable: " + creationType.name());
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified graph creation type is not instantiatable: " + creationType.name());
				}
				if (creationType.correspondsGroundTruthBenchmark()) {
					GroundTruthBenchmark defaultInstance = (GroundTruthBenchmark) benchmarkFactory
							.getInstance(creationType, new HashMap<String, String>());
					return Response.ok(requestHandler.writeParameters(defaultInstance.getParameters())).build();
				} else {
					throw new NotImplementedException("Specified graph creation type is not a benchmark.");
				}
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
	
		/**
		 * Returns the default parameters of a metric.
		 * 
		 * @param ocdMetricTypeStr
		 *            The name of an ocd metric type.
		 * @return A parameter xml. Or an error xml.
		 */
		@GET
		@Path("metrics/{OcdMetricType}/parameters/default")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"defaults"}, value = "Return Default metric Params", notes = "Returns the default parameters of a metric.")
		public Response getMetricDefaultParameters(@PathParam("OcdMetricType") String ocdMetricTypeStr) {
			try {
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				OcdMetricType metricType;
				try {
					metricType = OcdMetricType.valueOf(ocdMetricTypeStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified metric does not exist.");
				}
				if (!metricFactory.isInstantiatable(metricType)) {
					requestHandler.log(Level.WARNING, "user: " + username + ", "
							+ "Specified metric type is not instantiatable: " + metricType.name());
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified metric type is not instantiatable: " + metricType.name());
				}
				if (metricType.correspondsKnowledgeDrivenMeasure()) {
					KnowledgeDrivenMeasure defaultInstance = (KnowledgeDrivenMeasure) metricFactory
							.getInstance(metricType, new HashMap<String, String>());
					return Response.ok(requestHandler.writeParameters(defaultInstance.getParameters())).build();
				}
				if (metricType.correspondsStatisticalMeasure()) {
					StatisticalMeasure defaultInstance = (StatisticalMeasure) metricFactory.getInstance(metricType,
							new HashMap<String, String>());
					return Response.ok(requestHandler.writeParameters(defaultInstance.getParameters())).build();
				} else {
					throw new NotImplementedException("Metric type is not properly registered.");
				}
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
	    
		//////////////////////////////////////////////////////////////////////////
		//////////// ENUM LISTINGS
		//////////////////////////////////////////////////////////////////////////
		
	    /**
		 * Returns all cover creation type names.
		 * 
		 * @return The types in a names xml. Or an error xml.
		 */
		@GET
		@Path("covers/creationtypes")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"possible_types"}, value = "Return Possible Covers", notes = "Returns the names of all possible ocd algorithms to run.")
		public Response getCoverCreationMethodNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(CoverCreationType.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
	
		/**
		 * Returns compatible graph types, e.g. directed, of an algorithm. 
		 * 
		 * @param coverCreationTypeStr
		 *            A cover creation type corresponding to an ocd algorithm.
		 * @return A graph type xml. Or an error xml.
		 */
		@GET
		@Path("algorithms/{CoverCreationType}/graphTypes")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"possible_types"}, value = "Return Covers Compatible Graph Types", notes = "Returns the graph types compatible for the specified cover type.")
		public Response getAlgorithmCompatibleGraphTypes(@PathParam("CoverCreationType") String coverCreationTypeStr) {
			try {
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				CoverCreationType creationType;
				try {
					creationType = CoverCreationType.valueOf(coverCreationTypeStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified cover creation type does not exist.");
				}
				if (!algorithmFactory.isInstantiatable(creationType)) {
					requestHandler.log(Level.WARNING, "user: " + username + ", "
							+ "Specified cover creation type is not instantiatable: " + creationType.name());
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified cover creation type is not instantiatable: " + creationType.name());
				} else {
					OcdAlgorithm defaultInstance = algorithmFactory.getInstance(creationType,
							new HashMap<String, String>());
					return Response.ok(requestHandler.writeSpecificEnumNames(defaultInstance.compatibleGraphTypes())).build();
				}
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
		
		/**
		 * Returns all algorithm type names.
		 * 
		 * @return The types in a names xml. Or an error xml.
		 */
		@GET
		@Path("algorithms")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"names"}, value = "Algorithms information", notes = "Returns all algorithm type names.")
		public Response getAlgorithmNames() {
			try {
				return Response.ok(requestHandler.writeAlgorithmNames()).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
		
		/**
	     * Returns all centrality measure names.
	     * 
	     * @return The centrality measures names in an xml. Or an error xml.
	     */
	    @GET
	    @Path("centralities")
	    @Produces(MediaType.TEXT_XML)
	    @ApiResponses(value = {
	    		@ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized")
	    })
		@ApiOperation(tags = {"names"}, value = "Get Possible Centralities",
			notes = "Returns the names of all possible centrality measures to run.")
	    public Response getCentralityNames() {
	    	try {
				return Response.ok(requestHandler.writeCentralityMeasureNames()).build();
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	    
	    /**
		 * Returns compatible graph types, e.g. directed, of a centrality measure.
		 * 
		 * @param centralityMeasureTypeStr
		 *            A centrality creation type corresponding to a centrality calculation algorithm.
		 * @return A graph type xml. Or an error xml.
		 */
		@GET
		@Path("centralities/{CentralityMeasureType}/graphTypes")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"possible_types"}, value = "Return Centrality's Graph Types", notes = "Returns the possible graph types for a specified centrality measure type.")
		public Response getCentralityCompatibleGraphTypes(@PathParam("CentralityMeasureType") String centralityMeasureTypeStr) {
			try {
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				CentralityMeasureType centralityType;
				try {
					centralityType = CentralityMeasureType.valueOf(centralityMeasureTypeStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified cover creation type does not exist.");
				}
				if (!centralityAlgorithmFactory.isInstantiatable(centralityType)) {
					requestHandler.log(Level.WARNING, "user: " + username + ", "
							+ "Specified cover creation type is not instantiatable: " + centralityType.name());
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified cover creation type is not instantiatable: " + centralityType.name());
				} else {
					CentralityAlgorithm defaultInstance = centralityAlgorithmFactory.getInstance(centralityType,
							new HashMap<String, String>());
					return Response.ok(requestHandler.writeSpecificEnumNames(defaultInstance.compatibleGraphTypes())).build();
				}
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
	    
	    /**
	     * Returns all CentralitySimulation names.
	     * 
	     * @return The simulation names in an xml. Or an error xml.
	     */
	    @GET
	    @Path("centralitysimulations")
	    @Produces(MediaType.TEXT_XML)
	    @ApiResponses(value = {
	    		@ApiResponse(code = 200, message = "Success"),
	    		@ApiResponse(code = 401, message = "Unauthorized")
	    })
		@ApiOperation(tags = {"names"}, value = "Get Possible Centrality Simulations",
			notes = "Returns the names of all possible centrality simulations to run.")
	    public Response getSimulationNames() {
	    	try {
				return Response.ok(requestHandler.writeCentralitySimulationNames()).build();
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.SEVERE, "", e);
	    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
	    	}
	    }
	    
	    /**
		 * Returns compatible graph types of a centrality simulation.
		 * 
		 * @param centralitySimulationTypeStr
		 *            A centrality creation type corresponding to a centrality simulation algorithm.
		 * @return A graph type xml. Or an error xml.
		 */
		@GET
		@Path("centralitysimulations/{CentralitySimulationType}/graphTypes")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"possible_types"}, value = "Get CentralitySims Graph Types", notes = "Returns the possible graph types for a specified centrality simulation type.")
		public Response getCentralitySimulationCompatibleGraphTypes(@PathParam("CentralitySimulationType") String centralitySimulationTypeStr) {
			try {
				String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
				CentralitySimulationType simulationType;
				try {
					simulationType = CentralitySimulationType.valueOf(centralitySimulationTypeStr);
				} catch (Exception e) {
					requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified cover creation type does not exist.");
				}
				if (!centralitySimulationFactory.isInstantiatable(simulationType)) {
					requestHandler.log(Level.WARNING, "user: " + username + ", "
							+ "Specified cover creation type is not instantiatable: " + simulationType.name());
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Specified cover creation type is not instantiatable: " + simulationType.name());
				} else {
					CentralitySimulation defaultInstance = centralitySimulationFactory.getInstance(simulationType,
							new HashMap<String, String>());
					return Response.ok(requestHandler.writeSpecificEnumNames(defaultInstance.compatibleGraphTypes())).build();
				}
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
	
		/**
		 * Returns all ground truth benchmark type names.
		 * 
		 * @return The types in a names xml. Or an error xml.
		 */
		@GET
		@Path("benchmarks")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"names"}, value = "Benchmarks information", notes = "Returns all ground truth benchmark type names.")
		public Response getGroundTruthBenchmarkNames() {
			try {
				return Response.ok(requestHandler.writeGroundTruthBenchmarkNames()).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
	
		/**
		 * Returns all graph creation type names.
		 * 
		 * @return The types in a names xml. Or an error xml.
		 */
		@GET
		@Path("graphs/creationtypes")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"names"}, value = "Get Graph Creation Types", notes = "Returns all graph creation type names.")
		public Response getGraphCreationMethodNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(GraphCreationType.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
	
		/**
		 * Returns all graph input format names.
		 * 
		 * @return The formats in a names xml. Or an error xml.
		 */
		@GET
		@Path("graphs/formats/input")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"format_names"}, value = "Get Graph Input Formats", notes = "Returns all graph input format names.")
		public Response getGraphInputFormatNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(GraphInputFormat.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
	
		/**
		 * Returns all graph output format names.
		 * 
		 * @return The formats in a names xml. Or an error xml.
		 */
		@GET
		@Path("graphs/formats/output")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"format_names"}, value = "Get Graph Output Formats", notes = "Returns all graph output format names.")
		public Response getGraphOutputFormatNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(GraphOutputFormat.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
	
		/**
		 * Returns all graph properties.
		 * 
		 * @return The property types in a names xml. Or an error xml.
		 */
		@GET
		@Path("graphs/properties")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"properties"}, value = "Get Graph Property Information", notes = "Returns all graph property names.")
		public Response getProperties() {
			try {
				return Response.ok(requestHandler.writeEnumNames(GraphProperty.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
	
		}
	
		/**
		 * Returns all cover output format names.
		 * 
		 * @return The formats in a names xml. Or an error xml.
		 */
		@GET
		@Path("covers/formats/output")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"format_names"}, value = "Get Cover Output Types", notes = "Returns all cover output type names.")
		public Response getCoverOutputFormatNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(CoverOutputFormat.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
	
		/**
		 * Returns all cover input format names.
		 * 
		 * @return The formats in a names xml. Or an error xml.
		 */
		@GET
		@Path("covers/formats/input")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"format_names"}, value = "Get Cover Input Types", notes = "Returns all cover input type names.")
		public Response getCoverInputFormatNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(CoverInputFormat.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
		
		/**
		 * Returns all centrality creation type names.
		 * 
		 * @return The types in a names xml. Or an error xml.
		 */
		@GET
		@Path("centralities/creationtypes")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"possible_types"}, value = "Get Centrality's Creation Types", notes = "Returns all centrality creation type names.")
		public Response getCentralityCreationMethodNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(CentralityCreationType.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
		
		/**
		 * Returns all centrality output format names.
		 * 
		 * @return The formats in a names xml. Or an error xml.
		 */
		@GET
		@Path("centralities/formats/output")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"format_names"}, value = "Get Centrality Output Formats", notes = "Returns all centrality output format names.")
		public Response getCentralityOutputFormatNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(CentralityOutputFormat.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
	
		/**
		 * Returns all centrality input format names.
		 * 
		 * @return The formats in a names xml. Or an error xml.
		 */
		@GET
		@Path("centralities/formats/input")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"format_names"}, value = "Get Centrality Input Formats", notes = "Returns all centrality input format names.")
		public Response getCentralityInputFormatNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(CentralityInputFormat.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
		
		/**
		 * Returns all statistical measure type names.
		 * 
		 * @return The types in a names xml. Or an error xml.
		 */
		@GET
		@Path("metrics/statistical")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"names"}, value = "Statistical Measure Information", notes = "Returns all statistical measure type names.")
		public Response getStatisticalMeasureNames() {
			try {
				return Response.ok(requestHandler.writeStatisticalMeasureNames()).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
	
		/**
		 * Returns all knowledge-driven measure type names.
		 * 
		 * @return The types in a names xml. Or an error xml.
		 */
		@GET
		@Path("metrics/knowledgedriven")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"names"}, value = "Knowledge Driven Measure Information", notes = "Returns all knowledge-driven measure type names.")
		public Response getKnowledgeDrivenMeasureNames() {
			try {
				return Response.ok(requestHandler.writeKnowledgeDrivenMeasureNames()).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
	
		/**
		 * Returns all metric type names.
		 * 
		 * @return The types in a names xml. Or an error xml.
		 */
		@GET
		@Path("metrics")
		@Produces(MediaType.TEXT_XML)
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(tags = {"names"}, value = "Metrics information", notes = "Returns all metric type names.")
		public Response getMetricNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(OcdMetricType.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
		}
		
		//////////////////////////////////////////////////////////////////////////
		//////////// VIEWER-SPECIFIC ENUM LISTINGS
		//////////////////////////////////////////////////////////////////////////
		
		/**
		 * Returns all graph layout type names.
		 * 
		 * @return The types in a names xml. Or an error xml.
		 */
		@GET
		@Path("graphs/layout/names")
		@ApiOperation(tags = {"possible_types"}, value = "Get Graph Layout Types", notes = "Returns all possible graph layout types.")
		public Response getLayoutTypeNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(GraphLayoutType.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL,
						"Internal system error.");
			}
		}

		/**
		 * Returns all cover painting type names.
		 * 
		 * @return The types in a names xml. Or an error xml.
		 */
		@GET
		@Path("graphs/painting/names")
		@ApiOperation(tags = {"possible_types"}, value = "Get Cover Painting Types", notes = "Returns all cover painting types.")
		public Response getPaintingTypeNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(CoverPaintingType.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL,
						"Internal system error.");
			}
		}

		/**
		 * Returns all visual output format names.
		 * 
		 * @return The formats in a names xml. Or an error xml.
		 */
		@GET
		@Path("visualization/formats/output/names")
		@ApiOperation(tags = {"format_names"}, value = "Get Graph Painting Names", notes = "Returns all graph painting names.")
		public Response getVisualizationFormatNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(VisualOutputFormat.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL,
						"Internal system error.");
			}
		}

		/**
		 * Returns all centrality visualization type names.
		 * 
		 * @return The visualization types in a names xml. Or an error xml.
		 */
		@GET
		@Path("visualization/centralityVisualizationTypes/names")
		@ApiOperation(tags = {"names"}, value = "Get Centrality Visualization Names", notes = "Returns all centralitiy visualization type names.")
		public Response getCentralityVisualizationTypeNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(CentralityVisualizationType.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL,
						"Internal system error.");
			}
		}
		
		//////////////////////////////////////////////////////////////////////////
		//////////// Simulations
		//////////////////////////////////////////////////////////////////////////
		
		/**
		 * Gets all the simulations performed by the user
		 *
		 * @param parameters the parameters
		 * @return HttpResponse with the returnString
		 */
		@GET
		@Path("/simulation/")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		@ApiOperation(tags = {"show"}, value = "Get Simulations Info", notes = "Gets all the simulations performed by the user")
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		public Response getSimulations(SimulationSeriesParameters parameters) {
	
			List<SimulationSeries> series = new ArrayList<>();
			String userId = getUserId();
			try {
	
				series = entityHandler.getSimulationSeriesByUser(userId);
	
			} catch (Exception e) {
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, "fail to get simulation series. " + e.toString());
				//L2pLogger.logEvent(this, Event.SERVICE_ERROR, "fail to get simulation series. " + e.toString());
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("fail to get simulation series").build();
			}
	
			return Response.ok().entity(series).build();
	
		}
	
		@GET
		@Path("/simulation/meta")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		@ApiOperation(tags = {"show"}, value = "Get Simulations Meta Info", notes = "Gets meta information of all the simulations performed by the user")
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		public Response getSimulationMeta(@DefaultValue("0") @QueryParam("firstIndex") int firstIndex,
				@DefaultValue("0") @QueryParam("length") int length,
				@DefaultValue("0") @QueryParam("graphId") long graphId,
				SimulationSeriesParameters parameters) {

			if (parameters == null) {
				parameters = new SimulationSeriesParameters();
			}
	
			List<SimulationSeries> simulations = new ArrayList<>();
			try {
				if (firstIndex < 0 || length <= 0) {
					simulations = entityHandler.getSimulationSeriesByUser(getUserId());
				} else {
					if (graphId <= 0) {
						simulations = entityHandler.getSimulationSeriesByUser(getUserId(), firstIndex, length);
					} else {
						simulations = entityHandler.getSimulationSeriesByUser(getUserId(), graphId, firstIndex, length);
					}
				}
			} catch (Exception e) {
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, "fail to get simulation series. " + e.toString());
				//L2pLogger.logEvent(this, Event.SERVICE_ERROR, "fail to get simulation series. " + e.toString());
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("fail to get simulation series").build();
			}
	
			if (simulations == null || simulations.size() < 1)
				return Response.status(Status.BAD_REQUEST).entity("No simulation series found").build();
	
			List<SimulationSeriesMetaData> metaList = new ArrayList<>(simulations.size());
			try {
				for (SimulationSeries simulation : simulations) {
					try {
						SimulationSeriesMetaData metaData = simulation.getMetaData();
						metaData.setGraphName(entityHandler.getGraph(getUserName(), simulation.getParameters().getGraphId()).getName());
						metaList.add(metaData);
					} catch (Exception e) {
	
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("fail parse meta data").build();
			}
			return Response.ok().entity(metaList).build();
		}
	
		/**
		 * Gets the results of a performed simulation series on a network
		 *
		 * @param seriesId the id of the series
		 * @return HttpResponse with the returnString
		 */
		@GET
		@Path("/simulation/{seriesId}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiOperation(tags = {"show"}, value = "Get Simulation Results", notes = "Gets the results of a performed simulation")
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		public Response getSimulation(@PathParam("seriesId") long seriesId) {
	
			String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
			SimulationSeries series = null;
	
			try {
				series = entityHandler.getSimulationSeries(seriesId);
	
				if (series == null)
					return Response.status(Status.BAD_REQUEST).entity("no simulation with id " + seriesId + " found")
							.build();
	
				if (!series.isEvaluated()) {
					series.evaluate();
				}
	
			} catch (Exception e) {
				logger.log(Level.WARNING, "user: " + username, e);
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("internal error").build();
			}
	
			return Response.ok().entity(series).build();
		}
	
		/**
		 * Gets the results of a performed simulation series on a network
		 *
		 * @param seriesId the id of the series
		 * @return HttpResponse with the returnString
		 */
		@GET
		@Path("/simulation/{seriesId}/table")
		@Produces(MediaType.TEXT_PLAIN)
		@ApiOperation(tags = {"export"}, value = "Get Simulation Result Table", notes = "Gets the results of a performed simulation in a table")
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		public Response getSimulationTable(@PathParam("seriesId") long seriesId) {
	
			String username = getUserName();
			SimulationSeries series = null;
	
			try {
				series = entityHandler.getSimulationSeries(seriesId);
	
				if (series == null)
					return Response.status(Status.BAD_REQUEST).entity("no simulation with id " + seriesId + " found")
							.build();
	
				series.evaluate();
	
			} catch (Exception e) {
				logger.log(Level.WARNING, "user: " + username, e);
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("internal error").build();
			}
	
			String responseString = "";
			try {
				responseString = series.toTable().print();
			} catch (Exception e) {
				e.printStackTrace();
			}
	
			return Response.ok().entity(responseString).build();
		}
	
		/**
		 * Gets the parameters of a simulation
		 *
		 * @param seriesId the id of the series
		 * @return HttpResponse with the returnString
		 */
		@GET
		@Path("/simulation/{seriesId}/parameters")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiOperation(tags = {"show"}, value = "Get Simulation Parameters", notes = "Gets the parameters of a simulation")
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		public Response getSimulationParameters(@PathParam("seriesId") long seriesId) {
	
			SimulationSeriesParameters parameters = null;
			try {
				parameters = entityHandler.getSimulationParameters(seriesId);
			} catch (Exception e) {
				logger.log(Level.WARNING, "fail to get simulation series parameters");
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("fail to get simulation series parameters")
						.build();
			}
	
			return Response.ok().entity(parameters).build();
		}
	
		/**
		 * Deletes a performed simulation series on a network
		 *
		 * @param seriesId the id of the series
		 * @return HttpResponse with the returnString
		 */
		@DELETE
		@Path("/simulation/{seriesId}")
		@Produces(MediaType.TEXT_PLAIN)
		@ApiOperation(tags = {"delete"}, value = "Delete Simulation", notes = "Deletes a performed simulation")
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		public Response deleteSimulation(@PathParam("seriesId") long seriesId) {
	
			try {
				entityHandler.deleteSeries(seriesId);
			} catch (Exception e) {
				return Response.serverError().entity(e.getMessage()).build();
			}
	
			return Response.ok("done").build();
	
		}
	
		/**
		 * Starts the simulation of a cooperation and defection game simulation
		 * 
		 * @param parameters the parameters
		 * 
		 * @return HttpResponse with the returnString
		 */
		@POST
		@Path("/simulation")
		@Produces(MediaType.TEXT_PLAIN)
		@Consumes(MediaType.APPLICATION_JSON)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		@ApiOperation(tags = {"execution"}, value = "Run Simulation", notes = " Starts the simulation of a evolutionary cooperation and defection game.")
		public Response postSimulation(SimulationSeriesParameters parameters) {
	
			String username = getUserName();
	
			long graphId = parameters.getGraphId();
			CustomGraph network = entityHandler.getGraph(username, graphId);
			if (network == null)
				return Response.status(Status.BAD_REQUEST).entity("graph not found").build();
	
			if (parameters.getPayoffCC() == 0.0 && parameters.getPayoffCD() == 0.0 && parameters.getPayoffDC() == 0.0
					&& parameters.getPayoffDD() == 0.0) {
	
				if (parameters.getBenefit() == 0.0 && parameters.getCost() == 0.0) {
					return Response.status(Status.BAD_REQUEST).entity("invalid payoff").build();
				}
			}
	
			if (parameters.getDynamic() == null || parameters.getDynamic() == DynamicType.UNKNOWN) {
				return Response.status(Status.BAD_REQUEST).entity("dynamic does not exist").build();
			}
			
			//@MaxKissgen Own if statement here to check for emptiness of condition. Otherwise ServiceTest will fail as there's going to be an internal server error resulting from an empty condition.
			if (parameters.getCondition() == null || parameters.getCondition() == ConditionType.UNKNOWN) {
				return Response.status(Status.BAD_REQUEST).entity("condition does not exist").build();
			}
	
			SimulationSeries series = null;
			try {
				// Simulation
				SimulationBuilder simulationBuilder = new SimulationBuilder();
				simulationBuilder.setParameters(parameters);
				simulationBuilder.setNetwork(network);
				series = simulationBuilder.simulate();
	
			} catch (Exception e) {
				logger.log(Level.WARNING, "user: " + username, e);
				e.printStackTrace();
				return Response.serverError().entity("simulation could not be carried out\n" + e.getMessage()).build();
			}
	
			if(series.getSimulationDatasets() == null || !(series.getSimulationDatasets().size() == parameters.getIterations())) 
				return Response.serverError().entity("something went wrong").build();
	
			long result;
			try {
				result = entityHandler.store(series, getUserId());
	
			} catch (Exception e) {
				e.printStackTrace();
				return Response.serverError().entity("simulation not stored").build();
			}
	
			return Response.ok().entity("simulation done " + result).build();
		}
	
		///////////////////// Group ///////////////////////////////
	
		@PUT
		@Path("/simulation/group")
		@Produces(MediaType.TEXT_PLAIN)
		@Consumes(MediaType.APPLICATION_JSON)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		@ApiOperation(tags = {"execution"}, value = "Run simulation group", notes = " Starts a simulation group of evolutionary cooperation or defection games ")
		public Response putSimulationGroup(@DefaultValue("") @QueryParam("name") String name,
				List<Integer> seriesIds) {
	
			List<SimulationSeries> series = new ArrayList<>(seriesIds.size());			
			try {
				for(Integer id: seriesIds) {
					series.add(entityHandler.getSimulationSeries(id));				
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "user: " + getUserName(), e);
				e.printStackTrace();
				return Response.serverError().entity("Invalid simulation series \n" + e.getMessage()).build();
			}
	
			SimulationSeriesGroup group = null;			
			try {
	
				group = new SimulationSeriesGroup(series);
				group.setName(name);
				entityHandler.store(group, getUserId());
	
	
			} catch (Exception e) {
				e.printStackTrace();
				return Response.serverError().entity("fail store series group").build();
			}
			return Response.ok().entity("done").build();
		}
	
		@GET
		@Path("/simulation/group/meta")
		@Produces(MediaType.APPLICATION_JSON)
		@Consumes(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		@ApiOperation(tags = {"show"}, value = "Get Simulation Group Meta", notes = "Returns the meta information for a performed group of simulations")
		public Response getSimulationGroups(@DefaultValue("0") @QueryParam("firstIndex") int firstIndex,
				@DefaultValue("0") @QueryParam("length") int length) {
	
			List<SimulationSeriesGroup> simulations = new ArrayList<>();
			try {
				if (firstIndex < 0 || length <= 0) {
					simulations = entityHandler.getSimulationSeriesGroups(getUserId());
				} else {
					simulations = entityHandler.getSimulationSeriesGroups(getUserId(), firstIndex, length);
				}
			} catch (Exception e) {
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, "fail to get simulation series. " + e.toString());
				//L2pLogger.logEvent(this, Event.SERVICE_ERROR, "fail to get simulation series. " + e.toString());
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("fail to get simulation series").build();
			}
	
			if (simulations == null || simulations.size() < 1)
				return Response.status(Status.BAD_REQUEST).entity("No simulation series found").build();
	
			List<SimulationSeriesGroupMetaData> metaList = new ArrayList<>(simulations.size());
			try {
				for (SimulationSeriesGroup simulation : simulations) {
					SimulationSeriesGroupMetaData metaData = simulation.getMetaData();
					metaList.add(metaData);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("fail parse meta data").build();
			}
			return Response.ok().entity(metaList).build();
		}
	
		/**
		 * Gets the results of a performed simulation series group on a network
		 *
		 * @param groupId the id of the group
		 * @return HttpResponse with the returnString
		 */
		@GET
		@Path("/simulation/group/{groupId}/table")
		@Produces(MediaType.TEXT_PLAIN)
		@ApiOperation(tags = {"export"}, value = "Get Simulation Group Result Table", notes = "Gets the results of a performed simulation group in a table")
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		public Response getSimulationGroupTable(@PathParam("groupId") long groupId) {
	
			String username = getUserName();
			SimulationSeriesGroup series = null;
	
			try {
				series = entityHandler.getSimulationSeriesGroup(groupId);
	
				if (series == null)
					return Response.status(Status.BAD_REQUEST).entity("no simulation with id " + groupId + " found")
							.build();
	
				series.evaluate();
	
			} catch (Exception e) {
				logger.log(Level.WARNING, "user: " + username, e);
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("internal error").build();
			}
	
			String responseString = "";
			try {
				responseString = series.toTable().print();
			} catch (Exception e) {
				e.printStackTrace();
			}
	
			return Response.ok().entity(responseString).build();
		}
	
		@GET
		@Path("/simulation/group/{groupId}")
		@Produces(MediaType.APPLICATION_JSON)
		@Consumes(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		@ApiOperation(tags = {"show"}, value = "Get a Simulation Group", notes = "Returns a performed simulation group")
		public Response getSimulationGroup(@PathParam("groupId") long groupId) {
	
			SimulationSeriesGroup simulation = null;
			try {				
				simulation = entityHandler.getSimulationSeriesGroup(groupId);
				if(!simulation.isEvaluated())
					simulation.evaluate();
	
			} catch (Exception e) {
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, "fail to get simulation series. " + e.toString());
				//L2pLogger.logEvent(this, Event.SERVICE_ERROR, "fail to get simulation series. " + e.toString());
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("fail to get simulation series").build();
			}
	
			if (simulation == null)
				return Response.status(Status.BAD_REQUEST).entity("simulation series group not found").build();
	
			return Response.ok().entity(simulation).build();
		}
	
		/**
		 * Deletes a simulation series group
		 *
		 * @param groupId the id of the group
		 * @return HttpResponse with the returnString
		 */
		@DELETE
		@Path("/simulation/group/{groupId}")
		@Produces(MediaType.TEXT_PLAIN)
		@ApiOperation(tags = {"delete"}, value = "Delete Simulation Group", notes = "Deletes a performed simulation group")
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		public Response deleteSimulationSeriesGroup(@PathParam("groupId") long groupId) {
	
			try {
				entityHandler.deleteGroup(groupId);
			} catch (Exception e) {
				e.printStackTrace();
				return Response.serverError().entity(e.getMessage()).build();
			}			
			return Response.ok("done").build();
		}
	
	
		///////////////////// Mapping ///////////////////////////////
	
		@GET
		@Path("/simulation/group/{groupId}/mapping/")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiOperation(tags = {"show"}, value = "Get Simulation Results", notes = "Gets the results of a performed simulation")
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		public Response getSimulationGroupMapping(@PathParam("groupId") long groupId) {
	
			String username = getUserName();
			SimulationSeriesGroup simulationGroup = null;
			SimulationSeriesSetMapping mapping;
	
			try {
				simulationGroup = entityHandler.getSimulationSeriesGroup(groupId);

				if (simulationGroup == null)
					return Response.status(Status.BAD_REQUEST).entity("no simulation with id " + groupId + " found")
							.build();

				if (!simulationGroup.isEvaluated())
					simulationGroup.evaluate();
								
				MappingFactory factory = new MappingFactory();
				mapping = factory.build(simulationGroup.getSimulationSeries(), simulationGroup.getName());
				for(SimulationSeries sim: mapping.getSimulation()) {
					sim.setNetwork(entityHandler.getGraph(getUserName(), sim.getParameters().getGraphId()));
				}
				
				if (!mapping.isEvaluated())
					mapping.correlate();

				
			} catch (Exception e) {
				logger.log(Level.WARNING, "user: " + username, e);
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("internal error").build();
			}
	
			if(mapping == null)
				return Response.serverError().entity("no mapping found").build();
	
			return Response.ok().entity(mapping).build();
		}
		
		@PUT
		@Path("/simulation/group/mapping/")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		@ApiOperation(tags = {"show"}, value = "Get Simulation Groups Results", notes = "Gets the results of multiple simulation groups")
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		public Response getSimulationGroupsMapping(List<Long> groupIds) {

			String username = getUserName();
			List<SimulationSeriesGroup> groups = new ArrayList<>(groupIds.size());
			SimulationGroupSetMapping mapping = null;

			try {
				for (Long groupId : groupIds) {
					try {
						SimulationSeriesGroup group = entityHandler.getSimulationSeriesGroup(groupId);
						groups.add(group);
					
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

				try {

					for (int i = 0; i < groups.size(); i++) {
						for (int j = 0; j < groups.get(i).getSimulationSeries().size(); j++) {
							groups.get(i).getSimulationSeries().get(j).setNetwork(entityHandler.getGraph(getUserName(),
									groups.get(i).getSimulationSeries().get(j).getParameters().getGraphId()));
							groups.get(i).getSimulationSeries().get(j).evaluate();
						}
						groups.get(i).evaluate();
					}
					MappingFactory factory = new MappingFactory();
					mapping = factory.buildGroupMapping(groups, "Evaluation");
					mapping.correlate(username);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} catch (Exception e) {
				logger.log(Level.WARNING, "user: " + username, e);
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("internal error").build();
			}

			return Response.ok().entity(mapping).build();
		}

		////////////// Information //////////////////
	
		/**
		 * Returns all available dynamics
		 *
		 * @return HttpResponse with the returnString
		 */
		@GET
		@Path("/simulation/dynamics")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiOperation(tags = {"possible_types"}, value = "Get Dynamic Types", notes = "Get all available types of evolutionary dynamics")
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		public Response getDynamics() {
	
			return Response.status(Status.OK).entity(DynamicType.values()).build();
	
		}
	
		/**
		 * Returns all available games
		 *
		 * @return HttpResponse with the returnString
		 */
		@GET
		@Path("/simulation/games")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiOperation(tags = {"possible_types"}, value = "Get Game Types", notes = "Get all available types of (simulation) games")
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		public Response getGames() {
	
			return Response.status(Status.OK).entity(GameType.values()).build();
	
		}

		/**
		 * Returns all available break condition
		 * 
		 * @return HttpResponse with the returnString
		 */
		@GET
		@Path("/simulation/conditions")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiOperation(tags = {"special"}, value = "Get Condition", notes = "Get all available break conditions")
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
		public Response getBreakConditions() {

			return Response.status(Status.OK).entity(ConditionType.values()).build();

		}

	}
	//////////////////////////////////////////////////////////////////
	///////// RMI Methods
	//////////////////////////////////////////////////////////////////

	//////////// GRAPH ////////////

	/**
	 * Transforms the stored CustomGraph into a HashMap. The HashMap include the
	 * graph as adjacency list.
	 * 
	 * This method is intended to be used by other las2peer services for remote
	 * method invocation. It returns only default types and classes.
	 * 
	 * 
	 * @param graphId
	 *            Id of the requested stored graph
	 * @return HashMap
	 * 
	 */
	public Map<String, Object> getGraphById(long graphId) {

		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
		CustomGraph graph;
		try {
			graph = entityHandler.getGraph(username, graphId);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		Integer nodeCount = graph.nodeCount();
		Integer edgeCount = graph.edgeCount();
		Boolean directed = graph.isDirected();
		Boolean weighted = graph.isWeighted();
		String name = graph.getName();

		InvocationHandler invocationHandler = new InvocationHandler();
		List<List<Integer>> adjList = invocationHandler.getAdjList(graph);

		Map<String, Object> graphData = new HashMap<String, Object>();
		graphData.put("nodes", nodeCount);
		graphData.put("edges", edgeCount);
		graphData.put("directed", directed);
		graphData.put("weighted", weighted);
		graphData.put("name", name);
		graphData.put("graph", adjList);

		logger.log(Level.INFO, "RMI requested a graph: " + graphId);
		return graphData;
	}

	/**
	 * Get a List of all graph indices of a user
	 *
	 * @return List
	 * @throws AgentNotRegisteredException if the agent was not registered
	 */
	public List<Long> getGraphIds() throws AgentNotRegisteredException {

		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
		List<Long> graphIdList = new ArrayList<Long>();

		List<CustomGraph> graphList = entityHandler.getGraphs(username);
		for (int i = 0, si = graphList.size(); i < si; i++) {
			graphIdList.add(graphList.get(i).getId());
		}

		logger.log(Level.INFO, "RMI requested graph Ids");
		return graphIdList;
	}

	//////////// COVER ////////////

	/**
	 * Get the community lists representing the community structure.
	 * 
	 * This method is intended to be used by other las2peer services for remote
	 * method invocation. It returns only default types and classes.
	 * 
	 * @param graphId
	 *            Index of the requested graph
	 * @param coverId
	 *            Index of the requested community cover
	 * 
	 * @return HashMap including the community members lists. The outer list has
	 *         an entry for every community of the cover. The inner list
	 *         contains the indices of the member nodes.
	 * 
	 */
	public Map<String, Object> getCoverById(long graphId, long coverId) {

		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();
		Cover cover;
		try {
			cover = entityHandler.getCover(username, graphId, coverId);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		int communityCount = cover.communityCount();
		String algorithm = cover.getCreationMethod().getType().toString();

		InvocationHandler invocationHandler = new InvocationHandler();
		List<List<Integer>> communityMemberList = invocationHandler.getCommunityMemberList(cover);

		Map<String, Object> coverData = new HashMap<String, Object>();
		coverData.put("size", communityCount);
		coverData.put("algorithm", algorithm);
		coverData.put("graphId", graphId);
		coverData.put("coverId", coverId);
		coverData.put("cover", communityMemberList);

		return coverData;
	}

	/**
	 * List of cover indices that are available for a given graph id
	 * 
	 * This method is intended to be used by other las2peer services for remote
	 * method invocation. It returns only default types and classes.
	 * 
	 * @param graphId
	 *            Index of the requested graph
	 * 
	 * @return list containing cover indices.
	 * 
	 */
	public List<Long> getCoverIdsByGraphId(long graphId) {

		String username = ((UserAgent) Context.getCurrent().getMainAgent()).getLoginName();

		List<Cover> covers = entityHandler.getCovers(username, graphId);
		int size = covers.size();

		List<Long> coverIds = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			coverIds.add(covers.get(i).getId());
		}

		return coverIds;
	}

}