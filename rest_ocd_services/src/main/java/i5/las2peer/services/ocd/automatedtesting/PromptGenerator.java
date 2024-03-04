package i5.las2peer.services.ocd.automatedtesting;

import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.automatedtesting.helpers.*;
import i5.las2peer.services.ocd.automatedtesting.ocdaexecutor.GradleTaskExecutor;
import i5.las2peer.services.ocd.automatedtesting.ocdparser.OCDAParser;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;


import java.io.File;
import java.util.*;
import org.json.simple.JSONObject;

import static i5.las2peer.services.ocd.automatedtesting.OCDATestAutomationConstants.*;
import static i5.las2peer.services.ocd.automatedtesting.helpers.FormattingHelpers.*;
import static i5.las2peer.services.ocd.automatedtesting.helpers.OCDWriter.generateAndWriteFile;
import static i5.las2peer.services.ocd.automatedtesting.metric.OCDSubmetric.extractParameterNamesFromDeclarations;
import static i5.las2peer.services.ocd.automatedtesting.ocdparser.OCDAParser.*;

public class PromptGenerator {




    public static void main(String[] args) {
        if (args.length > 0) {
            String ocdaName = args[1];
            String ocdaCodePath = FileHelpers.cleanDuplicateDirectories(FileHelpers.getOCDAPath(ocdaName + ".java"));
            File ocdaCode = new File(ocdaCodePath);
            switch (args[0]) {
                case "generateAndWriteGraphTypeRelatedTestPrompt":
                    if (args.length > 1) {
                        String result = generateAndWriteGraphTypeRelatedTestPrompt(ocdaCode);
                        System.out.println(result);
                    } else {
                        System.out.println("OCDA name not provided for Graph Type Related Test Prompt.");
                    }
                    break;
                case "generateAndWriteOCDAParameterGenerationPrompt":
                    if (args.length > 1) {
                        String result = generateAndWriteOCDAParameterGenerationPrompt(ocdaCode);
                        System.out.println(result);
                    } else {
                        System.out.println("OCDA name not provided for OCDA Parameter Generation Prompt.");
                    }
                    break;
                case "runGenerateAndWriteOCDAMethodTestPrompt":
                    if (args.length > 2) { // Expecting at least one method name
                        List<String> methodNames = Arrays.asList(args).subList(2, args.length);
                        String result = generateAndWriteOCDAMethodTestPromptForGPTAPI(ocdaName, methodNames);
                       // System.out.println(result);
                    } else {
                        System.out.println("OCDA name or method names not provided for GPT API Method Test Prompt.");
                    }
                    break;
                default:
                    System.out.println("Invalid argument: " + args[0]);
                    break;
            }
        } else {
            System.out.println("No arguments provided.");
        }
    }


    /**
     * Generates a list of import statements for the test class of the OCD algorithm.
     * The import statements are tailored based on the provided OCD algorithm name and its compatibilities.
     *
     * @param ocdaCode The OCD algorithm class code for which the test class is being prepared.
     * @return A List of strings, each representing an import statement for the test class.
     */
    public static List<String> generateTestClassImports(File ocdaCode){

        String ocdaName = OCDAParser.getClassName(ocdaCode);
        List<String> compatibilities = OCDAParser.extractCompatibilities(ocdaCode);
        String multiLineString =
                "import static org.junit.jupiter.api.Assertions.*;\n" +
                "import org.junit.jupiter.api.BeforeEach;\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "import java.util.*;\n" +
                "import org.la4j.matrix.dense.Basic2DMatrix;\n" +
                "import " + OCDWriter.class.getName() + ";\n" +
                "import " + OcdAlgorithm.class.getName() + ";\n" +
                "import " + OcdAlgorithm.class.getPackage().getName() + "." + ocdaName + ";\n" +
                "import static " + OcdAlgorithm.class.getPackage().getName() + "." + ocdaName + ".*;\n" +  // this is needed for OCDA parameter name constants
                "import " + Cover.class.getName() + ";\n" +
                "import " + CustomGraph.class.getName() + ";\n" +
                "import " + GRAPH_FACTORY_CLASS_NAME + ";\n" +
                generateGraphTypeTestInterfaceImportString(compatibilities);

        String[] lines = multiLineString.split("\n");
        List<String> importStatementList = new ArrayList<>(Arrays.asList(lines));

        return importStatementList;
    }


