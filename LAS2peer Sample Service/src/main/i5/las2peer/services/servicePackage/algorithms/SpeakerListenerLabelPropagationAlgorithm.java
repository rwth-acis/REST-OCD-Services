package i5.las2peer.services.servicePackage.algorithms;


import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationHelpers.ListenerRuleCommand;
import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationHelpers.SpeakerRuleCommand;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.sparse.CompressedVector;

import y.base.Node;
import y.base.NodeCursor;

/**
 * Implements the Speaker Listener Label Propagation Algorithm.
 */
public class SpeakerListenerLabelPropagationAlgorithm implements
		OverlappingCommunityDetectionAlgorithm {

	/*
	 * Declaration of the graph types on which the algorithm can run.
	 */
	private static final HashSet<GraphType> compatibilities = new HashSet<GraphType>();
	static {
		compatibilities.add(GraphType.WEIGHTED);
		compatibilities.add(GraphType.DIRECTED);
	}

	private int memorySize;
	private double probabilityThreshold;
	private SpeakerRuleCommand speakerRule;
	private ListenerRuleCommand listenerRule;
	
	/**
	 * Creates an instance of the algorithm.
	 * @param memorySize Defines the size of the node memories
	 * and the number of iterations. The standard value is 100.
	 * @param probabilityThreshold Labels received by a node with a relative
	 * occurrence lower than this threshold will be ignored and not
	 * have any influence on that nodes community memberships. Has to be in the range
	 * [0,1]. The standard range is [0.02,0.1]
	 * @param speakerRule The rule according to which a speaker decides which label to send.
	 * The standard is the UniformSpeakerRule.
	 * @param listenerRule The listener rule according to which a listener decides which
	 * label to accept. The standard is the PopularityListenerRule.
	 */
	/*
	 * Protected constructor to prevent instantiation by anything but the AlgorithmFactory.
	 */
	protected SpeakerListenerLabelPropagationAlgorithm(int memorySize, double probabilityThreshold,
			SpeakerRuleCommand speakerRule, ListenerRuleCommand listenerRule) {
		this.memorySize = memorySize;
		this.speakerRule = speakerRule;
		this.listenerRule = listenerRule;
		this.probabilityThreshold = probabilityThreshold;
	}
	
	@Override
	public Set<GraphType> getCompatibleGraphTypes() {
		return compatibilities;
	}
	
	@Override
	public Cover detectOverlappingCommunities(
			CustomGraph graph) {
		/*
		 * Initializes node memories and node order
		 */
		List<List<Integer>> memories = new ArrayList<List<Integer>>();
		List<Node> nodeOrder = new ArrayList<Node>();
		initializeCommunityDetection(graph, memories, nodeOrder);
		/*
		 * Selects each node as a listener and updates its memory until
		 * the node memories are full.
		 */
		for(int t=0; t+1<memorySize; t++) {
			Collections.shuffle(nodeOrder);
			Node listener;
			for(int i=0; i<graph.nodeCount(); i++) {
				listener = nodeOrder.get(i);
				List<Integer> memory = memories.get(listener.index());
				memory.add(getNextLabel(graph, memories, listener));
			}
		}
		/*
		 * Returns the cover based on the node memories.
		 */
		Cover cover = calculateMembershipDegrees(graph, memories);
		cover.doNormalize();
		return cover;
	}
	
	protected void initializeCommunityDetection(CustomGraph graph, List<List<Integer>> memories, List<Node> nodeOrder) {
		for(int i=0; i<graph.nodeCount(); i++) {
			List<Integer> memory = new ArrayList<Integer>();
			memory.add(i);
			memories.add(memory);
			nodeOrder.add(graph.getNodeArray()[i]);
		}
	}
	
	/*
	 * Returns the next label to be received by the listener according to the speaker
	 * and the listener rule.
	 */
	protected int getNextLabel(CustomGraph graph, List<List<Integer>> memories, Node listener) {
		Map<Node, Integer> receivedLabels = new HashMap<Node, Integer>();
		NodeCursor speakers = listener.successors();
		while(speakers.ok()) {
			Node speaker = speakers.node();
			receivedLabels.put(speaker, speakerRule.getLabel(graph, speaker, memories.get(speaker.index())));
			speakers.next();
		}
		return listenerRule.getLabel(graph, listener, receivedLabels);
	}
	
	/*
	 * Calculates a cover with the membership degrees for all nodes based on the node memories.
	 */
	protected Cover calculateMembershipDegrees(CustomGraph graph, List<List<Integer>> memories) {
		Matrix membershipMatrix = new Basic2DMatrix();
		List<Integer> communities = new ArrayList<Integer>();
		/*
		 * Creates a label histogram for each node based on its memory
		 * and adapts the membership matrix accordingly.
		 */
		for(int i=0; i<memories.size(); i++) {
			List<Integer> memory = memories.get(i);
			int labelCount = memorySize;
			Map<Integer, Integer> histogram = getNodeHistogram(memory, labelCount);
			Vector nodeMembershipDegrees = calculateMembershipsFromHistogram(histogram, communities, labelCount);
		    if(nodeMembershipDegrees.length() > membershipMatrix.columns()) {
				/*
				 * Adapts matrix size for new communities.
				 */
		    	membershipMatrix = membershipMatrix.resize(graph.nodeCount(), nodeMembershipDegrees.length());
		    }
		    membershipMatrix.setRow(i, nodeMembershipDegrees);
		}
		return new Cover(graph, membershipMatrix);
	}
	
	/*
	 * Creates a histogram of the occurrence frequency based on the labels in the node memory.
	 * Manipulates labelCount to track the total number of labels represented in the histogram.
	 */
	protected Map<Integer, Integer> getNodeHistogram(List<Integer> memory, int labelCount) {
		Map<Integer, Integer> histogram = new HashMap<Integer, Integer>();
		Integer maxCount = 0;
		/*
		 * Creates the histogram.
		 */
		for (int label : memory) {
			if(histogram.containsKey(label)) {
				Integer count = histogram.get(label);
				histogram.put(label, ++count);
				if(count > maxCount) {
					maxCount = count;
				}
			}
			else {
				histogram.put(label, 1);
			}
		}
		/*
		 * Removes labels whose occurrence frequency is below the probability threshold.
		 */
	    for(Iterator<Map.Entry<Integer, Integer>> it = histogram.entrySet().iterator(); it.hasNext(); ) {
	        Map.Entry<Integer, Integer> entry = it.next();
	        int count = entry.getValue();
	        if((double)count / (double)memorySize < probabilityThreshold && count < maxCount) {
	        	it.remove();
	        	labelCount -= count;
	        }
	    }
	    return histogram;
	}
	
	/*
	 * Returns a vector of the membership degrees of a single node, calculated from its histogram.
	 * Manipulates the communities list to identify communities.
	 */
	protected Vector calculateMembershipsFromHistogram(Map<Integer, Integer> histogram, List<Integer> communities, int labelCount) {
		Vector membershipDegrees = new CompressedVector(communities.size());
	    for(Integer label : histogram.keySet()) {
	    	int count = histogram.get(label);
	    	if(!communities.contains(label)){
	    		/*
	    		 * Adapts vector size for new communities.
	    		 */
	    		communities.add(label);
	    		membershipDegrees = membershipDegrees.resize(communities.size());
	    	}
	    	membershipDegrees.set(communities.indexOf(label), (double)count / (double)labelCount);
	    }
	    return membershipDegrees;
	}
	
}
