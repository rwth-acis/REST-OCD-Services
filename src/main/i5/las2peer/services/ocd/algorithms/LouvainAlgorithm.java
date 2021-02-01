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
 * Implementation of the Louvain method of community detection.
 */
public class LouvainAlgorithm implements OcdAlgorithm {
	
  private int maxLayers = 20;
  
  /*
   * PARAMETER NAMES
   */
  
  protected static final String MAX_LAYERS_NAME = "maxLayers";
	
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
  
  public Matrix getMembershipMatrix(CustomGraph graph, int[] communitiesPerNode) {
	  Matrix membershipMatrix = new Basic2DMatrix(graph.nodeCount(), communitiesPerNode.length);
	  membershipMatrix = membershipMatrix.blank();
	  
	  for(int i=0; i<communitiesPerNode.length; i++) {
		  membershipMatrix.set(i, communitiesPerNode[i], 1);
	  }
	  
	  ArrayList<Vector> filledColumns = new ArrayList<Vector>();
	  int communityCount = 0;
	  for(int j=0; j<membershipMatrix.columns(); j++) {
		  if(membershipMatrix.getColumn(j).sum() != 0.0) {
			  filledColumns.add(membershipMatrix.getColumn(j));
			  communityCount++;
		  }
	  }
	  membershipMatrix = new Basic2DMatrix(graph.nodeCount(), communityCount);
	  for(int j=0; j<filledColumns.size(); j++) {
		  membershipMatrix.setColumn(j, filledColumns.get(j));
	  }
	  
	  return membershipMatrix;
  }
  
  @Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.LOUVAIN_ALGORITHM;
	}

  public int[] cluster() 
		  throws OcdAlgorithmException {
    return cluster(Integer.MAX_VALUE);
  }

  // detect communities
  public int[] cluster(int maxLayers) 
		  throws OcdAlgorithmException {
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
    	double mod = modularity(l);
        if (mod > modularityMax) {
      	  modularityMax = mod;
      	  bestCommunityIndex = l;
        }
    }
    //OWN TO GET BEST COMMUNITY BY MODULARITY
    
    //Account for the case that the original graph has the best modularity
    if(bestCommunityIndex <= 0)
    {
    	int orgGraphCommunity[] = new int[graphs.get(0).size()];
    	for(int i=0; i<graphs.get(0).size(); i++) {
    		orgGraphCommunity[i] = i;
    	}
    	return orgGraphCommunity;
    }    
    return communities.get(bestCommunityIndex-1);
  }

  /**
   * Get the modularity of the highest layer of the graph. If called before the clusterer has run, will throw
   * an {@link IndexOutOfBoundsException}.
   */
  public double modularity() {
    return graphs.get(layer).partitioning().modularity();
  }

  /**
   * Get the modularity of a specific layer of the graph. If called before the clusterer has run, will throw
   * an {@link IndexOutOfBoundsException}.
   *
   * @param l the index of the layer.
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
   * Get the number of layers created during the detection process.
   *
   * If called before the clusterer has run, will return 0.
   */
  public int getLayerCount() {
    return layer;
  }

  /**
   * Get the graphs created during the detection process.
   *
   * If called before the clusterer has run, will return an empty list.
   *
   * @return an immutable view of the graphs list.
   */
  public List<LouvainGraph> getGraphs() {
    return Collections.unmodifiableList(graphs);
  }

  private void addNewLayer() 
		  throws OcdAlgorithmException {
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

    private int run(LouvainGraph g) 
    		throws OcdAlgorithmException {
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

    private void reassignCommunities() 
    		throws OcdAlgorithmException {
      double mod = g.partitioning().modularity();
      double oldMod;
      int moves;
      boolean hasChanged;

      do {
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

    private int maximiseLocalModularity() 
    		throws OcdAlgorithmException {
      int moves = 0;
      for (int i = 0; i < g.order(); i++) {
        final int node = shuffledNodes[i];
        if (makeBestMove(node)) {
          moves++;
        }
      }
      return moves;
    }

    private boolean makeBestMove(int node)
    		throws OcdAlgorithmException {
      double max = 0d;
      int best = -1;

      for (int i = 0; i < g.neighbours(node).size(); i++) {
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

    // change in modularity if node is moved to community
    private double deltaModularity(int node, int community) {
      final double dnodecomm = g.partitioning().dnodecomm(node, community);
      final double ctot = g.partitioning().totDegree(community);
      final double wdeg = g.degree(node);
      return dnodecomm - ((ctot * wdeg) / g.m2());
    }
  }
}