    /**
     * Generates and writes prompt string containing partially completed unit tests based on graph types compatible
     * with the OCD algorithm. This prompt can be used with a GPT model to complete the partially completed tests.
     * The method also includes algorithm parameter definitions and the implementation of the
     * setParameters method from the algorithm code.
     *
     * @param ocdaCode A File object representing the OCD algorithm code.
     * @return A string representing the generated prompt, including partially completed unit tests,
     *         algorithm parameter definitions, and the implementation of the setParameters method.
     */
    public static String generateAndWriteGraphTypeRelatedTestPrompt(File ocdaCode) {

        // Initialize an empty StringBuilder to concatenate strings
        StringBuilder stringBuilder = new StringBuilder();

        // Get name of OCDA
        String ocdaName = OCDAParser.getClassName(ocdaCode);

        // Add initial sentence to prompt that mentions OCDA
        stringBuilder
                .append("I want you to complete partially completed unit tests related to graph types for algorithm " + ocdaName + ". Don't forget to include Javadoc comment on each unit test.")
                .append("\n\n");

        // Add partially completed unit tests for each compatible graph type to the prompt string
        stringBuilder
                .append("### Partially completed unit tests")
                .append("\n\n")
                .append(generatePartiallyCompletedGraphTypeTests(ocdaCode))
                .append("\n");


        // Extract OCD algorithm parameter definitions from the algorithm code, together with their Javadoc comments.
        // Then append it to the prompt string
        stringBuilder
                .append("### Algorithm parameter definitions")
                .append("\n\n")
                .append("My algorithm has the following algorithm parameters and default parameter values:")
                .append("\n\n");

        // Extract OCDA parameters of from the algorithm code
        List<String> ocdaParams = extractParameterNamesFromDeclarations(ocdaCode);

        // Extract full OCDA parameter declarations from the algorithm code, together with Javadoc comments (if present)
        List<String> ocdaParamsFullDeclarations =  OCDAParser.listSelectedClassVariables(ocdaCode,true,ocdaParams);

        // Add OCDA parameters to the prompt
        for (String ocdParameter : ocdaParamsFullDeclarations){
            stringBuilder
                    .append(ocdParameter)
                    .append("\n\n\n");
        }

        // Extract implementation of the setParameters method from the algorithm code and add it to the prompt string
        stringBuilder.append("### Implementation of the setParameters method")
                .append("\n\n")
                .append(getMethodImplementation(ocdaCode,SET_OCD_PARAMETERS_METHOD_NAME))
                .append("\n\n");



        // Path to write, which starts with root (i.e. not in child project) if root variable is set in gradle task
        String pathToWrite = PathResolver.addProjectRootPathIfSet("gpt/prompts/" + OCDAParser.getClassName(ocdaCode) +"_unit_test_completion_prompt.txt");

        // Write prompt for completing partially generated test classes
        generateAndWriteFile(pathToWrite , stringBuilder.toString(), false);

        // Create an empty file where GPT response should be written for processing
        createGPTResponseFileForTests(OCDAParser.getClassName(ocdaCode), "");

        return stringBuilder.toString();

    }


    /**
     * Create a file where GPT response that includes auto-generated tests should be stored for further processing.
     * @param ocdaName     Name of the OCD algorithm for which the file should be
     *                     created (i.e. OCDA for which the tests are generated).
     */
    public static void createGPTResponseFileForTests(String ocdaName, String content){
        String gptResponseFilePath = PathResolver.addProjectRootPathIfSet(FileHelpers.getAutoGeneratedUnprocessedUnitTestPath(ocdaName + OCDATestAutomationConstants.GPT_GENERATED_TEST_FILE_NAME_SUFFIX));
        OCDWriter.generateAndWriteFile(gptResponseFilePath, content, false);
    }

