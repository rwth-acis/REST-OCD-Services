package i5.las2peer.services.ocd.automatedtesting.ocdparser;


import java.io.*;

import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import i5.las2peer.services.ocd.automatedtesting.helpers.FileHelpers;
import i5.las2peer.services.ocd.automatedtesting.helpers.FormattingHelpers;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.type.Type;

public class OCDAParser {


    // Name of the method that holds compatible graph types of OCDA
    private static final String COMPATIBLE_GRAPH_TYPE_METHOD_NAME = "compatibleGraphTypes";


    // Declare javaParser as a class variable
    private static JavaParser javaParser = JavaParserSingleton.getInstance();


    /**
     * Use the singleton JavaParser to parse the Java file
     * @return Compulation unit of the parsed file
     */
    public static CompilationUnit parseJavaFile(File file) {
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
     * @return fully qualified class name from the CompilationUnit
     */
    private static String getFullyQualifiedClassName(CompilationUnit cu) {
        String packageName = cu.findFirst(PackageDeclaration.class)
                .map(PackageDeclaration::getNameAsString)
                .orElse("");
        String className = cu.findFirst(ClassOrInterfaceDeclaration.class)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElse("No class name found");

        return packageName.isEmpty() ? className : packageName + "." + className;
    }

    /**
     * @return fully qualified class name from the class file
     */
    public static String getFullyQualifiedClassName(File file) {
        return getFullyQualifiedClassName(parseJavaFile(file));
    }


    /**
     * Parses a given Java file and extracts parsing error messages, if any.
     * This method utilizes a Java parser to analyze the specified file, identifying syntactical and structural
     * issues that prevent successful parsing. It captures details of each identified problem, including its
     * location in the source file (if available) and a descriptive error message. This is particularly useful
     * for validating the syntax of Java code and providing feedback on errors.
     *
     * @param file The Java file to be parsed. This should be a valid file object pointing to a Java source file.
     * @return     A list of error messages, each providing details about a specific parsing issue.
     *             The error messages include the location (if available) and a description of the problem.
     *             Returns an empty list if the file is parsed successfully without any errors.
     * @throws     RuntimeException if an exception occurs during the file parsing process. This exception
     *             includes details about the cause, such as file access issues or interruptions in the parsing process.
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

    /**
     * Extracts the content of a specific line from a file.
     *
     * @param file The file from which to extract the line content.
     * @param lineNumber The line number to be extracted (1-based index).
     * @return The content of the specified line or an empty string if the line does not exist.
     *         Returns "Error retrieving line content" if an IOException occurs.
     */
    private static String extractLineContent(File file, int lineNumber) {
        try (Stream<String> lines = Files.lines(file.toPath())) {
            return lines.skip(lineNumber - 1).findFirst().orElse("");
        } catch (IOException e) {
            e.printStackTrace();
            return "Error retrieving line content";
        }
    }

    public static void main(String[] args) {

        try {

            // ===== Parsing OCD =====

            /* Parse the OCDA class file */
            File ocdaCode = new File(FileHelpers.getOCDAPath("SskAlgorithm.java"));
            File testFile = new File(FileHelpers.getOCDATestPath("SskAlgorithmTest1.java"));


            System.out.println(getMethodSignature(ocdaCode,"setParameters"));


            /* Identify compatible graph types for a parsed OCDA */
            List<String> compatibilities = extractCompatibilities(ocdaCode);

            /* Generate Chat-GPT prompt for a given OCDA that can be used to create an OCDA test class */
            //PromptGenerator.generateAndWritePromptString(file);


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
//
//            /* Check if the test class is executable or if exception is thrown */
//            OCDTestRunner.runCompiledTestClassWithJUnit5(testFile);


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
     * Extracts the signature of a specified method from a Java class file, including its visibility and throws clause.
     * This method uses JavaParser to analyze the source code and find the method declaration.
     *
     * @param javaFile The Java class file to parse.
     * @param methodName The name of the method whose signature is to be extracted.
     * @return A String representing the method signature, or an empty string if the method is not found.
     */
    public static String getMethodSignature(File javaFile, String methodName) {
        try {
            CompilationUnit cu = parseJavaFile(javaFile);

            AtomicReference<MethodDeclaration> foundMethod = new AtomicReference<>();

            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodDeclaration md, Void arg) {
                    if (md.getNameAsString().equals(methodName)) {
                        foundMethod.set(md);
                    }
                    super.visit(md, arg);
                }
            }, null);

            return Optional.ofNullable(foundMethod.get())
                    .map(md -> {
                        String visibility = md.getModifiers().stream()
                                .map(Modifier::getKeyword)
                                .filter(kw -> kw == Modifier.Keyword.PUBLIC ||
                                        kw == Modifier.Keyword.PROTECTED ||
                                        kw == Modifier.Keyword.PRIVATE)
                                .map(Modifier.Keyword::asString)
                                .findFirst()
                                .orElse("");
                        String returnType = md.getType().asString();
                        String parameters = md.getParameters().stream()
                                .map(Parameter::toString)
                                .collect(Collectors.joining(", ", "(", ")"));

                        // Extracting the 'throws' clause
                        String throwsClause = md.getThrownExceptions().stream()
                                .map(ReferenceType::asString)
                                .collect(Collectors.joining(", ", " throws ", ""));

                        // Only add the throws clause if exceptions are present
                        throwsClause = md.getThrownExceptions().isEmpty() ? "" : throwsClause;

                        return visibility + " " + returnType + " " + methodName + parameters + throwsClause;
                    })
                    .orElse("");
        } catch (Exception e) {
            throw new RuntimeException("Error extracting method signature: " + e.getMessage(), e);
        }
    }


