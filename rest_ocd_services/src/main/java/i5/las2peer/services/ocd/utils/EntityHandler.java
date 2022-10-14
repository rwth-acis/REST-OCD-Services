package i5.las2peer.services.ocd.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.persistence.*;

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.p2p.AgentNotRegisteredException;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.data.CentralityMapId;
import i5.las2peer.services.ocd.graphs.*;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.internal.inject.Custom;

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
		return graph.getPersistenceId();
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
	 * Finds the graph in the databank and updates its nodes and edges
	 *  
	 * @param newGraph Graph with the new edges and nodes
	 * @param graphId	Current graph ID
	 * @param username	Username
	 */
	public void updateGraph(CustomGraph newGraph, long graphId, String username){
		EntityManager em = getEntityManager();
		EntityTransaction tx = em.getTransaction();
		CustomGraphId identity = new CustomGraphId(graphId, username);
		CustomGraph graph;
		try{
			tx.begin();
			graph = em.find(CustomGraph.class, identity);
			graph.setStructureFrom(newGraph);

			tx.commit();
		} catch (RuntimeException e) {
			if(tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;
		}
		em.close();
	}

	/**
	 * Deletes a CustomGraph from the database
	 * 
	 * @param username
	 *            owner of the graph
	 * @param graphId
	 *            id of the graph
	 * @param threadHandler
	 * 			  the threadhandler
	 * @throws Exception if cover deletion failed
	 */
	public void deleteGraph(String username, long graphId, ThreadHandler threadHandler) throws Exception {

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
			
			List<CentralityMap> centralityMapList = getCentralityMaps(username, graphId);
			for (CentralityMap map : centralityMapList) {
				try {
					deleteCentralityMap(username, map, threadHandler);
				} catch (Exception e) {
					throw e;
				}
			}
			
			EntityTransaction tx = em.getTransaction();
			try {
				tx = em.getTransaction();
				tx.begin();
				
				CustomGraph graph = em.getReference(CustomGraph.class, id);
				
				if(graph.getPath() != "" && graph.getPath() != null) { // Delete index folder if graph is content graph
				    File file = new File(graph.getPath());
				    FileUtils.deleteDirectory(file);
				}
				
				em.remove(graph);
				tx.commit();
			} catch (RuntimeException e) {
				if (tx != null && tx.isActive()) {
					tx.rollback();
				}
				throw e;
			} catch (IOException e) {
				if (tx != null && tx.isActive()) {
					tx.rollback();
				}
				
				throw new RuntimeException("Could not delete folder of content graph");
			}
		}

	}

	/**
	 * Return all graphs of a user
	 * 
	 * @param username
	 *            graphs owner
	 * @return graph list
	 * @throws AgentNotRegisteredException if the agent was not registered
	 */
	public List<CustomGraph> getGraphs(String username) throws AgentNotRegisteredException {

		List<CustomGraph> queryResults;
		EntityManager em = getEntityManager();
		String queryStr = "SELECT g FROM " + CustomGraph.class.getName() + " g WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username";
		TypedQuery<CustomGraph> query = em.createQuery(queryStr, CustomGraph.class);
		query.setParameter("username", username);
		queryResults = query.getResultList();
		em.close();

		return queryResults;
	}


	/**
	 * Return specified graphs' meta information of a user using an efficient approach. This approach only
	 * loads necessary metadata about graphs. E.g. no information about nodes/edges is loaded
	 *
	 * @param username
	 * 			  the users username
	 * @param firstIndex
	 *            id of the first graph
	 * @param length
	 *            number of graphs
	 * @param executionStatusIds
	 * 			  the execution status ids of the graphs
	 * @return the list of graphs
	 */
	public ArrayList<CustomGraphMeta> getGraphMetaDataEfficiently(String username, int firstIndex, int length, List<Integer> executionStatusIds) {

		EntityManager em = getEntityManager();

		// Query graph info
		String queryStrGraphInfo = "SELECT"
				+ " g." + CustomGraph.ID_FIELD_NAME
				+ ", g." + CustomGraph.USER_NAME_FIELD_NAME
				+ ", g." + CustomGraph.NAME_FIELD_NAME
				+ ", g." + CustomGraph.NODE_COUNT_FIELD_NAME
				+ ", g." + CustomGraph.EDGE_COUNT_FIELD_NAME
				+ ", g." + CustomGraph.CREATION_METHOD_FIELD_NAME
				+ " FROM " + CustomGraph.class.getName() + " g" + " JOIN g." + CustomGraph.CREATION_METHOD_FIELD_NAME + " b"
				+ " WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username" + " AND b."
				+ GraphCreationLog.STATUS_ID_FIELD_NAME + " IN :execStatusIds";
		Query queryGraphInfo = em.createQuery(queryStrGraphInfo);
		queryGraphInfo.setFirstResult(firstIndex);
		queryGraphInfo.setMaxResults(length);
		queryGraphInfo.setParameter("username", username);
		queryGraphInfo.setParameter("execStatusIds", executionStatusIds);


		List graphInfoList = queryGraphInfo.getResultList();

		// Create CustomGraphMeta that holds metadata for each of the graphs.
		ArrayList<CustomGraphMeta> graphMetadatas = new ArrayList<>();

		// Create CustomGraphMeta instance for each graph queried above
		for(int i = 0; i < graphInfoList.size(); i++){

			Object[] graph_data = (Object[]) graphInfoList.get(i);
			// create a query for collection of graph types belonging to the currently observed graph
			String queryStrGraphTypes = "SELECT g." + CustomGraph.TYPES_FIELD_NAME + " FROM " + CustomGraph.class.getName() + " g" + " JOIN g." + CustomGraph.CREATION_METHOD_FIELD_NAME + " b"
					+ " WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username" + " AND g."
					+ CustomGraph.ID_FIELD_NAME + " = :graphId" + " AND b."
					+ GraphCreationLog.STATUS_ID_FIELD_NAME + " IN :execStatusIds";
			Query queryGraphTypes = em.createQuery(queryStrGraphTypes);
			queryGraphTypes.setFirstResult(firstIndex);
			queryGraphTypes.setMaxResults(length);
			queryGraphTypes.setParameter("username", username);
			queryGraphTypes.setParameter("execStatusIds", executionStatusIds);
			queryGraphTypes.setParameter("graphId", ((Long) graph_data[0]));

			List graphTypeSetList = queryGraphTypes.getResultList();

			ArrayList<Integer> graphTypeList = new ArrayList<Integer>();
			for(int j = 0; j < graphTypeSetList.size(); j++){
				graphTypeList.add((Integer) graphTypeSetList.get(j));
			}

			CustomGraphMeta graphMeta = new CustomGraphMeta(
					(Long) graph_data[0], //id
					(String) graph_data[1], // username
					(String) graph_data[2], // name
					(Long) graph_data[3], // nodeCount
					(Long) graph_data[4], // edgeCount
					graphTypeList,
					(GraphCreationLog) graph_data[5] // graph creation log
			);
			graphMetadatas.add(graphMeta);
		}
		em.close();


		return graphMetadatas;
	}

	/**
	 * Return a list of specific graphs of a user
	 * 
	 * @param username
	 * 			  the users username
	 * @param firstIndex
	 *            id of the first graph
	 * @param length
	 *            number of graphs
	 * @param executionStatusIds
	 * 			  the execution status ids of the graphs
	 * @return the list of graphs
	 */
	public List<CustomGraph> getGraphs(String username, int firstIndex, int length, List<Integer> executionStatusIds) {

		List<CustomGraph> queryResults;
		EntityManager em = getEntityManager();
		String queryStr = "SELECT g FROM " + CustomGraph.class.getName() + " g" + " JOIN g." + CustomGraph.CREATION_METHOD_FIELD_NAME + " b"
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
	 * @param username
	 * 			  the name of the user
	 * @param coverId
	 *            id of the cover
	 * @param graphId
	 *            id of the graph
	 * @return the found Cover instance or null if the Cover does not exist
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
	 * 			  the thread handler
	 * @throws IllegalArgumentException
	 *             cover does not exist
	 * @throws Exception
	 * 			   if cover deletion failed
	 */
	public void deleteCover(String username, long graphId, long coverId, ThreadHandler threadHandler) throws Exception {

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
	 * @param cover
	 *            the cover
	 * @param threadHandler
	 * 			  the threadhandler
	 */
	public void deleteCover(String username, Cover cover, ThreadHandler threadHandler) {

		synchronized (threadHandler) {

			threadHandler.interruptAll(cover);
			EntityManager em = getEntityManager();
			EntityTransaction tx = em.getTransaction();

			CoverId id = new CoverId(cover.getId(), new CustomGraphId(cover.getGraph().getPersistenceId(), username));
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
	 * Removes username from the InactivityData table from the database. This is to ensure that for users whose content
	 * has been deleted and who are inactive, no processing power is wasted for checking their data.
	 *
	 * @param username      Username to remove from the table.
	 * @param threadHandler the ThreadHandler.
	 */
	public void deleteUserInactivityData(String username, ThreadHandler threadHandler) {

		synchronized (threadHandler) {
			EntityManager em = getEntityManager();
			EntityTransaction tx = em.getTransaction();

			try {
				tx.begin();
				;

				// find user to delete
				String queryStr = "DELETE FROM " + InactivityData.class.getName() + " d WHERE d." + InactivityData.USER_NAME_FIELD_NAME + " = :username";
				TypedQuery<InactivityData> query = em.createQuery(queryStr, InactivityData.class);
				query.setParameter("username", username);
				query.executeUpdate();

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
	 * 		  the name of the user
	 * @param graphId
	 * 		  the id of the graph
	 * @param executionStatusIds
	 * 		  the ids of the execution statuses
	 * @param metricExecutionStatusIds
	 * 		  the ids of the metric execution statuses
	 * @param firstIndex
	 * 		  the first index
	 * @param length
	 * 		  the length of the result set
	 * @param includeMeta
	 * 		  boolean whether to include meta info or not
	 * @return a cover list
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
		queryStr += " AND g." + CustomGraph.ID_FIELD_NAME + " = " + graphId;
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

	//////////////////////// CENTRALITY ////////////////////////
	
	/**
	 * Get a stored centrality map.
	 * 
	 * @param username
	 *            Owner of the CentralityMap
	 * @param graphId
	 *            Id of the graph the CentralityMap is based on
	 * @param mapId
	 *            Id the of CentralityMap
	 * @return The found CentralityMap instance or null if the CentralityMap does not exist
	 */
	public CentralityMap getCentralityMap(String username, long graphId, long mapId) {
		
		EntityManager em = getEntityManager();
    	CustomGraphId gId = new CustomGraphId(graphId, username);
    	CentralityMapId cId = new CentralityMapId(mapId, gId);
		/*
		 * Finds CentralityMap
		 */
		EntityTransaction tx = em.getTransaction();
    	CentralityMap map;
    	try {
			tx.begin();
			map = em.find(CentralityMap.class, cId);
			tx.commit();
		}
    	catch( RuntimeException e ) {
			if( tx != null && tx.isActive() ) {
				tx.rollback();
			}
			throw e;
		}
		if (map == null) {
			logger.log(Level.WARNING,
					"user: " + username + ", " + "Centrality map does not exist: centrality map id " + mapId + ", graph id " + graphId);
		}
		return map;
	}
	
	/**
	 * Deletes a persisted CentralityMap from the database
	 * 
	 * @param username
	 *            Owner of the CentralityMap
	 * @param graphId
	 *            Id of the graph
	 * @param mapId
	 *            Id of the CentralityMap
	 * @param threadHandler
	 *            The ThreadHandler for algorithm execution
	 * @throws IllegalArgumentException if the centrality map was not found
	 */
	public void deleteCentralityMap(String username, long graphId, long mapId, ThreadHandler threadHandler) {
		CentralityMap map = getCentralityMap(username, graphId, mapId);		
		if (map == null)
			throw new IllegalArgumentException("Centrality map not found");
		
		deleteCentralityMap(username, map, threadHandler);
	}
	
	/**
	 * Deletes a persisted CentralityMap from the database
	 * 
	 * @param username
	 *            Owner of the CentralityMap
	 * @param map
	 *            The CentralityMap
	 * @param threadHandler
	 *            The ThreadHandler for algorithm execution
	 */
	public void deleteCentralityMap(String username, CentralityMap map, ThreadHandler threadHandler) {
		synchronized (threadHandler) {
			threadHandler.interruptAll(map);
			EntityManager em = getEntityManager();
			EntityTransaction tx = em.getTransaction();

			CentralityMapId id = new CentralityMapId(map.getId(), new CustomGraphId(map.getGraph().getPersistenceId(), username));
			try {
				tx.begin();
				em.remove(em.getReference(CentralityMap.class, id));
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
	 * Returns all centrality maps corresponding to a CustomGraph
	 * 
	 * @param username
	 *            Owner of the graph
	 * @param graphId
	 *            Id of the graph
	 * @return A list of the corresponding centrality maps
	 */
	public List<CentralityMap> getCentralityMaps(String username, long graphId) {

		EntityManager em = getEntityManager();
		String queryStr = "SELECT c from CentralityMap c" + " JOIN c." + CentralityMap.GRAPH_FIELD_NAME + " g" + " WHERE g."
				+ CustomGraph.USER_NAME_FIELD_NAME + " = :username" + " AND g." + CustomGraph.ID_FIELD_NAME + " = "
				+ graphId;
		TypedQuery<CentralityMap> query = em.createQuery(queryStr, CentralityMap.class);
		query.setParameter("username", username);

		return query.getResultList();
	}
}
