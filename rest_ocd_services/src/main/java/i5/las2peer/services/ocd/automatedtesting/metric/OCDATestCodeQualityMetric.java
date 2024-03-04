package i5.las2peer.services.ocd.automatedtesting.metric;

import i5.las2peer.services.ocd.automatedtesting.OCDATestAutomationConstants;
import i5.las2peer.services.ocd.automatedtesting.TestClassMerger;
import i5.las2peer.services.ocd.automatedtesting.helpers.FileHelpers;
import i5.las2peer.services.ocd.automatedtesting.helpers.OCDWriter;
import i5.las2peer.services.ocd.automatedtesting.helpers.PathResolver;
import i5.las2peer.services.ocd.automatedtesting.ocdparser.OCDAParser;

import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static i5.las2peer.services.ocd.automatedtesting.helpers.OCDWriter.generateAndWriteFile;

public class OCDATestCodeQualityMetric {

    /**
     * Weights of each necessary and desirable submetric that determine their importance
     * in the overal metric value calculation
     */
    public static double ocdSubmetricWeight = 1.0;
    public static double codeModificationSubmetricWeight = 1.0;
    public static double coverageSubmetricWeight = 1.0;
    public static double codeSmellSubmetricWeight = 1.0;

    /**
     * Whether to include desirable submetrics in the report
     */
    public static boolean includeCodeSmellSubmetric = false;
    public static boolean includeCoverageSubmetric = false;




    public static void main(String[] args) {


        // Output of GPT before it was parsed
        File gptOutput = new File(FileHelpers.getAutoGeneratedUnparsedTestPath("GeneratedSskAlgorithmTest.java"));

        // Main OCDA test class, where the new tests shoud be merged
        File mainOCDATestClass = new File (FileHelpers.getOCDATestPath("SskAlgorithmTest.java"));

        // This should be the test class file that was given as input to GPT that held partially completed unit tests
        File gptInput = new File(FileHelpers.getAutoGeneratedUnparsedTestPath("SskAlgorithmTest.java"));

        File ocdaCode = new File(FileHelpers.getOCDAPath("SskAlgorithm.java"));

        String ocdaName = "SskAlgorithm";
        //--------------------


        /* Evaluate submetrics */


        // Evaluate sub-metrics that check if the auto-generated code can be parsed and executed
        evaluateEssentialSubmetrics(ocdaName, gptOutput);

        // If code auto-generated code is valid, continue with other sub-metrics
        if (CodeValiditySubmetric.isNoParsingErrorFound() && CodeValiditySubmetric.isNoRuntimeErrorFound()) {

            // Note: here the main test class is evaluated, where the valid auto-generated code was parsed
            evaluateNecessarySubmetrics(mainOCDATestClass,gptInput,ocdaCode);
            evaluateDesirablesubmetrics(mainOCDATestClass,gptInput,ocdaCode);
        }


        /* Generate a report based on submetric evaluation */
        String qualityReport = generateAndWriteQualityReport(ocdaName, true, "");
        System.out.println(qualityReport);


    }



    /**
     * Generates and returns a quality report in Markdown format detailing the total metric value and individual
     * submetric evaluations. The report includes essential information about each submetric, such as code validity,
     * OCD submetrics, code modification, and coverage metrics, as well as their individual contributions to the total
     * metric value.
     * If desired, the report can also include issues identified during the evaluation process.
     *
     * @param ocdaName The name of the OCD algorithm being tested.
     * @param includeIssues Flag indicating whether to include identified issues in the report.
     * @param PathToFile    String representation of the file path of the evaluated code.
     *                      If non-empty it will be included in the report.
     * @return A Markdown formatted string representation of the quality report.
     */
    public static String generateAndWriteQualityReport(String ocdaName, boolean includeIssues, String PathToFile) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("### Automated Test Generation Quality Report For `").append(ocdaName).append("`").append("\n\n");