    /**
     * Extracts the implementation of a specified method from a Java class file.
     * This method uses JavaParser to analyze the source code and find the specified method.
     *
     * @param javaFile The Java class file to parse.
     * @param methodName The name of the method to find.
     * @return A string representing the whole method implementation, or an empty string if the method is not found.
     */
    public static String getMethodImplementation(File javaFile, String methodName) {
        try {
            CompilationUnit cu = parseJavaFile(javaFile);

            AtomicReference<MethodDeclaration> foundMethod = new AtomicReference<>();

            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodDeclaration md, Void arg) {
                    if (md.getNameAsString().equals(methodName)) {
                        foundMethod.set(md);
                    }
                    super.visit(md, arg);
                }
            }, null);

            return Optional.ofNullable(foundMethod.get())
                    .map(MethodDeclaration::toString)
                    .orElse("");
        } catch (Exception e) {
            throw new RuntimeException("Error extracting method implementation: " + e.getMessage(), e);
        }
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
     * Extracts and lists all import statements from a Java class file, sorted alphabetically.
     * This method uses JavaParser to analyze the source code and find import declarations.
     *
     * @param javaFile The Java class file to parse.
     * @return A sorted List of Strings, each representing an import statement.
     */
    public static List<String> extractSortedImports(File javaFile) {
        List<String> imports = new ArrayList<>();
        try {
            CompilationUnit cu = parseJavaFile(javaFile);

            for (ImportDeclaration importDecl : cu.getImports()) {
                imports.add(importDecl.toString().trim());
            }

            // Sort the list alphabetically
            Collections.sort(imports);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing import statements: " + e.getMessage(), e);
        }
        return imports;
    }

    /**
     * Merges two lists of String values, removes duplicates, and returns a sorted list.
     *
     * @param list1 The first list of String values.
     * @param list2 The second list of String values.
     * @return A sorted List of Strings, with duplicates removed.
     */
    public static List<String> mergeAndSortLists(List<String> list1, List<String> list2) {
        Set<String> mergedSet = new HashSet<>();

        // Add all elements from both lists to the set (duplicates are automatically removed)
        mergedSet.addAll(list1);
        mergedSet.addAll(list2);

        // Convert the set back to a list
        List<String> sortedList = new ArrayList<>(mergedSet);

        // Sort the list
        Collections.sort(sortedList);

        return sortedList;
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
     * Extracts and lists all non-final class-level variable declarations from a Java class file, optionally with
     * their properly formatted Javadoc comments. This method uses JavaParser to analyze the source code and find
     * field declarations and their associated comments.
     *
     * @param javaFile The Java class file to parse.
     * @param includeComments Flag to determine whether to include Javadoc comments.
     * @return A List of Strings, each representing a non-final class-level variable declaration,
     *         optionally with properly formatted Javadoc comment.
     */
    public static List<String> listNonFinalClassVariables(File javaFile, boolean includeComments) {
        List<String> classVariablesWithComments = new ArrayList<>();
        try {
            CompilationUnit cu = parseJavaFile(javaFile);
            List<FieldDeclaration> fields = cu.findAll(FieldDeclaration.class);

            for (FieldDeclaration field : fields) {
                if (isFieldFinal(field)) {
                    continue;
                }

                classVariablesWithComments.addAll(
                        collectFieldDeclarations(field, includeComments, null)
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing class variables with comments: " + e.getMessage(), e);
        }
        return classVariablesWithComments;
    }

    /**
     * Extracts and lists class-level variable declarations (non-final) for specified variable names from a Java class file,
     * optionally with their properly formatted Javadoc comments.
     *
     * @param javaFile       The Java class file to parse.
     * @param includeComments Flag to determine whether to include Javadoc comments.
     * @param ocdaParameters The list of variable names to include in the result.
     * @return A List of Strings, each representing a class-level variable declaration,
     *         optionally with properly formatted Javadoc comment, for the specified variable names.
     */
    public static List<String> listSelectedClassVariables(File javaFile, boolean includeComments, List<String> ocdaParameters) {
        List<String> selectedVariablesWithComments = new ArrayList<>();
        try {
            CompilationUnit cu = parseJavaFile(javaFile);
            List<FieldDeclaration> fields = cu.findAll(FieldDeclaration.class);

            for (FieldDeclaration field : fields) {
                if (isFieldFinal(field)) {
                    continue;
                }

                selectedVariablesWithComments.addAll(
                        collectFieldDeclarations(field, includeComments, ocdaParameters)
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing selected class variables with comments: " + e.getMessage(), e);
        }
        return selectedVariablesWithComments;
    }

    /**
     * Collects field declarations for a given field, optionally including Javadoc comments, and filters them based on
     * a list of specified variable names.
     *
     * @param field           The field from which variable declarations are collected.
     * @param includeComments If true, includes Javadoc comments for each field.
     * @param ocdaParameters  A list of variable names to filter the field declarations. If null, all variables are included.
     * @return A list of strings, each representing a variable declaration (optionally with its Javadoc comment) from the specified field.
     */
    private static List<String> collectFieldDeclarations(FieldDeclaration field, boolean includeComments, List<String> ocdaParameters) {
        List<String> fieldDeclarations = new ArrayList<>();
        for (VariableDeclarator variable : field.getVariables()) {
            // If ocdaParameters is not null, check if the variable name is contained in the list
            if (ocdaParameters == null || ocdaParameters.contains(variable.getNameAsString())) {
                String fieldDeclaration = createFieldDeclaration(field, variable, includeComments);
                fieldDeclarations.add(fieldDeclaration);
            }
        }
        return fieldDeclarations;
    }

    /**
     * Checks if a given field is declared as final.
     *
     * @param field The field to check.
     * @return true if the field is final, false otherwise.
     */
    private static boolean isFieldFinal(FieldDeclaration field) {
        return field.getModifiers().contains(Modifier.finalModifier());
    }

    /**
     * Creates a string representation of a field declaration, including its modifiers, type, name, initializer, and
     * optionally its Javadoc comment.
     *
     * @param field           The field from which the declaration is created.
     * @param variable        The variable inside the field to include in the declaration.
     * @param includeComments If true, includes the Javadoc comment for the field.
     * @return A string representing the field declaration, optionally including its Javadoc comment.
     */
    private static String createFieldDeclaration(FieldDeclaration field, VariableDeclarator variable, boolean includeComments) {
        String javadocComment = includeComments ? getJavadocComment(field) : "";
        String modifiers = field.getModifiers().stream().map(Modifier::toString).collect(Collectors.joining(" ")).trim();
        Type type = field.getCommonType();
        String vars = variable.getNameAsString() + (variable.getInitializer().isPresent() ? " = " + variable.getInitializer().get() : "");
        String fieldDeclaration = (modifiers.isEmpty() ? "" : modifiers + " ") + type + " " + vars + ";";

        return javadocComment.trim() + (includeComments && !javadocComment.isEmpty() ? "\n" : "") + fieldDeclaration.trim();
    }

    /**
     * Retrieves the Javadoc comment associated with a field, if it exists.
     *
     * @param field The field from which to retrieve the Javadoc comment.
     * @return The Javadoc comment as a string, or an empty string if no Javadoc comment is present.
     */
    private static String getJavadocComment(FieldDeclaration field) {
        return field.getComment()
                .filter(c -> c.isJavadocComment())
                .map(c -> ((JavadocComment) c).toString())
                .orElse("");
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


    /**
     * Extracts the default parameter values from a list of OCD algorithm parameter declarations.
     * Each parameter declaration is parsed to determine the variable name and its default value.
     *
     * @param ocdaParameterDeclarationsList A list of strings representing the parameter declarations in the OCD algorithm.
     * @return A map of parameter names to their corresponding default values.
     */
    public static Map<String, String> getDefaultParameterValues(List<String> ocdaParameterDeclarationsList) {

        Map<String, String> defaultParameterValues = new HashMap<>();
        ocdaParameterDeclarationsList.forEach(ocdaParameterDeclaration -> {
            defaultParameterValues.put(FormattingHelpers.extractVariableName(ocdaParameterDeclaration), FormattingHelpers.extractVariableValue(ocdaParameterDeclaration));
        });
        return defaultParameterValues;
    }

    /**
     * Extracts constants that hold names of OCD algorithm parameter names from a list of method call entries of
     * OCD algorithm parameter getter method.
     *
     * @param entries An ArrayList of method call entries from getParameters method of OCDA.
     * @return A List of strings containing the extracted OCDA parameter names.
     */
    public static List<String> extractOCDAParameterConstants(ArrayList<String> entries) {
        List<String> keys = new ArrayList<>();
        for (String entry : entries) {
            if (entry.contains("parameters.put(")) {
                int startIndex = entry.indexOf('(') + 1; // Start after '('
                int endIndex = entry.indexOf(','); // Stop at ','
                if (startIndex < endIndex && startIndex > 0 && endIndex > 0) {
                    String key = entry.substring(startIndex, endIndex).trim();
                    keys.add(key);
                }
            }
        }
        return keys;
    }
}
