package i5.las2peer.services.ocd;

import i5.las2peer.api.Service;
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
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.ocd.adapters.coverInput.CoverInputFormat;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputFormat;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputFormat;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputFormat;
import i5.las2peer.services.ocd.algorithms.AlgorithmLog;
import i5.las2peer.services.ocd.algorithms.AlgorithmType;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.benchmarks.BenchmarkLog;
import i5.las2peer.services.ocd.benchmarks.BenchmarkType;
import i5.las2peer.services.ocd.benchmarks.GroundTruthBenchmarkModel;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverId;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomGraphId;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.utils.Error;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import i5.las2peer.services.ocd.utils.RequestHandler;
import i5.las2peer.services.ocd.utils.ThreadHandler;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.la4j.matrix.sparse.CCSMatrix;



/**
 * 
 * LAS2peer Service
 * 
 * This is a template for a very basic LAS2peer service
 * that uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * @author Peter de Lange
 *
 */

@Produces("text/xml")
@Path("ocd")
@Version("0.1")
public class ServiceClass extends Service {
		
	/*
	 * Init service.
	 */
	static {
		RequestHandler reqHandler = new RequestHandler();
		reqHandler.log(Level.INFO, "Overlapping Community Detection Service started.");
	}
	
	private ThreadHandler threadHandler = new ThreadHandler();
	