        // Essential submetrics
        stringBuilder.append("#### Code Validity Submetric\n");
        stringBuilder.append("* No Parsing errors found: `").append(CodeValiditySubmetric.isNoParsingErrorFound() ? "true" : "false").append("`").append("\n");
        stringBuilder.append("* No Runtime errors found: `").append(CodeValiditySubmetric.isNoRuntimeErrorFound() ? "true" : "false").append("`").append("\n\n");


        if (CodeValiditySubmetric.isNoParsingErrorFound() && CodeValiditySubmetric.isNoRuntimeErrorFound()) {
            // OCD Sub-metric
            stringBuilder.append("#### OCD Submetric = " + roundToDecimalPlaces(OCDSubmetric.getOCDSumetricValue(),2) + "\n");
            stringBuilder.append("```text\n"); // Start code block to maintain formatting
            stringBuilder.append("  Ratio of compatible graph types tested: ").append(roundToDecimalPlaces(OCDSubmetric.getCompatibleGraphTypeTestRatio(),2)).append("\n");
            stringBuilder.append("  Ratio of algorithm parameters used in tests: ").append(roundToDecimalPlaces(OCDSubmetric.getAlgorithmParameterUsageRatio(),2)).append("\n");
            stringBuilder.append("```\n\n"); // End code block

            // Code Modification Sub-metric
            stringBuilder.append("#### Code Modification Submetric = " + roundToDecimalPlaces(CodeModificationSubmetric.getCodeModificationSubmetricValue(),2)  + " \n");
            stringBuilder.append("```text\n"); // Start code block to maintain formatting
            stringBuilder.append("  Ratio of auto-generated tests annotated as auto-generated: ").append(roundToDecimalPlaces(CodeModificationSubmetric.getAutogeneratedTestAnnotationRatio(),2)).append("\n");
            stringBuilder.append("  Ratio of lines that were correctly left unmodified:          ").append(roundToDecimalPlaces(CodeModificationSubmetric.getCorrectlyUnmodifiedMethodLineRatio(),2)).append("\n");
            stringBuilder.append("```\n\n"); // End code block

            // Coverage Sub-metric
            if (includeCoverageSubmetric) {
                stringBuilder.append("#### Coverage Submetric = " + roundToDecimalPlaces(CoverageSubmetric.getCoverageSubmetricValue(), 2) + "\n");
                stringBuilder.append("```text\n"); // Start code block to maintain formatting
                stringBuilder.append("  Method coverage: ").append(roundToDecimalPlaces(CoverageSubmetric.getMethodCoverage(), 2)).append("\n");
                stringBuilder.append("  Branch coverage: ").append(roundToDecimalPlaces(CoverageSubmetric.getBranchCoverage(), 2)).append("\n");
                stringBuilder.append("  Line coverage: ").append(roundToDecimalPlaces(CoverageSubmetric.getLineCoverage(), 2)).append("\n");
                stringBuilder.append("  Instruction coverage: ").append(roundToDecimalPlaces(CoverageSubmetric.getInstructionCoverage(), 2)).append("\n");
                stringBuilder.append("```\n\n"); // End code block
            }

            // Code Smell Sub-metric
            if (OCDATestCodeQualityMetric.includeCodeSmellSubmetric) {
                stringBuilder.append("#### Code Smell Submetric = " +roundToDecimalPlaces(CodeSmellSubmetric.getCodeSmellSubmetricValue(),2) + "\n");
                stringBuilder.append("```text\n"); // Start code block to maintain formatting
                stringBuilder.append("  Ratio representing lack of code smells: ").append(CodeSmellSubmetric.getNoCodeSmellRatio()).append("\n");
                stringBuilder.append("```\n\n"); // End code block

            }

            stringBuilder.append("### Total Metric Value = " + roundToDecimalPlaces(getTotalMetricValue(),2) + "\n");
        } else {
            // Auto-generated code is invalid
            stringBuilder.append("**Auto-generated code is invalid: Total Metric Value Based On Sub-Metrics:** ").append(roundToDecimalPlaces(getTotalMetricValue(),2)).append("\n");
        }

