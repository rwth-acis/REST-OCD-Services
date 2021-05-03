package i5.las2peer.services.ocd.adapters.graphInput;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.base.Edge;
import y.base.Node;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * A graph input adapter for importing graphs of relations between LMS users from the tech4comp LMS Triplestore. This will NOT import from a file.
 *
 */
public class LmsTripleStoreGraphInputAdapter extends AbstractGraphInputAdapter {
	@Override
	public void setParameter(Map<String, String> param) throws IllegalArgumentException, ParseException {
		
	}
	
	@Override
	public CustomGraph readGraph() throws AdapterException {
		CustomGraph graph = new CustomGraph();
		Map<String, Node> nodeIds = new HashMap<String, Node>();
		
		HashMap<String, String> users = getUsers();
		
		//create nodes for all users
		for(Map.Entry<String, String> user : users.entrySet()) {
			Node userNode = graph.createNode(); 
			graph.setNodeName(userNode, user.getValue());
			nodeIds.put(user.getKey(), userNode);
		}
		
		//Iterate through each user, get their created resources. Then get the other users that interacted with those and draw edges from them to the user
		for(Map.Entry<String, Node> user : nodeIds.entrySet()) { 
			ArrayList<String> resources = getCreatedResources(user.getKey());
			ArrayList<String> interactingUsers = getInteractingUsers(resources);
			
			for(String interactingUser : interactingUsers) {
				//System.out.println("USERS: " + user.getKey() + " " + interactingUser);
				graph.createEdge(nodeIds.get(interactingUser), user.getValue());
			}
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
		    "SELECT DISTINCT ?link " +
		    "WHERE { " +
		    " GRAPH <https://triplestore.tech4comp.dbis.rwth-aachen.de/LMSData/data> { " +
		    "      <" + profile + "> w3:label ?user . " +
		    "      <" + profile + "> ?interaction ?post . " +
		    "	   ?post leip:interactionResource ?link . " +
		    "      } " +
		    "	   FILTER (?interaction = leip:posted || ?interaction = leip:completed) " +	
		    "	} ";
		
		Query q = QueryFactory.create(userResourcesQueryString);
		QueryEngineHTTP qexec = new QueryEngineHTTP("https://triplestore.tech4comp.dbis.rwth-aachen.de/LMSData/query", q);
		qexec.addParam("Content-Type", "application/sparql-query");
		qexec.addParam("Accept", "application/json");
		
		ResultSet res = qexec.execSelect();
		
		ArrayList<String> resources = new ArrayList<String>();
		for(QuerySolution sol : ResultSetFormatter.toList(res)) {
			resources.add(sol.getResource("link").getURI());
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
		
		System.out.println(resourcesUserQueryStringInteract);
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
