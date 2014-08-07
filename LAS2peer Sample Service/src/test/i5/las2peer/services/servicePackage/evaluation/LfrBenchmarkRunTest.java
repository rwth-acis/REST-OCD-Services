package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.servicePackage.adapters.graphOutput.GraphOutputAdapter;
import i5.las2peer.services.servicePackage.adapters.graphOutput.WeightedEdgeListGraphOutputAdapter;
import i5.las2peer.services.servicePackage.algorithms.BinarySearchRandomWalkLabelPropagationAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.ClizzAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.MergingOfOverlappingCommunitiesAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithmExecutor;
import i5.las2peer.services.servicePackage.algorithms.RandomWalkLabelPropagationAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.SpeakerListenerLabelPropagationAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.SskAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.WeightedLinkCommunitiesAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.servicePackage.benchmarkModels.BenchmarkException;
import i5.las2peer.services.servicePackage.benchmarkModels.GroundTruthBenchmarkModel;
import i5.las2peer.services.servicePackage.benchmarkModels.LfrModel;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphProcessor;
import i5.las2peer.services.servicePackage.graph.GraphType;
import i5.las2peer.services.servicePackage.metrics.ExtendedNormalizedMutualInformation;
import i5.las2peer.services.servicePackage.metrics.KnowledgeDrivenMeasure;
import i5.las2peer.services.servicePackage.metrics.MetricException;
import i5.las2peer.services.servicePackage.metrics.MetricType;
import i5.las2peer.services.servicePackage.metrics.OcdMetricExecutor;
import i5.las2peer.services.servicePackage.metrics.OmegaIndex;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import org.junit.Ignore;
import org.junit.Test;

/*
 * Lfr Model Evaluation
 */

public class LfrBenchmarkRunTest {

