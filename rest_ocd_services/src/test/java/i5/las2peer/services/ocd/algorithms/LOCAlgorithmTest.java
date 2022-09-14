package i5.las2peer.services.ocd.algorithms;

import java.io.FileNotFoundException;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

import org.junit.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.Community;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import y.base.Node;

public class LOCAlgorithmTest {
	private static CustomGraph graph;
	private static Node n[];
	
	@Test
	public void testgraphs() throws OcdAlgorithmException, InterruptedException, FileNotFoundException, AdapterException, OcdMetricException {
		System.out.println("Test 1");
		graph = getGraph1();
		test4(graph);
		System.out.println("Test 2");
		graph = getGraph2();
		test4(graph);
		System.out.println("Test 3");
		graph = getGraph3();
		test4(graph);
		System.out.println("Test 4");
		graph = getGraph4();
		test4(graph);

	}
	
	private void test(CustomGraph graph) throws InterruptedException{
		LOCAlgorithm loca = new LOCAlgorithm();
		System.out.println("Graph statistiken :");
		System.out.println("Nodes " + graph.nodeCount() + " Edges " + graph.edgeCount());
		
		System.out.println("Test Local Density:");
		HashMap<Node, Integer> map = loca.getLocalDensityMap(graph);
		Node bestnode = loca.getMaxValueNode(map);
		Set<Node> cluster = new HashSet<Node>();
		System.out.println("Node : " + cluster.toString());
		cluster.add(bestnode);
		Node [] nodes = graph.getNodeArray();
		for(Node node : nodes) {
			double nodefitness = loca.getNodeFitness(node, cluster, graph);
			System.out.println(node.toString()+ "hat fitnesswert " + nodefitness);
		}
		
	}
	
	private void test2(CustomGraph graph) {
		
	}
	private void test3(CustomGraph graph) {
		
		
	}
	private void test4(CustomGraph graph) {
		LOCAlgorithm loca = new LOCAlgorithm();
		try {
			Cover cover = loca.detectOverlappingCommunities(graph);
			System.out.println("Das cover sieht so aus : " + cover.toString());
		} catch ( Exception e) {
			e.printStackTrace();
		}
	}
	
	// Creates graph1 from Paper
	private CustomGraph getGraph1() {	
		CustomGraph graph = new CustomGraph();
				
		// Creates nodes
		Node n[] = new Node[7];	
		for (int i = 0; i < 7; i++) {
			n[i] = graph.createNode();	
		}
				
		// first community (nodes: 0, 1, 2, 3)
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (i != j ) {
					graph.createEdge(n[i], n[j]);
				}
			}
		}
		
		// second community (nodes: 3, 4, 5, 6)
		for(int i = 3; i < 7; i++) {
			for (int j = 3; j < 7; j++) {
				if(i!=j ) {
				graph.createEdge(n[i], n[j]);
				}
			}
		}
		return graph;
	}
	
	// Creates graph2 from Paper
	private CustomGraph getGraph2() {
		
		graph = new CustomGraph();
				
		// Creates nodes
		n = new Node[8];	
		for (int i = 0; i < 8; i++) {
			n[i] = graph.createNode();	
		}	
		// first community (nodes: 0, 1, 2, 3)
		e(0,1);
		e(0,3);
		e(1,3);
		e(1,2);
		e(2,3);		
		// second community (nodes: 4, 5, 6, 7)
		for(int i = 4; i < 8; i++) {
			for (int j = 4; j < 8; j++) {
				if(i!=j ) {
				graph.createEdge(n[i], n[j]);
				}
			}
		}
		
		e(0,4);
		e(2,4);
		return graph;
	}
	
	// Creates a graph of 0-1-2-3-4
	private CustomGraph getGraph3() {
		graph = new CustomGraph();
		
		// Creates nodes
		n = new Node[7];	
		for (int i = 0; i < 7; i++) {
			n[i] = graph.createNode();	
		}
		e(0,1);
		e(1,2);
		e(2,3);
		e(3,4);
		e(4,5);
		e(5,6);
		return graph;
	}
	
	private CustomGraph getGraph4() {
		graph = new CustomGraph();
		
		// Creates nodes
		n = new Node[20];	
		for (int i = 0; i < 20; i++) {
			n[i] = graph.createNode();	
		}
		e(0,1);
		e(1,2);
		e(2,3);
		e(3,4);
		e(6,7);
		e(6,8);
		e(6,9);
		e(7,8);
		e(7,9);
		e(7,17);
		e(8,9);
		e(8,10);
		e(9,11);
		e(9,12);
		e(9,10);
		e(10,13);
		e(13,14);
		e(13,15);
		e(13,16);
		e(14,15);
		e(15,16);
		e(15,18);
		e(18,19);
		return graph;
	}
	
	private void e(int a, int b) {
		graph.createEdge(n[a], n[b]);
		graph.createEdge(n[b], n[a]);
	}
	
	private void p(String s) {
		System.out.println(s);
	}
}
