package i5.las2peer.services.ocd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.security.ServiceAgent;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import i5.las2peer.services.ocd.utils.RequestHandler;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.webConnector.WebConnector;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Example Test Class demonstrating a basic JUnit test structure.
 * 
 * @author Peter de Lange
 * 
 */

public class ServiceTest {

	private static final String HTTP_ADDRESS = "http://127.0.0.1";
	private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;

	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static UserAgent testAgent;
	private static final String testPass = "adamspass";

	private static final String testServiceClass = "i5.las2peer.services.ocd.ServiceClass";
	private static final String mainPath = "ocd/";
	private static long SawmillGraphId;
	private static long DolphinsGraphId;
	private static long AperiodicTwoCommunitiesGraphId;
	
	private static RequestHandler requestHandler = new RequestHandler();
	
	/**
	 * Called before the tests start.
	 * 
	 * Sets up the node and initializes connector and users that can be used
	 * throughout the tests.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void startServer() throws Exception {

		// start node
		node = LocalNode.newNode();
		node.storeAgent(MockAgentFactory.getAdam());
		node.launch();

		ServiceAgent testService = ServiceAgent.generateNewAgent(
				testServiceClass, "a pass");
		testService.unlockPrivateKey("a pass");

		node.registerReceiver(testService);

		// start connector
		logStream = new ByteArrayOutputStream();

		connector = new WebConnector(true, HTTP_PORT, false, 1000);
		connector.setSocketTimeout(10000);
		connector.setLogStream(new PrintStream(logStream));
		connector.start(node);
		Thread.sleep(1000); // wait a second for the connector to become ready
		testAgent = MockAgentFactory.getAdam();

		connector.updateServiceList();
		// avoid timing errors: wait for the repository manager to get all
		// services before continuing
		try {
			System.out.println("waiting..");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		/*
		 * Sets up the database environment for testing.
		 */
		
		setupDatabase();

	}

	/*
	 * Sets up the database environment for testing.
	 */
	private static void setupDatabase() throws AdapterException,
			FileNotFoundException, ParserConfigurationException {
		/*
		 * Set db content
		 */
		CustomGraph graph = OcdTestGraphFactory
				.getAperiodicTwoCommunitiesGraph();
		createGraph(graph);
		AperiodicTwoCommunitiesGraphId = graph.getId();
		graph = OcdTestGraphFactory.getDolphinsGraph();
		createGraph(graph);
		DolphinsGraphId = graph.getId();
		graph = OcdTestGraphFactory.getSawmillGraph();
		createGraph(graph);
		SawmillGraphId = graph.getId();
	}

	// Persists a graph for database setup.
	public static void createGraph(CustomGraph graph) throws AdapterException,
			FileNotFoundException, ParserConfigurationException {
		graph.setUserName(testAgent.getLoginName());
		EntityManager em = requestHandler.getEntityManager();
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
		System.out.println(requestHandler.writeId(graph));
	}

	/**
	 * Called after the tests have finished. Shuts down the server and prints
	 * out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void shutDownServer() throws Exception {

		connector.stop();
		node.shutDown();

		connector = null;
		node = null;

		LocalNode.reset();

		System.out.println("Connector-Log:");
		System.out.println("--------------");

		System.out.println(logStream.toString());

	}

	/**
	 * 
	 * Tests the validate method.
	 * 
	 */
	@Test
	public void testValidateLogin() {
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);

		try {
			c.setLogin(Long.toString(testAgent.getId()), testPass);
			ClientResponse result = c.sendRequest("GET", mainPath + "validate",
					"");
			assertEquals(200, result.getHttpCode());
			System.out.println("Result of 'testValidateLogin': "
					+ result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}

	}

	@Test
	public void testGetGraph() throws AdapterException, FileNotFoundException {
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		try {
			c.setLogin(Long.toString(testAgent.getId()), testPass);
			ClientResponse result = c.sendRequest("GET", mainPath + "graphs/"
					+ SawmillGraphId + "?outputFormat=META_XML", "");
			assertEquals(200, result.getHttpCode());
			System.out.println("Result of 'testGetGraphs' on Sawmill: "
					+ result.getResponse().trim());
			result = c.sendRequest("GET", mainPath + "graphs/" + DolphinsGraphId
					+ "?outputFormat=META_XML", "");
			assertEquals(200, result.getHttpCode());
			System.out.println("Result of 'testGetGraphs' on Dolphins: "
					+ result.getResponse().trim());
			result = c.sendRequest("GET",
					mainPath + "graphs/" + AperiodicTwoCommunitiesGraphId
							+ "?outputFormat=META_XML", "");
			assertEquals(200, result.getHttpCode());
			System.out
					.println("Result of 'testGetGraphs' on AperiodicTwoCommunities: "
							+ result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}
	}
	
	/**
	 * Method for debugging purposes. Here the concept of restMapping validation
	 * is shown. It is important to check, if all annotations are correct and
	 * consistent. Otherwise the service will not be accessible by the
	 * WebConnector.
	 * 
	 * @return true, if mapping correct
	 */
	@Test
	public void debugMapping() {
		String XML_LOCATION = "./restMapping.xml";
		/*
		 * Method was adapted manually.
		 * Since debugMapping was moved here from the service class,
		 * the method block of getRestMapping was copied here since that
		 * method cannot be called out of this class.
		 * 
		 * Start of getRestMapping Block
		 */
		String xml="";
        try {
            xml= RESTMapper.getMethodsAsXML(this.getClass());
        } catch (Exception e) {

            e.printStackTrace();
        }
        /*
         * End of getRestMapping Block
         */
		try {
			RESTMapper.writeFile(XML_LOCATION, xml);
		} catch (IOException e) {
			e.printStackTrace();
		}
		XMLCheck validator = new XMLCheck();
		ValidationResult result = validator.validate(xml);
		assertTrue(result.isValid());
	}

}