	private RequestHandler requestHandler = new RequestHandler();
	
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
     * 
     * Simple function to validate a user login.
     * Basically it only serves as a "calling point" and does not really validate a user
     *(since this is done previously by LAS2peer itself, the user does not reach this method
     * if he or she is not authenticated).
     * 
     */
    @GET
    @Path("validate")
    public String validateLogin()
    {
    	try {
    		return requestHandler.getConfirmationXml();
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }

//////////////////////////////////////////////////////////////////////////
//////////// GRAPHS
//////////////////////////////////////////////////////////////////////////
    
    /**
     * Creates a new graph.
     * @param graphInputFormatStr The id of the used graph input format.
     * @param contentStr A graph in the defined format.
     * @return The graph's id.
     * Or an error xml.
     */
    @POST
    @Path("graph/name/{name}/inputFormat/{GraphInputFormat}")
    public String createGraph(@PathParam("name") String nameStr, 
    		@PathParam("GraphInputFormat") String graphInputFormatStr, @ContentParam String contentStr)
    {
    	try {
	    	String username = ((UserAgent) getActiveAgent()).getLoginName();
	    	GraphInputFormat format;
	    	CustomGraph graph;
	    	try {
		    	format = GraphInputFormat.valueOf(graphInputFormatStr);
	    	} catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
	    		return requestHandler.getError(Error.PARAMETER_INVALID, "Specified input format does not exist.");
	    	}
	    	GraphInputAdapter adapter = format.getAdapterInstance();
		    Reader reader = new StringReader(contentStr);
		    adapter.setReader(reader);
	    	try {
	    		graph = adapter.readGraph();
	    	} catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
	    		return requestHandler.getError(Error.PARAMETER_INVALID, "Input graph does not correspond the specified format.");
	    	}
	    	graph.setUserName(username);
	    	graph.setName(nameStr);
	    	GraphProcessor processor = new GraphProcessor();
	    	processor.determineGraphTypes(graph);
	    	EntityManager em = requestHandler.getEntityManager();
	    	EntityTransaction tx = em.getTransaction();
			try {
				tx.begin();
				em.persist(graph);
				tx.commit();
			} catch( RuntimeException e ) {
				if( tx != null && tx.isActive() ) {
					tx.rollback();
				}
				throw e;
			}
			em.close();
	    	return requestHandler.getId(graph);
    	} catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
			return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}    	
    }
    
    /**
     * Returns stored graphs.
     * @param firstIndexStr Optional query parameter. The result list index of the first id to return. Defaults to 0.
     * @param lengthStr Optional query parameter. The number of ids to return. Defaults to Long.MAX_VALUE.
     * @param includeMetaStr Optional query parameter. If TRUE, instead of the ids the META XML of each graph is returned. Defaults to FALSE.
     * @param executionStatusesStr Optional query parameter. If set only those graphs are returned whose benchmark has one of the given ExecutionStatus names.
     * Multiple status names are separated using the "-" delimiter.
     * @return The graphs.
     * Or an error xml.
     */
    @GET
    @Path("graphs")
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
	    			List<String> executionStatusesStrList = requestHandler.getQueryMultiParam(executionStatusesStr);
	    			for(String executionStatusStr : executionStatusesStrList) {
    					ExecutionStatus executionStatus = ExecutionStatus.valueOf(executionStatusStr);
    					executionStatusIds.add(executionStatus.getId());
	    			}
	    		}
		    	catch (Exception e) {
		    		requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.getError(Error.PARAMETER_INVALID, "Specified execution status does not exist.");
		    	}
			}
			else {
				for(ExecutionStatus executionStatus : ExecutionStatus.values()) {
					executionStatusIds.add(executionStatus.getId());
				}
			}
			EntityManager em = requestHandler.getEntityManager();
			String queryStr = "SELECT g FROM CustomGraph g"
					+ " JOIN g." + CustomGraph.BENCHMARK_FIELD_NAME + " b"
					+ " WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username"
					+ " AND b." + BenchmarkLog.STATUS_ID_FIELD_NAME + " IN :execStatusIds";
			TypedQuery<CustomGraph> query = em.createQuery(queryStr, CustomGraph.class);
			try {
				int firstIndex = Integer.parseInt(firstIndexStr);
				query.setFirstResult(firstIndex);
			} catch (Exception e) {
				requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "First index is not valid.");
			}
			try {
				if(lengthStr != "") {
					int length = Integer.parseInt(lengthStr);
					query.setMaxResults(length);
				}
			}  catch (Exception e) {
				requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Length is not valid.");
			}
			boolean includeMeta;
			try {
				includeMeta = requestHandler.parseBoolean(includeMetaStr);
	    	}  catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "", e);
	    		return requestHandler.getError(Error.PARAMETER_INVALID, "Include meta is not a boolean value.");
	    	}
			query.setParameter("username", username);
			query.setParameter("execStatusIds", executionStatusIds);
			queryResults = query.getResultList();
			em.close();
			String responseStr;
			if(includeMeta) {
				responseStr = requestHandler.getGraphMetas(queryResults);
			}
			else {
				responseStr = requestHandler.getGraphIds(queryResults);
			}
			return responseStr;
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    @GET
    @Produces("text/plain")
    @Path("graph/{graphId}/outputFormat/{GraphOutputFormat}")
    public String getGraph(@PathParam("graphId") String graphIdStr,
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
				return requestHandler.getError(Error.PARAMETER_INVALID, "Graph id is not valid.");
    		}
    		try {
		    	format = GraphOutputFormat.valueOf(graphOuputFormatStr);
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Specified graph output format does not exist.");
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
					return requestHandler.getError(Error.PARAMETER_INVALID, "Graph does not exist: graph id " + graphId);
		    	}
				tx.commit();
			} catch( RuntimeException e ) {
				if( tx != null && tx.isActive() ) {
					tx.rollback();
				}
				throw e;
			}
			em.close();
	    	return requestHandler.getGraph(graph, format.getAdapterInstance());
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    @DELETE
    @Path("graph/{graphId}")
    public String deleteGraph(@PathParam("graphId") String graphIdStr) {
    	try {
    		long graphId;
	    	String username = ((UserAgent) getActiveAgent()).getLoginName();
	    	try {
    			graphId = Long.parseLong(graphIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Graph id is not valid.");
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
						return requestHandler.getError(Error.PARAMETER_INVALID, "Graph does not exist: graph id " + graphId);
			    	}
			    	tx.commit();
    			} catch( RuntimeException e ) {
    				if( tx != null && tx.isActive() ) {
    					tx.rollback();
    				}
    				throw e;
    			}
		    	List<Cover> queryResults;
				String queryStr = "SELECT c from Cover c"
						+ " JOIN c." + Cover.GRAPH_FIELD_NAME + " g"
						+ " WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username"
						+ " AND g." + CustomGraph.ID_FIELD_NAME + " = " + graphId;
				TypedQuery<Cover> query = em.createQuery(queryStr, Cover.class);
				query.setParameter("username", username);
				queryResults = query.getResultList();
				for(Cover cover : queryResults) {
					threadHandler.interruptAll(new CoverId(cover.getId(), id));
					tx = em.getTransaction();
					try {
						tx.begin();
						em.remove(cover);
						tx.commit();
	    			} catch( RuntimeException e ) {
	    				if( tx != null && tx.isActive() ) {
	    					tx.rollback();
	    				}
	    				throw e;
	    			}
				}
				threadHandler.interruptBenchmark(id);
				try {
					tx = em.getTransaction();
					tx.begin();
			    	em.remove(graph);
					tx.commit();
				} catch( RuntimeException e ) {
    				if( tx != null && tx.isActive() ) {
    					tx.rollback();
    				}
    				throw e;
    			}
    		}
	    	return requestHandler.getConfirmationXml();
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
//////////////////////////////////////////////////////////////////////////
//////////// COVERS
//////////////////////////////////////////////////////////////////////////

    /**
     * Returns stored covers.
     * @param firstIndexStr Optional query parameter. The result list index of the first id to return. Defaults to 0.
     * @param lengthStr Optional query parameter. The number of ids to return. Defaults to Long.MAX_VALUE.
     * @param includeMetaStr Optional query parameter. If TRUE, instead of the ids the META XML of each graph is returned. Defaults to FALSE.
     * @param executionStatusesStr Optional query parameter. If set only those covers are returned whose algorithm has one of the given ExecutionStatus names.
     * Multiple status names are separated using the "-" delimiter.
     * @return The covers.
     * Or an error xml.
     */
    @GET
    @Path("covers")
    public String getCovers(@QueryParam(name="firstIndex", defaultValue = "0") String firstIndexStr,
    		@QueryParam(name="length", defaultValue = "") String lengthStr,
    		@QueryParam(name="includeMeta", defaultValue = "FALSE") String includeMetaStr,
    		@QueryParam(name="executionStatuses", defaultValue = "") String executionStatusesStr,
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
					return requestHandler.getError(Error.PARAMETER_INVALID, "Graph id is not valid.");
	    		}
			}
			List<Integer> executionStatusIds = new ArrayList<Integer>();
			if(executionStatusesStr != "") {
	    		try {
	    			List<String> executionStatusesStrList = requestHandler.getQueryMultiParam(executionStatusesStr);
	    			for(String executionStatusStr : executionStatusesStrList) {
    					ExecutionStatus executionStatus = ExecutionStatus.valueOf(executionStatusStr);
    					executionStatusIds.add(executionStatus.getId());
	    			}
	    		}
		    	catch (Exception e) {
		    		requestHandler.log(Level.WARNING, "user: " + username, e);
					return requestHandler.getError(Error.PARAMETER_INVALID, "Specified execution status does not exist.");
		    	}
			}
			else {
				for(ExecutionStatus executionStatus : ExecutionStatus.values()) {
					executionStatusIds.add(executionStatus.getId());
				}
			}
			List<Cover> queryResults;
			EntityManager em = requestHandler.getEntityManager();
			String queryStr = "SELECT c from Cover c"
					+ " JOIN c." + Cover.GRAPH_FIELD_NAME + " g"
					+ " JOIN c." + Cover.ALGORITHM_FIELD_NAME + " a"
					+ " WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username"
					+ " AND a." + AlgorithmLog.STATUS_ID_FIELD_NAME + " IN :execStatusIds";
			if(graphIdStr != "") {
				queryStr += " AND g." + CustomGraph.ID_FIELD_NAME + " = " + graphId;
			}
			TypedQuery<Cover> query = em.createQuery(queryStr, Cover.class);
			try {
				int firstIndex = Integer.parseInt(firstIndexStr);
				query.setFirstResult(firstIndex);
			} catch (Exception e) {
				requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "First index is not valid.");
			}
			try {
				if(lengthStr != "") {
					int length = Integer.parseInt(lengthStr);
					query.setMaxResults(length);
				}
			}  catch (Exception e) {
				requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Length is not valid.");
			}
			boolean includeMeta;
			try {
				includeMeta = requestHandler.parseBoolean(includeMetaStr);
	    	}  catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "", e);
	    		return requestHandler.getError(Error.PARAMETER_INVALID, "Include meta is not a boolean value.");
	    	}
			query.setParameter("username", username);
			query.setParameter("execStatusIds", executionStatusIds);
			queryResults = query.getResultList();
			em.close();
			String responseStr;
			if(includeMeta) {
				responseStr = requestHandler.getCoverMetas(queryResults);
			}
			else {
				responseStr = requestHandler.getCoverIds(queryResults);
			}
			return responseStr;
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    @GET
    @Produces("text/plain")
    @Path("cover/{coverId}/graph/{graphId}/outputFormat/{CoverOutputFormat}")
    public String getCover(@PathParam("graphId") String graphIdStr, @PathParam("coverId") String coverIdStr, @PathParam("CoverOutputFormat") String coverOutputFormatStr) {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		long graphId;
    		try {
    			graphId = Long.parseLong(graphIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Graph id is not valid.");
    		}
    		long coverId;
    		try {
    			coverId = Long.parseLong(coverIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Cover id is not valid.");
    		}
    		CoverOutputFormat format;
    		try {
		    	format = CoverOutputFormat.valueOf(coverOutputFormatStr);
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Specified cover output format does not exist.");
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
			} catch( RuntimeException e ) {
				if( tx != null && tx.isActive() ) {
					tx.rollback();
				}
				throw e;
			}
	    	if(cover == null) {
	    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Cover does not exist.");
	    	}
	    	return requestHandler.getCover(cover, format.getAdapterInstance());
    	}     	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    /**
     * Deletes a cover.
     * If the cover is still being created by an algorithm, the algorithm is terminated as well.
     * If metrics are running on the cover, they are also terminated.
     * @param coverIdStr The cover id.
     * @param graphIdStr The graph id of the graph corresponding the cover.
     * @return A confirmation xml.
     * Or an error xml.
     */
    @DELETE
    @Path("cover/{coverId}/graph/{graphId}")
    public String deleteCover(@PathParam("coverId") String coverIdStr, @PathParam("graphId") String graphIdStr) {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		long graphId;
    		try {
    			graphId = Long.parseLong(graphIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Graph id is not valid.");
    		}
    		long coverId;
    		try {
    			coverId = Long.parseLong(coverIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Cover id is not valid.");
    		}
    		EntityManager em = requestHandler.getEntityManager();
	    	CustomGraphId gId = new CustomGraphId(graphId, username);
	    	CoverId cId = new CoverId(coverId, gId);
    		synchronized(threadHandler) {
    			/*
    			 * Finds cover
    			 */
    			EntityTransaction tx = em.getTransaction();
		    	Cover cover;
		    	try {
					tx.begin();
					cover = em.find(Cover.class, cId);
					tx.commit();
				} catch( RuntimeException e ) {
					if( tx != null && tx.isActive() ) {
						tx.rollback();
					}
					throw e;
				}
		    	if(cover == null) {
		    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
					return requestHandler.getError(Error.PARAMETER_INVALID, "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
		    	}
		    	/*
		    	 * Interrupts algorithms and metrics
		    	 */
		    	threadHandler.interruptAll(cId);
		    	/*
		    	 * Removes cover
		    	 */
		    	tx = em.getTransaction();
		    	try {
					tx.begin();
					em.remove(cover);
					tx.commit();
				} catch( RuntimeException e ) {
					if( tx != null && tx.isActive() ) {
						tx.rollback();
					}
					throw e;
				}
    			em.close();
    			return requestHandler.getConfirmationXml();
    		}
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
//////////////////////////////////////////////////////////////////////////
//////////// ALGORITHMS
//////////////////////////////////////////////////////////////////////////
    
    @POST
    @Path("cover/graph/{graphId}/name/{name}/algorithm/{AlgorithmType}")
    public String runAlgorithm(@PathParam("AlgorithmType") String algorithmTypeStr, @PathParam("graphId") String graphIdStr,
    		@PathParam("name") String nameStr, @ContentParam String content,
    		@QueryParam(name = "componentNodeCountFilter", defaultValue = "0") String componentNodeCountFilterStr) {
    	try {
    		int componentNodeCountFilter;
    		long graphId;
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		AlgorithmType algorithmType;
    		try {
    			graphId = Long.parseLong(graphIdStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Graph id is not valid.");
    		}
    		try {
    			componentNodeCountFilter = Integer.parseInt(componentNodeCountFilterStr);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Component node count filter is not valid.");
    		}
    		try {
    			algorithmType = AlgorithmType.valueOf(algorithmTypeStr);
    			if(algorithmType == AlgorithmType.UNDEFINED || algorithmType == AlgorithmType.GROUND_TRUTH) {
    				requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified algorithm type is not valid for this request: " + algorithmType.name());
    				return requestHandler.getError(Error.PARAMETER_INVALID, "Specified algorithm type is not valid for this request: " + algorithmType.name());
    			}
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Specified algorithm does not exist.");
	    	}
    		OcdAlgorithm algorithm;
    		Map<String, String> parameters;
    		try {
    			parameters = requestHandler.readParameters(content);
    			algorithm = algorithmType.getAlgorithmInstance(parameters);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Parameters are not valid.");
    		}
    		Cover cover;
	    	EntityManager em = requestHandler.getEntityManager();
	    	CustomGraphId id = new CustomGraphId(graphId, username);
	    	AlgorithmLog log;
	    	synchronized(threadHandler) {
		    	EntityTransaction tx = em.getTransaction();
		    	CustomGraph graph;
		    	try {
					tx.begin();
					graph = em.find(CustomGraph.class, id);
			    	if(graph == null) {
			    		requestHandler.log(Level.WARNING, "user: " + username + ", " + "Graph does not exist: graph id " + graphId);
						return requestHandler.getError(Error.PARAMETER_INVALID, "Graph does not exist: graph id " + graphId);
			    	}
			    	cover = new Cover(graph, new CCSMatrix(graph.nodeCount(), 0));
			    	log = new AlgorithmLog(algorithmType, parameters, algorithm.compatibleGraphTypes());
			    	cover.setAlgorithm(log);
			    	cover.setName(nameStr);
			    	em.persist(cover);
					tx.commit();
				} catch( RuntimeException e ) {
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
	    	return requestHandler.getId(cover);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
//////////////////////////////////////////////////////////////////////////
//////////// BENCHMARKS
//////////////////////////////////////////////////////////////////////////
    
    /**
     * Creates a ground truth benchmark cover.
     * @param coverNameStr The name of the cover.
     * @param graphNameStr The name of the underlying benchmark graph.
     * @param benchmarkTypeStr The benchmark type. Must correspond to a ground truth benchmark.
     * @param contentStr Optional benchmark parameters.
     * @return The id of the cover in creation (which also contains the graph id).
     * Or an error xml.
     */
    @POST
    @Path("cover/name/{coverName}/graphname/{graphName}/benchmark/{BenchmarkType}")
    public String createBenchmarkCover(@PathParam("coverName") String coverNameStr, @PathParam("graphName") String graphNameStr,
    		@PathParam("BenchmarkType") String benchmarkTypeStr, @ContentParam String contentStr) {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		BenchmarkType benchmarkType;
    		try {
    			benchmarkType = BenchmarkType.valueOf(benchmarkTypeStr);
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Specified ground truth benchmark does not exist.");
	    	}
    		if(!benchmarkType.isGroundTruthBenchmark()) {
    			requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified benchmark is not a ground truth benchmark: " + benchmarkType.name());
				return requestHandler.getError(Error.PARAMETER_INVALID, "Specified benchmark is not a ground truth benchmark: "+ benchmarkType.name());
    		}
    		Map<String, String> parameters;
    		GroundTruthBenchmarkModel benchmark;
    		try {
    			parameters = requestHandler.readParameters(contentStr);
    			benchmark = benchmarkType.getGroundTruthBenchmarkInstance(parameters);
    		}
    		catch (Exception e) {
    			requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Parameters are not valid.");
    		}
	    	EntityManager em = requestHandler.getEntityManager();
	    	CustomGraph graph = new CustomGraph();
	    	graph.setName(graphNameStr);
	    	BenchmarkLog log = new BenchmarkLog(benchmarkType, parameters);
	    	log.setStatus(ExecutionStatus.WAITING);
	    	graph.setBenchmark(log);
	    	Cover cover = new Cover(graph, new CCSMatrix(graph.nodeCount(), 0));
	    	cover.setName(coverNameStr);
	    	synchronized(threadHandler) {
		    	EntityTransaction tx = em.getTransaction();
		    	try {
					tx.begin();
					em.persist(graph);
			    	em.persist(cover);
					tx.commit();
				} catch( RuntimeException e ) {
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
	    	return requestHandler.getId(cover);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    
//////////////////////////////////////////////////////////////////////////
//////////// DEFAULT PARAMETERS
//////////////////////////////////////////////////////////////////////////    
    
    @GET
    @Path("algorithm/{AlgorithmType}/parameters/default")
    public String getAlgorithmDefaultParams(@PathParam("AlgorithmType") String algorithmTypeStr)
    {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		AlgorithmType algorithmType;
    		try {
    			algorithmType = AlgorithmType.valueOf(algorithmTypeStr);
    			if(algorithmType == AlgorithmType.UNDEFINED || algorithmType == AlgorithmType.GROUND_TRUTH) {
    				requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified algorithm type is not valid for this request: " + algorithmType.name());
    				return requestHandler.getError(Error.PARAMETER_INVALID, "Specified algorithm type is not valid for this request: " + algorithmType.name());
    			}
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Specified algorithm does not exist.");
	    	}
			OcdAlgorithm defaultInstance = algorithmType.getAlgorithmInstance(new HashMap<String, String>());
			return requestHandler.writeParameters(defaultInstance.getParameters());
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    @GET
    @Path("benchmark/{BenchmarkType}/parameters/default")
    public String getBenchmarkDefaultParams(@PathParam("BenchmarkType") String benchmarkTypeStr)
    {
    	try {
    		String username = ((UserAgent) getActiveAgent()).getLoginName();
    		BenchmarkType benchmarkType;
    		try {
    			benchmarkType = BenchmarkType.valueOf(benchmarkTypeStr);
    			if(benchmarkType == BenchmarkType.UNDEFINED || benchmarkType == BenchmarkType.REAL_WORLD) {
    				requestHandler.log(Level.WARNING, "user: " + username + ", " + "Specified benchmark type is not valid for this request: " + benchmarkType.name());
    				return requestHandler.getError(Error.PARAMETER_INVALID, "Specified benchmark type is not valid for this request: " + benchmarkType.name());
    			}
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Specified algorithm does not exist.");
	    	}
			GroundTruthBenchmarkModel defaultInstance = benchmarkType.getGroundTruthBenchmarkInstance(new HashMap<String, String>());
			return requestHandler.writeParameters(defaultInstance.getParameters());
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
//////////////////////////////////////////////////////////////////////////
//////////// ENUM LISTINGS
//////////////////////////////////////////////////////////////////////////
    
    @GET
    @Path("algorithms/names")
    public String getAlgorithmNames()
    {
    	try {
			return requestHandler.getEnumNames(AlgorithmType.class);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    @GET
    @Path("benchmarks/groundtruth/names")
    public String getBenchmarkNames()
    {
    	try {
			return requestHandler.getGroundTruthBenchmarkNames();
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    @GET
    @Path("graphs/formats/input/names")
    public String getGraphInputFormatNames()
    {
    	try {
			return requestHandler.getEnumNames(GraphInputFormat.class);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    @GET
    @Path("graphs/formats/output/names")
    public String getGraphOutputFormatNames()
    {
    	try {
			return requestHandler.getEnumNames(GraphOutputFormat.class);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    @GET
    @Path("covers/formats/output/names")
    public String getCoverOutputFormatNames()
    {
    	try {
			return requestHandler.getEnumNames(CoverOutputFormat.class);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    @GET
    @Path("covers/formats/input/names")
    public String getCoverInputFormatNames()
    {
    	try {
			return requestHandler.getEnumNames(CoverInputFormat.class);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    @GET
    @Path("metrics/statistical/names")
    public String getStatisticalMeasureNames()
    {
    	try {
			return requestHandler.getStatisticalMeasureNames();
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    @GET
    @Path("metrics/knowledgedriven/names")
    public String getKnowledgeDrivenMeasureNames()
    {
    	try {
			return requestHandler.getKnowledgeDrivenMeasureNames();
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
}