	@Ignore
	@Test
	public void testOnLfr() throws IOException, OcdAlgorithmException, AdapterException, MetricException, BenchmarkException
	{
		OcdAlgorithm[] algos = new OcdAlgorithm[10];
		String[] algoFileNameExtensions = new String[10];
		double[] timeAverages;
		double[] nmiAverages;
		double[] omegaAverages;
		algos[0] = new BinarySearchRandomWalkLabelPropagationAlgorithm();
		algos[1] = new ClizzAlgorithm();
		algos[2] = new MergingOfOverlappingCommunitiesAlgorithm();
		algos[3] = new RandomWalkLabelPropagationAlgorithm(0.05, 1000, 0.001);
		algos[4] = new RandomWalkLabelPropagationAlgorithm(0.1, 1000, 0.001);
		algos[5] = new RandomWalkLabelPropagationAlgorithm(0.15, 1000, 0.001);
		algos[6] = new RandomWalkLabelPropagationAlgorithm(0.2, 1000, 0.001);
		algos[7] = new SpeakerListenerLabelPropagationAlgorithm();
		algos[8] = new SskAlgorithm();
		algos[9] = new WeightedLinkCommunitiesAlgorithm();
		algoFileNameExtensions[0] = algos[0].getAlgorithmType().name() + ".txt";
		algoFileNameExtensions[1] = algos[1].getAlgorithmType().name() + ".txt";
		algoFileNameExtensions[2] = algos[2].getAlgorithmType().name() + ".txt";
		algoFileNameExtensions[3] = algos[3].getAlgorithmType().name() + "05" + ".txt";
		algoFileNameExtensions[4] = algos[4].getAlgorithmType().name() + "10" + ".txt";
		algoFileNameExtensions[5] = algos[5].getAlgorithmType().name() + "15" + ".txt";
		algoFileNameExtensions[6] = algos[6].getAlgorithmType().name() + "20" + ".txt";
		algoFileNameExtensions[7] = algos[7].getAlgorithmType().name() + ".txt";
		algoFileNameExtensions[8] = algos[8].getAlgorithmType().name() + ".txt";
		algoFileNameExtensions[9] = algos[9].getAlgorithmType().name() + ".txt";
		for(int i=0; i<2; i++) {
			timeAverages = new double[10];
			nmiAverages = new double[10];
			omegaAverages = new double[10];
			for(int j=0; j<2; j++) {
				GroundTruthBenchmarkModel model = new LfrModel(12, 0.1, i*0.1);
				Cover groundTruth = model.createGroundTruthCover();
				CustomGraph graph = groundTruth.getGraph();
				GraphProcessor processor = new GraphProcessor();
				processor.makeCompatible(graph, new HashSet<GraphType>());
				GraphOutputAdapter graphAdapter = new WeightedEdgeListGraphOutputAdapter(
						new FileWriter(EvaluationConstants.lfrGraphOutputPath + "k12mu01" + "param" + i + "it" + j + ".txt"));
				graphAdapter.writeGraph(graph);
				CoverOutputAdapter groundTruthAdapter = new EvaluationLabeledMembershipMatrixOutputAdapter(
						new FileWriter(EvaluationConstants.lfrCoverOutputPath + "k12mu01" + "param" + i + "it" + j + "groundTruth" + ".txt"));
				groundTruthAdapter.writeCover(groundTruth);
				for(int k=0; k<algos.length; k++) {
					OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
					Cover cover = executor.execute(graph, algos[k], 0);
					cover.filterMembershipsbyThreshold(0.15);
					OcdMetricExecutor metricExecutor = new OcdMetricExecutor();
					KnowledgeDrivenMeasure nmi = new ExtendedNormalizedMutualInformation();
					metricExecutor.executeKnowledgeDrivenMeasure(cover, groundTruth, nmi);
					KnowledgeDrivenMeasure omega = new OmegaIndex();
					metricExecutor.executeKnowledgeDrivenMeasure(cover, groundTruth, omega);
					CoverOutputAdapter coverAdapter = new EvaluationLabeledMembershipMatrixOutputAdapter(
							new FileWriter(EvaluationConstants.lfrCoverOutputPath + "k12mu01" + "param" + i + "it" + j + algoFileNameExtensions[k]));
					coverAdapter.writeCover(cover);
					timeAverages[k] += cover.getMetric(MetricType.EXECUTION_TIME).getValue() / 5d;
					nmiAverages[k] += cover.getMetric(MetricType.EXTENDED_NORMALIZED_MUTUAL_INFORMATION).getValue() / 5d;
					omegaAverages[k] += cover.getMetric(MetricType.OMEGA_INDEX).getValue() / 5d;
				}
			}
			BenchmarkAveragesOutputAdapter metricAdapter = new BenchmarkAveragesOutputAdapter(
					new FileWriter(EvaluationConstants.lfrMetricsOutputPath + "k12mu01" + "param" + i + ".txt"));
			metricAdapter.writeAverages(algoFileNameExtensions, timeAverages, nmiAverages, omegaAverages);
		}
	}

	@Test
	public void testLfrGraphs() throws IOException, OcdAlgorithmException, AdapterException, MetricException, BenchmarkException
	{
		for(int i=1; i<2; i++) {
			for(int j=0; j<3; j++) {
				GroundTruthBenchmarkModel model = new LfrModel(12, 0.1, i*0.1);
				Cover groundTruth = model.createGroundTruthCover();
				CustomGraph graph = groundTruth.getGraph();
				GraphProcessor processor = new GraphProcessor();
				processor.makeCompatible(graph, new HashSet<GraphType>());
				GraphOutputAdapter graphAdapter = new WeightedEdgeListGraphOutputAdapter(
						new FileWriter(EvaluationConstants.lfrGraphOutputPath + "k12mu01" + "param" + i + "it" + j + ".txt"));
				graphAdapter.writeGraph(graph);
				CoverOutputAdapter groundTruthAdapter = new EvaluationLabeledMembershipMatrixOutputAdapter(
						new FileWriter(EvaluationConstants.lfrCoverOutputPath + "k12mu01" + "param" + i + "it" + j + "groundTruth" + ".txt"));
				groundTruthAdapter.writeCover(groundTruth);
			}
		}
	}

	
	
}
