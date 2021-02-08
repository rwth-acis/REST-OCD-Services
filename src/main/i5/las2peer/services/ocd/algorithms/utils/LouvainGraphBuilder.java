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

package i5.las2peer.services.ocd.algorithms.utils;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.LouvainGraph;
import i5.las2peer.services.ocd.algorithms.utils.LouvainSparseIntMatrix;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import java.io.IOException;
import java.util.Random;
import java.util.Map.Entry;

/**
 * This class builds Louvain graphs from the graphs used in the ocd service
 */
public class LouvainGraphBuilder {

  private LouvainSparseIntMatrix matrix;
  private ArrayList<ArrayList<Integer>> adjList;
  private int[] degrees;
  private int order = 0;
  private int sizeDbl = 0;
  private int layer = 0;

  /**
   * Builds Louvain graphs from the graph used in the ocd service
   * @param graph A graph
   * @return A corresponding louvain graph
   * @throws InterruptedException
   * @throws OcdAlgorithmException
   */
  public LouvainGraph fromGraph(CustomGraph graph) 
		  throws InterruptedException, OcdAlgorithmException{
	order = graph.nodeCount();
	initialise();
	
	for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {		
		if(Thread.interrupted()) {
			throw new InterruptedException();
		}
		
		Edge edge = ec.edge();
		Node source = edge.source();
		Node target = edge.target();
		
		if (matrix.get(source.index(), target.index()) == 0) {
			insertEdgeSym(source.index(), target.index(), (int) graph.getEdgeWeight(edge));
		}
	}
	
	if (!matrix.isSymmetric()) {
	      throw new OcdAlgorithmException("constructed asymmetric matrix");
	}
	
	return build();
  }
  
  /**
   * Initialises the builder
   */
  private void initialise() 
		  throws InterruptedException {
    matrix = new LouvainSparseIntMatrix(order);
    degrees = new int[order];
    adjList = new ArrayList<ArrayList<Integer>>(order);
    for (int i = 0; i < order; i++) {
      if(Thread.interrupted()) {
			throw new InterruptedException();
	  }	
    	
      adjList.add(i,new ArrayList<Integer>());
    }
  }

  /**
   * Inserts a symmetrical edge
   * @param n1 A first node
   * @param n2 A second node
   * @param weight An edge weight
   */
  private void insertEdgeSym(int n1, int n2, int weight) {
    insertEdge(n1, n2, weight);
    if (n1 != n2) {
      insertEdge(n2, n1, weight);
    }
  }

  /**
   * Inserts an edge
   * @param n1 A first node
   * @param n2 A second node
   * @param weight An edge weight
   */
  private void insertEdge(int n1, int n2, int weight) {
    matrix.set(n1, n2, weight);
    adjList.get(n1).add(n2);
    degrees[n1] += weight;
    sizeDbl += weight;
  }

  public LouvainGraphBuilder setSize(int order) 
		  throws InterruptedException {
    this.order = order;
    initialise();

    return this;
  }

  /**
   * Adds an edge between two nodes
   * @param n1 A first node
   * @param n2 A second node
   * @param weight An edge weight
   * @return This LouvaingraphBuilder
   * @throws OcdAlgorithmException
   */
  public LouvainGraphBuilder addEdge(int n1, int n2, int weight) 
		  throws OcdAlgorithmException {
    if (n1 >= order) {
      throw new OcdAlgorithmException("" + n1 + " >= " + order);
    }
    if (n2 >= order) {
      throw new OcdAlgorithmException("" + n2 + " >= " + order);
    }
    if (matrix == null) {
      throw new OcdAlgorithmException("initialise first");
    }
    if (matrix.get(n1, n2) != 0) {
      throw new OcdAlgorithmException("already exists");
    }
    insertEdgeSym(n1, n2, weight);

    return this;
  }

  /**
   * Coarse-grains the weights between nodes of a graph
   * @param g A louvain graph
   * @param map A mapping
   * @return The processed louvain graph
   * @throws OcdAlgorithmException
   */
  public LouvainGraph coarseGrain(LouvainGraph g, HashMap<Integer,Integer> map) 
		  throws OcdAlgorithmException, InterruptedException {
    this.order = g.partitioning().numComms();
    this.layer = g.layer() + 1;
    initialise();
    int sum = 0;

    for (final Iterator<HashMap.Entry<Long,Integer>> it = g.partitioning().commWeightIterator(); it.hasNext(); ) {
      if(Thread.interrupted()) {
			throw new InterruptedException();
	  }
    	
      Entry<Long, Integer> entry = it.next();
      final int weight = entry.getValue();
      long cmMatrixSize = g.partitioning().cMatrix().size();
      if (weight != 0) {
        final int n1 = map.get(g.partitioning().cMatrix().sparseX(entry, cmMatrixSize));
        final int n2 = map.get(g.partitioning().cMatrix().sparseY(entry, cmMatrixSize));
        insertEdge(n1, n2, (int)weight);
        sum += weight;
      }
    }

    if (sum != g.size() * 2) {
      throw new OcdAlgorithmException("Louvain graph builder recieved wrong weights: " + sum + " " + (g.size() * 2));
    }
    if (sum != sizeDbl) {
      throw new OcdAlgorithmException("Louvain coarse-grain error: " + sum + " != " + sizeDbl);
    }
    return build();
  }

  /**
   * Builds a louvain graph from an existing louvain graph and a list of community members
   * @param g A louvain graph
   * @param members A list of community member indexes
   * @return
   */
  public LouvainGraph fromCommunity(LouvainGraph g, ArrayList<Integer> members) 
		  throws InterruptedException {
    this.order = members.size();
    initialise();

    for (int newNode = 0; newNode < order; newNode++) {
      if(Thread.interrupted()) {
			throw new InterruptedException();
	  }
    	
      final int oldNode = members.get(newNode);
      for (int i = 0; i < g.neighbours(oldNode).size(); i++) {
        final int oldNeigh = g.neighbours(oldNode).get(i);
        final int newNeigh;
        if ((newNeigh = members.indexOf(oldNeigh)) != -1) {
          insertEdge(newNode, newNeigh, (int)g.weight(oldNode, oldNeigh));
        }
      }
    }
    return build();
  }

  public LouvainSparseIntMatrix matrix() {
    return matrix;
  }

  public ArrayList<ArrayList<Integer>> adjList() {
    return adjList;
  }

  public int[] degrees() {
    return degrees;
  }

  public int sizeDbl() {
    return sizeDbl;
  }

  public int order() {
    return order;
  }

  public int layer() {
    return layer;
  }

  public LouvainGraph build() {
    return new LouvainGraph(this);
  }
}