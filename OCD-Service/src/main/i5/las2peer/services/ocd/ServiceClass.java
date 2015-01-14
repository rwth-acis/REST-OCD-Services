package i5.las2peer.services.ocd;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.ContentParam;
import i5.las2peer.restMapper.annotations.DELETE;
import i5.las2peer.restMapper.annotations.GET;
import i5.las2peer.restMapper.annotations.POST;
import i5.las2peer.restMapper.annotations.Path;
import i5.las2peer.restMapper.annotations.PathParam;
import i5.las2peer.restMapper.annotations.Produces;
import i5.las2peer.restMapper.annotations.QueryParam;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.annotations.swagger.ApiInfo;
import i5.las2peer.restMapper.annotations.swagger.ResourceListApi;
import i5.las2peer.restMapper.annotations.swagger.Summary;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.ocd.adapters.coverInput.CoverInputFormat;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputFormat;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputFormat;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputFormat;
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
import i5.las2peer.services.ocd.metrics.KnowledgeDrivenMeasure;
import i5.las2peer.services.ocd.metrics.OcdMetricFactory;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricLogId;
import i5.las2peer.services.ocd.metrics.OcdMetricType;
import i5.las2peer.services.ocd.metrics.StatisticalMeasure;
import i5.las2peer.services.ocd.utils.Error;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import i5.las2peer.services.ocd.utils.OcdRequestHandler;
import i5.las2peer.services.ocd.utils.RequestHandler;
import i5.las2peer.services.ocd.utils.ThreadHandler;

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

import org.apache.commons.lang3.NotImplementedException;
import org.la4j.matrix.sparse.CCSMatrix;



/**
 * 
 * LAS2peer Service Class
 * 
 * Provides the RESTful interface of the overlapping community detection service.
 * 
 * @author Sebastian
 *
 */
@Produces("text/xml")
@Path("ocd")
@Version("0.1")
@ApiInfo(
		  title="OCD",
		  description="A RESTful service for overlapping community detection.",
		  /* TODO add tos url */
		  termsOfServiceUrl="sample-tos.io",
		  contact="contact@contact.io",
		  license="Apache License 2",
		  licenseUrl="http://www.apache.org/licenses/LICENSE-2.0"
		)
public class ServiceClass extends Service {
		
	/*
	 * Service initialization.
	 */
	static {
		RequestHandler reqHandler = new RequestHandler();
		reqHandler.log(Level.INFO, "Overlapping Community Detection Service started.");
	}
	
	///////////////////////////////////////////////////////////
	//////// ATTRIBUTES
	///////////////////////////////////////////////////////////
	
	/**
	 * The thread handler used for algorithm, benchmark and metric execution.
	 */
	private ThreadHandler threadHandler = new ThreadHandler();
	
	/**
	 * The request handler used for simple request-related tasks.
	 */
	private OcdRequestHandler requestHandler = new OcdRequestHandler();
	
	/**
	 * The factory used for creating benchmarks.
	 */
	private OcdBenchmarkFactory benchmarkFactory = new OcdBenchmarkFactory();
	
	/**
	 * The factory used for creating algorithms.
	 */
	private OcdAlgorithmFactory algorithmFactory = new OcdAlgorithmFactory();
	
	/**
	 * The factory used for creating metrics.
	 */
	private OcdMetricFactory metricFactory = new OcdMetricFactory();
	
	
	//////////////////////////////////////////////////////////////////
	///////// METHODS
	//////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////
	///////// GENERAL
	//////////////////////////////////////////////////////////////////
	
