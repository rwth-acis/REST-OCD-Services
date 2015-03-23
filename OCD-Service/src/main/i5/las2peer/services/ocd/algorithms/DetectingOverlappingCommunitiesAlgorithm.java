package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.coverInput.CommunityMemberListsCoverInputAdapter;
import i5.las2peer.services.ocd.adapters.coverInput.CoverInputAdapterFactory;
import i5.las2peer.services.ocd.adapters.coverInput.CoverInputFormat;
import i5.las2peer.services.ocd.adapters.graphOutput.DocaGraphOutputAdapter;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputAdapter;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.benchmarks.OcdBenchmarkException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang3.SystemUtils;

public class DetectingOverlappingCommunitiesAlgorithm implements OcdAlgorithm {

	/*
	 * Path of the directory reserved for the doca application. 
	 */
	private static final String docaDirectoryPath = "ocd/doca/";
	/*
	 * Used for synchronization purposes. Executes the algorithm.
	 */
	private static DefaultExecutor executor = new DefaultExecutor();
	/* Path of the file holding an application that executes the DOCA algorithm. For Windows.
	 */
	private static String windowsAlgorithmPath = docaDirectoryPath + "DocaWindows.exe";
	/* Path of the file holding an application that executes the DOCA algorithm. For Linux.
	 */
	private static String linuxAlgorithmPath = "./DocaLinux";
	/*
	 * Filename parameter passed to the Doca Application.
	 */
	private static final String docaGraphPath = "network.txt";
	/*
	 * Path of the file containing the input graph.
	 */
	private static final String graphPath = docaDirectoryPath + docaGraphPath;
	/*
	 * Path of the file containing the output cover.
	 */
	private static final String coverPath = docaDirectoryPath + "cover.txt";
	/**
	 * The overlapping threshold.
	 * The default value is 0.75. Must be higher than 0 and lower than 2.
	 */
	private double overlappingThreshold = 0.75;
	
	/*
	 * PARAMETER NAMES
	 */
	private static String OVERLAPPING_THRESHOLD_NAME = "overlappingThreshold";
	
	@Override
	public void setParameters(Map<String, String> parameters) {
		if(parameters.containsKey(OVERLAPPING_THRESHOLD_NAME)) {
			overlappingThreshold = Double.parseDouble(parameters.get(OVERLAPPING_THRESHOLD_NAME));
			if(overlappingThreshold <= 0 || overlappingThreshold >= 2) {
				throw new IllegalArgumentException();
			}
			parameters.remove(OVERLAPPING_THRESHOLD_NAME);
		}
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(OVERLAPPING_THRESHOLD_NAME, Double.toString(overlappingThreshold));
		return parameters;
	}
	
	@Override
	public CoverCreationType getAlgorithmType() {
		/*
		 * TODO reinsert for use on Windows / when implemented for Linux
		 */
		// return CoverCreationType.DETECTING_OVERLAPPING_COMMUNITIES_ALGORITHM;
		return null;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		return compatibilities;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph)
			throws OcdAlgorithmException, InterruptedException {
		synchronized (executor) {
			try {
				CustomGraph graphCpy = new CustomGraph(graph);
				GraphOutputAdapter graphAdapter = new DocaGraphOutputAdapter();
				graphAdapter.setWriter(new FileWriter(graphPath));
				graphAdapter.writeGraph(graphCpy);
				String executorFilename;
				if(SystemUtils.IS_OS_WINDOWS) {
					executorFilename = windowsAlgorithmPath;
				}
				// TODO generate for linux
				/*
				else if(SystemUtils.IS_OS_LINUX) {
					executorFilename = linuxAlgorithmPath;
				}*/
				/*
				 * Algorithm not implemented for this operating system.
				 */
				else {
					throw new OcdBenchmarkException();
				}
				CommandLine cmdLine = new CommandLine(executorFilename);
				cmdLine.addArgument(docaGraphPath);
				cmdLine.addArgument(Double.toString(overlappingThreshold));
				File workingDirectory = new File(docaDirectoryPath);
				executor.setWorkingDirectory(workingDirectory);
				DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
				executor.execute(cmdLine, resultHandler);
				resultHandler.waitFor();
				// DOCA defines 1 as OK
				if(resultHandler.getExitValue() != 1) {
					System.out.println(resultHandler.getException());
					throw new OcdAlgorithmException("Doca Process exit value: " + resultHandler.getExitValue());
				}	
				CoverInputAdapterFactory factory = new CoverInputAdapterFactory();				
				CommunityMemberListsCoverInputAdapter coverAdapter = (CommunityMemberListsCoverInputAdapter)factory.getInstance(CoverInputFormat.COMMUNITY_MEMBERS_LISTS);
				coverAdapter.setCommunityNamesDefined(false);
				coverAdapter.setReader(new FileReader(coverPath));
				Cover coverCpy = coverAdapter.readCover(graphCpy);
				Cover cover = new Cover(graph, coverCpy.getMemberships());
				return cover;
			}
			catch(InterruptedException e) {
				throw e;
			}
			catch (Exception e) {
				e.printStackTrace();
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				throw new OcdAlgorithmException(e);
			}
		}
	}

}
