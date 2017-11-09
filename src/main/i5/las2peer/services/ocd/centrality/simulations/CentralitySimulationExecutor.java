package i5.las2peer.services.ocd.centrality.simulations;

import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmException;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

public class CentralitySimulationExecutor {
	
	/**
	 * Calculates a CentralityMap by running simulations on a graph.
	 * @param graph The graph.
	 * @param simulation The algorithm.
	 * @return A CentralityMap of the graph calculated by the algorithm.
	 * @throws CentralityAlgorithmException In case of an algorithm failure.
	 * @throws InterruptedException In case of an algorithm interrupt.
	 */
	public CentralityMap execute(CustomGraph graph, CentralitySimulation simulation) throws InterruptedException {
		CustomGraph graphCopy = new CustomGraph(graph);
		GraphProcessor processor = new GraphProcessor();
		processor.makeCompatible(graphCopy, simulation.compatibleGraphTypes());
		long startTime = System.currentTimeMillis();
		CentralityMap map = simulation.getValues(graphCopy);
		map.setCreationMethod(new CentralityCreationLog(simulation.getSimulationType(), CentralityCreationType.SIMULATION, simulation.getParameters(), simulation.compatibleGraphTypes()));
		map.getCreationMethod().setStatus(ExecutionStatus.COMPLETED);
		long executionTime = System.currentTimeMillis() - startTime;
		map.getCreationMethod().setExecutionTime(executionTime);
		return map;
	}

}
