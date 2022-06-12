package i5.las2peer.services.ocd.adapters.graphInput;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.graphstream.graph.Node;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * A graph input adapter for importing graphs of relations between LMS users from the tech4comp LMS Triplestore. This will NOT import from a file.
 *
 */
public class LmsTripleStoreGraphInputAdapter extends AbstractGraphInputAdapter {
	
	/////////////////
	//// Variables////
	/////////////////
	/**
	* Boolean for showing usernames or URIs as node names
	*/
	private Boolean showUserNames = false;
	
	/**
	* Comma separated string of URIs of users to be in the graph. If empty, all users will be
	*/
	private String involvedUserURIs = "";
	private ArrayList<String> involvedUsers = new ArrayList<String>();
	
	/**
	 * Starting date of posts to be considered
	 */
	private Date startDate = null;
	
	/**
	 * Ending date of posts to be considered
	 */
	private Date endDate = null;
	
	@Override
	public void setParameter(Map<String, String> param) throws IllegalArgumentException, ParseException {
		if (param.containsKey("showUserNames")) {
			showUserNames = Boolean.parseBoolean(param.get("showUserNames"));
			param.remove("showUserNames");
		}
		if (param.containsKey("involvedUserURIs")) {
			involvedUserURIs = param.get("involvedUserURIs");
		
			if(!involvedUserURIs.equals("")) {
				involvedUsers = new ArrayList<String>();
				
				String[] userUriArray = involvedUserURIs.split(",");
				for (String str : userUriArray) {
					involvedUsers.add(str);
				}			
			}
			param.remove("involvedUserURIs");
		}
		SimpleDateFormat df = new SimpleDateFormat ("yyyy-MM-dd");		
		if(param.containsKey("startDate")){
			startDate = df.parse(param.get("startDate"));
			param.remove("startDate");
		}
		if(param.containsKey("endDate")){
			endDate = df.parse(param.get("endDate"));
			param.remove("endDate");
		}
		if(!param.isEmpty()) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public CustomGraph readGraph() throws AdapterException {
		CustomGraph graph = new CustomGraph();
		Map<String, Node> nodeIds = new HashMap<String, Node>();
		
		HashMap<String, String> users = getUsers();
		try {
			if(!involvedUsers.isEmpty()) {
				//System.out.println("Users: " + involvedUsers + " " + involvedUsers.size() + " <" + involvedUserURIs + ">");
				int i = 0;
				for(String userUri : involvedUsers) {
					if(users.containsKey(userUri)) {
						Node userNode = graph.addNode(Integer.toString(i++));
						if(showUserNames) {
							graph.setNodeName(userNode, users.get(userUri));
						}
						else {
							graph.setNodeName(userNode, userUri);
						}
						
						nodeIds.put(userUri, userNode);
					}
				}
			}
			else {
				//create nodes for all users
				int i = 0;
				for(Map.Entry<String, String> user : users.entrySet()) {
					Node userNode = graph.addNode(Integer.toString(i++));
					if(showUserNames) {
						graph.setNodeName(userNode, user.getValue());
					}
					else {
						graph.setNodeName(userNode, user.getKey());
					}
					
					nodeIds.put(user.getKey(), userNode);
				}
			}
		}
		catch (Exception e) {
			throw new AdapterException("Could not parse users");
		}
		
		try {
			//Iterate through each user, get their created resources. Then get the other users that interacted with those and draw edges from them to the user
			for(Map.Entry<String, Node> user : nodeIds.entrySet()) { 
				ArrayList<String> resources = getCreatedResources(user.getKey());
				ArrayList<String> interactingUsers = getInteractingUsers(resources);
				
				for(String interactingUser : interactingUsers) {
					//System.out.println("USERS: " + user.getKey() + " " + interactingUser);
					if(nodeIds.containsKey(interactingUser)) {
						graph.addEdge(UUID.randomUUID().toString(), nodeIds.get(interactingUser), user.getValue());
					}
				}
			}
		}
		catch (Exception e) {
			throw new AdapterException("Could not parse resources");
		}
		
		return graph;
	}
	
	/**
	 * Queries all usernames from the LMS Triplestore
	 * @return A HashMap of all profiles with their urls as keys and their names as values
	 */
	private HashMap<String, String> getUsers() {
				
		String usersQueryString = 
			    "PREFIX w3: <http://www.w3.org/2000/01/rdf-schema#> " +
			    "PREFIX leip: <http://uni-leipzig.de/tech4comp/ontology/> " +
			    "SELECT DISTINCT ?profile ?user " +
			    "WHERE { " +
			    " GRAPH <https://triplestore.tech4comp.dbis.rwth-aachen.de/LMSData/data> { " +
			    "      ?profile a leip:user . " +
			    "      ?profile w3:label ?user . " +
			    "      } " +
			    "	}";
		
		Query q = QueryFactory.create(usersQueryString);
		QueryEngineHTTP qexec = new QueryEngineHTTP("https://triplestore.tech4comp.dbis.rwth-aachen.de/LMSData/query", q);
		qexec.addParam("Content-Type", "application/sparql-query");
		qexec.addParam("Accept", "application/json");
		
		ResultSet res = qexec.execSelect();
		
		HashMap<String, String> users = new HashMap<String, String>();
		for(QuerySolution sol : ResultSetFormatter.toList(res)) {
			users.put(sol.getResource("profile").getURI(), sol.getLiteral("user").getString());
		}
		
		qexec.close();
				
		return users;
	}
	
	/**
	 * Queries all links of LMS resources a specific profile has posted/completed
	 * @return A list of all resources
	 */
	private ArrayList<String> getCreatedResources(String profile) {
				
		//Query to get all resources a user is involved in through posts or completions
		String userResourcesQueryString = 
			"PREFIX w3: <http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX leip: <http://uni-leipzig.de/tech4comp/ontology/> " +
		    "SELECT DISTINCT ?link ?time " +
		    "WHERE { " +
		    " GRAPH <https://triplestore.tech4comp.dbis.rwth-aachen.de/LMSData/data> { " +
		    "      <" + profile + "> w3:label ?user . " +
		    "      <" + profile + "> ?interaction ?post . " +
		    "	   ?post leip:interactionResource ?link . " +
		    "	   ?post leip:timestamp ?time . " +
		    "      } " +
		    "	   FILTER ((?interaction = leip:posted || ?interaction = leip:completed) " +	
		    //"	   		&& (?time >= " + (startDate != null ? startDate.getTime() : 0) + " && ?time <= " + (endDate != null ? endDate.getTime() : Long.MAX_VALUE) + ") " + (This is not used until there are actual numbers instead of a string) 
		    "	 )" +
		    "	} ";
		
		Query q = QueryFactory.create(userResourcesQueryString);
		QueryEngineHTTP qexec = new QueryEngineHTTP("https://triplestore.tech4comp.dbis.rwth-aachen.de/LMSData/query", q);
		qexec.addParam("Content-Type", "application/sparql-query");
		qexec.addParam("Accept", "application/json");
		
		ResultSet res = qexec.execSelect();
		
		ArrayList<String> resources = new ArrayList<String>();
		for(QuerySolution sol : ResultSetFormatter.toList(res)) {
			if((startDate == null || sol.getLiteral("time").getLong() >= startDate.getTime()/1000) && (endDate == null || sol.getLiteral("time").getLong()/1000 <= endDate.getTime())) {
				resources.add(sol.getResource("link").getURI());
			}
		}
		
		qexec.close();
				
		return resources;
	}

	/**
	 * Queries all users that interacted with a set of resources, but did not post or complete them
	 * @return A list of user profile urls
	 */
	private ArrayList<String> getInteractingUsers (ArrayList<String> resources) {
		
		//No query to make if list is empty
		if(resources.isEmpty())
		{
			return new ArrayList<String>();
		}
		
		String resourcesCommaSep = "<" + resources.get(0) + ">";
		for(int i=1; i<resources.size(); i++) {
			resourcesCommaSep += ", <" + resources.get(i) + ">";
		}
				
		String resourcesUserQueryStringInteract = 
				"PREFIX w3: <http://www.w3.org/2000/01/rdf-schema#>" +
				"PREFIX leip: <http://uni-leipzig.de/tech4comp/ontology/> " +
				"SELECT DISTINCT ?profile " +
				"WHERE { " +
				" GRAPH <https://triplestore.tech4comp.dbis.rwth-aachen.de/LMSData/data> { " +
				"      ?profile w3:label ?user . " +
				"      ?profile ?interaction ?post . " +
				"	   ?post leip:interactionResource ?link . " +
				"      } " +
				"	   FILTER ((?interaction != leip:posted && ?interaction != leip:completed)" +
				"	   		&& ?link IN (" + resourcesCommaSep + ")) " +
				"	}";
		
		//System.out.println(resourcesUserQueryStringInteract);
		Query q = QueryFactory.create(resourcesUserQueryStringInteract);
		QueryEngineHTTP qexec = new QueryEngineHTTP("https://triplestore.tech4comp.dbis.rwth-aachen.de/LMSData/query", q);
		qexec.addParam("Content-Type", "application/sparql-query");
		qexec.addParam("Accept", "application/json");
		
		ResultSet res = qexec.execSelect();
		
		ArrayList<String> users = new ArrayList<String>();
		for(QuerySolution sol : ResultSetFormatter.toList(res)) {
			users.add(sol.getResource("profile").getURI());
		}
		
		qexec.close();
				
		return users;
	}
}
