package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.utils.AbstractCustomException;

import java.io.Serial;

public class OcdPersistenceLoadException extends AbstractCustomException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -2367623098820728369L;

    /**
     * A standard message for all algorithm exceptions.
     */
    private static final String errorMessage = "Object could not be loaded";

    /**
     * Creates an exception whose error message includes detailed information
     * from an additional exception.
     * @param e The additional exception.
     */
    public OcdPersistenceLoadException(Exception e) {
        super(errorMessage + "\nInternal Exception:\n" + getInternalExceptionString(e));
    }

    /**
     * Creates an exception whose message includes an additional string.
     * @param s The additional string.
     */
    public OcdPersistenceLoadException(String s) {
        super(errorMessage + "\n" + s);
    }

    /**
     * Creates a standard exception.
     */
    public OcdPersistenceLoadException() {
        super(errorMessage);
    }

    /**
     * Creates an exception whose message includes detailed information from an additional exception
     * and an additional string.
     * @param e The additional exception.
     * @param s The additional string.
     */
    public OcdPersistenceLoadException(Exception e, String s) {
        super(errorMessage + "\n" + s + "\nInternal Exception:\n" + getInternalExceptionString(e));
    }


}