    /**
     * Create a file where GPT response that includes auto-generated OCDA parameters should be stored
     * for further processing.
     * @param ocdaName     Name of the OCD algorithm for which the file should be
     *                     created (i.e. OCDA for which the tests are generated).
     */
    public static void createGPTResponseFileForOCDAParameters(String ocdaName, String content){
        String gptResponseFilePath = PathResolver.addProjectRootPathIfSet(FileHelpers.getAutoGeneratedUnprocessedUnitTestPath(ocdaName + OCDATestAutomationConstants.GPT_GENERATED_OCDA_PARAMETERS_FILE_NAME_SUFFIX));
        OCDWriter.generateAndWriteFile(gptResponseFilePath, content, false);
    }


    public static String generateAndWriteOCDAParameterGenerationPrompt(File ocdaCode){
        StringBuilder stringBuilder = new StringBuilder();

        String ocdaName = OCDAParser.getClassName(ocdaCode);

        /* Add OCD algorithm parameter definitions to the prompt */

        stringBuilder.append("I want you to generate diverse Overlapping Community Detection (OCD) " +
                "algorithm parameter values for the algorithm called " + ocdaName + ". After you generate algorithm " +
                "parameter values, replace placeholders in the JSON I provided with the corresponding parameter values " +
                "that you generated.").append("\n\n");

        // Extract OCD algorithm parameter definitions from the algorithm code, together with their Javadoc comments.
        // Then append it to the prompt string
        stringBuilder
                .append("### Algorithm parameter definitions")
                .append("\n\n")
                .append("My algorithm has the following algorithm parameters and default parameter values:")
                .append("\n\n");

        // Extract OCDA parameters of from the algorithm code
        List<String> ocdaParams = extractParameterNamesFromDeclarations(ocdaCode);

        // Extract full OCDA parameter declarations from the algorithm code, together with Javadoc comments (if present)
        List<String> ocdaParamsFullDeclarations =  OCDAParser.listSelectedClassVariables(ocdaCode,true,ocdaParams);

        // Add OCDA parameters to the prompt
        for (String ocdParameter : ocdaParamsFullDeclarations){
            stringBuilder
                    .append(ocdParameter)
                    .append("\n\n\n");
        }

        /* add setParameters implementation of the OCD algorithm to the prompt */

        // Replace OCD algorithm parameter name constants in setParameters implementation with actual parameter names.
        // This is to make it less confusing for GPT (e.g. LEADERSHIP_ITERATION_BOUND_NAME is replaced
        // with "leadershipIterationBound")

        // Get setParameters method implementation for the specified OCD algorithm
        String setParametersImplementation = getMethodImplementation(ocdaCode, SET_OCD_PARAMETERS_METHOD_NAME);

        // Extract parameter declarations without comments
        List<String> ocdaParameterDeclarationsList = OCDAParser.listSelectedClassVariables(ocdaCode,false,ocdaParams);

//        // List that will be filled with OCDA parameter names for easier access
//        List<String> ocdaParameterList = new ArrayList<>();

        for (String parameterDeclaration : ocdaParameterDeclarationsList){

            // Name of the OCD algorithm parameter (e.g. leadershipIterationBound)
            String parameterVariableName = FormattingHelpers.extractVariableName(parameterDeclaration);

            // Name of the constant that holds OCDA parameter name, used in setParameters implementation
            String parameterVariableNameConstant = FormattingHelpers.convertCamelCaseToUpperCaseWithUnderscores(parameterVariableName);

            setParametersImplementation = FormattingHelpers.replaceConstantWithVariable(parameterVariableNameConstant,parameterVariableName,setParametersImplementation);

            //System.out.println(parameterDeclaration + " | " + parameterVariableName + " | " + parameterVariableNameConstant);

        }

        // Add setParameters implementation to the prompt
        stringBuilder.append("### Implementation of the setParameters method")
                .append("\n\n")
                .append(setParametersImplementation)
                .append("\n\n");

        /* JSON with placeholders for parameter values to be completed by GPT */

        // Create a JSON that holds OCDA parameter and a list of placeholders for each parameter that should
        // be replaced by GPT with actual reasonable values
        Map<String, List<String>> placeholdersMap = new HashMap<>();

        // Number of different parameter values for which the placeholders should be created
        int parameterCount = Integer.parseInt(OCDA_PARAMETER_GENERATION_COUNT_FOR_OCD_ACCURACY_TESTS);

        for (String key : ocdaParams) {
            List<String> placeholders = new ArrayList<>();
            for (int i = 1; i <= parameterCount; i++) {
                placeholders.add(key + "_PLACEHOLDER_VALUE_" + i);
            }
            placeholdersMap.put(key, placeholders);
        }

        JSONObject json = new JSONObject(placeholdersMap);



        /* Add created JSON to the prompt */
        stringBuilder
                .append("### JSON with placeholders for algorithm parameter values that you must replace values")
                .append("\n\n")
                .append(json.toJSONString());


        // Path to write, which starts with root (i.e. not in child project) if root variable is set in gradle task
        String pathToWrite = PathResolver.addProjectRootPathIfSet("gpt/prompts/" + OCDAParser.getClassName(ocdaCode) +"_ocda_parameter_generation_prompt.txt");

        // Write prompt for generating OCDA parameters
        generateAndWriteFile(pathToWrite , stringBuilder.toString(), false);

        // Create an empty file where GPT response should be written for processing
        createGPTResponseFileForOCDAParameters(OCDAParser.getClassName(ocdaCode), "");

        return stringBuilder.toString();

    }

