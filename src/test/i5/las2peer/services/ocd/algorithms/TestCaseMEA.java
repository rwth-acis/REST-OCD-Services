package i5.las2peer.services.ocd.algorithms;

import java.io.FileWriter;


import i5.las2peer.services.ocd.algorithms.EvolutionaryAlgorithmBasedOnSimilarity;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;


import i5.las2peer.services.ocd.metrics.FrustrationMetric;
import i5.las2peer.services.ocd.metrics.SignedModularityMetric;

import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class TestCaseMEA {
	public static void main(String[] args) throws Exception {
		// Real networks
		String outputPathRealNetwork = "ocd/test/" + "outputRealNetworkMEA.txt";
//		String outputPathGraphCover = "ocd/test/" + "ooutputRealNetworkMEA_graphCover.txt";// another
																							// file
		FileWriter outputFileRealNetwork = new FileWriter(outputPathRealNetwork);
//		FileWriter outputFileGraphCover = new FileWriter(outputPathGraphCover);// another
																				// file

		CustomGraph[][] graphArray;
		graphArray = new CustomGraph[1][2];
		graphArray[0][0] = OcdTestGraphFactory.getWikiElecGraph();
		graphArray[0][1] = OcdTestGraphFactory.getWikiElecUndirectedGraph();


		EvolutionaryAlgorithmBasedOnSimilarity algoMEA = new EvolutionaryAlgorithmBasedOnSimilarity();

		SignedModularityMetric metricModularity = new SignedModularityMetric();
		FrustrationMetric metricFrustration = new FrustrationMetric();

		for (int i = 0; i < 1; i++) {
			long startMEA = System.currentTimeMillis();
			Cover coverMEA = algoMEA.detectOverlappingCommunities(graphArray[i][1]);
			long durationMEA = System.currentTimeMillis() - startMEA;

			try {
				outputFileRealNetwork.write(String.format("community structure: " + coverMEA.getCommunityStructure()));
				outputFileRealNetwork.write(System.lineSeparator());
				outputFileRealNetwork.write(String.format("modularity: " + metricModularity.measure(coverMEA)
						+ " / frustration: " + metricFrustration.measure(coverMEA)));
				outputFileRealNetwork.write(System.lineSeparator());
				outputFileRealNetwork.write(String.format("duration: " + durationMEA));

				// another file
//				outputFileGraphCover.write(String.format(coverMEA.toString() + " "));

			} finally {
				outputFileRealNetwork.close();
//				outputFileGraphCover.close();
			} // another file

		}
	}

}
