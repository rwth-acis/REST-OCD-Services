/* MIT License
Copyright (c) 2018 Neil Justice
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. */

package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.LouvainGraph;
import i5.las2peer.services.ocd.algorithms.utils.LouvainGraphBuilder;
import i5.las2peer.services.ocd.algorithms.utils.LouvainArrayUtils;
import i5.las2peer.services.ocd.algorithms.utils.LouvainGraphLayerMapper;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.util.HashMap;
import java.util.HashSet;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Implementation of the Louvain method of non-overlapping community detection. This implementation is based on the definition by Vincent D. Blondel, Jean-Loup Guillaume, Renaud Lambiotte, Etienne Lefebvre:
 * Fast unfolding of communities in large networks
 * https://doi.org/10.1088/1742-5468/2008/10/p10008
 * Largely corresponds to the one at https://github.com/neil-justice/louvain.
 * Handles undirected graphs(makes them undirected if not)
 */
public class LouvainAlgorithm implements OcdAlgorithm {
	
  /**
   * The number of community layers the algorithm is allowed to produce. Each node is a community from the previous layers. 
   */
  private int maxLayers = 20;
  
  /*
   * PARAMETER NAMES
   */
  
  public static final String MAX_LAYERS_NAME = "maxLayers";
	
  private final List<LouvainGraph> graphs = new ArrayList<>();
  private final Maximiser m = new Maximiser();
  private final Random rnd;
  private final LouvainGraphLayerMapper mapper = new LouvainGraphLayerMapper();
  private int totalMoves = 0;
  private int layer = 0; // current community layer
  private List<int[]> communities;

  public LouvainAlgorithm() {
    rnd = new Random();
  }

  public LouvainAlgorithm(long seed) {
    this();
    rnd.setSeed(seed);
  }
  
  @Override
  public Set<GraphType> compatibleGraphTypes() {
	  Set<GraphType> compatibilities = new HashSet<GraphType>();
	  compatibilities.add(GraphType.WEIGHTED);
      compatibilities.add(GraphType.DIRECTED);
	  return compatibilities;
  }
  
  @Override
  public Cover detectOverlappingCommunities(CustomGraph graph) 
		  throws OcdAlgorithmException, InterruptedException{
	  LouvainGraph lGraph = new LouvainGraphBuilder().fromGraph(graph);
	  graphs.add(lGraph);
	  
	  int[] communities = cluster(maxLayers);
	  
	  Matrix memberships = getMembershipMatrix(graph, communities);
	  
	  return new Cover(graph, memberships);
  }
  
  @Override
  public Map<String, String> getParameters() {
	  Map<String, String> parameters = new HashMap<String, String>();
	  parameters.put(MAX_LAYERS_NAME, Integer.toString(maxLayers));
	  return parameters;
  }

