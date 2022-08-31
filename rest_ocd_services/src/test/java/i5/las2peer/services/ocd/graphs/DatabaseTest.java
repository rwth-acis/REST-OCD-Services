package i5.las2peer.services.ocd.graphs;

import org.junit.Test;
import com.arangodb.model.StreamTransactionOptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.awt.Color;
import java.util.ArrayList;


import i5.las2peer.services.ocd.algorithms.LOCAlgorithm;
import i5.las2peer.services.ocd.algorithms.RandomWalkLabelPropagationAlgorithm;

import i5.las2peer.services.ocd.metrics.*;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricExecutor;
import i5.las2peer.services.ocd.metrics.StatisticalMeasure;
import i5.las2peer.services.ocd.metrics.ExtendedModularityMetric;

import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.measures.OutDegree;
import  i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmExecutor;
import  i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.measures.AlphaCentrality;
import i5.las2peer.services.ocd.centrality.measures.BridgingCentrality;
import i5.las2peer.services.ocd.centrality.measures.DegreeCentrality;
import i5.las2peer.services.ocd.centrality.measures.PageRank;
import i5.las2peer.services.ocd.centrality.measures.EigenvectorCentrality;

import y.base.Node;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.NodeCursor;

public class DatabaseTest {
	private static CustomGraph graph;
	private static Node n[];
	
	
	public void testCentralityMapPersist() {
		Database db = new Database();
		db.deleteDatabase();
		db.createDatabase();
		db.createCollections();
		CustomGraph g = getGraph1();
		g.setNodeNames();
		p("vor persists");
		db.storeGraph(g);
		CentralityMap cm = this.getCentralityMap(g, 3);
		cm.setName("centrality map name");
		db.storeCentralityMap(cm);
			
	}
	@Test
	public void test() {
		Database db = new Database();
		db.init();
		CustomGraph g = db.getGraph("249140");
		
		CentralityMap cm = db.getCentralityMap("249232", g);
		p("CM graph key");
		p(cm.getGraph().getKey());
		
		Cover c = db.getCover("249212", g);
		p("COVER graph key");
		p(c.getGraph().getKey());

	}
	
	
	
	
	public void persistExampleGraphCoverCMap() {
		Database db = new Database();
		db.deleteDatabase();
		db.init();
		CustomGraph g = getGraph4();
		
		Cover c1 = getLOCCover(g);
		Cover c2 = getRWLPCover(g);
		g.setNodeNames();
		g.setName("Test Graph 4 ");
		g.setPath("der index pfad 4");
		g.setUserName("Marcel");
		g.addType(GraphType.DIRECTED);
		g.addType(GraphType.WEIGHTED);
		c1.setName("Test LOCCover 4");
		c1.setSimCosts(34.1);
		c1.setName("Test RWLPCover 4");
		c1.setSimCosts(12.2);
		
		CoverCreationLog ccl = getCoverCreationLog("loc");
		CoverCreationLog ccl2 = getCoverCreationLog("rwlp");
		
		c1.setCreationMethod(ccl);
		c2.setCreationMethod(ccl2);
		
		setOcdMetricLog(c1, "em");
		setOcdMetricLog(c1, "nm");

		c1.setCommunityColor(0, Color.blue);
		c1.setCommunityColor(1,  Color.red);
		c1.setCommunityName(0, "erste community");
		c1.setCommunityName(1, "zweite community");
		
		db.storeGraph(g);
		CentralityMap cm = this.getCentralityMap(g, 3);
		CentralityMap cm2 = this.getCentralityMap(g, 2);
		
		cm.setName("PageRank centrality map name");
		cm2.setName("Degree centrality map name");
		
		db.storeCover(c1);
		db.storeCover(c2);
		
		p(c1.toString());
		p(c2.toString());
		db.storeCentralityMap(cm);
		db.storeCentralityMap(cm2);
		p("Centrality Maps : : : ");
		p(cm.toString());
		p(cm2.toString());
	}
	
	private Cover getLOCCover(CustomGraph g) {
		LOCAlgorithm loca = new LOCAlgorithm();
		Cover cover = new Cover(g);
		try {
			cover = loca.detectOverlappingCommunities(g);
		} catch ( Exception e) {
			e.printStackTrace();
		}
		return cover;
	}
	private Cover getRWLPCover(CustomGraph g) {
		RandomWalkLabelPropagationAlgorithm rwlpa = new RandomWalkLabelPropagationAlgorithm();
		Cover cover = new Cover(g);
		try {
			cover = rwlpa.detectOverlappingCommunities(g);
		} catch ( Exception e) {
			e.printStackTrace();
		}
		return cover;
	}
	private CoverCreationLog getCoverCreationLog(String typ) {
		int i = 0;
		if(typ == "loc") {i = 26;}
		else if(typ == "rwlp") {i = 2;}
		CoverCreationType cct = CoverCreationType.lookupType(i);
		Set<GraphType> graphTypes = this.getSomeGraphTypes();
		Map<String, String> param = this.getSomeParam();
		return new CoverCreationLog(cct, param, graphTypes);
	}
	
	private OcdMetricLog setOcdMetricLog(Cover c, String a) {
		StatisticalMeasure sm =new ExtendedModularityMetricCoMembership();
		OcdMetricType omt = OcdMetricType.lookupType(0);
		OcdMetricLog oml= new OcdMetricLog(omt, 0.5, this.getSomeParam(), c);
		if(a == "em") {
			 sm = new ExtendedModularityMetric();
			 omt = OcdMetricType.lookupType(2);
		}
		else if(a == "nm"){
			sm = new ModularityMetric();
			omt = OcdMetricType.lookupType(7);
		}
		OcdMetricExecutor ome = new OcdMetricExecutor();
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
		Map<String, String> param = new HashMap<String, String>();
		param.put("par1",  "val1");
		param.put("par2",  "val2");
		param.put("par3", "val3");
		return param;
	}
	private CentralityMap getCentralityMap(CustomGraph g, int i) {
		CentralityMap cm = new CentralityMap(g);
		CentralityAlgorithm ca = getCentralityAlgorithm(i);
		try {
			CentralityAlgorithmExecutor cae = new CentralityAlgorithmExecutor();
			cm = cae.execute(g, ca);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cm;
		
	}
	
	private CentralityAlgorithm getCentralityAlgorithm(int i) {
		CentralityAlgorithm ca = new OutDegree();
		switch(i){
        case 0:
            ca = new AlphaCentrality();
            break;
        case 1:
            ca = new BridgingCentrality();
            break;
        case 2:
            ca = new DegreeCentrality();
            break;
        case 3:
            ca = new PageRank();
            break;
        default:
        	ca = new EigenvectorCentrality();
            break;
        }
		return ca;
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

}
