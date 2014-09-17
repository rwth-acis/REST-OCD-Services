package i5.las2peer.services.ocd.benchmarks;

import i5.las2peer.services.ocd.utils.Parameterizable;

/**
 * The common interface for all Overlapping Community Detection Benchmarks.
 * Any classes implementing this interface must provide a default (no-argument) constructor.
 * @author Sebastian
 *
 */
public interface OcdBenchmark extends Parameterizable, GraphCreationMethod {

}
