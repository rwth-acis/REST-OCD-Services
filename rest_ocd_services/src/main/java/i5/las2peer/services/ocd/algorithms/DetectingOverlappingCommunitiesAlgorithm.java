package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

/**
 * This class is an implementation of the algorithm by Nam P. Nguyen, Thang N. Dinh, Dung T. Nguyen, My T. Thai:
 * Overlapping community structures and their detection on social networks
 * https://doi.org/10.1109/PASSAT/SocialCom.2011.16
 * and based on the C++ implementation Nam P. Nguyen
 */
public class DetectingOverlappingCommunitiesAlgorithm implements OcdAlgorithm {

	/**
	 * The overlapping threshold.
	 * The default value is 0.75. Must be higher than 0 and lower than 2.
	 */
	private double overlappingThreshold = 0.75;
	
	int communityCount = 0;
	ArrayList<Integer> nodeIntersections;			// interG Array containing the node indexes in the intersection of two NODES
	ArrayList<Integer> communityIntersections;		// interH Array containing the node indexes the intersection of two COMMUNITIES
	int nodeIntersectionCounter = 0;				// n_interG the counter of interG[]
	int communityIntersectionCounter = 0;			// n_interH the counter of interH[]
	int intersectionCounter = 0;					// n_countInt the counter of countInt[]
	
	int maxDegree = 0,					// other variables for reference purpose
		maxCommunityNodeSize = 0,			// maxn_ComID
		maxCommunityNumber = 0, 		// maxNumCid
		maxIntersectionSize = 0, 		// maxIntersection
		maxEdgeIntersectionSize = 0, 	// maxEdgeIntersection
		maxOutliers = 0; 				
	
	HashMap<Integer,ArrayList<Integer>> adjacencyList = null;		// adjList The graphs adjacency lists
	HashMap<Integer,HashMap<Integer, Integer>> communities = null;		// ComID Each array contains node indexes for the same community index
	HashMap<Integer,HashMap<Integer, Integer>> nodeCommunities = null;			// Cid HashMap containing the Community IDs for each node (only positive values)
	HashMap<Integer, Integer> nodeDegrees = null;		// degree The list containing the degree for each node (only positive values)
	HashMap<Integer, Integer> communityNodeSizes = null;		// n_ComID the number nodes in each community, and thus the number of Community ID(s) we can have for a given community
	ArrayList<Integer> communityNumbers = null;		// numCid The number of community indexes for each node
	HashMap<Integer, Integer> intersectionCounters = null;		// countInt Node counters for and intersection, etc (only positive values)
	HashMap<Integer, Integer> communityEdgeSizes = null;	//  numEdgeList number of edges in each community
	
	private int minNode = 3;
	private int realCommunityCount = 0;	// realNumCOM is the actual number of communities when some communities combine together.

	
	/*
	 * PARAMETER NAMES
	 */
	public static String OVERLAPPING_THRESHOLD_NAME = "overlappingThreshold";
	
