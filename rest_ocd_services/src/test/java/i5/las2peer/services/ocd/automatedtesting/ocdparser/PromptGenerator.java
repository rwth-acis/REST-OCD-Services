package i5.las2peer.services.ocd.automatedtesting.ocdparser;

import i5.las2peer.services.ocd.automatedtesting.metric.OCDSubmetric;
import i5.las2peer.services.ocd.test_interfaces.ocda.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static i5.las2peer.services.ocd.automatedtesting.ocdparser.OCDAParser.getOCDAPath;

public class PromptGenerator {



    /*  Constants related to graph types */
    // Method names to fetch graphs to be used in auto-generated tests
    //TODO: add/replace method names
    public static String UNDIRECTED_GRAPH_METHOD_NAME = "getSawmillGraph";
    public static String DIRECTED_GRAPH_METHOD_NAME = "getDirectedAperiodicTwoCommunitiesGraph";
    public static String WEIGHTED_GRAPH_METHOD_NAME = "getTwoCommunitiesWeightedGraph";

    public static String ZERO_WEIGHTS_GRAPH_METHOD_NAME = "todoZeroWeights"; //TODO: replace string with appropriate method name

    public static String NEGATIVE_WEIGHTS_GRAPH_METHOD_NAME = "todoNegativeWeights"; //TODO: replace string with appropriate method name

    public static String SELF_LOOPS_GRAPH_METHOD_NAME = "todoSelfLoops"; //TODO: replace string with appropriate method name

    // Name of the class where the test graphs are stored or generated
    public static String GRAPH_FACTORY_CLASS_NAME = "OcdTestGraphFactory";

    // Name of the shared method among OCDA test classes to get an algorithm instance
    public static String OCDA_GETTER = "getAlgorithm";

    // Name of the graph class used in WebOCD
    public static String GRAPH_CLASS_NAME = "CustomGraph";

    /*  Constants related to test names that must be auto-completed by ChatGPT */

    public static String UNDIRECTED_GRAPH_TEST_NAME = "undirectedGraphTest1";

    public static String DIRECTED_GRAPH_TEST_NAME = "directedGraphTest1";

    public static String WEIGHTED_GRAPH_TEST_NAME = "weightedGraphTest1";

    public static String ZERO_WEIGHTS_GRAPH_TEST_NAME = "zeroWeightsGraphTest1";

    public static String NEGATIVE_WEIGHTS_GRAPH_TEST_NAME = "negativeWeightsGraphTest1";

    public static String SELF_LOOPS_GRAPH_TEST_NAME = "selfLoopsGraphTest1";


    // Text used to annotate lines that should not be modified
    public static String DONT_MODIFY_COMMENT_STRING = "Don't modify";

    // Text used to annotate unit test comments indicating unit test is generated/completed by ChatGPT
    public static String AUTO_GENERATED_COMMENT_STRING = "Auto-Generated by ChatGPT";

