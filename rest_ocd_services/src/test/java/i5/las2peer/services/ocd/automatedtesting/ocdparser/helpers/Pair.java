package i5.las2peer.services.ocd.automatedtesting.ocdparser.helpers;

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
        return "{\"" +
                 left +
                "\":\"" + right +
                "\"}";
    }

}
