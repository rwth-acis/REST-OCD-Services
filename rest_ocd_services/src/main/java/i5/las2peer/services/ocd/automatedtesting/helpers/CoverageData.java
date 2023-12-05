package i5.las2peer.services.ocd.automatedtesting.helpers;

/**
 * This is a container class to hold coverage information for different types
 * of coverage metrics (e.g. branch, instruction).
 */
public class CoverageData {

    int missed;
    int covered;

    public CoverageData(int missed, int covered) {
        this.missed = missed;
        this.covered = covered;
    }

    public int getMissed() {
        return missed;
    }

    public int getCovered() {
        return covered;
    }

    public int getTotal(){
        return (missed + covered);
    }

    public double computeCoverage(){
        int total = getTotal();
        if (total == 0) {
            /* if there's nothing to cover, then there is full coverage by default */
            return 1;
        }
        return (double) covered / total;
    }

    @Override
    public String toString() {
        return "{" +
                "missed=" + missed +
                ", covered=" + covered +
                '}';
    }

}