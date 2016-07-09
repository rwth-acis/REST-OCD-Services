package i5.las2peer.services.ocd.algorithms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang3.SystemUtils;

import java.util.Scanner;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.benchmarks.OcdBenchmarkException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.Edge;
import y.base.EdgeCursor;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

/*
 * @author YLi
 */
public class SignedProbabilisticMixtureAlgorithm implements OcdAlgorithm {
	/*
	 * Path of the directory reserved for the application.
	 */
	private static final String DirectoryPath = "ocd/spm/";
	/*
	 * Used for synchronization purposes. Executes the application execution.
	 */
	private static DefaultExecutor executor = new DefaultExecutor();
	/*
	 * Path of the application for Linux based on the source codes of Chen et. al.
	 */
	private static String linuxApplicationPath = "./SpmLinux";
	/*
	 * Path of the application for Windows based on the source codes of Chen et
	 * al.
	 */
	private static String windowsApplicationPath = DirectoryPath + "SpmWindows.exe";
	/*
	 * Path of tuple file.
	 */
	private static final String graphPath = DirectoryPath + "network.tuple";
	/*
	 * Name of tuple file.
	 */
	private static final String graphName = "network.tuple";
	/*
	 * Path of the output of last generation.
	 */
	private static String LastResultPath = DirectoryPath + "result.txt";
	/**
	 * The number of trials. The default value is 3. Must be greater than 0.
	 */
	private int n = 3;

	/*
	 * PARAMETER NAMES
	 */
	protected final String TRIALCOUNT_NAME = "n";

	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.SIGNED_PROBABILISTIC_MIXTURE_ALGORITHM;
	}

	@Override
	public void setParameters(Map<String, String> parameters) {
		if (parameters.containsKey(TRIALCOUNT_NAME)) {
			n = Integer.parseInt(parameters.get(TRIALCOUNT_NAME));
			parameters.remove(TRIALCOUNT_NAME);
			if (n < 0) {
				throw new IllegalArgumentException();
			}
		}
		if (parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(TRIALCOUNT_NAME, Integer.toString(n));
		return parameters;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.NEGATIVE_WEIGHTS);
		compatibilities.add(GraphType.WEIGHTED);
		return compatibilities;
	}

	/**
	 * Creates a standard instance of the algorithm. All attributes are assigned
	 * their default values.
	 */
	public SignedProbabilisticMixtureAlgorithm() {

	}

	public SignedProbabilisticMixtureAlgorithm(int n) {
		this.n = n;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
		synchronized (executor) {
			try {
				String executorFilename;
				if (SystemUtils.IS_OS_LINUX) {
					executorFilename = linuxApplicationPath;
				} else if (SystemUtils.IS_OS_WINDOWS) {
					executorFilename = windowsApplicationPath;
				}
				/*
				 * Benchmark not implemented for this operating system.
				 */
				else {
					throw new OcdAlgorithmException();
				}
				writeNetworkFile(graph);
				CommandLine cmdLine = new CommandLine(executorFilename);
				cmdLine.addArgument(graphName);
				cmdLine.addArgument(Integer.toString(n));
				File workingDirectory = new File(DirectoryPath);
				executor.setWorkingDirectory(workingDirectory);
				DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
				executor.execute(cmdLine, resultHandler);
				resultHandler.waitFor();
				if (resultHandler.getExitValue() != 0) {
					System.out.println(resultHandler.getException());
					throw new OcdBenchmarkException("MEA Process exit value: " + resultHandler.getExitValue());
				}
				// read result file and generate membershipMatrix
				File resultFile = new File(LastResultPath);
				Integer nodeCount = graph.nodeCount();
				Matrix membershipMatrix = getMembershipMatrix(resultFile, nodeCount);
				Cover cover = new Cover(graph, membershipMatrix);
				return cover;
			} catch (InterruptedException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				throw new OcdAlgorithmException(e);
			}

		}
	}

	// translate graph into tuple file
	protected void writeNetworkFile(CustomGraph graph) throws IOException, InterruptedException {
		FileWriter networkFile = new FileWriter(graphPath);
		try {
			networkFile.write(Integer.toString(graph.nodeCount()));
			networkFile.write(System.lineSeparator());
			EdgeCursor edges = graph.edges();
			Edge edge;
			while (edges.ok()) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				edge = edges.edge();
				/*
				 * The algorithm reads network files starting with node 1.
				 */
				if (edge.source().index() <= edge.target().index()) {
					networkFile.write(Integer.toString(edge.source().index() + 1));
					networkFile.write("\t");
					networkFile.write(Integer.toString(edge.target().index() + 1));
					networkFile.write("\t");
					networkFile.write(Double.toString(graph.getEdgeWeight(edge)));
					networkFile.write(System.lineSeparator());
				}
				edges.next();
			}
		} finally {
			networkFile.close();
		}
	}

	//Transform the results written by the executable file into the membership matrix.
	protected Matrix getMembershipMatrix(File resultFile, int nodeCount) throws IOException, InterruptedException {
		Scanner communityResult = new Scanner(resultFile);
		int communityCount = 0;
		try {

			if (communityResult.next().equals("theta:")) {
				if (communityResult.hasNextInt()) {
					communityResult.next();
					while (!communityResult.hasNextInt()) {
						communityCount++;
						communityResult.next();
					}
				}
			}

			Matrix membershipMatrix = new CCSMatrix(nodeCount, communityCount);
			while (communityResult.hasNext()) {
				if (communityResult.next().equals("alpha/beta:")) {
					for (int i = 0; i < nodeCount; i++) {
						int nodeID = communityResult.nextInt();
						for (int j = 0; j < communityCount; j++) {
							/*
							 * result file of the executable file with nodes starting from 1
							 */
							membershipMatrix.set(nodeID - 1, j, communityResult.nextDouble());
						}
					}
				}
			}
			return membershipMatrix;
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			try {
				communityResult.close();
			} catch (Exception e) {
			}
		}

	}

}
