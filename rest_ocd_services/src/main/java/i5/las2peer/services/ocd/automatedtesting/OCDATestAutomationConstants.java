package i5.las2peer.services.ocd.automatedtesting;


import java.util.HashMap;
import java.util.Map;

public class OCDATestAutomationConstants {


    // Text used to annotate lines that should not be modified
    public static String DONT_MODIFY_COMMENT_STRING = "Don't modify";

    // Text used to annotate unit test comments indicating unit test is generated/completed by ChatGPT
    public static String AUTO_GENERATED_COMMENT_STRING = "Completed by GPT";

    // Test OCDA test class package name
    public static String TEST_CLASS_PACKAGE_STRING = "i5.las2peer.services.ocd.algorithms";

    // Base test interface package name
    public static String BASE_TEST_INTERFACES_PACKAGE_STRING = "i5.las2peer.services.ocd.ocdatestautomation.test_interfaces";

    /*  Constants related to test names that must be auto-completed by ChatGPT */

    public static String UNDIRECTED_GRAPH_TEST_NAME = "undirectedGraphTest1";

    public static String DIRECTED_GRAPH_TEST_NAME = "directedGraphTest1";

    public static String WEIGHTED_GRAPH_TEST_NAME = "weightedGraphTest1";

    public static String ZERO_WEIGHTS_GRAPH_TEST_NAME = "zeroWeightsGraphTest1";

    public static String NEGATIVE_WEIGHTS_GRAPH_TEST_NAME = "negativeWeightsGraphTest1";

    public static String SELF_LOOPS_GRAPH_TEST_NAME = "selfLoopsGraphTest1";

    /*  Constants related to graph types */
    // Method names to fetch graphs to be used in auto-generated tests
    //TODO: add/replace method names
    public static String UNDIRECTED_GRAPH_METHOD_NAME = "getUndirectedBipartiteGraph";
    public static String DIRECTED_GRAPH_METHOD_NAME = "getDirectedAperiodicTwoCommunitiesGraph";
    public static String WEIGHTED_GRAPH_METHOD_NAME = "getTwoCommunitiesWeightedGraph";

    public static String ZERO_WEIGHTS_GRAPH_METHOD_NAME = "getZeroAndNonZeroWeightMixGraph";

    public static String NEGATIVE_WEIGHTS_GRAPH_METHOD_NAME = "getMixedWeightsGraph";

    public static String SELF_LOOPS_GRAPH_METHOD_NAME = "getBipartiteGraphWithSelfLoops";

    // Name of the class where the test graphs are stored or generated
    public static String GRAPH_FACTORY_CLASS_NAME = "i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory";

    public static String AUTO_GENERATED_TEST_CLASS_PACKAGE = "i5.las2peer.services.ocd.ocdatestautomation.testrunner.generatedclasses";

    // Name of the shared method among OCDA test classes to get an algorithm instance
    public static String OCDA_GETTER = "getAlgorithm";

    // Name of the graph class used in WebOCD
    public static String GRAPH_CLASS_NAME = "CustomGraph";

    // Name of the method to set algorithm parameters
    public static String SET_OCD_PARAMETERS_METHOD_NAME = "setParameters";

    /* File paths */

    // Location of auto-generated GPT files that should be checked if they can be successfully parsed
    public static String GPT_GENERATED_TEST_CLASS_CODE_LOCATION = "gpt/classfiles/";

    // Location of gpt output prompt that includes unit tests which should be extracted.
    public static String GPT_GENERATED_UNPROCESSED_UNIT_TESTS_LOCATION = "gpt/unit_tests_unprocessed/";


    // Location of auto-generated GPT files that were successfully parsed and move do be checked for successful execution
    public static String PARSED_GPT_GENERATED_CODE_LOCATION = "rest_ocd_services/src/test/java/i5/las2peer/services/ocd/ocdatestautomation/testrunner/generatedclasses/";

    // Location of OCD algorithms in the main and test source sets
    public static String OCDA_LOCATION_IN_MAIN_SOURCE_SET = "rest_ocd_services/src/main/java/i5/las2peer/services/ocd/algorithms/";
    public static String OCDA_LOCATION_IN_TEST_SOURCE_SET = "rest_ocd_services/src/test/java/i5/las2peer/services/ocd/algorithms/";

