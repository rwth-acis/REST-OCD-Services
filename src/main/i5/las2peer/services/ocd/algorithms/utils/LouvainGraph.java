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
import i5.las2peer.services.ocd.algorithms.utils.LouvainSparseIntMatrix;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * An undirected, weighted, unmodifiable graph data structure. (though nodes
 * can be moved between communities, and this functionality is rolled into this
 * object. )
 */
public class LouvainGraph {
  private final LouvainSparseIntMatrix matrix;   // adjacency matrix with weight info
  private final ArrayList<ArrayList<Integer>> adjList;  // adjacency list
  private final int layer; // if > 0, its a coarse-grained community graph

  private final int[] degrees;            // degree of each node
  private final int order;                // no. of nodes
  private final int size;                 // sum of edge weights
  private final double m2;                // sum of edge weights * 2
  private final Partitioning partitioning;

  public LouvainGraph(LouvainGraphBuilder builder) {
	matrix = builder.matrix();
    adjList = builder.adjList();
    degrees = builder.degrees();
    order = builder.order();
    size = builder.sizeDbl() / 2;
    m2 = builder.sizeDbl();
    layer = builder.layer();

    partitioning = new Partitioning();
  }

  /**
   * loads a partition set.
   */
  public void loadPartitioning(int[] partitioning) 
		  throws OcdAlgorithmException {
    if (partitioning.length != order()) {
      throw new OcdAlgorithmException("new partitioning size-graph size mismatch: " +
          order() + " != " + partitioning.length);
    }
    for (int node = 0; node < order(); node++) {
      partitioning().moveToComm(node, partitioning[node]);
    }
  }

  public double m2() {
    return m2;
  }

  public int size() {
    return size;
  }

  public int layer() {
    return layer;
  }

  public int order() {
    return order;
  }

  public int degree(int node) {
    rangeCheck(node);
    return degrees[node];
  }

  public int weight(int n1, int n2) {
    rangeCheck(n1);
    rangeCheck(n2);
    return matrix.get(n1, n2);
  }

  public ArrayList<Integer> neighbours(int node) {
    rangeCheck(node);
    return adjList.get(node);
  }

  public Partitioning partitioning() {
    return partitioning;
  }

  private void rangeCheck(int index) {
    if (index >= order) {
      throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
  }

  private String outOfBoundsMsg(int index) {
    return "Index: " + index + ", Graph order: " + order;
  }

  public class Partitioning {
    private final LouvainSparseIntMatrix cMatrix;  // weights between communities
    private final int[] communities;        // comm of each node
    private final int[] totDegrees;         // total degree of community
    private final int[] intDegrees;         // int. degree of community
    private int numComms;                   // total no. of communities

    public Partitioning() {
      cMatrix = new LouvainSparseIntMatrix(matrix);
      communities = new int[order];
      totDegrees = new int[order];
      intDegrees = new int[order];
      numComms = order;

      for (int i = 0; i < order; i++) {
        communities[i] = i;
        totDegrees[i] = degree(i);
        intDegrees[i] = (int)matrix.get(i, i); // catches self-edges
      }
    }

    public void moveToComm(int node, int newComm) 
    		throws OcdAlgorithmException {
      rangeCheck(node);
      rangeCheck(newComm);

      final int oldComm = community(node);
      final int oldTotDegree = totDegree(oldComm);
      final int oldNewTotDegree = totDegree(newComm);
      if (oldComm == newComm) {
        return;
      }

      communities[node] = newComm;
      totDegrees[oldComm] -= degree(node);
      totDegrees[newComm] += degree(node);
      final ArrayList<Integer> neighbours = neighbours(node);
      for (int i = 0; i < neighbours.size(); i++) {
        final int neighbour = neighbours.get(i);
        final int weight = weight(node, neighbour);
        if (neighbour != node) {
        	cMatrix.add(newComm, community(neighbour), weight);
        	cMatrix.add(community(neighbour), newComm, weight);
        	cMatrix.add(oldComm, community(neighbour), -weight);
        	cMatrix.add(community(neighbour), oldComm, -weight);
          if (community(neighbour) == newComm) {
            intDegrees[newComm] += weight * 2;
          }
          if (community(neighbour) == oldComm) {
            intDegrees[oldComm] -= weight * 2;
          }
        }
      }
      final int selfWeight = weight(node, node);
      cMatrix.add(newComm, newComm, selfWeight);
      cMatrix.add(oldComm, oldComm, -selfWeight);
      intDegrees[oldComm] -= selfWeight;
      intDegrees[newComm] += selfWeight;

      if (totDegree(oldComm) == 0 && oldTotDegree > 0) {
        numComms--;
      }
      if (totDegree(newComm) > 0 && oldNewTotDegree == 0) {
        numComms++;
      }
      if (totDegree(oldComm) < 0) {
        throw new OcdAlgorithmException("-ve total degree");
      }
    }

    // weight between a community and a node
    public int dnodecomm(int node, int comm) {
      rangeCheck(node);
      rangeCheck(comm);

      int dnodecomm = 0;
      final ArrayList<Integer> neighbours = neighbours(node);
      for (int i = 0; i < neighbours.size(); i++) {
        final int neigh = neighbours.get(i);
        if (community(neigh) == comm && node != neigh) {
          dnodecomm += weight(node, neigh);
        }
      }
      return dnodecomm;
    }

    public double modularity() {
      double q = 0d;

      for (int comm = 0; comm < order; comm++) {
        final double ctot = totDegree(comm);
        final double cint = intDegree(comm);
        q += (cint / m2) - (ctot / m2) * (ctot / m2);
      }
      return q;
    }

    // returns the contribution that this comm makes to the total modularity
    public double modularityContribution(int comm) {
      rangeCheck(comm);
      final double ctot = totDegree(comm);
      final double cint = intDegree(comm);
      return (cint / m2) - (ctot / m2) * (ctot / m2);
    }

    public int[] communities() {
      return communities;
    }

    public int numComms() {
      return numComms;
    }

    public int community(int node) {
      rangeCheck(node);
      return communities[node];
    }

    public int totDegree(int comm) {
      rangeCheck(comm);
      return totDegrees[comm];
    }

    public int intDegree(int comm) {
      rangeCheck(comm);
      return intDegrees[comm];
    }
    
    public LouvainSparseIntMatrix cMatrix() {
    	return cMatrix;
    }

    public int communityWeight(int c1, int c2) {
      rangeCheck(c1);
      rangeCheck(c2);

      return cMatrix.get(c1, c2);
    }

    public Iterator<HashMap.Entry<Long,Integer>> commWeightIterator() {
      return cMatrix.iterator();
    }
  }
}