  @Override
  public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
	  if(parameters.containsKey(MAX_LAYERS_NAME)) {
		  maxLayers = Integer.parseInt(parameters.get(MAX_LAYERS_NAME));
		  if(maxLayers <= 0) {
			  throw new IllegalArgumentException();
		  }
		  parameters.remove(MAX_LAYERS_NAME);
	  }
	  if(parameters.size() > 0) {
		  throw new IllegalArgumentException();
	  }
  }
  
  /**
   * Builds a membership matrix from a given graph and community belonging per node
   * @param graph The graph the community detection was done on
   * @param communitiesPerNode An int array of which community belongs to which node, the node indexes correspond to the array indexes and the values to the community number
   * @return The membership matrix
   * @throws InterruptedException if the thread was interrupted
   */
  public Matrix getMembershipMatrix(CustomGraph graph, int[] communitiesPerNode)
		  throws InterruptedException {
	  Matrix membershipMatrix = new Basic2DMatrix(graph.getNodeCount(), communitiesPerNode.length);
	  membershipMatrix = membershipMatrix.blank();


      //TODO: is this correct behavior?
      //// this code block is meant to deal with the occasional crashes caused by the next loop
      int index = communitiesPerNode.length;
      if(communitiesPerNode.length > graph.getNodeCount()){
          index = graph.getNodeCount();
      }
      ////////

      for(int i=0; i<index; i++) {
		  if(Thread.interrupted()) {
				throw new InterruptedException();
		  }
		  
		  membershipMatrix.set(i, communitiesPerNode[i], 1);
	  }
	  
	  ArrayList<Vector> filledColumns = new ArrayList<Vector>();
	  int communityCount = 0;
	  for(int j=0; j<membershipMatrix.columns(); j++) {
		  if(Thread.interrupted()) {
				throw new InterruptedException();
		  }
		  
		  if(membershipMatrix.getColumn(j).sum() != 0.0) {
			  filledColumns.add(membershipMatrix.getColumn(j));
			  communityCount++;
		  }
	  }
	  membershipMatrix = new Basic2DMatrix(graph.getNodeCount(), communityCount);
	  for(int j=0; j<filledColumns.size(); j++) {
		  if(Thread.interrupted()) {
				throw new InterruptedException();
		  }
		  
		  membershipMatrix.setColumn(j, filledColumns.get(j));
	  }
	  
	  return membershipMatrix;
  }
  
  @Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.LOUVAIN_ALGORITHM;
	}

  /**
   * Computes an array of community belonging for each node, default if maxLayers is undefined
   * @return An int array of community belonging for each node, the node indexes correspond to the array indexes and the values to the community number
   * @throws OcdAlgorithmException if the execution failed
   * @throws InterruptedException if the thread was interrupted
   */
  public int[] cluster() 
		  throws OcdAlgorithmException, InterruptedException {
    return cluster(Integer.MAX_VALUE);
  }

  /**
   * Computes an array of community belonging for each node and produces at maximum as many community layers as it is allowed
   * @param maxLayers Maximum number of community layers allowed
   * @return An int array of community belonging for each node, the node indexes correspond to the array indexes and the values to the community number
   * @throws OcdAlgorithmException if the execution failed
   * @throws InterruptedException if the thread was interrupted
   */
  public int[] cluster(int maxLayers) 
		  throws OcdAlgorithmException, InterruptedException {
    if (maxLayers <= 0) {
      maxLayers = Integer.MAX_VALUE;
    }
    
    do {
      totalMoves = m.run(graphs.get(layer));
      if (totalMoves > 0 && maxLayers >= layer) {
        addNewLayer();
      }
    }
    while (totalMoves > 0 && maxLayers >= layer);
    
    communities = mapper.run();
    
    //OWN TO GET BEST COMMUNITY BY MODULARITY
    double modularityMax = 0;
    int bestCommunityIndex = 0;
    for(int l=0; l<graphs.size(); l++) {
    	if(Thread.interrupted()) {
			throw new InterruptedException();
    	}
    	
    	double mod = modularity(l);
        if (mod > modularityMax) {
      	  modularityMax = mod;
      	  bestCommunityIndex = l;
        }
    }
    //OWN TO GET BEST COMMUNITY BY MODULARITY
    
      //TODO: is this correct? this part of the code causes crash, hence it is uncommented till fix is found
    //Account for the case that the original graph has the best modularity
//    if(bestCommunityIndex <= 0 )
//    {
//    	int orgGraphCommunity[] = new int[graphs.get(0).size()];
//    	for(int i=0; i<graphs.get(0).size(); i++) {
//    		if(Thread.interrupted()) {
//				throw new InterruptedException();
//    		}
//
//    		orgGraphCommunity[i] = i;
//    	}
//    	return orgGraphCommunity;
//    }


      //TODO: is this correct? temporary fix to avoid out of bounds exception in the next line if bestCommunityIndex is 0
      if(bestCommunityIndex - 1 < 0){
          bestCommunityIndex = 1;
      }

    return communities.get(bestCommunityIndex-1);
  }

  /**
   * Get the modularity of the highest layer of the graph. If called before the clusterer has run, will throw
   * an {@link IndexOutOfBoundsException}.
   * @return the modularity
   */
  public double modularity() {
    return graphs.get(layer).partitioning().modularity();
  }

  /**
   * Gets the modularity of a specific layer of the graph. If called before the clusterer has run, will throw
   * an exception
   * @param l the index of the layer.
   * @return The modularity of a specific layer of the graph
   * @throws IndexOutOfBoundsException if the array index went out of bounds
   */
  public double modularity(int l) {
    if (l >= graphs.size()) {
      throw new ArrayIndexOutOfBoundsException("Graph has " + graphs.size() + " layers, asked for layer " + l);
    }
    return graphs.get(l).partitioning().modularity();
  }

  public List<int[]> getCommunities() {
    return communities;
  }

  /**
   * Get the number of layers created during the detection process. If called before the clusterer has run, will return 0.
   * @return The number of layers created during the detection process
   */
  public int getLayerCount() {
    return layer;
  }

  /**
   * Get the graphs created during the detection process. If called before the clusterer has run, will return an empty list.
   * @return An immutable view of the graphs list.
   */
  public List<LouvainGraph> getGraphs() {
    return Collections.unmodifiableList(graphs);
  }

  /**
   * Adds a new community layer
   * @throws OcdAlgorithmException
   */
  private void addNewLayer() 
		  throws OcdAlgorithmException, InterruptedException {
    final LouvainGraph last = graphs.get(layer);
    final HashMap<Integer,Integer> map = mapper.createLayerMap(last);
    layer++;
    final LouvainGraph coarse = new LouvainGraphBuilder().coarseGrain(last, map);
    graphs.add(coarse);
  }


  class Maximiser {
    private static final double PRECISION = 0.000001;

    private LouvainGraph g;
    private int[] shuffledNodes;

    /**
     * Runs the louvain maximiser for community modularity.
     * @param g A graph
     * @return The number of total moves taken
     * @throws OcdAlgorithmException
     */
    private int run(LouvainGraph g) 
    		throws OcdAlgorithmException, InterruptedException {
      this.g = g;
      shuffledNodes = new int[g.order()];
      LouvainArrayUtils.fillRandomly(shuffledNodes);
      totalMoves = 0;

      final long s1 = System.nanoTime();
      reassignCommunities();
      final long e1 = System.nanoTime();
      final double time = (e1 - s1) / 1000000000d;

      return totalMoves;
    }

    /**
     * Reassigns communities through maximising local modularity until modularity values don't change anymore
     * @throws OcdAlgorithmException
     */
    private void reassignCommunities() 
    		throws OcdAlgorithmException, InterruptedException {
      double mod = g.partitioning().modularity();
      double oldMod;
      int moves;
      boolean hasChanged;

      do {
    	if(Thread.interrupted()) {
    	  throw new InterruptedException();
		}
    	  
        hasChanged = true;
        oldMod = mod;
        moves = maximiseLocalModularity();
        totalMoves += moves;
        mod = g.partitioning().modularity();
        if (mod - oldMod <= PRECISION) {
          hasChanged = false;
        }
        if (moves == 0) {
          hasChanged = false;
        }
      } while (hasChanged);
    }

    /**
     * Maximises the local modularity of a set of nodes
     * @return The number of moves needed
     * @throws OcdAlgorithmException
     */
    private int maximiseLocalModularity() 
    		throws OcdAlgorithmException, InterruptedException {
      int moves = 0;
      for (int i = 0; i < g.order(); i++) {
    	if(Thread.interrupted()) {
		  throw new InterruptedException();
		}
    	  
        final int node = shuffledNodes[i];
        if (makeBestMove(node)) {
          moves++;
        }
      }
      return moves;
    }

    /**
     * Make best move for maximising local modularity
     * @param node A node
     * @return true if it was possible, false if there was no better move
     * @throws OcdAlgorithmException
     */
    private boolean makeBestMove(int node)
    		throws OcdAlgorithmException, InterruptedException {
      double max = 0d;
      int best = -1;

      for (int i = 0; i < g.neighbours(node).size(); i++) {
    	if(Thread.interrupted()) {
		  throw new InterruptedException();
		}  
    	  
        final int community = g.partitioning().community(g.neighbours(node).get(i));
        final double inc = deltaModularity(node, community);
        if (inc > max) {
          max = inc;
          best = community;
        }
      }

      if (best >= 0 && best != g.partitioning().community(node)) {
        g.partitioning().moveToComm(node, best);
        return true;
      } else {
        return false;
      }
    }

    /**
     * Computes the modularity delta for a node and a community. Modularity will change if node is moved to community
     * @param node A node index
     * @param community A community index
     * @return The modularity delta for a node and a community
     */
    private double deltaModularity(int node, int community) 
    		throws InterruptedException {
      final double dnodecomm = g.partitioning().dnodecomm(node, community);
      final double ctot = g.partitioning().totDegree(community);
      final double wdeg = g.degree(node);
      return dnodecomm - ((ctot * wdeg) / g.m2());
    }
  }
}