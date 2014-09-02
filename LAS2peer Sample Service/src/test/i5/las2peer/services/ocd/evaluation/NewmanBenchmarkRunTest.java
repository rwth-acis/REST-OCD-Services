package i5.las2peer.services.ocd.evaluation;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputAdapter;
import i5.las2peer.services.ocd.adapters.graphOutput.WeightedEdgeListGraphOutputAdapter;
import i5.las2peer.services.ocd.algorithms.BinarySearchRandomWalkLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.ClizzAlgorithm;
import i5.las2peer.services.ocd.algorithms.MergingOfOverlappingCommunitiesAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithmExecutor;
import i5.las2peer.services.ocd.algorithms.RandomWalkLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.SpeakerListenerLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.SskAlgorithm;
import i5.las2peer.services.ocd.algorithms.WeightedLinkCommunitiesAlgorithm;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.benchmarkModels.BenchmarkException;
import i5.las2peer.services.ocd.benchmarkModels.GroundTruthBenchmarkModel;
import i5.las2peer.services.ocd.benchmarkModels.NewmanModel;
import i5.las2peer.services.ocd.evaluation.BenchmarkAveragesOutputAdapter;
import i5.las2peer.services.ocd.evaluation.EvaluationConstants;
import i5.las2peer.services.ocd.evaluation.EvaluationLabeledMembershipMatrixOutputAdapter;
import i5.las2peer.services.ocd.graph.Cover;
import i5.las2peer.services.ocd.graph.CustomGraph;
import i5.las2peer.services.ocd.metrics.ExtendedNormalizedMutualInformation;
import i5.las2peer.services.ocd.metrics.KnowledgeDrivenMeasure;
import i5.las2peer.services.ocd.metrics.MetricException;
import i5.las2peer.services.ocd.metrics.MetricType;
import i5.las2peer.services.ocd.metrics.OcdMetricExecutor;
import i5.las2peer.services.ocd.metrics.OmegaIndex;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

/*
 * Newman Model Evaluation
 */
@Ignore
public class NewmanBenchmarkRunTest {

	@Test
	public void testOnNewman() throws IOException, OcdAlgorithmException, AdapterException, MetricException, BenchmarkException
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
				GroundTruthBenchmarkModel model = new NewmanModel(i);
				Cover groundTruth = model.createGroundTruthCover();
				CustomGraph graph = groundTruth.getGraph();
				GraphOutputAdapter graphAdapter = new WeightedEdgeListGraphOutputAdapter(
						new FileWriter(EvaluationConstants.newmanGraphOutputPath + "param" + i + "it" + j + ".txt"));
				graphAdapter.writeGraph(graph);
				CoverOutputAdapter groundTruthAdapter = new EvaluationLabeledMembershipMatrixOutputAdapter(
						new FileWriter(EvaluationConstants.newmanCoverOutputPath + "param" + i + "it" + j + "groundTruth" + ".txt"));
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
							new FileWriter(EvaluationConstants.newmanCoverOutputPath + "param" + i + "it" + j + algoFileNameExtensions[k]));
					coverAdapter.writeCover(cover);
					timeAverages[k] += cover.getMetric(MetricType.EXECUTION_TIME).getValue() / 2d;
					nmiAverages[k] += cover.getMetric(MetricType.EXTENDED_NORMALIZED_MUTUAL_INFORMATION).getValue() / 2d;
					omegaAverages[k] += cover.getMetric(MetricType.OMEGA_INDEX).getValue() / 2d;
				}
			}
			BenchmarkAveragesOutputAdapter metricAdapter = new BenchmarkAveragesOutputAdapter(
					new FileWriter(EvaluationConstants.newmanMetricsOutputPath + "param" + i + ".txt"));
			metricAdapter.writeAverages(algoFileNameExtensions, timeAverages, nmiAverages, omegaAverages);
		}
	}


	
	
}