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
import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CoverId;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomGraphId;
import i5.las2peer.services.ocd.graphs.GraphCreationLog;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;

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

	/////////////////////////// GRAPHS ///////////////////////////

	/**
	 * Persists a CustomGraph
	 * 
	 * @param graph
	 *            CustomGraph
	 * @return persistence id of the stored graph
	 */
	public long storeGraph(CustomGraph graph) {

		EntityManager em = getEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			em.persist(graph);
			tx.commit();
		} catch (RuntimeException e) {
			e.printStackTrace();
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;
		}
		em.close();
		return graph.getId();
	}

	/**
	 * Returns a persisted CustomGraph
	 * 
	 * @param username
	 *            owner of the graph
	 * @param graphId
	 *            id of the graph
	 * @return the found CustomGraph instance or null if the CustomGraph does
	 *         not exist
	 * @throws Exception
	 */
	public CustomGraph getGraph(String username, long graphId) {

		CustomGraphId identity = new CustomGraphId(graphId, username);
		CustomGraph graph = null;
		EntityManager em = getEntityManager();
		EntityTransaction tx = em.getTransaction();

		try {
			tx.begin();
			graph = em.find(CustomGraph.class, identity);
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;
		}
		em.close();

		if (graph == null) {
			logger.log(Level.WARNING, "user: " + username + ", " + "Graph does not exist: graph id " + graphId);
		}
		return graph;
	}

	/**
	 * Deletes a CustomGraph from the database
	 * 
	 * @param username
	 *            owner of the graph
	 * @param graphId
	 *            id of the graph
	 * @param threadHandler
	 * @throws Exception
	 */
	public void deleteGraph(String username, long graphId, ThreadHandler threadHandler) {

		EntityManager em = getEntityManager();
		CustomGraphId id = new CustomGraphId(graphId, username);

		synchronized (threadHandler) {

			threadHandler.interruptBenchmark(id);

			List<Cover> coverList = getCovers(username, graphId);
			for (Cover cover : coverList) {
				try {
					deleteCover(username, cover, threadHandler);
				} catch (Exception e) {
					throw e;
				}
			}

			EntityTransaction tx = em.getTransaction();
			try {
				tx = em.getTransaction();
				tx.begin();
				em.remove(em.getReference(CustomGraph.class, id));
				tx.commit();
			} catch (RuntimeException e) {
				if (tx != null && tx.isActive()) {
					tx.rollback();
				}
				throw e;
			}
		}

	}

	/**
	 * Return all graphs of a user
	 * 
	 * @param username
	 *            graphs owner
	 * @return graph list
	 * @throws AgentNotKnownException
	 */
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

	/**
	 * Return a list of specific graphs of a user
	 * 
	 * @param username
	 * @param firstIndex
	 *            id of the first graph
	 * @param length
	 *            number of graphs
	 * @param executionStatusIds
	 * @return
	 */
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

	/////////////////////////// COVERS ///////////////////////////

	/**
	 * Get a stored community-cover of a graph by its index
	 * 
	 * @param coverId
	 *            id of the cover
	 * @param graphId
	 *            id of the graph
	 * @return the found Cover instance or null if the Cover does not exist
	 * @throws Exception
	 */
	public Cover getCover(String username, long graphId, long coverId) {

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
		}
		return cover;
	}

	/**
	 * Deletes a persisted cover from the database
	 * 
	 * Checks whether cover is being calculated by a ground truth benchmark and
	 * if so deletes the graph instead.
	 * 
	 * @param username
	 *            owner of the cover
	 * @param coverId
	 *            id of the cover
	 * @param graphId
	 *            id of the graph
	 * @param threadHandler
	 * @throws IllegalArgumentException
	 *             cover does not exists
	 */
	public void deleteCover(String username, long graphId, long coverId, ThreadHandler threadHandler) {

		Cover cover = getCover(username, graphId, coverId);
		if (cover == null)
			throw new IllegalArgumentException("Cover not found");

		if (cover.getCreationMethod().getType().correspondsGroundTruthBenchmark()
				&& cover.getCreationMethod().getStatus() != ExecutionStatus.COMPLETED) {

			this.deleteGraph(username, graphId, threadHandler);
		}

		this.deleteCover(username, cover, threadHandler);
	}

	/**
	 * Deletes a persisted cover from the database
	 * 
	 * @param username
	 *            owner of the cover
	 * @param coverId
	 *            id of the cover
	 * @param graphId
	 *            id of the graph
	 * @param threadHandler
	 */
	public void deleteCover(String username, Cover cover, ThreadHandler threadHandler) {

		synchronized (threadHandler) {

			threadHandler.interruptAll(cover);
			EntityManager em = getEntityManager();
			EntityTransaction tx = em.getTransaction();

			CoverId id = new CoverId(cover.getId(), new CustomGraphId(cover.getGraph().getId(), username));
			try {
				tx.begin();
				em.remove(em.getReference(Cover.class, id));
				tx.commit();
			} catch (RuntimeException e) {
				if (tx != null && tx.isActive()) {
					tx.rollback();
				}
				throw e;
			}
			em.close();
		}
	}

	/**
	 * Returns all Covers corresponding to a CustomGraph
	 * 
	 * @param username
	 *            owner of the graph
	 * @param graphId
	 *            id of the graph
	 * @return cover list
	 */
	public List<Cover> getCovers(String username, long graphId) {

		EntityManager em = getEntityManager();
		String queryStr = "SELECT c from Cover c" + " JOIN c." + Cover.GRAPH_FIELD_NAME + " g" + " WHERE g."
				+ CustomGraph.USER_NAME_FIELD_NAME + " = :username" + " AND g." + CustomGraph.ID_FIELD_NAME + " = "
				+ graphId;
		TypedQuery<Cover> query = em.createQuery(queryStr, Cover.class);
		query.setParameter("username", username);

		return query.getResultList();
	}

	/**
	 * @param username
	 * @param graphId
	 * @param executionStatusIds
	 * @param metricExecutionStatusIds
	 * @param firstIndex
	 * @param length
	 * @param includeMeta
	 * @return
	 */
	public List<Cover> getCovers(String username, long graphId, List<Integer> executionStatusIds,
			List<Integer> metricExecutionStatusIds, int firstIndex, int length, boolean includeMeta) {

		EntityManager em = getEntityManager();

		String queryStr = "SELECT c from Cover c" + " JOIN c." + Cover.GRAPH_FIELD_NAME + " g" + " JOIN c."
				+ Cover.CREATION_METHOD_FIELD_NAME + " a";
		if (metricExecutionStatusIds != null && metricExecutionStatusIds.size() > 0) {
			queryStr += " JOIN c." + Cover.METRICS_FIELD_NAME + " m";
		}
		queryStr += " WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username" + " AND a."
				+ CoverCreationLog.STATUS_ID_FIELD_NAME + " IN :execStatusIds";
		if (metricExecutionStatusIds != null && metricExecutionStatusIds.size() > 0) {
			queryStr += " AND m." + OcdMetricLog.STATUS_ID_FIELD_NAME + " IN :metricExecStatusIds";
		}
		if (graphId >= 0) {
			queryStr += " AND g." + CustomGraph.ID_FIELD_NAME + " = " + graphId;
		}
		queryStr += " GROUP BY c";

		TypedQuery<Cover> query = em.createQuery(queryStr, Cover.class);
		query.setFirstResult(firstIndex);
		query.setMaxResults(length);
		query.setParameter("username", username);
		query.setParameter("execStatusIds", executionStatusIds);

		if (metricExecutionStatusIds != null && metricExecutionStatusIds.size() > 0) {
			query.setParameter("metricExecStatusIds", metricExecutionStatusIds);
		}
		List<Cover> queryResults = query.getResultList();
		em.close();
		return queryResults;
	}

	//////////////////////// METRICS ////////////////////////

}
