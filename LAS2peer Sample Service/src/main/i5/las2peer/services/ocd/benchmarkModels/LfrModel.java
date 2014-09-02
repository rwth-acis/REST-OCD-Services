package i5.las2peer.services.ocd.benchmarkModels;

import i5.las2peer.services.ocd.adapters.coverInput.CoverInputAdapter;
import i5.las2peer.services.ocd.adapters.coverInput.NodeCommunityListsInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.WeightedEdgeListGraphInputAdapter;
import i5.las2peer.services.ocd.algorithms.AlgorithmLog;
import i5.las2peer.services.ocd.algorithms.AlgorithmType;
import i5.las2peer.services.ocd.graph.Cover;
import i5.las2peer.services.ocd.graph.CustomGraph;
import i5.las2peer.services.ocd.graph.GraphType;
import i5.las2peer.services.ocd.utils.GlobalVariables;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.lang3.SystemUtils;

/**
 * The LFR benchmark model for weighted and directed graphs.
 * @author Sebastian
 *
 */
public class LfrModel implements GroundTruthBenchmarkModel {

	/*
	 * Used for synchronization purposes. Executes the benchmark graph calculation.
	 */
	private static DefaultExecutor executor = new DefaultExecutor();
	/*
	 * Name of the file containing the calculated benchmark graph.
	 */
	private static final String graphFilename = "ocd\\lfr\\network.dat";
	/*
	 * Name of the file containing the ground truth cover of the benchmark graph.
	 */
	private static final String coverFilename = "ocd\\lfr\\community.dat";
	
	/**
	 * The node count of the benchmark graphs.
	 * The default value is 1000.
	 */
	private int n = 1000;
	/**
	 * The average node degree of the benchmark graphs.
	 * The default value is 20.
	 */
	private int k = 20;
	/**
	 * The maximum node degree of the benchmark graphs.
	 * The default value is 50.
	 */
	private int maxk = 50;
	/**
	 * The topological mixing parameter which determines how many edges a node shares
	 * with nodes of other communities.
	 * The default value is 0.2.
	 */
	private double mut = 0.2;
	/**
	 * The weight mixing parameter for the community-internal strengths of nodes.
	 * The default value is 0.1.
	 */
	private double muw = 0.1;
	/**
	 * The exponent for the probability distribution of the total strengths of nodes.
	 * The default value is 1.5.
	 */
	private double beta = 1.5;
	/**
	 * The exponent for the probability distribution of the node degrees.
	 * The default value is -2.
	 */
	private double t1 = -2;
	/**
	 * The exponent for the probability distribution of community sizes.
	 * The default value is -1.
	 */
	private double t2 = -1;
	/**
	 * The minimum community size of the benchmark graphs.
	 * The default value is 10.
	 */
	private int minc = 10;
	/**
	 * The maximum community size of the benchmark graphs.
	 * The default value is 50.
	 */
	private int maxc = 50;
	/**
	 * The number of overlapping nodes in the benchmark graphs..
	 * The default value is 100.
	 */
	private int on = 100;
	/**
	 * The number of communities an overlapping node belongs to.
	 * The default value is 2.
	 */
	private int om = 2;
	
	/**
	 * Creates a partially standardized instance of the benchmark model.
	 * The constructor is designed mainly for the creation of four sets of standard benchmark graphs
	 * Each set is obtained by setting mu and k to a combination of the suggested standard values and
	 * continuously increasing the fraction of overlapping nodes.
	 * The unspecified values are set to n=1000, maxk=50, minc=10, maxc=50, om=2, t1=-2, t2=-1 and beta=1.5.
	 * @param k Sets k.
	 * Is typically either 12 or 24.
	 * @param mu Sets mut and muw.
	 * Is typically either 0.1 or 0.3.
	 * @param overlappingNodesFraction Determines the percentage of overlapping nodes and hence on.
	 * Is typically increased from 0 by intervals of 0.05 or 0.1 up to 0.5 or 1.
	 */
	public LfrModel(int k, double mu, double overlappingNodeFraction) {
		this.minc = 10;
		this.mut = mu;
		this.muw = mu;
		this.n = 1000;
		this.k = k;
		this.maxk = 50;
		this.on = (int) Math.round((n * overlappingNodeFraction));
		this.maxc = 50;
		this.om = 2;
		this.t1 = -2;
		this.t2 = -1;
		this.beta = 1.5;
	}
	
