package i5.las2peer.services.ocd.cooperation.data;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.junit.After;
import org.junit.Test;

import i5.las2peer.services.ocd.cooperation.data.SimulationEntityHandler;
import i5.las2peer.services.ocd.cooperation.data.simulation.AgentData;
import i5.las2peer.services.ocd.cooperation.data.simulation.Evaluation;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationDataset;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesGroup;
import i5.las2peer.services.ocd.graphs.CustomGraph;

public class SimulationEntityHandlerTest {

	private static final String PERSISTENCE_UNIT_NAME = "ocd";
	private static final EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
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
		series.setUserId(userId);

		long seriesId = 0;
		try {
			seriesId = entityHandler.store(series, userId);
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
		assertEquals(userId, resultSeries.getUserId());
		assertEquals(seriesId, resultSeries.getId());
	}

	@Test
	public void getSimulationSeries() {

		SimulationSeries series = new SimulationSeries();
		long userId = 7;
		series.setUserId(7);

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
		assertEquals(userId, resultSeries.getUserId());
		assertEquals(seriesId, resultSeries.getId());

	}
	
	@Test
	public void getSimulationSeriesWithGraph() {

		SimulationSeries series = new SimulationSeries();
		CustomGraph graph = new CustomGraph();
		graph.setName("testGraphName");
		long userId = 7;
		series.setUserId(7);
		series.setNetwork(graph);

		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		em.persist(series);
		em.flush();
		em.getTransaction().commit();
		long seriesId = series.getId();
		long graphId = graph.getId();
		em.close();

		SimulationSeries resultSeries = null;
		try {
			resultSeries = entityHandler.getSimulationSeries(seriesId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertNotNull(resultSeries);
		assertEquals(userId, resultSeries.getUserId());
		assertEquals(seriesId, resultSeries.getId());
		assertNotNull(resultSeries.getNetwork());
		assertEquals("testGraphName", resultSeries.getNetwork().getName());
		assertEquals(graphId, resultSeries.getNetwork().getId());
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
		series1.setUserId(userId);
		
		SimulationSeries series2 = new SimulationSeries();
		series2.setUserId(userId);
		
		SimulationSeries series3 = new SimulationSeries();
		series3.setUserId(22);
		
		SimulationSeries series4 = new SimulationSeries();
		series4.setUserId(userId);
		
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
			resultSeries = entityHandler.getSimulationSeriesByUser(userId);
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
			Id = entityHandler.store(simulation, Id);
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
		s1.setUserId(userId);
		
		SimulationSeriesGroup s2 = new SimulationSeriesGroup();
		s2.setUserId(12);
		
		SimulationSeriesGroup s3 = new SimulationSeriesGroup();
		s3.setUserId(userId);
		
		SimulationSeriesGroup s4 = new SimulationSeriesGroup();
		s4.setUserId(15);
		
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
			resultList = entityHandler.getSimulationSeriesGroups(userId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertNotNull(resultList);
		assertEquals(2, resultList.size());
		
	}
	
}
