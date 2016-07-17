package i5.las2peer.services.ocd.algorithms;

import java.io.FileWriter;

import i5.las2peer.services.ocd.algorithms.EvolutionaryAlgorithmBasedOnSimilarity;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import i5.las2peer.services.ocd.metrics.FrustrationMetric;
import i5.las2peer.services.ocd.metrics.SignedModularityMetric;

import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class TestCaseSPM {
	public static void main(String[] args) throws Exception {
		// Real networks
		String outputPathRealNetwork = "ocd/test/" + "outputRealNetworkSPM.txt";
//		String outputPathGraphCover = "ocd/test/" + "ooutputRealNetworkSPM_graphCover.txt";// another
																							// file
		FileWriter outputFileRealNetwork = new FileWriter(outputPathRealNetwork);
//		FileWriter outputFileGraphCover = new FileWriter(outputPathGraphCover);// another
																				// file

		CustomGraph[][] graphArray;
		graphArray = new CustomGraph[1][2];
		graphArray[0][0] = OcdTestGraphFactory.getWikiElecGraph();
		graphArray[0][1] = OcdTestGraphFactory.getWikiElecUndirectedGraph();

		EvolutionaryAlgorithmBasedOnSimilarity algoSPM = new EvolutionaryAlgorithmBasedOnSimilarity();

		SignedModularityMetric metricModularity = new SignedModularityMetric();
		FrustrationMetric metricFrustration = new FrustrationMetric();

		for (int i = 0; i < 1; i++) {
			long startSPM = System.currentTimeMillis();
			Cover coverSPM = algoSPM.detectOverlappingCommunities(graphArray[i][1]);
			long durationSPM = System.currentTimeMillis() - startSPM;

			try {
				outputFileRealNetwork.write(String.format("community structure: " + coverSPM.getCommunityStructure()));
				outputFileRealNetwork.write(System.lineSeparator());
				outputFileRealNetwork.write(String.format("modularity: " + metricModularity.measure(coverSPM)
						+ " / frustration: " + metricFrustration.measure(coverSPM)));
				outputFileRealNetwork.write(System.lineSeparator());
				outputFileRealNetwork.write(String.format("duration: " + durationSPM));

				// another file
//				outputFileGraphCover.write(String.format(coverSPM.toString() + " "));

			} finally {
				outputFileRealNetwork.close();
//				outputFileGraphCover.close();
			} // another file

		}
	}

}