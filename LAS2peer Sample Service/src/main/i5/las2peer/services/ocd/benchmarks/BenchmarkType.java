package i5.las2peer.services.ocd.benchmarks;

import java.security.InvalidParameterException;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

/**
 * Representation of overlapping community detection algorithms.
 * @author Sebastian
 *
 */
public enum BenchmarkType {

	UNDEFINED (0),
	REAL_WORLD (1),
	NEWMAN (2),
	LFR (3);
	
	private GroundTruthBenchmarkModel getGroundTruthBenchmarkInstance() {
		switch (this) {
			case LFR:
				return new LfrModel();
			case NEWMAN:
				return new NewmanModel();
			default:
				throw new NotImplementedException("Ground Truth Benchmark Model not registered.");
		}
	}
	
	public GroundTruthBenchmarkModel getGroundTruthBenchmarkInstance(Map<String, String> parameters) {
		GroundTruthBenchmarkModel benchmark = getGroundTruthBenchmarkInstance();
		benchmark.setParameters(parameters);
		return benchmark;
	}
	
	public boolean isGroundTruthBenchmark() {
		switch(this) {
			case LFR:
				return true;
			case NEWMAN:
				return true;
			default:
				return false;
		}
	}
	
	private final int id;
	
	private BenchmarkType(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public static BenchmarkType lookupType(int id) {
        for (BenchmarkType type : BenchmarkType.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        throw new InvalidParameterException();
	}
	
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
}
