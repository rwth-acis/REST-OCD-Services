package i5.las2peer.services.ocd.automatedtesting.ocdparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.*;

import com.github.javaparser.JavaParser;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;


import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

public class OCDAParser {


    // Name of the method that holds compatible graph types of OCDA
    private static final String COMPATIBLE_GRAPH_TYPE_METHOD_NAME = "compatibleGraphTypes";


    // Declare javaParser as a class variable
    private static JavaParser javaParser = JavaParserSingleton.getInstance();


    /**
     * @param ocdaFileName      Name of the OCDA class file
     * @return                  String representation of the path to the OCDA class
     */
    public static String getOCDAPath(String ocdaFileName){
        // path starting with 'rest_ocd_services'
        String relativeFilePath = "rest_ocd_services/src/main/java/i5/las2peer/services/ocd/algorithms/" + ocdaFileName;

        // Get the current working directory (content root)
        String contentRoot = System.getProperty("user.dir");

        // Create the absolute file path
        String absoluteFilePath = contentRoot + File.separator + relativeFilePath;

        return absoluteFilePath;
    }

    /**
     * @param ocdaTestFileName      Name of the OCDA test class file
     * @return                  String representation of the path to the OCDA class
     */
    public static String getOCDATestPath(String ocdaTestFileName){
        // path starting with 'rest_ocd_services'
        String relativeFilePath = "rest_ocd_services/src/test/java/i5/las2peer/services/ocd/algorithms/" + ocdaTestFileName;

        // Get the current working directory (content root)
        String contentRoot = System.getProperty("user.dir");

        // Create the absolute file path
        String absoluteFilePath = contentRoot + File.separator + relativeFilePath;

        return absoluteFilePath;
    }



    /**
     * Use the singleton JavaParser to parse the Java file
     * @return Compulation unit of the parsed file
     */
    private static CompilationUnit parseJavaFile(File file) {
        try {
            return javaParser.parse(file).getResult().orElseThrow(() -> new RuntimeException("Parsing failed"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @return class name from the CompilationUnit
      */
    private static String getClassName(CompilationUnit cu) {
        return cu.findFirst(ClassOrInterfaceDeclaration.class)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElse("No class name found");
    }

    /**
     * Parses a given Java file and returns a list of parsing error messages.
     *
     * @param file The Java file to be parsed.
     * @return A list of error messages. Returns an empty list if no errors are found.
     */
    public static List<String> getParsingErrors(File file) {
        try {
            ParseResult<CompilationUnit> parseResult = javaParser.parse(file);

            if (!parseResult.isSuccessful()) {
                return parseResult.getProblems().stream()
                        .map(problem -> {
                            TokenRange tokenRange = problem.getLocation().orElse(null);
                            String location = (tokenRange != null) ? "Line " + tokenRange.getBegin().getRange().get().begin.line + ": " : "";
                            return location + problem.getMessage();
                        })
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return an empty list if there are no parsing errors
        return List.of();
    }

    public static void main(String[] args) {

        try {

            // ===== Parsing OCD =====

            /* Parse the OCDA class file */
            File file = new File(getOCDAPath("SskAlgorithm.java"));
            CompilationUnit compilationUnit = parseJavaFile(file);


            /* Identify compatible graph types for a parsed OCDA */
            List<String> compatibilities = extractCompatibilities(file);

            /* Generate Chat-GPT prompt for a given OCDA that can be used to create an OCDA test class */
            PromptGenerator.generatePromptString(compatibilities,getClassName(compilationUnit));

            // ==== Parse file for compilation errors ===
//            File fileBasic = new File("someFile.java"); //TODO: decide where to put files to parse
//            System.out.println("compilation errors list: "+ getParsingErrors(fileBasic));


            // ===== RUNTIME =====

            /* Parse the OCDA Test class file */
            File testFile = new File(getOCDATestPath("SSKAlgorithmTest.java"));
            CompilationUnit compilationUnitTest = parseJavaFile(testFile);

            /* Check if the test class is executable or if exception is thrown */
            OCDTestRunner.runCompiledTestClassWithJUnit5(compilationUnitTest, testFile);


        } catch (Exception e) {
            System.err.println("Error parsing file: " + e.getMessage());
        }
    }


    /**
     * Gets method calls and corresponding method call lines for a specified java class (file)
     * @param javaFile    Java class the method calls of which to list
     */
    public static void listMethodCalls(File javaFile) {
        try {
            JavaParser javaParser = JavaParserSingleton.getInstance();
            new VoidVisitorAdapter<Object>() {
                @Override
                public void visit(MethodCallExpr n, Object arg) {
                    super.visit(n, arg);
                    System.out.println(" [L " + n.getBegin().get().line + "] " + n);
                }
            }.visit(javaParser.parse(javaFile).getResult().get(), null);
        } catch ( IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Gets method calls and corresponding method call lines for a specified method within a specified
     * Java class.
     * @param javaFile         Java class to parse
     * @param targetMethodName Method within the Java class whose methods should be listed
     */
    public static void listMethodCallsInMethod(File javaFile, String targetMethodName) {
        try {
            javaParser.parse(javaFile).getResult().ifPresent(cu -> {
                Optional<MethodDeclaration> method = cu.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals(targetMethodName));

                method.ifPresent(m -> {
                    new VoidVisitorAdapter<Object>() {
                        @Override
                        public void visit(MethodCallExpr n, Object arg) {
                            super.visit(n, arg);
                            System.out.println(" [L " + n.getBegin().get().line + "] " + n);
                        }
                    }.visit(m, null);
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Identifies compatible graph types for a given OCDA algorithm
     * @param javaFile        OCDA algorithm class file
     * @return                List of compatible graph types with the specified OCDA
     */
    public static List<String> extractCompatibilities(File javaFile) {
        List<String> compatibilities = new ArrayList<>();
        try {
            javaParser.parse(javaFile).getResult().ifPresent(cu -> {
                Optional<MethodDeclaration> method = cu.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals(COMPATIBLE_GRAPH_TYPE_METHOD_NAME));

                method.ifPresent(m -> {
                    m.findAll(MethodCallExpr.class).stream()
                            .filter(mc -> mc.getNameAsString().equals("add"))
                            .forEach(mc -> {
                                mc.getArguments().forEach(arg -> {
                                    compatibilities.add(arg.toString());
                                });
                            });
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return compatibilities;
    }


    /**
     * Identifies methods defined within the OCDA algorithm
     * @param javaFile       OCDA algorithm class file
     * @return               List of methods defined within the specified OCDA
     */
    public static List<String> extractMethods(File javaFile)  {

        try {
            CompilationUnit cu = javaParser.parse(javaFile).getResult().get();
            List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
            return methods.stream()
                    .map(MethodDeclaration::getNameAsString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
