package i5.las2peer.services.ocd.algorithms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import i5.las2peer.services.ocd.algorithms.mea.MeaAlgorithm;
import i5.las2peer.services.ocd.automatedtesting.helpers.PathResolver;
import org.apache.commons.exec.DefaultExecutor;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

/**
 * Implements the algorithm by C. Liu, J. Liu, and Z. Jiang:
 * A multiobjective evolutionary algorithm based on similarity for community detection from signed social networks
 * https://doi.org/10.1109/TCYB.2014.2305974
 * @author YLi
 */
//TODO: Rework this algorithm implementation so that it doesn't need to produce pajek files anymore, they are an unnecessary extra step
public class EvolutionaryAlgorithmBasedOnSimilarity implements OcdAlgorithm {
	/**
	 * Path of the directory reserved for the application.
	 */
	public static final String DirectoryPath = PathResolver.resolvePath("ocd/mea") + File.separator;
	/**
	 * Used for synchronization purposes. Executes the application execution.
	 */
	private static DefaultExecutor executor = new DefaultExecutor();

	/**
	 * Path of paj file.
	 */
	private static final String graphPath = DirectoryPath + "network.paj";
	private static final String graphName = "network.paj";
	/**
	 * Path of the output of last generation.
	 */
	private static String LastResultPath = DirectoryPath + "network.paj.0099.pop";

	private int minNodeIndex = 0;

	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.EVOLUTIONARY_ALGORITHM_BASED_ON_SIMILARITY;
	}

	@Override
	public void setParameters(Map<String, String> parameters) {
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Map<String, String> getParameters() {
		return new HashMap<String, String>();
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.NEGATIVE_WEIGHTS);
		compatibilities.add(GraphType.WEIGHTED);
		compatibilities.add(GraphType.DIRECTED);
		return compatibilities;
	}

	/**
	 * Creates a standard instance of the algorithm. All attributes are assigned
	 * their default values.
	 */
	public EvolutionaryAlgorithmBasedOnSimilarity() {
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException, InterruptedException {
		synchronized (executor) {
			try {

				/*
				 * The source code of this algorithm requires the first node of
				 * the network file to have the index 1. If not, the input file
				 * should be adapted.
				 */
				Iterator<Node> nodes = graph.iterator();
				Node node;
				int count = 0;
				while (nodes.hasNext()) {
					node = nodes.next();
					if (count == 0) {
						minNodeIndex = node.getIndex();
					} else {
						if (node.getIndex() < minNodeIndex) {
							minNodeIndex = node.getIndex();
						}
					}
					count++;
				}
				writeNetworkFile(graph);



				try {
					MeaAlgorithm.executeMEA(graphPath, graphName);
				}catch(Exception e){}


				int nodeCount = graph.getNodeCount();
				Matrix membershipMatrix = translateCommunityFile(LastResultPath, nodeCount);
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

	/** 
	 * translate graph into paj file
	 * 
	 * @param graph the examined graph
	 * @throws IOException if the reading of the file failed
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected void writeNetworkFile(CustomGraph graph) throws IOException, InterruptedException {
		FileWriter networkFile = new FileWriter(graphPath);
		try {
			networkFile.write(String.format("*Vertices "));
			networkFile.write("\t");
			networkFile.write(Integer.toString(graph.getNodeCount()));
			networkFile.write(System.lineSeparator());
			networkFile.write(String.format("*Edges"));
			networkFile.write(System.lineSeparator());
			Iterator<Edge> edges = graph.edges().iterator();
			Edge edge;
			while (edges.hasNext()) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				edge = edges.next();
				if (edge.getSourceNode().getIndex() <= edge.getTargetNode().getIndex()) {
					networkFile.write(Integer.toString(edge.getSourceNode().getIndex() + (1 - minNodeIndex)));// networkFile.write(Integer.toString(edge.getSourceNode().getIndex()+1));
					networkFile.write("\t");
					networkFile.write(Integer.toString(edge.getTargetNode().getIndex() + (1 - minNodeIndex)));// networkFile.write(Integer.toString(edge.getSourceNode().getIndex()+1));
					networkFile.write("\t");
					networkFile.write(Double.toString(graph.getEdgeWeight(edge)));
					networkFile.write(System.lineSeparator());
				}
			}
		} finally {
			networkFile.close();
		}
	}

	/**
	 * Transform the results written by the executable file into the membership matrix.
	 * @param LastResultPath the path of the last result
	 * @param nodeCount the number of nodes for the matrix
	 * @return matrix the membership matrix
	 * @throws IOException if the file reading failed
	 * @throws InterruptedException if the thread was interrupted
	 */
	protected Matrix translateCommunityFile(String LastResultPath, int nodeCount)
			throws IOException, InterruptedException {
		File resultFile = new File(LastResultPath);
		Scanner communityResult = new Scanner(resultFile);
		List<List<Integer>> membership = new ArrayList<List<Integer>>();
		Map<Integer, List<Integer>> membershipTranspose = new HashMap<Integer, List<Integer>>();
		Integer communitySize;
		try {
			while (communityResult.hasNext()) {
				if (communityResult.next().equals("#community")) {
					communityResult.next();
					communityResult.next();
					communitySize = communityResult.nextInt();
					List<Integer> memberList = new ArrayList<Integer>();
					memberList.clear();
					for (int i = 0; i < communitySize; i++) {

						memberList.add(communityResult.nextInt());

					}
					membership.add(memberList);
				}

			}
			int communityCount = membership.size();
			for (int i = 0; i < communityCount; i++) {
				Integer community = i;
				for (Integer element : membership.get(i)) {

					List<Integer> communityList = new ArrayList<Integer>();
					if (membershipTranspose.keySet().contains(element)) {
						membershipTranspose.get(element).add(community);
					} else {
						communityList.add(community);
						membershipTranspose.put(element, communityList);
					}
				}
			}
			Matrix membershipMatrix = new CCSMatrix(nodeCount, membership.size());
			for (int i = 0; i < nodeCount; i++) {
				double entryInTheLine = (double) 1 / (double) membershipTranspose.get(i).size();
				for (int j = 0; j < membershipTranspose.get(i).size(); j++) {
					membershipMatrix.set(i, membershipTranspose.get(i).get(j), entryInTheLine);
				}
			}
			return membershipMatrix;
		}

		catch (Exception e) {
			throw new IOException(e);
		} finally {
			try {
				communityResult.close();
			} catch (Exception e) {
			}
		}
	}
}