	/**
	 * Creates a customized instance of the benchmark model.
	 * The parameters must be values which are valid for the LFR model.
	 * @param n Sets n.
	 * @param k Sets k.
	 * @param maxk Sets maxk.
	 * @param mut Sets mut.
	 * @param muw Sets muw.
	 * @param beta Sets beta.
	 * @param t1 Sets t1.
	 * @param t2 Sets t2.
	 * @param minc Sets minc.
	 * @param maxc Sets maxc.
	 * @param on Sets on.
	 * @param om Sets om.
	 */
	public LfrModel(int n, int k, int maxk, double mut, double muw, 
			double beta, double t1, double t2, int minc, int maxc, int on, int om) {
		this.minc = minc;
		this.mut = mut;
		this.muw = muw;
		this.n = n;
		this.k = k;
		this.maxk = maxk;
		this.on = on;
		this.maxc = maxc;
		this.om = om;
		this.t1 = t1;
		this.t2 = t2;
		this.beta = beta;
	}
	
	@Override
	public Cover createGroundTruthCover() throws BenchmarkException {
		synchronized (executor) {
			try {
				String executorFilename;
				if(SystemUtils.IS_OS_WINDOWS) {
					executorFilename = GlobalVariables.lfrBenchmarkCalculatorFilename;
				}
				// TODO add linux version
				/*
				else if(SystemUtils.IS_OS_LINUX) {
					
				}*/
				/*
				 * Benchmark not implemented for this operating system.
				 */
				else {
					throw new BenchmarkException();
				}
				CommandLine cmdLine = new CommandLine(executorFilename);
				cmdLine.addArgument("-N");
				cmdLine.addArgument(Integer.toString(this.n));
				cmdLine.addArgument("-k");
				cmdLine.addArgument(Integer.toString(this.k));
				cmdLine.addArgument("-maxk");
				cmdLine.addArgument(Integer.toString(this.maxk));
				cmdLine.addArgument("-muw");
				cmdLine.addArgument(Double.toString(this.muw));
				cmdLine.addArgument("-mut");
				cmdLine.addArgument(Double.toString(this.mut));
				cmdLine.addArgument("-beta");
				cmdLine.addArgument(Double.toString(this.beta));
				cmdLine.addArgument("-minc");
				cmdLine.addArgument(Integer.toString(this.minc));
				cmdLine.addArgument("-maxc");
				cmdLine.addArgument(Integer.toString(this.maxc));
				cmdLine.addArgument("-on");
				cmdLine.addArgument(Integer.toString(this.on));
				cmdLine.addArgument("-om");
				cmdLine.addArgument(Integer.toString(this.om));
				cmdLine.addArgument("-t1");
				cmdLine.addArgument(Double.toString( - this.t1));
				cmdLine.addArgument("-t2");
				cmdLine.addArgument(Double.toString( - this.t2));
				File workingDirectory = new File("bin");
				executor.setWorkingDirectory(workingDirectory);
				ExecuteWatchdog watchdog = new ExecuteWatchdog(20000);
				executor.setWatchdog(watchdog);
				DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
				executor.execute(cmdLine, resultHandler);
				resultHandler.waitFor();
				if(resultHandler.getExitValue() != 0) {
					throw new BenchmarkException();
				}
				GraphInputAdapter graphAdapter = new WeightedEdgeListGraphInputAdapter(new FileReader(graphFilename));
				CustomGraph graph = graphAdapter.readGraph();
				graph.addType(GraphType.DIRECTED);
				graph.addType(GraphType.WEIGHTED);
				CoverInputAdapter coverAdapter = new NodeCommunityListsInputAdapter(new FileReader(coverFilename));
				Cover cover = coverAdapter.readCover(graph);
				cover.setAlgorithm(new AlgorithmLog(AlgorithmType.GROUND_TRUTH, new HashMap<String, String>(), new HashSet<GraphType>()));
				return cover;
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new BenchmarkException();
			}
		}
	}

}
