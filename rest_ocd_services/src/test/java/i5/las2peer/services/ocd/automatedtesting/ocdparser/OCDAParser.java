package i5.las2peer.services.ocd.automatedtesting.ocdparser;


import java.io.*;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.type.Type;

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
     * @return class name from the class file
     */
    public static String getClassName(File file) {
        return getClassName(parseJavaFile(file));
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
            PromptGenerator.generateAndWritePromptString(file);


//
//            // ==== Parse file for compilation errors ===
////            File fileBasic = new File("someFile.java"); //TODO: decide where to put files to parse
////            System.out.println("compilation errors list: "+ getParsingErrors(fileBasic));
//
//
//            // ===== RUNTIME =====
//
//            /* Parse the OCDA Test class file */
//            File testFile = new File(getOCDATestPath("SskAlgorithmTest.java"));
//            CompilationUnit compilationUnitTest = parseJavaFile(testFile);
//
//            /* Check if the test class is executable or if exception is thrown */
//            OCDTestRunner.runCompiledTestClassWithJUnit5(compilationUnitTest, testFile);


//            /////////////////
//            File testFile = new File(getOCDATestPath("SskAlgorithmTest.java"));
//            CompilationUnit compilationUnit = parseJavaFile(testFile);
//
//            String res = getFullClassDeclaration(testFile);
//            System.out.println("HELLO " + res);




        } catch (Exception e) {
            System.err.println("Error parsing file: " + e.getMessage());
        }
    }


    /**
     * Gets method calls and corresponding method call lines for a specified java class (file).
     * The lines can be omitted by setting a boolean flag.
     * @param javaFile    Java class the method calls of which to list
     * @param includeLines Whether to include code lines or not
     *
     * @return             A list of strings of method calls within a specified class file
     */
    public static ArrayList<String> listMethodCalls(File javaFile, Boolean includeLines) {
        ArrayList<String> methodCallList = new ArrayList<>();
        try {
            JavaParser javaParser = JavaParserSingleton.getInstance();
            new VoidVisitorAdapter<Object>() {
                @Override
                public void visit(MethodCallExpr n, Object arg) {
                    super.visit(n, arg);
                    if(includeLines) {
                        methodCallList.add(" [L " + n.getBegin().get().line + "] " + n);
                        System.out.println(" [L " + n.getBegin().get().line + "] " + n);
                    }else {
                        methodCallList.add(n.toString());
                        System.out.println(n);
                    }
                }
            }.visit(javaParser.parse(javaFile).getResult().get(), null);
        } catch ( IOException e) {
            throw new RuntimeException(e);
        }

        return methodCallList;
    }


    /**
     * Gets method calls and corresponding method call lines for a specified method within a specified
     * Java class.
     * @param javaFile         Java class to parse
     * @param targetMethodName Method within the Java class whose methods should be listed
     * @param includeLines     Whether to include the line numbers
     *
     *@return                  A list of strings of method calls within a specified method
     */
    public static ArrayList<String> listMethodCallsInMethod(File javaFile, String targetMethodName, Boolean includeLines) {
        ArrayList<String> methodCallList = new ArrayList<>();
        try {
            javaParser.parse(javaFile).getResult().ifPresent(cu -> {
                Optional<MethodDeclaration> method = cu.findFirst(MethodDeclaration.class,
                        m -> m.getNameAsString().equals(targetMethodName));

                method.ifPresent(m -> {
                    new VoidVisitorAdapter<Object>() {
                        @Override
                        public void visit(MethodCallExpr n, Object arg) {
                            super.visit(n, arg);
                            if (includeLines) {
                                methodCallList.add(" [L " + n.getBegin().get().line + "] " + n);
                                //System.out.println(" [L " + n.getBegin().get().line + "] " + n);
                            } else {
                                methodCallList.add(n.toString());
                                //System.out.println(n);
                            }
                        }
                    }.visit(m, null);
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return methodCallList;
    }

    /**
     * Lists lines within a specified method of a Java class that are annotated with a specific comment.
     * This method parses the given Java file, locates the specified method, and then iterates through
     * each line of that method to check for the presence of the specified annotation comment at the end.
     * Optionally, it can include the line numbers in the output.
     *
     * @param javaFile         The Java file to parse.
     * @param targetMethodName The name of the method within the Java class to search for annotated lines.
     * @param annotation       The specific comment annotation to look for at the end of each line within the method.
     * @param includeLines     Flag indicating whether to include line numbers in the output.
     * @return                 A list of strings, each representing a line from the method that ends with the specified
     *                          annotation. If includeLines is true, each string will start with the line number in the
     *                          format " [L {line number}] ".
     * @throws RuntimeException If an IOException occurs during file reading.
     */
    public static List<String> listAnnotatedLinesInMethod(File javaFile, String targetMethodName, String annotation, boolean includeLines) {
        List<String> annotatedLines = new ArrayList<>();
        try {
            String fileContent = new String(Files.readAllBytes(javaFile.toPath()));
            String[] lines = fileContent.split("\\r?\\n");

            javaParser.parse(javaFile).getResult().ifPresent(cu -> {
                Optional<MethodDeclaration> method = cu.findFirst(MethodDeclaration.class,
                        m -> m.getNameAsString().equals(targetMethodName));

                method.ifPresent(m -> {
                    Range methodRange = m.getRange().get();
                    for (int i = methodRange.begin.line; i <= methodRange.end.line; i++) {
                        String line = lines[i - 1].trim(); // -1 because array index is 0-based
                        if (line.endsWith(annotation)) {
                            String annotatedLine = includeLines ? " [L " + i + "] " + line : line;
                            annotatedLines.add(annotatedLine);
                        }
                    }
                });
            });
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + javaFile, e);
        }

        return annotatedLines;
    }

    /**
     * Extracts and lists all class-level variable declarations from a Java class file.
     * This method uses JavaParser to analyze the source code and find field declarations.
     *
     * @param javaFile The Java class file to parse.
     * @return A List of Strings, each representing a class-level variable declaration.
     */
    public static List<String> listClassVariables(File javaFile) {
        List<String> classVariables = new ArrayList<>();
        try {
            CompilationUnit cu = parseJavaFile(javaFile);
            List<FieldDeclaration> fields = cu.findAll(FieldDeclaration.class);

            for (FieldDeclaration field : fields) {
                String modifiers = field.getModifiers().stream()
                        .map(Modifier::toString)
                        .collect(Collectors.joining(" ")).trim();

                Type type = field.getCommonType();
                String vars = field.getVariables().stream()
                        .map(v -> v.getNameAsString() + (v.getInitializer().isPresent() ? " = " + v.getInitializer().get() : ""))
                        .collect(Collectors.joining(", "));

                String fieldDeclaration = (modifiers.isEmpty() ? "" : modifiers + " ") + type + " " + vars + ";";
                classVariables.add(fieldDeclaration.trim());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing class variables: " + e.getMessage(), e);
        }
        return classVariables;
    }

    /**
     * Extracts and lists all variable declarations from a specified method within a Java class file.
     * This method uses JavaParser to analyze the source code and find variable declaration expressions.
     *
     * @param javaFile         The Java class file to parse.
     * @param targetMethodName The name of the method within the Java class from which to extract variable declarations.
     * @param includeLines     Flag to determine whether to include line numbers in the output.
     * @return                 An ArrayList of Strings, each representing a variable declaration within the specified method.
     *                         Includes line numbers if includeLines is true.
     */
    public static ArrayList<String> listVariableDeclarationsInMethod(File javaFile, String targetMethodName, Boolean includeLines) {
        ArrayList<String> variableDeclarationList = new ArrayList<>();
        try {
            javaParser.parse(javaFile).getResult().ifPresent(cu -> {
                Optional<MethodDeclaration> method = cu.findFirst(MethodDeclaration.class,
                        m -> m.getNameAsString().equals(targetMethodName));

                method.ifPresent(m -> {
                    new VoidVisitorAdapter<Object>() {
                        @Override
                        public void visit(VariableDeclarationExpr n, Object arg) {
                            super.visit(n, arg);
                            String declaration = n.toString();
                            if (includeLines) {
                                declaration = " [L " + n.getBegin().get().line + "] " + declaration;
                            }
                            variableDeclarationList.add(declaration);
                            System.out.println(declaration);
                        }
                    }.visit(m, null);
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return variableDeclarationList;
    }


    /**
     * Extracts comments from a specified method within a Java class file.
     * This method uses JavaParser to analyze the source code and retrieve comments.
     *
     * @param javaFile         The Java class file to parse.
     * @param targetMethodName The name of the method within the Java class from which to extract comments.
     * @return                 A String representing the comment attached to the specified method.
     *                         Returns empty string if no comment is found.
     */
    public static String getMethodComment(File javaFile, String targetMethodName) {
        final String[] methodComment = {""};
        try {
            javaParser.parse(javaFile).getResult().ifPresent(cu -> {
                Optional<MethodDeclaration> method = cu.findFirst(MethodDeclaration.class,
                        m -> m.getNameAsString().equals(targetMethodName));

                method.ifPresent(m -> {
                    Optional<Comment> comment = m.getComment();
                    comment.ifPresent(c -> methodComment[0] = c.getContent());
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return methodComment[0];
    }


    /**
     * Extracts the full class declaration from a specified Java class file.
     * This includes access modifiers, class name, extended superclass, and implemented interfaces.
     *
     * @param javaFile The Java class file to parse.
     * @return         A String representing the full class declaration.
     *                 Returns null if the class declaration is not found.
     */
    public static String getFullClassDeclaration(File javaFile) {
        JavaParser javaParser = new JavaParser();
        try {
            ParseResult<CompilationUnit> result = javaParser.parse(javaFile);

            if (result.getResult().isPresent()) {
                CompilationUnit cu = result.getResult().get();
                Optional<ClassOrInterfaceDeclaration> classDecl = cu.findFirst(ClassOrInterfaceDeclaration.class);

                if (classDecl.isPresent()) {
                    ClassOrInterfaceDeclaration classDeclaration = classDecl.get();
                    String declaration = classDeclaration.getAccessSpecifier().asString() + " class " +
                            classDeclaration.getNameAsString();

                    if (!classDeclaration.getImplementedTypes().isEmpty()) {
                        declaration += " implements " + classDeclaration.getImplementedTypes().toString().replaceAll("\\[|\\]", "");
                    }

                    if (classDeclaration.getExtendedTypes().isNonEmpty()) {
                        declaration += " extends " + classDeclaration.getExtendedTypes().toString().replaceAll("\\[|\\]", "");
                    }

                    return declaration;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }


    /**
     * Identifies compatible graph types for a given OCDA algorithm
     * @param javaFile        OCDA algorithm class file
     * @return                List of compatible graph types with the specified OCDA
     */
    public static List<String> extractCompatibilities(File javaFile) {
        List<String> compatibleGraphTypes = new ArrayList<>();
        try {
            javaParser.parse(javaFile).getResult().ifPresent(cu -> {
                Optional<MethodDeclaration> method = cu.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals(COMPATIBLE_GRAPH_TYPE_METHOD_NAME));

                method.ifPresent(m -> {
                    m.findAll(MethodCallExpr.class).stream()
                            .filter(mc -> mc.getNameAsString().equals("add"))
                            .forEach(mc -> {
                                mc.getArguments().forEach(arg -> {
                                    compatibleGraphTypes.add(arg.toString());
                                });
                            });
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return compatibleGraphTypes;
    }


    /**
     * Identifies compatible graph types for a given OCDA algorithm. Additionally, an artificial undirected
     * GraphType is introduced in the compatible graph type list. This is used for test
     * quality evaluation where undirected graphs are needed and used, however WebOCD doesn't have
     * a separate undirected grpah type.
     *
     * @param javaFile        OCDA algorithm class file
     * @return                List of compatible graph types with the specified OCDA
     */
    public static List<String> extractCompatibilitiesAndAddUndirectedGraphType(File javaFile){
        List<String> compatibleGraphTypes = extractCompatibilities(javaFile);

        compatibleGraphTypes.add("GraphType.UNDIRECTED");

        return compatibleGraphTypes;
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
