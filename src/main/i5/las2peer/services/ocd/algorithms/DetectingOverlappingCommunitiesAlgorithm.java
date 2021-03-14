package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.coverInput.CommunityMemberListsCoverInputAdapter;
import i5.las2peer.services.ocd.adapters.coverInput.CoverInputAdapterFactory;
import i5.las2peer.services.ocd.adapters.coverInput.CoverInputFormat;
import i5.las2peer.services.ocd.adapters.graphOutput.DocaGraphOutputAdapter;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputAdapter;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.benchmarks.OcdBenchmarkException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.Edge;
import y.base.Node;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang3.SystemUtils;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

/**
 * This class is an implementation of the DetectingOverlappingCommunities algorithm from 
 * and based on their C++ implementation
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
	private static String OVERLAPPING_THRESHOLD_NAME = "overlappingThreshold";
	
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
		/*
		 * TODO reinsert for use on Windows / when implemented for Linux
		 */
		// return CoverCreationType.DETECTING_OVERLAPPING_COMMUNITIES_ALGORITHM;
		return null;
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
		System.out.println(communities);
		Matrix membershipMatrix = new Basic2DMatrix(graph.nodeCount(), communities.size());
		
		int j=0;
		for(Map.Entry<Integer, HashMap<Integer,Integer>> communityPair : communities.entrySet()) {
			for(int nodeIndex : communityPair.getValue().values()) {
				membershipMatrix.set(nodeIndex, j, 1);
			}
			j++;
		}
		
		for(int i=0; i<membershipMatrix.rows(); i++) {
			membershipMatrix.setRow(i, membershipMatrix.getRow(i).divide(membershipMatrix.getRow(i).sum())); // Set correct ratios
		}
		
		return membershipMatrix;
	}
	
	public void findOverlappingCommunityStructures(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
		communityIntersections = new ArrayList<Integer>();//initialize communityIntersections
		nodeDegrees = new HashMap<Integer, Integer>(graph.nodeCount());
		adjacencyList = new  HashMap<Integer, ArrayList<Integer>>(graph.nodeCount());
		for(Node node : graph.getNodeArray()) {// Initialize nodeDegree, maxDegree, AdjacencyList
			Set<Node> neighbours = graph.getNeighbours(node);
			nodeDegrees.put(node.index(), neighbours.size());
			
			maxDegree = Math.max(maxDegree, nodeDegrees.get(node.index()));
			
			adjacencyList.put(node.index(), new ArrayList<Integer>(neighbours.size()));
			for(Node neighbour : neighbours) {
				adjacencyList.get(node.index()).add(neighbour.index());
			}
		}
		
		findDenseCommunities(graph);
		combineOverlappingCommunities(graph);
		findTinyCommunities(graph);
		visitUnAssignedVertices(graph);
	}
	
	public void findDenseCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
		//init nodeCommunities, communityNumbers, intersectionCounters
		communityNumbers = new ArrayList<Integer>(graph.nodeCount());
		intersectionCounters = new HashMap<Integer,Integer>();
		nodeCommunities = new HashMap<Integer, HashMap<Integer, Integer>>(graph.nodeCount());
		for(int i=0; i<=graph.nodeCount(); i++) { // initially, each node has one community ID, TODO: i was 1 first, check if this breaks anything
			nodeCommunities.put(i, new HashMap<Integer, Integer>());
			communityNumbers.add(0);
		}
		//memset(intersectionCounters, 0, UINT_SIZE*(MULTI_N)); // set up the intersectionCounters array
				
		//init communities, communityNodeSizes, communityEdgeSizes
		communities = new HashMap<Integer, HashMap<Integer, Integer>>();
		communityNodeSizes = new HashMap<Integer, Integer>();
		//memset(communityNodeSizes, 0, UINT_SIZE*MULTI_N); // memset ComID counter, make sure it contains all 0's at the beginning
		communityEdgeSizes = new HashMap<Integer, Integer>();
		//memset(communityEdgeSizes, 0, UINT_SIZE*MULTI_N);		
		
		int numEdge=0;
		for(Edge edge : graph.getEdgeArray()) { // reading from the beginning of file
			if (!sameCommunity(edge.source().index(), edge.target().index())) 
			{ // if a and b are not in a community together
				tryFormingNewCommunity(edge.source().index(), edge.target().index(), graph); // try to form a dense local community from (a,b)
			}
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
		}
		//resizeComID_n_ComID(); // resize ComID[][] and n_ComID[] arrays
	}
	
	public boolean sameCommunity(int nodeA, int nodeB) throws OcdAlgorithmException {
		updateCounter();
		if (communityNumbers.get(nodeA)<=0 || communityNumbers.get(nodeB)<=0) { // if one of them is fresh
			return false; // then they will not be
		}
		for(int i=0; i<communityNumbers.get(nodeA); i++) { // otherwise, mark all Cid(s) of node A
			if (nodeCommunities.get(nodeA).get(i) > communityCount || nodeCommunities.get(nodeA).get(i) <=0) { // safety check
				throw new OcdAlgorithmException("Error: Cid[a][i] > numCOM || Cid[a][i] <=0 in isInSameCS()");
			}
			intersectionCounters.put(nodeCommunities.get(nodeA).get(i), intersectionCounter);
		}
		for(int i=0; i<communityNumbers.get(nodeB); i++) { // and then, mark Cid(s) of node B
			if (nodeCommunities.get(nodeB).get(i) > communityCount || nodeCommunities.get(nodeB).get(i) <=0) {
				throw new OcdAlgorithmException("Error: Cid[b][i] > numCOM || Cid[b][i] <=0 in isInSameCS()");
			}
			if (intersectionCounters.get(nodeCommunities.get(nodeB).get(i)) == intersectionCounter) { // if we find a common community
				return true; // return true
			}
		}
		return false; // otherwise, return false
	}

	public void tryFormingNewCommunity(int nodeA, int nodeB, CustomGraph graph) throws OcdAlgorithmException {
		int numEdge=0, sufficeEdge=0;
		findIntersectionLinearG(nodeA, nodeB, graph); // find adjList[nodeA] intersect adjList[nodeB], the result is in interG[] and n_interG	
		if ( nodeIntersectionCounter > minNode ) { // if they have something in common
			numEdge = findTotalNumberOfEdges(nodeIntersections, nodeIntersectionCounter); // find number of edges in the intersection
			boolean isOK = (numEdge>=findTau(nodeIntersectionCounter, graph)); //1000 maximum for dense communities
			if (isOK) { // if we can form a new community
				communityCount++;
				markNodes(communityCount); // mark all nodes in the intersection interG[]			
				communities.put(communityCount, new HashMap<Integer, Integer>(nodeIntersections.size())); // initialize ComID[numCOM]
				for(int i=0; i<nodeIntersections.size(); i++) {
					communities.get(communityCount).put(i, nodeIntersections.get(i));
				}
				//memcpy(ComID[numCOM], nodeIntersections, nodeIntersectionCounter * UINT_SIZE); // copy data from interG[]
				communityEdgeSizes.put(communityCount,numEdge); // set the number of edges in numEdgeList[numCOM]
				communityNodeSizes.put(communityCount, nodeIntersectionCounter); // n_interG is the number of nodes inside this community
			}
		}
	}
	
	// Find the intersection of adjacencyList[nodeA] and adjacencyList[nodeB].
	// In the end, we put *nodeA* and *nodeB* to the end of the intersection
	public void findIntersectionLinearG(int nodeA, int nodeB, CustomGraph graph) {
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
		System.out.println(nodeIntersections);
	}
	
	// This function finds the number of edges within the set nodes[] of n nodes
	public int findTotalNumberOfEdges(ArrayList<Integer> nodes, int n) {
		System.out.println(nodes.size() + " " + n);
		if (n <= 1) { // if there is no more than a node, return 0
			return 0;
		}
		updateCounter(); // update the counter
		int i=0, j=0, numEdges=0;
		for(i=0; i<n; i++) { // first mark all the nodes in the current list
			intersectionCounters.put(nodes.get(i), intersectionCounter);
		}
		for(i=0; i<n; i++) { // now, iterate through all elements in the list
			for(j=0; j<nodeDegrees.get(nodes.get(i)); j++) {
				if ( intersectionCounters.get(adjacencyList.get(nodes.get(i)).get(j)) == intersectionCounter ) {
					numEdges++;
				}
			}
		}
		return numEdges/2;
	}
	
	public void combineOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException {
		boolean done=false;	
		double overlappingScore;
		HashMap<Integer, Integer> communityAdjacencyList = new HashMap<Integer, Integer>();
		int comI=0, j=0, round=1, comA=0, foundAdjacencies=0;
		Integer totalEdges = new Integer(0);
		 // initialize the comAdjList[]
		while (!done) { // start checking overlapped communities
			done = true;
			realCommunityCount = communityCount; // make a copy of the current number of communities
			for(comI = communityCount; comI >= 1; comI--) { // we start from bottom - up
				foundAdjacencies = findCommunityAdjacencyList(comI, communityAdjacencyList, foundAdjacencies, graph); // find the adjacent list of community comI, the result is in comAdjList[] and n_comAdjList
				if (foundAdjacencies <= 0) { // if this community does not overlap with any other community
					continue;
				}
				for(j=0; j<foundAdjacencies; j++) { // if this community is adjacent to at least a community
					comA = communityAdjacencyList.get(j);				
					overlappingScore = findOverlappingScore(comI, comA, totalEdges, graph); // find the overlapping score of the two ComID
					if (overlappingScore >= overlappingThreshold) { // if the overlapping score is too big										
						if (overlappingScore >= 2) { // If score means full overlap
							swapAndDeleteComID(comA, comI);
							realCommunityCount--; // update the real number of communities
							done = false; // tell the program that it is not done yet
						} else {
							unionComID(comA, comI, communityIntersections, communityIntersectionCounter, totalEdges); // Combine the two sets & delete community i_th					
							realCommunityCount--; // update the real number of communities
							done = false; // tell the program that it is not done yet
							if ( communityNodeSizes.get(comI) != null ) { // if ComID[i] disappears, we can skip it
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
		if (communityNodeSizes.get(comA) <= 3) { // if community comA is just a triangle
			return 0; // return nothing
		}		
		int i=0, j=0, x=0, comId=0;
		updateCounter(); // update the counter
		for(i=0; i<communityNodeSizes.get(comA); i++) { // find the number of adjacent communities
			x = communities.get(comA).get(i);	// x is the current element in the list
			if (x > graph.nodeCount() || x <0) { //TODO: was <= 0, see if this changes anything
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
	
	double findOverlappingScore(int comA, int comB, Integer totalEdges, CustomGraph graph) throws OcdAlgorithmException {
		int minNode = 0, minEdge = 0;
		minNode = Math.min(communityNodeSizes.get(comA) == null ? -1 : communityNodeSizes.get(comA), communityNodeSizes.get(comB) == null ? -1 : communityNodeSizes.get(comB)); // find the smaller set cardinality
		minEdge = Math.min(communityEdgeSizes.get(comA) == null ? -1 : communityEdgeSizes.get(comA), communityEdgeSizes.get(comB) == null ? -1 : communityEdgeSizes.get(comB)); // find the smaller number of edges
		if (minNode<=0 || minEdge<=0) {
			return 0; // <-- here, we don't output error since some merged communities may vanish and the system has not been updated yet
		}
		findComIDIntersectionH(comA, comB); // find the intersection of ComID[comA] and ComID[comB]
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
	public void swapAndDeleteComID(int i, int j) { // swap communities i and j if they overlap too much. The community with bigger ID will be deleted
		int bigger = Math.max(i,j),smaller = Math.min(i,j);
		if (communityNodeSizes.get(bigger) > communityNodeSizes.get(smaller)) { // if ComID[bigger] has more data than ComID[smaller], we copy ComID[smaller] <-- ComID[bigger]
			communityNodeSizes.put(smaller,communityNodeSizes.get(bigger));
			communityEdgeSizes.put(smaller, communityEdgeSizes.get(bigger));
			communities.put(smaller, communities.get(bigger)); // create new memory allocation
		}	
		// else, what we simply do is to get rid of the bigger community ComID[bigger]
		// Remove all links to to-be-deleted community and decrease communityNumber for contained nodes TODO: Was not here before, check if ok
		//for(int nodeIndex : communities.get(bigger).values()) {
		//	System.out.print(nodeIndex + " ");
		//	nodeCommunities.get(nodeIndex).remove(bigger);
		//	communityNumbers.set(nodeIndex, communityNumbers.get(nodeIndex) - 1);
		//}
		//System.out.println();
		
		communityNodeSizes.remove(bigger); // we need to free up n_ComID[bigger]
		communityEdgeSizes.remove(bigger); // and NumEdgeList[bigger]
		communities.remove(bigger);
		System.out.println("DELETED COMM " + bigger);
	}
	
	//TODO: Also storage-critical, check!
	// Here bb > aa. This is a very important step
	public void unionComID(int aa, int bb, ArrayList<Integer> inter, int n_inter, int interEdges) throws OcdAlgorithmException {
		int a = Math.min(aa, bb), b = Math.max(aa, bb);
		int i=0, n_tmp=0, nA = communityNodeSizes.get(a)-n_inter;
		int tmp[] = new int[nA]; // create tmp[], a new array for ComID[a] \ ComID[b]
		int moreEdges = findExtraEdges(a, b, inter, n_inter); // find extra edges going between comA and comB
		updateCounter(); // update the counter
		for(i=0; i<communityNodeSizes.get(b); i++){
			intersectionCounters.put(communities.get(b).get(i), intersectionCounter); // mark elements in the bigger array
			nodeCommunities.get(communities.get(b).get(i)).put(communityNumbers.get(communities.get(b).get(i)), a); // assign new community ID for ComID[b][i]
			communityNumbers.set(communities.get(b).get(i), communityNumbers.get(communities.get(b).get(i)) +1);
		}
		n_tmp = 0;
		for(i=0; i<communityNodeSizes.get(a); i++) { // mark elements in ComID[a] only
			if (intersectionCounters.get(communities.get(a).get(i)) != intersectionCounter) { // find ComID[a] \ ComID[b]
				if (n_tmp >= nA) { // Safety check
					throw new OcdAlgorithmException("Error: n_tmp >= n_ComID[a] - n_inter + 1 in unionComID()");
				}
				tmp[ n_tmp++ ] = communities.get(a).get(i); // copy to tmp			
			}
		}
		communityNodeSizes.put(a, communityNodeSizes.get(a) + communityNodeSizes.get(b) - n_inter); // update the number of vertices in ComID[a]
		communities.put(a, new HashMap<Integer, Integer>(communityNodeSizes.get(a))); // restore ComID[a]
		for(i=0; i<n_tmp; i++) { // copy elements in ComID[a] \ ComID[b]
			communities.get(a).put(i,tmp[i]);
		}
		for(i=0; i<communityNodeSizes.get(b); i++) { // copy elements in ComID[b]
			communities.get(a).put(i+n_tmp, communities.get(b).get(i));
		}
		communityNodeSizes.put(b,0); // now, there should be no elements in ComID[b]
		communityEdgeSizes.put(a, communityEdgeSizes.get(a) + communityEdgeSizes.get(b) - interEdges + moreEdges); // update the numEdgeList
		
		// Remove all links to to-be-deleted community and decrease communityNumber for contained nodes TODO: Was not here before, check if ok
		//for(int nodeIndex : communities.get(b).values()) {
		//	System.out.print(nodeIndex + " " + nodeCommunities.get(nodeIndex).remove(b) + " ");
		//
		//	communityNumbers.set(nodeIndex, communityNumbers.get(nodeIndex) - 1);
		//}
		//System.out.println();
		communityEdgeSizes.remove(b);
		communities.remove(b);
		System.out.println("UNIFIED COMMS, DELETED: " + b + " " + communities.get(a) + " " + communities.get(b));
	}
	
	// Function to find the extra edges that comA had to comB, other than the intersected edges arr[]
	int findExtraEdges(int comA, int comB, ArrayList<Integer> arr, int n_arr) {
		updateCounter();
		int i=0, j=0, id=0, old=0, numExtra=0;
		for(i=0; i<n_arr; i++) { // first mark nodes in the intersection
			intersectionCounters.put(arr.get(i), intersectionCounter);
		}
		old = intersectionCounter + 1;
		for(i=0; i<communityNodeSizes.get(comA); i++) {// then mark nodes in comA[] with the new counter
			if ( intersectionCounters.get(communities.get(comA).get(i)) != intersectionCounter ) {
				intersectionCounters.put(communities.get(comA).get(i), old); // just for speed up, should be countInt[ ComID[comA][i] ] = n_countInt + 1
			}
		}
		old = intersectionCounter;
		updateCounter(); // now, n_countInt <-- n_countInt + 1;
		for(i=0; i<communityNodeSizes.get(comB); i++) { // next, mark nodes in comB[]
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
	public boolean reassignCommunityID() { // here ncomid2[] is the array with the exact number of node in each community
		int i=1, id=2, j=0, x=0; // **** very important initial parameters ****
		for(int index=0; index<communityNumbers.size(); index++) {
			communityNumbers.set(index, 0);
		}		
		//------------------------------------//
		updateCounter();
		for(j=0; j<communityNodeSizes.get(1); j++) { // refine all duplicate community ID for ComID[1]
			x = communities.get(1).get(j);
			if (intersectionCounters.get(x) != intersectionCounter ) {
				nodeCommunities.get(x).put(communityNumbers.get(x), 1); // assign community id for x TODO: Check if that will always go well as we don't address any id directly
				communityNumbers.set(x, communityNumbers.get(x) + 1);
				intersectionCounters.put(x,intersectionCounter); // mark x so that we dont have duplicates
			}
		}
		//------------------------------------//
		while (i <= communityCount) { // reassign new community ID
			for(i=i+1; i<=communityCount && communityNodeSizes.get(i)<=0; i++); // skip over communities that were combined
			if (i > communityCount) {
				break;
			}
			if (i > id) { // if we encounter a big combined community, we need to copy it
				communities.put(id, new HashMap<Integer, Integer>(communityNodeSizes.get(i))); // initialize new memory allocation
				updateCounter();
				communityNodeSizes.put(id,0);
				for(j=0; j<communityNodeSizes.get(i); j++) { // copy nodes from ComID[i] to ComID[id]
					x = communities.get(i).get(j);
					if ( intersectionCounters.get(x) != intersectionCounter ) { // make sure that we dont have a duplicate
						intersectionCounters.put(x, intersectionCounter);
						communities.get(id).put(communityNodeSizes.get(id), x); // inlude x into ComID[id]						
						communityNodeSizes.put(id, communityNodeSizes.get(id) +1);
						nodeCommunities.get(x).put(communityNumbers.get(x), id); //TODO: Check if that will always go well as we don't address any id directly
						communityNumbers.set(x, communityNumbers.get(x) +1); //TODO: Check if that will always go well as we don't address any id directly
					}
				}		
				communityEdgeSizes.put(id, communityEdgeSizes.get(i)); // update numEdgeList[id];
				communityEdgeSizes.remove(i); // update numEdgeList[i]
				communityNodeSizes.put(i,0); // clear the number of nodes in community ith
				communities.remove(i);
			} else {
				updateCounter();			
				for(j=0; j<communityNodeSizes.get(i); j++) { // copy nodes from ComID[i] to ComID[id]
					x = communities.get(i).get(j);
					if ( intersectionCounters.get(x) != intersectionCounter ) { // make sure that we dont have a duplicate
						intersectionCounters.put(x, intersectionCounter);	
						nodeCommunities.get(x).put(communityNumbers.get(x), i);
						communityNumbers.set(x, communityNumbers.get(x) +1);
					}
				}
			}
			//------------------------------//
			id++;
			//------------------------------//
		}
		communityCount = realCommunityCount; // Update the real number of community;
		return true;
	}
	
	public int findComIDIntersectionH(int comA, int comB) {
		System.out.println("Checking CommIntersection for " + comA + ":" + communities.get(comA) + ", " + comB + ":" + communities.get(comB));
		communityIntersectionCounter = 0; // the default number of nodes in the intersection is set to 0
		int j=0;
		updateCounter(); // update the counter
		for(j=0; j<communityNodeSizes.get(comA); j++) {
			intersectionCounters.put(communities.get(comA).get(j), intersectionCounter); // now, mark all nodes in comA[]
		}
		for(j=0; j<communityNodeSizes.get(comB); j++) { // scan comB to get the intersection
			if ( intersectionCounters.get(communities.get(comB).get(j)) == intersectionCounter) { // if there is a common node in comB[]
				System.out.println(communities.get(comB).get(j) + " is in");
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
		return 1;
	}
	
	public void findTinyCommunities(CustomGraph graph) throws OcdAlgorithmException {
		realCommunityCount = communityCount;
		for(Edge edge : graph.getEdgeArray()) { // Read the adjacent list to find N and M
			if ( communityNumbers.get(edge.source().index())>0 || communityNumbers.get(edge.target().index())>0 ) {
				continue;
			}
			findIntersectionLinearG(edge.source().index(), edge.target().index(), graph); // Find the intersection of adjList[a] and adjList[b]
			if (nodeIntersectionCounter == 3) { // If we find a triangle
				if (communityCount >= graph.nodeCount()) { // If numCOM exceed the upper bound, return error
					throw new OcdAlgorithmException("Error : numCOM >= MULTI_N in findTinyCommunities()" + " " + communities.size() + " " + graph.nodeCount() + "\n" + communities);
				}
				markNodes(communityCount + 1); // Mark all the node in the intersection
				communityCount++;
				communities.put(communityCount, new HashMap<Integer, Integer>(nodeIntersections.size())); // Locate the memory for this tiny com
				System.out.println("BEFORETINY: " + communities.get(communityCount));
				for(int i=0; i<nodeIntersections.size(); i++) {					
					communities.get(communityCount).put(i, nodeIntersections.get(i));				
				}
				System.out.println("AFTERTINY: " + communities.get(communityCount));
				communityNodeSizes.put(communityCount, 3); // Update n_ComID[numCOM]
				communityEdgeSizes.put(communityCount, 3); // Update the numEdgeList
			}
		}	
	}
	
	//TODO: Check datatypes here
	// This function assign community IDs for vertices that have not been assigned ones yet
	public void visitUnAssignedVertices(CustomGraph graph) throws OcdAlgorithmException {
		int i, j, id, x, k, n_res;
		int mark[] = new int[communityCount+1];
		HashMap<Integer, ArrayList<Integer>> res = new HashMap<Integer, ArrayList<Integer>>();
		res.put(0, new ArrayList<Integer>(communityCount+1));
		res.put(1, new ArrayList<Integer>(communityCount+1));	
		HashMap<Integer, Integer> oldCommunityNodeSizes;
		HashMap<Integer, Integer> numDegList = new HashMap<Integer, Integer>(communityCount+1);
		maxOutliers = 0; // The number of outliers	
		//memset(numDegList, 0, UINT_SIZE*(communities.size()+1)); // Initialize the numDegList array
		findNumDegList(numDegList); // Find the numDegList;
		oldCommunityNodeSizes = new HashMap<Integer, Integer>(communityNodeSizes);
		for(i=0; i<graph.nodeCount(); i++) { // Begin to find
			if (communityNumbers.get(i) > 0) {
				continue;
			}
			//memset(res[0], 0, (communities.size()+1)*UINT_SIZE);
			//memset(res[1], 0, (communities.size()+1)*UINT_SIZE);
			//memset(mark, 0, (communities.size()+1)*UINT_SIZE);
			n_res = 0;
			System.out.println(i);
			for(j=0; j<nodeDegrees.get(i); j++) { // Find the Cids of communities that are adjacent to vertex i and the number of edges i connects to them
				x = adjacencyList.get(i).get(j); // x is the vertex number
				for(k=0; k<communityNumbers.get(x); k++) { // Iterate from all community IDs that x may have
					id = nodeCommunities.get(x).get(k);
					if ( mark[id] == 0 ) {
						mark[id] = n_res++;
						res.get(0).add(id);
					}
					res.get(1).set(mark[id], res.get(1).get(mark[id])+1); ///<--- Remember to replace by weight here, if necessary -->///
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
		finalRefinement(oldCommunityNodeSizes, graph); // Final refinement
		for(i=1;i<=graph.nodeCount();i++) {
			maxCommunityNumber = Math.max( maxCommunityNumber, communityNumbers.get(i) );
		}
		for(i=1;i<=communityCount;i++) {
			maxCommunityNodeSize = Math.max( maxCommunityNodeSize, communityNodeSizes.get(i) );
		}
	}
	
	private void updateCounter() {
		//TODO: Check whether this safety check is still necessary;
		//if (nodeIntersectionCounter >= SAFE_ULONGMAX) { // if the counter gets too large...
		//	memset(countInt, 0, MULTI_N * UINT_SIZE); // clear the counter
		//	nodeIntersectionCounter = 0; // reset it
		//}
		intersectionCounter++; // increase the counter
	}
	
	//TODO: Check if that monster of an assignment was done correctly
	private void markNodes(int communityID) {
		for(int i=0; i<nodeIntersectionCounter; i++) {
			System.out.println("Marking for " + nodeIntersections.get(i));
			System.out.println(nodeCommunities.get(nodeIntersections.get(i)) + " " + communityNumbers.get(nodeIntersections.get(i)));
			nodeCommunities.get(nodeIntersections.get(i)).put(communityNumbers.get(nodeIntersections.get(i)), communityID); // mark nodes with their corresponding comID
			communityNumbers.set(nodeIntersections.get(i), communityNumbers.get(nodeIntersections.get(i)) + 1);
		}
	}

	private int findTau(int n, CustomGraph graph) throws OcdAlgorithmException { // find the suffice number of edges
		if (n <= 3) { // if we dont have enough nodes
			return 0;
		}
		if (n > graph.nodeCount()) { // if the number of nodes gets too large
			throw new OcdAlgorithmException("Error: Counter too large for findTau");
		}
		int pn = n*(n-1)/2; // since we have a safety check above, this step should not be a problem
		return (int)(Math.round((Math.pow(pn, 1.0 - 1.0/pn))));
	}
	
	private void findNumDegList(HashMap<Integer, Integer> arr) {
		int i,j;
		for(i=1; i<=communityCount; i++) { // Find the numDegList
			for(j=0; j<communityNodeSizes.get(i); j++) {
				arr.put(i, (i>=arr.size() ? 0 : arr.get(i)) + nodeDegrees.get(communities.get(i).get(j)));				
			}
		}
	}
	
	private void finalRefinement(HashMap<Integer, Integer> oldCommunityNodeSizes, CustomGraph graph) throws OcdAlgorithmException {
		int i, j;
		
		for(i=1; i<=communityCount; i++) {
			if (oldCommunityNodeSizes.get(i) == communityNodeSizes.get(i) || communityNodeSizes.get(i) <=0 ) { // if nothing changes, we skip
				continue;
			}
			// else, we need to do reallocate memory
			communities.put(i, new HashMap<Integer, Integer>(communityNodeSizes.get(i))); // allocate new memory
		}
		for(Integer key : communityNodeSizes.keySet()) {
			communityNodeSizes.put(key,0);
		}
		for(i=0; i<graph.nodeCount(); i++) { // refining all Cid(s) of all nodes TODO: Remove, this is just ordering of the nodes in the community lists and essentially completely worthless for us as we use a membership matrix
			for(j=0; j<communityNumbers.get(i); j++) {
				if (nodeCommunities.get(i).get(j) > communityCount) {
					throw new OcdAlgorithmException("Error: Cid[i][j] > numCOM in finalRefinement() " + nodeCommunities.get(i).get(j) + " " + communityCount);
				}
				communities.get(nodeCommunities.get(i).get(j)).put(communityNodeSizes.get(nodeCommunities.get(i).get(j)), i);
				communityNodeSizes.put(nodeCommunities.get(i).get(j), communityNodeSizes.get(nodeCommunities.get(i).get(j))+1);
			}
		}
	}

}
