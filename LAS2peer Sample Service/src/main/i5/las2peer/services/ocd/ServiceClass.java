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
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputFormat;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputFormat;
import i5.las2peer.services.ocd.graph.CustomGraph;
import i5.las2peer.services.ocd.graph.CustomGraphId;
import i5.las2peer.services.ocd.graph.GraphProcessor;
import i5.las2peer.services.ocd.utils.Error;
import i5.las2peer.services.ocd.utils.RequestHandler;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.xml.parsers.ParserConfigurationException;



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
     * @throws ParserConfigurationException 
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
    
//    /**
//     * 
//     * Another example method.
//     * 
//     * @param myInput
//     * 
//     */
//    @Produces("text/plain")
//    @POST
//    @Path("myMethodPath/{input}")
//    public String exampleMethod( @PathParam("input") String myInput)
//    {
//    	String returnString = "";
//    	returnString += "You have entered " + myInput + "!";
//    	return returnString;
//    }
    
//    /**
//     * 
//     * Another example method.
//     * 
//     * @param myInput
//     * 
//     */
//    @POST
//    @Produces("text/plain")
//    @Path("myMethodPath2")
//    public String exampleMethod2( @ContentParam String myInput)
//    {
//    	String returnString = "";
//    	returnString += "You have entered " + myInput + "!";
//    	return returnString;
//    }
    
    /**
     * Creates a new graph.
     * @param formatIdStr The id of the used graph input format.
     * @param contentStr A graph in the defined format.
     * @return The graph's id
     * Or an error response.
     */
    @POST
    @Path("graph/inputFormat/{inputFormatId}")
    public String createGraph(@PathParam("inputFormatId") String formatIdStr, @ContentParam String contentStr)
    {
    	try {
	    	String username = ((UserAgent) getActiveAgent()).getLoginName();
	    	GraphInputFormat format;
	    	CustomGraph graph;
	    	try {
		    	int formatId = Integer.parseInt(formatIdStr);
		    	format = GraphInputFormat.lookupType(formatId);
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
     * @param firstIndexStr Optional query parameter. The index of the first id to return. Defaults to 0.
     * @param lengthStr Optional query parameter. The number of ids to return. Defaults to Long.MAX_VALUE.
     * @return All graphs between firstIndex (included) and firstIndex + length (excluded).
     * Or an error response.
     */
    @GET
    @Path("graphs")
    public String getGraphs(
    		@QueryParam(name="firstIndex", defaultValue = "0") String firstIndexStr,
    		@QueryParam(name="length", defaultValue = "") String lengthStr)
    {
    	try {
			String username = ((UserAgent) getActiveAgent()).getLoginName();
			List<CustomGraph> queryResults;
			EntityManager em = requestHandler.getEntityManager();
			TypedQuery<CustomGraph> query = em.createQuery("Select g from CustomGraph g where g.userName = :username", CustomGraph.class);
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
			query.setParameter("username", username);
			queryResults = query.getResultList();
			em.close();
			return requestHandler.getIds(queryResults);
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
    @GET
    @Produces("text/plain")
    @Path("graph/{graphId}/format/{outputFormatId}")
    public String getGraph(@PathParam("graphId") String graphIdStr,
    		@PathParam("outputFormatId") String outputFormatIdStr)
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
		    	int formatId = Integer.parseInt(outputFormatIdStr);
		    	format = GraphOutputFormat.lookupType(formatId);
    		}
	    	catch (Exception e) {
	    		requestHandler.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Specified output format does not exist.");
	    	}
	    	EntityManager em = requestHandler.getEntityManager();
	    	CustomGraphId id = new CustomGraphId(graphId, username);
	    	EntityTransaction tx = em.getTransaction();
	    	CustomGraph graph;
	    	try {
				tx.begin();
				graph = em.find(CustomGraph.class, id);
		    	if(graph == null) {
		    		requestHandler.log(Level.WARNING, "user: " + username + ", graph id " + graphId + " is not valid.");
					return requestHandler.getError(Error.PARAMETER_INVALID, "Graph id is not valid.");
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
	    	EntityTransaction tx = em.getTransaction();
	    	try {
				tx.begin();
				CustomGraph graph = em.find(CustomGraph.class, id);
				if(graph != null) {
					em.remove(graph);
				}
				tx.commit();
			} catch( RuntimeException e ) {
				if( tx != null && tx.isActive() ) {
					tx.rollback();
				}
				throw e;
			}
	    	return requestHandler.getConfirmationXml();
    	}
    	catch (Exception e) {
    		requestHandler.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
    }
    
}
