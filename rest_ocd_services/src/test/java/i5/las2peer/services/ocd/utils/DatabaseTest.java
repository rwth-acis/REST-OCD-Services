package i5.las2peer.services.ocd.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphCreationLog;
import i5.las2peer.services.ocd.graphs.GraphCreationType;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.algorithms.LOCAlgorithm;
import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.Cover;

import i5.las2peer.services.ocd.metrics.*;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricExecutor;
import i5.las2peer.services.ocd.metrics.StatisticalMeasure;
import i5.las2peer.services.ocd.metrics.ExtendedModularityMetric;
import y.base.Node;

public class DatabaseTest {
	private static CustomGraph graph;
	private static Node n[];
	
	
	public void testPersist() {
		Database db = new Database();
		db.deleteDatabase();
		db.createDatabase();
		db.createCollections();
		
		//create graphcreationlog
		CustomGraph g = getGraph1();
		GraphCreationLog gcl = g.getCreationMethod();
		
		//create covercreationlog
		CoverCreationType cct = CoverCreationType.lookupType(26);
		p("cct " +cct.toString());
		Set<GraphType> graphTypes = this.getSomeGraphTypes();
		p("graphtypes " + graphTypes.toString());
		Map<String, String> param = this.getSomeParam();
		p("param " + param.toString());
		
		CoverCreationLog ccl = new CoverCreationLog(cct, param, graphTypes);
		
		//create ocdmetriclog
		Cover cover = getLOCCover(g);
		OcdMetricLog oml = this.getOcdMetricLog(cover, "em");
		p("oml " + oml.toString());
			
		db.persistGraphCreationLog(gcl);
		db.persistCoverCreationLog(ccl);
		db.persistOcdMetricLog(oml);

		p("GCL key: " + gcl.key);
		p("CCL key: " + ccl.key);
		p("OML key: " + oml.key);
	}
	
	@Test
	public void testLoad() {
		Database db = new Database();
		db.createDatabase();
		GraphCreationLog g = db.loadGraphCreationLog("34175");
		CoverCreationLog c = db.loadCoverCreationLog("34177");
		OcdMetricLog m = db.loadOcdMetricLog("34179");
		p("g : " + g.toString());
		p("c : " + c.toString());
		p("m : " + m.toString());
	}
	
	
	
	
	private Cover getLOCCover(CustomGraph g) {
		LOCAlgorithm loca = new LOCAlgorithm();
		Cover cover = new Cover(g);
		try {
			cover = loca.detectOverlappingCommunities(g);
			System.out.println("Das cover sieht so aus : " + cover.toString());
		} catch ( Exception e) {
			e.printStackTrace();
		}
		return cover;
	}
	private OcdMetricLog getOcdMetricLog(Cover c, String a) {
		StatisticalMeasure sm =new ExtendedModularityMetricCoMembership();
		if(a == "em") {
			 sm = new ExtendedModularityMetric();
		}
		else if(a == "nm"){
			sm = new NewmanModularityCombined();
		}
		OcdMetricExecutor ome = new OcdMetricExecutor();
		OcdMetricType omt = OcdMetricType.lookupType(0);
		OcdMetricLog oml= new OcdMetricLog(omt, 0.5, null, c);
		try {
			oml = ome.executeStatisticalMeasure(c, sm);
		} catch ( Exception e) {
			e.printStackTrace();
		}
		return oml;
	}
	private Set<GraphType> getSomeGraphTypes() {
		GraphType gt1 = GraphType.lookupType(2);
		GraphType gt2 = GraphType.lookupType(5);
		Set<GraphType> graphTypes = new HashSet<GraphType>();
		graphTypes.add(gt1);
		graphTypes.add(gt2);
		return graphTypes;
	}
	private Map<String, String> getSomeParam(){
		Map<String, String> param = new HashMap();
		param.put("par1",  "val1");
		param.put("par2",  "val2");
		param.put("par3", "val3");
		return param;
	}
	private void p(String s) {
		System.out.println(s);
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
			Node n[] = new Node[8];	
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
			
			/*
			 * Connect above two communities, which creates another small community of size 3 (nodes 0, 5, 10)
			 */
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

}