    /**
     * Generates a unit test template for compatible graph types of the algorithm.
     * This method reads the specified OCD algorithm code file to determine compatible graph types,
     * then creates a unit test template for each type, excluding 'CONTENT_UNLINKED' and 'CONTENT_LINKED' types.
     * It also adds an artificial undirected type to the list of compatible graph types as each OCD algorithm in WebOCD
     * works on undirected graphs, even though this type does not exist as a separate GraphType.
     *
     * @param ocdaCode The OCDAParser code file from which to extract compatible graph types.
     * @return A string containing the generated unit test templates.
     */
    public static String generatePartiallyCompletedGraphTypeTests(File ocdaCode){

        StringBuilder unitTestTemplatesBuilder = new StringBuilder();


        // Map graph type strings to corresponding getter methods for fetching test graphs
        Map<String, String> graphTypeStringToTestNameMap = getGraphTypeBasedTestNames();


        // parse compatible graph types for the algorithm (list entries are e.g. "GraphType.DIRECTED")
        List<String> compatibleGraphTypeStrings = OCDAParser.extractCompatibilities(ocdaCode);

        // Add artificial undirected type to create a test for an undirected graph without code duplication
        compatibleGraphTypeStrings.add("GraphType.UNDIRECTED");


        // generate test for each graph type except content unlinked and content linked, as those need rework in WebOCD. Content linked and content unlinked graphs are ignored for now as these need a complete rework in WebOCD.
        for (String compatibleGraphTypeString : compatibleGraphTypeStrings) {

            if (!compatibleGraphTypeString.equals("GraphType.CONTENT_UNLINKED")
                    && !compatibleGraphTypeString.equals("GraphType.CONTENT_LINKED")){

                // Build a unit test name based on the graph type that is tested
                StringBuilder testNameStringBuilder = new StringBuilder(graphTypeStringToTestNameMap.get(compatibleGraphTypeString));

                unitTestTemplatesBuilder.append(generateUnitTestString(testNameStringBuilder.toString(),
                        compatibleGraphTypeString));
                unitTestTemplatesBuilder.append("\n");
            }

        }


        return unitTestTemplatesBuilder.toString();
    }


    /**
     * Generates a partially completed unit test string for a specified OCD algorithm method.
     * This method constructs a test method template with a TODO comment indicating where further
     * test implementation should be added. The generated test method includes basic structure and
     * exception handling, designed to be completed with specific test logic.
     *
     * @param ocdaMethodName The name of the OCD algorithm method for which the unit test is to be generated.
     * @return A string representing the partially completed unit test method.
     */
    public static String generateOCDAMethodUnitTestString(String ocdaMethodName){
        StringBuilder unitTestStringBuilder = new StringBuilder();

        unitTestStringBuilder.append(
                "\t/**\n" +
                        "\t * " + AUTO_GENERATED_COMMENT_STRING + "\n" +
                        "\t */\n" +
                        "\t@Test\n" +
                        "\tpublic void " + ocdaMethodName + "Test() throws Exception {\n" +
                        "\t\ttry {\n" +
                        "\n" +
                        "\t\t\t //TODO: To be completed by ChatGPT. Respect access modifiers and don't leave any variable unpopulated.\n" +
                        "\n" +
                        "\t\t} catch (Throwable t){\n" +
                        "\t\t\t fail(\"Test failed due to an exception or assertion error: \" + t.getMessage()); // Don't modify\n" +
                        "\t\t\t throw t; // Don't modify\n" +
                        "\t\t}\n" +
                        "\t}\n"
        );

        return unitTestStringBuilder.toString();
    }