    /**
     * Generates a prompt for a given OCDA and its compatible graph types. This prompt can be used as an input
     * to ChatGPT. The output should be a downloadable file representing a test class for the specified ocda.
     * Since the code will be auto-generated, it should be reviewed manually.
     * @Param ocdaCode              File containing OCD algorithm code
     */
    public static void generateAndWritePromptString(File ocdaCode){

        // Extract OCD algorithm name from the class file
        String ocdaName = OCDAParser.getClassName(ocdaCode);

        // Extract compatible graph types for the given algorithm
        List<String> compatibilities = OCDAParser.extractCompatibilities(ocdaCode);

        // String that will hold interface names that an OCDA test class should implement
        String baseInterfaceNamesToImplement = "";

        // All OCDA test classes implement test interface for algorithm parameters and undirected graph type
        baseInterfaceNamesToImplement += OCDAParameterTestReq.class.getSimpleName()
                + ", " + UndirectedGraphTestReq.class.getSimpleName();



        // Initialize an empty StringBuilder to concatenate strings
        StringBuilder stringBuilder = new StringBuilder();

        // Initial text for ChatGPT instructing what to do that mentions partially completed unit tests
        stringBuilder.append("I have a framework for community detection algorithms written in Java. I will " +
                "give you code of one such algorithm called " + ocdaName + " and a partially completed test " +
                "class for the algorithm. Your task is to complete the test class by generating the " +
                "missing parts in unit tests of the given algorithm and providing me the full test class " +
                "with the completed unit tests inside as a downloadable file. \n");

        // Instruction on how partially completed tests can be identified and not to remove comment indicating
        // that content these tests is going to be generated by ChatGPT
        stringBuilder.append("In the partially written test class that you have to complete, the unit tests that " +
                "you must complete are annotated with a following javadoc comment '\n" +
                "\t/* \n" +
                "\t* " + AUTO_GENERATED_COMMENT_STRING + "\n" +
                "\t*/\n" +
                "'. It is important that you do not remove string '" + AUTO_GENERATED_COMMENT_STRING +"' from this comment. " +
                "Instead, any additional comment you add should start on the next line from " +
                "string '" + AUTO_GENERATED_COMMENT_STRING + "'.\n");


        // Instruction on how to complete each unit test that must be completed and placeholder explanation
        stringBuilder.append("You should identify each unit test annotated with this comment and complete the unit " +
                "test as described below. In each unit test you have to complete, there is a placeholder where " +
                "your code should go. This placeholder is a comment '//TODO: Set algorithm parameters here. To " +
                "be completed by ChatGPT'. You should replace this comment with your code. Within unit tests " +
                "there might be some lines that end with a comment '// " + DONT_MODIFY_COMMENT_STRING + "'. Do not change those lines.\n\n");


        // Indicate that what follows is something that must be considered when ChatGPT generates code
        stringBuilder.append("Consider the following:\n\n");

        // Instruction and explanation on how algorithms can be executed on graphs
        stringBuilder.append("Executing the algorithm on graphs: in each unit test that you have to complete, " +
                "the algorithm must be executed on a graph that is of type CustomGraph. The graph on which " +
                "the algorithm should be executed is already instantiated in each unit test you have to complete " +
                "and you should use this CustomGraph instance defined within each unit test. For example, to execute " +
                "the algorithm on a CustomGraph instance with a name graphName use " +
                "'"+ OCDA_GETTER +"().detectOverlappingcommunities(graphName)'.\n\n");

        // Instruction on partially completed unit tests for each compatible graph type of the algorithm
        stringBuilder.append("Number of partially completed unit tests which you must complete, that test the " +
                "algorithm execution on different graph types and the names of these partially completed unit " +
                "tests are as follows:\n");


        // all OCDA are compatible with undirected networks in WebOCD
        stringBuilder.append("- 1 partially completed unit test to test the algorithm on an undirected graph. " +
                "This unit test methods is called '"  + PromptGenerator.UNDIRECTED_GRAPH_TEST_NAME + "()'.").append("\n");


        // Iterate through the list and generate strings based on values
        for (String graphType : compatibilities) {
            if (graphType.equals("GraphType.DIRECTED")){

                // test interface for directed graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + DirectedGraphTestReq.class.getSimpleName();

                // instruction to generate a test used on a directed graph
                stringBuilder.append("- 1 partially completed unit test to test the algorithm on a directed graph. " +
                        "This unit test method is called '"  + PromptGenerator.DIRECTED_GRAPH_TEST_NAME + "()'.")
                        .append("\n");
            }
            if (graphType.equals("GraphType.WEIGHTED")){

                // test interface for directed graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + WeightedGraphTestReq.class.getSimpleName();


                // instruction to generate a test used on a weighted graph
                stringBuilder.append("- 1 partially completed unit test to test the algorithm on a weighted graph. " +
                                "This unit test method is called '"  + PromptGenerator.WEIGHTED_GRAPH_TEST_NAME + "()'.")
                        .append("\n");
            }
            if (graphType.equals("GraphType.ZERO_WEIGHTS")){
                // test interface for zero weight graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + ZeroWeightedGraphTestReq.class.getSimpleName();


                // instruction to generate a test used on a weighted graph
                stringBuilder.append("- 1 partially completed unit test to test the algorithm on a graph with zero weights. " +
                                "This unit test method is called '"  + PromptGenerator.ZERO_WEIGHTS_GRAPH_TEST_NAME + "()'.")
                        .append("\n");
            }
            if (graphType.equals("GraphType.NEGATIVE_WEIGHTS")){
                // test interface for negative weights graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + NegativeWeightGraphTestReq.class.getSimpleName();


                // instruction to generate a test used on a weighted graph
                stringBuilder.append("- 1 partially completed unit test to test the algorithm on a graph with negative weights. " +
                                "This unit test method is called '"  + PromptGenerator.NEGATIVE_WEIGHTS_GRAPH_TEST_NAME + "()'.")
                        .append("\n");
            }
            if (graphType.equals("GraphType.SELF_LOOPS")){
                // test interface for self loops graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + SelfLoopGraphTestReq.class.getSimpleName();


                // instruction to generate a test used on a weighted graph
                stringBuilder.append("- 1 partially completed unit test to test the algorithm on a graph with self loops. " +
                                "This unit test method is called '"  + PromptGenerator.SELF_LOOPS_GRAPH_TEST_NAME + "()'.")
                        .append("\n");
            }

        }

        stringBuilder.append("\n");

        // Extract algorithm parameter names form the algorithm code
        List<String> algorithmParameterNames = OCDSubmetric.extractParameterNamesFromDeclarations(OCDAParser.listClassVariables(ocdaCode));
        String algorithmParameterNameString = algorithmParameterNames.stream().map(s -> "'" + s + "'").collect(Collectors.joining(", "));

        // Explanation and instruction about algorithm parameters
        stringBuilder.append("Algorithm parameters: the algorithm has algorithm parameters that need to be set " +
                "before the algorithm is executed. " + ocdaName + " has the following algorithm" +
                " parameters: "+ algorithmParameterNameString + ". Using the code I gave you, you must identify " +
                "types of these parameters (e.g. int, double etc) and possible value ranges that these parameters can have.\n");



        // Example code
        stringBuilder.append("As an example, below is a code snippet for some algorithm called SomeAlgorithm " +
                "that has parameters \'seedCount\' and \'accuracy\':\n");

        stringBuilder.append("'\n" +
                "public class SomeAlgorithm implements OcdAlgorithm {\n" +
                "\n" +
                "\t/**\n" +
                "\t * The number of seed which will be determined by the seed strategy. This number\n" +
                "\t * will later result in the number of communities.\n" +
                "\t */\n" +
                "\tprivate int seedCount = 10;\n" +
                "\t/**\n" +
                "\t * The accuracy parameter is required in step 3 (Expansion).\n" +
                "\t * It controls the size of the resulting communities.\n" +
                "\t * If the accuracy is large (e.g. 10^-2) the resulting communities are small.\n" +
                "\t * If the accuracy is small (e.g. 10^-8) the resulting communities are large.\n" +
                "\t */\n" +
                "\tprivate double accuracy = 0.00000001;\n" +
                "\t\n" +
                "\t\n" +
                "\t/*\n" +
                "\t * PARAMETER NAMES\n" +
                "\t */\n" +
                "\t\n" +
                "\tprotected static final String SEED_COUNT_NAME = \"seedCount\";\n" +
                "\n" +
                "\tprotected static final String ACCURACY_NAME = \"accuracy\";\n" +
                "\t\n" +
                "\t// ... rest of the code \n" +
                "\t\n" +
                "'.").append("\n\n");




        // Instruction on setting different algorithm parameters and adding javadoc comments to tests
        stringBuilder.append("Once you identify parameters of the algorithm, then you need to set reasonable parameter " +
                "values in each unit test before executing the algorithm (the parameter values between tests should " +
                "differ). To set parameter values use 'setParameters(Map<String, String> parameters)'. Make sure to " +
                "also add explanation in javadoc comment about why you chose the parameters you chose.\n\n");


        // Example unit test that sets algorithm parameters
        stringBuilder.append("Below is a code snippet example of how a unit test might look like for an algorithm " +
                "called SomeAlgorithm. This example unit test sets algorithm parameters using setParameters method " +
                "and then executes the algorithm on a CustomGraph instantiated within the unit test using " +
                "getAlgorithm().detectOverlappingCommunities(undirectedGraph) method.\n\n");

        stringBuilder.append("'\n" +
                "@Test\n" +
                "public void testOnUndirectedGraph1() {\n" +
                "\t\n" +
                "\ttry {\n" +
                "\t\n" +
                "\t\t// graph on which the algorithm is executed \n" +
                "\t\tCustomGraph undirectedGraph = OcdTestGraphFactory.getSawmillGraph();\n" +
                "\t\t\n" +
                "\t\t// set algorithm parameters\n" +
                "\t\tMap<String, String> parameters = new HashMap<String, String>();\n" +
                "\t\tparameters.put(\"seedCount\", \"5\");\n" +
                "\t\tparameters.put(\"accuracy\", \"0.0000001\");\n" +
                "\t\tgetAlgorithm().setParameters(parameters);\n" +
                "\t\t\n" +
                "\t\t// execute algorithm on a graph and generate a cover\n" +
                "\t\tCover cover = getAlgorithm().detectOverlappingCommunities(undirectedGraph);\n" +
                "\t} catch(Exception e){\n" +
                "\t\te.printStackTrace();\n" +
                "\t}\n" +
                "}\n" +
                "\n" +
                "'.");

        // Add partially completed test class code (for ChatGPT to complete based on above instructions) to the prompt
        stringBuilder.append("\n\n");
        stringBuilder.append("Consider the instructions, explanations and examples given above for completing the " +
                "partially completed code. Add your code to the partially completed algorithm test class code given " +
                "below. You shouldn't modify code preceding comment '//ChatGPT code'. Additionally, you should not " +
                "change lines that end with a comment '// " + DONT_MODIFY_COMMENT_STRING + "':\n\n");

        stringBuilder.append("'\n" +
                "package i5.las2peer.services.ocd.algorithms;\n" +
                "\n" +
                "import static org.junit.jupiter.api.Assertions.assertEquals;\n" +
                "import static org.junit.jupiter.api.Assertions.assertTrue;\n" +
                "import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;\n" +
                "import i5.las2peer.services.ocd.algorithms." + ocdaName + ";\n" +
                "import i5.las2peer.services.ocd.graphs.Cover;\n" +
                "import i5.las2peer.services.ocd.test_interfaces.ocda.DirectedGraphTestReq;\n" +
                "import i5.las2peer.services.ocd.test_interfaces.ocda.OCDAParameterTestReq;\n" +
                "import i5.las2peer.services.ocd.test_interfaces.ocda.UndirectedGraphTestReq;\n" +
                "import i5.las2peer.services.ocd.test_interfaces.ocda.WeightedGraphTestReq;\n" +
                "import org.junit.jupiter.api.BeforeEach;\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "import i5.las2peer.services.ocd.graphs.CustomGraph;\n" +
                "import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;\n" +
                "import java.util.HashMap;\n" +
                "import java.util.Map;\n" +
                "import i5.las2peer.services.ocd.graphs.CustomGraph;\n" +
                "import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;\n" +
                "\n" +
                "public class " +  ocdaName +"Test implements "
                + baseInterfaceNamesToImplement + " {\n" +
                "\n" +
                "\tOcdAlgorithm algo;\n" +
                "\n" +
                "\t@BeforeEach\n" +
                "\tpublic void setup() {\n" +
                "\t\talgo = new "+ ocdaName +"();\n" +
                "\t}\n" +
                "\n" +
                "\t@Override\n" +
                "\tpublic OcdAlgorithm getAlgorithm() {\n" +
                "\t\treturn algo;\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "\t// ChatGPT Code \n" +
                "\n" +
                "\t/**\n" +
                "\t * " + AUTO_GENERATED_COMMENT_STRING + "\n" +
                "\t */\n" +
                "\t@Test\n" +
                "\tpublic void undirectedGraphTest1(){\n" +
                "\t\ttry {\n" +
                "\t\t\tCustomGraph undirectedGraph = OcdTestGraphFactory.getSawmillGraph(); // " + DONT_MODIFY_COMMENT_STRING
                +"\n" +
                "\n" +
                "\t\t\t//TODO: Set algorithm parameters here. To be completed by ChatGPT\n" +
                "\n" +
                "\t\t\tCover cover = getAlgorithm().detectOverlappingCommunities(undirectedGraph); // "
                + DONT_MODIFY_COMMENT_STRING +"\n" +
                "\n" +
                "\t\t} catch (Exception e){\n" +
                "\t\t\te.printStackTrace();\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "\t/**\n" +
                "\t * " + AUTO_GENERATED_COMMENT_STRING + "\n" +
                "\t */\n" +
                "\t@Test\n" +
                "\tpublic void directedGraphTest1(){\n" +
                "\t\ttry {\n" +
                "\t\t\tCustomGraph directedGraph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph(); // "
                + DONT_MODIFY_COMMENT_STRING + "\n" +
                "\n" +
                "\t\t\t//TODO: Set algorithm parameters here. To be completed by ChatGPT\n" +
                "\n" +
                "\t\t\tCover cover = getAlgorithm().detectOverlappingCommunities(directedGraph); // "
                + DONT_MODIFY_COMMENT_STRING + "\n" +
                "\n" +
                "\t\t} catch (Exception e){\n" +
                "\t\t\te.printStackTrace();\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "\n" +
                "\t/**\n" +
                "\t * " + AUTO_GENERATED_COMMENT_STRING + "\n" +
                "\t */\n" +
                "\t@Test\n" +
                "\tpublic void weightedGraphTest1(){\n" +
                "\t\ttry {\n" +
                "\t\t\tCustomGraph weightedGraph = OcdTestGraphFactory.getTwoCommunitiesWeightedGraph(); // "
                + DONT_MODIFY_COMMENT_STRING +"\n" +
                "\n" +
                "\t\t\t//TODO: Set algorithm parameters here. To be completed by ChatGPT\n" +
                "\n" +
                "\n" +
                "\t\t\tCover cover = getAlgorithm().detectOverlappingCommunities(weightedGraph); // "
                + DONT_MODIFY_COMMENT_STRING +"\n" +
                "\n" +
                "\t\t} catch (Exception e){\n" +
                "\t\t\te.printStackTrace();\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\n" +
                "}\n" +
                "'.");



        // Additional important note on what to do in case of an error and to avoid multiple prompts
        stringBuilder.append("\n\nImportant: If the code is too long for you to process, concentrate on relevant parts " +
                "for the task at hand. You can also split the task into subtasks and then combine the results, if " +
                "you need to. Additionally, if an error occurs, instead of terminating, regenerate the response. " +
                "Do not ask me a follow up question. Your final response must be a full test class as " +
                "a downloadable file.");

        System.out.println(stringBuilder.toString()); //TODO:DELETE


        generateAndWriteFile(ocdaName+"-promptText.txt", stringBuilder.toString());


    }

