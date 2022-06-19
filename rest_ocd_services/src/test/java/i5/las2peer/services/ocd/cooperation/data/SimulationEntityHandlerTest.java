package i5.las2peer.services.ocd.cooperation.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.After;
import org.junit.Test;

import i5.las2peer.services.ocd.cooperation.data.simulation.AgentData;
import i5.las2peer.services.ocd.cooperation.data.simulation.Evaluation;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationDataset;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesGroup;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesParameters;
import i5.las2peer.services.ocd.graphs.CustomGraph;

public class SimulationEntityHandlerTest {

	private static final String PERSISTENCE_UNIT_NAME = "ocd";
	private static final EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME,
			Collections.singletonMap(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML, "META-INF/testing/persistence.xml"));
	private SimulationEntityHandler entityHandler = new SimulationEntityHandler();

	@After
	public void clearDatabase() {

		EntityManager em = factory.createEntityManager();
		EntityTransaction etx = em.getTransaction();
		etx.begin();
		Query query;
		query = em.createQuery("DELETE FROM CustomEdge", CustomGraph.class);
		query.executeUpdate();
		query = em.createQuery("DELETE FROM Community", CustomGraph.class);
		query.executeUpdate();
		query = em.createQuery("DELETE FROM CustomNode", CustomGraph.class);
		query.executeUpdate();
		query = em.createQuery("DELETE FROM Cover", CustomGraph.class);
		query.executeUpdate();
		query = em.createQuery("DELETE FROM CustomGraph", CustomGraph.class);
		query.executeUpdate();
		query = em.createQuery("DELETE FROM AgentData", AgentData.class);
		query.executeUpdate();
		query = em.createQuery("DELETE FROM SimulationDataset", SimulationDataset.class);
		query.executeUpdate();
		query = em.createQuery("DELETE FROM SimulationSeries", SimulationSeries.class);
		query.executeUpdate();
		etx.commit();
	}

	/////////////////////// Simulation Series //////////////////////////
	
	@Test
	public void storeSimulationSeries() {

		SimulationSeries series = new SimulationSeries();
		series.setCooperationEvaluation(new Evaluation(new double[]{1.0,2.0,3.0}));
		long userId = 7;
		series.setUserId(Long.toString(userId));

		long seriesId = 0;
		try {
			seriesId = entityHandler.store(series, Long.toString(userId));
		} catch (Exception e) {
			e.printStackTrace();
		}

		EntityManager em = factory.createEntityManager();
		SimulationSeries resultSeries;
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			resultSeries = em.find(SimulationSeries.class, seriesId);
			tx.commit();
		} catch (RuntimeException e) {
			tx.rollback();
			throw e;
		}
		em.close();

		assertNotNull(resultSeries);
		assertEquals(Long.toString(userId), resultSeries.getUserId());
		assertEquals(seriesId, resultSeries.getId());
	}

	@Test
	public void storeSimulationSeriesWithDatasets() {

		SimulationSeries series = new SimulationSeries();
		SimulationDataset d1 = new SimulationDataset();
		d1.setName("da1");
		SimulationDataset d2 = new SimulationDataset();
		d2.setName("da2");
		List<SimulationDataset> list = new ArrayList<>();
		list.add(d1);
		list.add(d2);
		series.setSimulationDatasets(list);
		long userId = 7;
		series.setUserId(Long.toString(userId));

		long seriesId = 0;
		try {
			seriesId = entityHandler.store(series, Long.toString(userId));
		} catch (Exception e) {
			e.printStackTrace();
		}

		EntityManager em = factory.createEntityManager();
		SimulationSeries resultSeries;
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			resultSeries = em.find(SimulationSeries.class, seriesId);
			tx.commit();
		} catch (RuntimeException e) {
			tx.rollback();
			throw e;
		}
		em.close();

		assertNotNull(resultSeries);
		assertNotNull(resultSeries.getSimulationDatasets());
		assertEquals(2, resultSeries.size());
		assertEquals("da1", resultSeries.getSimulationDatasets().get(0).getName());
	}

	@Test
	public void getSimulationSeries() {

		SimulationSeries series = new SimulationSeries();
		long userId = 7;
		series.setUserId(Integer.toString(7));

		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		em.persist(series);
		em.flush();
		em.getTransaction().commit();
		long seriesId = series.getId();
		em.close();

		SimulationSeries resultSeries = null;
		try {
			resultSeries = entityHandler.getSimulationSeries(seriesId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertNotNull(resultSeries);
		assertEquals(Long.toString(userId), resultSeries.getUserId());
		assertEquals(seriesId, resultSeries.getId());

	}
	
	@Test
	public void getSimulationSeriesWithDatasets() {

		SimulationSeries series = new SimulationSeries();
		long userId = 7;
		series.setUserId(Integer.toString(7));

		SimulationDataset d1 = new SimulationDataset();
		d1.setName("da1");
		SimulationDataset d2 = new SimulationDataset();
		d2.setName("da2");
		List<SimulationDataset> list = new ArrayList<>();
		list.add(d1);
		list.add(d2);

		series.setSimulationDatasets(list);

		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		em.persist(series);
		em.flush();
		em.getTransaction().commit();
		long seriesId = series.getId();
		em.close();

		SimulationSeries resultSeries = null;
		try {
			resultSeries = entityHandler.getSimulationSeries(seriesId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertNotNull(resultSeries);
		assertNotNull(resultSeries.getSimulationDatasets());
		assertEquals(2, resultSeries.size());
		assertEquals("da1", resultSeries.getSimulationDatasets().get(0).getName());

	}

	@Test
	public void getSimulationSeriesByGraphId() {

		SimulationSeries series = new SimulationSeries();
		long userId = 7;
		series.setUserId(Integer.toString(7));

		SimulationSeriesParameters parameters = new SimulationSeriesParameters();
		parameters.setGraphId(23);
		parameters.setGraphName("testGraphName123");
		series.setParameters(parameters);

		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		em.persist(series);
		em.flush();
		em.getTransaction().commit();
		long seriesId = series.getId();
		em.close();

		List<SimulationSeries> resultSeries = null;
		try {
			resultSeries = entityHandler.getSimulationSeriesByUser(Integer.toString(7), 23, 0, 10);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertNotNull(resultSeries);
		assertEquals(resultSeries.size(), 1);
		assertNotNull(resultSeries.get(0).getParameters());
		assertEquals(23, resultSeries.get(0).getParameters().getGraphId());

	}

	@Test
	public void getSimulationSeriesWithGraph() {

		SimulationSeries series = new SimulationSeries();
		CustomGraph graph = new CustomGraph();
		graph.setName("testGraphName");
		long userId = 7;
		series.setUserId(Integer.toString(7));
		series.setNetwork(graph);

		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		em.persist(series);
		em.flush();
		em.getTransaction().commit();
		long seriesId = series.getId();
		long graphId = graph.getPersistenceId();
		em.close();

		SimulationSeries resultSeries = null;
		try {
			resultSeries = entityHandler.getSimulationSeries(seriesId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertNotNull(resultSeries);
		assertEquals(Long.toString(userId), resultSeries.getUserId());
		assertEquals(seriesId, resultSeries.getId());
		assertNotNull(resultSeries.getNetwork());
		assertEquals("testGraphName", resultSeries.getNetwork().getName());
		assertEquals(graphId, resultSeries.getNetwork().getPersistenceId());
	}
	
	@Test
	public void deleteSimulationSeries() {

		SimulationSeries series = new SimulationSeries();

		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		em.persist(series);
		em.flush();
		em.getTransaction().commit();
		long seriesId = series.getId();


		try {
			entityHandler.delete(series);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SimulationSeries resultSeries = null;
		em.getTransaction().begin();		
		resultSeries = em.find(SimulationSeries.class, seriesId);
		em.getTransaction().commit();
		em.close();

		assertNotNull(resultSeries);
		assertEquals(seriesId, resultSeries.getId());		
	}
	
	
	@Test
	public void getSimulationSeriesByUser() {
		
		long userId = 23;
		
		SimulationSeries series1 = new SimulationSeries();
		series1.setUserId(Long.toString(userId));
		
		SimulationSeries series2 = new SimulationSeries();
		series2.setUserId(Long.toString(userId));
		
		SimulationSeries series3 = new SimulationSeries();
		series3.setUserId(Integer.toString(22));
		
		SimulationSeries series4 = new SimulationSeries();
		series4.setUserId(Long.toString(userId));
		
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		em.persist(series1);
		em.persist(series2);
		em.persist(series3);
		em.persist(series4);
		em.flush();
		em.getTransaction().commit();
		em.close();

		List<SimulationSeries> resultSeries = null;
		try {
			resultSeries = entityHandler.getSimulationSeriesByUser(Long.toString(userId));
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertNotNull(resultSeries);
		assertEquals(3, resultSeries.size());
		
	}
	
	
/////////////////////// Simulation Series Group //////////////////////////
	
	@Test
	public void storeSimulationSeriesGroup() {

		SimulationSeriesGroup simulation = new SimulationSeriesGroup();
		long Id = 0;
		try {
			Id = entityHandler.store(simulation, Long.toString(Id));
		} catch (Exception e) {
			e.printStackTrace();
		}

		EntityManager em = factory.createEntityManager();
		SimulationSeriesGroup result;
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			result = em.find(SimulationSeriesGroup.class, Id);
			tx.commit();
		} catch (RuntimeException e) {
			tx.rollback();
			throw e;
		}
		em.close();

		assertNotNull(result);
		assertEquals(Id, result.getId());
	}

	@Test
	public void getSimulationSeriesGroup() {

		SimulationSeriesGroup simulation = new SimulationSeriesGroup();

		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		em.persist(simulation);
		em.flush();
		em.getTransaction().commit();
		long Id = simulation.getId();
		em.close();

		SimulationSeriesGroup result = null;
		try {
			result = entityHandler.getSimulationSeriesGroup(Id);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertNotNull(result);
		assertEquals(Id, result.getId());

	}
	
	@Test
	public void deleteSimulationSeriesGroup() {

		SimulationSeriesGroup simulation = new SimulationSeriesGroup();

		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		em.persist(simulation);
		em.flush();
		em.getTransaction().commit();
		long Id = simulation.getId();


		try {
			entityHandler.delete(simulation);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SimulationSeriesGroup result = null;
		em.getTransaction().begin();		
		result = em.find(SimulationSeriesGroup.class, Id);
		em.getTransaction().commit();
		em.close();

		assertNotNull(result);
		assertEquals(Id, result.getId());

	}
	
	@Test
	public void getSimulationSeriesGroupByUser() {
		
		long userId = 25;
		
		SimulationSeriesGroup s1 = new SimulationSeriesGroup();
		s1.setUserId(Long.toString(userId));
		
		SimulationSeriesGroup s2 = new SimulationSeriesGroup();
		s2.setUserId(Integer.toString(12));
		
		SimulationSeriesGroup s3 = new SimulationSeriesGroup();
		s3.setUserId(Long.toString(userId));
		
		SimulationSeriesGroup s4 = new SimulationSeriesGroup();
		s4.setUserId(Integer.toString(15));
		
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		em.persist(s1);
		em.persist(s2);
		em.persist(s3);
		em.persist(s4);
		em.flush();
		em.getTransaction().commit();
		em.close();

		List<SimulationSeriesGroup> resultList = null;
		try {
			resultList = entityHandler.getSimulationSeriesGroups(Long.toString(userId));
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertNotNull(resultList);
		assertEquals(2, resultList.size());
		
	}
	
}
