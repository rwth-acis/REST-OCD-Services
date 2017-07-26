package i5.las2peer.services.ocd.utils;

import java.util.List;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.p2p.AgentNotKnownException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverId;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomGraphId;
import i5.las2peer.services.ocd.graphs.GraphCreationLog;

/**
 * Manages the access on persisted data for the Service Class.
 * 
 */
public class EntityHandler {

	/**
	 * l2p logger
	 */
	private final static L2pLogger logger = L2pLogger.getInstance(EntityHandler.class.getName());

	/**
	 * Default name of the persistence unit used for the creation of entity
	 * managers.
	 */
	private static final String defaultPersistenceUnitName = "ocd";

	/**
	 * The factory used for the creation of entity managers.
	 */
	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory(defaultPersistenceUnitName);

	/**
	 * Sets the persistence unit for entity managers produced by any handler.
	 * 
	 * @param persistenceUnitName
	 *            The name of the persistence unit.
	 */
	public static void setPersistenceUnit(String persistenceUnitName) {
		emf = Persistence.createEntityManagerFactory(persistenceUnitName);
	}
	
	/**
	 * Creates a new instance. Also initiates the database connection.
	 */
	public EntityHandler() {
		EntityManager em = emf.createEntityManager();
		em.close();
	}
	
	/**
	 * Creates a new entity manager.
	 * 
	 * @return The entity manager.
	 */
	public EntityManager getEntityManager() {
		return emf.createEntityManager();
	}	

	///////////////// GRAPH /////////////////

	/**
	 * Get a stored graph by its index
	 * 
	 * @param graphId
	 * @return CustomGraph
	 * @throws Exception
	 */
	public CustomGraph getGraph(String username, long graphId) throws Exception {

		EntityManager em = getEntityManager();
		CustomGraphId id = new CustomGraphId(graphId, username);
		EntityTransaction tx = em.getTransaction();
		CustomGraph graph = null;

		try {
			tx.begin();
			graph = em.find(CustomGraph.class, id);
			if (graph == null) {
				logger.log(Level.WARNING, "user: " + username + ", " + "Graph does not exist: graph id " + graphId);
				throw new Exception();
			}
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;
		}
		em.close();
		return graph;
	}

	public List<CustomGraph> getGraphs(String username) throws AgentNotKnownException {

		List<CustomGraph> queryResults;
		EntityManager em = getEntityManager();
		String queryStr = "SELECT g FROM CustomGraph g WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username";
		TypedQuery<CustomGraph> query = em.createQuery(queryStr, CustomGraph.class);
		query.setParameter("username", username);
		queryResults = query.getResultList();
		em.close();

		return queryResults;
	}

	public List<CustomGraph> getGraphs(String username, int firstIndex, int length, List<Integer> executionStatusIds) {

		List<CustomGraph> queryResults;
		EntityManager em = getEntityManager();
		String queryStr = "SELECT g FROM CustomGraph g" + " JOIN g." + CustomGraph.CREATION_METHOD_FIELD_NAME + " b"
				+ " WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username" + " AND b."
				+ GraphCreationLog.STATUS_ID_FIELD_NAME + " IN :execStatusIds";
		TypedQuery<CustomGraph> query = em.createQuery(queryStr, CustomGraph.class);
		query.setFirstResult(firstIndex);
		query.setMaxResults(length);
		query.setParameter("username", username);
		query.setParameter("execStatusIds", executionStatusIds);
		queryResults = query.getResultList();
		em.close();

		return queryResults;

	}

	///////////////// Cover /////////////////

	/**
	 * Get a stored community-cover of a graph by its index
	 * 
	 * @param coverId
	 * @param graphId
	 * @return Cover
	 * @throws Exception
	 */
	public Cover getCover(String username, long coverId, long graphId) throws Exception {

		EntityManager em = getEntityManager();
		CustomGraphId gId = new CustomGraphId(graphId, username);
		CoverId cId = new CoverId(coverId, gId);
		EntityTransaction tx = em.getTransaction();
		Cover cover;

		try {
			tx.begin();
			cover = em.find(Cover.class, cId);
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;
		}
		if (cover == null) {
			logger.log(Level.WARNING,
					"user: " + username + ", " + "Cover does not exist: cover id " + coverId + ", graph id " + graphId);
			throw new Exception();
		}
		return cover;
	}
}