	@Override
	public void setParameters(Map<String, String> parameters) {
		if(parameters.containsKey(OVERLAPPING_THRESHOLD_NAME)) {
			overlappingThreshold = Double.parseDouble(parameters.get(OVERLAPPING_THRESHOLD_NAME));
			if(overlappingThreshold <= 0 || overlappingThreshold >= 2) {
				throw new IllegalArgumentException();
			}
			parameters.remove(OVERLAPPING_THRESHOLD_NAME);
		}
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(OVERLAPPING_THRESHOLD_NAME, Double.toString(overlappingThreshold));
		return parameters;
	}
	
	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.DETECTING_OVERLAPPING_COMMUNITIES_ALGORITHM;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		return compatibilities;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph)
			throws OcdAlgorithmException, InterruptedException {
		try {
			findOverlappingCommunityStructures(graph);
			
			Matrix membershipMatrix = getMembershipMatrix(graph);
			
			Cover cover = new Cover(graph, membershipMatrix);
			return cover;
		}
		catch(InterruptedException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			throw new OcdAlgorithmException(e);
		}
	}
	
	public Matrix getMembershipMatrix(CustomGraph graph) {
//DEBUG		System.out.println(communities);
		Matrix membershipMatrix = new Basic2DMatrix(graph.getNodeCount(), communities.size());
		
		int j=0;
		for(Map.Entry<Integer, HashMap<Integer,Integer>> communityPair : communities.entrySet()) {
			for(int nodeIndex : communityPair.getValue().values()) {
				membershipMatrix.set(nodeIndex, j, 1);
			}
			j++;
		}
		
		for(int i=0; i<membershipMatrix.rows(); i++) {
			double rowSum = membershipMatrix.getRow(i).sum();
			if(rowSum != 0) {
				membershipMatrix.setRow(i, membershipMatrix.getRow(i).divide(rowSum)); // Set ratios
			}			 
		}
				
		return membershipMatrix;
	}
	
	/**
	 * Main procedure of the Algorithm, consists of variable initialization, finding the graphs dense communities(those over 3 nodes), merging similar ones,
	 * finding tiny communities and a final refinement
	 * @param graph The graph the algorithm is run on
	 * @throws OcdAlgorithmException if the execution failed
	 * @throws InterruptedException if the thread was interrupted
	 */
	public void findOverlappingCommunityStructures(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
		communityIntersections = new ArrayList<Integer>(); // initialize communityIntersections
		nodeDegrees = new HashMap<Integer, Integer>(graph.getNodeCount());
		adjacencyList = new  HashMap<Integer, ArrayList<Integer>>(graph.getNodeCount());
		for(Node node : graph.nodes().toArray(Node[]::new)) {// Initialize nodeDegree, maxDegree, AdjacencyList
			Set<Node> neighbours = graph.getNeighbours(node);
			nodeDegrees.put(node.getIndex(), neighbours.size());
			
			maxDegree = Math.max(maxDegree, nodeDegrees.get(node.getIndex()));
			
			adjacencyList.put(node.getIndex(), new ArrayList<Integer>(neighbours.size()));
			for(Node neighbour : neighbours) {
				adjacencyList.get(node.getIndex()).add(neighbour.getIndex());
			}
		}
		
		findDenseCommunities(graph);
		combineOverlappingCommunities(graph);
		findTinyCommunities(graph);
		visitUnAssignedVertices(graph);
	}
	
	/**
	 * Finds communities with over 3 members through analyzing node intersections
	 * @param graph the examined graph
	 * @throws OcdAlgorithmException if the execution failed
	 * @throws InterruptedException if the thread was interrupted
	 */
	public void findDenseCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
		// initialize nodeCommunities, communityNumbers, intersectionCounters
		communityNumbers = new ArrayList<Integer>(graph.getNodeCount());
		intersectionCounters = new HashMap<Integer,Integer>();
		nodeCommunities = new HashMap<Integer, HashMap<Integer, Integer>>(graph.getNodeCount());
		for(int i=0; i<=graph.getNodeCount(); i++) { // initially, each node has one community ID, TODO: i was 1 first, check if this breaks anything
			nodeCommunities.put(i, new HashMap<Integer, Integer>());
			communityNumbers.add(0);
		}
				
		// initialize communities, communityNodeSizes, communityEdgeSizes
		communities = new HashMap<Integer, HashMap<Integer, Integer>>();
		communityNodeSizes = new HashMap<Integer, Integer>();
		communityEdgeSizes = new HashMap<Integer, Integer>();
		
		int numEdge=0;
		for(Edge edge : graph.edges().toArray(Edge[]::new)) { // reading from the beginning of file
			if (!sameCommunity(edge.getSourceNode().getIndex(), edge.getTargetNode().getIndex())) 
			{ // if a and b are not in a community together
				tryFormingNewCommunity(edge.getSourceNode().getIndex(), edge.getTargetNode().getIndex(), graph); // try to form a dense local community from (a,b)
			}
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
		}
		for(int i=0; i<communityCount; i++) {
			if(communityNodeSizes.get(i) == null) {
				communityNodeSizes.put(i,0);
			}
		}
		//resizeComID_n_ComID(); // resize communities[][] and n_communities[] arrays
	}
	
	/**
	 * Checks whether two nodes(represented by their indices) belong to a same community:
	 * Thus, if there exists an (active) community that is in both of their nodeCommunities
	 * @param nodeA The first node
	 * @param nodeB The second node
	 * @return true if yes, false if no
	 * @throws OcdAlgorithmException if the execution failed
	 */
	public boolean sameCommunity(int nodeA, int nodeB) throws OcdAlgorithmException {
		updateCounter();
		if (communityNumbers.get(nodeA)<=0 || communityNumbers.get(nodeB)<=0) { // if one of them is fresh
			return false; // then they will not be
		}
		for(int i=0; i<communityNumbers.get(nodeA); i++) { // otherwise, nodeCommunities of node A
			if (nodeCommunities.get(nodeA).get(i) > communityCount || nodeCommunities.get(nodeA).get(i) <=0) { // safety check
				throw new OcdAlgorithmException("Error: nodeCommunities.get(nodeA).get(i) > communityCount || nodeCommunities.get(nodeA).get(i) <=0 in isInSameCS()");
			}
			intersectionCounters.put(nodeCommunities.get(nodeA).get(i), intersectionCounter);
		}
		for(int i=0; i<communityNumbers.get(nodeB); i++) { // and then, mark nodeCommunities of node B
			if (nodeCommunities.get(nodeB).get(i) > communityCount || nodeCommunities.get(nodeB).get(i) <=0) {
				throw new OcdAlgorithmException("Error: nodeCommunities.get(nodeB).get(i) > communityCount || nodeCommunities.get(nodeB).get(i) <=0 in isInSameCS()");
			}
			if (intersectionCounters.get(nodeCommunities.get(nodeB).get(i)) != null &&  intersectionCounters.get(nodeCommunities.get(nodeB).get(i)) == intersectionCounter) { // if we find a common community
				return true; // return true
			}
		}
		return false; // otherwise, return false
	}

	/** 
	 * Tries to form a new community off of two nodes, that is, if they share an intersection of over three nodes
	 * @param nodeA The first node
	 * @param nodeB The second node
	 * @param graph The graph the algorithm is run on
	 * @throws OcdAlgorithmException if the execution failed
	 */
	public void tryFormingNewCommunity(int nodeA, int nodeB, CustomGraph graph) throws OcdAlgorithmException {
		int numEdge=0;
		findNodeIntersection(nodeA, nodeB, graph); // Find the intersection of both nodes adjacencyLists, the result is in nodeIntersections and the nodeIntersectionCounter	
		if ( nodeIntersectionCounter > minNode ) { // If they have something in common
			numEdge = findTotalNumberOfEdges(nodeIntersections, nodeIntersectionCounter); // Find number of edges in the intersection
			boolean isOK = (numEdge>=findTau(nodeIntersectionCounter, graph)); // Checks whether a maximum size for dense communities is met
			if (isOK) { // If we can form a new community
				communityCount++;
				markNodes(communityCount); // Mark all nodes in the nodeIntersection			
				communities.put(communityCount, new HashMap<Integer, Integer>(nodeIntersections.size())); // initialize communities[numCOM]
				for(int i=0; i<nodeIntersections.size(); i++) {
					communities.get(communityCount).put(i, nodeIntersections.get(i));
				}
				communityEdgeSizes.put(communityCount,numEdge); // Set the number of edges in numEdgeList[numCOM]
				communityNodeSizes.put(communityCount, nodeIntersectionCounter); // n_interG is the number of nodes inside this community
			}
		}
	}
	
	/**
	 * Finds the intersection of two nodes adjacencyLists (with both of them included)
	 * @param nodeA The first node
	 * @param nodeB The second node
	 * @param graph The graph the algorithm is run on
	 */
	public void findNodeIntersection(int nodeA, int nodeB, CustomGraph graph) {
		nodeIntersections = new ArrayList<Integer>(); //init/clear nodeIntersections
		nodeIntersectionCounter = 0; // the number of elements in the intersection is 0 at beginning
		updateCounter(); // update the counter	
		int i;
		for(i=0; i<nodeDegrees.get(nodeA); i++) {// mark nodes in adjList[nodeA]
			intersectionCounters.put(adjacencyList.get(nodeA).get(i), intersectionCounter);
		}
		for(i=0; i<nodeDegrees.get(nodeB); i++) { // if we find something in common of the two sets			
			if ( intersectionCounters.get(adjacencyList.get(nodeB).get(i)) != null && intersectionCounters.get(adjacencyList.get(nodeB).get(i)) == intersectionCounter) { //TODO: Null check added here, see if always sensible				
				nodeIntersections.add(adjacencyList.get(nodeB).get(i));
				nodeIntersectionCounter++;
				
			}
		}
		if (nodeIntersectionCounter > 0) { // if two sets intersect, include nodeA and nodeB in the intersection
			nodeIntersections.add(nodeA);
			nodeIntersectionCounter++;
			nodeIntersections.add(nodeB);
			nodeIntersectionCounter++;
		}
//DEBUG		System.out.println(nodeIntersections);
	}
	
	/**
	 * Finds the number of edges between a set of n nodes
	 * @param nodes The set of nodes
	 * @param n The number of nodes
	 * @return The number of edges within the set
	 */
	public int findTotalNumberOfEdges(ArrayList<Integer> nodes, int n) {
		if (n <= 1) { // If there is no more than one node, return 0
			return 0;
		}
		updateCounter(); // Update the counter
		int i=0, j=0, numEdges=0;
		for(i=0; i<n; i++) { // first mark all the nodes in the current list
			intersectionCounters.put(nodes.get(i), intersectionCounter);
		}
		for(i=0; i<n; i++) { // now, iterate through all elements in the list
			for(j=0; j<nodeDegrees.get(nodes.get(i)); j++) {
				if ( intersectionCounters.get(adjacencyList.get(nodes.get(i)).get(j)) != null && intersectionCounters.get(adjacencyList.get(nodes.get(i)).get(j)) == intersectionCounter ) {
					numEdges++;
				}
			}
		}
		return numEdges/2;
	}
	
	/**
	 * Combines all communities that have an overlappingScore greater than the overlappingThreshold
	 * @param graph The graph the algorithm is run on
	 * @throws OcdAlgorithmException if the execution failed
	 */
	public void combineOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException {
		boolean done=false;	
		double overlappingScore;
		HashMap<Integer, Integer> communityAdjacencyList = new HashMap<Integer, Integer>();
		int comI=0, j=0, comA=0, foundAdjacencies=0;
		Integer totalEdges = Integer.valueOf(0);		
		while (!done) { // Start checking for overlapped communities
			done = true;
			realCommunityCount = communityCount; // make a copy of the current number of communities
			for(comI = communityCount; comI >= 1; comI--) { // we start from bottom - up
				foundAdjacencies = findCommunityAdjacencyList(comI, communityAdjacencyList, foundAdjacencies, graph); // find the adjacent list of community comI, the result is given in communityAdjacencyList and communityCount
				if (foundAdjacencies <= 0) { // if this community does not overlap with any other community
					continue;
				}
				for(j=0; j<foundAdjacencies; j++) { // if this community is adjacent to at least a community
					comA = communityAdjacencyList.get(j);				
					overlappingScore = findOverlappingScore(comI, comA, totalEdges, graph); // find the overlapping score of the two communities
					if (overlappingScore >= overlappingThreshold) { // if the overlapping score is big enough									
						if (overlappingScore >= 2) { // If score means full overlap
							swapAndDeleteCommunities(comA, comI);
							realCommunityCount--; // update the real number of communities
							done = false; // tell the program that it is not done yet
						} else {
							mergeCommunities(comA, comI, communityIntersections, communityIntersectionCounter, totalEdges); // Combine the two sets & delete community i_th					
							realCommunityCount--; // update the real number of communities
							done = false; // tell the program that it is not done yet
							if ( communityNodeSizes.get(comI) != null ) { // if communities[i] disappears, we can skip it
								break;
							}
						}
					}
				}
			}
			if (!done) {
				reassignCommunityID(); // we reassign community ID right here
			}
		}
	}
	
	public int findCommunityAdjacencyList(int comA, HashMap<Integer, Integer> communityAdjacencyList, Integer foundAdjacencies, CustomGraph graph) throws OcdAlgorithmException { // function to find a list of communities that comA is adjacent to
		foundAdjacencies = 0;
		if (communityNodeSizes.get(comA) == null || communityNodeSizes.get(comA) <= 3) { // if community comA is just a triangle
			return 0; // return nothing
		}		
		int i=0, j=0, x=0, comId=0;
		updateCounter(); // update the counter
		for(i=0; i<communityNodeSizes.get(comA); i++) { // find the number of adjacent communities
			x = communities.get(comA).get(i);	// x is the current element in the list
			if (x > graph.getNodeCount() || x <0) { //TODO: was <= 0, see if this changes anything
				throw new OcdAlgorithmException("Error: x > N || x <0 in findComAdjList: " + x + " " + communities.get(comA));
			}
			if (communityNumbers.get(x) < 2) { // skip x if it is in just one community
				continue;
			}
			for(j=0; j<communityNumbers.get(x); j++) {
				comId = nodeCommunities.get(x).get(j); // comId is the id of one adjacent community
				if ((comId > communityCount && communityNodeSizes.get(comId)> 0)|| comId<=0) { //TODO: First was comId>communities.size(), check if ok like this
					throw new OcdAlgorithmException("Error: comId is gerater communityCount "  + comId + " " + communityCount + " " + x);
				}				
				if ( communityNodeSizes.get(comId)!= null && communityNodeSizes.get(comId)>0 && (intersectionCounters.get(comId) == null || intersectionCounters.get(comId)<intersectionCounter) && comId!=comA ) { // if x belongs to more than 2 communities, count it
					if (foundAdjacencies >= communityCount) { // safety check
						throw new OcdAlgorithmException("Error: size of communityAdjacencyList bigger total number of communities in findComAdjList " + j + " " + communityAdjacencyList.size() + " " + communities.size() + " " + communityNumbers.get(x));
					}
					communityAdjacencyList.put(foundAdjacencies, comId); // record the community ID
					foundAdjacencies++;
					intersectionCounters.put(comId,intersectionCounter);	// mark the id of Cid[x][j]
				}
			}
		}
		return foundAdjacencies;
	}
	
	/**
	 * Finds the overlappingScore for two communities(represented by their indexes), they overlap if they have more than 3 nodes in common or 
	 * if the sum of the fraction of shared nodes through the minimal number of shared nodes(3) and the fraction of edges in between the two through the minimal number of edges(edges of smaller community edge-wise) is greater than the overlapping threshold
	 * @param comA The first community
	 * @param comB The second community
	 * @param totalEdges The total number of edges in between the two communities
	 * @param graph The graph the algorithm is run on
	 * @return The overlapping score(Exactly 0 if no overlap, 2 if full overlap)
	 * @throws OcdAlgorithmException if the execution failed
	 */
	double findOverlappingScore(int comA, int comB, Integer totalEdges, CustomGraph graph) throws OcdAlgorithmException {
		int minNode = 0, minEdge = 0;
		minNode = Math.min(communityNodeSizes.get(comA) == null ? -1 : communityNodeSizes.get(comA), communityNodeSizes.get(comB) == null ? -1 : communityNodeSizes.get(comB)); // find the smaller set cardinality
		minEdge = Math.min(communityEdgeSizes.get(comA) == null ? -1 : communityEdgeSizes.get(comA), communityEdgeSizes.get(comB) == null ? -1 : communityEdgeSizes.get(comB)); // find the smaller number of edges
		if (minNode<=0 || minEdge<=0) {
			return 0; // <-- here, we don't output error since some merged communities may vanish and the system has not been updated yet
		}
		findCommunityIntersection(comA, comB); // find the intersection of comA and comB
		if (communityIntersectionCounter <= 1) {// if the intersection has less than one element
			return 0;
		}
		if (communityIntersectionCounter >= minNode) { // if they are fully overlapped
			return 2;
		}
		totalEdges = findTotalNumberOfEdges(communityIntersections, communityIntersectionCounter); // find number of edges in the intersection
		double res = (double)communityIntersectionCounter/minNode + (double)totalEdges/minEdge; // find the overlapping score
		if (res > 2) {
			throw new OcdAlgorithmException("Error: overlappingScore bigger than 2 in findOverlappingScore");
		}
		return res;
	}
	
	//TODO: Check if that goes well
	/**
	 * Swaps two communities(represented by their indices) if they overlap too much. The community with bigger ID will be deleted
	 * @param comA The first community
	 * @param comB The second community
	 */
	public void swapAndDeleteCommunities(int comA, int comB) {
		int bigger = Math.max(comA,comB),smaller = Math.min(comA,comB);
		if (communityNodeSizes.get(bigger) > communityNodeSizes.get(smaller)) { // if the bigger community has more data than the smaller one, we copy its data
			communityNodeSizes.put(smaller,communityNodeSizes.get(bigger));
			communityEdgeSizes.put(smaller, communityEdgeSizes.get(bigger));
			communities.put(smaller, communities.get(bigger)); // create new memory allocation
		}	
		// Else, what we simply do is to get rid of the bigger community		
		communityNodeSizes.put(bigger,0); 
		communityEdgeSizes.put(bigger,0);
		communities.remove(bigger);
//DEBUG		System.out.println("DELETED COMM " + bigger);
	}
	
	/**
	 * (Here comBb is greater than comAa. This is a very important step)
	 * Merges two communities(represented by their indices) by emptying the one with the bigger index and copying its values into the one with the smaller index
	 * @param comAa The first community
	 * @param comBb The second Community
	 * @param inter The intersection between the two communities
	 * @param n_inter The size of the intersection between the two communities
	 * @param interEdges The edges in the intersection between the two communities
	 * @throws OcdAlgorithmException if the execution failed
	 */
	public void mergeCommunities(int comAa, int comBb, ArrayList<Integer> inter, int n_inter, int interEdges) throws OcdAlgorithmException {
		int comA = Math.min(comAa, comBb), comB = Math.max(comAa, comBb);
		int i=0, n_tmp=0, nA = communityNodeSizes.get(comA)-n_inter;
		int tmp[] = new int[nA]; // create tmp[], a new array for communities[a] \ communities[b]
		int moreEdges = findExtraEdges(comA, comB, inter, n_inter); // find extra edges going between comA and comB
		updateCounter(); // update the counter
		for(i=0; i<communityNodeSizes.get(comB); i++){
			intersectionCounters.put(communities.get(comB).get(i), intersectionCounter); // mark elements in the bigger array
			nodeCommunities.get(communities.get(comB).get(i)).put(communityNumbers.get(communities.get(comB).get(i)), comA); // assign new community ID for communities[b][i]
			communityNumbers.set(communities.get(comB).get(i), communityNumbers.get(communities.get(comB).get(i)) +1);
		}
		n_tmp = 0;
		for(i=0; i<communityNodeSizes.get(comA); i++) { // mark elements in communities[a] only
			if (intersectionCounters.get(communities.get(comA).get(i)) != intersectionCounter) { // find communities[a] \ communities[b]
				if (n_tmp >= nA) { // Safety check
					throw new OcdAlgorithmException("Error: n_tmp >= n_communities[a] - n_inter + 1 in unionComID()");
				}
				tmp[ n_tmp++ ] = communities.get(comA).get(i); // copy to tmp			
			}
		}
		communityNodeSizes.put(comA, communityNodeSizes.get(comA) + communityNodeSizes.get(comB) - n_inter); // update the number of vertices in communities[a]
		communities.put(comA, new HashMap<Integer, Integer>(communityNodeSizes.get(comA))); // restore communities[a]
		for(i=0; i<n_tmp; i++) { // copy elements in communities[a] \ communities[b]
			communities.get(comA).put(i,tmp[i]);
		}
		for(i=0; i<communityNodeSizes.get(comB); i++) { // copy elements in communities[b]
			communities.get(comA).put(i+n_tmp, communities.get(comB).get(i));
		}
		communityNodeSizes.put(comB,0); // now, there should be no elements in communities[b]
		communityEdgeSizes.put(comA, communityEdgeSizes.get(comA) + communityEdgeSizes.get(comB) - interEdges + moreEdges); // update communityEdgeSizes
		
		// Remove all links to to-be-deleted community and decrease communityNumber for contained nodes
		communityEdgeSizes.put(comB,0);
		communities.remove(comB);
//DEBUG		System.out.println("UNIFIED COMMS, DELETED: " + comB + " " + communities.get(comA) + " " + communities.get(comB));
	}
	
	/**
	 * Finds extra edges between two communities
	 * @param comA The first community
	 * @param comB The second community
	 * @param comIntersection The intersection between the two communities
	 * @param commIntersectionCounter The size of the intersection
	 * @return The number of extra edges between the two communities
	 */
	public int findExtraEdges(int comA, int comB, ArrayList<Integer> comIntersection, int commIntersectionCounter) {
		updateCounter();
		int i=0, j=0, id=0, old=0, numExtra=0;
		for(i=0; i<commIntersectionCounter; i++) { // first mark nodes in the intersection
			intersectionCounters.put(comIntersection.get(i), intersectionCounter);
		}
		old = intersectionCounter + 1;
		for(i=0; i<communityNodeSizes.get(comA); i++) {// then mark nodes in comA with the new counter
			if ( intersectionCounters.get(communities.get(comA).get(i)) != intersectionCounter ) {
				intersectionCounters.put(communities.get(comA).get(i), old);
			}
		}
		old = intersectionCounter;
		updateCounter();
		for(i=0; i<communityNodeSizes.get(comB); i++) { // next, mark nodes in comB
			id = communities.get(comB).get(i);
			if (intersectionCounters.get(id) == old) { // skip over nodes in the intersection
				continue;
			}
			for(j=0; j<nodeDegrees.get(id); j++) {
				if (intersectionCounters.get(adjacencyList.get(id).get(j)) == intersectionCounter) {// if we find an edge
					numExtra++; // count it
				}
			}
		}	
		return numExtra;
	}
	
	//TODO: Check if sensible
	/** 
	 * Reassigns a community to another id
	 */
	public void reassignCommunityID() {
		int i=1, id=2, j=0, x=0; // **** very important initial parameters ****
		for(int index=0; index<communityNumbers.size(); index++) {
			communityNumbers.set(index, 0);
		}	
		
		updateCounter();
		for(j=0; j<communityNodeSizes.get(1); j++) { // refine all duplicate community IDs for the first community
			x = communities.get(1).get(j);
			if (intersectionCounters.get(x) != intersectionCounter ) {
				nodeCommunities.get(x).put(communityNumbers.get(x), 1); // assign community id for x TODO: Check if that will always go well as we don't address any id directly
				communityNumbers.set(x, communityNumbers.get(x) + 1);
				intersectionCounters.put(x,intersectionCounter); // mark x so that we dont have duplicates
			}
		}

		while (i <= communityCount) { // reassign new community ID
			for(i=i+1; i<=communityCount && communityNodeSizes.get(i) <= 0; i++); // skip over communities that were combined
			if (i > communityCount) {
				break;
			}
			if (i > id) { // if we encounter a big combined community, we need to copy it
				communities.put(id, new HashMap<Integer, Integer>(communityNodeSizes.get(i))); // initialize new memory allocation
				updateCounter();
				communityNodeSizes.put(id,0);
				for(j=0; j<communityNodeSizes.get(i); j++) { // copy nodes from community i to to community id
					x = communities.get(i).get(j);
					if ( intersectionCounters.get(x) != intersectionCounter ) { // make sure that we don't have a duplicate
						intersectionCounters.put(x, intersectionCounter);
						communities.get(id).put(communityNodeSizes.get(id), x); // include x into community id					
						communityNodeSizes.put(id, communityNodeSizes.get(id) +1);
						nodeCommunities.get(x).put(communityNumbers.get(x), id); //TODO: Check if that will always go well as we don't address any id directly
						communityNumbers.set(x, communityNumbers.get(x) +1); //TODO: Check if that will always go well as we don't address any id directly
					}
				}		
				communityEdgeSizes.put(id, communityEdgeSizes.get(i)); // update the edge sizes of community id;
				communityEdgeSizes.put(i,0); // update the edge sizes of community i
				communityNodeSizes.put(i,0); // clear the number of nodes in community i
				communities.remove(i);
			} else {
				updateCounter();			
				for(j=0; j<communityNodeSizes.get(i); j++) { // copy nodes from communities[i] to communities[id]
					x = communities.get(i).get(j);
					if ( intersectionCounters.get(x) != intersectionCounter ) { // make sure that we dont have a duplicate
						intersectionCounters.put(x, intersectionCounter);	
						nodeCommunities.get(x).put(communityNumbers.get(x), i);
						communityNumbers.set(x, communityNumbers.get(x) +1);
					}
				}
			}
			id++;
		}
		communityCount = realCommunityCount; // Update the real number of community;
	}
	
	/**
	 * Finds the intersection between two communities
	 * @param comA The first community
	 * @param comB The second community
	 */
	public void findCommunityIntersection(int comA, int comB) {
//DEBUG		System.out.println("Checking CommIntersection for " + comA + ":" + communities.get(comA) + ", " + comB + ":" + communities.get(comB));
		communityIntersectionCounter = 0; // the default number of nodes in the intersection is set to 0
		int j=0;
		updateCounter(); // update the counter
		for(j=0; j<communityNodeSizes.get(comA); j++) {
			intersectionCounters.put(communities.get(comA).get(j), intersectionCounter); // now, mark all nodes in comA[]
		}
		for(j=0; j<communityNodeSizes.get(comB); j++) { // scan comB to get the intersection
			if ( intersectionCounters.get(communities.get(comB).get(j)) == intersectionCounter) { // if there is a common node in comB[]
//DEBUG				System.out.println(communities.get(comB).get(j) + " is in");
				if(communityIntersectionCounter < communityIntersections.size()) {
					communityIntersections.set(communityIntersectionCounter, communities.get(comB).get(j)); // include this node to the intersection
				}
				else
				{
					communityIntersections.add(communities.get(comB).get(j));
				}
				communityIntersectionCounter++;
			}
		}
	}
	
	/**
	 * Finds tiny communities, i.e. those with less than minNode(3) nodes
	 * @param graph The graph the algorithm is run on
	 * @throws OcdAlgorithmException if the execution failed
	 */
	public void findTinyCommunities(CustomGraph graph) throws OcdAlgorithmException {
		realCommunityCount = communityCount;
		for(Edge edge : graph.edges().toArray(Edge[]::new)) { // Read the adjacent list to find N and M
			if ( communityNumbers.get(edge.getSourceNode().getIndex())>0 || communityNumbers.get(edge.getTargetNode().getIndex())>0 ) {
				continue;
			}
			findNodeIntersection(edge.getSourceNode().getIndex(), edge.getTargetNode().getIndex(), graph); // Find the intersection of the two adjacencyLists
			if (nodeIntersectionCounter == 3) { // If we find a triangle
				if (communityCount >= graph.getNodeCount()) { // If communityCount exceeds the upper bound, return error
					throw new OcdAlgorithmException("Error : numCOM >= MULTI_N in findTinyCommunities()" + " " + communities.size() + " " + graph.getNodeCount() + "\n" + communities);
				}
				markNodes(communityCount + 1); // Mark all the nodes in the intersection
				communityCount++;
				communities.put(communityCount, new HashMap<Integer, Integer>(nodeIntersections.size()));
				for(int i=0; i<nodeIntersections.size(); i++) {					
					communities.get(communityCount).put(i, nodeIntersections.get(i));				
				}
				communityNodeSizes.put(communityCount, 3); // Update the communityNodeSize at communityCount
				communityEdgeSizes.put(communityCount, 3); // Update the communityEdgeSizes at communityCount
			}
		}	
	}
	
	//TODO: Check datatypes here
	/**
	 * Assigns community IDs for vertices that have not been assigned ones yet
	 * @param graph The graph the algorithm is run on
	 * @throws OcdAlgorithmException if the execution failed
	 */
	public void visitUnAssignedVertices(CustomGraph graph) throws OcdAlgorithmException {		
		int i, j, id, x, k, n_res;
		int mark[] = new int[communityCount+1];
		HashMap<Integer, HashMap<Integer, Integer>> res = new HashMap<Integer, HashMap<Integer, Integer>>();
		res.put(0, new HashMap<Integer, Integer>(communityCount+1));
		res.put(1, new HashMap<Integer, Integer>(communityCount+1));	
		HashMap<Integer, Integer> oldCommunityNodeSizes;
		HashMap<Integer, Integer> numDegList = new HashMap<Integer, Integer>(communityCount+1);
		maxOutliers = 0; // The number of outliers	
		findNumDegList(numDegList); // Find the numDegList;
		oldCommunityNodeSizes = new HashMap<Integer, Integer>(communityNodeSizes);
		for(i=0; i<graph.getNodeCount(); i++) { // Begin to find
			if (communityNumbers.get(i) > 0) {
				continue;
			}
			n_res = 0;
			for(j=0; j<nodeDegrees.get(i); j++) { // Find the communityNumbers of communities that are adjacent to vertex i and the number of edges i connects to them
				x = adjacencyList.get(i).get(j); // x is the vertex number
				for(k=0; k<communityNumbers.get(x); k++) { // Iterate from all community IDs that x may have
					id = nodeCommunities.get(x).get(k);
					if ( mark[id] == 0 ) {
						mark[id] = ++n_res;
						res.get(0).put(n_res, id);
					}
					res.get(1).put(mark[id], (res.get(1).get(mark[id]) == null ? 0 : res.get(1).get(mark[id])) +1); ///<--- Remember to replace by weight here, if necessary -->///
				}
			}
			if (n_res <= 0) { // If this node doesn't connect to any other community
				maxOutliers++;
				continue;
			}
			double ratio = 0, newRatio = 0;		
			communityNumbers.set(i,0);
			for(j=1; j<=n_res; j++) { // Find the maximum score from those adjacent communities
				if (res.get(0).get(j) > realCommunityCount) { // Skip over tiny communities (i.e. triangles)
					continue;
				}
				ratio = (1.15 * communityEdgeSizes.get(res.get(0).get(j)))/numDegList.get(res.get(0).get(j));
				newRatio = (double)(communityEdgeSizes.get(res.get(0).get(j))+ res.get(1).get(j))/(numDegList.get(res.get(0).get(j))+nodeDegrees.get(i));
				if (newRatio >= ratio) {					
					nodeCommunities.get(i).put(communityNumbers.get(i), res.get(0).get(j)); // Assign node i to community res[0][j]
					communityNumbers.set(i, communityNumbers.get(i) +1);
					communityNodeSizes.put(res.get(0).get(j), communityNodeSizes.get(res.get(0).get(j)) +1); // Increase the number of nodes inside community res[0][j]
					communityEdgeSizes.put(res.get(0).get(j), communityEdgeSizes.get(res.get(0).get(j)) + res.get(1).get(j)); // Update numEdgeList
					numDegList.put(res.get(0).get(j), numDegList.get(res.get(0).get(j)) + nodeDegrees.get(i)); // Update numDegList
				}
			}		
		}
		//finalRefinement(oldCommunityNodeSizes, graph); // Final refinement TODO: Remove
		for(i=1;i<=graph.getNodeCount();i++) {
			maxCommunityNumber = Math.max( maxCommunityNumber, communityNumbers.get(i) );
		}
		for(i=1;i<=communityCount;i++) {
			maxCommunityNodeSize = Math.max( maxCommunityNodeSize, (communityNodeSizes.get(i) != null ? communityNodeSizes.get(i) : 0) );
		}
	}
	
	/**
	 * Updates the intersectionCounter
	 */
	private void updateCounter() {
		//TODO: Check whether this safety check is still necessary;
		//if (nodeIntersectionCounter >= SAFE_ULONGMAX) { // if the counter gets too large...
		//	memset(countInt, 0, MULTI_N * UINT_SIZE); // clear the counter
		//	nodeIntersectionCounter = 0; // reset it
		//}
		intersectionCounter++; // increase the counter
	}
	
	/**
	 * Marks nodes in a community(represented by index)
	 * @param communityId The community index
	 */
	private void markNodes(int communityId) {
		for(int i=0; i<nodeIntersectionCounter; i++) {
//DEBUG			System.out.println("Marking for " + nodeIntersections.get(i));
//DEBUG			System.out.println(nodeCommunities.get(nodeIntersections.get(i)) + " " + communityNumbers.get(nodeIntersections.get(i)));
			nodeCommunities.get(nodeIntersections.get(i)).put(communityNumbers.get(nodeIntersections.get(i)), communityId); // mark nodes with their corresponding comID
			communityNumbers.set(nodeIntersections.get(i), communityNumbers.get(nodeIntersections.get(i)) + 1);
		}
	}

	/**
	 * Finds the tau to calculate the maximum number of nodes in a community
	 * @param n The number of nodes
	 * @param graph The graph the algorithm is run on
	 * @return tau
	 * @throws OcdAlgorithmException
	 */
	private int findTau(int n, CustomGraph graph) throws OcdAlgorithmException { // find the suffice number of edges
		if (n <= 3) { // if we dont have enough nodes
			return 0;
		}
		if (n > graph.getNodeCount()) { // if the number of nodes gets too large
			throw new OcdAlgorithmException("Error: Counter too large for findTau");
		}
		int pn = n*(n-1)/2; // since we have a safety check above, this step should not be a problem
		return (int)(Math.round((Math.pow(pn, 1.0 - 1.0/pn))));
	}
	
	private void findNumDegList(HashMap<Integer, Integer> arr) {
		int i,j;
		for(i=1; i<=communityCount; i++) { // Find the numDegList
			if(communityNodeSizes.get(i) != null) {
				for(j=0; j<communityNodeSizes.get(i); j++) {
					arr.put(i, (i>=arr.size() ? 0 : arr.get(i)) + nodeDegrees.get(communities.get(i).get(j)));				
				}
			}
		}
	}	

}
