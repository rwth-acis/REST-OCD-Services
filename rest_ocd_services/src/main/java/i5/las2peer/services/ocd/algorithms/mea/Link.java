package i5.las2peer.services.ocd.algorithms.mea;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

/**
 * This class represents link structure used in MEA algorithm's original code written in c.
 * Each node has a link which can be used to access all outgoing edges from the node
  */

public class Link {
    public int to;
    public Link next;

}
