package i5.las2peer.services.servicePackage.adapters.coverInput;

import i5.las2peer.services.servicePackage.adapters.Adapters;
import i5.las2peer.services.servicePackage.algorithms.Algorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.io.FileReader;
import java.io.Reader;
import java.util.List;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

public class LabeledMembershipMatrixInputAdapter extends AbstractCoverInputAdapter {

	@Override
	public Cover readCover(String filename, CustomGraph graph, Algorithm algorithm) throws Exception {
		Reader reader = null;
		try {
			reader = new FileReader(filename);
			List<String> line = Adapters.readLine(reader);
			Matrix memberships = new CCSMatrix(graph.nodeCount(), line.size() - 1);
			int nodeIndex = 0;
			double belongingFactor;
			while(line.size() > 0) {
				for(int i=1; i<line.size(); i++) {
					belongingFactor = Double.parseDouble(line.get(i));
					memberships.set(nodeIndex, i-1, belongingFactor);
				}
				nodeIndex++;
				line = Adapters.readLine(reader);
			}
			return new Cover(graph, memberships, algorithm);
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				reader.close();
			}
			catch (Exception e) {
			}
		}
	}

}
