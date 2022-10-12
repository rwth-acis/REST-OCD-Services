package i5.las2peer.services.ocd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;

import i5.las2peer.services.ocd.utils.DatabaseConfig;
import i5.las2peer.services.ocd.utils.Database;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.api.security.ServiceAgent;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.security.MessageReceiver;
import i5.las2peer.security.ServiceAgentImpl;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import i5.las2peer.services.ocd.utils.EntityHandler;
import i5.las2peer.services.ocd.utils.RequestHandler;
import i5.las2peer.services.ocd.utils.ThreadHandler;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.tools.LocalNodeStarter;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;

/**
 * Test service calls
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
	private static String SawmillGraphKey;
	private static long DolphinsGraphId;
	private static String DolphinsGraphKey;
	private static long AperiodicTwoCommunitiesGraphId;
	private static String AperiodicTwoCommunitiesGraphKey;
	
	private static Database database;
	private static RequestHandler requestHandler = new RequestHandler();
	//private static EntityHandler entityHandler = new EntityHandler();

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
		node = (new LocalNodeManager()).newNode(); //TODO: Check if sensible
		testAgent = MockAgentFactory.getAdam();
		testAgent.unlock(testPass); // agent must be unlocked in order
												// to be stored
		node.storeAgent(testAgent);
		node.launch();

		// during testing, the specified service version does not matter
		ServiceAgent testService = ServiceAgentImpl
				.createServiceAgent(ServiceNameVersion.fromString(ServiceClass.class.getName() + "@1.0"), "a pass");
		testService.unlock("a pass");

		if(testService instanceof MessageReceiver) {
			node.registerReceiver((MessageReceiver) testService);
		}
		else {
			throw new Exception("Error: testService is not an instance of MessageReceiver");
		}
		

		// start connector
		logStream = new ByteArrayOutputStream();

		connector = new WebConnector(true, HTTP_PORT, false, 1000);
		connector.setLogStream(new PrintStream(logStream));
		connector.start(node);
		Thread.sleep(1000); // wait a second for the connector to become ready
		testAgent = MockAgentFactory.getAdam();

		/*
		 * Sets up the database environment for testing.
		 */

		setupDatabase();

	}

	/**
	 * Sets up the database environment for testing.
	 * 
	 * @throws AdapterException
	 * @throws FileNotFoundException
	 * @throws ParserConfigurationException
	 */
	private static void setupDatabase() throws AdapterException, FileNotFoundException, ParserConfigurationException {
		/*
		 * Set db content
		 */
		DatabaseConfig.setConfigFile(false);
		database = new Database();
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		createGraph(graph);
		AperiodicTwoCommunitiesGraphKey = graph.getKey();
		graph = OcdTestGraphFactory.getDolphinsGraph();
		createGraph(graph);
		DolphinsGraphKey = graph.getKey();
		graph = OcdTestGraphFactory.getSawmillGraph();
		createGraph(graph);
		SawmillGraphKey = graph.getKey();
	}


	/**
	 * Persists a graph for database setup.
	 * 
	 * @param graph CustomGraph
	 * @throws AdapterException
	 * @throws FileNotFoundException
	 * @throws ParserConfigurationException
	 */
	public static void createGraph(CustomGraph graph)
			throws AdapterException, FileNotFoundException, ParserConfigurationException {
		graph.setUserName(testAgent.getLoginName());
		database.storeGraph(graph);
		System.out.println(requestHandler.writeId(graph));
	}
	
