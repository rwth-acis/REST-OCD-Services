package i5.las2peer.services.ocd.ocdatestautomation;

import i5.las2peer.services.ocd.automatedtesting.ocdparser.OCDAParser;
import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.*;

import java.io.File;
import java.util.List;

import static i5.las2peer.services.ocd.automatedtesting.OCDATestAutomationConstants.*;
import static i5.las2peer.services.ocd.automatedtesting.helpers.OCDWriter.generateAndWriteFile;
import static i5.las2peer.services.ocd.automatedtesting.ocdparser.OCDAParser.*;
import static i5.las2peer.services.ocd.automatedtesting.PromptGenerator.*;

public class OCDATestClassGenerator {


    /**
     * Generates and writes a test class file for a specified OCD algorithm. This method dynamically constructs a test
     * class based on the characteristics and compatible graph types of the given OCD algorithm. The generated test
     * class implements appropriate test interfaces depending on the graph types compatible with the OCD algorithm.
     * For instance, OCD algorithms compatible with directed graphs will implement the DirectedGraphTestReq interface.
     * The created unit tests within the test class are partially complete and are designed to be finalized using a GPT.
     *
     * @param ocdaCode The file containing the OCD algorithm code. This file is parsed to determine the algorithm's
     *                  characteristics and compatible graph types.
     *
     * @implNote The method performs several key functions:
     * 1. Extracts the OCD algorithm name from the class file.
     * 2. Identifies compatible graph types for the algorithm.
     * 3. Determines the interfaces to be implemented by the test class based on the algorithm's compatibilities.
     * 4. Constructs the test class by appending necessary import statements, interface implementations, and partially completed unit tests.
     * 5. Writes the constructed test class to a Java file.
     *
     * The generated test class includes standard setup for unit tests, such as a setup method annotated with @BeforeEach.
     * The generated code section is marked with "ChatGPT Code" to indicate where the GPT should complete the unit tests.
     */
    public static void generateAndWriteOCDTestClass(File ocdaCode){

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


        // Iterate through the list of compatible graph type strings and add corresponding interfaces
        // to the interfaces that the test class implements
        for (String graphType : compatibilities) {
            if (graphType.equals("GraphType.DIRECTED")){
                // test interface for directed graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + DirectedGraphTestReq.class.getSimpleName();
            }
            if (graphType.equals("GraphType.WEIGHTED")){
                // test interface for directed graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + WeightedGraphTestReq.class.getSimpleName();

            }
            if (graphType.equals("GraphType.ZERO_WEIGHTS")){
                // test interface for zero weight graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + ZeroWeightsGraphTestReq.class.getSimpleName();

            }
            if (graphType.equals("GraphType.NEGATIVE_WEIGHTS")){
                // test interface for negative weights graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + NegativeWeightsGraphTestReq.class.getSimpleName();

            }
            if (graphType.equals("GraphType.SELF_LOOPS")){
                // test interface for self loops graphs should be implemented in the OCDA test class
                baseInterfaceNamesToImplement+= ", " + SelfLoopsGraphTestReq.class.getSimpleName();

            }

        }

        // Merge import statements from the OCD algorithm class and its test class, since the algorithm class
        // imports might be needed for the test class if the user wants to test OCD algorithm methods.
        List<String> mergedImports = mergeAndSortLists(extractSortedImports(ocdaCode),
                generateTestClassImports(ocdaCode));



        /* Generate partially completed test class */


        // Append package to the test class
        stringBuilder.append("package " + TEST_CLASS_PACKAGE_STRING + ";").append("\n\n");

        // Append import statements to the test class
        mergedImports.forEach(importStatement -> stringBuilder.append(importStatement).append("\n"));

        // Add interface implementation and partially completed unit tests to the test class,
        // based on compatible graph types.
        stringBuilder.append("\n\n" +
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
                generatePartiallyCompletedGraphTypeTests(ocdaCode)+ "\n" +
                "}\n"
        );

        // Write test class to a file
        generateAndWriteFile("gpt","classfiles/"+ocdaName+"Test.java", stringBuilder.toString());


    }


    public static void main(String[] args) {
        File ocdaCode = new File(getOCDAPath("SskAlgorithm.java"));
        // Generate OCD algorithm test class with partially completed unit tests
        generateAndWriteOCDTestClass(ocdaCode);
    }
}
