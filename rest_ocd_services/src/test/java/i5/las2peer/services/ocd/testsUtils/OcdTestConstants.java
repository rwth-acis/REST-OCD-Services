package i5.las2peer.services.ocd.testsUtils;

public class OcdTestConstants {
	
	/*
	 * Input and output folder
	 */
	public static final String outputFolderPath = "ocd/test/output/";	
	public static final String inputFolderPath = "ocd/test/input/";
	
	/*
	 * Graph input filenames 
	 */
	public static final String sawmillWeightedEdgeListInputPath = inputFolderPath + "SawmillEdgeList.txt";
	public static final String sawmillNodeWeightedEdgeListInputPath = inputFolderPath + "SawmillNodeEdgeList.txt";
	public static final String sawmillUnweightedEdgeListInputPath = inputFolderPath + "SawmillUnweightedEdgeList.txt";
	public static final String sawmillGraphMlInputPath = inputFolderPath + "SawmillGraphMl.xml";
	public static final String sawmillAdjacencyMatrixInputPath = inputFolderPath + "SawmillAdjacencyMatrix.txt";
	public static final String fitnessGraphMlInputPath = inputFolderPath + "fitness_graph_jung.graphml";
	public static final String siamDmUnweightedEdgeListInputPath = inputFolderPath + "SIAM_DM.txt";
	public static final String facebookUnweightedEdgeListInputPath = inputFolderPath + "FacebookUnweightedEdgeList.txt";
	public static final String newmanClizzGraphWeightedEdgeListInputPath = inputFolderPath + "newmanClizzGraphWeightedEdgeList.txt";
	public static final String newmanLinkGraphWeightedEdgeListInputPath = inputFolderPath + "newmanLinkGraphWeightedEdgeList.txt";
	public static final String dolphinsGmlInputPath = inputFolderPath + "dolphins.gml";
	public static final String zacharyGmlInputPath = inputFolderPath + "karate.gml";
	public static final String docaTestUnweightedEdgeListInputPath = inputFolderPath + "docaTestUnweightedEdgeList.txt";
	public static final String urchEdgeListInputPath = inputFolderPath + "UrchTest.txt";
	public static final String bioJavaEdgeListInputPath = inputFolderPath + "BioJava.txt";
	public static final String urchPostsEdgeListInputPath = inputFolderPath + "URCH_POSTS.txt";
	public static final String pgsql200EdgeListInputPath = inputFolderPath + "PGSQL_200.txt";
	public static final String jmolEdgeListInputPath = inputFolderPath + "JMOL.txt";
	public static final String academiaXMLInputPath = inputFolderPath + "stackexAcademia.xml";
	public static final String signedLfrWeightedEdgeListInputPath = inputFolderPath + "signedLfrWeightedEdgeList.txt";
	public static final String lfrUnweightedEdgeListInputPath = inputFolderPath + "lfrUnweightedEdgeList.txt";
	public static final String signedLfrSixNodesWeightedEdgeListInputPath = inputFolderPath
			+ "signedLfrSixNodesWeightedEdgeList.txt";
	public static final String signedLfrBlurredWeightedEdgeListInputPath = inputFolderPath
			+ "signedLfrBlurredWeightedEdgeList.txt";
	public static final String sloveneParliamentaryPartyWeightedEdgeListInputPath = inputFolderPath
			+ "sloveneParliamentaryPartyWeightedEdgeList.txt";
	public static final String gahukuGamaWeightedEdgeListInputPath = inputFolderPath + "GahukuGamaWeightedEdgeList.txt";
	public static final String wikiElecWeightedEdgeListInputPath = inputFolderPath + "wikiElecWeightedEdgeList.txt";
	public static final String epinionsWeightedEdgeListInputPath = inputFolderPath + "epinionsWeightedEdgeList.txt";
	public static final String slashDotWeightedEdgeListInputPath = inputFolderPath + "slashDotWeightedEdgeList.txt";

	public static final String timestampedEdgeListWithoutActionPath = inputFolderPath + "TimestampedEdgeListWithoutAction.txt";

	public static final String timestampedEdgeListWithActionPath = inputFolderPath + "TimestampedEdgeListWithAction.txt";
	
	/*
	 * Cover input filenames
	 */
	
