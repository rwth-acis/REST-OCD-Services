package i5.las2peer.services.ocd.automatedtesting.metric;

import i5.las2peer.services.ocd.automatedtesting.ocdparser.OCDAParser;
import i5.las2peer.services.ocd.automatedtesting.ocdparser.PromptGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OCDSubmetric {

    //TODO: add variables for each part of this submetric

    /**
     * Represents whether each compatible graph type is tested in the test class of the algorithm.
     * Initially this value is 1. Each missing test will reduce the value.
     */
    public static double compatibleGraphTypeTestRatio = 1;

    /**
     * Represents ratio of OCDA parameters that are used within the OCDA test class. Ideally this should be 1,
     * which means every parameter is used.
     */
    public static double algorithmParameterUsageRatio = 1;

    /**
     * List that holds instructions that will be used to generate the prompt for improving ChatGPT code
     */
    public static ArrayList<String> promptImprovementRemarks = new ArrayList<>();


    public static void main(String[] args) {
        System.out.println("Inside OcdSubMetric: ");

        File gptInput = new File(OCDAParser.getOCDATestPath("SskAlgorithmTest1.java"));
        //File gptOutput = new File(OCDAParser.getOCDATestPath("SskAlgorithmTest.java"));
        File gptOutput = new File("someFile.java");

        File ocdaCode = new File(OCDAParser.getOCDAPath("SskAlgorithm.java"));

       evaluateCompatibleGraphTesting(gptOutput,ocdaCode);
       evaluateAlgorithmParameterTesting(gptOutput, ocdaCode);


        System.out.println("======= PROMPT IMPROVEMENT REMARKS ======");

        for (String remark : promptImprovementRemarks){
            System.out.println(remark);
        }
        System.out.println("\n\nfinal scores: compatibleGraphTypeTestRatio="+compatibleGraphTypeTestRatio + " | algorithmParameterUsageRatio="+algorithmParameterUsageRatio );

    }

    /**
     * Evaluates if all algorithm parameters were adequately covered in the unit tests of the OCDA test class.
     * This method checks whether each algorithm parameter is set in the auto-generated unit tests for each compatible
     * graph type. The submetric value is calculated based on the coverage of these parameters, with a value of 1
     * indicating complete coverage. If any parameters are missing in the tests, the method also generates instructions
     * for subsequent prompts to ChatGPT to include these missing parameters in the unit tests.
     *
     * @param gptOutput The file containing the generated test class by ChatGPT.
     * @param ocdaCode The file containing the OCDA algorithm code.
     * @return A double value representing the submetric of parameter coverage in the unit tests.
     *         A value of 1 indicates that all parameters were set in all auto-generated unit tests,
     *         and the value decreases as the number of missed parameters increases.
     */
    public static double evaluateAlgorithmParameterTesting(File gptOutput, File ocdaCode){
        // Get all variable declarations in the OCDA class file
        List<String> variableDeclarations = OCDAParser.listClassVariables(ocdaCode);

        // Extract variable names from the variable declarations of the OCDA class
        List<String> algorithmParameterNames = extractParameterNamesFromDeclarations(OCDAParser.listClassVariables(ocdaCode));

        // Compatible graph types of the algorithm
        List<String> compatibleGraphTypes = OCDAParser.extractCompatibilitiesAndAddUndirectedGraphType(ocdaCode);


        // Number of algorithm parameters that were not set in the unit tests
        int missedAlgorithmParameterCount = 0;

        // Map to associate each compatibleGraphType with its corresponding test name
        Map<String, String> graphTypeToTestNameMap = getGraphTypeBasedTestNames();

        // Check if each algorithm variable name is present in a unit test for each graph type. This is needed
        // because every algorithm variable should be set by each unit test created by ChatGPT.
        for (String compatibleGraphType : compatibleGraphTypes){
            System.out.println("------------- LOOKING AT GRAPH TYPE " + compatibleGraphType + " ------------------"); //TODO:DELETE

            String graphTypeTestName = graphTypeToTestNameMap.get(compatibleGraphType);

            if (graphTypeTestName != null) {

                // method calls in unit test for each graph type.
                ArrayList<String> methodCallsInTest = OCDAParser.listMethodCallsInMethod(gptOutput, graphTypeTestName, false);

                // Filter method calls to only include method calls related to setting OCDA parameters
                List<String> filteredMethodCalls = extractParameterRelatedMethodCalls(methodCallsInTest);
                System.out.println("filtered method calls " + filteredMethodCalls); //TODO:DELETE

                for (String algorithmParameterName : algorithmParameterNames) {

                    // whether each method call include setting the specified algorithm parameter
                    boolean unitTestContainsOCDAParameter = filteredMethodCalls.stream()
                            .anyMatch(unitTestParameterSetting -> unitTestParameterSetting.contains(algorithmParameterName));

                    if (!unitTestContainsOCDAParameter) {
                        //System.out.println("Missing algorithm parameter: unit test " + graphTypeTestName + " must set algorithm parameter " + algorithmParameterName);//TODO:DELETE
                        promptImprovementRemarks.add("Missing algorithm parameter: unit test " + graphTypeTestName + " must set algorithm parameter " + algorithmParameterName);
                        missedAlgorithmParameterCount += 1;
                    }

                    System.out.println(algorithmParameterName + "? " + unitTestContainsOCDAParameter);//TODO:DELETE
                }
            }
        }




        /* Sub-metric value calculation */
        int compatibleGraphTypeCount = compatibleGraphTypes.size();

        // Each unit test should set all parameters. For each missing parameter set, the submetric value reduces
        algorithmParameterUsageRatio = 1 - (double)missedAlgorithmParameterCount/ (compatibleGraphTypeCount * algorithmParameterNames.size());
        // System.out.println("compatible graph types " + compatibleGraphTypeCount  + " | algorithm parameter count " + algorithmParameterNames.size()+ " | missed parameters " + missedAlgorithmParameterCount+ " | submetric value " + submetricValue);

        return algorithmParameterUsageRatio;
    }


    /**
     * Creates and returns a mapping of graph types to their corresponding unit test names.
     * This mapping is intended for use in generating tests to be completed by ChatGPT.
     * The method also adds an artificial UNDIRECTED graph type entry, as each Overlapping
     * Community Detection Algorithm (OCDA) test class in WebOCD is expected to have a
     * corresponding test for undirected graphs.
     *
     * The method populates a Map where each key is a specific graph type (e.g., DIRECTED, WEIGHTED) and each value
     * is the name of the unit test associated with that graph type.
     *
     * @return A Map where each key is a string representing a graph type, and each value is a string
     *         representing the corresponding unit test name that needs to be completed.
     */
    public static Map<String, String>  getGraphTypeBasedTestNames(){

        Map<String, String> graphTypeToTestNameMap = new HashMap<>();

        graphTypeToTestNameMap.put("GraphType.DIRECTED", PromptGenerator.DIRECTED_GRAPH_TEST_NAME);
        graphTypeToTestNameMap.put("GraphType.WEIGHTED", PromptGenerator.WEIGHTED_GRAPH_TEST_NAME);
        graphTypeToTestNameMap.put("GraphType.NEGATIVE_WEIGHTS", PromptGenerator.NEGATIVE_WEIGHTS_GRAPH_TEST_NAME);
        graphTypeToTestNameMap.put("GraphType.SELF_LOOPS", PromptGenerator.SELF_LOOPS_GRAPH_TEST_NAME);
        graphTypeToTestNameMap.put("GraphType.ZERO_WEIGHTS", PromptGenerator.ZERO_WEIGHTS_GRAPH_TEST_NAME);

        // While undirected graph doesn't exist as a separate GraphType, each OCDA is compatible with undirected graphs.
        graphTypeToTestNameMap.put("GraphType.UNDIRECTED", PromptGenerator.UNDIRECTED_GRAPH_TEST_NAME);

        return graphTypeToTestNameMap;
    }


    /**
     * Extracts parameter names from a list of (class) variable declarations (e.g. in the OCDA).
     * This method filters out any variables declared as 'final String', assuming they are constants,
     * and retains other algorithm variables. It then extracts the names of these variables for further processing.
     *
     * The method uses regular expressions to identify and extract the variable names from the declarations.
     * It considers a variable name to be the word preceding either an '=' sign or a semicolon (';').
     * This is under the assumption that the variable declarations follow standard Java syntax.
     *
     * @param variableDeclarations A list of String representations of variable declarations.
     * @return A list of algorithm variable names extracted from the provided variable declarations.
     */
    public static List<String> extractParameterNamesFromDeclarations(List<String> variableDeclarations){

        // Filter the variable declaration list to remove any variables that are declared as final String. This
        // should remove constants and only keep the algorithm variables that should be set within a test.
        List<String> filteredVariableDeclarations = variableDeclarations.stream()
                .filter(s -> !s.contains("final String"))
                .collect(Collectors.toList());

        // Extract OCDA variable names
        List<String> algorithmVariableNames = filteredVariableDeclarations.stream()
                .map(s -> {
                    Matcher m = Pattern.compile("\\b(\\w+)(\\s*=|;)").matcher(s);
                    return m.find() ? m.group(1) : "";
                })
                .collect(Collectors.toList());

        return algorithmVariableNames;
    }


    /**
     * Extracts method calls from a unit test that are related to setting Overlapping Community Detection (OCDA)
     * algorithm parameters. This method filters a list of method calls to include only those that are involved
     * in setting algorithm parameters, specifically looking for calls to 'parameters.put'. It assumes that
     * setting an algorithm parameter involves a method call to 'parameters.put' with relevant arguments.
     *
     * @param methodCallsInTest A list of method call strings extracted from a unit test.
     * @return A filtered list of method calls that are related to setting OCDA algorithm parameters.
     *         Each element in the list represents a method call that includes 'parameters.put'.
     */
    public static List<String> extractParameterRelatedMethodCalls(List<String> methodCallsInTest){

        // Filter the method call list to only include the parts concerning setting algorithm parameter values
        List<String> filteredMethodCalls = methodCallsInTest.stream()
                .filter(s -> s.contains("parameters.put"))
                .collect(Collectors.toList());

        return filteredMethodCalls;
    }



    /**
     * Computes the part of the submetric value that checks whether all compatible graphs are tested in the
     * test class of the algorithm. Calculation is based on the number of missing tests and the total
     * compatibility count. This method calculates the proportion of tests not missing and returns a value
     * between 0 and 1. A result of 1 indicates no missing tests, and the value decreases linearly as the number
     * of missing tests increases. This method shouldn't be called with totalCompatibilityCount=0
     * since all OCDA should be compatible with undirected graphs.
     *
     * Additionally, instructions are generated that can be used for subsequent prompts to ChatGPT in order
     * to fix the missing compatible graph type tests (if any).
     *
     * @param gptOutput File that holds the test class generated by ChatGPT.
     * @param ocdaCode File that holds the OCD algorithm code.tests.
     * @return The submetric value, representing the proportion of tests not missing.
     */
    public static double evaluateCompatibleGraphTesting(File gptOutput, File ocdaCode) {
        // List of methods in the test class for the OCDA. This list is used to ensure no method is missing
        // e.g. due to existing methods being removed by ChatGPT
        List<String> ocdaTestClassMethods = OCDAParser.extractMethods(gptOutput);

        // Get compatible graph types for the OCDA together with an artificial UNDIRECTED graph compatibility
        List<String> compatibleGraphTypes = OCDAParser.extractCompatibilitiesAndAddUndirectedGraphType(ocdaCode);

        // Map to associate each compatibleGraphType with its corresponding test name
        Map<String, String> graphTypeToTestNameMap = getGraphTypeBasedTestNames();

        // variables needed for sub-metric value calculation
        int totalCompatibilityCount = compatibleGraphTypes.size();
        int missingTests = 0;

        /* Check if there is a test for each compatible graph type */
        // Check for each compatible graph type
        for (String compatibleGraphType : compatibleGraphTypes) {
            System.out.println(compatibleGraphType);

            String testMethodName = graphTypeToTestNameMap.get(compatibleGraphType);
            if (testMethodName != null && !ocdaTestClassMethods.contains(testMethodName)) {
                addMissingTestToPrompt(testMethodName);
                missingTests++;
            }
        }


        // Sub-metric value calculation

        compatibleGraphTypeTestRatio = (1 - ((double) missingTests / totalCompatibilityCount));

        //System.out.println("\n\nmissing tests=" + missingTests + "\ntotalCompatibilityCount=" + totalCompatibilityCount + "\npromptImprovementRemarks:" + promptImprovementRemarks + "\ncalculatedValue=" + compatibleGraphTypeTestRatio);//TODO:DELETE

        return compatibleGraphTypeTestRatio;

    }



    /**
     * Add missing test information to the list of instructions which will be used to generate a prompt
     * to improve the code generated by ChatGPT.
     * @param testName          Name of the test that is missing.
     */
    public static void addMissingTestToPrompt(String testName){
        promptImprovementRemarks.add("Missing unit test: unit test " +  testName
                + " was removed, even though it must be in the test class.");
    }

    /**
     * Resets variables of this submetric to be reused
     */
    public void resetOCDSubmetricVariables(){
        promptImprovementRemarks = new ArrayList<>();
        compatibleGraphTypeTestRatio = 1;
    }


    /**
     * Checks if the class declaration remains unmodified between two Java class files.
     * This method can be used to compare if the class declaration in a file given as a prompt input was modified
     * by ChatGPT. It compares the full class declarations (including modifiers, class name, and any extended or
     * implemented interfaces) of the same class in two different files.
     *
     * @param gptInput  The original Java class file to compare. Typically, this would be the file given as input
     *                  to ChatGPT.
     * @param gptOutput The modified Java class file to compare. This would be the file after processing or
     *                  modification by ChatGPT.
     * @return          Returns true if the full class declaration in the gptOutput file is exactly the same as
     *                  in the gptInput file, indicating that the class declaration remains unmodified.
     *                  Returns false if there are any differences in the class declarations.
     */
    public static Boolean isClassDeclarationUnmodified(File gptInput, File gptOutput) {
        return OCDAParser.getFullClassDeclaration(gptInput).equals(OCDAParser.getFullClassDeclaration(gptOutput));
    }


    /**
     * Checks if the comment associated with a specified method remains unmodified between two Java class files.
     * This method can be used to compare if the comment of a method given as a prompt input was modified
     * by ChatGPT. In this case, input would be the method that was part of the partially completed code prompt,
     * while the output would be the method that was part of the ChatGPT output.
     * This comparison ignores differences in whitespace (tabs, spaces).
     *
     * @param gptInput       The original Java class file to compare.
     * @param gptOutput      The modified Java class file to compare.
     * @param targetMethod   The name of the method whose comments are being compared.
     * @return               Returns true if the comment in the target method of the gptOutput file, when normalized for whitespace,
     *                       contains the comment from the corresponding method in the gptInput file, also normalized for whitespace.
     *                       This indicates that the comment text remains unmodified or includes the original. Returns false otherwise.
     */
    public static Boolean isMethodCommentTextUnmodified(File gptInput, File gptOutput, String targetMethod) {
        String inputComment = OCDAParser.getMethodComment(gptInput, targetMethod).replaceAll("\\s+", " ").trim();
        String outputComment = OCDAParser.getMethodComment(gptOutput, targetMethod).replaceAll("\\s+", " ").trim();
        return outputComment.contains(inputComment);
    }







}