	/**
	 * This method is needed for every RESTful application in LAS2peer.
	 * 
	 * @return the mapping
	 */
    public String getRESTMapping()
    {
        String result="";
        try {
            result= RESTMapper.getMethodsAsXML(this.getClass());
        } catch (Exception e) {

            e.printStackTrace();
        }
        return result;
    }
    
    
    /**
     * Simple function to validate a user login.
     * Basically it only serves as a "calling point" and does not really validate a user
     * (since this is done previously by LAS2peer itself, the user does not reach this method
     * if he or she is not authenticated).
     * @return A confirmation XML.
     */
    @GET
    @Path("validate")
    @ResourceListApi(description = "User validation")
    @Summary("Simple function to validate a user login.")
    public String validateLogin()
    {
    	try {
    		return requestHandler.writeConfirmationXml();
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
//////////////////////////////////////////////////////////////////
///////// SWAGGER
//////////////////////////////////////////////////////////////////
    
    @GET
    @Path("api-docs")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse getSwaggerResourceListing(){
      return RESTMapper.getSwaggerResourceListing(this.getClass());
    }

    @GET
    @Path("api-docs/{tlr}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse getSwaggerApiDeclaration(@PathParam("tlr") String tlr){
      return RESTMapper.getSwaggerApiDeclaration(this.getClass(), tlr, "http://localhost:8080/ocd/");
    }

//////////////////////////////////////////////////////////////////////////
//////////// GRAPHS
//////////////////////////////////////////////////////////////////////////
    
    /**
     * Imports a graph.
     * @param nameStr The name for the graph.
     * @param creationTypeStr The type of the creation method used to create the graph.
     * @param graphInputFormatStr The name of the graph input format.
     * @param doMakeUndirectedStr Optional query parameter. Defines whether directed edges shall be turned into undirected edges (TRUE) or not.
     * @param contentStr The graph input.
     * @return A graph id xml.
     * Or an error xml.
     */
    @POST
    @Path("graphs/name/{name}/creationmethod/{GraphCreationType}/inputFormat/{GraphInputFormat}")
    @Summary("Imports a graph.")
    public String createGraph(
    		@PathParam("name") String nameStr,
    		@PathParam("GraphCreationType") String creationTypeStr,
    		@PathParam("GraphInputFormat") String graphInputFormatStr,
    		@QueryParam(name="doMakeUndirected", defaultValue = "FALSE") String doMakeUndirectedStr,
    		@ContentParam String contentStr)
    {
    	try {
	    	String username = ((UserAgent) getActiveAgent()).getLoginName();
	    	GraphInputFormat format;
	    	CustomGraph graph;
	    	try {
		    	format = GraphInputFormat.valueOf(graphInputFormatStr);
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
	    		return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified input format does not exist.");
	    	}
	    	GraphCreationType benchmarkType;
	    	try {
	    		benchmarkType = GraphCreationType.valueOf(creationTypeStr);
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
	    		return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified input format does not exist.");
	    	}
	    	try {
	    		graph = requestHandler.parseGraph(contentStr, format);
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
	    		return requestHandler.writeError(Error.PARAMETER_INVALID, "Input graph does not correspond to the specified format.");
	    	}
	    	boolean doMakeUndirected;
	    	try {
	    		doMakeUndirected = requestHandler.parseBoolean(doMakeUndirectedStr);
	    	}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
	    		return requestHandler.writeError(Error.PARAMETER_INVALID, "Do make undirected ist not a boolean value.");
	    	}
	    	graph.setUserName(username);
	    	graph.setName(URLDecoder.decode(nameStr, "UTF-8"));
	    	GraphCreationLog log = new GraphCreationLog(benchmarkType, new HashMap<String, String>());
	    	log.setStatus(ExecutionStatus.COMPLETED);
	    	graph.setCreationMethod(log);
	    	GraphProcessor processor = new GraphProcessor();
	    	processor.determineGraphTypes(graph);
	    	if(doMakeUndirected) {
	    		Set<GraphType> graphTypes = graph.getTypes();
	    		if(graphTypes.remove(GraphType.DIRECTED)) {
	    			processor.makeCompatible(graph, graphTypes);
	    		}
	    	}
	    	EntityManager em = requestHandler.getEntityManager();
	    	EntityTransaction tx = em.getTransaction();
			try {
				tx.begin();
				em.persist(graph);
				tx.commit();
			}
			catch( RuntimeException e ) {
				if( tx != null && tx.isActive() ) {
					tx.rollback();
				}
				throw e;
			}
			em.close();
	    	return requestHandler.writeId(graph);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
			return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}    	
    }
    
    /**
     * Returns the ids (or meta information) of multiple graphs.
     * @param firstIndexStr Optional query parameter. The result list index of the first id to return. Defaults to 0.
     * @param lengthStr Optional query parameter. The number of ids to return. Defaults to Long.MAX_VALUE.
     * @param includeMetaStr Optional query parameter. If TRUE, instead of the ids the META XML of each graph is returned. Defaults to FALSE.
     * @param executionStatusesStr Optional query parameter. If set only those graphs are returned whose creation method has one of the given ExecutionStatus names.
     * Multiple status names are separated using the "-" delimiter.
     * @return The graphs.
     * Or an error xml.
     */
    
    @GET
    @Path("graphs")
    @ResourceListApi(description = "Manage graphs")
    @Summary("Returns the ids or meta information of multiple graphs.")
    public String getGraphs(
    		@QueryParam(name="firstIndex", defaultValue = "0") String firstIndexStr,
    		@QueryParam(name="length", defaultValue = "") String lengthStr,
    		@QueryParam(name="includeMeta", defaultValue = "FALSE") String includeMetaStr,
    		@QueryParam(name="executionStatuses", defaultValue ="") String executionStatusesStr)
    {
    	try {
			String username = ((UserAgent) getActiveAgent()).getLoginName();
			List<CustomGraph> queryResults;
			List<Integer> executionStatusIds = new ArrayList<Integer>();
			if(executionStatusesStr != "") {
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
			EntityManager em = requestHandler.getEntityManager();
			String queryStr = "SELECT g FROM CustomGraph g"
					+ " JOIN g." + CustomGraph.CREATION_METHOD_FIELD_NAME + " b"
					+ " WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username"
					+ " AND b." + GraphCreationLog.STATUS_ID_FIELD_NAME + " IN :execStatusIds";
			TypedQuery<CustomGraph> query = em.createQuery(queryStr, CustomGraph.class);
			try {
				int firstIndex = Integer.parseInt(firstIndexStr);
				query.setFirstResult(firstIndex);
			}
			catch (Exception e) {
				requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "First index is not valid.");
			}
			try {
				if(lengthStr != "") {
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
				responseStr = requestHandler.writeGraphMetas(queryResults);
			}
			else {
				responseStr = requestHandler.writeGraphIds(queryResults);
			}
			return responseStr;
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns a graph in a specified output format.
     * @param graphIdStr The graph id.
     * @param graphOuputFormatStr The name of the graph output format.
     * @return The graph output.
     * Or an error xml.
     */
    @GET
    @Produces("text/plain")
    @Path("graphs/{graphId}/outputFormat/{GraphOutputFormat}")
    @Summary("Returns a graph in a specified output format.")
    public String getGraph(
    		@PathParam("graphId") String graphIdStr,
    		@PathParam("GraphOutputFormat") String graphOuputFormatStr)
    {
    	try {
    		long graphId;
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		GraphOutputFormat format;
    		try {
    			graphId = Long.parseLong(graphIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
    		}
    		try {
		    	format = GraphOutputFormat.valueOf(graphOuputFormatStr);
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified graph output format does not exist.");
	    	}
	    	EntityManager em = requestHandler.getEntityManager();
	    	CustomGraphId id = new CustomGraphId(graphId, username);
	    	EntityTransaction tx = em.getTransaction();
	    	CustomGraph graph;
	    	try {
				tx.begin();
				graph = em.find(CustomGraph.class, id);
		    	if(graph == null) {
		    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Graph does not exist: graph id " + graphId);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph does not exist: graph id " + graphId);
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
	    	return requestHandler.writeGraph(graph, format);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Deletes a graph.
     * All covers based on the graph are removed as well.
     * If a benchmark is currently calculating the graph the execution is terminated.
     * If an algorithm is currently calculating a cover based on the graph it is terminated.
     * If a metric is currently running on a cover based on the grap it is terminated.
     * @param graphIdStr The graph id.
     * @return A confirmation xml.
     * Or an error xml.
     */
    @DELETE
    @Path("graphs/{graphId}")
    @Summary("Deletes a graph.")
    public String deleteGraph(
    		@PathParam("graphId") String graphIdStr)
    {
    	try {
    		long graphId;
	    	String username = ((UserAgent) getActiveAgent()).getLoginName();
	    	try {
    			graphId = Long.parseLong(graphIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
    		}
	    	EntityManager em = requestHandler.getEntityManager();
	    	CustomGraphId id = new CustomGraphId(graphId, username);
	    	CustomGraph graph;
    		synchronized(threadHandler) {
    			EntityTransaction tx = em.getTransaction();
    			try {
					tx.begin();
					graph = em.find(CustomGraph.class, id);
			    	if(graph == null) {
			    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Graph does not exist: graph id " + graphId);
						return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph does not exist: graph id " + graphId);
			    	}
			    	tx.commit();
    			}
    			catch( RuntimeException e ) {
    				if( tx != null && tx.isActive() ) {
    					tx.rollback();
    				}
    				throw e;
    			}
    			threadHandler.interruptBenchmark(id);
		    	List<Cover> queryResults;
				String queryStr = "SELECT c from Cover c"
						+ " JOIN c." + Cover.GRAPH_FIELD_NAME + " g"
						+ " WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username"
						+ " AND g." + CustomGraph.ID_FIELD_NAME + " = " + graphId;
				TypedQuery<Cover> query = em.createQuery(queryStr, Cover.class);
				query.setParameter("username", username);
				queryResults = query.getResultList();
				for(Cover cover : queryResults) {
					threadHandler.interruptAll(cover);
					tx = em.getTransaction();
					try {
						tx.begin();
						em.remove(cover);
						tx.commit();
	    			}
					catch( RuntimeException e ) {
	    				if( tx != null && tx.isActive() ) {
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
				}
				catch( RuntimeException e ) {
    				if( tx != null && tx.isActive() ) {
    					tx.rollback();
    				}
    				throw e;
    			}
    		}
	    	return requestHandler.writeConfirmationXml();
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
//////////////////////////////////////////////////////////////////////////
//////////// COVERS
//////////////////////////////////////////////////////////////////////////

    /**
     * Imports a cover for an existing graph.
     * @param graphIdStr The id of the graph that the cover is based on.
     * @param nameStr A name for the cover.
     * @param creationTypeStr The name of the creation method the cover was created by.
     * @param coverInputFormatStr The name of the input format.
     * @param contentStr The cover input.
     * @return A cover id xml.
     * Or an error xml.
     */
    @POST
    @Path("covers/graph/{graphId}/name/{name}/creationmethod/{CoverCreationType}/inputFormat/{CoverInputFormat}")
    @Summary("Imports a cover for an existing graph.")
    public String createCover(
    		@PathParam("graphId") String graphIdStr,
    		@PathParam("name") String nameStr,
    		@PathParam("CoverCreationType") String creationTypeStr,
    		@PathParam("CoverInputFormat") String coverInputFormatStr,
    		@ContentParam String contentStr)
    {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		long graphId;
    		try {
    			graphId = Long.parseLong(graphIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
    		}
    		CoverInputFormat format;
    		try {
		    	format = CoverInputFormat.valueOf(coverInputFormatStr);
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified cover input format does not exist.");
	    	}
		    CoverCreationType algorithmType;
		    try {
    			algorithmType = CoverCreationType.valueOf(creationTypeStr);
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified algorithm does not exist.");
	    	}
		    Set<GraphType> graphTypes;
    		if(algorithmFactory.isInstantiatable(algorithmType)) {
    			OcdAlgorithm algorithm;
    			algorithm = algorithmFactory.getInstance(algorithmType, new HashMap<String, String>());
    			graphTypes = algorithm.compatibleGraphTypes();
    		}
    		else {
    			graphTypes = new HashSet<GraphType>();
    		}
    		CoverCreationLog log = new CoverCreationLog(algorithmType, new HashMap<String, String>(), graphTypes);
    		log.setStatus(ExecutionStatus.COMPLETED);
		    EntityManager em = requestHandler.getEntityManager();
		    EntityTransaction tx = em.getTransaction();
		    CustomGraphId id = new CustomGraphId(graphId, username);
	    	Cover cover;
	    	try {
				tx.begin();
				CustomGraph graph = em.find(CustomGraph.class, id);
		    	if(graph == null) {
		    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Graph does not exist: graph id " + graphId);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph does not exist: graph id " + graphId);
		    	}
		    	try {
		    		cover = requestHandler.parseCover(contentStr, graph, format);
		    	} 
		    	catch (Exception e) {
		    		requestHandler.log(Level.WARNING, "user: " + username, e);
		    		return requestHandler.writeError(Error.PARAMETER_INVALID, "Input cover does not correspond to the specified format.");
		    	}
		    	cover.setCreationMethod(log);
		    	cover.setName(URLDecoder.decode(nameStr, "UTF-8"));
		    	em.persist(cover);
				tx.commit();
			}
	    	catch( RuntimeException e ) {
				if( tx != null && tx.isActive() ) {
					tx.rollback();
				}
				throw e;
			}
			em.close();
	    	return requestHandler.writeId(cover);
    	}
       	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns the ids (or meta information) of multiple covers.
     * @param firstIndexStr Optional query parameter. The result list index of the first id to return. Defaults to 0.
     * @param lengthStr Optional query parameter. The number of ids to return. Defaults to Long.MAX_VALUE.
     * @param includeMetaStr Optional query parameter. If TRUE, instead of the ids the META XML of each graph is returned. Defaults to FALSE.
     * @param executionStatusesStr Optional query parameter. If set only those covers are returned whose creation method status corresponds to one of the given ExecutionStatus names.
     * Multiple status names are separated using the "-" delimiter.
     * @param metricExecutionStatusesStr Optional query parameter.
     * If set only those covers are returned that have a corresponding metric log
     * with a status corresponding to one of the given ExecutionStatus names.
     * Multiple status names are separated using the "-" delimiter.
     * @param graphIdStr Optional query parameter. If set only those covers are returned that are based on the corresponding graph.
     * @return The covers.
     * Or an error xml.
     */
    @GET
    @Path("covers")
    @ResourceListApi(description = "Manage covers")
    @Summary("Returns the ids (or meta information) of multiple covers.")
    public String getCovers(
    		@QueryParam(name="firstIndex", defaultValue = "0") String firstIndexStr,
    		@QueryParam(name="length", defaultValue = "") String lengthStr,
    		@QueryParam(name="includeMeta", defaultValue = "FALSE") String includeMetaStr,
    		@QueryParam(name="executionStatuses", defaultValue = "") String executionStatusesStr,
    		@QueryParam(name="metricExecutionStatuses", defaultValue = "") String metricExecutionStatusesStr,
    		@QueryParam(name="graphId", defaultValue = "") String graphIdStr)
    {
    	try {
			String username = ((UserAgent) getActiveAgent()).getLoginName();
			long graphId = 0;
			if(graphIdStr != "") {
	    		try {
	    			graphId = Long.parseLong(graphIdStr);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
			}
			List<Integer> executionStatusIds = new ArrayList<Integer>();
			if(executionStatusesStr != "") {
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
			List<Integer> metricExecutionStatusIds = new ArrayList<Integer>();
			if(metricExecutionStatusesStr != "") {
	    		try {
	    			List<String> metricExecutionStatusesStrList = requestHandler.parseQueryMultiParam(metricExecutionStatusesStr);
	    			for(String executionStatusStr : metricExecutionStatusesStrList) {
    					ExecutionStatus executionStatus = ExecutionStatus.valueOf(executionStatusStr);
    					metricExecutionStatusIds.add(executionStatus.getId());
	    			}
	    		}
		    	catch (Exception e) {
		    		requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified metric execution status does not exist.");
		    	}
			}
			List<Cover> queryResults;
			EntityManager em = requestHandler.getEntityManager();
			/*
			 * Query
			 */
			String queryStr = "SELECT c from Cover c"
					+ " JOIN c." + Cover.GRAPH_FIELD_NAME + " g"
					+ " JOIN c." + Cover.CREATION_METHOD_FIELD_NAME + " a";
			if(metricExecutionStatusesStr != "") {
					queryStr += " JOIN c." + Cover.METRICS_FIELD_NAME + " m";
			}
			queryStr += " WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username"
					+ " AND a." + CoverCreationLog.STATUS_ID_FIELD_NAME + " IN :execStatusIds";
			if(metricExecutionStatusesStr != "") {
					queryStr += " AND m." + OcdMetricLog.STATUS_ID_FIELD_NAME + " IN :metricExecStatusIds";
			}
			if(graphIdStr != "") {
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
			}
			catch (Exception e) {
				requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "First index is not valid.");
			}
			try {
				if(lengthStr != "") {
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
			if(metricExecutionStatusesStr != "") {
				query.setParameter("metricExecStatusIds", metricExecutionStatusIds);
			}
			queryResults = query.getResultList();
			em.close();
			String responseStr;
			if(includeMeta) {
				responseStr = requestHandler.writeCoverMetas(queryResults);
			}
			else {
				responseStr = requestHandler.writeCoverIds(queryResults);
			}
			return responseStr;
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns a cover in a specified format.
     * @param graphIdStr The id of the graph that the cover is based on.
     * @param coverIdStr The cover id.
     * @param coverOutputFormatStr The cover output format.
     * @return The cover output.
     * Or an error xml.
     */
    @GET
    @Produces("text/plain")
    @Path("covers/{coverId}/graph/{graphId}/outputFormat/{CoverOutputFormat}")
    @Summary("Returns a cover in a specified format.")
    public String getCover(
    		@PathParam("graphId") String graphIdStr,
    		@PathParam("coverId") String coverIdStr,
    		@PathParam("CoverOutputFormat") String coverOutputFormatStr)
    {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		long graphId;
    		try {
    			graphId = Long.parseLong(graphIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
    		}
    		long coverId;
    		try {
    			coverId = Long.parseLong(coverIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover id is not valid.");
    		}
    		CoverOutputFormat format;
    		try {
		    	format = CoverOutputFormat.valueOf(coverOutputFormatStr);
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified cover output format does not exist.");
	    	}
    		EntityManager em = requestHandler.getEntityManager();
	    	CustomGraphId gId = new CustomGraphId(graphId, username);
	    	CoverId cId = new CoverId(coverId, gId);
			/*
			 * Finds cover
			 */
			EntityTransaction tx = em.getTransaction();
	    	Cover cover;
	    	try {
				tx.begin();
				cover = em.find(Cover.class, cId);
				tx.commit();
			}
	    	catch( RuntimeException e ) {
				if( tx != null && tx.isActive() ) {
					tx.rollback();
				}
				throw e;
			}
	    	if(cover == null) {
	    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
	    	}
	    	return requestHandler.writeCover(cover, format);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Deletes a cover.
     * If the cover is still being created by an algorithm, the algorithm is terminated.
     * If the cover is still being created by a ground truth benchmark,
     * the benchmark is terminated and the corresponding graph is deleted as well.
     * If metrics are running on the cover, they are terminated.
     * @param coverIdStr The cover id.
     * @param graphIdStr The graph id of the graph corresponding the cover.
     * @return A confirmation xml.
     * Or an error xml.
     */
    @DELETE
    @Path("covers/{coverId}/graph/{graphId}")
    @Summary("Deletes a cover.")
    public String deleteCover(
    		@PathParam("coverId") String coverIdStr,
    		@PathParam("graphId") String graphIdStr)
    {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		long graphId;
    		try {
    			graphId = Long.parseLong(graphIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
    		}
    		long coverId;
    		try {
    			coverId = Long.parseLong(coverIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover id is not valid.");
    		}
    		EntityManager em = requestHandler.getEntityManager();
	    	CustomGraphId gId = new CustomGraphId(graphId, username);
	    	CoverId cId = new CoverId(coverId, gId);
	    	/*
	    	 * Checks whether cover is being calculated by a ground truth benchmark and if so deletes the graph instead.
	    	 */
	    	EntityTransaction tx = em.getTransaction();
	    	Cover cover;
	    	try {
				tx.begin();
				cover = em.find(Cover.class, cId);
				tx.commit();
			}
	    	catch( RuntimeException e ) {
				if( tx != null && tx.isActive() ) {
					tx.rollback();
				}
				throw e;
			}
	    	if(cover == null) {
	    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
	    	}
	    	if(cover.getCreationMethod().getType().correspondsGroundTruthBenchmark() && cover.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
	    		return this.deleteGraph(graphIdStr);
	    	}
	    	/*
	    	 * Deletes the cover.
	    	 */
    		synchronized(threadHandler) {
    			tx = em.getTransaction();
		    	try {
					tx.begin();
					cover = em.find(Cover.class, cId);
					tx.commit();
				}
		    	catch( RuntimeException e ) {
					if( tx != null && tx.isActive() ) {
						tx.rollback();
					}
					throw e;
				}
		    	if(cover == null) {
		    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
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
				}
		    	catch( RuntimeException e ) {
					if( tx != null && tx.isActive() ) {
						tx.rollback();
					}
					throw e;
				}
    			em.close();
    			return requestHandler.writeConfirmationXml();
    		}
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
//////////////////////////////////////////////////////////////////////////
//////////// ALGORITHMS
//////////////////////////////////////////////////////////////////////////
    
    /**
     * Creates a new cover by running an algorithm on an existing graph.
     * @param graphIdStr The id of the graph to run the algorithm on, must have the creation method status completed.
     * @param nameStr The name for the cover.
     * @param creationTypeStr The name of a cover creation type corresponding to an ocd algorithm.
     * Defines the algorithm to execute.
     * @param content A parameter xml defining any non-default parameters passed to the algorithm.
     * @param componentNodeCountFilterStr Option query parameter. The component node count filter applied by the OcdAlgorithmExecutor.
     * @return The id of the cover being calculated which is reserved for the algorithm result.
     * Or an error xml.
     */
    @POST
    @Path("algorithms/{CoverCreationType}/graph/{graphId}/name/{name}")
    @Summary("Creates a new cover by running an algorithm on an existing graph.")
    public String runAlgorithm(
    		@PathParam("graphId") String graphIdStr,
    		@PathParam("name") String nameStr,
    		@PathParam("CoverCreationType") String creationTypeStr,
    		@ContentParam String content,
    		@QueryParam(name = "componentNodeCountFilter", defaultValue = "0") String componentNodeCountFilterStr)
    {
    	try {
    		int componentNodeCountFilter;
    		long graphId;
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		CoverCreationType algorithmType;
    		try {
    			graphId = Long.parseLong(graphIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
    		}
    		try {
    			componentNodeCountFilter = Integer.parseInt(componentNodeCountFilterStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Component node count filter is not valid.");
    		}
    		try {
    			algorithmType = CoverCreationType.valueOf(creationTypeStr);
    			if(algorithmType == CoverCreationType.UNDEFINED || algorithmType == CoverCreationType.GROUND_TRUTH) {
    				requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified algorithm type is not valid for this request: " + algorithmType.name());
    				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified algorithm type is not valid for this request: " + algorithmType.name());
    			}
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified algorithm does not exist.");
	    	}
    		OcdAlgorithm algorithm;
    		Map<String, String> parameters;
    		try {
    			parameters = requestHandler.parseParameters(content);
    			algorithm = algorithmFactory.getInstance(algorithmType, parameters);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Parameters are not valid.");
    		}
    		Cover cover;
	    	EntityManager em = requestHandler.getEntityManager();
	    	CustomGraphId id = new CustomGraphId(graphId, username);
	    	CoverCreationLog log;
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
			    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Invalid graph creation method status for metric execution: " + graph.getCreationMethod().getStatus().name());
						return requestHandler.writeError(Error.PARAMETER_INVALID, "Invalid graph creation method status for metric execution: " + graph.getCreationMethod().getStatus().name());
			    	}
			    	cover = new Cover(graph, new CCSMatrix(graph.nodeCount(), 0));
			    	log = new CoverCreationLog(algorithmType, parameters, algorithm.compatibleGraphTypes());
			    	cover.setCreationMethod(log);
			    	cover.setName(URLDecoder.decode(nameStr, "UTF-8"));
			    	em.persist(cover);
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
				threadHandler.runAlgorithm(cover, algorithm, componentNodeCountFilter);
	    	}
	    	return requestHandler.writeId(cover);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
//////////////////////////////////////////////////////////////////////////
//////////// BENCHMARKS
//////////////////////////////////////////////////////////////////////////
    
    /**
     * Creates a ground truth benchmark cover.
     * @param coverNameStr The name for the cover.
     * @param graphNameStr The name for the underlying benchmark graph.
     * @param creationTypeStr The name of a graph creation type corresponding a ground truth benchmark.
     * @param contentStr A parameter xml defining any non-default parameters passed to the benchmark.
     * @return The id of the cover being calculated which is reserved for the benchmark result
     * (contains also the id of the graph being calculated which is reserved for the benchmark result as well).
     * Or an error xml.
     */
    @POST
    @Path("benchmarks/{GraphCreationType}/graph/{graphName}/cover/{coverName}")
    @Summary("Creates a ground truth benchmark cover.")
    public String runGroundTruthBenchmark(
    		@PathParam("coverName") String coverNameStr,
    		@PathParam("graphName") String graphNameStr,
    		@PathParam("GraphCreationType") String creationTypeStr,
    		@ContentParam String contentStr)
    {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		GraphCreationType benchmarkType;
    		CoverCreationType coverCreationType;
    		try {
    			benchmarkType = GraphCreationType.valueOf(creationTypeStr);
    			coverCreationType = CoverCreationType.valueOf(creationTypeStr);
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified benchmark does not exist.");
	    	}
    		Map<String, String> parameters;
    		GroundTruthBenchmark benchmark;
    		if(!benchmarkFactory.isInstantiatable(benchmarkType)) {
    			requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified benchmark is not instantiatable: " + benchmarkType.name());
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified benchmark is not instantiatable: "+ benchmarkType.name());
    		}
    		else if(!benchmarkType.correspondsGroundTruthBenchmark()) {
    			requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified benchmark is not a ground truth benchmark: " + benchmarkType.name());
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified benchmark is not a ground truth benchmark: "+ benchmarkType.name());
    		}
    		else {
	    		try {
	    			parameters = requestHandler.parseParameters(contentStr);
	    			benchmark = (GroundTruthBenchmark)benchmarkFactory.getInstance(benchmarkType, parameters);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Parameters are not valid.");
	    		}
    		}
	    	EntityManager em = requestHandler.getEntityManager();
	    	CustomGraph graph = new CustomGraph();
	    	graph.setName(URLDecoder.decode(graphNameStr, "UTF-8"));
	    	graph.setUserName(username);
	    	GraphCreationLog log = new GraphCreationLog(benchmarkType, parameters);
	    	log.setStatus(ExecutionStatus.WAITING);
	    	graph.setCreationMethod(log);
	    	Cover cover = new Cover(graph, new CCSMatrix(graph.nodeCount(), 0));
	    	cover.setName(URLDecoder.decode(coverNameStr, "UTF-8"));
	    	CoverCreationLog coverLog = new CoverCreationLog(coverCreationType, parameters, new HashSet<GraphType>());
	    	coverLog.setStatus(ExecutionStatus.WAITING);
	    	cover.setCreationMethod(coverLog);
	    	synchronized(threadHandler) {
		    	EntityTransaction tx = em.getTransaction();
		    	try {
					tx.begin();
					em.persist(graph);
			    	em.persist(cover);
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
				 * Registers and starts benchmark creation.
				 */
				threadHandler.runGroundTruthBenchmark(cover, benchmark);
	    	}
	    	return requestHandler.writeId(cover);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }

//////////////////////////////////////////////////////////////////////////
////////////METRICS
//////////////////////////////////////////////////////////////////////////

    /**
     * Runs a statistical measure on a cover and creates the corresponding log.
     * @param coverIdStr The id of the cover, must have the creation method status completed.
     * @param graphIdStr The id of the graph corresponding to the cover.
     * @param metricTypeStr The name of an OcdMetricType corresponding to a statistical measure.
     * @param contentStr A parameter xml defining any non-default parameters passed to the metric.
     * @return The id of the metric log being calculated which is reserved for the metric result
     * (contains also the corresponding cover and graph id).
     * Or an error xml.
     */
    @POST
    @Path("metrics/{OcdMetricType}/graph/{graphId}/cover/{coverId}")
    @Summary("Runs a statistical measure on a cover and creates the corresponding log.")
    public String runStatisticalMeasure(
    		@PathParam("coverId") String coverIdStr,
    		@PathParam("graphId") String graphIdStr,
    		@PathParam("OcdMetricType") String metricTypeStr,
    		@ContentParam String contentStr)
    {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		long graphId;
    		try {
    			graphId = Long.parseLong(graphIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
    		}
    		long coverId;
    		try {
    			coverId = Long.parseLong(coverIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover id is not valid.");
    		}
    		OcdMetricType metricType;
    		try {
    			metricType = OcdMetricType.valueOf(metricTypeStr);
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified metric does not exist.");
	    	}
    		Map<String, String> parameters;
    		StatisticalMeasure metric;
    		if(!metricFactory.isInstantiatable(metricType)) {
    			requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified metric is not instantiatable: " + metricType.name());
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified metric is not instantiatable: "+ metricType.name());
    		}
    		else if(!metricType.correspondsStatisticalMeasure()) {
    			requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified metric is not a statistical measure: " + metricType.name());
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified metric is not a statistical measure: "+ metricType.name());
    		}
    		else {
	    		try {
	    			parameters = requestHandler.parseParameters(contentStr);
	    			metric = (StatisticalMeasure)metricFactory.getInstance(metricType, parameters);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Parameters are not valid.");
	    		}
    		}
    		EntityManager em = requestHandler.getEntityManager();
	    	CustomGraphId gId = new CustomGraphId(graphId, username);
	    	CoverId cId = new CoverId(coverId, gId);
			/*
			 * Finds cover
			 */
	    	OcdMetricLog log;
	    	synchronized(threadHandler) {
				EntityTransaction tx = em.getTransaction();
		    	Cover cover;
		    	try {
					tx.begin();
					cover = em.find(Cover.class, cId);
			    	if(cover == null) {
			    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
						return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover does not exist.");
			    	}
			    	if(cover.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
			    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Invalid cover creation method status for metric execution: " + cover.getCreationMethod().getStatus().name());
						return requestHandler.writeError(Error.PARAMETER_INVALID, "Invalid cover creation method status for metric execution: " + cover.getCreationMethod().getStatus().name());
			    	}
			    	log = new OcdMetricLog(metricType, 0, parameters, cover);
			    	log.setStatus(ExecutionStatus.WAITING);
			    	cover.addMetric(log);
					tx.commit();
				}
		    	catch( RuntimeException e ) {
					if( tx != null && tx.isActive() ) {
						tx.rollback();
					}
					throw e;
				}
		    	threadHandler.runStatisticalMeasure(log, metric, cover);
	    	}
	    	return requestHandler.writeId(log);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }

    /**
     * Runs a knowledge-driven measure on a cover and creates the corresponding log.
     * @param coverIdStr The id of the cover, must have the creation method status completed.
     * @param graphIdStr The id of the graph corresponding to the cover.
     * @param metricTypeStr An OcdMetricType corresponding to a statistical measure.
     * @param groundTruthCoverIdStr The id of the ground truth cover used by the metric, must have the creation method status completed.
     * The ground truth cover's corresponding graph must be the same as the one corresponding 
     * to the first cover.
     * @param contentStr A parameter xml defining any non-default parameters passed to the algorithm.
     * @return The id of the metric log being calculated which is reserved for the metric result
     * (contains also the corresponding cover and graph id).
     * Or an error xml.
     */
    @POST
    @Path("metrics/{OcdMetricType}/graph/{graphId}/cover/{coverId}/groundtruth/{groundTruthCoverId}")
    @Summary("Runs a knowledge-driven measure on a cover and creates the corresponding log.")
    public String runKnowledgeDrivenMeasure(
    		@PathParam("coverId") String coverIdStr,
    		@PathParam("graphId") String graphIdStr,
    		@PathParam("OcdMetricType") String metricTypeStr,
    		@PathParam("groundTruthCoverId") String groundTruthCoverIdStr,
    		@ContentParam String contentStr)
    {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		long graphId;
    		try {
    			graphId = Long.parseLong(graphIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
    		}
    		long coverId;
    		try {
    			coverId = Long.parseLong(coverIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover id is not valid.");
    		}
    		long groundTruthCoverId;
    		try {
    			groundTruthCoverId = Long.parseLong(groundTruthCoverIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Ground truth cover id is not valid.");
    		}
    		OcdMetricType metricType;
    		try {
    			metricType = OcdMetricType.valueOf(metricTypeStr);
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified metric does not exist.");
	    	}
    		Map<String, String> parameters;
    		KnowledgeDrivenMeasure metric;
    		if(!metricFactory.isInstantiatable(metricType)) {
    			requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified metric is not instantiatable: " + metricType.name());
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified metric is not instantiatable: "+ metricType.name());
    		}
    		else if(!metricType.correspondsKnowledgeDrivenMeasure()) {
    			requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified metric is not a knowledge-driven measure: " + metricType.name());
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified metric is not a knowledge-driven measure: "+ metricType.name());
    		}
    		else {
	    		try {
	    			parameters = requestHandler.parseParameters(contentStr);
	    			metric = (KnowledgeDrivenMeasure)metricFactory.getInstance(metricType, parameters);
	    		}
	    		catch (Exception e) {
	    			requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.writeError(Error.PARAMETER_INVALID, "Parameters are not valid.");
	    		}
    		}
    		EntityManager em = requestHandler.getEntityManager();
	    	CustomGraphId gId = new CustomGraphId(graphId, username);
	    	CoverId cId = new CoverId(coverId, gId);
	    	CoverId gtId = new CoverId(groundTruthCoverId, gId);
			/*
			 * Finds cover
			 */
	    	OcdMetricLog log;
	    	synchronized(threadHandler) {
				EntityTransaction tx = em.getTransaction();
		    	Cover cover;
		    	Cover groundTruth;
		    	try {
					tx.begin();
					cover = em.find(Cover.class, cId);
			    	if(cover == null) {
			    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
						return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
			    	}
			    	if(cover.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
			    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Invalid cover creation method status for metric execution: " + cover.getCreationMethod().getStatus().name());
						return requestHandler.writeError(Error.PARAMETER_INVALID, "Invalid cover creation method status for metric execution: " + cover.getCreationMethod().getStatus().name());
			    	}
			    	if(groundTruthCoverId != coverId) {
				    	groundTruth = em.find(Cover.class, gtId);
				    	if(groundTruth == null) {
				    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Ground truth cover does not exist: cover id " + groundTruthCoverId + ", graph id " + graphId);
							return requestHandler.writeError(Error.PARAMETER_INVALID, "Ground truth cover does not exist: cover id " + groundTruthCoverId + ", graph id " + graphId);
				    	}
			    	}
			    	else {
			    		groundTruth = cover;
			    	}
			    	if(groundTruth.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {
			    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Invalid ground truth cover creation method status for metric execution: " + groundTruth.getCreationMethod().getStatus().name());
						return requestHandler.writeError(Error.PARAMETER_INVALID, "Invalid ground truth cover creation method status for metric execution: " + groundTruth.getCreationMethod().getStatus().name());
			    	}
			    	log = new OcdMetricLog(metricType, 0, parameters, cover);
			    	log.setStatus(ExecutionStatus.WAITING);
			    	cover.addMetric(log);
					tx.commit();
				}
		    	catch( RuntimeException e ) {
					if( tx != null && tx.isActive() ) {
						tx.rollback();
					}
					throw e;
				}
		    	threadHandler.runKnowledgeDrivenMeasure(log, metric, cover, groundTruth);
	    	}
	    	return requestHandler.writeId(log);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Deletes a metric.
     * If the metric is still in calculation, the execution is terminated.
     * @param coverIdStr The id of the cover that the metric belongs to.
     * @param graphIdStr The id of the graph corresponding to the cover.
     * @param metricIdStr The metric id.
     * @return A confirmation xml.
     * Or an error xml.
     */
    @DELETE
    @Path("metrics/{metricId}/graph/{graphId}/cover/{coverId}")
    @Summary("Deletes a metric.")
    public String deleteMetric(
    		@PathParam("coverId") String coverIdStr,
    		@PathParam("graphId") String graphIdStr,
    		@PathParam("metricId") String metricIdStr)
    {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		long graphId;
    		try {
    			graphId = Long.parseLong(graphIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Graph id is not valid.");
    		}
    		long coverId;
    		try {
    			coverId = Long.parseLong(coverIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Cover id is not valid.");
    		}
    		long metricId;
    		try {
    			metricId = Long.parseLong(metricIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Metric id is not valid.");
    		}
    		EntityManager em = requestHandler.getEntityManager();
	    	CustomGraphId gId = new CustomGraphId(graphId, username);
	    	CoverId cId = new CoverId(coverId, gId);
	    	OcdMetricLogId mId = new OcdMetricLogId(metricId, cId);
	    	EntityTransaction tx = em.getTransaction();
	    	OcdMetricLog log;
	    	/*
	    	 * Deletes the metric.
	    	 */
    		synchronized(threadHandler) {
    			tx = em.getTransaction();
    	    	try {
    				tx.begin();
    				log = em.find(OcdMetricLog.class, mId);
    				tx.commit();
    			}
    	    	catch( RuntimeException e ) {
    				if( tx != null && tx.isActive() ) {
    					tx.rollback();
    				}
    				throw e;
    			}
    	    	if(log == null) {
    	    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Metric does not exist: cover id " + coverId + ", graph id " + graphId + ", metric id " + metricId);
    				return requestHandler.writeError(Error.PARAMETER_INVALID, "Metric does not exist: cover id " + coverId + ", graph id " + graphId + ", metric id " + metricId);
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
				}
		    	catch( RuntimeException e ) {
					if( tx != null && tx.isActive() ) {
						tx.rollback();
					}
					throw e;
				}
    			em.close();
    			return requestHandler.writeConfirmationXml();
    		}
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
//////////////////////////////////////////////////////////////////////////
//////////// DEFAULT PARAMETERS
//////////////////////////////////////////////////////////////////////////    
    
    /**
     * Returns the default parameters of an algorithm.
     * @param coverCreationTypeStr The name of a cover creation type corresponding to an ocd algorithm.
     * @return A parameter xml.
     * Or an error xml.
     */
    @GET
    @Path("algorithms/{CoverCreationType}/parameters/default")
    @Summary("Returns the default parameters of an algorithm.")
    public String getAlgorithmDefaultParams(
    		@PathParam("CoverCreationType") String coverCreationTypeStr)
    {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		CoverCreationType creationType;
    		try {
    			creationType = CoverCreationType.valueOf(coverCreationTypeStr);
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified cover creation type does not exist.");
	    	}
			if(!algorithmFactory.isInstantiatable(creationType)) {
				requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified cover creation type is not instantiatable: " + creationType.name());
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified cover creation type is not instantiatable: " + creationType.name());
			}
			else {
				OcdAlgorithm defaultInstance = algorithmFactory.getInstance(creationType, new HashMap<String, String>());
				return requestHandler.writeParameters(defaultInstance.getParameters());
			}
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns the default parameters of a benchmark.
     * @param graphCreationTypeStr The name of a graph creation type corresponding to an ocd benchmark.
     * @return A parameter xml.
     * Or an error xml.
     */
    @GET
    @Path("benchmarks/{GraphCreationType}/parameters/default")
    @Summary("Returns the default parameters of a benchmark.")
    public String getBenchmarkDefaultParams(
    		@PathParam("GraphCreationType") String graphCreationTypeStr)
    {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		GraphCreationType creationType;
    		try {
    			creationType = GraphCreationType.valueOf(graphCreationTypeStr);
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified graph creation type does not exist.");
	    	}
    		if(!benchmarkFactory.isInstantiatable(creationType)) {
				requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified graph creation type is not instantiatable: " + creationType.name());
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified graph creation type is not instantiatable: " + creationType.name());
			}
    		if(creationType.correspondsGroundTruthBenchmark())
    		{
    			GroundTruthBenchmark defaultInstance = (GroundTruthBenchmark)benchmarkFactory.getInstance(creationType, new HashMap<String, String>());
    			return requestHandler.writeParameters(defaultInstance.getParameters());
    		}
    		else {
    			throw new NotImplementedException("Specified graph creation type is not a benchmark.");
    		}
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns the default parameters of a metric.
     * @param ocdMetricTypeStr The name of an ocd metric type.
     * @return A parameter xml.
     * Or an error xml.
     */
    @GET
    @Path("metrics/{OcdMetricType}/parameters/default")
    @Summary("Returns the default parameters of a metric.")
    public String getMetricDefaultParameters(
    		@PathParam("OcdMetricType") String ocdMetricTypeStr)
    {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		OcdMetricType metricType;
    		try {
    			metricType = OcdMetricType.valueOf(ocdMetricTypeStr);
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified metric does not exist.");
	    	}
    		if(!metricFactory.isInstantiatable(metricType)) {
				requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified metric type is not instantiatable: " + metricType.name());
				return requestHandler.writeError(Error.PARAMETER_INVALID, "Specified metric type is not instantiatable: " + metricType.name());
			}
    		if(metricType.correspondsKnowledgeDrivenMeasure())
    		{
    			KnowledgeDrivenMeasure defaultInstance = (KnowledgeDrivenMeasure)metricFactory.getInstance(metricType, new HashMap<String, String>());
    			return requestHandler.writeParameters(defaultInstance.getParameters());
    		}
    		if(metricType.correspondsStatisticalMeasure())
    		{
    			StatisticalMeasure defaultInstance = (StatisticalMeasure)metricFactory.getInstance(metricType, new HashMap<String, String>());
    			return requestHandler.writeParameters(defaultInstance.getParameters());
    		}
    		else {
    			throw new NotImplementedException("Metric type is not properly registered.");
    		}
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
//////////////////////////////////////////////////////////////////////////
//////////// ENUM LISTINGS
//////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns all cover creation type names.
     * @return The types in a names xml.
     * Or an error xml.
     */
    @GET
    @Path("covers/creationmethods")
    @Summary("Returns all cover creation type names.")
    public String getCoverCreationMethodNames()
    {
    	try {
			return requestHandler.writeEnumNames(CoverCreationType.class);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns all algorithm type names.
     * @return The types in a names xml.
     * Or an error xml.
     */
    @GET
    @Path("algorithms")
    @ResourceListApi(description = "Algorithms information")
    @Summary("Returns all algorithm type names.")
    public String getAlgorithmNames()
    {
    	try {
			return requestHandler.writeAlgorithmNames();
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns all ground truth benchmark type names.
     * @return The types in a names xml.
     * Or an error xml.
     */
    @GET
    @Path("benchmarks")
    @Summary("Returns all ground truth benchmark type names.")
    public String getGroundTruthBenchmarkNames()
    {
    	try {
			return requestHandler.writeGroundTruthBenchmarkNames();
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns all graph creation type names.
     * @return The types in a names xml.
     * Or an error xml.
     */
    @GET
    @Path("graphs/creationmethods")
    @Summary("Returns all graph creation type names.")
    public String getGraphCreationMethodNames()
    {
    	try {
			return requestHandler.writeEnumNames(GraphCreationType.class);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns all graph input format names.
     * @return The formats in a names xml.
     * Or an error xml.
     */
    @GET
    @Path("graphs/formats/input")
    @Summary("Returns all graph input format names.")
    public String getGraphInputFormatNames()
    {
    	try {
			return requestHandler.writeEnumNames(GraphInputFormat.class);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns all graph output format names.
     * @return The formats in a names xml.
     * Or an error xml.
     */
    @GET
    @Path("graphs/formats/output")
    @Summary("Returns all graph output format names.")
    public String getGraphOutputFormatNames()
    {
    	try {
			return requestHandler.writeEnumNames(GraphOutputFormat.class);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns all cover output format names.
     * @return The formats in a names xml.
     * Or an error xml.
     */
    @GET
    @Path("covers/formats/output")
    @Summary("Returns all cover creation type names.")
    public String getCoverOutputFormatNames()
    {
    	try {
			return requestHandler.writeEnumNames(CoverOutputFormat.class);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns all cover input format names.
     * @return The formats in a names xml.
     * Or an error xml.
     */
    @GET
    @Path("covers/formats/input")
    @Summary("Returns all cover creation type names.")
    public String getCoverInputFormatNames()
    {
    	try {
			return requestHandler.writeEnumNames(CoverInputFormat.class);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns all statistical measure type names.
     * @return The types in a names xml.
     * Or an error xml.
     */
    @GET
    @Path("metrics/statistical")
    @Summary("Returns all statistical measure type names.")
    public String getStatisticalMeasureNames()
    {
    	try {
			return requestHandler.writeStatisticalMeasureNames();
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns all knowledge-driven measure type names.
     * @return The types in a names xml.
     * Or an error xml.
     */
    @GET
    @Path("metrics/knowledgedriven")
    @Summary("Returns all knowledge-driven measure type names.")
    public String getKnowledgeDrivenMeasureNames()
    {
    	try {
			return requestHandler.writeKnowledgeDrivenMeasureNames();
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Returns all metric type names.
     * @return The types in a names xml.
     * Or an error xml.
     */
    @GET
    @Path("metrics")
    @ResourceListApi(description = "Metrics information")
    @Summary("Returns all metric type names.")
    public String getMetricNames()
    {
    	try {
			return requestHandler.writeEnumNames(OcdMetricType.class);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.writeError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
}