    public static void main(String[] args) {
        File ocdaCode = new File(getOCDAPath("SskAlgorithm.java"));
        generateAndWritePromptString(ocdaCode);




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
    private static String generateUnitTestTemplates(File ocdaCode){

        StringBuilder unitTestTemplatesBuilder = new StringBuilder();


        // Map graph type strings to corresponding getter methods for fetching test graphs
        Map<String, String> graphTypeStringToTestNameMap = new HashMap<>();
        graphTypeStringToTestNameMap.put("GraphType.DIRECTED", PromptGenerator.DIRECTED_GRAPH_TEST_NAME);
        graphTypeStringToTestNameMap.put("GraphType.WEIGHTED", PromptGenerator.WEIGHTED_GRAPH_TEST_NAME);
        graphTypeStringToTestNameMap.put("GraphType.ZERO_WEIGHTS", PromptGenerator.ZERO_WEIGHTS_GRAPH_TEST_NAME);
        graphTypeStringToTestNameMap.put("GraphType.NEGATIVE_WEIGHTS", PromptGenerator.NEGATIVE_WEIGHTS_GRAPH_TEST_NAME);
        graphTypeStringToTestNameMap.put("GraphType.SELF_LOOPS", PromptGenerator.SELF_LOOPS_GRAPH_TEST_NAME);

        // Adding nonexistent Undirected graph type string to the map avoids code duplication when generating tests
        graphTypeStringToTestNameMap.put("GraphType.UNDIRECTED", PromptGenerator.UNDIRECTED_GRAPH_TEST_NAME);



        // parse compatible graph types for the algorithm (list entries are e.g. "GraphType.DIRECTED")
        List<String> compatibleGraphTypeStrings = OCDAParser.extractCompatibilities(ocdaCode);

        // Add artificial undirected type to create a test for an undirected graph without code duplication
        compatibleGraphTypeStrings.add("GraphType.UNDIRECTED");

        System.out.println(compatibleGraphTypeStrings);

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
     * Converts an underscore-separated string to camelCase format.
     * This method is particularly useful for transforming constant-style
     * (uppercase with underscores) variable names into camelCase variable names.
     * For example, an input string "ZERO_WEIGHTS" will be converted to "zeroWeights".
     *
     * @param s The underscore-separated string to be converted.
     * @return The camelCase version of the input string. If the input is empty
     *         or only contains underscores, an empty string is returned.
     */
    private static String toCamelCaseFromUnderscore(String s) {
        String[] parts = s.trim().split("_"); // Split on underscores
        if (parts.length == 0) return "";

        StringBuilder camelCaseString = new StringBuilder(parts[0].toLowerCase()); // First word stays as it is

        for (int i = 1; i < parts.length; i++) {
            camelCaseString.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1).toLowerCase());
        }

        return camelCaseString.toString();
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
                        "\tpublic void " + testName + "(){\n" +
                        "\t\ttry {\n" +
                        "\t\t\t" + generateGraphInstantiationString(compatibleGraphTypeString) + " // Don't modify\n" +
                        "\n" +
                        "\t\t\t//TODO: Set algorithm parameters here. To be completed by ChatGPT\n" +
                        "\n" +
                        "\t\t\t" + generateCoverInstantiationString(compatibleGraphTypeString) + " // Don't modify\n" +
                        "\n" +
                        "\t\t} catch (Exception e){\n" +
                        "\t\t\te.printStackTrace();\n" +
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
        return "Cover cover = " + PromptGenerator.OCDA_GETTER +"().detectOverlappingCommunities(" + generateGraphVariableName(compatibleGraphTypeString) + ");";

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
        Map<String, String> graphTypeToMethodNameMap = new HashMap<>();
        graphTypeToMethodNameMap.put("GraphType.DIRECTED", PromptGenerator.DIRECTED_GRAPH_METHOD_NAME);
        graphTypeToMethodNameMap.put("GraphType.WEIGHTED", PromptGenerator.WEIGHTED_GRAPH_METHOD_NAME);
        graphTypeToMethodNameMap.put("GraphType.ZERO_WEIGHTS", PromptGenerator.ZERO_WEIGHTS_GRAPH_METHOD_NAME);
        graphTypeToMethodNameMap.put("GraphType.NEGATIVE_WEIGHTS", PromptGenerator.NEGATIVE_WEIGHTS_GRAPH_METHOD_NAME);
        graphTypeToMethodNameMap.put("GraphType.SELF_LOOPS", PromptGenerator.SELF_LOOPS_GRAPH_METHOD_NAME);

        // Add artificial undirected type to instantiate an undirected graph without code duplication
        graphTypeToMethodNameMap.put("GraphType.UNDIRECTED", PromptGenerator.UNDIRECTED_GRAPH_METHOD_NAME);



        String methodName = graphTypeToMethodNameMap.get(compatibleGraphTypeString);

        // create a string of graph variable declaration depending on the graph type
        if (methodName != null) {

            String InstantiationString = PromptGenerator.GRAPH_CLASS_NAME + " " + generateGraphVariableName(compatibleGraphTypeString)+ " = "
                    + PromptGenerator.GRAPH_FACTORY_CLASS_NAME + "." + methodName + "();";

            return InstantiationString;

        }
        return "";
    }


