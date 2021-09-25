package i5.las2peer.services.ocd.benchmarks;

import i5.las2peer.services.ocd.adapters.coverInput.CoverInputAdapter;
import i5.las2peer.services.ocd.adapters.coverInput.NodeCommunityListsCoverInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.ocd.adapters.graphInput.UnweightedEdgeListGraphInputAdapter;
import i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.signedlfr.benchm;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.Edge;
import y.base.EdgeCursor;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang3.SystemUtils;
import org.la4j.matrix.Matrix;

/**
 * The signed LFR benchmark model for signed and directed graphs. Makes use of
 * an LFR benchmark application written by Lancichinetti (the one for directed networks).
 * 
 * @author YLi
 *
 */
public class SignedLfrBenchmark implements GroundTruthBenchmark {
	/**
	 * Path of the directory reserved for the signed LFR benchmark application.
	 */
	private static final String SignedLfrDirectoryPath = "ocd/signedLfr/";
	/**
	 * Used for synchronization purposes. Executes the benchmark graph
	 * calculation.
	 */
	private static DefaultExecutor executor = new DefaultExecutor();
	/**
	 * Path of the file holding an application developed by Lancichinetti that
	 * calculates LFR benchmark graphs. For Windows.
	 */
	private static String windowsBenchmarkGeneratorPath = SignedLfrDirectoryPath + "SignedLfrBenchmarkWindows.exe";
	/**
	 * Path of the file holding an application developed by Lancichinetti that
	 * calculates LFR benchmark graphs. For Linux.
	 */
	private static String linuxBenchmarkGeneratorPath = "./SignedLfrBenchmarkLinux";
	/**
	 * Path of the file containing the calculated benchmark graph.
	 */
	private static final String graphPath = SignedLfrDirectoryPath + "network.dat";
	/**
	 * Path of the file containing the ground truth cover of the benchmark
	 * graph.
	 */
	private static final String coverPath = SignedLfrDirectoryPath + "community.dat";

	/**
	 * The node count of the benchmark graphs. The default value is 1000. Must
	 * be greater than 0.
	 */
	private int n = 1000;
	/**
	 * The average node degree of the benchmark graphs. The default value is 20.
	 * Must be greater than 0.
	 */
	private int k = 20;
	/**
	 * The maximum node degree of the benchmark graphs. The default value is 50.
	 * Must be greater or equal k.
	 */
	private int maxk = 50;
	/**
	 * The topological mixing parameter which determines how many edges a node
	 * shares with nodes of other communities. The default value is 0.2. Must be
	 * in [0, 1].
	 */
	private double mu = 0.2;
	/**
	 * The exponent for the probability distribution of the node degrees. The
	 * default value is -2.
	 */
	private double t1 = -2;
	/**
	 * The exponent for the probability distribution of community sizes. The
	 * default value is -1.
	 */
	private double t2 = -1;
	/**
	 * The minimum community size of the benchmark graphs. The default value is
	 * 10. Must be greater than 0.
	 */
	private int minc = 10;
	/**
	 * The maximum community size of the benchmark graphs. The default value is
	 * 50. Must be greater or equal minc.
	 */
	private int maxc = 50;
	/**
	 * The number of overlapping nodes in the benchmark graphs. The default
	 * value is 100. Must be greater or equal 0.
	 */
	private int on = 100;
	/**
	 * The number of communities an overlapping node belongs to. The default
	 * value is 2. Must be greater or equal 2.
	 */
	private int om = 2;
	/**
	 * The fraction of intra-edges which are negative. The default value is
	 * 0.05.
	 */
	private double neg = 0.05;
	/**
	 * The fraction of inter-edges which are positive. The default value is
	 * 0.05.
	 */
	private double pos = 0.05;	
	/**
	 * This variable is used to produce a benchmark whose distribution of the ratio of external
	 * in-degree/total in-degree is superiorly (inferiorly) bounded by the mixing
	 * parameter (only for the topology). In other words, if you use one of these
	 * options, the mixing parameter is not the average ratio of external
	 * degree/total degree (as it used to be) but the maximum (or the minimum) of
	 * that distribution. When using one of these options, what the program
	 * essentially does is to approximate the external degree always by excess (or
	 * by defect) and if necessary to modify the degree distribution. Nevertheless,
	 * this last possibility occurs for a few nodes and numerical simulations show
	 * that it does not affect the degree distribution
	 */
	private boolean excess = false;
	private boolean defect = false;
	
