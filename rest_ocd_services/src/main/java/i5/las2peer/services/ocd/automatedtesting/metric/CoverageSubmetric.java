package i5.las2peer.services.ocd.automatedtesting.metric;

import i5.las2peer.services.ocd.automatedtesting.ocdparser.JacocoReportParser;
import i5.las2peer.services.ocd.automatedtesting.helpers.CoverageData;

import java.util.ArrayList;
import java.util.HashMap;

public class CoverageSubmetric {


    /* coverage values */
    /**
     * Code coverage values.
     */
    private static double branchCoverage = 0;
    private static double instructionCoverage = 0;
    private static double lineCoverage = 0;
    private static double methodCoverage = 0;

    /*  */
    /**
     * Weights of each coverage type that should be used when calculating coverage.
     */
    private static double branchCoverageWeight = 1.0;
    private static double instructionCoverageWeight = 1.0;
    private static double lineCoverageWeight = 1.0;
    private static double methodCoverageWeight = 1.0;


    /**
     * List that holds instructions that will be used to generate the prompt for improving ChatGPT code.
     */
    private static ArrayList<String> promptImprovementRemarks = new ArrayList<>();



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
     * Important: this method should only be executed after JaCoCo report was generated
     *
     * @param ocdaName The name of the overlapping community detection algorithm class.
     * @return The weighted average of the coverage metrics. Returns 1 if no data is available
     * or if weights sum up to 0.
     */
    public static double evaluateCoverageSubmetric(String ocdaName){

        // TODO: this should be done in a nicer way in the future
        // If class name starts with string Generated it is not the main test class
        if (ocdaName.contains("Generated")) {
            ocdaName = ocdaName.substring("Generated".length());
        }

        HashMap<String, CoverageData> parsedJacocoReport = JacocoReportParser.parseJacocoXmlReportForClass(ocdaName);

        branchCoverage = parsedJacocoReport.get("BRANCH").computeCoverage();
        instructionCoverage = parsedJacocoReport.get("INSTRUCTION").computeCoverage();
        lineCoverage = parsedJacocoReport.get("LINE").computeCoverage();
        methodCoverage = parsedJacocoReport.get("METHOD").computeCoverage();


        return getCoverageSubmetricValue();

    }

    /**
     * Calculates the total coverage sub-metric value based on the evaluation of each coverage type.
     *
     * @return The calculated total coverage sub-metric value.
     */
    public static double getCoverageSubmetricValue(){
        double totalCoverage = 0;
        double totalWeight = branchCoverageWeight + instructionCoverageWeight +
                lineCoverageWeight + methodCoverageWeight;

        /* calculate value for each coverage type */
        totalCoverage += branchCoverage * branchCoverageWeight;
        totalCoverage += instructionCoverage * instructionCoverageWeight;
        totalCoverage += lineCoverage * lineCoverageWeight;
        totalCoverage += methodCoverage * methodCoverageWeight;

        return totalWeight > 0 ? totalCoverage / totalWeight : 1;
    }



    /**
     * Resets variables of coverage submetric to be reused
     */
    public static void resetSubmetricVariables(){
        promptImprovementRemarks = new ArrayList<>();
        branchCoverage = 0;
        instructionCoverage = 0;
        lineCoverage = 0;
        methodCoverage = 0;

    }

    public static double getBranchCoverageWeight() {
        return branchCoverageWeight;
    }

    public static void setBranchCoverageWeight(double branchCoverageWeight) {
        CoverageSubmetric.branchCoverageWeight = branchCoverageWeight;
    }

    public static double getInstructionCoverageWeight() {
        return instructionCoverageWeight;
    }

    public static void setInstructionCoverageWeight(double instructionCoverageWeight) {
        CoverageSubmetric.instructionCoverageWeight = instructionCoverageWeight;
    }

    public static double getLineCoverageWeight() {
        return lineCoverageWeight;
    }

    public static void setLineCoverageWeight(double lineCoverageWeight) {
        CoverageSubmetric.lineCoverageWeight = lineCoverageWeight;
    }

    public static double getMethodCoverageWeight() {
        return methodCoverageWeight;
    }

    public static void setMethodCoverageWeight(double methodCoverageWeight) {
        CoverageSubmetric.methodCoverageWeight = methodCoverageWeight;
    }

    public static ArrayList<String> getPromptImprovementRemarks() {
        return promptImprovementRemarks;
    }

    public static double getBranchCoverage() {
        return branchCoverage;
    }

    public static double getInstructionCoverage() {
        return instructionCoverage;
    }

    public static double getLineCoverage() {
        return lineCoverage;
    }

    public static double getMethodCoverage() {
        return methodCoverage;
    }

}