    // Number of diverse parameter values that should be generated (using GPT) for OCD accuracy tests
    public static String OCDA_PARAMETER_GENERATION_COUNT_FOR_OCD_ACCURACY_TESTS = "5";

    // Suffix of the name where GPT generated tests are stored for further processing. Prefix is the algorithm name. E.g. SskAlgorithm_gpt_generated_tests.txt
    public static String GPT_GENERATED_TEST_FILE_NAME_SUFFIX = "_gpt_generated_tests.txt";


    // Suffix of the name where GPT generated CODA parameters are stored for further processing. Prefix is the algorithm name. E.g. SskAlgorithm_gpt_generated_ocda_parameters.txt
    public static String GPT_GENERATED_OCDA_PARAMETERS_FILE_NAME_SUFFIX = "_gpt_generated_ocda_parameters.txt";



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
    public static Map<String, String> getGraphTypeBasedTestNames(){

        Map<String, String> graphTypeToTestNameMap = new HashMap<>();

        graphTypeToTestNameMap.put("GraphType.DIRECTED", DIRECTED_GRAPH_TEST_NAME);
        graphTypeToTestNameMap.put("GraphType.WEIGHTED", WEIGHTED_GRAPH_TEST_NAME);
        graphTypeToTestNameMap.put("GraphType.NEGATIVE_WEIGHTS", NEGATIVE_WEIGHTS_GRAPH_TEST_NAME);
        graphTypeToTestNameMap.put("GraphType.SELF_LOOPS", SELF_LOOPS_GRAPH_TEST_NAME);
        graphTypeToTestNameMap.put("GraphType.ZERO_WEIGHTS", ZERO_WEIGHTS_GRAPH_TEST_NAME);

        // While undirected graph doesn't exist as a separate GraphType, each OCDA is compatible with undirected graphs.
        graphTypeToTestNameMap.put("GraphType.UNDIRECTED", UNDIRECTED_GRAPH_TEST_NAME);

        return graphTypeToTestNameMap;
    }


    /**
     * Returns a map of graph types to their corresponding method names for fetching test graphs.
     * This method is used to easily retrieve the method names associated with different types of graphs,
     * facilitating the generation of specific graph instances for testing purposes.
     *
     * The map includes entries for various graph types, such as directed, weighted, and graphs with special
     * characteristics like zero weights, negative weights, or self-loops. Additionally, it includes an
     * artificial entry for an undirected graph type, since while this type does not exist in WebOCD, all
     * OCD algorithms are compatible with an undirected graphs in WebOCD.
     *
     * The method names are defined as constants and are used in the generation of test classes and
     * automated testing procedures.
     *
     * @return A Map where each key is a string representing a graph type (e.g., "GraphType.DIRECTED"),
     *         and each value is a string representing the corresponding method name used to generate
     *         that type of graph (e.g., "getDirectedAperiodicTwoCommunitiesGraph").
     */
    public static Map<String, String> getGraphTypeToMethodNameMap(){

        // map graph types to corresponding getter methods for fetching test graphs
        Map<String, String> graphTypeToMethodNameMap = new HashMap<>();
        graphTypeToMethodNameMap.put("GraphType.DIRECTED", DIRECTED_GRAPH_METHOD_NAME);
        graphTypeToMethodNameMap.put("GraphType.WEIGHTED", WEIGHTED_GRAPH_METHOD_NAME);
        graphTypeToMethodNameMap.put("GraphType.ZERO_WEIGHTS", ZERO_WEIGHTS_GRAPH_METHOD_NAME);
        graphTypeToMethodNameMap.put("GraphType.NEGATIVE_WEIGHTS", NEGATIVE_WEIGHTS_GRAPH_METHOD_NAME);
        graphTypeToMethodNameMap.put("GraphType.SELF_LOOPS", SELF_LOOPS_GRAPH_METHOD_NAME);

        // Add artificial undirected type to instantiate an undirected graph without code duplication
        graphTypeToMethodNameMap.put("GraphType.UNDIRECTED", UNDIRECTED_GRAPH_METHOD_NAME);

        return graphTypeToMethodNameMap;
    }


}
