package i5.las2peer.services.ocd;

import i5.las2peer.api.Context;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.p2p.AgentNotKnownException;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.Agent;
import i5.las2peer.security.UserAgent;

import i5.las2peer.services.ocd.adapters.coverInput.CoverInputFormat;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputFormat;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputFormat;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputFormat;
import i5.las2peer.services.ocd.algorithms.ContentBasedWeightingAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithmFactory;
import i5.las2peer.services.ocd.benchmarks.GroundTruthBenchmark;
import i5.las2peer.services.ocd.benchmarks.OcdBenchmarkFactory;
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
import i5.las2peer.services.ocd.metrics.ExecutionTime;
import i5.las2peer.services.ocd.metrics.KnowledgeDrivenMeasure;
import i5.las2peer.services.ocd.metrics.OcdMetricFactory;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricLogId;
import i5.las2peer.services.ocd.metrics.OcdMetricType;
import i5.las2peer.services.ocd.metrics.StatisticalMeasure;
import i5.las2peer.services.ocd.utils.EntityHandler;
import i5.las2peer.services.ocd.utils.Error;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import i5.las2peer.services.ocd.utils.InvocationHandler;
import i5.las2peer.services.ocd.utils.OcdRequestHandler;
import i5.las2peer.services.ocd.utils.ThreadHandler;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.NotImplementedException;
import org.la4j.matrix.sparse.CCSMatrix;

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