	public static final String sawmillArbitraryLabeledMembershipMatrixInputPath = inputFolderPath + "SawmillLabeledMembershipMatrix.txt";
	public static final String sawmillGroundTruthLabeledMembershipMatrixInputPath = inputFolderPath + "SawmillGroundTruthLabeledMembershipMatrix.txt";
	public static final String facebookGroundTruthCommunityMemberListxInputPath = inputFolderPath + "FacebookGroundTruthCommunityMemberList.txt";
	public static final String sawmillGroundTruthCommunityMemberListxInputPath = inputFolderPath + "SawmillGroundTruthCommunityMemberList.txt";
	public static final String newmanClizzGroundTruthLabeledMembershipMatrixInputPath = inputFolderPath + "newmanClizzGroundTruthLabeledMembershipMatrix.txt";
	public static final String newmanClizzCoverLabeledMembershipMatrixInputPath = inputFolderPath + "newmanClizzCoverLabeledMembershipMatrix.txt";
	public static final String newmanLinkGroundTruthLabeledMembershipMatrixInputPath = inputFolderPath + "newmanLinkGroundTruthLabeledMembershipMatrix.txt";
	public static final String newmanLinkCoverLabeledMembershipMatrixInputPath = inputFolderPath + "newmanLinkCoverLabeledMembershipMatrix.txt";
	public static final String signedLfrGroundTruthLabeledMembershipMatrixInputPath = inputFolderPath
			+ "signedLfrGroundTruthLabeledMembershipMatrix.txt";
	public static final String lfrGroundTruthLabeledMembershipMatrixInputPath = inputFolderPath
			+ "signedLfrGroundTruthLabeledMembershipMatrix.txt";
	public static final String signedLfrSixNodesGroundTruthLabeledMembershipMatrixInputPath = inputFolderPath
			+ "signedLfrSixNodesGroundTruthLabeledMembershipMatrix.txt";
	public static final String signedLfrBlurredGroundTruthLabeledMembershipMatrixInputPath = inputFolderPath
			+ "signedLfrBlurredGroundTruthLabeledMembershipMatrix.txt";
	public static final String sloveneParliamentaryPartyCommunityMemberMatrixInputPath = inputFolderPath
			+ "sloveneParliamentaryPartyGroundTruthMembershipMatrix.txt";
	
	/*
	 * Cover output filenames
	 */
	public static final String sawmillLabeledMembershipMatrixOutputPath = outputFolderPath + "SawmillLabeledMembershipMatrix.txt";
	public static final String dolphinsLabeledMembershipMatrixOutputPath = outputFolderPath + "DolphinsLabeledMembershipMatrix.txt";
	public static final String testXmlCoverOutputPath = outputFolderPath + "TestXmlCover";
	
	/*
	 * Graph output filenames
	 */
	public static final String sawmillWeightedEdgeListOutputPath = outputFolderPath + "SawmillWeightedEdgeList.txt";
	public static final String sawmillGraphMlOutputPath = outputFolderPath + "SawmillGraphMl.xml";
	public static final String sawmillMetaXmlOutputPath = outputFolderPath + "SawmillMetaXml.xml";

	/*
	 * Metadata output filenames
	 */
	public static final String testMetaXmlGraphMetaOutputPath = outputFolderPath + "testMetaXmlGraphMeta";
	public static final String testMetaXmlCoverMetaOutputPath = outputFolderPath + "testMetaXmlCoverMeta";
	public static final String testMetaXmlMetricMetaOutputPath = outputFolderPath + "testMetaXmlCentralityMeta";



	/*
	 * Graph names
	 */
	public static final String sawmillName = "Sawmill";
	public static final String directedSawmillName = "Sawmill_Directed";
	public static final String dolphinsName = "Dolphins";
	public static final String aperiodicTwoCommunitiesName = "Aperiodic Two Communities";
	public static final String directedAperiodicTwoCommunitiesName = "Aperiodic Two Communities Directed";
	public static final String facebookName = "Facebook";
	public static final String siamDmName = "SIAM DM";
	public static final String twoCommunitiesName = "Two Communities";
	public static final String fiveNodesGraphName = "Simple Graph with five nodes";
	public static final String simpleTwoComponentsName = "Simple Two Components";
	public static final String linkCommunitiesTestName = "Link Communities Test Graph";
	public static final String newmanClizzName = "Newman Clizz";
	public static final String newmanLinkName = "Newman Link";
	public static final String miniServiceTestGraphName = "Mini Service Test Graph";
	public static final String docaTestGraphName = "Doca Test Graph";
	public static final String contentTestName = "Content Test Graph";
	public static final String jmolName = "Jmol Test Graph";
	public static final String signedLfrGraphName = "Signed Lfr Graph";
	public static final String lfrGraphName = "Lfr Graph";
	public static final String signedLfrSixNodesGraphName = "Signed Lfr Graph";
	public static final String signedLfrBlurredGraphName = "Signed Lfr Blurred Graph";
	public static final String sloveneParliamentaryPartyGraphName = "Slovene Parliamentary Party Graph";
	public static final String wikiElecGraphName = "Wiki Elec Graph";
	public static final String epinionsGraphName = "Epinions Graph";
	public static final String slashDotGraphName = "Slash Dot Graph";	
	public static final String LinkGraphName = "Link Graph";	
	public static final String ModularityTestGraphName = "Undirected Unweighted Graph for Modularity testing";
	public static final String gahukuGamaGraphName = "Gahuku Gama Graph";
	
	/*
	 * others
	 */
	public static final String spmResultFilePath = outputFolderPath + "spmResultPartyNetwork.txt";
	public static final String meaResultFilePath = outputFolderPath + "meaSignedLfrGraphCommunityResult.pop";


}
