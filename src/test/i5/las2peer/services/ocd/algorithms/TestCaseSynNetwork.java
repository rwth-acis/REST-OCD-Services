package i5.las2peer.services.ocd.algorithms;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.la4j.matrix.Matrix;

import i5.las2peer.services.ocd.algorithms.EvolutionaryAlgorithmBasedOnSimilarity;
import i5.las2peer.services.ocd.algorithms.SignedDMIDAlgorithm;
import i5.las2peer.services.ocd.algorithms.SignedProbabilisticMixtureAlgorithm;
import i5.las2peer.services.ocd.benchmarks.SignedLfrBenchmark;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphProcessor;

import i5.las2peer.services.ocd.metrics.ExtendedNormalizedMutualInformationMetric;
import i5.las2peer.services.ocd.metrics.FrustrationMetric;
import i5.las2peer.services.ocd.metrics.SignedModularityMetric;

public class TestCaseSynNetwork {
	public static void main(String[] args) throws Exception {
		/*
		 * algorithms
		 */
		EvolutionaryAlgorithmBasedOnSimilarity algoMEA = new EvolutionaryAlgorithmBasedOnSimilarity();
		SignedProbabilisticMixtureAlgorithm algoSPM = new SignedProbabilisticMixtureAlgorithm();
		SignedDMIDAlgorithm algoSDMID = new SignedDMIDAlgorithm();

		/*
		 * metrics
		 */
		ExtendedNormalizedMutualInformationMetric metricNMI = new ExtendedNormalizedMutualInformationMetric();
		SignedModularityMetric metricModularity = new SignedModularityMetric();
		FrustrationMetric metricFrustration = new FrustrationMetric();

		/*
		 * Synthetic network basic definitions
		 */
		GraphProcessor processor = new GraphProcessor();

		int n = 100;//200
		int k = 3;
		int maxk = 15;// 6
		double mu = 0.1;
		double t1 = -2;
		double t2 = -1;
		int minc = 5;
		int maxc = 30;
		int on = 5;
		int om = 2;
		double neg = 0.01;
		double pos = 0.01;

		/*
		 * output
		 */
		String outputPath = "ocd/test/" + "outputSynNetwork.txt";
//		String outputPathGraphCover = "ocd/test/" + "outputSynNetwork1_graphCover.txt";// another
																						// file
		FileWriter outputFile = new FileWriter(outputPath);
//		FileWriter outputFileGraphCover = new FileWriter(outputPathGraphCover);// another
																				// file
		outputFile.write(String.format("parameters: " + n + " " + k + " " + maxk + " " + mu + " " + t1 + " " + t2 + " "
				+ minc + " " + maxc + " " + on + " " + om + " " + neg + " " + pos));

		/*
		 * loop to generate synthetic networks
		 */
		try {
			for (int i = 0; i < 3; i++) {
				for   (pos= 0.01; pos <=0.10; pos=pos+0.01) {//here comes the change)
					SignedLfrBenchmark myLfr = new SignedLfrBenchmark(n, k, maxk, mu, t1, t2, minc, maxc, on, om, neg,
							pos);
					Cover realCover = myLfr.createGroundTruthCover();
					CustomGraph graph = realCover.getGraph();

					/*
					 * SDMID
					 */
					long startSDMID = System.currentTimeMillis();
					Cover coverSDMIDLfr = algoSDMID.detectOverlappingCommunities(graph);
					long durationSDMID = System.currentTimeMillis() - startSDMID;

					/*
					 * create a copy of graph
					 */
					CustomGraph undirectedGraph = processor.copyGraph(graph);
					Matrix membership = realCover.getMemberships();
					processor.makeUndirected(undirectedGraph);
					Cover undirectedCover = new Cover(undirectedGraph, membership);

					/*
					 * SPM & MEA
					 */

					long startMEA = System.currentTimeMillis();
					Cover coverMEALfr = algoMEA.detectOverlappingCommunities(undirectedGraph);
					long durationMEA = System.currentTimeMillis() - startMEA;
					long startSPM = System.currentTimeMillis();
					Cover coverSPMLfr = algoSPM.detectOverlappingCommunities(undirectedGraph);
					long durationSPM = System.currentTimeMillis() - startSPM;
					/*
					 * Cover list
					 */
					List<Cover> coverList = new ArrayList<Cover>();
					int arrayStart = 0;
					coverList.add(arrayStart, coverSDMIDLfr);
					coverList.add(arrayStart + 1, coverMEALfr);
					coverList.add(arrayStart + 2, coverSPMLfr);

					/*
					 * time list
					 */

					List<Long> timeList = new ArrayList<Long>();
					int arrayTimeStart = 0;
					timeList.add(arrayTimeStart, durationSDMID);
					timeList.add(arrayTimeStart + 1, durationMEA);
					timeList.add(arrayTimeStart + 2, durationSPM);

					outputFile.write(System.lineSeparator());
					outputFile.write(String.format("\t\t"));
					for (Cover cover : coverList) {
						outputFile.write(String.format(metricModularity.measure(cover) + "\t"));
					}
					outputFile.write(String.format("\t\t"));
					for (Cover cover : coverList) {
						outputFile.write(String.format(metricFrustration.measure(cover) + "\t"));
					}
					outputFile.write(String.format("\t\t"));
					for (Cover cover : coverList) {
						// differentiate cover
						if (cover == coverMEALfr | cover == coverSPMLfr) {
							outputFile.write(String.format(metricNMI.measure(cover, undirectedCover) + "\t"));
						} else {
							outputFile.write(String.format(metricNMI.measure(cover, realCover) + "\t"));
						}
					}
					outputFile.write(String.format("\t\t"));

					/*
					 * write time
					 */
					for (Long time : timeList) {
						outputFile.write(String.format(time + "\t"));
					}

					/*
					 * write community structure
					 */
					for (Cover cover : coverList) {
						outputFile.write(String.format(cover.getCommunityStructure() + "\t"));
					}
					outputFile.write(String.format("\t\t"));
					outputFile.write(String.format(realCover.getCommunityStructure() + " "));

					// another file
					// for (Cover cover:coverList){
					// outputFileGraphCover.write(String.format(cover.toString()+"
					// "));
					// outputFileGraphCover.write(System.lineSeparator());
					// outputFileGraphCover.write(System.lineSeparator());
					// }
				}
				outputFile.write(System.lineSeparator());
			}
		} finally {
			outputFile.close();
//			outputFileGraphCover.close();
		} // another file

	}
}