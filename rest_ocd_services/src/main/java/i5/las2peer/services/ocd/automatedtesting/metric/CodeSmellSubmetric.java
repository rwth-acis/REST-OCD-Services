package i5.las2peer.services.ocd.automatedtesting.metric;

import i5.las2peer.services.ocd.automatedtesting.OCDATestAutomationConstants;
import i5.las2peer.services.ocd.automatedtesting.helpers.*;
import i5.las2peer.services.ocd.automatedtesting.ocdaexecutor.GradleTaskExecutor;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers.HelperMethods;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static i5.las2peer.services.ocd.automatedtesting.OCDATestAutomationConstants.AUTO_GENERATED_TEST_CLASS_PACKAGE;
import static i5.las2peer.services.ocd.automatedtesting.ocdparser.PmdReportParser.parsePmdXmlReportForViolationsForClass;

public class CodeSmellSubmetric {


    /**
     * Value that determines from what number of code smell type presence will the submetric value become 0.
     */
    public static final int THRESHOLD_MAX_CODE_SMELL_TYPES = 10;


    /**
     * Ratio of lack of code smells. Value 1 represents no code smells detected.
     */
    private static double noCodeSmellRatio = 1.0;


    /**
     * List that holds instructions that will be used to generate the prompt for improving ChatGPT code
     */
    private static ArrayList<String> promptImprovementRemarks = new ArrayList<>();

    public static void main(String[] args) {

        System.out.println("submetric value is " + evaluateCodeSmellSubmetric("GeneratedSskAlgorithmTest.java"));

    }

    /**
     * Evaluates the code smell submetric for a given class based on its code smell data parsed from a PMD report.
     * This method calculates the submetric value depending on the different types of code smells present.
     * It also creates a list of code improvement instructions based on the identified code smells.
     * These instructions are intended for use in subsequent prompts to ChatGPT to improve the generated code.
     *
     * Important: this method should only be used after PMD report has been generated.
     *
     * @param ocdaTestClassName The name of the OCDA test class to be evaluated for code smells.
     * @return The calculated submetric value, which is a measure of code quality. The value is between 0 and 1,
     *         where 0 indicates a high number of diverse code smells and 1 indicates fewer or no code smells.
     */
    public static double evaluateCodeSmellSubmetric(String ocdaTestClassName){

        // generate pmd report to include newest tests
         GradleTaskExecutor.runPmdTest();

         HashMap<String, List<CodeSmellData>> ruleViolations
                = parsePmdXmlReportForViolationsForClass(OCDATestAutomationConstants.AUTO_GENERATED_TEST_CLASS_PACKAGE, ocdaTestClassName);


         /* number of code smell types that were detected in the algorithm code*/
         int detectedCodeSmellTypeCount = 0;

        ArrayList<String> untrackedSmells =  new ArrayList<String>(Arrays.asList("AvoidDuplicateLiterals","BeanMembersShouldSerialize", "AvoidCatchingThrowable", "UnnecessaryImport"));

        for (String codeSmellType : ruleViolations.keySet()){
            if (untrackedSmells.contains(codeSmellType)){
                continue;
            }
            detectedCodeSmellTypeCount++;

            // Add code smells to the prompt improvement remarks list to be used to improve future prompts to ChatGPT
            for (CodeSmellData codeSmellData : ruleViolations.get(codeSmellType)){
                promptImprovementRemarks.add("Code smell on line " + codeSmellData.getBeginLine() + ": " + codeSmellData.getDescription());

            }

        }


//        for (String pString : promptImprovementRemarks){
//            System.out.println(pString);
//        }


        if (detectedCodeSmellTypeCount >= THRESHOLD_MAX_CODE_SMELL_TYPES) {
            noCodeSmellRatio = 0;
        } else {
            noCodeSmellRatio = 1.0 - (double) detectedCodeSmellTypeCount / THRESHOLD_MAX_CODE_SMELL_TYPES;
        }

        return noCodeSmellRatio;
    }


    /**
     * Calculates the total Code smell sub-metric value based on the evaluation lack of code smells.
     *
     * @return The calculated total Code Smell sub-metric value.
     */
    public static double getCodeSmellSubmetricValue(){
         return noCodeSmellRatio;
    }


    /**
     * Resets variables of code smell submetric to be reused
     */
    public static void resetSubmetricVariables(){
        promptImprovementRemarks = new ArrayList<>();
        noCodeSmellRatio = 1;

    }

    public static ArrayList<String> getPromptImprovementRemarks() {
        return promptImprovementRemarks;
    }


    public static double getNoCodeSmellRatio() {
        return noCodeSmellRatio;
    }


}
