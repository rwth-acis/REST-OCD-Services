package i5.las2peer.services.servicePackage.evaluation;

public class EvaluationConstants {
	
	public static final String outputFolderPath = "data\\evaluation_outputs\\";	
	public static final String inputFolderPath = "data\\evaluation_inputs\\";
	
	/*
	 * graph input files
	 */
	public static final String acmSigmodUnweightedEdgeListInputPath = inputFolderPath + "ACM_SIGMOD.txt";
	public static final String cikmUnweightedEdgeListInputPath = inputFolderPath + "CIKM.txt";
	public static final String icdeUnweightedEdgeListInputPath = inputFolderPath + "ICDE.txt";
	public static final String icdmUnweightedEdgeListInputPath = inputFolderPath + "ICDM.txt";
	public static final String kddUnweightedEdgeListInputPath = inputFolderPath + "KDD.txt";
	public static final String podsUnweightedEdgeListInputPath = inputFolderPath + "PODS.txt";
	public static final String siamDmUnweightedEdgeListInputPath = inputFolderPath + "SIAM_DM.txt";
	public static final String vldbUnweightedEdgeListInputPath = inputFolderPath + "VLDB.txt";
	
	/*
	 * slpa cover output files
	 */
	public static final String slpaAcmSigmodLabeledMembershipMatrixOutputPath = outputFolderPath + "slpaAcmSigmod.txt";
	public static final String slpaCikmLabeledMembershipMatrixOutputPath = outputFolderPath + "slpaCikm.txt";
	public static final String slpaIcdeLabeledMembershipMatrixOutputPath = outputFolderPath + "slpaIcde.txt";
	public static final String slpaIcdmLabeledMembershipMatrixOutputPath = outputFolderPath + "slpaIcdm.txt";
	public static final String slpaKddLabeledMembershipMatrixOutputPath = outputFolderPath + "slpaKdd.txt";
	public static final String slpaPodsLabeledMembershipMatrixOutputPath = outputFolderPath + "slpaPods.txt";
	public static final String slpaSiamDmLabeledMembershipMatrixOutputPath = outputFolderPath + "slpaSiamDm.txt";
	public static final String slpaVldbLabeledMembershipMatrixOutputPath = outputFolderPath + "slpaVldb.txt";
	
	/*
	 * ssk cover output files
	 */
	public static final String sskSiamDmLabeledMembershipMatrixOutputPath = outputFolderPath + "sskSiamDm.txt";

}
