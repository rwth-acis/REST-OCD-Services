package i5.las2peer.services.ocd.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

/**
 * Handles Remote Method Invocation calls from other services in the las2peer
 * network
 * 
 */
public class InvocationHandler {

	//private EntityHandler entityHandler = new EntityHandler();

	//////////// GRAPH ////////////

	/**
	 * 
	 * Converts a Graph into an Adjacency list. The outer list has an entry for
	 * every node of the graph. The inner list contains the Indices of the
	 * connected nodes. *
	 * 
	 * @param graph the graph
	 * 
	 * @return Adjacency list
	 * 
	 */
	public List<List<Integer>> getAdjList(CustomGraph graph) {

		int size = graph.getNodeCount();
		List<List<Integer>> adjList = new ArrayList<>(size+1);
		adjList.add(0, new ArrayList<Integer>());
		
		for (int i = 1; i <= size; i++) {
			List<Integer> list = new ArrayList<>();
			adjList.add(i, list);
		}
		Iterator<Edge> ec = graph.edges().iterator();
		while(ec.hasNext()) {
			Edge edge = ec.next();
			Node source = edge.getSourceNode();
			Node target = edge.getTargetNode();

			int sourceId = Integer.parseInt(graph.getNodeName(source));
			int targetId = Integer.parseInt(graph.getNodeName(target));
			
			adjList.get(sourceId).add(targetId);
		}

		return adjList;
	}

	//////////// COVER ////////////

	/**
	 * 
	 * Converts a Cover into Community Members Lists. The outer list has an
	 * entry for every community of the cover. The inner list contains the
	 * Indices of the member nodes.
	 * 
	 * @param cover the cover
	 * 
	 * @return community member list
	 * 
	 */

	public List<List<Integer>> getCommunityMemberList(Cover cover) {

		int size = cover.communityCount();
		List<List<Integer>> communityMemberList = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			communityMemberList.add(cover.getCommunityMemberIndices(i));
		}

		return communityMemberList;
	}

//	public List<Integer> getCoverIdsByGraphId(long graphId, String username) {		//TODO is not used
//
//		List<Cover> queryResults;
//		EntityManager em = entityHandler.getEntityManager();
//
//		String queryStr = "SELECT c from Cover c" + " JOIN c." + Cover.GRAPH_FIELD_NAME + " g";
//		queryStr += " WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username";
//		queryStr += " AND g." + CustomGraph.ID_FIELD_NAME + " = " + graphId;
//
//		queryStr += " GROUP BY c";
//		TypedQuery<Cover> query = em.createQuery(queryStr, Cover.class);
//
//		query.setParameter("username", username);
//		queryResults = query.getResultList();
//		em.close();
//
//		int size = queryResults.size();
//		ArrayList<Integer> coverIds = new ArrayList<Integer>(size);
//		for (int i = 0; i < size; i++) {
//			coverIds.add((int) queryResults.get(i).getId());
//		}
//
//		return coverIds;
//
//	}

}