    /**
     * Creates a variable name for a graph instantiation to be used in unit tests based on the provided graph type string.
     * This method processes a graph type string (expected to be in a format like "GraphType.DIRECTED") and converts the
     * portion after the dot into camelCase, appending "Graph" at the end. For instance, "GraphType.DIRECTED" would be
     * converted to "directedGraph". This is particularly useful for generating descriptive and consistent variable names
     * in unit test code.
     *
     * @param compatibleGraphTypeString A string representing the graph type, in a format like "GraphType.SOMETYPE".
     * @return A string that represents the camelCase variable name for the graph, suitable for use in unit test code.
     */
    private static String generateGraphVariableName(String compatibleGraphTypeString){
        return toCamelCaseFromUnderscore(compatibleGraphTypeString.split("\\.")[1]) + "Graph";
    }



    /**
     * Generates a file with the specified filename and writes the provided content to it.
     * If a file with the given filename already exists, it will be overwritten. The method
     * creates a new file and uses a BufferedWriter to write the content. In case of any
     * IOExceptions, the exception is caught and its stack trace is printed.
     *
     * @param filename The name of the file to be created or overwritten.
     * @param content  The content to be written into the file.
     */
    private static void generateAndWriteFile(String filename, String content) {
        try {

            File directory = new File("chatgpt_prompts");
            if (!directory.exists()){
                directory.mkdir(); // Create the directory if it does not exist
            }

            // Create a File object
            File file = new File(directory,filename);

            // Check if the file already exists
            if (file.exists()) {
                // Delete the existing file
                file.delete();
            }

            // Create a new file with the same name
            file.createNewFile();

            // Create a BufferedWriter to write to the new file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(content);
                System.out.println("File '" + filename + "' has been generated and written.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