    /**
     * Generates a partially completed unit test string for a given test name and compatible graph type.
     * This method auto-generates the structure of a JUnit test method, including placeholders for
     * setting algorithm parameters and instantiating graph covers. It is designed to facilitate
     * the testing of the OCD algorithm on different types of graphs. The method includes pre-written
     * code sections that should not be modified, as well as placeholders where auto-generated code by
     * ChatGPT should be placed
     *
     * @param testName The name of the unit test method to be generated, reflecting the graph type.
     * @param compatibleGraphTypeString A string representing the graph type on which the OCD algorithm is to be tested.
     * @return A string representing a partially completed JUnit test method.
     */
    private static String generateUnitTestString(String testName, String compatibleGraphTypeString){
        StringBuilder unitTestStringBuilder = new StringBuilder();

        unitTestStringBuilder.append(
                "\t/**\n" +
                        "\t * " + AUTO_GENERATED_COMMENT_STRING + "\n" +
                        "\t */\n" +
                        "\t@Test\n" +
                        "\tpublic void " + testName + "() throws Exception {\n" +
                        "\t\ttry {\n" +
                        "\t\t\t" + generateGraphInstantiationString(compatibleGraphTypeString) + " // Don't modify\n" +
                        "\n" +
                        "\t\t\tMap<String, String> parameters = new HashMap<>(); // Don't modify\n" +
                        "\t\t\t//TODO: Set algorithm parameters here. To be completed by ChatGPT\n" +
                        "\t\t\tgetAlgorithm().setParameters(parameters); // Don't modify\n" +
                        "\n" +
                        "\t\t\t" + generateCoverInstantiationString(compatibleGraphTypeString) + " // Don't modify\n" +
                        "\t\t\t" +"assertTrue(cover.getCommunities().size() >= 1); // Don't modify\n" +
                        "\n" +
                        "\t\t} catch (Throwable t){\n" +
                        "\t\t\t" + "fail(\"Test failed due to an exception or assertion error: \" + t.getMessage()); // Don't modify\n" +
                        "\t\t\t" + "throw t; // Don't modify\n" +
                        "\t\t}\n" +
                        "\t}\n"
        );

        return unitTestStringBuilder.toString();
    }


    /**
     * Generates a string for instantiating a 'Cover' object, used in the execution of the OCDA algorithm on a graph.
     * This method creates a Java line of code that initializes a 'Cover' object by calling the OCDA algorithm's
     * 'detectOverlappingCommunities' method on a specific type of graph. The type of graph is determined based on the
     * provided compatible graph type string. This generated code line is intended to be used in the setup of unit tests
     * for the OCDA algorithm.
     *
     * @param compatibleGraphTypeString A string indicating the type of graph (e.g. GraphType.DIRECTED) used in the test.
     * @return A string representing a line of Java code for instantiating a 'Cover' object with the OCDA algorithm
     * and the specified graph.
     */
    private static String generateCoverInstantiationString(String compatibleGraphTypeString) {
        // Cover cover = getAlgorithm().detectOverlappingCommunities(weightedGraph); // Don't modify
        return "Cover cover = " + OCDA_GETTER +"().detectOverlappingCommunities(" + generateGraphVariableName(compatibleGraphTypeString) + ");";

    }

