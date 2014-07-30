package i5.las2peer.services.servicePackage.graph;

import static org.junit.Assert.*;
import i5.las2peer.services.servicePackage.algorithms.AlgorithmType;
import i5.las2peer.services.servicePackage.algorithms.AlgorithmLog;
import i5.las2peer.services.servicePackage.metrics.MetricType;
import i5.las2peer.services.servicePackage.metrics.MetricLog;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import y.base.Edge;
import y.base.Node;

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
		Node nodeA = graph.createNode();
		Node nodeB = graph.createNode();
		Node nodeC = graph.createNode();
		graph.setNodeName(nodeA, "A");
		graph.setNodeName(nodeB, "B");
		graph.setNodeName(nodeC, "C");
		Edge edgeAB = graph.createEdge(nodeA, nodeB);
		graph.setEdgeWeight(edgeAB, 5);
		Edge edgeBC = graph.createEdge(nodeB, nodeC);
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
		em = emf.createEntityManager();
		Matrix memberships = new CCSMatrix(3, 2);
		memberships.set(0, 0, 1);
		memberships.set(1, 0, 0.5);
		memberships.set(1, 1, 0.5);
		memberships.set(2, 1, 1);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("param1", "val1");
		params.put("param2", "val2");
		AlgorithmLog algo = new AlgorithmLog(AlgorithmType.UNDEFINED, params, new HashSet<GraphType>());
		Cover cover = new Cover(graph, memberships, algo);
		cover.setName(coverName);
		cover.setCommunityColor(1, Color.BLUE);
		cover.setCommunityName(1, "Community1");
		MetricLog metric = new MetricLog(MetricType.EXECUTION_TIME, 3.55, params);
		cover.setMetric(metric);
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
		em = emf.createEntityManager();
		TypedQuery<Cover> query = em.createQuery("Select c from Cover c where c.name = :name", Cover.class);
		query.setParameter("name", coverName);
		List<Cover> queryResults = query.getResultList();
		em.close();
		assertEquals(1, queryResults.size());
		Cover persistedCover = queryResults.get(0);
		System.out.println("Name: " + persistedCover.getName());
		System.out.println("Community Count: " + persistedCover.communityCount());
		System.out.println("Algo: " + persistedCover.getAlgorithm().getType().toString());
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
		assertEquals(cover.getAlgorithm().getType(), persistedCover.getAlgorithm().getType());
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
	}
}
