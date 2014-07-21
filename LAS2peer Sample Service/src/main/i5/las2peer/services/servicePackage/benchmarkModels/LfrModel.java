package i5.las2peer.services.servicePackage.benchmarkModels;

import i5.las2peer.services.servicePackage.adapters.coverInput.CoverInputAdapter;
import i5.las2peer.services.servicePackage.adapters.coverInput.NodeCommunityListInputAdapter;
import i5.las2peer.services.servicePackage.adapters.graphInput.GraphInputAdapter;
import i5.las2peer.services.servicePackage.adapters.graphInput.WeightedEdgeListGraphInputAdapter;
import i5.las2peer.services.servicePackage.algorithms.Algorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;

public class LfrModel implements GroundTruthBenchmarkModel {

	private static Object lfrCalculatorLock = new Object();
	
	@Override
	public Cover createGroundTruthCover() throws BenchmarkException {
		synchronized (lfrCalculatorLock) {
			//////////////7 TODO remove output if possible
			byte[] buffer = new byte[10000000];
			InputStream output = new ByteArrayInputStream(buffer);
			try {
				/*CommandLine cmdLine = new CommandLine("bin\\benchmark.exe");
				cmdLine.addArgument("-N 1000");
				cmdLine.addArgument("-k 15");
				cmdLine.addArgument("-maxk 50");
				cmdLine.addArgument("-muw 0.1");
				cmdLine.addArgument("-minc 20");
				cmdLine.addArgument("-maxc 50");*/
				CommandLine cmdLine = new CommandLine("bin\\benchmark.exe");
				cmdLine.addArgument("-N");
				cmdLine.addArgument("1000");
				cmdLine.addArgument("-k");
				cmdLine.addArgument("15");
				cmdLine.addArgument("-maxk");
				cmdLine.addArgument("50");
				cmdLine.addArgument("-muw");
				cmdLine.addArgument("0.1");
				cmdLine.addArgument("-minc");
				cmdLine.addArgument("20");
				cmdLine.addArgument("-maxc");
				cmdLine.addArgument("50");
				//CommandLine cmdLine = new CommandLine("'bin\\benchmark.exe' '-N 1000' '-k 15' '-maxk 50' '-muw 0.1' '-minc 20' '-maxc 50'");
				DefaultExecutor executor = new DefaultExecutor();
				File workingDirectory = new File("bin");
				executor.setWorkingDirectory(workingDirectory);
				ExecuteWatchdog watchdog = new ExecuteWatchdog(20000);
				executor.setWatchdog(watchdog);
				ExecuteStreamHandler streamHandler = executor.getStreamHandler();
				streamHandler.setProcessOutputStream(output);
				DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
				executor.execute(cmdLine, resultHandler);
				resultHandler.waitFor();
				GraphInputAdapter graphAdapter = new WeightedEdgeListGraphInputAdapter("tmp\\network.dat");
				CustomGraph graph = graphAdapter.readGraph();
				CoverInputAdapter coverAdapter = new NodeCommunityListInputAdapter("tmp\\community.dat");
				return coverAdapter.readCover(graph, Algorithm.UNDEFINED);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new BenchmarkException();
			}
			/////////77 TODO remove output if possible
			finally {
				InputStreamReader reader = new InputStreamReader(output);
				try {
					while(reader.ready()) {
						System.out.print(reader.read());
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {
					try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

}
