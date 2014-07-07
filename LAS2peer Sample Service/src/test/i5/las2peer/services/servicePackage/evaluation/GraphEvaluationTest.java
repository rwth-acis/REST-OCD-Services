package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.OverlappingCommunityDetectionAnalyzer;
import i5.las2peer.services.servicePackage.algorithms.OverlappingCommunityDetectionAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.SSKAlgorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphProcessor;
import i5.las2peer.services.servicePackage.metrics.StatisticalMeasure;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import y.base.Node;

public class GraphEvaluationTest {

	@Test
	public void testSiamComponents() {
		System.out.println("Siam Components:");
		CustomGraph graph = OcdTestGraphFactory.getSiamDmGraph();
		GraphProcessor processor = new GraphProcessor();
		Map<CustomGraph, Map<Node, Node>> components = processor.divideIntoConnectedComponents(graph);
		Iterator<CustomGraph> iterator = components.keySet().iterator();
		CustomGraph component;
		int index = 0;
		while(iterator.hasNext()) {
			component = iterator.next();
			System.out.println("Component Nodes " + index + ": " + component.nodeCount());
			index++;
		}
	}

	@Test
	public void testSiamAnalyzer() {
		System.out.println("Siam Component Analyzer:");
		CustomGraph graph = OcdTestGraphFactory.getSiamDmGraph();
		OverlappingCommunityDetectionAnalyzer analyzer = new OverlappingCommunityDetectionAnalyzer();
		List<CustomGraph> graphs = new ArrayList<CustomGraph>();
		graphs.add(graph);
		List<OverlappingCommunityDetectionAlgorithm> algorithms = new ArrayList<OverlappingCommunityDetectionAlgorithm>();
		algorithms.add(new SSKAlgorithm());
		List<StatisticalMeasure> statisticalMeasures = new ArrayList<StatisticalMeasure>();
		List<Cover> covers = analyzer.analyze(graphs, algorithms, statisticalMeasures, 1100);
		Cover cover = covers.get(0);
		System.out.println(cover.toString());
	}
	
}
