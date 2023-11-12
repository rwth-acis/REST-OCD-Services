package i5.las2peer.services.ocd.automatedtesting.ocdparser;

import i5.las2peer.services.ocd.test_interfaces.ocda.DirectedGraphTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.OCDAParameterTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.UndirectedGraphTestReq;
import i5.las2peer.services.ocd.test_interfaces.ocda.WeightedGraphTestReq;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PromptGenerator {



    // Method names to fetch graphs to be used in auto-generated tests
    //TODO: add/replace method names
    public static String UNDIRECTED_GRAPH_METHOD_NAME = "getSawmillGraph()";
    public static String DIRECTED_GRAPH_METHOD_NAME = "getDirectedAperiodicTwoCommunitiesGraph()";
    public static String WEIGHTED_GRAPH_METHOD_NAME = "getTwoCommunitiesWeightedGraph()";

    public static String ZERO_WEIGHTS_GRAPH_METHOD_NAME = ""; //TODO: replace string with appropriate method name

    public static String NEGATIVE_WEIGHTS_GRAPH_METHOD_NAME = ""; //TODO: replace string with appropriate method name

    public static String SELF_LOOPS_GRAPH_METHOD_NAME = ""; //TODO: replace string with appropriate method name

    // Name of the class where the test graphs are stored or generated
    public static String GRAPH_FACTORY_CLASS_NAME = "OcdTestGraphFactory";

    // Name of the graph class used in WebOCD
    public static String GRAPH_CLASS_NAME = "CustomGraph";

    /**
     * Generates a prompt for a given OCDA and its compatible graph types. This prompt can be used as an input
     * to ChatGPT. The output should be a downloadable file representing a test class for the specified ocda.
     * Since the code will be auto-generated, it should be reviewed manually.
     * @param compatibilities       List of compatible graph types for OCDA
     * @param ocdaName              Name of the OCDA for which the prompt should be generated
     */
    public static void generatePromptString(List<String> compatibilities, String ocdaName){

        // string that will hold interface names that an OCDA test class should implement
        String baseInterfaceNamesToImplement = "";

        // all OCDA test classes implement test interface for algorithm parameters and undirected graph type
        baseInterfaceNamesToImplement += OCDAParameterTestReq.class.getSimpleName()
                + ", " + UndirectedGraphTestReq.class.getSimpleName();



        // Initialize an empty StringBuilder to concatenate strings
        StringBuilder stringBuilder = new StringBuilder();

        // Initial text for ChatGPT instructing what to do
        stringBuilder.append("I have a framework for community detection algorithms written in Java. " +
                "I will give you code of one such algorithm called " + ocdaName +
                ". Your task is to generate unit tests for the given algorithm and provide me the test " +
                "class with the unit tests inside as a downloadable file. In each unit test the algorithm " +
                "should be executed on some test graph that you can get using OcdTestGraphFactory " +
                "as described below: \n");


        // all OCDA are compatible with undirected networks
        stringBuilder.append("- 3 tests should use an undirected graph. You can get this graph by executing '"
                + GRAPH_CLASS_NAME + " undirectedGraph = " + GRAPH_FACTORY_CLASS_NAME
                + "." + UNDIRECTED_GRAPH_METHOD_NAME + ";'").append("\n");

        // Iterate through the list and generate strings based on values
        for (String graphType : compatibilities) {
            if (graphType.equals("GraphType.DIRECTED")){

                // test interface for directed graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + DirectedGraphTestReq.class.getSimpleName();

                // instruction to generate a test used on a directed graph
                stringBuilder.append("- 1 test should use a directed graph. You can get this graph by executing '"
                        + GRAPH_CLASS_NAME + " directedGraph = " + GRAPH_FACTORY_CLASS_NAME
                        + "." + DIRECTED_GRAPH_METHOD_NAME + ";'").append("\n");
            }
            if (graphType.equals("GraphType.WEIGHTED")){

                // test interface for directed graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + WeightedGraphTestReq.class.getSimpleName();

                // instruction to generate a test used on a weighted graph
                stringBuilder.append("- 1 test should use a weighted graph. You can get this graph by executing '"
                        + GRAPH_CLASS_NAME + " weightedGraph = " + GRAPH_FACTORY_CLASS_NAME
                        + "." + WEIGHTED_GRAPH_METHOD_NAME + ";'").append("\n");
            }
            if (graphType.equals("GraphType.ZERO_WEIGHTS")){
                //TODO:
            }
            if (graphType.equals("GraphType.NEGATIVE_WEIGHTS")){
                //TODO:
            }
            if (graphType.equals("GraphType.SELF_LOOPS")){
                //TODO:
            }

        }

        // algorithm execution instruction
        stringBuilder.append("\nTo execute the algorithm on a graph use " +
                "'getAlgorithm.detectOverlappingcommunities(graph)'. Each algorithm has a set of parameters. " +
                "You must find and set reasonable parameter values in each unit test. To set parameter values " +
                "use setParameters(Map<String, String> parameters).  Below is a code snippet for some algorithm " +
                "called SomeAlgorithm that has parameters \"seedCount\" and \"accuracy\": ");

        // example OCDA code with algorithm parameters
        stringBuilder.append("\n'\n" +
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
                "'.");

        // clarification to ensure parameter name constants and parameters are not confused
        stringBuilder.append("\nNote that in the line \"protected static final String SEED_COUNT_NAME " +
                "= \"seedCount\";\" the parameter is \"seedCount\" and not SEED_COUNT_NAME, so when setting " +
                "parameter value, \"seedCount\" would be used in this case.");


        // explanation of the test generation example that follows
        stringBuilder.append("\nBelow is a code snippet example of how a test on an undirected graph could be " +
                "generated for SomeAlgorithm. Notice how parameters are set using the setParameters method:\n");


        // test generation example
        stringBuilder.append("\n'\n" +
                "import i5.las2peer.services.ocd.adapters.AdapterException;\n" +
                "import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;\n" +
                "import i5.las2peer.services.ocd.graphs.Cover;\n" +
                "import i5.las2peer.services.ocd.graphs.CustomGraph;\n" +
                "import i5.las2peer.services.ocd.metrics.OcdMetricException;\n" +
                "import i5.las2peer.services.ocd.test_interfaces.ocda.OCDAParameterTestReq;\n" +
                "import i5.las2peer.services.ocd.test_interfaces.ocda.UndirectedGraphTestReq;\n" +
                "import i5.las2peer.services.ocd.test_interfaces.ocda.WeightedGraphTestReq;\n" +
                "import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;\n" +
                "\n" +
                "import java.io.FileNotFoundException;\n" +
                "import java.util.HashMap;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "import org.junit.jupiter.api.BeforeEach;\n" +
                "import org.junit.jupiter.api.Disabled;\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "\n" +
                "\n" +
                "public class SomeAlgorithmTest implements UndirectedGraphTestReq {\n" +
                "\n" +
                "\n" +
                "\tOcdAlgorithm algo;\n" +
                "\n" +
                "\t@BeforeEach\n" +
                "\tpublic void setup() {\n" +
                "\t\talgo = new SomeAlgorithm();\n" +
                "\t}\n" +
                "\n" +
                "\t@Override\n" +
                "\tpublic OcdAlgorithm getAlgorithm() {\n" +
                "\t\treturn algo;\n" +
                "\t}\n" +
                "\n" +
                "\t@Test\n" +
                "\tpublic void testOnUndirectedGraph() {\n" +
                "\t\t\n" +
                "\t\ttry {\n" +
                "\t\t\tCustomGraph undirectedGraph = OcdTestGraphFactory.getSawmillGraph();\n" +
                "\t\t\tMap<String, String> parameters = new HashMap<String, String>();\n" +
                "\t\t\tparameters.put(\"seedCount\", \"5\");\n" +
                "\t\t\tparameters.put(\"accuracy\", \"0.0000001\");\n" +
                "\t\t\tgetAlgorithm.setParameters(parameters);\n" +
                "\t\t\tCover cover = getAlgorithm.detectOverlappingCommunities(undirectedGraph);\n" +
                "\t\t} catch(Exception e){\n" +
                "\t\t\te.printStackTrace();\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}\n" +
                "'.");

        // explanation on where to write unit tests
        stringBuilder.append("\nIt is important that your output file includes full and functional unit test code " +
                "you generated. All unit tests you generate should be located below the comment /* ChatGPT Code */ " +
                "in the following code:\n");

        // template that ChatGPT should fill with the code
        stringBuilder.append("\n'\n" +
                "package i5.las2peer.services.ocd.algorithms;\n" +
                "\n" +
                "import static org.junit.jupiter.api.Assertions.assertEquals;\n" +
                "import static org.junit.jupiter.api.Assertions.assertTrue;\n" +
                "\n" +
                "import i5.las2peer.services.ocd.test_interfaces.ocda.DirectedGraphTestReq;\n" +
                "import i5.las2peer.services.ocd.test_interfaces.ocda.OCDAParameterTestReq;\n" +
                "import i5.las2peer.services.ocd.test_interfaces.ocda.UndirectedGraphTestReq;\n" +
                "import i5.las2peer.services.ocd.test_interfaces.ocda.WeightedGraphTestReq;\n" +
                "import org.junit.jupiter.api.BeforeEach;\n" +
                "import org.junit.jupiter.api.Disabled;\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "\n" +
                "import i5.las2peer.services.ocd.adapters.AdapterException;\n" +
                "import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;\n" +
                "import i5.las2peer.services.ocd.graphs.Cover;\n" +
                "import i5.las2peer.services.ocd.graphs.CustomGraph;\n" +
                "import i5.las2peer.services.ocd.metrics.OcdMetricException;\n" +
                "import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;\n" +
                "\n" +
                "import java.io.FileNotFoundException;\n" +
                "import java.util.Map;\n" +
                "import java.util.HashMap;\n" +
                "\n" +
                "import org.la4j.matrix.Matrix;\n" +
                "import org.la4j.vector.Vector;\n" +
                "import org.la4j.vector.Vectors;\n" +
                "\n" +
                "import org.graphstream.graph.Node;\n" +
                "public class " +  ocdaName +"Test implements "
                + baseInterfaceNamesToImplement + " {\n" +
                "\n" +
                "\tOcdAlgorithm algo;\n" +
                "\n" +
                "\t@BeforeEach\n" +
                "\tpublic void setup() {\n" +
                "\t\talgo = new " + ocdaName +"();\n" +
                "\t}\n" +
                "\n" +
                "\t@Override\n" +
                "\tpublic OcdAlgorithm getAlgorithm() {\n" +
                "\t\treturn algo;\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "\t/* ChatGPT Code */\n" +
                "}\n" +
                "'.");


        // instructions to follow when generating tests
        stringBuilder.append("\nEnsure the following: \n" +
                "1. I want your tests to test different reasonable parameter values for the parameters you found. " +
                "Don't use default parameter values. \n" +
                "2. Make sure to have a try catch block in each unit test to catch potential exceptions\n" +
                "3. Ensure to add comment annotation on each test mentioning the test is auto-generated and " +
                "should be manually reviewed. \n" +
                "4. Ensure to add a javadoc comment explaining what the unit test tests and why you chose " +
                "the parameter values you chose.\n" +
                "5. Make sure to correctly use the algorithm parameter names. Parameters are defined at the " +
                "beginning of the algorithm code. Parameter names are defined using modifiers \"protected static " +
                "final String\". Parameters can be set using setParameters(Map<String, String> parameters) method " +
                "where the key is the parameter name and value is the parameter value. Parameters can be retrieved " +
                "using public Map<String, String> getParameters().\n" +
                "6. In each unit test you generate, you should set all algorithm parameters to reasonable values. " +
                "Parameter values used in each test should differ from each other.\n" +
                "7. Ensure there are no duplicate unit tests.");


        // additional important note to avoid multiple prompts mentioning how input is too long
        stringBuilder.append("\nimportant: If the code is too long for you to process, concentrate on relevant " +
                "parts for the task at hand. Do not ask me a follow up question. Your final response must be a " +
                "full test class as a downloadable file.");

        // System.out.println("final result \n"+ stringBuilder.toString());

        generateAndWriteFile(ocdaName+"-promptText.txt", stringBuilder.toString());


    }


    /**
     *  Method to generate a file with the given filename and write the provided content.
     *  If the file exists, it will be overwritten.
     */
    private static void generateAndWriteFile(String filename, String content) {
        try {
            // Create a File object
            File file = new File(filename);

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
