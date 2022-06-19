package i5.las2peer.services.ocd.graphs;

import static org.junit.Assert.assertEquals;
import i5.las2peer.services.ocd.benchmarks.OcdBenchmarkException;
import i5.las2peer.services.ocd.benchmarks.GroundTruthBenchmark;
import i5.las2peer.services.ocd.benchmarks.OcdBenchmarkExecutor;
import i5.las2peer.services.ocd.benchmarks.OcdBenchmarkFactory;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricType;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

public class CoverPersistenceTest {

	private static final String userName = "coverPersistenceUser";
	private static final String graphName = "coverPersistenceGraph";
	private static final String coverName = "coverPersistenceCover";
	private static final String invalidCoverName = "invalidCoverName";
	
	EntityManagerFactory emf = Persistence.createEntityManagerFactory("ocd");
	
	@Test
	public void testPersist() {
		EntityManager em = emf.createEntityManager();
		CustomGraph graph = new CustomGraph();
		graph.setUserName(userName);
		graph.setName(graphName);
		Node nodeA = graph.addNode("A");
		Node nodeB = graph.addNode("B");
		Node nodeC = graph.addNode("C");
		graph.setNodeName(nodeA, "A");
		graph.setNodeName(nodeB, "B");
		graph.setNodeName(nodeC, "C");
		Edge edgeAB = graph.addEdge(UUID.randomUUID().toString(), nodeA, nodeB);
		graph.setEdgeWeight(edgeAB, 5);
		Edge edgeBC = graph.addEdge(UUID.randomUUID().toString(), nodeB, nodeC);
		graph.setEdgeWeight(edgeBC, 2.5);
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			em.persist(graph);
			tx.commit();
		} catch( RuntimeException ex ) {
			if( tx != null && tx.isActive() ) tx.rollback();
			throw ex;
		}
		em.close();
		CustomGraphId gId = new CustomGraphId(graph.getPersistenceId(), userName);
		em = emf.createEntityManager();
		Matrix memberships = new CCSMatrix(3, 2);
		memberships.set(0, 0, 1);
		memberships.set(1, 0, 0.5);
		memberships.set(1, 1, 0.5);
		memberships.set(2, 1, 1);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("param1", "val1");
		params.put("param2", "val2");
		CoverCreationLog algo = new CoverCreationLog(CoverCreationType.UNDEFINED, params, new HashSet<GraphType>());
		Cover cover = new Cover(graph, memberships);
		cover.setCreationMethod(algo);
		cover.setName(coverName);
		cover.setCommunityColor(1, Color.BLUE);
		cover.setCommunityName(1, "Community1");
		OcdMetricLog metric = new OcdMetricLog(OcdMetricType.EXECUTION_TIME, 3.55, params, cover);
		cover.addMetric(metric);
		tx = em.getTransaction();
		try {
			tx.begin();
			em.persist(cover);
			tx.commit();
		} catch( RuntimeException ex ) {
			if( tx != null && tx.isActive() ) tx.rollback();
			throw ex;
		}
		em.close();
		CoverId cId = new CoverId(cover.getId(), gId);
		em = emf.createEntityManager();
		TypedQuery<Cover> query = em.createQuery("Select c from Cover c where c.name = :name", Cover.class);
		query.setParameter("name", coverName);
		List<Cover> queryResults = query.getResultList();
		em.close();
		//assertEquals(1, queryResults.size());
		Cover persistedCover = queryResults.get(0);
		System.out.println("Name: " + persistedCover.getName());
		System.out.println("Community Count: " + persistedCover.communityCount());
		System.out.println("Algo: " + persistedCover.getCreationMethod().getType().toString());
		System.out.println("Metrics Count: " + persistedCover.getMetrics().size());
		for(int i=0; i<cover.communityCount(); i++) {
			System.out.println("Com: " + i);
			System.out.println("Name cov: " + cover.getCommunityName(i));
			System.out.println("Name covP: " + persistedCover.getCommunityName(i));
			System.out.println("Color cov: " + cover.getCommunityColor(i));
			System.out.println("Color covP: " + persistedCover.getCommunityColor(i));
		}
		assertEquals(coverName, persistedCover.getName());
		assertEquals(graphName, persistedCover.getGraph().getName());
		assertEquals(cover.communityCount(), persistedCover.communityCount());
		for(int i=0; i<cover.communityCount(); i++) {
			assertEquals(cover.getCommunityColor(i), persistedCover.getCommunityColor(i));
			assertEquals(cover.getCommunityName(i), persistedCover.getCommunityName(i));
			assertEquals(cover.getCommunitySize(i), persistedCover.getCommunitySize(i));
		}
		assertEquals(cover.getCreationMethod().getType(), persistedCover.getCreationMethod().getType());
		assertEquals(cover.getMetrics().size(), persistedCover.getMetrics().size());
		for(int i=0; i<cover.getMetrics().size(); i++) {
			assertEquals(cover.getMetrics().get(i).getType(), persistedCover.getMetrics().get(i).getType());
			assertEquals(cover.getMetrics().get(i).getValue(), persistedCover.getMetrics().get(i).getValue(), 0);
		}
		em = emf.createEntityManager();
		query = em.createQuery("Select c from Cover c where c.name = :name", Cover.class);
		query.setParameter("name", invalidCoverName);
		queryResults = query.getResultList();
		em.close();
		assertEquals(0, queryResults.size());
		em=emf.createEntityManager();
		tx = em.getTransaction();
		try {
			tx.begin();
			persistedCover = em.find(Cover.class, cId);
			em.remove(persistedCover);
			graph = em.find(CustomGraph.class, gId);
			em.remove(graph);
			tx.commit();
		} catch( RuntimeException ex ) {
			if( tx != null && tx.isActive() ) tx.rollback();
			throw ex;
		}
		em.close();
	}
	
	@Test
	public void testMergeCover() throws OcdBenchmarkException, InstantiationException, IllegalAccessException, InterruptedException {
		String graphNameStr = "newman01graph";
		String coverNameStr = "newman01cover";
		GraphCreationType benchmarkType = GraphCreationType.NEWMAN;
		Map<String, String> parameters = new HashMap<String, String>();
		CustomGraph graph = new CustomGraph();
		OcdBenchmarkFactory benchmarkFactory = new OcdBenchmarkFactory();
		GroundTruthBenchmark benchmark = (GroundTruthBenchmark)benchmarkFactory.getInstance(benchmarkType, parameters);
		GraphCreationLog log = new GraphCreationLog(benchmarkType, parameters);
    	graph.setCreationMethod(log);
    	graph.setName(graphNameStr);
    	Cover cover = new Cover(graph, new CCSMatrix());
    	cover.setName(coverNameStr);
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			em.persist(graph);
			em.persist(cover);
			tx.commit();
		} catch( RuntimeException ex ) {
			if( tx != null && tx.isActive() ) tx.rollback();
			throw ex;
		}
		em.close();
		System.out.println("CID: " + cover.getId());
		System.out.println("GID: " + graph.getPersistenceId());
		OcdBenchmarkExecutor executor = new OcdBenchmarkExecutor();
		Cover calculatedCover = executor.calculateGroundTruthBenchmark(benchmark);
		CustomGraph calculatedGraph = calculatedCover.getGraph();
		CustomGraphId gId = new CustomGraphId(graph.getPersistenceId(), graph.getUserName());
		CoverId cId = new CoverId(cover.getId(), gId);
	
		em = emf.createEntityManager();
		tx = em.getTransaction();
		try {
			tx.begin();
			System.out.println("PrePersistG");
			Cover pCover = em.find(Cover.class, cId);
			pCover.getGraph().setStructureFrom(calculatedGraph);
			tx.commit();
		} catch( RuntimeException ex ) {
			if( tx != null && tx.isActive() ) tx.rollback();
			throw ex;
		}
		em.close();
		em = emf.createEntityManager();
		tx = em.getTransaction();
		try {
			tx.begin();
			System.out.println("PrePersistC");
			Cover pCover = em.find(Cover.class, cId);
			pCover.setMemberships(calculatedCover.getMemberships());
			tx.commit();
		} catch( RuntimeException ex ) {
			if( tx != null && tx.isActive() ) tx.rollback();
			throw ex;
		}
		em.close();
		
		em = emf.createEntityManager();
		tx = em.getTransaction();
		Cover coverRead;
		try {
			tx.begin();
			coverRead = em.find(Cover.class, cId);
			tx.commit();
		} catch( RuntimeException ex ) {
			if( tx != null && tx.isActive() ) tx.rollback();
			throw ex;
		}
		em.close();
		
		assertEquals(4, coverRead.communityCount());
		System.out.println("RPCID: " + coverRead.getId());
		System.out.println("RPGID: " + coverRead.getGraph().getPersistenceId());
		System.out.println("Nodes: " + coverRead.getGraph().getNodeCount());
		System.out.println(coverRead);
		
	}
	
}
