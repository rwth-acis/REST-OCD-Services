package i5.las2peer.services.ocd.automatedtesting.ocdparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import static com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_17;

public class JavaParserSingleton {
    private static JavaParser instance;


    private JavaParserSingleton() {

    }

    public static synchronized JavaParser getInstance() {
        if (instance == null) {
            ParserConfiguration parserConfig = new ParserConfiguration();
            parserConfig.setLanguageLevel(JAVA_17);
            instance = new JavaParser(parserConfig);
        }
        return instance;
    }
}
