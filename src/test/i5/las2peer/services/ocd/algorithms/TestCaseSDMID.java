package i5.las2peer.services.ocd.algorithms;

import java.io.FileWriter;

import i5.las2peer.services.ocd.algorithms.SignedDMIDAlgorithm;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import i5.las2peer.services.ocd.metrics.FrustrationMetric;
import i5.las2peer.services.ocd.metrics.SignedModularityMetric;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class TestCaseSDMID {
	public static void main(String[] args) throws Exception {
		// Real networks
		String outputPathRealNetwork = "ocd/test/" + "outputRealNetworkSDMID.txt";
//		String outputPathGraphCover = "ocd/test/" + "ooutputRealNetworkSDMID_graphCover.txt";// another
																								// file
		FileWriter outputFileRealNetwork = new FileWriter(outputPathRealNetwork);
//		FileWriter outputFileGraphCover = new FileWriter(outputPathGraphCover);// another
																				// file

		CustomGraph[][] graphArray;
		graphArray = new CustomGraph[1][2];
		graphArray[0][0] = OcdTestGraphFactory.getWikiElecGraph();
		graphArray[0][1] = OcdTestGraphFactory.getWikiElecUndirectedGraph();


		SignedDMIDAlgorithm algoSDMID = new SignedDMIDAlgorithm();

		SignedModularityMetric metricModularity = new SignedModularityMetric();
		FrustrationMetric metricFrustration = new FrustrationMetric();

		for (int i = 0; i < 1; i++) {
			long start = System.currentTimeMillis();
			Cover coverSDMID = algoSDMID.detectOverlappingCommunities(graphArray[i][0]);
			long duration = System.currentTimeMillis() - start;

			try {
				outputFileRealNetwork
						.write(String.format("community structure: " + coverSDMID.getCommunityStructure()));
				outputFileRealNetwork.write(System.lineSeparator());
				outputFileRealNetwork.write(String.format("modularity: " + metricModularity.measure(coverSDMID)
						+ " / frustration: " + metricFrustration.measure(coverSDMID)));
				outputFileRealNetwork.write(System.lineSeparator());
				outputFileRealNetwork.write(String.format("duration: " + duration));

				// another file
//				outputFileGraphCover.write(String.format(coverSDMID.toString() + " "));

			} finally {
				outputFileRealNetwork.close();
//				outputFileGraphCover.close();
			} // another file

		}
	}

}
