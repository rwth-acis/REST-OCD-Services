package i5.las2peer.services.ocd.automatedtesting.metric;

import i5.las2peer.services.ocd.automatedtesting.ocdparser.JacocoReportParser;
import i5.las2peer.services.ocd.automatedtesting.ocdparser.helpers.CoverageData;
import i5.las2peer.services.ocd.automatedtesting.ocdparser.helpers.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class CoverageSubmetric {

    /* weights of each coverage type that should be used when calculating coverage */
    public static final double BRANCH_COVERAGE_WEIGHT = 1.0;
    public static final double INSTRUCTION_COVERAGE_WEIGHT = 1.0;
    public static final double LINE_COVERAGE_WEIGHT = 1.0;
    public static final double METHOD_COVERAGE_WEIGHT = 1.0;



    /* List that holds instructions that will be used to generate the prompt for improving ChatGPT code */
    public static ArrayList<String> promptImprovementRemarks = new ArrayList<>();

    /* coverage values */
    public static double branchCoverage = 0;
    public static double instructionCoverage = 0;
    public static double lineCoverage = 0;
    public static double methodCoverage = 0;





    public static void main(String[] args) {
        HashMap<String, CoverageData> parsedJacocoReport = JacocoReportParser.parseJacocoXmlReportForClass("SskAlgorithm");
        System.out.println(parsedJacocoReport);//TODO:DELETE
        System.out.println("coverage submetric value is " + evaluateCoverageSubmetric("SskAlgorithm"));

    }


    /**
     * Calculates the weighted coverage values based on the parsed JaCoCo report for a specified overlapping
     * community detection algorithm. This method evaluates different types of coverage metrics (branch,
     * instruction, line and method coverage) and computes a weighted average of these values. The name
     * of the algorithm class is passed as a parameter and should match the class name in the JaCoCo report.
     *
     * @param ocdaName The name of the overlapping community detection algorithm class.
     * @return The weighted average of the coverage metrics. Returns 1 if no data is available
     * or if weights sum up to 0.
     */
    public static double evaluateCoverageSubmetric(String ocdaName){

        HashMap<String, CoverageData> parsedJacocoReport = JacocoReportParser.parseJacocoXmlReportForClass(ocdaName);

        double totalCoverage = 0;
        double totalWeight = BRANCH_COVERAGE_WEIGHT + INSTRUCTION_COVERAGE_WEIGHT +
                LINE_COVERAGE_WEIGHT + METHOD_COVERAGE_WEIGHT; //+ CLASS_COVERAGE_WEIGHT;

        /* calculate value for each coverage type */
        totalCoverage += parsedJacocoReport.get("BRANCH").computeCoverage() * BRANCH_COVERAGE_WEIGHT;
        totalCoverage += parsedJacocoReport.get("INSTRUCTION").computeCoverage() * INSTRUCTION_COVERAGE_WEIGHT;
        totalCoverage += parsedJacocoReport.get("LINE").computeCoverage() * LINE_COVERAGE_WEIGHT;
        totalCoverage += parsedJacocoReport.get("METHOD").computeCoverage() * METHOD_COVERAGE_WEIGHT;


        return totalWeight > 0 ? totalCoverage / totalWeight : 1;


    }


    /**
     * Resets variables of coverage submetric to be reused
     */
    public static void resetOCDSubmetricVariables(){
        promptImprovementRemarks = new ArrayList<>();
        branchCoverage = 0;
        instructionCoverage = 0;
        lineCoverage = 0;
        methodCoverage = 0;

    }
}
