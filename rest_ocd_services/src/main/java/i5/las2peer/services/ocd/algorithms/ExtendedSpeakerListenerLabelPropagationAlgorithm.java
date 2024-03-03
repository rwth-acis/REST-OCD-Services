package i5.las2peer.services.ocd.algorithms;


import i5.las2peer.services.ocd.algorithms.utils.SlpaListenerRuleCommand;
import i5.las2peer.services.ocd.algorithms.utils.SlpaPopularityListenerRule;
import i5.las2peer.services.ocd.algorithms.utils.SlpaSpeakerRuleCommand;
import i5.las2peer.services.ocd.algorithms.utils.SlpaUniformSpeakerRule;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

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

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

/**
 * Implements a custom extended version of the original Speaker Listener Label Propagation Algorithm by J. Xie, B. K. Szymanski, and X. Liu:
 * Slpa: Uncovering overlapping communities in social networks via a speaker-listener interaction dynamic process
 * https://doi.org/10.1109/ICDMW.2011.154
 *
 * Using the Uniform Speaker Rule and the Popularity Listener Rule:
 * Handles directed and unweighted graphs. For unweighted and undirected graphs,
 * it behaves the same as the original algorithm.
 */
public class ExtendedSpeakerListenerLabelPropagationAlgorithm implements
		OcdAlgorithm {
	
	/**
	 * The size of the node memories and the number of iterations.
	 * The default value is 100. Must be greater than 0.
	 */
	private int memorySize = 100;
	/**
	 * The lower bound for the relative label occurrence.
	 * Labels received by a node with a relative occurrence lower than this threshold will be ignored
	 * and do not have any influence on that nodes community memberships.
	 * The default value is 0.15. Must be at least 0 and at most 1.
	 * Recommended are values between 0.02 and 0.1.
	 */
	private double probabilityThreshold = 0.15;
	/**
	 * The speaker rule according to which a speaker decides which label to send.
	 * Currently only the UniformSpeakerRule is implemented.
	 */
	private SlpaSpeakerRuleCommand speakerRule = new SlpaUniformSpeakerRule();
	/**
	 * The listener rule according to which a listener decides which label to accept.
	 * Currently only the popularity listener rule is implemented.
	 */
	private SlpaListenerRuleCommand listenerRule = new SlpaPopularityListenerRule();
	
	
	/*
	 * PARAMETER NAMES
	 */
	
	public static final String PROBABILITY_THRESHOLD_NAME = "probabilityThreshold";

	public static final String MEMORY_SIZE_NAME = "memorySize";
	
	/**
	 * Creates a standard instance of the algorithm.
	 * All attributes are assigned their default values.
	 */
	public ExtendedSpeakerListenerLabelPropagationAlgorithm() {
	}
	
	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.EXTENDED_SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM;
	}
	
	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(MEMORY_SIZE_NAME, Integer.toString(memorySize));
		parameters.put(PROBABILITY_THRESHOLD_NAME, Double.toString(probabilityThreshold));
		return parameters;
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.containsKey(MEMORY_SIZE_NAME)) {
			memorySize = Integer.parseInt(parameters.get(MEMORY_SIZE_NAME));
			if(memorySize <= 0) {
				throw new IllegalArgumentException();
			}
			parameters.remove(MEMORY_SIZE_NAME);
		}
		if(parameters.containsKey(PROBABILITY_THRESHOLD_NAME)) {
			probabilityThreshold = Double.parseDouble(parameters.get(PROBABILITY_THRESHOLD_NAME));
			if(probabilityThreshold < 0 || probabilityThreshold > 1) {
				throw new IllegalArgumentException();
			}
			parameters.remove(PROBABILITY_THRESHOLD_NAME);
		}
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.WEIGHTED);
		compatibilities.add(GraphType.DIRECTED);
		return compatibilities;
	}
	
	@Override
	public Cover detectOverlappingCommunities(
			CustomGraph graph) throws InterruptedException {
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
		Node listener;
		List<Integer> memory;
		for(int t=0; t+1<memorySize; t++) {
			Collections.shuffle(nodeOrder);
			for(int i=0; i<graph.getNodeCount(); i++) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				listener = nodeOrder.get(i);
				memory = memories.get(listener.getIndex());
				memory.add(getNextLabel(graph, memories, listener));
			}
		}
		/*
		 * Returns the cover based on the node memories.
		 */
		return calculateMembershipDegrees(graph, memories);
	}
	
	protected void initializeCommunityDetection(CustomGraph graph, List<List<Integer>> memories, List<Node> nodeOrder) throws InterruptedException {
		List<Integer> memory;
		Node[] nodeArray = graph.nodes().toArray(Node[]::new);
		for(int i=0; i<graph.getNodeCount(); i++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			memory = new ArrayList<Integer>();
			memory.add(i);
			memories.add(memory);
			nodeOrder.add(nodeArray[i]);
		}
	}
	
	/**
	 * Returns the next label to be received by the listener according to the speaker
	 * and the listener rule.
	 * @param graph the examined graph
	 * @param listener the listener
	 * @param memories the memories of the speaker
	 * @return the next label
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	protected int getNextLabel(CustomGraph graph, List<List<Integer>> memories, Node listener) throws InterruptedException {
		Map<Node, Integer> receivedLabels = new HashMap<Node, Integer>();
		Iterator<Node> speakersIt = graph.getSuccessorNeighbours(listener).iterator();
		Node speaker;
		while(speakersIt.hasNext()) {
			speaker = speakersIt.next();
			receivedLabels.put(speaker, speakerRule.getLabel(graph, speaker, memories.get(speaker.getIndex())));
		}
		return listenerRule.getLabel(graph, listener, receivedLabels);
	}
	
	/**
	 * Calculates a cover with the membership degrees for all nodes based on the node memories.
	 * @param graph the examined graph
	 * @param memories the memories of the node
	 * @throws InterruptedException if the thread was interrupted
	 * @return the cover
	 */
	protected Cover calculateMembershipDegrees(CustomGraph graph, List<List<Integer>> memories) throws InterruptedException {
		Matrix membershipMatrix = new Basic2DMatrix();
		List<Integer> communities = new ArrayList<Integer>();
		/*
		 * Creates a label histogram for each node based on its memory
		 * and adapts the membership matrix accordingly.
		 */
		List<Integer> memory;
		int labelCount;
		Map<Integer, Integer> histogram;
		Vector nodeMembershipDegrees;
		for(int i=0; i<memories.size(); i++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			memory = memories.get(i);
			labelCount = memorySize;
			histogram = getNodeHistogram(memory, labelCount);
			nodeMembershipDegrees = calculateMembershipsFromHistogram(histogram, communities, labelCount);
		    if(nodeMembershipDegrees.length() > membershipMatrix.columns()) {
				/*
				 * Adapts matrix size for new communities.
				 */
		    	membershipMatrix = membershipMatrix.resize(graph.getNodeCount(), nodeMembershipDegrees.length());
		    }
		    membershipMatrix.setRow(i, nodeMembershipDegrees);
		}
		return new Cover(graph, membershipMatrix);
	}
	
	/**
	 * Creates a histogram of the occurrence frequency based on the labels in the node memory.
	 * Manipulates labelCount to track the total number of labels represented in the histogram.
	 * @param memory the memory of the node
	 * @param labelCount the number of labels for the node
	 * @return the histogram
	 */
	protected Map<Integer, Integer> getNodeHistogram(List<Integer> memory, int labelCount) {
		Map<Integer, Integer> histogram = new HashMap<Integer, Integer>();
		Integer maxCount = 0;
		/*
		 * Creates the histogram.
		 */
		int count;
		for (int label : memory) {
			if(histogram.containsKey(label)) {
				count = histogram.get(label).intValue();
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
		Map.Entry<Integer, Integer> entry;
	    for(Iterator<Map.Entry<Integer, Integer>> it = histogram.entrySet().iterator(); it.hasNext(); ) {
	        entry = it.next();
	        count = entry.getValue();
	        if((double)count / (double)memorySize < probabilityThreshold && count < maxCount) {
	        	it.remove();
	        	labelCount -= count;
	        }
	    }
	    return histogram;
	}
	
	/**
	 * Returns a vector of the membership degrees of a single node, calculated from its histogram.
	 * Manipulates the communities list to identify communities.
	 * @param histogram the histogram
	 * @param communities the communities
	 * @param labelCount the number of labels
	 * @return the membership degree vector
	 */
	protected Vector calculateMembershipsFromHistogram(Map<Integer, Integer> histogram, List<Integer> communities, int labelCount) {
		Vector membershipDegrees = new CompressedVector(communities.size());
		int count;
	    for(Integer label : histogram.keySet()) {
	    	count = histogram.get(label);
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