@ServicePath("ocd")
@Api
@SwaggerDefinition(info = @Info(title = "LAS2peer OCD Service", version = "1.0", description = "A RESTful service for overlapping community detection.", termsOfService = "sample-tos.io", contact = @Contact(name = "Sebastian Krott", email = "sebastian.krott@rwth-aachen.de"), license = @License(name = "Apache License 2", url = "http://www.apache.org/licenses/LICENSE-2.0")))
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
	}

	public Long getUserId() {
		Agent user = getContext().getMainAgent();
		return user.getId();
	}

	public String getUserName() throws AgentNotKnownException {
		UserAgent agent = (UserAgent) Context.getCurrent().getAgent(getUserId());
		return agent.getLoginName();
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
	private final static OcdRequestHandler requestHandler = new OcdRequestHandler();

	/**
	 * The entity handler used for access stored entities.
	 */
	private final static EntityHandler entityHandler = new EntityHandler();

	/**
	 * The factory used for creating benchmarks.
	 */
	private final static OcdBenchmarkFactory benchmarkFactory = new OcdBenchmarkFactory();

	/**
	 * The factory used for creating algorithms.
	 */
	private final static OcdAlgorithmFactory algorithmFactory = new OcdAlgorithmFactory();

	/**
	 * The factory used for creating metrics.
	 */
	private final static OcdMetricFactory metricFactory = new OcdMetricFactory();

	//////////////////////////////////////////////////////////////////
	///////// REST Service Methods
	//////////////////////////////////////////////////////////////////

	@Path("/")
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
		@ApiOperation(value = "User validation", notes = "Simple function to validate a user login.")
		public Response validateLogin() {
			try {
				return Response.ok(requestHandler.writeConfirmationXml()).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}
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
		@ApiOperation(value = "User validation", notes = "Imports a graph.")
		public Response createGraph(@DefaultValue("unnamed") @QueryParam("name") String nameStr,
				@DefaultValue("UNDEFINED") @QueryParam("creationType") String creationTypeStr,
				@DefaultValue("GRAPH_ML") @QueryParam("inputFormat") String graphInputFormatStr,
				@DefaultValue("FALSE") @QueryParam("doMakeUndirected") String doMakeUndirectedStr,
				@DefaultValue("2004-01-01") @QueryParam("startDate") String startDateStr,
				@DefaultValue("2004-01-20") @QueryParam("endDate") String endDateStr,
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
					if (format == GraphInputFormat.NODE_CONTENT_EDGE_LIST || format == GraphInputFormat.XML) {
						param.put("startDate", startDateStr);
						param.put("endDate", endDateStr);
						if (format == GraphInputFormat.XML) {
							param.put("indexPath", indexPathStr);
							param.put("filePath", filePathStr);
						} else {
							param.put("path", indexPathStr);
						}
					}
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
				EntityManager em = entityHandler.getEntityManager();
				EntityTransaction tx = em.getTransaction();
				try {
					tx.begin();
					em.persist(graph);
					tx.commit();
				} catch (RuntimeException e) {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
					throw e;
				}
				em.close();
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
		@ApiOperation(value = "User validation", notes = "Stores a graph step by step.")
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
		@ApiOperation(value = "User validation", notes = "Process the stored graph.")
		public Response processStoredGraph(@DefaultValue("unnamed") @QueryParam("name") String nameStr,
				@DefaultValue("UNDEFINED") @QueryParam("creationType") String creationTypeStr,
				@DefaultValue("GRAPH_ML") @QueryParam("inputFormat") String graphInputFormatStr,
				@DefaultValue("FALSE") @QueryParam("doMakeUndirected") String doMakeUndirectedStr,
				@DefaultValue("2004-01-01") @QueryParam("startDate") String startDateStr,
				@DefaultValue("2004-01-20") @QueryParam("endDate") String endDateStr,
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
					endDateStr, indexPathStr, filePathStr, contentStr.toString());
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
		@ApiOperation(value = "Manage graphs", notes = "Returns the ids or meta information of multiple graphs.")
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
		@ApiOperation(value = "", notes = "Returns a graph in a specified output format.")
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
				EntityManager em = entityHandler.getEntityManager();
				CustomGraphId id = new CustomGraphId(graphId, username);
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
					tx.commit();
				} catch (RuntimeException e) {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
					throw e;
				}
				em.close();
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
		@ApiOperation(value = "", notes = "Deletes a graph.")
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
				EntityManager em = entityHandler.getEntityManager();
				CustomGraphId id = new CustomGraphId(graphId, username);
				CustomGraph graph;
				synchronized (threadHandler) {
					EntityTransaction tx = em.getTransaction();
					try {
						tx.begin();
						graph = em.find(CustomGraph.class, id);
						if (graph == null) {
							requestHandler.log(Level.WARNING,
									"user: " + username + ", " + "Graph does not exist: graph id " + graphId);
							return requestHandler.writeError(Error.PARAMETER_INVALID,
									"Graph does not exist: graph id " + graphId);
						}
						tx.commit();
					} catch (RuntimeException e) {
						if (tx != null && tx.isActive()) {
							tx.rollback();
						}
						throw e;
					}
					threadHandler.interruptBenchmark(id);
					List<Cover> queryResults;
					String queryStr = "SELECT c from Cover c" + " JOIN c." + Cover.GRAPH_FIELD_NAME + " g" + " WHERE g."
							+ CustomGraph.USER_NAME_FIELD_NAME + " = :username" + " AND g." + CustomGraph.ID_FIELD_NAME
							+ " = " + graphId;
					TypedQuery<Cover> query = em.createQuery(queryStr, Cover.class);
					query.setParameter("username", username);
					queryResults = query.getResultList();
					for (Cover cover : queryResults) {
						threadHandler.interruptAll(cover);
						tx = em.getTransaction();
						try {
							tx.begin();
							em.remove(cover);
							tx.commit();
						} catch (RuntimeException e) {
							if (tx != null && tx.isActive()) {
								tx.rollback();
							}
							throw e;
						}
					}
					try {
						tx = em.getTransaction();
						tx.begin();
						em.remove(graph);
						tx.commit();
					} catch (RuntimeException e) {
						if (tx != null && tx.isActive()) {
							tx.rollback();
						}
						throw e;
					}
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
		@ApiOperation(value = "", notes = "Imports a cover for an existing graph.")
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
				CustomGraphId id = new CustomGraphId(graphId, username);
				Cover cover;
				try {
					tx.begin();
					CustomGraph graph = em.find(CustomGraph.class, id);
					if (graph == null) {
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
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
				@ApiResponse(code = 401, message = "Unauthorized") })
		@ApiOperation(value = "Manage covers", notes = "Returns the ids (or meta information) of multiple covers.")
		public Response getCovers(@DefaultValue("0") @QueryParam("firstIndex") String firstIndexStr,
				@DefaultValue("") @QueryParam("length") String lengthStr,
				@DefaultValue("FALSE") @QueryParam("includeMeta") String includeMetaStr,
				@DefaultValue("") @QueryParam("executionStatuses") String executionStatusesStr,
				@DefaultValue("") @QueryParam("metricExecutionStatuses") String metricExecutionStatusesStr,
				@DefaultValue("") @QueryParam("graphId") String graphIdStr) {
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
		@ApiOperation(value = "", notes = "Returns a cover in a specified format.")
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
					cover = entityHandler.getCover(username, coverId, graphId);
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
		@ApiOperation(value = "", notes = "Deletes a cover.")
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
				EntityManager em = entityHandler.getEntityManager();
				CustomGraphId gId = new CustomGraphId(graphId, username);
				CoverId cId = new CoverId(coverId, gId);
				/*
				 * Checks whether cover is being calculated by a ground truth
				 * benchmark and if so deletes the graph instead.
				 */
				EntityTransaction tx = em.getTransaction();
				Cover cover;
				try {
					tx.begin();
					cover = em.find(Cover.class, cId);
					tx.commit();
				} catch (RuntimeException e) {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
					throw e;
				}
				if (cover == null) {
					requestHandler.log(Level.WARNING, "user: " + username + ", " + "Cover does not exist: cover id "
							+ coverId + ", graph id " + graphId);
					return requestHandler.writeError(Error.PARAMETER_INVALID,
							"Cover does not exist: cover id " + coverId + ", graph id " + graphId);
				}
				if (cover.getCreationMethod().getType().correspondsGroundTruthBenchmark()
						&& cover.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
					return this.deleteGraph(graphIdStr);
				}
				/*
				 * Deletes the cover.
				 */
				synchronized (threadHandler) {
					tx = em.getTransaction();
					try {
						tx.begin();
						cover = em.find(Cover.class, cId);
						tx.commit();
					} catch (RuntimeException e) {
						if (tx != null && tx.isActive()) {
							tx.rollback();
						}
						throw e;
					}
					if (cover == null) {
						requestHandler.log(Level.WARNING, "user: " + username + ", " + "Cover does not exist: cover id "
								+ coverId + ", graph id " + graphId);
						return requestHandler.writeError(Error.PARAMETER_INVALID,
								"Cover does not exist: cover id " + coverId + ", graph id " + graphId);
					}
					/*
					 * Interrupts algorithms and metrics.
					 */
					threadHandler.interruptAll(cover);
					/*
					 * Removes cover
					 */
					tx = em.getTransaction();
					try {
						tx.begin();
						em.remove(cover);
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
		@ApiOperation(value = "", notes = "Creates a new cover by running an algorithm on an existing graph.")
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
						if (weight && (algorithm
								.getAlgorithmType() == CoverCreationType.COST_FUNC_OPT_CLUSTERING_ALGORITHM
								|| algorithm.getAlgorithmType() == CoverCreationType.WORD_CLUSTERING_REF_ALGORITHM)) {
							requestHandler.log(Level.WARNING,
									"user: " + username + ", "
											+ "Invalid algorihtm in combination of weighting requested: "
											+ algorithm.getAlgorithmType().toString());
							return requestHandler.writeError(Error.PARAMETER_INVALID,
									"Invalid algorihtm in combination of weighting requested:"
											+ algorithm.getAlgorithmType().toString());
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
					 * Registers and starts algorithmex
					 */
					threadHandler.runAlgorithm(cover, algorithm, componentNodeCountFilter);
				}
				return Response.ok(requestHandler.writeId(cover)).build();
			} catch (Exception e) {
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
		@ApiOperation(value = "", notes = "Creates a ground truth benchmark cover.")
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
		@ApiOperation(value = "", notes = "Runs a statistical measure on a cover and creates the corresponding log.")
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
		@ApiOperation(value = "", notes = "Runs a knowledge-driven measure on a cover and creates the corresponding log.")
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
		@ApiOperation(value = "", notes = "Deletes a metric.")
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
		@ApiOperation(value = "", notes = "Returns the default parameters of an algorithm.")
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
		@ApiOperation(value = "", notes = "Returns the default parameters of a benchmark.")
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
		@ApiOperation(value = "", notes = "Returns the default parameters of a metric.")
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
		@ApiOperation(value = "", notes = "Returns all cover creation type names.")
		public Response getCoverCreationMethodNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(CoverCreationType.class)).build();
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
		@ApiOperation(value = "Algorithms information", notes = "Returns all algorithm type names.")
		public Response getAlgorithmNames() {
			try {
				return Response.ok(requestHandler.writeAlgorithmNames()).build();
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
		@ApiOperation(value = "Algorithms information", notes = "Returns all ground truth benchmark type names.")
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
		@ApiOperation(value = "", notes = "Returns all graph creation type names.")
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
		@ApiOperation(value = "", notes = "Returns all graph input format names.")
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
		@ApiOperation(value = "", notes = "Returns all graph output format names.")
		public Response getGraphOutputFormatNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(GraphOutputFormat.class)).build();
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
		@ApiOperation(value = "", notes = "Returns all cover creation type names.")
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
		@ApiOperation(value = "", notes = "Returns all cover creation type names.")
		public Response getCoverInputFormatNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(CoverInputFormat.class)).build();
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
		@ApiOperation(value = "", notes = "Returns all statistical measure type names.")
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
		@ApiOperation(value = "", notes = "Returns all knowledge-driven measure type names.")
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
		@ApiOperation(value = "Metrics information", notes = "Returns all metric type names.")
		public Response getMetricNames() {
			try {
				return Response.ok(requestHandler.writeEnumNames(OcdMetricType.class)).build();
			} catch (Exception e) {
				requestHandler.log(Level.SEVERE, "", e);
				return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
			}

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
		Boolean directed = graph.getTypes().contains(GraphType.DIRECTED);
		Boolean weighted = graph.getTypes().contains(GraphType.WEIGHTED);
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

		logger.log(Level.INFO, "RMI requested a graph");
		return graphData;
	}

	/**
	 * Get a List of all graph indices of a user
	 * 
	 * @param graphId
	 *            Id of the requested stored graph
	 * @return HashMap
	 * 
	 */
	public List<Long> getGraphIds() throws AgentNotKnownException {

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
			cover = entityHandler.getCover(username, coverId, graphId);
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

		List<Cover> queryResults;
		EntityManager em = entityHandler.getEntityManager();

		String queryStr = "SELECT c from Cover c" + " JOIN c." + Cover.GRAPH_FIELD_NAME + " g";
		queryStr += " WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username";
		queryStr += " AND g." + CustomGraph.ID_FIELD_NAME + " = " + graphId;

		queryStr += " GROUP BY c";
		TypedQuery<Cover> query = em.createQuery(queryStr, Cover.class);

		query.setParameter("username", username);
		queryResults = query.getResultList();
		em.close();

		int size = queryResults.size();
		List<Long> coverIds = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			coverIds.add(queryResults.get(i).getId());
		}

		return coverIds;

	}

}