        if (includeIssues) {
            stringBuilder.append("### Issues Negatively Affecting Metric Value\n");
            LinkedHashMap<String, List<String>> issuesList = getPromptImprovementInstructions();
            for (Map.Entry<String, List<String>> entry : issuesList.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();

                if (!OCDATestCodeQualityMetric.includeCodeSmellSubmetric && CodeSmellSubmetric.class.getSimpleName().equals(key)) {
                    continue;
                }

                stringBuilder.append("#### ").append(key).append(" Issues\n");
                if (values.isEmpty()) {
                    stringBuilder.append("    None\n\n"); // Indent "None" if the issues list is empty
                } else {
                    for (String value : values) {
                        stringBuilder.append("     ").append(value).append("\n"); // Indent each bullet point
                    }
                    stringBuilder.append("\n"); // Add a new line for spacing after listing all issues
                }
            }
        }



//        if (!PathToFile.equals("")) {
//            stringBuilder.append("## Evaluated Code Location\n");
//            stringBuilder.append("`").append(PathToFile).append("`").append("\n");
//        }

        // Write report about automated testing quality results for the specified OCD algorithm
        generateAndWriteFile(PathResolver.addProjectRootPathIfSet("gpt/reports/") +ocdaName
                +"_automated_testing_report.md", stringBuilder.toString(),false);

        // Add report to the GPT communication log
        OCDWriter.logGptCommunication(stringBuilder.toString(), false, ocdaName, "GPT Code Evaluation Report");