    /**
     * Generates a Java code string for instantiating a graph object based on a provided graph type string.
     * This method maps the input graph type string, which should follow the format of the GraphType enum
     * (e.g., "GraphType.DIRECTED"), to a corresponding method name for fetching test graphs. It then constructs
     * a line of code to instantiate a graph of the specified type using these method names. The method handles
     * various graph types, including directed, weighted, and others, and also handles an artificially added
     * undirected type for flexibility.
     *
     * @param compatibleGraphTypeString A string representing the graph type, matching the GraphType enum format.
     * @return A string representing a line of Java code for instantiating a graph object of the specified type.
     *         Returns an empty string if the graph type is not recognized or not mapped.
     */
    private static String generateGraphInstantiationString(String compatibleGraphTypeString){

        // map graph types to corresponding getter methods for fetching test graphs
        Map<String, String> graphTypeToMethodNameMap = getGraphTypeToMethodNameMap();

        String methodName = graphTypeToMethodNameMap.get(compatibleGraphTypeString);

        // create a string of graph variable declaration depending on the graph type
        if (methodName != null) {

            String InstantiationString = GRAPH_CLASS_NAME + " " + generateGraphVariableName(compatibleGraphTypeString)+ " = "
                    + extractLastPart(GRAPH_FACTORY_CLASS_NAME) + "." + methodName + "();";

            return InstantiationString;

        }
        return "";
    }

    /**
     * Generates and writes a prompt for test generation for a specific OCD algorithm method.
     * The generated prompt includes import statements from the test class and the name of the algorithm, which is
     * extracted from the OCD algorithm class file. This OCD algorithm class file should be provided as input
     * together with the prompt text when the output of this method is used as an input prompt to GPT.
     *
     * As a response, the user will be prompted to provide a method name for which the test should be generated.
     *
     * @param ocdaCode     A File object representing the OCD algorithm code.
     * @return A string representing the generated prompt, including import statements and the algorithm's class name.
     */
    public static String generateAndWriteOCDAMethodTestPromptForCustomGPT(File ocdaCode){
        // Initialize an empty StringBuilder to concatenate strings
        StringBuilder stringBuilder = new StringBuilder();

        // Merge import statements from the OCD algorithm class and its test class, since the algorithm class
        // imports might be needed for the test class if the user wants to test OCD algorithm methods.
        List<String> importStatements = mergeAndSortLists(extractSortedImports(ocdaCode),
                generateTestClassImports(ocdaCode));


        // Add import statements from the test class to the prompt, such that GPT knows what is available for tests
        stringBuilder
                .append("Import statements in the test class are as follows:")
                .append("\n\"\n");

        importStatements.forEach(importStatement -> stringBuilder.append(importStatement).append("\n"));


        stringBuilder
                .append("\"")
                .append("\n\n");

        // Add name of the algorithm to the prompt
        stringBuilder
                .append("The full OCD algorithm code is provided as a file called ")
                .append(OCDAParser.getClassName(ocdaCode))
                .append(".java")
                .append("\n\n");



        return stringBuilder.toString();

    }

    /**
     * Generates and writes a prompt for test generation for a specific OCD algorithm methods.
     * The generated prompt includes the signatures of the methods for which the tests should be generated,
     * partially completed unit tests for these methods (that GPT must complete) and the full OCD algorithm
     * code from which additional information can be extracted (as needed).
     *
     * As a response, the user will be prompted to provide a method name for which the test should be generated.
     *
     * @param ocdaName     A File object representing the OCD algorithm code.
     * @return A string representing the generated prompt, including import statements and the algorithm's class name.
     */
    public static String generateAndWriteOCDAMethodTestPromptForGPTAPI(String ocdaName, List<String> methodNames){

        // OCD algorithm code
        String ocdaCodePath = FileHelpers.cleanDuplicateDirectories(FileHelpers.getOCDAPath(ocdaName + ".java"));
        File ocdaCode = new File(ocdaCodePath);


        // Initialize an empty StringBuilder to concatenate strings
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Assume Role 2 and please complete partially completed unit tests for the specified methods of " + ocdaName + ":\n");

        // Add method names for to the prompt for which tests should be generated
        stringBuilder.append("### Method Names\n");
        stringBuilder.append(String.join(",", methodNames));
        stringBuilder.append("\n\n");

        // Add method signatures to the prompt for which tests should be generated
        stringBuilder.append("### Method Signatures\n");
        methodNames.forEach(methodName -> {
            stringBuilder.append(OCDAParser.getMethodSignature(ocdaCode,methodName)).append("\n");
        });
        stringBuilder.append("\n\n");

        // Append test class with partially completed unit tests for specified OCDA methods
        stringBuilder
                .append("### Test class with partially completed unit tests for the methods I want to test\n\n");

        String ocdaTestClassPath = OCDATestAutomationConstants.GPT_GENERATED_TEST_CLASS_CODE_LOCATION + "Generated" + ocdaName + "Test.java";

        // Generate OCDA test class with partially completed unit tests
        GradleTaskExecutor.runInitializeOCDAMethodTestFiles(ocdaName,methodNames);
        File ocdaTestClass = new File(PathResolver.addProjectRootPathIfSet(ocdaTestClassPath));
        stringBuilder
                .append(FileHelpers.readFileAsString(ocdaTestClass))
                .append("\n");

        // Add OCD algorithm code to the prompt
        stringBuilder
                .append("### Full OCD algorithm code\n")
                .append("\n");
        stringBuilder
                .append(FileHelpers.readFileAsString(ocdaCode))
                .append("\n");

        // Create an empty file where GPT response should be written for processing
        createGPTResponseFileForTests(OCDAParser.getClassName(ocdaCode), "");


        // Path to write, which starts with root (i.e. not in child project) if root variable is set in gradle task
        String pathToWrite = PathResolver.addProjectRootPathIfSet("gpt/prompts/" + OCDAParser.getClassName(ocdaCode) +"_unit_test_completion_prompt.txt");
        // Write prompt for completing partially generated test classes
        generateAndWriteFile(pathToWrite, stringBuilder.toString(), false);

        return stringBuilder.toString();

    }




