package i5.las2peer.services.servicePackage.evaluation;

public class EvaluationConstants {
	
	public static final String outputFolderPath = "data\\evaluation_outputs\\";	
	public static final String inputFolderPath = "data\\evaluation_inputs\\";
	
	/*
	 * graph input files
	 */
	public static final String aercsUnweightedEdgeListInputPath = inputFolderPath + "AERCS.txt";
	public static final String coraUnweightedEdgeListInputPath = inputFolderPath + "cora.txt";
	public static final String emailWeightedEdgeListInputPath = inputFolderPath + "email.txt";
	public static final String internetUnweightedEdgeListInputPath = inputFolderPath + "internet.txt";
	public static final String jazzWeightedEdgeListInputPath = inputFolderPath + "jazz.txt";
	public static final String pgpWeightedEdgeListInputPath = inputFolderPath + "pgp.txt";
	
	/*
	 * cover output file prefixes
	 */
	public static final String aercsCoverOutputPath = outputFolderPath + "AERCS.txt";
	public static final String coraCoverOutputPath = outputFolderPath + "cora.txt";
	public static final String emailCoverOutputPath = outputFolderPath + "email.txt";
	public static final String internetCoverOutputPath = outputFolderPath + "internet.txt";
	public static final String jazzCoverOutputPath = outputFolderPath + "jazz.txt";
	public static final String pgpCoverOutputPath = outputFolderPath + "pgp.txt";

	
}
