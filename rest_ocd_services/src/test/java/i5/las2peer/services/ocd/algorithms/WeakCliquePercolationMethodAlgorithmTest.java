package i5.las2peer.services.ocd.algorithms;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.WeakClique;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import org.graphstream.graph.Node;

public class WeakCliquePercolationMethodAlgorithmTest {

	
	/*
	 * Run the algorithm on a simple graph with 3 communities 
	 */
	@Test
	public void testOnSimpleGraph() throws OcdAlgorithmException, AdapterException, FileNotFoundException, InterruptedException{
		
		// Creates new graph
		CustomGraph graph = new CustomGraph();
		
		// Creates nodes
		int size = 11;
		Node n[] = new Node[size];
		
		
		for (int i = 0; i < size; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			
		}
				
		// first community (nodes: 0, 1, 2, 3, 4)
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				if (i != j ) {
					graph.addEdge(UUID.randomUUID().toString(), n[i], n[j]);
				}
			}
		}
		
		// second community (nodes: 5, 6, 7, 8, 9)
		for(int i = 5; i < 10; i++) {
			for (int j = 5; j < 10; j++) {
				if(i!=j ) {
				graph.addEdge(UUID.randomUUID().toString(), n[i], n[j]);
				}
			}
		}
		
		/*
		 * Connect above two communities, which creates another small community of size 3 (nodes 0, 5, 10)
		 */
		graph.addEdge(UUID.randomUUID().toString(), n[5], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[10], n[5]);
		graph.addEdge(UUID.randomUUID().toString(), n[0], n[10]);
		graph.addEdge(UUID.randomUUID().toString(), n[10], n[0]);
		
		// instantiate the algorithm
		WeakCliquePercolationMethodAlgorithm wcpm = new WeakCliquePercolationMethodAlgorithm();
		
		try {
			wcpm.detectOverlappingCommunities(graph);
		} catch (OcdAlgorithmException | OcdMetricException | InterruptedException e) {
			e.printStackTrace();
		}
	
	}
	
	
	/*
	 * Test saltonIndex method, which is based on the description in the algorithm paper
	 */
	@Test
	public void saltonIndexTest() {
		
		// create a basic 3x3 matrix
		Matrix network = new Basic2DMatrix(3,3);
		
		/*
		 * Graph has 3 nodes,  2 of which have a single shared neighbor
		 */
		network.set(0, 2, 1);
		network.set(1, 2, 1);
		
		// instantiate the algorithm
		WeakCliquePercolationMethodAlgorithm wcpm = new WeakCliquePercolationMethodAlgorithm();
		
		// find salton index between nodes 0 and 1
		double salton_index = wcpm.saltonIndex(network, 0, 1);
		
		/*
		 * Since salton index is calclated between nodes 0 and 1 which share all of their neighbors (only one neighbor), salton index between themshould be 1
		 */
		assertEquals(1, salton_index, 0.01);
	}
	
	
	/*
	 * Test Merge method, which is based on the description in the algorithm paper
	 */
	@Test
	public void MergeTest() {
		
		// create a basic 3x3 matrix
		Matrix network = new Basic2DMatrix(3,3);
		
		for (int i = 0; i < network.rows(); i++) {
			for (int j = 0; j < network.columns(); j++) {
				if ( i != j) {
					network.set(i, j, 1);
				}
			}
		}
		// instantiate the algorithm
		WeakCliquePercolationMethodAlgorithm wcpm = new WeakCliquePercolationMethodAlgorithm();
		
		// instantiate variables needed for Merge method
		WeakClique wclique = new WeakClique();
					 
		wclique.add(0); // add node 0 to the community
		
		HashSet<WeakClique> all_wcliques = new HashSet<WeakClique>();
		
		// there are 3 weak cliques each with just 1 node
		for (int i = 0; i < 3; i ++) {
			WeakClique wclique_to_add = new WeakClique();
			wclique_to_add.add(i);
			

			if(i == 2) {
				
				// create weak clique with two nodes: 0 and 2
				wclique_to_add.add(0);
				
			}
			
			all_wcliques.add(wclique_to_add);
			
		}
		
		HashSet<WeakClique> S = new HashSet<WeakClique>();
		
		double threshold = 0.5;	
		
		
		/*
		 * Try to merge WeakClique with only node 0, with all its neighbouring WeakCliques, which are WeakCliques that have only node 1 and nodes 0, 2.
		 */
		wcpm.Merge(wclique, S, threshold, all_wcliques, network);
		
		// iterator over set of weakcliques resulting from merging
		Iterator<WeakClique> it = S.iterator();
		
		/*
		 * Since threshold is 0.5 and the only neighbor of wclique is weak clique with
		 * nodes 0 and 2 (because they share node 0) with a similarity of 1.0, wclique
		 * which consisting only of the node 0 and its neighbor which consists of nodes 0
		 * and 2 will be merged
		 */
		
		WeakClique the_only_weakclique_in_merged_cliques = it.next();
		assertEquals(2, the_only_weakclique_in_merged_cliques.size(), 0.01);
		
	}
	
	
	/*
	 * Test priorityOfNode method, which is based on the description in the algorithm paper
	 */
	@Test
	public void nodePriorityTest() {
		
		// create a basic 3x3 matrix
		Matrix network = new Basic2DMatrix(3,3);
		
		// add edges to the network, so that each node is connected to all other nodes
		for (int i = 0; i < network.rows(); i++) {
			for (int j = 0; j < network.columns(); j++) {
				if ( i != j) {
					network.set(i, j, 1);
				}
			}
		}		
		
		// instantiate the algorithm
		WeakCliquePercolationMethodAlgorithm wcpm = new WeakCliquePercolationMethodAlgorithm();
		
		
		// node 0 has two neighbors (k = 2), there is one edge between neighbors of node 0 (m_u = 1) as a result, priority should be (k + m_u)/(k+1) = 1
		double priority = wcpm.priorityOfNode(network, 0);
		assertEquals(1, priority, 0.01);
		
	}
	
	
}
