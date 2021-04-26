package i5.las2peer.services.ocd.adapters.graphInput;

import static org.junit.Assert.*;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.GraphMlGraphInputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestConstants;

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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class LmsTripleStoreGraphInputAdapterTest {

	@Test
	public void test() throws MalformedURLException, IOException {		
		InputStream input = new URL("https://triplestore.tech4comp.dbis.rwth-aachen.de/LMSData/data").openStream();	
		
//		final BufferedReader reader = new BufferedReader(
//                new InputStreamReader(input));
//        String line = null;
//        while ((line = reader.readLine()) != null) {
//            System.out.println(line);
//        }
//        reader.close();
		
		// Create an empty in-memory model and populate it from the graph
		Model model = ModelFactory.createMemModelMaker().createDefaultModel();
		
		try (InputStream in = input) {
        RDFParser.create()
            .source(in)
            .forceLang(RDFLanguages.TRIG)
            .errorHandler(ErrorHandlerFactory.errorHandlerStrict)
            .parse(model);
		}
		
		
		//model.read(input, null, "TRIG"); // null base URI, since model URIs are absolute
		input.close();

		// Create a new query
		String queryString = 
		    "PREFIX w3: <http://www.w3.org/2000/01/rdf-schema#>" +
		    "PREFIX leip: <http://uni-leipzig.de/tech4comp/ontology/>" +
		    "SELECT ?post " +
		    "WHERE {" +
		    "      ?profile w3:label \"Carlos Aleman\" . " +
		    "      ?profile leip:posted ?post . " +
		    "      }";

		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		// Output query results    
		ResultSetFormatter.out(System.out, results, query);

		// Important stuff! free up resources used running the query
		qe.close();
	}
			
}