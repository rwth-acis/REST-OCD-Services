package i5.las2peer.services.ocd.cooperation.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesGroup;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesParameters;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.DynamicType;
import i5.las2peer.services.ocd.cooperation.simulation.game.GameType;
import i5.las2peer.services.ocd.utils.EntityHandler;

public class SimulationEntityHandler extends EntityHandler {
	
	private static SimulationEntityHandler handler;
	
	public static synchronized SimulationEntityHandler getInstance() {
		if (handler == null) {
			handler = new SimulationEntityHandler();
		}
		return handler;
	}
	
	//////////////////////// Simulation Series //////////////////////////
	
	/**
	 * Return a persisted SimulationSeries
	 * 
	 * @param seriesId Id of the simulation series
	 * @return the requested simulation series
	 */
	public SimulationSeries getSimulationSeries(long seriesId) {

		SimulationSeries series;
		EntityManager em = getEntityManager();
		try {
		em.getTransaction().begin();
		series = em.find(SimulationSeries.class, seriesId);
		em.getTransaction().commit();
		} catch (RuntimeException e) {
			em.getTransaction().rollback();
			throw e;
		}		
	
		return series;
	}

	/**
	 * Persists a SimulationSeries
	 * 
	 * @param series the simulation series
	 * @return the persistence id
	 */
	public synchronized long store(SimulationSeries series, long userId) {
		
		series.setUserId(userId);
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		em.persist(series);
		em.getTransaction().commit();

		long seriesId = series.getId();
		em.close();

		return seriesId;
	}
	
	/**
	 * Removes a SimulatioSeries from the database
	 * 
	 * @param series SimulationSeries
	 */
	public void deleteSeries(long Id) {

		SimulationSeries simulation = getSimulationSeries(Id);
		delete(simulation);
	}
	
	/**
	 * Removes a SimulatioSeries from the database
	 * 
	 * @param series SimulationSeries
	 */
	public synchronized void delete(SimulationSeries series) {

		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		series = em.getReference(SimulationSeries.class, series.getId());
		em.remove(series);
		em.close();
	}
	
	/**
	 * @return all SimulationSeries of a specific user
	 */
	public List<SimulationSeries> getSimulationSeriesByUser(long userId) {

		EntityManager em = getEntityManager();
		TypedQuery<SimulationSeries> query = em.createQuery("SELECT s FROM SimulationSeries s WHERE s.userId = :id", SimulationSeries.class);
		query.setParameter("id", userId);
		List<SimulationSeries> seriesList = query.getResultList();
		return seriesList;
	}
	
	/** 	  
	 * @param userId Id of the user
	 * @param firstIndex Id of the first simulation
	 * @param length Number of simulations
	 * @return List of SimulationSeries of a specific user
	 */
	public List<SimulationSeries> getSimulationSeriesByUser(long userId, int firstIndex, int length) {
		
		EntityManager em = getEntityManager();
		TypedQuery<SimulationSeries> query = em.createQuery("SELECT s FROM SimulationSeries s WHERE s.userId = :id", SimulationSeries.class);
		query.setFirstResult(firstIndex);
		query.setMaxResults(length);
		query.setParameter("id", userId);		
		List<SimulationSeries> seriesList = query.getResultList();
		return seriesList;		
	}
	
