package i5.las2peer.services.ocd.automatedtesting.helpers;

/**
 * A helper class acting as a pair of two objects
 * @param <L>
 * @param <R>
 */
public class Pair<L, R> {
    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() { return left; }
    public R getRight() { return right; }

    @Override
    public String toString() {
        return "{\"" + escapeJson(left.toString()) + "\":\"" + escapeJson(right.toString()) + "\"}";
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

}