    /**
     * Generates a list of import statements for the test class of a specified OCD algorithm. The import statements
     * include the imports from the algorithm code, as well as typical imports needed in OCDA test classes.
     * @param ocdaCode      OCD algorithm code for the test class of which the import statements should be generated
     * @return              A list of import statement strings
     */
    public static List<String> getOCDATestClassImportStatements(File ocdaCode) {
        return mergeAndSortLists(extractSortedImports(ocdaCode),
                generateTestClassImports(ocdaCode));
    }


    /**
     * Generates import statements for test interfaces corresponding to compatible graph types.
     *
     * This method constructs import statements for each graph type compatible with a specific algorithm.
     * It first appends the import statement for the OCDAParameterTestReq class as this interface is implemented by
     * all OCDA test classes, independent of compatible graph types. Then, for each compatible graph type,
     * it converts the graph type to camel case and capitalizes the first letter (e.g., ZERO_WEIGHTS becomes
     * ZeroWeights). Then, it uses the graph type name to create import statement for the respective graph
     * test interface and add this import statement to a string of import statements that will be used in the prompt.
     * These interfaces for each graph type are assumed to be in the same package as the base
     * interface (BaseGraphTestReq) from which they inherit.
     *
     * @param compatibleGraphTypes A list of strings representing the compatible graph types (e.g. GraphType.DIRECTED).
     * @return A string containing import statements for each of the compatible graph type test interfaces.
     */
    private static String generateGraphTypeTestInterfaceImportString(List<String> compatibleGraphTypes){

        // Each OCDA in WebOCD should work on an undirected graph
        if (!compatibleGraphTypes.contains("GraphType.UNDIRECTED")){
            compatibleGraphTypes.add("GraphType.UNDIRECTED");
        }

        StringBuilder importString = new StringBuilder("import " + BASE_TEST_INTERFACES_PACKAGE_STRING + ".OCDAParameterTestReq;\n");

        // Package of the base test interface, the other test interfaces should be in the same package
        String baseTestInterfacePackage = BASE_TEST_INTERFACES_PACKAGE_STRING;

        for (String compatibleGraphType : compatibleGraphTypes){

            // Turn graph type to a camel case, e.g. ZERO_WEIGHTS will be turned into zeroWeights
            String graphTypeCamelCase = toCamelCaseFromUnderscore(compatibleGraphType.split("\\.")[1]);

            // Capitalize the camel case string as it is common for class names
            String capitalizedGraphTypeCamelCase = capitalizeFirstLetter(graphTypeCamelCase);

            // Create an import statement based on a graph type that will be used in the prompt generator
            importString.append("import " + baseTestInterfacePackage + "."
                    + capitalizedGraphTypeCamelCase + "GraphTestReq;\n");
        }

        return importString.toString();
    }





}