	/**
	 * Returns a list of SimulationSeries filtered by their parameters
	 * 
	 * @param parameters
	 * @return List of SimulationsSeries
	 */
	public List<SimulationSeries> getSimulationSeries(long userId, long graphId, DynamicType dynamic, GameType game) {

		EntityManager em = getEntityManager();				 
		TypedQuery<SimulationSeries> query = em.createQuery(
				"SELECT s FROM SimulationSeries s JOIN Parameters p WHERE s.userId =:userId AND p.graphId =:graphId AND p.dynamic =:dynamic AND p.game =:game",
				SimulationSeries.class);
				
		query.setParameter("userId", userId);
		query.setParameter("graphId", graphId);
		query.setParameter("dynamic", dynamic);
		query.setParameter("game", game);
		List<SimulationSeries> seriesList = query.getResultList();
		return seriesList;
	}

	
	/**
	 * Returns the parameters of a simulation series
	 * 
	 * @param seriesId
	 * @return
	 */
	public SimulationSeriesParameters getSimulationParameters(long seriesId) {

		EntityManager em = getEntityManager();
		TypedQuery<SimulationSeriesParameters> query = em.createQuery("SELECT p FROM Parameters AS p WHERE s.series_seriesId =:id",
				SimulationSeriesParameters.class);
		query.setParameter("id", seriesId);
		SimulationSeriesParameters parameters = query.getSingleResult();
		return parameters;
	}
	
	
//////////////////////// Simulation Series Groups //////////////////////////
	
	/**
	 * Persists a SimulationSeriesGroup
	 * 
	 * @param group
	 * @return
	 */
	public synchronized long store(SimulationSeriesGroup group, long userId) {
		
		group.setUserId(userId);
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		em.persist(group);
		em.flush();
		em.getTransaction().commit();

		long id = group.getId();
		em.close();

		return id;
	}	

	/**
	 * Returns a persisted SimulationSeriesGroup
	 * 
	 * @param id SimulationSeriesGroup Id
	 * @return 
	 */
	public SimulationSeriesGroup getSimulationSeriesGroup(long id) {
		
		SimulationSeriesGroup simulation;
		EntityManager em = getEntityManager();
		try {
		em.getTransaction().begin();
		simulation = em.find(SimulationSeriesGroup.class, id);
		em.getTransaction().commit();
		} catch (RuntimeException e) {
			em.getTransaction().rollback();
			throw e;
		}
		return simulation;
	}
	
	/**
	 * Removes a SimulatioSeriesGroup from the database
	 * 
	 * @param simulation SimulationSeriesGroup
	 */
	public void deleteGroup(long id) {

		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		SimulationSeriesGroup simulation = em.getReference(SimulationSeriesGroup.class, id);
		em.remove(simulation);
		em.getTransaction().commit();
		em.close();
	}
	
	/**
	 * Removes a SimulatioSeriesGroup from the database
	 * 
	 * @param simulation SimulationSeriesGroup
	 */
	public synchronized void delete(SimulationSeriesGroup simulation) {

		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		simulation = em.getReference(SimulationSeriesGroup.class, simulation.getId());
		em.remove(simulation);
		em.getTransaction().commit();
		em.close();
	}
	
	
	/**
	 * @return all SimulationSeriesGroups of a specific user
	 */
	public List<SimulationSeriesGroup> getSimulationSeriesGroups(long userId) {
		
		EntityManager em = getEntityManager();
		TypedQuery<SimulationSeriesGroup> query = em.createQuery("SELECT s FROM SimulationSeriesGroup s WHERE s.userId = :id", SimulationSeriesGroup.class);
		query.setParameter("id", userId);
		List<SimulationSeriesGroup> list = query.getResultList();
		return list;
	}

	/**
	 * Return a list of SimulationSeriesGroups.
	 * 
	 * @param userId Id of owner
	 * @param firstIndex 
	 * @param length Number of entries
	 * @return simulationSeriesGroups
	 */
	public List<SimulationSeriesGroup> getSimulationSeriesGroups(long userId, int firstIndex, int length) {
		
		EntityManager em = getEntityManager();
		TypedQuery<SimulationSeriesGroup> query = em.createQuery("SELECT s FROM SimulationSeriesGroup s WHERE s.userId = :id", SimulationSeriesGroup.class);
		query.setFirstResult(firstIndex);
		query.setMaxResults(length);
		query.setParameter("id", userId);
		List<SimulationSeriesGroup> list = query.getResultList();
		return list;
	}


	
}