//	/**
//	 * Persists a simulation for database setup.
//	 * 
//	 * @param simulation
//	 * @throws AdapterException
//	 * @throws FileNotFoundException
//	 * @throws ParserConfigurationException
//	 */
//	public static long createSimulation(SimulationSeries simulation)
//			throws AdapterException, FileNotFoundException, ParserConfigurationException {
//		simulation.setUserId(testAgent.getIdentifier());
//		EntityManager em = entityHandler.getEntityManager();
//		EntityTransaction tx = em.getTransaction();
//		long sId;
//		try {
//			tx.begin();
//			em.persist(simulation);
//			em.flush();
//			sId = simulation.getId();
//			tx.commit();
//		} catch (RuntimeException e) {
//			if (tx != null && tx.isActive()) {
//				tx.rollback();
//			}
//			throw e;
//		}
//		em.close();
//		return sId;
//	}

	/**
	 * Called after the tests have finished. Shuts down the server and prints
	 * out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void shutDownServer() throws Exception {
		ThreadHandler t = new ThreadHandler();
		String user = testAgent.getLoginName();
		database.deleteGraph(user, AperiodicTwoCommunitiesGraphKey, t);
		database.deleteGraph(user, DolphinsGraphKey, t);
		database.deleteGraph(user, SawmillGraphKey, t);
		database.deleteUserInactivityData("adam", null);
		
		connector.stop();
		node.shutDown();

		connector = null;
		node = null;

		//node.reset(); TODO: Should not be needed anymore, check if NodeManager needs to be remembered and thus reset here

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
	public void validateLogin() {
		MiniClient c = new MiniClient();
		c.setConnectorEndpoint(HTTP_ADDRESS +":"+ HTTP_PORT);

		try {
			c.setLogin(testAgent.getIdentifier(), testPass);
			ClientResponse result = c.sendRequest("GET", mainPath + "validate", "");
			assertEquals(200, result.getHttpCode());
			System.out.println("Result of 'testValidateLogin': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}

	}

	///////////////////////////// Graphs /////////////////////////////

	@Test
	public void getGraphMetaXMLFormat() throws AdapterException, FileNotFoundException {
		MiniClient c = new MiniClient();
		c.setConnectorEndpoint(HTTP_ADDRESS +":"+ HTTP_PORT);
		try {
			c.setLogin(testAgent.getIdentifier(), testPass);
			System.out.println("SEND REQUEST : ");
			ClientResponse result = c.sendRequest("GET",
					mainPath + "graphs/" + SawmillGraphKey + "?outputFormat=META_XML", "");
			System.out.println("GOT AN ANSWER");
			System.out.println("Result of 'testGetGraphs' on Sawmill: " + result.getResponse().trim());
			assertEquals(200, result.getHttpCode());

			result = c.sendRequest("GET", mainPath + "graphs/" + DolphinsGraphKey + "?outputFormat=META_XML", "");
			System.out.println("Result of 'testGetGraphs' on Dolphins: " + result.getResponse().trim());
			assertEquals(200, result.getHttpCode());

			result = c.sendRequest("GET",
					mainPath + "graphs/" + AperiodicTwoCommunitiesGraphKey + "?outputFormat=META_XML", "");
			System.out.println("Result of 'testGetGraphs' on AperiodicTwoCommunities: " + result.getResponse().trim());
			assertEquals(200, result.getHttpCode());
			System.out.println("-------XML -------");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}
	}

	@Test
	public void getGraphPropertiesXMLFormat() throws AdapterException, FileNotFoundException {
		MiniClient c = new MiniClient();
		c.setConnectorEndpoint(HTTP_ADDRESS +":"+ HTTP_PORT);
		try {
			c.setLogin(testAgent.getIdentifier(), testPass);
			ClientResponse result = c.sendRequest("GET",
					mainPath + "graphs/" + DolphinsGraphKey + "?outputFormat=PROPERTIES_XML", "");		//TODO changes
			System.out.println("Result of 'testGetGraphs' on Dolphins: " + result.getResponse().trim());
			System.out.println("-------XML -------");
			assertEquals(200, result.getHttpCode());

		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}
	}

	@Test
	public void getGraphInvalidID() throws AdapterException, FileNotFoundException {
		MiniClient c = new MiniClient();
		c.setConnectorEndpoint(HTTP_ADDRESS +":"+ HTTP_PORT);
		try {
			c.setLogin(testAgent.getIdentifier(), testPass);

			ClientResponse result = c.sendRequest("GET", mainPath + "graphs/" + 999 + "?outputFormat=META_XML", "");
			System.out.println("Result of 'testGetGraphs' on Sawmill: " + result.getResponse().trim());
			assertEquals(400, result.getHttpCode());

			result = c.sendRequest("GET", mainPath + "graphs/" + -4 + "?outputFormat=META_XML", "");
			System.out.println("Result of 'testGetGraphs' on Sawmill: " + result.getResponse().trim());
			assertEquals(400, result.getHttpCode());

		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}
	}

	@Test
	public void getEnumListingProperties() throws AdapterException, FileNotFoundException {
		MiniClient c = new MiniClient();
		c.setConnectorEndpoint(HTTP_ADDRESS +":"+ HTTP_PORT);
		try {
			c.setLogin(testAgent.getIdentifier(), testPass);
			ClientResponse result = c.sendRequest("GET", mainPath + "graphs/properties", "");
			assertEquals(200, result.getHttpCode());

		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
		}
	}

	///////////////////////////// Simulations /////////////////////////////
	
//	@Test
//	public void getSimulation() throws AdapterException, FileNotFoundException {
//		MiniClient c = new MiniClient();
//		c.setConnectorEndpoint(HTTP_ADDRESS +":"+ HTTP_PORT);
//		
//		SimulationSeries s1 = new SimulationSeries();
//		s1.setName("name");
//		long id1 = 0;
//		SimulationSeries s2 = new SimulationSeries();
//		s1.setName("name2");
//		long id2 = 0;
//		
//		try {
//			id1 = createSimulation(s1);
//			id2 = createSimulation(s2);
//		} catch (ParserConfigurationException e1) {
//			e1.printStackTrace();
//		}	
//		System.out.print(id1);
//		
//		try {
//			c.setLogin(testAgent.getIdentifier(), testPass);
//
//			ClientResponse result = c.sendRequest("GET",
//					mainPath + "simulation/" + 124, "");
//			System.out.println("Result of 'getSimulation' " + result.getResponse().trim());
//			assertEquals(400, result.getHttpCode());
//			
//			result = c.sendRequest("GET",
//					mainPath + "simulation/" + id1, "");
//			System.out.println("Result of 'getSimulation' " + result.getResponse().trim());
//			assertEquals(200, result.getHttpCode());
//			
//			result = c.sendRequest("GET",
//					mainPath + "simulation/" + id2, "");
//			System.out.println("Result of 'getSimulation' " + result.getResponse().trim());
//			assertEquals(200, result.getHttpCode());
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail("Exception: " + e);
//		}
//	}
//	
//	@Test
//	public void startSimulation() throws AdapterException, FileNotFoundException {
//		MiniClient c = new MiniClient();
//		c.setConnectorEndpoint(HTTP_ADDRESS +":"+ HTTP_PORT);
//		
//		try {
//			c.setLogin(testAgent.getIdentifier(), testPass);
//			ClientResponse result = c.sendRequest("POST",
//					mainPath + "simulation" , "{\"graphId\":2,\"dynamic\":\"Moran\",\"dynamicValues\":[],\"payoffCC\":1.0,\"payoffCD\":1.0,\"payoffDC\":1.0,\"payoffDD\":1.0,\"iterations\":20}", "application/json", "", new HashMap<>());
//			System.out.println("Result of 'startSimulation' " + result.getResponse().trim());
//			assertEquals(400, result.getHttpCode());
//			
//			c.setLogin(testAgent.getIdentifier(), testPass);
//			result = c.sendRequest("POST",
//					mainPath + "simulation" , "{\"graphId\":2,\"dynamic\":\"Moran\",\"dynamicValues\":[],\"payoffValues\":[1.0,2.0,3.1,0.0],\"iterations\":20}", "application/json", "", new HashMap<>());
//			System.out.println("Result of 'startSimulation' " + result.getResponse().trim());
//			assertEquals(400, result.getHttpCode());
//
//						
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail("Exception: " + e);
//		}
//	}
//	
}