        return stringBuilder.toString();
    }




    /**
     * Evaluates essential submetrics to determine whether the auto-generated code is parsable and executable.
     * This method first checks if the code generated by GPT can be parsed. If parsing is successful, the method
     * then moves the parsed file to the test source set for execution. If the execution is successful, it merges
     * the auto-generated unit tests with the main test class of the specified OCD algorithm.
     *
     * @param ocdaName The name of the OCD algorithm for which the tests are generated.
     * @param gptOutput The file containing the output generated by GPT.
     * @return A list containing any issues regarding prompt validity sub-metric.
     */
    public static List<String> evaluateEssentialSubmetrics(String ocdaName, File gptOutput){

        // Evaluate whether the file outputted by GPT is parsable
        CodeValiditySubmetric.evaluateIsCodeParsable(gptOutput);


        // Class name of the test class holding auto-generated tests
        String autoGeneratedOCDATestClassName = "Generated" + ocdaName + "Test";

        // If parsing was successful move the parsed file to a test source set so that it can be executed as a test
        if (CodeValiditySubmetric.isNoParsingErrorFound()){
            System.out.println("No parsing error found. Moving file to test source root to be executed...");
            String from = OCDATestAutomationConstants.GPT_GENERATED_TEST_CLASS_CODE_LOCATION + autoGeneratedOCDATestClassName + ".java";
            String to = OCDATestAutomationConstants.PARSED_GPT_GENERATED_CODE_LOCATION + autoGeneratedOCDATestClassName + ".java";
            FileHelpers.moveFile(from, to, true);


            // Evaluate if the test class generated by GPT can successfully be executed
            CodeValiditySubmetric.evaluateIsCodeRunnable(autoGeneratedOCDATestClassName);

            // If auto-generated tests didn't cause parsing or runtime errors, merge the new tests
            // into the main test class of the algorithm
            if (CodeValiditySubmetric.isNoRuntimeErrorFound()) {



                // Test class file into which the newly auto-generated tests should be merged
                File ocdaTestClass = new File(FileHelpers.getOCDATestPath(ocdaName + "Test.java"));

                // Test class file holding newly auto-generated tests that passed validation checks
                File autoGeneratedTests = new File(FileHelpers.getAutoGeneratedTestPath(autoGeneratedOCDATestClassName  + ".java"));

                System.out.println("No parsing and runtime errors found: "
                        + (CodeValiditySubmetric.isNoParsingErrorFound() && CodeValiditySubmetric.isNoRuntimeErrorFound())
                        + ". Merging auto-generated tests into " + ocdaName + "Test.java" + "...");


                // Merge unit tests from the test class holding newly generated tests into the main test class of the
                // OCD algorithm.
                TestClassMerger.mergeTestClasses(ocdaTestClass, autoGeneratedTests);

                // Fix comments in the lines that shouldn't be modified by ChatGPT. This is needed due to inline
                // comments getting moved to a line above when java parser is used for unit test extraction
                TestClassMerger.processComments(ocdaTestClass);


                // Delete auto-generated test file, after the tests have been merged into the main test file
                //autoGeneratedTests.delete();


            } else {
                // Runtime errors found, log them to be used in the next prompt
                System.out.println("Runtime Errors Found!");
                //CodeValiditySubmetric.getPromptImprovementRemarks().forEach(s -> System.out.println(s)); //TODO:DELETE
            }



        } else {
            System.out.println("Parsing Errors Found!");
            //CodeValiditySubmetric.getPromptImprovementRemarks().forEach(s -> System.out.println(s)); //TODO:DELETE
        }


        return CodeValiditySubmetric.getPromptImprovementRemarks();

    }

    /**
     * Evaluates necessary sub-metrics to assess. This method performs a series of evaluations on the given files to
     * determine metrics like the ratio of compatible graph types tested, the coverage of algorithm parameters
     * in unit tests, and modifications  in code comments and lines marked as not to be removed.
     *
     * @param gptOutputParsed the file containing the parsed output from GPT.
     * @param gptInput the file containing the initial input provided to GPT.
     * @param ocdaCode the file containing the OCD algorithm code to be evaluated.
     *
     * The method performs the following evaluations:
     * 1. Evaluates the ratio of compatible graph types of the OCD algorithm that are tested in the test class
     *    of the algorithm.
     * 2. Evaluates the ratio of algorithm parameters that were adequately covered in the unit tests of the
     *    OCD algorithm test class.
     * 3. Evaluates the ratio of Javadoc comments indicating auto-generated/completed code that were removed
     *    or altered.
     * 4. Evaluates the ratio of lines that were marked as not to be removed but were removed, among all marked lines.
     */
    public static void evaluateNecessarySubmetrics(File gptOutputParsed, File gptInput, File ocdaCode){

        // Evaluate ratio of compatible graphs types that are tested in the test class of the algorithm as
        // described in the initial prompt given to ChatGPT
        OCDSubmetric.evaluateCompatibleGraphTypeTestRaio(gptOutputParsed, ocdaCode);

        // Evaluate ratio of algorithm parameters that were adequately covered in the unit tests of the OCDA test
        // class as described in the initial prompt given to ChatGPT.
        OCDSubmetric.evaluateAlgorithmParameterUsageRatio(gptOutputParsed, ocdaCode);

        // Evaluate ratio of javadoc comments indicating auto-generated/completed code that were removed/altered
        CodeModificationSubmetric.evaluateMethodCommentModification(gptOutputParsed, gptInput);

        // Evaluate the ratio of lines that were marked as not to be removed but were removed (among all  marked lines)
        CodeModificationSubmetric.evaluateLineModificationInTests(gptOutputParsed, gptInput);


    }


    /**
     * Evaluates desirable submetrics to assess additional quality attributes of the code and tests.
     * This method focuses on optional or 'nice-to-have' metrics, which contribute to the overall
     * quality but are not strictly necessary for compliance.
     *
     * @param gptOutputParsed the file containing the parsed output from GPT.
     * @param gptInput the file containing the initial input provided to GPT.
     * @param ocdaCode the file containing the OCD algorithm code to be evaluated.
     *
     * The method performs the following evaluations:
     * 1. Extracts code coverage values from the JaCoCo report. This submetric is used for code quality
     *    report generation and does not contribute to subsequent prompts.
     * 2. Evaluates code smell submetrics for the test class of the OCDA code to assess potential issues
     *    in the code structure or implementation that could affect maintainability.
     */
    private static void evaluateDesirablesubmetrics(File gptOutputParsed, File gptInput, File ocdaCode){

        // Extract code coverage values from the JaCoCo report. Note that this submetric does not
        // contribute to subsequent prompts. It is however used for code quality report generation
        CoverageSubmetric.evaluateCoverageSubmetric(OCDAParser.getClassName(ocdaCode));


        CodeSmellSubmetric.evaluateCodeSmellSubmetric(OCDAParser.getClassName(ocdaCode) + "Test");
    }



    /**
     * Collects and returns prompt improvement instructions based on the evaluations from various sub-metrics.
     * This method aggregates remarks from different sub-metrics to guide the generation of subsequent prompts
     * used as input to GPT.
     *
     * @return A LinkedHashMap where each key is a sub-metric class name and the value is a list of strings
     *         representing improvement remarks for that sub-metric.
     */
    public static LinkedHashMap<String, List<String>> getPromptImprovementInstructions(){
        LinkedHashMap<String, List<String>> promptImprovementInstructions = new LinkedHashMap<>();

        promptImprovementInstructions.put(CodeValiditySubmetric.class.getSimpleName(), CodeValiditySubmetric.getPromptImprovementRemarks());
        promptImprovementInstructions.put(OCDSubmetric.class.getSimpleName(), OCDSubmetric.getPromptImprovementRemarks());
        promptImprovementInstructions.put(CodeModificationSubmetric.class.getSimpleName(), CodeModificationSubmetric.getPromptImprovementRemarks());
        promptImprovementInstructions.put(CodeSmellSubmetric.class.getSimpleName(), CodeSmellSubmetric.getPromptImprovementRemarks());

        return promptImprovementInstructions;

    }


    /**
     * Calculates the total metric value based on evaluated submetrics and their respective weights.
     * This method considers various submetrics like OCD submetric, code modification, and coverage. It also includes
     * code smell submetric if flagged to be included. The method returns 0 if any parsing or runtime errors
     * are found (i.e. if code validity submetric fails), indicating that the auto-generated code needs fixing.
     *
     * @return The weighted average sum of the evaluated sub-metrics, or 0 if any parsing or runtime errors are found.
     */
    public static double getTotalMetricValue(){

        if (CodeValiditySubmetric.isNoRuntimeErrorFound() == false
                || CodeValiditySubmetric.isNoRuntimeErrorFound() == false) {
            return 0;
        }

        // Sum of weights of each submetric that are included in the total metric calculation
        double submetricWeightSums = ocdSubmetricWeight + codeModificationSubmetricWeight;

        // Weighted sum of the evaluated submetric values
        double submetricValueWeightedSums =
                OCDSubmetric.getOCDSumetricValue() * ocdSubmetricWeight +
                CodeModificationSubmetric.getCodeModificationSubmetricValue() * codeModificationSubmetricWeight;



        if (includeCoverageSubmetric) {
            submetricWeightSums += coverageSubmetricWeight;
            submetricValueWeightedSums += CoverageSubmetric.getCoverageSubmetricValue() * coverageSubmetricWeight;
        }

        // Add code smell sbumetric if it should be included in the report
        if (includeCodeSmellSubmetric) {
            submetricWeightSums += codeSmellSubmetricWeight;
            submetricValueWeightedSums += CodeSmellSubmetric.getCodeSmellSubmetricValue() * codeSmellSubmetricWeight;
        }


        // If parsing and execution of the generated code didn't fail, the weighted average sum of the sub-metrics is
        // returned. Otherwise, 0 is returned since this implies the auto-generated code is either not parsable
        // or not executable and should be fixed
        if (CodeValiditySubmetric.isNoParsingErrorFound() && CodeValiditySubmetric.isNoRuntimeErrorFound()) {
            return (submetricValueWeightedSums / submetricWeightSums);
        } else {
            return 0;
        }

    }


    /**
     * Rounds a given number to a specified number of decimal places.
     * The method throws an IllegalArgumentException if the number of decimal places is negative.
     *
     * @param number The number to be rounded.
     * @param decimalPlaces The number of decimal places to round the number to.
     * @return The number rounded to the specified number of decimal places.
     */
    public static double roundToDecimalPlaces(double number, int decimalPlaces) {
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("Decimal places must be non-negative");
        }

        BigDecimal bd = BigDecimal.valueOf(number);
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
