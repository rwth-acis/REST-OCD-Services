package i5.las2peer.services.ocd.automatedtesting.metric;

import i5.las2peer.services.ocd.automatedtesting.ocdparser.helpers.CodeSmellData;
import i5.las2peer.services.ocd.automatedtesting.ocdparser.helpers.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static i5.las2peer.services.ocd.automatedtesting.ocdparser.PmdReportParser.parsePmdXmlReportForViolationsForClass;

public class CodeSmellSubmetric {

    /* Value that determines from what number of code smell type presence will the submetric value become 0. */
    public static final int THRESHOLD_MAX_CODE_SMELL_TYPES = 10;

    /* List that holds instructions that will be used to generate the prompt for improving ChatGPT code */
    public static ArrayList<String> promptImprovementRemarks = new ArrayList<>();

    /* Number of code smells of different types detected in the analyzed code */
    public static int detectedCodeSmellTypeCount = 0;

    public static void main(String[] args) {

        System.out.println("submetric value is " + evaluateCodeSmellSubmetric("SskAlgorithmTest"));

    }

    /**
     * Evaluates the code smell submetric for a given class based on its code smell data parsed from a PMD report.
     * This method calculates the submetric value depending on the different types of code smells present.
     * It also creates a list of code improvement instructions based on the identified code smells.
     * These instructions are intended for use in subsequent prompts to ChatGPT to improve the generated code.
     *
     * @param ocdaTestClassName The name of the OCDA test class to be evaluated for code smells.
     * @return The calculated submetric value, which is a measure of code quality. The value is between 0 and 1,
     *         where 0 indicates a high number of diverse code smells and 1 indicates fewer or no code smells.
     */    public static double evaluateCodeSmellSubmetric(String ocdaTestClassName){
        HashMap<String, List<CodeSmellData>> ruleViolations
                = parsePmdXmlReportForViolationsForClass(ocdaTestClassName);

        /* number of code smell types that were detected in the algorithm code*/
        detectedCodeSmellTypeCount = ruleViolations.size();

        System.out.println("number of detected violation types " + detectedCodeSmellTypeCount);//TODO:DELETE
        for (String codeSmellType : ruleViolations.keySet()){

            String instructionString = "These following " + ruleViolations.get(codeSmellType).size() +" code smells of type " + codeSmellType + " should be fixed:\n";
            int cnt = 1;
            for (CodeSmellData codeSmellData : ruleViolations.get(codeSmellType)){
                instructionString += "   " + cnt  + ". on line " + codeSmellData.getBeginLine() + ": " + codeSmellData.getDescription() + "\n";
                cnt++;
            }

            promptImprovementRemarks.add(instructionString);
        }

        for (String pString : promptImprovementRemarks){
            System.out.println(pString);
        }

        double submetricValue;
        if (detectedCodeSmellTypeCount >= THRESHOLD_MAX_CODE_SMELL_TYPES) {
            submetricValue = 0;
        } else {
            submetricValue = 1.0 - (double) detectedCodeSmellTypeCount / THRESHOLD_MAX_CODE_SMELL_TYPES;
        }

        return submetricValue;
    }
}
