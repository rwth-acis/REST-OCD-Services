package i5.las2peer.services.ocd.automatedtesting.helpers;

/**
 * This is a container class to hold code smell information based on the PMD report
 */
public class CodeSmellData {
    private final String beginLine;

    private final String endLine;

    private final String violatedRule;

    private final String ruleSet;

    private final String examinedClass;
    private final String description;



    public CodeSmellData(String beginLine, String endLine, String violatedRule, String ruleSet, String examinedClass, String description) {
        this.beginLine = beginLine;
        this.endLine = endLine;
        this.violatedRule = violatedRule;
        this.ruleSet = ruleSet;
        this.examinedClass = examinedClass;
        this.description = description;
    }

    public String getBeginLine() {
        return beginLine;
    }

    public String getEndLine() {
        return endLine;
    }

    public String getViolatedRule() {
        return violatedRule;
    }

    public String getRuleSet() {
        return ruleSet;
    }

    public String getExaminedClass() {
        return examinedClass;
    }

    public String getDescription() {
        return description;
    }


    public String toStringShort() {
        return "{" +
                ", violatedRule='" + violatedRule + '\'' +
                "beginLine='" + beginLine + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public String toString() {
        return "CodeSmellData{" +
                "beginLine='" + beginLine + '\'' +
                ", endLine='" + endLine + '\'' +
                ", violatedRule='" + violatedRule + '\'' +
                ", ruleSet='" + ruleSet + '\'' +
                ", examinedClass='" + examinedClass + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}