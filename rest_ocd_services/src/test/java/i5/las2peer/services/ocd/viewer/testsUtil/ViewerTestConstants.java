package i5.las2peer.services.ocd.viewer.testsUtil;

public class ViewerTestConstants {
	
	/*
	 * Graph input files
	 */
	public static final String outputFolderPath = "ocd/test/output/";	
	
	public static final String inputFolderPath = "ocd/test/input/";
	
	public static final String sawmillEdgeListInputPath = inputFolderPath + "SawmillEdgeList.txt";
	
	public static final String dolphingGmlInputPath = inputFolderPath + "dolphins.gml";

	
	/*
	 * Cover input files
	 */
	public static final String slpaSawmillLabeledMembershipMatrix = inputFolderPath + "SlpaSawmillLabeledMembershipMatrix.txt";	
	
	public static final String slpaDolphinsLabeledMembershipMatrix = inputFolderPath + "SlpaDolphinsLabeledMembershipMatrix.txt";
	
	
	/*
	 * Svg graph output files
	 */
	public static final String tinyCircleSvgOutputPath = ViewerTestConstants.outputFolderPath + "TinyCircleGraph.svg";
	
	public static final String twoCommunitiesSvgOutputPath = ViewerTestConstants.outputFolderPath + "TwoCommunitiesGraph.svg";
	
	public static final String sawmillSvgOutputPath = ViewerTestConstants.outputFolderPath + "SawmillGraph.svg";
	
	public static final String dolphinsSvgOutputPath = ViewerTestConstants.outputFolderPath + "DolphinsGraph.svg";
	
	/*
	 * Json graph output files
	 */
	public static final String tinyCircleJsonOutputPath = ViewerTestConstants.outputFolderPath + "TinyCircleGraph.json";
	
	public static final String twoCommunitiesJsonOutputPath = ViewerTestConstants.outputFolderPath + "TwoCommunitiesGraph.json";
	
	public static final String sawmillJsonOutputPath = ViewerTestConstants.outputFolderPath + "SawmillGraph.json";
	
	/*
	 * Svg cover output files
	 */
	
	public static final String slpaSawmillSvgOutputPath = ViewerTestConstants.outputFolderPath + "SlpaSawmillCover.svg";
	
	public static final String slpaDolphinsSvgOutputPath = ViewerTestConstants.outputFolderPath + "SlpaDolphinsCover.svg";
	
	public static final String sequenceCover1OutputPath = ViewerTestConstants.outputFolderPath + "sequenceCover1.svg";
	public static final String sequenceCover2OutputPath = ViewerTestConstants.outputFolderPath + "sequenceCover2.svg";
}
