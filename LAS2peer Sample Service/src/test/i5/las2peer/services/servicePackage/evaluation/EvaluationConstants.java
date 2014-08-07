package i5.las2peer.services.servicePackage.evaluation;

public class EvaluationConstants {
	
	public static final String outputFolderPath = "data\\evaluation_outputs\\";	
	public static final String inputFolderPath = "data\\evaluation_inputs\\";
	public static final String newmanOutputFolderPath = "data\\evaluation_outputs_newman\\";
	public static final String lfrOutputFolderPath = "data\\evaluation_outputs_lfr\\";
	
	/*
	 * test files
	 */
	public static final String testSawmillEvaluationLabeledMembershipMatrixOutputPath = outputFolderPath + "testAdapterSawmill";
	public static final String sawmillCoverOutputPath = outputFolderPath + "testSawmillCover";
	public static final String sawmillWeightedEdgeListInputPath = inputFolderPath + "testSawmill.txt";
	
	/*
	 * graph input files
	 */
	public static final String aercsUnweightedEdgeListInputPath = inputFolderPath + "AERCS.txt";
	public static final String coraUnweightedEdgeListInputPath = inputFolderPath + "cora.txt";
	public static final String emailWeightedEdgeListInputPath = inputFolderPath + "email.txt";
	public static final String internetUnweightedEdgeListInputPath = inputFolderPath + "internet.txt";
	public static final String jazzWeightedEdgeListInputPath = inputFolderPath + "jazz.txt";
	public static final String pgpWeightedEdgeListInputPath = inputFolderPath + "pgp.txt";
	public static final String facebookUnweightedEdgeListInputPath = inputFolderPath + "facebook.txt";
	
	/*
	 * cover output file prefixes
	 */
	public static final String aercsCoverOutputPath = outputFolderPath + "AERCSCover";
	public static final String coraCoverOutputPath = outputFolderPath + "coraCover";
	public static final String emailCoverOutputPath = outputFolderPath + "emailCover";
	public static final String internetCoverOutputPath = outputFolderPath + "internetCover";
	public static final String jazzCoverOutputPath = outputFolderPath + "jazzCover";
	public static final String pgpCoverOutputPath = outputFolderPath + "pgpCover";
	public static final String facebookCoverOutputPath = outputFolderPath + "facebookCover";
	public static final String newmanCoverOutputPath = newmanOutputFolderPath + "newmanCover";
	public static final String lfrCoverOutputPath = lfrOutputFolderPath + "lfrCover";
	
	/*
	 * graph output file prefixes
	 */
	public static final String newmanGraphOutputPath = newmanOutputFolderPath + "newmanGraph";
	public static final String lfrGraphOutputPath = lfrOutputFolderPath + "lfrGraph";
	
	/*
	 * metric averages output file prefixes
	 */
	public static final String newmanMetricsOutputPath = newmanOutputFolderPath + "newmanMetrics";
	public static final String lfrMetricsOutputPath = lfrOutputFolderPath + "lfrMetrics";
	
}