	/**
	 * To have a random network: using this option will set muw=0, mut=0, and
	 * minc=maxc=N, i.e. there will be one only community.
	 */
	private boolean fixed_range = false;

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(N_NAME, Integer.toString(n));
		parameters.put(K_NAME, Integer.toString(k));
		parameters.put(MAXK_NAME, Integer.toString(maxk));
		parameters.put(MU_NAME, Double.toString(mu));
		parameters.put(T1_NAME, Double.toString(t1));
		parameters.put(T2_NAME, Double.toString(t2));
		parameters.put(MINC_NAME, Integer.toString(minc));
		parameters.put(MAXC_NAME, Integer.toString(maxc));
		parameters.put(ON_NAME, Integer.toString(on));
		parameters.put(OM_NAME, Integer.toString(om));
		parameters.put(NEG_NAME, Double.toString(neg));
		parameters.put(POS_NAME, Double.toString(pos));
		parameters.put(EXCESS_NAME, Boolean.toString(excess));
		parameters.put(DEFECT_NAME, Boolean.toString(defect));
		parameters.put(FIXED_RANGE_NAME, Boolean.toString(fixed_range));
		return parameters;
	}

	@Override
	public void setParameters(Map<String, String> parameters) {
		if (parameters.containsKey(N_NAME)) {
			n = Integer.parseInt(parameters.get(N_NAME));
			parameters.remove(N_NAME);
			if (n <= 0) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.containsKey(K_NAME)) {
			k = Integer.parseInt(parameters.get(K_NAME));
			parameters.remove(K_NAME);
			if (k <= 0) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.containsKey(MAXK_NAME)) {
			maxk = Integer.parseInt(parameters.get(MAXK_NAME));
			parameters.remove(MAXK_NAME);
			if (maxk < k) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.containsKey(MU_NAME)) {
			mu = Double.parseDouble(parameters.get(MU_NAME));
			parameters.remove(MU_NAME);
			if (mu < 0 || mu > 1) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.containsKey(T1_NAME)) {
			t1 = Double.parseDouble(parameters.get(T1_NAME));
			parameters.remove(T1_NAME);
		}
		if (parameters.containsKey(T2_NAME)) {
			t2 = Double.parseDouble(parameters.get(T2_NAME));
			parameters.remove(T2_NAME);
		}
		if (parameters.containsKey(MINC_NAME)) {
			minc = Integer.parseInt(parameters.get(MINC_NAME));
			parameters.remove(MINC_NAME);
			if (minc < 1) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.containsKey(MAXC_NAME)) {
			maxc = Integer.parseInt(parameters.get(MAXC_NAME));
			parameters.remove(MAXC_NAME);
			if (maxc < minc) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.containsKey(ON_NAME)) {
			on = Integer.parseInt(parameters.get(ON_NAME));
			parameters.remove(ON_NAME);
			if (on < 0 || on > n) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.containsKey(OM_NAME)) {
			om = Integer.parseInt(parameters.get(OM_NAME));
			parameters.remove(OM_NAME);
			if (om < 2) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.containsKey(POS_NAME)) {
			pos = Double.parseDouble(parameters.get(POS_NAME));
			parameters.remove(POS_NAME);
			if (pos < 0 || pos > 1) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.containsKey(NEG_NAME)) {
			neg = Double.parseDouble(parameters.get(NEG_NAME));
			parameters.remove(NEG_NAME);
			if (neg < 0 || neg > 1) {
				throw new IllegalArgumentException();
			}
		}
		if(parameters.containsKey(EXCESS_NAME)) {
			excess = Boolean.parseBoolean(EXCESS_NAME);
			parameters.remove(EXCESS_NAME);
		}
		if(parameters.containsKey(DEFECT_NAME)) {
			defect = Boolean.parseBoolean(DEFECT_NAME);
			parameters.remove(DEFECT_NAME);
		}
		if(parameters.containsKey(FIXED_RANGE_NAME )) {
			fixed_range = Boolean.parseBoolean(FIXED_RANGE_NAME );
			parameters.remove(FIXED_RANGE_NAME );
		}
		if (parameters.size() > 0) {
			System.out.println(parameters);
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Creates a standardized instance of the benchmark model.
	 */
	public SignedLfrBenchmark() {
	}

	/*
	 * PARAMETER NAMES
	 */
	protected final String N_NAME = "n";
	protected final String K_NAME = "k";
	protected final String MAXK_NAME = "maxk";
	protected final String MU_NAME = "mu";
	protected final String T1_NAME = "t1";
	protected final String T2_NAME = "t2";
	protected final String MINC_NAME = "minc";
	protected final String MAXC_NAME = "maxc";
	protected final String ON_NAME = "on";
	protected final String OM_NAME = "om";
	protected final String POS_NAME = "pos";
	protected final String NEG_NAME = "neg";
	protected final String EXCESS_NAME = "excess";
	protected final String DEFECT_NAME = "defect";
	protected final String FIXED_RANGE_NAME = "fixed_range";

	/**
	 * Creates a customized instance of the benchmark model. The parameters must
	 * be values which are valid for the LFR model.
	 * 
	 * @param n
	 *            Sets n.
	 * @param k
	 *            Sets k.
	 * @param maxk
	 *            Sets maxk.
	 * @param mu
	 *            Sets mu.
	 * @param t1
	 *            Sets t1.
	 * @param t2
	 *            Sets t2.
	 * @param minc
	 *            Sets minc.
	 * @param maxc
	 *            Sets maxc.
	 * @param on
	 *            Sets on.
	 * @param om
	 *            Sets om.
	 * @param pos 
	 *            Value for pos.
	 * @param neg
	 *            Value for neg.
	 */
	public SignedLfrBenchmark(int n, int k, int maxk, double mu, double t1, double t2, int minc, int maxc, int on,
			int om, double pos, double neg) {
		this.minc = minc;
		this.mu = mu;
		this.n = n;
		this.k = k;
		this.maxk = maxk;
		this.on = on;
		this.maxc = maxc;
		this.om = om;
		this.t1 = t1;
		this.t2 = t2;
		this.pos = pos;
		this.neg = neg;
	}
	
	/**
	 * Creates a customized instance of the benchmark model, including setting boolean parameters excess, defect, fixed_range. The parameters must
	 * be values which are valid for the LFR model.
	 * 
	 * @param n
	 *            Sets n.
	 * @param k
	 *            Sets k.
	 * @param maxk
	 *            Sets maxk.
	 * @param mu
	 *            Sets mu.
	 * @param t1
	 *            Sets t1.
	 * @param t2
	 *            Sets t2.
	 * @param minc
	 *            Sets minc.
	 * @param maxc
	 *            Sets maxc.
	 * @param on
	 *            Sets on.
	 * @param om
	 *            Sets om.
	 * @param pos 
	 *            Value for pos.
	 * @param neg
	 *            Value for neg.
	 * @param excesss
	 *            Sets excess.
	 * @param defect 
	 *            Sets defect.
	 * @param fixed_range
	 *            Sets fixed_range.
	 *        
	 */
	public SignedLfrBenchmark(int n, int k, int maxk, double mu, double t1, double t2, int minc, int maxc, int on,
			int om, double pos, double neg, boolean excesss, boolean defect, boolean fixed_range) {
		this.minc = minc;
		this.mu = mu;
		this.n = n;
		this.k = k;
		this.maxk = maxk;
		this.on = on;
		this.maxc = maxc;
		this.om = om;
		this.t1 = t1;
		this.t2 = t2;
		this.pos = pos;
		this.neg = neg;
		this.excess = excesss;
		this.defect = defect;
		this.fixed_range = fixed_range;
	}
	
	

	@Override
	public Cover createGroundTruthCover() throws OcdBenchmarkException, InterruptedException {
		synchronized (executor) {
			try {
				String executorFilename;
				if (SystemUtils.IS_OS_WINDOWS) {
					executorFilename = windowsBenchmarkGeneratorPath;
				} else if (SystemUtils.IS_OS_LINUX) {
					executorFilename = linuxBenchmarkGeneratorPath;
				}
				/*
				 * Benchmark not implemented for this operating system.
				 */
				else {
					throw new OcdBenchmarkException();
				}
				CommandLine cmdLine = new CommandLine(executorFilename);
				cmdLine.addArgument("-N");
				cmdLine.addArgument(Integer.toString(this.n));
				cmdLine.addArgument("-k");
				cmdLine.addArgument(Integer.toString(this.k));
				cmdLine.addArgument("-maxk");
				cmdLine.addArgument(Integer.toString(this.maxk));
				cmdLine.addArgument("-mu");
				cmdLine.addArgument(Double.toString(this.mu));
				cmdLine.addArgument("-minc");
				cmdLine.addArgument(Integer.toString(this.minc));
				cmdLine.addArgument("-maxc");
				cmdLine.addArgument(Integer.toString(this.maxc));
				cmdLine.addArgument("-on");
				cmdLine.addArgument(Integer.toString(this.on));
				cmdLine.addArgument("-om");
				cmdLine.addArgument(Integer.toString(this.om));
				cmdLine.addArgument("-t1");
				cmdLine.addArgument(Double.toString(-this.t1));
				cmdLine.addArgument("-t2");
				cmdLine.addArgument(Double.toString(-this.t2));
				File workingDirectory = new File(SignedLfrDirectoryPath);
				File networkFile = new File(graphPath);
				if (networkFile.exists()) {
					networkFile.delete();
				}
				
                ///////////COMMENTED OUT WHEN C++ BASED ALGORITHM IS USED//////////////////
//				executor.setWorkingDirectory(workingDirectory);
//				DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
//				executor.execute(cmdLine, resultHandler);
//				resultHandler.waitFor();
//				if (resultHandler.getExitValue() != 0) {
//					System.out.println(resultHandler.getException());
//					throw new OcdBenchmarkException("LFR Process exit value: " + resultHandler.getExitValue());
//				}
				
				
				benchm.directed_network_benchmark(excess, defect, n, k, maxk, t1, t2, mu, on, om, minc, maxc, fixed_range); // Signed LFR algorithm based on C++ directed network algorithm
				GraphInputAdapter graphAdapter = new UnweightedEdgeListGraphInputAdapter(new FileReader(graphPath));
				CustomGraph graph = graphAdapter.readGraph();
				graph.addType(GraphType.DIRECTED);
				graph.addType(GraphType.NEGATIVE_WEIGHTS);
				CoverInputAdapter coverAdapter = new NodeCommunityListsCoverInputAdapter(new FileReader(coverPath));
				Cover cover = coverAdapter.readCover(graph);
				Cover signedCover = setWeightSign(cover, pos, neg);
				cover.setCreationMethod(new CoverCreationLog(CoverCreationType.GROUND_TRUTH,
						new HashMap<String, String>(), new HashSet<GraphType>()));
				return signedCover;
			} catch (InterruptedException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				throw new OcdBenchmarkException(e);
			}
		}
	}

	/**
	 * Returns a cover with the same membership matrix but the signed graph.
	 * 
	 * @param cover The ground truth of the LFR model.	 
	 * @param neg The fraction of intra-edges which are negative.	 
	 * @param pos The fraction of inter-edges which are positive.	 
	 * @return The adapted cover.
	 * @throws java.lang.InterruptedException when method execution is interrupted
	 */

	protected Cover setWeightSign(Cover cover, double pos, double neg) throws InterruptedException {
		CustomGraph graph = cover.getGraph();
		Matrix membership = cover.getMemberships();
		List<Edge> intraEdgeList = new ArrayList<Edge>();
		List<Edge> interEdgeList = new ArrayList<Edge>();
		List<Edge> intraNegativeList = new ArrayList<Edge>();
		int communityCount = membership.columns();
		EdgeCursor edges = graph.edges();
		Edge edge;
		/*
		 * negate the weight of all edges connecting two nodes in different
		 * communities.
		 */
		while (edges.ok()) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			edge = edges.edge();
			boolean beingIntraEdge = false;
			/*
			 * If two nodes are residing in at least one common community, the
			 * edge connecting them is regarded as an intra-edge.
			 */
			for (int i = 0; i < communityCount; i++) {
				if (membership.get(edge.source().index(), i) * membership.get(edge.target().index(), i) != 0) {
					intraEdgeList.add(edge);
					beingIntraEdge = true;
					break;
				}
			}
			if (beingIntraEdge == false) {
				interEdgeList.add(edge);
				graph.setEdgeWeight(edge, graph.getEdgeWeight(edge) * (-1));
			}
			edges.next();
		}
		/*
		 * randomly negate the weight of intra-edges depending on the parameter
		 * neg.
		 */
		int intraEdgeCount = intraEdgeList.size();
		int intraEdgesNegate = (int) Math.round(intraEdgeCount * neg);
		for (int i = 0; i < intraEdgesNegate; i++) {
			Edge positiveEdge = null;
			Boolean edgeRepeat = true;
			while (edgeRepeat) {
				positiveEdge = intraEdgeList.get((int) (Math.random() * (intraEdgeCount)));
				if (!intraNegativeList.contains(positiveEdge)) {
					edgeRepeat = false;
				}
			}
			graph.setEdgeWeight(positiveEdge, graph.getEdgeWeight(positiveEdge) * (-1));
			intraNegativeList.add(positiveEdge);
		}
		/*
		 * randomly negate the weight of inter-edges depending on the parameter
		 * pos.
		 */
		int interEdgeCount = interEdgeList.size();
		int interEdgesNegate = (int) Math.round(interEdgeCount * pos);
		Edge negativeEdge;
		for (int i = 0; i < interEdgesNegate; i++) {
			negativeEdge = interEdgeList.get((int) (Math.random() * (interEdgeCount)));
			graph.setEdgeWeight(negativeEdge, graph.getEdgeWeight(negativeEdge) * (-1));
		}
		return cover;
	}

}